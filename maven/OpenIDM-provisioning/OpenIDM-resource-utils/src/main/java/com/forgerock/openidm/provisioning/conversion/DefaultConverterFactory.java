/*
 *
 * Copyright (c) 2010 ForgeRock Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1.php or
 * OpenIDM/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at OpenIDM/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted 2010 [name of copyright owner]"
 *
 * $Id$
 */
package com.forgerock.openidm.provisioning.conversion;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Converter between Java object class and XML representation.
 * 
 * @author elek
 */
public class DefaultConverterFactory implements ConverterFactory {

    private static DefaultConverterFactory conversion = new DefaultConverterFactory();

    public static DefaultConverterFactory getInstance() {
        return conversion;
    }
    private List<Converter> converters = new ArrayList<Converter>();

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public DefaultConverterFactory() {
        addConverter(new StringConverter());
        addConverter(new BooleanConverter());
        addConverter(new IntegerConverter());
        addConverter(new ActivationConverter());
        addConverter(new SyncTokenConverter());
    }


    @Override
    public Converter getConverter(QName xmlType) {
        for (Converter converter : converters) {
            if (converter.getXmlType().equals(xmlType)) {
                return converter;
            }
        }
        throw new IllegalArgumentException("Unsupported conversion type " + xmlType);
    }

    @Override
    public Converter getConverter(Class clazz) {
        for (Converter converter : converters) {
            if (converter.getJavaTypes().contains(clazz)) {
                return converter;
            }
        }
        throw new IllegalArgumentException("Unsupported conversion type " + clazz);
    }

    public void addConverter(Converter converter){
        converters.add(converter);
    }

    public List<Converter> getConverters() {
        return converters;
    }

    
}
