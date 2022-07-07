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
package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;

@Getter
@Setter
public class OperationInputDefinition extends InputDataDefinition {

    private String source;
    private String sourceProperty;
    private String toscaDefaultValue;

    @JsonCreator
    public OperationInputDefinition() {
        super();
    }

    public OperationInputDefinition(String name, InputDataDefinition inputDefinition, String source, String sourceProperty) {
        super(inputDefinition);
        setName(name);
        setSource(source);
        setSourceProperty(sourceProperty);
    }

    public OperationInputDefinition(OperationInputDefinition oid) {
        super(oid);
        setName(oid.getName());
        setSource(oid.getSource());
        setSourceProperty(oid.getSourceProperty());
    }

    public OperationInputDefinition(String name, String property, Boolean mandatory, String type) {
        super();
        setName(name);
        setInputId(property);
        setRequired(mandatory);
        setType(type);
    }

    public String getLabel() {
        return (String) getToscaPresentationValue(JsonPresentationFields.ARTIFACT_LABEL);
    }

    public void setLabel(String name) {
        setToscaPresentationValue(JsonPresentationFields.ARTIFACT_LABEL, name);
    }

}
