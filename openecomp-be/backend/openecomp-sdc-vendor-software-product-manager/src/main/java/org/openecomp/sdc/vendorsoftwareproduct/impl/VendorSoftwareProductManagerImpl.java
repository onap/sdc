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
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ValidationErrorBuilder;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.generator.datatypes.tosca.VspModelInfo;
import org.openecomp.sdc.healing.api.HealingManager;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.VendorSoftwareProductNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CreatePackageForNonFinalVendorSoftwareProductErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.DeploymentFlavorErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.FileCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.InformationArtifactCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NicInternalNetworkErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.OnboardingMethodErrorBuilder;
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
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
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
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.errors.RequestedVersionInvalidErrorBuilder;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdcrests.activitylog.types.ActivityType;
import org.slf4j.MDC;

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
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class VendorSoftwareProductManagerImpl implements VendorSoftwareProductManager {
  private String VALIDATION_VSP_ID = "validationOnlyVspId";
  private static final String VALIDATION_VSP_NAME = "validationOnlyVspName";
  //private static final String VALIDATION_VSP_USER = "validationOnlyVspUser";

  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private static final Logger LOGGER =
      LoggerFactory.getLogger(VendorSoftwareProductManagerImpl.class);

  private final OrchestrationTemplateDao orchestrationTemplateDao;
  private final VendorSoftwareProductInfoDao vspInfoDao;
  private final VersioningManager versioningManager;
  private final VendorSoftwareProductDao vendorSoftwareProductDao;
  private final VendorLicenseFacade vendorLicenseFacade;
  private final ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao;
  private final EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> enrichedServiceModelDao;
  private final HealingManager healingManager;
  private final VendorLicenseArtifactsService licenseArtifactsService;
  private final InformationArtifactGenerator informationArtifactGenerator;
  private final PackageInfoDao packageInfoDao;
  private final ActivityLogManager activityLogManager;
  private final DeploymentFlavorDao deploymentFlavorDao;
  private final NicDao nicDao;
  private final ManualVspToscaManager manualVspToscaManager;

  /**
   * Instantiates a new Vendor software product manager.
   *
   * @param versioningManager            the versioning manager
   * @param vendorSoftwareProductDao     the vendor software product dao
   * @param orchestrationTemplateDataDao the orchestration template data dao
   * @param vspInfoDao                   the vsp info dao
   * @param vendorLicenseFacade          the vendor license facade
   * @param serviceModelDao              the service model dao
   * @param enrichedServiceModelDao      the enriched service model dao
   * @param healingManager               the healing manager
   * @param licenseArtifactsService      the license artifacts service
   * @param informationArtifactGenerator the information artifact generator
   * @param packageInfoDao               the package info dao
   * @param activityLogManager           the activity log manager
   * @param deploymentFlavorDao          the deployment flavor dao
   * @param nicDao                       the nic dao
   */
  public VendorSoftwareProductManagerImpl(
      VersioningManager versioningManager,
      VendorSoftwareProductDao vendorSoftwareProductDao,
      OrchestrationTemplateDao orchestrationTemplateDataDao,
      VendorSoftwareProductInfoDao vspInfoDao,
      VendorLicenseFacade vendorLicenseFacade,
      ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao,
      EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> enrichedServiceModelDao,
      HealingManager healingManager,
      VendorLicenseArtifactsService licenseArtifactsService,
      InformationArtifactGenerator informationArtifactGenerator,
      PackageInfoDao packageInfoDao,
      ActivityLogManager activityLogManager,
      DeploymentFlavorDao deploymentFlavorDao,
      NicDao nicDao,
      ManualVspToscaManager manualVspToscaManager) {
    this.versioningManager = versioningManager;
    this.vendorSoftwareProductDao = vendorSoftwareProductDao;
    this.orchestrationTemplateDao = orchestrationTemplateDataDao;
    this.vspInfoDao = vspInfoDao;
    this.vendorLicenseFacade = vendorLicenseFacade;
    this.serviceModelDao = serviceModelDao;
    this.enrichedServiceModelDao = enrichedServiceModelDao;
    this.healingManager = healingManager;
    this.licenseArtifactsService = licenseArtifactsService;
    this.informationArtifactGenerator = informationArtifactGenerator;
    this.packageInfoDao = packageInfoDao;
    this.activityLogManager = activityLogManager;
    this.deploymentFlavorDao = deploymentFlavorDao;
    this.nicDao = nicDao;
    this.manualVspToscaManager = manualVspToscaManager;

    registerToVersioning();
  }

  private void registerToVersioning() {
    vendorSoftwareProductDao.registerVersioning(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
    serviceModelDao.registerVersioning(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
    enrichedServiceModelDao.registerVersioning(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
  }

  @Override
  public Version checkout(String vendorSoftwareProductId, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vendorSoftwareProductId);
    MDC.put(LoggerConstants.SERVICE_NAME, LoggerServiceName.Checkout_Entity.toString());

    Version newVersion = versioningManager
        .checkout(VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
            vendorSoftwareProductId, user);

    if (newVersion != null) {
      ActivityLogEntity activityLogEntity =
          new ActivityLogEntity(vendorSoftwareProductId, String.valueOf(newVersion.getMajor() + 1),
              ActivityType.CHECKOUT.toString(), user, true, "", "");
      activityLogManager.addActionLog(activityLogEntity, user);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vendorSoftwareProductId);
    return newVersion;
  }


  @Override
  public Version undoCheckout(String vendorSoftwareProductId, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vendorSoftwareProductId);

    Version version =
        getVersionInfo(vendorSoftwareProductId, VersionableEntityAction.Read, user)
            .getActiveVersion();

    ActivityLogEntity activityLogEntity =
        new ActivityLogEntity(vendorSoftwareProductId, String.valueOf(version.getMajor() + 1),
            ActivityType.UNDO_CHECKOUT.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);

    String preVspName = vspInfoDao
        .get(new VspDetails(vendorSoftwareProductId, version)).getName();

    Version newVersion = versioningManager.undoCheckout(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        vendorSoftwareProductId, user);

    String postVspName = vspInfoDao
        .get(new VspDetails(vendorSoftwareProductId, newVersion))
        .getName();

    updateUniqueName(preVspName, postVspName);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vendorSoftwareProductId);

    return newVersion;
  }

  @Override
  public Version checkin(String vendorSoftwareProductId, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vendorSoftwareProductId);

    Version newVersion = versioningManager.checkin(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        vendorSoftwareProductId, user, null);

    if (newVersion != null) {
      ActivityLogEntity activityLogEntity =
          new ActivityLogEntity(vendorSoftwareProductId, String.valueOf(newVersion.getMajor() + 1),
              ActivityType.CHECKIN.toString(), user, true, "", "");
      activityLogManager.addActionLog(activityLogEntity, user);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vendorSoftwareProductId);

    return newVersion;
  }

  @Override
  public ValidationResponse submit(String vspId, String user) throws IOException {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    Version version = getVersionInfo(vspId, VersionableEntityAction.Read, user).getActiveVersion();
    VspDetails vspDetails = getVsp(vspId, version, user);
    UploadDataEntity uploadData = orchestrationTemplateDao.getOrchestrationTemplate(vspId, version);
    ToscaServiceModel serviceModel =
        serviceModelDao.getServiceModel(vspId, vspDetails.getVersion());

    ValidationResponse validationResponse = new ValidationResponse();
    validationResponse
        .setVspErrors(validateCompletedVendorSoftwareProduct(vspDetails, uploadData, serviceModel),
            LoggerServiceName.Submit_VSP, LoggerTragetServiceName.SUBMIT_VSP);

    if (isCyclicDependencyInComponents(vspId, vspDetails.getVersion())) {
      Collection<ErrorCode> vspErrors = validationResponse.getVspErrors() == null
          ? new ArrayList<>()
          : validationResponse.getVspErrors();
      vspErrors.add(ComponentDependencyModelErrorBuilder
          .getcyclicDependencyComponentErrorBuilder());
      validationResponse.setVspErrors(vspErrors, LoggerServiceName.Submit_VSP,
          LoggerTragetServiceName.SUBMIT_VSP);
    }

    validationResponse.setLicensingDataErrors(validateLicensingData(vspDetails));
    validationResponse
        .setUploadDataErrors(validateUploadData(uploadData, vspDetails),
            LoggerServiceName.Submit_VSP,
            LoggerTragetServiceName.SUBMIT_VSP);

    validationResponse.setQuestionnaireValidationResult(
        validateQuestionnaire(vspDetails.getId(), vspDetails.getVersion(), vspDetails
            .getOnboardingMethod()));

    if ("Manual".equals(vspDetails.getOnboardingMethod())) {
      Collection<ErrorCode> deploymentFlavourValidationErrList =
          deploymentFlavorValidation(vspDetails.getId(), vspDetails.getVersion());
      if (validationResponse.getVspErrors() != null) {
        if (deploymentFlavourValidationErrList != null) {
          validationResponse.getVspErrors().addAll(deploymentFlavourValidationErrList);
        }
      } else {
        validationResponse
            .setVspErrors(deploymentFlavourValidationErrList, LoggerServiceName.Submit_VSP,
                LoggerTragetServiceName.SUBMIT_VSP);
      }

      Set<CompositionEntityValidationData> compositionEntityValidationData =
          componentValidation(vspDetails.getId(), vspDetails.getVersion());
      if (validationResponse.getQuestionnaireValidationResult() != null) {
        if (!CollectionUtils.isEmpty(compositionEntityValidationData)) {
          validationResponse.getQuestionnaireValidationResult().getValidationData()
              .addAll(compositionEntityValidationData);
        }
      } else {
        validationResponse.setQuestionnaireValidationResult(
            CollectionUtils.isEmpty(compositionEntityValidationData) ? null :
                new QuestionnaireValidationResult(compositionEntityValidationData));
      }

      //Generate Tosca service model for Manual Onboarding flow
      VspModelInfo vspModelInfo = manualVspToscaManager.gatherVspInformation(vspId, version, user);
      serviceModel = manualVspToscaManager.generateToscaModel(vspModelInfo);
    }
    validationResponse.setCompilationErrors(
        compile(vspId, vspDetails.getVersion(), serviceModel),
        LoggerServiceName.Submit_VSP, LoggerTragetServiceName.SUBMIT_VSP);

    if (validationResponse.isValid()) {
      Version newVersion = versioningManager.submit(
          VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
          vspId, user, null);
      ActivityLogEntity activityLogEntity = new ActivityLogEntity(vspDetails.getId(), String
          .valueOf(newVersion.getMajor()),
          ActivityType.SUBMIT.toString(), user, true, "", "");
      activityLogManager.addActionLog(activityLogEntity, user);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return validationResponse;
  }

  private boolean isCyclicDependencyInComponents(String vendorSoftwareProductId,
                                                 Version version) {
    final Collection<ComponentDependencyModelEntity> componentDependencyModelEntities =
        vendorSoftwareProductDao.listComponentDependencies(vendorSoftwareProductId, version);
    ComponentDependencyTracker dependencyTracker = new ComponentDependencyTracker();

    for (ComponentDependencyModelEntity entity : componentDependencyModelEntities) {
      dependencyTracker.addDependency(entity.getSourceComponentId(), entity.getTargetComponentId());
    }
    return dependencyTracker.isCyclicDependencyPresent();
  }

  private Collection<ErrorCode> deploymentFlavorValidation(String vspId,
                                                           Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);
    Set<CompositionEntityValidationData> validationData = new HashSet<>();
    Collection<ErrorCode> errorCodeList = new ArrayList<>();
    Collection<DeploymentFlavorEntity> deploymentFlavors =
        vendorSoftwareProductDao.listDeploymentFlavors(vspId, version);
    if (!CollectionUtils.isEmpty(deploymentFlavors)) {
      deploymentFlavors.forEach(deploymentFlavor -> {
        DeploymentFlavorEntity deployment = vendorSoftwareProductDao.getDeploymentFlavor(vspId,
            version, deploymentFlavor.getId());
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
        vendorSoftwareProductDao.listComponents(vspId, version);
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
    Collection<ImageEntity> images = vendorSoftwareProductDao.listImages(vspId, version,
        component.getId());
    if (CollectionUtils.isEmpty(images)) {
      CompositionEntityValidationData compositionEntityValidationData = new
          CompositionEntityValidationData(component.getType(), component.getId());
      compositionEntityValidationData.setEntityName(component.getComponentCompositionData()
          .getDisplayName());
      ErrorCode vfcMissingImageErrorBuilder =
          ComponentErrorBuilder.VfcMissingImageErrorBuilder();
      List<String> errors = new ArrayList<>();
      errors.add(vfcMissingImageErrorBuilder.message());
      compositionEntityValidationData.setErrors(errors);
      validationData.add(compositionEntityValidationData);
    }
  }


  private List<ErrorCode> validateCompletedVendorSoftwareProduct(
      VspDetails vspDetails, UploadDataEntity uploadData, Object serviceModel) {

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
    if ("Manual".equals(vspDetails.getOnboardingMethod())) {
      //Manual Onboarding specific validations
      Collection<DeploymentFlavorEntity> deploymentFlavorEntities = vendorSoftwareProductDao
          .listDeploymentFlavors(vspDetails.getId(), vspDetails.getVersion());
      if (CollectionUtils.isEmpty(deploymentFlavorEntities)) {
        ErrorCode vspMissingDeploymentFlavorErrorBuilder =
            VendorSoftwareProductInvalidErrorBuilder.VspMissingDeploymentFlavorErrorBuilder();
        errors.add(vspMissingDeploymentFlavorErrorBuilder);
      }
      errors.addAll(validateMandatoryLicenseFields(vspDetails));
    } else {
      //Heat flow specific VSP validations
      if (uploadData == null || uploadData.getContentData() == null || serviceModel == null) {
        errors.add(VendorSoftwareProductInvalidErrorBuilder
            .VendorSoftwareProductMissingServiceModelErrorBuilder(vspDetails.getId(),
                vspDetails.getVersion()));
      }
      if (vspDetails.getVlmVersion() != null || vspDetails.getLicenseAgreement() != null
          || vspDetails.getFeatureGroups() != null) {
        errors.addAll(validateMandatoryLicenseFields(vspDetails));
      }
    }
    return errors.isEmpty() ? null : errors;
  }

  private List<ErrorCode> validateMandatoryLicenseFields(VspDetails vspDetails) {
    List<ErrorCode> errors = new ArrayList<>();
    if (vspDetails.getVlmVersion() == null) {
      errors.add(createMissingMandatoryFieldError(
          "licensing version (in the format of: {integer}.{integer})"));
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

  String getVspQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(null);
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(null);
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.vsp, schemaInput);
  }

  private static void sortVspListByModificationTimeDescOrder(
      List<VersionedVendorSoftwareProductInfo> vsps) {
    vsps.sort((o1, o2) -> o2.getVspDetails().getWritetimeMicroSeconds()
        .compareTo(o1.getVspDetails().getWritetimeMicroSeconds()));
  }


  private Map<String, List<ErrorMessage>> compile(String vendorSoftwareProductId, Version version,
                                                  ToscaServiceModel serviceModel) {
    if (serviceModel == null) {
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
  public String fetchValidationVsp(String user) {
    try {
      validateUniqueName(VALIDATION_VSP_NAME);
    } catch (Exception ignored) {
      LOGGER.debug("Ignored exception when validating unique VSP name", ignored);
      return VALIDATION_VSP_ID;
    }
    VspDetails validationVsp = new VspDetails();
    validationVsp.setName(VALIDATION_VSP_NAME);

    vspInfoDao.create(validationVsp);
    Version version = versioningManager.create(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        validationVsp.getId(), user);
    validationVsp.setVersion(version);

    createUniqueName(VALIDATION_VSP_NAME);
    VALIDATION_VSP_ID = validationVsp.getId();
    return VALIDATION_VSP_ID;
  }

  @Override
  public VspDetails createVsp(VspDetails vspDetails, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(null);

    validateUniqueName(vspDetails.getName());

    vspDetails.setOnboardingOrigin(OnboardingTypesEnum.NONE.toString());

    vspInfoDao.create(vspDetails);//id will be set in the dao
    vspInfoDao.updateQuestionnaireData(vspDetails.getId(), null,
        new JsonSchemaDataGenerator(getVspQuestionnaireSchema(null)).generateData());

    Version version = versioningManager
        .create(VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
            vspDetails.getId(), user);
    vspDetails.setVersion(version);
    ActivityLogEntity activityLogEntity = new ActivityLogEntity(vspDetails.getId(), String
        .valueOf(vspDetails.getVersion().getMajor() + 1),
        ActivityType.CREATE_NEW.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);
    String vspName = vspDetails.getName();
    createUniqueName(vspName);
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(null);
    return vspDetails;
  }

  @Override
  public List<VersionedVendorSoftwareProductInfo> listVsps(String versionFilter, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(null);

    Map<String, VersionInfo> idToVersionsInfo = versioningManager.listEntitiesVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, user,
        VersionableEntityAction.Read);

    List<VersionedVendorSoftwareProductInfo> vsps = new ArrayList<>();
    for (Map.Entry<String, VersionInfo> entry : idToVersionsInfo.entrySet()) {
      VersionInfo versionInfo = entry.getValue();
      if (versionFilter != null && versionFilter.equals(VersionStatus.Final.name())) {
        if (versionInfo.getLatestFinalVersion() == null) {
          continue;
        }
        versionInfo.setActiveVersion(versionInfo.getLatestFinalVersion());
        versionInfo.setStatus(VersionStatus.Final);
        versionInfo.setLockingUser(null);
      }

      Version version = versionInfo.getActiveVersion();
      if (user.equals(versionInfo.getLockingUser())) {
        version.setStatus(VersionStatus.Locked);
      }
      try {
        VspDetails vsp = vspInfoDao.get(new VspDetails(entry.getKey(), version));
        if (vsp != null && !vsp.getId().equals(VALIDATION_VSP_ID)) {
          vsp.setValidationDataStructure(null);
          vsps.add(new VersionedVendorSoftwareProductInfo(vsp, versionInfo));
        }
      } catch (RuntimeException rte) {
        LOGGER.error(
            "Error trying to retrieve vsp[" + entry.getKey() + "] version[" + version.toString
                () + "] " +
                "message:" + rte.getMessage(), rte);
      }
    }

    sortVspListByModificationTimeDescOrder(vsps);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(null);

    return vsps;
  }

  @Override
  public void updateVsp(VspDetails vspDetails, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspDetails.getId());

    VspDetails retrieved = vspInfoDao.get(vspDetails);
    if (!Objects.equals(retrieved.getOnboardingMethod(), vspDetails.getOnboardingMethod())) {
      final ErrorCode onboardingMethodUpdateErrorCode = OnboardingMethodErrorBuilder
          .getOnboardingUpdateError();

      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPDATE_VSP, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), onboardingMethodUpdateErrorCode.message());

      throw new CoreException(onboardingMethodUpdateErrorCode);
    }

    //If any existing feature group is removed from VSP which is also associated in DF then
    //update DF to remove feature group associations.
    updateDeploymentFlavor(vspDetails, user);

    updateUniqueName(retrieved.getName(), vspDetails.getName());
    vspDetails.setOldVersion(retrieved.getOldVersion());

    vspInfoDao.update(vspDetails);
    //vendorSoftwareProductDao.updateVspLatestModificationTime(vspDetails.getId(), activeVersion);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspDetails.getId());
  }

  private void updateDeploymentFlavor(VspDetails vspDetails, String user) {
    final List<String> featureGroups = vspDetails.getFeatureGroups();
    if (featureGroups != null) {
      final Collection<DeploymentFlavorEntity> deploymentFlavorEntities = deploymentFlavorDao
          .list(new DeploymentFlavorEntity(vspDetails.getId(), vspDetails
              .getVersion(), null));
      if (Objects.nonNull(deploymentFlavorEntities)) {
        deploymentFlavorEntities.forEach(deploymentFlavorEntity -> {
          final String featureGroupId =
              deploymentFlavorEntity.getDeploymentFlavorCompositionData().getFeatureGroupId();
          if (!featureGroups.contains(featureGroupId)) {
            DeploymentFlavor deploymentFlavorCompositionData =
                deploymentFlavorEntity.getDeploymentFlavorCompositionData();
            deploymentFlavorCompositionData.setFeatureGroupId(null);
            deploymentFlavorEntity.setDeploymentFlavorCompositionData
                (deploymentFlavorCompositionData);
            vendorSoftwareProductDao.updateDeploymentFlavor(deploymentFlavorEntity);
          }
        });
      }
    }
  }


  @Override
  public VspDetails getVsp(String vspId, Version version, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    VspDetails vsp = vspInfoDao.get(new VspDetails(vspId, version));
    if (vsp == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_VSP, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Requested VSP not found");
      throw new CoreException(new VendorSoftwareProductNotFoundErrorBuilder(vspId).build());
    }
    vsp.setValidationData(orchestrationTemplateDao.getValidationData(vspId, version));
    if (Objects.isNull(vsp.getOnboardingOrigin())) { //todo should this only be done for non-Manual?
      vsp.setOnboardingOrigin(OnboardingTypesEnum.ZIP.toString());
    }

    if (Objects.isNull(vsp.getNetworkPackageName())) {
      vsp.setNetworkPackageName("Upload File");
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return vsp;
  }

  @Override
  public Version callAutoHeal(String vspId, VersionInfo versionInfo,
                              VspDetails vendorSoftwareProductInfo, String user)
      throws Exception {
    switch (versionInfo.getStatus()) {
      case Locked:
        if (user.equals(versionInfo.getLockingUser())) {
          autoHeal(vspId, versionInfo.getActiveVersion(), vendorSoftwareProductInfo,
              versionInfo.getLockingUser());
        }
        return versionInfo.getActiveVersion();
      case Available:
        Version checkoutVersion = checkout(vspId, user);
        autoHeal(vspId, checkoutVersion, vendorSoftwareProductInfo, user);
        return checkin(vspId, user);
      case Final:
        return healAndAdvanceFinalVersion(vspId, vendorSoftwareProductInfo, user);
      default:
        //do nothing
        break;
    }
    return versionInfo.getActiveVersion();
  }

  public Version healAndAdvanceFinalVersion(String vspId, VspDetails vendorSoftwareProductInfo,
                                            String user) throws IOException {

    Version checkoutVersion = checkout(vspId, user);
    autoHeal(vspId, checkoutVersion, vendorSoftwareProductInfo, user);
    Version checkinVersion = checkin(vspId, user);

    ValidationResponse response = Objects.requireNonNull(submit(vspId, user),
        "Null response not expected");

    if (!response.isValid()) {
      return checkinVersion;
    }

    Version finalVersion = checkinVersion.calculateNextFinal();
    createPackage(vspId, finalVersion, user);
    return finalVersion;
  }

  @Override
  public void deleteVsp(String vspId, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
        LoggerTragetServiceName.DELETE_VSP, ErrorLevel.ERROR.name(),
        LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Unsupported operation");
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);

    throw new UnsupportedOperationException(
        VendorSoftwareProductConstants.UNSUPPORTED_OPERATION_ERROR);
  }

  @Override
  public void heal(String vspId, Version version, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    VersionInfo versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);

    version = VersionStatus.Locked.equals(versionInfo.getStatus())
        ? versionInfo.getActiveVersion()
        : checkout(vspId, user);
    version.setStatus(VersionStatus.Locked);

    Optional<String> errorMessages =
        healingManager.healAll(getHealingParamsAsMap(vspId, version, user));

    VspDetails vspDetails = new VspDetails(vspId, version);
    vspDetails.setOldVersion(null);
    vspInfoDao.updateOldVersionIndication(vspDetails);

    LOGGER.audit("Healed VSP " + vspDetails.getId());
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);

    errorMessages.ifPresent(s -> {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder().withId("HEALING_ERROR")
              .withCategory(ErrorCategory.APPLICATION).withMessage(s).build());
    });
  }

  private void autoHeal(String vspId, Version checkoutVersion, VspDetails vspDetails, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    checkoutVersion.setStatus(VersionStatus.Locked);
    Map<String, Object> healingParams = getHealingParamsAsMap(vspId, checkoutVersion, user);

    Optional<String> errorMessages = healingManager.healAll(healingParams);

    vspDetails.setVersion(checkoutVersion);
    vspDetails.setOldVersion(null);
    vspInfoDao.updateOldVersionIndication(vspDetails);

    LOGGER.audit("Healed VSP " + vspDetails.getName());
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);

    errorMessages.ifPresent(s -> {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder().withId("HEALING_ERROR")
              .withCategory(ErrorCategory.APPLICATION).withMessage(s).build());
    });
  }

  private Map<String, Object> getHealingParamsAsMap(String vspId, Version version, String user) {
    Map<String, Object> healingParams = new HashMap<>();

    healingParams.put(SdcCommon.VSP_ID, vspId);
    healingParams.put(SdcCommon.VERSION, version);
    healingParams.put(SdcCommon.USER, user);

    return healingParams;
  }

  @Override
  public List<PackageInfo> listPackages(String category, String subCategory) {
    return packageInfoDao.listByCategory(category, subCategory);
  }

  @Override
  public File getTranslatedFile(String vspId, Version version, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);
    String errorMessage;
    if (version == null) {
      errorMessage = "Package not found";
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_TRANSLATED_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), errorMessage);
      throw new CoreException(new PackageNotFoundErrorBuilder(vspId).build());
    } else if (!version.isFinal()) {
      errorMessage = "Invalid requested version";
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_VERSION_INFO, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), errorMessage);
      throw new CoreException(new RequestedVersionInvalidErrorBuilder().build());
    }

    PackageInfo packageInfo =
        packageInfoDao.get(new PackageInfo(vspId, version));
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

  public byte[] getOrchestrationTemplateFile(String vspId, Version version, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    UploadDataEntity uploadData = orchestrationTemplateDao.getOrchestrationTemplate(vspId, version);
    ByteBuffer contentData = uploadData.getContentData();
    if (contentData == null) {
      return null;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (final ZipOutputStream zos = new ZipOutputStream(baos);
         ZipInputStream zipStream = new ZipInputStream(
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
  public PackageInfo createPackage(String vspId, Version version, String user) throws IOException {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    if (!version.isFinal()) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_PACKAGE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't create package");
      throw new CoreException(
          new CreatePackageForNonFinalVendorSoftwareProductErrorBuilder(vspId, version)
              .build());
    }

    ToscaServiceModel toscaServiceModel = enrichedServiceModelDao.getServiceModel(vspId, version);
    VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));
    Version vlmVersion = vspDetails.getVlmVersion();

    PackageInfo packageInfo = createPackageInfo(vspId, vspDetails);

    ToscaFileOutputServiceCsarImpl toscaServiceTemplateServiceCsar =
        new ToscaFileOutputServiceCsarImpl();
    FileContentHandler licenseArtifacts = licenseArtifactsService
        .createLicenseArtifacts(vspDetails.getId(), vspDetails.getVendorId(), vlmVersion,
            vspDetails.getFeatureGroups(), user);
    //todo add tosca validation here
    packageInfo.setTranslatedFile(ByteBuffer.wrap(
        toscaServiceTemplateServiceCsar.createOutputFile(toscaServiceModel, licenseArtifacts)));

    packageInfoDao.create(packageInfo);

    LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.CREATE_PACKAGE + vspId);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
    return packageInfo;
  }

  private PackageInfo createPackageInfo(String vspId, VspDetails vspDetails) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.setVspId(vspId);
    packageInfo.setVersion(vspDetails.getVersion());
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

  public QuestionnaireResponse getVspQuestionnaire(String vspId, Version version, String user) {
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
  public void updateVspQuestionnaire(String vspId, Version version, String questionnaireData,
                                     String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id", vspId);

    vspInfoDao.updateQuestionnaireData(vspId, version, questionnaireData);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id", vspId);
  }


  private Map<String, List<ErrorMessage>> validateUploadData(UploadDataEntity uploadData,
                                                             VspDetails vspDetails)
      throws IOException {

    Map<String, List<ErrorMessage>> validationErrors = new HashMap<>();
    if (uploadData == null || uploadData.getContentData() == null) {
      return null;
    }

    FileContentHandler fileContentMap =
        CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.getOnboardingTypesEnum
                (vspDetails.getOnboardingOrigin()),
            uploadData.getContentData().array());

    if (CommonUtil.isFileOriginFromZip(vspDetails.getOnboardingOrigin())) {
      ValidationManager validationManager =
          ValidationManagerUtil.initValidationManager(fileContentMap);
      validationErrors.putAll(validationManager.validate());
    }

    return
        MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, validationErrors))
            ? null : validationErrors;
  }

  private VersionInfo getVersionInfo(String vendorSoftwareProductId, VersionableEntityAction action,
                                     String user) {
    return versioningManager.getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        vendorSoftwareProductId, user, action);
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

    Collection<NicEntity> nics = vendorSoftwareProductDao.listNicsByVsp(vspId, version);

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
        vendorSoftwareProductDao.listComponentsCompositionAndQuestionnaire(vspId, version);
    components.forEach(component -> compositionEntityDataManager.addEntity(component,
        new ComponentQuestionnaireSchemaInput(nicNamesByComponent.get(component.getId()),
            JsonUtil.json2Object(component.getQuestionnaireData(), Map.class))));

    Collection<ComputeEntity> computes = vendorSoftwareProductDao.listComputesByVsp(vspId, version);
    computes.forEach(compute -> compositionEntityDataManager.addEntity(compute, null));

    if ("Manual".equals(onboardingMethod)) {
      Collection<ImageEntity> images = vendorSoftwareProductDao.listImagesByVsp(vspId, version);
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
  public File getInformationArtifact(String vspId, Version version, String user) {
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

  void validateUniqueName(String vspName) {
    UniqueValueUtil.validateUniqueValue(
        VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME, vspName);
  }

  void createUniqueName(String vspName) {
    UniqueValueUtil.createUniqueValue(
        VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME, vspName);
  }

  void updateUniqueName(String oldVspName, String newVspName) {
    UniqueValueUtil.updateUniqueValue(
        VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
        oldVspName, newVspName);
  }

  @Override
  public Collection<ComputeEntity> getComputeByVsp(String vspId, Version version,
                                                   String user) {
    return vendorSoftwareProductDao.listComputesByVsp(vspId, version);
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
