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
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.MonitoringUploadErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VendorSoftwareProductUtils;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersionableSubEntityNotFoundErrorBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MonitoringUploadsManagerImpl implements MonitoringUploadsManager {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private ComponentArtifactDao componentArtifactDao;
  private static final Logger logger =
      LoggerFactory.getLogger(VendorSoftwareProductManagerImpl.class);

  MonitoringUploadsManagerImpl(ComponentArtifactDao componentArtifactDao) {
    this.componentArtifactDao = componentArtifactDao;

    componentArtifactDao.registerVersioning(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
  }

  @Override
  public void delete(String vspId, Version version, String componentId,
                     MonitoringUploadType monitoringUploadType) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    ComponentMonitoringUploadEntity componentMonitoringUploadEntity =
        setValuesForComponentArtifactEntityUpload(vspId, version, null, componentId, null,
            monitoringUploadType, null);
    Optional<ComponentMonitoringUploadEntity> retrieved = componentArtifactDao.getByType(
        componentMonitoringUploadEntity);

    if (!retrieved.isPresent()) {
      throw new CoreException(new VersionableSubEntityNotFoundErrorBuilder(
          componentMonitoringUploadEntity.getEntityType(),
          monitoringUploadType.name(),
          VspDetails.ENTITY_TYPE,
          componentMonitoringUploadEntity.getFirstClassCitizenId(),
          version).build());
    }

    componentArtifactDao.delete(retrieved.get());

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  @Override
  public void upload(InputStream object, String filename, String vspId,
                     Version version, String componentId,
                     MonitoringUploadType type) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    if (object == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPLOAD_MONITORING_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid " + type
              .toString() + " zip file");
      throw new CoreException(new MonitoringUploadErrorBuilder(
          Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()).build());
    } else {
      Map<String, List<ErrorMessage>> errors = new HashMap<>();
      try {
        byte[] uploadedFileData = FileUtils.toByteArray(object);
        final FileContentHandler upload =
            validateZip(vspId, version, uploadedFileData, errors);
        if (type.equals(MonitoringUploadType.VES_EVENTS)) {
          validateVesEventUpload(upload, errors);
        }
        if (MapUtils.isNotEmpty(errors)) {
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
              LoggerTragetServiceName.UPLOAD_MONITORING_FILE, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid " + type
                  .toString() + " zip file");
          throw new CoreException(
              new MonitoringUploadErrorBuilder(
                  errors.values().iterator().next().get(0).getMessage())
                  .build());
        }

        createArtifactInDatabase(vspId, version, filename, componentId, type,
            uploadedFileData);

      } catch (Exception exception) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.UPLOAD_MONITORING_FILE, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid " + type.toString() + "zip file");
        throw new CoreException(new MonitoringUploadErrorBuilder(exception.getMessage()).build());
      }
    }
    logger.audit("Uploaded Monitoring File for component id:" + componentId + " ,vspId:" + vspId);
    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  private void validateVesEventUpload(FileContentHandler upload,
                                      Map<String, List<ErrorMessage>> errors) {
    if (!CommonUtil.validateAllFilesYml(upload)) {
      ErrorMessage.ErrorMessageUtil.addMessage(SdcCommon.UPLOAD_FILE, errors)
          .add(new ErrorMessage(ErrorLevel.ERROR,
              Messages.VES_ZIP_SHOULD_CONTAIN_YML_ONLY.getErrorMessage()));
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_MONITORING_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_VES_FILE);
      throw new CoreException(
          new MonitoringUploadErrorBuilder(
              Messages.VES_ZIP_SHOULD_CONTAIN_YML_ONLY.getErrorMessage())
              .build());
    }
  }

  private void createArtifactInDatabase(String vspId, Version version, String filename,
                                        String componentId,
                                        MonitoringUploadType type,
                                        byte[] uploadedFileData) {
    String artifactId = CommonMethods.nextUuId();
    ComponentMonitoringUploadEntity componentMonitoringUploadEntity =
        setValuesForComponentArtifactEntityUpload(vspId, version, filename, componentId,
            artifactId, type, uploadedFileData);
    componentArtifactDao.create(componentMonitoringUploadEntity);
  }

  @Override
  public MonitoringUploadStatus listFilenames(String vspId, Version version, String componentId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    ComponentMonitoringUploadEntity current =
        new ComponentMonitoringUploadEntity(vspId, version, componentId, null);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return setMonitoringUploadStatusValues(current);
  }


  private MonitoringUploadStatus setMonitoringUploadStatusValues(
      ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    MonitoringUploadStatus monitoringUploadStatus = new MonitoringUploadStatus();

    Collection<ComponentMonitoringUploadEntity> artifactNames =
        componentArtifactDao.list(componentMonitoringUploadEntity);
    Map<MonitoringUploadType, String> artifactTypeToFilename =
        VendorSoftwareProductUtils.mapArtifactsByType(artifactNames);

    if (MapUtils.isNotEmpty(artifactTypeToFilename)) {
      if (artifactTypeToFilename.containsKey(MonitoringUploadType.SNMP_TRAP)) {
        monitoringUploadStatus
            .setSnmpTrap(artifactTypeToFilename.get(MonitoringUploadType.SNMP_TRAP));
      }
      if (artifactTypeToFilename.containsKey(MonitoringUploadType.SNMP_POLL)) {
        monitoringUploadStatus
            .setSnmpPoll(artifactTypeToFilename.get(MonitoringUploadType.SNMP_POLL));
      }
      if (artifactTypeToFilename.containsKey(MonitoringUploadType.VES_EVENTS)) {
        monitoringUploadStatus
            .setVesEvent(artifactTypeToFilename.get(MonitoringUploadType.VES_EVENTS));
      }
    }

    return monitoringUploadStatus;
  }

  private ComponentMonitoringUploadEntity setValuesForComponentArtifactEntityUpload(
      String vspId, Version version, String filename, String componentId, String artifactId,
      MonitoringUploadType monitoringUploadType, byte[] uploadedFileData) {

    ComponentMonitoringUploadEntity
        entity = new ComponentMonitoringUploadEntity();

    entity.setVspId(vspId);
    entity.setVersion(version);
    entity.setComponentId(componentId);
    entity.setId(artifactId);
    entity.setType(monitoringUploadType);
    entity.setArtifactName(filename);

    if (Objects.nonNull(uploadedFileData)) {
      entity.setArtifact(ByteBuffer.wrap(uploadedFileData));
    }

    return entity;
  }

  private FileContentHandler validateZip(String vspId, Version version, byte[] uploadedFileData,
                                         Map<String, List<ErrorMessage>> errors) {
    FileContentHandler contentMap;
    try {
      contentMap =
          CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP, uploadedFileData);
      VendorSoftwareProductUtils.validateContentZipData(contentMap, errors);
    } catch (IOException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_MONITORING_FILE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid Monitoring zip file");
      throw new CoreException(
          new MonitoringUploadErrorBuilder(vspId, version,
              Messages.INVALID_ZIP_FILE.getErrorMessage())
              .build());
    }
    return contentMap;
  }
}
