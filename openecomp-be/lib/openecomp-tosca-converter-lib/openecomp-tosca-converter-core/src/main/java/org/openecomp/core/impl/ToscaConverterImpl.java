/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.core.impl;

import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.datatypes.CsarFileTypes;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

import java.util.HashMap;
import java.util.Map;

import static org.openecomp.core.converter.datatypes.Constants.globalStName;
import static org.openecomp.core.converter.datatypes.Constants.mainStName;

public class ToscaConverterImpl extends AbstractToscaConverter {

  @Override
  public ToscaServiceModel convert(FileContentHandler fileContentHandler) {
      Map<String, byte[]> csarFiles = new HashMap<>(fileContentHandler.getFiles());
      ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
      Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();
      FileContentHandler artifacts = new FileContentHandler();
      GlobalSubstitutionServiceTemplate gsst = new GlobalSubstitutionServiceTemplate();
      csarFiles.putAll(fileContentHandler.getFiles());
      for (Map.Entry<String, byte[]> fileEntry : csarFiles.entrySet()) {
          CsarFileTypes fileType = getFileType(fileEntry.getKey());
          switch (fileType) {
              case mainServiceTemplate:
                  handleServiceTemplate(mainStName, fileEntry.getKey(), csarFiles, serviceTemplates);
                  break;

              case globalServiceTemplate:
                  handleServiceTemplate(globalStName, fileEntry.getKey(), csarFiles, serviceTemplates);
                  break;

              case externalFile:
                  artifacts.addFile(
                          getConcreteArtifactFileName(fileEntry.getKey()), fileEntry.getValue());
                  break;

              case definitionsFile:
                  handleDefintionTemplate(fileEntry.getKey(), csarFiles, gsst);
                  break;

              default:
                  break;
          }
      }
      handleMetadataFile(csarFiles);
      updateToscaServiceModel(toscaServiceModel, serviceTemplates, artifacts, gsst, csarFiles, mainStName);
      return toscaServiceModel;
    }

    @Override
    public void convertTopologyTemplate(ServiceTemplate serviceTemplate, ServiceTemplateReaderService readerService) {
        new VnfTopologyTemplateConverter().convertTopologyTemplate(serviceTemplate, readerService);
    }
}
