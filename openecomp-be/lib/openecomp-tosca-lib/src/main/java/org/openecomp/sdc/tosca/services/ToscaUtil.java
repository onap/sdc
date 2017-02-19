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

import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityType;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    return getServiceTemplateFileName(serviceTemplate.getMetadata().getTemplate_name());
  }

  /**
   * Gets service template file name.
   *
   * @param templateName the template name
   * @return the service template file name
   */
  public static String getServiceTemplateFileName(String templateName) {
    return (Objects.isNull(templateName) ? UUID.randomUUID().toString() : templateName)
        + "ServiceTemplate.yaml";
  }

  /**
   * Add service template to map with key file name.
   *
   * @param serviceTemplates      the service templates
   * @param commonServiceTemplate the common service template
   */
  public static void addServiceTemplateToMapWithKeyFileName(
      Map<String, ServiceTemplate> serviceTemplates, ServiceTemplate commonServiceTemplate) {
    serviceTemplates
        .put(ToscaUtil.getServiceTemplateFileName(commonServiceTemplate), commonServiceTemplate);
  }

  /**
   * Convert type to definition capability definition.
   *
   * @param type           the type
   * @param capabilityType the capability type
   * @param properties     the properties
   * @param description    the description
   * @return the capability definition
   */
  public static CapabilityDefinition convertTypeToDefinition(String type,
                                                             CapabilityType capabilityType,
                                                             Map<String, Object> properties,
                                                             String description) {
    CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
    capabilityDefinition.setAttributes(capabilityType.getAttributes());
    capabilityDefinition.setProperties(capabilityType.getProperties());
    if (description == null) {
      capabilityDefinition.setDescription(capabilityType.getDescription());
    } else {
      capabilityDefinition.setDescription(description);
    }
    capabilityDefinition.setType(type);

    capabilityDefinition.getProperties()
        .entrySet()
        .stream()
        .filter(entry -> properties.containsKey(entry.getKey()))
        .forEach(entry -> entry.getValue()
            .set_default(properties.get(entry.getKey())));


    return capabilityDefinition;

  }

  /**
   * Normalize component name node type map.
   *
   * @param toscaModel the tosca model
   * @param components the components
   * @return the map
   */
  public static Map<String, List<NodeType>> normalizeComponentNameNodeType(
      ToscaServiceModel toscaModel, Set<String> components) {

    Map<String, List<NodeType>> normalizedData = new HashMap<>();
    toscaModel
        .getServiceTemplates()
        .entrySet().stream().filter(entry -> entry
        .getValue()
        .getNode_types() != null)
        .forEach(entry -> entry
            .getValue()
            .getNode_types()
            .entrySet().stream()
            .filter(nodeTypeEntry -> components
                .contains(nodeTypeEntry
                    .getKey()))
            .forEach(nodeTypeEntry -> addNodeType(nodeTypeEntry.getKey(), nodeTypeEntry.getValue(),
                normalizedData)));
    return normalizedData;
  }

  private static void addNodeType(String key, NodeType value,
                                  Map<String, List<NodeType>> normalizedData) {
    if (!normalizedData.containsKey(key)) {
      normalizedData.put(key, new ArrayList<>());
    }
    normalizedData.get(key).add(value);
  }
}
