package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class CompositionDataDefinition extends ToscaDataDefinition {
	
	public Map<String, ComponentInstanceDataDefinition> getComponentInstances() {
		return (Map<String, ComponentInstanceDataDefinition>) getToscaPresentationValue(JsonPresentationFields.COMPONENT_INSTANCES);
	}
	public void setComponentInstances(Map<String, ComponentInstanceDataDefinition> componentInstances) {
		setToscaPresentationValue(JsonPresentationFields.COMPONENT_INSTANCES, componentInstances);
	}
	public Map<String, RelationshipInstDataDefinition> getRelations() {
		return (Map<String, RelationshipInstDataDefinition>) getToscaPresentationValue(JsonPresentationFields.RELATIONS);
	}
	public void setRelations(Map<String, RelationshipInstDataDefinition> relations) {
		setToscaPresentationValue(JsonPresentationFields.RELATIONS, relations);
	}
	public void addInstance( String key, ComponentInstanceDataDefinition instance ){
		Map<String, ComponentInstanceDataDefinition> instances = getComponentInstances();
		if (instances == null ){
			instances = new HashMap<>();
			setToscaPresentationValue(JsonPresentationFields.COMPONENT_INSTANCES, instances );
		}
		instances.put(key, instance);
	}
	
	public void addRelation( String key, RelationshipInstDataDefinition relation ){
		Map<String, RelationshipInstDataDefinition> relations = getRelations();
		if (relations == null ){
			relations = new HashMap<>();
			setToscaPresentationValue(JsonPresentationFields.RELATIONS, relations );
		}
		relations.put(key, relation);
	}
}
