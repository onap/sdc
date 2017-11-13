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

import org.apache.commons.collections.MapUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.datatypes.model.AsdcModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tosca service model.
 */
public class ToscaServiceModel implements AsdcModel {
  private FileContentHandler artifactFiles;
  private FileContentHandler externalFiles;
  private Map<String, ServiceTemplate> serviceTemplates;
  private String entryDefinitionServiceTemplate;

  public ToscaServiceModel() {
  }

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

  public ToscaServiceModel(FileContentHandler artifactFiles,
                           FileContentHandler externalFiles,
                           Map<String, ServiceTemplate> serviceTemplates,
                           String entryDefinitionServiceTemplate) {
    this.artifactFiles = artifactFiles;
    this.externalFiles = externalFiles;
    this.serviceTemplates = serviceTemplates;
    this.entryDefinitionServiceTemplate = entryDefinitionServiceTemplate;
  }

  /**
   * Gets artifact files.
   *
   * @return the artifact files
   */
  public FileContentHandler getArtifactFiles() {
    return artifactFiles;
  }

  public void setArtifactFiles(FileContentHandler artifactFiles) {
    this.artifactFiles = artifactFiles;
  }

  /**
   * Gets service templates.
   *
   * @return the service templates
   */
  public Map<String, ServiceTemplate> getServiceTemplates() {
    return Collections.unmodifiableMap(serviceTemplates);
  }

  public void addServiceTemplate(String serviceTemplateName,
                                 ServiceTemplate serviceTemplate) {
    if(MapUtils.isEmpty(serviceTemplates)){
      serviceTemplates = new HashMap<>();
    }

    serviceTemplates.put(serviceTemplateName, serviceTemplate);
  }

  /**
   * Sets service templates.
   *
   * @param serviceTemplates the service templates
   */
  public void setServiceTemplates(Map<String, ServiceTemplate> serviceTemplates) {
    this.serviceTemplates = serviceTemplates;
  }

  /**
   * Gets entry definition service template.
   *
   * @return the entry definition service template
   */
  public String getEntryDefinitionServiceTemplate() {
    return entryDefinitionServiceTemplate;
  }

  /**
   * Sets entry definition service template.
   *
   * @param entryDefinitionServiceTemplate the entry definition service template
   */
  public void setEntryDefinitionServiceTemplate(String entryDefinitionServiceTemplate) {
    this.entryDefinitionServiceTemplate = entryDefinitionServiceTemplate;
  }

  /**
   * Gets cloned service model.
   *
   * @param toscaServiceModel the tosca service model
   * @return the cloned service model
   */
  public static ToscaServiceModel getClonedServiceModel(ToscaServiceModel toscaServiceModel) {
    return ToscaServiceModel.class.cast(DataModelUtil.getClonedObject(toscaServiceModel));
  }
}
