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

package org.openecomp.sdc.translator.services.heattotosca.mapping;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.ConfigConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslatorHeatToToscaPropertyConverter {

  /**
   * Gets tosca properties simple conversion.
   *
   * @param heatProperties            the heat properties
   * @param toscaProperties           the tosca properties
   * @param heatFileName              the heat file name
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param resourceType              the resource type
   * @param template                  the template
   * @param context                   the context
   * @return the tosca properties simple conversion
   */
  //Convert property assuming the property type in heat is same as the property type in tosca
  public static Map<String, Object> getToscaPropertiesSimpleConversion(ServiceTemplate
                                                                           serviceTemplate,
      String resourceId,Map<String, Object> heatProperties, Map<String, Object> toscaProperties,
      String heatFileName, HeatOrchestrationTemplate heatOrchestrationTemplate, String
      resourceType, Template template, TranslationContext context) {

    toscaProperties = toscaProperties != null ? toscaProperties : new HashMap<>();

    for (String heatPropertyName : context.getElementSet(resourceType, Constants.PROP)) {

      setSimpleProperty(serviceTemplate, resourceId, heatProperties, heatFileName, resourceType,
          heatOrchestrationTemplate,
          context, toscaProperties, heatPropertyName, null, template);
    }
    return toscaProperties;
  }

  /**
   * Sets simple property.
   *
   * @param heatProperties            the heat properties
   * @param heatFileName              the heat file name
   * @param resourceType              the resource type
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param context                   the context
   * @param toscaProperties           the tosca properties
   * @param heatPropertyName          the heat property name
   * @param toscaPropertyName         the tosca property name
   * @param template                  the template
   */
  public static void setSimpleProperty(ServiceTemplate serviceTemplate,String resourceId,
                                       Map<String, Object> heatProperties,
                                       String heatFileName,
                                       String resourceType,
                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                       TranslationContext context,
                                       Map<String, Object> toscaProperties, String heatPropertyName,
                                       String toscaPropertyName, Template template) {
    Object propertyValue = null;
    if (heatProperties != null) {
      propertyValue = heatProperties.get(heatPropertyName);
    }
    if (propertyValue == null) {
      return;
    }

    if (toscaPropertyName == null) {
      toscaPropertyName = resourceType == null ? heatPropertyName
          : context.getElementMapping(resourceType, Constants.PROP, heatPropertyName);
      if (toscaPropertyName == null) {
        return;
      }
    }
    toscaProperties.put(toscaPropertyName,
        getToscaPropertyValue(serviceTemplate, resourceId,heatPropertyName, propertyValue,
            resourceType,
            heatFileName, heatOrchestrationTemplate, template, context));
  }


  /**
   * Gets tosca property value.
   *
   * @param propertyName              the property name
   * @param propertyValue             the property value
   * @param resourceType              the resource type
   * @param heatFileName              the heat file name
   * @param heatOrchestrationTemplate the heat orchestration template
   * @param template                  the template
   * @param context                   the context
   * @return the tosca property value
   */
  public static Object getToscaPropertyValue(ServiceTemplate serviceTemplate, String resourceId,
                                             String propertyName, Object propertyValue,
                                             String  resourceType, String heatFileName,
                                             HeatOrchestrationTemplate heatOrchestrationTemplate,
                                             Template template, TranslationContext context) {
    if (propertyValue instanceof Map && !((Map) propertyValue).isEmpty()) {
      Map.Entry<String, Object> functionMapEntry =
          (Map.Entry<String, Object>) ((Map) propertyValue).entrySet().iterator().next();
      if (FunctionTranslationFactory.getInstance(functionMapEntry.getKey()).isPresent()) {
        return FunctionTranslationFactory.getInstance(functionMapEntry.getKey()).get()
            .translateFunction(serviceTemplate, resourceId, propertyName ,functionMapEntry
                .getKey(), functionMapEntry
                .getValue(),  heatFileName, heatOrchestrationTemplate, template, context);
      }
      Map<String, Object> propertyValueMap = new HashMap<>();
      for (Map.Entry<String, Object> entry : ((Map<String, Object>) propertyValue).entrySet()) {
        String toscaPropertyName = resourceType == null ? null : context
            .getElementMapping(resourceType, Constants.PROP, propertyName
                + ConfigConstants.TRANS_MAPPING_DELIMITER_CHAR + entry.getKey());
        toscaPropertyName = toscaPropertyName != null ? toscaPropertyName : entry.getKey();
        propertyValueMap.put(toscaPropertyName,
            getToscaPropertyValue(serviceTemplate, resourceId,
                propertyName + ConfigConstants.TRANS_MAPPING_DELIMITER_CHAR
                    + entry.getKey(), entry.getValue(), resourceType, heatFileName,
                heatOrchestrationTemplate, template, context));
      }
      return propertyValueMap;
    } else if (propertyValue instanceof List && !((List) propertyValue).isEmpty()) {
      List propertyValueArray = new ArrayList<>();
      for (int i = 0; i < ((List) propertyValue).size(); i++) {
        propertyValueArray.add(
            getToscaPropertyValue(serviceTemplate, resourceId, propertyName, ((List)
                propertyValue).get(i),
                resourceType, heatFileName, heatOrchestrationTemplate, template, context));
      }
      return propertyValueArray;
    }
    return propertyValue;
  }
}
