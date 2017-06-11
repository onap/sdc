package org.openecomp.sdc.be.model.jsontitan.datamodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabiltyProperty;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabiltyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;

public class TopologyTemplate extends ToscaElement{

	public TopologyTemplate() {
		super(ToscaElementTypeEnum.TopologyTemplate);
	}
	private Map<String, PropertyDataDefinition> inputs;
	private Map<String, MapPropertiesDataDefinition> instInputs;
	private Map<String, ? extends ToscaDataDefinition> heatParameters;
	private Map<String, MapPropertiesDataDefinition> instAttributes;
	private Map<String, MapPropertiesDataDefinition> instProperties;
	private Map<String, GroupDataDefinition> groups;
	private Map<String, MapGroupsDataDefinition> instGroups;
	private Map<String, ArtifactDataDefinition> serviceApiArtifacts;
	private Map<String, CompositionDataDefinition> compositions; 

	private Map<String, MapListCapabiltyDataDefinition> calculatedCapabilities;
	private Map<String, MapListRequirementDataDefinition> calculatedRequirements;
	private Map<String, MapListCapabiltyDataDefinition> fullfilledCapabilities;
	private Map<String, MapListRequirementDataDefinition> fullfilledRequirements;
	
	private Map<String, MapCapabiltyProperty> calculatedCapabilitiesProperties;
	
	private Map<String, MapArtifactDataDefinition> instDeploymentArtifacts;
	private Map<String, MapArtifactDataDefinition> instanceArtifacts;

	
	public Map<String, PropertyDataDefinition> getInputs() {
		return inputs;
	}
	public void setInputs(Map<String, PropertyDataDefinition> inputs) {
		this.inputs = inputs;
	}
	public Map<String, MapPropertiesDataDefinition> getInstInputs() {
		return instInputs;
	}
	public void setInstInputs(Map<String, MapPropertiesDataDefinition> instInputs) {
		this.instInputs = instInputs;
	}
	public Map<String, ? extends ToscaDataDefinition> getHeatParameters() {
		return heatParameters;
	}
	public void setHeatParameters(Map<String, ? extends ToscaDataDefinition> heatParameters) {
		this.heatParameters = heatParameters;
	}
	public Map<String, MapPropertiesDataDefinition> getInstAttributes() {
		return instAttributes;
	}
	public void setInstAttributes(Map<String, MapPropertiesDataDefinition> instAttributes) {
		this.instAttributes = instAttributes;
	}
	public Map<String, MapPropertiesDataDefinition> getInstProperties() {
		return instProperties;
	}
	public void setInstProperties(Map<String, MapPropertiesDataDefinition> instProperties) {
		this.instProperties = instProperties;
	}
	public Map<String, GroupDataDefinition> getGroups() {
		return groups;
	}
	public void setGroups(Map<String, GroupDataDefinition> groups) {
		this.groups = groups;
	}
	public Map<String, MapGroupsDataDefinition> getInstGroups() {
		return instGroups;
	}
	public void setInstGroups(Map<String, MapGroupsDataDefinition> instGroups) {
		this.instGroups = instGroups;
	}
	public Map<String, ArtifactDataDefinition> getServiceApiArtifacts() {
		return serviceApiArtifacts;
	}
	public void setServiceApiArtifacts(Map<String, ArtifactDataDefinition> serviceApiArtifacts) {
		this.serviceApiArtifacts = serviceApiArtifacts;
	}
	public Map<String, CompositionDataDefinition> getCompositions() {
		return compositions;
	}
	public void setCompositions(Map<String, CompositionDataDefinition> compositions) {
		this.compositions = compositions;
	}
	public Map<String, MapListCapabiltyDataDefinition> getCalculatedCapabilities() {
		return calculatedCapabilities;
	}
	public void setCalculatedCapabilities(Map<String, MapListCapabiltyDataDefinition> calculatedCapabilities) {
		this.calculatedCapabilities = calculatedCapabilities;
	}
	public Map<String, MapListRequirementDataDefinition> getCalculatedRequirements() {
		return calculatedRequirements;
	}
	public void setCalculatedRequirements(Map<String, MapListRequirementDataDefinition> calculatedRequirements) {
		this.calculatedRequirements = calculatedRequirements;
	}
	public Map<String, MapListCapabiltyDataDefinition> getFullfilledCapabilities() {
		return fullfilledCapabilities;
	}
	public void setFullfilledCapabilities(Map<String, MapListCapabiltyDataDefinition> fullfilledCapabilities) {
		this.fullfilledCapabilities = fullfilledCapabilities;
	}
	public Map<String, MapListRequirementDataDefinition> getFullfilledRequirements() {
		return fullfilledRequirements;
	}
	public void setFullfilledRequirements(Map<String, MapListRequirementDataDefinition> fullfilledRequirements) {
		this.fullfilledRequirements = fullfilledRequirements;
	}
	
	public Map<String, MapArtifactDataDefinition> getInstDeploymentArtifacts() {
		return instDeploymentArtifacts;
	}
	public void setInstDeploymentArtifacts(Map<String, MapArtifactDataDefinition> instDeploymentArtifacts) {
		this.instDeploymentArtifacts = instDeploymentArtifacts;
	}
	
	public Map<String, MapCapabiltyProperty> getCalculatedCapabilitiesProperties() {
		return calculatedCapabilitiesProperties;
	}
	public void setCalculatedCapabilitiesProperties(Map<String, MapCapabiltyProperty> calculatedCapabilitiesProperties) {
		this.calculatedCapabilitiesProperties = calculatedCapabilitiesProperties;
	}
	
	public Map<String, MapArtifactDataDefinition> getInstanceArtifacts() {
		return instanceArtifacts;
	}
	public void setInstanceArtifacts(Map<String, MapArtifactDataDefinition> instanceArtifacts) {
		this.instanceArtifacts = instanceArtifacts;
	}
	/**
	 * Adds component instance to composition of topology template
	 * Note that component instance will be overrided in case if the topology template already contains a component instance with the same name
	 * @param componentInstance
	 */
	public void addComponentInstance(ComponentInstanceDataDefinition componentInstance){
		if(getCompositions() == null){
			compositions = new HashMap<>();
		}
		if(MapUtils.isEmpty(getCompositions())){
			compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), new CompositionDataDefinition());
		}
		if(MapUtils.isEmpty(getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances())){
			getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).setComponentInstances(new HashMap<>());
		}
		getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances().put(componentInstance.getUniqueId(), componentInstance);
	}
	/**
	 * Returns map of component instances from composition
	 * @return
	 */
	public Map<String, ComponentInstanceDataDefinition> getComponentInstances() {
		Map<String, ComponentInstanceDataDefinition> instances = null;
		if(getCompositions() != null && getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()) != null ){
			instances = getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances();
		}
		return instances;
	}
	
	
	/**
	 * Sets map of component instances to composition of topology template
	 * Note that component instances will be overrided in case if the topology template already contains a component instances
	 * @param instances
	 */
	public void setComponentInstances(Map<String, ComponentInstanceDataDefinition> instances) {
		if(getCompositions() == null){
			compositions = new HashMap<>();
		}
		if(MapUtils.isEmpty(getCompositions())){
			compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), new CompositionDataDefinition());
		}
		getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).setComponentInstances(instances);
	}
	public Map<String, RelationshipInstDataDefinition> getRelations() {
		Map<String, RelationshipInstDataDefinition> relations = null;
		if( getCompositions() != null && getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()) != null ){
			relations = getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getRelations();
		}
		return relations;
	}
}
