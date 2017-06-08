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

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.MibManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MibDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.MibEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.MibUploadErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MibUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VendorSoftwareProductUtils;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersionableSubEntityNotFoundErrorBuilder;
import org.openecomp.sdcrests.activitylog.types.ActivityType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MibManagerImpl implements MibManager {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private ActivityLogManager activityLogManager;
  private MibDao mibDao;

  public MibManagerImpl(MibDao mibDao,
                        ActivityLogManager activityLogManager) {
    this.mibDao = mibDao;

    this.activityLogManager = activityLogManager;
    mibDao.registerVersioning(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
  }

  @Override
  public void delete(String vspId, Version version, String componentId,
                     ArtifactType artifactType, String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    MibEntity mibEntity =
        setValuesForComponentArtifactEntityUpload(vspId, version, null, componentId, null,
            artifactType, null);
    Optional<MibEntity> retrieved = mibDao.getByType(mibEntity);

    if (!retrieved.isPresent()) {
      throw new CoreException(new VersionableSubEntityNotFoundErrorBuilder(
          mibEntity.getEntityType(),
          artifactType.name(),
          VspDetails.ENTITY_TYPE,
          mibEntity.getFirstClassCitizenId(),
          version).build());
    }

    mibDao.delete(retrieved.get());

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  @Override
  public void upload(InputStream object, String filename, String vspId,
                     Version version, String componentId, ArtifactType artifactType,
                     String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    if (object == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPLOAD_MIB, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid MIB zip file");
      throw new CoreException(new MibUploadErrorBuilder(
          Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()).build());
    } else {
      Map<String, List<ErrorMessage>> errors = new HashMap<>();
      try {
        byte[] uploadedFileData = FileUtils.toByteArray(object);
        validateMibZipContent(vspId, version, uploadedFileData, errors);
        if (MapUtils.isNotEmpty(errors)) {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.UPLOAD_MIB, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid MIB zip file");
          throw new CoreException(
              new MibUploadErrorBuilder(errors.values().iterator().next().get(0).getMessage())
                  .build());
        }

        createArtifactInDatabase(vspId, version, filename, componentId, artifactType,
            uploadedFileData);

      } catch (Exception exception) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.UPLOAD_MIB, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid MIB zip file");
        throw new CoreException(new MibUploadErrorBuilder(exception.getMessage()).build());
      }
    }

    ActivityLogEntity activityLogEntity =
        new ActivityLogEntity(vspId, String.valueOf(version.getMajor() + 1),
            ActivityType.UPLOAD_ARTIFACT.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);


    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  private void createArtifactInDatabase(String vspId, Version version, String filename,
                                        String componentId, ArtifactType artifactType,
                                        byte[] uploadedFileData) {
    String artifactId = CommonMethods.nextUuId();
    MibEntity mibEntity =
        setValuesForComponentArtifactEntityUpload(vspId, version, filename, componentId,
            artifactId, artifactType, uploadedFileData);
    mibDao.create(mibEntity);
  }

  @Override
  public MibUploadStatus listFilenames(String vspId, Version version, String componentId,
                                       String user) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    MibEntity current =
        new MibEntity(vspId, version, componentId, null);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return setMibUploadStatusValues(current);
  }


  private MibUploadStatus setMibUploadStatusValues(
      MibEntity mibEntity) {
    MibUploadStatus mibUploadStatus = new MibUploadStatus();

    Collection<MibEntity> artifactNames =
        mibDao.list(mibEntity);
    Map<ArtifactType, String> artifactTypeToFilename =
        VendorSoftwareProductUtils.filterNonTrapOrPollArtifacts(artifactNames);

    if (MapUtils.isNotEmpty(artifactTypeToFilename)) {
      if (artifactTypeToFilename.containsKey(ArtifactType.SNMP_TRAP)) {
        mibUploadStatus.setSnmpTrap(artifactTypeToFilename.get(ArtifactType.SNMP_TRAP));
      }
      if (artifactTypeToFilename.containsKey(ArtifactType.SNMP_POLL)) {
        mibUploadStatus.setSnmpPoll(artifactTypeToFilename.get(ArtifactType.SNMP_POLL));
      }
    }

    return mibUploadStatus;
  }

  private MibEntity setValuesForComponentArtifactEntityUpload(
      String vspId, Version version, String filename, String componentId, String artifactId,
      ArtifactType artifactType, byte[] uploadedFileData) {

    MibEntity mibEntity = new MibEntity();

    mibEntity.setVspId(vspId);
    mibEntity.setVersion(version);
    mibEntity.setComponentId(componentId);
    mibEntity.setId(artifactId);
    mibEntity.setType(artifactType);
    mibEntity.setArtifactName(filename);

    if (Objects.nonNull(uploadedFileData)) {
      mibEntity.setArtifact(ByteBuffer.wrap(uploadedFileData));
    }

    return mibEntity;
  }

  private void validateMibZipContent(String vspId, Version version, byte[] uploadedFileData,
                                     Map<String, List<ErrorMessage>> errors) {
    FileContentHandler contentMap;
    try {
      contentMap = CommonUtil.loadUploadFileContent(uploadedFileData);
      VendorSoftwareProductUtils.validateContentZipData(contentMap, errors);
    } catch (IOException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_MIB, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid MIB zip file");
      throw new CoreException(
          new MibUploadErrorBuilder(vspId, version, Messages.INVALID_ZIP_FILE.getErrorMessage())
              .build());
    }
  }
}
