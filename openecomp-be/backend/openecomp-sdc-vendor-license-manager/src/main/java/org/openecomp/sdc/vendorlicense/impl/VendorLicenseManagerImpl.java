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

import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activityLog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
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
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdcrests.activitylog.types.ActivityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE;

public class VendorLicenseManagerImpl implements VendorLicenseManager {
  private static final VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();
  private static final VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();
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
  private ActivityLogManager activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final Logger logger =
      LoggerFactory.getLogger(VendorLicenseManagerImpl.class);

  private static void sortVlmListByModificationTimeDescOrder(
      List<VersionedVendorLicenseModel> vendorLicenseModels) {
    vendorLicenseModels.sort((o1, o2) -> o2.getVendorLicenseModel().getWritetimeMicroSeconds()
        .compareTo(o1.getVendorLicenseModel().getWritetimeMicroSeconds()));
  }

  @Override
  public void checkout(String vendorLicenseModelId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vendorLicenseModelId);

    Version newVersion = versioningManager
        .checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vendorLicenseModelId, user);

    ActivityLogEntity activityLogEntity = new ActivityLogEntity(vendorLicenseModelId, String.valueOf(newVersion.getMajor()+1),
            ActivityType.CHECKOUT.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);

    newVersion.setStatus(VersionStatus.Locked);
    vendorLicenseFacade.updateVlmLastModificationTime(vendorLicenseModelId, newVersion);

    mdcDataDebugMessage.debugExitMessage("VLM id", vendorLicenseModelId);
  }

  @Override
  public void undoCheckout(String vendorLicenseModelId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vendorLicenseModelId);

    Version newVersion = versioningManager
        .undoCheckout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vendorLicenseModelId, user);
    vendorLicenseFacade.updateVlmLastModificationTime(vendorLicenseModelId, newVersion);

    mdcDataDebugMessage.debugExitMessage("VLM id", vendorLicenseModelId);
  }

  @Override
  public void checkin(String vendorLicenseModelId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vendorLicenseModelId);

    Version newVersion = vendorLicenseFacade.checkin(vendorLicenseModelId, user);

    ActivityLogEntity activityLogEntity = new ActivityLogEntity(vendorLicenseModelId,
            String.valueOf(newVersion.getMajor()+1), ActivityType.CHECKIN.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);

    mdcDataDebugMessage.debugExitMessage("VLM id", vendorLicenseModelId);
  }

  @Override
  public void submit(String vendorLicenseModelId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id", vendorLicenseModelId);

    Version newVersion = vendorLicenseFacade.submit(vendorLicenseModelId, user);

    ActivityLogEntity activityLogEntity = new ActivityLogEntity(vendorLicenseModelId, String.valueOf(newVersion.getMajor()),
            ActivityType.SUBMIT.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);

    mdcDataDebugMessage.debugExitMessage("VLM id", vendorLicenseModelId);
  }

  @Override
  public Collection<VersionedVendorLicenseModel> listVendorLicenseModels(String versionFilter,
                                                                         String user) {
    mdcDataDebugMessage.debugEntryMessage(null);

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

      Version version = versionInfo.getActiveVersion();
      if (user.equals(versionInfo.getLockingUser())) {
        version.setStatus(VersionStatus.Locked);
      }

      try {
        VendorLicenseModelEntity vlm =
            vendorLicenseModelDao.get(new VendorLicenseModelEntity(entry.getKey(), version));
        if (vlm != null) {
          VersionedVendorLicenseModel versionedVlm = new VersionedVendorLicenseModel();
          versionedVlm.setVendorLicenseModel(vlm);
          versionedVlm.setVersionInfo(versionInfo);
          vendorLicenseModels.add(versionedVlm);
        }
      }catch(RuntimeException rte){
      logger.error("Error trying to retrieve vlm["+entry.getKey()+"] version["+version.toString
          ()+"] " +
          "message:"+rte
          .getMessage());
    }
    }

    sortVlmListByModificationTimeDescOrder(vendorLicenseModels);

    mdcDataDebugMessage.debugExitMessage(null);
    return vendorLicenseModels;
  }

  @Override
  public VendorLicenseModelEntity createVendorLicenseModel(VendorLicenseModelEntity vendorLicenseModelEntity, String user) {

    VendorLicenseModelEntity vendorLicenseModelCreated = vendorLicenseFacade.createVendorLicenseModel(vendorLicenseModelEntity, user);

    ActivityLogEntity activityLogEntity = new ActivityLogEntity(vendorLicenseModelCreated.getId(),
            String.valueOf(vendorLicenseModelCreated.getVersion().getMajor()+1),
            ActivityType.CREATE_NEW.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);

    return vendorLicenseModelCreated;
  }

  @Override
  public void updateVendorLicenseModel(VendorLicenseModelEntity vendorLicenseModelEntity,
                                       String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vendorLicenseModelEntity.getId());

    Version version = VersioningUtil.resolveVersion(null,
        getVersionInfo(vendorLicenseModelEntity.getId(), VersionableEntityAction.Write, user),
        user);
    vendorLicenseModelEntity.setVersion(version);

    String existingVendorName = vendorLicenseModelDao.get(vendorLicenseModelEntity).getVendorName();
    UniqueValueUtil
        .updateUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, existingVendorName,
            vendorLicenseModelEntity.getVendorName());
    vendorLicenseModelDao.update(vendorLicenseModelEntity);

    vendorLicenseFacade
        .updateVlmLastModificationTime(vendorLicenseModelEntity.getId(), version);

    mdcDataDebugMessage.debugExitMessage("VLM id", vendorLicenseModelEntity.getId());
  }

  @Override
  public VersionedVendorLicenseModel getVendorLicenseModel(String vlmId, Version version,
                                                           String user) {
    return vendorLicenseFacade.getVendorLicenseModel(vlmId, version, user);
  }

  @Override
  public void deleteVendorLicenseModel(String vlmId, String user) {
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
        LoggerTragetServiceName.DELETE_ENTITY, ErrorLevel.ERROR.name(),
        LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.UNSUPPORTED_OPERATION);
    throw new UnsupportedOperationException(VendorLicenseConstants.UNSUPPORTED_OPERATION_ERROR);
  }

  @Override
  public Collection<LicenseAgreementEntity> listLicenseAgreements(String vlmId, Version version,
                                                                  String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return licenseAgreementDao.list(new LicenseAgreementEntity(vlmId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user), user),
        null));
  }

  @Override
  public LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                                       String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", licenseAgreement.getVendorLicenseModelId());
    mdcDataDebugMessage
        .debugExitMessage("VLM id", licenseAgreement.getVendorLicenseModelId());
    return vendorLicenseFacade.createLicenseAgreement(licenseAgreement, user);
  }

  @Override
  public void updateLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                     Set<String> addedFeatureGroupIds,
                                     Set<String> removedFeatureGroupIds, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", licenseAgreement
        .getVendorLicenseModelId(), licenseAgreement.getId());

    Version version = VersioningUtil.resolveVersion(licenseAgreement.getVersion(),
        getVersionInfo(licenseAgreement.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user);
    licenseAgreement.setVersion(version);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(licenseAgreement);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseAgreement, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateContainedEntitiesExistence(new FeatureGroupEntity().getEntityType(),
        removedFeatureGroupIds, retrieved, retrieved.getFeatureGroupIds());
    VersioningUtil.validateEntitiesExistence(addedFeatureGroupIds,
        new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(), version, null),
        featureGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);

    UniqueValueUtil.updateUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        retrieved.getName(), licenseAgreement.getName(), licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().toString());
    licenseAgreementDao.updateColumnsAndDeltaFeatureGroupIds(licenseAgreement, addedFeatureGroupIds,
        removedFeatureGroupIds);

    addFeatureGroupsToLicenseAgreementRef(addedFeatureGroupIds, licenseAgreement);
    removeFeatureGroupsToLicenseAgreementRef(removedFeatureGroupIds, licenseAgreement);

    vendorLicenseFacade
        .updateVlmLastModificationTime(licenseAgreement.getVendorLicenseModelId(), version);

    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", licenseAgreement
        .getVendorLicenseModelId(), licenseAgreement.getId());
  }

  @Override
  public LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                        String licenseAgreementId, String user) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", vlmId, licenseAgreementId);
    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", vlmId, licenseAgreementId);
    return vendorLicenseFacade.getLicenseAgreementModel(vlmId, version, licenseAgreementId, user);
  }

  @Override
  public void deleteLicenseAgreement(String vlmId, Version version, String licenseAgreementId,
                                     String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", vlmId, licenseAgreementId);

    version = VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Write, user), user);
    LicenseAgreementEntity input =
        new LicenseAgreementEntity(vlmId, version, licenseAgreementId);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(input);
    VersioningUtil.validateEntityExistence(retrieved, input, VendorLicenseModelEntity.ENTITY_TYPE);

    removeFeatureGroupsToLicenseAgreementRef(retrieved.getFeatureGroupIds(), retrieved);

    licenseAgreementDao.delete(input);
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    vendorLicenseFacade
        .updateVlmLastModificationTime(input.getVendorLicenseModelId(), input.getVersion());

    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", vlmId, licenseAgreementId);
  }

  @Override
  public Collection<FeatureGroupEntity> listFeatureGroups(String vlmId, Version version,
                                                          String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return featureGroupDao.list(new FeatureGroupEntity(vlmId, VersioningUtil
        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user), user),
        null));
  }

  @Override
  public FeatureGroupEntity createFeatureGroup(FeatureGroupEntity featureGroup, String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", featureGroup.getVendorLicenseModelId());
    mdcDataDebugMessage.debugExitMessage("VLM id", featureGroup.getId());
    return vendorLicenseFacade.createFeatureGroup(featureGroup, user);
  }

  @Override
  public void updateFeatureGroup(FeatureGroupEntity featureGroup,
                                 Set<String> addedLicenseKeyGroups,
                                 Set<String> removedLicenseKeyGroups,
                                 Set<String> addedEntitlementPools,
                                 Set<String> removedEntitlementPools,
                                 String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id", featureGroup
        .getVendorLicenseModelId(), featureGroup.getId());

    Version version = VersioningUtil.resolveVersion(featureGroup.getVersion(),
        getVersionInfo(featureGroup.getVendorLicenseModelId(), VersionableEntityAction.Write, user),
        user);
    featureGroup.setVersion(version);

    FeatureGroupEntity retrieved = featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    VersioningUtil.validateContainedEntitiesExistence(new LicenseKeyGroupEntity().getEntityType(),
        removedLicenseKeyGroups, retrieved, retrieved.getLicenseKeyGroupIds());
    VersioningUtil.validateContainedEntitiesExistence(new EntitlementPoolEntity().getEntityType(),
        removedEntitlementPools, retrieved, retrieved.getEntitlementPoolIds());

    VersioningUtil.validateEntitiesExistence(addedLicenseKeyGroups,
        new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(), version, null),
        licenseKeyGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateEntitiesExistence(addedEntitlementPools,
        new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), version, null),
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
        .updateVlmLastModificationTime(featureGroup.getVendorLicenseModelId(), version);

    mdcDataDebugMessage.debugExitMessage("VLM id, FG id", featureGroup
        .getVendorLicenseModelId(), featureGroup.getId());
  }

  @Override
  public FeatureGroupModel getFeatureGroupModel(FeatureGroupEntity featureGroup, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id",
        featureGroup.getVendorLicenseModelId(), featureGroup.getId());

    mdcDataDebugMessage.debugExitMessage("VLM id, FG id",
        featureGroup.getVendorLicenseModelId(), featureGroup.getId());
    return vendorLicenseFacade.getFeatureGroupModel(featureGroup, user);
  }

  @Override
  public void deleteFeatureGroup(FeatureGroupEntity featureGroup, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id",
        featureGroup.getVendorLicenseModelId(), featureGroup.getId());

    Version version = VersioningUtil.resolveVersion(featureGroup.getVersion(),
        getVersionInfo(featureGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user);
    featureGroup.setVersion(version);
    FeatureGroupEntity retrieved = featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    removeLicenseKeyGroupsToFeatureGroupsRef(retrieved.getLicenseKeyGroupIds(), featureGroup);
    removeEntitlementPoolsToFeatureGroupsRef(retrieved.getEntitlementPoolIds(), featureGroup);

    for (String licenceAgreementId : retrieved.getReferencingLicenseAgreements()) {
      licenseAgreementDao.removeFeatureGroup(
          new LicenseAgreementEntity(featureGroup.getVendorLicenseModelId(), version,
              licenceAgreementId), featureGroup.getId());
    }

    featureGroupDao.delete(featureGroup);
    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    vendorLicenseFacade.updateVlmLastModificationTime(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion());

    mdcDataDebugMessage
        .debugExitMessage("VLM id, FG id",
            featureGroup.getVendorLicenseModelId(), featureGroup.getId());
  }

  @Override
  public Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version,
                                                                String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return vendorLicenseFacade.listEntitlementPools(vlmId, version, user);
  }

  @Override
  public EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool,
                                                     String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", entitlementPool.getVendorLicenseModelId());
    mdcDataDebugMessage
        .debugExitMessage("VLM id", entitlementPool.getVendorLicenseModelId());
    return vendorLicenseFacade.createEntitlementPool(entitlementPool, user);
  }

  @Override
  public void updateEntitlementPool(EntitlementPoolEntity entitlementPool, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());

    Version version = VersioningUtil.resolveVersion(entitlementPool.getVersion(),
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user);
    vendorLicenseFacade
        .updateVlmLastModificationTime(entitlementPool.getVendorLicenseModelId(), version);
    vendorLicenseFacade.updateEntitlementPool(entitlementPool, user);

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());
  }

  @Override
  public EntitlementPoolEntity getEntitlementPool(EntitlementPoolEntity entitlementPool,
                                                  String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());

    entitlementPool.setVersion(VersioningUtil.resolveVersion(entitlementPool.getVersion(),
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Read,
            user), user));

    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());
    return retrieved;
  }

  @Override
  public void deleteEntitlementPool(EntitlementPoolEntity entitlementPool, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());

    Version version = VersioningUtil.resolveVersion(entitlementPool.getVersion(),
        getVersionInfo(entitlementPool.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user);
    entitlementPool.setVersion(version);

    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);

    for (String referencingFeatureGroupId : retrieved.getReferencingFeatureGroups()) {
      featureGroupDao.removeEntitlementPool(
          new FeatureGroupEntity(entitlementPool.getVendorLicenseModelId(), version,
              referencingFeatureGroupId), entitlementPool.getId());
    }

    entitlementPoolDao.delete(entitlementPool);

    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    vendorLicenseFacade.updateVlmLastModificationTime(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion());

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());
  }

  @Override
  public Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version,
                                                                String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return vendorLicenseFacade.listLicenseKeyGroups(vlmId, version, user);
  }

  @Override
  public LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                                     String user) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", licenseKeyGroup.getVendorLicenseModelId());

    mdcDataDebugMessage.debugExitMessage("VLM id", licenseKeyGroup
        .getVendorLicenseModelId());
    return vendorLicenseFacade.createLicenseKeyGroup(licenseKeyGroup, user);
  }

  @Override
  public void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());

    Version version = VersioningUtil.resolveVersion(licenseKeyGroup.getVersion(),
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user);
    vendorLicenseFacade
        .updateVlmLastModificationTime(licenseKeyGroup.getVendorLicenseModelId(), version);

    vendorLicenseFacade.updateLicenseKeyGroup(licenseKeyGroup, user);

    mdcDataDebugMessage.debugExitMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());
  }

  @Override
  public LicenseKeyGroupEntity getLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                                  String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());

    licenseKeyGroup.setVersion(VersioningUtil.resolveVersion(licenseKeyGroup.getVersion(),
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Read,
            user), user));

    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    mdcDataDebugMessage.debugExitMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());
    return retrieved;
  }

  @Override
  public void deleteLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());

    Version version = VersioningUtil.resolveVersion(licenseKeyGroup.getVersion(),
        getVersionInfo(licenseKeyGroup.getVendorLicenseModelId(), VersionableEntityAction.Write,
            user), user);
    licenseKeyGroup.setVersion(version);

    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    for (String referencingFeatureGroupId : retrieved.getReferencingFeatureGroups()) {
      featureGroupDao.removeLicenseKeyGroup(
          new FeatureGroupEntity(licenseKeyGroup.getVendorLicenseModelId(), version,
              referencingFeatureGroupId), licenseKeyGroup.getId());
    }

    licenseKeyGroupDao.delete(licenseKeyGroup);

    UniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.LICENSE_KEY_GROUP_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    vendorLicenseFacade.updateVlmLastModificationTime(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion());

    mdcDataDebugMessage.debugExitMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());
  }

  private void addFeatureGroupsToLicenseAgreementRef(Set<String> featureGroupIds,
                                                     LicenseAgreementEntity licenseAgreement) {
    if (featureGroupIds != null) {
      for (String featureGroupId : featureGroupIds) {
        featureGroupDao.addReferencingLicenseAgreement(
            new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(),
                licenseAgreement.getVersion(), featureGroupId), licenseAgreement.getId());
      }
    }
  }

  private void removeFeatureGroupsToLicenseAgreementRef(Set<String> featureGroupIds,
                                                        LicenseAgreementEntity licenseAgreement) {
    if (featureGroupIds != null) {
      for (String featureGroupId : featureGroupIds) {
        featureGroupDao.removeReferencingLicenseAgreement(
            new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(),
                licenseAgreement.getVersion(), featureGroupId), licenseAgreement.getId());
      }
    }
  }

  private void addLicenseKeyGroupsToFeatureGroupsRef(Set<String> licenseKeyGroupIds,
                                                     FeatureGroupEntity featureGroup) {
    if (licenseKeyGroupIds != null) {
      for (String licenseKeyGroupId : licenseKeyGroupIds) {
        licenseKeyGroupDao.addReferencingFeatureGroup(
            new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), licenseKeyGroupId), featureGroup.getId());
      }
    }
  }

  private void removeLicenseKeyGroupsToFeatureGroupsRef(Set<String> licenseKeyGroupIds,
                                                        FeatureGroupEntity featureGroup) {
    if (licenseKeyGroupIds != null) {
      for (String licenseKeyGroupId : licenseKeyGroupIds) {
        licenseKeyGroupDao.removeReferencingFeatureGroup(
            new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), licenseKeyGroupId), featureGroup.getId());
      }
    }
  }

  private void addEntitlementPoolsToFeatureGroupsRef(Set<String> entitlementPoolIds,
                                                     FeatureGroupEntity featureGroup) {
    if (entitlementPoolIds != null) {
      for (String entitlementPoolId : entitlementPoolIds) {
        entitlementPoolDao.addReferencingFeatureGroup(
            new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(),
                featureGroup.getVersion(), entitlementPoolId), featureGroup.getId());
      }
    }
  }

  private void removeEntitlementPoolsToFeatureGroupsRef(Set<String> entitlementPoolIds,
                                                        FeatureGroupEntity featureGroup) {
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
