package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.DuplicateComputeInComponentErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NotSupportedHeatOnboardMethodErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.compute.Compute;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComputeCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VendorSoftwareProductUtils;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComputeManagerImpl implements ComputeManager {

  private static final MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private ComputeDao computeDao;
  private CompositionEntityDataManager compositionEntityDataManager;
  private VendorSoftwareProductInfoDao vspInfoDao;
  private DeploymentFlavorDao deploymentFlavorDao;
  private ComponentDao componentDao;
  private static final String MANUAL = "Manual";

  public ComputeManagerImpl(
      VendorSoftwareProductInfoDao vspInfoDao,
      ComputeDao computeDao,
      CompositionEntityDataManager compositionEntityDataManager,
      DeploymentFlavorDao deploymentFlavorDao,
      ComponentDao componentDao
     ) {
    this.computeDao = computeDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.vspInfoDao = vspInfoDao;
    this.deploymentFlavorDao = deploymentFlavorDao;
    this.componentDao = componentDao;
  }

  @Override
  public ComputeEntity createCompute(ComputeEntity compute, String user) {
    ComputeEntity createdCompute = null;
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", compute.getVspId(),
        compute.getComponentId());

    /*Version activeVersion =
        getVersionInfo(compute.getVspId(), VersionableEntityAction.Write, user).getActiveVersion();
    compute.setVersion(activeVersion);*/
    //if (!isManual(compute.getVspId(), activeVersion)) {
    if (!vspInfoDao.isManual(compute.getVspId(), compute.getVersion())) {
      ErrorCode onboardingMethodUpdateErrorCode = NotSupportedHeatOnboardMethodErrorBuilder
          .getAddComputeNotSupportedHeatOnboardMethodErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_COMPUTE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
          onboardingMethodUpdateErrorCode.message());
      throw new CoreException(onboardingMethodUpdateErrorCode);
    } else {
      //validateComponentId(compute.getVspId(),compute.getVersion(),compute.getComponentId());
      validateCompute(compute);
      createdCompute = createCompute(compute);
    }

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id", compute.getVspId(), compute.getComponentId());

    return createdCompute;
  }

  private ComputeEntity createCompute(ComputeEntity compute) {

    return compositionEntityDataManager.createCompute(compute);
  }

  private void validateCompute(ComputeEntity compute) {
    Collection<ComputeEntity> vfcComputeList = listCompute(compute.getVspId(),compute.getVersion
        (),compute.getComponentId());

    if (!isComputeNameUnique(vfcComputeList,compute.getComputeCompositionData().getName())) {
      final ErrorCode duplicateComputeInComponentErrorBuilder =
          new DuplicateComputeInComponentErrorBuilder(compute.getComputeCompositionData().getName(),
              compute.getComponentId()).build();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_COMPUTE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(),
          duplicateComputeInComponentErrorBuilder.message());
      throw new CoreException(duplicateComputeInComponentErrorBuilder);
    }

  }

  private void validateComputeUpdate(ComputeEntity compute) {
    Collection<ComputeEntity> vfcComputeList = listCompute(compute.getVspId(),compute.getVersion
        (),compute.getComponentId());

    for (ComputeEntity ce : vfcComputeList) {
      if (ce.getId().equals(compute.getId())) {
        vfcComputeList.remove(ce);
        break;
      }
    }

    if (!isComputeNameUnique(vfcComputeList,compute.getComputeCompositionData().getName())) {
      final ErrorCode duplicateComputeInComponentErrorBuilder =
          new DuplicateComputeInComponentErrorBuilder(compute.getComputeCompositionData().getName(),
              compute.getComponentId()).build();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPDATE_COMPUTE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(),
          duplicateComputeInComponentErrorBuilder.message());
      throw new CoreException(duplicateComputeInComponentErrorBuilder);
    }

  }

  @Override
  public Collection<ListComputeResponse> listCompute(String vspId, Version version,
                                                     String componentId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);
    //validateComponentId(vspId, version, componentId);
    ComputeEntity entity = new ComputeEntity(vspId, version, componentId, null);
    Collection<ComputeEntity> computes = computeDao.list(entity);

    Collection<ListComputeResponse> computeResponse =
        getListComputeResponse(vspId, version, user, computes);
    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return computeResponse;
  }

  private Collection<ListComputeResponse> getListComputeResponse(String vspId, Version version,
                                                                 String user,
                                                                 Collection<ComputeEntity> computes) {
    Set<String> vspComputes = getComputeAssociatedWithDepFlavors(vspId, version, user);
    Collection<ListComputeResponse> computeResponse = new ArrayList<ListComputeResponse>();
    for(ComputeEntity computeEntity : computes) {
      ListComputeResponse response = new ListComputeResponse();
      response.setComputeEntity(computeEntity);
      if(vspComputes.contains(computeEntity.getId())) {
        response.setAssociatedWithDeploymentFlavor(true);
      } else {
        response.setAssociatedWithDeploymentFlavor(false);
      }
      computeResponse.add(response);
    }
    return computeResponse;
  }

  private Set<String> getComputeAssociatedWithDepFlavors(String vspId, Version version,
                                                         String user) {
    final Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    Set<String> vspComputes = new HashSet<String>();
    for(DeploymentFlavorEntity entity : deploymentFlavorEntities) {
      final List<ComponentComputeAssociation> componentComputeAssociations =
          entity.getDeploymentFlavorCompositionData().getComponentComputeAssociations();
      if(componentComputeAssociations != null  && !componentComputeAssociations.isEmpty()) {
        for(ComponentComputeAssociation association : componentComputeAssociations) {
          vspComputes.add(association.getComputeFlavorId());
        }
      }
    }
    return vspComputes;
  }

  private boolean isComputeNameUnique(Collection<ComputeEntity> vfcComputeList, String name) {
    for (ComputeEntity compute : vfcComputeList) {
      if (compute.getComputeCompositionData().getName().equalsIgnoreCase(name)) {
        return false;
      }
    }
    return true;
  }

  private Collection<ComputeEntity> listCompute(String vspId, Version version,String componentId) {
    Collection<ComputeEntity> computeEntities =
        computeDao.list(new ComputeEntity(vspId, version, componentId, null));

    return computeEntities;
  }

  @Override
  public CompositionEntityResponse<ComputeData> getCompute(String vspId, Version version,
                                                           String componentId,
                                                           String computeFlavorId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id, compute id", vspId,
        componentId, computeFlavorId);

    /*version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));*/
    ComputeEntity computeEntity = getCompute(vspId, version, componentId, computeFlavorId);
    ComputeData compute = computeEntity.getComputeCompositionData();

    ComputeCompositionSchemaInput schemaInput = new ComputeCompositionSchemaInput();
    schemaInput.setManual(vspInfoDao.isManual(vspId, version));
    schemaInput.setCompute(compute);

    CompositionEntityResponse<ComputeData> response = new CompositionEntityResponse<>();
    response.setId(computeFlavorId);
    response.setData(compute);
    response.setSchema(getComputeCompositionSchema(schemaInput));

    mdcDataDebugMessage.debugExitMessage("VSP id, component id, compute id", vspId,
        componentId, computeFlavorId);

    return response;
  }

  private ComputeEntity getCompute(String vspId, Version version, String componentId, String
      computeFlavorId) {
    //validateComponentId(vspId,version,componentId);
    ComputeEntity retrieved = computeDao.get(new ComputeEntity(vspId, version, componentId,
        computeFlavorId));
    VersioningUtil
        .validateEntityExistence(retrieved, new ComputeEntity(vspId, version, componentId,
            computeFlavorId), VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  /*private void validateComponentId(String vspId, Version version, String componentId) {
    ComponentEntity retrivedComponent = componentDao.get(new ComponentEntity(vspId, version,
        componentId));
    VersioningUtil
        .validateEntityExistence(retrivedComponent, new ComponentEntity(vspId, version,
            componentId),VspDetails.ENTITY_TYPE);
  }*/

  @Override
  public QuestionnaireResponse getComputeQuestionnaire(String vspId, Version version, String
          componentId, String computeId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, componentId", vspId, componentId, computeId);

    /*version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));*/
    //validateComponentId(vspId,version,componentId);
    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    //validateComponentId(vspId,version,componentId);
    ComputeEntity computeQuestionnaire = computeDao.getQuestionnaireData(vspId, version, componentId, computeId);
    VersioningUtil
            .validateEntityExistence(computeQuestionnaire, new ComputeEntity(vspId, version, componentId,
                    computeId), VspDetails.ENTITY_TYPE);
    questionnaireResponse.setData(computeQuestionnaire.getQuestionnaireData());
    questionnaireResponse.setSchema(getComputeQuestionnaireSchema(null));

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId,
            componentId, computeId);

    return questionnaireResponse;
  }


  protected String getComputeQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.compute,
            schemaInput);
  }


  @Override
  public void updateComputeQuestionnaire(String vspId, Version version, String componentId, String
      computeId,
                                         String questionnaireData, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id, compute id", vspId,
        componentId, computeId);

    /*Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    getComponent(vspId, activeVersion, componentId);*/
    ComputeEntity retrieved = computeDao.get(new ComputeEntity(vspId,version,componentId,
        computeId));
    VersioningUtil.validateEntityExistence(retrieved, new ComputeEntity(vspId, version,
        componentId, computeId), VspDetails.ENTITY_TYPE);

    computeDao.updateQuestionnaireData(vspId, version, componentId, computeId, questionnaireData);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id, compute id", vspId,
        componentId, computeId);
  }

  @Override
  public CompositionEntityValidationData updateCompute(ComputeEntity compute, String user) {
    mdcDataDebugMessage
            .debugEntryMessage("VSP id, component id", compute.getVspId(), compute.getComponentId(),
                    compute.getId());

    /*Version activeVersion =
        getVersionInfo(image.getVspId(), VersionableEntityAction.Write, user).getActiveVersion();
    image.setVersion(activeVersion);*/

    ComputeEntity retrieved = getComputeEntity(compute.getVspId(), compute.getVersion(), compute.getComponentId(),
            compute.getId());

    if(!vspInfoDao.isManual(compute.getVspId(), compute.getVersion())) {
      final ComputeData computeCompositionData = compute.getComputeCompositionData();
      final String name = computeCompositionData.getName();
      //final String format = computeCompositionData.getFormat();
      validateHeatVspComputeUpdate("Name", name, retrieved.getComputeCompositionData()
              .getName());
      /*validateHeatVspComputeUpdate("format", format, retrieved.getComputeCompositionData()
          .getFormat());*/
    }

    Collection<ComputeEntity> vfcComputeList = listComputes(compute.getVspId() ,
            compute.getVersion(), compute.getComponentId());

    //Set to null so that retrieved object is equal to one in list and gets removed.
    retrieved.setQuestionnaireData(null);
    vfcComputeList.remove(retrieved);
    if(vspInfoDao.isManual(compute.getVspId(), compute.getVersion()))
      validateVfcCompute(compute, vfcComputeList);

    //Set format to default value in order to handle FTL validation when compute format is null
    /*if(compute.getComputeCompositionData().getFormat() == null)
      compute.getComputeCompositionData().setFormat(ComputeFormat.qcow2.name());*/

    ComputeCompositionSchemaInput schemaInput = new ComputeCompositionSchemaInput();
    schemaInput.setCompute(compute.getComputeCompositionData());

    CompositionEntityValidationData validationData = compositionEntityDataManager
            .validateEntity(compute, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      computeDao.update(compute);
    }

    mdcDataDebugMessage
            .debugExitMessage("VSP id, component id", compute.getVspId(), compute.getComponentId(),
                    compute.getId());

    return validationData;
  }

  private void validateHeatVspComputeUpdate(String name, String value, String retrivedValue) {

    if(value != null && !value.equals(retrivedValue)) {

      final ErrorCode updateHeatComputeErrorBuilder =
              DuplicateComputeInComponentErrorBuilder.getComputeHeatReadOnlyErrorBuilder(name);

      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.UPDATE_COMPUTE, ErrorLevel.ERROR.name(),
              LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
              updateHeatComputeErrorBuilder.message());
      throw new CoreException(updateHeatComputeErrorBuilder);
    }
  }

  private void validateVfcCompute(ComputeEntity compute, Collection<ComputeEntity> vfcComputeList) {
    if (isComputeNameDuplicate(vfcComputeList,compute.getComputeCompositionData().getName(), compute.getId())) {
      ErrorCode errorCode = DuplicateComputeInComponentErrorBuilder.getDuplicateComputeNameErrorBuilder(compute
              .getComputeCompositionData().getName(), compute.getComponentId());

      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.CREATE_COMPONENT, ErrorLevel.ERROR.name(),
              errorCode.id(),errorCode.message());

      throw new CoreException(errorCode);
    }
  }

  private boolean isComputeNameDuplicate(Collection<ComputeEntity> computes, String name, String computeId) {
    for (ComputeEntity compute : computes) {
      if (compute.getComputeCompositionData().getName().equals(name) && !compute.getId().equals(computeId)) {
        return true;
      }
    }
    return false;
  }


  private ComputeEntity getComputeEntity(String vspId, Version version, String componentId,
  String computeId) {
      //validateComponentId(vspId,version,componentId);
      ComputeEntity computeEntity = computeDao.get(new ComputeEntity(vspId, version, componentId, computeId));
      VersioningUtil.validateEntityExistence(computeEntity, new ComputeEntity(vspId, version, componentId,
      computeId), VspDetails.ENTITY_TYPE);
      return computeEntity;
      }

  private Collection<ComputeEntity> listComputes(String vspId, Version version, String componentId) {
    return computeDao.list(new ComputeEntity(vspId, version, componentId, null));
  }

  @Override
  public void deleteCompute(String vspId, Version version, String componentId, String
      computeFlavorId, String user) {
    final String VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG =
        "Composition entities may not be created / deleted for Vendor Software Product "
            + "whose entities were uploaded";
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id, compute id", vspId,
        componentId, computeFlavorId);

    /*Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();*/
    if (!vspInfoDao.isManual(vspId, version)) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_COMPUTE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't delete compute");
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());
    }
    ComputeEntity retrived = getCompute(vspId,version,componentId,computeFlavorId);
    if (retrived != null){
      deleteComputeFromDeploymentFlavors(vspId,version,computeFlavorId);
      computeDao.delete(new ComputeEntity(vspId, version, componentId, computeFlavorId));
    }

    mdcDataDebugMessage.debugExitMessage("VSP id, component id, compute id", vspId,
        componentId, computeFlavorId);
  }

  private void deleteComputeFromDeploymentFlavors(String vspId, Version activeVersion,
                                                  String computeFlavorId) {
    //Collection<DeploymentFlavorEntity> listDF = listDeploymentFlavors(vspId, activeVersion);
    Collection<DeploymentFlavorEntity> listDF = deploymentFlavorDao.list(new DeploymentFlavorEntity
        (vspId, activeVersion, null));
    for(DeploymentFlavorEntity df : listDF) {
      DeploymentFlavorEntity deploymentFlavorEntity=removeComputeFromDF(df, computeFlavorId);
      if(deploymentFlavorEntity!=null)
        deploymentFlavorDao.update(deploymentFlavorEntity);
    }
  }

  private DeploymentFlavorEntity removeComputeFromDF(DeploymentFlavorEntity df, String
      computeFlavorId) {
    DeploymentFlavor flavor = df.getDeploymentFlavorCompositionData();
    List<ComponentComputeAssociation> associations = flavor.getComponentComputeAssociations();
    if (associations != null) {
      List<ComponentComputeAssociation> updatedAssociations = new ArrayList<>();
      for (ComponentComputeAssociation ca : associations) {
        if (ca.getComputeFlavorId() != null && ca.getComputeFlavorId().equals(computeFlavorId)) {
          ComponentComputeAssociation updateCaremoveCompute = new ComponentComputeAssociation();
          updateCaremoveCompute.setComponentId(ca.getComponentId());
          updatedAssociations.add(updateCaremoveCompute);
        } else {
          updatedAssociations.add(ca);
        }
      }
      flavor.setComponentComputeAssociations(updatedAssociations);
      df.setDeploymentFlavorCompositionData(flavor);
      return df;
    }
    return null;
  }

  protected String getComputeCompositionSchema(SchemaTemplateInput schemaInput){
    mdcDataDebugMessage.debugEntryMessage(null, null);
    mdcDataDebugMessage.debugExitMessage(null, null);
    return SchemaGenerator.generate(SchemaTemplateContext.composition, CompositionEntityType.compute, schemaInput);
  }

   /*boolean isManual(String vspId, Version version) {

    VspDetails vsp = vspInfoDao.get(new VspDetails(vspId, version));
    String onboardingMethod = vsp.getOnboardingMethod();
    if (MANUAL.equals(onboardingMethod)) {
      return true;
    }
    return false;
  }*/

}
