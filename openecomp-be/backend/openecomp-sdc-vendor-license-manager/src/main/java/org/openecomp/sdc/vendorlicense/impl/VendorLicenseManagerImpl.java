/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdc.vendorlicense.impl;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
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
import java.util.Objects;
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
  private static final String VLM_ID = "VLM id";
  private static final String EP_LKGID = "EP/LKGId";
  private static final String VLM_ID_LKG_ID = "VLM id, LKG id";
  private static final String VLM_ID_LA_ID = "VLM id, LA id";
  private static final String VLM_ID_FG_ID = "VLM id, FG id";
  private static final String VLM_ID_EP_ID = "VLM id, EP id";
  private static final String EP_POOL_START_TIME = "T00:00:00Z";
  private static final String EP_POOL_EXPIRY_TIME = "T23:59:59Z";
  private static final  DateTimeFormatter FORMATTER
          = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss'Z'");
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
    vendorLicenseFacade.validate(vendorLicenseModelId, version);
  }

  @Override
  public VendorLicenseModelEntity createVendorLicenseModel(
      VendorLicenseModelEntity vendorLicenseModelEntity) {
    vendorLicenseModelDao.create(vendorLicenseModelEntity);
    return vendorLicenseModelEntity;
  }

  @Override
  public void updateVendorLicenseModel(VendorLicenseModelEntity vendorLicenseModelEntity) {
    String existingVendorName = vendorLicenseModelDao.get(vendorLicenseModelEntity).getVendorName();

    updateUniqueName(VendorLicenseConstants.UniqueValues.VENDOR_NAME, existingVendorName,
        vendorLicenseModelEntity.getVendorName());
    vendorLicenseModelDao.update(vendorLicenseModelEntity);
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
    return licenseAgreementDao.list(new LicenseAgreementEntity(vlmId, version, null));
  }

  @Override
  public LicenseAgreementEntity createLicenseAgreement(LicenseAgreementEntity licenseAgreement) {
    return vendorLicenseFacade.createLicenseAgreement(licenseAgreement);
  }

  @Override
  public void updateLicenseAgreement(LicenseAgreementEntity licenseAgreement,
                                     Set<String> addedFeatureGroupIds,
                                     Set<String> removedFeatureGroupIds) {
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
  }

  @Override
  public LicenseAgreementModel getLicenseAgreementModel(String vlmId, Version version,
                                                        String licenseAgreementId) {
    return vendorLicenseFacade.getLicenseAgreementModel(vlmId, version, licenseAgreementId);
  }

  @Override
  public void deleteLicenseAgreement(String vlmId, Version version, String licenseAgreementId) {
    LicenseAgreementEntity input =
        new LicenseAgreementEntity(vlmId, version, licenseAgreementId);
    LicenseAgreementEntity retrieved = licenseAgreementDao.get(input);
    VersioningUtil.validateEntityExistence(retrieved, input, VendorLicenseModelEntity.ENTITY_TYPE);

    removeFeatureGroupsToLicenseAgreementRef(retrieved.getFeatureGroupIds(), retrieved);

    licenseAgreementDao.delete(retrieved);

    deleteUniqueName(VendorLicenseConstants.UniqueValues.LICENSE_AGREEMENT_NAME,
        retrieved.getVendorLicenseModelId(), retrieved.getVersion().toString(),
        retrieved.getName());
  }

  @Override
  public Collection<FeatureGroupEntity> listFeatureGroups(String vlmId, Version version) {
    return vendorLicenseFacade.listFeatureGroups(vlmId, version);
  }

  @Override
  public FeatureGroupEntity createFeatureGroup(FeatureGroupEntity featureGroup) {
    return vendorLicenseFacade.createFeatureGroup(featureGroup);
  }

  @Override
  public void updateFeatureGroup(FeatureGroupEntity featureGroup,
                                 Set<String> addedLicenseKeyGroups,
                                 Set<String> removedLicenseKeyGroups,
                                 Set<String> addedEntitlementPools,
                                 Set<String> removedEntitlementPools) {
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

    updateEpLkgOnMrnChange(featureGroup, addedLicenseKeyGroups, addedEntitlementPools, retrieved);
  }

  /**
   * If MRN is updated in feature group then update all linked EPs and Lkgs with new versionUuId
   * @param featureGroup - Feature Group entity which is requested for update
   * @param addedLicenseKeyGroups - LicenseKeyGroups added with Feature Group
   * @param addedEntitlementPools - EntitlementPools added with Feature Group
   * @param retrieved - Feature Group entity fetched from database
   */
  private void updateEpLkgOnMrnChange(FeatureGroupEntity featureGroup,
                                      Set<String> addedLicenseKeyGroups,
                                      Set<String> addedEntitlementPools,
                                      FeatureGroupEntity retrieved) {
    if (Objects.nonNull(retrieved.getManufacturerReferenceNumber())
        && !retrieved.getManufacturerReferenceNumber().equals(featureGroup
        .getManufacturerReferenceNumber())) {
      if (CollectionUtils.isEmpty(addedEntitlementPools)) {
        updateEntitlementPool(featureGroup, retrieved.getEntitlementPoolIds());
      } else {
        updateEntitlementPool(featureGroup, addedEntitlementPools);
      }

      if (CollectionUtils.isEmpty(addedLicenseKeyGroups)) {
        updateLicenseKeyGroup(featureGroup, retrieved.getLicenseKeyGroupIds());
      } else {
        updateLicenseKeyGroup(featureGroup, addedLicenseKeyGroups);
      }
    }
  }

  private void updateEntitlementPool(FeatureGroupEntity featureGroup,
                                     Set<String> entitlementPoolIds) {
    for (String epId: entitlementPoolIds) {
      final EntitlementPoolEntity entitlementPoolEntity = entitlementPoolDao
          .get(new EntitlementPoolEntity(featureGroup.getVendorLicenseModelId(), featureGroup
              .getVersion(), epId));
      if (Objects.nonNull(entitlementPoolEntity)) {
        entitlementPoolEntity.setVersionUuId(CommonMethods.nextUuId());
        entitlementPoolDao.update(entitlementPoolEntity);
      }
    }
  }

  private void updateLicenseKeyGroup(FeatureGroupEntity featureGroup,
                                     Set<String> licenseKeyGroupIds) {
    for (String lkgId: licenseKeyGroupIds) {
      final LicenseKeyGroupEntity licenseKeyGroupEntity = licenseKeyGroupDao
          .get(new LicenseKeyGroupEntity(featureGroup.getVendorLicenseModelId(),
              featureGroup.getVersion(), lkgId));
      if (Objects.nonNull(licenseKeyGroupEntity)) {
        licenseKeyGroupEntity.setVersionUuId(CommonMethods.nextUuId());
        licenseKeyGroupDao.update(licenseKeyGroupEntity);
      }
    }
  }

  @Override
  public FeatureGroupModel getFeatureGroupModel(FeatureGroupEntity featureGroup) {
    return vendorLicenseFacade.getFeatureGroupModel(featureGroup);
  }

  @Override
  public void deleteFeatureGroup(FeatureGroupEntity featureGroup) {
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
  }

  @Override
  public Collection<EntitlementPoolEntity> listEntitlementPools(String vlmId, Version version) {
    return vendorLicenseFacade.listEntitlementPools(vlmId, version);
  }

  @Override
  public EntitlementPoolEntity createEntitlementPool(EntitlementPoolEntity entitlementPool) {
    entitlementPool.setStartDate(entitlementPool.getStartDate() != null ? (entitlementPool
        .getStartDate().trim().length() != 0 ? entitlementPool.getStartDate() + EP_POOL_START_TIME
        : null) : null);
    entitlementPool.setExpiryDate(entitlementPool.getExpiryDate() != null ? (entitlementPool
        .getExpiryDate().trim().length() != 0 ? entitlementPool.getExpiryDate() + EP_POOL_EXPIRY_TIME 
        : null) : null);

    validateCreateDate(entitlementPool.getStartDate(), entitlementPool.getExpiryDate(),
        entitlementPool.getVendorLicenseModelId());
    return vendorLicenseFacade.createEntitlementPool(entitlementPool);
  }

  private void validateCreateDate(String startDate, String expiryDate,
                                  String vendorLicenseModelId) {
  LocalDate parsedStartDate = parseLocalDate(startDate);
  LocalDate parsedExpiryDate = parseLocalDate(expiryDate);


    validateIfStartAndExpiryDateIsNotNull(startDate, expiryDate,
            vendorLicenseModelId, parsedStartDate, parsedExpiryDate);

    if (startDate != null && expiryDate == null
                      && parsedStartDate.atStartOfDay().isBefore
          (LocalDate.now().atStartOfDay())) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
        throw new CoreException(
            new InvalidDateErrorBuilder(vendorLicenseModelId)
                .build());
    }

    if (startDate == null && expiryDate != null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
      throw new CoreException(
          new InvalidDateErrorBuilder(vendorLicenseModelId)
              .build());

    }
  }

  private void validateIfStartAndExpiryDateIsNotNull(String startDate, String expiryDate,
                                                     String vendorLicenseModelId,
                                                     LocalDate parsedStartDate,
                                                     LocalDate parsedExpiryDate) {
    if (startDate != null && expiryDate != null
            && isValidatStartAndExpiryDate(parsedStartDate, parsedExpiryDate)) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
      throw new CoreException(
              new InvalidDateErrorBuilder(vendorLicenseModelId)
                      .build());
    }
  }

  private boolean isValidatStartAndExpiryDate(LocalDate parsedStartDate,
                                              LocalDate parsedExpiryDate) {
    return parsedStartDate.atStartOfDay().isBefore(LocalDate.now().atStartOfDay())
    || parsedExpiryDate.atStartOfDay().isEqual(parsedStartDate.atStartOfDay())
    || parsedExpiryDate.isBefore(parsedStartDate);
  }

  private static LocalDate parseLocalDate(String date) {
    if (date == null || date.isEmpty()) {
      return null;
    }

    return LocalDate.parse(date, FORMATTER );
  }

  private void validateUpdateDate(String startDate, String expiryDate,
                                  String vendorLicenseModelId) {
    LocalDate parsedStartDate = parseLocalDate(startDate);
    LocalDate parsedExpiryDate = parseLocalDate(expiryDate);

    if (startDate != null && expiryDate != null
            && (parsedExpiryDate.atStartOfDay()
            .isEqual(parsedStartDate.atStartOfDay())
            || parsedExpiryDate.isBefore(parsedStartDate ))) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.VALIDATE_DATE_RANGE,ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
      throw new CoreException(
              new InvalidDateErrorBuilder(vendorLicenseModelId)
                      .build());
    }

    if (startDate == null && expiryDate != null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_DATE_RANGE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VALUE);
      throw new CoreException(
          new InvalidDateErrorBuilder(vendorLicenseModelId)
              .build());

    }
  }

  @Override
  public void updateEntitlementPool(EntitlementPoolEntity entitlementPool) {
    entitlementPool.setStartDate(entitlementPool.getStartDate() != null ? (entitlementPool
        .getStartDate().trim().length() != 0 ? entitlementPool.getStartDate() + EP_POOL_START_TIME
        : null) : null);
    entitlementPool.setExpiryDate(entitlementPool.getExpiryDate() != null ? (entitlementPool
        .getExpiryDate().trim().length() != 0 ? entitlementPool.getExpiryDate() + EP_POOL_EXPIRY_TIME 
        : null) : null);

    validateUpdateDate(entitlementPool.getStartDate(), entitlementPool.getExpiryDate(),
        entitlementPool.getVendorLicenseModelId());
    vendorLicenseFacade.updateEntitlementPool(entitlementPool);
  }

  @Override
  public EntitlementPoolEntity getEntitlementPool(EntitlementPoolEntity entitlementPool) {
    EntitlementPoolEntity retrieved = entitlementPoolDao.get(entitlementPool);
    VersioningUtil
        .validateEntityExistence(retrieved, entitlementPool, VendorLicenseModelEntity.ENTITY_TYPE);
    DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    if (retrieved.getStartDate() != null) {
      retrieved.setStartDate(LocalDate.parse(retrieved.getStartDate(), FORMATTER ).format
          (targetFormatter));
    }

    if (retrieved.getExpiryDate() != null) {
      retrieved.setExpiryDate(LocalDate.parse(retrieved.getExpiryDate(), FORMATTER ).format
          (targetFormatter));
    }
    return retrieved;
  }

  @Override
  public void deleteEntitlementPool(EntitlementPoolEntity entitlementPool) {
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
  }

  protected void deleteChildLimits(String vlmId, Version version, String epLkgId) {
    Optional<Collection<LimitEntity>> limitEntities = Optional.ofNullable(
        listLimits(vlmId, version, epLkgId));
    limitEntities.ifPresent(entities -> entities.forEach(this::deleteLimit));
  }

  @Override
  public Collection<LicenseKeyGroupEntity> listLicenseKeyGroups(String vlmId, Version version) {
    return vendorLicenseFacade.listLicenseKeyGroups(vlmId, version);
  }

  @Override
  public LicenseKeyGroupEntity createLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    licenseKeyGroup.setStartDate(licenseKeyGroup.getStartDate() != null ? (licenseKeyGroup
        .getStartDate().trim().length() != 0 ? licenseKeyGroup.getStartDate() + EP_POOL_START_TIME
        : null) : null);
    licenseKeyGroup.setExpiryDate(licenseKeyGroup.getExpiryDate() != null ? (licenseKeyGroup
        .getExpiryDate().trim().length() != 0 ? licenseKeyGroup.getExpiryDate() + EP_POOL_EXPIRY_TIME 
        : null) : null);

    validateCreateDate(licenseKeyGroup.getStartDate(), licenseKeyGroup.getExpiryDate(),
        licenseKeyGroup.getVendorLicenseModelId());
    return vendorLicenseFacade.createLicenseKeyGroup(licenseKeyGroup);
  }

  @Override
  public void updateLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    licenseKeyGroup.setStartDate(licenseKeyGroup.getStartDate() != null ? (licenseKeyGroup
        .getStartDate().trim().length() != 0 ? licenseKeyGroup.getStartDate() + EP_POOL_START_TIME
        : null) : null);
    licenseKeyGroup.setExpiryDate(licenseKeyGroup.getExpiryDate() != null ? (licenseKeyGroup
        .getExpiryDate().trim().length() != 0 ? licenseKeyGroup.getExpiryDate() + EP_POOL_EXPIRY_TIME 
        : null) : null);

    validateUpdateDate(licenseKeyGroup.getStartDate(), licenseKeyGroup.getExpiryDate(),
        licenseKeyGroup.getVendorLicenseModelId());
    vendorLicenseFacade.updateLicenseKeyGroup(licenseKeyGroup);
  }

  @Override
  public LicenseKeyGroupEntity getLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
    LicenseKeyGroupEntity retrieved = licenseKeyGroupDao.get(licenseKeyGroup);
    VersioningUtil
        .validateEntityExistence(retrieved, licenseKeyGroup, VendorLicenseModelEntity.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public void deleteLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup) {
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
  }

  @Override
  public LimitEntity createLimit(LimitEntity limit) {
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
    return vendorLicenseFacade.listLimits(vlmId, version, epLkgId);
  }

  @Override
  public void deleteLimit(LimitEntity limitEntity) {
    if (!isLimitPresent(limitEntity)) {
      VersioningUtil
          .validateEntityExistence(null, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);
    }
    LimitEntity retrieved = limitDao.get(limitEntity);
    VersioningUtil
        .validateEntityExistence(retrieved, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);

    limitDao.delete(limitEntity);

    updateParentForLimit(limitEntity);
  }

  @Override
  public void updateLimit(LimitEntity limit) {
    getLimit(limit);
    validateLimit(limit);
    vendorLicenseFacade.updateLimit(limit);
    updateParentForLimit(limit);
  }

  private boolean isLimitPresent(LimitEntity limit) {
    return limitDao.isLimitPresent(limit);
  }

  @Override
  public LimitEntity getLimit(LimitEntity limitEntity) {
    if (!isLimitPresent(limitEntity)) {
      VersioningUtil
          .validateEntityExistence(null, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);
    }
    LimitEntity retrieved = limitDao.get(limitEntity);
    VersioningUtil
        .validateEntityExistence(retrieved, limitEntity, VendorLicenseModelEntity.ENTITY_TYPE);
    return retrieved;
  }

  /**
   * update Parent of limit (EP/LKG) versionuuid when limit is modified so that limit updates are
   * captured in VLM XML
   */
  private void updateParentForLimit(LimitEntity limit) {
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
