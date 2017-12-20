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

package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

public interface ProcessManager {
  Collection<ProcessEntity> listProcesses(String vspId, Version version, String componentId);

  void deleteProcesses(String vspId, Version version, String componentId);

  ProcessEntity createProcess(ProcessEntity processEntity);

  ProcessEntity getProcess(String vspId, Version version, String componentId, String processId);

  void updateProcess(ProcessEntity processEntity);

  void deleteProcess(String vspId, Version version, String componentId, String processId);

  File getProcessArtifact(String vspId, Version version, String componentId, String processId);

  void deleteProcessArtifact(String vspId, Version version, String componentId, String processId);

  void uploadProcessArtifact(InputStream uploadFile, String fileName, String vspId,
                             Version version, String componentId, String processId);
}
