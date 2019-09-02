/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

import fj.data.Either;
import org.apache.commons.text.StrSubstitutor;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

public class ArtifactExternalServletTest extends JerseyTest {
    public static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    public static final HttpSession session = Mockito.mock(HttpSession.class);
    public static final ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
    public static final ResourceBusinessLogic resourceBusinessLogic = Mockito.mock(ResourceBusinessLogic.class);
    public static final Resource resource = Mockito.mock(Resource.class);
    public static final UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
    public static final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    public static final ArtifactsBusinessLogic artifactsBusinessLogic = Mockito.mock(ArtifactsBusinessLogic.class);

    private static final ServletContext servletContext = Mockito.mock(ServletContext.class);
    public static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    private static final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
    private static final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
    private static final UserBusinessLogic userAdmin = Mockito.mock(UserBusinessLogic.class);
    private static final ComponentsUtils componentUtils = Mockito.mock(ComponentsUtils.class);
    private static final ResponseFormat generalErrorResponseFormat = new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    private static final ResponseFormat unauthorizedResponseFormat = Mockito.mock(ResponseFormat.class);
    private static final ResponseFormat notFoundResponseFormat = Mockito.mock(ResponseFormat.class);
    private static final ResponseFormat badRequestResponseFormat = Mockito.mock(ResponseFormat.class);
    private static final String ASSET_TYPE = "assetType";
    public static final String UUID = "uuid";
    private static final String RESOURCE_INSTANCE_NAME = "resourceInstanceName";
    private static final String INTERFACE_UUID = "interfaceUUID";
    private static final String OPERATION_UUID = "operationUUID";
    private static final String ARTIFACT_UUID = "artifactUUID";
    private static final String EMPTY_JSON = "{}";

    /* Users */
    private static User designerUser = new User("designer", "designer", "designer", "designer@email.com", Role.DESIGNER.name(), System
            .currentTimeMillis());

    @BeforeClass
    public static void setup() {

        //Needed for User Authorization
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getUserAdmin()).thenReturn(userAdmin);
        when(servletUtils.getComponentsUtils()).thenReturn(componentUtils);
        when(componentUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION)).thenReturn(unauthorizedResponseFormat);
        when(unauthorizedResponseFormat.getStatus()).thenReturn(HttpStatus.SC_UNAUTHORIZED);

        ResponseFormat okResponseFormat = new ResponseFormat(org.apache.http.HttpStatus.SC_OK);

        when(componentUtils.getResponseFormat(ActionStatus.OK)) .thenReturn(okResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(badRequestResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)) .thenReturn(generalErrorResponseFormat);
        when(componentUtils.getResponseFormat(any(ComponentException.class)))
                .thenReturn(generalErrorResponseFormat);

        ByResponseFormatComponentException ce = Mockito.mock(ByResponseFormatComponentException.class);
        when(ce.getResponseFormat()).thenReturn(unauthorizedResponseFormat);

        //Needed for error configuration
        when(notFoundResponseFormat.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(badRequestResponseFormat.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        when(componentUtils.getResponseFormat(eq(ActionStatus.RESOURCE_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_VERSION_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.EXT_REF_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID), any())).thenReturn(badRequestResponseFormat);
        
        Either<User, ActionStatus> designerEither = Either.left(designerUser);

        when(userAdmin.getUser(designerUser.getUserId(), false)).thenReturn(designerEither);

        String appConfigDir = "src/test/resources/config";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);

        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @Test
    public void uploadInterfaceOperationArtifactNoInstanceIdHeaderTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, "assetType");
        parametersMap.put(UUID, "uuid");
        parametersMap.put(INTERFACE_UUID, "interfaceUUID");
        parametersMap.put(OPERATION_UUID, "operationUUID");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/interfaces/{interfaceUUID}/operations/{operationUUID}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void uploadInterfaceOperationArtifactNoUserHeaderTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, "assetType");
        parametersMap.put(UUID, "uuid");
        parametersMap.put(INTERFACE_UUID, "interfaceUUID");
        parametersMap.put(OPERATION_UUID, "operationUUID");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/interfaces/{interfaceUUID}/operations/{operationUUID}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void uploadInterfaceOperationArtifactTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, "assetType");
        parametersMap.put(UUID, "uuid");
        parametersMap.put(INTERFACE_UUID, "interfaceUUID");
        parametersMap.put(OPERATION_UUID, "operationUUID");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/interfaces/{interfaceUUID}/operations/{operationUUID}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = Either.left(artifactDefinition);
        when(artifactsBusinessLogic
                .updateArtifactOnInterfaceOperationByResourceUUID(anyString(), any(),
                        any(), any(), any(),
                        any(), any(), any(),
                        any()))
                .thenReturn(uploadArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void uploadInterfaceOperationArtifactFailedUploadTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, "assetType");
        parametersMap.put(UUID, "uuid");
        parametersMap.put(INTERFACE_UUID, "interfaceUUID");
        parametersMap.put(OPERATION_UUID, "operationUUID");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/interfaces/{interfaceUUID}/operations/{operationUUID}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");
        Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither =
                Either.right(new ResponseFormat(HttpStatus.SC_CONFLICT));

        when(artifactsBusinessLogic
                .updateArtifactOnInterfaceOperationByResourceUUID(anyString(), any(),
                        any(), any(), any(),
                        any(), any(), any(),
                        any()))
                .thenReturn(uploadArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void uploadInterfaceOperationArtifactExceptionDuringProcessingTest() {
        String uuid = "uuidToThrow_uploadArtifact";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, "assetType");
        parametersMap.put(UUID, uuid);
        parametersMap.put(INTERFACE_UUID, "interfaceUUID");
        parametersMap.put(OPERATION_UUID, "operationUUID");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/interfaces/{interfaceUUID}/operations/{operationUUID}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        when(artifactsBusinessLogic
                .updateArtifactOnInterfaceOperationByResourceUUID(anyString(), any(),
                        any(), any(), any(),
                        any(), any(), any(),
                        any()))
                .thenThrow(new RuntimeException("Text exception"));

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void uploadArtifactUnknownComponentTypeTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, "something_new");
        parametersMap.put(UUID, "uuid");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void uploadArtifactErrorDuringUploadProcessingTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither =
                Either.right(new ResponseFormat(HttpStatus.SC_CONFLICT));

        when(artifactsBusinessLogic
                .uploadArtifactToComponentByUUID(anyString(), any(), any(), any(),  any(),
                        any()))
                .thenReturn(uploadArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void uploadArtifactExceptionDuringUploadTest() {
        String uuid = "uuidToThrow_uploadArtifact";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, uuid);

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        given(artifactsBusinessLogic.uploadArtifactToComponentByUUID(anyString(), any(),
                any(), eq(uuid), any(), any()))
                .willAnswer( invocation -> { throw new IOException("Test exception"); });

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void uploadArtifactTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = Either.left(artifactDefinition);
        when(artifactsBusinessLogic
                .uploadArtifactToComponentByUUID(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(uploadArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void uploadArtifactToInstanceErrorDuringUploadProcessingTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(RESOURCE_INSTANCE_NAME, "resourceInstanceName");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Either<ArtifactDefinition, ResponseFormat> uploadArtifactToRiByUUIDEither =
                Either.right(new ResponseFormat(HttpStatus.SC_CONFLICT));

        given(artifactsBusinessLogic.uploadArtifactToRiByUUID(anyString(), any(),
                any(), any(), any(), any()))
                .willAnswer( invocation -> uploadArtifactToRiByUUIDEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void uploadArtifactToInstanceExceptionDuringUploadTest() {
        String uuid = "uuidToThrow_uploadArtifactToInstance";
        String resourceInstanceName = "resourceInstanceNameToThrow_uploadArtifactToInstance";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, uuid);
        parametersMap.put(RESOURCE_INSTANCE_NAME, resourceInstanceName);

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        given(artifactsBusinessLogic.uploadArtifactToRiByUUID(anyString(), any(),
                any(), eq(uuid), eq(resourceInstanceName), any()))
                .willAnswer( invocation -> { throw new IOException("Test exception"); });

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void uploadArtifactToInstanceTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(RESOURCE_INSTANCE_NAME, "resourceInstanceName");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Either<ArtifactDefinition, ResponseFormat> uploadArtifactToRiByUUIDEither = Either.left(artifactDefinition);

        given(artifactsBusinessLogic.uploadArtifactToRiByUUID(anyString(), any(),
                any(), any(), any(), any()))
                .willAnswer( invocation -> uploadArtifactToRiByUUIDEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void updateArtifactErrorDuringUpdateProcessingTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Either<ArtifactDefinition, ResponseFormat> updateArtifactEither =
                Either.right(new ResponseFormat(HttpStatus.SC_CONFLICT));

        given(artifactsBusinessLogic.updateArtifactOnComponentByUUID(anyString(), any(),
                any(), any(), any(), any(), any()))
                .willAnswer( invocation -> updateArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void updateArtifactExceptionDuringUpdateTest() {
        String uuid = "uuidToThrow_updateArtifact";
        String artifactUUID = "artifactUUIDToThrow_updateArtifact";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, uuid);
        parametersMap.put(ARTIFACT_UUID, artifactUUID);

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        given(artifactsBusinessLogic.updateArtifactOnComponentByUUID(anyString(), any(),
                any(), eq(uuid), eq(artifactUUID), any(), any()))
                .willAnswer( invocation -> { throw new IOException("Test exception"); });

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void updateArtifactTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Either<ArtifactDefinition, ResponseFormat> uploadArtifactToRiByUUIDEither = Either.left(artifactDefinition);

        given(artifactsBusinessLogic.updateArtifactOnComponentByUUID(anyString(), any(),
                any(), any(), any(), any(), any()))
                .willAnswer( invocation -> uploadArtifactToRiByUUIDEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }
    
    /////////////////////////

    @Test
    public void updateArtifactOnResourceInstanceErrorDuringUpdateProcessingTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(RESOURCE_INSTANCE_NAME, "resourceInstanceName");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Either<ArtifactDefinition, ResponseFormat> updateArtifactOnResourceInstanceEither =
                Either.right(new ResponseFormat(HttpStatus.SC_CONFLICT));

        given(artifactsBusinessLogic.updateArtifactOnRiByUUID(anyString(), any(),
                any(), any(), any(), any(), any()))
                .willAnswer( invocation -> updateArtifactOnResourceInstanceEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void updateArtifactOnResourceInstanceExceptionDuringUpdateTest() {
        String uuid = "uuidToThrow_updateArtifactOnResourceInstance";
        String resourceInstanceName = "resourceInstanceNameToThrow_updateArtifactOnResourceInstance";
        String artifactUUID = "artifactUUIDToThrow_updateArtifactOnResourceInstance";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, uuid);
        parametersMap.put(RESOURCE_INSTANCE_NAME, resourceInstanceName);
        parametersMap.put(ARTIFACT_UUID, artifactUUID);

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        given(artifactsBusinessLogic.updateArtifactOnRiByUUID(anyString(), any(),
                any(), eq(uuid), eq(resourceInstanceName), eq(artifactUUID), any()))
                .willAnswer( invocation -> { throw new IOException("Test exception"); });

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void updateArtifactOnResourceInstanceTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(RESOURCE_INSTANCE_NAME, "resourceInstanceName");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Either<ArtifactDefinition, ResponseFormat> updateArtifactOnResourceInstanceEither = Either.left(artifactDefinition);

        given(artifactsBusinessLogic.updateArtifactOnRiByUUID(anyString(), any(),
                any(), any(), any(), any(), any()))
                .willAnswer( invocation -> updateArtifactOnResourceInstanceEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .post(Entity.json(EMPTY_JSON));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void deleteArtifactErrorDuringDeleteProcessingTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Either<ArtifactDefinition, ResponseFormat> deleteArtifactOnComponentByUUIDEither =
                Either.right(new ResponseFormat(HttpStatus.SC_CONFLICT));

        given(artifactsBusinessLogic.deleteArtifactOnComponentByUUID(any(), any(), any(), any(),
                any(), any()))
                .willAnswer( invocation -> deleteArtifactOnComponentByUUIDEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void deleteArtifactExceptionDuringDeleteTest() {
        String uuid = "uuidToThrow_deleteArtifact";
        String artifactUUID = "artifactUUIDToThrow_deleteArtifact";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, uuid);
        parametersMap.put(ARTIFACT_UUID, artifactUUID);

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        given(artifactsBusinessLogic.deleteArtifactOnComponentByUUID(any(), any(), eq(uuid),
                eq(artifactUUID), any(), any()))
                .willAnswer( invocation -> { throw new IOException("Test exception"); });

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void deleteArtifactTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Either<ArtifactDefinition, ResponseFormat> deleteArtifactOnComponentByUUIDEither = Either.left(artifactDefinition);

        given(artifactsBusinessLogic.deleteArtifactOnComponentByUUID(any(), any(), any(), any(),
                any(), any()))
                .willAnswer( invocation -> deleteArtifactOnComponentByUUIDEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void deleteArtifactOnResourceErrorDuringDeleteProcessingTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(RESOURCE_INSTANCE_NAME, "resourceInstanceName");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Either<ArtifactDefinition, ResponseFormat> deleteArtifactOnRiByUUIDEither =
                Either.right(new ResponseFormat(HttpStatus.SC_CONFLICT));

        given(artifactsBusinessLogic.deleteArtifactOnRiByUUID(any(), any(), any(), any(),
                any(), any()))
                .willAnswer( invocation -> deleteArtifactOnRiByUUIDEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void deleteArtifactOnResourceExceptionDuringDeleteTest() {
        String uuid = "uuidToThrow_deleteArtifactOnResource";
        String resourceInstanceName = "resourceInstanceNameToThrow_deleteArtifactOnResource";
        String artifactUUID = "artifactUUIDToThrow_deleteArtifactOnResource";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, uuid);
        parametersMap.put(RESOURCE_INSTANCE_NAME, resourceInstanceName);
        parametersMap.put(ARTIFACT_UUID, artifactUUID);

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        given(artifactsBusinessLogic.deleteArtifactOnRiByUUID(any(), any(), eq(uuid),
                eq(resourceInstanceName), eq(artifactUUID), any()))
                .willAnswer( invocation -> { throw new IOException("Test exception"); });

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void deleteArtifactOnResourceTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(RESOURCE_INSTANCE_NAME, "resourceInstanceName");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        Either<ArtifactDefinition, ResponseFormat> deleteArtifactOnRiByUUIDEither = Either.left(artifactDefinition);

        given(artifactsBusinessLogic.deleteArtifactOnRiByUUID(any(), any(), any(), any(),
                any(), any()))
                .willAnswer( invocation -> deleteArtifactOnRiByUUIDEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .delete();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void downloadComponentArtifactErrorDuringDownloadProcessingTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Either<byte[], ResponseFormat> downloadComponentArtifactByUUIDsEither =
                Either.right(new ResponseFormat(HttpStatus.SC_SERVICE_UNAVAILABLE));

        given(artifactsBusinessLogic.downloadComponentArtifactByUUIDs(any(), any(), any(), any()))
                .willAnswer( invocation -> downloadComponentArtifactByUUIDsEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_SERVICE_UNAVAILABLE);
    }

    @Test
    public void downloadComponentArtifactExceptionDuringUploadTest() {
        String uuid = "uuidToThrow_downloadComponentArtifact";
        String artifactUUID = "artifactUUIDToThrow_downloadComponentArtifact";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, uuid);
        parametersMap.put(ARTIFACT_UUID, artifactUUID);

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        given(artifactsBusinessLogic.downloadComponentArtifactByUUIDs(any(), eq(uuid),
                eq(artifactUUID), any()))
                .willAnswer( invocation -> { throw new IOException("Test exception"); });

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void downloadComponentArtifactTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        byte[] responsePayload = {0xA, 0xB, 0xC, 0xD};
        Either<byte[], ResponseFormat> downloadComponentArtifactByUUIDsEither = Either.left(responsePayload);

        given(artifactsBusinessLogic.downloadComponentArtifactByUUIDs(any(), any(), any(), any()))
                .willAnswer( invocation -> downloadComponentArtifactByUUIDsEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void downloadResourceInstanceArtifactErrorDuringDownloadProcessingTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(RESOURCE_INSTANCE_NAME, "resourceInstanceName");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        Either<byte[], ResponseFormat> downloadResourceInstanceArtifactByUUIDsEither =
                Either.right(new ResponseFormat(HttpStatus.SC_SERVICE_UNAVAILABLE));

        given(artifactsBusinessLogic.downloadResourceInstanceArtifactByUUIDs(any(), any(), any(),
                any()))
                .willAnswer( invocation -> downloadResourceInstanceArtifactByUUIDsEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_SERVICE_UNAVAILABLE);
    }

    @Test
    public void downloadResourceInstanceArtifactExceptionDuringUploadTest() {
        String uuid = "uuidToThrow_downloadResourceInstanceArtifact";
        String resourceInstanceName = "resourceInstanceNameToThrow_downloadResourceInstanceArtifact";
        String artifactUUID = "artifactUUIDToThrow_downloadResourceInstanceArtifact";
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, uuid);
        parametersMap.put(RESOURCE_INSTANCE_NAME, resourceInstanceName);
        parametersMap.put(ARTIFACT_UUID, artifactUUID);

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        when(artifactsBusinessLogic.downloadResourceInstanceArtifactByUUIDs(any(), eq(uuid),
                eq(resourceInstanceName), eq(artifactUUID)))
                .thenThrow(new ByResponseFormatComponentException(generalErrorResponseFormat));

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void downloadResourceInstanceArtifactTest() {
        Map<String,String> parametersMap = new HashMap<>();
        parametersMap.put(ASSET_TYPE, ComponentTypeEnum.SERVICE_PARAM_NAME);
        parametersMap.put(UUID, "uuid");
        parametersMap.put(RESOURCE_INSTANCE_NAME, "resourceInstanceName");
        parametersMap.put(ARTIFACT_UUID, "artifactUUID");

        String formatEndpoint = "/v1/catalog/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

        byte[] responsePayload = {0xA, 0xB, 0xC, 0xD};
        Either<byte[], ResponseFormat> downloadResourceInstanceArtifactByUUIDsEither = Either.left(responsePayload);

        given(artifactsBusinessLogic.downloadResourceInstanceArtifactByUUIDs(any(), any(), any(),
                any()))
                .willAnswer( invocation -> downloadResourceInstanceArtifactByUUIDsEither);

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
                .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
    }

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(ArtifactExternalServlet.class)
                .register(new AbstractBinder() {

                    @Override
                    protected void configure() {
                        bind(request).to(HttpServletRequest.class);
                        bind(userBusinessLogic).to(UserBusinessLogic.class);
                        bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                        bind(componentUtils).to(ComponentsUtils.class);
                        bind(servletUtils).to(ServletUtils.class);
                        bind(resourceImportManager).to(ResourceImportManager.class);
                        bind(artifactsBusinessLogic).to(ArtifactsBusinessLogic.class);
                    }
                })
                .property("contextConfig", context);
    }
}