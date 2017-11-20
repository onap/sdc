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

package org.openecomp.sdc.heat.services;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by TALIO on 2/19/2017.
 */
public class HeatStructureUtil {

  /**
   * Gets referenced values by function name.
   *
   * @param filename      the filename
   * @param functionName  the function name
   * @param propertyValue the property value
   * @param globalContext the global context
   * @return the referenced values by function name
   */
  public static Set<String> getReferencedValuesByFunctionName(String filename, String functionName,
                                                              Object propertyValue,
                                                              GlobalValidationContext globalContext) {
    Set<String> valuesNames = new HashSet<>();
    if (propertyValue instanceof Map) {
      Map<String, Object> currPropertyMap = (Map<String, Object>) propertyValue;
      if (currPropertyMap.containsKey(functionName)) {
        Object getFunctionValue = currPropertyMap.get(functionName);
        if (!(getFunctionValue instanceof String) && functionName.equals(
            ResourceReferenceFunctions.GET_RESOURCE.getFunction())) {
          globalContext.addMessage(filename, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                  .getErrorWithParameters(globalContext.getMessageCode(), Messages.INVALID_GET_RESOURCE_SYNTAX.getErrorMessage(),
                      getFunctionValue == null ? "null" : getFunctionValue.toString()),
              LoggerTragetServiceName.VALIDATE_GET_RESOURCE, "Invalid get_resource syntax");
          return valuesNames;
        }
        if (getFunctionValue instanceof String) {

          if (functionName.equals(ResourceReferenceFunctions.GET_FILE.getFunction())) {
            getFunctionValue = ((String) getFunctionValue).replace("file:///", "");
          }

          valuesNames.add((String) getFunctionValue);
        } else if (getFunctionValue instanceof List) {
          if (CollectionUtils.isNotEmpty((List) getFunctionValue)) {
            if (((List) getFunctionValue).get(0) instanceof String) {
              valuesNames.add(((String) ((List) getFunctionValue).get(0)).replace("file:///", ""));
            } else {
              valuesNames.addAll(getReferencedValuesByFunctionName(filename, functionName,
                  ((List) getFunctionValue).get(0), globalContext));
            }

          }
        } else {
          valuesNames.addAll(
              getReferencedValuesByFunctionName(filename, functionName, getFunctionValue,
                  globalContext));
        }
      } else {
        for (Map.Entry<String, Object> nestedPropertyMap : currPropertyMap.entrySet()) {
          valuesNames.addAll(getReferencedValuesByFunctionName(filename, functionName,
              nestedPropertyMap.getValue(), globalContext));
        }
      }
    } else if (propertyValue instanceof List) {
      List propertyValueArray = (List) propertyValue;
      for (Object propValue : propertyValueArray) {
        valuesNames.addAll(
            getReferencedValuesByFunctionName(filename, functionName, propValue,
                globalContext));
      }
    }

    return valuesNames;
  }


  public static boolean isNestedResource(String resourceType) {
    if(Objects.isNull(resourceType)){
      return false;
    }
    return resourceType.endsWith(".yaml") || resourceType.endsWith(".yml");
  }

}
