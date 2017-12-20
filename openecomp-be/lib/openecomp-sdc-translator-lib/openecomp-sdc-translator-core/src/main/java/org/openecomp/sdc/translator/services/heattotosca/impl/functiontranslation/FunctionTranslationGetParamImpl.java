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

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatPseudoParameters;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.Template;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslation;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SHIRIA
 * @since December 15, 2016.
 */
public class FunctionTranslationGetParamImpl implements FunctionTranslation {
  @Override
  public Object translateFunction(ServiceTemplate serviceTemplate, String resourceId,
                                  String propertyName, String functionKey,
                                  Object functionValue, String heatFileName,
                                  HeatOrchestrationTemplate heatOrchestrationTemplate,
                                  Template toscaTemplate, TranslationContext context) {
    Map returnValue = new HashMap<>();
    returnValue.put(ToscaFunctions.GET_INPUT.getDisplayName(),
        translateGetParamFunctionExpression(serviceTemplate, resourceId, propertyName,
            functionValue, heatFileName, heatOrchestrationTemplate, context));
    return returnValue;
  }

  private static Object translateGetParamFunctionExpression(ServiceTemplate serviceTemplate,
                                                            String resourceId,
                                                            String propertyName,Object functionValue,
                                                            String heatFileName,
                                                            HeatOrchestrationTemplate
                                                                heatOrchestrationTemplate,
                                                            TranslationContext context) {
    Object returnValue = null;
    if (functionValue instanceof String) {
      returnValue = functionValue;
      if (HeatPseudoParameters.getPseudoParameterNames().contains(functionValue)) {
        context
            .addUsedHeatPseudoParams(heatFileName, (String) functionValue, (String) functionValue);
      }
    } else if (functionValue instanceof List) {
      returnValue = new ArrayList<>();
      for (int i = 0; i < ((List) functionValue).size(); i++) {
        Object paramValue = ((List) functionValue).get(i);
        if ((paramValue instanceof Map && !((Map) paramValue).isEmpty())) {
          Map<String, Object> paramMap = (Map) paramValue;
          ((List) returnValue).add(translatedInnerMap(serviceTemplate, resourceId,
              propertyName, paramMap, heatFileName, heatOrchestrationTemplate, context));
        } else {
          ((List) returnValue).add(paramValue);
        }
      }
    }

    return returnValue;
  }

  private static Object translatedInnerMap(ServiceTemplate serviceTemplate, String resourceId,
                                           String propertyName, Map<String, Object> paramMap,
                                           String heatFileName,HeatOrchestrationTemplate
                                               heatOrchestrationTemplate,
                                           TranslationContext context) {

    Map<String, Object> translatedInnerMapValue = new HashMap<>();
    for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
      if (FunctionTranslationFactory.getInstance(entry.getKey()).isPresent()) {
        return FunctionTranslationFactory.getInstance(entry.getKey()).get()
            .translateFunction(serviceTemplate, resourceId, propertyName, entry.getKey(),
                entry.getValue(), heatFileName, heatOrchestrationTemplate, null, context);
      } else {
        translatedInnerMapValue.put(entry.getKey(),
            translatedInnerValue(serviceTemplate, resourceId, propertyName,entry.getValue(),
                heatFileName, heatOrchestrationTemplate, context));

      }
    }
    return translatedInnerMapValue;
  }

  private static Object translatedInnerValue(ServiceTemplate serviceTemplate, String resourceId,
                                             String propertyName,Object value, String heatFileName,
                                             HeatOrchestrationTemplate heatOrchestrationTemplate,
                                             TranslationContext context) {
    if (value instanceof String) {
      return value;
    } else if (value instanceof Map) {
      return translatedInnerMap(serviceTemplate, resourceId, propertyName,(Map<String, Object>)
          value, heatFileName, heatOrchestrationTemplate, context);
    } else if (value instanceof List) {
      List returnedList = new ArrayList();
      for (int i = 0; i < ((List) value).size(); i++) {
        returnedList.add(translatedInnerValue(serviceTemplate, resourceId, propertyName,
            ((List) value).get(i), heatFileName, heatOrchestrationTemplate, context));
      }
      return returnedList;
    }

    return value;
  }
}
