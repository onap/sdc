/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import fj.data.Either;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.janusgraph.graphdb.types.system.EmptyVertex;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.facade.operations.UserOperation;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.impl.UserAdminOperation;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.be.user.UserBusinessLogicExt;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.dao.utils.UserStatusEnum.ACTIVE;
import static org.openecomp.sdc.be.dao.utils.UserStatusEnum.INACTIVE;
import static org.openecomp.sdc.be.user.Role.ADMIN;
import static org.openecomp.sdc.be.user.Role.DESIGNER;

public class UserEndpointTest extends JerseySpringBaseTest {

    static final String USER_ID = "jh0003";
    static final String NEW_USER_ID = "ab0001";
    static final String MODIFIER_ID = "admin1";

    private static ComponentsUtils componentUtils;
    private static JanusGraphGenericDao janusGraphGenericDao;
    private static ToscaOperationFacade toscaOperationFacade;
    private static LifecycleBusinessLogic lifecycleBusinessLogic;
    private static UserOperation facadeUserOperation;


    private UserData userData = new UserData();
    private UserData modifierData = new UserData();

    @org.springframework.context.annotation.Configuration
    @Import(BaseTestConfig.class)
    static class UserTestConfig {

        @Bean
        UserAdminServlet userEndpoint() {
            ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
            return new UserAdminServlet(userBusinessLogic(), componentsUtils, userBusinessLogicExt());
        }

        @Bean
        UserBusinessLogic userBusinessLogic() {
            return new UserBusinessLogic(userAdminOperation(), componentUtils, facadeUserOperation);
        }

        @Bean
        UserBusinessLogicExt userBusinessLogicExt() {
            return new UserBusinessLogicExt(userBusinessLogic(), userAdminOperation(), lifecycleBusinessLogic, componentUtils);
        }

        @Bean
        UserAdminOperation userAdminOperation() {
            return new UserAdminOperation(janusGraphGenericDao, toscaOperationFacade);
        }
    }

    @BeforeClass
    public static void initClass() {
        janusGraphGenericDao = mock(JanusGraphGenericDao.class);
        componentUtils = mock(ComponentsUtils.class);
        toscaOperationFacade = mock(ToscaOperationFacade.class);
        lifecycleBusinessLogic = mock(LifecycleBusinessLogic.class);
        facadeUserOperation = mock(UserOperation.class);
    }

    @Before
    public void setup() {
        setUserProperties(userData, USER_ID, DESIGNER, ACTIVE);
        setUserProperties(modifierData, MODIFIER_ID, ADMIN, ACTIVE);
        Either<UserData, JanusGraphOperationStatus> janusGraphValidUser = Either.left(userData);
        Either<UserData, JanusGraphOperationStatus> janusGraphValidModifier = Either.left(modifierData);
        when(janusGraphGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), USER_ID, UserData.class))
                .thenReturn(janusGraphValidUser);
        when(janusGraphGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), MODIFIER_ID, UserData.class))
                .thenReturn(janusGraphValidModifier);
        when(janusGraphGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), NEW_USER_ID, UserData.class))
                .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
    }

    private void setUserProperties(UserData user, String userId, Role role, UserStatusEnum statusEnum) {
        user.setUserId(userId);
        user.setRole(role.name());
        user.setStatus(statusEnum.name());
    }

    @Override
    protected void configureClient(ClientConfig config) {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        config.register(jacksonJsonProvider);
        config.register(MultiPartFeature.class);
    }

    @Override
    protected ResourceConfig configure() {
        return super.configure(UserEndpointTest.UserTestConfig.class)
                .register(UserAdminServlet.class)
                .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "WARNING");
    }

    @Test
    public void getUser_success() {
        User user = target().path("/v1/user/" + USER_ID)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID)
                .get(User.class);
        assertThat(user.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    public void getUserRole_success() {
        String result = target().path("/v1/user/" + USER_ID + "/role")
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, MODIFIER_ID)
                .get(String.class);
        assertThat(result).isEqualTo("{ \"role\" : \"" + DESIGNER.name() + "\" }");
    }

    @Test
    public void updateUserRole_success() {
        UserAdminServlet.UserRole role = new UserAdminServlet.UserRole();
        role.setRole(ADMIN);
        EmptyVertex emptyVertex = new EmptyVertex();
        UserData updatedUser = new UserData();
        setUserProperties(updatedUser, USER_ID, ADMIN, ACTIVE);
        when(janusGraphGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), USER_ID))
                .thenReturn(Either.left(emptyVertex));
        when(janusGraphGenericDao.getOutgoingEdgesByCriteria(eq(emptyVertex), eq(GraphEdgeLabels.STATE), any()))
                .thenReturn(Either.left(new ArrayList<>()));
        when(janusGraphGenericDao.updateNode(eq(updatedUser), eq(UserData.class))).thenReturn(Either.left(updatedUser));
        User user = target().path("/v1/user/" + USER_ID + "/role")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, MODIFIER_ID)
                .post(Entity.entity(role, MediaType.APPLICATION_JSON),User.class);
        assertThat(user.getRole()).isEqualTo(ADMIN.name());
    }

    @Test
    public void createUser_success() {
        User newUser = new User();
        newUser.setUserId(NEW_USER_ID);
        UserData updatedUser = new UserData();
        setUserProperties(updatedUser, NEW_USER_ID, DESIGNER, ACTIVE);
        //when(janusGraphGenericDao.updateNode(any(), eq(UserData.class))).thenReturn(Either.left(updatedUser));
        when(janusGraphGenericDao.createNode(any(), eq(UserData.class))).thenReturn(Either.left(updatedUser));
        Response response = target().path("/v1/user")
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, MODIFIER_ID)
                .post(Entity.entity(newUser, MediaType.APPLICATION_JSON),Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        User createdUser = response.readEntity(User.class);
        assertThat(createdUser.getUserId()).isEqualTo(NEW_USER_ID);
        assertThat(createdUser.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    public void authorizeUser_success() {
        when(janusGraphGenericDao.updateNode(any(), eq(UserData.class))).thenReturn(Either.left(userData));
        User user = target().path("/v1/user/authorize")
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, USER_ID)
                .header("HTTP_CSP_FIRSTNAME", "Jimmy")
                .header("HTTP_CSP_LASTNAME", "Hendrix")
                .header("HTTP_CSP_EMAIL", "admin@sdc.com")
                .get(User.class);
        assertThat(user.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    public void deactivateUser_success() {
        EmptyVertex emptyVertex = new EmptyVertex();
        UserData updatedUser = new UserData();
        setUserProperties(updatedUser, USER_ID, DESIGNER, INACTIVE);
        when(janusGraphGenericDao.getVertexByProperty(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), USER_ID))
                .thenReturn(Either.left(emptyVertex));
        when(janusGraphGenericDao.getOutgoingEdgesByCriteria(eq(emptyVertex), eq(GraphEdgeLabels.STATE), any()))
                .thenReturn(Either.left(new ArrayList<>()));
        when(janusGraphGenericDao.updateNode(eq(updatedUser), eq(UserData.class))).thenReturn(Either.left(updatedUser));
        User user = target().path("/v1/user/" + USER_ID)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, MODIFIER_ID)
                .delete(User.class);
        assertThat(user.getUserId()).isEqualTo(USER_ID);
    }

}
