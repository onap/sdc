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

import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ProcessManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.UploadInvalidErrorBuilder;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.activitylog.types.ActivityType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class ProcessManagerImpl implements ProcessManager {
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private final ActivityLogManager activityLogManager;

  private final VendorSoftwareProductDao vendorSoftwareProductDao;

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  public ProcessManagerImpl(VendorSoftwareProductDao vendorSoftwareProductDao, ActivityLogManager activityLogManager) {
    this.vendorSoftwareProductDao = vendorSoftwareProductDao;
    this.activityLogManager = activityLogManager;
  }

  @Override
  public Collection<ProcessEntity> listProcesses(String vspId, Version version,
                                                 String componentId,
                                                 String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", vspId, componentId);
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", vspId, componentId);

    return vendorSoftwareProductDao.listProcesses(vspId, version, componentId);
  }

  @Override
  public void deleteProcesses(String vspId, Version version, String componentId, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", vspId, componentId);

    Collection<ProcessEntity> processes =
        vendorSoftwareProductDao.listProcesses(vspId, version, componentId);

    if (!processes.isEmpty()) {
      for (ProcessEntity process : processes) {
        deleteUniqueValue(process.getVspId(), process.getVersion(), process.getComponentId(),
            process.getName());
      }

      vendorSoftwareProductDao.deleteProcesses(vspId, version, componentId);
    }
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  @Override
  public ProcessEntity createProcess(ProcessEntity process, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", process.getId(),
        process.getComponentId());
    validateUniqueName(process.getVspId(), process.getVersion(), process.getComponentId(),
        process.getName());
    //process.setId(CommonMethods.nextUuId());

    vendorSoftwareProductDao.createProcess(process);
    createUniqueName(process.getVspId(), process.getVersion(), process.getComponentId(),
        process.getName());

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", process.getId(),
        process.getComponentId());

    return process;
  }


  @Override
  public ProcessEntity getProcess(String vspId, Version version, String componentId,
                                  String processId, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved =
        vendorSoftwareProductDao.getProcess(vspId, version, componentId, processId);
    validateProcessExistence(vspId, version, componentId, processId, retrieved);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", vspId, componentId);

    return retrieved;
  }

  @Override
  public void updateProcess(ProcessEntity process, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", process.getId(),
        process.getComponentId());

    ProcessEntity retrieved = vendorSoftwareProductDao
        .getProcess(process.getVspId(), process.getVersion(), process.getComponentId(),
            process.getId());
    validateProcessExistence(process.getVspId(), process.getVersion(), process.getComponentId(),
        process.getId(), retrieved);

    updateUniqueName(process.getVspId(), process.getVersion(), process.getComponentId(),
        retrieved.getName(), process.getName());
    vendorSoftwareProductDao.updateProcess(process);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", process.getId(),
        process.getComponentId());
  }

  @Override
  public void deleteProcess(String vspId, Version version, String componentId, String processId,
                            String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved =
        vendorSoftwareProductDao.getProcess(vspId, version, componentId, processId);
    validateProcessExistence(vspId, version, componentId, processId, retrieved);

    vendorSoftwareProductDao.deleteProcess(vspId, version, componentId, processId);
    deleteUniqueValue(retrieved.getVspId(), retrieved.getVersion(), retrieved.getComponentId(),
        retrieved.getName());

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", vspId, componentId);
  }


  @Override
  public File getProcessArtifact(String vspId, Version version, String componentId,
                                 String processId, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved =
        vendorSoftwareProductDao.getProcessArtifact(vspId, version, componentId, processId);
    validateProcessArtifactExistence(vspId, version, componentId, processId, retrieved);

    File file = new File(String.format("%s_%s_%s", vspId, componentId, processId));
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(retrieved.getArtifact().array());
    } catch (IOException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_PROCESS_ARTIFACT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't get process artifact");
      throw new CoreException(new UploadInvalidErrorBuilder().build(), exception);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", vspId, componentId);

    return file;
  }

  @Override
  public void deleteProcessArtifact(String vspId, Version version, String componentId,
                                    String processId, String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved =
        vendorSoftwareProductDao.getProcessArtifact(vspId, version, componentId, processId);
    validateProcessArtifactExistence(vspId, version, componentId, processId, retrieved);

    vendorSoftwareProductDao.deleteProcessArtifact(vspId, version, componentId, processId);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  @Override
  public void uploadProcessArtifact(InputStream artifactFile, String artifactFileName, String vspId,
                                    Version version, String componentId, String processId,
                                    String user) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved =
        vendorSoftwareProductDao.getProcess(vspId, version, componentId, processId);
    validateProcessExistence(vspId, version, componentId, processId, retrieved);

    if (artifactFile == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPLOAD_PROCESS_ARTIFACT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't upload process artifact");
      throw new CoreException(new UploadInvalidErrorBuilder().build());
    }

    byte[] artifact;
    try {
      artifact = FileUtils.toByteArray(artifactFile);
    } catch (RuntimeException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPLOAD_PROCESS_ARTIFACT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't upload process artifact");
      throw new CoreException(new UploadInvalidErrorBuilder().build(), exception);
    }

    vendorSoftwareProductDao.uploadProcessArtifact(vspId, version, componentId, processId, artifact,
            artifactFileName);
    ActivityLogEntity activityLogEntity = new ActivityLogEntity(vspId, String.valueOf(version.getMajor()+1),
        ActivityType.UPLOAD_MONITORING_FILE.toString(), user, true, "", "");
    activityLogManager.addActionLog(activityLogEntity, user);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id", vspId, componentId);
  }


  private void validateProcessExistence(String vspId, Version version, String componentId,
                                        String processId, ProcessEntity retrieved) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id, process id", vspId, componentId,
        processId);

    if (retrieved != null) {
      return;
    }
    VersioningUtil.validateEntityExistence(retrieved,
        new ProcessEntity(vspId, version, componentId, processId),
        VspDetails.ENTITY_TYPE);//todo retrieved is always null ??

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id, process id", vspId, componentId,
        processId);
  }

  private void validateProcessArtifactExistence(String vspId, Version version, String componentId,
                                                String processId, ProcessEntity retrieved) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP id, component id, process id", vspId, componentId,
        processId);

    if (retrieved != null) {
      VersioningUtil.validateEntityExistence(retrieved.getArtifact(),
          new ProcessEntity(vspId, version, componentId, processId),
          VspDetails.ENTITY_TYPE);
    } else {
      VersioningUtil.validateEntityExistence(retrieved,
          new ProcessEntity(vspId, version, componentId, processId),
          VspDetails.ENTITY_TYPE); //todo retrieved is always null ??
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP id, component id, process id", vspId, componentId,
        processId);
  }


  protected void validateUniqueName(String vspId, Version version, String componentId,
                                    String processName) {
    UniqueValueUtil.validateUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
        vspId, version.toString(), componentId, processName);
  }

  protected void createUniqueName(String vspId, Version version, String componentId,
                                  String processName) {
    UniqueValueUtil
        .createUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME, vspId,
            version.toString(), componentId, processName);
  }

  protected void updateUniqueName(String vspId, Version version, String componentId,
                                  String oldProcessName, String newProcessName) {
    UniqueValueUtil
        .updateUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME, oldProcessName,
            newProcessName, vspId, version.toString(), componentId);
  }

  protected void deleteUniqueValue(String vspId, Version version, String componentId,
                                   String processName) {
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME, vspId,
            version.toString(), componentId, processName);
  }
}
