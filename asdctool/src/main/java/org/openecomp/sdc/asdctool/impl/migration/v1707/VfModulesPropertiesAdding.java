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

package org.openecomp.sdc.asdctool.impl.migration.v1707;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.asdctool.impl.migration.v1702.DataTypesUpdate;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.BaseOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.resources.data.PropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("vfModulesPropertiesAdding")
public class VfModulesPropertiesAdding {

	private static Logger LOGGER = LoggerFactory.getLogger(VfModulesPropertiesAdding.class);
	
	@Autowired
    private ToscaOperationFacade toscaOperationFacade;
	
	@Autowired
    private TopologyTemplateOperation topologyTemplateOperation;
	
	@Resource(name ="group-type-operation-mig")
    private GroupTypeOperation groupTypeOperation;
	
	@Resource(name = "property-operation-mig")
    private PropertyOperation propertyOperation;
	
	
	public boolean migrate(String groupsTypeYmlFilePath) {
		LOGGER.debug("Going to add new properties to vfModules. ");
		boolean result = true;
		GroupTypeDefinition vfModule;
		Either<List<GraphVertex>, TitanOperationStatus> getAllTopologyTemplatesRes = null;
		String vfModuleUid = "org.openecomp.groups.VfModule.1.0.grouptype";
		Either<GroupTypeDefinition, TitanOperationStatus> getGroupTypeVfModuleRes ;
		List<PropertyDefinition> updatedProperties = null;
		try{
			LOGGER.debug("Going to fetch {}. ", vfModuleUid);
			getGroupTypeVfModuleRes = groupTypeOperation.getGroupTypeByUid(vfModuleUid);
			
			if(getGroupTypeVfModuleRes.isRight() && getGroupTypeVfModuleRes.right().value() != TitanOperationStatus.NOT_FOUND){
				LOGGER.debug("Failed to fetch the group type {}. The status is {}. ", vfModuleUid, getGroupTypeVfModuleRes.right().value());
				result = false;
			}
			if(getGroupTypeVfModuleRes.isRight() && getGroupTypeVfModuleRes.right().value() == TitanOperationStatus.NOT_FOUND){
				LOGGER.debug("The group type with id {} was not found. Skipping adding the new properties. ", vfModuleUid);
				return true;
			}
			if(result){
				LOGGER.debug("Going to add the new properties {} to org.openecomp.groups.VfModule.1.0.grouptype. ");
				vfModule = getGroupTypeVfModuleRes.left().value();
				updatedProperties = getAllVfModuleTypePropertiesFromYaml(groupsTypeYmlFilePath);
				result = addNewPropertiesToGroupType(vfModule, getNewVfModuleTypeProperties(updatedProperties, vfModule));
				if(!result){
					LOGGER.debug("Failed to add the new properties {} to org.openecomp.groups.VfModule.1.0.grouptype. ");
				}
			}
			if(result && CollectionUtils.isNotEmpty(updatedProperties)){
				Map<GraphPropertyEnum, Object> propsHasNot = new EnumMap<>(GraphPropertyEnum.class);
				propsHasNot.put(GraphPropertyEnum.IS_DELETED, true);
				getAllTopologyTemplatesRes = toscaOperationFacade.getTitanDao().getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, null, propsHasNot, JsonParseFlagEnum.ParseAll);
				if (getAllTopologyTemplatesRes.isRight() && getAllTopologyTemplatesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
					LOGGER.debug("Failed to fetch all non marked topology templates , propsHasNot {}, error {}", propsHasNot, getAllTopologyTemplatesRes.right().value());
					result = false;
				}
			}
			if(result && getAllTopologyTemplatesRes!=null && getAllTopologyTemplatesRes.isLeft()){
				result = addNewVfModulesProperties(getAllTopologyTemplatesRes.left().value(), updatedProperties);
			}
		} catch (Exception e){
			result = false;
		}
		finally{
			if(result){
				toscaOperationFacade.commit();
			} else {
				toscaOperationFacade.rollback();
			}
		}
		return result;
	}

	private boolean addNewVfModulesProperties(List<GraphVertex> components, List<PropertyDefinition> updatedProperties) {
		boolean result = true;
		for(GraphVertex component : components){
			LOGGER.debug("Going to add the new properties {} to component {}. ", updatedProperties, component.getUniqueId());
			result = addNewPropertiesToVfModules(component, updatedProperties);
			if(!result){
				LOGGER.debug("Failed to add the new properties {} to component {}. ", updatedProperties, component.getUniqueId());
				break;
			}
			toscaOperationFacade.commit();
		}
		return result;
	}

	private boolean addNewPropertiesToVfModules(GraphVertex componentV, List<PropertyDefinition> updatedProperties) {
		boolean result = true;
		List<GroupDefinition> vfModules = null;
		Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getToscaElementRes = toscaOperationFacade.getToscaElement(componentV);
		if(getToscaElementRes.isRight()){
			LOGGER.debug("Failed to fetch the component {}. ", componentV.getUniqueId());
			result = false;
		}
		else if(CollectionUtils.isNotEmpty(getToscaElementRes.left().value().getGroups())){
			vfModules = getToscaElementRes.left().value().getGroups().stream().filter(g -> g.getType().equals(BaseOperation.VF_MODULE)).collect(Collectors.toList());
		}
		if(vfModules != null){
			vfModules.forEach(vfModule -> addAllNewProperties(vfModule.getProperties(), updatedProperties));
			StorageOperationStatus status = topologyTemplateOperation.updateToscaDataOfToscaElement(componentV, EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, vfModules, JsonPresentationFields.NAME);
			if(status!= StorageOperationStatus.OK){
				LOGGER.debug("Failed to add the new properties {} to groups of component {}. ", updatedProperties, componentV.getUniqueId());
				result = false;
			}
		}
		if(result && CollectionUtils.isNotEmpty(getToscaElementRes.left().value().getComponentInstances())){
			result = addPropertiesToVfModuleInstances(getToscaElementRes.left().value(), componentV, updatedProperties);
		}
		return result;
	}

	private void addAllNewProperties(List<PropertyDataDefinition> vfModuleProperties, List<PropertyDefinition> updatedProperties) {
		Map<String, PropertyDataDefinition> propertiesMap = vfModuleProperties.stream().collect(Collectors.toMap(p->p.getName(), p->p));
		
		for(PropertyDefinition property : updatedProperties){
			if(!propertiesMap.containsKey(property.getName())){
				vfModuleProperties.add(property);
			}
		}
	}

	private boolean addPropertiesToVfModuleInstances(org.openecomp.sdc.be.model.Component component, GraphVertex componentV, List<PropertyDefinition> updatedProperties) {
		boolean result = true;
		List<GroupInstance> vfModuleInstances;
		List<String> pathKeys;
		LOGGER.debug("Going to add the new properties {} to group instances of component {}. ", updatedProperties, componentV.getUniqueId());
		for(ComponentInstance componentInstance : component.getComponentInstances()){
			vfModuleInstances = null;
			if(CollectionUtils.isNotEmpty(componentInstance.getGroupInstances())){
				vfModuleInstances = componentInstance.getGroupInstances()
						.stream()
						.filter(gi -> gi.getType().equals(BaseOperation.VF_MODULE))
						.collect(Collectors.toList());
			}
			if(vfModuleInstances != null){
				for(GroupInstance vfModuleInstance :vfModuleInstances){
					addAllNewProperties(vfModuleInstance.getProperties(),updatedProperties);
					pathKeys = new ArrayList<>();
					pathKeys.add(componentInstance.getUniqueId());
					StorageOperationStatus status = topologyTemplateOperation
							.updateToscaDataDeepElementOfToscaElement(componentV, EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS, vfModuleInstance, pathKeys, JsonPresentationFields.NAME);
					if(status!= StorageOperationStatus.OK){
						result = false;
						LOGGER.debug("Failed to add the new properties {} to group instances of component {}. ", updatedProperties, componentV.getUniqueId());
						break;
					}
				}
				if(!result){
					LOGGER.debug("Failed to add the new properties {} to group instances of component {}. ", updatedProperties, componentV.getUniqueId());
					break;
				}
			}
		}
		return result;
	}
	
	private boolean addNewPropertiesToGroupType(GroupTypeDefinition vfModule, List<PropertyDefinition> newProperties) {
		boolean result = true;
		Either<Map<String, PropertyData>, TitanOperationStatus> addPropertiesRes = propertyOperation
				.addPropertiesToElementType(vfModule.getUniqueId(), NodeTypeEnum.GroupType, newProperties);
		if(addPropertiesRes.isRight()){
			result = false;
		}
		return result;
	}

	private List<PropertyDefinition> getAllVfModuleTypePropertiesFromYaml(String groupsTypeYmlFilePath) {
		List<DataTypeDefinition> groupTypes = DataTypesUpdate.extractDataTypesFromYaml(groupsTypeYmlFilePath);
		DataTypeDefinition vfModule = groupTypes.stream().filter(g -> g.getName().equals(BaseOperation.VF_MODULE)).findFirst().orElse(null);
		return vfModule.getProperties();
	}
	
	private List<PropertyDefinition> getNewVfModuleTypeProperties(List<PropertyDefinition> allVfModuleTypeProperties, GroupTypeDefinition vfModule) {
		Map<String, PropertyDefinition> existingVfModuleTypeProperties = vfModule.getProperties()
				.stream()
				.collect(Collectors.toMap(p -> p.getName(), p -> p));
		
		List<PropertyDefinition> newGroupTypeProperties = new ArrayList<>();
		for(PropertyDefinition property : allVfModuleTypeProperties){
			if(!existingVfModuleTypeProperties.containsKey(property.getName())){
				newGroupTypeProperties.add(property);
			}
		}
		return newGroupTypeProperties;
	}

	public String description() {
		return "vfModulesPropertiesAdding";
	}

}
