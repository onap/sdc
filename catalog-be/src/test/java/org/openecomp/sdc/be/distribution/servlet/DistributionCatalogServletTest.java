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
import javax.servlet.http.HttpSession;
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

    public static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    public static final HttpSession session = Mockito.mock(HttpSession.class);
    public static final UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
    public static final ArtifactsBusinessLogic artifactsBusinessLogic = Mockito.mock(ArtifactsBusinessLogic.class);

    private static final ServletContext servletContext = Mockito.mock(ServletContext.class);
    public static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    private static final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
    private static final ComponentsUtils componentUtils = Mockito.mock(ComponentsUtils.class);
    private static final ResponseFormat okResponseFormat = new ResponseFormat(HttpStatus.SC_OK);
    private static final ResponseFormat generalErrorResponseFormat = new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    private static final ResponseFormat createdResponseFormat = new ResponseFormat(HttpStatus.SC_CREATED);
    private static final ResponseFormat noContentResponseFormat = new ResponseFormat(HttpStatus.SC_NO_CONTENT);
    private static final ResponseFormat unauthorizedResponseFormat = new ResponseFormat(HttpStatus.SC_UNAUTHORIZED);
    private static final ResponseFormat notFoundResponseFormat = new ResponseFormat(HttpStatus.SC_NOT_FOUND);
    private static final ResponseFormat badRequestResponseFormat = new ResponseFormat(HttpStatus.SC_BAD_REQUEST);
    private static final String SERVICE_VERSION = "serviceVersion";
    private static final String ARTIFACT_NAME = "artifactName";
    private static final String SERVICE_NAME = "serviceName";
    private static final byte[] BYTE_ARRAY = new byte[]{0xA, 0xB, 0xC, 0xD};
    private static final String RESOURCE_NAME = "resourceName";
    private static final String RESOURCE_VERSION = "resourceVersion";
    private static final String RESOURCE_INSTANCE_NAME = "resourceInstanceName";

    @BeforeClass
    public static void setup() {

        //Needed for User Authorization
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(componentUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION)).thenReturn(unauthorizedResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(okResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(createdResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.NO_CONTENT)).thenReturn(noContentResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(badRequestResponseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(generalErrorResponseFormat);
        when(componentUtils.getResponseFormat(any(ComponentException.class)))
                .thenReturn(generalErrorResponseFormat);

        ByResponseFormatComponentException ce = Mockito.mock(ByResponseFormatComponentException.class);
        when(ce.getResponseFormat()).thenReturn(unauthorizedResponseFormat);

        //Needed for error configuration
        when(componentUtils.getResponseFormat(eq(ActionStatus.RESOURCE_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_VERSION_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.EXT_REF_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID), any())).thenReturn(badRequestResponseFormat);

        String appConfigDir = "src/test/resources/config";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);

        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @Before
    public void resetSomeMocks() {
        reset(artifactsBusinessLogic);
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

        Either<byte[], ResponseFormat> downloadServiceArtifactEither = Either.right(notFoundResponseFormat);

        when(artifactsBusinessLogic.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName))
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

        when(artifactsBusinessLogic.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName))
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
        when(artifactsBusinessLogic.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName))
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

        Either<byte[], ResponseFormat> downloadResourceArtifactEither = Either.right(notFoundResponseFormat);
        when(artifactsBusinessLogic.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName,
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

        when(artifactsBusinessLogic.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName,
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
        when(artifactsBusinessLogic.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName,
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

        Either<byte[], ResponseFormat> downloadResourceArtifactEither = Either.right(notFoundResponseFormat);
        when(artifactsBusinessLogic.downloadRsrcInstArtifactByNames(serviceName, serviceVersion, resourceInstanceName,
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

        when(artifactsBusinessLogic.downloadRsrcInstArtifactByNames(serviceName, serviceVersion, resourceInstanceName,
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
        when(artifactsBusinessLogic.downloadRsrcInstArtifactByNames(serviceName, serviceVersion, resourceInstanceName,
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
                        bind(request).to(HttpServletRequest.class);
                        bind(userBusinessLogic).to(UserBusinessLogic.class);
                        bind(componentUtils).to(ComponentsUtils.class);
                        bind(artifactsBusinessLogic).to(ArtifactsBusinessLogic.class);
                    }
                })
                .property("contextConfig", context);
    }
}