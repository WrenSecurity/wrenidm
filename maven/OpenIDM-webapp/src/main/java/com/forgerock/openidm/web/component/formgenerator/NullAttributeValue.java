package com.forgerock.openidm.web.component.formgenerator;

import java.io.Serializable;

/**
 *
 * @author Vilo Repan
 */
public class NullAttributeValue implements Serializable {

    private String message;

    NullAttributeValue() {
    }

    NullAttributeValue(String message) {
        this.message = message;
    }

    String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "NullAttributeValue: " + message;
    }
}
