package com.forgerock.openidm.provisioning.converter;

import org.springframework.core.convert.converter.Converter;

/**
 * Factory to get converter instance.
 *
 * @author elek
 */
@Deprecated
public interface ConverterFactory {

    /**
     * Return with the appropriate converter.
     *
     *
     * @param <S>
     * @param <T>
     * @param targetClass
     * @param value
     * @return
     * @throws UnsupportedOperationException if no converter registerd
     */
    <S, T> Converter<S, T> getConverter(Class<T> targetClass, S value);
}
