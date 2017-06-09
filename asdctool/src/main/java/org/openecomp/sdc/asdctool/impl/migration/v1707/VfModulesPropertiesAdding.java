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
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
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

	private static Logger LOGGER = LoggerFactory.getLogger(ToscaTemplateRegeneration.class);
	
	@Autowired
    private ToscaOperationFacade toscaOperationFacade;
	
	@Autowired
    private TopologyTemplateOperation topologyTemplateOperation;
	
	@Resource(name ="group-type-operation-mig")
    private GroupTypeOperation groupTypeOperation;
	
	@Resource(name = "property-operation-mig")
    private PropertyOperation propertyOperation;
	
	
	public boolean migrate(String groupsTypeYmlFilePath) {
		boolean result = true;
		Either<Map<org.openecomp.sdc.be.model.Component, GraphVertex>, StorageOperationStatus> getAllComponentsRes = null;
		GroupTypeDefinition vfModule;
		Either<List<GraphVertex>, TitanOperationStatus> getAllTopologyTemplatesRes = null;
		List<PropertyDefinition> newProperties = null;

		Either<GroupTypeDefinition, TitanOperationStatus> getGroupTypeVfModuleRes ;
		try{
			getGroupTypeVfModuleRes = groupTypeOperation.getGroupTypeByUid("org.openecomp.groups.VfModule.1.0.grouptype");
			
			if(getGroupTypeVfModuleRes.isRight()){
				 result = false;
			}
			if(result){
				vfModule = getGroupTypeVfModuleRes.left().value();
				newProperties = getNewVfModuleTypeProperties(getAllVfModuleTypePropertiesFromYaml(groupsTypeYmlFilePath), vfModule);
				result = addNewPropertiesToGroupType(vfModule, newProperties);
			}
			if(result && CollectionUtils.isNotEmpty(newProperties)){
				Map<GraphPropertyEnum, Object> propsHasNot = new EnumMap<>(GraphPropertyEnum.class);
				propsHasNot.put(GraphPropertyEnum.IS_DELETED, true);
				getAllTopologyTemplatesRes = toscaOperationFacade.getTitanDao().getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, null, propsHasNot, JsonParseFlagEnum.ParseAll);
				if (getAllTopologyTemplatesRes.isRight() && getAllTopologyTemplatesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
					LOGGER.debug("Failed to fetch all non marked topology templates , propsHasNot {}, error {}", propsHasNot, getAllTopologyTemplatesRes.right().value());
					result = false;
				}
			}
			if(result && getAllTopologyTemplatesRes!=null && getAllTopologyTemplatesRes.isLeft()){
				getAllComponentsRes = getAllContainerComponents(getAllTopologyTemplatesRes.left().value());
				if(getAllComponentsRes.isRight()){
					result = false;
				}
			}
			if(result && getAllComponentsRes != null){
				result = addNewVfModulesProperties(getAllComponentsRes.left().value(), newProperties);
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

	private boolean addNewVfModulesProperties(Map<org.openecomp.sdc.be.model.Component, GraphVertex> components, List<PropertyDefinition> newGroupTypeProperties) {
		boolean result = true;
		for(Map.Entry<org.openecomp.sdc.be.model.Component, GraphVertex> component : components.entrySet()){
			result = addNewPropertiesToVfModules(component, newGroupTypeProperties);
			if(!result){
				break;
			}
		}
		return result;
	}

	private boolean addNewPropertiesToVfModules(Entry<org.openecomp.sdc.be.model.Component, GraphVertex> component, List<PropertyDefinition> newGroupTypeProperties) {
		boolean result = true;
		List<GroupDefinition> vfModules = null;
		if(CollectionUtils.isNotEmpty(component.getKey().getGroups())){
			vfModules = component.getKey().getGroups().stream().filter(g -> g.getType().equals(BaseOperation.VF_MODULE)).collect(Collectors.toList());
		}
		if(vfModules != null){
			vfModules.forEach(vfModule -> vfModule.getProperties().addAll(newGroupTypeProperties));
			StorageOperationStatus status = topologyTemplateOperation.updateToscaDataOfToscaElement(component.getValue(), EdgeLabelEnum.GROUPS, VertexTypeEnum.GROUPS, vfModules, JsonPresentationFields.NAME);
			if(status!= StorageOperationStatus.OK){
				result = false;
			}
		}
		if(result && CollectionUtils.isNotEmpty(component.getKey().getComponentInstances())){
			result = addPropertiesToVfModuleInstances(component, newGroupTypeProperties);
		}
		return result;
	}

	private boolean addPropertiesToVfModuleInstances(Entry<org.openecomp.sdc.be.model.Component, GraphVertex> component, List<PropertyDefinition> newGroupTypeProperties) {
		boolean result = true;
		List<GroupInstance> vfModuleInstances;
		List<String> pathKeys;
		for(ComponentInstance componentInstance : component.getKey().getComponentInstances()){
			vfModuleInstances = null;
			if(CollectionUtils.isNotEmpty(componentInstance.getGroupInstances())){
				vfModuleInstances = componentInstance.getGroupInstances()
						.stream()
						.filter(gi -> gi.getType().equals(BaseOperation.VF_MODULE))
						.collect(Collectors.toList());
			}
			if(vfModuleInstances != null){
				for(GroupInstance vfModuleInstance :vfModuleInstances){
					vfModuleInstance.getProperties().addAll(newGroupTypeProperties);
					pathKeys = new ArrayList<>();
					pathKeys.add(componentInstance.getUniqueId());
					StorageOperationStatus status = topologyTemplateOperation
							.updateToscaDataDeepElementOfToscaElement(component.getValue(), EdgeLabelEnum.INST_GROUPS, VertexTypeEnum.INST_GROUPS, vfModuleInstance, pathKeys, JsonPresentationFields.NAME);
					if(status!= StorageOperationStatus.OK){
						result = false;
						break;
					}
				}
				if(!result){
					break;
				}
			}
		}
		return result;
	}

	private Either<Map<org.openecomp.sdc.be.model.Component, GraphVertex>, StorageOperationStatus> getAllContainerComponents(List<GraphVertex> componentsV) {
		Map<org.openecomp.sdc.be.model.Component, GraphVertex> foundComponents = new HashMap<>();
		Either<Map<org.openecomp.sdc.be.model.Component, GraphVertex>, StorageOperationStatus> result = null;
		for(GraphVertex componentV : componentsV){
			Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentRes = toscaOperationFacade.getToscaElement(componentV);
			if(getComponentRes.isRight()){
				result = Either.right(getComponentRes.right().value());
				break;
			}
			foundComponents.put(getComponentRes.left().value(), componentV);
		}
		if(result == null){
			result = Either.left(foundComponents);
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
