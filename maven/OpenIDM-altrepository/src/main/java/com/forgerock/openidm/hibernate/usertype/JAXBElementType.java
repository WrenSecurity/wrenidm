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
package com.forgerock.openidm.hibernate.usertype;

import com.forgerock.openidm.util.jaxb.JAXBUtil;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.xml.bind.JAXBElement;
import org.hibernate.Hibernate;

import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class JAXBElementType implements EnhancedUserType {

    public static final String code_id = "$Id$";
    private static final String CAST_EXCEPTION_TEXT = " cannot be cast to a java.util.UUID.";

    private static final int[] SQL_TYPES = {Types.LONGVARCHAR};
    /*private static final String DEFAULT_CLASS = "FinSomeCalss";
    private static final String PROP_CLASS_NAME = "class";
    private Class clazz = null;


    public void setParameterValues(Properties parameters) {
    String class_name = parameters.getProperty(PROP_CLASS_NAME, DEFAULT_CLASS);
    try {
    clazz = Class.forName(class_name);
    } catch (ClassNotFoundException e) {
    throw new HibernateException("JAXB class: " + class_name + " not found", e);
    }
    }
     */

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class returnedClass() {
        return JAXBElement.class;
    }

    @Override
    public String objectToSQLString(Object value) {
        String result = null;
        if (value instanceof String) {
            result = (String) value;
            result = result.substring(result.indexOf("?>") + 2);
        }
        return result;
    }

    @Override
    public String toXMLString(Object object) {
        if (null != object) {
            return null; //objectToSQLString(JAXBUtil.marshalle(object));
        }
        return null;
    }

    @Override
    public Object fromXMLString(String xml) {
        if (null != xml) {
            return null; //JAXBUtil.unmarshalle(xml);
        }
        return null;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if ((x == null) && (y == null)) {
            return true;
        }
        if ((x == null) || (y == null)) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        if (x == null) {
            throw new IllegalArgumentException(
                    " Parameter for hashCode must not be null");
        }
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
        Object result = null;
        String objectXML = (String) Hibernate.TEXT.nullSafeGet(resultSet, names[0]);
        if (!resultSet.wasNull()) {
            result = null == objectXML ? null : fromXMLString(objectXML);
        }
        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException {
        String xml = toXMLString(value);
        Hibernate.TEXT.nullSafeSet(statement, xml, index);
    }

    /**
     * Make deep copy by using Serialization or JAXB Serialization. This
     * Implementaion assume that Java serialization is faster than JAXB
     * Serialization. Serializable Objects will be copied using Serialization,
     * the other will be copied using JAXB binding
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return fromXMLString(toXMLString(value));
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    /**
     * Serialize as XML String using JAXB
     */
    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return toXMLString(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
