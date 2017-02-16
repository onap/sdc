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

package org.openecomp.sdc.enrichment.enrichmentartifacts;

import org.openecomp.core.enrichment.enrichmentartifacts.EnrichmentArtifactsService;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;

public class EnrichmentArtifactsServiceImpl implements EnrichmentArtifactsService {
  EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> enrichedServiceModelDao =
      EnrichedServiceModelDaoFactory.getInstance().createInterface();

  @Override
  public FileContentHandler addMibs(String vspId, Version version) {
    FileContentHandler externalArtifacts = new FileContentHandler();
    List<ServiceArtifact> mibsList = enrichedServiceModelDao.getExternalArtifacts(vspId, version);
    addMibsToFileContentHandler(mibsList, externalArtifacts);

    return externalArtifacts;
  }


  private void addMibsToFileContentHandler(List<ServiceArtifact> mibsList,
                                           FileContentHandler externalArtifacts) {
    for (ServiceArtifact serviceArtifact : mibsList) {
      String filename = serviceArtifact.getName();
      externalArtifacts.addFile(filename, serviceArtifact.getContent());
    }
  }


  private boolean isFileArtifact(String filename) {
    return !filename.contains(AsdcCommon.HEAT_META) && !filename.contains(AsdcCommon.MANIFEST_NAME);
  }
}
