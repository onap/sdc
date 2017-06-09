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

import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.resources.data.GroupTypeData;

import fj.data.Either;

public interface IGroupTypeOperation {

	/**
	 * @param groupTypeDefinition
	 * @return
	 */
	public Either<GroupTypeDefinition, StorageOperationStatus> addGroupType(GroupTypeDefinition groupTypeDefinition);

	public Either<GroupTypeDefinition, StorageOperationStatus> addGroupType(GroupTypeDefinition groupTypeDefinition, boolean inTransaction);

	/**
	 * @param uniqueId
	 * @return
	 */
	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupType(String uniqueId);

	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupType(String uniqueId, boolean inTransaction);

	public Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeByType(String name);

	public Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeByType(String name, boolean inTransaction);

	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByTypeAndVersion(String name, String version);

	public Either<GroupTypeDefinition, StorageOperationStatus> getGroupTypeByTypeAndVersion(String name, String version, boolean inTransaction);

	public Either<GroupTypeData, TitanOperationStatus> getLatestGroupTypeByNameFromGraph(String name);

}
