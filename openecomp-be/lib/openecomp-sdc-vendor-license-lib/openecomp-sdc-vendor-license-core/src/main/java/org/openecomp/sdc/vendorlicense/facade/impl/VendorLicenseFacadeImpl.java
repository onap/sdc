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

import org.apache.commons.collections4.CollectionUtils;
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
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersionableSubEntityNotFoundErrorBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE;
import static org.openecomp.sdc.vendorlicense.errors.UncompletedVendorLicenseModelErrorType.SUBMIT_UNCOMPLETED_VLM_MSG_FG_MISSING_EP;
import static org.openecomp.sdc.vendorlicense.errors.UncompletedVendorLicenseModelErrorType.SUBMIT_UNCOMPLETED_VLM_MSG_LA_MISSING_FG;

public class VendorLicenseFacadeImpl implements VendorLicenseFacade {
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
  public FeatureGroupEntity getFeatureGroup(FeatureGroupEntity featureGroup) {
    FeatureGroupEntity retrieved = featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);
    if (retrieved.getManufacturerReferenceNumber() == null) {
      updateManufacturerNumberInFeatureGroup(retrieved);
    }
    return retrieved;
  }

  @Override
  public FeatureGroupModel getFeatureGroupModel(FeatureGroupEntity featureGroup) {
    FeatureGroupEntity retrieved = getFeatureGroup(featureGroup);

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
                                                        String licenseAgreementId) {
    LicenseAgreementEntity retrieved =
        getLicenseAgreement(vlmId, version, licenseAgreementId);

    LicenseAgreementModel licenseAgreementModel = new LicenseAgreementModel();
    licenseAgreementModel.setLicenseAgreement(retrieved);

    for (String featureGroupId : retrieved.getFeatureGroupIds()) {
      licenseAgreementModel.getFeatureGroups().add(featureGroupDao
          .get(new FeatureGroupEntity(vlmId, retrieved.getVersion(), featureGroupId)));
    }

    return licenseAgreementModel;
  }

  @Override
  public EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool) {
    entitlementPool.setVersionUuId(CommonMethods.nextUuId());
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
        entitlementPool.getVendorLicenseModelId(), entitlementPool.getVersion().getId(),
        entitlementPool.getName());
    entitlementPoolDao.create(entitlementPool);
    return entitlementPool;
  }

  @Override
  public void updateEntitlementPool(EntitlementPoolEntity entitlementPool) {
    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);
    if (retrieved.equals(entitlementPool)) {
      return;
    }
    UniqueValueUtil.updateUniqueValue(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
        retrieved.getName(), entitlementPool.getName(), entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().getId());
    entitlementPool.setVersionUuId(CommonMethods.nextUuId());
    entitlementPoolDao.update(entitlementPool);
  }

  @Override
  public Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version) {
    return licenseKeyGroupDao.list(new LicenseKeyGroupEntity(vlmId, version, null));
  }

  @Override
  public Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version) {
    return entitlementPoolDao.list(new EntitlementPoolEntity(vlmId, version, null));
  }

  @Override
  public void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    if (retrieved.equals(licenseKeyGroup)) {
      return;
    }
    licenseKeyGroup.setVersionUuId((CommonMethods.nextUuId()));
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);
    UniqueValueUtil.updateUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_KEY_GROUP_NAME,
        retrieved.getName(), licenseKeyGroup.getName(), licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().getId());
    licenseKeyGroupDao.update(licenseKeyGroup);
  }

  @Override
  public LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    licenseKeyGroup.setVersionUuId(CommonMethods.nextUuId());
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_KEY_GROUP_NAME,
        licenseKeyGroup.getVendorLicenseModelId(), licenseKeyGroup.getVersion().getId(),
        licenseKeyGroup.getName());
    licenseKeyGroupDao.create(licenseKeyGroup);
    return licenseKeyGroup;
  }

  @Override
  public VendorLicenseModelEntity getVendorLicenseModel(String vlmId, Version version) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);

    VendorLicenseModelEntity vendorLicenseModel =
        vendorLicenseModelDao.get(new VendorLicenseModelEntity(vlmId, version));
    if (vendorLicenseModel == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_VLM, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.ENTITY_NOT_FOUND);
      throw new CoreException(new VendorLicenseModelNotFoundErrorBuilder(vlmId).build());
    }

    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return vendorLicenseModel;
  }

  @Override
  public LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement) {
    //licenseAgreement.setId(CommonMethods.nextUuId());
    VersioningUtil.validateEntitiesExistence(licenseAgreement.getFeatureGroupIds(),
        new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(),
            licenseAgreement.getVersion(),
            null),
        featureGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);

    UniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        licenseAgreement.getVendorLicenseModelId(), licenseAgreement.getVersion().getId(),
        licenseAgreement.getName());

    licenseAgreementDao.create(licenseAgreement);
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        licenseAgreement.getVendorLicenseModelId(), licenseAgreement.getVersion().getId(),
        licenseAgreement.getName());
    if (licenseAgreement.getFeatureGroupIds() != null) {
      for (String addedFgId : licenseAgreement.getFeatureGroupIds()) {
        featureGroupDao.addReferencingLicenseAgreement(
            new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(),
                licenseAgreement.getVersion(),
                addedFgId), licenseAgreement.getId());
      }
    }
    return licenseAgreement;
  }

  @Override
  public FeatureGroupEntity createFeatureGroup(FeatureGroupEntity featureGroup) {
    VersioningUtil.validateEntitiesExistence(featureGroup.getLicenseKeyGroupIds(),
        new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(), featureGroup.getVersion(),
            null),
        licenseKeyGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateEntitiesExistence(featureGroup.getEntitlementPoolIds(),
        new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), featureGroup.getVersion(),
            null),
        entitlementPoolDao, VendorLicenseModelEntity.ENTITY_TYPE);
    UniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        featureGroup.getVendorLicenseModelId(), featureGroup.getVersion().getId(),
        featureGroup.getName());

    featureGroupDao.create(featureGroup);
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        featureGroup.getVendorLicenseModelId(), featureGroup.getVersion().getId(),
        featureGroup.getName());

    if (featureGroup.getLicenseKeyGroupIds() != null) {
      for (String addedLkgId : featureGroup.getLicenseKeyGroupIds()) {
        licenseKeyGroupDao.addReferencingFeatureGroup(
            new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), addedLkgId),
            featureGroup.getId());
      }
    }

    if (featureGroup.getEntitlementPoolIds() != null) {
      for (String addedEpId : featureGroup.getEntitlementPoolIds()) {
        entitlementPoolDao.addReferencingFeatureGroup(
            new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), addedEpId), featureGroup.getId());
      }
    }
    return featureGroup;
  }

  @Override
  public Collection<FeatureGroupEntity> listFeatureGroups(String vlmId, Version version) {
    Collection<FeatureGroupEntity> featureGroupEntities =
        featureGroupDao.list(new FeatureGroupEntity(vlmId, version, null));
    featureGroupEntities.stream()
        .filter(fgEntity -> Objects.isNull(fgEntity.getManufacturerReferenceNumber()))
        .forEach(this::updateManufacturerNumberInFeatureGroup);
    return featureGroupEntities;
  }


  @Override
  public Collection<ErrorCode> validateLicensingData(String vlmId, Version version,
                                                     String licenseAgreementId,
                                                     Collection<String> featureGroupIds) {
    // TODO: 5/21/2017 validate version exists and final
/*    try {
      VersionInfo versionInfo = getVersionInfo(vlmId, VersionableEntityAction.Read, "");
      if (version == null || !version.isFinal()
          || !versionInfo.getViewableVersions().contains(version)) {
        return Collections.singletonList(new RequestedVersionInvalidErrorBuilder().build());
      }
    } catch (CoreException exception) {
      return Collections.singletonList(exception.code());
    }*/

    List<ErrorCode> errorMessages = new ArrayList<>();

    try {
      getLicenseAgreement(vlmId, version, licenseAgreementId);
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
  public LicenseAgreementEntity getLicenseAgreement(String vlmId, Version version,
                                                    String licenseAgreementId) {
    LicenseAgreementEntity input = new LicenseAgreementEntity(vlmId, version, licenseAgreementId);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(input);
    VersioningUtil.validateEntityExistence(retrieved, input, VendorLicenseModelEntity.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public Collection<LimitEntity> listLimits(String vlmId, Version version, String epLkgId) {
    return limitDao.list(new LimitEntity(vlmId, version, epLkgId, null));
  }

  @Override
  public LimitEntity createLimit(LimitEntity limit) {
    limitDao.create(limit);
    return limit;
  }

  @Override
  public void updateLimit(LimitEntity limit) {
    limitDao.update(limit);
  }

  private void updateManufacturerNumberInFeatureGroup(FeatureGroupEntity featureGroupEntity) {
    if (CollectionUtils.isNotEmpty(featureGroupEntity.getEntitlementPoolIds())) {
      Object[] entitlementPoolIdsList = featureGroupEntity.getEntitlementPoolIds().toArray();
      if (entitlementPoolIdsList.length > 0) {
        String entitlementPoolId = entitlementPoolIdsList[0].toString();
        EntitlementPoolEntity entitlementPoolEntity =
            new EntitlementPoolEntity(featureGroupEntity.getVendorLicenseModelId(),
                featureGroupEntity.getVersion(), entitlementPoolId);
        entitlementPoolEntity = entitlementPoolDao.get(entitlementPoolEntity);
        featureGroupEntity.setManufacturerReferenceNumber(
            entitlementPoolDao.getManufacturerReferenceNumber(entitlementPoolEntity));
        featureGroupDao.update(featureGroupEntity);
      }
    }
  }

  @Override
  public void validate(String vendorLicenseModelId, Version version) {
    Collection<String> allFeatureGroupEntities = new HashSet<>();
    Collection<LicenseAgreementEntity> licenseAgreements = licenseAgreementDao
        .list(new LicenseAgreementEntity(vendorLicenseModelId, version, null));

    if (CollectionUtils.isNotEmpty(licenseAgreements)) {
      licenseAgreements.forEach(licenseAgreement -> {
        if (CollectionUtils.isEmpty(licenseAgreement.getFeatureGroupIds())) {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.SUBMIT_ENTITY, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.SUBMIT_ENTITY);
          throw new CoreException(
              new SubmitUncompletedLicenseModelErrorBuilder(
                  SUBMIT_UNCOMPLETED_VLM_MSG_LA_MISSING_FG).build());
        }
        allFeatureGroupEntities.addAll(licenseAgreement.getFeatureGroupIds());
      });

      allFeatureGroupEntities.forEach(fg -> {
        FeatureGroupEntity featureGroupEntity =
            featureGroupDao.get(new FeatureGroupEntity(vendorLicenseModelId, version, fg));
        if (CollectionUtils.isEmpty(featureGroupEntity.getEntitlementPoolIds())) {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.SUBMIT_ENTITY, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.SUBMIT_ENTITY);
          throw new CoreException(
              new SubmitUncompletedLicenseModelErrorBuilder(
                  SUBMIT_UNCOMPLETED_VLM_MSG_FG_MISSING_EP).build());
        }
      });
    }
  }


}
