package org.openecomp.sdc.asdctool.migration.tasks.mig1710;


import com.google.common.collect.Lists;
import fj.data.Either;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeMigration1710Test {

    private final static String USER = "jh0003";
    private final static String CONF_LEVEL =  "5.0";

    private final User user = new User();
    private UpgradeMigration1710 migration;
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
    private ConfigurationSource configurationSource;
    private static ConfigurationManager configurationManager;
    private static List<String> resources = Stream.of("org.openecomp.resource.cp.extCP").collect(Collectors.toList());
    private static Map<String, List<String>> resourcesForUpgrade;

    @BeforeClass
    public static void setUpClass() {
        resourcesForUpgrade = new HashMap<>();
        resourcesForUpgrade.put(CONF_LEVEL, resources);
    }

    @Before
    public void setUp() {
        migration = new UpgradeMigration1710();
        migration.setUserAdminOperation(userAdminOperation);
        migration.setTitanDao(titanDao);
        migration.setTosckaOperationFacade(toscaOperationFacade);
        migration.setLifecycleBusinessLogic(lifecycleBusinessLogic);

        user.setUserId(USER);
        configurationManager = new ConfigurationManager(configurationSource);
        configurationManager.setConfiguration(new Configuration());
        configurationManager.getConfiguration().setSkipUpgradeVSPs(true);
        configurationManager.getConfiguration().setSkipUpgradeFailedVfs(true);
        configurationManager.getConfiguration().setAutoHealingOwner(USER);

    }

    @Test
    public void nodeTypesUpgradeFailed() {
        resolveUserAndDefineUpgradeLevel();
        when(titanDao.getByCriteria(any(), any(), any(), any()))
                                                .thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void nodeTypesUpgradePassedAndVFsUpgradeFailedWhenSkipFailedVFsIsNotSupported() {
        final boolean failOnVfUpgrade = true;
        final boolean upgradeServices = false;
        final boolean exceptionOnVfUpgrade = false;
        final boolean upgradeFVC = false;
        configurationManager.getConfiguration().setSkipUpgradeFailedVfs(false);
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnVfUpgrade, upgradeServices, upgradeFVC);
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void upgradeAllVFsUpgradeFailedOnExceptionWhenSkipFailedVFsIsNotSupported() {
        final boolean failOnVfUpgrade = false;
        final boolean upgradeServices = false;
        final boolean exceptionOnVfUpgrade = true;
        final boolean upgradeFVC = false;
        configurationManager.getConfiguration().setSkipUpgradeFailedVfs(false);
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnVfUpgrade, upgradeServices, upgradeFVC);
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void upgradeAllIfVFsUpgradeFailedOnExceptionWhenSkipFailedVFsIsSupported() {
        final boolean failOnVfUpgrade = false;
        final boolean upgradeServices = true;
        final boolean exceptionOnFvUpgrade = true;
        final boolean upgradeFVC = false;
        configurationManager.getConfiguration().setSkipUpgradeFailedVfs(true);
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnFvUpgrade, upgradeServices, upgradeFVC);
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
    }


    @Test
    public void upgradeAll() {
        final boolean failOnVfUpgrade = false;
        final boolean upgradeServices = true;
        final boolean exceptionOnFvUpgrade = false;
        final boolean upgradeFVC = false;
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnFvUpgrade, upgradeServices, upgradeFVC);
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void upgradeAllWhenVspUpgradeIsRequired() {
        final boolean failOnVfUpgrade = false;
        final boolean upgradeServices = true;
        final boolean exceptionOnFvUpgrade = false;
        final boolean upgradeFVC = true;
        resolveUserAndDefineUpgradeLevel();
        upgradeRules(failOnVfUpgrade, exceptionOnFvUpgrade, upgradeServices, upgradeFVC);
        configurationManager.getConfiguration().setSkipUpgradeVSPs(false);
        migration.setComponentsUtils(componentUtils);
        assertEquals(MigrationResult.MigrationStatus.COMPLETED, migration.migrate().getMigrationStatus());
    }

    @Test
    public void migrationFailedWhenUserNotResolved() {
        when(userAdminOperation.getUserData(anyString(), eq(false))).thenReturn(Either.right(ActionStatus.MISSING_INFORMATION));
        when(titanDao.rollback()).thenReturn(TitanOperationStatus.OK);
        assertEquals(MigrationResult.MigrationStatus.FAILED, migration.migrate().getMigrationStatus());
    }

    private void resolveUserAndDefineUpgradeLevel() {
        when(userAdminOperation.getUserData(anyString(), eq(false))).thenReturn(Either.left(user));
        configurationManager.getConfiguration().setToscaConformanceLevel(CONF_LEVEL);
        configurationManager.getConfiguration().setResourcesForUpgrade(resourcesForUpgrade);
    }

    private void upgradeRules(boolean failedVfUpgrade, boolean exceptionOnVfUpgrade, boolean upgradeService, boolean upgradeVFCs) {
        GraphVertex component = new GraphVertex();
        component.setJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE, LifecycleStateEnum.CERTIFIED.name());
        component.setJsonMetadataField(JsonPresentationFields.UNIQUE_ID, "12345");
        List<GraphVertex> components = Lists.newArrayList();
        components.add(component);

        Resource resource = new Resource();
        Either<Component, StorageOperationStatus> foundResource = Either.left(resource);

        when(titanDao.getByCriteria(any(), any(), any(), any()))
                                                        .thenReturn(Either.left(components));
        when(titanDao.getParentVertecies(any(), any(), any()))
                                                        //1th node to upgrade
                                                        .thenReturn(Either.left(components))
                                                        //parent of the 1th node - stop recursion
                                                        .thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        if (failedVfUpgrade) {
            Either<Component, StorageOperationStatus> getToscaForVF = Either.right(StorageOperationStatus.NOT_FOUND);
            when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(foundResource)
                                                                    .thenReturn(foundResource)
                                                                    .thenReturn(getToscaForVF);
        }
        else {
            if (exceptionOnVfUpgrade) {
                when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(foundResource)
                        .thenReturn(foundResource)
                        .thenThrow(new RuntimeException());
            }
            else {
                when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(foundResource);
                //happy flow
                if (upgradeService) {
                    Either<Resource, StorageOperationStatus> service = Either.left(resource);
                    if (upgradeVFCs) {
                        when(componentUtils.convertFromStorageResponse(any(), any())).thenCallRealMethod();
                        when(componentUtils.getResponseFormat(any(ActionStatus.class),any())).thenCallRealMethod();
                        when(toscaOperationFacade.getLatestCertifiedByToscaResourceName(any(), any(), any()))
                                .thenReturn(service)
                                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND))
                                .thenReturn(service)
                                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
                    }
                    else {
                        when(toscaOperationFacade.getLatestCertifiedByToscaResourceName(any(), any(), any()))
                                .thenReturn(service);
                    }
                }
            }
        }
        List<ComponentInstance> instances = Lists.newArrayList();
        instances.add(createComponentInstance());
        resource.setComponentInstances(instances);
        Either<Resource, ResponseFormat> fromLifeCycle = Either.left(resource);
        doReturn(fromLifeCycle).when(lifecycleBusinessLogic)
                .changeComponentState(any(), any(), any(), any(), any(),eq(true), eq(false));

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
        instance.setComponentVersion("");
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
