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
package org.onap.sdc.tosca.datatypes.model;

public class ParameterDefinition extends DefinitionOfDataType {

    @Override
    public ParameterDefinition clone() {
        DefinitionOfDataType definitionOfDataType = super.clone();
        ParameterDefinition parameterDefinition = new ParameterDefinition();
        parameterDefinition.set_default(definitionOfDataType.get_default());
        parameterDefinition.setConstraints(definitionOfDataType.getConstraints());
        parameterDefinition.setDescription(definitionOfDataType.getDescription());
        parameterDefinition.setEntry_schema(definitionOfDataType.getEntry_schema());
        parameterDefinition.setRequired(definitionOfDataType.getRequired());
        parameterDefinition.setType(definitionOfDataType.getType());
        parameterDefinition.setStatus(definitionOfDataType.getStatus());
        parameterDefinition.setValue(definitionOfDataType.getValue());
        return parameterDefinition;
    }
}
