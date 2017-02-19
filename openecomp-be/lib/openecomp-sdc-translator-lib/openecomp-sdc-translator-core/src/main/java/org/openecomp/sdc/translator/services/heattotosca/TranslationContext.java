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

package org.openecomp.sdc.translator.services.heattotosca;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestFile;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class TranslationContext {


  private static Map<String, Map<String, Map<String, String>>> translationMapping;
  private static Map<String, ServiceTemplate> globalServiceTemplates;

  static {

    String propertyFileName = AsdcCommon.HEAT_TO_TOSCA_MAPPING_CONF;
    InputStream is = FileUtils.getFileInputStream(propertyFileName);
    translationMapping = JsonUtil.json2Object(is, Map.class);
    globalServiceTemplates = GlobalTypesGenerator.getGlobalTypesServiceTemplate();
  }

  private ManifestFile manifest;

  private FileContentHandler files = new FileContentHandler();

  private Map<String, FileData.Type> manifestFiles = new HashMap<>();
  //Key - file name, value - file type
  private Set<String> nestedHeatsFiles = new HashSet<>();
  private FileContentHandler externalArtifacts = new FileContentHandler();

  private Map<String, Set<String>> translatedResources = new HashMap<>();
  // Key - heat file name,value - set of heat resource ids which were translated
  private Map<String, Set<String>> heatStackGroupMembers = new HashMap<>();
  // Key - heat file name, value - translated Node template id
  private Map<String, Map<String, String>> translatedIds = new HashMap<>();
  // Key - heat file name, value - Map with Key - heat resource Id, Value - tosca entity template id
  private Map<String, ServiceTemplate> translatedServiceTemplates = new HashMap<>();
  // key - service template type, value - translated service templates
  private Map<String, TranslatedHeatResource> heatSharedResourcesByParam = new HashMap<>();
  //key - heat param name, value - shared resource data

  public void addManifestFile(String fileName, FileData.Type fileType) {
    this.manifestFiles.put(fileName, fileType);
  }

  public Set<String> getNestedHeatsFiles() {
    return nestedHeatsFiles;
  }

  public Map<String, Set<String>> getHeatStackGroupMembers() {
    return heatStackGroupMembers;
  }

  public FileContentHandler getFiles() {
    return files;
  }

  public void setFiles(Map<String, byte[]> files) {
    this.files.putAll(files);
  }

  public InputStream getFileContent(String fileName) {
    return files.getFileContent(fileName);
  }

  public void addFile(String name, byte[] content) {
    files.addFile(name, content);
  }

  public ManifestFile getManifest() {
    return manifest;
  }

  public void setManifest(ManifestFile manifest) {
    this.manifest = manifest;
  }

  public Map<String, Set<String>> getTranslatedResources() {
    return translatedResources;
  }

  public Map<String, Map<String, String>> getTranslatedIds() {
    return translatedIds;
  }

  // get tosca name from mapping configuration file
  //element type - parameter/attribute
  // element name - heat parameter/attribute name
  //return value - tosca parameter/attribute name
  public String getElementMapping(String resourceType, String elementType, String elementName) {
    return translationMapping.get(resourceType).get(elementType).get(elementName);
  }

  public Map<String, String> getElementMapping(String resourceType, String elementType) {
    return translationMapping.get(resourceType).get(elementType);
  }

  public Set<String> getElementSet(String resourceType, String elementType) {
    return translationMapping.get(resourceType).get(elementType).keySet();
  }

  public Map<String, ServiceTemplate> getTranslatedServiceTemplates() {
    return translatedServiceTemplates;
  }

  public ServiceTemplate getGlobalSubstitutionServiceTemplate() {
    return getTranslatedServiceTemplates().get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
  }

  public FileContentHandler getExternalArtifacts() {
    return externalArtifacts;
  }

  public void addExternalArtifacts(String name, byte[] content) {
    this.externalArtifacts.addFile(name, content);
  }


  public Map<String, TranslatedHeatResource> getHeatSharedResourcesByParam() {
    return heatSharedResourcesByParam;
  }

  public void addHeatSharedResourcesByParam(String parameterName, String resourceId,
                                            Resource resource) {
    this.addHeatSharedResourcesByParam(parameterName,
        new TranslatedHeatResource(resourceId, resource));
  }

  public void addHeatSharedResourcesByParam(String parameterName,
                                            TranslatedHeatResource translatedHeatResource) {
    this.heatSharedResourcesByParam.put(parameterName, translatedHeatResource);
  }

  public Map<String, ServiceTemplate> getGlobalServiceTemplates() {
    return globalServiceTemplates;
  }


}
