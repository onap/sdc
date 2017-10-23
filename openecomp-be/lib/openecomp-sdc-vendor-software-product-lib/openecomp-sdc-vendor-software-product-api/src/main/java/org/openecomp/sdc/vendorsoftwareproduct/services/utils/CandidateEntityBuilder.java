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

package org.openecomp.sdc.vendorsoftwareproduct.services.utils;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.HeatFileAnalyzer;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.HeatFileAnalyzerRowDataImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.CandidateDataEntityTo;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.AnalyzedZipHeatFiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CandidateEntityBuilder {

  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();

  private final CandidateService candidateService;

  public CandidateEntityBuilder(CandidateService candidateService) {
    this.candidateService = candidateService;
  }

  public OrchestrationTemplateCandidateData buildCandidateEntityFromZip(
      VspDetails vspDetails, byte[] uploadedFileData, FileContentHandler contentMap,
      Map<String, List<ErrorMessage>> uploadErrors, String user) throws Exception {
    //mdcDataDebugMessage.debugEntryMessage("VSP Id", vspDetails.getId());

    try (InputStream zipFileManifest = contentMap.getFileContent(SdcCommon.MANIFEST_NAME)) {
      HeatFileAnalyzer heatFileAnalyzer = new HeatFileAnalyzerRowDataImpl();
      AnalyzedZipHeatFiles analyzedZipHeatFiles =
              heatFileAnalyzer.analyzeFilesNotEligibleForModulesFromFileAnalyzer(contentMap.getFiles());
      HeatStructureTree tree = getHeatStructureTree(vspDetails, contentMap, analyzedZipHeatFiles);

      CandidateDataEntityTo candidateDataEntityTo =
              new CandidateDataEntityTo(vspDetails.getId(), user, uploadedFileData, tree, contentMap,
                      vspDetails.getVersion());
      candidateDataEntityTo.setErrors(uploadErrors);
      OrchestrationTemplateCandidateData candidateDataEntity =
              candidateService.createCandidateDataEntity(candidateDataEntityTo, zipFileManifest,
                      analyzedZipHeatFiles);

      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP Id", vspDetails.getId());
      return candidateDataEntity;
    }
  }

  private HeatStructureTree getHeatStructureTree(VspDetails vspDetails,
                                                 FileContentHandler contentMap,
                                                 AnalyzedZipHeatFiles analyzedZipHeatFiles) throws IOException {
    addManifestToFileContentMapIfNotExist(vspDetails, contentMap, analyzedZipHeatFiles);
    HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(contentMap);
    heatTreeManager.createTree();
    return heatTreeManager.getTree();
  }

  private void addManifestToFileContentMapIfNotExist(VspDetails vspDetails,
                                                     FileContentHandler fileContentHandler,
                                                     AnalyzedZipHeatFiles analyzedZipHeatFiles) throws IOException {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("VSP Id", vspDetails.getId());

    try (InputStream manifest = fileContentHandler.getFileContent(SdcCommon.MANIFEST_NAME)) {

      if (Objects.isNull(manifest)) {
        Optional<ManifestContent> manifestContentOptional =
                candidateService.createManifest(vspDetails, fileContentHandler, analyzedZipHeatFiles);
        if (!manifestContentOptional.isPresent()) {
          throw new RuntimeException(Messages.CREATE_MANIFEST_FROM_ZIP.getErrorMessage());
        }
        ManifestContent manifestContent = manifestContentOptional.get();
        fileContentHandler.addFile(
                SdcCommon.MANIFEST_NAME,
                String.valueOf(JsonUtil.sbObject2Json(manifestContent)).getBytes());
      }
    } finally {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("VSP Id", vspDetails.getId());
    }
  }
}
