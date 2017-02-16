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

package org.openecomp.sdc.tosca.datatypes;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.datatypes.model.AsdcModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;

import java.util.Collections;
import java.util.Map;

public class ToscaServiceModel implements AsdcModel {
  private FileContentHandler artifactFiles;
  private Map<String, ServiceTemplate> serviceTemplates;
  private String entryDefinitionServiceTemplate;

  /**
   * Instantiates a new Tosca service model.
   *
   * @param artifactFiles                  the artifact files
   * @param serviceTemplates               the service templates
   * @param entryDefinitionServiceTemplate the entry definition service template
   */
  public ToscaServiceModel(FileContentHandler artifactFiles,
                           Map<String, ServiceTemplate> serviceTemplates,
                           String entryDefinitionServiceTemplate) {
    this.artifactFiles = artifactFiles;
    this.serviceTemplates = serviceTemplates;
    this.entryDefinitionServiceTemplate = entryDefinitionServiceTemplate;
  }


  public FileContentHandler getArtifactFiles() {
    return artifactFiles;//MapUtils.isEmpty(artifactFiles) ? Collections.EMPTY_MAP
    // : Collections.unmodifiableMap(artifactFiles);
  }


  public Map<String, ServiceTemplate> getServiceTemplates() {
    return Collections.unmodifiableMap(serviceTemplates);
  }

  public void setServiceTemplates(Map<String, ServiceTemplate> serviceTemplates) {
    this.serviceTemplates = serviceTemplates;
  }

  public String getEntryDefinitionServiceTemplate() {
    return entryDefinitionServiceTemplate;
  }

  public void setEntryDefinitionServiceTemplate(String entryDefinitionServiceTemplate) {
    this.entryDefinitionServiceTemplate = entryDefinitionServiceTemplate;
  }
}
