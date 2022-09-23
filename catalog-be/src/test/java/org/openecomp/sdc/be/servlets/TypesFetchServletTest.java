/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ArtifactTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.CapabilitiesBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.RelationshipTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.servlets.builder.ServletResponseBuilder;
import org.openecomp.sdc.be.servlets.exception.OperationExceptionMapper;
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

class TypesFetchServletTest extends JerseyTest {
    private static final String USER_ID = "cs0008";
    private static final User USER = new User(USER_ID);

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private ServletContext servletContext;
    @Mock
    private WebAppContextWrapper webAppContextWrapper;
    @Mock
    private WebApplicationContext webApplicationContext;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private UserBusinessLogic userBusinessLogic;
    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ServletUtils servletUtils;
    @Mock
    private ResourceImportManager resourceImportManager;
    @Mock
    private PropertyBusinessLogic propertyBusinessLogic;
    @Mock
    private RelationshipTypeBusinessLogic relationshipTypeBusinessLogic;
    @Mock
    private CapabilitiesBusinessLogic capabilitiesBusinessLogic;
    @Mock
    private InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;
    @Mock
    private ResourceBusinessLogic resourceBusinessLogic;
    @Mock
    private ArtifactTypeBusinessLogic artifactTypeBusinessLogic;
    @Mock
    private ResponseFormatManager responseFormatManager;
    @Mock
    private ModelOperation modelOperation;

    private final Path rootPath = Path.of("/v1/catalog");
    private final Path nodeTypesPath = rootPath.resolve("nodeTypes");
    private User user;

    @BeforeEach
    void resetMock() throws Exception {
        super.setUp();
        initMocks();
        initConfig();
        initTestData();
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Test
    void testGetAllNodeTypesServlet() {
        final String modelName = "ETSI-SOL001-331";
        Resource res1 = new Resource();
        res1.setName("node type 1");
        res1.setToscaResourceName("toscaResName1");
        Either<List<Component>, ResponseFormat> actionResponse =
            Either.left(List.of());
        Either<List<Component>, ResponseFormat> actionResponseNonAbstract =
            Either.left(List.of(res1));
        when(responseFormat.getStatus())
            .thenReturn(HttpStatus.OK_200);
        when(componentsUtils.getResponseFormat(ActionStatus.OK))
            .thenReturn(responseFormat);
        when(servletUtils.getUserAdmin())
            .thenReturn(userBusinessLogic);
        when(userBusinessLogic.getUser(anyString()))
            .thenReturn(user);
        when(resourceBusinessLogic.getLatestVersionNotAbstractComponentsMetadata(
            true,
            HighestFilterEnum.HIGHEST_ONLY,
            ComponentTypeEnum.RESOURCE,
            null,
            user.getUserId(),
            modelName,
            false)
        )
            .thenReturn(actionResponse);

        when(resourceBusinessLogic.getLatestVersionNotAbstractComponentsMetadata(
            false,
            HighestFilterEnum.HIGHEST_ONLY,
            ComponentTypeEnum.RESOURCE,
            null,
            user.getUserId(),
            modelName,
            false)
        )
            .thenReturn(actionResponseNonAbstract);

        final var response = target()
            .path(nodeTypesPath.toString())
            .queryParam("model", modelName)
            .request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, USER_ID)
            .get();
        assertEquals(response.getStatus(), HttpStatus.OK_200);
        final Map<String, Map<String, Object>> actualResponseContent = response.readEntity(Map.class);
        assertTrue(actualResponseContent.containsKey("toscaResName1"));
        final Map<String, Object> component = actualResponseContent.get("toscaResName1");
        final Map<String, Map<String, String>> componentMetadata = (Map<String, Map<String, String>>) component.get("componentMetadataDefinition");
        final Map<String, String> componentMetadataDefinition = componentMetadata.get("componentMetadataDataDefinition");
        assertEquals(res1.getName(), componentMetadataDefinition.get("name"));
        assertEquals(res1.getComponentType().name(), componentMetadataDefinition.get("componentType"));
        assertEquals(res1.getToscaResourceName(), componentMetadataDefinition.get("toscaResourceName"));
        assertEquals(res1.getResourceType().getValue(), componentMetadataDefinition.get("resourceType"));
    }

    @Override
    protected ResourceConfig configure() {
        MockitoAnnotations.openMocks(this);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        final ApplicationContext context =
            new AnnotationConfigApplicationContext(SpringConfig.class);
        return new ResourceConfig(TypesFetchServlet.class)
            .register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(request).to(HttpServletRequest.class);
                    bind(userBusinessLogic).to(UserBusinessLogic.class);
                    bind(componentInstanceBusinessLogic).to(ComponentInstanceBusinessLogic.class);
                    bind(componentsUtils).to(ComponentsUtils.class);
                    bind(servletUtils).to(ServletUtils.class);
                    bind(resourceImportManager).to(ResourceImportManager.class);
                    bind(resourceBusinessLogic).to(ResourceBusinessLogic.class);
                    bind(propertyBusinessLogic).to(PropertyBusinessLogic.class);
                    bind(relationshipTypeBusinessLogic).to(RelationshipTypeBusinessLogic.class);
                    bind(capabilitiesBusinessLogic).to(CapabilitiesBusinessLogic.class);
                    bind(interfaceOperationBusinessLogic).to(InterfaceOperationBusinessLogic.class);
                    bind(artifactTypeBusinessLogic).to(ArtifactTypeBusinessLogic.class);
                    bind(modelOperation).to(ModelOperation.class);
                }
            })
            .register(new OperationExceptionMapper(
                new ServletResponseBuilder(),
                responseFormatManager
            ))
            .property("contextConfig", context);
    }

    void initMocks() {
        when(request.getSession())
            .thenReturn(session);
        when(session.getServletContext())
            .thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
            .thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext))
            .thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(ComponentInstanceBusinessLogic.class))
            .thenReturn(componentInstanceBusinessLogic);
        when(webApplicationContext.getBean(UserBusinessLogic.class))
            .thenReturn(userBusinessLogic);
        when(userBusinessLogic.getUser(USER_ID, false))
            .thenReturn(USER);
        when(request.getHeader(Constants.USER_ID_HEADER))
            .thenReturn(USER_ID);
        when(webApplicationContext.getBean(ServletUtils.class))
            .thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils())
            .thenReturn(componentsUtils);
        when(webApplicationContext.getBean(ResourceImportManager.class))
            .thenReturn(resourceImportManager);
        when(webApplicationContext.getBean(PropertyBusinessLogic.class))
            .thenReturn(propertyBusinessLogic);
        when(webApplicationContext.getBean(RelationshipTypeBusinessLogic.class))
            .thenReturn(relationshipTypeBusinessLogic);
        when(webApplicationContext.getBean(CapabilitiesBusinessLogic.class))
            .thenReturn(capabilitiesBusinessLogic);
        when(webApplicationContext.getBean(InterfaceOperationBusinessLogic.class))
            .thenReturn(interfaceOperationBusinessLogic);
        when(webApplicationContext.getBean(ResourceBusinessLogic.class))
            .thenReturn(resourceBusinessLogic);
        when(webApplicationContext.getBean(ArtifactTypeBusinessLogic.class))
            .thenReturn(artifactTypeBusinessLogic);
        when(webApplicationContext.getBean(ModelOperation.class))
            .thenReturn(modelOperation);
    }

    void initConfig() {
        final String appConfigDir = "src/test/resources/config/catalog-be";
        final ConfigurationSource configurationSource =
            new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        final ConfigurationManager configurationManager =
            new ConfigurationManager(configurationSource);
        final org.openecomp.sdc.be.config.Configuration configuration =
            new org.openecomp.sdc.be.config.Configuration();
        configuration.setJanusGraphInMemoryGraph(true);
        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    private void initTestData() {
        user = new User();
        user.setUserId(USER_ID);
        user.setRole(Role.ADMIN.name());
        when(userBusinessLogic.getUser(USER_ID)).thenReturn(user);
    }
}