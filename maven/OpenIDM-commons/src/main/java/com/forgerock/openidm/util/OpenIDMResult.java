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
package com.forgerock.openidm.util;

import com.forgerock.openidm.xml.ns._public.common.common_1.OperationalResultType;
import java.util.UUID;
import javax.xml.namespace.QName;

/**
 * This needs to be a common object. It's a temporary solution.
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class OpenIDMResult {

    public static final String code_id = "$Id$";
    private OperationalResultType _currentOperationalResult;
    private OperationalResultType _parentOperationalResult;

    public OpenIDMResult() {
        _currentOperationalResult = new OperationalResultType();
        _currentOperationalResult.setOid(UUID.randomUUID().toString());
    }

    public OpenIDMResult(OperationalResultType result, QName operation) {
        _currentOperationalResult = new OperationalResultType();
        _currentOperationalResult.setOid(UUID.randomUUID().toString());
        _currentOperationalResult.setType(operation);
        _parentOperationalResult = result;
    }

    public void addNamedResult(String name, Object value) {
    }

    public void addException(Throwable t) {
    }

    public Object getResult(String type) {
        return "TEST";
    }

    public boolean hasError(){
        return false;
    }
}
