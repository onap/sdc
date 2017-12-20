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
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ProcessManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.UploadInvalidErrorBuilder;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;

public class ProcessManagerImpl implements ProcessManager {
  private static final String PROCESS_ARTIFACT_NOT_EXIST_MSG =
      "Process artifact for process with Id %s does not exist for %s with Id %s and version %s";

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private ProcessDao processDao;

  public ProcessManagerImpl(ProcessDao processDao) {
    this.processDao = processDao;
  }

  @Override
  public Collection<ProcessEntity> listProcesses(String vspId, Version version,
                                                 String componentId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);
    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return processDao.list(new ProcessEntity(vspId, version, componentId, null));
  }

  @Override
  public void deleteProcesses(String vspId, Version version, String componentId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity allProcesses = new ProcessEntity(vspId, version, componentId, null);
    Collection<ProcessEntity> processes = processDao.list(allProcesses);

    if (!processes.isEmpty()) {
      for (ProcessEntity process : processes) {
        deleteUniqueValue(process.getVspId(), process.getVersion(), process.getComponentId(),
            process.getName());
      }
    }

    if (componentId == null) {
      processDao.deleteVspAll(vspId,version);
    } else {
      processDao.deleteAll(allProcesses);
    }

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  @Override
  public ProcessEntity createProcess(ProcessEntity process) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", process.getId(),
        process.getComponentId());
    validateUniqueName(process.getVspId(), process.getVersion(), process.getComponentId(),
        process.getName());

    processDao.create(process);
    createUniqueName(process.getVspId(), process.getVersion(), process.getComponentId(),
        process.getName());

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", process.getId(),
        process.getComponentId());

    return process;
  }

  @Override
  public ProcessEntity getProcess(String vspId, Version version, String componentId,
                                  String processId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved =
        processDao.get(new ProcessEntity(vspId, version, componentId, processId));
    validateProcessExistence(vspId, version, componentId, processId, retrieved);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return retrieved;
  }

  @Override
  public void updateProcess(ProcessEntity process) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", process.getId(),
        process.getComponentId());

    ProcessEntity retrieved = processDao.get(process);
    validateProcessExistence(process.getVspId(), process.getVersion(), process.getComponentId(),
        process.getId(), retrieved);

    updateUniqueName(process.getVspId(), process.getVersion(), process.getComponentId(),
        retrieved.getName(), process.getName());
    processDao.update(process);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", process.getId(),
        process.getComponentId());
  }

  @Override
  public void deleteProcess(String vspId, Version version, String componentId, String processId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved = getProcess(vspId, version, componentId, processId);

    processDao.delete(retrieved);
    deleteUniqueValue(retrieved.getVspId(), retrieved.getVersion(), retrieved.getComponentId(),
        retrieved.getName());

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }


  @Override
  public File getProcessArtifact(String vspId, Version version, String componentId,
                                 String processId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved = getValidatedProcessArtifact(vspId, version, componentId, processId);

    File file = new File(String.format("%s_%s_%s", vspId, componentId, processId));
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(retrieved.getArtifact().array());
    } catch (IOException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GET_PROCESS_ARTIFACT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't get process artifact");
      throw new CoreException(new UploadInvalidErrorBuilder().build());
    }

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);

    return file;
  }

  @Override
  public void deleteProcessArtifact(String vspId, Version version, String componentId,
                                    String processId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity retrieved = getValidatedProcessArtifact(vspId, version, componentId, processId);

    processDao.deleteArtifact(retrieved);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  @Override
  public void uploadProcessArtifact(InputStream artifactFile, String artifactFileName, String vspId,
                                    Version version, String componentId, String processId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, component id", vspId, componentId);

    ProcessEntity process = getProcess(vspId, version, componentId, processId);
    process.setArtifactName(artifactFileName);
    process.setArtifact(readArtifact(artifactFile));
    processDao.uploadArtifact(process);

    mdcDataDebugMessage.debugExitMessage("VSP id, component id", vspId, componentId);
  }

  private ProcessEntity getValidatedProcessArtifact(String vspId, Version version,
                                                    String componentId, String processId) {
    ProcessEntity retrieved =
        processDao.getArtifact(new ProcessEntity(vspId, version, componentId, processId));
    validateProcessArtifactExistence(vspId, version, componentId, processId, retrieved);
    return retrieved;
  }

  private ByteBuffer readArtifact(InputStream artifactInputStream) {
    if (artifactInputStream == null) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPLOAD_PROCESS_ARTIFACT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't upload process artifact");
      throw new CoreException(new UploadInvalidErrorBuilder().build());
    }
    try {
      return ByteBuffer.wrap(FileUtils.toByteArray(artifactInputStream));
    } catch (RuntimeException exception) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.UPLOAD_PROCESS_ARTIFACT, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't upload process artifact");
      throw new CoreException(new UploadInvalidErrorBuilder().build());
    }
  }


  private void validateProcessExistence(String vspId, Version version, String componentId,
                                        String processId, ProcessEntity retrieved) {
    VersioningUtil.validateEntityExistence(retrieved,
        new ProcessEntity(vspId, version, componentId, processId),
        VspDetails.ENTITY_TYPE);
  }

  private void validateProcessArtifactExistence(String vspId, Version version, String componentId,
                                                String processId, ProcessEntity retrieved) {
    ProcessEntity inputProcess = new ProcessEntity(vspId, version, componentId, processId);
    VersioningUtil.validateEntityExistence(retrieved, inputProcess, VspDetails.ENTITY_TYPE);
    if (retrieved.getArtifact() == null) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withCategory(ErrorCategory.APPLICATION)
          .withId(VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND)
          .withMessage(String.format(PROCESS_ARTIFACT_NOT_EXIST_MSG,
              processId, VspDetails.ENTITY_TYPE, vspId, version)).build());
    }
  }


  protected void validateUniqueName(String vspId, Version version, String componentId,
                                    String processName) {
    UniqueValueUtil.validateUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
        vspId, version.getId(), componentId, processName);
  }

  protected void createUniqueName(String vspId, Version version, String componentId,
                                  String processName) {
    UniqueValueUtil
        .createUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME, vspId,
            version.getId(), componentId, processName);
  }

  protected void updateUniqueName(String vspId, Version version, String componentId,
                                  String oldProcessName, String newProcessName) {
    UniqueValueUtil
        .updateUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME, oldProcessName,
            newProcessName, vspId, version.getId(), componentId);
  }

  protected void deleteUniqueValue(String vspId, Version version, String componentId,
                                   String processName) {
    if (componentId == null) {
      UniqueValueUtil
          .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME, vspId,
              version.getId(), processName);
    }
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME, vspId,
            version.getId(), componentId, processName);
  }
}
