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

import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.InputStream;

public interface MonitoringUploadsManager {
  void delete(String vspId, Version version, String componentId,
              MonitoringUploadType monitoringUploadType);

  void upload(InputStream object, String filename, String vspId, Version version,
              String componentId, MonitoringUploadType monitoringUploadType);

  MonitoringUploadStatus listFilenames(String vspId, Version version, String componentId);
}
