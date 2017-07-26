package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.DeploymentFlavorErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NotSupportedHeatOnboardMethodErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.DeploymentFlavorCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeploymentFlavorManagerImpl implements DeploymentFlavorManager {

  private static final MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private VendorSoftwareProductInfoDao vspInfoDao;
  private DeploymentFlavorDao deploymentFlavorDao;
  private CompositionEntityDataManager compositionEntityDataManager;
  private  ComponentDao componentDao;
  private ComputeDao computeDao;

  public DeploymentFlavorManagerImpl(
      VendorSoftwareProductInfoDao vspInfoDao,
      DeploymentFlavorDao deploymentFlavorDao,
      CompositionEntityDataManager compositionEntityDataManager,
      ComponentDao componentDao,
      ComputeDao computeDao

  ) {

    this.vspInfoDao = vspInfoDao;
    this.deploymentFlavorDao = deploymentFlavorDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.componentDao = componentDao;
    this.computeDao = computeDao;

  }

  @Override
  public Collection<DeploymentFlavorEntity> listDeploymentFlavors(String vspId, Version version,
                                                                  String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);
    /*version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));*/

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
    return listDeploymentFlavors(vspId, version);
  }

  private Collection<DeploymentFlavorEntity> listDeploymentFlavors(String vspId, Version version) {
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    return deploymentFlavorEntities;
  }

  @Override
  public DeploymentFlavorEntity createDeploymentFlavor(
      DeploymentFlavorEntity deploymentFlavorEntity, String user) {
    DeploymentFlavorEntity createDeploymentFlavor = null;
    mdcDataDebugMessage.debugEntryMessage("VSP id ", deploymentFlavorEntity.getVspId());
    /*Version activeVersion =
        getVersionInfo(deploymentFlavorEntity.getVspId(), VersionableEntityAction.Write, user)
            .getActiveVersion();
    deploymentFlavorEntity.setVersion(activeVersion);*/

    if (!vspInfoDao.isManual(deploymentFlavorEntity.getVspId(),
        deploymentFlavorEntity.getVersion())) {
      ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder
          .getAddDeploymentNotSupportedHeatOnboardErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), deploymentFlavorErrorBuilder.message());
      throw new CoreException(deploymentFlavorErrorBuilder);
    } else {
      validateDeploymentFlavor(deploymentFlavorEntity, user, deploymentFlavorEntity.getVersion());
      createDeploymentFlavor =
          compositionEntityDataManager.createDeploymentFlavor(deploymentFlavorEntity);
    }
    return createDeploymentFlavor;
  }

  private void validateDeploymentFlavor(DeploymentFlavorEntity deploymentFlavorEntity, String
      user, Version activeVersion) {

    if(!deploymentFlavorEntity.getDeploymentFlavorCompositionData().getModel().matches(VendorSoftwareProductConstants.NAME_PATTERN))
    {
      ErrorCode errorCode = DeploymentFlavorErrorBuilder.getDeploymentFlavorNameFormatErrorBuilder(
              VendorSoftwareProductConstants.NAME_PATTERN);

      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.UPDATE_NIC, ErrorLevel.ERROR.name(),
              errorCode.id(),errorCode.message());

      throw new CoreException(errorCode);
    }
    //Validation for unique model.
    Collection<DeploymentFlavorEntity> listDeploymentFlavors =
        listDeploymentFlavors(deploymentFlavorEntity.getVspId(),
            activeVersion);
    isDeploymentFlavorModelDuplicate(deploymentFlavorEntity, listDeploymentFlavors);

    List<String> featureGroups =
        getFeatureGroupListForVsp(deploymentFlavorEntity.getVspId(), user, activeVersion);
    String featureGroup = deploymentFlavorEntity.getDeploymentFlavorCompositionData()
        .getFeatureGroupId();
    if (featureGroup != null && featureGroup.trim().length()>0) {
      if (isEmpty(featureGroups) || (!(validFeatureGroup(featureGroups, featureGroup)))) {
        ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder
            .getFeatureGroupNotexistErrorBuilder(featureGroup, deploymentFlavorEntity.getVspId(),
                activeVersion);
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.CREATE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), deploymentFlavorErrorBuilder.message());
        throw new CoreException(deploymentFlavorErrorBuilder);
      }
    }

    validateComponentComputeAssociation(deploymentFlavorEntity, activeVersion);
  }

  private void isDeploymentFlavorModelDuplicate(DeploymentFlavorEntity deploymentFlavorEntity,
                                                Collection<DeploymentFlavorEntity> listDeploymentFlavors) {
    listDeploymentFlavors.forEach(deploymentFlavor -> {
      if (deploymentFlavorEntity.getDeploymentFlavorCompositionData().getModel().equalsIgnoreCase(
          deploymentFlavor.getDeploymentFlavorCompositionData().getModel())) {
        ErrorCode deploymentFlavorModelErrorBuilder = DeploymentFlavorErrorBuilder
            .getDuplicateDeploymentFlavorModelErrorBuilder(
                deploymentFlavorEntity.getDeploymentFlavorCompositionData().getModel(),
                deploymentFlavorEntity.getVspId());
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.CREATE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), deploymentFlavorModelErrorBuilder.message());
        throw new CoreException(deploymentFlavorModelErrorBuilder);
      }
    });
  }

  private List<String> getFeatureGroupListForVsp(String vspId,
                                                 String user, Version activeVersion) {
    /*VersionedVendorSoftwareProductInfo versionedVendorSoftwareProductInfo = getVspDetails(
        vspId,activeVersion, user);
    return versionedVendorSoftwareProductInfo.getVspDetails()
        .getFeatureGroups();*/

    final VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, activeVersion));
    return vspDetails.getFeatureGroups();
  }

  private boolean isEmpty(Collection coll) {
    return (coll == null || coll.isEmpty());
  }

  private boolean validFeatureGroup(List<String> featureGroups, String featureGroupId) {
    Iterator<String> iterator = featureGroups.iterator();
    boolean valid = false;
    while (iterator.hasNext()) {
      String fgId = iterator.next().trim();
      if (fgId.equals(featureGroupId)) {
        valid = true;
        break;
      } else {
        valid = false;
      }
    }
    return valid;
  }

  private void validateComponentComputeAssociation(DeploymentFlavorEntity deploymentFlavorEntity,
                                                   Version activeVersion) {
    List<ComponentComputeAssociation> componentComputeAssociationList = deploymentFlavorEntity
        .getDeploymentFlavorCompositionData().getComponentComputeAssociations();
    List<String> vfcList = new ArrayList<>();
    if (!isEmpty(componentComputeAssociationList)) {
      componentComputeAssociationList.forEach(componentComputeAssociation -> {
        if ((componentComputeAssociation.getComponentId() == null || componentComputeAssociation
            .getComponentId().trim().length() == 0) &&
            (componentComputeAssociation
                .getComputeFlavorId() != null && componentComputeAssociation
                .getComputeFlavorId().trim().length() > 0)) {
          ErrorCode invalidAssociationErrorBuilder = DeploymentFlavorErrorBuilder
              .getInvalidAssociationErrorBuilder();
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.CREATE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), invalidAssociationErrorBuilder.message());
          throw new CoreException(invalidAssociationErrorBuilder);
        } else if (componentComputeAssociation.getComponentId() != null &&
            componentComputeAssociation.getComponentId().trim().length() > 0 ) {
          ComponentEntity component = getComponent(deploymentFlavorEntity.getVspId(), activeVersion,
              componentComputeAssociation.getComponentId());
          if (componentComputeAssociation
              .getComputeFlavorId() != null && componentComputeAssociation
              .getComputeFlavorId().trim().length() > 0 ) {
            ComputeEntity computeFlavor = computeDao.get(new ComputeEntity(deploymentFlavorEntity
                    .getVspId(), activeVersion, componentComputeAssociation.getComponentId(),
                componentComputeAssociation.getComputeFlavorId()));
            if (computeFlavor == null) {
              ErrorCode invalidComputeIdErrorBuilder = DeploymentFlavorErrorBuilder
                  .getInvalidComputeIdErrorBuilder(componentComputeAssociation.getComputeFlavorId(),
                      componentComputeAssociation.getComponentId());
              MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
                  LoggerTragetServiceName.CREATE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
                  LoggerErrorCode.DATA_ERROR.getErrorCode(),
                  invalidComputeIdErrorBuilder.message());
              throw new CoreException(invalidComputeIdErrorBuilder);
            }
          }
          vfcList.add(componentComputeAssociation.getComponentId());
        }
      });
      Map<String, Integer> frequencyMapping = CollectionUtils.getCardinalityMap(vfcList);

      for (Integer vfcCount : frequencyMapping.values()) {
        if (vfcCount != 1) {
          ErrorCode duplicateVfcAssociationErrorBuilder = DeploymentFlavorErrorBuilder
              .getDuplicateVfcAssociationErrorBuilder();
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.CREATE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(),
              duplicateVfcAssociationErrorBuilder.message());
          throw new CoreException(duplicateVfcAssociationErrorBuilder);
        }
      }
    }
  }

  private ComponentEntity getComponent(String vspId, Version version, String componentId) {
    ComponentEntity retrieved = componentDao.get(new ComponentEntity(vspId, version, componentId));
    VersioningUtil
        .validateEntityExistence(retrieved, new ComponentEntity(vspId, version, componentId),
            VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public CompositionEntityResponse<DeploymentFlavor> getDeploymentFlavor(String vspId,
                                                                         Version version,
                                                                         String deploymentFlavorId,
                                                                         String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, deployment flavor id", vspId, deploymentFlavorId);

    /*version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));*/
    DeploymentFlavorEntity deploymentFlavorEntity = getDeploymentFlavor(vspId,version,
        deploymentFlavorId);
    DeploymentFlavor deploymentFlavor = deploymentFlavorEntity.getDeploymentFlavorCompositionData();
    DeploymentFlavorCompositionSchemaInput schemaInput = new
        DeploymentFlavorCompositionSchemaInput();
    schemaInput.setManual(vspInfoDao.isManual(vspId, version));
    schemaInput.setDeploymentFlavor(deploymentFlavor);
    List<String> featureGroups =
        getFeatureGroupListForVsp(vspId, user, version);
    schemaInput.setFeatureGroupIds(featureGroups);
    CompositionEntityResponse<DeploymentFlavor> response = new CompositionEntityResponse<>();
    response.setId(deploymentFlavorId);
    response.setSchema((SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.deployment,
            schemaInput)));
    response.setData(deploymentFlavor);
    mdcDataDebugMessage
        .debugExitMessage("VSP id, deployment flavor id ", vspId, deploymentFlavorId);

    return response;
  }

  private DeploymentFlavorEntity getDeploymentFlavor(String vspId, Version version, String
      deploymentFlavorId) {
    DeploymentFlavorEntity retrieved = deploymentFlavorDao.get(new DeploymentFlavorEntity(vspId,
        version, deploymentFlavorId));
    VersioningUtil
        .validateEntityExistence(retrieved, new DeploymentFlavorEntity(vspId, version,
            deploymentFlavorId ), VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public CompositionEntityResponse<DeploymentFlavor> getDeploymentFlavorSchema(String vspId,
                                                                               Version version,
                                                                               String user) {
    /*version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));*/
    DeploymentFlavorCompositionSchemaInput schemaInput= new
        DeploymentFlavorCompositionSchemaInput();
    schemaInput.setManual(vspInfoDao.isManual(vspId, version));
    List<String> featureGroups =
        getFeatureGroupListForVsp(vspId, user, version);
    schemaInput.setFeatureGroupIds(featureGroups);
    CompositionEntityResponse<DeploymentFlavor> response = new CompositionEntityResponse<>();
    response.setSchema((SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.deployment,
            schemaInput)));
    return response;
  }

  @Override
  public void deleteDeploymentFlavor(String vspId, Version version, String deploymentFlavorId,
                                     String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, deployment flavor id", vspId, deploymentFlavorId);
    /*Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();*/
    DeploymentFlavorEntity deploymentFlavorEntity = getDeploymentFlavor(vspId,version,
        deploymentFlavorId);
    if (!vspInfoDao.isManual(vspId, version)) {
      final ErrorCode deleteDeploymentFlavorErrorBuilder =
          NotSupportedHeatOnboardMethodErrorBuilder
              .getDelDeploymentFlavorNotSupportedHeatOnboardMethodErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
          deleteDeploymentFlavorErrorBuilder.message());
      throw new CoreException(deleteDeploymentFlavorErrorBuilder);
    }
    if(deploymentFlavorEntity != null) {
      deploymentFlavorDao.delete(new DeploymentFlavorEntity(vspId, version, deploymentFlavorId));

    }
    mdcDataDebugMessage
        .debugExitMessage("VSP id, deployment flavor id", vspId, deploymentFlavorId);
  }

  public CompositionEntityValidationData updateDeploymentFlavor(DeploymentFlavorEntity
                                                                    deploymentFlavorEntity, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, deploymentFlavor id", deploymentFlavorEntity
        .getVspId(), deploymentFlavorEntity.getId());
    /*Version activeVersion =
        getVersionInfo(deploymentFlavorEntity.getVspId(), VersionableEntityAction.Write, user)
            .getActiveVersion();*/

    if (!vspInfoDao.isManual(deploymentFlavorEntity.getVspId(),
        deploymentFlavorEntity.getVersion())) {
      final ErrorCode updateDeploymentFlavorErrorBuilder =
          NotSupportedHeatOnboardMethodErrorBuilder
              .getUpdateDfNotSupportedHeatOnboardMethodErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPDATE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
          updateDeploymentFlavorErrorBuilder.message());
      throw new CoreException(updateDeploymentFlavorErrorBuilder);
    }
    else {
        if(!deploymentFlavorEntity.getDeploymentFlavorCompositionData().getModel().matches(VendorSoftwareProductConstants.NAME_PATTERN))
        {
            ErrorCode errorCode = DeploymentFlavorErrorBuilder.getDeploymentFlavorNameFormatErrorBuilder(
                    VendorSoftwareProductConstants.NAME_PATTERN);
            MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
                    LoggerTragetServiceName.UPDATE_DEPLOYMENT_FLAVOR, ErrorLevel.ERROR.name(),
                    errorCode.id(),errorCode.message());
            throw new CoreException(errorCode);
        }
    }
    //deploymentFlavorEntity.setVersion(activeVersion);
    DeploymentFlavorEntity retrieved =
        getDeploymentFlavor(deploymentFlavorEntity.getVspId(), deploymentFlavorEntity.getVersion(),
            deploymentFlavorEntity.getId());


    Collection<DeploymentFlavorEntity> listDeploymentFlavors = listDeploymentFlavors
        (deploymentFlavorEntity.getVspId(), deploymentFlavorEntity.getVersion());
    listDeploymentFlavors.remove(retrieved);
    isDeploymentFlavorModelDuplicate(deploymentFlavorEntity, listDeploymentFlavors);

    //validateComponentComputeAssociation(deploymentFlavorEntity, activeVersion);
    validateComponentComputeAssociation(deploymentFlavorEntity, deploymentFlavorEntity.getVersion());

    DeploymentFlavorCompositionSchemaInput schemaInput = new
        DeploymentFlavorCompositionSchemaInput();
    schemaInput.setManual(vspInfoDao.isManual(deploymentFlavorEntity.getVspId(),
        deploymentFlavorEntity.getVersion()));
    schemaInput.setDeploymentFlavor(retrieved.getDeploymentFlavorCompositionData());

    List<String> featureGroups =
        getFeatureGroupListForVsp(deploymentFlavorEntity.getVspId(), user,
            deploymentFlavorEntity.getVersion());
    schemaInput.setFeatureGroupIds(featureGroups);

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(deploymentFlavorEntity, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      deploymentFlavorDao.update(deploymentFlavorEntity);
    }

    mdcDataDebugMessage.debugExitMessage("VSP id, deploymentFlavor id",
        deploymentFlavorEntity.getVspId(), deploymentFlavorEntity.getId());
    return validationData;
  }

}
