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
package com.forgerock.openidm.app.repository.impl;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.app.repository.*;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.web.security.User;
import com.forgerock.openidm.util.DOMUtil;
import com.forgerock.openidm.util.diff.CalculateXmlDiff;
import com.forgerock.openidm.util.diff.DiffException;
import com.forgerock.openidm.web.consumer.ModelService;
import com.forgerock.openidm.web.security.Credentials;
import com.forgerock.openidm.xml.ns._public.common.common_1.CredentialsType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectContainerType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectFactory;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectModificationType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ObjectType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PagingType;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.QueryType;
import com.forgerock.openidm.xml.ns._public.common.common_1.UserType;
import com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage;
import com.forgerock.openidm.xml.ns._public.model.model_1.ModelPortType;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
@Service
public class UserRepositoryImpl implements UserRepository {

    public static final String code_id = "$Id$";
    private static final Trace logger = TraceManager.getTrace(UserRepositoryImpl.class);

    @RolesAllowed(value = {"ROLE_USER"})
    @Override
    public void delete(UUID id) throws RepositoryException {
    }

    @Override
    public User save(User person) throws RepositoryException {
        try {
            UserType userType = getUser(person.getOid());
            updateUserType(userType, person);
            UserType oldUserType = getUser(person.getOid());
            ObjectFactory of = new ObjectFactory();
            ObjectContainerType userContainer = of.createObjectContainerType();
            userContainer.setObject(userType);

            ObjectModificationType modification = CalculateXmlDiff.calculateChanges(oldUserType, userType);
            if (modification != null && modification.getOid() != null) {
                getModel().modifyObject(modification);
            }
        } catch (com.forgerock.openidm.xml.ns._public.model.model_1.FaultMessage ex) {
            StringBuilder message = new StringBuilder();
            message.append("Can't save user, reason: ");
            if (ex.getFaultInfo() != null) {
                message.append(ex.getFaultInfo().getMessage());
            } else {
                message.append(ex.getMessage());
            }
            throw new RepositoryException(message.toString(), ex);
        } catch (DiffException ex) {
            throw new RepositoryException("Can't save user. Unexpected error: " +
                    "Couldn't create create diff.", ex);
        }

        return null;
    }

    @Override
    public int countAll() throws RepositoryException {
        return 0;
    }

    @RolesAllowed(value = {"ROLE_USER"})
    @Override
    public List<User> findAll(int firstResult, int maxResult) throws RepositoryException {
        return null;
    }

    @RolesAllowed(value = {"ROLE_USER"})
    @Override
    public User findById(UUID id) throws RepositoryException {
        return null;
    }

    @Override
    public User findByUsername(String username) throws RepositoryException {
        try {
            QueryType query = new QueryType();
            query.setFilter(createQuery(username));
            logger.trace("Looking for user, query:\n" + DOMUtil.printDom(query.getFilter()));

            ObjectListType list = getModel().searchObjects(query, new PagingType());
            if (list == null) {
                return null;
            }
            List<ObjectType> objects = list.getObject();
            logger.trace("Users found: {}.", new Object[]{objects.size()});
            if (objects.size() == 0 || objects.size() > 1) {
                return null;
            }

            return createUser((UserType) objects.get(0));
        } catch (FaultMessage ex) {

            throw new RepositoryException("Couldn't get user '" + username + "', reason: " + getMessage(ex), ex);
        }
    }

    private String getMessage(FaultMessage ex) {
        if (ex.getFaultInfo() != null) {
            return ex.getFaultInfo().getMessage();
        }

        return ex.getMessage();
    }

    private void updateUserType(UserType userType, User user) {
        CredentialsType credentials = userType.getCredentials();
        if (credentials == null) {
            credentials = new CredentialsType();
            userType.setCredentials(credentials);
        }
        CredentialsType.Password password = credentials.getPassword();
        if (password == null) {
            password = new CredentialsType.Password();
            credentials.setPassword(password);
        }

        password.setFailedLogins(new BigInteger(Integer.toString(user.getCredentials().getFailedLogins())));

        try {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(user.getCredentials().getLastFailedLoginAttempt());
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            password.setLastFailedLoginTimestamp(calendar);
        } catch (DatatypeConfigurationException ex) {
            logger.error("Can't save last failed login timestamp, reason: " + ex.getMessage());
        }
    }

    private UserType getUser(String oid) throws FaultMessage {
        ObjectContainerType container = getModel().getObject(oid, new PropertyReferenceListType());
        if (container != null && (container.getObject() instanceof UserType)) {
            return (UserType) container.getObject();
        }

        return null;
    }

    private User createUser(UserType userType) {
        boolean enabled = false;
        CredentialsType credentialsType = userType.getCredentials();
        if (credentialsType != null && credentialsType.isAllowedIdmGuiAccess() != null) {
            enabled = credentialsType.isAllowedIdmGuiAccess();
        }

        User user = new User(userType.getOid(), userType.getName(), enabled);
        user.setFamilyName(userType.getFamilyName());
        user.setFullName(userType.getFullName());
        user.setGivenName(userType.getGivenName());
        user.setHonorificPrefix(userType.getHonorificPrefix());
        user.setHonorificSuffix(userType.getHonorificSuffix());

        if (credentialsType != null && credentialsType.getPassword() != null) {
            CredentialsType.Password password = credentialsType.getPassword();

            Credentials credentials = user.getCredentials();
            Element pwd = getValue(password.getAny());
            credentials.setPassword(pwd.getTextContent(), pwd.getLocalName());
            if (password.getFailedLogins() == null || password.getFailedLogins().intValue() < 0) {
                credentials.setFailedLogins(0);
            } else {
                credentials.setFailedLogins(password.getFailedLogins().intValue());
            }
            XMLGregorianCalendar calendar = password.getLastFailedLoginTimestamp();
            if (calendar != null) {
                credentials.setLastFailedLoginAttempt(calendar.toGregorianCalendar().getTimeInMillis());
            } else {
                credentials.setLastFailedLoginAttempt(0);
            }
        }

        return user;
    }

    private Element getValue(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof Node) {
            Node node = (Node) object;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
        }

        return null;
    }

    private Element createQuery(String username) {
        Document document = DOMUtil.getDocument();
        Element and = document.createElementNS(SchemaConstants.NS_C, "c:and");
        document.appendChild(and);

        Element type = document.createElementNS(SchemaConstants.NS_C, "c:type");
        type.setAttribute("uri", "http://openidm.forgerock.com/xml/ns/public/common/common-1.xsd#UserType");
        and.appendChild(type);

        Element equal = document.createElementNS(SchemaConstants.NS_C, "c:equal");
        and.appendChild(equal);
        Element value = document.createElementNS(SchemaConstants.NS_C, "c:value");
        equal.appendChild(value);
        Element name = document.createElementNS(SchemaConstants.NS_C, "c:name");
        name.setTextContent(username);
        value.appendChild(name);

        return and;
    }

    private ModelPortType getModel() {
        ModelService service = new ModelService();
        return service.getModelPort();
    }
}
