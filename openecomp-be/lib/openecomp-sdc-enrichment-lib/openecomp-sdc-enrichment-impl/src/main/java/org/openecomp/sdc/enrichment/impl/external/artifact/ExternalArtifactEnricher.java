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

package org.openecomp.sdc.enrichment.impl.external.artifact;

import org.openecomp.core.enrichment.types.ComponentArtifactType;
import org.openecomp.core.enrichment.types.MibInfo;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.impl.tosca.ComponentInfo;
import org.openecomp.sdc.enrichment.inter.Enricher;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExternalArtifactEnricher extends Enricher {

  private static EnrichedServiceModelDao enrichedServiceModelDao =
      EnrichedServiceModelDaoFactory.getInstance().createInterface();


  @Override
  public Map<String, List<ErrorMessage>> enrich() {

    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    input.getEntityInfo().entrySet().stream().forEach(
        entry -> enrichComponentMib(entry.getKey(), (ComponentInfo) entry.getValue(), errors));


    return errors;
  }


  private void enrichComponentMib(String componentName, ComponentInfo componentInfo,
                                  Map<String, List<ErrorMessage>> errors) {

    String vspId = input.getKey();
    Version version = input.getVersion();
    ServiceArtifact mibServiceArtifact = new ServiceArtifact();
    mibServiceArtifact.setVspId(vspId);
    mibServiceArtifact.setVersion(version);
    enrichMibFiles(mibServiceArtifact, componentInfo, errors);
  }

  private void enrichMibFiles(ServiceArtifact mibServiceArtifact, ComponentInfo componentInfo,
                              Map<String, List<ErrorMessage>> errors) {
    if (componentInfo.getMibInfo() == null) {
      return;
    }
    enrichMibByType(componentInfo.getMibInfo().getSnmpTrap(), ComponentArtifactType.SNMP_TRAP,
        mibServiceArtifact, errors);
    enrichMibByType(componentInfo.getMibInfo().getSnmpPoll(), ComponentArtifactType.SNMP_POLL,
        mibServiceArtifact, errors);
  }

  private void enrichMibByType(MibInfo mibInfo, ComponentArtifactType type,
                               ServiceArtifact mibServiceArtifact,
                               Map<String, List<ErrorMessage>> errors) {
    if (mibInfo == null) {
      return;
    }
    FileContentHandler mibs;
    try {
      mibs = FileUtils.getFileContentMapFromZip(FileUtils.toByteArray(mibInfo.getContent()));
    } catch (IOException ioException) {
      ErrorMessage.ErrorMessageUtil
          .addMessage(mibServiceArtifact.getName() + "." + type.name(), errors)
          .add(new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_ZIP_FILE.getErrorMessage()));
      return;
    }
    Set<String> fileList = mibs.getFileList();
    for (String fileName : fileList) {
      mibServiceArtifact.setContentData(FileUtils.toByteArray(mibs.getFileContent(fileName)));
      mibServiceArtifact.setName(mibInfo.getName() + File.separator + fileName);
      enrichedServiceModelDao.storeExternalArtifact(mibServiceArtifact);
    }
  }
}
