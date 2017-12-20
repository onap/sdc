package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.json.JsonSchemaDataGenerator;
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
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
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
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComputeCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

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

  public ComputeManagerImpl(VendorSoftwareProductInfoDao vspInfoDao,
                            ComputeDao computeDao,
                            CompositionEntityDataManager compositionEntityDataManager,
                            DeploymentFlavorDao deploymentFlavorDao) {
    this.computeDao = computeDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.vspInfoDao = vspInfoDao;
    this.deploymentFlavorDao = deploymentFlavorDao;
  }

  @Override
  public ComputeEntity createCompute(ComputeEntity compute) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, component id", compute.getVspId(), compute.getComponentId());

    if (!vspInfoDao.isManual(compute.getVspId(), compute.getVersion())) {
      ErrorCode onboardingMethodUpdateErrorCode = NotSupportedHeatOnboardMethodErrorBuilder
          .getAddComputeNotSupportedHeatOnboardMethodErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_COMPUTE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
          onboardingMethodUpdateErrorCode.message());
      throw new CoreException(onboardingMethodUpdateErrorCode);
    } else {
      validateUniqueName(compute.getVspId(), compute.getVersion(), compute.getComponentId(),
          compute.getComputeCompositionData().getName());

      compute.setQuestionnaireData(
          new JsonSchemaDataGenerator(getComputeQuestionnaireSchema(null)).generateData());
      computeDao.create(compute);
      createUniqueName(compute.getVspId(), compute.getVersion(), compute.getComponentId(),
          compute.getComputeCompositionData().getName());
    }

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id", compute.getVspId(), compute.getComponentId());

    return compute;
  }


  @Override
  public Collection<ListComputeResponse> listComputes(String vspId, Version version,
                                                      String componentId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);
    Collection<ComputeEntity> computes =
        computeDao.list(new ComputeEntity(vspId, version, componentId, null));

    Collection<ListComputeResponse> computeResponse =
        getListComputeResponse(vspId, version, computes);
    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return computeResponse;
  }

  private Collection<ListComputeResponse> getListComputeResponse(String vspId, Version version,
                                                                 Collection<ComputeEntity> computes) {
    Set<String> vspComputes = getComputeAssociatedWithDepFlavors(vspId, version);
    Collection<ListComputeResponse> computeResponse = new ArrayList<>();
    for (ComputeEntity computeEntity : computes) {
      ListComputeResponse response = new ListComputeResponse();
      response.setComputeEntity(computeEntity);
      if (vspComputes.contains(computeEntity.getId())) {
        response.setAssociatedWithDeploymentFlavor(true);
      } else {
        response.setAssociatedWithDeploymentFlavor(false);
      }
      computeResponse.add(response);
    }
    return computeResponse;
  }

  private Set<String> getComputeAssociatedWithDepFlavors(String vspId, Version version) {
    final Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    Set<String> vspComputes = new HashSet<>();
    for (DeploymentFlavorEntity entity : deploymentFlavorEntities) {
      final List<ComponentComputeAssociation> componentComputeAssociations =
          entity.getDeploymentFlavorCompositionData().getComponentComputeAssociations();
      if (componentComputeAssociations != null && !componentComputeAssociations.isEmpty()) {
        for (ComponentComputeAssociation association : componentComputeAssociations) {
          vspComputes.add(association.getComputeFlavorId());
        }
      }
    }
    return vspComputes;
  }

  @Override
  public CompositionEntityResponse<ComputeData> getCompute(String vspId, Version version,
                                                           String componentId,
                                                           String computeFlavorId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id, compute id", vspId,
        componentId, computeFlavorId);

    ComputeEntity computeEntity = getValidatedCompute(vspId, version, componentId, computeFlavorId);
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

  private ComputeEntity getValidatedCompute(String vspId, Version version, String componentId,
                                            String computeFlavorId) {
    ComputeEntity retrieved = computeDao.get(new ComputeEntity(vspId, version, componentId,
        computeFlavorId));
    VersioningUtil
        .validateEntityExistence(retrieved, new ComputeEntity(vspId, version, componentId,
            computeFlavorId), VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public QuestionnaireResponse getComputeQuestionnaire(String vspId, Version version, String
      componentId, String computeId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, componentId", vspId, componentId, computeId);

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    ComputeEntity computeQuestionnaire =
        computeDao.getQuestionnaireData(vspId, version, componentId, computeId);
    VersioningUtil
        .validateEntityExistence(computeQuestionnaire,
            new ComputeEntity(vspId, version, componentId, computeId), VspDetails.ENTITY_TYPE);
    questionnaireResponse.setData(computeQuestionnaire.getQuestionnaireData());
    questionnaireResponse.setSchema(getComputeQuestionnaireSchema(null));

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId, computeId);

    return questionnaireResponse;
  }


  @Override
  public void updateComputeQuestionnaire(String vspId, Version version, String componentId,
                                         String computeId, String questionnaireData) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, component id, compute id", vspId, componentId, computeId);

    ComputeEntity retrieved = computeDao.get(new ComputeEntity(vspId, version, componentId,
        computeId));
    VersioningUtil.validateEntityExistence(retrieved, new ComputeEntity(vspId, version,
        componentId, computeId), VspDetails.ENTITY_TYPE);

    computeDao.updateQuestionnaireData(vspId, version, componentId, computeId, questionnaireData);

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id, compute id", vspId, componentId, computeId);
  }

  @Override
  public CompositionEntityValidationData updateCompute(ComputeEntity compute) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, component id", compute.getVspId(), compute.getComponentId(),
            compute.getId());

    ComputeEntity retrieved =
        getComputeEntity(compute.getVspId(), compute.getVersion(), compute.getComponentId(),
            compute.getId());

    boolean manual = vspInfoDao.isManual(compute.getVspId(), compute.getVersion());
    if (!manual) {
      validateHeatVspComputeUpdate("Name",
          compute.getComputeCompositionData().getName(),
          retrieved.getComputeCompositionData().getName());
    }

    ComputeCompositionSchemaInput schemaInput = new ComputeCompositionSchemaInput();
    schemaInput.setCompute(compute.getComputeCompositionData());

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(compute, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      updateUniqueName(compute.getVspId(), compute.getVersion(), compute.getComponentId(),
          retrieved.getComputeCompositionData().getName(),
          compute.getComputeCompositionData().getName());
      computeDao.update(compute);
    }

    mdcDataDebugMessage
        .debugExitMessage("VSP id, component id", compute.getVspId(), compute.getComponentId(),
            compute.getId());

    return validationData;
  }

  private void validateHeatVspComputeUpdate(String name, String value, String retrivedValue) {

    if (value != null && !value.equals(retrivedValue)) {

      final ErrorCode updateHeatComputeErrorBuilder =
          DuplicateComputeInComponentErrorBuilder.getComputeHeatReadOnlyErrorBuilder(name);

      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPDATE_COMPUTE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(),
          updateHeatComputeErrorBuilder.message());
      throw new CoreException(updateHeatComputeErrorBuilder);
    }
  }

  private ComputeEntity getComputeEntity(String vspId, Version version, String componentId,
                                         String computeId) {
    ComputeEntity computeEntity =
        computeDao.get(new ComputeEntity(vspId, version, componentId, computeId));
    VersioningUtil
        .validateEntityExistence(computeEntity, new ComputeEntity(vspId, version, componentId,
            computeId), VspDetails.ENTITY_TYPE);
    return computeEntity;
  }

  @Override
  public void deleteCompute(String vspId, Version version, String componentId,
                            String computeFlavorId) {
    final String VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG =
        "Composition entities may not be created / deleted for Vendor Software Product "
            + "whose entities were uploaded";
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id, compute id", vspId,
        componentId, computeFlavorId);

    if (!vspInfoDao.isManual(vspId, version)) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_COMPUTE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't delete compute");
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());
    }
    ComputeEntity retrieved = getValidatedCompute(vspId, version, componentId, computeFlavorId);
    if (retrieved != null) {
      deleteComputeFromDeploymentFlavors(vspId, version, computeFlavorId);
      computeDao.delete(new ComputeEntity(vspId, version, componentId, computeFlavorId));
      deleteUniqueValue(retrieved.getVspId(), retrieved.getVersion(), retrieved.getComponentId(),
          retrieved.getComputeCompositionData().getName());
    }

    mdcDataDebugMessage.debugExitMessage("VSP id, component id, compute id", vspId,
        componentId, computeFlavorId);
  }

  private void deleteComputeFromDeploymentFlavors(String vspId, Version version,
                                                  String computeFlavorId) {
    Collection<DeploymentFlavorEntity> listDF =
        deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    for (DeploymentFlavorEntity df : listDF) {
      DeploymentFlavorEntity deploymentFlavorEntity = removeComputeFromDF(df, computeFlavorId);
      if (deploymentFlavorEntity != null) {
        deploymentFlavorDao.update(deploymentFlavorEntity);
      }
    }
  }

  private DeploymentFlavorEntity removeComputeFromDF(DeploymentFlavorEntity df,
                                                     String computeFlavorId) {
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

  protected String getComputeCompositionSchema(SchemaTemplateInput schemaInput) {
    mdcDataDebugMessage.debugEntryMessage(null);
    mdcDataDebugMessage.debugExitMessage(null);
    return SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.compute, schemaInput);
  }

  protected String getComputeQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    mdcDataDebugMessage.debugEntryMessage(null);
    mdcDataDebugMessage.debugExitMessage(null);
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.compute, schemaInput);
  }

  protected void validateUniqueName(String vspId, Version version, String componentId,
                                    String name) {
    UniqueValueUtil.validateUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME,
        vspId, version.getId(), componentId, name);
  }

  protected void createUniqueName(String vspId, Version version, String componentId, String name) {
    UniqueValueUtil
        .createUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, vspId,
            version.getId(), componentId, name);
  }

  protected void updateUniqueName(String vspId, Version version, String componentId,
                                  String oldName, String newName) {
    UniqueValueUtil
        .updateUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, oldName,
            newName, vspId, version.getId(), componentId);
  }

  protected void deleteUniqueValue(String vspId, Version version, String componentId, String name) {
    if (componentId == null) {
      UniqueValueUtil
          .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, vspId,
              version.getId(), name);
    }
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, vspId,
            version.getId(), componentId, name);
  }
}
