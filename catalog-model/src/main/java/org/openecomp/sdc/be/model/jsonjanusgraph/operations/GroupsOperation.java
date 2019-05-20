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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("groups-operation")
public class GroupsOperation extends BaseOperation {

    private static final Logger log = Logger.getLogger(GroupsOperation.class.getName());

	public StorageOperationStatus deleteCalculatedCapabilitiesWithProperties(String componentId, List<GroupDefinition> groupDefinitions) {
		Optional<StorageOperationStatus> error = groupDefinitions.stream().map(g->removeCalculatedCapabilityFromComponent(componentId, g.getUniqueId())).filter(status-> status!=StorageOperationStatus.OK).findFirst();
		if(!error.isPresent()){
			Map<String, MapCapabilityProperty> extractCapabilityPropertiesFromGroups = ModelConverter.extractCapabilityPropertiesFromGroups(groupDefinitions, false);
			error = extractCapabilityPropertiesFromGroups.keySet().stream().map(k->removeCalculatedCapabilityPropertiesFromComponent(componentId, k)).filter(status-> status!=StorageOperationStatus.OK).findFirst();
		}
		if(error.isPresent()){
			return error.get();
		}
		return StorageOperationStatus.OK;
	}

	/**
	 * Adds the map of the calculated capabilities and the map of the calculated capabilities properties the the component on the graph
	 * @param 	componentId
	 * @param 	calculatedCapabilities
	 * @param 	calculatedCapabilitiesProperties
	 * @return	status of the result the operation
	 */
	public StorageOperationStatus addCalculatedCapabilitiesWithProperties(String componentId, Map<String, MapListCapabilityDataDefinition> calculatedCapabilities, Map<String, MapCapabilityProperty> calculatedCapabilitiesProperties) {

		Optional<StorageOperationStatus> error = calculatedCapabilities.entrySet().stream().map(e-> addElementToComponent(componentId, VertexTypeEnum.CALCULATED_CAPABILITIES, EdgeLabelEnum.CALCULATED_CAPABILITIES, e)).filter(status-> status!=StorageOperationStatus.OK).findFirst();
		if(!error.isPresent()){
			error = calculatedCapabilitiesProperties.entrySet().stream().map(e->addCalculatedCapabilityPropertiesToComponent(componentId, e)).filter(status-> status!=StorageOperationStatus.OK).findFirst();
		}
		if(error.isPresent()){
			return error.get();
		}
		return StorageOperationStatus.OK;
	}
	
	public StorageOperationStatus updateCalculatedCapabilitiesWithProperties(String componentId, Map<String, MapListCapabilityDataDefinition> calculatedCapabilities, Map<String, MapCapabilityProperty> calculatedCapabilitiesProperties) {

		Optional<StorageOperationStatus> error = calculatedCapabilities.entrySet().stream().map(e->updateCalculatedCapabilityOfComponent(componentId, e)).filter(status-> status!=StorageOperationStatus.OK).findFirst();
		if(!error.isPresent()){
			error = calculatedCapabilitiesProperties.entrySet().stream().map(e->updateCalculatedCapabilityPropertiesOnComponent(componentId, e)).filter(status-> status!=StorageOperationStatus.OK).findFirst();
		}
		if(error.isPresent()){
			return error.get();
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus updateCalculatedCapabilityOfComponent(String componentId, Entry<String, MapListCapabilityDataDefinition> capabilities){
		if(MapUtils.isNotEmpty(capabilities.getValue().getMapToscaDataDefinition()))
			return updateToscaDataDeepElementsBlockToToscaElement(componentId, EdgeLabelEnum.CALCULATED_CAPABILITIES, capabilities.getValue(), capabilities.getKey());
		return StorageOperationStatus.OK;
	}
	
	private StorageOperationStatus addCalculatedCapabilityPropertiesToComponent(String componentId, Entry<String, MapCapabilityProperty> properties){
		if(MapUtils.isNotEmpty(properties.getValue().getMapToscaDataDefinition()))
			return addToscaDataDeepElementsBlockToToscaElement(componentId, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, properties.getValue(), properties.getKey());
		return StorageOperationStatus.OK;
	}
	
	private StorageOperationStatus updateCalculatedCapabilityPropertiesOnComponent(String componentId, Entry<String, MapCapabilityProperty> properties){
		if(MapUtils.isNotEmpty(properties.getValue().getMapToscaDataDefinition()))
			return updateToscaDataDeepElementsBlockToToscaElement(componentId, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, properties.getValue(), properties.getKey());
		return StorageOperationStatus.OK;
	}
	
	private StorageOperationStatus removeCalculatedCapabilityFromComponent(String componentId, String groupId){
		return deleteToscaDataDeepElementsBlockOfToscaElement(componentId, EdgeLabelEnum.CALCULATED_CAPABILITIES, VertexTypeEnum.CALCULATED_CAPABILITIES, groupId);
	}
	
	private StorageOperationStatus removeCalculatedCapabilityPropertiesFromComponent(String componentId, String groupId){
		return deleteToscaDataDeepElementsBlockOfToscaElement(componentId, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, VertexTypeEnum.CALCULATED_CAP_PROPERTIES, groupId);
	}


    public Either<List<GroupDefinition>, StorageOperationStatus> createGroups(Component component, Map<String, GroupDataDefinition> groups) {

        Either<List<GroupDefinition>, StorageOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex = null;
        StorageOperationStatus status = null;

		getComponentVertex = janusGraphDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
		if (getComponentVertex.isRight()) {
			result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
		}
        if (result == null) {
            status = topologyTemplateOperation.associateGroupsToComponent(getComponentVertex.left().value(), groups);
            if (status != StorageOperationStatus.OK) {
                result = Either.right(status);
            }
        }
        if (result == null) {
            result = Either.left(ModelConverter.convertToGroupDefinitions(groups));
        }
        return result;
    }

    public <T extends GroupDataDefinition> Either<List<GroupDefinition>, StorageOperationStatus> addGroups(Component component, List<T> groups) {
        Either<List<GroupDefinition>, StorageOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex;
        StorageOperationStatus status;

		getComponentVertex = janusGraphDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
		if (getComponentVertex.isRight()) {
			result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
		}
        if (result == null) {
            status = addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, groups, JsonPresentationFields.NAME);

            if (status != StorageOperationStatus.OK) {
                result = Either.right(status);
            }
        }

        if (result == null) {
			Map<String, GroupDataDefinition> mapGroup = groups.stream().collect(Collectors.toMap(GroupDataDefinition::getName, x->x));
            result = Either.left(ModelConverter.convertToGroupDefinitions(mapGroup));
        }
        return result;
    }

    public Either<List<GroupDefinition>, StorageOperationStatus> deleteGroups(Component component, List<GroupDataDefinition> groups) {
        Either<List<GroupDefinition>, StorageOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex = null;
        StorageOperationStatus status = null;

		getComponentVertex = janusGraphDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
		if (getComponentVertex.isRight()) {
			result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
		}
        if (result == null) {
			List<String> groupName = groups.stream().map(GroupDataDefinition::getName).collect(Collectors.toList());
            status = deleteToscaDataElements(component.getUniqueId(), EdgeLabelEnum.GROUPS, groupName);

            if (status != StorageOperationStatus.OK) {
                result = Either.right(status);
            }
        }

        if (result == null) {
			Map<String, GroupDataDefinition> mapGroup = groups.stream().collect(Collectors.toMap( GroupDataDefinition::getName, x->x));
            result = Either.left(ModelConverter.convertToGroupDefinitions(mapGroup));
        }
        return result;
    }

    public <T extends GroupDataDefinition> Either<List<GroupDefinition>, StorageOperationStatus> updateGroups(Component component, List<T> groups, boolean promoteVersion) {
        Either<List<GroupDefinition>, StorageOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex = null;
        StorageOperationStatus status = null;

		getComponentVertex = janusGraphDao.getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
		if (getComponentVertex.isRight()) {
			result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
		}
        if (result == null) {
            groups.forEach(gr -> {
                updateVersion(promoteVersion, gr);
                String groupUUID = UniqueIdBuilder.generateUUID();
                gr.setGroupUUID(groupUUID);
            });

            status = updateToscaDataOfToscaElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, groups, JsonPresentationFields.NAME);

            if (status != StorageOperationStatus.OK) {
                result = Either.right(status);
            }
        }

        if (result == null) {
			Map<String, GroupDataDefinition> mapGroup = groups.stream().collect(Collectors.toMap( GroupDataDefinition::getName, x->x));
            result = Either.left(ModelConverter.convertToGroupDefinitions(mapGroup));
        }
        return result;
    }

    private <T extends GroupDataDefinition> void updateVersion(boolean promoteVersion, T group) {
        if(promoteVersion) {
            String version = group.getVersion();
            String newVersion = increaseMajorVersion(version);
            group.setVersion(newVersion);
        }
    }

    public void updateGroupOnComponent(String componentId, GroupDefinition groupDefinition) {
        GraphVertex componentVertex = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata)
                .left()
                .on(this::onJanusGraphError);

        StorageOperationStatus updateToscaResult = updateToscaDataOfToscaElement(componentVertex, EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, groupDefinition,
                JsonPresentationFields.NAME);

        if (StorageOperationStatus.OK != updateToscaResult) {
            throw new StorageException(updateToscaResult, groupDefinition.getUniqueId());
        }

        updateLastUpdateDate(componentVertex);
    }

    private void updateLastUpdateDate(GraphVertex componentVertex) {
        componentVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
        janusGraphDao.updateVertex(componentVertex)
                .left()
                .on(this::onJanusGraphError);
    }

    GraphVertex onJanusGraphError(JanusGraphOperationStatus janusGraphOperationStatus) {
        throw new StorageException(
                DaoStatusConverter.convertJanusGraphStatusToStorageStatus(janusGraphOperationStatus));
    }

    public Either<List<GroupProperty>, StorageOperationStatus> updateGroupPropertiesOnComponent(String componentId, GroupDefinition group, List<GroupProperty> newGroupProperties) {

        Either<List<GroupProperty>, StorageOperationStatus> result = null;
        Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex = null;
        GraphVertex componentVertex = null;

		getComponentVertex = janusGraphDao.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata);
		if (getComponentVertex.isRight()) {
			CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch component {}. Status is {} ", componentId);
			result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
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
            if (updateDataRes != StorageOperationStatus.OK) {
                log.debug("Failed to update properties for group {} error {}", group.getName(), updateDataRes);
                result = Either.right(updateDataRes);
            }
        }
        if (result == null) {
            componentVertex.setJsonMetadataField(JsonPresentationFields.LAST_UPDATE_DATE, System.currentTimeMillis());
            Either<GraphVertex, JanusGraphOperationStatus> updateRes = janusGraphDao.updateVertex(componentVertex);
            if (updateRes.isRight()) {
                CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update the component {}. Status is {} ", componentId, updateRes.right().value());
                result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(updateRes.right().value()));
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

        String[] versionParts = version.split(ToscaElementLifecycleOperation.VERSION_DELIMITER_REGEXP);
        Integer majorVersion = Integer.parseInt(versionParts[0]);

        majorVersion++;

        return String.valueOf(majorVersion);

    }

    public Either<List<GroupInstance>, StorageOperationStatus> updateGroupInstances(Component component, String instanceId, List<GroupInstance> updatedGroupInstances) {

        Either<List<GroupInstance>, StorageOperationStatus> result = null;
        StorageOperationStatus status = null;

        Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex = janusGraphDao
            .getVertexById(component.getUniqueId(), JsonParseFlagEnum.NoParse);
        if (getComponentVertex.isRight()) {
            result = Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
        }
        if (result == null) {
            List<String> pathKeys = new ArrayList<>();
            pathKeys.add(instanceId);
            status = updateToscaDataDeepElementsOfToscaElement(component.getUniqueId(), EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS, updatedGroupInstances, pathKeys, JsonPresentationFields.NAME);
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
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update group {} of component {}. The status is}. ", currentGroup.getName(), component.getName(), status);
            return Either.right(status);
        }
        return Either.left(currentGroup);
    }

    public StorageOperationStatus deleteGroup(Component component, String currentGroupName) {
        StorageOperationStatus status = deleteToscaDataElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, currentGroupName, JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to delete group {} of component {}. The status is}. ", currentGroupName, component.getName(), status);
        }
        return status;
    }

    public Either<GroupDefinition, StorageOperationStatus> addGroup(Component component, GroupDefinition currentGroup) {
        StorageOperationStatus status = addToscaDataToToscaElement(component.getUniqueId(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, currentGroup, JsonPresentationFields.NAME);
        if (status != StorageOperationStatus.OK) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to update group {} of component {}. The status is}. ", currentGroup.getName(), component.getName(), status);
            return Either.right(status);
        }
        return Either.left(currentGroup);
    }

    public Either<GroupInstance, StorageOperationStatus> updateGroupInstancePropertyValuesOnGraph(String componentId, String instanceId, GroupInstance oldGroupInstance, List<GroupInstanceProperty> newProperties) {

        Either<GraphVertex, JanusGraphOperationStatus> getComponentVertex = janusGraphDao
            .getVertexById(componentId, JsonParseFlagEnum.ParseMetadata);
        if (getComponentVertex.isRight()) {
            CommonUtility.addRecordToLog(log, LogLevelEnum.DEBUG, "Failed to fetch component {}. Status is {} ", componentId);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(getComponentVertex.right().value()));
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
            log.debug("Failed to update properties for group instance {} error {}", oldGroupInstance.getName(), updateDataRes);
            return Either.right(updateDataRes);
        }
        return Either.left(oldGroupInstance);
    }
}
