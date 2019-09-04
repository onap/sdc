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

package org.openecomp.sdc.be.distribution.servlet;

import fj.data.Either;
import org.apache.commons.text.StrSubstitutor;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
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
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.TestUtils.downloadedPayloadMatchesExpected;

public class DistributionCatalogServletTest extends JerseyTest {

    private static final HttpServletRequest HTTP_SERVLET_REQUEST = Mockito.mock(HttpServletRequest.class);
    private static final UserBusinessLogic USER_BUSINESS_LOGIC = Mockito.mock(UserBusinessLogic.class);
    private static final ArtifactsBusinessLogic ARTIFACTS_BUSINESS_LOGIC = Mockito.mock(ArtifactsBusinessLogic.class);
    private static final ServletContext SERVLET_CONTEXT = Mockito.mock(ServletContext.class);
    private static final WebAppContextWrapper WEB_APP_CONTEXT_WRAPPER = Mockito.mock(WebAppContextWrapper.class);
    private static final WebApplicationContext WEB_APPLICATION_CONTEXT = Mockito.mock(WebApplicationContext.class);
    private static final ComponentsUtils COMPONENT_UTILS = Mockito.mock(ComponentsUtils.class);
    private static final ResponseFormat OK_RESPONSE_FORMAT = new ResponseFormat(HttpStatus.SC_OK);
    private static final ResponseFormat GENERAL_ERROR_RESPONSE_FORMAT = new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    private static final ResponseFormat CREATED_RESPONSE_FORMAT = new ResponseFormat(HttpStatus.SC_CREATED);
    private static final ResponseFormat NO_CONTENT_RESPONSE_FORMAT = new ResponseFormat(HttpStatus.SC_NO_CONTENT);
    private static final ResponseFormat UNAUTHORIZED_RESPONSE_FORMAT = new ResponseFormat(HttpStatus.SC_UNAUTHORIZED);
    private static final ResponseFormat NOT_FOUND_RESPONSE_FORMAT = new ResponseFormat(HttpStatus.SC_NOT_FOUND);
    private static final ResponseFormat BAD_REQUEST_RESPONSE_FORMAT = new ResponseFormat(HttpStatus.SC_BAD_REQUEST);
    private static final String SERVICE_VERSION = "serviceVersion";
    private static final String ARTIFACT_NAME = "artifactName";
    private static final String SERVICE_NAME = "serviceName";
    private static final String RESOURCE_NAME = "resourceName";
    private static final String RESOURCE_VERSION = "resourceVersion";
    private static final String RESOURCE_INSTANCE_NAME = "resourceInstanceName";
    private static final byte[] BYTE_ARRAY = new byte[]{0xA, 0xB, 0xC, 0xD};

    @BeforeClass
    public static void setup() {
        when(SERVLET_CONTEXT.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(WEB_APP_CONTEXT_WRAPPER);
        when(WEB_APP_CONTEXT_WRAPPER.getWebAppContext(SERVLET_CONTEXT)).thenReturn(WEB_APPLICATION_CONTEXT);

        setUpResponseFormatsForMocks();
        setUpMockTestConfiguration();
    }

    private static void setUpMockTestConfiguration() {
        String appConfigDir = "src/test/resources/config";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);

        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    private static void setUpResponseFormatsForMocks() {
        when(COMPONENT_UTILS.getResponseFormat(ActionStatus.RESTRICTED_OPERATION)).thenReturn(UNAUTHORIZED_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(ActionStatus.OK)).thenReturn(OK_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(ActionStatus.CREATED)).thenReturn(CREATED_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(ActionStatus.NO_CONTENT)).thenReturn(NO_CONTENT_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(BAD_REQUEST_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(GENERAL_ERROR_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(any(ComponentException.class)))
                .thenReturn(GENERAL_ERROR_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(eq(ActionStatus.RESOURCE_NOT_FOUND), any())).thenReturn(NOT_FOUND_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(eq(ActionStatus.COMPONENT_VERSION_NOT_FOUND), any())).thenReturn(NOT_FOUND_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(eq(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND), any())).thenReturn(NOT_FOUND_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(eq(ActionStatus.EXT_REF_NOT_FOUND), any())).thenReturn(NOT_FOUND_RESPONSE_FORMAT);
        when(COMPONENT_UTILS.getResponseFormat(eq(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID), any())).thenReturn(BAD_REQUEST_RESPONSE_FORMAT);
        ByResponseFormatComponentException ce = Mockito.mock(ByResponseFormatComponentException.class);
        when(ce.getResponseFormat()).thenReturn(UNAUTHORIZED_RESPONSE_FORMAT);
    }

    @Before
    public void resetSomeMocks() {
        reset(ARTIFACTS_BUSINESS_LOGIC);
    }

    @Test
    public void downloadServiceArtifactMissingInstanceIdHeaderTest() {
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, SERVICE_NAME);
        parametersMap.put(SERVICE_VERSION, SERVICE_VERSION);
        parametersMap.put(ARTIFACT_NAME, ARTIFACT_NAME);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Response response = target()
                .path(path)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void downloadServiceArtifactNoArtifactFoundTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<byte[], ResponseFormat> downloadServiceArtifactEither = Either.right(NOT_FOUND_RESPONSE_FORMAT);

        when(ARTIFACTS_BUSINESS_LOGIC.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName))
                .thenReturn(downloadServiceArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void downloadServiceArtifactExceptionDuringProcessingTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(ARTIFACTS_BUSINESS_LOGIC.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName))
                .thenThrow(new RuntimeException("Test exception: downloadServiceArtifact"));

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void downloadServiceArtifactTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<byte[], ResponseFormat> downloadServiceArtifactEither = Either.left(BYTE_ARRAY);
        when(ARTIFACTS_BUSINESS_LOGIC.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName))
                .thenReturn(downloadServiceArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertTrue(response.getHeaders().containsKey(Constants.CONTENT_DISPOSITION_HEADER));
        assertTrue(downloadedPayloadMatchesExpected(response, BYTE_ARRAY));
    }

    @Test
    public void downloadResouceArtifactNoArtifactFoundTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String resourceName = RESOURCE_NAME;
        String resourceVersion = RESOURCE_VERSION;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(RESOURCE_VERSION, resourceVersion);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/resources/{resourceName}/{resourceVersion}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<byte[], ResponseFormat> downloadResourceArtifactEither = Either.right(NOT_FOUND_RESPONSE_FORMAT);
        when(ARTIFACTS_BUSINESS_LOGIC.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName,
                resourceVersion, artifactName))
                .thenReturn(downloadResourceArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void downloadResouceArtifactExceptionDuringProcessingTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String resourceName = RESOURCE_NAME;
        String resourceVersion = RESOURCE_VERSION;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(RESOURCE_VERSION, resourceVersion);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/resources/{resourceName}/{resourceVersion}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(ARTIFACTS_BUSINESS_LOGIC.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName,
                resourceVersion, artifactName))
                .thenThrow(new RuntimeException("Test exception: downloadResouceArtifact"));

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void downloadResouceArtifactTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String resourceName = RESOURCE_NAME;
        String resourceVersion = RESOURCE_VERSION;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(RESOURCE_NAME, resourceName);
        parametersMap.put(RESOURCE_VERSION, resourceVersion);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/resources/{resourceName}/{resourceVersion}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<byte[], ResponseFormat> downloadResourceArtifactEither = Either.left(BYTE_ARRAY);
        when(ARTIFACTS_BUSINESS_LOGIC.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName,
                resourceVersion, artifactName))
                .thenReturn(downloadResourceArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertTrue(response.getHeaders().containsKey(Constants.CONTENT_DISPOSITION_HEADER));
        assertTrue(downloadedPayloadMatchesExpected(response, BYTE_ARRAY));
    }

    @Test
    public void downloadResourceInstanceArtifactNoArtifactFoundTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String resourceInstanceName = RESOURCE_INSTANCE_NAME;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(RESOURCE_INSTANCE_NAME, resourceInstanceName);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/resourceInstances/{resourceInstanceName}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<byte[], ResponseFormat> downloadResourceArtifactEither = Either.right(NOT_FOUND_RESPONSE_FORMAT);
        when(ARTIFACTS_BUSINESS_LOGIC.downloadRsrcInstArtifactByNames(serviceName, serviceVersion, resourceInstanceName,
                artifactName))
                .thenReturn(downloadResourceArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void downloadResourceInstanceArtifactExceptionDuringProcessingTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String resourceInstanceName = RESOURCE_INSTANCE_NAME;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(RESOURCE_INSTANCE_NAME, resourceInstanceName);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/resourceInstances/{resourceInstanceName}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        when(ARTIFACTS_BUSINESS_LOGIC.downloadRsrcInstArtifactByNames(serviceName, serviceVersion, resourceInstanceName,
                artifactName))
                .thenThrow(new RuntimeException("Test exception: ownloadResourceInstanceArtifact"));

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void downloadResourceInstanceArtifactTest() {
        String serviceName = SERVICE_NAME;
        String serviceVersion = SERVICE_VERSION;
        String resourceInstanceName = RESOURCE_INSTANCE_NAME;
        String artifactName = ARTIFACT_NAME;

        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(SERVICE_NAME, serviceName);
        parametersMap.put(SERVICE_VERSION, serviceVersion);
        parametersMap.put(RESOURCE_INSTANCE_NAME, resourceInstanceName);
        parametersMap.put(ARTIFACT_NAME, artifactName);

        String formatEndpoint = "/v1/catalog/services/{serviceName}/{serviceVersion}/resourceInstances/{resourceInstanceName}/artifacts/{artifactName}";
        String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{", "}");

        Either<byte[], ResponseFormat> downloadResourceArtifactEither = Either.left(BYTE_ARRAY);
        when(ARTIFACTS_BUSINESS_LOGIC.downloadRsrcInstArtifactByNames(serviceName, serviceVersion, resourceInstanceName,
                artifactName))
                .thenReturn(downloadResourceArtifactEither);

        Response response = target()
                .path(path)
                .request()
                .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertTrue(response.getHeaders().containsKey(Constants.CONTENT_DISPOSITION_HEADER));
        assertTrue(downloadedPayloadMatchesExpected(response, BYTE_ARRAY));
    }

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(DistributionCatalogServlet.class)
                .register(new AbstractBinder() {

                    @Override
                    protected void configure() {
                        bind(HTTP_SERVLET_REQUEST).to(HttpServletRequest.class);
                        bind(USER_BUSINESS_LOGIC).to(UserBusinessLogic.class);
                        bind(COMPONENT_UTILS).to(ComponentsUtils.class);
                        bind(ARTIFACTS_BUSINESS_LOGIC).to(ArtifactsBusinessLogic.class);
                    }
                })
                .property("contextConfig", context);
    }
}