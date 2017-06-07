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
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.RequirementImplDef;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public interface IRequirementOperation {

	/**
	 * add a requirement to resource
	 * 
	 * @param reqName
	 * @param reqDefinition
	 * @param nodeType
	 * @param uniqueId
	 * @return
	 */
	public Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource(String reqName, RequirementDefinition reqDefinition, String resourceId);

	public Either<RequirementDefinition, StorageOperationStatus> addRequirementToResource(String reqName, RequirementDefinition reqDefinition, String resourceId, boolean inTransaction);

	public Either<RequirementDefinition, StorageOperationStatus> addRequirementImplToResource(String reqName, RequirementImplDef reqDefinition, String resourceId, String parentReqUniqueId);

	public Either<RequirementDefinition, StorageOperationStatus> addRequirementImplToResource(String reqName, RequirementImplDef reqDefinition, String resourceId, String parentReqUniqueId, boolean inTransaction);

	/**
	 * get requirement of resource
	 * 
	 * @param reqName
	 * @param resourceId
	 * @return
	 */
	public Either<RequirementDefinition, StorageOperationStatus> getRequirementOfResource(String reqName, String resourceId);

	public Either<RequirementDefinition, StorageOperationStatus> getRequirementOfResource(String reqName, String resourceId, boolean inTransaction);

	public Either<Map<String, RequirementDefinition>, StorageOperationStatus> getAllResourceRequirements(String resourceId, boolean inTransaction);

	Either<Map<String, List<RequirementDefinition>>, StorageOperationStatus> getAllRequirementsOfResourceOnly(String resourceId, boolean inTransaction);

	public Either<Map<String, RequirementDefinition>, TitanOperationStatus> getResourceRequirements(String resourceId);

	public Either<Map<String, RequirementDefinition>, StorageOperationStatus> deleteAllRequirements(String resourceId, boolean inTransaction);

	public Either<RequirementDefinition, TitanOperationStatus> getRequirement(String uniqueId);

	StorageOperationStatus addRequirementToResource(TitanVertex metadataVertex, String reqName, RequirementDefinition reqDefinition, String resourceId, boolean inTransaction);
}
