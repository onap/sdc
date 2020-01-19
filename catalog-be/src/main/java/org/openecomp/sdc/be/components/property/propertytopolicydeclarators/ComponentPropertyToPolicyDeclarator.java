/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.property.propertytopolicydeclarators;

import fj.data.Either;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.property.DefaultPropertyDeclarator;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Component
public class ComponentPropertyToPolicyDeclarator extends DefaultPropertyDeclarator<Component, PropertyDataDefinition> {

    private ToscaOperationFacade toscaOperationFacade;
    PropertyBusinessLogic propertyBL;


    public ComponentPropertyToPolicyDeclarator(ComponentsUtils componentsUtils, PropertyOperation propertyOperation,
            ToscaOperationFacade toscaOperationFacade, PropertyBusinessLogic propertyBusinessLogic) {
        super(componentsUtils, propertyOperation);
        this.toscaOperationFacade = toscaOperationFacade;
        this.propertyBL = propertyBusinessLogic;
    }

    @Override
    public PropertyDataDefinition createDeclaredProperty(PropertyDataDefinition prop) {
        return new PropertyDataDefinition(prop);
    }

    @Override
    public Either<?, StorageOperationStatus> updatePropertiesValues(Component component, String policyId,
            List<PropertyDataDefinition> properties) {
        if(CollectionUtils.isNotEmpty(properties)) {
            for(PropertyDataDefinition property : properties) {
                Either<PropertyDefinition, StorageOperationStatus>
                        storageStatus = toscaOperationFacade
                                                .updatePropertyOfComponent(component, new PropertyDefinition(property));
                if(storageStatus.isRight()) {
                    return Either.right(storageStatus.right().value());
                }
            }
        }
        return Either.left(properties);

    }

    @Override
    public Optional<Component> resolvePropertiesOwner(Component component, String propertyId) {
        return Optional.of(component);
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition input) {
        // no need for implementation since we are in a policy scenario
        return StorageOperationStatus.OK;
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsListInputs(Component component, InputDefinition input) {
        // no need for implementation since we are in a policy scenario
        return StorageOperationStatus.OK;
    }

    @Override
    public void addPropertiesListToInput(PropertyDataDefinition declaredProp, InputDefinition input) {
        // no need for implementation since we are in a policy scenario
    }

    @Override
    public StorageOperationStatus unDeclarePropertiesAsPolicies(Component component, PolicyDefinition policy) {
        Optional<PropertyDefinition> propertyToUpdateCandidate =
                getDeclaredPropertyByPolicyId(component, policy.getUniqueId());

        if(propertyToUpdateCandidate.isPresent()) {
            return unDeclarePolicy(component, propertyToUpdateCandidate.get(), policy);
        }

        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus unDeclarePolicy(Component component, PropertyDefinition propertyToUpdate, PolicyDefinition policy) {
        updatePropertyAfterUndeclaration(propertyToUpdate, policy);

        Either<PropertyDefinition, StorageOperationStatus> status = toscaOperationFacade
                                                                            .updatePropertyOfComponent(component, propertyToUpdate);
        if(status.isRight()) {
            return status.right().value();
        }

        return StorageOperationStatus.OK;
    }

    private void updatePropertyAfterUndeclaration(PropertyDefinition propertyToUpdate, PolicyDefinition policy) {
        List<GetPolicyValueDataDefinition> getPolicyValues = propertyToUpdate.getGetPolicyValues();
        Optional<GetPolicyValueDataDefinition> getPolicyCandidateToRemove = getPolicyValues.stream()
                                                                                    .filter(getPolicyValue -> getPolicyValue.getPolicyId()
                                                                                                                      .equals(policy.getUniqueId()))
                                                                                    .findAny();

        getPolicyCandidateToRemove.ifPresent(getPolicyValue -> {
            getPolicyValues.remove(getPolicyValue);
            propertyToUpdate.setValue(getPolicyValue.getOrigPropertyValue());
        });
    }

    private Optional<PropertyDefinition> getDeclaredPropertyByPolicyId(Component component,
            String policyId) {
        List<PropertyDefinition> properties = component.getProperties();

        if(CollectionUtils.isEmpty(properties)) {
            return Optional.empty();
        }

        for(PropertyDefinition propertyDefinition : properties) {
            List<GetPolicyValueDataDefinition> getPolicyValues = propertyDefinition.getGetPolicyValues();
            if(CollectionUtils.isEmpty(getPolicyValues)) {
                continue;
            }


            Optional<GetPolicyValueDataDefinition> getPolicyCandidate =
                    getPolicyValues.stream().filter(getPolicy -> getPolicy.getPolicyId().equals(policyId)).findAny();

            if(getPolicyCandidate.isPresent()) {
                propertyDefinition.setValue(getPolicyCandidate.get().getOrigPropertyValue());
                return Optional.of(propertyDefinition);
            }
        }

        return Optional.empty();
    }

}
