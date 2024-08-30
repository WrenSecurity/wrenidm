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
import org.flowable.idm.api.Group;
import org.flowable.idm.api.GroupQuery;
import org.flowable.idm.api.NativeGroupQuery;
import org.flowable.idm.api.NativeTokenQuery;
import org.flowable.idm.api.NativeUserQuery;
import org.flowable.idm.api.Picture;
import org.flowable.idm.api.Privilege;
import org.flowable.idm.api.PrivilegeMapping;
import org.flowable.idm.api.PrivilegeQuery;
import org.flowable.idm.api.Token;
import org.flowable.idm.api.TokenQuery;
import org.flowable.idm.api.User;
import org.flowable.idm.api.UserQuery;
import org.flowable.idm.engine.IdmEngineConfiguration;
import org.flowable.idm.engine.impl.IdmIdentityServiceImpl;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.ConnectionFactory;
import org.forgerock.json.resource.ResourceException;

public class IdmIdentityService extends IdmIdentityServiceImpl {

    public static final String ID_ATTR = "_id";
    public static final String NAME_ATTR = "name";
    public static final String USERNAME_ATTR = "userName";
    public static final String GIVEN_NAME_ATTR = "givenName";
    public static final String SURNAME_ATTR = "sn";
    public static final String MAIL_ATTR = "mail";
    public static final String MEMBERS_ATTR = "members";

    private ConnectionFactory connectionFactory;

    public IdmIdentityService(ConnectionFactory connectionFactory, IdmEngineConfiguration idmEngineConfiguration) {
        super(idmEngineConfiguration);
        this.connectionFactory = connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public GroupQuery createGroupQuery() {
        return new IdmGroupQuery(getConnection());
    }

    @Override
    public void setAuthenticatedUserId(String userId) {
        Authentication.setAuthenticatedUserId(userId);
    }

    // User related unsupported methods

    @Override
    public UserQuery createUserQuery() {
        throw new UnsupportedOperationException("Creating user query is not supported.");
    }

    @Override
    public NativeUserQuery createNativeUserQuery() {
        throw new UnsupportedOperationException("Creating native user query is not supported.");
    }

    @Override
    public User newUser(String userId) {
        throw new UnsupportedOperationException("Creating user is not supported.");
    }

    @Override
    public void saveUser(User user) {
        throw new UnsupportedOperationException("Saving user is not supported.");
    }

    @Override
    public void updateUserPassword(User user) {
        throw new UnsupportedOperationException("Updating user password is not supported.");
    }

    @Override
    public void deleteUser(String userId) {
        throw new UnsupportedOperationException("Deleting user is not supported.");
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        throw new UnsupportedOperationException("Checking of user password is not supported.");
    }

    @Override
    public void setUserPicture(String userId, Picture picture) {
        throw new UnsupportedOperationException("Setting user picture is not supported.");
    }

    @Override
    public Picture getUserPicture(String userId) {
        throw new UnsupportedOperationException("Getting user picture is not supported.");
    }

    @Override
    public void setUserInfo(String userId, String key, String value) {
        throw new UnsupportedOperationException("Setting user info is not supported.");
    }

    @Override
    public String getUserInfo(String userId, String key) {
        throw new UnsupportedOperationException("Getting user info is not supported.");
    }

    @Override
    public List<String> getUserInfoKeys(String userId) {
        throw new UnsupportedOperationException("Getting user info keys is not supported.");
    }

    @Override
    public void deleteUserInfo(String userId, String key) {
        throw new UnsupportedOperationException("Deleting user info is not supported.");
    }

    // Group related unsupported methods

    @Override
    public Group newGroup(String groupId) {
        throw new UnsupportedOperationException("Creating group is not supported.");
    }

    @Override
    public NativeGroupQuery createNativeGroupQuery() {
        throw new UnsupportedOperationException("Creating native group query is not supported.");
    }

    @Override
    public void saveGroup(Group group) {
        throw new UnsupportedOperationException("Saving group is not supported.");
    }

    @Override
    public void deleteGroup(String groupId) {
        throw new UnsupportedOperationException("Deleting group is not supported.");
    }

    @Override
    public void createMembership(String userId, String groupId) {
        throw new UnsupportedOperationException("Creating membership is not supported.");
    }

    @Override
    public void deleteMembership(String userId, String groupId) {
        throw new UnsupportedOperationException("Deleting membership is not supported.");
    }

    // Privilege related unsupported methods

    @Override
    public Privilege createPrivilege(String name) {
        throw new UnsupportedOperationException("Creating privilege is not supported.");
    }

    @Override
    public void addUserPrivilegeMapping(String privilegeId, String userId) {
        throw new UnsupportedOperationException("Creating user privilege mapping is not supported.");
    }

    @Override
    public void deleteUserPrivilegeMapping(String privilegeId, String userId) {
        throw new UnsupportedOperationException("Deleting user privilege mapping is not supported.");
    }

    @Override
    public void addGroupPrivilegeMapping(String privilegeId, String groupId) {
        throw new UnsupportedOperationException("Creating group privilege mapping is not supported.");
    }

    @Override
    public void deleteGroupPrivilegeMapping(String privilegeId, String groupId) {
        throw new UnsupportedOperationException("Deleting group privilege mapping is not supported.");
    }

    @Override
    public List<PrivilegeMapping> getPrivilegeMappingsByPrivilegeId(String privilegeId) {
        throw new UnsupportedOperationException("Getting privilege mappings is not supported.");
    }

    @Override
    public void deletePrivilege(String id) {
        throw new UnsupportedOperationException("Deleting privilege is not supported.");
    }

    @Override
    public PrivilegeQuery createPrivilegeQuery() {
        throw new UnsupportedOperationException("Creating privilege query is not supported.");
    }

    @Override
    public List<Group> getGroupsWithPrivilege(String name) {
        throw new UnsupportedOperationException("Getting groups with privileges is not supported.");
    }

    @Override
    public List<User> getUsersWithPrivilege(String name) {
        throw new UnsupportedOperationException("Getting users with privileges is not supported.");
    }

    // Token related unsupported methods

    @Override
    public Token newToken(String tokenId) {
        throw new UnsupportedOperationException("Creating token is not supported.");
    }

    @Override
    public void saveToken(Token token) {
        throw new UnsupportedOperationException("Saving token is not supported.");
    }

    @Override
    public void deleteToken(String tokenId) {
        throw new UnsupportedOperationException("Deleting token is not supported.");
    }

    @Override
    public TokenQuery createTokenQuery() {
        throw new UnsupportedOperationException("Creating token query is not supported.");
    }

    @Override
    public NativeTokenQuery createNativeTokenQuery() {
        throw new UnsupportedOperationException("Creating native token query is not supported.");
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
