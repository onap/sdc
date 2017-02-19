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

import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.CSAR;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.GENERAL_COMPONENT_ID;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UPLOAD_RAW_DATA;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.VSP_PACKAGE_ZIP;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.enrichment.EnrichmentArtifactsServiceFactory;
import org.openecomp.core.enrichment.api.EnrichmentManager;
import org.openecomp.core.enrichment.enrichmentartifacts.EnrichmentArtifactsService;
import org.openecomp.core.enrichment.factory.EnrichmentManagerFactory;
import org.openecomp.core.enrichment.types.ComponentArtifactType;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonSchemaDataGenerator;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.types.MessageContainerUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.ValidationErrorBuilder;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.impl.tosca.ComponentInfo;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.validation.utils.ValidationManagerUtil;
import org.openecomp.sdc.vendorlicense.VendorLicenseArtifactServiceFactory;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorlicense.licenseartifacts.VendorLicenseArtifactsService;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentArtifactEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessArtifactEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CreatePackageForNonFinalVendorSoftwareProductErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.FileCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.MibUploadErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.PackageInvalidErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.PackageNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.TranslationFileCreationErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.UploadInvalidErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductInvalidErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.services.CompositionDataExtractor;
import org.openecomp.sdc.vendorsoftwareproduct.services.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireValidationResult;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentQuestionnaireSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MibUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NetworkCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NicCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.vendorsoftwareproduct.util.CompilationUtil;
import org.openecomp.sdc.vendorsoftwareproduct.util.VendorSoftwareProductUtils;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.errors.RequestedVersionInvalidErrorBuilder;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type Vendor software product manager.
 */
public class VendorSoftwareProductManagerImpl implements VendorSoftwareProductManager {

  private static final String VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG =
      "Composition entities may not be created / deleted for Vendor Software Product whose "
          + "entities were uploaded";

  private static final VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();
  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static final VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();
  private static final ComponentArtifactDao componentArtifactDao =
      ComponentArtifactDaoFactory.getInstance().createInterface();
  private static final ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();
  private static final EnrichedServiceModelDao<ToscaServiceModel, ServiceElement>
      enrichedServiceModelDao = EnrichedServiceModelDaoFactory.getInstance().createInterface();
  private static VendorLicenseArtifactsService licenseArtifactsService =
      VendorLicenseArtifactServiceFactory.getInstance().createInterface();
  private static EnrichmentArtifactsService enrichmentArtifactsService =
      EnrichmentArtifactsServiceFactory.getInstance().createInterface();


  /**
   * Instantiates a new Vendor software product manager.
   */
  public VendorSoftwareProductManagerImpl() {
    vendorSoftwareProductDao.registerVersioning(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
    serviceModelDao.registerVersioning(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
    enrichedServiceModelDao.registerVersioning(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
    componentArtifactDao.registerVersioning(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
  }

  private static List<ErrorCode> validateCompletedVendorSoftwareProduct(VspDetails vspDetails,
                                                                        UploadDataEntity uploadData,
                                                                        Object serviceModel) {
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

  private static String getVspQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.vsp, schemaInput);
  }

  private static String getComponentQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.component,
            schemaInput);
  }

  private static String getNicQuestionnaireSchema(SchemaTemplateInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.nic, schemaInput);
  }

  private static void sortVspListByModificationTimeDescOrder(
      List<VersionedVendorSoftwareProductInfo> vendorLicenseModels) {
    Collections.sort(vendorLicenseModels, new Comparator<VersionedVendorSoftwareProductInfo>() {
      @Override
      public int compare(VersionedVendorSoftwareProductInfo o1,
                         VersionedVendorSoftwareProductInfo o2) {
        return o2.getVspDetails().getWritetimeMicroSeconds()
            .compareTo(o1.getVspDetails().getWritetimeMicroSeconds());
      }
    });
  }

  private boolean isManual(String vspId, Version version) {
    return false;
  }

  @Override
  public Version checkout(String vendorSoftwareProductId, String user) {
    Version newVersion = versioningManager
        .checkout(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, vendorSoftwareProductId, user);
    vendorSoftwareProductDao.updateVspLatestModificationTime(vendorSoftwareProductId, newVersion);
    return newVersion;
  }

  @Override
  public Version undoCheckout(String vendorSoftwareProductId, String user) {
    Version newVersion = versioningManager
        .undoCheckout(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, vendorSoftwareProductId, user);
    vendorSoftwareProductDao.updateVspLatestModificationTime(vendorSoftwareProductId, newVersion);
    return newVersion;
  }

  @Override
  public Version checkin(String vendorSoftwareProductId, String user) {
    Version newVersion = versioningManager
        .checkin(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, vendorSoftwareProductId, user, null);
    vendorSoftwareProductDao.updateVspLatestModificationTime(vendorSoftwareProductId, newVersion);
    return newVersion;
  }

  @Override
  public ValidationResponse submit(String vendorSoftwareProductId, String user) throws IOException {
    VspDetails vspDetails = getVspDetails(vendorSoftwareProductId, null, user).getVspDetails();
    UploadDataEntity uploadData = vendorSoftwareProductDao
        .getUploadData(new UploadDataEntity(vendorSoftwareProductId, vspDetails.getVersion()));
    ToscaServiceModel serviceModel =
        serviceModelDao.getServiceModel(vendorSoftwareProductId, vspDetails.getVersion());
    Version newVersion = null;

    ValidationResponse validationResponse = new ValidationResponse();
    validationResponse
        .setVspErrors(validateCompletedVendorSoftwareProduct(vspDetails, uploadData, serviceModel));
    validationResponse.setLicensingDataErrors(validateLicensingData(vspDetails));
    validationResponse.setUploadDataErrors(validateUploadData(uploadData));
    validationResponse.setQuestionnaireValidationResult(
        validateQuestionnaire(vspDetails.getId(), vspDetails.getVersion()));
    validationResponse.setCompilationErrors(
        compile(vendorSoftwareProductId, vspDetails.getVersion(), serviceModel));

    if (validationResponse.isValid()) {
      newVersion = versioningManager
          .submit(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, vendorSoftwareProductId, user, null);
    }
    //vendorSoftwareProductDao.updateVspLatestModificationTime(vendorSoftwareProductId, newVersion);
    return validationResponse;
  }

  private Map<String, List<ErrorMessage>> compile(String vendorSoftwareProductId, Version version,
                                                  ToscaServiceModel serviceModel) {
    Collection<ComponentEntity> components = listComponents(vendorSoftwareProductId, version);
    if (serviceModel == null) {
      return null;
    }
    if (CollectionUtils.isEmpty(components)) {
      enrichedServiceModelDao.storeServiceModel(vendorSoftwareProductId, version, serviceModel);
      return null;
    }
    EnrichmentManager<ToscaServiceModel> enrichmentManager =
        EnrichmentManagerFactory.getInstance().createInterface();
    enrichmentManager.initInput(vendorSoftwareProductId, version);
    enrichmentManager.addModel(serviceModel);

    ComponentInfo componentInfo = new ComponentInfo();
    Map<String, List<ErrorMessage>> compileErrors = new HashMap<>();
    CompilationUtil.addMonitoringInfo(componentInfo, compileErrors);
    for (ComponentEntity componentEntity : components) {
      ComponentInfo currentEntityComponentInfo = new ComponentInfo();
      currentEntityComponentInfo.setCeilometerInfo(componentInfo.getCeilometerInfo());
      CompilationUtil
          .addMibInfo(vendorSoftwareProductId, version, componentEntity, currentEntityComponentInfo,
              compileErrors);
      enrichmentManager.addEntityInput(componentEntity.getComponentCompositionData().getName(),
          currentEntityComponentInfo);

    }
    Map<String, List<ErrorMessage>> enrichErrors;
    enrichErrors = enrichmentManager.enrich();
    enrichedServiceModelDao
        .storeServiceModel(vendorSoftwareProductId, version, enrichmentManager.getModel());
    if (enrichErrors != null) {
      compileErrors.putAll(enrichErrors);
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(vendorSoftwareProductId, version);

    return compileErrors;
  }

  private Collection<ErrorCode> validateLicensingData(VspDetails vspDetails) {
    if (vspDetails.getVendorId() == null || vspDetails.getVlmVersion() == null
        || vspDetails.getLicenseAgreement() == null
        || CollectionUtils.isEmpty(vspDetails.getFeatureGroups())) {
      return null;
    }
    return vendorLicenseFacade
        .validateLicensingData(vspDetails.getVendorId(), vspDetails.getVlmVersion(),
            vspDetails.getLicenseAgreement(), vspDetails.getFeatureGroups());
  }

  @Override
  public VspDetails createNewVsp(VspDetails vspDetails, String user) {
    UniqueValueUtil.validateUniqueValue(
        VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
        vspDetails.getName());
    vspDetails.setId(CommonMethods.nextUuId());

    //        vspDetails.setLastModificationTime(new Date());

    Version version = versioningManager
        .create(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, vspDetails.getId(), user);
    vspDetails.setVersion(version);

    //        vspDetails.setLastModificationTime(new Date());

    vendorSoftwareProductDao.createVendorSoftwareProductInfo(vspDetails);
    vendorSoftwareProductDao.updateQuestionnaire(vspDetails.getId(), version,
        new JsonSchemaDataGenerator(getVspQuestionnaireSchema(null)).generateData());
    UniqueValueUtil
        .createUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            vspDetails.getName());

    return vspDetails;
  }

  @Override
  public List<VersionedVendorSoftwareProductInfo> getVspList(String versionFilter, String user) {
    Map<String, VersionInfo> idToVersionsInfo = versioningManager
        .listEntitiesVersionInfo(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, user,
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

      VspDetails vsp = vendorSoftwareProductDao.getVendorSoftwareProductInfo(
          new VspDetails(entry.getKey(), entry.getValue().getActiveVersion()));
      if (vsp != null) {
        vsp.setValidationDataStructure(null);
        vsps.add(new VersionedVendorSoftwareProductInfo(vsp, entry.getValue()));
      }
    }

    sortVspListByModificationTimeDescOrder(vsps);
    return vsps;
  }

  @Override
  public void updateVsp(VspDetails vspDetails, String user) {
    Version activeVersion =
        getVersionInfo(vspDetails.getId(), VersionableEntityAction.Write, user).getActiveVersion();
    vspDetails.setVersion(activeVersion);
    //        vspDetails.setLastModificationTime(new Date());

    VspDetails retrieved = vendorSoftwareProductDao.getVendorSoftwareProductInfo(vspDetails);
    vspDetails.setValidationData(retrieved.getValidationData());
    UniqueValueUtil
        .updateUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            retrieved.getName(), vspDetails.getName());
    vendorSoftwareProductDao.updateVendorSoftwareProductInfo(vspDetails);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspDetails.getId(), activeVersion);
  }

  @Override
  public VersionedVendorSoftwareProductInfo getVspDetails(String vspId, Version version,
                                                          String user) {
    VersionInfo versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);
    if (version == null) {
      version = versionInfo.getActiveVersion();
    } else {
      if (!versionInfo.getViewableVersions().contains(version)) {
        throw new CoreException(new RequestedVersionInvalidErrorBuilder().build());
      }
    }

    VspDetails vendorSoftwareProductInfo =
        vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(vspId, version));
    if (vendorSoftwareProductInfo == null) {
      throw new CoreException(new VendorSoftwareProductNotFoundErrorBuilder(vspId).build());
    }
    return new VersionedVendorSoftwareProductInfo(vendorSoftwareProductInfo, versionInfo);
  }

  @Override
  public void deleteVsp(String vspId, String user) {
    throw new UnsupportedOperationException("Unsupported operation for 1607 release.");
  }

  @Override
  public UploadFileResponse uploadFile(String vspId, InputStream heatFileToUpload, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    UploadFileResponse uploadFileResponse = new UploadFileResponse();

    if (heatFileToUpload == null) {
      uploadFileResponse.addStructureError(AsdcCommon.UPLOAD_FILE,
          new ErrorMessage(ErrorLevel.ERROR,
              Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()));
      return uploadFileResponse;
    }

    InputStream uploadedFileData;
    FileContentHandler fileContentMap;
    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    try {
      fileContentMap = getContent(heatFileToUpload, errors);
      if (!errors.isEmpty()) {
        return addStructureErrorsToResponse(uploadFileResponse, errors);
      }

      uploadedFileData = fileContentMap.getFileContent(UPLOAD_RAW_DATA);
      fileContentMap.remove(UPLOAD_RAW_DATA);

      ValidationManagerUtil.handleMissingManifest(fileContentMap, errors);
      if (!errors.isEmpty()) {
        return addStructureErrorsToResponse(uploadFileResponse, errors);
      }

    } catch (CoreException ce) {
      ErrorMessage.ErrorMessageUtil.addMessage(AsdcCommon.UPLOAD_FILE, errors)
          .add(new ErrorMessage(ErrorLevel.ERROR, ce.getMessage()));
      return addStructureErrorsToResponse(uploadFileResponse, errors);
    }

    HeatStructureTree tree = createAndValidateHeatTree(uploadFileResponse, fileContentMap);

    deleteUploadDataAndContent(vspId, activeVersion);
    saveHotData(vspId, activeVersion, uploadedFileData, fileContentMap, tree);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);

    ToscaServiceModel toscaServiceModel =
        VendorSoftwareProductUtils.loadAndTranslateTemplateData(fileContentMap)
            .getToscaServiceModel();
    if (toscaServiceModel != null) {
      serviceModelDao.storeServiceModel(vspId, activeVersion, toscaServiceModel);
      saveCompositionData(vspId, activeVersion,
          CompositionDataExtractor.extractServiceCompositionData(toscaServiceModel));
    }

    return uploadFileResponse;
  }

  private UploadFileResponse addStructureErrorsToResponse(UploadFileResponse uploadFileResponse,
                                                          Map<String, List<ErrorMessage>> errors) {
    uploadFileResponse.addStructureErrors(errors);
    return uploadFileResponse;
  }

  private HeatStructureTree createAndValidateHeatTree(UploadFileResponse uploadFileResponse,
                                                      FileContentHandler fileContentMap) {
    VendorSoftwareProductUtils.addFileNamesToUploadFileResponse(fileContentMap, uploadFileResponse);
    Map<String, List<ErrorMessage>> validationErrors =
        ValidationManagerUtil.initValidationManager(fileContentMap).validate();
    uploadFileResponse.getErrors().putAll(validationErrors);

    HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(fileContentMap);
    heatTreeManager.createTree();
    heatTreeManager.addErrors(validationErrors);
    return heatTreeManager.getTree();
  }

  private void saveHotData(String vspId, Version activeVersion, InputStream uploadedFileData,
                           FileContentHandler fileContentMap, HeatStructureTree tree) {
    Map<String, Object> manifestAsMap = (Map<String, Object>) JsonUtil
        .json2Object(fileContentMap.getFileContent(AsdcCommon.MANIFEST_NAME), Map.class);

    UploadDataEntity uploadData = new UploadDataEntity(vspId, activeVersion);
    uploadData.setPackageName((String) manifestAsMap.get("name"));
    uploadData.setPackageVersion((String) manifestAsMap.get("version"));
    uploadData.setContentData(ByteBuffer.wrap(FileUtils.toByteArray(uploadedFileData)));
    uploadData.setValidationDataStructure(new ValidationStructureList(tree));
    vendorSoftwareProductDao.updateUploadData(uploadData);
  }

  private FileContentHandler getContent(InputStream heatFileToUpload,
                                        Map<String, List<ErrorMessage>> errors) {
    FileContentHandler contentMap = null;
    byte[] uploadedFileData;
    try {
      uploadedFileData = FileUtils.toByteArray(heatFileToUpload);
      VendorSoftwareProductUtils.validateRawZipData(uploadedFileData, errors);
      contentMap = VendorSoftwareProductUtils.loadUploadFileContent(uploadedFileData);
      VendorSoftwareProductUtils.validateContentZipData(contentMap, errors);
      contentMap.addFile(UPLOAD_RAW_DATA, uploadedFileData);
    } catch (IOException e0) {
      ErrorMessage.ErrorMessageUtil.addMessage(AsdcCommon.UPLOAD_FILE, errors)
          .add(new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_ZIP_FILE.getErrorMessage()));
    }
    return contentMap;
  }

  private void validateMibZipContent(String vspId, Version version, byte[] uploadedFileData,
                                     Map<String, List<ErrorMessage>> errors) {
    FileContentHandler contentMap;
    try {
      contentMap = VendorSoftwareProductUtils.loadUploadFileContent(uploadedFileData);
      VendorSoftwareProductUtils.validateContentZipData(contentMap, errors);
    } catch (IOException e0) {
      throw new CoreException(
          new MibUploadErrorBuilder(vspId, version, Messages.INVALID_ZIP_FILE.getErrorMessage())
              .build());
    }
  }

  @Override
  public List<PackageInfo> listPackages(String category, String subCategory) {
    return vendorSoftwareProductDao.listPackages(category, subCategory);
  }

  @Override
  public File getTranslatedFile(String vspId, Version version, String user) {
    VersionInfo versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);
    if (version == null) {
      if (versionInfo.getLatestFinalVersion() == null) {
        throw new CoreException(new PackageNotFoundErrorBuilder(vspId).build());
      }
      version = versionInfo.getLatestFinalVersion();
    } else {
      if (!version.isFinal() || !versionInfo.getViewableVersions().contains(version)) {
        throw new CoreException(new RequestedVersionInvalidErrorBuilder().build());
      }
    }

    PackageInfo packageInfo =
        vendorSoftwareProductDao.getPackageInfo(new PackageInfo(vspId, version));
    if (packageInfo == null) {
      throw new CoreException(new PackageNotFoundErrorBuilder(vspId, version).build());
    }

    ByteBuffer translatedFileBuffer = packageInfo.getTranslatedFile();
    if (translatedFileBuffer == null) {
      throw new CoreException(new PackageInvalidErrorBuilder(vspId, version).build());
    }

    File translatedFile = new File(VSP_PACKAGE_ZIP);

    try {
      FileOutputStream fos = new FileOutputStream(translatedFile);
      fos.write(translatedFileBuffer.array());
      fos.close();
    } catch (IOException e0) {
      throw new CoreException(new TranslationFileCreationErrorBuilder(vspId, version).build(), e0);
    }

    return translatedFile;
  }

  @Override
  public File getLatestHeatPackage(String vspId,
                                   String user) { //todo remove the writing to file system..
    VersionInfo versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);
    Version version = versionInfo.getActiveVersion();

    UploadDataEntity uploadData =
        vendorSoftwareProductDao.getUploadData(new UploadDataEntity(vspId, version));

    ByteBuffer contentData = uploadData.getContentData();
    if (contentData == null) {
      return null;
    }

    File heatPkgFile = new File(String.format("heats-for-%s.zip", vspId));

    try {
      FileOutputStream fos = new FileOutputStream(heatPkgFile);
      fos.write(contentData.array());
      fos.close();
    } catch (IOException e0) {
      throw new CoreException(new FileCreationErrorBuilder(vspId).build(), e0);
    }
    return heatPkgFile;
  }

  @Override
  public PackageInfo createPackage(String vspId, String user) throws IOException {
    VersionInfo versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);
    Version activeVersion = versionInfo.getActiveVersion();
    if (!activeVersion.isFinal()) {
      throw new CoreException(
          new CreatePackageForNonFinalVendorSoftwareProductErrorBuilder(vspId, activeVersion)
              .build());
    }

    ToscaServiceModel toscaServiceModel =
        enrichedServiceModelDao.getServiceModel(vspId, activeVersion);
    VspDetails vspDetails =
        vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(vspId, activeVersion));
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

    vendorSoftwareProductDao.insertPackageDetails(packageInfo);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, vspDetails.getVersion());

    return packageInfo;
  }

  private PackageInfo createPackageInfo(String vspId, VspDetails vspDetails) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.setVspId(vspId);
    packageInfo.setVersion(vspDetails.getVersion());
    packageInfo.setDisplayName(vspDetails.getPackageName());
    packageInfo.setVspName(vspDetails.getName());
    packageInfo.setVspDescription(vspDetails.getDescription());
    packageInfo.setCategory(vspDetails.getCategory());
    packageInfo.setSubCategory(vspDetails.getSubCategory());
    packageInfo.setVendorName(vspDetails.getVendorName());
    packageInfo.setPackageType(CSAR);
    packageInfo.setVendorRelease("1.0"); //todo TBD
    return packageInfo;
  }

  @Override
  public QuestionnaireResponse getVspQuestionnaire(String vspId, Version version, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setData(getVspQuestionnaire(vspId, version).getQuestionnaireData());
    questionnaireResponse.setSchema(getVspQuestionnaireSchema(null));

    return questionnaireResponse;
  }

  private VspQuestionnaireEntity getVspQuestionnaire(String vspId, Version version) {
    VspQuestionnaireEntity retrieved = vendorSoftwareProductDao.getQuestionnaire(vspId, version);
    VersioningUtil.validateEntityExistence(retrieved, new VspQuestionnaireEntity(vspId, version),
        VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public void updateVspQuestionnaire(String vspId, String questionnaireData, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);

    vendorSoftwareProductDao.updateQuestionnaire(vspId, activeVersion, questionnaireData);
  }

  @Override
  public Collection<NetworkEntity> listNetworks(String vspId, Version version, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    return listNetworks(vspId, version);
  }

  private Collection<NetworkEntity> listNetworks(String vspId, Version version) {
    return vendorSoftwareProductDao.listNetworks(vspId, version);
  }

  @Override
  public NetworkEntity createNetwork(NetworkEntity network, String user) {
    Version activeVersion =
        getVersionInfo(network.getVspId(), VersionableEntityAction.Write, user).getActiveVersion();
    network.setVersion(activeVersion);
    if (!isManual(network.getVspId(), activeVersion)) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(network.getVspId(), activeVersion);
    return null;
  }

  private NetworkEntity createNetwork(NetworkEntity network) {
    network.setId(CommonMethods.nextUuId());
    vendorSoftwareProductDao.createNetwork(network);

    return network;
  }

  @Override
  public CompositionEntityValidationData updateNetwork(NetworkEntity network, String user) {
    Version activeVersion =
        getVersionInfo(network.getVspId(), VersionableEntityAction.Write, user).getActiveVersion();
    network.setVersion(activeVersion);
    NetworkEntity retrieved = getNetwork(network.getVspId(), activeVersion, network.getId());

    NetworkCompositionSchemaInput schemaInput = new NetworkCompositionSchemaInput();
    schemaInput.setManual(isManual(network.getVspId(), activeVersion));
    schemaInput.setNetwork(retrieved.getNetworkCompositionData());

    CompositionEntityValidationData validationData = CompositionEntityDataManager
        .validateEntity(network, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      vendorSoftwareProductDao.updateNetwork(network);
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(network.getVspId(), activeVersion);

    return validationData;
  }

  @Override
  public CompositionEntityResponse<Network> getNetwork(String vspId, Version version,
                                                       String networkId, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    NetworkEntity networkEntity = getNetwork(vspId, version, networkId);
    Network network = networkEntity.getNetworkCompositionData();

    NetworkCompositionSchemaInput schemaInput = new NetworkCompositionSchemaInput();
    schemaInput.setManual(isManual(vspId, version));
    schemaInput.setNetwork(network);

    CompositionEntityResponse<Network> response = new CompositionEntityResponse<>();
    response.setId(networkId);
    response.setData(network);
    response.setSchema(SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.network, schemaInput));

    return response;
  }

  private NetworkEntity getNetwork(String vspId, Version version, String networkId) {
    NetworkEntity retrieved = vendorSoftwareProductDao.getNetwork(vspId, version, networkId);
    VersioningUtil.validateEntityExistence(retrieved, new NetworkEntity(vspId, version, networkId),
        VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public void deleteNetwork(String vspId, String networkId, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    if (!isManual(vspId, activeVersion)) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public QuestionnaireResponse getComponentQuestionnaire(String vspId, Version version,
                                                         String componentId, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse.setData(getComponent(vspId, version, componentId).getQuestionnaireData());
    List<String> nicNames = listNics(vspId, version, componentId).stream()
        .map(nic -> nic.getNicCompositionData().getName()).collect(Collectors.toList());
    questionnaireResponse.setSchema(getComponentQuestionnaireSchema(
        new ComponentQuestionnaireSchemaInput(nicNames,
            JsonUtil.json2Object(questionnaireResponse.getData(), Map.class))));

    return questionnaireResponse;
  }

  @Override
  public void updateComponentQuestionnaire(String vspId, String componentId,
                                           String questionnaireData, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    getComponent(vspId, activeVersion, componentId);

    vendorSoftwareProductDao
        .updateComponentQuestionnaire(vspId, activeVersion, componentId, questionnaireData);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public Collection<ComponentEntity> listComponents(String vspId, Version version, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    return listComponents(vspId, version);
  }

  private Collection<ComponentEntity> listComponents(String vspId, Version version) {
    return vendorSoftwareProductDao.listComponents(vspId, version);
  }

  @Override
  public void deleteComponents(String vspId, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    if (!isManual(vspId, activeVersion)) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public ComponentEntity createComponent(ComponentEntity component, String user) {
    Version activeVersion =
        getVersionInfo(component.getVspId(), VersionableEntityAction.Write, user)
            .getActiveVersion();
    component.setVersion(activeVersion);

    if (!isManual(component.getVspId(), activeVersion)) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());

    }

    /*        ComponentCompositionSchemaInput schemaInput = new ComponentCompositionSchemaInput();
        schemaInput.setManual(true);
        CompositionEntityValidationData validationData = CompositionEntityDataManager
        .validateEntity(component, SchemaTemplateContext.composition, schemaInput);
        if (CollectionUtils.isEmpty(validationData.getErrors())) {
            return createComponent(component);
        }
        return validationData;*/

    vendorSoftwareProductDao.updateVspLatestModificationTime(component.getVspId(), activeVersion);

    return null;
  }

  private ComponentEntity createComponent(ComponentEntity component) {
    component.setId(CommonMethods.nextUuId());
    component.setQuestionnaireData(
        new JsonSchemaDataGenerator(getComponentQuestionnaireSchema(null)).generateData());

    vendorSoftwareProductDao.createComponent(component);

    return component;
  }

  @Override
  public CompositionEntityResponse<ComponentData> getComponent(String vspId, Version version,
                                                               String componentId, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    ComponentEntity componentEntity = getComponent(vspId, version, componentId);
    ComponentData component = componentEntity.getComponentCompositionData();

    ComponentCompositionSchemaInput schemaInput = new ComponentCompositionSchemaInput();
    schemaInput.setManual(isManual(vspId, version));
    schemaInput.setComponent(component);

    CompositionEntityResponse<ComponentData> response = new CompositionEntityResponse<>();
    response.setId(componentId);
    response.setData(component);
    response.setSchema(SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.component, schemaInput));

    return response;
  }

  private ComponentEntity getComponent(String vspId, Version version, String componentId) {
    ComponentEntity retrieved = vendorSoftwareProductDao.getComponent(vspId, version, componentId);
    VersioningUtil
        .validateEntityExistence(retrieved, new ComponentEntity(vspId, version, componentId),
            VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public CompositionEntityValidationData updateComponent(ComponentEntity component, String user) {
    Version activeVersion =
        getVersionInfo(component.getVspId(), VersionableEntityAction.Write, user)
            .getActiveVersion();
    component.setVersion(activeVersion);
    ComponentEntity retrieved =
        getComponent(component.getVspId(), activeVersion, component.getId());

    ComponentCompositionSchemaInput schemaInput = new ComponentCompositionSchemaInput();
    schemaInput.setManual(isManual(component.getVspId(), activeVersion));
    schemaInput.setComponent(retrieved.getComponentCompositionData());

    CompositionEntityValidationData validationData = CompositionEntityDataManager
        .validateEntity(component, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      vendorSoftwareProductDao.updateComponent(component);
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(component.getVspId(), activeVersion);

    return validationData;
  }

  @Override
  public void deleteComponent(String vspId, String componentId, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    if (!isManual(vspId, activeVersion)) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity> listProcesses(
      String vspId, Version version, String componentId,
      String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    if (!GENERAL_COMPONENT_ID.equals(componentId)) {
      getComponent(vspId, version, componentId);
    }
    return vendorSoftwareProductDao.listProcesses(vspId, version, componentId);
  }

  @Override
  public void deleteProcesses(String vspId, String componentId, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    if (!GENERAL_COMPONENT_ID.equals(componentId)) {
      getComponent(vspId, activeVersion, componentId);
    }

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity> processes =
        vendorSoftwareProductDao.listProcesses(vspId, activeVersion, componentId);
    for (org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity process : processes) {
      UniqueValueUtil.deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
          process.getVspId(), process.getVersion().toString(), process.getComponentId(),
          process.getName());
    }

    vendorSoftwareProductDao.deleteProcesses(vspId, activeVersion, componentId);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity createProcess(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity process, String user) {
    Version activeVersion =
        getVersionInfo(process.getVspId(), VersionableEntityAction.Write, user).getActiveVersion();
    process.setVersion(activeVersion);
    UniqueValueUtil.validateUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
        process.getVspId(), process.getVersion().toString(), process.getComponentId(),
        process.getName());
    process.setId(CommonMethods.nextUuId());
    if (!GENERAL_COMPONENT_ID.equals(process.getComponentId())) {
      getComponent(process.getVspId(), activeVersion, process.getComponentId());
    }

    vendorSoftwareProductDao.createProcess(process);
    UniqueValueUtil.createUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
        process.getVspId(), process.getVersion().toString(), process.getComponentId(),
        process.getName());

    vendorSoftwareProductDao.updateVspLatestModificationTime(process.getVspId(), activeVersion);
    return process;
  }

  @Override
  public org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity getProcess(String vspId,
                                                                                 Version version,
                                                                                 String componentId,
                                                                                 String processId,
                                                                                 String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity retrieved =
        vendorSoftwareProductDao.getProcess(vspId, version, componentId, processId);
    validateProcessExistence(vspId, version, componentId, processId, retrieved);
    return retrieved;
  }

  @Override
  public void updateProcess(org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity process,
                            String user) {
    Version activeVersion =
        getVersionInfo(process.getVspId(), VersionableEntityAction.Write, user).getActiveVersion();
    process.setVersion(activeVersion);

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity retrieved =
        vendorSoftwareProductDao
            .getProcess(process.getVspId(), activeVersion, process.getComponentId(),
                process.getId());
    validateProcessExistence(process.getVspId(), activeVersion, process.getComponentId(),
        process.getId(), retrieved);

    UniqueValueUtil.updateUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
        retrieved.getName(), process.getName(), process.getVspId(), process.getVersion().toString(),
        process.getComponentId());
    vendorSoftwareProductDao.updateProcess(process);

    vendorSoftwareProductDao.updateVspLatestModificationTime(process.getVspId(), activeVersion);
  }

  @Override
  public void deleteProcess(String vspId, String componentId, String processId, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity retrieved =
        vendorSoftwareProductDao.getProcess(vspId, activeVersion, componentId, processId);
    validateProcessExistence(vspId, activeVersion, componentId, processId, retrieved);

    vendorSoftwareProductDao.deleteProcess(vspId, activeVersion, componentId, processId);
    UniqueValueUtil.deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
        retrieved.getVspId(), retrieved.getVersion().toString(), retrieved.getComponentId(),
        retrieved.getName());

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public File getProcessArtifact(String vspId, Version version, String componentId,
                                 String processId, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    ProcessArtifactEntity retrieved =
        vendorSoftwareProductDao.getProcessArtifact(vspId, version, componentId, processId);
    validateProcessArtifactExistence(vspId, version, componentId, processId, retrieved);

    File file = new File(String
        .format("%s_%s_%s_%s", vspId, version.toString().replace('.', '_'), componentId,
            processId));
    try {
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(retrieved.getArtifact().array());
      fos.close();
    } catch (IOException e0) {
      throw new CoreException(new UploadInvalidErrorBuilder().build());
    }

    return file;
  }

  @Override
  public void deleteProcessArtifact(String vspId, String componentId, String processId,
                                    String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    ProcessArtifactEntity retrieved =
        vendorSoftwareProductDao.getProcessArtifact(vspId, activeVersion, componentId, processId);
    validateProcessArtifactExistence(vspId, activeVersion, componentId, processId, retrieved);

    vendorSoftwareProductDao.deleteProcessArtifact(vspId, activeVersion, componentId, processId);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public void uploadProcessArtifact(InputStream artifactFile, String artifactFileName, String vspId,
                                    String componentId, String processId, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity retrieved =
        vendorSoftwareProductDao.getProcess(vspId, activeVersion, componentId, processId);
    validateProcessExistence(vspId, activeVersion, componentId, processId, retrieved);

    if (artifactFile == null) {
      throw new CoreException(new UploadInvalidErrorBuilder().build());
    }

    byte[] artifact;
    try {
      artifact = FileUtils.toByteArray(artifactFile);
    } catch (RuntimeException e0) {
      throw new CoreException(new UploadInvalidErrorBuilder().build());
    }

    vendorSoftwareProductDao
        .uploadProcessArtifact(vspId, activeVersion, componentId, processId, artifact,
            artifactFileName);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity> listNics(
      String vspId, Version version, String componentId,
      String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity> nics =
        listNics(vspId, version, componentId);

    Map<String, String> networksNameById = listNetworksNameById(vspId, version);
    nics.stream().forEach(nicEntity -> {
      Nic nic = nicEntity.getNicCompositionData();
      nic.setNetworkName(networksNameById.get(nic.getNetworkId()));
      nicEntity.setNicCompositionData(nic);
    });
    return nics;
  }

  private Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity> listNics(
      String vspId, Version version, String componentId) {
    getComponent(vspId, version, componentId);

    return vendorSoftwareProductDao.listNics(vspId, version, componentId);
  }

  private Map<String, String> listNetworksNameById(String vspId, Version version) {
    Collection<NetworkEntity> networks = listNetworks(vspId, version);
    return networks.stream().collect(Collectors.toMap(NetworkEntity::getId,
        networkEntity -> networkEntity.getNetworkCompositionData().getName()));
  }

  @Override
  public org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity createNic(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nic, String user) {
    Version activeVersion =
        getVersionInfo(nic.getVspId(), VersionableEntityAction.Write, user).getActiveVersion();
    nic.setVersion(activeVersion);
    if (!isManual(nic.getVspId(), activeVersion)) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(nic.getVspId(), activeVersion);

    return null;
  }

  private org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity createNic(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nic) {
    nic.setId(CommonMethods.nextUuId());
    nic.setQuestionnaireData(
        new JsonSchemaDataGenerator(getNicQuestionnaireSchema(null)).generateData());

    vendorSoftwareProductDao.createNic(nic);

    return nic;
  }

  @Override
  public CompositionEntityResponse<Nic> getNic(String vspId, Version version, String componentId,
                                               String nicId, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        nicEntity = getNic(vspId, version, componentId, nicId);
    Nic nic = nicEntity.getNicCompositionData();

    NicCompositionSchemaInput schemaInput = new NicCompositionSchemaInput();
    schemaInput.setManual(isManual(vspId, version));
    schemaInput.setNic(nic);
    Map<String, String> networksNameById = listNetworksNameById(vspId, version);
    nic.setNetworkName(networksNameById.get(nic.getNetworkId()));
    schemaInput.setNetworkIds(networksNameById.keySet());

    CompositionEntityResponse<Nic> response = new CompositionEntityResponse<>();
    response.setId(nicId);
    response.setData(nic);
    response.setSchema(SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.nic, schemaInput));

    return response;
  }

  private org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity getNic(String vspId,
                                                                            Version version,
                                                                            String componentId,
                                                                            String nicId) {
    getComponent(vspId, version, componentId);
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        retrieved = vendorSoftwareProductDao.getNic(vspId, version, componentId, nicId);
    VersioningUtil
        .validateEntityExistence(retrieved,
            new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vspId, version,
                componentId, nicId),
            VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public void deleteNic(String vspId, String componentId, String nicId, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    if (!isManual(vspId, activeVersion)) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
              .withMessage(VSP_COMPOSITION_EDIT_NOT_ALLOWED_MSG).build());
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public CompositionEntityValidationData updateNic(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nic, String user) {
    Version activeVersion =
        getVersionInfo(nic.getVspId(), VersionableEntityAction.Write, user).getActiveVersion();
    nic.setVersion(activeVersion);
    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        retrieved = getNic(nic.getVspId(), activeVersion, nic.getComponentId(), nic.getId());

    NicCompositionSchemaInput schemaInput = new NicCompositionSchemaInput();
    schemaInput.setManual(isManual(nic.getVspId(), activeVersion));
    schemaInput.setNic(retrieved.getNicCompositionData());

    CompositionEntityValidationData validationData = CompositionEntityDataManager
        .validateEntity(nic, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      vendorSoftwareProductDao.updateNic(nic);
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(nic.getVspId(), activeVersion);
    return validationData;
  }

  @Override
  public QuestionnaireResponse getNicQuestionnaire(String vspId, Version version,
                                                   String componentId, String nicId, String user) {
    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vspId, VersionableEntityAction.Read, user));

    QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
    questionnaireResponse
        .setData(getNic(vspId, version, componentId, nicId).getQuestionnaireData());
    questionnaireResponse.setSchema(getNicQuestionnaireSchema(null));

    return questionnaireResponse;
  }

  @Override
  public void updateNicQuestionnaire(String vspId, String componentId, String nicId,
                                     String questionnaireData, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    getNic(vspId, activeVersion, componentId, nicId);

    vendorSoftwareProductDao
        .updateNicQuestionnaire(vspId, activeVersion, componentId, nicId, questionnaireData);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public void deleteComponentMib(String vspId, String componentId, boolean isTrap, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    ComponentArtifactEntity componentArtifactEntity =
        setValuesForComponentArtifactEntityUpload(vspId, activeVersion, null, componentId, null,
            isTrap, null);
    ComponentArtifactEntity retrieved =
        componentArtifactDao.getArtifactByType(componentArtifactEntity);

    componentArtifactDao.delete(retrieved);

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  @Override
  public void uploadComponentMib(InputStream object, String filename, String vspId,
                                 String componentId, boolean isTrap, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Write, user).getActiveVersion();
    ComponentArtifactEntity componentArtifactEntity;


    if (object == null) {
      throw new CoreException(new MibUploadErrorBuilder(
          Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()).build());
    } else {
      byte[] uploadedFileData;
      Map<String, List<ErrorMessage>> errors = new HashMap<>();
      try {
        uploadedFileData = FileUtils.toByteArray(object);
        validateMibZipContent(vspId, activeVersion, uploadedFileData, errors);
        if (MapUtils.isNotEmpty(errors)) {
          throw new CoreException(
              new MibUploadErrorBuilder(errors.values().iterator().next().get(0).getMessage())
                  .build());
        }

        createArtifactInDatabase(vspId, activeVersion, filename, componentId, isTrap,
            uploadedFileData);

      } catch (Exception e0) {
        throw new CoreException(new MibUploadErrorBuilder(e0.getMessage()).build());
      }
    }

    vendorSoftwareProductDao.updateVspLatestModificationTime(vspId, activeVersion);
  }

  private void createArtifactInDatabase(String vspId, Version activeVersion, String filename,
                                        String componentId, boolean isTrap,
                                        byte[] uploadedFileData) {
    ComponentArtifactEntity componentArtifactEntity;

    String artifactId = CommonMethods.nextUuId();
    componentArtifactEntity =
        setValuesForComponentArtifactEntityUpload(vspId, activeVersion, filename, componentId,
            artifactId, isTrap, uploadedFileData);
    componentArtifactDao.update(componentArtifactEntity);
  }

  @Override
  public MibUploadStatus listMibFilenames(String vspId, String componentId, String user) {
    Version activeVersion =
        getVersionInfo(vspId, VersionableEntityAction.Read, user).getActiveVersion();
    ComponentArtifactEntity current =
        new ComponentArtifactEntity(vspId, activeVersion, componentId, null);

    return setMibUploadStatusValues(current);

  }

  private MibUploadStatus setMibUploadStatusValues(
      ComponentArtifactEntity componentArtifactEntity) {
    MibUploadStatus mibUploadStatus = new MibUploadStatus();

    Collection<ComponentArtifactEntity> artifactNames =
        componentArtifactDao.getArtifactNamesAndTypesForComponent(componentArtifactEntity);
    Map<ComponentArtifactType, String> artifactTypeToFilename =
        VendorSoftwareProductUtils.filterNonTrapOrPollArtifacts(artifactNames);

    if (MapUtils.isNotEmpty(artifactTypeToFilename)) {
      if (artifactTypeToFilename.containsKey(ComponentArtifactType.SNMP_TRAP)) {
        mibUploadStatus.setSnmpTrap(artifactTypeToFilename.get(ComponentArtifactType.SNMP_TRAP));
      }
      if (artifactTypeToFilename.containsKey(ComponentArtifactType.SNMP_POLL)) {
        mibUploadStatus.setSnmpPoll(artifactTypeToFilename.get(ComponentArtifactType.SNMP_POLL));
      }
    }

    return mibUploadStatus;
  }

  private ComponentArtifactEntity setValuesForComponentArtifactEntityUpload(String vspId,
                                                                            Version version,
                                                                            String filename,
                                                                            String componentId,
                                                                            String artifactId,
                                                                            boolean isTrap,
                                                                            byte[]
                                                                                uploadedFileData) {
    ComponentArtifactEntity componentArtifactEntity = new ComponentArtifactEntity();

    componentArtifactEntity.setVspId(vspId);
    componentArtifactEntity.setVersion(version);
    componentArtifactEntity.setComponentId(componentId);
    componentArtifactEntity.setId(artifactId);
    componentArtifactEntity.setType(ComponentArtifactType.getComponentArtifactType(isTrap));
    componentArtifactEntity.setArtifactName(filename);

    if (Objects.nonNull(uploadedFileData)) {
      componentArtifactEntity.setArtifact(ByteBuffer.wrap(uploadedFileData));
    }

    return componentArtifactEntity;
  }

  private void validateProcessExistence(String vspId, Version version, String componentId,
                       String processId,
                       org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity retrieved) {
    if (retrieved != null) {
      return;
    }
    if (!GENERAL_COMPONENT_ID.equals(componentId)) {
      getComponent(vspId, version, componentId);
    }
    VersioningUtil.validateEntityExistence(retrieved,
        new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vspId, version,
            componentId, processId),
        VspDetails.ENTITY_TYPE);//todo retrieved is always null ??
  }

  private void validateProcessArtifactExistence(String vspId, Version version, String componentId,
                                                String processId, ProcessArtifactEntity retrieved) {
    if (retrieved != null) {
      VersioningUtil.validateEntityExistence(retrieved.getArtifact(),
          new ProcessArtifactEntity(vspId, version, componentId, processId),
          VspDetails.ENTITY_TYPE);
    } else {
      if (!GENERAL_COMPONENT_ID.equals(componentId)) {
        getComponent(vspId, version, componentId);
      }
      VersioningUtil.validateEntityExistence(retrieved,
          new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity(vspId, version,
              componentId, processId),
          VspDetails.ENTITY_TYPE); //todo retrieved is always null ??
    }
  }

  private Map<String, List<ErrorMessage>> validateUploadData(UploadDataEntity uploadData)
      throws IOException {
    if (uploadData == null || uploadData.getContentData() == null) {
      return null;
    }

    FileContentHandler fileContentMap =
        VendorSoftwareProductUtils.loadUploadFileContent(uploadData.getContentData().array());
    ValidationManager validationManager =
        ValidationManagerUtil.initValidationManager(fileContentMap);
    Map<String, List<ErrorMessage>> validationErrors = validationManager.validate();

    return
        MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, validationErrors))
            ? null : validationErrors;
  }

  private VersionInfo getVersionInfo(String vendorSoftwareProductId, VersionableEntityAction action,
                                     String user) {
    return versioningManager
        .getEntityVersionInfo(VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE, vendorSoftwareProductId,
            user, action);
  }

  private void saveCompositionData(String vspId, Version version, CompositionData compositionData) {
    Map<String, String> networkIdByName = new HashMap<>();
    for (Network network : compositionData.getNetworks()) {

      NetworkEntity networkEntity = new NetworkEntity(vspId, version, null);
      networkEntity.setNetworkCompositionData(network);

      if (network.getName() != null) {
        networkIdByName.put(network.getName(), createNetwork(networkEntity).getId());
      }
    }

    for (Component component : compositionData.getComponents()) {
      ComponentEntity componentEntity = new ComponentEntity(vspId, version, null);
      componentEntity.setComponentCompositionData(component.getData());

      String componentId = createComponent(componentEntity).getId();

      if (CollectionUtils.isNotEmpty(component.getNics())) {
        for (Nic nic : component.getNics()) {
          if (nic.getNetworkName() != null) {
            nic.setNetworkId(networkIdByName.get(nic.getNetworkName()));
            nic.setNetworkName(null);
          }

          org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
              nicEntity =
              new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(vspId, version,
                  componentId, null);
          nicEntity.setNicCompositionData(nic);
          createNic(nicEntity);
        }
      }
    }
  }

  private void deleteUploadDataAndContent(String vspId, Version version) {
    vendorSoftwareProductDao.deleteUploadData(vspId, version);
  }

  private QuestionnaireValidationResult validateQuestionnaire(String vspId, Version version) {
    CompositionEntityDataManager compositionEntityDataManager = new CompositionEntityDataManager();
    compositionEntityDataManager
        .addEntity(vendorSoftwareProductDao.getQuestionnaire(vspId, version), null);

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity> nics =
        vendorSoftwareProductDao.listNicsByVsp(vspId, version);

    Map<String, List<String>> nicNamesByComponent = new HashMap<>();
    for (org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nicEntity : nics) {
      compositionEntityDataManager.addEntity(nicEntity, null);

      Nic nic = nicEntity.getNicCompositionData();
      if (nic != null && nic.getName() != null) {
        List<String> nicNames = nicNamesByComponent.get(nicEntity.getComponentId());
        if (nicNames == null) {
          nicNames = new ArrayList<>();
          nicNamesByComponent.put(nicEntity.getComponentId(), nicNames);
        }
        nicNames.add(nic.getName());
      }
    }

    Collection<ComponentEntity> components =
        vendorSoftwareProductDao.listComponentsQuestionnaire(vspId, version);
    components.stream().forEach(component -> compositionEntityDataManager.addEntity(component,
        new ComponentQuestionnaireSchemaInput(nicNamesByComponent.get(component.getId()),
            JsonUtil.json2Object(component.getQuestionnaireData(), Map.class))));

    Map<CompositionEntityId, Collection<String>> errorsByEntityId =
        compositionEntityDataManager.validateEntitiesQuestionnaire();
    if (MapUtils.isNotEmpty(errorsByEntityId)) {
      compositionEntityDataManager.buildTrees();
      compositionEntityDataManager.addErrorsToTrees(errorsByEntityId);
      Collection<CompositionEntityValidationData> roots = compositionEntityDataManager.getTrees();
      return new QuestionnaireValidationResult(roots.iterator().next());
    }

    return null;
  }
}
