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

package org.openecomp.sdc.be.model.jsontitan.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("groups-operation")
public class GroupsOperation extends BaseOperation {

	private static Logger logger = LoggerFactory.getLogger(GroupsOperation.class.getName());

	public Either<List<GroupDefinition>, StorageOperationStatus> createGroups(Component component, User user, ComponentTypeEnum componentType, Map<String, GroupDataDefinition> groups) {

		Either<List<GroupDefinition>, StorageOperationStatus> result = null;
		Either<GraphVertex, TitanOperationStatus> getComponentVertex = null;
		StorageOperationStatus status = null;

		if (result == null) {
			getComponentVertex = titanDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
			if (getComponentVertex.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
			}
		}
		if (result == null) {
			status = topologyTemplateOperation.associateGroupsToComponent(getComponentVertex.left().value(), groups);
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
			}
		}
	/*	if (result == null) {
			status = topologyTemplateOperation.associateGroupsPropertiesToComponent(getComponentVertex.left().value(), groupsProperties);
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
			}
		}*/
		if (result == null) {
			result = Either.left(ModelConverter.convertToGroupDefinitions(groups));
		}
		return result;
	}
	
	public Either<List<GroupDefinition>, StorageOperationStatus> addGroups(Component component, User user, ComponentTypeEnum componentType, List<GroupDataDefinition> groups) {
		// TODO Auto-generated method stub
		Either<List<GroupDefinition>, StorageOperationStatus> result = null;
		Either<GraphVertex, TitanOperationStatus> getComponentVertex = null;
		StorageOperationStatus status = null;

		if (result == null) {
			getComponentVertex = titanDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
			if (getComponentVertex.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
			}
		} 
		if (result == null) {
			status = addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, groups, JsonPresentationFields.NAME);
			
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
			}
		}
		
		if (result == null) {
			Map<String, GroupDataDefinition> mapGroup = groups.stream().collect(Collectors.toMap( x-> x.getName(), x->x));
			result = Either.left(ModelConverter.convertToGroupDefinitions(mapGroup));
		}
		return result;
	}
	
	public Either<List<GroupDefinition>, StorageOperationStatus> deleteGroups(Component component, User user, ComponentTypeEnum componentType, List<GroupDataDefinition> groups) {
		// TODO Auto-generated method stub
		Either<List<GroupDefinition>, StorageOperationStatus> result = null;
		Either<GraphVertex, TitanOperationStatus> getComponentVertex = null;
		StorageOperationStatus status = null;

		if (result == null) {
			getComponentVertex = titanDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
			if (getComponentVertex.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
			}
		} 
		if (result == null) {
			List<String> groupName = groups.stream().map(g -> g.getName()).collect(Collectors.toList());
			status = deleteToscaDataElements(component.getUniqueId(), EdgeLabelEnum.GROUPS, groupName);
						
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
			}
		}
		
		if (result == null) {
			Map<String, GroupDataDefinition> mapGroup = groups.stream().collect(Collectors.toMap( x-> x.getName(), x->x));
			result = Either.left(ModelConverter.convertToGroupDefinitions(mapGroup));
		}
		return result;
	}
	
	public Either<List<GroupDefinition>, StorageOperationStatus> updateGroups(Component component, ComponentTypeEnum componentType, List<GroupDataDefinition> groups) {
		// TODO Auto-generated method stub
		Either<List<GroupDefinition>, StorageOperationStatus> result = null;
		Either<GraphVertex, TitanOperationStatus> getComponentVertex = null;
		StorageOperationStatus status = null;

		if (result == null) {
			getComponentVertex = titanDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
			if (getComponentVertex.isRight()) {
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
			}
		} 
		if (result == null) {
			groups.forEach(gr -> {
				String version = gr.getVersion();
				String newVersion = increaseMajorVersion(version);
				gr.setVersion(newVersion);
				String groupUUID = UniqueIdBuilder.generateUUID();
				gr.setGroupUUID(groupUUID);
			});

			status = updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, groups, JsonPresentationFields.NAME);
						
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
			}
		}
		
		if (result == null) {
			Map<String, GroupDataDefinition> mapGroup = groups.stream().collect(Collectors.toMap( x-> x.getName(), x->x));
			result = Either.left(ModelConverter.convertToGroupDefinitions(mapGroup));
		}
		return result;
	}
	
	
	public Either<List<GroupProperty>, StorageOperationStatus> updateGroupPropertiesOnComponent(String componentId, GroupDefinition group, List<GroupProperty> newGroupProperties) {
		
		Either<List<GroupProperty>,StorageOperationStatus> result = null;
		Either<GraphVertex, TitanOperationStatus> getComponentVertex = null;
		GraphVertex componentVertex = null;
		StorageOperationStatus status = null;

		if (result == null) {
			getComponentVertex = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata);
			if (getComponentVertex.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch component {}. Status is {} ", componentId);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
			}
		} 
		if (result == null) {
			componentVertex = getComponentVertex.left().value();
			//update 
			List<PropertyDataDefinition> properties = group.getProperties();
			newGroupProperties.forEach(np -> {
				Optional<PropertyDataDefinition> currentProp = properties.stream().filter(p -> p.getName().equals(np.getName())).findAny();
				if (currentProp.isPresent()) {	
					currentProp.get().setValue(np.getValue());
				}
			});
			
			StorageOperationStatus updateDataRes = updateToscaDataOfToscaElement(componentVertex, EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, group, JsonPresentationFields.NAME);
			if ( updateDataRes != StorageOperationStatus.OK ){
				logger.debug("Failed to update properties for group {} error {}", group.getName(), updateDataRes);
				result = Either.right(updateDataRes);
			}
		}
		if (result == null) {
			componentVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
			Either<GraphVertex, TitanOperationStatus> updateRes = titanDao.updateVertex(componentVertex);
			if (updateRes.isRight()) {
				CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update the component {}. Status is {} ",  componentId, updateRes.right().value());
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(updateRes.right().value()));
			}
		}
		if (result == null) {
			result = Either.left(newGroupProperties);
		}
		return result;
	}
	
	/**
	 * The version of the group is an integer. In order to support BC, we might get a version in a float format.
	 * 
	 * @param version
	 * @return
	 */
	private String increaseMajorVersion(String version) {

		String[] versionParts = version.split(ToscaElementLifecycleOperation.VERSION_DELIMETER_REGEXP);
		Integer majorVersion = Integer.parseInt(versionParts[0]);

		majorVersion++;

		return String.valueOf(majorVersion);

	}

	public Either<List<GroupInstance>, StorageOperationStatus> updateGroupInstances(Component component, ComponentTypeEnum componentType, String instanceId, List<GroupInstance> updatedGroupInstances) {

		Either<List<GroupInstance>, StorageOperationStatus> result = null;
		StorageOperationStatus status = null;

		Either<GraphVertex, TitanOperationStatus> getComponentVertex = titanDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
		if (getComponentVertex.isRight()) {
			result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
		}
		if (result == null) {
			List<String> pathKeys = new ArrayList<>();
			pathKeys.add(instanceId);
			status = updateToscaDataDeepElementsOfToscaElement(component.getUniqueId(), EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS, updatedGroupInstances, pathKeys,  JsonPresentationFields.NAME);
			if (status != StorageOperationStatus.OK) {
				result = Either.right(status);
			}
		}
		if (result == null) {
			result = Either.left(updatedGroupInstances);
		}
		return result;
	}

	public Either<GroupDefinition, StorageOperationStatus> updateGroup(Component component, GroupDefinition currentGroup) {
		StorageOperationStatus status = updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, currentGroup, JsonPresentationFields.NAME);
		if(status != StorageOperationStatus.OK){
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update group {} of component {}. The status is}. ", currentGroup.getName(), component.getName(), status);
			return Either.right(status);
		}
		return Either.left(currentGroup);
	}

	public StorageOperationStatus deleteGroup(Component component, String currentGroupName) {
		StorageOperationStatus status = deleteToscaDataElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, currentGroupName, JsonPresentationFields.NAME);
		if(status != StorageOperationStatus.OK){
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to delete group {} of component {}. The status is}. ", currentGroupName, component.getName(), status);
		}
		return status;
	}

	public Either<GroupDefinition, StorageOperationStatus> addGroup(Component component, GroupDefinition currentGroup) {
		StorageOperationStatus status = addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, currentGroup, JsonPresentationFields.NAME);
		if(status != StorageOperationStatus.OK){
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to update group {} of component {}. The status is}. ", currentGroup.getName(), component.getName(), status);
			return Either.right(status);
		}
		return Either.left(currentGroup);
	}
	
	public Either<GroupInstance, StorageOperationStatus> updateGroupInstancePropertyValuesOnGraph(String componentId, String instanceId, GroupInstance oldGroupInstance, List<GroupInstanceProperty> newProperties) {

		Either<GraphVertex, TitanOperationStatus> getComponentVertex = titanDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata);
		if (getComponentVertex.isRight()) {
			CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, "Failed to fetch component {}. Status is {} ", componentId);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
		}

		List<PropertyDataDefinition> propertiesOld = oldGroupInstance.getProperties();
		newProperties.forEach(np -> {
			Optional<PropertyDataDefinition> prop = propertiesOld.stream().filter(p -> p.getName().equals(np.getName())).findFirst();
			if (prop.isPresent()) {
				prop.get().setValue(np.getValue());
			}
		});
		GroupInstanceDataDefinition groupInstanceDataDefinition = new GroupInstanceDataDefinition(oldGroupInstance);
		List<String> pathKeys = new ArrayList<>();
		pathKeys.add(instanceId);
		StorageOperationStatus updateDataRes = updateToscaDataDeepElementOfToscaElement(componentId, EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS, groupInstanceDataDefinition, pathKeys, JsonPresentationFields.NAME);
		if (updateDataRes != StorageOperationStatus.OK) {
			logger.debug("Failed to update properties for group instance {} error {}", oldGroupInstance.getName(), updateDataRes);
			return Either.right(updateDataRes);
		}
		return Either.left(oldGroupInstance);
	}

}
