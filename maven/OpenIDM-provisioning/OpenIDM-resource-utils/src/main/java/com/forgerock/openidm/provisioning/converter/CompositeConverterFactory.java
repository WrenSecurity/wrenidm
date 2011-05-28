package com.forgerock.openidm.provisioning.converter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.core.convert.converter.Converter;

/**
 * Proxy the request to the first matching factory.
 *
 * @author elek
 */
public class CompositeConverterFactory extends BasicConverterFactory {

    private List<ConverterFactory> factories = new ArrayList<ConverterFactory>();

    @Override
    public <S, T> Converter<S, T> getConverter(Class<T> targetClass, S value) {
        for (ConverterFactory factory : factories) {
            try {
                Converter converter = factory.getConverter(targetClass, value);
                return converter;
            } catch (NoSuchConverterException ex) {
                //noop, try the next factory
            }
        }
        throw new NoSuchConverterException("Converter not found for class " + targetClass);
    }

    public void addConverterFactory(ConverterFactory factory){
        factories.add(factory);

    }
}
