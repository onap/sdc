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
package org.onap.sdc.tosca.datatypes.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.error.ToscaRuntimeException;
import org.onap.sdc.tosca.services.CommonUtil;
import org.onap.sdc.tosca.services.DataModelCloneUtil;

public class InterfaceType extends Interface {

    protected static final String CONVERT_INTERFACE_TYPE_OBJECT_ERROR = "Could not create InterfaceType from input object, input object -  ";
    private String derived_from;
    private String version;
    private Map<String, String> metadata;
    private String description;
    private Map<String, PropertyDefinition> inputs;
    private Map<String, OperationDefinition> operations;

    public InterfaceType() {
    }

    public InterfaceType(Object toscaInterfaceTypeObj) {
        InterfaceType interfaceType = convertObjToInterfaceType(toscaInterfaceTypeObj);
        this.setDerived_from(interfaceType.getDerived_from());
        this.setVersion(interfaceType.getVersion());
        this.setDescription(interfaceType.getDescription());
        this.setMetadata(DataModelCloneUtil.cloneStringStringMap(interfaceType.getMetadata()));
        this.setInputs(DataModelCloneUtil.cloneStringPropertyDefinitionMap(interfaceType.getInputs()));
        this.setOperations(DataModelCloneUtil.cloneStringOperationDefinitionMap(interfaceType.getOperations()));
    }

    protected InterfaceType convertObjToInterfaceType(Object toscaInterfaceTypeObj) {
        try {
            Optional<InterfaceType> interfaceType = CommonUtil.createObjectUsingSetters(toscaInterfaceTypeObj, this.getClass());
            if (interfaceType.isPresent()) {
                updateInterfaceTypeOperations(CommonUtil.getObjectAsMap(toscaInterfaceTypeObj), interfaceType.get());
                return interfaceType.get();
            } else {
                throw new ToscaRuntimeException(CONVERT_INTERFACE_TYPE_OBJECT_ERROR + toscaInterfaceTypeObj.toString());
            }
        } catch (Exception exc) {
            throw new ToscaRuntimeException(CONVERT_INTERFACE_TYPE_OBJECT_ERROR + toscaInterfaceTypeObj.toString(), exc);
        }
    }

    private void updateInterfaceTypeOperations(Map<String, Object> interfaceAsMap, InterfaceType interfaceType) {
        Set<String> fieldNames = CommonUtil.getClassFieldNames(interfaceType.getClass());
        for (Map.Entry<String, Object> entry : interfaceAsMap.entrySet()) {
            Optional<Map.Entry<String, ? extends OperationDefinition>> operationDefinition = createOperation(entry.getKey(), entry.getValue(),
                fieldNames, OperationDefinitionType.class);
            operationDefinition.ifPresent(operation -> interfaceType.addOperation(operation.getKey(), operation.getValue()));
        }
    }

    public String getDerived_from() {
        return derived_from;
    }

    public void setDerived_from(String derivedFrom) {
        this.derived_from = derivedFrom;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, PropertyDefinition> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, PropertyDefinition> inputs) {
        this.inputs = inputs;
    }

    public Map<String, OperationDefinition> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, OperationDefinition> operations) {
        this.operations = operations;
    }

    public void addOperation(String operationName, OperationDefinition operationDefinition) {
        if (MapUtils.isEmpty(this.operations)) {
            this.operations = new HashMap<>();
        }
        this.operations.put(operationName, operationDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(derived_from, version, metadata, description, inputs, operations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InterfaceType)) {
            return false;
        }
        InterfaceType that = (InterfaceType) o;
        return Objects.equals(derived_from, that.derived_from) && Objects.equals(version, that.version) && Objects.equals(metadata, that.metadata)
            && Objects.equals(description, that.description) && Objects.equals(inputs, that.inputs) && Objects.equals(operations, that.operations);
    }

    public Optional<Object> convertInterfaceTypeToToscaObj() {
        return convertInterfaceToToscaInterfaceObj(this);
    }
}
