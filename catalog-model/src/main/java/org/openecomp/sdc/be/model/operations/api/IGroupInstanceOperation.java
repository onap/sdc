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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;

import org.openecomp.sdc.be.model.ComponentInstance;

import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.exception.ResponseFormat;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

public interface IGroupInstanceOperation {
	

	public Either<List<GroupInstance>, StorageOperationStatus> getAllGroupInstances(String componentInstId, NodeTypeEnum compInstNodeType);

	public Either<Integer, StorageOperationStatus> increaseAndGetGroupInstancePropertyCounter(String groupInstanceId);

	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToGroupInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, Integer index, boolean inTransaction);

	public Either<ComponentInstanceProperty, StorageOperationStatus> updatePropertyValueInGroupInstance(ComponentInstanceProperty gropuInstanceProperty, String groupInstanceId, boolean inTransaction);

	StorageOperationStatus dissociateAndAssociateGroupsInstanceFromArtifact(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact);

}
