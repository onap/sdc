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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.externalapi.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.ComponentLocker;
import org.openecomp.sdc.be.components.impl.ExternalRefsBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.path.utils.GraphTestUtils;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.config.Configuration.HeatDeploymentArtifactTimeout;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.DAOJanusGraphStrategy;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.dto.ExternalRefDTO;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.config.ContainerInstanceTypesData;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArchiveOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.CategoryOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ExternalReferencesOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.IdMapper;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelElementOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.model.operations.impl.OperationUtils;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.DefaultExceptionMapper;
import org.openecomp.sdc.be.servlets.exception.StorageExceptionMapper;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.WebApplicationContext;

class ExternalRefsServletTest extends JerseyTest {

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
    private static String serviceVertexUuid;
    private static String resourceVertexUuid;
    /* Users */
    private static final User adminUser = new User("admin", "admin", "admin", "admin@email.com", Role.ADMIN.name(),
        System.currentTimeMillis());
    private static final User designerUser = new User("designer", "designer", "designer", "designer@email.com",
        Role.DESIGNER.name(), System
        .currentTimeMillis());
    private static final User otherDesignerUser = new User("otherDesigner", "otherDesigner", "otherDesigner",
        "otherDesigner@email.com", Role.DESIGNER
        .name(), System.currentTimeMillis());
    private static final User otherUser = new User("other", "other", "other", "other@email.com", Role.DESIGNER.name(),
        System.currentTimeMillis());

    private static final IdMapper idMapper = mock(IdMapper.class);
    private static final WebAppContextWrapper webAppContextWrapper = mock(WebAppContextWrapper.class);
    private static final ServletContext servletContext = mock(ServletContext.class);
    private static final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    private static final ServletUtils servletUtils = mock(ServletUtils.class);
    private static final ComponentsUtils componentUtils = mock(ComponentsUtils.class);
    private static final ResponseFormat responseFormat = mock(ResponseFormat.class);
    private static final ResponseFormat notFoundResponseFormat = mock(ResponseFormat.class);
    private static final ResponseFormat badRequestResponseFormat = mock(ResponseFormat.class);
    private static final UserBusinessLogic userAdmin = mock(UserBusinessLogic.class);
    private static final ToscaOperationFacade toscaOperationFacadeMock = mock(ToscaOperationFacade.class);
    private static final AccessValidations accessValidationsMock = mock(AccessValidations.class);
    private static final ComponentLocker componentLocker = mock(ComponentLocker.class);
    private static final HealingJanusGraphGenericDao janusGraphGenericDao = mock(HealingJanusGraphGenericDao.class);
    private static final IGraphLockOperation graphLockOperation = mock(IGraphLockOperation.class);
    private static final ByResponseFormatComponentException ce = mock(ByResponseFormatComponentException.class);
    private static final Component resourceComponentMock = mock(Component.class);
    private static final Component serviceComponentMock = mock(Component.class);
    private static final ModelOperation modelOperation = mock(ModelOperation.class);
    private static final ModelElementOperation modelElementOperation = mock(ModelElementOperation.class);

    @BeforeAll
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

        when(ce.getResponseFormat()).thenReturn(responseFormat);
        doThrow(ce).when(accessValidationsMock).validateUserCanWorkOnComponent(any(), any(), eq(otherDesignerUser.getUserId()), any());
        doThrow(ce).when(accessValidationsMock).validateUserCanWorkOnComponent(any(), any(), eq(otherUser.getUserId()), any());

        //Needed for error configuration
        when(notFoundResponseFormat.getStatus()).thenReturn(HttpStatus.NOT_FOUND.value());
        when(badRequestResponseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST.value());
        when(componentUtils.getResponseFormat(eq(ActionStatus.RESOURCE_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_VERSION_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.EXT_REF_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID), (String[]) any())).thenReturn(badRequestResponseFormat);
        when(userAdmin.getUser(adminUser.getUserId(), false)).thenReturn(adminUser);
        when(userAdmin.getUser(designerUser.getUserId(), false)).thenReturn(designerUser);
        when(userAdmin.getUser(otherUser.getUserId(), false)).thenReturn(otherUser);
        //========================================================================================================================

        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        HeatDeploymentArtifactTimeout heatDeploymentArtifactTimeout = new HeatDeploymentArtifactTimeout();
        heatDeploymentArtifactTimeout.setDefaultMinutes(30);
        configuration.setAafAuthNeeded(false);
        configuration.setHeatArtifactDeploymentTimeout(heatDeploymentArtifactTimeout);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @BeforeEach
    public void before() throws Exception {
        super.setUp();

        when(resourceComponentMock.getVersion()).thenReturn(VERSION);
        when(resourceComponentMock.getUniqueId()).thenReturn(resourceVertexUuid);

        when(serviceComponentMock.getVersion()).thenReturn(VERSION);
        when(serviceComponentMock.getUniqueId()).thenReturn(serviceVertexUuid);

        List<Component> listComponents = new LinkedList<>();
        listComponents.add(serviceComponentMock);

        when(toscaOperationFacadeMock.getComponentListByUuid(eq(serviceVertexUuid), any())).thenReturn(Either.left(listComponents));
        when(toscaOperationFacadeMock.getComponentByUuidAndVersion(serviceVertexUuid, VERSION)).thenReturn(Either.left(serviceComponentMock));
        when(toscaOperationFacadeMock.getComponentByUuidAndVersion(resourceVertexUuid, VERSION)).thenReturn(Either.left(resourceComponentMock));
        when(toscaOperationFacadeMock.getLatestComponentByUuid(eq(serviceVertexUuid), any())).thenReturn(Either.left(listComponents.get(0)));
        when(toscaOperationFacadeMock.getLatestComponentByUuid(eq(resourceVertexUuid), any())).thenReturn(Either.left(resourceComponentMock));
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Test
    void testGetExternalRefsForExistingComponentInstance() {
        String path = String
            .format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid,
                VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .get();

        List<String> dto = response.readEntity(new GenericType<List<String>>() {
        });
        assertThat(dto).containsExactly(REF_1, REF_2, REF_3, REF_5);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void testGetExternalRefsForAsset() {
        String path = String
            .format("/v1/catalog/services/%s/version/%s/externalReferences/%s", serviceVertexUuid, VERSION,
                MONITORING_OBJECT_TYPE);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .get();

        Map<String, List<String>> dtoMap = response.readEntity(new GenericType<HashMap<String, List<String>>>() {
        });
        assertThat(dtoMap.get(COMPONENT_ID)).containsExactly(REF_1, REF_2, REF_3, REF_5);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void testGetExternalRefsForAssetWithMissingEcompHeader() {
        String path = String
            .format("/v1/catalog/services/%s/version/%s/externalReferences/%s", serviceVertexUuid, VERSION,
                MONITORING_OBJECT_TYPE);

        //No X-Ecomp-Instance-ID header
        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void testAddExternalRefForResource() {
        String path = String
            .format("/v1/catalog/resources/%s/resourceInstances/%s/externalReferences/%s", resourceVertexUuid,
                COMPONENT_ID, MONITORING_OBJECT_TYPE);
        String getPath = String
            .format("/v1/catalog/resources/%s/version/%s/externalReferences/%s", resourceVertexUuid, VERSION,
                MONITORING_OBJECT_TYPE);

        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .post(Entity.json(new ExternalRefDTO(REF_1)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        //Check that GET will include the new reference
        response = target()
            .path(getPath)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, adminUser.getUserId())
            .get();

        Map<String, List<String>> dto = response.readEntity(new GenericType<Map<String, List<String>>>() {
        });
        assertThat(dto.get(COMPONENT_ID)).containsExactlyInAnyOrder(REF_1);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void testAddExternalRefForExistingComponentInstance() {
        String path = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid,
                COMPONENT_ID, MONITORING_OBJECT_TYPE);
        String getPath = String
            .format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid,
                VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .post(Entity.json(new ExternalRefDTO(REF_4)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        //Check that GET will include the new reference
        response = target()
            .path(getPath)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, adminUser.getUserId())
            .get();

        List<String> dto = response.readEntity(new GenericType<List<String>>() {
        });
        assertThat(dto).containsExactlyInAnyOrder(REF_1, REF_2, REF_3, REF_4, REF_5);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void testDeleteExternalRefForExistingComponentInstance() {
        String deletePath = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid,
                COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);
        String getPath = String
            .format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid,
                VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target()
            .path(deletePath)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .delete();

        //Verify that the deleted reference is returned in body
        ExternalRefDTO dto = response.readEntity(ExternalRefDTO.class);
        assertThat(dto.getReferenceUUID()).isEqualTo(REF_5);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        //Check that GET will NOT include the deleted reference
        response = target()
            .path(getPath)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .get();

        List<String> getResponse = response.readEntity(new GenericType<List<String>>() {
        });
        assertThat(getResponse).containsExactlyInAnyOrder(REF_1, REF_2, REF_3);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void testUpdateExternalRefForExistingComponentInstance() {
        String updatePath = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid,
                COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);
        String getPath = String
            .format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid,
                VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target()
            .path(updatePath)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .put(Entity.json(new ExternalRefDTO(REF_4)));

        //Verify that the updated reference is returned in body
        ExternalRefDTO putResponseBody = response.readEntity(ExternalRefDTO.class);
        assertThat(putResponseBody.getReferenceUUID()).isEqualTo(REF_4);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        //Check that GET will include the updated reference
        response = target()
            .path(getPath)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .get();

        List<String> dto = response.readEntity(new GenericType<List<String>>() {
        });
        assertThat(dto).containsExactlyInAnyOrder(REF_1, REF_2, REF_3, REF_4);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    /*
     * Rainy Scenarios
     */
    @Test
    void testAddExternalRefForNonExistingAssetId() {
        String path = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s", "non-existing-uuid",
                COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .post(Entity.json(new ExternalRefDTO(REF_4)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testAddExternalRefForNonExistingCompInstId() {
        String path = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid,
                "FAKE_COM_ID", MONITORING_OBJECT_TYPE);
        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .post(Entity.json(new ExternalRefDTO(REF_4)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testAddExistingExternalRef() {
        String path = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid,
                COMPONENT_ID, MONITORING_OBJECT_TYPE);
        Response response = target()
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .post(Entity.json(new ExternalRefDTO(REF_1)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value()); //Ref not created but still OK is returned
    }

    @Test
    void testUpdateExternalRefForNonExistingAssetId() {
        String updatePath = String.format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s",
            "nonExistingServiceVertexUuid", COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);

        Response response = target()
            .path(updatePath)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .put(Entity.json(new ExternalRefDTO(REF_4)));

        //Verify that the 404 is returned
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    @Test
    void testUpdateExternalRefForNonExistingObjectIdOrOldRef() {
        String updatePath = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid,
                COMPONENT_ID, "FAKE_OBJ_TYPE", REF_5);

        Response response = target()
            .path(updatePath)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .put(Entity.json(new ExternalRefDTO(REF_4)));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testDeleteExternalRefForNonExistingAssetId() {
        String deletePath = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", "non-existing-asset",
                COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);
        Response response = target()
            .path(deletePath)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testDeleteExternalRefForNonExistingRef() {
        String deletePath = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid,
                COMPONENT_ID, MONITORING_OBJECT_TYPE, "FAKE_REF");
        Response response = target()
            .path(deletePath)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testGetExternalRefsForNonExistingAsset() {
        String path = String
            .format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", "fake-asset-id",
                VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testGetExternalRefsForNonExistingVersion() {
        String path = String
            .format("/v1/catalog/services/%s/version/%s/resourceInstances/%s/externalReferences/%s", serviceVertexUuid,
                FAKE_VERSION, COMPONENT_ID, MONITORING_OBJECT_TYPE);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, designerUser.getUserId())
            .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testDeleteExternalRefsForExistingComponentInstanceWithUnauthorizedUser() {
        String path = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid,
                COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);

        Response response = target()
            .path(path)
            .request()
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, otherUser.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void testDeleteExternalRefForUserWhichIsNotCurrentUpdater() {
        String deletePath = String
            .format("/v1/catalog/services/%s/resourceInstances/%s/externalReferences/%s/%s", serviceVertexUuid,
                COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);
        Response response = target()
            .path(deletePath)
            .request(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, otherDesignerUser.getUserId())
            .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(TestSpringConfig.class);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new ResourceConfig(ExternalRefsServlet.class)
            .register(DefaultExceptionMapper.class)
            .register(ComponentExceptionMapper.class)
            .register(StorageExceptionMapper.class)
            .property("contextConfig", context);
    }

    @Configuration
    @PropertySource("classpath:dao.properties")
    static class TestSpringConfig {

        private GraphVertex serviceVertex;
        private GraphVertex resourceVertex;
        private ExternalReferencesOperation externalReferenceOperation;
        private HealingJanusGraphDao janusGraphDao;

        @Bean
        ExternalRefsServlet externalRefsServlet() {
            return new ExternalRefsServlet(componentUtils, externalRefsBusinessLogic());
        }

        @Bean
        OperationUtils operationUtils() {
            return new OperationUtils(janusGraphDao());
        }

        @Bean
        ComponentExceptionMapper componentExceptionMapper() {
            return new ComponentExceptionMapper(componentUtils);
        }

        @Bean
        StorageExceptionMapper storageExceptionMapper() {
            return new StorageExceptionMapper(componentUtils);
        }

        @Bean
        DefaultExceptionMapper defaultExceptionMapper() {
            return new DefaultExceptionMapper();
        }

        @Bean
        ExternalRefsBusinessLogic externalRefsBusinessLogic() {
            return new ExternalRefsBusinessLogic(externalReferencesOperation(), toscaOperationFacade(),
                accessValidations(), componentLocker());
        }

        @Bean
        AccessValidations accessValidations() {
            return accessValidationsMock;
        }

        @Bean
        ExternalReferencesOperation externalReferencesOperation() {
            this.externalReferenceOperation = new ExternalReferencesOperation(janusGraphDao(), nodeTypeOpertaion(),
                topologyTemplateOperation(), idMapper());
            this.externalReferenceOperation.setHealingPipelineDao(healingPipelineDao());
            GraphTestUtils.clearGraph(janusGraphDao);
            initGraphForTest();
            return this.externalReferenceOperation;
        }

        @Bean
        ToscaOperationFacade toscaOperationFacade() {
            return toscaOperationFacadeMock;
        }

        @Bean
        IdMapper idMapper() {
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
        ArchiveOperation archiveOperation() {
            return new ArchiveOperation(janusGraphDao(), graphLockOperation());
        }

        @Bean
        IGraphLockOperation graphLockOperation() {
            return graphLockOperation;
        }

        @Bean
        NodeTypeOperation nodeTypeOpertaion() {
            return new NodeTypeOperation(null);
        }

        @Bean
        NodeTemplateOperation nodeTemplateOperation() {
            return new NodeTemplateOperation();
        }

        @Bean
        GroupsOperation groupsOperation() {
            return new GroupsOperation();
        }

        @Bean
        HealingJanusGraphDao janusGraphDao() {
            this.janusGraphDao = new HealingJanusGraphDao(healingPipelineDao(), janusGraphClient());
            return janusGraphDao;
        }

        @Bean
        JanusGraphClient janusGraphClient() {
            return new JanusGraphClient(janusGraphClientStrategy());
        }

        @Bean
        JanusGraphClientStrategy janusGraphClientStrategy() {
            return new DAOJanusGraphStrategy();
        }

        @Bean
        CategoryOperation categoryOperation() {
            return new CategoryOperation();
        }

        @Bean
        ComponentLocker componentLocker() {
            return componentLocker;
        }

        @Bean
        JanusGraphGenericDao janusGraphGenericDao() {
            return janusGraphGenericDao;
        }

        @Bean
        ModelOperation modelOperation() {
            return modelOperation;
        }

        @Bean
        ModelElementOperation modelElementOperation() {
            return modelElementOperation;
        }

        @Bean("healingPipelineDao")
        HealingPipelineDao healingPipelineDao() {
            HealingPipelineDao healingPipelineDao = new HealingPipelineDao();
            healingPipelineDao.setHealVersion(1);
            healingPipelineDao.initHealVersion();
            return healingPipelineDao;
        }

        @Bean
        ContainerInstanceTypesData containerInstanceTypesData() {
            return new ContainerInstanceTypesData();
        }

        private void initGraphForTest() {
            resourceVertex = GraphTestUtils.createResourceVertex(janusGraphDao, new HashMap<>(), ResourceTypeEnum.VF);
            resourceVertexUuid = resourceVertex.getUniqueId();

            //create a service and add ref
            serviceVertex = GraphTestUtils.createServiceVertex(janusGraphDao, new HashMap<>());
            serviceVertexUuid = this.serviceVertex.getUniqueId();

            //monitoring references
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_1);
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_2);
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_3);
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, MONITORING_OBJECT_TYPE, REF_5);

            //workflow references
            externalReferenceOperation.addExternalReference(serviceVertexUuid, COMPONENT_ID, WORKFLOW_OBJECT_TYPE, REF_6);

            final JanusGraphOperationStatus commit = this.janusGraphDao.commit();
            assertThat(commit).isEqualTo(JanusGraphOperationStatus.OK);
        }

    }
}
