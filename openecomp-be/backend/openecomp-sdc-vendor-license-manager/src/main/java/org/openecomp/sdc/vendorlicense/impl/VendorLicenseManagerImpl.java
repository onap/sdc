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

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE;

import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.errors.InvalidDateErrorBuilder;
import org.openecomp.sdc.vendorlicense.errors.LimitErrorBuilder;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdcrests.activitylog.types.ActivityType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class VendorLicenseManagerImpl implements VendorLicenseManager {
  private VersioningManager versioningManager;
  private VendorLicenseFacade vendorLicenseFacade;
  private VendorLicenseModelDao vendorLicenseModelDao;
  private LicenseAgreementDao licenseAgreementDao;
  private FeatureGroupDao featureGroupDao;
  private EntitlementPoolDao entitlementPoolDao;
  private LicenseKeyGroupDao licenseKeyGroupDao;
  private LimitDao limitDao;
  private ActivityLogManager activityLogManager;

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static final Logger logger =
          LoggerFactory.getLogger(VendorLicenseManagerImpl.class);

  public VendorLicenseManagerImpl(VersioningManager versioningManager,
                                  VendorLicenseFacade vendorLicenseFacade,
                                  VendorLicenseModelDao vendorLicenseModelDao,
                                  LicenseAgreementDao licenseAgreementDao,
                                  FeatureGroupDao featureGroupDao,
                                  EntitlementPoolDao entitlementPoolDao,
                                  LicenseKeyGroupDao licenseKeyGroupDao,
                                  ActivityLogManager activityLogManager,
                                  LimitDao limitDao) {
    this.versioningManager = versioningManager;
    this.vendorLicenseFacade = vendorLicenseFacade;
    this.vendorLicenseModelDao = vendorLicenseModelDao;
    this.licenseAgreementDao = licenseAgreementDao;
    this.featureGroupDao = featureGroupDao;
    this.entitlementPoolDao = entitlementPoolDao;
    this.licenseKeyGroupDao = licenseKeyGroupDao;
    this.activityLogManager = activityLogManager;
    this.limitDao = limitDao;
  }


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

    ActivityLogEntity activityLogEntity =
        new ActivityLogEntity(vendorLicenseModelId, String.valueOf(newVersion.getMajor() + 1),
            ActivityType.UNDO_CHECKOUT.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);

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

    Version version = resloveVersion(vendorLicenseModelEntity.getId(),null,
            getVersionInfo(vendorLicenseModelEntity.getId(), VersionableEntityAction.Write, user),
            user);
    vendorLicenseModelEntity.setVersion(version);

    String existingVendorName = vendorLicenseModelDao.get(vendorLicenseModelEntity).getVendorName();

    updateUniqueName(VendorLicenseConstants.UniqueValues.VENDOR_NAME, existingVendorName,
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
    LicenseAgreementEntity licenseAgreementEntity =  createLicenseAgreementForList(vlmId, version,
            user);
//    return licenseAgreementDao.list(new LicenseAgreementEntity(vlmId, VersioningUtil
//        .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user), user),
//        null));
    return licenseAgreementDao.list(licenseAgreementEntity);
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

    updateUniqueName(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
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

    licenseAgreementDao.delete(retrieved);

    deleteUniqueName(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
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
    return vendorLicenseFacade.listFeatureGroups(vlmId, version, user);
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

    updateUniqueName(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
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

    deleteUniqueName(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
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

    entitlementPool.setStartDate(entitlementPool.getStartDate() != null ? (entitlementPool
            .getStartDate().trim().length() != 0 ? entitlementPool.getStartDate()+"T00:00:00Z"
            : null) : null);
    entitlementPool.setExpiryDate(entitlementPool.getExpiryDate() != null ? (entitlementPool
            .getExpiryDate().trim().length() != 0 ? entitlementPool.getExpiryDate()+"T23:59:59Z"
            : null) : null);

    validateCreateDate(entitlementPool.getStartDate(), entitlementPool.getExpiryDate(),
            entitlementPool.getVendorLicenseModelId());
    return vendorLicenseFacade.createEntitlementPool(entitlementPool, user);
  }

  private void validateCreateDate(String startDate, String expiryDate, String vendorLicenseModelId){
    mdcDataDebugMessage.debugEntryMessage("Start date and end date", startDate
            +"   "+expiryDate);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss'Z'");

    if(startDate != null && expiryDate != null) {
      if (LocalDate.parse(startDate, formatter).atStartOfDay().isBefore
              (LocalDate.now().atStartOfDay()) || LocalDate.parse(expiryDate, formatter).atStartOfDay()
              .isEqual(LocalDate.parse(startDate, formatter).atStartOfDay()) || LocalDate
              .parse(expiryDate, formatter).isBefore(LocalDate.parse(startDate, formatter))) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
                LoggerTragetServiceName.VALIDATE_DATE_RANGE,ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
        throw new CoreException(
                new InvalidDateErrorBuilder(vendorLicenseModelId)
                        .build());
      }
    }

    if(startDate != null && expiryDate == null) {
      if (LocalDate.parse(startDate, formatter).atStartOfDay().isBefore
              (LocalDate.now().atStartOfDay())) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
                LoggerTragetServiceName.VALIDATE_DATE_RANGE,ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
        throw new CoreException(
                new InvalidDateErrorBuilder(vendorLicenseModelId)
                        .build());
      }
    }

    if(startDate == null && expiryDate != null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.VALIDATE_DATE_RANGE,ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
      throw new CoreException(
              new InvalidDateErrorBuilder(vendorLicenseModelId)
                      .build());

    }

    mdcDataDebugMessage.debugExitMessage(null,null);
  }

  private void validateUpdateDate(String startDate, String expiryDate, String vendorLicenseModelId){
    mdcDataDebugMessage.debugEntryMessage("Start date and end date", startDate
            +"   "+ expiryDate);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss'Z'");

    if(startDate != null && expiryDate != null) {
      if (LocalDate.parse(expiryDate, formatter).atStartOfDay()
              .isEqual(LocalDate.parse(startDate, formatter).atStartOfDay()) ||
              LocalDate.parse(expiryDate, formatter).isBefore(LocalDate.parse(startDate, formatter))) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
                LoggerTragetServiceName.VALIDATE_DATE_RANGE,ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
        throw new CoreException(
                new InvalidDateErrorBuilder(vendorLicenseModelId)
                        .build());
      }
    }

    if(startDate == null && expiryDate != null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.VALIDATE_DATE_RANGE,ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
      throw new CoreException(
              new InvalidDateErrorBuilder(vendorLicenseModelId)
                      .build());

    }

    mdcDataDebugMessage.debugExitMessage(null,null);
  }

  @Override
  public void updateEntitlementPool(EntitlementPoolEntity entitlementPool, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", entitlementPool
            .getVendorLicenseModelId(), entitlementPool.getId());

    entitlementPool.setStartDate(entitlementPool.getStartDate() != null ? (entitlementPool
            .getStartDate().trim().length() != 0 ? entitlementPool.getStartDate()+"T00:00:00Z"
            : null) : null);
    entitlementPool.setExpiryDate(entitlementPool.getExpiryDate() != null ? (entitlementPool
            .getExpiryDate().trim().length() != 0 ? entitlementPool.getExpiryDate()+"T23:59:59Z"
            : null) : null);

    validateUpdateDate(entitlementPool.getStartDate(), entitlementPool.getExpiryDate(),
            entitlementPool.getVendorLicenseModelId());
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

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss'Z'");
    DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    if(retrieved.getStartDate() != null){
      retrieved.setStartDate(LocalDate.parse(retrieved.getStartDate(),formatter).format
              (targetFormatter));
    }

    if(retrieved.getExpiryDate() != null){
      retrieved.setExpiryDate(LocalDate.parse(retrieved.getExpiryDate(),formatter).format
              (targetFormatter));
    }

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

    deleteChildLimits(entitlementPool.getVendorLicenseModelId(), entitlementPool.getVersion(), entitlementPool.getId(), user);

    entitlementPoolDao.delete(entitlementPool);

    deleteUniqueName(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
            retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
            retrieved.getName());

    vendorLicenseFacade.updateVlmLastModificationTime(entitlementPool.getVendorLicenseModelId(),
            entitlementPool.getVersion());

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", entitlementPool
            .getVendorLicenseModelId(), entitlementPool.getId());
  }

  protected void deleteChildLimits(String vlmId, Version version, String epLkgId, String user) {
    Optional<Collection<LimitEntity>> limitEntities = Optional.ofNullable(
            listLimits(vlmId, version, epLkgId, user));
    limitEntities.ifPresent(entities->
            entities.forEach(entity->
                    deleteLimit(entity, user)));
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

    licenseKeyGroup.setStartDate(licenseKeyGroup.getStartDate() != null ? (licenseKeyGroup
            .getStartDate().trim().length() != 0 ? licenseKeyGroup.getStartDate()+"T00:00:00Z"
            : null) : null);
    licenseKeyGroup.setExpiryDate(licenseKeyGroup.getExpiryDate() != null ? (licenseKeyGroup
            .getExpiryDate().trim().length() != 0 ? licenseKeyGroup.getExpiryDate()+"T23:59:59Z"
            : null) : null);

    validateCreateDate(licenseKeyGroup.getStartDate(), licenseKeyGroup.getExpiryDate(),
            licenseKeyGroup.getVendorLicenseModelId());
    return vendorLicenseFacade.createLicenseKeyGroup(licenseKeyGroup, user);
  }

  @Override
  public void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LKG id", licenseKeyGroup
            .getVendorLicenseModelId(), licenseKeyGroup.getId());

    licenseKeyGroup.setStartDate(licenseKeyGroup.getStartDate() != null ? (licenseKeyGroup
            .getStartDate().trim().length() != 0 ? licenseKeyGroup.getStartDate()+"T00:00:00Z"
            : null) : null);
    licenseKeyGroup.setExpiryDate(licenseKeyGroup.getExpiryDate() != null ? (licenseKeyGroup
            .getExpiryDate().trim().length() != 0 ? licenseKeyGroup.getExpiryDate()+"T23:59:59Z"
            : null) : null);

    validateUpdateDate(licenseKeyGroup.getStartDate(), licenseKeyGroup.getExpiryDate(),
            licenseKeyGroup.getVendorLicenseModelId());

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

    deleteChildLimits(licenseKeyGroup.getVendorLicenseModelId(), licenseKeyGroup.getVersion(), licenseKeyGroup.getId(), user);

    licenseKeyGroupDao.delete(licenseKeyGroup);

    deleteUniqueName(VendorLicenseConstants.UniqueValues.LICENSE_KEY_GROUP_NAME,
            retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
            retrieved.getName());

    vendorLicenseFacade.updateVlmLastModificationTime(licenseKeyGroup.getVendorLicenseModelId(),
            licenseKeyGroup.getVersion());

    mdcDataDebugMessage.debugExitMessage("VLM id, LKG id", licenseKeyGroup
            .getVendorLicenseModelId(), licenseKeyGroup.getId());
  }

  @Override
  public LimitEntity createLimit(LimitEntity limit, String user) {
    mdcDataDebugMessage
            .debugEntryMessage("VLM id", limit.getVendorLicenseModelId(), "EP/LKGId", limit
                    .getEpLkgId());
    mdcDataDebugMessage
            .debugExitMessage("VLM id", limit.getVendorLicenseModelId(), "EP/LKGId", limit
                    .getEpLkgId());
    validateLimit(limit, user);
    LimitEntity createdLimit = vendorLicenseFacade.createLimit(limit, user);
    updateParentForLimit(limit,user);
    return createdLimit;
  }

  private void validateLimit(LimitEntity limit, String user) {
    Version version = VersioningUtil.resolveVersion(limit.getVersion(),
            getVersionInfo(limit.getVendorLicenseModelId(), VersionableEntityAction.Write,
                    user), user);
    Collection<LimitEntity> limitList = listLimits(limit.getVendorLicenseModelId(),version
            ,limit.getEpLkgId(), user);

    if (!isLimitNameUnique(limitList,limit.getName(), limit.getType(), limit.getId())) {
      final ErrorCode duplicateLimitNameErrorBuilder =
              LimitErrorBuilder.getDuplicateNameErrorbuilder(limit.getName(), limit.getType().name());
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerServiceName.Create_LIMIT.toString(), ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(),
              duplicateLimitNameErrorBuilder.message());
      throw new CoreException(duplicateLimitNameErrorBuilder);
    }
  }

  private boolean isLimitNameUnique(Collection<LimitEntity> limitList, String name, LimitType
          type, String id) {
    for (LimitEntity limit : limitList) {
      if(limit.getName().equalsIgnoreCase(name) &&
              limit.getType().name().equalsIgnoreCase(type.name())) {
        if(id != null && limit.getId().equals(id)){
          continue;
        }
        return false;
      }
    }
    return true;
  }

  @Override
  public Collection<LimitEntity> listLimits(String vlmId, Version version, String epLkgId,
                                            String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId, "EP/LKGId", epLkgId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId, "EP/LKGId", epLkgId);
    return vendorLicenseFacade.listLimits(vlmId, version, epLkgId, user);
  }

  @Override
  public void deleteLimit(LimitEntity limitEntity, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id, Limit Id", limitEntity
            .getVendorLicenseModelId(), limitEntity.getEpLkgId(), limitEntity.getId());

    Version version = VersioningUtil.resolveVersion(limitEntity.getVersion(),
            getVersionInfo(limitEntity.getVendorLicenseModelId(), VersionableEntityAction.Write,
                    user), user);
    limitEntity.setVersion(version);

    if ( !isLimitPresent(limitEntity)) {
      VersioningUtil
              .validateEntityExistence(null, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);
    }
    LimitEntity retrieved = limitDao.get(limitEntity);
    VersioningUtil
            .validateEntityExistence(retrieved, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);

    limitDao.delete(limitEntity);

    vendorLicenseFacade.updateVlmLastModificationTime(limitEntity.getVendorLicenseModelId(),
            limitEntity.getVersion());

    updateParentForLimit(limitEntity,user);

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id, Limit Id", limitEntity
            .getVendorLicenseModelId(), limitEntity.getEpLkgId(), limitEntity.getId());
  }

  @Override
  public void updateLimit(LimitEntity limit, String user) {
    mdcDataDebugMessage
            .debugEntryMessage("VLM id", limit.getVendorLicenseModelId(), "EP/LKGId", limit
                    .getEpLkgId());
    getLimit(limit,user);
    validateLimit(limit, user);
    vendorLicenseFacade.updateLimit(limit, user);
    updateParentForLimit(limit,user);
    mdcDataDebugMessage
            .debugExitMessage("VLM id", limit.getVendorLicenseModelId(), "EP/LKGId", limit
                    .getEpLkgId());
  }

  private boolean isLimitPresent(LimitEntity limit) {
    return limitDao.isLimitPresent(limit);
  }

  @Override
  public LimitEntity getLimit(LimitEntity limitEntity,
                              String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", limitEntity.getVendorLicenseModelId(),
            "EP/LKGId", limitEntity.getEpLkgId());

    limitEntity.setVersion(VersioningUtil.resolveVersion(limitEntity.getVersion(),
            getVersionInfo(limitEntity.getVendorLicenseModelId(), VersionableEntityAction.Read,
                    user), user));
    if(!isLimitPresent(limitEntity)){
      VersioningUtil
              .validateEntityExistence(null, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);
    }
    LimitEntity retrieved = limitDao.get(limitEntity);
    VersioningUtil
            .validateEntityExistence(retrieved, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);

    mdcDataDebugMessage.debugExitMessage("VLM id", limitEntity.getVendorLicenseModelId(),
            "EP/LKGId", limitEntity.getEpLkgId());
    return retrieved;
  }

  /**
   * update Parent of limit (EP/LKG) versionuuid when limit is modified so that limit updates are
   * captured in VLM XML
   * @param limit
   * @param user
   */
  private void updateParentForLimit(LimitEntity limit, String user) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", limit.getVendorLicenseModelId(),
            "EP/LKGId", limit.getEpLkgId(), "Limit Parent ", limit.getParent());
    if ("EntitlementPool".equals(limit.getParent()) ) {
      EntitlementPoolEntity entitlementPoolEntity =
              entitlementPoolDao.get(new EntitlementPoolEntity(limit.getVendorLicenseModelId(),
                      limit.getVersion(), limit.getEpLkgId()));
      vendorLicenseFacade.updateEntitlementPool(entitlementPoolEntity, user);
    }

    if ("LicenseKeyGroup".equals(limit.getParent())) {
      LicenseKeyGroupEntity licenseKeyGroupEntity = licenseKeyGroupDao.get(
              new LicenseKeyGroupEntity(limit.getVendorLicenseModelId(), limit.getVersion(),
                      limit.getEpLkgId()));
      vendorLicenseFacade.updateLicenseKeyGroup(licenseKeyGroupEntity, user);
    }

    mdcDataDebugMessage.debugEntryMessage("VLM id", limit.getVendorLicenseModelId(),
            "EP/LKGId", limit.getEpLkgId(), "Limit Parent ", limit.getParent());
  }

  protected void addFeatureGroupsToLicenseAgreementRef(Set<String> featureGroupIds,
                                                       LicenseAgreementEntity licenseAgreement) {
    if (featureGroupIds != null) {
      for (String featureGroupId : featureGroupIds) {
        featureGroupDao.addReferencingLicenseAgreement(
                new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(),
                        licenseAgreement.getVersion(), featureGroupId), licenseAgreement.getId());
      }
    }
  }

  protected void removeFeatureGroupsToLicenseAgreementRef(Set<String> featureGroupIds,
                                                          LicenseAgreementEntity licenseAgreement) {
    if (featureGroupIds != null) {
      for (String featureGroupId : featureGroupIds) {
        featureGroupDao.removeReferencingLicenseAgreement(
                new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(),
                        licenseAgreement.getVersion(), featureGroupId), licenseAgreement.getId());
      }
    }
  }

  protected void addLicenseKeyGroupsToFeatureGroupsRef(Set<String> licenseKeyGroupIds,
                                                       FeatureGroupEntity featureGroup) {
    if (licenseKeyGroupIds != null) {
      for (String licenseKeyGroupId : licenseKeyGroupIds) {
        licenseKeyGroupDao.addReferencingFeatureGroup(
                new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(),
                        featureGroup.getVersion(), licenseKeyGroupId), featureGroup.getId());
      }
    }
  }

  protected void removeLicenseKeyGroupsToFeatureGroupsRef(Set<String> licenseKeyGroupIds,
                                                          FeatureGroupEntity featureGroup) {
    if (licenseKeyGroupIds != null) {
      for (String licenseKeyGroupId : licenseKeyGroupIds) {
        licenseKeyGroupDao.removeReferencingFeatureGroup(
                new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(),
                        featureGroup.getVersion(), licenseKeyGroupId), featureGroup.getId());
      }
    }
  }

  protected void addEntitlementPoolsToFeatureGroupsRef(Set<String> entitlementPoolIds,
                                                       FeatureGroupEntity featureGroup) {
    if (entitlementPoolIds != null) {
      for (String entitlementPoolId : entitlementPoolIds) {
        entitlementPoolDao.addReferencingFeatureGroup(
                new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(),
                        featureGroup.getVersion(), entitlementPoolId), featureGroup.getId());
      }
    }
  }

  protected void removeEntitlementPoolsToFeatureGroupsRef(Set<String> entitlementPoolIds,
                                                          FeatureGroupEntity featureGroup) {
    if (entitlementPoolIds != null) {
      for (String entitlementPoolId : entitlementPoolIds) {
        entitlementPoolDao.removeReferencingFeatureGroup(
                new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(),
                        featureGroup.getVersion(), entitlementPoolId), featureGroup.getId());
      }
    }
  }

  protected VersionInfo getVersionInfo(String vendorLicenseModelId, VersionableEntityAction action,
                                       String user) {
    return vendorLicenseFacade.getVersionInfo(vendorLicenseModelId, action, user);
  }

  protected LicenseAgreementEntity createLicenseAgreementForList(String vlmId, Version version,
                                                                 String user) {
    return new LicenseAgreementEntity(vlmId, VersioningUtil
            .resolveVersion(version, getVersionInfo(vlmId, VersionableEntityAction.Read, user), user),
            null);
  }

  protected void updateUniqueName(String uniqueValueType ,String oldName, String newName,String ...
          context) {
    UniqueValueUtil
            .updateUniqueValue(uniqueValueType, oldName, newName,context);
  }

  protected void deleteUniqueName(String uniqueValueType,String ... uniqueCombination) {
    UniqueValueUtil.deleteUniqueValue(uniqueValueType, uniqueCombination);
  }

  protected Version resloveVersion(String vlmId,Version requestedVersion, VersionInfo versionInfo,
                                   String user){
    return VersioningUtil.resolveVersion(null,
            getVersionInfo(vlmId, VersionableEntityAction.Write, user), user);
  }

}
