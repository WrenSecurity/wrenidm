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
package com.forgerock.openidm.web.component;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Vilo Repan
 */
@FacesValidator("PasswordValidator")
public class PasswordValidator implements Validator {

    private static transient Trace logger = TraceManager.getTrace(PasswordValidator.class);
    public static final String OTHER_COMPONENT_ID = "otherComponentId";

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null || !(value instanceof String)) {
            throw createMessage("", "");
        }
        String password1 = (String) value;

        String otherComponentId = (String) component.getAttributes().get(OTHER_COMPONENT_ID);
        UIInput comp = findComponent(context.getViewRoot(), otherComponentId);
        if (comp == null) {
            logger.warn("Can't find component with name '{}', Component with password validator doesn't " +
                    "have atttribute '{}' defined.", new Object[]{otherComponentId, OTHER_COMPONENT_ID});
            throw createMessage("Component not found.", "Component '" + otherComponentId +
                    "' not found, can't properly validate field.");
        }
        String password2 = (String) comp.getValue();

        boolean equal = password1 == null ? password2 == null : password1.equals(password2);
        if (!equal) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please check password fields.", "Passwords doesn't match.");

            throw new ValidatorException(message);
        }
    }

    private ValidatorException createMessage(String summary, String detail) {
        return new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
    }

    private UIInput findComponent(UIComponent parent, String id) {
        if (id.equals(parent.getId()) && (parent instanceof UIInput)) {
            return (UIInput) parent;
        }

        List<UIComponent> children = parent.getChildren();
        if (children != null && !children.isEmpty()) {
            for (UIComponent child : children) {
                UIInput input = findComponent(child, id);
                if (input != null) {
                    return input;
                }
            }
        }

        return null;
    }
}
