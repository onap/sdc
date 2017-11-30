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

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;

import fj.data.Either;

public interface IComponentInstanceOperation {

	public Either<Integer, StorageOperationStatus> increaseAndGetResourceInstanceSpecificCounter(String resourceInstanceId, GraphPropertiesDictionary counterType, boolean inTransaction);

	/**
	 * Adds Attribute to resource instance
	 * 
	 * @param resourceInstanceAttribute
	 *            * @param resourceInstanceId * @param index * @param inTransaction
	 * @return
	 **/
	public Either<ComponentInstanceProperty, StorageOperationStatus> addAttributeValueToResourceInstance(ComponentInstanceProperty resourceInstanceAttribute, String resourceInstanceId, Integer index, boolean inTransaction);

	/**
	 * Updates Attribute on resource instance
	 * 
	 * @param attribute
	 * @param resourceInstanceId
	 * @param inTransaction
	 * @return
	 */
	public Either<ComponentInstanceProperty, StorageOperationStatus> updateAttributeValueInResourceInstance(ComponentInstanceProperty attribute, String resourceInstanceId, boolean inTransaction);


	public Either<ComponentInstanceInput, StorageOperationStatus> addInputValueToResourceInstance(ComponentInstanceInput input, String resourceInstanceId, Integer innerElement, boolean b);

	public Either<ComponentInstanceInput, StorageOperationStatus> updateInputValueInResourceInstance(ComponentInstanceInput input, String resourceInstanceId, boolean b);


	public StorageOperationStatus updateCustomizationUUID(String componentInstanceId);
	/**
	 * updates componentInstance modificationTime on graph node
	 * @param componentInstance
	 * @param componentInstanceType
	 * @param modificationTime
	 * @param inTransaction
	 * @return
	 */
	public Either<ComponentInstanceData, StorageOperationStatus> updateComponentInstanceModificationTimeAndCustomizationUuidOnGraph(ComponentInstance componentInstance, NodeTypeEnum componentInstanceType, Long modificationTime, boolean inTransaction);

}
