package org.openecomp.sdc.be.tosca;


import fj.data.Either;
import org.openecomp.sdc.be.components.impl.exceptions.SdcResourceNotFoundException;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.tosca.model.IToscaMetadata;
import org.openecomp.sdc.be.tosca.model.ToscaMetadata;
import org.openecomp.sdc.be.tosca.model.ToscaPolicyTemplate;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

@Service
public class PolicyExportParserImpl implements PolicyExportParser {

	private static final Logger log = Logger.getLogger(PolicyExportParserImpl.class);
   
	private ApplicationDataTypeCache dataTypeCache;
	private Map<String, DataTypeDefinition> dataTypes;
	private PropertyConvertor propertyConvertor = PropertyConvertor.getInstance();
	
	@Autowired
	public PolicyExportParserImpl(ApplicationDataTypeCache dataTypeCache) {
		this.dataTypeCache = dataTypeCache;
		this.dataTypes = getDataTypes();
	}
	
	private Map<String, DataTypeDefinition> getDataTypes()  {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypesEither = dataTypeCache.getAll();
		if (dataTypesEither.isRight()) {
			log.error("Failed to retrieve all data types {}", dataTypesEither.right().value()); 
			throw new SdcResourceNotFoundException(); 
		}
		
		return dataTypesEither.left().value();
	}
	
	@Override
	public Map<String, ToscaPolicyTemplate> getPolicies(Component component) {
		Map<String, ToscaPolicyTemplate> toscaPolicies = null;
		Map<String, PolicyDefinition> policies = component.getPolicies();		
		if (isNotEmpty(policies)) {

			 toscaPolicies = policies.values().stream().collect(
					 Collectors.toMap(
							 PolicyDefinition::getName,
							 policy->getToscaPolicyTemplate(policy,component)));
			log.debug("policies converted");
		}	
		return toscaPolicies;	
	}
	
	private ToscaPolicyTemplate getToscaPolicyTemplate(PolicyDefinition policyDefinition,Component component) {
		
		 String type = policyDefinition.getPolicyTypeName();
	     IToscaMetadata metadata = getToscaPolicyTemplateMetadata(policyDefinition);
	     Map<String, Object> properties = getToscaPolicyTemplateProperties(policyDefinition);
	     List<String> targets = getToscaPolicyTemplateTargets(
	    		 policyDefinition,component.getComponentInstances(),component.getGroups());
				
		return new ToscaPolicyTemplate(type, metadata, properties, targets);
	}
	
	private List<String> getToscaPolicyTemplateTargets(PolicyDefinition policyDefinition,
			List<ComponentInstance> componentInstances, List<GroupDefinition> groups) {

		Map<PolicyTargetType, List<String>> targets = policyDefinition.getTargets();
		List<String> targetNames = null;

		if (targets == null || targets.isEmpty()) {
			return null;
		}

		List<String> componentInstancesTargets = targets.get(PolicyTargetType.COMPONENT_INSTANCES);
		List<String> groupTargets = targets.get(PolicyTargetType.GROUPS);
		
		if (isNotEmpty(componentInstancesTargets) && isNotEmpty(componentInstances)) {	
			// get target names by Id from component instances
			Map<String, String> targetNamesByIdFromComponentInstances = 
					getTargetNamesByIdFromComponentInstances(componentInstances);
			targetNames = targetNamesLazyInstantiation(targetNames);
			addTargetNames(componentInstancesTargets, targetNames, targetNamesByIdFromComponentInstances);
			
		}
		
		if (isNotEmpty(groupTargets) && isNotEmpty(groups)) {
			// get target names by id from group definitions
			Map<String, String> targetNamesByIdFromGroupDefinitions = getTargetNamesByIdFromGroupDefinitions(groups);
			targetNames = targetNamesLazyInstantiation(targetNames);
			addTargetNames(groupTargets, targetNames, targetNamesByIdFromGroupDefinitions);
			
		}

		return targetNames;
	}

	private List<String> targetNamesLazyInstantiation(List<String> targetNames) {
		if (targetNames == null) {
			targetNames = new ArrayList<>();
		}
		return targetNames;
	}

	private void addTargetNames(List<String> targets, List<String> targetNames,
			Map<String, String> targetNamesById) {
		
		if (!targetNamesById.isEmpty()) {
			
			for (String id : targets) {
				String name = targetNamesById.get(id);
				if (name != null) {
					targetNames.add(name);
				}
			}
		}
	}

	private Map<String, String> getTargetNamesByIdFromGroupDefinitions(List<GroupDefinition> groups) {	
		return groups.stream().collect(
				Collectors.toMap(GroupDefinition::getUniqueId, GroupDefinition::getName));		
	}

	private Map<String, String> getTargetNamesByIdFromComponentInstances(List<ComponentInstance> componentInstances) {
		return componentInstances.stream().collect(
				Collectors.toMap(ComponentInstance::getUniqueId,ComponentInstance::getName));
	}

	private Map<String, Object> getToscaPolicyTemplateProperties(PolicyDefinition policyDefinition) {
		
		List<PropertyDataDefinition> tempProperties = policyDefinition.getProperties();
		
		if (isEmpty(tempProperties)) {
			return null;
		}
				
		Map<String, Object> props = new HashMap<>();

		tempProperties.forEach(input -> 
			propertyConvertor.convertAndAddValue(dataTypes, props, input, getPropertyValue(input))
		);

		if (props.isEmpty()) {
			return null;
		} else {
			return props;
		}	
	}

	private Supplier<String> getPropertyValue(PropertyDataDefinition propertyDataDefinition) {
		return () -> {
			if (isNotEmpty(propertyDataDefinition.getValue())) {
				return propertyDataDefinition.getValue();
			} else {
				return propertyDataDefinition.getDefaultValue();
			}
		};
	}

	private IToscaMetadata getToscaPolicyTemplateMetadata(PolicyDefinition policyDefinition) {
		IToscaMetadata metadata = new ToscaMetadata();
		metadata.setInvariantUUID(policyDefinition.getInvariantUUID());
		metadata.setUUID(policyDefinition.getPolicyUUID());
		metadata.setName(policyDefinition.getName());
		metadata.setVersion(policyDefinition.getVersion());
		return metadata;
	}

}
