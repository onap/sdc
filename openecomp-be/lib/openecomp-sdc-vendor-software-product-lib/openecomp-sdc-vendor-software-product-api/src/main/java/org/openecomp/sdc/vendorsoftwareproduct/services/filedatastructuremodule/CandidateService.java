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

package org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.CandidateDataEntityTo;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.AnalyzedZipHeatFiles;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CandidateService {
  Optional<ErrorMessage> validateNonEmptyFileToUpload(InputStream heatFileToUpload);

  Optional<ErrorMessage> validateRawZipData(byte[] uploadedFileData);

  OrchestrationTemplateCandidateData createCandidateDataEntity(
      CandidateDataEntityTo candidateDataEntityTo, InputStream zipFileManifest,
      AnalyzedZipHeatFiles analyzedZipHeatFiles) throws Exception;

  void updateCandidateUploadData(String vspId, Version version,
                                 OrchestrationTemplateCandidateData uploadData);

  Optional<FilesDataStructure> getOrchestrationTemplateCandidateFileDataStructure(String vspId,
                                                                                  Version version);

  void updateOrchestrationTemplateCandidateFileDataStructure(String vspId, Version version,
                                                             FilesDataStructure fileDataStructure);

  OrchestrationTemplateCandidateData getOrchestrationTemplateCandidate(String vspId,
                                                                       Version version);

  OrchestrationTemplateCandidateData getOrchestrationTemplateCandidateInfo(String vspId,
                                                                       Version version);

  Optional<ByteArrayInputStream> fetchZipFileByteArrayInputStream(String vspId,
                                                                  OrchestrationTemplateCandidateData candidateDataEntity,
                                                                  String manifest,
                                                                  OnboardingTypesEnum type,
                                                                  Map<String, List<ErrorMessage>> uploadErrors);

  byte[] replaceManifestInZip(ByteBuffer contentData, String manifest, String vspId,
                              OnboardingTypesEnum type) throws IOException;

  Optional<ManifestContent> createManifest(VspDetails vspDetails,
                                           FileContentHandler fileContentHandler,
                                           AnalyzedZipHeatFiles analyzedZipHeatFiles);

  String createManifest(VspDetails vspDetails, FilesDataStructure structure);

  Optional<List<ErrorMessage>> validateFileDataStructure(FilesDataStructure filesDataStructure);
}
