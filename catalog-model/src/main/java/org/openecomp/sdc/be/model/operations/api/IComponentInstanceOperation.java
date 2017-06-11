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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.RequirementAndRelationshipPair;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.AttributeValueData;
import org.openecomp.sdc.be.resources.data.CapabilityData;
import org.openecomp.sdc.be.resources.data.ComponentInstanceData;
import org.openecomp.sdc.be.resources.data.RequirementData;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public interface IComponentInstanceOperation {

	/**
	 * add resource instance to service
	 * 
	 * @param containerComponentId
	 *            - component id
	 * @param instanceNumber
	 *            - instance number of the component instance
	 * @param componentInstance
	 * @param inTransaction
	 * @return
	 */
	public Either<ComponentInstance, StorageOperationStatus> createComponentInstance(String containerComponentId, NodeTypeEnum containerNodeType, String instanceNumber, ComponentInstance componentInstance, NodeTypeEnum instNodeType,
			boolean inTransaction);

	/**
	 * add resource instance to service with internal transaction
	 * 
	 * @param containerComponentId
	 * @param instanceNumber
	 * @param componentInstance
	 * @return
	 */
	public Either<ComponentInstance, StorageOperationStatus> createComponentInstance(String containerComponentId, NodeTypeEnum containerNodeType, String instanceNumber, ComponentInstance componentInstance, NodeTypeEnum instNodeType);

	/**
	 * delete resource instance from component
	 * 
	 * @param containerComponentId
	 *            - containerComponent id
	 * @param resourceInstUid
	 *            - resource instance uid
	 * @param inTransaction
	 * @return
	 */
	public Either<ComponentInstance, StorageOperationStatus> deleteComponentInstance(NodeTypeEnum containerNodeType, String containerComponentId, String resourceInstUid, boolean inTransaction);

	public Either<ComponentInstance, StorageOperationStatus> deleteComponentInstance(NodeTypeEnum containerNodeType, String containerComponentId, String resourceInstUid);

	/**
	 * associate 2 resource instances for a given requirement
	 * 
	 * @param serviceId
	 * @param fromResInstanceUid
	 * @param toResInstanceUid
	 * @param requirement
	 * @param relationship
	 * @param inTransaction
	 * @return
	 */
	// public Either<RequirementCapabilityRelDef, StorageOperationStatus>
	// associateResourceInstances(
	// String serviceId, NodeTypeEnum nodeType, String fromResInstanceUid,
	// String toResInstanceUid, String requirement, String relationship,
	// boolean inTransaction);

	// public Either<RequirementCapabilityRelDef, StorageOperationStatus>
	// associateResourceInstances(
	// String serviceId, NodeTypeEnum nodeType, String fromResInstanceUid,
	// String toResInstanceUid, String requirement, String relationship);

	public Either<RequirementCapabilityRelDef, StorageOperationStatus> associateResourceInstances(String serviceId, NodeTypeEnum nodeType, RequirementCapabilityRelDef relation, boolean inTransaction, boolean isClone);

	public Either<RequirementCapabilityRelDef, StorageOperationStatus> associateResourceInstances(String serviceId, NodeTypeEnum nodeType, RequirementCapabilityRelDef relation);

	/**
	 * 
	 * dissociate the relation between 2 resource instances for a given requirement
	 * 
	 * @param serviceId
	 * @param fromResInstanceUid
	 * @param toResInstanceUid
	 * @param requirement
	 * @param inTransaction
	 * @return
	 */
	public Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances(String serviceId, NodeTypeEnum nodeType, RequirementCapabilityRelDef requirementDef, boolean inTransaction);

	public Either<RequirementCapabilityRelDef, StorageOperationStatus> dissociateResourceInstances(String serviceId, NodeTypeEnum nodeType, RequirementCapabilityRelDef requirementDef);

	/**
	 * update the properties of a given resource instance
	 * 
	 * @param serviceId
	 * @param resourceInstanceName
	 * @param resourceInstance
	 * @param inTransaction
	 * @return
	 */
	public Either<ComponentInstance, StorageOperationStatus> updateResourceInstance(String serviceId, NodeTypeEnum nodeType, String resourceInstanceName, ComponentInstance resourceInstance, boolean inTransaction);

	public Either<ComponentInstance, StorageOperationStatus> updateResourceInstance(String serviceId, NodeTypeEnum nodeType, String resourceInstanceName, ComponentInstance resourceInstance);

	/**
	 * get all resource instances of a given service and the relations between the resource instances
	 * 
	 * @param serviceId
	 * @param inTransaction
	 * @return
	 */
	public Either<ImmutablePair<List<ComponentInstance>, List<RequirementCapabilityRelDef>>, StorageOperationStatus> getAllComponentInstances(String componentId, NodeTypeEnum containerNodeType, NodeTypeEnum compInstNodeType, boolean inTransaction);

	public Either<List<String>, StorageOperationStatus> getAllComponentInstancesNames(String componentId, NodeTypeEnum nodeType, boolean inTransaction);

	public Either<List<String>, StorageOperationStatus> getAllComponentInstancesNames(String componentId, NodeTypeEnum nodeType);
	
	/**
	 * get all component instance properties and values from graph 
	 * @param resourceInstance
	 * @return
	 */
	public Either<List<ComponentInstanceProperty>, StorageOperationStatus> getComponentInstancesPropertiesAndValuesFromGraph(
			ComponentInstance resourceInstance);
	
	/**
	 * get resource instance from id
	 * 
	 * @param resourceId
	 * @return resource instance of given id
	 */
	public Either<ComponentInstance, StorageOperationStatus> getResourceInstanceById(String resourceId);

	public Either<List<ComponentInstance>, StorageOperationStatus> deleteAllComponentInstances(String serviceId, NodeTypeEnum nodeType, boolean inTransaction);

	public Either<List<ComponentInstance>, StorageOperationStatus> deleteAllComponentInstances(String serviceId, NodeTypeEnum nodeType);

	public Either<Integer, StorageOperationStatus> increaseAndGetResourceInstanceSpecificCounter(String resourceInstanceId, GraphPropertiesDictionary counterType, boolean inTransaction);

	public String createComponentInstLogicalName(String instanceNumber, String componentInstanceName);

	public Either<Boolean, StorageOperationStatus> isComponentInstanceNameExist(String parentComponentId, NodeTypeEnum parentNodeType, String compInstId, String componentInstName);

	public Either<Boolean, StorageOperationStatus> validateParent(String parentId, String uniqId, boolean inTransaction);

	public Either<ComponentInstance, StorageOperationStatus> getFullComponentInstance(ComponentInstance componentInstance, NodeTypeEnum compInstNodeType);

	public Either<Boolean, StorageOperationStatus> isAvailableRequirement(ComponentInstance fromResInstance, RequirementAndRelationshipPair relationPair);

	public Either<Boolean, StorageOperationStatus> isAvailableCapabilty(ComponentInstance toResInstance, RequirementAndRelationshipPair relationPair);

	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToResourceInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, Integer index, boolean inTransaction);

	public Either<ComponentInstanceProperty, StorageOperationStatus> addPropertyValueToResourceInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, boolean isvalidate, Integer index, boolean inTransaction);

	/**
	 * Adds Attribute to resource instance
	 * 
	 * @param resourceInstanceAttribute
	 *            * @param resourceInstanceId * @param index * @param inTransaction
	 * @return
	 **/
	public Either<ComponentInstanceProperty, StorageOperationStatus> addAttributeValueToResourceInstance(ComponentInstanceProperty resourceInstanceAttribute, String resourceInstanceId, Integer index, boolean inTransaction);

	public Either<ComponentInstanceProperty, StorageOperationStatus> updatePropertyValueInResourceInstance(ComponentInstanceProperty resourceInstanceProperty, String resourceInstanceId, boolean inTransaction);

	/**
	 * Updates Attribute on resource instance
	 * 
	 * @param attribute
	 * @param resourceInstanceId
	 * @param inTransaction
	 * @return
	 */
	public Either<ComponentInstanceProperty, StorageOperationStatus> updateAttributeValueInResourceInstance(ComponentInstanceProperty attribute, String resourceInstanceId, boolean inTransaction);

	public Either<AttributeValueData, TitanOperationStatus> createOrUpdateAttributeOfResourceInstance(ComponentInstanceProperty attributeInstanceProperty, String resourceInstanceId);

	public Either<ComponentInstanceInput, StorageOperationStatus> addInputValueToResourceInstance(ComponentInstanceInput input, String resourceInstanceId, Integer innerElement, boolean b);

	public Either<ComponentInstanceInput, StorageOperationStatus> updateInputValueInResourceInstance(ComponentInstanceInput input, String resourceInstanceId, boolean b);

	public Either<Map<String, ArtifactDefinition>, StorageOperationStatus> fetchCIEnvArtifacts(String componentInstanceId);

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

	Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> getCapabilities(ComponentInstance compInstance, NodeTypeEnum nodeTypeEnum);

	Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> getRequirements(ComponentInstance compInstance, NodeTypeEnum nodeTypeEnum);

	Either<List<ImmutablePair<CapabilityData, GraphEdge>>, TitanOperationStatus> getFulfilledCapabilities(ComponentInstance compInstance, NodeTypeEnum nodeTypeEnum);

	Either<List<ImmutablePair<RequirementData, GraphEdge>>, TitanOperationStatus> getFulfilledRequirements(ComponentInstance compInstance, NodeTypeEnum nodeTypeEnum);
}
