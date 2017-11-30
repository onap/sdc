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

import java.util.Map;

import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;

import fj.data.Either;

public interface IInterfaceLifecycleOperation {

	public Either<InterfaceDefinition, StorageOperationStatus> createInterfaceOnResource(InterfaceDefinition interf, String resourceId, String interfaceName, boolean failIfExist, boolean inTransaction);

	public Either<InterfaceDefinition, StorageOperationStatus> addInterfaceToResource(InterfaceDefinition interf, String resourceId, String interfaceName, boolean inTransaction);

	public Either<Operation, StorageOperationStatus> updateInterfaceOperation(String resourceId, String interfaceName, String operationName, Operation interf);

	public Either<Operation, StorageOperationStatus> updateInterfaceOperation(String resourceId, String interfaceName, String operationName, Operation interf, boolean inTransaction);

	public Either<Operation, StorageOperationStatus> deleteInterfaceOperation(String resourceId, String interfaceName, String operationName, boolean inTransaction);

	public Either<Map<String, InterfaceDefinition>, StorageOperationStatus> getAllInterfacesOfResource(String resourceId, boolean recursively, boolean inTransaction);

	public Either<Map<String, InterfaceDefinition>, StorageOperationStatus> getAllInterfacesOfResource(String resourceId, boolean recursively);

	public Either<InterfaceDefinition, StorageOperationStatus> createInterfaceType(InterfaceDefinition interf);

	public Either<InterfaceDefinition, StorageOperationStatus> createInterfaceType(InterfaceDefinition interf, boolean inTransaction);

	public Either<InterfaceDefinition, StorageOperationStatus> getInterface(String interfaceId);

	public String getShortInterfaceName(InterfaceDataDefinition interfaceDefinition);
}
