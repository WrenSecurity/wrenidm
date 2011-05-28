package com.forgerock.openidm.provisioning.converter;

import com.forgerock.openidm.api.exceptions.OpenIDMException;

/**
 * Exception if converter not found.
 *
 * @author elek
 */
public class NoSuchConverterException extends OpenIDMException {

    public NoSuchConverterException() {
    }

    public NoSuchConverterException(String message) {
        super(message);
    }

    public NoSuchConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchConverterException(Throwable cause) {
        super(cause);
    }
}
