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
package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.PolicyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.components.property.GetInputUtils.isGetInputValueForInput;

@org.springframework.stereotype.Component
public class PolicyPropertyDeclarator extends DefaultPropertyDeclarator<PolicyDefinition, PropertyDataDefinition> {

    private static final Logger log = Logger.getLogger(PolicyPropertyDeclarator.class);
    private PolicyOperation policyOperation;

    public PolicyPropertyDeclarator(ComponentsUtils componentsUtils, PropertyOperation propertyOperation, PolicyOperation policyOperation) {
        super(componentsUtils, propertyOperation);
        this.policyOperation = policyOperation;
    }

    @Override
    PropertyDataDefinition createDeclaredProperty(PropertyDataDefinition prop) {
        return new PropertyDataDefinition(prop);
    }

    @Override
    Either<?, StorageOperationStatus> updatePropertiesValues(Component component, String policyId, List<PropertyDataDefinition> properties) {
        log.debug("#updatePropertiesValues - updating policies properties for policy {} on component {}", policyId, component.getUniqueId());
        StorageOperationStatus updateStatus = policyOperation.updatePolicyProperties(component, policyId, properties);
        return updateStatus == StorageOperationStatus.OK ? Either.left(updateStatus) : Either.right(updateStatus);
    }

    @Override
    Optional<PolicyDefinition> resolvePropertiesOwner(Component component, String policyId) {
        log.debug("#resolvePropertiesOwner - fetching policy {} of component {}", policyId, component.getUniqueId());
        return Optional.ofNullable(component.getPolicyById(policyId));
    }

    @Override
    void addPropertiesListToInput(PropertyDataDefinition declaredProp, InputDefinition input) {
        List<ComponentInstanceProperty> propertiesList = input.getProperties();
        if(propertiesList == null) {
            propertiesList = new ArrayList<>(); // adding the property with the new value for UI
        }
        propertiesList.add(new ComponentInstanceProperty(declaredProp));
        input.setProperties(propertiesList);

    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition inputForDelete) {
        return getPolicyPropertiesDeclaredAsInput(component, inputForDelete.getUniqueId())
                .map(policyProperties -> unDeclarePolicyProperties(component, inputForDelete, policyProperties))
                .orElse(StorageOperationStatus.OK);
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsListInputs(Component component, InputDefinition inputForDelete) {
        return getPolicyPropertiesDeclaredAsInput(component, inputForDelete.getUniqueId())
                .map(policyProperties -> unDeclarePolicyProperties(component, inputForDelete, policyProperties))
                .orElse(StorageOperationStatus.OK);
    }

    private StorageOperationStatus unDeclarePolicyProperties(Component container, InputDefinition input, PolicyProperties policyProperties) {
        String policyId = policyProperties.getPolicyId();
        List<PropertyDataDefinition> propsDeclaredAsInput = policyProperties.getProperties();
        propsDeclaredAsInput.forEach(policyProp -> prepareValueBeforeDelete(input, policyProp, Collections.emptyList()));
        return policyOperation.updatePolicyProperties(container, policyId, propsDeclaredAsInput);
    }

    private Optional<PolicyProperties> getPolicyPropertiesDeclaredAsInput(Component container, String inputId) {
        if (container.getPolicies() == null) {
            return Optional.empty();
        }
        return container.getPolicies()
                .values()
                .stream()
                .filter(policy -> Objects.nonNull(policy.getProperties()))
                .map(policy -> getPolicyPropertiesDeclaredAsInput(policy, inputId))
                .filter(PolicyProperties::isNotEmpty)
                .findFirst();
    }

    private boolean isPropertyDeclaredAsInputByInputId(PropertyDataDefinition property, String inputId) {
        if (CollectionUtils.isEmpty(property.getGetInputValues())) {
            return false;
        }
        return property.getGetInputValues().stream()
                .filter(Objects::nonNull)
                .anyMatch(getInputVal -> isGetInputValueForInput(getInputVal, inputId));
    }

    private PolicyProperties getPolicyPropertiesDeclaredAsInput(PolicyDefinition policy, String inputId) {
        List<PropertyDataDefinition> collect = policy.getProperties()
                .stream()
                .filter(prop -> isPropertyDeclaredAsInputByInputId(prop, inputId))
                .collect(Collectors.toList());
        return new PolicyProperties(policy.getUniqueId(), collect);

    }

    private class PolicyProperties {
        private String policyId;
        private List<PropertyDataDefinition> properties;

        PolicyProperties(String policyId, List<PropertyDataDefinition> properties) {
            this.policyId = policyId;
            this.properties = (properties == null)? null : new ArrayList<>(properties);
        }

        String getPolicyId() {
            return policyId;
        }

        public List<PropertyDataDefinition> getProperties() {
            return new ArrayList<>(properties);
        }

        boolean isNotEmpty() {
            return CollectionUtils.isNotEmpty(properties);
        }
    }
}
