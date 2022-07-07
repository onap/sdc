/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

@Data
public class ToscaGetFunctionDataDefinition implements ToscaFunction, ToscaFunctionParameter {

    private String propertyUniqueId;
    private String propertyName;
    private PropertySource propertySource;
    private String sourceUniqueId;
    private String sourceName;
    private ToscaGetFunctionType functionType;
    private List<String> propertyPathFromSource = new ArrayList<>();

    public ToscaGetFunctionDataDefinition() {
        //necessary for JSON conversions
    }

    public boolean isSubProperty() {
        return propertyPathFromSource != null && propertyPathFromSource.size() > 1;
    }

    /**
     * Builds the value of a property based on the TOSCA get function information.
     */
    public String generatePropertyValue() {
        return new Gson().toJson(getJsonObjectValue());
    }

    @Override
    public Object getJsonObjectValue() {
        if (functionType == null) {
            throw new IllegalStateException("functionType is required in order to generate the get function value");
        }
        if (CollectionUtils.isEmpty(propertyPathFromSource)) {
            throw new IllegalStateException("propertyPathFromSource is required in order to generate the get function value");
        }

        if (functionType == ToscaGetFunctionType.GET_PROPERTY || functionType == ToscaGetFunctionType.GET_ATTRIBUTE) {
            return buildFunctionValueWithPropertySource();
        }
        if (functionType == ToscaGetFunctionType.GET_INPUT) {
            return buildGetInputFunctionValue();
        }

        throw new UnsupportedOperationException(String.format("ToscaGetFunctionType '%s' is not supported yet", functionType));
    }

    private Map<String, Object> buildFunctionValueWithPropertySource() {
        if (propertySource == null) {
            throw new IllegalStateException(
                String.format("propertySource is required in order to generate the %s value", functionType.getFunctionName())
            );
        }
        if (propertySource == PropertySource.SELF) {
            return Map.of(functionType.getFunctionName(),
                Stream.concat(Stream.of(PropertySource.SELF.getName()), propertyPathFromSource.stream()).collect(Collectors.toList())
            );
        }
        if (propertySource == PropertySource.INSTANCE) {
            if (sourceName == null) {
                throw new IllegalStateException(
                    String.format("sourceName is required in order to generate the %s from INSTANCE value", functionType.getFunctionName())
                );
            }
            return Map.of(functionType.getFunctionName(),
                Stream.concat(Stream.of(sourceName), propertyPathFromSource.stream()).collect(Collectors.toList())
            );
        }

        throw new UnsupportedOperationException(String.format("Tosca property source '%s' not supported", propertySource));
    }

    private Map<String, Object> buildGetInputFunctionValue() {
        if (this.propertyPathFromSource.size() == 1) {
            return Map.of(this.functionType.getFunctionName(), this.propertyPathFromSource.get(0));
        }
        return Map.of(this.functionType.getFunctionName(), this.propertyPathFromSource);
    }

    @Override
    public ToscaFunctionType getType() {
        if (functionType == null) {
            return null;
        }
        switch (functionType) {
            case GET_INPUT:
                return ToscaFunctionType.GET_INPUT;
            case GET_PROPERTY:
                return ToscaFunctionType.GET_PROPERTY;
            case GET_ATTRIBUTE:
                return ToscaFunctionType.GET_ATTRIBUTE;
            default:
                return null;
        }
    }

    @Override
    public String getValue() {
        return this.generatePropertyValue();
    }

}
