/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.externalapi.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ExternalRefsBusinessLogic;
import org.openecomp.sdc.be.components.path.utils.GraphTestUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.dto.ExternalRefDTO;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.CategoryOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ExternalReferencesOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.GroupsOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.IdMapper;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.WebApplicationContext;

import fj.data.Either;

public class ExternalRefServletTest extends JerseyTest {

    private static ConfigurationManager configurationManager;
    static String serviceVertexUuid;
    private static final HttpSession session = Mockito.mock(HttpSession.class);
    private static final ServletContext servletContext = Mockito.mock(ServletContext.class);

    public static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    private static final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
    private static final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
    private static final UserBusinessLogic userAdmin = Mockito.mock(UserBusinessLogic.class);
    private static final ComponentsUtils componentUtils = Mockito.mock(ComponentsUtils.class);
    private static final ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);
    private static final ResponseFormat notFoundResponseFormat = Mockito.mock(ResponseFormat.class);
    private static final ResponseFormat badRequestResponseFormat = Mockito.mock(ResponseFormat.class);
    private static final ToscaOperationFacade toscaOperationFacadeMock = Mockito.mock(ToscaOperationFacade.class);
    private static final GraphLockOperation graphLockOperation = Mockito.mock(GraphLockOperation.class);
    private static final TitanGenericDao titanGenericDao = Mockito.mock(TitanGenericDao.class);
    private static final ICacheMangerOperation cacheManagerOperation = Mockito.mock(ICacheMangerOperation.class);

    private static final String COMPONENT_ID = "ci-MyComponentName";

    private static final String FAKE_COMPONENT_ID = "ci-MyFAKEComponentName";
    private static final String MONITORING_OBJECT_TYPE = "monitoring";
    private static final String WORKFLOW_OBJECT_TYPE = "workflow";
    private static final String VERSION = "0.1";
    private static final String FAKE_VERSION = "0.5";
    private static final String REF_1 = "ref1";
    private static final String REF_2 = "ref2";
    private static final String REF_3 = "ref3";
    private static final String REF_4 = "ref4";
    private static final String REF_5 = "ref5";
    //workflow
    private static final String REF_6 = "ref6";

    @Configuration
    static class TestSpringConfig {

        private GraphVertex serviceVertex;
        private ExternalReferencesOperation externalReferenceOperation;
        private TitanDao titanDao;

        @Bean
        ExternalRefsServlet externalRefsServlet(){
            return new ExternalRefsServlet(externalRefsBusinessLogic(), componentUtils);
        }

        @Bean
        ExternalRefsBusinessLogic externalRefsBusinessLogic() {
            return new ExternalRefsBusinessLogic(externalReferencesOperation(), toscaOperationFacade(), graphLockOperation());
        }

        @Bean
        ExternalReferencesOperation externalReferencesOperation() {
            this.externalReferenceOperation = new ExternalReferencesOperation(titanDao(), nodeTypeOpertaion(), topologyTemplateOperation(), idMapper());
            GraphTestUtils.clearGraph(titanDao);
            initGraphForTest();
            return this.externalReferenceOperation;
        }

        @Bean
        ToscaOperationFacade toscaOperationFacade(){
            Component componentMock = Mockito.mock(Component.class);
            when(componentMock.getVersion()).thenReturn(VERSION);
            when(componentMock.getUniqueId()).thenReturn(serviceVertexUuid);
            List<Component> listComponents = new LinkedList<Component>();
            listComponents.add(componentMock);
            when(toscaOperationFacadeMock.getComponentListByUuid(eq(serviceVertexUuid), any())).thenReturn(Either.left(listComponents));
            when(toscaOperationFacadeMock.getComponentByUuidAndVersion(eq(serviceVertexUuid), eq(VERSION))).thenReturn(Either.left(componentMock));
            when(toscaOperationFacadeMock.getLatestComponentByUuid(eq(serviceVertexUuid), any())).thenReturn(Either.left(listComponents.get(0)));
            return toscaOperationFacadeMock;
        }

        @Bean
        IdMapper idMapper() {
            IdMapper idMapper = Mockito.mock(IdMapper.class);
            when(idMapper.mapComponentNameToUniqueId(eq(COMPONENT_ID), any(GraphVertex.class))).thenReturn(COMPONENT_ID);
            when(idMapper.mapUniqueIdToComponentNameTo(eq(COMPONENT_ID), any(GraphVertex.class))).thenReturn(COMPONENT_ID);
            when(idMapper.mapComponentNameToUniqueId(eq(FAKE_COMPONENT_ID), any(GraphVertex.class))).thenReturn(null);
            return idMapper;
        }

        @Bean
        TopologyTemplateOperation topologyTemplateOperation() {
            return new TopologyTemplateOperation();
        }

        @Bean
        NodeTypeOperation nodeTypeOpertaion() {
            return new NodeTypeOperation(null);
        }

        @Bean
        NodeTemplateOperation nodeTemplateOperation(){
            return new NodeTemplateOperation();
        }

        @Bean
        GroupsOperation groupsOperation(){
            return new GroupsOperation();
        }

        @Bean
        ICacheMangerOperation cacheMangerOperation() { return cacheManagerOperation; }

        @Bean
        TitanDao titanDao() {
            this.titanDao = new TitanDao(titanGraphClient());
            return titanDao;
        }

        @Bean
        TitanGraphClient titanGraphClient() {
            return new TitanGraphClient(titanClientStrategy());
        }

        @Bean
        TitanClientStrategy titanClientStrategy() {
            return new DAOTitanStrategy();
        }

        @Bean
        CategoryOperation categoryOperation(){
            return new CategoryOperation();
        }

        @Bean
        GraphLockOperation graphLockOperation() { return graphLockOperation; }

        @Bean
        TitanGenericDao titanGenericDao() { return titanGenericDao; }

        private void initGraphForTest() {
            //create a service and add 1 ref
            serviceVertex = GraphTestUtils.createServiceVertex(titanDao, new HashMap<>());
            serviceVertexUuid = this.serviceVertex.getUniqueId();

            //monitoring references
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_1);
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_2);
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_3);
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);

            //workflow references
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, WORKFLOW_OBJECT_TYPE, REF_6);

            final TitanOperationStatus commit = this.titanDao.commit();
            assertThat(commit).isEqualTo(TitanOperationStatus.OK);
        }


    }

    public static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    /* Users */
    private static User adminUser = new User("admin", "admin", "admin", "admin@email.com", Role.ADMIN.name(), System.currentTimeMillis());
    private static User designerUser = new User("designer", "designer", "designer", "designer@email.com", Role.DESIGNER.name(), System.currentTimeMillis());
    private static User otherUser = new User("other", "other", "other", "other@email.com", Role.OPS.name(), System.currentTimeMillis());


    @BeforeClass
    public static void setup() {

        //Needed for User Authorization
        //========================================================================================================================
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getUserAdmin()).thenReturn(userAdmin);
        when(servletUtils.getComponentsUtils()).thenReturn(componentUtils);
        when(componentUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION)).thenReturn(responseFormat);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED.value());

        //Needed for error configuration
        when(notFoundResponseFormat.getStatus()).thenReturn(HttpStatus.NOT_FOUND.value());
        when(badRequestResponseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST.value());
        when(componentUtils.getResponseFormat(eq(ActionStatus.RESOURCE_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_VERSION_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.EXT_REF_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID), (String[]) any())).thenReturn(badRequestResponseFormat);

        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<User, ActionStatus> adminEither = Either.left(adminUser);
        Either<User, ActionStatus> designerEither = Either.left(designerUser);
        Either<User, ActionStatus> otherEither = Either.left(otherUser);

        when(userAdmin.getUser(adminUser.getUserId(), false)).thenReturn(adminEither);
        when(userAdmin.getUser(designerUser.getUserId(), false)).thenReturn(designerEither);
        when(userAdmin.getUser(otherUser.getUserId(), false)).thenReturn(otherEither);
        //========================================================================================================================

        if (ConfigurationManager.getConfigurationManager() == null) {
            String appConfigDir = "src/test/resources/config";
            ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
            configurationManager = new ConfigurationManager(configurationSource);

            org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
            configuration.setTitanInMemoryGraph(true);

            configurationManager.setConfiguration(configuration);
        }
        ExternalConfiguration.setAppName("catalog-be");
    }

    @Before
    public void beforeTest() {

    }

    @Test
    public void testGetExternalRefsForExistingComponentInstance() {
        String path = String.format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);

        Response response = target().
                            path(path).
                            request().
                            accept(MediaType.APPLICATION_JSON).
                            header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                            header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                            get();

        List<String> dto = response.readEntity(new GenericType<List<String>>() { });
        assertThat(dto).containsExactly(REF_1, REF_2, REF_3, REF_5);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testGetExternalRefsForAsset() {
        String path = String.format("/v1/catalog/services/%s/version/%s/externalReferences/%s", serviceVertexUuid, VERSION, MONITORING_OBJECT_TYPE);

        Response response = target().
                path(path).
                request().
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                get();

        Map<String, List<String>> dtoMap = response.readEntity(new GenericType<HashMap<String, List<String>>>() { });
        assertThat(dtoMap.get(COMPONENT_ID)).containsExactly(REF_1, REF_2, REF_3, REF_5);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testAddExternalRefForExistingComponentInstance(){
        String path = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE);
        String getPath = String.format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target().
                            path(path).
                            request(MediaType.APPLICATION_JSON).
                            accept(MediaType.APPLICATION_JSON).
                            header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                            header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                            post(Entity.json(new ExternalRefDTO(REF_4)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        //Check that GET will include the new reference
        response = target().
                        path(getPath).
                        request().
                        accept(MediaType.APPLICATION_JSON).
                        header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                        header(Constants.USER_ID_HEADER, adminUser.getUserId()).
                        get();

        List<String> dto = response.readEntity(new GenericType<List<String>>() { });
        assertThat(dto).containsExactlyInAnyOrder(REF_1, REF_2, REF_3, REF_4, REF_5);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testDeleteExternalRefForExistingComponentInstance(){
        String deletePath = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);
        String getPath = String.format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target().
                path(deletePath).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                delete();

        //Verify that the deleted reference is returned in body
        ExternalRefDTO dto = response.readEntity(ExternalRefDTO.class);
        assertThat(dto.getReferenceUUID()).isEqualTo(REF_5);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        //Check that GET will NOT include the deleted reference
        response = target().
                path(getPath).
                request().
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                get();

        List<String> getResponse = response.readEntity(new GenericType<List<String>>() { });
        assertThat(getResponse).containsExactlyInAnyOrder(REF_1, REF_2, REF_3);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void testUpdateExternalRefForExistingComponentInstance(){
        String updatePath = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);
        String getPath = String.format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target().
                path(updatePath).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                put(Entity.json(new ExternalRefDTO(REF_4)));

        //Verify that the updated reference is returned in body
        ExternalRefDTO putResponseBody = response.readEntity(ExternalRefDTO.class);
        assertThat(putResponseBody.getReferenceUUID()).isEqualTo(REF_4);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        //Check that GET will include the updated reference
        response = target().
                path(getPath).
                request().
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                get();


        List<String> dto = response.readEntity(new GenericType<List<String>>() { });
        assertThat(dto).containsExactlyInAnyOrder(REF_1, REF_2, REF_3, REF_4);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }


    /*
    * Rainy Scenarios
    */
    @Test
    public void testAddExternalRefForNonExistingAssetId(){
        String path = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s", "non-existing-uuid", COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target().
                path(path).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                post(Entity.json(new ExternalRefDTO(REF_4)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testAddExternalRefForNonExistingCompInstId(){
        String path = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, "FAKE_COM_ID", MONITORING_OBJECT_TYPE);
        Response response = target().
                path(path).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                post(Entity.json(new ExternalRefDTO(REF_4)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testAddExistingExternalRef(){
        String path = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target().
                path(path).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                post(Entity.json(new ExternalRefDTO(REF_1)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()); //Ref not created but still OK is returned
    }

    @Test
    public void testUpdateExternalRefForNonExistingAssetId(){
        String updatePath = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", "nonExistingServiceVertexUuid", COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);

        Response response = target().
                path(updatePath).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                put(Entity.json(new ExternalRefDTO(REF_4)));

        //Verify that the 404 is returned
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    @Test
    public void testUpdateExternalRefForNonExistingObjectIdOrOldRef(){
        String updatePath = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid, COMPONENT_ID, "FAKE_OBJ_TYPE", REF_5);

        Response response = target().
                path(updatePath).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                put(Entity.json(new ExternalRefDTO(REF_4)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        System.out.println(response.readEntity(String.class));
    }

    @Test
    public void testDeleteExternalRefForNonExistingAssetId(){
        String deletePath = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", "non-existing-asset", COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);
        Response response = target().
                path(deletePath).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testDeleteExternalRefForNonExistingRef(){
        String deletePath = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, "FAKE_REF");
        Response response = target().
                path(deletePath).
                request(MediaType.APPLICATION_JSON).
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        System.out.println(response.readEntity(String.class));
    }

    @Test
    public void testGetExternalRefsForNonExistingAsset() {
        String path = String.format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", "fake-asset-id", VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);

        Response response = target().
                        path(path).
                        request().
                        accept(MediaType.APPLICATION_JSON).
                        header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                        header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                        get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testGetExternalRefsForNonExistingVersion() {
        String path = String.format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, FAKE_VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);

        Response response = target().
                path(path).
                request().
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, designerUser.getUserId()).
                get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testGetExternalRefsForExistingComponentInstanceWithUnauthorizedUser() {
        String path = String.format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid, VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);

        Response response = target().
                path(path).
                request().
                accept(MediaType.APPLICATION_JSON).
                header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").
                header(Constants.USER_ID_HEADER, otherUser.getUserId()).
                get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(TestSpringConfig.class);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new ResourceConfig(ExternalRefsServlet.class)
                .register(new AbstractBinder() {

                    @Override
                    protected void configure() {
                        bind(request).to(HttpServletRequest.class);
                        when(request.getSession()).thenReturn(session);
                        when(request.getHeader(eq(Constants.X_ECOMP_INSTANCE_ID_HEADER))).thenReturn("mockXEcompInstIdHeader");
                        when(session.getServletContext()).thenReturn(servletContext);
                    }
                })
                .property("contextConfig", context);
    }

}
