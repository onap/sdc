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
	
	public Either<GroupInstance, StorageOperationStatus> createGroupInstance(String ComponentInstId,   GroupInstance groupInstance, boolean isCreateLogicalName);
	
	public Either<GroupInstance, StorageOperationStatus> createGroupInstance(TitanVertex ciVertex, String componentInstId,  GroupInstance groupInstance, boolean isCreateLogicalName);
	
	public Either<GroupInstance, StorageOperationStatus> deleteGroupInstanceInstance(NodeTypeEnum containerNodeType, String containerComponentId, String groupInstUid);
	
	public Either<GroupInstance, StorageOperationStatus> updateGroupInstance(String serviceId, NodeTypeEnum nodeType, String resourceInstanceName, ComponentInstance resourceInstance);
	
	public Either<List<GroupInstance>, StorageOperationStatus> getAllGroupInstances(String componentInstId, NodeTypeEnum compInstNodeType);
	
	public Either<GroupInstance, TitanOperationStatus> getGroupInstanceById(String groupResourceId);

	public TitanOperationStatus deleteAllGroupInstances(String componentInstId);

	public Either<Integer, StorageOperationStatus> increaseAndGetGroupInstancePropertyCounter(String groupInstanceId);

	public Either<Boolean, StorageOperationStatus> isGroupInstanceNameExist(String parentComponentId, NodeTypeEnum parentNodeType, String compInstId, String componentInstName);

	public Either<ComponentInstance, StorageOperationStatus> getFullGroupInstance(ComponentInstance componentInstance, NodeTypeEnum compInstNodeType);

	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToGroupInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, Integer index, boolean inTransaction);

	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToGroupInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, boolean isvalidate, Integer index, boolean inTransaction);

	public Either<ComponentInstanceProperty, StorageOperationStatus> updatePropertyValueInGroupInstance(ComponentInstanceProperty gropuInstanceProperty, String groupInstanceId, boolean inTransaction);
	
	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> fetchCIEnvArtifacts(String componentInstanceId);

	public StorageOperationStatus updateCustomizationUUID(String componentInstanceId);
	
	public String createGroupInstLogicalName(String instanceNumber, String groupInstanceName);

	public Either<GroupInstance, StorageOperationStatus> associateArtifactsToGroupInstance(String groupId, List<String> artifactsId);

	StorageOperationStatus dissociateAndAssociateGroupsInstanceFromArtifact(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact);

	StorageOperationStatus dissociateAndAssociateGroupsInstanceFromArtifactOnGraph(String componentId, NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact);
	/**
	 * updates group instance property values
	 * @param value
	 * @param newProperties
	 * @return
	 */
	public Either<GroupInstance, StorageOperationStatus> updateGroupInstancePropertyValues(GroupInstance value, List<GroupInstanceProperty> newProperties);
	/**
	 * updates group instance property values
	 * @param oldGroupInstance
	 * @param newProperties
	 * @param inTransaction
	 * @return
	 */
	public Either<GroupInstance, StorageOperationStatus> updateGroupInstancePropertyValues(GroupInstance oldGroupInstance, List<GroupInstanceProperty> newProperties, Boolean inTransaction);
	
}
