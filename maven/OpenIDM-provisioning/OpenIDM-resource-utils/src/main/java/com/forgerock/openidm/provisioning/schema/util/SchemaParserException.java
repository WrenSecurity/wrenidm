package com.forgerock.openidm.provisioning.schema.util;

/**
 *
 * @author Vilo Repan
 */
public class SchemaParserException extends Exception {

    public SchemaParserException(String message) {
        super(message);
    }

    public SchemaParserException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
