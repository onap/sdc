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

import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface OrchestrationTemplateCandidateManager {
  UploadFileResponse upload(String vspId, Version version, InputStream heatFileToUpload,
                            String fileSuffix, String networkPackageName);

  OrchestrationTemplateActionResponse process(String vspId, Version version);

  Optional<FilesDataStructure> getFilesDataStructure(String vspId, Version version);

  ValidationResponse updateFilesDataStructure(String vspId, Version version,
                                              FilesDataStructure fileDataStructure);

  Optional<Pair<String, byte[]>> get(String vspId, Version version) throws IOException;

  OrchestrationTemplateCandidateData getInfo(String vspId, Version version);
}
