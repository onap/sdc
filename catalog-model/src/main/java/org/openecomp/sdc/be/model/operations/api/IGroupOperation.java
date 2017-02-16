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

import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.GroupData;

import fj.data.Either;

public interface IGroupOperation {

	// add full group to component
	public Either<GroupData, TitanOperationStatus> addGroupToGraph(NodeTypeEnum nodeTypeEnum, String componentId,
			GroupDefinition groupDefinition);

	public Either<GroupDefinition, StorageOperationStatus> addGroup(NodeTypeEnum nodeTypeEnum, String componentId,
			GroupDefinition groupDefinition);

	public Either<GroupDefinition, StorageOperationStatus> addGroup(NodeTypeEnum nodeTypeEnum, String componentId,
			GroupDefinition groupDefinition, boolean inTransaction);

	public Either<List<GroupDefinition>, StorageOperationStatus> addGroups(NodeTypeEnum nodeTypeEnum,
			String componentId, List<GroupDefinition> groups, boolean inTransaction);

	// get group
	public Either<GroupDefinition, TitanOperationStatus> getGroupFromGraph(String uniqueId);

	public Either<GroupDefinition, StorageOperationStatus> getGroup(String uniqueId);

	public Either<GroupDefinition, StorageOperationStatus> getGroup(String uniqueId, boolean inTransaction);

	// get all groups under component
	public Either<List<GroupDefinition>, TitanOperationStatus> getAllGroupsFromGraph(String componentId,
			NodeTypeEnum componentTypeEnum);

	public Either<List<GroupDefinition>, StorageOperationStatus> getAllGroups(String componentId,
			NodeTypeEnum compTypeEnum, boolean inTransaction);

	public Either<List<GroupDefinition>, StorageOperationStatus> getAllGroups(String componentId,
			NodeTypeEnum compTypeEnum);

	// delete all groups under component
	public Either<List<GroupDefinition>, TitanOperationStatus> deleteAllGroupsFromGraph(String componentId,
			NodeTypeEnum compTypeEnum);

	public Either<List<GroupDefinition>, StorageOperationStatus> deleteAllGroups(String componentId,
			NodeTypeEnum compTypeEnum, boolean inTransaction);

	public Either<List<GroupDefinition>, StorageOperationStatus> deleteAllGroups(String componentId,
			NodeTypeEnum compTypeEnum);

	// Association
	public Either<List<String>, StorageOperationStatus> getAssociatedGroupsToComponentInstance(
			String componentInstanceId, boolean inTransaction);

	public Either<List<String>, StorageOperationStatus> getAssociatedGroupsToComponentInstance(
			String componentInstanceId);

	public Either<List<String>, TitanOperationStatus> getAssociatedGroupsToComponentInstanceFromGraph(
			String componentInstanceId);

	public StorageOperationStatus associateGroupsToComponentInstance(List<String> groups, String componentInstanceId,
			String compInstName, boolean inTransaction);

	public StorageOperationStatus associateGroupsToComponentInstance(List<String> groups, String componentInstanceId,
			String compInstName);

	public Either<List<GraphRelation>, TitanOperationStatus> associateGroupsToComponentInstanceOnGraph(
			List<String> groups, String componentInstanceId, String compInstName);

	public Either<List<GraphRelation>, TitanOperationStatus> dissociateAllGroupsFromArtifactOnGraph(String componentId,
			NodeTypeEnum componentTypeEnum, String artifactId);

	public StorageOperationStatus dissociateAllGroupsFromArtifact(String componentId, NodeTypeEnum componentTypeEnum,
			String artifactId, boolean inTransaction);

	public StorageOperationStatus dissociateAllGroupsFromArtifact(String componentId, NodeTypeEnum componentTypeEnum,
			String artifactId);

	public TitanOperationStatus dissociateAndAssociateGroupsFromArtifactOnGraph(String componentId,
			NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact);

	public StorageOperationStatus dissociateAndAssociateGroupsFromArtifact(String componentId,
			NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact, boolean inTransaction);

	public StorageOperationStatus dissociateAndAssociateGroupsFromArtifact(String componentId,
			NodeTypeEnum componentTypeEnum, String oldArtifactId, ArtifactData newArtifact);

	public boolean isGroupExist(String groupName, boolean inTransaction);
}
