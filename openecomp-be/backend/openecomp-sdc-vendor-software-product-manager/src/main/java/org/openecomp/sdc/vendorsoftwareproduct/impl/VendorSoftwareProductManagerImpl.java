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
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ValidationErrorBuilder;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.licenseartifacts.VendorLicenseArtifactsService;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.VendorSoftwareProductNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CreatePackageForNonFinalVendorSoftwareProductErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.FileCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.InformationArtifactCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.PackageInvalidErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.PackageNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.TranslationFileCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductInvalidErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireValidationResult;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
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
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class VendorSoftwareProductManagerImpl implements VendorSoftwareProductManager {
  private static final String VALIDATION_VSP_ID = "validationOnlyVspId";
  private static final String VALIDATION_VSP_NAME = "validationOnlyVspName";
  //private static final String VALIDATION_VSP_USER = "validationOnlyVspUser";

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final Logger logger =
      LoggerFactory.getLogger(VendorSoftwareProductManagerImpl.class);

  private OrchestrationTemplateDao orchestrationTemplateDao;
  private VendorSoftwareProductInfoDao vspInfoDao;
  private VersioningManager versioningManager;
  private VendorSoftwareProductDao vendorSoftwareProductDao;
  private VendorLicenseFacade vendorLicenseFacade;
  private ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao;
  private EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> enrichedServiceModelDao;
  private HealingManager healingManager;
  private VendorLicenseArtifactsService licenseArtifactsService;
  private CompositionEntityDataManager compositionEntityDataManager;
  private InformationArtifactGenerator informationArtifactGenerator;
  private PackageInfoDao packageInfoDao;
  private ActivityLogManager activityLogManager;


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
      CompositionEntityDataManager compositionEntityDataManager,
      InformationArtifactGenerator informationArtifactGenerator,
      PackageInfoDao packageInfoDao,
      ActivityLogManager activityLogManager) {
    this.versioningManager = versioningManager;
    this.vendorSoftwareProductDao = vendorSoftwareProductDao;
    this.orchestrationTemplateDao = orchestrationTemplateDataDao;
    this.vspInfoDao = vspInfoDao;
    this.vendorLicenseFacade = vendorLicenseFacade;
    this.serviceModelDao = serviceModelDao;
    this.enrichedServiceModelDao = enrichedServiceModelDao;
    this.healingManager = healingManager;
    this.licenseArtifactsService = licenseArtifactsService;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.informationArtifactGenerator = informationArtifactGenerator;
    this.packageInfoDao = packageInfoDao;
    this.activityLogManager = activityLogManager;

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
    mdcDataDebugMessage.debugEntryMessage("VSP id", vendorSoftwareProductId);
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

    mdcDataDebugMessage.debugExitMessage("VSP id", vendorSoftwareProductId);
    return newVersion;
  }


  @Override
  public Version undoCheckout(String vendorSoftwareProductId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vendorSoftwareProductId);

    Version version =
        getVersionInfo(vendorSoftwareProductId, VersionableEntityAction.Read, user)
            .getActiveVersion();
    String preVspName = vspInfoDao
        .get(new VspDetails(vendorSoftwareProductId, version)).getName();

    Version newVersion = versioningManager.undoCheckout(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        vendorSoftwareProductId, user);

    String postVspName = vspInfoDao
        .get(new VspDetails(vendorSoftwareProductId, newVersion))
        .getName();

    updateUniqueName(preVspName, postVspName);

    mdcDataDebugMessage.debugExitMessage("VSP id", vendorSoftwareProductId);

    return newVersion;
  }

  @Override
  public Version checkin(String vendorSoftwareProductId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vendorSoftwareProductId);

    Version newVersion = versioningManager.checkin(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        vendorSoftwareProductId, user, null);

    if (newVersion != null) {
      ActivityLogEntity activityLogEntity =
          new ActivityLogEntity(vendorSoftwareProductId, String.valueOf(newVersion.getMajor() + 1),
              ActivityType.CHECKIN.toString(), user, true, "", "");
      activityLogManager.addActionLog(activityLogEntity, user);
    }

    mdcDataDebugMessage.debugExitMessage("VSP id", vendorSoftwareProductId);

    return newVersion;
  }

  @Override
  public ValidationResponse submit(String vspId, String user) throws IOException {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

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
        .setUploadDataErrors(validateUploadData(uploadData), LoggerServiceName.Submit_VSP,
            LoggerTragetServiceName.SUBMIT_VSP);
    validationResponse.setQuestionnaireValidationResult(
        validateQuestionnaire(vspDetails.getId(), vspDetails.getVersion()));

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

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);

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

  private static List<ErrorCode> validateCompletedVendorSoftwareProduct(
      VspDetails vspDetails, UploadDataEntity uploadData, Object serviceModel) {

    List<ErrorCode> errros = new ArrayList<>();

    if (vspDetails.getName() == null) {
      errros.add(createMissingMandatoryFieldError("name"));
    }
    if (vspDetails.getDescription() == null) {
      errros.add(createMissingMandatoryFieldError("description"));
    }
    if (vspDetails.getVendorId() == null) {
      errros.add(createMissingMandatoryFieldError("vendor Id"));
    }
    if (vspDetails.getVlmVersion() == null) {
      errros.add(createMissingMandatoryFieldError(
          "licensing version (in the format of: {integer}.{integer})"));
    }
    if (vspDetails.getCategory() == null) {
      errros.add(createMissingMandatoryFieldError("category"));
    }
    if (vspDetails.getSubCategory() == null) {
      errros.add(createMissingMandatoryFieldError("sub category"));
    }
    if (vspDetails.getLicenseAgreement() == null) {
      errros.add(createMissingMandatoryFieldError("license agreement"));
    }
    if (CollectionUtils.isEmpty(vspDetails.getFeatureGroups())) {
      errros.add(createMissingMandatoryFieldError("feature groups"));
    }
    if (uploadData == null || uploadData.getContentData() == null || serviceModel == null) {
      errros.add(
          new VendorSoftwareProductInvalidErrorBuilder(vspDetails.getId(), vspDetails.getVersion())
              .build());
    }

    return errros.isEmpty() ? null : errros;
  }

  private static ErrorCode createMissingMandatoryFieldError(String fieldName) {
    return new ValidationErrorBuilder("must be supplied", fieldName).build();
  }

  String getVspQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    mdcDataDebugMessage.debugEntryMessage(null);
    mdcDataDebugMessage.debugExitMessage(null);
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

    enrichedServiceModelDao
        .storeServiceModel(vendorSoftwareProductId, version, enrichmentManager.getModel());

    return enrichErrors;
  }

  private Collection<ErrorCode> validateLicensingData(VspDetails vspDetails) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspDetails.getId());

    if (vspDetails.getVendorId() == null || vspDetails.getVlmVersion() == null
        || vspDetails.getLicenseAgreement() == null
        || CollectionUtils.isEmpty(vspDetails.getFeatureGroups())) {
      return null;
    }

    mdcDataDebugMessage.debugExitMessage("VSP id", vspDetails.getId());
    return vendorLicenseFacade
        .validateLicensingData(vspDetails.getVendorId(), vspDetails.getVlmVersion(),
            vspDetails.getLicenseAgreement(), vspDetails.getFeatureGroups());
  }

  @Override
  public String fetchValidationVsp(String user) {
    try {
      validateUniqueName(VALIDATION_VSP_NAME);
    } catch (Exception ignored) {
      return VALIDATION_VSP_ID;
    }
    VspDetails validationVsp = new VspDetails();
    validationVsp.setName(VALIDATION_VSP_NAME);
    validationVsp.setId(VALIDATION_VSP_ID);
    Version version = versioningManager.create(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        validationVsp.getId(),
        user);
    validationVsp.setVersion(version);

    vspInfoDao.create(validationVsp);
    createUniqueName(VALIDATION_VSP_NAME);
    return VALIDATION_VSP_ID;
  }

  @Override
  public VspDetails createVsp(VspDetails vspDetails, String user) {
    mdcDataDebugMessage.debugEntryMessage(null);

    validateUniqueName(vspDetails.getName());

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
    mdcDataDebugMessage.debugExitMessage(null);
    return vspDetails;
  }

  @Override
  public List<VersionedVendorSoftwareProductInfo> listVsps(String versionFilter, String user) {
    mdcDataDebugMessage.debugEntryMessage(null);

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
      }catch(RuntimeException rte){
        logger.error("Error trying to retrieve vsp["+entry.getKey()+"] version["+version.toString
            ()+"] " +
            "message:"+rte
            .getMessage());
      }
    }

    sortVspListByModificationTimeDescOrder(vsps);

    mdcDataDebugMessage.debugExitMessage(null);

    return vsps;
  }

  @Override
  public void updateVsp(VspDetails vspDetails, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspDetails.getId());

    VspDetails retrieved = vspInfoDao.get(vspDetails);

    updateUniqueName(retrieved.getName(), vspDetails.getName());
    vspDetails.setOldVersion(retrieved.getOldVersion());

    vspInfoDao.update(vspDetails);
    //vendorSoftwareProductDao.updateVspLatestModificationTime(vspDetails.getId(), activeVersion);

    mdcDataDebugMessage.debugExitMessage("VSP id", vspDetails.getId());
  }


  @Override
  public VspDetails getVsp(String vspId, Version version, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    VspDetails vsp = vspInfoDao.get(new VspDetails(vspId, version));
    if (vsp == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_VSP, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Requested VSP not found");
      throw new CoreException(new VendorSoftwareProductNotFoundErrorBuilder(vspId).build());
    }
    vsp.setValidationData(orchestrationTemplateDao.getValidationData(vspId, version));

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
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
        Version checkoutFinalVersion = checkout(vspId,user);
        autoHeal(vspId, checkoutFinalVersion, vendorSoftwareProductInfo, user);
        Version checkinFinalVersion = checkin(vspId,user);
        ValidationResponse response = submit(vspId, user);
        if(!response.isValid()) {
          return checkout(vspId, user);
        }

        try {
          Version finalVersion = checkinFinalVersion.calculateNextFinal();
          createPackage(vspId, finalVersion, user);
          return finalVersion;
        } catch (IOException e) {
          throw new Exception(e.getMessage());
        }
    }
    return versionInfo.getActiveVersion();
  }

  @Override

  public void deleteVsp(String vspId, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
        LoggerTragetServiceName.DELETE_VSP, ErrorLevel.ERROR.name(),
        LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Unsupported operation");
    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);

    throw new UnsupportedOperationException(
        VendorSoftwareProductConstants.UNSUPPORTED_OPERATION_ERROR);
  }

  @Override
  public void heal(String vspId, Version version, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    VersionInfo versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);

    version = VersionStatus.Locked.equals(versionInfo.getStatus())
        ? versionInfo.getActiveVersion()
        : checkout(vspId, user);
    version.setStatus(VersionStatus.Locked);

    healingManager.healAll(getHealingParamsAsMap(vspId, version, user));

    VspDetails vspDetails = new VspDetails(vspId, version);
    vspDetails.setOldVersion(null);
    vspInfoDao.updateOldVersionIndication(vspDetails);

    logger.audit("Healed VSP " + vspDetails.getId());
    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
  }

  private void autoHeal(String vspId, Version checkoutVersion, VspDetails vspDetails, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    checkoutVersion.setStatus(VersionStatus.Locked);
    Map<String, Object> healingParams = getHealingParamsAsMap(vspId, checkoutVersion, user);
    healingManager.healAll(healingParams);
    vspDetails.setVersion(checkoutVersion);
    vspDetails.setOldVersion(null);
    vspInfoDao.updateOldVersionIndication(vspDetails);

    logger.audit("Healed VSP " + vspDetails.getName());

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
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
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);
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

    try {
      FileOutputStream fos = new FileOutputStream(translatedFile);
      fos.write(translatedFileBuffer.array());
      fos.close();
    } catch (IOException exception) {
      errorMessage = "Can't create package";
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_TRANSLATED_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), errorMessage);
      throw new CoreException(new TranslationFileCreationErrorBuilder(vspId, version).build(),
          exception);
    }

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);

    return translatedFile;
  }

  @Override

  public byte[] getOrchestrationTemplateFile(String vspId, Version version, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

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

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
    return baos.toByteArray();
  }

  @Override
  public PackageInfo createPackage(String vspId, Version version, String user) throws IOException {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

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

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
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
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    VspQuestionnaireEntity retrieved = vspInfoDao.getQuestionnaire(vspId, version);
    VersioningUtil.validateEntityExistence(retrieved, new VspQuestionnaireEntity(vspId, version),
        VspDetails.ENTITY_TYPE);

    String questionnaireData = retrieved.getQuestionnaireData();

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setData(questionnaireData);
    questionnaireResponse.setSchema(getVspQuestionnaireSchema(null));

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);

    return questionnaireResponse;
  }

  @Override
  public void updateVspQuestionnaire(String vspId, Version version, String questionnaireData,
                                     String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);

    vspInfoDao.updateQuestionnaireData(vspId, version, questionnaireData);

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
  }


  private Map<String, List<ErrorMessage>> validateUploadData(UploadDataEntity uploadData)
      throws IOException {
    if (uploadData == null || uploadData.getContentData() == null) {
      return null;
    }

    FileContentHandler fileContentMap =
        CommonUtil.loadUploadFileContent(uploadData.getContentData().array());
    //todo - check
    ValidationManager validationManager =
        ValidationManagerUtil.initValidationManager(fileContentMap);
    Map<String, List<ErrorMessage>> validationErrors = validationManager.validate();

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


  private QuestionnaireValidationResult validateQuestionnaire(String vspId, Version version) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);


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

    Map<CompositionEntityId, Collection<String>> errorsByEntityId =
        compositionEntityDataManager.validateEntitiesQuestionnaire();
    if (MapUtils.isNotEmpty(errorsByEntityId)) {
      compositionEntityDataManager.buildTrees();
      compositionEntityDataManager.addErrorsToTrees(errorsByEntityId);
/*      Set<CompositionEntityValidationData> entitiesWithValidationErrors =
          compositionEntityDataManager.getEntityListWithErrors();*/
      //Collection<CompositionEntityValidationData> roots = compositionEntityDataManager.getTrees();

      mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
      return new QuestionnaireValidationResult(
          compositionEntityDataManager.getAllErrorsByVsp(vspId));
    }

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
    return null;
  }

  @Override
  public File getInformationArtifact(String vspId, Version version, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);
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
      OutputStream out = new BufferedOutputStream(new FileOutputStream(infoArtifactFile));
      out.write(infoArtifactAsByteBuffer.array());
      out.close();
    } catch (IOException e) {
      throw new CoreException(new InformationArtifactCreationErrorBuilder(vspId).build(), e);
    }

    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);
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
}
