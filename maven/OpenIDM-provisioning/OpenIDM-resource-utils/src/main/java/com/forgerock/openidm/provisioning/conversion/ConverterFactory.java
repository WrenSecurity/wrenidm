package com.forgerock.openidm.provisioning.conversion;

import javax.xml.namespace.QName;

/**
 * Factory to choose the right converter based on types.
 *
 * @author elek
 */
public interface ConverterFactory {

    public Converter getConverter(QName xmlType);

    public Converter getConverter(Class clazz);
}
