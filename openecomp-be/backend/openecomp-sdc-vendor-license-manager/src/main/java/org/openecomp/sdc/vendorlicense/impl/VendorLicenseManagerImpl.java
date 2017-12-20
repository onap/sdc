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
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
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
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class VendorLicenseManagerImpl implements VendorLicenseManager {
  private VendorLicenseFacade vendorLicenseFacade;
  private VendorLicenseModelDao vendorLicenseModelDao;
  private LicenseAgreementDao licenseAgreementDao;
  private FeatureGroupDao featureGroupDao;
  private EntitlementPoolDao entitlementPoolDao;
  private LicenseKeyGroupDao licenseKeyGroupDao;
  private LimitDao limitDao;

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public VendorLicenseManagerImpl(VendorLicenseFacade vendorLicenseFacade,
                                  VendorLicenseModelDao vendorLicenseModelDao,
                                  LicenseAgreementDao licenseAgreementDao,
                                  FeatureGroupDao featureGroupDao,
                                  EntitlementPoolDao entitlementPoolDao,
                                  LicenseKeyGroupDao licenseKeyGroupDao,
                                  LimitDao limitDao) {
    this.vendorLicenseFacade = vendorLicenseFacade;
    this.vendorLicenseModelDao = vendorLicenseModelDao;
    this.licenseAgreementDao = licenseAgreementDao;
    this.featureGroupDao = featureGroupDao;
    this.entitlementPoolDao = entitlementPoolDao;
    this.licenseKeyGroupDao = licenseKeyGroupDao;
    this.limitDao = limitDao;
  }


  @Override
  public void validate(String vendorLicenseModelId, Version version) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vendorLicenseModelId);

    vendorLicenseFacade.validate(vendorLicenseModelId, version);

    mdcDataDebugMessage.debugExitMessage("VLM id", vendorLicenseModelId);
  }

  @Override
  public VendorLicenseModelEntity createVendorLicenseModel(
      VendorLicenseModelEntity vendorLicenseModelEntity) {
    mdcDataDebugMessage.debugEntryMessage(null);

    vendorLicenseModelDao.create(vendorLicenseModelEntity);
    mdcDataDebugMessage.debugExitMessage(null);

    return vendorLicenseModelEntity;
  }

  @Override
  public void updateVendorLicenseModel(VendorLicenseModelEntity vendorLicenseModelEntity) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vendorLicenseModelEntity.getId());

    String existingVendorName = vendorLicenseModelDao.get(vendorLicenseModelEntity).getVendorName();

    updateUniqueName(VendorLicenseConstants.UniqueValues.VENDOR_NAME, existingVendorName,
        vendorLicenseModelEntity.getVendorName());
    vendorLicenseModelDao.update(vendorLicenseModelEntity);

    mdcDataDebugMessage.debugExitMessage("VLM id", vendorLicenseModelEntity.getId());
  }

  @Override
  public VendorLicenseModelEntity getVendorLicenseModel(String vlmId, Version version) {
    return vendorLicenseFacade.getVendorLicenseModel(vlmId, version);
  }

  @Override
  public void deleteVendorLicenseModel(String vlmId, Version version) {
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
        LoggerTragetServiceName.DELETE_ENTITY, ErrorLevel.ERROR.name(),
        LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.UNSUPPORTED_OPERATION);
    throw new UnsupportedOperationException(VendorLicenseConstants.UNSUPPORTED_OPERATION_ERROR);
  }

  @Override
  public Collection<LicenseAgreementEntity> listLicenseAgreements(String vlmId, Version version) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return licenseAgreementDao.list(new LicenseAgreementEntity(vlmId, version, null));
  }

  @Override
  public LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", licenseAgreement.getVendorLicenseModelId());
    mdcDataDebugMessage
        .debugExitMessage("VLM id", licenseAgreement.getVendorLicenseModelId());
    return vendorLicenseFacade.createLicenseAgreement(licenseAgreement);
  }

  @Override
  public void updateLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                     Set<String> addedFeatureGroupIds,
                                     Set<String> removedFeatureGroupIds) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", licenseAgreement
        .getVendorLicenseModelId(), licenseAgreement.getId());

    LicenseAgreementEntity retrieved = licenseAgreementDao.get(licenseAgreement);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseAgreement, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateContainedEntitiesExistence(new FeatureGroupEntity().getEntityType(),
        removedFeatureGroupIds, retrieved, retrieved.getFeatureGroupIds());
    VersioningUtil.validateEntitiesExistence(addedFeatureGroupIds,
        new FeatureGroupEntity(licenseAgreement.getVendorLicenseModelId(),
            licenseAgreement.getVersion(),
            null),
        featureGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);

    updateUniqueName(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        retrieved.getName(), licenseAgreement.getName(), licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().getId());
    licenseAgreementDao.updateColumnsAndDeltaFeatureGroupIds(licenseAgreement, addedFeatureGroupIds,
        removedFeatureGroupIds);

    addFeatureGroupsToLicenseAgreementRef(addedFeatureGroupIds, licenseAgreement);
    removeFeatureGroupsToLicenseAgreementRef(removedFeatureGroupIds, licenseAgreement);

    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", licenseAgreement
        .getVendorLicenseModelId(), licenseAgreement.getId());
  }

  @Override
  public LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                        String licenseAgreementId) {

    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", vlmId, licenseAgreementId);
    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", vlmId, licenseAgreementId);
    return vendorLicenseFacade.getLicenseAgreementModel(vlmId, version, licenseAgreementId);
  }

  @Override
  public void deleteLicenseAgreement(String vlmId, Version version, String licenseAgreementId) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LA id", vlmId, licenseAgreementId);

    LicenseAgreementEntity input =
        new LicenseAgreementEntity(vlmId, version, licenseAgreementId);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(input);
    VersioningUtil.validateEntityExistence(retrieved, input, VendorLicenseModelEntity.ENTITY_TYPE);

    removeFeatureGroupsToLicenseAgreementRef(retrieved.getFeatureGroupIds(), retrieved);

    licenseAgreementDao.delete(retrieved);

    deleteUniqueName(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    mdcDataDebugMessage.debugExitMessage("VLM id, LA id", vlmId, licenseAgreementId);
  }

  @Override
  public Collection<FeatureGroupEntity> listFeatureGroups(String vlmId, Version version) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return vendorLicenseFacade.listFeatureGroups(vlmId, version);
  }

  @Override
  public FeatureGroupEntity createFeatureGroup(FeatureGroupEntity featureGroup) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", featureGroup.getVendorLicenseModelId());
    mdcDataDebugMessage.debugExitMessage("VLM id", featureGroup.getId());
    return vendorLicenseFacade.createFeatureGroup(featureGroup);
  }

  @Override
  public void updateFeatureGroup(FeatureGroupEntity featureGroup,
                                 Set<String> addedLicenseKeyGroups,
                                 Set<String> removedLicenseKeyGroups,
                                 Set<String> addedEntitlementPools,
                                 Set<String> removedEntitlementPools) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id", featureGroup
        .getVendorLicenseModelId(), featureGroup.getId());

    FeatureGroupEntity retrieved = featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    VersioningUtil.validateContainedEntitiesExistence(new LicenseKeyGroupEntity().getEntityType(),
        removedLicenseKeyGroups, retrieved, retrieved.getLicenseKeyGroupIds());
    VersioningUtil.validateContainedEntitiesExistence(new EntitlementPoolEntity().getEntityType(),
        removedEntitlementPools, retrieved, retrieved.getEntitlementPoolIds());

    VersioningUtil.validateEntitiesExistence(addedLicenseKeyGroups,
        new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(), featureGroup.getVersion(),
            null),
        licenseKeyGroupDao, VendorLicenseModelEntity.ENTITY_TYPE);
    VersioningUtil.validateEntitiesExistence(addedEntitlementPools,
        new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), featureGroup.getVersion(),
            null),
        entitlementPoolDao, VendorLicenseModelEntity.ENTITY_TYPE);

    updateUniqueName(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        retrieved.getName(), featureGroup.getName(), featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());

    addLicenseKeyGroupsToFeatureGroupsRef(addedLicenseKeyGroups, featureGroup);
    removeLicenseKeyGroupsToFeatureGroupsRef(removedLicenseKeyGroups, featureGroup);
    addEntitlementPoolsToFeatureGroupsRef(addedEntitlementPools, featureGroup);
    removeEntitlementPoolsToFeatureGroupsRef(removedEntitlementPools, featureGroup);

    featureGroupDao.updateFeatureGroup(featureGroup, addedEntitlementPools, removedEntitlementPools,
        addedLicenseKeyGroups, removedLicenseKeyGroups);

    mdcDataDebugMessage.debugExitMessage("VLM id, FG id", featureGroup
        .getVendorLicenseModelId(), featureGroup.getId());
  }

  @Override
  public FeatureGroupModel getFeatureGroupModel(FeatureGroupEntity featureGroup) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id",
        featureGroup.getVendorLicenseModelId(), featureGroup.getId());

    mdcDataDebugMessage.debugExitMessage("VLM id, FG id",
        featureGroup.getVendorLicenseModelId(), featureGroup.getId());
    return vendorLicenseFacade.getFeatureGroupModel(featureGroup);
  }

  @Override
  public void deleteFeatureGroup(FeatureGroupEntity featureGroup) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, FG id",
        featureGroup.getVendorLicenseModelId(), featureGroup.getId());

    FeatureGroupEntity retrieved = featureGroupDao.get(featureGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, featureGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    removeLicenseKeyGroupsToFeatureGroupsRef(retrieved.getLicenseKeyGroupIds(), featureGroup);
    removeEntitlementPoolsToFeatureGroupsRef(retrieved.getEntitlementPoolIds(), featureGroup);

    for (String licenceAgreementId : retrieved.getReferencingLicenseAgreements()) {
      licenseAgreementDao.removeFeatureGroup(
          new LicenseAgreementEntity(featureGroup.getVendorLicenseModelId(),
              featureGroup.getVersion(),
              licenceAgreementId), featureGroup.getId());
    }

    featureGroupDao.delete(featureGroup);

    deleteUniqueName(VendorLicenseConstants.UniqueValues.FEATURE_GROUP_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    mdcDataDebugMessage
        .debugExitMessage("VLM id, FG id",
            featureGroup.getVendorLicenseModelId(), featureGroup.getId());
  }

  @Override
  public Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return vendorLicenseFacade.listEntitlementPools(vlmId, version);
  }

  @Override
  public EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", entitlementPool.getVendorLicenseModelId());
    mdcDataDebugMessage
        .debugExitMessage("VLM id", entitlementPool.getVendorLicenseModelId());

    entitlementPool.setStartDate(entitlementPool.getStartDate() != null ? (entitlementPool
        .getStartDate().trim().length() != 0 ? entitlementPool.getStartDate() + "T00:00:00Z"
        : null) : null);
    entitlementPool.setExpiryDate(entitlementPool.getExpiryDate() != null ? (entitlementPool
        .getExpiryDate().trim().length() != 0 ? entitlementPool.getExpiryDate() + "T23:59:59Z"
        : null) : null);

    validateCreateDate(entitlementPool.getStartDate(), entitlementPool.getExpiryDate(),
        entitlementPool.getVendorLicenseModelId());
    return vendorLicenseFacade.createEntitlementPool(entitlementPool);
  }

  private void validateCreateDate(String startDate, String expiryDate,
                                  String vendorLicenseModelId) {
    mdcDataDebugMessage.debugEntryMessage("Start date and end date", startDate
        + "   " + expiryDate);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss'Z'");

    if (startDate != null && expiryDate != null) {
      if (LocalDate.parse(startDate, formatter).atStartOfDay().isBefore
          (LocalDate.now().atStartOfDay()) || LocalDate.parse(expiryDate, formatter).atStartOfDay()
          .isEqual(LocalDate.parse(startDate, formatter).atStartOfDay()) || LocalDate
          .parse(expiryDate, formatter).isBefore(LocalDate.parse(startDate, formatter))) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
        throw new CoreException(
            new InvalidDateErrorBuilder(vendorLicenseModelId)
                .build());
      }
    }

    if (startDate != null && expiryDate == null) {
      if (LocalDate.parse(startDate, formatter).atStartOfDay().isBefore
          (LocalDate.now().atStartOfDay())) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
        throw new CoreException(
            new InvalidDateErrorBuilder(vendorLicenseModelId)
                .build());
      }
    }

    if (startDate == null && expiryDate != null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
      throw new CoreException(
          new InvalidDateErrorBuilder(vendorLicenseModelId)
              .build());

    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void validateUpdateDate(String startDate, String expiryDate,
                                  String vendorLicenseModelId) {
    mdcDataDebugMessage.debugEntryMessage("Start date and end date", startDate
        + "   " + expiryDate);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss'Z'");

    if (startDate != null && expiryDate != null) {
      if (LocalDate.parse(expiryDate, formatter).atStartOfDay()
          .isEqual(LocalDate.parse(startDate, formatter).atStartOfDay()) ||
          LocalDate.parse(expiryDate, formatter).isBefore(LocalDate.parse(startDate, formatter))) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
        throw new CoreException(
            new InvalidDateErrorBuilder(vendorLicenseModelId)
                .build());
      }
    }

    if (startDate == null && expiryDate != null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
      throw new CoreException(
          new InvalidDateErrorBuilder(vendorLicenseModelId)
              .build());

    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  @Override
  public void updateEntitlementPool(EntitlementPoolEntity entitlementPool) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());

    entitlementPool.setStartDate(entitlementPool.getStartDate() != null ? (entitlementPool
        .getStartDate().trim().length() != 0 ? entitlementPool.getStartDate() + "T00:00:00Z"
        : null) : null);
    entitlementPool.setExpiryDate(entitlementPool.getExpiryDate() != null ? (entitlementPool
        .getExpiryDate().trim().length() != 0 ? entitlementPool.getExpiryDate() + "T23:59:59Z"
        : null) : null);

    validateUpdateDate(entitlementPool.getStartDate(), entitlementPool.getExpiryDate(),
        entitlementPool.getVendorLicenseModelId());
    vendorLicenseFacade.updateEntitlementPool(entitlementPool);

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());
  }

  @Override
  public EntitlementPoolEntity getEntitlementPool(EntitlementPoolEntity entitlementPool) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());

    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss'Z'");
    DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    if (retrieved.getStartDate() != null) {
      retrieved.setStartDate(LocalDate.parse(retrieved.getStartDate(), formatter).format
          (targetFormatter));
    }

    if (retrieved.getExpiryDate() != null) {
      retrieved.setExpiryDate(LocalDate.parse(retrieved.getExpiryDate(), formatter).format
          (targetFormatter));
    }

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());
    return retrieved;
  }

  @Override
  public void deleteEntitlementPool(EntitlementPoolEntity entitlementPool) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());

    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);

    for (String referencingFeatureGroupId : retrieved.getReferencingFeatureGroups()) {
      featureGroupDao.removeEntitlementPool(
          new FeatureGroupEntity(entitlementPool.getVendorLicenseModelId(),
              entitlementPool.getVersion(),
              referencingFeatureGroupId), entitlementPool.getId());
    }

    deleteChildLimits(entitlementPool.getVendorLicenseModelId(), entitlementPool.getVersion(),
        entitlementPool.getId());

    entitlementPoolDao.delete(entitlementPool);

    deleteUniqueName(VendorLicenseConstants.UniqueValues.ENTITLEMENT_POOL_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id", entitlementPool
        .getVendorLicenseModelId(), entitlementPool.getId());
  }

  protected void deleteChildLimits(String vlmId, Version version, String epLkgId) {
    Optional<Collection<LimitEntity>> limitEntities = Optional.ofNullable(
        listLimits(vlmId, version, epLkgId));
    limitEntities.ifPresent(entities -> entities.forEach(this::deleteLimit));
  }

  @Override
  public Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId);
    return vendorLicenseFacade.listLicenseKeyGroups(vlmId, version);
  }

  @Override
  public LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", licenseKeyGroup.getVendorLicenseModelId());

    mdcDataDebugMessage.debugExitMessage("VLM id", licenseKeyGroup
        .getVendorLicenseModelId());

    licenseKeyGroup.setStartDate(licenseKeyGroup.getStartDate() != null ? (licenseKeyGroup
        .getStartDate().trim().length() != 0 ? licenseKeyGroup.getStartDate() + "T00:00:00Z"
        : null) : null);
    licenseKeyGroup.setExpiryDate(licenseKeyGroup.getExpiryDate() != null ? (licenseKeyGroup
        .getExpiryDate().trim().length() != 0 ? licenseKeyGroup.getExpiryDate() + "T23:59:59Z"
        : null) : null);

    validateCreateDate(licenseKeyGroup.getStartDate(), licenseKeyGroup.getExpiryDate(),
        licenseKeyGroup.getVendorLicenseModelId());
    return vendorLicenseFacade.createLicenseKeyGroup(licenseKeyGroup);
  }

  @Override
  public void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());

    licenseKeyGroup.setStartDate(licenseKeyGroup.getStartDate() != null ? (licenseKeyGroup
        .getStartDate().trim().length() != 0 ? licenseKeyGroup.getStartDate() + "T00:00:00Z"
        : null) : null);
    licenseKeyGroup.setExpiryDate(licenseKeyGroup.getExpiryDate() != null ? (licenseKeyGroup
        .getExpiryDate().trim().length() != 0 ? licenseKeyGroup.getExpiryDate() + "T23:59:59Z"
        : null) : null);

    validateUpdateDate(licenseKeyGroup.getStartDate(), licenseKeyGroup.getExpiryDate(),
        licenseKeyGroup.getVendorLicenseModelId());
    vendorLicenseFacade.updateLicenseKeyGroup(licenseKeyGroup);

    mdcDataDebugMessage.debugExitMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());
  }

  @Override
  public LicenseKeyGroupEntity getLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());

    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    mdcDataDebugMessage.debugExitMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());
    return retrieved;
  }

  @Override
  public void deleteLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());

    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);

    for (String referencingFeatureGroupId : retrieved.getReferencingFeatureGroups()) {
      featureGroupDao.removeLicenseKeyGroup(
          new FeatureGroupEntity(licenseKeyGroup.getVendorLicenseModelId(),
              licenseKeyGroup.getVersion(),
              referencingFeatureGroupId), licenseKeyGroup.getId());
    }

    deleteChildLimits(licenseKeyGroup.getVendorLicenseModelId(), licenseKeyGroup.getVersion(),
        licenseKeyGroup.getId());

    licenseKeyGroupDao.delete(licenseKeyGroup);

    deleteUniqueName(VendorLicenseConstants.UniqueValues.LICENSE_KEY_GROUP_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());

    mdcDataDebugMessage.debugExitMessage("VLM id, LKG id", licenseKeyGroup
        .getVendorLicenseModelId(), licenseKeyGroup.getId());
  }

  @Override
  public LimitEntity createLimit(LimitEntity limit) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", limit.getVendorLicenseModelId(), "EP/LKGId", limit
            .getEpLkgId());
    mdcDataDebugMessage
        .debugExitMessage("VLM id", limit.getVendorLicenseModelId(), "EP/LKGId", limit
            .getEpLkgId());
    validateLimit(limit);
    LimitEntity createdLimit = vendorLicenseFacade.createLimit(limit);
    updateParentForLimit(limit);
    return createdLimit;
  }

  private void validateLimit(LimitEntity limit) {
    Collection<LimitEntity> limitList =
        listLimits(limit.getVendorLicenseModelId(), limit.getVersion()
            , limit.getEpLkgId());

    if (!isLimitNameUnique(limitList, limit.getName(), limit.getType(), limit.getId())) {
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
      if (limit.getName().equalsIgnoreCase(name) &&
          limit.getType().name().equalsIgnoreCase(type.name())) {
        if (id != null && limit.getId().equals(id)) {
          continue;
        }
        return false;
      }
    }
    return true;
  }

  @Override
  public Collection<LimitEntity> listLimits(String vlmId, Version version, String epLkgId) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", vlmId, "EP/LKGId", epLkgId);
    mdcDataDebugMessage.debugExitMessage("VLM id", vlmId, "EP/LKGId", epLkgId);
    return vendorLicenseFacade.listLimits(vlmId, version, epLkgId);
  }

  @Override
  public void deleteLimit(LimitEntity limitEntity) {
    mdcDataDebugMessage.debugEntryMessage("VLM id, EP id, Limit Id", limitEntity
        .getVendorLicenseModelId(), limitEntity.getEpLkgId(), limitEntity.getId());

    if (!isLimitPresent(limitEntity)) {
      VersioningUtil
          .validateEntityExistence(null, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);
    }
    LimitEntity retrieved = limitDao.get(limitEntity);
    VersioningUtil
        .validateEntityExistence(retrieved, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);

    limitDao.delete(limitEntity);

    updateParentForLimit(limitEntity);

    mdcDataDebugMessage.debugExitMessage("VLM id, EP id, Limit Id", limitEntity
        .getVendorLicenseModelId(), limitEntity.getEpLkgId(), limitEntity.getId());
  }

  @Override
  public void updateLimit(LimitEntity limit) {
    mdcDataDebugMessage
        .debugEntryMessage("VLM id", limit.getVendorLicenseModelId(), "EP/LKGId", limit
            .getEpLkgId());
    getLimit(limit);
    validateLimit(limit);
    vendorLicenseFacade.updateLimit(limit);
    updateParentForLimit(limit);
    mdcDataDebugMessage
        .debugExitMessage("VLM id", limit.getVendorLicenseModelId(), "EP/LKGId", limit
            .getEpLkgId());
  }

  private boolean isLimitPresent(LimitEntity limit) {
    return limitDao.isLimitPresent(limit);
  }

  @Override
  public LimitEntity getLimit(LimitEntity limitEntity) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", limitEntity.getVendorLicenseModelId(),
        "EP/LKGId", limitEntity.getEpLkgId());

    if (!isLimitPresent(limitEntity)) {
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
   */
  private void updateParentForLimit(LimitEntity limit) {
    mdcDataDebugMessage.debugEntryMessage("VLM id", limit.getVendorLicenseModelId(),
        "EP/LKGId", limit.getEpLkgId(), "Limit Parent ", limit.getParent());
    if ("EntitlementPool".equals(limit.getParent())) {
      EntitlementPoolEntity entitlementPoolEntity =
          entitlementPoolDao.get(new EntitlementPoolEntity(limit.getVendorLicenseModelId(),
              limit.getVersion(), limit.getEpLkgId()));
      vendorLicenseFacade.updateEntitlementPool(entitlementPoolEntity);
    }

    if ("LicenseKeyGroup".equals(limit.getParent())) {
      LicenseKeyGroupEntity licenseKeyGroupEntity = licenseKeyGroupDao.get(
          new LicenseKeyGroupEntity(limit.getVendorLicenseModelId(), limit.getVersion(),
              limit.getEpLkgId()));
      vendorLicenseFacade.updateLicenseKeyGroup(licenseKeyGroupEntity);
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

  protected void updateUniqueName(String uniqueValueType, String oldName, String newName, String...
      context) {
    UniqueValueUtil
        .updateUniqueValue(uniqueValueType, oldName, newName, context);
  }

  protected void deleteUniqueName(String uniqueValueType, String... uniqueCombination) {
    UniqueValueUtil.deleteUniqueValue(uniqueValueType, uniqueCombination);
  }
}
