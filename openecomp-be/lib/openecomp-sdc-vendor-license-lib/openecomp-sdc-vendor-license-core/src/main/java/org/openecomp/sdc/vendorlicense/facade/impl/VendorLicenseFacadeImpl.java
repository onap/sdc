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

package org.openecomp.sdc.vendorlicense.facade.impl;

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE;
import static org.openecomp.sdc.vendorlicense.errors.UncompletedVendorLicenseModelErrorType.SUBMIT_UNCOMPLETED_VLM_MSG_FG_MISSING_EP;
import static org.openecomp.sdc.vendorlicense.errors.UncompletedVendorLicenseModelErrorType.SUBMIT_UNCOMPLETED_VLM_MSG_LA_MISSING_FG;
import static org.openecomp.sdc.vendorlicense.errors.UncompletedVendorLicenseModelErrorType.SUBMIT_UNCOMPLETED_VLM_MSG_MISSING_LA;

import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.errors.SubmitUncompletedLicenseModelErrorBuilder;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseModelNotFoundErrorBuilder;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.RequestedVersionInvalidErrorBuilder;
import org.openecomp.sdc.versioning.errors.VersionableSubEntityNotFoundErrorBuilder;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class VendorLicenseFacadeImpl implements VendorLicenseFacade {

  private static final VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();

  private static final VendorLicenseModelDao
      vendorLicenseModelDao = VendorLicenseModelDaoFactory.getInstance().createInterface();
  private static final LicenseAgreementDao
      licenseAgreementDao = LicenseAgreementDaoFactory.getInstance().createInterface();
  private static final FeatureGroupDao featureGroupDao =
      FeatureGroupDaoFactory.getInstance().createInterface();
  private static final EntitlementPoolDao
      entitlementPoolDao = EntitlementPoolDaoFactory.getInstance().createInterface();
  private static final LicenseKeyGroupDao
      licenseKeyGroupDao = LicenseKeyGroupDaoFactory.getInstance().createInterface();
  private static final LimitDao limitDao = LimitDaoFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  /**
   * Instantiates a new Vendor license facade.
   */
  public VendorLicenseFacadeImpl() {
    vendorLicenseModelDao.registerVersioning(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    licenseAgreementDao.registerVersioning(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    featureGroupDao.registerVersioning(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    entitlementPoolDao.registerVersioning(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    licenseKeyGroupDao.registerVersioning(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    limitDao.registerVersioning(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
  }

  @Override
  public Version checkin(String vendorLicenseModelId, String user) {
    Version newVersion = versioningManager
        .checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vendorLicenseModelId, user, null);
    updateVlmLastModificationTime(vendorLicenseModelId, newVersion);
    return newVersion;
  }

  @Override
  public Version submit(String vendorLicenseModelId, String user) {
    validateCompletedVendorLicenseModel(vendorLicenseModelId, user);
    Version newVersion = versioningManager
        .submit(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vendorLicenseModelId, user, null);
    updateVlmLastModificationTime(vendorLicenseModelId, newVersion);
    return newVersion;
  }

  @Override
  public FeatureGroupEntity getFeatureGroup(FeatureGroupEntity featureGroup, String user) {
    Version version = VersioningUtil.resolveVersion(featureGroup.getVersion(),
        getVersionInfo(featureGroup.getVendorLicenseModelId(), VersionableEntityAction.Read,
            user), user);
    featureGroup.setVersion(version);


    return getFeatureGroup(featureGroup);
  }

  private FeatureGroupEntity getFeatureGroup(FeatureGroupEntity featureGroup) {
    FeatureGroupEntity retrieved = featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);
    if(retrieved.getManufacturerReferenceNumber() == null){
      Object[] entitlementPoolIdsList = retrieved.getEntitlementPoolIds().toArray();
      if(entitlementPoolIdsList != null && entitlementPoolIdsList.length > 0){
        String entitlementPoolId = entitlementPoolIdsList[0].toString();
        EntitlementPoolEntity entitlementPoolEntity = new EntitlementPoolEntity(retrieved.getVendorLicenseModelId(),
                retrieved.getVersion(), entitlementPoolId);
        entitlementPoolEntity = entitlementPoolDao.get(entitlementPoolEntity);
        retrieved.setManufacturerReferenceNumber(entitlementPoolDao.getManufacturerReferenceNumber(
                entitlementPoolEntity));
        featureGroupDao.update(retrieved);
      }
    }

    return retrieved;
  }

  @Override
  public FeatureGroupModel getFeatureGroupModel(FeatureGroupEntity featureGroup, String user) {
    FeatureGroupEntity retrieved = getFeatureGroup(featureGroup, user);

    FeatureGroupModel featureGroupModel = new FeatureGroupModel();
    featureGroupModel.setFeatureGroup(retrieved);

    for (String licenseKeyGroupId : retrieved.getLicenseKeyGroupIds()) {
      featureGroupModel.getLicenseKeyGroups().add(licenseKeyGroupDao.get(
          new LicenseKeyGroupEntity(retrieved.getVendorLicenseModelId(), retrieved.getVersion(),
              licenseKeyGroupId)));
    }
    for (String entitlementPoolId : retrieved.getEntitlementPoolIds()) {
      featureGroupModel.getEntitlementPools().add(entitlementPoolDao.get(
          new EntitlementPoolEntity(retrieved.getVendorLicenseModelId(), retrieved.getVersion(),
              entitlementPoolId)));
    }

    return featureGroupModel;
  }

  @Override
  public LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                        String licenseAgreementId, String user) {
    LicenseAgreementEntity retrieved =
        getLicenseAgreement(vlmId, version, licenseAgreementId, user);

    LicenseAgreementModel licenseAgreementModel = new LicenseAgreementModel();
    licenseAgreementModel.setLicenseAgreement(retrieved);

    for (String featureGroupId : retrieved.getFeatureGroupIds()) {
      licenseAgreementModel.getFeatureGroups().add(featureGroupDao
          .get(new FeatureGroupEntity(vlmId, retrieved.getVersion(), featureGroupId)));
    }

    return licenseAgreementModel;
  }

  @Override
  public EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool,
                                                     String user) {
    entitlementPool.setVersion(VersioningUtil.resolveVersion(entitlementPool.getVersion(),
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user));
    //entitlementPool.setId(CommonMethods.nextUuId());
    entitlementPool.setVersionUuId(CommonMethods.nextUuId());
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
        entitlementPool.getVendorLicenseModelId(), entitlementPool.getVersion().toString(),
        entitlementPool.getName());
    entitlementPoolDao.create(entitlementPool);
    updateVlmLastModificationTime(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion());
    return entitlementPool;
  }

  @Override
  public void updateEntitlementPool(EntitlementPoolEntity entitlementPool, String user) {
    entitlementPool.setVersion(VersioningUtil.resolveVersion(entitlementPool.getVersion(),
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user));
    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);

    UniqueValueUtil.updateUniqueValue(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
        retrieved.getName(), entitlementPool.getName(), entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().toString());
    entitlementPool.setVersionUuId(CommonMethods.nextUuId());
    entitlementPoolDao.update(entitlementPool);

    updateVlmLastModificationTime(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion());

  }

  @Override
  public Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version,
                                                                String user) {
    return licenseKeyGroupDao.list(new LicenseKeyGroupEntity(vlmId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user), user),
        null));
  }

  @Override
  public Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version,
                                                                String user) {
    return entitlementPoolDao.list(new EntitlementPoolEntity(vlmId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user), user),
        null));
  }

  @Override
  public void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user) {
    licenseKeyGroup.setVersion(VersioningUtil.resolveVersion(licenseKeyGroup.getVersion(),
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user));
    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    licenseKeyGroup.setVersionUuId((CommonMethods.nextUuId()));
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);
    UniqueValueUtil.updateUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_KEY_GROUP_NAME,
        retrieved.getName(), licenseKeyGroup.getName(), licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().toString());
    licenseKeyGroupDao.update(licenseKeyGroup);

    updateVlmLastModificationTime(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion());
  }

  @Override
  public LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                                     String user) {
    licenseKeyGroup.setVersion(VersioningUtil.resolveVersion(licenseKeyGroup.getVersion(),
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user));
    //licenseKeyGroup.setId(CommonMethods.nextUuId());
    licenseKeyGroup.setVersionUuId(CommonMethods.nextUuId());
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_KEY_GROUP_NAME,
        licenseKeyGroup.getVendorLicenseModelId(), licenseKeyGroup.getVersion().toString(),
        licenseKeyGroup.getName());
    licenseKeyGroupDao.create(licenseKeyGroup);
    updateVlmLastModificationTime(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion());
    return licenseKeyGroup;
  }

  @Override
  public VersionedVendorLicenseModel getVendorLicenseModel(String vlmId, Version version,
                                                           String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

    VersionInfo versionInfo = getVersionInfo(vlmId, VersionableEntityAction.Read, user);

    VendorLicenseModelEntity vendorLicenseModel = vendorLicenseModelDao.get(
        new VendorLicenseModelEntity(vlmId,
            VersioningUtil.resolveVersion(version, versionInfo, user)));
    if (vendorLicenseModel == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_VLM, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.ENTITY_NOT_FOUND);
      throw new CoreException(new VendorLicenseModelNotFoundErrorBuilder(vlmId).build());
    }

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return new VersionedVendorLicenseModel(vendorLicenseModel, versionInfo);
  }

  @Override
  public VendorLicenseModelEntity createVendorLicenseModel(
      VendorLicenseModelEntity vendorLicenseModelEntity, String user) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    UniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME,
        vendorLicenseModelEntity.getVendorName());
    //vendorLicenseModelEntity.setId(CommonMethods.nextUuId());

    vendorLicenseModelDao.create(vendorLicenseModelEntity);
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME,
        vendorLicenseModelEntity.getVendorName());

    Version version = versioningManager
        .create(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vendorLicenseModelEntity.getId(), user);
    vendorLicenseModelEntity.setVersion(version);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return vendorLicenseModelEntity;
  }

  @Override
  public LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                                       String user) {
    Version version = VersioningUtil.resolveVersion(licenseAgreement.getVersion(),
        getVersionInfo(licenseAgreement.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user);
    licenseAgreement.setVersion(version);
    //licenseAgreement.setId(CommonMethods.nextUuId());
    VersioningUtil.validateEntitiesExistence(licenseAgreement.getFeatureGroupIds(),
        new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(), version, null),
        featureGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);

    UniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        licenseAgreement.getVendorLicenseModelId(), licenseAgreement.getVersion().toString(),
        licenseAgreement.getName());

    licenseAgreementDao.create(licenseAgreement);
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        licenseAgreement.getVendorLicenseModelId(), licenseAgreement.getVersion().toString(),
        licenseAgreement.getName());
    if (licenseAgreement.getFeatureGroupIds() != null) {
      for (String addedFgId : licenseAgreement.getFeatureGroupIds()) {
        featureGroupDao.addReferencingLicenseAgreement(
            new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(), version,
                addedFgId), licenseAgreement.getId());
      }
    }
    updateVlmLastModificationTime(licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion());

    return licenseAgreement;
  }

  @Override
  public FeatureGroupEntity createFeatureGroup(FeatureGroupEntity featureGroup, String user) {
    Version version = VersioningUtil.resolveVersion(featureGroup.getVersion(),
        getVersionInfo(featureGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user);
    //featureGroup.setId(CommonMethods.nextUuId());
    featureGroup.setVersion(version);
    VersioningUtil.validateEntitiesExistence(featureGroup.getLicenseKeyGroupIds(),
        new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(), version, null),
        licenseKeyGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateEntitiesExistence(featureGroup.getEntitlementPoolIds(),
        new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), version, null),
        entitlementPoolDao, VendorLicenseModelEntity.ENTITY_TYPE);
    UniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        featureGroup.getVendorLicenseModelId(), featureGroup.getVersion().toString(),
        featureGroup.getName());

    featureGroupDao.create(featureGroup);
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        featureGroup.getVendorLicenseModelId(), featureGroup.getVersion().toString(),
        featureGroup.getName());

    if (featureGroup.getLicenseKeyGroupIds() != null) {
      for (String addedLkgId : featureGroup.getLicenseKeyGroupIds()) {
        licenseKeyGroupDao.addReferencingFeatureGroup(
            new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(), version, addedLkgId),
            featureGroup.getId());
      }
    }

    if (featureGroup.getEntitlementPoolIds() != null) {
      for (String addedEpId : featureGroup.getEntitlementPoolIds()) {
        entitlementPoolDao.addReferencingFeatureGroup(
            new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), version, addedEpId),
            featureGroup.getId());
      }
    }

    updateVlmLastModificationTime(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion());

    return featureGroup;
  }

  @Override
  public Collection<ErrorCode> validateLicensingData(String vlmId, Version version,
                                                     String licenseAgreementId,
                                                     Collection<String> featureGroupIds) {
    try {
      VersionInfo versionInfo = getVersionInfo(vlmId, VersionableEntityAction.Read, "");
      if (version == null || !version.isFinal()
          || !versionInfo.getViewableVersions().contains(version)) {
        return Collections.singletonList(new RequestedVersionInvalidErrorBuilder().build());
      }
    } catch (CoreException exception) {
      return Collections.singletonList(exception.code());
    }

    List<ErrorCode> errorMessages = new ArrayList<>();

    try {
      getLicenseAgreement(vlmId, licenseAgreementId, version);
    } catch (CoreException exception) {
      errorMessages.add(exception.code());
    }

    for (String featureGroupId : featureGroupIds) {
      try {
        FeatureGroupEntity featureGroup =
            getFeatureGroup(new FeatureGroupEntity(vlmId, version, featureGroupId));
        if (!featureGroup.getReferencingLicenseAgreements().contains(licenseAgreementId)) {
          errorMessages.add(new VersionableSubEntityNotFoundErrorBuilder(
              featureGroup.getEntityType(),
              featureGroupId,
              LicenseAgreementEntity.ENTITY_TYPE,
              licenseAgreementId,
              version).build());
        }
      } catch (CoreException exception) {
        errorMessages.add(exception.code());
      }
    }

    return errorMessages;
  }

  @Override
  public VersionInfo getVersionInfo(String vendorLicenseModelId, VersionableEntityAction action,
                                    String user) {
    return versioningManager
        .getEntityVersionInfo(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vendorLicenseModelId, user,
            action);
  }

  @Override
  public void updateVlmLastModificationTime(String vendorLicenseModelId, Version version) {
    VendorLicenseModelEntity retrieved =
        vendorLicenseModelDao.get(new VendorLicenseModelEntity(vendorLicenseModelId, version));
    vendorLicenseModelDao.update(retrieved);
  }

  @Override
  public LicenseAgreementEntity getLicenseAgreement(String vlmId, Version version,
                                                    String licenseAgreementId, String user) {
    return getLicenseAgreement(vlmId, licenseAgreementId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user), user));
  }

  @Override
  public LimitEntity createLimit(LimitEntity limit, String user) {
    limit.setVersion(VersioningUtil.resolveVersion(limit.getVersion(),
        getVersionInfo(limit.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user));
    //limit.setVersionUuId(CommonMethods.nextUuId());
    limitDao.create(limit);
    updateVlmLastModificationTime(limit.getVendorLicenseModelId(),
        limit.getVersion());
    return limit;
  }

  @Override
  public Collection<LimitEntity> listLimits(String vlmId, Version version, String epLkgId,
                                                      String user) {
    return limitDao.list(new LimitEntity(vlmId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user), user),
        epLkgId, null));

  }

  @Override
  public void updateLimit(LimitEntity limit, String user) {
    limit.setVersion(VersioningUtil.resolveVersion(limit.getVersion(),
        getVersionInfo(limit.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user));
    //limit.setVersionUuId(CommonMethods.nextUuId());
    limitDao.update(limit);
    updateVlmLastModificationTime(limit.getVendorLicenseModelId(),
        limit.getVersion());
  }

  private LicenseAgreementEntity getLicenseAgreement(String vlmId, String licenseAgreementId,
                                                     Version version) {
    LicenseAgreementEntity input = new LicenseAgreementEntity(vlmId, version, licenseAgreementId);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(input);
    VersioningUtil.validateEntityExistence(retrieved, input, VendorLicenseModelEntity.ENTITY_TYPE);
    return retrieved;
  }

  private void validateCompletedVendorLicenseModel(String vendorLicenseModelId, String user) {
    Version version = VersioningUtil.resolveVersion(null,
        getVersionInfo(vendorLicenseModelId, VersionableEntityAction.Read, user), user);
    Collection<LicenseAgreementEntity> licenseAgreements = licenseAgreementDao
        .list(new LicenseAgreementEntity(vendorLicenseModelId, version, null));

    if (licenseAgreements == null || licenseAgreements.isEmpty()) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.SUBMIT_ENTITY, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.SUBMIT_ENTITY);
      throw new CoreException(
              new SubmitUncompletedLicenseModelErrorBuilder(SUBMIT_UNCOMPLETED_VLM_MSG_MISSING_LA).build());
    }

    for (LicenseAgreementEntity licenseAgreement : licenseAgreements) {
        if (licenseAgreement.getFeatureGroupIds() == null || licenseAgreement.getFeatureGroupIds().isEmpty()) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.SUBMIT_ENTITY, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.SUBMIT_ENTITY);
        throw new CoreException(
                new SubmitUncompletedLicenseModelErrorBuilder(SUBMIT_UNCOMPLETED_VLM_MSG_LA_MISSING_FG).build());
      }
    }

    Collection<FeatureGroupEntity> featureGroupEntities =
        featureGroupDao.list(new FeatureGroupEntity(vendorLicenseModelId, version, null));
    for (FeatureGroupEntity featureGroupEntity : featureGroupEntities) {
        if (featureGroupEntity.getEntitlementPoolIds() == null || featureGroupEntity.getEntitlementPoolIds().isEmpty()) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.SUBMIT_ENTITY, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.SUBMIT_ENTITY);
        throw new CoreException(
                new SubmitUncompletedLicenseModelErrorBuilder(SUBMIT_UNCOMPLETED_VLM_MSG_FG_MISSING_EP).build());
      }
    }

  }
}
