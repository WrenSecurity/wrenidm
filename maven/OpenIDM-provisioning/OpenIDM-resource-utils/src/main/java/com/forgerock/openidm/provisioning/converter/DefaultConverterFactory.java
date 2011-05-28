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
package com.forgerock.openidm.provisioning.converter;

/**
 * Returns the converter based on a type.
 * org.springframework.core.convert.converter.Converter
 * http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/validation.html#core-convert
 *
 * @author elek
 */
public class DefaultConverterFactory extends BasicConverterFactory {

    public static final String code_id = "$Id$";

    private static DefaultConverterFactory instance = new DefaultConverterFactory();
    
    private DefaultConverterFactory() {
        addConverter(Pair.of(String.class, int.class), new StringToIntegerConverter());
        addConverter(Pair.of(String.class, boolean.class), new StringToBooleanConverter());
        registerConverter(new StringToBooleanConverter());
        registerConverter(new StringToFileConverter());
        registerConverter(new StringToIntegerConverter());
        registerConverter(new StringToStringConverter());
    }

    public static DefaultConverterFactory getInstace(){
        return instance;

    }

    
    
}
