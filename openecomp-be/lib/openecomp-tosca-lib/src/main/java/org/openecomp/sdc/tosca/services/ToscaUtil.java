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

package org.openecomp.sdc.tosca.services;

import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The type Tosca util.
 */
public class ToscaUtil {

  /**
   * Gets service template file name.
   *
   * @param serviceTemplate the service template
   * @return the service template file name
   */
  public static String getServiceTemplateFileName(ServiceTemplate serviceTemplate) {
    if (serviceTemplate == null) {
      return null;
    }
    if (serviceTemplate.getMetadata() == null) {
      return UUID.randomUUID().toString() + "ServiceTemplate.yaml";
    }
    return getServiceTemplateFileName(serviceTemplate.getMetadata());
  }

  /**
   * Gets service template file name.
   *
   * @param metaData the file name
   * @return the service template file name
   */
  public static String getServiceTemplateFileName(Map<String, String> metadata) {
    if (metadata.get(ToscaConstants.ST_METADATA_FILE_NAME) != null) {
      return metadata.get(ToscaConstants.ST_METADATA_FILE_NAME);
    } else if (metadata.get(ToscaConstants.ST_METADATA_TEMPLATE_NAME) != null) {
      return metadata.get(ToscaConstants.ST_METADATA_TEMPLATE_NAME) + "ServiceTemplate.yaml";
    }
    return UUID.randomUUID().toString() + "ServiceTemplate.yaml";

  }


  /**
   * Add service template to map with key file name.
   *
   * @param serviceTemplateMap the service template map
   * @param serviceTemplate    the service template
   */
  public static void addServiceTemplateToMapWithKeyFileName(
      Map<String, ServiceTemplate> serviceTemplateMap, ServiceTemplate serviceTemplate) {
    serviceTemplateMap.put(ToscaUtil.getServiceTemplateFileName(serviceTemplate), serviceTemplate);
  }

  public static String getServiceTemplateFileName(String templateName) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, templateName);
    return getServiceTemplateFileName(metadata);
  }
}
