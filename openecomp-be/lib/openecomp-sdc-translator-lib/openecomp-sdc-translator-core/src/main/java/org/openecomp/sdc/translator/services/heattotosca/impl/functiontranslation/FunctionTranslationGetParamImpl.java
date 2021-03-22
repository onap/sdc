/*
 * Copyright © 2016-2018 European Support Limited
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
package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.heat.datatypes.model.HeatPseudoParameters;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslation;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;

public class FunctionTranslationGetParamImpl implements FunctionTranslation {

    private static Object translateGetParamFunctionExpression(FunctionTranslator functionTranslator) {
        Object functionValue = functionTranslator.getFunctionValue();
        Object returnValue = null;
        if (functionValue instanceof String) {
            returnValue = functionValue;
            if (HeatPseudoParameters.getPseudoParameterNames().contains(functionValue)) {
                functionTranslator.getContext()
                    .addUsedHeatPseudoParams(functionTranslator.getHeatFileName(), (String) functionValue, (String) functionValue);
            }
        } else if (functionValue instanceof List) {
            returnValue = new ArrayList<>();
            for (int i = 0; i < ((List) functionValue).size(); i++) {
                Object paramValue = ((List) functionValue).get(i);
                if ((paramValue instanceof Map && !((Map) paramValue).isEmpty())) {
                    Map<String, Object> paramMap = (Map) paramValue;
                    ((List) returnValue).add(translatedInnerMap(functionTranslator, paramMap));
                } else {
                    ((List) returnValue).add(paramValue);
                }
            }
        }
        return returnValue;
    }

    private static Object translatedInnerMap(FunctionTranslator functionTranslator, Map<String, Object> paramMap) {
        Map<String, Object> translatedInnerMapValue = new HashMap<>();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            Optional<FunctionTranslation> functionTranslationInstance = FunctionTranslationFactory.getInstance(entry.getKey());
            if (functionTranslationInstance.isPresent()) {
                functionTranslator.setFunctionValue(entry.getValue());
                return functionTranslationInstance.get().translateFunction(functionTranslator);
            } else {
                translatedInnerMapValue.put(entry.getKey(), translatedInnerValue(functionTranslator, entry.getValue()));
            }
        }
        return translatedInnerMapValue;
    }

    private static Object translatedInnerValue(FunctionTranslator functionTranslator, Object value) {
        if (value instanceof String) {
            return value;
        } else if (value instanceof Map) {
            return translatedInnerMap(functionTranslator, (Map<String, Object>) value);
        } else if (value instanceof List) {
            List<Object> returnedList = new ArrayList<>();
            for (int i = 0; i < ((List) value).size(); i++) {
                returnedList.add(translatedInnerValue(functionTranslator, ((List) value).get(i)));
            }
            return returnedList;
        }
        return value;
    }

    @Override
    public Object translateFunction(FunctionTranslator functionTranslator) {
        Map<String, Object> returnValue = new HashMap<>();
        returnValue.put(ToscaFunctions.GET_INPUT.getFunctionName(), translateGetParamFunctionExpression(functionTranslator));
        return returnValue;
    }
}
