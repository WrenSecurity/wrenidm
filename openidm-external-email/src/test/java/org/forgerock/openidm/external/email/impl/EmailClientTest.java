/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2026 Wren Security.
 */
package org.forgerock.openidm.external.email.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.array;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.forgerock.json.JsonValueException;
import org.forgerock.json.resource.BadRequestException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Base64;

public class EmailClientTest {

    private static final String FROM = "sender@example.com";
    private static final String TO = "recipient@example.com";
    private static final String ATTACHMENT_BASE64 =
            Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3, 4, 5 });

    private EmailClient client;

    @BeforeClass
    public void setUp() {
        client = new EmailClient(json(object(field("from", FROM))));
    }

    @Test
    public void textOnlyProducesPlainTextMessage() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("text", "Hello plain text")
        )));

        assertThat(message.getContentType()).startsWith("text/plain");
        assertThat(message.getContent()).isEqualTo("Hello plain text");
    }

    @Test
    public void htmlOnlyProducesHtmlMessage() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("html", "<b>Hello</b>")
        )));

        assertThat(message.getContentType()).startsWith("text/html");
        assertThat(message.getContent()).isEqualTo("<b>Hello</b>");
    }

    @Test
    public void textAndHtmlProducesMultipartAlternative() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("text", "Hello"),
                field("html", "<b>Hello</b>")
        )));

        assertThat(message.getContentType()).startsWith("multipart/alternative");
        MimeMultipart body = (MimeMultipart) message.getContent();
        assertThat(body.getCount()).isEqualTo(2);
        assertThat(body.getBodyPart(0).getContentType()).startsWith("text/plain");
        assertThat(body.getBodyPart(0).getContent()).isEqualTo("Hello");
        assertThat(body.getBodyPart(1).getContentType()).startsWith("text/html");
        assertThat(body.getBodyPart(1).getContent()).isEqualTo("<b>Hello</b>");
    }

    @Test
    public void textWithAttachmentProducesMultipartMixed() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("text", "See attached"),
                field("attachments", array(object(
                        field("type", "application/pdf"),
                        field("content", ATTACHMENT_BASE64),
                        field("name", "doc.pdf")
                )))
        )));

        assertThat(message.getContentType()).startsWith("multipart/mixed");
        MimeMultipart mixed = (MimeMultipart) message.getContent();
        assertThat(mixed.getCount()).isEqualTo(2);
        assertThat(mixed.getBodyPart(0).getContentType()).startsWith("text/plain");
        assertThat(mixed.getBodyPart(0).getContent()).isEqualTo("See attached");
        assertThat(mixed.getBodyPart(1).getContentType()).startsWith("application/pdf");
        assertThat(mixed.getBodyPart(1).getFileName()).isEqualTo("doc.pdf");
    }

    @Test
    public void htmlWithAttachmentProducesMultipartMixed() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("html", "<b>See attached</b>"),
                field("attachments", array(object(
                        field("type", "application/pdf"),
                        field("content", ATTACHMENT_BASE64),
                        field("name", "doc.pdf")
                )))
        )));

        assertThat(message.getContentType()).startsWith("multipart/mixed");
        MimeMultipart mixed = (MimeMultipart) message.getContent();
        assertThat(mixed.getCount()).isEqualTo(2);
        assertThat(mixed.getBodyPart(0).getContentType()).startsWith("text/html");
        assertThat(mixed.getBodyPart(0).getContent()).isEqualTo("<b>See attached</b>");
        assertThat(mixed.getBodyPart(1).getContentType()).startsWith("application/pdf");
        assertThat(mixed.getBodyPart(1).getFileName()).isEqualTo("doc.pdf");
    }

    @Test
    public void textAndHtmlWithAttachmentProducesNestedStructure() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("text", "Hello"),
                field("html", "<b>Hello</b>"),
                field("attachments", array(object(
                        field("type", "application/pdf"),
                        field("content", ATTACHMENT_BASE64),
                        field("name", "doc.pdf")
                )))
        )));

        assertThat(message.getContentType()).startsWith("multipart/mixed");
        MimeMultipart mixed = (MimeMultipart) message.getContent();
        assertThat(mixed.getCount()).isEqualTo(2);
        assertThat(mixed.getBodyPart(0).getContentType()).startsWith("multipart/alternative");
        MimeMultipart alternative = (MimeMultipart) mixed.getBodyPart(0).getContent();
        assertThat(alternative.getCount()).isEqualTo(2);
        assertThat(alternative.getBodyPart(0).getContentType()).startsWith("text/plain");
        assertThat(alternative.getBodyPart(1).getContentType()).startsWith("text/html");
        assertThat(mixed.getBodyPart(1).getContentType()).startsWith("application/pdf");
        assertThat(mixed.getBodyPart(1).getFileName()).isEqualTo("doc.pdf");
    }

    @Test
    public void multipleAttachmentsAreAllAdded() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("text", "See attached"),
                field("attachments", array(
                        object(field("type", "application/pdf"), field("content", ATTACHMENT_BASE64), field("name", "first.pdf")),
                        object(field("type", "image/png"), field("content", ATTACHMENT_BASE64), field("name", "second.png"))
                ))
        )));

        MimeMultipart mixed = (MimeMultipart) message.getContent();
        assertThat(mixed.getCount()).isEqualTo(3);
        assertThat(mixed.getBodyPart(1).getFileName()).isEqualTo("first.pdf");
        assertThat(mixed.getBodyPart(2).getFileName()).isEqualTo("second.png");
    }

    @Test
    public void noBodyFieldsProducesEmptyTextPlain() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO)
        )));

        assertThat(message.getContentType()).startsWith("text/plain");
        assertThat(message.getContent()).isEqualTo("");
    }

    @Test
    public void subjectDefaultsWhenAbsent() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("text", "Hello")
        )));

        assertThat(message.getSubject()).isEqualTo("<no subject>");
    }

    @Test
    public void configuredFromIsUsedWhenFromAbsentInParams() throws Exception {
        MimeMessage message = client.buildMessage(json(object(
                field("to", TO),
                field("text", "Hello")
        )));

        assertThat(message.getFrom()[0].toString()).isEqualTo(FROM);
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void missingFromWithNoDefaultThrows() throws Exception {
        EmailClient clientNoDefault = new EmailClient(json(object()));
        clientNoDefault.buildMessage(json(object(
                field("to", TO),
                field("text", "Hello")
        )));
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void emptyFromAddressThrows() throws Exception {
        client.buildMessage(json(object(
                field("from", ""),
                field("to", TO),
                field("text", "Hello")
        )));
    }

    @Test(expectedExceptions = JsonValueException.class)
    public void attachmentWithMissingTypeThrows() throws Exception {
        client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("attachments", array(object(
                        field("content", ATTACHMENT_BASE64)
                )))
        )));
    }

    @Test(expectedExceptions = JsonValueException.class)
    public void attachmentWithMissingBodyThrows() throws Exception {
        client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("attachments", array(object(
                        field("type", "application/pdf")
                )))
        )));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void attachmentWithInvalidBase64Throws() throws Exception {
        client.buildMessage(json(object(
                field("from", FROM),
                field("to", TO),
                field("attachments", array(object(
                        field("type", "application/pdf"),
                        field("content", "not valid base64!!!")
                )))
        )));
    }
}
