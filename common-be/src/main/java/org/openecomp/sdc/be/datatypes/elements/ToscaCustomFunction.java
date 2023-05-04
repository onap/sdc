/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToscaCustomFunction implements ToscaFunction, ToscaFunctionParameter {

    private String name;
    private ToscaFunctionType toscaFunctionType;
    private List<ToscaFunctionParameter> parameters = new ArrayList<>();

    @Override
    public ToscaFunctionType getType() {
        return ToscaFunctionType.CUSTOM;
    }

    @Override
    public String getValue() {
        return new Gson().toJson(getJsonObjectValue());
    }

    @Override
    public Object getJsonObjectValue() {
        if (ToscaFunctionType.GET_INPUT.equals(this.toscaFunctionType)) {
            Map<String, Object> getInput = parameters.stream().collect(Collectors.toMap(
                input -> "$" + name,
                input -> {
                    Map<String, Object> inputMap = (Map<String, Object>) input.getJsonObjectValue();
                    return inputMap.get(ToscaFunctionType.GET_INPUT.getName());
                }));
            return getInput;
        }
        return Map.of(
            "$" + name,
            parameters.stream().map(ToscaFunctionParameter::getJsonObjectValue).collect(Collectors.toList())
        );
    }

    public void addParameter(final ToscaFunctionParameter functionParameter) {
        this.parameters.add(functionParameter);
    }

}
