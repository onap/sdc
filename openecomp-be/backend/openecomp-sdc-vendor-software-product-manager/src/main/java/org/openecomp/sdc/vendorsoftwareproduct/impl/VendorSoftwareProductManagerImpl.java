/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.dao.UniqueValueDao;
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
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.licenseartifacts.VendorLicenseArtifactsService;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ManualVspToscaManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeDao;
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
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi.ETSIService;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi.ETSIServiceImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireValidationResult;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
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
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductInvalidErrorBuilder.candidateDataNotProcessedOrAbortedErrorBuilder;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductInvalidErrorBuilder.invalidProcessedCandidate;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductInvalidErrorBuilder.vspMissingDeploymentFlavorErrorBuilder;

public class VendorSoftwareProductManagerImpl implements VendorSoftwareProductManager {

  private VspMergeDao vspMergeDao;
  private OrchestrationTemplateDao orchestrationTemplateDao;
  private OrchestrationTemplateCandidateManager orchestrationTemplateCandidateManager;
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
  private UniqueValueUtil uniqueValueUtil;
  private CandidateService candidateService;

  @Override
  public ValidationResponse validate(VspDetails vspDetails) throws IOException {
    List<ErrorCode> vspErrors = new ArrayList<>(validateVspFields(vspDetails));
    ValidationResponse validationResponse = new ValidationResponse();
    if (Objects.nonNull(vspDetails.getOnboardingMethod())
        && OnboardingMethod.Manual.name().equals(vspDetails.getOnboardingMethod())) {
      validateManualOnboardingMethod(vspDetails, validationResponse, vspErrors);
    } else {
      validateOrchestrationTemplateCandidate(validationResponse, vspErrors, vspDetails.getId(), vspDetails.getVersion());
      if (!validationResponse.isValid()) {
        return validationResponse;
      }
      validateLicense(vspDetails, vspErrors);
      OrchestrationTemplateEntity orchestrationTemplate =
          orchestrationTemplateDao.get(vspDetails.getId(), vspDetails.getVersion());
      ToscaServiceModel serviceModel =
          serviceModelDao.getServiceModel( vspDetails.getId(), vspDetails.getVersion());
      if (isOrchestrationTemplateMissing(orchestrationTemplate)
          || isServiceModelMissing(serviceModel)) {
        vspErrors.add(VendorSoftwareProductInvalidErrorBuilder
            .vendorSoftwareProductMissingServiceModelErrorBuilder(vspDetails.getId(),
                vspDetails.getVersion()));
      }
      validationResponse.setUploadDataErrors(validateOrchestrationTemplate(orchestrationTemplate));
    }

    QuestionnaireValidationResult questionnaireValidationResult = validateQuestionnaire
        (vspDetails.getId(), vspDetails.getVersion(), vspDetails.getOnboardingMethod());

    if (Objects.nonNull(questionnaireValidationResult)) {
      if (validationResponse.getQuestionnaireValidationResult() == null || validationResponse
          .getQuestionnaireValidationResult().getValidationData() == null) {
        validationResponse.setQuestionnaireValidationResult(questionnaireValidationResult);
      } else {
        validationResponse.getQuestionnaireValidationResult().getValidationData().addAll
            (questionnaireValidationResult.getValidationData());
      }
    }

    Collection<ComponentDependencyModelEntity> componentDependencies =
        componentDependencyModelDao.list(new ComponentDependencyModelEntity(vspDetails.getId(), vspDetails.getVersion(), null));

    if (validateComponentDependencies(componentDependencies)) {
      vspErrors
          .add(ComponentDependencyModelErrorBuilder.getcyclicDependencyComponentErrorBuilder());
    }
    validationResponse
        .setVspErrors(vspErrors);
    validationResponse.setLicensingDataErrors(validateLicensingData(vspDetails));
    return validationResponse;
  }

  private void validateLicense(VspDetails vspDetails, List<ErrorCode> vspErrors) {
    if (vspDetails.getVlmVersion() != null || vspDetails.getLicenseAgreement() != null
            || vspDetails.getFeatureGroups() != null) {
      vspErrors.addAll(validateMandatoryLicenseFields(vspDetails));
    }
  }

  private void validateOrchestrationTemplateCandidate(ValidationResponse validationResponse,
                                                      List<ErrorCode> vspErrors, String vspId,
                                                      Version version) {
    orchestrationTemplateCandidateManager.getInfo(vspId, version)
        .ifPresent(candidateInfo -> {
          String fileName = candidateInfo.getFileName();
          vspErrors.add(candidateInfo.getValidationData().isEmpty()
              ? candidateDataNotProcessedOrAbortedErrorBuilder(fileName)
              : invalidProcessedCandidate(fileName));
          validationResponse.setVspErrors(vspErrors);
        });
  }

  private void validateManualOnboardingMethod(VspDetails vspDetails,
                                              ValidationResponse validationResponse,
                                              List<ErrorCode> vspErrors) {
    vspErrors.addAll(validateMandatoryLicenseFields(vspDetails));

    Collection<DeploymentFlavorEntity> deploymentFlavors = deploymentFlavorDao
        .list(new DeploymentFlavorEntity(vspDetails.getId(), vspDetails.getVersion(), null));
    if (CollectionUtils.isEmpty(deploymentFlavors)) {
      vspErrors.add(vspMissingDeploymentFlavorErrorBuilder());
    }
    vspErrors.addAll(validateDeploymentFlavors(deploymentFlavors));

    Set<CompositionEntityValidationData> componentValidationResult =
        componentValidation(vspDetails.getId(), vspDetails.getVersion());
    if (!CollectionUtils.isEmpty(componentValidationResult)) {
      if (validationResponse.getQuestionnaireValidationResult() == null
          || validationResponse.getQuestionnaireValidationResult().getValidationData() == null) {
        validationResponse.setQuestionnaireValidationResult(
            new QuestionnaireValidationResult(componentValidationResult));
      } else {
        validationResponse.getQuestionnaireValidationResult().getValidationData()
            .addAll(componentValidationResult);
      }
    }
  }

  @Override
  public Map<String, List<ErrorMessage>> compile(String vspId, Version version) {
    ToscaServiceModel serviceModel =
        OnboardingMethod.Manual.name().equals(getValidatedVsp(vspId, version).getOnboardingMethod())
            //Generate Tosca service model for Manual Onboarding flow
            ? manualVspToscaManager
            .generateToscaModel(manualVspToscaManager.gatherVspInformation(vspId, version))
            : serviceModelDao.getServiceModel(vspId, version);

    return compile(vspId, version, serviceModel);
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
        DeploymentFlavor deploymentLocalFlavor = deployment.getDeploymentFlavorCompositionData();
        if (deploymentLocalFlavor != null) {
          if (deploymentLocalFlavor.getFeatureGroupId() == null) {
            ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder.
                getFeatureGroupMandatoryErrorBuilder(deploymentLocalFlavor.getModel());
            errorCodeList.add(deploymentFlavorErrorBuilder);
          }
          validateComponentComputeAssociations(errorCodeList, deploymentFlavor,
              deployment, deploymentLocalFlavor);
        }
      });
    }
    return errorCodeList;
  }

  private void validateComponentComputeAssociations(Collection<ErrorCode> errorCodeList,
                                                    DeploymentFlavorEntity deploymentFlavor,
                                                    DeploymentFlavorEntity deployment,
                                                    DeploymentFlavor deploymentlocalFlavor) {
    List<ComponentComputeAssociation> componentComputeAssociations =
        deploymentlocalFlavor.getComponentComputeAssociations();
    if (CollectionUtils.isEmpty(componentComputeAssociations)) {
        validateCompositionEntity(errorCodeList, deploymentFlavor, deployment, deploymentlocalFlavor);
    } else {
      componentComputeAssociations.forEach(componentComputeAssociation -> {
        if (componentComputeAssociation == null
            || !(componentComputeAssociation.getComponentId() != null
            && componentComputeAssociation.getComputeFlavorId() != null)) {
            validateCompositionEntity(errorCodeList, deploymentFlavor, deployment, deploymentlocalFlavor);
        }
      });
    }
  }

  private void validateCompositionEntity(Collection<ErrorCode> errorCodeList,
                                                                             DeploymentFlavorEntity deploymentFlavor,
                                                                             DeploymentFlavorEntity deployment,
                                                                             DeploymentFlavor deploymentlocalFlavor){
    CompositionEntityValidationData compositionEntityValidationData = new
            CompositionEntityValidationData(CompositionEntityType.deployment, deploymentFlavor
            .getId());
    compositionEntityValidationData.setEntityName(deployment
            .getDeploymentFlavorCompositionData().getModel());
    ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder
            .getInvalidComponentComputeAssociationErrorBuilder(
                    deploymentlocalFlavor.getModel());
    errorCodeList.add(deploymentFlavorErrorBuilder);
  }

  private Set<CompositionEntityValidationData> componentValidation(String vspId, Version version) {
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
          ComponentErrorBuilder.vfcMissingImageErrorBuilder();
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
    if (isServiceModelMissing(serviceModel)) {
      return null;
    }

    enrichedServiceModelDao.deleteAll(vendorSoftwareProductId, version);

    EnrichmentManager<ToscaServiceModel> enrichmentManager =
        EnrichmentManagerFactory.getInstance().createInterface();
    enrichmentManager.init(vendorSoftwareProductId, version);
    enrichmentManager.setModel(serviceModel);
    Map<String, List<ErrorMessage>> enrichErrors = enrichmentManager.enrich();

    enrichedServiceModelDao
        .storeServiceModel(vendorSoftwareProductId, version, enrichmentManager.getModel());

    return enrichErrors;
  }

  private Collection<ErrorCode> validateLicensingData(VspDetails vspDetails) {
    if (vspDetails.getVendorId() != null) {
      Optional<ErrorCode> errorCode = vendorLicenseFacade.validateVendorForUsage(vspDetails.getVendorId(),vspDetails.getVlmVersion());
      if (errorCode.isPresent()) {
        return Collections.singleton(errorCode.get());
      }
    }

    if (vspDetails.getVendorId() == null || vspDetails.getVlmVersion() == null
        || vspDetails.getLicenseAgreement() == null
        || CollectionUtils.isEmpty(vspDetails.getFeatureGroups())) {
      return Collections.emptyList();
    }
    return vendorLicenseFacade
        .validateLicensingData(vspDetails.getVendorId(), vspDetails.getVlmVersion(),
            vspDetails.getLicenseAgreement(), vspDetails.getFeatureGroups());
  }

  @Override
  public VspDetails createVsp(VspDetails vspDetails) {
    vspInfoDao.create(vspDetails);
    vspInfoDao.updateQuestionnaireData(vspDetails.getId(), vspDetails.getVersion(),
        new JsonSchemaDataGenerator(getVspQuestionnaireSchema(null)).generateData());
    return vspDetails;
  }

  @Override
  public void updateVsp(VspDetails vspDetails) {
    VspDetails retrieved = vspInfoDao.get(vspDetails);
    if (retrieved == null) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder()
          .withMessage(String.format("Vsp with id %s and version %s does not exist.",
              vspDetails.getId(), vspDetails.getVersion().getId()))).build());
    }
    vspDetails.setOnboardingMethod(retrieved.getOnboardingMethod());

    //If any existing feature group is removed from VSP which is also associated in DF then
    //update DF to remove feature group associations.
    updateDeploymentFlavor(vspDetails);

    updateUniqueName(retrieved.getName(), vspDetails.getName());
    vspInfoDao.update(vspDetails);
  }

  private void updateDeploymentFlavor(VspDetails vspDetails) {
    final List<String> featureGroups = vspDetails.getFeatureGroups();
    if (featureGroups != null) {
      final Collection<DeploymentFlavorEntity> deploymentFlavorEntities = deploymentFlavorDao
          .list(new DeploymentFlavorEntity(vspDetails.getId(), vspDetails
              .getVersion(), null));
      if (Objects.nonNull(deploymentFlavorEntities)) {
        for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
          updateDeploymentFlavourEntity(featureGroups, deploymentFlavorEntity);
        }
      }
    }
  }

  private void updateDeploymentFlavourEntity(List<String> featureGroups,
                                             DeploymentFlavorEntity deploymentFlavorEntity) {
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


  @Override
  public VspDetails getVsp(String vspId, Version version) {
    return getValidatedVsp(vspId, version);
  }

  private VspDetails getValidatedVsp(String vspId, Version version) {
    VspDetails vsp = vspInfoDao.get(new VspDetails(vspId, version));
    if (vsp == null) {
      throw new CoreException(new VendorSoftwareProductNotFoundErrorBuilder(vspId).build());
    }
    return vsp;
  }

  @Override
  public void deleteVsp(String vspId, Version version) {
    vspMergeDao.deleteHint(vspId, version);
  }

  @Override
  public List<PackageInfo> listPackages(String category, String subCategory) {
    return packageInfoDao.listByCategory(category, subCategory);
  }

  @Override
  public File getTranslatedFile(String vspId, Version version) {
    PackageInfo packageInfo = packageInfoDao.get(new PackageInfo(vspId, version));
    if (packageInfo == null) {
      throw new CoreException(new PackageNotFoundErrorBuilder(vspId, version).build());
    }

    ByteBuffer translatedFileBuffer = packageInfo.getTranslatedFile();
    if (translatedFileBuffer == null) {
      throw new CoreException(new PackageInvalidErrorBuilder(vspId, version).build());
    }

    File translatedFile = new File(VendorSoftwareProductConstants.VSP_PACKAGE_ZIP);

    try (FileOutputStream fos = new FileOutputStream(translatedFile)) {
      fos.write(translatedFileBuffer.array());
    } catch (IOException exception) {
      throw new CoreException(new TranslationFileCreationErrorBuilder(vspId, version).build(),
          exception);
    }
    return translatedFile;
  }

  @Override
  public byte[] getOrchestrationTemplateFile(String vspId, Version version) {
    OrchestrationTemplateEntity uploadData = orchestrationTemplateDao.get(vspId, version);
    ByteBuffer contentData = uploadData.getContentData();
    if (contentData == null) {
      return new byte[0];
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (final ZipOutputStream zos = new ZipOutputStream(baos);
         ZipInputStream ignored = new ZipInputStream(
             new ByteArrayInputStream(contentData.array()))) {
      zos.write(contentData.array());
    } catch (IOException exception) {
      throw new CoreException(new FileCreationErrorBuilder(vspId).build(), exception);
    }
    return baos.toByteArray();
  }

  @Override
  public OrchestrationTemplateEntity getOrchestrationTemplateInfo(String vspId, Version version) {
    return orchestrationTemplateDao.getInfo(vspId, version);
  }

  @Override
  public Optional<FilesDataStructure> getOrchestrationTemplateStructure(String vspId,
                                                                        Version version) {
    Optional<String> jsonFileDataStructure =
        orchestrationTemplateDao.getOrchestrationTemplateStructure(vspId, version);

    if (jsonFileDataStructure.isPresent() && JsonUtil.isValidJson(jsonFileDataStructure.get())) {
      return Optional
          .of(JsonUtil.json2Object(jsonFileDataStructure.get(), FilesDataStructure.class));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public PackageInfo createPackage(String vspId, Version version) throws IOException {
    ToscaServiceModel toscaServiceModel = enrichedServiceModelDao.getServiceModel(vspId, version);
    VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));
    Version vlmVersion = vspDetails.getVlmVersion();
    if (vlmVersion != null) {
      populateVersionsForVlm(vspDetails.getVendorId(), vlmVersion);
    }
    PackageInfo packageInfo = createPackageInfo(vspDetails);

    ToscaFileOutputServiceCsarImpl toscaServiceTemplateServiceCsar =
        new ToscaFileOutputServiceCsarImpl();
    FileContentHandler licenseArtifacts = licenseArtifactsService
        .createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion,
            vspDetails.getFeatureGroups());
    ETSIService etsiService = new ETSIServiceImpl();
    if (etsiService.isSol004WithToscaMetaDirectory(toscaServiceModel.getArtifactFiles())) {
        FileContentHandler handler = toscaServiceModel.getArtifactFiles();
        Manifest manifest = etsiService.getManifest(handler);
        etsiService.moveNonManoFileToArtifactFolder(handler, manifest);
        packageInfo.setResourceType(etsiService.getResourceType(manifest).name());
    }
    packageInfo.setTranslatedFile(ByteBuffer.wrap(
        toscaServiceTemplateServiceCsar.createOutputFile(toscaServiceModel, licenseArtifacts)));

    packageInfoDao.create(packageInfo);
    return packageInfo;
  }

  void populateVersionsForVlm(String vlmId, Version vlmVersion) {
    VersioningManager versioningManager = VersioningManagerFactory.getInstance().createInterface();
    versioningManager.list(vlmId).stream()
        .filter(version -> version.getId().equalsIgnoreCase(vlmVersion.getId()))
        .findAny()
        .ifPresent(version -> {
          vlmVersion.setMinor(version.getMinor());
          vlmVersion.setMajor(version.getMajor());
        });
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
    VspQuestionnaireEntity retrieved = vspInfoDao.getQuestionnaire(vspId, version);
    VersioningUtil.validateEntityExistence(retrieved, new VspQuestionnaireEntity(vspId, version),
        VspDetails.ENTITY_TYPE);

    String questionnaireData = retrieved.getQuestionnaireData();

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setData(questionnaireData);
    questionnaireResponse.setSchema(getVspQuestionnaireSchema(null));
    return questionnaireResponse;
  }

  @Override
  public void updateVspQuestionnaire(String vspId, Version version, String questionnaireData) {
    vspInfoDao.updateQuestionnaireData(vspId, version, questionnaireData);
  }


  private Map<String, List<ErrorMessage>> validateOrchestrationTemplate(
      OrchestrationTemplateEntity orchestrationTemplate) throws IOException {

    if (isOrchestrationTemplateMissing(orchestrationTemplate)) {
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
            JsonUtil.json2Object(component.getQuestionnaireData(), Map.class), null,
                                                     OnboardingMethod.Manual.name().equals(onboardingMethod))));

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
      return new QuestionnaireValidationResult(
          compositionEntityDataManager.getAllErrorsByVsp(vspId));
    }
    return null;
  }

  @Override
  public File getInformationArtifact(String vspId, Version version) {
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
    return infoArtifactFile;
  }

  String getVspQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.vsp, schemaInput);
  }

  @Override
  public Optional<Pair<String, byte[]>> get(String vspId, Version version) throws IOException {

    OrchestrationTemplateEntity orchestrationTemplateEntity =
        orchestrationTemplateDao.get(vspId, version);

    if (isOrchestrationTemplateMissing(orchestrationTemplateEntity)) {
      return Optional.empty();
    }

    if (CommonUtil.isFileOriginFromZip(orchestrationTemplateEntity.getFileSuffix())) {
      return Optional.of(new ImmutablePair<>(OnboardingTypesEnum.ZIP.toString(), candidateService
          .getZipData(orchestrationTemplateEntity.getContentData())));
    }
    return Optional.of(new ImmutablePair<>(orchestrationTemplateEntity.getFileSuffix(),
        orchestrationTemplateEntity.getContentData().array()));
  }

  void updateUniqueName(String oldVspName, String newVspName) {
    uniqueValueUtil.updateUniqueValue(
        VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
        oldVspName, newVspName);
  }

  @Override
  public Collection<ComputeEntity> getComputeByVsp(String vspId, Version version) {
    return computeDao.listByVsp(vspId, version);
  }

  private boolean isOrchestrationTemplateMissing(
      OrchestrationTemplateEntity orchestrationTemplate) {
    return orchestrationTemplate == null
        || orchestrationTemplate.getContentData() == null
        || orchestrationTemplate.getFileSuffix() == null
        || orchestrationTemplate.getFileName() == null;
  }

  private boolean isServiceModelMissing(ToscaServiceModel serviceModel) {
    return serviceModel == null || serviceModel.getEntryDefinitionServiceTemplate() == null;
  }

  public static class Builder{
    private VspMergeDao vspMergeDao;
    private OrchestrationTemplateDao orchestrationTemplateDao;
    private OrchestrationTemplateCandidateManager orchestrationTemplateCandidateManager;
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
    private UniqueValueDao uniqueValueDao;
    private CandidateService candidateService;

    public Builder vspMerge(VspMergeDao vspMergeDao){
      this.vspMergeDao = vspMergeDao;
      return this;
    }

    public Builder orchestrationTemplate(OrchestrationTemplateDao orchestrationTemplateDao) {
      this.orchestrationTemplateDao = orchestrationTemplateDao;
      return this;
    }

    public Builder orchestrationTemplateCandidateManager(OrchestrationTemplateCandidateManager orchestrationTemplateCandidateManager) {
      this.orchestrationTemplateCandidateManager = orchestrationTemplateCandidateManager;
      return this;
    }

    public Builder vspInfo(VendorSoftwareProductInfoDao vspInfoDao) {
      this.vspInfoDao = vspInfoDao;
      return this;
    }

    public Builder vendorLicenseFacade(VendorLicenseFacade vendorLicenseFacade) {
      this.vendorLicenseFacade = vendorLicenseFacade;
      return this;
    }

    public Builder serviceModel(ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao) {
      this.serviceModelDao = serviceModelDao;
      return this;
    }

    public Builder enrichedServiceModel(EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> enrichedServiceModelDao) {
      this.enrichedServiceModelDao = enrichedServiceModelDao;
      return this;
    }

    public Builder licenseArtifactsService(VendorLicenseArtifactsService licenseArtifactsService) {
      this.licenseArtifactsService = licenseArtifactsService;
      return this;
    }

    public Builder informationArtifactGenerator(InformationArtifactGenerator informationArtifactGenerator) {
      this.informationArtifactGenerator = informationArtifactGenerator;
      return this;
    }

    public Builder packageInfo(PackageInfoDao packageInfoDao) {
      this.packageInfoDao = packageInfoDao;
      return this;
    }

    public Builder deploymentFlavor(DeploymentFlavorDao deploymentFlavorDao) {
      this.deploymentFlavorDao = deploymentFlavorDao;
      return this;
    }

    public Builder component(ComponentDao componentDao) {
      this.componentDao = componentDao;
      return this;
    }

    public Builder componentDependencyModel(ComponentDependencyModelDao componentDependencyModelDao) {
      this.componentDependencyModelDao = componentDependencyModelDao;
      return this;
    }

    public Builder nic(NicDao nicDao) {
      this.nicDao = nicDao;
      return this;
    }

    public Builder compute(ComputeDao computeDao) {
      this.computeDao = computeDao;
      return this;
    }

    public Builder image(ImageDao imageDao) {
      this.imageDao = imageDao;
      return this;
    }

    public Builder manualVspToscaManager(ManualVspToscaManager manualVspToscaManager) {
      this.manualVspToscaManager = manualVspToscaManager;
      return this;
    }

    public Builder uniqueValue(UniqueValueDao uniqueValueDao) {
      this.uniqueValueDao = uniqueValueDao;
      return this;
    }

    public Builder candidateService(CandidateService candidateService) {
      this.candidateService = candidateService;
      return this;
    }

    private void registerToVersioning() {
      if(serviceModelDao != null) {
        serviceModelDao.registerVersioning(
                VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
      }
      if(enrichedServiceModelDao != null) {
        enrichedServiceModelDao.registerVersioning(
                VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
      }
    }

    public VendorSoftwareProductManager build(){
      VendorSoftwareProductManagerImpl vendorSoftwareProductManager = new VendorSoftwareProductManagerImpl();
      vendorSoftwareProductManager.vspMergeDao = this.vspMergeDao;
      vendorSoftwareProductManager.orchestrationTemplateDao = this.orchestrationTemplateDao;
      vendorSoftwareProductManager.orchestrationTemplateCandidateManager = this.orchestrationTemplateCandidateManager;
      vendorSoftwareProductManager.vspInfoDao = this.vspInfoDao;
      vendorSoftwareProductManager.vendorLicenseFacade = this.vendorLicenseFacade;
      vendorSoftwareProductManager.serviceModelDao = this.serviceModelDao;
      vendorSoftwareProductManager.enrichedServiceModelDao = this.enrichedServiceModelDao;
      vendorSoftwareProductManager.licenseArtifactsService = this.licenseArtifactsService;
      vendorSoftwareProductManager.informationArtifactGenerator = this.informationArtifactGenerator;
      vendorSoftwareProductManager.packageInfoDao = this.packageInfoDao;
      vendorSoftwareProductManager.deploymentFlavorDao = this.deploymentFlavorDao;
      vendorSoftwareProductManager.componentDao = this.componentDao;
      vendorSoftwareProductManager.componentDependencyModelDao = this.componentDependencyModelDao;
      vendorSoftwareProductManager.nicDao = this.nicDao;
      vendorSoftwareProductManager.computeDao = this.computeDao;
      vendorSoftwareProductManager.imageDao = this.imageDao;
      vendorSoftwareProductManager.manualVspToscaManager = this.manualVspToscaManager;
      vendorSoftwareProductManager.uniqueValueUtil = new UniqueValueUtil(this.uniqueValueDao);
      vendorSoftwareProductManager.candidateService = candidateService;
      this.registerToVersioning();
      return vendorSoftwareProductManager;
    }
  }

}
