package com.forgerock.openidm.provisioning.integration.identityconnector.converter;

import com.forgerock.openidm.provisioning.converter.BasicConverterFactory;
import com.forgerock.openidm.provisioning.converter.CompositeConverterFactory;
import com.forgerock.openidm.provisioning.converter.DefaultConverterFactory;

/**
 * ICF specific converters.
 *
 * @author elek
 */
public class ICFConverterFactory extends CompositeConverterFactory {

    private static ICFConverterFactory instance = new ICFConverterFactory();

    private ICFConverterFactory() {
        try {
            addConverterFactory(DefaultConverterFactory.getInstace());
            BasicConverterFactory icfConverters = new BasicConverterFactory();
            icfConverters.registerConverter(new StringToGuardedStringConverter());
            icfConverters.registerConverter(new GuardedStringToStringConverter());
            addConverterFactory(icfConverters);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public static ICFConverterFactory getInstance() {
        return instance;
    }
}
