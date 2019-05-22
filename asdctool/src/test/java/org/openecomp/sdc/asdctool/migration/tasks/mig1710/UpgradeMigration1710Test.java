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
package org.openecomp.sdc.asdctool.migration.tasks.mig1710;

import com.google.common.collect.Lists;
import fj.data.Either;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.tasks.handlers.XlsOutputHandler;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.scheduledtasks.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.http.client.api.HttpRequestHandler;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeMigration1710Test {

    private static final String USER = "jh0003";
    private static final String CONF_LEVEL = "5.0";
    private static final String COMPONENT_UNIQUE_ID = "12345";
    private static final String OLD_VERSION = "1.0";
    private static final String UPDATED_VERSION = "2.0";
    private static final String CSAR_UUID = "1234578";
    private static HttpRequestHandler originHandler;

    private final User user = new User();

    @InjectMocks
    private UpgradeMigration1710 migration = new UpgradeMigration1710();
    @Mock
    private IUserAdminOperation userAdminOperation;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private LifecycleBusinessLogic lifecycleBusinessLogic;
    @Mock
    private TitanDao titanDao;
    @Mock
    private ComponentsUtils componentUtils;
    @Mock
    private CsarOperation csarOperation;
    @Mock
    private ConfigurationSource configurationSource;
    //don't remove - it is intended to avoid the xls file generating
    @Mock
    private XlsOutputHandler outputHandler;
    @Mock
    private ResourceBusinessLogic resourceBusinessLogic;
    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private ComponentsCleanBusinessLogic componentsCleanBusinessLogic;

    private static ConfigurationManager configurationManager;
    private static List<String> resources = Stream.of("org.openecomp.resource.cp.extCP").collect(Collectors.toList());
    private static Map<String, List<String>> resourcesForUpgrade;

    private Resource resource;
    private Service service;
    private List<String> vfList = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() {
        resourcesForUpgrade = new HashMap<>();
        resourcesForUpgrade.put(CONF_LEVEL, resources);
        originHandler = HttpRequestHandler.get();
    }

    @AfterClass
    public static void tearDownClass() {
        //put the origin handler back
        HttpRequestHandler.setTestInstance(originHandler);
    }

    @Before
    public void setUp() {
        user.setUserId(USER);
        configurationManager = new ConfigurationManager(configurationSource);
        configurationManager.setConfiguration(new Configuration());
        configurationManager.getConfiguration().setSkipUpgradeVSPs(true);
        configurationManager.getConfiguration().setSkipUpgradeFailedVfs(true);
        configurationManager.getConfiguration().setAutoHealingOwner(USER);
        configurationManager.getConfiguration().setSupportAllottedResourcesAndProxy(true);
        configurationManager.getConfiguration().setDeleteLockTimeoutInSeconds(10);
        configurationManager.getConfiguration().setMaxDeleteComponents(5);
        configurationManager.getConfiguration().setEnableAutoHealing(true);
        configurationManager.getConfiguration().setToscaConformanceLevel("5.0");
        HashMap<String, List<String>> resourcesForUpgrade = new HashMap();
        resourcesForUpgrade.put("5.0", Lists.newArrayList("port"));
        configurationManager.getConfiguration().setResourcesForUpgrade(resourcesForUpgrade);

        migration.init();
        migration.setNodeTypesSupportOnly(false);
        when(componentsCleanBusinessLogic.lockDeleteOperation()).thenReturn(StorageOperationStatus.OK);

        resource = new Resource();
        resource.setCsarUUID(CSAR_UUID);
        resource.setVersion(OLD_VERSION);
        resource.setUniqueId(COMPONENT_UNIQUE_ID);

        service = new Service();
        service.setVersion(OLD_VERSION);
        service.setUniqueId(COMPONENT_UNIQUE_ID);

        vfList.add(COMPONENT_UNIQUE_ID);

        when(responseFormat.getFormattedMessage())
                .thenReturn("");
        when(componentUtils.getResponseFormat(any(ActionStatus.class), any()))
                .thenReturn(responseFormat);
        when(componentUtils.convertFromStorageResponse(any(), any())).thenCallRealMethod();
        mockChangeComponentState();
    }

    @Test
    public void nodeTypesUpgradeFailed() {
        migration.setNodeTypesSupportOnly(true);
        resolveUserAndDefineUpgradeLevel();
        when(titanDao.getByCriteria(any(), any(), any(), any()))
                .thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void migrationDisabled() {
        configurationManager.getConfiguration().setEnableAutoHealing(false);
        migration.init();
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
        verify(titanDao, times(0)).commit();
        verify(titanDao, times(0)).rollback();
    }

    @Test
    public void migrationFailedIfDeleteNodeLockFailed() {
        when(componentsCleanBusinessLogic.lockDeleteOperation())
                .thenReturn(StorageOperationStatus.BAD_REQUEST);
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void migrationFailedIfDeleteNodeLockRetryFailed() {
        when(componentsCleanBusinessLogic.lockDeleteOperation())
                .thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT)
                .thenReturn(StorageOperationStatus.BAD_REQUEST);
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void nodeTypesOnlyUpgradePassed() {
        migration.setNodeTypesSupportOnly(true);
        upgradeAllScenario(false);
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
        verify(titanDao, times(2)).commit();
        verify(titanDao, times(0)).rollback();
    }

    @Test
    public void nodeTypesUpgradePassedAndVFsUpgradeFailedWhenSkipFailedVFsIsNotSupported() {
        final boolean failOnVfUpgrade = true;
        final boolean upgradeServices = false;
        final boolean exceptionOnVfUpgrade = false;
        final boolean upgradeVFC = false;
        final boolean isFailed = true;
        configurationManager.getConfiguration().setSkipUpgradeFailedVfs(false);
        migration.init();
        migration.setNodeTypesSupportOnly(false);
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnVfUpgrade, upgradeServices, upgradeVFC, isFailed);
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
        verify(titanDao, times(1)).commit();
        verify(titanDao, times(2)).rollback();
    }


    @Test
    public void upgradeAllVFsUpgradeFailedOnExceptionWhenSkipFailedVFsIsNotSupported() {
        final boolean failOnVfUpgrade = false;
        final boolean upgradeServices = false;
        final boolean exceptionOnVfUpgrade = true;
        final boolean upgradeVFC = false;
        final boolean isFailed = true;
        configurationManager.getConfiguration().setSkipUpgradeFailedVfs(false);
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnVfUpgrade, upgradeServices, upgradeVFC, isFailed);
        migration.init();
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
        verify(titanDao, times(2)).commit();
        verify(titanDao, times(0)).rollback();
    }

    @Test
    public void upgradeAllIfVFsUpgradeFailedOnExceptionWhenSkipFailedVFsIsSupported() {
        final boolean failOnVfUpgrade = false;
        final boolean upgradeServices = true;
        final boolean exceptionOnFvUpgrade = true;
        final boolean upgradeVFC = false;
        final boolean isFailed = false;
        configurationManager.getConfiguration().setSkipUpgradeFailedVfs(true);
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnFvUpgrade, upgradeServices, upgradeVFC, isFailed);
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
        verify(titanDao, times(3)).commit();
        verify(titanDao, times(1)).rollback();
    }


    @Test
    public void upgradeAll() {
        upgradeAllScenario(true);
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
        verify(titanDao, times(4)).commit();
        verify(titanDao, times(0)).rollback();
    }

    @Test
    public void upgradeAllWhenDeleteLockRetrySucceeded() {
        when(componentsCleanBusinessLogic.lockDeleteOperation())
                .thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT)
                .thenReturn(StorageOperationStatus.OK);
        upgradeAllScenario(true);
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
        verify(titanDao, times(4)).commit();
        verify(titanDao, times(0)).rollback();
    }

    @Test
    public void upgradeAllWhenVspUpgradeIsRequired() {
        final boolean failOnVfUpgrade = false;
        final boolean upgradeServices = true;
        final boolean exceptionOnFvUpgrade = false;
        final boolean upgradeVFC = true;
        final boolean isFailed = true;
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnFvUpgrade, upgradeServices, upgradeVFC, isFailed);
        configurationManager.getConfiguration().setSkipUpgradeVSPs(false);
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void migrationFailedWhenUserNotResolved() {
        when(userAdminOperation.getUserData(anyString(), eq(false))).thenReturn(Either.right(ActionStatus.MISSING_INFORMATION));
        when(titanDao.rollback()).thenReturn(TitanOperationStatus.OK);
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void verifyThatCheckedOutResourcesMarkedAsDeletedIfUpgradeFailed() {
        mockCheckoutFlow();
        when(resourceBusinessLogic.validateAndUpdateResourceFromCsar(any(Resource.class), any(), any(), any(),
                any()))
                .thenThrow(new ByResponseFormatComponentException(responseFormat));
        when(resourceBusinessLogic.deleteResource(anyString(), any()))
                .thenReturn(responseFormat);
        mockChangeComponentState();
        migration.upgradeVFs(vfList, false);
        verify(resourceBusinessLogic).deleteResource(anyString(), any());
    }

    @Test
    public void verifyThatCheckedOutAllottedResourcesMarkedAsDeletedIfUpgradeFailed() {
        mockCheckoutFlow();
        when(resourceBusinessLogic.validateAndUpdateResourceFromCsar(any(Resource.class), any(), any(), any(),
                any()))
                .thenThrow(new ByResponseFormatComponentException(responseFormat));
        when(resourceBusinessLogic.deleteResource(anyString(), any()))
                .thenReturn(responseFormat);
        mockChangeComponentState();
        migration.upgradeVFs(vfList, true);
        verify(resourceBusinessLogic).deleteResource(anyString(), any());
    }

    @Test
    public void verifyThatCheckedOutResourceIsNotMarkedAsDeletedIfUpgradeSucceeded() {
        mockCheckoutFlow();
        resource.setVersion(UPDATED_VERSION);
        when(resourceBusinessLogic.validateAndUpdateResourceFromCsar(any(Resource.class), any(), any(), any(),
                any()))
                .thenReturn(resource);
        mockChangeComponentState();
        migration.upgradeVFs(vfList, true);
        verify(resourceBusinessLogic, times(0)).deleteResource(anyString(), any());
    }

    @Test
    public void verifyThatCheckedOutServicesMarkedAsDeletedIfUpgradeFailed() {
        List<String> servicesForUpgrade = new ArrayList<>();
        servicesForUpgrade.add(COMPONENT_UNIQUE_ID);

        Either<Resource, StorageOperationStatus> foundServices = Either.left(resource);
        mockCheckoutFlow();
        when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class)))
                .thenReturn(Either.left(service));
        when(toscaOperationFacade.getLatestCertifiedByToscaResourceName(any(), any(), any()))
                .thenReturn(foundServices);
        migration.upgradeServices(servicesForUpgrade, component -> true, "services");
        verify(serviceBusinessLogic, times(0)).deleteService(anyString(), any());
    }

    @Test
    public void verifyThatCheckedOutServicesIsNotMarkedAsDeletedIfUpgradeSucceeded() {
        List<String> servicesForUpgrade = new ArrayList<>();
        servicesForUpgrade.add(COMPONENT_UNIQUE_ID);

        mockCheckoutFlow();
        when(toscaOperationFacade.getLatestCertifiedByToscaResourceName(anyString(), any(VertexTypeEnum.class), any(JsonParseFlagEnum.class)))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(serviceBusinessLogic.deleteService(anyString(), any()))
                .thenReturn(responseFormat);
        migration.upgradeServices(servicesForUpgrade, component -> true, "services");
        verify(serviceBusinessLogic).deleteService(anyString(), any());
    }


    @Test
    public void unlockDeleteOperationIsPerformedIfItWasLocked() {
        migration.isLockDeleteOperationSucceeded();
        migration.unlockDeleteOperation();
        verify(componentsCleanBusinessLogic).unlockDeleteOperation();
    }

    @Test
    public void unlockDeleteOperationIsNotPerformedIfItWasNotLocked() {
        when(componentsCleanBusinessLogic.lockDeleteOperation()).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        migration.isLockDeleteOperationSucceeded();
        migration.unlockDeleteOperation();
        verify(componentsCleanBusinessLogic, times(0)).unlockDeleteOperation();
    }

    @Test
    public void deleteLockSucceededAfterRetry() {
        when(componentsCleanBusinessLogic.lockDeleteOperation())
                .thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT)
                .thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT)
                .thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT)
                .thenReturn(StorageOperationStatus.OK);
        migration.isLockDeleteOperationSucceeded();
        migration.unlockDeleteOperation();
        verify(componentsCleanBusinessLogic).unlockDeleteOperation();
    }

    @Test
    public void deleteLockFailedAfterRetry() {
        when(componentsCleanBusinessLogic.lockDeleteOperation())
                .thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT);
        migration.isLockDeleteOperationSucceeded();
        migration.unlockDeleteOperation();
        verify(componentsCleanBusinessLogic, times(0)).unlockDeleteOperation();
    }

    @Test
    public void deleteMarkedResourcesWhenLimitIsReached() {
        ArrayList<NodeTypeEnum> componentsToClean = new ArrayList<>();
        componentsToClean.add(NodeTypeEnum.Resource);
        migration.setUser(user);
        migration.setMarkedAsDeletedResourcesCnt(5);
        migration.deleteResourcesIfLimitIsReached();
        verify(componentsCleanBusinessLogic).cleanComponents(componentsToClean, true);
    }

    @Test
    public void deleteMarkedResourcesNotCalledWhenLimitIsNotReached() {
        ArrayList<NodeTypeEnum> componentsToClean = new ArrayList<>();
        componentsToClean.add(NodeTypeEnum.Resource);
        migration.setUser(user);
        migration.setMarkedAsDeletedResourcesCnt(3);
        migration.deleteResourcesIfLimitIsReached();
        verify(componentsCleanBusinessLogic, times(0)).cleanComponents(componentsToClean, true);
    }

    @Test
    public void deleteMarkedServicesWhenLimitIsReached() {
        ArrayList<NodeTypeEnum> componentsToClean = new ArrayList<>();
        componentsToClean.add(NodeTypeEnum.Service);
        migration.setUser(user);
        migration.setMarkedAsDeletedServicesCnt(5);
        migration.deleteServicesIfLimitIsReached();
        verify(componentsCleanBusinessLogic).cleanComponents(componentsToClean, true);
    }

    @Test
    public void deleteMarkedServicesNotCalledWhenLimitIsNotReached() {
        ArrayList<NodeTypeEnum> componentsToClean = new ArrayList<>();
        componentsToClean.add(NodeTypeEnum.Service);
        migration.setUser(user);
        migration.setMarkedAsDeletedServicesCnt(2);
        migration.deleteServicesIfLimitIsReached();
        verify(componentsCleanBusinessLogic, times(0)).cleanComponents(componentsToClean, true);
    }

    @Test
    public void getVfUpgradeStatusWhenUpgradeFailedAndItIsInstance() {
        assertEquals(UpgradeMigration1710.UpgradeStatus.NOT_UPGRADED, migration.getVfUpgradeStatus(false, true));
    }

    @Test
    public void getVfUpgradeStatusWhenUpgradeFailedAndItIsNotInstance() {
        assertEquals(UpgradeMigration1710.UpgradeStatus.NOT_UPGRADED, migration.getVfUpgradeStatus(false, false));
    }

    @Test
    public void getVfUpgradeStatusWhenUpgradeSucceededAndItIsInstance() {
        assertEquals(UpgradeMigration1710.UpgradeStatus.UPGRADED_AS_INSTANCE, migration.getVfUpgradeStatus(true, true));
    }

    @Test
    public void getVfUpgradeStatusWhenUpgradeSucceededAndItIsNotInstance() {
        assertEquals(UpgradeMigration1710.UpgradeStatus.UPGRADED, migration.getVfUpgradeStatus(true, false));
    }

    private void resolveUserAndDefineUpgradeLevel() {
        when(userAdminOperation.getUserData(anyString(), eq(false))).thenReturn(Either.left(user));
        configurationManager.getConfiguration().setToscaConformanceLevel(CONF_LEVEL);
        configurationManager.getConfiguration().setResourcesForUpgrade(resourcesForUpgrade);
    }

    private void upgradeAllScenario(boolean upgradeServices) {
        final boolean failOnVfUpgrade = false;
        final boolean exceptionOnFvUpgrade = false;
        final boolean upgradeVFC = false;
        final boolean isFailed = false;
        final boolean isProxy = true;

        resolveUserAndDefineUpgradeLevel();
        mockCheckoutFlow();
        when(resourceBusinessLogic.validateAndUpdateResourceFromCsar(any(Resource.class), any(), any(), any(),
                any()))
                .thenReturn(resource);
        upgradeRules(failOnVfUpgrade, exceptionOnFvUpgrade, upgradeServices, upgradeVFC, isFailed, isProxy);
    }

    private void upgradeRules(boolean failedVfUpgrade, boolean exceptionOnVfUpgrade, boolean upgradeService,
                              boolean upgradeVFCs, boolean isFailed) {
        upgradeRules(failedVfUpgrade, exceptionOnVfUpgrade, upgradeService, upgradeVFCs, isFailed, false);
    }

    private void upgradeRules(boolean failedVfUpgrade, boolean exceptionOnVfUpgrade, boolean upgradeService,
                              boolean upgradeVFCs, boolean isFailed, boolean isProxy) {

        mockNodeTypesUpgrade();
        Either<Component, StorageOperationStatus> foundResource = Either.left(resource);

        if (failedVfUpgrade) {
            getToscaElementMockForVfUpgradeFailedScenario(foundResource);
        } else {
            if (exceptionOnVfUpgrade) {
                getToscaElementMockForExceptionOnUpgradeScenario(foundResource, upgradeService);
            } else {
                when(toscaOperationFacade.getToscaElement(anyString()))
                        .thenReturn(foundResource);
            }
        }
        //happy flow
        if (upgradeService) {
            mockForUpgradeServiceScenario(foundResource, upgradeVFCs, isFailed);
        }
    }

    private void mockNodeTypesUpgrade() {
        GraphVertex component = createComponent();
        List<GraphVertex> components = Lists.newArrayList();
        components.add(component);

        when(titanDao.getByCriteria(any(), any(), any(), any()))
                .thenReturn(Either.left(components));
        when(titanDao.getParentVertecies(any(GraphVertex.class), any(EdgeLabelEnum.class), any(JsonParseFlagEnum.class)))
                //1th node to upgrade
                .thenReturn(Either.left(components))
                //parent of the 1th node - stop recursion
                .thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
    }

    private GraphVertex createComponent() {
        GraphVertex component = new GraphVertex();
        component.setJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE,LifecycleStateEnum.CERTIFIED.name());
        component.setJsonMetadataField(JsonPresentationFields.UNIQUE_ID,COMPONENT_UNIQUE_ID);
        component.setJsonMetadataField(JsonPresentationFields.CI_COMPONENT_VERSION,UPDATED_VERSION);
        return component;
    }

    private void mockChangeComponentState() {
        List<ComponentInstance> instances = Lists.newArrayList();
        instances.add(createComponentInstance());

        Resource checkedOutResource = new Resource();
        checkedOutResource.setUniqueId("123400");
        checkedOutResource.setComponentInstances(instances);
        Either<Resource, ResponseFormat> fromLifeCycle = Either.left(checkedOutResource);
        doReturn(fromLifeCycle).when(lifecycleBusinessLogic)
                .changeComponentState(any(), any(), any(), any(), any(),eq(true), eq(false));
    }

    private void getToscaElementMockForVfUpgradeFailedScenario(Either<Component, StorageOperationStatus> foundResource) {
        when(toscaOperationFacade.getToscaElement(anyString()))
                .thenReturn(foundResource)
                .thenReturn(foundResource)
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
    }

    private void mockForUpgradeServiceScenario(Either<Component, StorageOperationStatus> foundResource, boolean upgradeVFC, boolean isFailed) {
        Either<Resource, StorageOperationStatus> foundService = Either.left(resource);
        if (upgradeVFC) {
            when(toscaOperationFacade.getToscaElement(anyString()))
                    .thenReturn(foundResource)
                    .thenReturn(foundResource)
                    .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        }
        else if (!isFailed) {
            when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class)))
                    .thenReturn(Either.left(resource));
            when(toscaOperationFacade.getLatestCertifiedByToscaResourceName(any(), any(), any()))
                    .thenReturn(foundService);
        }
    }

    private void getToscaElementMockForExceptionOnUpgradeScenario(Either<Component, StorageOperationStatus> foundResource, boolean upgradeService) {
        if (upgradeService) {
            service.setVersion(UPDATED_VERSION);
            Either<Component, StorageOperationStatus> foundService = Either.left(service);
            when(toscaOperationFacade.getToscaElement(anyString()))
                    .thenReturn(foundResource)
                    .thenReturn(foundResource)
                    .thenThrow(new RuntimeException())
                    .thenReturn(foundService);
        }
        else {
            when(toscaOperationFacade.getToscaElement(anyString()))
                    .thenReturn(foundResource)
                    .thenReturn(foundResource)
                    .thenThrow(new RuntimeException());
        }
    }

    private void mockCheckoutFlow() {
        GraphVertex component = new GraphVertex();
        component.setJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE, LifecycleStateEnum.CERTIFIED.name());
        component.setJsonMetadataField(JsonPresentationFields.UNIQUE_ID, COMPONENT_UNIQUE_ID);
        List<GraphVertex> components = Lists.newArrayList();
        components.add(component);

        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(resource));
        when(titanDao.getByCriteria(any(), any(), any(), any()))
                .thenReturn(Either.left(components));
        when(csarOperation.getCsarLatestVersion(anyString(), any()))
                .thenReturn(Either.left("2.0"));
    }

    private ComponentInstance createComponentInstance() {
        ComponentInstance instance = new ComponentInstance();
        instance.setIcon("");
        instance.setUniqueId("");
        instance.setName("");
        instance.setComponentUid("");
        instance.setCreationTime(1L);
        instance.setModificationTime(2L);
        instance.setDescription("");
        instance.setPosX("");
        instance.setPosY("");
        instance.setPropertyValueCounter(1);
        instance.setNormalizedName("");
        instance.setOriginType(OriginTypeEnum.CVFC);
        instance.setCustomizationUUID("");
        instance.setComponentName("");
        instance.setComponentVersion(OLD_VERSION);
        instance.setToscaComponentName("");
        instance.setInvariantName("");
        instance.setSourceModelInvariant("");
        instance.setSourceModelName("");
        instance.setSourceModelUuid("");
        instance.setSourceModelUid("");
        instance.setIsProxy(false);
        return instance;
    }


}
