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


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.externalapi.servlet.representation.ResourceAssetMetadata;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

class AssetsDataServletTest extends JerseyTest {

    private static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private static final HttpSession session = Mockito.mock(HttpSession.class);
    private static final ServletContext servletContext = Mockito.mock(ServletContext.class);
    private static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    private static final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
    private static final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
    private static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    private static final ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
    private static final ResourceBusinessLogic resourceBusinessLogic = Mockito.mock(ResourceBusinessLogic.class);
    private static final ServiceBusinessLogic serviceBusinessLogic = Mockito.mock(ServiceBusinessLogic.class);
    private static final ElementBusinessLogic elementBusinessLogic = Mockito.mock(ElementBusinessLogic.class);
    private static final Resource resource = Mockito.mock(Resource.class);
    private static final CategoryDefinition categoryDefinition = Mockito.mock(CategoryDefinition.class);
    private static final SubCategoryDefinition subCategoryDefinition = Mockito.mock(SubCategoryDefinition.class);
    private static final AssetMetadataConverter assetMetadataConverter = Mockito.mock(AssetMetadataConverter.class);
    private static final ResourceAssetMetadata resourceAssetMetadata = new ResourceAssetMetadata();
    private static final LifecycleBusinessLogic lifecycleBusinessLogic = Mockito.mock(LifecycleBusinessLogic.class);
    private static final UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
    private static final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito
        .mock(ComponentInstanceBusinessLogic.class);


    @BeforeAll
    public static void setup() {
        ExternalConfiguration.setAppName("catalog-be");
        when(request.getSession()).thenReturn(session);
        when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn("mockXEcompInstanceId");
        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn("mockAttID");
        when(request.getRequestURL()).thenReturn(new StringBuffer("sdc/v1/catalog/resources"));

        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
            .thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);

        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(webApplicationContext.getBean(ResourceBusinessLogic.class)).thenReturn(resourceBusinessLogic);

        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        mockResponseFormat();

        when(resource.getName()).thenReturn("MockVFCMT");
        when(resource.getSystemName()).thenReturn("mockvfcmt");
        Either<Resource, ResponseFormat> eitherRet = Either.left(resource);
        when(componentsUtils
            .convertJsonToObjectUsingObjectMapper(Mockito.any(), Mockito.any(), Mockito.eq(Resource.class),
                Mockito.any(), Mockito.eq(ComponentTypeEnum.RESOURCE))).thenReturn(eitherRet);

        when(webApplicationContext.getBean(ResourceImportManager.class)).thenReturn(resourceImportManager);
        when(webApplicationContext.getBean(ElementBusinessLogic.class)).thenReturn(elementBusinessLogic);
        when(categoryDefinition.getName()).thenReturn("Template");
        when(subCategoryDefinition.getName()).thenReturn("Monitoring Template");
        when(categoryDefinition.getSubcategories()).thenReturn(Arrays.asList(subCategoryDefinition));
        when(elementBusinessLogic.getAllResourceCategories())
            .thenReturn(Either.left(Arrays.asList(categoryDefinition)));
        when(resourceBusinessLogic
            .createResource(Mockito.eq(resource), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(resource);
        when(webApplicationContext.getBean(AssetMetadataConverter.class)).thenReturn(assetMetadataConverter);
        when(request.isUserInRole(anyString())).thenReturn(true);

        Mockito.doReturn(Either.left(resourceAssetMetadata)).when(assetMetadataConverter)
            .convertToSingleAssetMetadata(Mockito.eq(resource), Mockito.anyString(),
                Mockito.eq(true));

        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
            appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);

        configurationManager.setConfiguration(configuration);
    }

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private static void mockResponseFormat() {
        when(componentsUtils.getResponseFormat(Mockito.any(ActionStatus.class), Mockito.any(String[].class)))
            .thenAnswer((Answer<ResponseFormat>) invocation -> {
                ResponseFormat ret;
                final ActionStatus actionStatus = invocation.getArgument(0);
                switch (actionStatus) {
                    case CREATED: {
                        ret = new ResponseFormat(HttpStatus.SC_CREATED);
                        break;
                    }
                    default: {
                        ret = new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        break;
                    }
                }
                return ret;
            });
    }


    @Test
    void createVfcmtHappyScenario() {
        final JSONObject createRequest = buildCreateJsonRequest();
        Response response = target().path("/v1/catalog/resources").request(MediaType.APPLICATION_JSON)
            .header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId")
            .header(Constants.USER_ID_HEADER, "mockAttID")
            .post(Entity.json(createRequest.toJSONString()), Response.class);
        assertEquals(HttpStatus.SC_CREATED, response.getStatus());

    }

    private static final String BASIC_CREATE_REQUEST = "{\r\n" +
        "  \"name\": \"VFCMT_1\",\r\n" +
        "  \"description\": \"VFCMT Description\",\r\n" +
        "  \"resourceType\" : \"VFCMT\",\r\n" +
        "  \"category\": \"Template\",\r\n" +
        "  \"subcategory\": \"Monitoring Template\",\r\n" +
        "  \"vendorName\" : \"DCAE\",\r\n" +
        "  \"vendorRelease\" : \"1.0\",\r\n" +
        "  \"tags\": [\r\n" +
        "    \"VFCMT_1\"\r\n" +
        "  ],\r\n" +
        "  \"icon\" : \"defaulticon\",\r\n" +
        "  \"contactId\": \"cs0008\"\r\n" +
        "}";

    private JSONObject buildCreateJsonRequest() {

        JSONParser parser = new JSONParser();
        return (JSONObject) FunctionalInterfaces.swallowException(() -> parser.parse(BASIC_CREATE_REQUEST));

    }

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new ResourceConfig()
            .register(new CrudExternalServlet(componentInstanceBusinessLogic, componentsUtils,
                servletUtils, resourceImportManager, elementBusinessLogic, assetMetadataConverter,
                lifecycleBusinessLogic, resourceBusinessLogic, serviceBusinessLogic))
            .register(new AbstractBinder() {

                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                }
            })
            .property("contextConfig", context);
    }
}
