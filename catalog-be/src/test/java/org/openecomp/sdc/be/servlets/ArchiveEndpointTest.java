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

package org.openecomp.sdc.be.servlets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ArchiveBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.path.utils.GraphTestUtils;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.DAOTitanStrategy;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.HealingTitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.titan.HealingTitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.jsontitan.operations.ArchiveOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.CategoryOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.GroupsOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
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

public class ArchiveEndpointTest extends JerseyTest {

    private static final ServletContext servletContext = mock(ServletContext.class);
    private static final String CSAR_UUID1 = "123456789abcdefgh";
    private static final String CSAR_UUID2 = "987654321abcdefgh";

    public static final WebAppContextWrapper webAppContextWrapper = mock(WebAppContextWrapper.class);
    private static final WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    private static final ServletUtils servletUtils = mock(ServletUtils.class);
    private static final UserBusinessLogic userAdmin = mock(UserBusinessLogic.class);
    private static final ComponentsUtils componentUtils = mock(ComponentsUtils.class);
    private static final ResponseFormat responseFormat = mock(ResponseFormat.class);
    private static final ResponseFormat notFoundResponseFormat = mock(ResponseFormat.class);
    private static final ResponseFormat badRequestResponseFormat = mock(ResponseFormat.class);
    private static final ResponseFormat invalidServiceStateResponseFormat = mock(ResponseFormat.class);
    private static final AccessValidations accessValidationsMock = mock(AccessValidations.class);
    private static final ComponentValidations componentValidationsMock = mock(ComponentValidations.class);
    private static final IGraphLockOperation graphLockOperation = mock(IGraphLockOperation.class);
    private static final HealingTitanGenericDao titanGenericDao = mock(HealingTitanGenericDao.class);
    private static final HealingPipelineDao HEALING_PIPELINE_DAO = mock(HealingPipelineDao.class);
    private static final ICacheMangerOperation cacheManagerOperation = mock(ICacheMangerOperation.class);
    private static GraphVertex serviceVertex;
    private static GraphVertex resourceVertex;
    private static GraphVertex resourceVertexVspArchived;

    private static HealingTitanDao titanDao;

    @Configuration
    @PropertySource("classpath:dao.properties")
    static class TestSpringConfig {
        private ArchiveOperation archiveOperation;
        private GraphVertex catalogVertex;

        @Bean
        ArchiveEndpoint archiveEndpoint() {
            return new ArchiveEndpoint(archiveBusinessLogic());
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
        ArchiveBusinessLogic archiveBusinessLogic() {
            return new ArchiveBusinessLogic(titanDao(), accessValidations(), archiveOperation(), toscaOperationFacade(), componentUtils);
        }

        @Bean
        ArchiveOperation archiveOperation() {
            this.archiveOperation = new ArchiveOperation(titanDao(), graphLockOperation());
            GraphTestUtils.clearGraph(titanDao);
            initGraphForTest();
            return this.archiveOperation;
        }

        @Bean
        ComponentValidations componentValidations() {
            return componentValidationsMock;
        }

        @Bean
        AccessValidations accessValidations() {
            return accessValidationsMock;
        }

        @Bean
        ToscaOperationFacade toscaOperationFacade() {
            return new ToscaOperationFacade();
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
        NodeTemplateOperation nodeTemplateOperation() {
            return new NodeTemplateOperation();
        }

        @Bean
        GroupsOperation groupsOperation() {
            return new GroupsOperation();
        }

        @Bean
        ICacheMangerOperation cacheMangerOperation() {
            return cacheManagerOperation;
        }

        @Bean
        HealingTitanDao titanDao() {
            titanDao = new HealingTitanDao(titanGraphClient());
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
        CategoryOperation categoryOperation() {
            return new CategoryOperation();
        }

        @Bean
        IGraphLockOperation graphLockOperation() {
            return graphLockOperation;
        }

        @Bean
        TitanGenericDao titanGenericDao() {
            return titanGenericDao;
        }

        @Bean
        HealingPipelineDao healingPipelineDao(){
            return HEALING_PIPELINE_DAO;
        }

        private void initGraphForTest() {
            //Create Catalog Root
            catalogVertex = GraphTestUtils.createRootCatalogVertex(titanDao);
            //Create Archive Root
            GraphTestUtils.createRootArchiveVertex(titanDao);

            createSingleVersionServiceAndResource();
        }

        private void createSingleVersionServiceAndResource() {
            //Create Service for Scenario 1 Tests (1 Service)
            serviceVertex = GraphTestUtils.createServiceVertex(titanDao, propsForHighestVersion());


            Map<GraphPropertyEnum, Object> props = propsForHighestVersion();
            props.put(GraphPropertyEnum.IS_VSP_ARCHIVED, false);
            props.put(GraphPropertyEnum.CSAR_UUID, CSAR_UUID1);
            resourceVertex = GraphTestUtils.createResourceVertex(titanDao, props, ResourceTypeEnum.VF);

            props = propsForHighestVersion();
            props.put(GraphPropertyEnum.IS_VSP_ARCHIVED, true);
            props.put(GraphPropertyEnum.CSAR_UUID, CSAR_UUID2);
            resourceVertexVspArchived = GraphTestUtils.createResourceVertex(titanDao, props, ResourceTypeEnum.VF);

            //Connect Service/Resource to Catalog Root
            titanDao.createEdge(catalogVertex, serviceVertex, EdgeLabelEnum.CATALOG_ELEMENT, null);
            titanDao.createEdge(catalogVertex, resourceVertex, EdgeLabelEnum.CATALOG_ELEMENT, null);
        }

        private Map<GraphPropertyEnum, Object> propsForHighestVersion(){
            Map<GraphPropertyEnum, Object> props = new HashMap<>();
            props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
            return props;
        }
    }

    public static final HttpServletRequest request = mock(HttpServletRequest.class);

    /* Users */
    private static final User adminUser = new User("admin", "admin", "admin", "admin@email.com", Role.ADMIN.name(), System.currentTimeMillis());
    private static final User designerUser = new User("designer", "designer", "designer", "designer@email.com", Role.DESIGNER.name(), System
                                                                                                                                              .currentTimeMillis());
    private static final User otherUser = new User("other", "other", "other", "other@email.com", Role.OPS.name(), System.currentTimeMillis());

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
        when(componentUtils.getResponseFormat(eq(ActionStatus.INVALID_SERVICE_STATE), any())).thenReturn(invalidServiceStateResponseFormat);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.UNAUTHORIZED.value());

        ComponentException ce = new ByResponseFormatComponentException(responseFormat);
        doThrow(ce).when(accessValidationsMock).userIsAdminOrDesigner(eq(otherUser.getUserId()), any());

        //Needed for error configuration
        when(notFoundResponseFormat.getStatus()).thenReturn(HttpStatus.NOT_FOUND.value());
        when(invalidServiceStateResponseFormat.getStatus()).thenReturn(HttpStatus.CONFLICT.value());
        when(badRequestResponseFormat.getStatus()).thenReturn(HttpStatus.BAD_REQUEST.value());
        when(componentUtils.getResponseFormat(eq(ActionStatus.RESOURCE_NOT_FOUND), (String[]) any())).thenReturn(notFoundResponseFormat);
        when(componentUtils.getResponseFormat(eq(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID), (String[]) any())).thenReturn(badRequestResponseFormat);

        when(graphLockOperation.lockComponent(anyString(), any(NodeTypeEnum.class))).thenReturn(StorageOperationStatus.OK);

        Either<User, ActionStatus> adminEither = Either.left(adminUser);
        Either<User, ActionStatus> designerEither = Either.left(designerUser);
        Either<User, ActionStatus> otherEither = Either.left(otherUser);

        when(userAdmin.getUser(adminUser.getUserId(), false)).thenReturn(adminEither);
        when(userAdmin.getUser(designerUser.getUserId(), false)).thenReturn(designerEither);
        when(userAdmin.getUser(otherUser.getUserId(), false)).thenReturn(otherEither);
        //========================================================================================================================

        String appConfigDir = "src/test/resources/config";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

        org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
        configuration.setTitanInMemoryGraph(true);

        configurationManager.setConfiguration(configuration);
        ExternalConfiguration.setAppName("catalog-be");
    }

    @Test
    public void getArchivedComponents_Empty() {
        assertOnGetArchivedComponents(null, 0);
    }

    @Test
    public void archiveAndGetArchivedComponents_SingleComponent() {
        archiveComponent(serviceVertex.getUniqueId(), HttpStatus.OK.value());
        assertOnGetArchivedComponents(ComponentTypeEnum.SERVICE_PARAM_NAME, 1);
    }

    @Test
    public void attemptArchiveCheckedOutService() {
        checkoutComponent(serviceVertex);
        archiveComponent(serviceVertex.getUniqueId(), HttpStatus.CONFLICT.value());
    }

    @Test
    public void testOnArchivedVsps(){
        String path = "/v1/catalog/notif/vsp/archived";
        List<String> csarIds = new LinkedList<>();
        csarIds.add("123456");
        csarIds.add(CSAR_UUID2);   //An archived CSAR ID
        Response response = target()
                                    .path(path)
                                    .request(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                                    .post(Entity.json(csarIds));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertOnVertexProp(resourceVertexVspArchived.getUniqueId(), true);
    }

    @Test
    public void testOnRestoredVsps(){
        String path = "/v1/catalog/notif/vsp/restored";
        List<String> csarIds = new LinkedList<>();
        csarIds.add("123456");
        csarIds.add(CSAR_UUID1);   //Non archived CSAR_ID
        Response response = target()
                                    .path(path)
                                    .request(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                                    .post(Entity.json(csarIds));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertOnVertexProp(resourceVertex.getUniqueId(), false);
    }

    /*
     *   Rainy Scenarios
     */
    @Test
    public void archiveWithInvalidUid() {
        archiveComponent("fakeUid", HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void restoreWithInvalidUid() {
        restoreComponent("fakeUid", HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void archiveWithTester() {
        String path = String.format("/v1/catalog/services/%s/%s", serviceVertex.getUniqueId(), "archive");
        Response response = target()
                                    .path(path)
                                    .request()
                                    .accept(MediaType.APPLICATION_JSON)
                                    .header(Constants.USER_ID_HEADER, otherUser.getUserId())
                                    .post(null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    private void checkoutComponent(GraphVertex component) {
        Either<GraphVertex, TitanOperationStatus> vE = titanDao.getVertexById(component.getUniqueId());
        GraphVertex v = vE.left().value();
        v.addMetadataProperty(GraphPropertyEnum.STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        v.setJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        titanDao.updateVertex(v);
        titanDao.commit();
    }

    private void assertOnVertexProp(String componentId, Object expectedValue) {
        Either<GraphVertex, TitanOperationStatus> vE = titanDao.getVertexById(componentId);
        GraphVertex v = vE.left().value();
        assertThat(v.getMetadataProperty(GraphPropertyEnum.IS_VSP_ARCHIVED)).isEqualTo(expectedValue);
    }

    private void archiveComponent(String id, int expectedStatus){ archiveOrRestoreComponent(id, ArchiveOperation.Action.ARCHIVE, expectedStatus); }
    private void restoreComponent(String id, int expectedStatus){ archiveOrRestoreComponent(id, ArchiveOperation.Action.RESTORE, expectedStatus); }

    private void archiveOrRestoreComponent(String compUid, ArchiveOperation.Action action, int expectedStatus) {
        String path = String.format("/v1/catalog/services/%s/%s", compUid, action.name().toLowerCase());
        Response response = target()
                                    .path(path)
                                    .request()
                                    .accept(MediaType.APPLICATION_JSON)
                                    .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                                    .post(null);

        assertThat(response.getStatus()).isEqualTo(expectedStatus);
    }

    private void assertOnGetArchivedComponents(String componentType, int expectedCount) {
        String path = "/v1/catalog/archive";

        Response response = target()
                                    .path(path)
                                    .request()
                                    .accept(MediaType.APPLICATION_JSON)
                                    .header(Constants.USER_ID_HEADER, designerUser.getUserId())
                                    .get();

        Map<String, List<CatalogComponent>> archivedComponents = response.readEntity(new GenericType<Map<String, List<CatalogComponent>>>() {  });
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        if (componentType == null) {
            assertThat(archivedComponents).isEmpty();
        } else {
            assertThat(archivedComponents.get(componentType)).hasSize(expectedCount);
        }

    }

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(TestSpringConfig.class);
        return new ResourceConfig(ArchiveEndpoint.class)
                       .register(DefaultExceptionMapper.class)
                       .register(ComponentExceptionMapper.class)
                       .register(StorageExceptionMapper.class)
                       .property("contextConfig", context);
    }
}
