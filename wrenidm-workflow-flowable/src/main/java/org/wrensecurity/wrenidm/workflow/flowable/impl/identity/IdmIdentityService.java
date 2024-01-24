/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License
 * for the specific language governing permission and limitations under the
 * License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each
 * file and include the License file at legal/CDDLv1.0.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2012-2015 ForgeRock AS.
 * Portions Copyright 2018-2024 Wren Security.
 */
package org.wrensecurity.wrenidm.workflow.flowable.impl.identity;

import java.util.List;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.IdentityService;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.GroupQuery;
import org.flowable.idm.api.NativeGroupQuery;
import org.flowable.idm.api.NativeUserQuery;
import org.flowable.idm.api.Picture;
import org.flowable.idm.api.User;
import org.flowable.idm.api.UserQuery;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.ConnectionFactory;
import org.forgerock.json.resource.ResourceException;

public class IdmIdentityService implements IdentityService {

    public static final String ID_ATTR = "id";
    public static final String NAME_ATTR = "name";
    public static final String USERNAME_ATTR = "userName";
    public static final String GIVEN_NAME_ATTR = "givenName";
    public static final String SURNAME_ATTR = "sn";
    public static final String MAIL_ATTR = "mail";

    private ConnectionFactory connectionFactory;

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public UserQuery createUserQuery() {
        return new IdmUserQuery(getConnection());
    }

    @Override
    public GroupQuery createGroupQuery() {
        return new IdmGroupQuery(getConnection());
    }

    @Override
    public void setAuthenticatedUserId(String userId) {
        Authentication.setAuthenticatedUserId(userId);
    }

    @Override
    public NativeUserQuery createNativeUserQuery() {
        throw new UnsupportedOperationException("Native user query is not supported.");
    }

    @Override
    public User newUser(String userId) {
        throw new UnsupportedOperationException("User creation is not supported.");
    }

    @Override
    public void saveUser(User user) {
        throw new UnsupportedOperationException("User creation is not supported.");
    }

    @Override
    public void updateUserPassword(User user) {
        throw new UnsupportedOperationException("User password update is not supported.");
    }

    @Override
    public void deleteUser(String userId) {
        throw new UnsupportedOperationException("User deletion is not supported.");
    }

    @Override
    public Group newGroup(String groupId) {
        throw new UnsupportedOperationException("Group creation is not supported.");
    }

    @Override
    public NativeGroupQuery createNativeGroupQuery() {
        throw new UnsupportedOperationException("Native group query is not supported.");
    }

    @Override
    public List<Group> getPotentialStarterGroups(String processDefinitionId) {
        throw new UnsupportedOperationException("Getting of potential starter groups is not supported.");
    }

    @Override
    public List<User> getPotentialStarterUsers(String processDefinitionId) {
        throw new UnsupportedOperationException("Getting of potential starter users is not supported.");
    }

    @Override
    public void saveGroup(Group group) {
        throw new UnsupportedOperationException("Group creation is not supported.");
    }

    @Override
    public void deleteGroup(String groupId) {
        throw new UnsupportedOperationException("Group deletion is not supported.");
    }

    @Override
    public void createMembership(String userId, String groupId) {
        throw new UnsupportedOperationException("Membership creation is not supported.");
    }

    @Override
    public void deleteMembership(String userId, String groupId) {
        throw new UnsupportedOperationException("Membership deletion is not supported.");
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        throw new UnsupportedOperationException("Checking of user password is not supported.");
    }

    @Override
    public void setUserPicture(String userId, Picture picture) {
        throw new UnsupportedOperationException("Setting of user picture is not supported.");
    }

    @Override
    public Picture getUserPicture(String userId) {
        throw new UnsupportedOperationException("Getting of user picture is not supported.");
    }

    @Override
    public void setUserInfo(String userId, String key, String value) {
        throw new UnsupportedOperationException("Setting of user info is not supported.");
    }

    @Override
    public String getUserInfo(String userId, String key) {
        throw new UnsupportedOperationException("Getting of user info is not supported.");
    }

    @Override
    public List<String> getUserInfoKeys(String userId) {
        throw new UnsupportedOperationException("Getting of user info keys is not supported.");
    }

    @Override
    public void deleteUserInfo(String userId, String key) {
        throw new UnsupportedOperationException("Deleting of user info is not supported.");
    }

    /**
     * Get connection to perform IdM-related queries.
     *
     * @return {@link Connection} instance.
     */
    private Connection getConnection() {
        try {
            return connectionFactory.getConnection();
        } catch (ResourceException e) {
            throw new IllegalStateException("Failed to get connection.", e);
        }
    }

}
