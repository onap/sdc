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

package org.openecomp.sdc.vendorlicense.impl;

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants
    .VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE;

import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VendorLicenseManagerImpl implements VendorLicenseManager {

  private static final VersioningManager versioningManager =
      org.openecomp.sdc.versioning.VersioningManagerFactory.getInstance().createInterface();
  private static final VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();

  private static final VendorLicenseModelDao
      vendorLicenseModelDao = VendorLicenseModelDaoFactory.getInstance().createInterface();
  private static final org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao
      licenseAgreementDao = LicenseAgreementDaoFactory.getInstance().createInterface();
  private static final FeatureGroupDao featureGroupDao =
      FeatureGroupDaoFactory.getInstance().createInterface();
  private static final EntitlementPoolDao
      entitlementPoolDao = EntitlementPoolDaoFactory.getInstance().createInterface();
  private static final LicenseKeyGroupDao
      licenseKeyGroupDao = LicenseKeyGroupDaoFactory.getInstance().createInterface();

  private static void sortVlmListByModificationTimeDescOrder(
      List<VersionedVendorLicenseModel> vendorLicenseModels) {
    Collections.sort(vendorLicenseModels, new Comparator<VersionedVendorLicenseModel>() {
      @Override
      public int compare(VersionedVendorLicenseModel o1, VersionedVendorLicenseModel o2) {
        return o2.getVendorLicenseModel().getWritetimeMicroSeconds()
            .compareTo(o1.getVendorLicenseModel().getWritetimeMicroSeconds());
      }
    });
  }

  @Override
  public void checkout(String vendorLicenseModelId, String user) {
    Version newVersion = versioningManager
        .checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vendorLicenseModelId, user);
    vendorLicenseFacade.updateVlmLastModificationTime(vendorLicenseModelId, newVersion);
  }

  @Override
  public void undoCheckout(String vendorLicenseModelId, String user) {
    Version newVersion = versioningManager
        .undoCheckout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vendorLicenseModelId, user);
    vendorLicenseFacade.updateVlmLastModificationTime(vendorLicenseModelId, newVersion);
  }

  @Override
  public void checkin(String vendorLicenseModelId, String user) {
    vendorLicenseFacade.checkin(vendorLicenseModelId, user);
  }

  @Override
  public void submit(String vendorLicenseModelId, String user) {
    vendorLicenseFacade.submit(vendorLicenseModelId, user);
  }

  @Override
  public Collection<VersionedVendorLicenseModel> listVendorLicenseModels(String versionFilter,
                                                                         String user) {
    Map<String, VersionInfo> idToVersionsInfo = versioningManager
        .listEntitiesVersionInfo(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, user,
            VersionableEntityAction.Read);

    List<VersionedVendorLicenseModel> vendorLicenseModels = new ArrayList<>();
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

      VendorLicenseModelEntity
          vlm = vendorLicenseModelDao
          .get(new VendorLicenseModelEntity(entry.getKey(), versionInfo.getActiveVersion()));
      if (vlm != null) {
        VersionedVendorLicenseModel versionedVlm = new VersionedVendorLicenseModel();
        versionedVlm.setVendorLicenseModel(vlm);
        versionedVlm.setVersionInfo(versionInfo);
        vendorLicenseModels.add(versionedVlm);
      }
    }

    sortVlmListByModificationTimeDescOrder(vendorLicenseModels);

    return vendorLicenseModels;
  }

  @Override
  public VendorLicenseModelEntity createVendorLicenseModel(
      VendorLicenseModelEntity vendorLicenseModelEntity, String user) {
    return vendorLicenseFacade.createVendorLicenseModel(vendorLicenseModelEntity, user);
  }

  @Override
  public void updateVendorLicenseModel(VendorLicenseModelEntity vendorLicenseModelEntity,
                                       String user) {
    Version activeVersion =
        getVersionInfo(vendorLicenseModelEntity.getId(), VersionableEntityAction.Write, user)
            .getActiveVersion();
    vendorLicenseModelEntity.setVersion(activeVersion);

    String existingVendorName = vendorLicenseModelDao.get(vendorLicenseModelEntity).getVendorName();
    UniqueValueUtil
        .updateUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, existingVendorName,
            vendorLicenseModelEntity.getVendorName());
    vendorLicenseModelDao.update(vendorLicenseModelEntity);

    vendorLicenseFacade
        .updateVlmLastModificationTime(vendorLicenseModelEntity.getId(), activeVersion);
  }

  @Override
  public VersionedVendorLicenseModel getVendorLicenseModel(String vlmId, Version version,
                                                           String user) {
    return vendorLicenseFacade.getVendorLicenseModel(vlmId, version, user);
  }

  @Override
  public void deleteVendorLicenseModel(String vlmId, String user) {
    throw new UnsupportedOperationException("Unsupported operation for 1607 release.");

    /*        Version activeVersion = getVersionInfo(vlmId, VersionableEntityAction.Write, user)
    .getActiveVersion();

        vendorLicenseModelDao.delete(new VendorLicenseModelEntity(vlmId, activeVersion));
        licenseAgreementDao.deleteAll(new LicenseAgreementEntity(vlmId, activeVersion, null));
        featureGroupDao.deleteAll(new FeatureGroupEntity(vlmId, activeVersion, null));
        licenseKeyGroupDao.deleteAll(new LicenseKeyGroupEntity(vlmId, activeVersion, null));
        entitlementPoolDao.deleteAll(new EntitlementPoolEntity(vlmId, activeVersion, null));*/
  }

  @Override
  public Collection<LicenseAgreementEntity> listLicenseAgreements(String vlmId, Version version,
                                                                  String user) {
    return licenseAgreementDao.list(new LicenseAgreementEntity(vlmId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user)), null));
  }

  @Override
  public LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                                       String user) {
    return vendorLicenseFacade.createLicenseAgreement(licenseAgreement, user);
  }

  @Override
  public void updateLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                     Set<String> addedFeatureGroupIds,
                                     Set<String> removedFeatureGroupIds, String user) {
    Version activeVersion =
        getVersionInfo(licenseAgreement.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion();
    licenseAgreement.setVersion(activeVersion);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(licenseAgreement);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseAgreement, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateContainedEntitiesExistence(
        new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity().getEntityType(),
        removedFeatureGroupIds, retrieved, retrieved.getFeatureGroupIds());
    VersioningUtil.validateEntitiesExistence(addedFeatureGroupIds,
        new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(
            licenseAgreement.getVendorLicenseModelId(), activeVersion, null),
        featureGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);

    UniqueValueUtil.updateUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        retrieved.getName(), licenseAgreement.getName(), licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().toString());
    licenseAgreementDao.updateColumnsAndDeltaFeatureGroupIds(licenseAgreement, addedFeatureGroupIds,
        removedFeatureGroupIds);

    addFeatureGroupsToLicenseAgreementRef(addedFeatureGroupIds, licenseAgreement);
    removeFeatureGroupsToLicenseAgreementRef(removedFeatureGroupIds, licenseAgreement);

    vendorLicenseFacade
        .updateVlmLastModificationTime(licenseAgreement.getVendorLicenseModelId(), activeVersion);
  }

  @Override
  public LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                        String licenseAgreementId, String user) {
    return vendorLicenseFacade.getLicenseAgreementModel(vlmId, version, licenseAgreementId, user);
  }

  @Override
  public void deleteLicenseAgreement(String vlmId, String licenseAgreementId, String user) {
    Version activeVersion =
        getVersionInfo(vlmId, VersionableEntityAction.Write, user).getActiveVersion();
    LicenseAgreementEntity input =
        new LicenseAgreementEntity(vlmId, activeVersion, licenseAgreementId);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(input);
    VersioningUtil.validateEntityExistence(retrieved, input, VendorLicenseModelEntity.ENTITY_TYPE);

    removeFeatureGroupsToLicenseAgreementRef(retrieved.getFeatureGroupIds(), retrieved);

    licenseAgreementDao.delete(input);
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    vendorLicenseFacade
        .updateVlmLastModificationTime(input.getVendorLicenseModelId(), input.getVersion());
  }

  @Override
  public Collection<org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity> listFeatureGroups(
      String vlmId, Version version,
      String user) {
    return featureGroupDao
        .list(new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(vlmId, VersioningUtil
            .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user)),
            null));
  }

  @Override
  public org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity createFeatureGroup(
      org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup, String user) {
    return vendorLicenseFacade.createFeatureGroup(featureGroup, user);
  }

  @Override
  public void updateFeatureGroup(
      org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup,
      Set<String> addedLicenseKeyGroups,
      Set<String> removedLicenseKeyGroups,
      Set<String> addedEntitlementPools,
      Set<String> removedEntitlementPools,
      String user) {
    Version activeVersion =
        getVersionInfo(featureGroup.getVendorLicenseModelId(), VersionableEntityAction.Write, user)
            .getActiveVersion();
    featureGroup.setVersion(activeVersion);

    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity retrieved =
        featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    VersioningUtil.validateContainedEntitiesExistence(new LicenseKeyGroupEntity().getEntityType(),
        removedLicenseKeyGroups, retrieved, retrieved.getLicenseKeyGroupIds());
    VersioningUtil.validateContainedEntitiesExistence(new EntitlementPoolEntity().getEntityType(),
        removedEntitlementPools, retrieved, retrieved.getEntitlementPoolIds());

    VersioningUtil.validateEntitiesExistence(addedLicenseKeyGroups,
        new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(), activeVersion, null),
        licenseKeyGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateEntitiesExistence(addedEntitlementPools,
        new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), activeVersion, null),
        entitlementPoolDao, VendorLicenseModelEntity.ENTITY_TYPE);
    UniqueValueUtil.updateUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        retrieved.getName(), featureGroup.getName(), featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().toString());

    addLicenseKeyGroupsToFeatureGroupsRef(addedLicenseKeyGroups, featureGroup);
    removeLicenseKeyGroupsToFeatureGroupsRef(removedLicenseKeyGroups, featureGroup);
    addEntitlementPoolsToFeatureGroupsRef(addedEntitlementPools, featureGroup);
    removeEntitlementPoolsToFeatureGroupsRef(removedEntitlementPools, featureGroup);

    featureGroupDao.updateFeatureGroup(featureGroup, addedEntitlementPools, removedEntitlementPools,
        addedLicenseKeyGroups, removedLicenseKeyGroups);

    vendorLicenseFacade
        .updateVlmLastModificationTime(featureGroup.getVendorLicenseModelId(), activeVersion);
  }

  @Override
  public FeatureGroupModel getFeatureGroupModel(
      org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup, String user) {
    return vendorLicenseFacade.getFeatureGroupModel(featureGroup, user);
  }

  @Override
  public void deleteFeatureGroup(
      org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup, String user) {
    Version activeVersion =
        getVersionInfo(featureGroup.getVendorLicenseModelId(), VersionableEntityAction.Write, user)
            .getActiveVersion();
    featureGroup.setVersion(activeVersion);
    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity retrieved =
        featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    removeLicenseKeyGroupsToFeatureGroupsRef(retrieved.getLicenseKeyGroupIds(), featureGroup);
    removeEntitlementPoolsToFeatureGroupsRef(retrieved.getEntitlementPoolIds(), featureGroup);

    for (String licenceAgreementId : retrieved.getReferencingLicenseAgreements()) {
      licenseAgreementDao.removeFeatureGroup(
          new LicenseAgreementEntity(featureGroup.getVendorLicenseModelId(), activeVersion,
              licenceAgreementId), featureGroup.getId());
    }

    featureGroupDao.delete(featureGroup);
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    vendorLicenseFacade.updateVlmLastModificationTime(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion());
  }

  @Override
  public Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version,
                                                                String user) {
    return vendorLicenseFacade.listEntitlementPools(vlmId, version, user);
  }

  @Override
  public EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool,
                                                     String user) {
    return vendorLicenseFacade.createEntitlementPool(entitlementPool, user);
  }

  @Override
  public void updateEntitlementPool(EntitlementPoolEntity entitlementPool, String user) {
    Version activeVersion =
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion();
    vendorLicenseFacade
        .updateVlmLastModificationTime(entitlementPool.getVendorLicenseModelId(), activeVersion);
    vendorLicenseFacade.updateEntitlementPool(entitlementPool, user);
  }

  @Override
  public EntitlementPoolEntity getEntitlementPool(EntitlementPoolEntity entitlementPool,
                                                  String user) {
    entitlementPool.setVersion(VersioningUtil.resolveVersion(entitlementPool.getVersion(),
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Read,
            user)));

    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public void deleteEntitlementPool(EntitlementPoolEntity entitlementPool, String user) {
    Version activeVersion =
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion();
    entitlementPool.setVersion(activeVersion);

    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);

    for (String referencingFeatureGroupId : retrieved.getReferencingFeatureGroups()) {
      featureGroupDao.removeEntitlementPool(
          new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(
              entitlementPool.getVendorLicenseModelId(), activeVersion,
              referencingFeatureGroupId), entitlementPool.getId());
    }

    entitlementPoolDao.delete(entitlementPool);
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    vendorLicenseFacade.updateVlmLastModificationTime(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion());
  }

  @Override
  public Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version,
                                                                String user) {
    return vendorLicenseFacade.listLicenseKeyGroups(vlmId, version, user);
  }

  @Override
  public LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                                     String user) {
    return vendorLicenseFacade.createLicenseKeyGroup(licenseKeyGroup, user);
  }

  @Override
  public void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user) {
    Version activeVersion =
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion();
    vendorLicenseFacade
        .updateVlmLastModificationTime(licenseKeyGroup.getVendorLicenseModelId(), activeVersion);

    vendorLicenseFacade.updateLicenseKeyGroup(licenseKeyGroup, user);
  }

  @Override
  public LicenseKeyGroupEntity getLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                                  String user) {
    licenseKeyGroup.setVersion(VersioningUtil.resolveVersion(licenseKeyGroup.getVersion(),
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Read,
            user)));

    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public void deleteLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user) {
    Version activeVersion =
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user).getActiveVersion();
    licenseKeyGroup.setVersion(activeVersion);

    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    licenseKeyGroupDao.delete(licenseKeyGroup);
    for (String referencingFeatureGroupId : retrieved.getReferencingFeatureGroups()) {
      featureGroupDao.removeLicenseKeyGroup(
          new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(
              licenseKeyGroup.getVendorLicenseModelId(), activeVersion,
              referencingFeatureGroupId), licenseKeyGroup.getId());
    }
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_KEY_GROUP_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    vendorLicenseFacade.updateVlmLastModificationTime(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion());
  }

  private void addFeatureGroupsToLicenseAgreementRef(Set<String> featureGroupIds,
                                                     LicenseAgreementEntity licenseAgreement) {
    if (featureGroupIds != null) {
      for (String featureGroupId : featureGroupIds) {
        featureGroupDao.addReferencingLicenseAgreement(
            new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(
                licenseAgreement.getVendorLicenseModelId(),
                licenseAgreement.getVersion(), featureGroupId), licenseAgreement.getId());
      }
    }
  }

  private void removeFeatureGroupsToLicenseAgreementRef(Set<String> featureGroupIds,
                                                        LicenseAgreementEntity licenseAgreement) {
    if (featureGroupIds != null) {
      for (String featureGroupId : featureGroupIds) {
        featureGroupDao.removeReferencingLicenseAgreement(
            new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(
                licenseAgreement.getVendorLicenseModelId(),
                licenseAgreement.getVersion(), featureGroupId), licenseAgreement.getId());
      }
    }
  }

  private void addLicenseKeyGroupsToFeatureGroupsRef(Set<String> licenseKeyGroupIds,
              org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup) {
    if (licenseKeyGroupIds != null) {
      for (String licenseKeyGroupId : licenseKeyGroupIds) {
        licenseKeyGroupDao.addReferencingFeatureGroup(
            new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), licenseKeyGroupId), featureGroup.getId());
      }
    }
  }

  private void removeLicenseKeyGroupsToFeatureGroupsRef(Set<String> licenseKeyGroupIds,
               org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup) {
    if (licenseKeyGroupIds != null) {
      for (String licenseKeyGroupId : licenseKeyGroupIds) {
        licenseKeyGroupDao.removeReferencingFeatureGroup(
            new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), licenseKeyGroupId), featureGroup.getId());
      }
    }
  }

  private void addEntitlementPoolsToFeatureGroupsRef(Set<String> entitlementPoolIds,
              org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup) {
    if (entitlementPoolIds != null) {
      for (String entitlementPoolId : entitlementPoolIds) {
        entitlementPoolDao.addReferencingFeatureGroup(
            new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), entitlementPoolId), featureGroup.getId());
      }
    }
  }

  private void removeEntitlementPoolsToFeatureGroupsRef(Set<String> entitlementPoolIds,
               org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity featureGroup) {
    if (entitlementPoolIds != null) {
      for (String entitlementPoolId : entitlementPoolIds) {
        entitlementPoolDao.removeReferencingFeatureGroup(
            new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), entitlementPoolId), featureGroup.getId());
      }
    }
  }

  private VersionInfo getVersionInfo(String vendorLicenseModelId, VersionableEntityAction action,
                                     String user) {
    return vendorLicenseFacade.getVersionInfo(vendorLicenseModelId, action, user);
  }
}
