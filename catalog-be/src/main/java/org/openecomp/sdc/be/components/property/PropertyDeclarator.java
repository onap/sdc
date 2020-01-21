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

package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.List;

public interface PropertyDeclarator {

    /**
     * creates a list of inputs from the given list of properties and updates the properties accordingly
     * @param component the container
     * @param propertiesOwnerId the id of the owner of the properties to declare (e.g ComponentInstance, Policy, Group etc)
     * @param propsToDeclare the list of properties that are being declared as inputs
     * @return the list of inputs that were created from the given properties
     */
    Either<List<InputDefinition>, StorageOperationStatus> declarePropertiesAsInputs(Component component, String propertiesOwnerId, List<ComponentInstancePropInput> propsToDeclare);

    /**
     * returns the values of declared properties to each original state before it was declared as an input.
     * this function is to be called when an input, that was created by declaring a property, is deleted.
     * @param component the container of the input to be deleted
     * @param input the input to be deleted
     */
    StorageOperationStatus unDeclarePropertiesAsInputs(Component component, InputDefinition input);

    /**
     * creates a list of policies from the given list of properties and updates the properties accordingly
     * @param component the container
     * @param propertiesOwnerId the id of the owner of the properties to declare (e.g ComponentInstance, Policy, Group etc)
     * @param propsToDeclare the list of properties that are being declared as inputs
     * @return the list of policies that were created from the given properties
     */
    Either<List<PolicyDefinition>, StorageOperationStatus> declarePropertiesAsPolicies(Component component, String propertiesOwnerId, List<ComponentInstancePropInput> propsToDeclare);

    /**
     * returns the values of declared properties to each original state before it was declared as an policy.
     * this function is to be called when an policy, that was created by declaring a property, is deleted.
     * @param component the container of the input to be deleted
     * @param policy the policy to be deleted
     */
    StorageOperationStatus unDeclarePropertiesAsPolicies(Component component, PolicyDefinition policy);

    /**
     * Updates given list of properties to get values from the specified "list input" with get_input function.
     * This function does NOT create "list input", it needs to be created separately.
     * @param component the container
     * @param propertiesOwnerId the id of the owner of the properties to declare (e.g ComponentInstance, Policy, Group etc)
     * @param propsToDeclare the list of properties that are being declared as inputs
     * @param input the input from which properties get values
     * @return the input same as passed one at 4th argument
     */
    Either<InputDefinition, StorageOperationStatus> declarePropertiesAsListInput(Component component, String propertiesOwnerId, List<ComponentInstancePropInput> propsToDeclare, InputDefinition input);

    /**
     * Un declare properties declared as list type input
     * @param component the container of the input to be deleted
     * @param input the input to be deleted
     */
    StorageOperationStatus unDeclarePropertiesAsListInputs(Component component, InputDefinition input);
}
