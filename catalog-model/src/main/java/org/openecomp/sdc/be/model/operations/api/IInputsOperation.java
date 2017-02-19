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

package org.openecomp.sdc.be.model.operations.api;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.resources.data.AttributeData;
import org.openecomp.sdc.be.resources.data.InputsData;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public interface IInputsOperation {

	Either<String, StorageOperationStatus> deleteInput(String inputId);

	Either<List<InputDefinition>, TitanOperationStatus> addInputsToGraph(String componentId, NodeTypeEnum nodeType,
			Map<String, InputDefinition> inputs, Map<String, DataTypeDefinition> dataTypes);

	Either<List<InputDefinition>, StorageOperationStatus> addInputsToComponent(String resourceId, NodeTypeEnum nodeType,
			ComponentInstInputsMap componentInsInputs, Map<String, DataTypeDefinition> dataTypes);

	TitanOperationStatus findNodeNonInheretedInputs(String uniqueId, List<InputDefinition> inputs);

	Either<List<InputDefinition>, StorageOperationStatus> getInputsOfComponent(String compId, String fromName,
			int amount);

	Either<List<ComponentInstanceInput>, TitanOperationStatus> getAllInputsOfResourceInstance(
			ComponentInstance compInstance);

	Either<Map<String, InputDefinition>, StorageOperationStatus> deleteAllInputsAssociatedToNode(NodeTypeEnum nodeType,
			String uniqueId);

	// TitanOperationStatus findNodeNonInheretedAttribues(String uniqueId,
	// NodeTypeEnum nodeType, List<AttributeDefinition> attributes);

	Either<InputsData, StorageOperationStatus> addInput(String inputName, InputDefinition inputDefinition,
			String componentId, NodeTypeEnum nodeType);

	Either<InputsData, TitanOperationStatus> addInputToGraph(String propertyName, InputDefinition inputDefinition,
			String componentId, NodeTypeEnum nodeType);

	Either<AttributeData, StorageOperationStatus> updateInput(String inputId, InputDefinition newInDef,
			Map<String, DataTypeDefinition> dataTypes);

	TitanOperationStatus findAllResourceInputs(String uniqueId, List<InputDefinition> inputs);

	Either<InputDefinition, StorageOperationStatus> getInputById(String uniqueId, boolean skipProperties,
			boolean skipinputsValue);

	TitanOperationStatus addInputsToGraph(TitanVertex metadata, String componentId, Map<String, InputDefinition> inputs,
			Map<String, DataTypeDefinition> dataTypes);

}
