/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2015 ForgeRock AS. All Rights Reserved
 * Portions Copyright 2018 Wren Security.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * Portions Copyright 2026 Wren Security.
 */
package org.forgerock.openidm.external.email.impl;

import org.eclipse.angus.mail.smtp.SMTPTransport;
import org.eclipse.angus.mail.util.MailSSLSocketFactory;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import jakarta.activation.DataHandler;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * Email client.
 */
public class EmailClient {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "25";
    private String username = null;
    private String password = null;
    private String fromAddr = null;
    private boolean smtpAuth = false;
    private Properties props = new Properties();
    private Session session;

    // Keys in the JSON configuration
    public static final String CONFIG_MAIL_SMTP_HOST = "host";
    public static final String CONFIG_MAIL_SMTP_PORT = "port";
    public static final String CONFIG_MAIL_SMTP_AUTH = "auth";
    public static final String CONFIG_MAIL_SMTP_AUTH_ENABLE = "enable";
    public static final String CONFIG_MAIL_SMTP_AUTH_PASSWORD = "password";
    public static final String CONFIG_MAIL_SMTP_AUTH_USERNAME = "username";
    public static final String CONFIG_MAIL_SMTP_STARTTLS = "starttls";
    public static final String CONFIG_MAIL_SMTP_STARTTLS_ENABLE = "enable";
    public static final String CONFIG_MAIL_FROM = "from";
    public static final String CONFIG_MAIL_DEBUG = "debug";

    public EmailClient(JsonValue config) throws RuntimeException {

        props.put("mail.smtp.host", config.get(CONFIG_MAIL_SMTP_HOST).defaultTo(DEFAULT_HOST).asString());
        props.put("mail.smtp.port", config.get(CONFIG_MAIL_SMTP_PORT).defaultTo(DEFAULT_PORT).asString());
        props.put("mail.debug", String.valueOf(config.get(CONFIG_MAIL_DEBUG).defaultTo(false).asBoolean()));

        JsonValue authConfig = config.get(CONFIG_MAIL_SMTP_AUTH);
        if (!authConfig.isNull()) {
            smtpAuth = authConfig.get(CONFIG_MAIL_SMTP_AUTH_ENABLE).defaultTo(false).asBoolean();
            if (smtpAuth) {
                username = authConfig.get(CONFIG_MAIL_SMTP_AUTH_USERNAME).required().asString();
                password = authConfig.get(CONFIG_MAIL_SMTP_AUTH_PASSWORD).required().asString();
            }
            props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        }

        JsonValue starttlsConfig = config.get(CONFIG_MAIL_SMTP_STARTTLS);
        boolean startTLS = starttlsConfig.get(CONFIG_MAIL_SMTP_STARTTLS_ENABLE).defaultTo(false).asBoolean();
        if (startTLS) {
            props.put("mail.smtp.starttls.enable", String.valueOf(startTLS));
            // temporary hack to avoid cert check
            try {
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                props.put("mail.smtp.ssl.socketFactory", sf);
            } catch (Exception e) {
            }
        }

        fromAddr = config.get(CONFIG_MAIL_FROM).asString();
        session = withAngusMailClassLoader(() -> Session.getInstance(props));
    }

    public void send(JsonValue params) throws BadRequestException {
        withAngusMailClassLoader(() -> {
            try {
                MimeMessage message = buildMessage(applyLegacyParams(params));
                Transport transport = session.getTransport("smtp");
                if (smtpAuth) {
                    transport.connect(username, password);
                } else {
                    transport.connect();
                }
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
                return null;
            } catch (MessagingException e) {
                throw new BadRequestException(e);
            }
        });
    }

    /**
     * Build MimeMessage instance according to the parameters in <em>params</em>.
     *
     * <p>Envelope fields:
     * <ul>
     *   <li>{@code from} - From: address (falls back to configured default)</li>
     *   <li>{@code to} - To: recipients, comma-separated</li>
     *   <li>{@code cc} - Cc: recipients, comma-separated</li>
     *   <li>{@code bcc} - Bcc: recipients, comma-separated</li>
     *   <li>{@code subject} - message subject</li>
     * </ul>
     *
     * <p>Body fields (semantic):
     * <ul>
     *   <li>{@code text} - plain-text body</li>
     *   <li>{@code html} - HTML body</li>
     *   <li>{@code attachments} - array of {@code {type, body (Base64), name}} objects</li>
     * </ul>
     * The correct multipart structure is chosen automatically based on which fields are present.
     *
     * @throws BadRequestException if required fields are absent or malformed.
     */
    protected MimeMessage buildMessage(JsonValue params) throws BadRequestException {
        InternetAddress from = null;
        InternetAddress[] to = null;
        InternetAddress[] cc = null;
        InternetAddress[] bcc = null;

        String subject = params.get("subject")
                .defaultTo(params.get("_subject"))
                .defaultTo("<no subject>")
                .asString();

        try {
            if (params.get("from").isNotNull()) {
                from = new InternetAddress(params.get("from").asString());
            } else if (params.get("_from").isNotNull()) {
                from = new InternetAddress(params.get("_from").asString());
            } else if (fromAddr != null) {
                from = new InternetAddress(fromAddr);
            } else {
                throw new BadRequestException("From: email address is absent");
            }
        } catch (AddressException ae) {
            throw new BadRequestException("Bad From: email address");
        }

        try {
            to = InternetAddress.parse(params.get("to").defaultTo(params.get("_to")).asString());
        } catch (AddressException ae) {
            throw new BadRequestException("Bad To: email address");
        }

        try {
            if (params.get("cc").isNotNull()) {
                cc = InternetAddress.parse(params.get("cc").asString());
            } else if (params.get("_cc").isNotNull()) {
                cc = InternetAddress.parse(params.get("_cc").asString());
            }
        } catch (AddressException ae) {
            throw new BadRequestException("Bad Cc: email address");
        }

        try {
            if (params.get("bcc").isNotNull()) {
                bcc = InternetAddress.parse(params.get("bcc").asString());
            } else if (params.get("_bcc").isNotNull()) {
                bcc = InternetAddress.parse(params.get("_bcc").asString());
            }
        } catch (AddressException ae) {
            throw new BadRequestException("Bad Bcc: email address");
        }

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(from);
            message.setRecipients(Message.RecipientType.TO, to);
            if (cc != null) {
                message.setRecipients(Message.RecipientType.CC, cc);
            }
            if (bcc != null) {
                message.setRecipients(Message.RecipientType.BCC, bcc);
            }
            message.setSubject(subject);
            setMessageContent(message, params);
            message.saveChanges();
            return message;
        } catch (MessagingException e) {
            throw new BadRequestException(e);
        }
    }

    private void setMessageContent(Message message, JsonValue params) throws MessagingException, BadRequestException {
        String text = params.get("text").asString();
        String html = params.get("html").asString();
        JsonValue attachments = params.get("attachments");
        if (attachments.size() == 0) {
            setPartContent(message, text, html);
        } else {
            MimeMultipart mixed = new MimeMultipart("mixed");
            MimeBodyPart bodyPart = new MimeBodyPart();
            setPartContent(bodyPart, text, html);
            mixed.addBodyPart(bodyPart);
            for (JsonValue attachment : attachments) {
                mixed.addBodyPart(buildAttachmentBodyPart(attachment));
            }
            message.setContent(mixed);
        }
    }

    private void setPartContent(Part part, String text, String html) throws MessagingException {
        if (text != null && html != null) {
            Multipart result = new MimeMultipart("alternative");
            BodyPart textPart = new MimeBodyPart();
            textPart.setContent(text, "text/plain; charset=UTF-8");
            result.addBodyPart(textPart);
            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(html, "text/html; charset=UTF-8");
            result.addBodyPart(htmlPart);
            part.setContent(result);
        } else if (text != null) {
            part.setContent(text, "text/plain; charset=UTF-8");
        } else if (html != null) {
            part.setContent(html, "text/html; charset=UTF-8");
        } else {
            part.setContent("", "text/plain; charset=UTF-8");
        }
    }

    private BodyPart buildAttachmentBodyPart(JsonValue attachment) throws MessagingException, BadRequestException {
        String type = attachment.get("type").required().asString();
        String content = attachment.get("content").required().asString();
        BodyPart bodyPart = new MimeBodyPart();
        byte[] bytes = Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8));
        bodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(bytes, type)));
        String name = attachment.get("name").asString();
        if (name != null) {
            bodyPart.setFileName(name);
        }
        return bodyPart;
    }

    /**
     * Translate legacy parameters to their current equivalents for backward compatibility.
     */
    private JsonValue applyLegacyParams(JsonValue params) {
        JsonValue result = params.copy();
        String body = result.get("body").defaultTo(result.get("_body")).asString();
        if (body == null) {
            return result;
        }
        String type = result.get("type").defaultTo(result.get("_type")).defaultTo("text/plain").asString();
        if (type.equalsIgnoreCase("text/html")) {
            result.put("html", body);
        } else {
            result.put("text", body);
        }
        return result;
    }

    /**
     * Runs {@code action} with the thread context classloader (TCCL) temporarily switched to the
     * angus-mail bundle classloader.
     *
     * <p>In OSGi, jakarta.mail resolves several services (e.g. {@code StreamProvider} used by
     * {@code Session}, and the {@code MailcapCommandMap} entries used by {@code Transport}) via
     * the TCCL. The TCCL on Felix SCR/DS and HTTP threads has no visibility into the angus-mail
     * bundle, so these lookups fail unless the TCCL is temporarily switched to a classloader that
     * can see angus-mail.
     * See https://github.com/eclipse-ee4j/angus-mail/issues/148
     */
    private static <T, E extends Exception> T withAngusMailClassLoader(ThrowingSupplier<T, E> action) throws E {
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SMTPTransport.class.getClassLoader());
        try {
            return action.get();
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T, E extends Exception> {
        T get() throws E;
    }

}
