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
package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields.TOSCA_RESOURCE_NAME;

public class InterfaceDataDefinition extends ToscaDataDefinition implements Serializable {
    @Getter
    @Setter
    private boolean userCreated;

    public InterfaceDataDefinition(String type, String description) {
        this();
        setType(type);
        setDescription(description);
    }

    @JsonCreator
    public InterfaceDataDefinition() {
        super();
        setOperations(new HashMap<>());
    }

    public InterfaceDataDefinition(final InterfaceDataDefinition interfaceDataDefinition) {
        setUniqueId(interfaceDataDefinition.getUniqueId());
        setType(interfaceDataDefinition.getType());
        setDescription(interfaceDataDefinition.getDescription());
        setToscaResourceName(interfaceDataDefinition.getToscaResourceName());
        setOperations(interfaceDataDefinition.getOperations());
        setInputs(interfaceDataDefinition.getInputs());
        setUserCreated(interfaceDataDefinition.isUserCreated());
    }

    public String getUniqueId() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
    }

    public void setUniqueId(String uniqueId) {
        setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, uniqueId);
    }

    public void setType(String type) {
        setToscaPresentationValue(JsonPresentationFields.TYPE, type);
    }

    public String getDescription() {
        return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
    }

    public void setDescription(String description) {
        setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
    }

    public String getDerivedFrom() {
        return (String) getToscaPresentationValue(JsonPresentationFields.DERIVED_FROM);
    }

    public void setDerivedFrom(final String derivedFrom) {
        setToscaPresentationValue(JsonPresentationFields.DERIVED_FROM, derivedFrom);
    }

    public void setVersion(final String version) {
        setToscaPresentationValue(JsonPresentationFields.VERSION, version);
    }

    public String getToscaResourceName() {
        return (String) getToscaPresentationValue(TOSCA_RESOURCE_NAME);
    }

    public Map<String, OperationDataDefinition> getOperations() {
        return (Map<String, OperationDataDefinition>) getToscaPresentationValue(JsonPresentationFields.OPERATIONS);
    }

    public void setOperations(Map<String, OperationDataDefinition> operations) {
        setToscaPresentationValue(JsonPresentationFields.OPERATIONS, operations);
    }

    public void setToscaResourceName(String toscaResourceName) {
        setToscaPresentationValue(TOSCA_RESOURCE_NAME, toscaResourceName);
    }

    public Long getCreationDate() {
        return (Long) getToscaPresentationValue(JsonPresentationFields.CREATION_DATE);
    }

    public void setCreationDate(Long creationDate) {
        setToscaPresentationValue(JsonPresentationFields.CREATION_DATE, creationDate);
    }

    public Long getLastUpdateDate() {
        return (Long) getToscaPresentationValue(JsonPresentationFields.LAST_UPDATE_DATE);
    }

    public void setLastUpdateDate(Long lastUpdateDate) {
        setToscaPresentationValue(JsonPresentationFields.LAST_UPDATE_DATE, lastUpdateDate);
    }

    public Map<String, InputDataDefinition> getInputs() {
        return (Map<String, InputDataDefinition>) getToscaPresentationValue(JsonPresentationFields.INTERFACE_INPUT);
    }

    public void setInputs(final Map<String, InputDataDefinition> inputs) {
        setToscaPresentationValue(JsonPresentationFields.INTERFACE_INPUT, inputs);
    }
}
