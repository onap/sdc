package org.openecomp.sdc.asdctool.migration.tasks.mig1710;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.core.task.PostMigration;
import org.openecomp.sdc.asdctool.migration.tasks.handlers.XlsOutputHandler;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CsarOperation;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component
public class UpgradeMigration1710 implements PostMigration {

    private static final String UNKNOWN = "UNKNOWN";

    private static final String CHECKOUT_MESSAGE = "checkout upon upgrade migration";

    private static final String FAILED_TO_CHANGE_STATE_OF_COMPONENT = "Failed to change state of component with name {}, invariantUUID {}, version {} to {}. ";

    private static final String FAILED_TO_UPGRADE_COMPONENT = "Failed to upgrade {} with name {}, invariantUUID {}, version {}. Operation {}. The reason for failure: {}. ";

    private static final String UPGRADE_COMPONENT_SUCCEEDED = "Upgrade of {} with name {}, invariantUUID {}, version {} finished successfully. ";

    private static final String UPGRADE_VFS_FAILED = "Upgrade VFs upon upgrade migration 1710 process failed. ";

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeMigration1710.class);

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
    private CsarOperation csarOperation;
    
    @Autowired
    private ServiceComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Autowired
    private ComponentsUtils componentsUtils;

    private final XlsOutputHandler outputHandler = new XlsOutputHandler("COMPONENT TYPE", "COMPONENT NAME", "COMPONENT UUID", "COMPONENT UNIQUE_ID", "UPGRADE STATUS", "DESCRIPTION");

    private User user = null;

    private final LifecycleChangeInfoWithAction changeInfo = new  LifecycleChangeInfoWithAction(CHECKOUT_MESSAGE, LifecycleChanceActionEnum.UPGRADE_MIGRATION);

    private final Map<String,GraphVertex> latestGenericTypes = new HashMap<>();

    private boolean isVfcUpgradeRequired = false;

    private boolean skipIfUpgradeVfFailed = true;

    /** below methods is defined on package level for testing
     * where Spring object injection is not used  **/
    void setUserAdminOperation(IUserAdminOperation userAdminOperation) { this.userAdminOperation = userAdminOperation; }

    void setTitanDao(TitanDao titanDao) { this.titanDao = titanDao; }

    void setTosckaOperationFacade(ToscaOperationFacade toscaOperationFacade) { this.toscaOperationFacade = toscaOperationFacade; }

    void setLifecycleBusinessLogic(LifecycleBusinessLogic lifecycleBusinessLogic) { this.lifecycleBusinessLogic = lifecycleBusinessLogic; }

    void setComponentsUtils(ComponentsUtils componentsUtils) { this.componentsUtils = componentsUtils; }


    /***********************************************/

    @Override
    public String description() {
        return "Upgrade migration 1710 - post migration task, which is dedicated to upgrade all latest certified (and not checked out) Node types, VFs and Services. ";
    }

    private enum UpgradeStatus{
        UPGRADED,
        NOT_UPGRADED
    }

    @Override
    public MigrationResult migrate() {
        LOGGER.info("Starting upgrade migration 1710 process. ");
        MigrationResult migrationResult = new MigrationResult();

        try{
            boolean result = true;

            isVfcUpgradeRequired = !ConfigurationManager.getConfigurationManager().getConfiguration().getSkipUpgradeVSPsFlag();
            skipIfUpgradeVfFailed = ConfigurationManager.getConfigurationManager().getConfiguration().getSkipUpgradeFailedVfs();
            final String userId = ConfigurationManager.getConfigurationManager().getConfiguration().getAutoHealingOwner();

            Either<User, ActionStatus> userReq = userAdminOperation.getUserData(userId, false);
            if (userReq.isRight()) {
                result = false;
                LOGGER.error("Upgrade migration was failed. User {} resolve failed: {} ", userId, userReq.right().value());
            }
            else {
                user = userReq.left().value();
                LOGGER.info("User {} will perform upgrade operation", user.toString());
            }

            if(result){
                result = upgradeNodeTypes();
            }
            if(result){
                result = upgradeVFs();
            }
            if(result){
                upgradeServices();
            }
            if(result){
                LOGGER.info("Upgrade migration 1710 has been successfully finished. ");
                titanDao.commit();
                migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.COMPLETED);
            } else {
                LOGGER.info("Upgrade migration 1710 was failed. ");
                titanDao.rollback();
                migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.FAILED);
            }
        } catch(Exception e){
            LOGGER.error("Upgrade migration 1710 was failed. ", e);
            titanDao.rollback();
            migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.FAILED);
        } finally {
            outputHandler.writeOutput();
        }
        return migrationResult;
    }

    private StorageOperationStatus upgradeServices() {
        LOGGER.info("Starting upgrade services upon upgrade migration 1710 process. ");
        Map<String, String> latestOriginResourceVersions = new HashMap<>();
        Either<List<String>, TitanOperationStatus> getServicesRes = getAllLatestCertifiedComponentUids(VertexTypeEnum.TOPOLOGY_TEMPLATE, ComponentTypeEnum.SERVICE);
        if(getServicesRes.isRight()){
            return StorageOperationStatus.GENERAL_ERROR;
        }
        for(String currUid : getServicesRes.left().value()){
            try{
                if(handleService(currUid, latestOriginResourceVersions)){
                    titanDao.commit();
                } else {
                    processComponentUpgradeFailure(ComponentTypeEnum.SERVICE.name(), currUid, "");
                }
            } catch(Exception e){
                processComponentUpgradeFailure(ComponentTypeEnum.SERVICE.name(), currUid, e.getMessage());
            }
        }
        return StorageOperationStatus.OK;
    }

    private void processComponentUpgradeFailure(final String name, final String currUid, final String reason) {
        LOGGER.error("Failed to upgrade {} with uniqueId {} due to a reason {}. ", name, currUid, reason);
        titanDao.rollback();
    }

    private boolean handleService(String uniqueId, Map<String, String> latestOriginResourceVersions) {
        LOGGER.info("Starting upgrade Service with uniqueId {} upon upgrade migration 1710 process. ", uniqueId);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getServiceRes = toscaOperationFacade.getToscaElement(uniqueId);
        if(getServiceRes.isRight()){
            LOGGER.error("Failed to upgrade service with uniqueId {} due to {}. ", uniqueId, getServiceRes.right().value());
            outputHandler.addRecord(ComponentTypeEnum.SERVICE.name(), UNKNOWN, UNKNOWN, uniqueId, MigrationResult.MigrationStatus.FAILED.name(), getServiceRes.right().value());
            return false;
        }
        String derivedFromGenericType =  getServiceRes.left().value().getDerivedFromGenericType();
        LOGGER.debug("derivedFromGenericType: {}", derivedFromGenericType );
        if (derivedFromGenericType == null) {
            //malformed field value, upgrade required
            return upgradeService(getServiceRes.left().value());
        }
        if(!latestGenericTypes.containsKey(derivedFromGenericType)){
            Either<List<GraphVertex>, TitanOperationStatus> getDerivedRes = findDerivedResources(derivedFromGenericType);
            if(getDerivedRes.isRight()){
                LOGGER.error(FAILED_TO_UPGRADE_COMPONENT, getServiceRes.left().value().getComponentType().getValue(), getServiceRes.left().value().getName(), getServiceRes.left().value().getInvariantUUID(), getServiceRes.left().value().getVersion(), "findDerivedResources", getDerivedRes.right().value());
                outputHandler.addRecord( getServiceRes.left().value().getComponentType().name(),getServiceRes.left().value().getName(), getServiceRes.left().value().getInvariantUUID(), getServiceRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), getDerivedRes.right().value());
                return false;
            }
            latestGenericTypes.put(derivedFromGenericType, getDerivedRes.left().value().get(0));
        }
        if(latestVersionExists(latestGenericTypes.get(derivedFromGenericType), getServiceRes.left().value().getDerivedFromGenericVersion())){
            return upgradeService(getServiceRes.left().value());
        }
        if(!collectLatestOriginResourceVersions(getServiceRes.left().value(), latestOriginResourceVersions)){
            return false;
        }
        if(shouldUpgrade(getServiceRes.left().value(), latestOriginResourceVersions)){
            return upgradeService(getServiceRes.left().value());
        }
        outputHandler.addRecord(getServiceRes.left().value().getComponentType().name(), getServiceRes.left().value().getName(), getServiceRes.left().value().getInvariantUUID(), getServiceRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.COMPLETED.name(), UpgradeStatus.NOT_UPGRADED);
        return true;
    }

    private boolean collectLatestOriginResourceVersions(org.openecomp.sdc.be.model.Component component,	Map<String, String> latestOriginResourceVersions) {
        if(CollectionUtils.isNotEmpty(component.getComponentInstances())){
            for(ComponentInstance instance : component.getComponentInstances()){
                if(instance.getOriginType() != OriginTypeEnum.ServiceProxy && !latestOriginResourceVersions.containsKey(instance.getToscaComponentName())){
                    VertexTypeEnum vertexType = ModelConverter.getVertexType(instance.getOriginType().name());
                    Either<Resource, StorageOperationStatus> getOriginRes = toscaOperationFacade.getLatestCertifiedByToscaResourceName(instance.getToscaComponentName(), vertexType, JsonParseFlagEnum.ParseMetadata);
                    if(getOriginRes.isRight()){
                        LOGGER.error(FAILED_TO_UPGRADE_COMPONENT, component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion(), "toscaOperationFacade.getLatestCertifiedByToscaResourceName", getOriginRes.right().value());
                        outputHandler.addRecord( component.getComponentType().name(), component.getName(), component.getInvariantUUID(), component.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), getOriginRes.right().value());
                        return false;
                    }
                    latestOriginResourceVersions.put(instance.getToscaComponentName(), getOriginRes.left().value().getVersion());
                }
            }
        }
        return true;
    }

    private boolean shouldUpgrade(org.openecomp.sdc.be.model.Component component, Map<String, String> latestOriginResources) {
        boolean shouldUpgrade = false;
        if(CollectionUtils.isNotEmpty(component.getComponentInstances())){
            for(ComponentInstance instance : component.getComponentInstances()){
                if(instance.getOriginType() == OriginTypeEnum.ServiceProxy){
                    LOGGER.info("The service with name {}, invariantUUID {}, version {}, contains Service proxy instance {}, than the service should be upgraded. ", component.getName(), component.getInvariantUUID(), component.getVersion(), instance.getName());
                    shouldUpgrade = true;
                }
                if(isGreater(latestOriginResources.get(instance.getToscaComponentName()), instance.getComponentVersion())){
                    LOGGER.info("The service with name {}, invariantUUID {}, version {}, contains instance {} from outdated version of origin {} {} , than the service should be upgraded. ", component.getName(), component.getInvariantUUID(), component.getVersion(), instance.getName(), instance.getComponentName(), instance.getComponentVersion());
                    shouldUpgrade = true;
                }
            }
        }
        return shouldUpgrade;
    }

    private boolean upgradeService(org.openecomp.sdc.be.model.Component service) {
        String serviceName = service.getName();
        String serviceUuid = service.getUUID();
        LOGGER.info("Starting upgrade Service with name {}, invariantUUID {}, version {} upon upgrade migration 1710 process. ", serviceName, service.getInvariantUUID(), service.getVersion());
        LOGGER.info("Starting to perform check out of service {}. ", serviceName);
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkouRes = lifecycleBusinessLogic.changeComponentState(service.getComponentType(), service.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, changeInfo, true, false);
        if(checkouRes.isRight()){
            LOGGER.error(FAILED_TO_UPGRADE_COMPONENT, service.getComponentType().getValue(), serviceName, service.getInvariantUUID(), service.getVersion(), "lifecycleBusinessLogic.changeComponentState", checkouRes.right().value().getFormattedMessage());
            outputHandler.addRecord(service.getComponentType().name(), serviceName, serviceUuid, service.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), checkouRes.right().value().getFormattedMessage());
            return false;
        }
        Either<org.openecomp.sdc.be.model.Component, ResponseFormat>  updateCompositionRes = updateComposition(checkouRes.left().value());
        if(updateCompositionRes.isRight()){
            LOGGER.error(FAILED_TO_UPGRADE_COMPONENT, service.getComponentType().getValue(), serviceName, service.getInvariantUUID(), service.getVersion(), "updateComposition", updateCompositionRes.right().value().getFormattedMessage());
            outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getUUID(), checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), updateCompositionRes.right().value().getFormattedMessage());
            return false;
        }
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat>  certifyRes = performFullCertification(checkouRes.left().value());
        if(certifyRes.isRight()){
            LOGGER.error(FAILED_TO_UPGRADE_COMPONENT, service.getComponentType().getValue(), serviceName, service.getInvariantUUID(), service.getVersion(), "performFullCertification", certifyRes.right().value().getFormattedMessage());
            outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getInvariantUUID(), checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), certifyRes.right().value().getFormattedMessage());
            return false;
        }
        outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), serviceUuid, checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.COMPLETED.name(), UpgradeStatus.UPGRADED);
        return true;
    }

    private Either<org.openecomp.sdc.be.model.Component, ResponseFormat> updateComposition(org.openecomp.sdc.be.model.Component component) {
        Either<ComponentInstance, ResponseFormat> upgradeInstanceRes;
        for(ComponentInstance instance : component.getComponentInstances()){
            upgradeInstanceRes = upgradeInstance(component, instance);
            if(upgradeInstanceRes.isRight()) {
                LOGGER.error(FAILED_TO_UPGRADE_COMPONENT, component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion(), "upgradeInstance", upgradeInstanceRes.right().value().getFormattedMessage());
                outputHandler.addRecord(component.getComponentType().name(), component.getName(), component.getUUID(), component.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), upgradeInstanceRes.right().value().getFormattedMessage());
                return Either.right(upgradeInstanceRes.right().value());
            }
        }
        return Either.left(component);
    }

    private Either<ComponentInstance, ResponseFormat> upgradeInstance(org.openecomp.sdc.be.model.Component component, ComponentInstance instance) {
        LOGGER.info("Starting upgrade {} instance {} upon upgrade migration 1710 process. ", component.getComponentType().getValue(), instance.getName());
        ComponentInstance newComponentInstance = new ComponentInstance(instance);
        if(instance.getOriginType() == OriginTypeEnum.ServiceProxy){
            return upgradeServiceProxyInstance(component, instance, newComponentInstance);
        }
        return upgradeResourceInstance(component, instance, newComponentInstance);
    }

    private Either<ComponentInstance, ResponseFormat> upgradeResourceInstance(org.openecomp.sdc.be.model.Component component,
                                                            ComponentInstance instance, ComponentInstance newComponentInstance) {
        LOGGER.info("Starting upgrade {} instance {} upon upgrade migration 1710 process. ", component.getComponentType().getValue(), instance.getName());
        VertexTypeEnum vertexType = ModelConverter.getVertexType(instance.getOriginType().name());
        Either<Resource, StorageOperationStatus> getOriginRes = toscaOperationFacade.getLatestCertifiedByToscaResourceName(instance.getToscaComponentName(), vertexType, JsonParseFlagEnum.ParseMetadata);
        if(getOriginRes.isRight()){
            LOGGER.info("Upgrade of {} instance {} upon upgrade migration 1710 process failed due to a reason {}. ",
                    component.getComponentType().getValue(), instance.getName(), getOriginRes.right().value());
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getOriginRes.right().value(), instance.getOriginType().getComponentType())));
        }
        newComponentInstance.setComponentName(getOriginRes.left().value().getName());
        newComponentInstance.setComponentUid(getOriginRes.left().value().getUniqueId());
        newComponentInstance.setComponentVersion(getOriginRes.left().value().getVersion());
        newComponentInstance.setToscaComponentName(((Resource)getOriginRes.left().value()).getToscaResourceName());
        if(isGreater(getOriginRes.left().value().getVersion(), instance.getComponentVersion())){
            return changeAssetVersion(component, instance, newComponentInstance);
        }

        //upgrade nodes contained by CVFC
        if(isVfcUpgradeRequired && newComponentInstance.getOriginType() == OriginTypeEnum.CVFC &&
                                                    !upgradeVf(getOriginRes.left().value().getUniqueId())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        LOGGER.info("Upgrade of {} instance {} upon upgrade migration 1710 process finished successfully. ",
                                                                    component.getComponentType().getValue(), instance.getName());
        return Either.left(instance);
    }

    private Either<ComponentInstance, ResponseFormat> upgradeServiceProxyInstance(org.openecomp.sdc.be.model.Component component, ComponentInstance instance, ComponentInstance newComponentInstance) {
        Either<List<GraphVertex>, TitanOperationStatus> getLatestOriginServiceRes = getLatestCertifiedService(instance.getSourceModelInvariant());
        if(getLatestOriginServiceRes.isRight()){
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertTitanStatusToStorageStatus(getLatestOriginServiceRes.right().value()), instance.getOriginType().getComponentType())));
        }
        newComponentInstance.setComponentVersion((String) getLatestOriginServiceRes.left().value().get(0).getJsonMetadataField(JsonPresentationFields.VERSION));
        newComponentInstance.setSourceModelUid((String) getLatestOriginServiceRes.left().value().get(0).getJsonMetadataField(JsonPresentationFields.UNIQUE_ID));
        newComponentInstance.setSourceModelName((String) getLatestOriginServiceRes.left().value().get(0).getJsonMetadataField(JsonPresentationFields.NAME));
        newComponentInstance.setSourceModelUuid((String) getLatestOriginServiceRes.left().value().get(0).getJsonMetadataField(JsonPresentationFields.UUID));
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
        LOGGER.info("Starting upgrade node types upon upgrade migration 1710 process. ");
        String toscaConformanceLevel = ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel();
        Map<String, List<String>> resourcesForUpgrade = ConfigurationManager.getConfigurationManager().getConfiguration().getResourcesForUpgrade();
        Map<String, org.openecomp.sdc.be.model.Component> upgradedNodeTypesMap = new HashMap<> ();
        List<String> nodeTypes;
        if(resourcesForUpgrade.containsKey(toscaConformanceLevel)){
             nodeTypes = resourcesForUpgrade.get(toscaConformanceLevel);
            if(nodeTypes !=null && !nodeTypes.isEmpty()){
                Either<List<String>, TitanOperationStatus> getRes = getAllLatestCertifiedComponentUids(VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE);
                if(getRes.isRight()){
                    return false;
                }
                List<String> allNodeTypes = getRes.left().value();

                for(String toscaResourceName: nodeTypes){
                    Either<List<GraphVertex>, StorageOperationStatus> status = getLatestByName(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
                    if (status.isRight()) {
                        LOGGER.error("Failed to find node type {} ", toscaResourceName);
                        return false;
                    }
                    List<GraphVertex> vList = status.left().value();
                    for (GraphVertex vertex : vList) {
                        StorageOperationStatus updateRes = upgradeNodeType(vertex, upgradedNodeTypesMap, allNodeTypes, nodeTypes);
                        if (updateRes != StorageOperationStatus.OK) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean upgradeVFs() {
        LOGGER.info("Starting upgrade VFs upon upgrade migration 1710 process. ");
        Either<List<String>, TitanOperationStatus> getVfsRes = getAllLatestCertifiedComponentUids(VertexTypeEnum.TOPOLOGY_TEMPLATE, ComponentTypeEnum.RESOURCE);
        if(getVfsRes.isRight()){
            LOGGER.info(UPGRADE_VFS_FAILED);
            return false;
        }
        for (String currUid : getVfsRes.left().value()) {
            try {
                if (!upgradeVf(currUid)) {
                    processComponentUpgradeFailure(ComponentTypeEnum.RESOURCE.name(), currUid, "");
                    if (!skipIfUpgradeVfFailed) {
                        LOGGER.info(UPGRADE_VFS_FAILED);
                        return false;
                    }
                }
                titanDao.commit();
            } catch (Exception e) {
                processComponentUpgradeFailure(ComponentTypeEnum.RESOURCE.name(), currUid, e.getMessage());
                if (!skipIfUpgradeVfFailed) {
                    LOGGER.info(UPGRADE_VFS_FAILED);
                    return false;
                }
            }
        }
        LOGGER.info("Upgrade VFs upon upgrade migration 1710 process finished successfully. ");
        return true;
    }

    private boolean upgradeVf(String uniqueId) {
        LOGGER.info("Starting upgrade VF with uniqueId {} upon upgrade migration 1710 process. ", uniqueId);
        Either<String, StorageOperationStatus> latestVersionRes;
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getRes = toscaOperationFacade.getToscaElement(uniqueId);
        if(getRes.isRight()){
            LOGGER.debug("Failed to fetch VF with uniqueId {} upon upgrade migration 1710 process. ", uniqueId);
            outputHandler.addRecord(ComponentTypeEnum.RESOURCE.name(), UNKNOWN, UNKNOWN, uniqueId, MigrationResult.MigrationStatus.FAILED.name(), getRes.right().value());
            return false;
        }
        if(StringUtils.isNotEmpty(getRes.left().value().getCsarUUID())){
            LOGGER.info("Going to fetch the latest version of VSP with csarUUID {} upon upgrade migration 1710 process. ", getRes.left().value().getCsarUUID());
            latestVersionRes = csarOperation.getCsarLatestVersion(getRes.left().value().getCsarUUID(), user);
            if(latestVersionRes.isRight()){
                LOGGER.debug("Failed to fetch the latest version of VSP with csarUUID {} upon upgrade migration 1710 process. ", getRes.left().value().getCsarUUID());
                outputHandler.addRecord(getRes.left().value().getComponentType().name(), getRes.left().value().getName(), getRes.left().value().getUUID(), getRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(),latestVersionRes.right().value());
                return false;
            }
            if(isGreater(latestVersionRes.left().value(), getRes.left().value().getCsarVersion())){
                return upgradeVfWithLatestVsp(getRes.left().value(), latestVersionRes);
            }
            if (!isVfcUpgradeRequired){
                LOGGER.warn("Warning: No need to upgrade VF with name {}, invariantUUID {}, version {} and VSP version {}. No new version of VSP. ", getRes.left().value().getName(), getRes.left().value().getInvariantUUID(), getRes.left().value().getVersion(), getRes.left().value().getCsarVersion());
            }
        }
        return upgradeComponentWithLatestGeneric(getRes.left().value());
    }

    private boolean upgradeVfWithLatestVsp(org.openecomp.sdc.be.model.Component vf, Either<String, StorageOperationStatus> latestVersionRes) {
        LOGGER.info("Starting upgrade vf with name {}, invariantUUID {}, version {} and latest VSP version {} upon upgrade migration 1710 process. ", vf.getName(), vf.getInvariantUUID(), vf.getVersion(), latestVersionRes.left().value());
        LOGGER.info("Starting to perform check out of vf with name {}, invariantUUID {}, version {}. ", vf.getName(),vf.getInvariantUUID(), vf.getVersion());
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkouRes = lifecycleBusinessLogic.changeComponentState(vf.getComponentType(), vf.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, changeInfo, true, false);
        if(checkouRes.isRight()){
            outputHandler.addRecord(vf.getComponentType().name(), vf.getName(), vf.getUUID(), vf.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), checkouRes.right().value().getFormattedMessage());
            return false;
        }
        LOGGER.info("Starting update vf with name {}, invariantUUID {}, version {} and latest VSP {}. ", vf.getName(), vf.getInvariantUUID(), vf.getVersion(), latestVersionRes.left().value());
        Resource resourceToUpdate = new Resource(((Resource) checkouRes.left().value()).getComponentMetadataDefinition());
        resourceToUpdate.setDerivedFromGenericType(((Resource) checkouRes.left().value()).getDerivedFromGenericType());
        resourceToUpdate.setDerivedFromGenericVersion(((Resource) checkouRes.left().value()).getDerivedFromGenericVersion());
        resourceToUpdate.setCsarVersion(Double.toString(Double.parseDouble(latestVersionRes.left().value())));
        Either<Resource, ResponseFormat> updateResourceFromCsarRes = resourceBusinessLogic.validateAndUpdateResourceFromCsar(resourceToUpdate, user, null, null, resourceToUpdate.getUniqueId());
        if(updateResourceFromCsarRes.isRight()){
            outputHandler.addRecord(resourceToUpdate.getComponentType().name(), resourceToUpdate.getName(), resourceToUpdate.getUUID(), resourceToUpdate.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), updateResourceFromCsarRes.right().value().getFormattedMessage());
            LOGGER.info("Failed to update vf with name {}, invariantUUID {}, version {} and latest VSP {}. ", vf.getName(), vf.getInvariantUUID(), vf.getVersion(), latestVersionRes.left().value());
            return false;
        }
        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> certifyRes =  performFullCertification(checkouRes.left().value());
        if(certifyRes.isRight()){
            LOGGER.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, checkouRes.left().value().getName(), checkouRes.left().value().getInvariantUUID(), checkouRes.left().value().getVersion(), LifeCycleTransitionEnum.CERTIFY);
            outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getInvariantUUID(), checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), certifyRes.right().value().getFormattedMessage());
            return false;
        }
        LOGGER.info("Full certification of vf with name {}, invariantUUID {}, version {} finished . ", vf.getName(), vf.getInvariantUUID(), vf.getVersion(), latestVersionRes.left().value());
        outputHandler.addRecord(certifyRes.left().value().getComponentType().name(), certifyRes.left().value().getName(), certifyRes.left().value().getUUID(), certifyRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.COMPLETED.name(), UpgradeStatus.UPGRADED);
        return true;
    }

    private boolean upgradeComponentWithLatestGeneric(org.openecomp.sdc.be.model.Component component) {
        String derivedFromGenericType =  component.getDerivedFromGenericType();
        String derivedFromGenericVersion = component.getDerivedFromGenericVersion();
        org.openecomp.sdc.be.model.Component updatedComponent = component;
        if(StringUtils.isNotEmpty(derivedFromGenericType) && !latestGenericTypes.containsKey(derivedFromGenericType)){
            LOGGER.info("Starting upgrade vf with name {}, invariantUUID {}, version {}, latest derived from generic type {}, latest derived from generic version {}. ", component.getName(), component.getInvariantUUID(), component.getVersion(), derivedFromGenericType, derivedFromGenericVersion);
            LOGGER.info("Starting to fetch latest generic node type {}. ", derivedFromGenericType);
            Either<List<GraphVertex>, TitanOperationStatus> getDerivedRes = findDerivedResources(derivedFromGenericType);
            if(getDerivedRes.isRight()){
                outputHandler.addRecord(component.getComponentType().name(), component.getName(), component.getInvariantUUID(), component.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), getDerivedRes.right().value());
                LOGGER.info("Failed to upgrade component with name {}, invariantUUID {}, version {} and latest generic. Status is {}. ", component.getName(), component.getInvariantUUID(), component.getVersion(), derivedFromGenericType);
                return false;
            }
            latestGenericTypes.put(derivedFromGenericType, getDerivedRes.left().value().get(0));
        }
        if(StringUtils.isEmpty(derivedFromGenericType) ||
                latestVersionExists(latestGenericTypes.get(derivedFromGenericType), derivedFromGenericVersion) ||
                isVfcUpgradeRequired){
            if(StringUtils.isNotEmpty(derivedFromGenericType))
                LOGGER.info("Newer version {} of derived from generic type {} exists. ", latestGenericTypes.get(derivedFromGenericType).getJsonMetadataField(JsonPresentationFields.VERSION), derivedFromGenericType);
            else
                LOGGER.info("The vf resource with name {}, invariantUUID {}, version {},  has an empty derivedFromGenericType field. ", component.getName(), component.getInvariantUUID(), component.getVersion());

            LOGGER.info("Starting to perform check out of vf with name {}, invariantUUID {}, version {}. ", component.getName(), component.getInvariantUUID(), component.getVersion());
            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkouRes = lifecycleBusinessLogic.changeComponentState(component.getComponentType(), component.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, changeInfo, true, false);
            if(checkouRes.isRight()){
                LOGGER.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.CHECKOUT);
                outputHandler.addRecord(component.getComponentType().name(), component.getName(), component.getInvariantUUID(), component.getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), checkouRes.right().value().getFormattedMessage());
                return false;
            }
            //update included VFCs, if it is required as per configuration
            if(isVfcUpgradeRequired && CollectionUtils.isNotEmpty(checkouRes.left().value().getComponentInstances())){
                LOGGER.info("VFC upgrade is required: updating components of vf with name {}, invariantUUID {}, version {}. ", component.getName(), component.getInvariantUUID(), component.getVersion());
                Either<org.openecomp.sdc.be.model.Component, ResponseFormat>  updateCompositionRes =
                                                updateComposition(checkouRes.left().value());
                if(updateCompositionRes.isRight()){
                    LOGGER.error(FAILED_TO_UPGRADE_COMPONENT, checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getInvariantUUID(), checkouRes.left().value().getVersion(), "updateComposition", updateCompositionRes.right().value().getFormattedMessage());
                    outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getUUID(), checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), updateCompositionRes.right().value().getFormattedMessage());
                    return false;
                }
            }
            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> certifyRes = performFullCertification(checkouRes.left().value());
            if(certifyRes.isRight()){
                LOGGER.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.CERTIFY);
                outputHandler.addRecord(checkouRes.left().value().getComponentType().name(), checkouRes.left().value().getName(), checkouRes.left().value().getInvariantUUID(), checkouRes.left().value().getUniqueId(), MigrationResult.MigrationStatus.FAILED.name(), certifyRes.right().value().getFormattedMessage());
                return false;
            }
            updatedComponent = certifyRes.left().value();
        } else {
            LOGGER.info("The version {} of derived from generic type {} is up to date. No need to upgrade component with name {}, invariantUUID {} and version {}. ", latestGenericTypes.get(derivedFromGenericType), derivedFromGenericType,component.getName(), component.getInvariantUUID(), component.getVersion());
        }
        LOGGER.info(UPGRADE_COMPONENT_SUCCEEDED, component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion());
        outputHandler.addRecord(updatedComponent.getComponentType().name(), updatedComponent.getName(), updatedComponent.getUUID(), updatedComponent.getUniqueId(), MigrationResult.MigrationStatus.COMPLETED.name(), updatedComponent.equals(component) ? UpgradeStatus.NOT_UPGRADED : UpgradeStatus.UPGRADED);
        return true;
    }

    private StorageOperationStatus upgradeNodeType(GraphVertex nodeTypeV, Map<String, org.openecomp.sdc.be.model.Component> upgradedNodeTypesMap, List<String> allCertifiedUids, List<String> nodeTypes) {
        StorageOperationStatus result = StorageOperationStatus.OK;
        LOGGER.info("Starting upgrade node type with name {}, invariantUUID {}, version{}. ", nodeTypeV.getMetadataProperty(GraphPropertyEnum.NAME), nodeTypeV.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID), nodeTypeV.getMetadataProperty(GraphPropertyEnum.VERSION));
        LOGGER.info("Starting to find derived to for node type with name {}, invariantUUID {}, version{}. ", nodeTypeV.getMetadataProperty(GraphPropertyEnum.NAME), nodeTypeV.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID), nodeTypeV.getMetadataProperty(GraphPropertyEnum.VERSION));
        Either<List<GraphVertex>, TitanOperationStatus> parentResourceRes = titanDao.getParentVertecies(nodeTypeV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.ParseMetadata);
        if(parentResourceRes.isRight() && parentResourceRes.right().value() != TitanOperationStatus.NOT_FOUND ){
            return DaoStatusConverter.convertTitanStatusToStorageStatus(parentResourceRes.right().value());

        }
        List<GraphVertex> derivedResourcesUid = new ArrayList<>();
        if(parentResourceRes.isLeft()){
            for(GraphVertex chV: parentResourceRes.left().value()){
                Optional<String> op = allCertifiedUids.stream().filter(id -> id.equals((String)chV.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID))).findAny();
                if(op.isPresent()){
                    derivedResourcesUid.add(chV);
                }
            }
        }
        String uniqueId = (String)nodeTypeV.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID);
        Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getRes = toscaOperationFacade.getToscaElement(uniqueId);
        if(getRes.isRight()){
            LOGGER.info("failed to fetch element with uniqueId {} ", uniqueId);
            return getRes.right().value();
        }

        org.openecomp.sdc.be.model.Resource nt = (Resource)getRes.left().value();
        boolean isNeedToUpgrade = true;
        if(upgradedNodeTypesMap.containsKey(nt.getToscaResourceName()) || nodeTypes.stream().filter( p -> p.equals(nt.getToscaResourceName())).findAny().isPresent()){
            isNeedToUpgrade = false;
        }
        if(isNeedToUpgrade){
            LOGGER.info("Starting to perform check out of node type with name {}, invariantUUID {}, version {}. ", nt.getName(), nt.getInvariantUUID(), nt.getVersion());
            Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> checkouRes = lifecycleBusinessLogic.changeComponentState(nt.getComponentType(), nt.getUniqueId(), user, LifeCycleTransitionEnum.CHECKOUT, changeInfo, true, false);
            if(checkouRes.isRight()){
                return StorageOperationStatus.GENERAL_ERROR;
            }
            org.openecomp.sdc.be.model.Component upgradetComp = checkouRes.left().value();
            boolean res = performFullCertification(upgradetComp).isLeft();
            if(!res){
                return StorageOperationStatus.GENERAL_ERROR;
            }
            upgradedNodeTypesMap.put(nt.getToscaResourceName(), upgradetComp);
            titanDao.commit();
        }
        for(GraphVertex chV: derivedResourcesUid){
            result = upgradeNodeType(chV, upgradedNodeTypesMap, allCertifiedUids, nodeTypes);
            LOGGER.info("Upgrade node type with name {}, invariantUUID {}, version {} has been finished with the status {}", chV.getMetadataProperty(GraphPropertyEnum.NAME), chV.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID), chV.getMetadataProperty(GraphPropertyEnum.VERSION), result);
        }
        return result;
    }

    private Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat> performFullCertification(org.openecomp.sdc.be.model.Component component) {
        LOGGER.info("Starting to perform full certification of {} with name {}, invariantUUID {}, version {}. ",
                                            component.getComponentType().getValue(), component.getName(), component.getInvariantUUID(), component.getVersion());

        Either<? extends org.openecomp.sdc.be.model.Component, ResponseFormat>  changeStateEither = lifecycleBusinessLogic.changeComponentState(component.getComponentType(), component.getUniqueId(), user, LifeCycleTransitionEnum.CERTIFICATION_REQUEST, changeInfo, true, false);
        if(changeStateEither.isRight()){
            LOGGER.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.CERTIFICATION_REQUEST);
            return changeStateEither;
        }
        changeStateEither = lifecycleBusinessLogic.changeComponentState(component.getComponentType(), changeStateEither.left().value().getUniqueId(), user, LifeCycleTransitionEnum.START_CERTIFICATION, changeInfo, true, false);
        if(changeStateEither.isRight()){
            LOGGER.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.START_CERTIFICATION);
            return changeStateEither;
        }
        changeStateEither = lifecycleBusinessLogic.changeComponentState(component.getComponentType(), changeStateEither.left().value().getUniqueId(), user, LifeCycleTransitionEnum.CERTIFY, changeInfo, true, false);
        if(changeStateEither.isRight()){
            LOGGER.info(FAILED_TO_CHANGE_STATE_OF_COMPONENT, component.getName(), component.getInvariantUUID(), component.getVersion(), LifeCycleTransitionEnum.CERTIFY);
        }
        else {
            LOGGER.info("Full certification of {} with name {}, invariantUUID {}, version {} finished successfully",
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
        return isGreater((String)latestDerivedFrom.getJsonMetadataField(JsonPresentationFields.VERSION), currentVersion);
    }

    private boolean isGreater(String latestVersion, String currentVersion) {
        if(latestVersion != null && currentVersion == null)
            return true;
        if(latestVersion == null)
            return false;
        return Double.parseDouble(latestVersion) > Double.parseDouble(currentVersion);
    }

    private Either<List<String>, TitanOperationStatus> getAllLatestCertifiedComponentUids(VertexTypeEnum vertexType, ComponentTypeEnum componentType) {
        LOGGER.info("Starting to fetch all latest certified not checked out components with type {} upon upgrade migration 1710 process", componentType);
        Either<List<String>, TitanOperationStatus> result = null;
        Map<String, String> latestCertifiedMap = new HashMap<>();
        Map<String, String> latestNotCertifiedMap = new HashMap<>();
        
        Either<List<GraphVertex>, TitanOperationStatus> getComponentsRes = getAllLatestCertifiedComponents(vertexType, componentType);
        if(getComponentsRes.isRight() && getComponentsRes.right().value() != TitanOperationStatus.NOT_FOUND){
            LOGGER.error("Failed to fetch all latest certified not checked out components with type {}. Status is {}. ", componentType, getComponentsRes.right().value());
            result = Either.right(getComponentsRes.right().value());
        }
        if(getComponentsRes.isRight()){
            result = Either.left(new ArrayList<>());
        }
        if(result == null){
            for(GraphVertex component : getComponentsRes.left().value()){
                String invariantUUID = (String)component.getJsonMetadataField(JsonPresentationFields.INVARIANT_UUID);
                if(((String)component.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals(LifecycleStateEnum.CERTIFIED.name())){
                    latestCertifiedMap.put(invariantUUID, (String)component.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID));
                } else {
                    latestNotCertifiedMap.put(invariantUUID, (String)component.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID));
                }
            }
            result = Either.left(latestCertifiedMap.entrySet().stream().filter(e->!latestNotCertifiedMap.containsKey(e.getKey())).map(e->e.getValue()).collect(Collectors.toList()));
        }
        return result;
    }

    private Either<List<GraphVertex>, TitanOperationStatus> getAllLatestCertifiedComponents(VertexTypeEnum vertexType, ComponentTypeEnum componentType){

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);
        if(vertexType == VertexTypeEnum.TOPOLOGY_TEMPLATE && componentType == ComponentTypeEnum.RESOURCE)
            propertiesNotToMatch.put(GraphPropertyEnum.RESOURCE_TYPE, ResourceTypeEnum.CVFC.name());
        return titanDao.getByCriteria(vertexType, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseMetadata);
    }

    protected Either<List<String>, TitanOperationStatus> findResourcesPathRecursively(GraphVertex nodeTypeV, List<String> allCertifiedUids) {
        Either<List<GraphVertex>, TitanOperationStatus> parentResourceRes = titanDao.getParentVertecies(nodeTypeV, EdgeLabelEnum.DERIVED_FROM, JsonParseFlagEnum.ParseMetadata);
        if(parentResourceRes.isRight()){
            return Either.right(parentResourceRes.right().value());
        }
        List<GraphVertex> derivedResourcesUid = new ArrayList<>();
        for(GraphVertex chV: parentResourceRes.left().value()){
            Optional<String> op = allCertifiedUids.stream().filter(id -> id.equals((String)chV.getJsonMetadataField(JsonPresentationFields.UNIQUE_ID))).findAny();
            if(op.isPresent()){
                derivedResourcesUid.add(chV);
            }
        }
        return null;
    }

    private  Either<List<GraphVertex>,  StorageOperationStatus> getLatestByName(GraphPropertyEnum property, String nodeName){

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);

        propertiesToMatch.put(property, nodeName);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        Either<List<GraphVertex>, TitanOperationStatus> highestResources = titanDao.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseMetadata);
        if (highestResources.isRight()) {
            TitanOperationStatus status = highestResources.right().value();
            LOGGER.debug("Failed to fetch resource with name {}. Status is {} ", nodeName, status);
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
        }
        List<GraphVertex> resources = highestResources.left().value();
        List<GraphVertex> result = new ArrayList<>();
        for(GraphVertex component:resources){
            if(((String)component.getJsonMetadataField(JsonPresentationFields.LIFECYCLE_STATE)).equals(LifecycleStateEnum.CERTIFIED.name())){
                result.add(component);
            }
        }
        return Either.left(result);
    }

}
