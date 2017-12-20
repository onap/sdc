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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.enrichment.api.EnrichmentManager;
import org.openecomp.core.enrichment.factory.EnrichmentManagerFactory;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonSchemaDataGenerator;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ValidationErrorBuilder;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.licenseartifacts.VendorLicenseArtifactsService;
import org.openecomp.sdc.vendorsoftwareproduct.ManualVspToscaManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.VendorSoftwareProductNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OnboardingMethod;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.DeploymentFlavorErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.FileCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.InformationArtifactCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NicInternalNetworkErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.PackageInvalidErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.PackageNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.TranslationFileCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductInvalidErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireValidationResult;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentQuestionnaireSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.vendorsoftwareproduct.utils.ComponentDependencyTracker;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class VendorSoftwareProductManagerImpl implements VendorSoftwareProductManager {
  private static MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private static final Logger LOGGER =
      LoggerFactory.getLogger(VendorSoftwareProductManagerImpl.class);

  private OrchestrationTemplateDao orchestrationTemplateDao;
  private VendorSoftwareProductInfoDao vspInfoDao;
  private VendorLicenseFacade vendorLicenseFacade;
  private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao;
  private EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> enrichedServiceModelDao;
  private VendorLicenseArtifactsService licenseArtifactsService;
  private InformationArtifactGenerator informationArtifactGenerator;
  private PackageInfoDao packageInfoDao;
  private DeploymentFlavorDao deploymentFlavorDao;
  private ComponentDao componentDao;
  private ComponentDependencyModelDao componentDependencyModelDao;
  private NicDao nicDao;
  private ComputeDao computeDao;
  private ImageDao imageDao;
  private ManualVspToscaManager manualVspToscaManager;

  public VendorSoftwareProductManagerImpl(
      OrchestrationTemplateDao orchestrationTemplateDataDao,
      VendorSoftwareProductInfoDao vspInfoDao,
      VendorLicenseFacade vendorLicenseFacade,
      ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao,
      EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> enrichedServiceModelDao,
      VendorLicenseArtifactsService licenseArtifactsService,
      InformationArtifactGenerator informationArtifactGenerator,
      PackageInfoDao packageInfoDao,
      DeploymentFlavorDao deploymentFlavorDao,
      ComponentDao componentDao,
      ComponentDependencyModelDao componentDependencyModelDao,
      NicDao nicDao,
      ComputeDao computeDao,
      ImageDao imageDao,
      ManualVspToscaManager manualVspToscaManager) {
    this.orchestrationTemplateDao = orchestrationTemplateDataDao;
    this.vspInfoDao = vspInfoDao;
    this.vendorLicenseFacade = vendorLicenseFacade;
    this.serviceModelDao = serviceModelDao;
    this.enrichedServiceModelDao = enrichedServiceModelDao;
    this.licenseArtifactsService = licenseArtifactsService;
    this.informationArtifactGenerator = informationArtifactGenerator;
    this.packageInfoDao = packageInfoDao;
    this.deploymentFlavorDao = deploymentFlavorDao;
    this.componentDao = componentDao;
    this.componentDependencyModelDao = componentDependencyModelDao;
    this.nicDao = nicDao;
    this.computeDao = computeDao;
    this.imageDao = imageDao;
    this.manualVspToscaManager = manualVspToscaManager;

    registerToVersioning();
  }

  private void registerToVersioning() {
    serviceModelDao.registerVersioning(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
    enrichedServiceModelDao.registerVersioning(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
  }


  @Override
  public ValidationResponse validate(String vspId, Version version) throws IOException {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    VspDetails vspDetails = getValidatedVsp(vspId, version);
    Collection<ComponentDependencyModelEntity> componentDependencies =
        componentDependencyModelDao.list(new ComponentDependencyModelEntity(vspId, version, null));

    ValidationResponse validationResponse = new ValidationResponse();
    validationResponse.setQuestionnaireValidationResult(
        validateQuestionnaire(vspDetails.getId(), vspDetails.getVersion(),
            vspDetails.getOnboardingMethod()));

    List<ErrorCode> vspErrors = new ArrayList<>();
    vspErrors.addAll(validateVspFields(vspDetails));
    if (validateComponentDependencies(componentDependencies)) {
      vspErrors
          .add(ComponentDependencyModelErrorBuilder.getcyclicDependencyComponentErrorBuilder());
    }
    if (Objects.nonNull(vspDetails.getOnboardingMethod()) &&
        OnboardingMethod.Manual.name().equals(vspDetails.getOnboardingMethod())) {
      vspErrors.addAll(validateMandatoryLicenseFields(vspDetails));

      Collection<DeploymentFlavorEntity> deploymentFlavors = deploymentFlavorDao
          .list(new DeploymentFlavorEntity(vspDetails.getId(), vspDetails.getVersion(), null));
      if (CollectionUtils.isEmpty(deploymentFlavors)) {
        vspErrors
            .add(VendorSoftwareProductInvalidErrorBuilder.VspMissingDeploymentFlavorErrorBuilder());
      }
      vspErrors.addAll(validateDeploymentFlavors(deploymentFlavors));

      Set<CompositionEntityValidationData> componentValidationResult =
          componentValidation(vspDetails.getId(), vspDetails.getVersion());
      if (!CollectionUtils.isEmpty(componentValidationResult)) {
        if (validationResponse.getQuestionnaireValidationResult() == null ||
            validationResponse.getQuestionnaireValidationResult().getValidationData() == null) {
          validationResponse.setQuestionnaireValidationResult(
              new QuestionnaireValidationResult(componentValidationResult));
        } else {
          validationResponse.getQuestionnaireValidationResult().getValidationData()
              .addAll(componentValidationResult);
        }
      }
    } else {
      if (vspDetails.getVlmVersion() != null || vspDetails.getLicenseAgreement() != null
          || vspDetails.getFeatureGroups() != null) {
        vspErrors.addAll(validateMandatoryLicenseFields(vspDetails));
      }
      OrchestrationTemplateEntity orchestrationTemplate =
          orchestrationTemplateDao.get(vspId, version);
      ToscaServiceModel serviceModel =
          serviceModelDao.getServiceModel(vspId, vspDetails.getVersion());
      if (!isOrchestrationTemplateExist(orchestrationTemplate) ||
          !isServiceModelExist(serviceModel)) {
        vspErrors.add(VendorSoftwareProductInvalidErrorBuilder
            .VendorSoftwareProductMissingServiceModelErrorBuilder(vspDetails.getId(),
                vspDetails.getVersion()));
      }
      validationResponse.setUploadDataErrors(validateOrchestrationTemplate(orchestrationTemplate),
          LoggerServiceName.Submit_VSP, LoggerTragetServiceName.SUBMIT_VSP);
    }
    validationResponse
        .setVspErrors(vspErrors, LoggerServiceName.Submit_VSP, LoggerTragetServiceName.SUBMIT_VSP);
    validationResponse.setLicensingDataErrors(validateLicensingData(vspDetails));


    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return validationResponse;
  }

  @Override
  public Map<String, List<ErrorMessage>> compile(String vspId, Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    ToscaServiceModel serviceModel =
        OnboardingMethod.Manual.name().equals(getValidatedVsp(vspId, version).getOnboardingMethod())
            //Generate Tosca service model for Manual Onboarding flow
            ? manualVspToscaManager
            .generateToscaModel(manualVspToscaManager.gatherVspInformation(vspId, version))
            : serviceModelDao.getServiceModel(vspId, version);

    Map<String, List<ErrorMessage>> compilationErrors = compile(vspId, version, serviceModel);
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return compilationErrors;
  }

  private boolean validateComponentDependencies(
      Collection<ComponentDependencyModelEntity> componentDependencies) {
    ComponentDependencyTracker dependencyTracker = new ComponentDependencyTracker();

    for (ComponentDependencyModelEntity componentDependency : componentDependencies) {
      dependencyTracker.addDependency(componentDependency.getSourceComponentId(),
          componentDependency.getTargetComponentId());
    }
    return dependencyTracker.isCyclicDependencyPresent();
  }

  private Collection<ErrorCode> validateDeploymentFlavors(
      Collection<DeploymentFlavorEntity> deploymentFlavors) {

    Collection<ErrorCode> errorCodeList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(deploymentFlavors)) {
      deploymentFlavors.forEach(deploymentFlavor -> {
        DeploymentFlavorEntity deployment = deploymentFlavorDao.get(deploymentFlavor);
        DeploymentFlavor deploymentlocalFlavor = deployment.getDeploymentFlavorCompositionData();
        if (deploymentlocalFlavor != null) {
          if (deploymentlocalFlavor.getFeatureGroupId() == null) {
            ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder.
                getFeatureGroupMandatoryErrorBuilder(deploymentlocalFlavor.getModel());
            errorCodeList.add(deploymentFlavorErrorBuilder);
          }
          List<ComponentComputeAssociation> componetComputeAssociations =
              deploymentlocalFlavor.getComponentComputeAssociations();
          if (CollectionUtils.isEmpty(componetComputeAssociations)) {
            CompositionEntityValidationData compositionEntityValidationData = new
                CompositionEntityValidationData(CompositionEntityType.deployment, deploymentFlavor
                .getId());
            compositionEntityValidationData.setEntityName(deployment
                .getDeploymentFlavorCompositionData().getModel());
            ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder
                .getInvalidComponentComputeAssociationErrorBuilder(
                    deploymentlocalFlavor.getModel());

            errorCodeList.add(deploymentFlavorErrorBuilder);
          } else {
            componetComputeAssociations.forEach(componetComputeAssociation -> {
              if (componetComputeAssociation == null
                  || !(componetComputeAssociation.getComponentId() != null
                  && componetComputeAssociation.getComputeFlavorId() != null)) {
                CompositionEntityValidationData compositionEntityValidationData = new
                    CompositionEntityValidationData(CompositionEntityType.deployment,
                    deploymentFlavor.getId());
                compositionEntityValidationData.setEntityName(deployment
                    .getDeploymentFlavorCompositionData().getModel());
                ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder
                    .getInvalidComponentComputeAssociationErrorBuilder(
                        deploymentlocalFlavor.getModel());

                errorCodeList.add(deploymentFlavorErrorBuilder);
              }
            });
          }
        }
      });
    }
    return errorCodeList;
  }

  private Set<CompositionEntityValidationData> componentValidation(String vspId, Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    Set<CompositionEntityValidationData> validationData = new HashSet<>();
    Collection<ComponentEntity> components =
        componentDao.list(new ComponentEntity(vspId, version, null));
    if (!CollectionUtils.isEmpty(components)) {
      components.forEach(component -> {
        validateImage(vspId, version, validationData, component);
        validateNic(vspId, version, validationData, component);

      });
    }

    return validationData;
  }

  private void validateNic(String vspId, Version version,
                           Set<CompositionEntityValidationData> validationData,
                           ComponentEntity component) {
    Collection<NicEntity> nics =
        nicDao.list(new NicEntity(vspId, version, component.getId(), null));
    if (CollectionUtils.isNotEmpty(nics)) {
      nics.forEach(nicEntity -> {
        NicEntity nic = nicDao.get(new NicEntity(vspId, version, component.getId(),
            nicEntity.getId()));
        NetworkType networkType = nic.getNicCompositionData().getNetworkType();
        String networkId = nic.getNicCompositionData().getNetworkId();
        if (networkType.equals(NetworkType.Internal) && networkId == null) {
          CompositionEntityValidationData compositionEntityValidationData = new
              CompositionEntityValidationData(CompositionEntityType.nic, nic.getId());
          compositionEntityValidationData.setEntityName(nic.getNicCompositionData().getName());
          ErrorCode nicInternalNetworkErrorBuilder = NicInternalNetworkErrorBuilder
              .getNicNullNetworkIdInternalNetworkIdErrorBuilder();
          List<String> errors = new ArrayList<>();
          errors.add(nicInternalNetworkErrorBuilder.message());
          compositionEntityValidationData.setErrors(errors);
          validationData.add(compositionEntityValidationData);
        }
      });
    }
  }

  private void validateImage(String vspId, Version version,
                             Set<CompositionEntityValidationData> validationData,
                             ComponentEntity component) {
    Collection<ImageEntity> images =
        imageDao.list(new ImageEntity(vspId, version, component.getId(), null));
    if (CollectionUtils.isEmpty(images)) {
      CompositionEntityValidationData compositionEntityValidationData = new
          CompositionEntityValidationData(component.getType(), component.getId());
      compositionEntityValidationData
          .setEntityName(component.getComponentCompositionData().getDisplayName());
      ErrorCode vfcMissingImageErrorBuilder =
          ComponentErrorBuilder.VfcMissingImageErrorBuilder();
      List<String> errors = new ArrayList<>();
      errors.add(vfcMissingImageErrorBuilder.message());
      compositionEntityValidationData.setErrors(errors);
      validationData.add(compositionEntityValidationData);
    }
  }

  private List<ErrorCode> validateVspFields(VspDetails vspDetails) {
    List<ErrorCode> errors = new ArrayList<>();

    if (vspDetails.getName() == null) {
      errors.add(createMissingMandatoryFieldError("name"));
    }
    if (vspDetails.getDescription() == null) {
      errors.add(createMissingMandatoryFieldError("description"));
    }
    if (vspDetails.getVendorId() == null) {
      errors.add(createMissingMandatoryFieldError("vendor Id"));
    }
    if (vspDetails.getCategory() == null) {
      errors.add(createMissingMandatoryFieldError("category"));
    }
    if (vspDetails.getSubCategory() == null) {
      errors.add(createMissingMandatoryFieldError("sub category"));
    }
    return errors;
  }

  private List<ErrorCode> validateMandatoryLicenseFields(VspDetails vspDetails) {
    List<ErrorCode> errors = new ArrayList<>();
    if (vspDetails.getVlmVersion() == null) {
      errors.add(createMissingMandatoryFieldError("licensing version"));
    }
    if (vspDetails.getLicenseAgreement() == null) {
      errors.add(createMissingMandatoryFieldError("license agreement"));
    }
    if (CollectionUtils.isEmpty(vspDetails.getFeatureGroups())) {
      errors.add(createMissingMandatoryFieldError("feature groups"));
    }
    return errors;
  }

  private static ErrorCode createMissingMandatoryFieldError(String fieldName) {
    return new ValidationErrorBuilder("must be supplied", fieldName).build();
  }

  private Map<String, List<ErrorMessage>> compile(String vendorSoftwareProductId, Version version,
                                                  ToscaServiceModel serviceModel) {
    if (!isServiceModelExist(serviceModel)) {
      return null;
    }

    enrichedServiceModelDao.deleteAll(vendorSoftwareProductId, version);

    EnrichmentManager<ToscaServiceModel> enrichmentManager =
        EnrichmentManagerFactory.getInstance().createInterface();
    enrichmentManager.init(vendorSoftwareProductId, version);
    enrichmentManager.setModel(serviceModel);
    Map<String, List<ErrorMessage>> enrichErrors = enrichmentManager.enrich();

    if (MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, enrichErrors))) {
      LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.ENRICHMENT_COMPLETED
          + vendorSoftwareProductId);
    } else {
      enrichErrors.values().forEach(errorList ->
          auditIfContainsErrors(errorList, vendorSoftwareProductId,
              AuditMessages.ENRICHMENT_ERROR));
    }

    enrichedServiceModelDao
        .storeServiceModel(vendorSoftwareProductId, version, enrichmentManager.getModel());

    return enrichErrors;
  }

  private Collection<ErrorCode> validateLicensingData(VspDetails vspDetails) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspDetails.getId());

    if (vspDetails.getVendorId() == null || vspDetails.getVlmVersion() == null
        || vspDetails.getLicenseAgreement() == null
        || CollectionUtils.isEmpty(vspDetails.getFeatureGroups())) {
      return null;
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspDetails.getId());
    return vendorLicenseFacade
        .validateLicensingData(vspDetails.getVendorId(), vspDetails.getVlmVersion(),
            vspDetails.getLicenseAgreement(), vspDetails.getFeatureGroups());
  }

  @Override
  public VspDetails createVsp(VspDetails vspDetails) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(null);

    vspInfoDao.create(vspDetails);
    vspInfoDao.updateQuestionnaireData(vspDetails.getId(), vspDetails.getVersion(),
        new JsonSchemaDataGenerator(getVspQuestionnaireSchema(null)).generateData());

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(null);
    return vspDetails;
  }

  @Override
  public void updateVsp(VspDetails vspDetails) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspDetails.getId());

    VspDetails retrieved = vspInfoDao.get(vspDetails);
    // TODO: 6/21/2017 remove this validation when validation will be added in the REST level
    if (retrieved == null) {
      throw new RuntimeException(String.format("Vsp with id %s and version %s does not exist.",
          vspDetails.getId(), vspDetails.getVersion().getId()));
    }
    vspDetails.setOnboardingMethod(retrieved.getOnboardingMethod());

    //If any existing feature group is removed from VSP which is also associated in DF then
    //update DF to remove feature group associations.
    updateDeploymentFlavor(vspDetails);

    updateUniqueName(retrieved.getName(), vspDetails.getName());
    vspInfoDao.update(vspDetails);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspDetails.getId());
  }

  private void updateDeploymentFlavor(VspDetails vspDetails) {
    final List<String> featureGroups = vspDetails.getFeatureGroups();
    if (featureGroups != null) {
      final Collection<DeploymentFlavorEntity> deploymentFlavorEntities = deploymentFlavorDao
          .list(new DeploymentFlavorEntity(vspDetails.getId(), vspDetails
              .getVersion(), null));
      if (Objects.nonNull(deploymentFlavorEntities)) {
        for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
          final String featureGroupId =
              deploymentFlavorEntity.getDeploymentFlavorCompositionData().getFeatureGroupId();
          if (!featureGroups.contains(featureGroupId)) {
            DeploymentFlavor deploymentFlavorCompositionData =
                deploymentFlavorEntity.getDeploymentFlavorCompositionData();
            deploymentFlavorCompositionData.setFeatureGroupId(null);
            deploymentFlavorEntity.setDeploymentFlavorCompositionData
                (deploymentFlavorCompositionData);
            deploymentFlavorDao.update(deploymentFlavorEntity);
          }
        }
      }
    }
  }


  @Override
  public VspDetails getVsp(String vspId, Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    VspDetails vsp = getValidatedVsp(vspId, version);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return vsp;
  }

  private VspDetails getValidatedVsp(String vspId, Version version) {
    VspDetails vsp = vspInfoDao.get(new VspDetails(vspId, version));
    if (vsp == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_VSP, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Requested VSP not found");
      throw new CoreException(new VendorSoftwareProductNotFoundErrorBuilder(vspId).build());
    }
    return vsp;
  }

  @Override
  public void deleteVsp(String vspId) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
        LoggerTragetServiceName.DELETE_VSP, ErrorLevel.ERROR.name(),
        LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Unsupported operation");
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);

    throw new UnsupportedOperationException(
        VendorSoftwareProductConstants.UNSUPPORTED_OPERATION_ERROR);
  }

  @Override
  public List<PackageInfo> listPackages(String category, String subCategory) {
    return packageInfoDao.listByCategory(category, subCategory);
  }

  @Override
  public File getTranslatedFile(String vspId, Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);
    String errorMessage;

    PackageInfo packageInfo = packageInfoDao.get(new PackageInfo(vspId, version));
    if (packageInfo == null) {
      errorMessage = "Package not found";
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_TRANSLATED_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), errorMessage);
      throw new CoreException(new PackageNotFoundErrorBuilder(vspId, version).build());
    }

    ByteBuffer translatedFileBuffer = packageInfo.getTranslatedFile();
    if (translatedFileBuffer == null) {
      errorMessage = "Package not found";
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_TRANSLATED_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), errorMessage);
      throw new CoreException(new PackageInvalidErrorBuilder(vspId, version).build());
    }

    File translatedFile = new File(VendorSoftwareProductConstants.VSP_PACKAGE_ZIP);

    try (FileOutputStream fos = new FileOutputStream(translatedFile)) {
      fos.write(translatedFileBuffer.array());
    } catch (IOException exception) {
      errorMessage = "Can't create package";
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_TRANSLATED_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), errorMessage);
      throw new CoreException(new TranslationFileCreationErrorBuilder(vspId, version).build(),
          exception);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);

    return translatedFile;
  }

  @Override

  public byte[] getOrchestrationTemplateFile(String vspId, Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    OrchestrationTemplateEntity uploadData = orchestrationTemplateDao.get(vspId, version);
    ByteBuffer contentData = uploadData.getContentData();
    if (contentData == null) {
      return null;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (final ZipOutputStream zos = new ZipOutputStream(baos);
         ZipInputStream ignored = new ZipInputStream(
             new ByteArrayInputStream(contentData.array()))) {
      zos.write(contentData.array());
    } catch (IOException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_UPLOADED_HEAT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't get uploaded HEAT");
      throw new CoreException(new FileCreationErrorBuilder(vspId).build(), exception);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return baos.toByteArray();
  }

  @Override
  public OrchestrationTemplateEntity getOrchestrationTemplateInfo(String vspId, Version version) {
    return orchestrationTemplateDao.getInfo(vspId, version);
  }

  @Override
  public PackageInfo createPackage(String vspId, Version version) throws IOException {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    ToscaServiceModel toscaServiceModel = enrichedServiceModelDao.getServiceModel(vspId, version);
    VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));
    Version vlmVersion = vspDetails.getVlmVersion();

    PackageInfo packageInfo = createPackageInfo(vspDetails);

    ToscaFileOutputServiceCsarImpl toscaServiceTemplateServiceCsar =
        new ToscaFileOutputServiceCsarImpl();
    FileContentHandler licenseArtifacts = licenseArtifactsService
        .createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion,
            vspDetails.getFeatureGroups());
    //todo add tosca validation here
    packageInfo.setTranslatedFile(ByteBuffer.wrap(
        toscaServiceTemplateServiceCsar.createOutputFile(toscaServiceModel, licenseArtifacts)));

    packageInfoDao.create(packageInfo);

    LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.CREATE_PACKAGE + vspId);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return packageInfo;
  }

  private PackageInfo createPackageInfo(VspDetails vspDetails) {
    PackageInfo packageInfo = new PackageInfo(vspDetails.getId(), vspDetails.getVersion());
    packageInfo.setVspName(vspDetails.getName());
    packageInfo.setVspDescription(vspDetails.getDescription());
    packageInfo.setCategory(vspDetails.getCategory());
    packageInfo.setSubCategory(vspDetails.getSubCategory());
    packageInfo.setVendorName(vspDetails.getVendorName());
    packageInfo.setPackageType(VendorSoftwareProductConstants.CSAR);
    packageInfo.setVendorRelease("1.0"); //todo TBD
    return packageInfo;
  }

  @Override

  public QuestionnaireResponse getVspQuestionnaire(String vspId, Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    VspQuestionnaireEntity retrieved = vspInfoDao.getQuestionnaire(vspId, version);
    VersioningUtil.validateEntityExistence(retrieved, new VspQuestionnaireEntity(vspId, version),
        VspDetails.ENTITY_TYPE);

    String questionnaireData = retrieved.getQuestionnaireData();

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setData(questionnaireData);
    questionnaireResponse.setSchema(getVspQuestionnaireSchema(null));

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);

    return questionnaireResponse;
  }

  @Override
  public void updateVspQuestionnaire(String vspId, Version version, String questionnaireData) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    vspInfoDao.updateQuestionnaireData(vspId, version, questionnaireData);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
  }


  private Map<String, List<ErrorMessage>> validateOrchestrationTemplate(
      OrchestrationTemplateEntity orchestrationTemplate) throws IOException {

    if (!isOrchestrationTemplateExist(orchestrationTemplate)) {
      return null;
    }
    Map<String, List<ErrorMessage>> validationErrors = new HashMap<>();

    FileContentHandler fileContentMap = CommonUtil.validateAndUploadFileContent(
        OnboardingTypesEnum.getOnboardingTypesEnum(orchestrationTemplate.getFileSuffix()),
        orchestrationTemplate.getContentData().array());

    if (CommonUtil.isFileOriginFromZip(orchestrationTemplate.getFileSuffix())) {
      ValidationManager validationManager =
          ValidationManagerUtil.initValidationManager(fileContentMap);
      validationErrors.putAll(validationManager.validate());
    }

    return
        MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, validationErrors))
            ? null : validationErrors;
  }

  private QuestionnaireValidationResult validateQuestionnaire(String vspId, Version version,
                                                              String onboardingMethod) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    // The apis of CompositionEntityDataManager used here are stateful!
    // so, it must be re-created from scratch when it is used!
    CompositionEntityDataManager compositionEntityDataManager =
        CompositionEntityDataManagerFactory.getInstance().createInterface();
    compositionEntityDataManager
        .addEntity(vspInfoDao.getQuestionnaire(vspId, version), null);

    Collection<NicEntity> nics = nicDao.listByVsp(vspId, version);

    Map<String, List<String>> nicNamesByComponent = new HashMap<>();
    for (NicEntity nicEntity : nics) {
      compositionEntityDataManager.addEntity(nicEntity, null);

      Nic nic = nicEntity.getNicCompositionData();
      if (nic != null && nic.getName() != null) {
        List<String> nicNames =
            nicNamesByComponent.computeIfAbsent(nicEntity.getComponentId(), k -> new ArrayList<>());
        nicNames.add(nic.getName());
      }
    }

    Collection<ComponentEntity> components =
        componentDao.listCompositionAndQuestionnaire(vspId, version);
    components.forEach(component -> compositionEntityDataManager.addEntity(component,
        new ComponentQuestionnaireSchemaInput(nicNamesByComponent.get(component.getId()),
            JsonUtil.json2Object(component.getQuestionnaireData(), Map.class))));

    Collection<ComputeEntity> computes = computeDao.listByVsp(vspId, version);
    computes.forEach(compute -> compositionEntityDataManager.addEntity(compute, null));

    if (OnboardingMethod.Manual.name().equals(onboardingMethod)) {
      Collection<ImageEntity> images = imageDao.listByVsp(vspId, version);
      images.forEach(image -> compositionEntityDataManager.addEntity(image, null));
    }

    Map<CompositionEntityId, Collection<String>> errorsByEntityId =
        compositionEntityDataManager.validateEntitiesQuestionnaire();
    if (MapUtils.isNotEmpty(errorsByEntityId)) {
      compositionEntityDataManager.buildTrees();
      compositionEntityDataManager.addErrorsToTrees(errorsByEntityId);
/*      Set<CompositionEntityValidationData> entitiesWithValidationErrors =
          compositionEntityDataManager.getEntityListWithErrors();*/
      //Collection<CompositionEntityValidationData> roots = compositionEntityDataManager.getTrees();

      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
      return new QuestionnaireValidationResult(
          compositionEntityDataManager.getAllErrorsByVsp(vspId));
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return null;
  }

  @Override
  public File getInformationArtifact(String vspId, Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);
    VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));

    if (vspDetails == null) {
      return null;
    }

    String vspName = vspDetails.getName();
    ByteBuffer infoArtifactAsByteBuffer;
    File infoArtifactFile;
    try {
      infoArtifactAsByteBuffer = ByteBuffer.wrap(informationArtifactGenerator.generate(vspId,
          version).getBytes());

      infoArtifactFile =
          new File(
              String.format(VendorSoftwareProductConstants.INFORMATION_ARTIFACT_NAME, vspName));
      try (OutputStream out = new BufferedOutputStream(new FileOutputStream(infoArtifactFile))) {
        out.write(infoArtifactAsByteBuffer.array());
      }

    } catch (IOException ex) {
      throw new CoreException(new InformationArtifactCreationErrorBuilder(vspId).build(), ex);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return infoArtifactFile;
  }

  String getVspQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(null);
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(null);
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.vsp, schemaInput);
  }

  void updateUniqueName(String oldVspName, String newVspName) {
    UniqueValueUtil.updateUniqueValue(
        VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
        oldVspName, newVspName);
  }

  @Override
  public Collection<ComputeEntity> getComputeByVsp(String vspId, Version version) {
    return computeDao.listByVsp(vspId, version);
  }

  private boolean isOrchestrationTemplateExist(OrchestrationTemplateEntity orchestrationTemplate) {
    return orchestrationTemplate != null &&
        orchestrationTemplate.getContentData() != null &&
        orchestrationTemplate.getFileSuffix() != null &&
        orchestrationTemplate.getFileName() != null;
  }

  private boolean isServiceModelExist(ToscaServiceModel serviceModel) {
    return serviceModel != null && serviceModel.getEntryDefinitionServiceTemplate() != null;
  }

  private void auditIfContainsErrors(List<ErrorMessage> errorList, String vspId, String auditType) {
    errorList.forEach(errorMessage -> {
      if (errorMessage.getLevel().equals(ErrorLevel.ERROR)) {
        LOGGER.audit(AuditMessages.AUDIT_MSG + String.format(auditType, errorMessage.getMessage(),
            vspId));
      }
    });
  }


}
