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

import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
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


public class VendorLicenseFacadeImpl
    implements VendorLicenseFacade {


  private static final VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();

  private static final VendorLicenseModelDao vendorLicenseModelDao =
      VendorLicenseModelDaoFactory.getInstance().createInterface();
  private static final LicenseAgreementDao licenseAgreementDao =
      LicenseAgreementDaoFactory.getInstance().createInterface();
  private static final FeatureGroupDao featureGroupDao =
      FeatureGroupDaoFactory.getInstance().createInterface();
  private static final EntitlementPoolDao entitlementPoolDao =
      EntitlementPoolDaoFactory.getInstance().createInterface();
  private static final LicenseKeyGroupDao licenseKeyGroupDao =
      LicenseKeyGroupDaoFactory.getInstance().createInterface();

  /**
   * Instantiates a new Vendor license facade.
   */
  public VendorLicenseFacadeImpl() {
    vendorLicenseModelDao
        .registerVersioning(VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    licenseAgreementDao
        .registerVersioning(VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    featureGroupDao
        .registerVersioning(VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    entitlementPoolDao
        .registerVersioning(VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
    licenseKeyGroupDao
        .registerVersioning(VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE);
  }

  @Override
  public void checkin(String vendorLicenseModelId, String user) {
    Version newVersion = versioningManager
        .checkin(VendorLicenseConstants
            .VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE,
            vendorLicenseModelId, user, null);
    updateVlmLastModificationTime(vendorLicenseModelId, newVersion);
  }

  @Override
  public void submit(String vendorLicenseModelId, String user) {
    validateCompletedVendorLicenseModel(vendorLicenseModelId, user);
    Version newVersion = versioningManager
        .submit(VendorLicenseConstants
            .VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE,
            vendorLicenseModelId, user, null);
    updateVlmLastModificationTime(vendorLicenseModelId, newVersion);
  }

  @Override
  public FeatureGroupEntity getFeatureGroup(FeatureGroupEntity featureGroup, String user) {
    Version version = VersioningUtil.resolveVersion(featureGroup.getVersion(),
        getVersionInfo(featureGroup.getVendorLicenseModelId(), VersionableEntityAction.Read, user));
    featureGroup.setVersion(version);
    return getFeatureGroup(featureGroup);
  }

  private FeatureGroupEntity getFeatureGroup(FeatureGroupEntity featureGroup) {
    FeatureGroupEntity retrieved = featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);
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
  public LicenseAgreementEntity getLicenseAgreement(String vlmId, Version version,
                                                    String licenseAgreementId, String user) {
    return getLicenseAgreement(vlmId, licenseAgreementId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user)));
  }

  private LicenseAgreementEntity getLicenseAgreement(String vlmId, String licenseAgreementId,
                                                     Version version) {
    LicenseAgreementEntity input = new LicenseAgreementEntity(vlmId, version, licenseAgreementId);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(input);
    VersioningUtil.validateEntityExistence(retrieved, input, VendorLicenseModelEntity.ENTITY_TYPE);
    return retrieved;
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
    entitlementPool.setVersion(
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion());
    entitlementPool.setId(CommonMethods.nextUuId());
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
  public LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                                     String user) {
    licenseKeyGroup.setVersion(
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion());
    licenseKeyGroup.setId(CommonMethods.nextUuId());
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
  public void updateEntitlementPool(EntitlementPoolEntity entitlementPool, String user) {
    entitlementPool.setVersion(
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion());
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
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user)), null));
  }

  @Override
  public Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version,
                                                                String user) {
    return entitlementPoolDao.list(new EntitlementPoolEntity(vlmId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user)), null));
  }

  @Override
  public void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user) {
    licenseKeyGroup.setVersion(
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion());
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
  public VersionInfo getVersionInfo(String vendorLicenseModelId, VersionableEntityAction action,
                                    String user) {
    return versioningManager
        .getEntityVersionInfo(VendorLicenseConstants
            .VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE,
            vendorLicenseModelId, user,
            action);
  }

  @Override
  public VersionedVendorLicenseModel getVendorLicenseModel(String vlmId, Version version,
                                                           String user) {
    VersionInfo versionInfo = getVersionInfo(vlmId, VersionableEntityAction.Read, user);

    VendorLicenseModelEntity vendorLicenseModel = vendorLicenseModelDao.get(
        new VendorLicenseModelEntity(vlmId, VersioningUtil.resolveVersion(version, versionInfo)));
    if (vendorLicenseModel == null) {
      throw new CoreException(new VendorLicenseModelNotFoundErrorBuilder(vlmId).build());
    }

    return new VersionedVendorLicenseModel(vendorLicenseModel, versionInfo);
  }

  @Override
  public VendorLicenseModelEntity createVendorLicenseModel(
      VendorLicenseModelEntity vendorLicenseModelEntity, String user) {
    UniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME,
        vendorLicenseModelEntity.getVendorName());
    vendorLicenseModelEntity.setId(CommonMethods.nextUuId());

    Version version = versioningManager
        .create(VendorLicenseConstants
            .VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE,
            vendorLicenseModelEntity.getId(), user);
    vendorLicenseModelEntity.setVersion(version);

    //        vendorLicenseModelEntity.setLastModificationTime(new Date());

    vendorLicenseModelDao.create(vendorLicenseModelEntity);
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME,
        vendorLicenseModelEntity.getVendorName());

    return vendorLicenseModelEntity;
  }

  @Override
  public LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                                       String user) {
    Version activeVersion =
        getVersionInfo(licenseAgreement.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion();
    licenseAgreement.setVersion(activeVersion);
    licenseAgreement.setId(CommonMethods.nextUuId());
    VersioningUtil.validateEntitiesExistence(licenseAgreement.getFeatureGroupIds(),
        new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(), activeVersion, null),
        featureGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);
    UniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        licenseAgreement.getVendorLicenseModelId(), licenseAgreement.getVersion().toString(),
        licenseAgreement.getName());

    if (licenseAgreement.getFeatureGroupIds() != null) {
      for (String addedFgId : licenseAgreement.getFeatureGroupIds()) {
        featureGroupDao.addReferencingLicenseAgreement(
            new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(), activeVersion,
                addedFgId), licenseAgreement.getId());
      }
    }

    licenseAgreementDao.create(licenseAgreement);
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        licenseAgreement.getVendorLicenseModelId(), licenseAgreement.getVersion().toString(),
        licenseAgreement.getName());

    updateVlmLastModificationTime(licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion());

    return licenseAgreement;
  }

  @Override
  public FeatureGroupEntity createFeatureGroup(FeatureGroupEntity featureGroup, String user) {
    Version activeVersion =
        getVersionInfo(featureGroup.getVendorLicenseModelId(), VersionableEntityAction.Write, user)
            .getActiveVersion();
    featureGroup.setId(CommonMethods.nextUuId());
    featureGroup.setVersion(activeVersion);
    VersioningUtil.validateEntitiesExistence(featureGroup.getLicenseKeyGroupIds(),
        new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(), activeVersion, null),
        licenseKeyGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateEntitiesExistence(featureGroup.getEntitlementPoolIds(),
        new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), activeVersion, null),
        entitlementPoolDao, VendorLicenseModelEntity.ENTITY_TYPE);
    UniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        featureGroup.getVendorLicenseModelId(), featureGroup.getVersion().toString(),
        featureGroup.getName());

    if (featureGroup.getLicenseKeyGroupIds() != null) {
      for (String addedLkgId : featureGroup.getLicenseKeyGroupIds()) {
        licenseKeyGroupDao.addReferencingFeatureGroup(
            new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(), activeVersion,
                addedLkgId), featureGroup.getId());
      }
    }

    if (featureGroup.getEntitlementPoolIds() != null) {
      for (String addedEpId : featureGroup.getEntitlementPoolIds()) {
        entitlementPoolDao.addReferencingFeatureGroup(
            new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), activeVersion,
                addedEpId), featureGroup.getId());
      }
    }

    featureGroupDao.create(featureGroup);
    UniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        featureGroup.getVendorLicenseModelId(), featureGroup.getVersion().toString(),
        featureGroup.getName());

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
    } catch (CoreException coreException) {
      return Collections.singletonList(coreException.code());
    }

    List<ErrorCode> errorMessages = new ArrayList<>();

    try {
      getLicenseAgreement(vlmId, licenseAgreementId, version);
    } catch (CoreException coreException) {
      errorMessages.add(coreException.code());
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
      } catch (CoreException coreException) {
        errorMessages.add(coreException.code());
      }
    }

    return errorMessages;
  }

  @Override
  public void updateVlmLastModificationTime(String vendorLicenseModelId, Version version) {
    VendorLicenseModelEntity retrieved =
        vendorLicenseModelDao.get(new VendorLicenseModelEntity(vendorLicenseModelId, version));
    vendorLicenseModelDao.update(retrieved);
    //        entity.setLastModificationTime(new Date());
    //
    //        vendorLicenseModelDao.updateLastModificationTime(entity);
  }

  private void validateCompletedVendorLicenseModel(String vendorLicenseModelId, String user) {
    Version activeVersion =
        getVersionInfo(vendorLicenseModelId, VersionableEntityAction.Read, user).getActiveVersion();
    Collection<LicenseAgreementEntity> licenseAgreements = licenseAgreementDao
        .list(new LicenseAgreementEntity(vendorLicenseModelId, activeVersion, null));

    if (licenseAgreements == null || licenseAgreements.isEmpty()) {
      throw new CoreException(
          new SubmitUncompletedLicenseModelErrorBuilder(vendorLicenseModelId).build());
    }

    for (LicenseAgreementEntity licenseAgreement : licenseAgreements) {
      if (licenseAgreement.getFeatureGroupIds() == null
          || licenseAgreement.getFeatureGroupIds().isEmpty()) {
        throw new CoreException(
            new SubmitUncompletedLicenseModelErrorBuilder(vendorLicenseModelId).build());
      }
    }

    Collection<FeatureGroupEntity> featureGroupEntities = featureGroupDao
        .list(new FeatureGroupEntity(vendorLicenseModelId, activeVersion, null));
    for (FeatureGroupEntity featureGroupEntity : featureGroupEntities) {
      if (featureGroupEntity.getEntitlementPoolIds() == null
          || featureGroupEntity.getEntitlementPoolIds().isEmpty()) {
        throw new CoreException(
            new SubmitUncompletedLicenseModelErrorBuilder(vendorLicenseModelId).build());
      }
    }
  }
}
