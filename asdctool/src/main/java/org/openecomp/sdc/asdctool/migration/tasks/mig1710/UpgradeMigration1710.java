package org.openecomp.sdc.asdctool.migration.tasks.mig1710;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.core.task.PostMigration;
import org.openecomp.sdc.asdctool.migration.tasks.handlers.XlsOutputHandler;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
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
import org.openecomp.sdc.be.datatypes.enums.*;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class UpgradeMigration1710 implements PostMigration {

    private static final String SERVICE_UUID_RPOPERTY = "providing_service_uuid";

    private static final String SERVICE_INVARIANT_UUID_RPOPERTY = "providing_service_invariant_uuid";

    private static final String UNKNOWN = "UNKNOWN";

    private static final String CHECKOUT_MESSAGE = "checkout upon upgrade migration";

    private static final String FAILED_TO_CHANGE_STATE_OF_COMPONENT = "Failed to change state of component with name {}, invariantUUID {}, version {} to {}. ";

    private static final String FAILED_TO_UPGRADE_COMPONENT = "Failed to upgrade {} with name {}, invariantUUID {}, version {}. Operation {}. The reason for failure: {}. ";

    private static final String UPGRADE_COMPONENT_SUCCEEDED = "Upgrade of {} with name {}, invariantUUID {}, version {} finished successfully. ";

    private static final String UPGRADE_VFS_FAILED = "Upgrade VFs upon upgrade migration 1710 process failed. ";

    private static final Logger log = Logger.getLogger(UpgradeMigration1710.class);

    private static final String ALLOTTED_RESOURCE_NAME = "Allotted Resource";

    //as per US 397775, only node type upgrade should be enabled,
    // to support resource and service upgrade, this flag should be reverted
    private boolean isNodeTypesSupportOnly = true;

    @Autowired
    private TitanDao titanDao;

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;

    @Autowired
    private LifecycleBusinessLogic lifecycleBusinessLogic;

    @Autowired
    private IUserAdminOperation userAdminOperation;

    @Autowired
    private ResourceBusinessLogic resourceBusinessLogic;

    @Autowired
    private ServiceBusinessLogic serviceBusinessLogic;

    @Autowired
    private CsarOperation csarOperation;

    @Autowired
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Autowired
    private ComponentsUtils componentsUtils;

    @Autowired
    private ComponentsCleanBusinessLogic componentsCleanBusinessLogic;

    private XlsOutputHandler outputHandler = new XlsOutputHandler(null, "UpgradeMigration1710report","COMPONENT TYPE", "COMPONENT NAME", "COMPONENT UUID", "COMPONENT UNIQUE_ID", "UPGRADE STATUS", "DESCRIPTION");

    private User user = null;

    private final LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction(CHECKOUT_MESSAGE, LifecycleChanceActionEnum.UPGRADE_MIGRATION);

    private final Map<String, GraphVertex> latestGenericTypes = new HashMap<>();

    private final Map<String, String> latestOriginResourceVersions = new HashMap<>();

    private final Map<String, org.openecomp.sdc.be.model.Component> upgradedNodeTypesMap = new HashMap<>();

    private List<String> nodeTypes;

    private List<String> proxyServiceContainers = new ArrayList<>();

    private List<String> vfAllottedResources = new ArrayList<>();

    private List<String> allottedVfContainers = new ArrayList<>();

    private boolean isVfcUpgradeRequired = false;

    private boolean skipIfUpgradeVfFailed = true;

    private boolean isAllottedAndProxySupported = true;

    private String userId;

    private boolean isCleanupLocked = false;

    private int markedAsDeletedResourcesCnt = 0;

    private int markedAsDeletedServicesCnt = 0;

    //how many components can be deleted once
    private int maxDeleteComponents = 10;

    private boolean enableAutoHealing = true;

    //map for tracing checked out resources that keep in place after upgrade failure
    private HashMap<String, String> certifiedToNextCheckedOutUniqueId = new HashMap<>();

    private int deleteLockTimeoutInSeconds = 60;

    private boolean isLockSucceeded = false;

    /***********************************************/

    @VisibleForTesting
    void setNodeTypesSupportOnly(boolean nodeTypesSupportOnly) {
        isNodeTypesSupportOnly = nodeTypesSupportOnly;
    }

    @VisibleForTesting
    void setUser(User user) {
        this.user = user;
    }

    @VisibleForTesting
    void setMarkedAsDeletedResourcesCnt(int markedAsDeletedResourcesCnt) {
        this.markedAsDeletedResourcesCnt = markedAsDeletedResourcesCnt;
    }

    @VisibleForTesting
    void setMarkedAsDeletedServicesCnt(int markedAsDeletedServicesCnt) {
        this.markedAsDeletedServicesCnt = markedAsDeletedServicesCnt;
    }

    @PostConstruct
    void init() {
        Configuration config = ConfigurationManager.getConfigurationManager().getConfiguration();
        isVfcUpgradeRequired = !config.getSkipUpgradeVSPsFlag();
        skipIfUpgradeVfFailed = config.getSkipUpgradeFailedVfs();
        isAllottedAndProxySupported = config.getSupportAllottedResourcesAndProxyFlag();
        deleteLockTimeoutInSeconds = config.getDeleteLockTimeoutInSeconds();
        maxDeleteComponents = config.getMaxDeleteComponents();

        String toscaConformanceLevel = config.getToscaConformanceLevel();
        Map<String, List<String>> resourcesForUpgrade = config.getResourcesForUpgrade();
        nodeTypes = resourcesForUpgrade.get(toscaConformanceLevel);
        enableAutoHealing = config.isEnableAutoHealing();
        userId = config.getAutoHealingOwner();
        isNodeTypesSupportOnly = true;
    }

    @Override
    public String description() {
        return "Upgrade migration 1710 - post migration task, which is dedicated to upgrade all latest certified (and not checked out) Node types, VFs and Services. ";
    }

    enum UpgradeStatus {
        UPGRADED,
        UPGRADED_AS_INSTANCE,
        NOT_UPGRADED
    }

    @Override
    public MigrationResult migrate() {
        MigrationResult migrationResult = new MigrationResult();
        //stop the upgrade if this ask is disabled
        if (!enableAutoHealing) {
            log.warn("Upgrade migration 1710 task is disabled");
            migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.COMPLETED);
            return migrationResult ;
        }
        log.info("Starting upgrade migration 1710 process. ");
        boolean result = true;

        try {
            //lock cleanup node to avoid BE to delete marked components
            //while the auto-healing process is running
            isLockSucceeded = isNodeTypesSupportOnly ? true : isLockDeleteOperationSucceeded();

            if (!isLockSucceeded) {
                result = false;
                log.error("Cleanup node can't be locked. Upgrade migration failed");
            }
            else {
                Either<User, ActionStatus> userReq = userAdminOperation.getUserData(userId, false);
                if (userReq.isRight()) {
                    result = false;
                    log.error("Upgrade migration failed. User {} resolve failed: {} ", userId, userReq.right().value());
                } else {
                    user = userReq.left().value();
                    log.info("User {} will perform upgrade operation", user.getUserId());
                }
            }
            if (result) {
                result = upgradeNodeTypes();
            }
            if (!isNodeTypesSupportOnly && result) {
                result = upgradeTopologyTemplates();
            }
        }
        catch (Exception e) {
            result = false;
            log.error("Error occurred during the migration: ", e);
        } finally {
            MigrationResult.MigrationStatus status = result ?
                    MigrationResult.MigrationStatus.COMPLETED : MigrationResult.MigrationStatus.FAILED;
            cleanup(status);
            migrationResult.setMigrationStatus(status);
        }
        return migrationResult;
    }

    private boolean upgradeTopologyTemplates() {
        if (upgradeVFs()) {
            upgradeServices();
            upgradeProxyServiceContainers();
            upgradeAllottedVFs();
            upgradeAllottedVfContainers();
            return true;
        }
        return false;
    }

    private void cleanup(MigrationResult.MigrationStatus status) {
        if (status == MigrationResult.MigrationStatus.COMPLETED ) {
            log.info("Upgrade migration 1710 has been successfully finished. ");
            titanDao.commit();
        } else {
            log.info("Upgrade migration 1710 was failed. ");
            titanDao.rollback();
        }
        outputHandler.writeOutputAndCloseFile();
        if (!isNodeTypesSupportOnly && isLockSucceeded) {
            //delete rest of components if their upgrade failed
            markedAsDeletedResourcesCnt = maxDeleteComponents;
            deleteResourcesIfLimitIsReached();
            markedAsDeletedServicesCnt = maxDeleteComponents;
            deleteServicesIfLimitIsReached();
            unlockDeleteOperation();
        }
    }

    void upgradeServices(List<String> uniqueIDs, Predicate<org.openecomp.sdc.be.model.Component> shouldUpgrade, final String containerName) {
        log.info("Starting upgrade {} upon upgrade migration 1710 process. ", containerName);
        for (String currUid : uniqueIDs) {
            upgradeServiceAndCommitIfNeeded(currUid, shouldUpgrade);
        }
        log.info("Upgrade {} upon upgrade migration 1710 process is finished. ", containerName);
    }

    private void upgradeServiceAndCommitIfNeeded(String currUid, Predicate<org.openecomp.sdc.be.model.Component> shouldUpgrade) {
        boolean result = true;
        try {
            result = handleService(currUid, shouldUpgrade);
        } catch (Exception e) {
            result = false;
            log.error("Failed to upgrade service with uniqueId {} due to a reason {}. ", currUid, e.getMessage());
            log.debug("Failed to upgrade service with uniqueId {}", currUid, e);
        }
        finally {
            if (result) {
                log.info("Service upgrade finished successfully: uniqueId {} ", currUid);
                titanDao.commit();
            }
            else {
                log.error("Failed to upgrade service with uniqueId {} ", currUid);
                titanDao.rollback();
            }
            markCheckedOutServiceAsDeletedIfUpgradeFailed(currUid, result);
        }
    }

    private void upgradeAllottedVfContainers() {
        upgradeServices(allottedVfContainers, component -> true, "proxy " + allottedVfContainers.size() + " service containers");
    }

    private void upgradeServices() {
        Either<List<String>, TitanOperationStatus> getServicesRes = getAllLatestCertifiedComponentUids(VertexTypeEnum.TOPOLOGY_TEMPLATE, ComponentTypeEnum.SERVICE);
        if (getServicesRes.isRight()) {
            log.error("Failed to retrieve the latest certified service versions");
            return;
        }
        upgradeServices(getServicesRes.left().value(), this::shouldUpgrade, "services");
    }

    private void upgradeProxyServiceContainers() {
        upgradeServices(proxyServiceContainers, component -> true, "proxy service containers");
    }

    private boolean handleService(String uniqueId, Predicate<org.openecomp.sdc.be.model.Component> shouldUpgrade) {
        log.info("Starting upgrade Service with uniqueId {} upon upgrade migration 1710 process. ", uniqueId);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getServiceRes = toscaOperationFacade.getToscaElement(uniqueId);
        if(getServiceRes.isRight()){
            log.error("Failed to upgrade service with uniqueId {} due to {}. ", uniqueId, getServiceRes.right().value());
            outputHandler.addRecord(ComponentTypeEnum.SERVICE.name(), UNKNOWN, UNKNOWN, uniqueId, MigrationResult.MigrationStatus.FAILED.name(), getServiceRes.right().value());
            return false;
        }
        String derivedFromGenericType =  getServiceRes.left().value().getDerivedFromGenericType();
        log.debug("derivedFromGenericType: {}", derivedFromGenericType );
        if (derivedFromGenericType == null) {
            //malformed field value, upgrade required
            return upgradeService(getServiceRes.left().value());
        }
        if(!latestGenericTypes.containsKey(derivedFromGenericType)){
            Either<List<GraphVertex>, TitanOperationStatus> getDerivedRes = findDerivedResources(derivedFromGenericType);
            if(getDerivedRes.isRight()){
                log.error(FAILED_TO_UPGRADE_COMPONENT, getServiceRes.left().value().getComponentType().getValue(), getServiceRes.left().value().getName(), getServiceRes.left().value().getInvariantUUID(), getServiceRes.left().value().getVersion(), "findDerivedResources", getDerivedRes.right().value());
                outputHandler.addRecord( getServiceRes.left().value().getComponentType().name(),getServiceRes.left().value().getName(), getServiceRes.left().value().getInvariantUUID(), getServiceRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), getDerivedRes.right().value());
                return false;
            }
            latestGenericTypes.put(derivedFromGenericType, getDerivedRes.left().value().get(0));
        }
        if(latestVersionExists(latestGenericTypes.get(derivedFromGenericType), getServiceRes.left().value().getDerivedFromGenericVersion())){
            return upgradeService(getServiceRes.left().value());
        }
        if(!collectLatestOriginResourceVersions(getServiceRes.left().value())){
            return false;
        }
        if(shouldUpgrade.test(getServiceRes.left().value())){
            return upgradeService(getServiceRes.left().value());
        }
        outputHandler.addRecord(getServiceRes.left().value().getComponentType().name(), getServiceRes.left().value().getName(), getServiceRes.left().value().getInvariantUUID(), getServiceRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.COMPLETED.name(), UpgradeStatus.NOT_UPGRADED);
        return true;
    }

    private boolean collectLatestOriginResourceVersions(org.openecomp.sdc.be.model.Component component) {
        if (CollectionUtils.isNotEmpty(component.getComponentInstances())) {
            for (ComponentInstance instance : component.getComponentInstances()) {
                if (instance.getOriginType() != OriginTypeEnum.ServiceProxy && !latestOriginResourceVersions.containsKey(instance.getToscaComponentName()) && !addComponent(component, instance)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean addComponent(org.openecomp.sdc.be.model.Component component, ComponentInstance instance) {
        VertexTypeEnum vertexType = ModelConverter.getVertexType(instance.getOriginType().name());
        Either<Resource, StorageOperationStatus> getOriginRes = toscaOperationFacade.getLatestCertifiedByToscaResourceName(instance.getToscaComponentName(), vertexType, JsonParseFlagEnum.ParseMetadata);
        if (getOriginRes.isRight()) {
            log.error(FAILED_TO_UPGRADE_COMPONENT, component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion(), "toscaOperationFacade.getLatestCertifiedByToscaResourceName", getOriginRes.right().value());
            outputHandler.addRecord(component.getComponentType().name(), component.getName(), component.getInvariantUUID(), component.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), getOriginRes.right().value());
            return false;
        }
        latestOriginResourceVersions.put(instance.getToscaComponentName(), getOriginRes.left().value().getVersion());
        return true;
    }

    private boolean shouldUpgrade(org.openecomp.sdc.be.model.Component component) {
        if(CollectionUtils.isNotEmpty(component.getComponentInstances())) {
            if (containsProxyOrAllottedVF(component)) {
                return false;
            }
            for(ComponentInstance instance : component.getComponentInstances()){
                if(isGreater(latestOriginResourceVersions.get(instance.getToscaComponentName()), instance.getComponentVersion())){
                    log.info("The service with name {}, invariantUUID {}, version {}, contains instance {} from outdated version of origin {} {} , than the service should be upgraded. ", component.getName(), component.getInvariantUUID(), component.getVersion(), instance.getName(), instance.getComponentName(), instance.getComponentVersion());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsProxyOrAllottedVF(org.openecomp.sdc.be.model.Component component) {
        return !component.getComponentInstances()
                .stream()
                .filter(i->isProxyOrAllottedVF(i, component.getUniqueId()))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isProxyOrAllottedVF(ComponentInstance instance, String uniqueId) {
        if (instance.getOriginType() == OriginTypeEnum.ServiceProxy) {
            keepProxyServiceContainerIfSupported(uniqueId);
            return true;
        }
        if (isAllottedResource(instance.getActualComponentUid())) {
            keepAllottedVfContainerIfSupported(uniqueId);
            return true;
        }
        return false;
    }

    private void keepAllottedVfContainerIfSupported(final String uniqueId) {
        if (isAllottedAndProxySupported && !allottedVfContainers.contains(uniqueId)) {
            log.info("Add a service with uniqueId {} to allotted VF containers container list", uniqueId);
            allottedVfContainers.add(uniqueId);
        }
    }

    private void keepProxyServiceContainerIfSupported(final String uniqueId) {
        if (isAllottedAndProxySupported && !proxyServiceContainers.contains(uniqueId)) {
            log.info("Add a service with uniqueId {} to proxy service container list", uniqueId);
            proxyServiceContainers.add(uniqueId);
        }
    }

    private boolean upgradeService(org.openecomp.sdc.be.model.Component service) {
        String serviceName = service.getName();
        String serviceUuid = service.getUUID();
        log.info("Starting upgrade Service with name {}, invariantUUID {}, version {} upon upgrade migration 1710 process. ", serviceName, service.getInvariantUUID(), service.getVersion());
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkouRes = checkOutComponent(service);
        if (checkouRes.isRight()) {
            log.error(FAILED_TO_UPGRADE_COMPONENT, service.getComponentType().getValue(), serviceName, service.getInvariantUUID(), service.getVersion(), "lifecycleBusinessLogic.changeComponentState", checkouRes.right().value().getFormattedMessage());
            outputHandler.addRecord(service.getComponentType().name(), serviceName, serviceUuid, service.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), checkouRes.right().value().getFormattedMessage());
            return false;
        }
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat> updateCompositionRes = updateComposition(checkouRes.left().value());
        if (updateCompositionRes.isRight()) {
            log.error(FAILED_TO_UPGRADE_COMPONENT, service.getComponentType().getValue(), serviceName, service.getInvariantUUID(), service.getVersion(), "updateComposition", updateCompositionRes.right().value().getFormattedMessage());
            outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getUUID(), checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), updateCompositionRes.right().value().getFormattedMessage());
            return false;
        }
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> certifyRes = performFullCertification(checkouRes.left().value());
        if (certifyRes.isRight()) {
            log.error(FAILED_TO_UPGRADE_COMPONENT, service.getComponentType().getValue(), serviceName, service.getInvariantUUID(), service.getVersion(), "performFullCertification", certifyRes.right().value().getFormattedMessage());
            outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getInvariantUUID(), checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), certifyRes.right().value().getFormattedMessage());
            return false;
        }
        outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), serviceUuid, checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.COMPLETED.name(), UpgradeStatus.UPGRADED);
        return true;
    }

    private Either<org.openecomp.sdc.be.model.Component, ResponseFormat> updateComposition(org.openecomp.sdc.be.model.Component component) {
        if (component != null && component.getComponentInstances() != null) {
            Either<ComponentInstance, ResponseFormat> upgradeInstanceRes;
            for (ComponentInstance instance : component.getComponentInstances()) {
                upgradeInstanceRes = upgradeInstance(component, instance);
                if (upgradeInstanceRes.isRight()) {
                    log.error(FAILED_TO_UPGRADE_COMPONENT, component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion(), "upgradeInstance", upgradeInstanceRes.right().value().getFormattedMessage());
                    outputHandler.addRecord(component.getComponentType().name(), component.getName(), component.getUUID(), component.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), upgradeInstanceRes.right().value().getFormattedMessage());
                    return Either.right(upgradeInstanceRes.right().value());
                }
            }
        }
        return Either.left(component);
    }

    private Either<ComponentInstance, ResponseFormat> upgradeInstance(org.openecomp.sdc.be.model.Component component, ComponentInstance instance) {
        log.info("Starting upgrade {} instance {} upon upgrade migration 1710 process. ", component.getComponentType().getValue(), instance.getName());
        ComponentInstance newComponentInstance = new ComponentInstance(instance);
        if (instance.getOriginType() == OriginTypeEnum.ServiceProxy) {
            return upgradeServiceProxyInstance(component, instance, newComponentInstance);
        }
        return upgradeResourceInstance(component, instance, newComponentInstance);
    }

    private Either<ComponentInstance, ResponseFormat> upgradeResourceInstance(org.openecomp.sdc.be.model.Component component, ComponentInstance instance, ComponentInstance newComponentInstance) {

        log.info("Starting upgrade {} instance {} upon upgrade migration 1710 process. ", component.getComponentType().getValue(), instance.getName());
        Either<ComponentInstance, ResponseFormat> upgradeInstanceRes = null;
        VertexTypeEnum vertexType = ModelConverter.getVertexType(instance.getOriginType().name());
        Either<Resource, StorageOperationStatus> getOriginRes = toscaOperationFacade.getLatestCertifiedByToscaResourceName(instance.getToscaComponentName(), vertexType, JsonParseFlagEnum.ParseMetadata);
        if(getOriginRes.isRight()){
            log.info("Upgrade of {} instance {} upon upgrade migration 1710 process failed due to a reason {}. ",
                    component.getComponentType().getValue(), instance.getName(), getOriginRes.right().value());
            upgradeInstanceRes = Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getOriginRes.right().value(), instance.getOriginType().getComponentType())));
        }
        if(upgradeInstanceRes == null) {
            copyComponentNameAndVersionToNewInstance(newComponentInstance, getOriginRes.left().value());

            if(isGreater(getOriginRes.left().value().getVersion(), instance.getComponentVersion())){
                upgradeInstanceRes = changeAssetVersion(component, instance, newComponentInstance);
            }
            if((upgradeInstanceRes == null || upgradeInstanceRes.isLeft()) && isAllottedResource(instance.getComponentUid()) && MapUtils.isNotEmpty(component.getComponentInstancesProperties())){
                ComponentInstance instanceToUpdate = upgradeInstanceRes == null ? instance : upgradeInstanceRes.left().value();
                upgradeInstanceRes = Either.left(updateServiceUuidProperty(component, instanceToUpdate, component.getComponentInstancesProperties().get(instance.getUniqueId())));
            }
        }
        //upgrade nodes contained by CVFC
        if(upgradeInstanceRes == null && isVfcUpgradeRequired && newComponentInstance.getOriginType() == OriginTypeEnum.CVFC &&
                !upgradeVf(getOriginRes.left().value().getUniqueId(), false, true)) {
            upgradeInstanceRes = Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        if(upgradeInstanceRes == null){
            upgradeInstanceRes = Either.left(instance);
        }
        log.info("Upgrade of {} instance {} upon upgrade migration 1710 process finished successfully. ",
                component.getComponentType().getValue(), instance.getName());
        return upgradeInstanceRes;
    }

    private void copyComponentNameAndVersionToNewInstance(ComponentInstance newComponentInstance, Resource originResource) {
        newComponentInstance.setComponentName(originResource.getName());
        newComponentInstance.setComponentUid(originResource.getUniqueId());
        newComponentInstance.setComponentVersion(originResource.getVersion());
        newComponentInstance.setToscaComponentName(originResource.getToscaResourceName());
    }

    private ComponentInstance updateServiceUuidProperty(org.openecomp.sdc.be.model.Component component, ComponentInstance instance, List<ComponentInstanceProperty> instanceProperties){
        if(isAllottedResource(instance.getComponentUid()) && instanceProperties != null){
            Optional<ComponentInstanceProperty> propertyUuid = instanceProperties.stream().filter(p->p.getName().equals(SERVICE_UUID_RPOPERTY)).findFirst();
            Optional<ComponentInstanceProperty> propertyInvariantUuid = instanceProperties.stream().filter(p->p.getName().equals(SERVICE_INVARIANT_UUID_RPOPERTY)).findFirst();
            if(propertyUuid.isPresent() && propertyInvariantUuid.isPresent()){
                String serviceInvariantUUID = propertyInvariantUuid.get().getValue();
                Either<List<GraphVertex>, TitanOperationStatus> getLatestOriginServiceRes = getLatestCertifiedService(serviceInvariantUUID);
                if (getLatestOriginServiceRes.isRight()) {
                    return instance;
                }
                propertyUuid.get().setValue((String) getLatestOriginServiceRes.left().value().get(0).getJsonMetadataField(JsonPresentationFields.UUID));
                componentInstanceBusinessLogic.createOrUpdatePropertiesValues(component.getComponentType(), component.getUniqueId(), instance.getUniqueId(), Lists.newArrayList(propertyUuid.get()), user.getUserId())
                        .right()
                        .forEach(e -> log.debug("Failed to update property {} of the instance {} of the component {}. ", SERVICE_UUID_RPOPERTY, instance.getUniqueId(), component.getName()));
            }
        }
        return instance;
    }

    private boolean isAllottedResource(String uniqueId){
        ComponentParametersView filters = new ComponentParametersView(true);
        filters.setIgnoreCategories(false);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getResourceRes = toscaOperationFacade.getToscaElement(uniqueId, filters);
        if(getResourceRes.isRight()){
            return false;
        }
        if(getResourceRes.left().value().getCategories() != null && getResourceRes.left().value().getCategories().get(0)!= null){
            return ALLOTTED_RESOURCE_NAME.equals(getResourceRes.left().value().getCategories().get(0).getName());
        }
        return false;
    }

    private boolean isAllottedVf(org.openecomp.sdc.be.model.Component component){
        if(component.getComponentType() != ComponentTypeEnum.RESOURCE || ((Resource)component).getResourceType() != ResourceTypeEnum.VF){
            return false;
        }
        return isAllottedResource(component.getUniqueId());
    }

    private Either<ComponentInstance, ResponseFormat> upgradeServiceProxyInstance(org.openecomp.sdc.be.model.Component component, ComponentInstance instance, ComponentInstance newComponentInstance) {
        Either<List<GraphVertex>, TitanOperationStatus> getLatestOriginServiceRes = getLatestCertifiedService(instance.getSourceModelInvariant());
        if (getLatestOriginServiceRes.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(getLatestOriginServiceRes.right().value()), instance.getOriginType().getComponentType())));
        }
        ModelConverter.getVertexType(instance.getOriginType().name());
        Either<Resource, StorageOperationStatus> getOriginRes = toscaOperationFacade.getLatestByName(instance.getComponentName());
        if(getOriginRes.isRight()){
            log.info("Upgrade of {} instance {} upon upgrade migration 1710 process failed due to a reason {}. ",
                    component.getComponentType().getValue(), instance.getName(), getOriginRes.right().value());
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getOriginRes.right().value(), instance.getOriginType().getComponentType())));
        }
        newComponentInstance.setComponentUid((String) getLatestOriginServiceRes.left().value().get(0).getJsonMetadataField(JsonPresentationFields.UNIQUE_ID));
        return changeAssetVersion(component, instance, newComponentInstance);
    }

    private Either<List<GraphVertex>, TitanOperationStatus> getLatestCertifiedService(String invariantUUID) {

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, invariantUUID);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        return titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseMetadata);
    }

    private Either<ComponentInstance, ResponseFormat> changeAssetVersion(org.openecomp.sdc.be.model.Component containerComponent, ComponentInstance instance, ComponentInstance newComponentInstance) {
        return componentInstanceBusinessLogic.changeComponentInstanceVersion(ComponentTypeEnum.SERVICE_PARAM_NAME, containerComponent.getUniqueId(), instance.getUniqueId(), user.getUserId(), newComponentInstance);
    }

    private boolean upgradeNodeTypes() {
        log.info("Starting upgrade node types upon upgrade migration 1710 process. ");
        if (nodeTypes != null && !nodeTypes.isEmpty()) {
            Either<List<String>, TitanOperationStatus> getRes = getAllLatestCertifiedComponentUids(VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE);
            if (getRes.isRight()) {
                return false;
            }
            for (String toscaResourceName : nodeTypes) {
                if (!upgradeNodeType(toscaResourceName, getRes.left().value())) {
                    return false;
                }
            }
        }
        else {
            log.info("No node types for upgrade are configured");
        }
        return true;
    }

    private boolean upgradeNodeType(String toscaResourceName, List<String> allNodeTypes) {
        Either<List<GraphVertex>, StorageOperationStatus> status = getLatestByName(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        if (status.isRight()) {
            log.error("Failed to find node type {} ", toscaResourceName);
            return false;
        }
        List<GraphVertex> vList = status.left().value();
        for (GraphVertex vertex : vList) {
            StorageOperationStatus updateRes = upgradeNodeType(vertex, allNodeTypes);
            if (updateRes != StorageOperationStatus.OK) {
                return false;
            }
        }
        return true;
    }

    private boolean upgradeVFs() {
        log.info("Starting upgrade VFs upon upgrade migration 1710 process. ");
        Either<List<String>, TitanOperationStatus> getVfsRes = getAllLatestCertifiedComponentUids(VertexTypeEnum.TOPOLOGY_TEMPLATE, ComponentTypeEnum.RESOURCE);
        if (getVfsRes.isRight()) {
            log.info(UPGRADE_VFS_FAILED);
            return false;
        }
        return upgradeVFs(getVfsRes.left().value(), false);
    }

    private boolean upgradeAllottedVFs() {
        log.info("Starting upgrade {} allotted Vfs with upon upgrade migration 1710 process. ", vfAllottedResources.size());
        return upgradeVFs(vfAllottedResources, true);
    }

    boolean upgradeVFs(List<String> resourceList, boolean isAllottedVfsUpgrade) {
        for (String currUid : resourceList) {
            boolean result = true;
            try {
                result = upgradeVf(currUid, isAllottedVfsUpgrade, false);
                if (!result && !skipIfUpgradeVfFailed) {
                    return false;
                }
            } catch (Exception e) {
                log.error("The exception {} occurred upon upgrade VFs. ", e.getMessage());
                log.debug("The exception occurred upon upgrade VFs:", e);
                result = false;
                if (!skipIfUpgradeVfFailed) {
                    return false;
                }
            }
            finally {
                if (result) {
                    log.info("Resource upgrade finished successfully: uniqueId {} ", currUid);
                    titanDao.commit();
                }
                else {
                    log.error("Failed to upgrade resource with uniqueId {} ", currUid);
                    titanDao.rollback();
                }
                markCheckedOutResourceAsDeletedIfUpgradeFailed(currUid, result);
            }
        }
        log.info("Upgrade VFs upon upgrade migration 1710 process finished successfully. ");
        return true;
    }

    private boolean upgradeVf(String uniqueId, boolean allottedVfsUpgrade, boolean isInstance) {
        log.info("Starting upgrade VF with uniqueId {} upon upgrade migration 1710 process. ", uniqueId);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getRes = toscaOperationFacade.getToscaElement(uniqueId);
        if (getRes.isRight()) {
            log.debug("Failed to fetch VF with uniqueId {} upon upgrade migration 1710 process. ", uniqueId);
            outputHandler.addRecord(ComponentTypeEnum.RESOURCE.name(), UNKNOWN, UNKNOWN, uniqueId, MigrationResult.MigrationStatus.FAILED.name(), getRes.right().value());
            return false;
        }
        if(!allottedVfsUpgrade && isAllottedVf(getRes.left().value())){
            keepAllottedResourceIfSupported(uniqueId);
            return true;
        }
        if (StringUtils.isNotEmpty(getRes.left().value().getCsarUUID())) {
            log.info("Going to fetch the latest version of VSP with csarUUID {} upon upgrade migration 1710 process. ", getRes.left().value().getCsarUUID());
            Either<String, StorageOperationStatus> latestVersionRes = csarOperation.getCsarLatestVersion(getRes.left().value().getCsarUUID(), user);
            if (latestVersionRes.isRight()) {
                log.debug("Failed to fetch the latest version of VSP with csarUUID {} upon upgrade migration 1710 process. ", getRes.left().value().getCsarUUID());
                outputHandler.addRecord(getRes.left().value().getComponentType().name(), getRes.left().value().getName(), getRes.left().value().getUUID(), getRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), latestVersionRes.right().value());
                return false;
            }
            if (isGreater(latestVersionRes.left().value(), getRes.left().value().getCsarVersion())) {
                return upgradeVfWithLatestVsp(getRes.left().value(), latestVersionRes.left().value(), isInstance);
            }
            if (isVfcUpgradeRequired) {
                return upgradeComponentWithLatestGeneric(getRes.left().value(), isInstance);
            }
            log.warn("Warning: No need to upgrade VF with name {}, invariantUUID {}, version {} and VSP version {}. No new version of VSP. ", getRes.left().value().getName(), getRes.left().value().getInvariantUUID(), getRes.left().value().getVersion(), getRes.left().value().getCsarVersion());
            return true;
        }
        else {
            return upgradeComponentWithLatestGeneric(getRes.left().value(), isInstance);
        }
    }

    private void keepAllottedResourceIfSupported(final String uniqueId) {
        if (isAllottedAndProxySupported && !vfAllottedResources.contains(uniqueId)) {
            log.info("Add a resource with uniqueId {} to allotted resource list", uniqueId);
            vfAllottedResources.add(uniqueId);
        }
    }

    private boolean upgradeVfWithLatestVsp(org.openecomp.sdc.be.model.Component vf, String latestVersion, boolean isInstance) {
        log.info("Starting upgrade vf with name {}, invariantUUID {}, version {} and latest VSP version {} upon upgrade migration 1710 process. ", vf.getName(), vf.getInvariantUUID(), vf.getVersion(), latestVersion);
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkouRes = checkOutComponent(vf);
        if (checkouRes.isRight()) {
            outputHandler.addRecord(vf.getComponentType().name(), vf.getName(), vf.getUUID(), vf.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), checkouRes.right().value().getFormattedMessage());
            return false;
        }
        Resource resourceToUpdate = new Resource(((Resource) checkouRes.left().value()).getComponentMetadataDefinition());
        resourceToUpdate.setDerivedFromGenericType(((Resource) checkouRes.left().value()).getDerivedFromGenericType());
        resourceToUpdate.setDerivedFromGenericVersion(((Resource) checkouRes.left().value()).getDerivedFromGenericVersion());
        resourceToUpdate.setCsarVersion(Double.toString(Double.parseDouble(latestVersion)));
        resourceToUpdate.setCategories(((Resource)checkouRes.left().value()).getCategories());
        try {
            Resource updateResourceFromCsarRes = resourceBusinessLogic.validateAndUpdateResourceFromCsar(resourceToUpdate, user, null, null, resourceToUpdate.getUniqueId());
        } catch(ByResponseFormatComponentException e){
            outputHandler.addRecord(resourceToUpdate.getComponentType().name(), resourceToUpdate.getName(), resourceToUpdate.getUUID(), resourceToUpdate.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), e.getResponseFormat().getFormattedMessage());
            log.info("Failed to update vf with name {}, invariantUUID {}, version {} and latest VSP {}. ", vf.getName(), vf.getInvariantUUID(), vf.getVersion(), latestVersion);
            return false;
        }
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> certifyRes = performFullCertification(checkouRes.left().value());
        if (certifyRes.isRight()) {
            log.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, checkouRes.left().value().getName(), checkouRes.left().value().getInvariantUUID(), checkouRes.left().value().getVersion(), LifeCycleTransitionEnum.CERTIFY);
            outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getInvariantUUID(), checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), certifyRes.right().value().getFormattedMessage());
            return false;
        }
        log.info("Full certification of vf with name {}, invariantUUID {}, version {} finished . ", vf.getName(), vf.getInvariantUUID(), vf.getVersion(), latestVersion);
        outputHandler.addRecord(certifyRes.left().value().getComponentType().name(), certifyRes.left().value().getName(), certifyRes.left().value().getUUID(), certifyRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.COMPLETED.name(), getVfUpgradeStatus(true, isInstance));
        return true;
    }

    private boolean upgradeComponentWithLatestGeneric(org.openecomp.sdc.be.model.Component component, boolean isInstance) {
        String derivedFromGenericType = component.getDerivedFromGenericType();
        String derivedFromGenericVersion = component.getDerivedFromGenericVersion();
        org.openecomp.sdc.be.model.Component updatedComponent = component;
        if (failedToFindDerivedResourcesOfNodeType(component, derivedFromGenericType, derivedFromGenericVersion)) {
            return false;
        }
        if (StringUtils.isEmpty(derivedFromGenericType) ||
                latestVersionExists(latestGenericTypes.get(derivedFromGenericType), derivedFromGenericVersion) ||
                isVfcUpgradeRequired ||
                isAllottedAndProxySupported) {
            if (StringUtils.isNotEmpty(derivedFromGenericType)) {
                log.info("Newer version {} of derived from generic type {} exists. ", latestGenericTypes.get(derivedFromGenericType).getJsonMetadataField(JsonPresentationFields.VERSION), derivedFromGenericType);
            }
            else {
                log.info("The vf resource with name {}, invariantUUID {}, version {},  has an empty derivedFromGenericType field. ", component.getName(), component.getInvariantUUID(), component.getVersion());
            }
            updatedComponent = checkOutAndCertifyComponent(component);
        } else {
            log.info("The version {} of derived from generic type {} is up to date. No need to upgrade component with name {}, invariantUUID {} and version {}. ", latestGenericTypes.get(derivedFromGenericType), derivedFromGenericType, component.getName(), component.getInvariantUUID(), component.getVersion());
        }
        if (updatedComponent != null) {
            log.info(UPGRADE_COMPONENT_SUCCEEDED, component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion());
            outputHandler.addRecord(updatedComponent.getComponentType().name(), updatedComponent.getName(), updatedComponent.getUUID(), updatedComponent.getUniqueId(), MigrationResult.MigrationStatus.COMPLETED.name(),
                    getVfUpgradeStatus(!updatedComponent.equals(component), isInstance));
        }
        return true;
    }

    private org.openecomp.sdc.be.model.Component checkOutAndCertifyComponent(org.openecomp.sdc.be.model.Component component) {

        log.info("Starting to perform check out of vf with name {}, invariantUUID {}, version {}. ", component.getName(), component.getInvariantUUID(), component.getVersion());
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkoutRes = checkOutComponent(component);
        if (checkoutRes.isRight()) {
            log.error(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.CHECKOUT);
            outputHandler.addRecord(component.getComponentType().name(), component.getName(), component.getInvariantUUID(), component.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), checkoutRes.right().value().getFormattedMessage());
            return null;
        }

        if (!updateCompositionFailed(component, checkoutRes.left().value())) {
            return null;
        }
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> certifyRes = performFullCertification(checkoutRes.left().value());
        if (certifyRes.isRight()) {
            log.error(FAILED_TO_UPGRADE_COMPONENT, component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion(), "performFullCertification", certifyRes.right().value());
            outputHandler.addRecord(checkoutRes.left().value().getComponentType().name(), checkoutRes.left().value().getName(), checkoutRes.left().value().getInvariantUUID(), checkoutRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), certifyRes.right().value().getFormattedMessage());
            return null;
        }
        return certifyRes.left().value();
    }

    private boolean failedToFindDerivedResourcesOfNodeType(org.openecomp.sdc.be.model.Component component, String derivedFromGenericType, String derivedFromGenericVersion) {
        if (StringUtils.isNotEmpty(derivedFromGenericType) && !latestGenericTypes.containsKey(derivedFromGenericType)) {
            log.info("Starting upgrade vf with name {}, invariantUUID {}, version {}, latest derived from generic type {}, latest derived from generic version {}. ", component.getName(), component.getInvariantUUID(), component.getVersion(), derivedFromGenericType, derivedFromGenericVersion);
            log.info("Starting to fetch latest generic node type {}. ", derivedFromGenericType);
            Either<List<GraphVertex>, TitanOperationStatus> getDerivedRes = findDerivedResources(derivedFromGenericType);
            if (getDerivedRes.isRight()) {
                outputHandler.addRecord(component.getComponentType().name(), component.getName(), component.getInvariantUUID(), component.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), getDerivedRes.right().value());
                log.info("Failed to upgrade component with name {}, invariantUUID {}, version {} and latest generic. Status is {}. ", component.getName(), component.getInvariantUUID(), component.getVersion(), derivedFromGenericType);
                return true;
            }
            latestGenericTypes.put(derivedFromGenericType, getDerivedRes.left().value().get(0));
        }
        return false;
    }

    private boolean updateCompositionFailed(org.openecomp.sdc.be.model.Component component, org.openecomp.sdc.be.model.Component checkoutResource) {
        //try to update included VFCs, if it is either required as per configuration or an allotted resource
        if ((isVfcUpgradeRequired && CollectionUtils.isNotEmpty(checkoutResource.getComponentInstances())) || isAllottedAndProxySupported) {
            log.info("VFC upgrade is required: updating components of vf with name {}, invariantUUID {}, version {}. ", component.getName(), component.getInvariantUUID(), component.getVersion());
            Either<org.openecomp.sdc.be.model.Component, ResponseFormat> updateCompositionRes = updateComposition(checkoutResource);
            if (updateCompositionRes.isRight()) {
                if (log.isErrorEnabled()) {
                    log.error(FAILED_TO_UPGRADE_COMPONENT, checkoutResource.getComponentType().name(), checkoutResource.getName(), checkoutResource.getInvariantUUID(), checkoutResource.getVersion(), "updateComposition", updateCompositionRes.right().value().getFormattedMessage());
                }
                outputHandler.addRecord(checkoutResource.getComponentType().name(), checkoutResource.getName(), checkoutResource.getUUID(), checkoutResource.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), updateCompositionRes.right().value().getFormattedMessage());
                return true;
            }
        }
        return false;
    }

    private StorageOperationStatus upgradeNodeType(GraphVertex nodeTypeV, List<String> allCertifiedUids) {
        StorageOperationStatus result = StorageOperationStatus.OK;
        log.info("Starting upgrade node type with name {}, invariantUUID {}, version{}. ", nodeTypeV.getMetadataProperty(GraphPropertyEnum.NAME), nodeTypeV.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID), nodeTypeV.getMetadataProperty(GraphPropertyEnum.VERSION));
        log.info("Starting to find derived to for node type with name {}, invariantUUID {}, version{}. ", nodeTypeV.getMetadataProperty(GraphPropertyEnum.NAME), nodeTypeV.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID), nodeTypeV.getMetadataProperty(GraphPropertyEnum.VERSION));
        Either<List<GraphVertex>, TitanOperationStatus> parentResourceRes = titanDao.getParentVertecies(nodeTypeV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.ParseMetadata);
        if (parentResourceRes.isRight() && parentResourceRes.right().value() != TitanOperationStatus.NOT_FOUND) {
            return DaoStatusConverter.convertTitanStatusToStorageStatus(parentResourceRes.right().value());

        }
        List<GraphVertex> derivedResourcesUid = getAllDerivedGraphVertices(allCertifiedUids, parentResourceRes);
        String uniqueId = (String) nodeTypeV.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID);

        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getRes = toscaOperationFacade.getToscaElement(uniqueId);
        if (getRes.isRight()) {
            log.info("failed to fetch element with uniqueId {} ", uniqueId);
            return getRes.right().value();
        }

        Resource nodeType = (Resource)getRes.left().value();
        if (!upgradedNodeTypesMap.containsKey(nodeType.getToscaResourceName()) && !nodeTypes.stream().anyMatch(p -> p.equals(nodeType.getToscaResourceName()))
            && !isNodeTypeUpgradeSucceeded((Resource) getRes.left().value())) {
                return StorageOperationStatus.GENERAL_ERROR;
        }
        for (GraphVertex chV : derivedResourcesUid) {
            result = upgradeNodeType(chV, allCertifiedUids);
            log.info("Upgrade node type with name {}, invariantUUID {}, version {} has been finished with the status {}", chV.getMetadataProperty(GraphPropertyEnum.NAME), chV.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID), chV.getMetadataProperty(GraphPropertyEnum.VERSION), result);
        }
        return result;
    }

    private boolean isNodeTypeUpgradeSucceeded(Resource nodeType) {
        log.info("Starting to perform check out of node type with name {}, invariantUUID {}, version {}. ", nodeType.getName(), nodeType.getInvariantUUID(), nodeType.getVersion());
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkouRes =
                lifecycleBusinessLogic.changeComponentState(nodeType.getComponentType(), nodeType.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, changeInfo, true, false);
        if (checkouRes.isRight()) {
            log.info("Failed to check out node type with name {}, invariantUUID {} due to {}", nodeType.getName(), nodeType.getInvariantUUID(), checkouRes.right().value());
            return false;
        }
        if (performFullCertification(checkouRes.left().value()).isLeft()) {
            upgradedNodeTypesMap.put(nodeType.getToscaResourceName(), checkouRes.left().value());
            titanDao.commit();
            return true;
        }
        return false;
    }

    private List<GraphVertex> getAllDerivedGraphVertices(List<String> allCertifiedUids, Either<List<GraphVertex>, TitanOperationStatus> parentResources) {
        List<GraphVertex> derivedResourcesUid = new ArrayList<>();

        if (parentResources.isLeft()) {
            for (GraphVertex chV : parentResources.left().value()) {
                Optional<String> op = allCertifiedUids.stream().filter(id -> id.equals((String) chV.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID))).findAny();
                if (op.isPresent()) {
                    derivedResourcesUid.add(chV);
                }
            }
        }
        return derivedResourcesUid;
    }

    private Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> performFullCertification(org.openecomp.sdc.be.model.Component component) {
        log.info("Starting to perform full certification of {} with name {}, invariantUUID {}, version {}. ",
                component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion());

        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> changeStateEither = lifecycleBusinessLogic.changeComponentState(component.getComponentType(), component.getUniqueId(), user, LifeCycleTransitionEnum.CERTIFICATION_REQUEST, changeInfo, true, false);
        if (changeStateEither.isRight()) {
            log.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.CERTIFICATION_REQUEST);
            return changeStateEither;
        }
        changeStateEither = lifecycleBusinessLogic.changeComponentState(component.getComponentType(), changeStateEither.left().value().getUniqueId(), user, LifeCycleTransitionEnum.START_CERTIFICATION, changeInfo, true, false);
        if (changeStateEither.isRight()) {
            log.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.START_CERTIFICATION);
            return changeStateEither;
        }
        changeStateEither = lifecycleBusinessLogic.changeComponentState(component.getComponentType(), changeStateEither.left().value().getUniqueId(), user, LifeCycleTransitionEnum.CERTIFY, changeInfo, true, false);
        if (changeStateEither.isRight()) {
            log.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.CERTIFY);
        } else {
            log.info("Full certification of {} with name {}, invariantUUID {}, version {} finished successfully",
                    changeStateEither.left().value().getComponentType().getValue(), changeStateEither.left().value().getName(),
                    changeStateEither.left().value().getInvariantUUID(), changeStateEither.left().value().getVersion());
        }
        return changeStateEither;
    }

    private Either<List<GraphVertex>, TitanOperationStatus> findDerivedResources(String parentResource) {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());

        propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, parentResource);
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        return titanDao.getByCriteria(VertexTypeEnum.NODE_TYPE, propertiesToMatch, JsonParseFlagEnum.ParseMetadata);
    }

    private boolean latestVersionExists(GraphVertex latestDerivedFrom, String currentVersion) {
        return isGreater((String) latestDerivedFrom.getJsonMetadataField(JsonPresentationFields.VERSION), currentVersion);
    }

    private boolean isGreater(String latestVersion, String currentVersion) {
        if (latestVersion != null && currentVersion == null) {
            return true;
        }
        if (latestVersion == null) {
            return false;
        }
        return Double.parseDouble(latestVersion) > Double.parseDouble(currentVersion);
    }

    private Either<List<String>, TitanOperationStatus> getAllLatestCertifiedComponentUids(VertexTypeEnum vertexType, ComponentTypeEnum componentType) {
        log.info("Starting to fetch all latest certified not checked out components with type {} upon upgrade migration 1710 process", componentType);
        Either<List<String>, TitanOperationStatus> result = null;
        Map<String, String> latestCertifiedMap = new HashMap<>();
        Map<String, String> latestNotCertifiedMap = new HashMap<>();

        Either<List<GraphVertex>, TitanOperationStatus> getComponentsRes = getAllLatestComponents(vertexType, componentType);
        if (getComponentsRes.isRight() && getComponentsRes.right().value() != TitanOperationStatus.NOT_FOUND) {
            log.error("Failed to fetch all latest certified not checked out components with type {}. Status is {}. ", componentType, getComponentsRes.right().value());
            result = Either.right(getComponentsRes.right().value());
        }
        if (getComponentsRes.isRight()) {
            result = Either.left(new ArrayList<>());
        }
        if (result == null) {
            for (GraphVertex component : getComponentsRes.left().value()) {
                String invariantUUID = (String) component.getJsonMetadataField(JsonPresentationFields.INVARIANT_UUID);
                if (((String) component.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals(LifecycleStateEnum.CERTIFIED.name())) {
                    latestCertifiedMap.put(invariantUUID, (String) component.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID));
                } else {
                    latestNotCertifiedMap.put(invariantUUID, (String) component.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID));
                }
            }
            result = Either.left(latestCertifiedMap.entrySet().stream().filter(e -> !latestNotCertifiedMap.containsKey(e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList()));
        }
        return result;
    }

    private Either<List<GraphVertex>, TitanOperationStatus> getAllLatestComponents(VertexTypeEnum vertexType, ComponentTypeEnum componentType) {

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);

        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        if (vertexType == VertexTypeEnum.TOPOLOGY_TEMPLATE && componentType == ComponentTypeEnum.RESOURCE) {
            propertiesNotToMatch.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.CVFC.name());
        }
        return titanDao.getByCriteria(vertexType, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseMetadata);
    }

    private Either<List<GraphVertex>, StorageOperationStatus> getLatestByName(GraphPropertyEnum property, String nodeName) {

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);

        propertiesToMatch.put(property, nodeName);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        Either<List<GraphVertex>, TitanOperationStatus> highestResources = titanDao.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseMetadata);
        if (highestResources.isRight()) {
            TitanOperationStatus status = highestResources.right().value();
            log.debug("Failed to fetch resource with name {}. Status is {} ", nodeName, status);
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
        }
        List<GraphVertex> resources = highestResources.left().value();
        List<GraphVertex> result = new ArrayList<>();
        for (GraphVertex component : resources) {
            if (((String) component.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals(LifecycleStateEnum.CERTIFIED.name())) {
                result.add(component);
            }
        }
        return Either.left(result);
    }

    private void deleteMarkedComponents(NodeTypeEnum componentType, int toBeDeleted) {
        Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanComponentsResult;
        List<NodeTypeEnum> cleanComponents = new ArrayList<>();
        cleanComponents.add(componentType);
        try {
            log.info("Trying to delete {} components of type {} marked as deleted", toBeDeleted, componentType);
            cleanComponentsResult = componentsCleanBusinessLogic.cleanComponents(cleanComponents, true);
            logDeleteResult(componentType, cleanComponentsResult.get(componentType));
        }
        catch (Exception e) {
            log.error("Exception occurred {}", e.getMessage());
            log.debug("Exception occurred", e);
        }
    }

    private void logDeleteResult(NodeTypeEnum type, Either<List<String>, ResponseFormat> deleteResult) {
        if (deleteResult == null) {
            return;
        }
        if (deleteResult.isLeft()) {
            log.info("Checked out {} versions are deleted successfully", type.getName());
        }
        else {
            log.info("Cleanup of checked out {} versions failed due to the error: {}", type.getName(), deleteResult.right().value().getFormattedMessage());
        }
    }

    private void markCheckedOutResourceAsDeletedIfUpgradeFailed(String certUid, boolean isNotFailed) {
        String checkedOutUniqueId = certifiedToNextCheckedOutUniqueId.remove(certUid);
        if (!isNotFailed && checkedOutUniqueId != null) {
            try {
                //mark as deleted the checked out resource as this upgrade failed
                ResponseFormat respFormat = resourceBusinessLogic.deleteResource(checkedOutUniqueId.toLowerCase(), user);
                log.info("Checked out resource uniqueId = {} is marked as deleted, status: {}", checkedOutUniqueId, respFormat.getFormattedMessage());
                deleteResourcesIfLimitIsReached();
            }
            catch (Exception e) {
                log.error("Error occurred:", e);
            }
        }
    }

    private void markCheckedOutServiceAsDeletedIfUpgradeFailed(String certUid, boolean isNotFailed) {
        String checkedOutUniqueId = certifiedToNextCheckedOutUniqueId.remove(certUid);
        if (!isNotFailed && checkedOutUniqueId != null) {
            try {
                //delete the checked out resource as this upgrade failed
                ResponseFormat respFormat = serviceBusinessLogic.deleteService(checkedOutUniqueId.toLowerCase(), user);
                log.info("Checked out service uniqueId = {} is marked as deleted, status: {}", checkedOutUniqueId, respFormat.getFormattedMessage());
                deleteServicesIfLimitIsReached();
            } catch (Exception e) {
                log.error("Error occurred:", e);
            }
        }

    }

    void deleteResourcesIfLimitIsReached() {
        markedAsDeletedResourcesCnt++;
        if (markedAsDeletedResourcesCnt >= maxDeleteComponents) {
            deleteMarkedComponents(NodeTypeEnum.Resource, markedAsDeletedResourcesCnt);
            markedAsDeletedResourcesCnt = 0;
        }
    }

    void deleteServicesIfLimitIsReached() {
        markedAsDeletedServicesCnt++;
        if (markedAsDeletedServicesCnt >= maxDeleteComponents) {
            deleteMarkedComponents(NodeTypeEnum.Service, markedAsDeletedServicesCnt);
            markedAsDeletedServicesCnt = 0;
        }
    }

    boolean isLockDeleteOperationSucceeded() {
        StorageOperationStatus status = componentsCleanBusinessLogic.lockDeleteOperation();

        switch(status) {
            case OK:
                log.info("Lock delete operation succeeded");
                isCleanupLocked = true;
                break;
            case FAILED_TO_LOCK_ELEMENT:
                log.info("Delete operation node is already locked");
                isCleanupLocked = isLockRetrySucceeded();
                break;
            default:
                log.error("Lock delete operation failed due to the error: {}", status);
                isCleanupLocked = false;
                break;
        }
        return isCleanupLocked;
    }

    private boolean isLockRetrySucceeded() {
        long startTime = System.currentTimeMillis();
        //try to lock the cleanup resource until configurable time interval is finished
        while (System.currentTimeMillis() - startTime <= deleteLockTimeoutInSeconds * 1000) {
            try {
                //sleep one second and try lock again
                Thread.sleep(1000);
                if (componentsCleanBusinessLogic.lockDeleteOperation() == StorageOperationStatus.OK) {
                    return true;
                }
            } catch (InterruptedException e) {
                log.error("Error occurred: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    void unlockDeleteOperation() {
        if (isCleanupLocked) {
            try {
                componentsCleanBusinessLogic.unlockDeleteOperation();
                log.info("Lock delete operation is canceled");
                isCleanupLocked = false;
            }
            catch (Exception e) {
                log.debug("Failed to unlock delete operation", e);
                log.error("Failed to unlock delete operation due to the error {}", e.getMessage());
            }
        }
    }

    private Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkOutComponent(org.openecomp.sdc.be.model.Component component) {
        log.info("Starting to perform check out of {} {}, uniqueId = {}", component.getComponentType(), component.getName(), component.getUniqueId());
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkoutRes =
                lifecycleBusinessLogic.changeComponentState(component.getComponentType(), component.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, changeInfo, true, false);
        if (checkoutRes.isLeft()) {
            //add the uniqueId from "upgradeVf(String uniqueId)" and checkouRes's uniqueUID to the new map
            certifiedToNextCheckedOutUniqueId.put(component.getUniqueId(), checkoutRes.left().value().getUniqueId());
            log.debug("Add checked out component uniqueId = {} produced from certified component uniqueId = {} to the checked out map", checkoutRes.left().value().getUniqueId(), component.getUniqueId());
        }
        return checkoutRes;
    }

    UpgradeStatus getVfUpgradeStatus(boolean isUpgraded, boolean isInstance) {
        if (isUpgraded) {
            return isInstance ? UpgradeStatus.UPGRADED_AS_INSTANCE : UpgradeStatus.UPGRADED;
        }
        return UpgradeStatus.NOT_UPGRADED;
    }

}
