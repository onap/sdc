package org.openecomp.sdc.be.datamodel.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiComponentMetadata;
import org.openecomp.sdc.be.ui.model.UiResourceDataTransfer;
import org.openecomp.sdc.be.ui.model.UiResourceMetadata;
import org.openecomp.sdc.be.ui.model.UiServiceDataTransfer;
import org.openecomp.sdc.be.ui.model.UiServiceMetadata;

public class UiComponentDataConverter {
	
	public static void setUiTranferDataByFieldName(UiComponentDataTransfer dataTransfer, Component component, String fieldName) {
				
			switch (ComponentFieldsEnum.findByValue(fieldName)) {	

				case INPUTS:
					if(component.getInputs() == null){
						dataTransfer.setInputs(new ArrayList<>());
					} else {
						dataTransfer.setInputs(component.getInputs());
					}
					break;
					
				case COMPONENT_INSTANCE_RELATION:
					if(component.getComponentInstancesRelations() == null){
						dataTransfer.setComponentInstancesRelations(new ArrayList<>());
					} else {
						dataTransfer.setComponentInstancesRelations(component.getComponentInstancesRelations());
					}

					break;
					
				case GROUPS:
					if(component.getGroups() == null){
						dataTransfer.setGroups(new ArrayList<>());
					} else {
						dataTransfer.setGroups(component.getGroups());
					}	
					break;
					
				case COMPONENT_INSTANCES:
					if(component.getComponentInstances() == null) {
						dataTransfer.setComponentInstances(new ArrayList<>());
					} else {
						dataTransfer.setComponentInstances(component.getComponentInstances());
					}	
					break;
					
				case COMPONENT_INSTANCES_PROPERTIES:
					if(component.getComponentInstancesProperties() == null) {
						dataTransfer.setComponentInstancesProperties(new HashMap<>());
					} else {
						dataTransfer.setComponentInstancesProperties(component.getComponentInstancesProperties());
					}	
					break;
					
				case CAPABILITIES:
					if(component.getCapabilities() == null) {
						dataTransfer.setCapabilities(new HashMap<>());
					} else {
						dataTransfer.setCapabilities(component.getCapabilities());
					}						
					break;
					
				case REQUIREMENTS:
					if(component.getRequirements() == null) {
						dataTransfer.setRequirements(new HashMap<>());
					} else {
						dataTransfer.setRequirements(component.getRequirements());
					}			
					break;
					
				case DEPLOYMENT_ARTIFACTS:
					if(component.getDeploymentArtifacts() == null) {
						dataTransfer.setDeploymentArtifacts(new HashMap<>());
					} else {
						dataTransfer.setDeploymentArtifacts(component.getDeploymentArtifacts());
					}			
					break;
					
				case TOSCA_ARTIFACTS:
					if(component.getToscaArtifacts() == null) {
						dataTransfer.setToscaArtifacts(new HashMap<>());
					} else {
						dataTransfer.setToscaArtifacts(component.getToscaArtifacts());
					}			
					break;
					
				case ARTIFACTS:
					if(component.getArtifacts() == null) {
						dataTransfer.setArtifacts(new HashMap<>());
					} else {
						dataTransfer.setArtifacts(component.getArtifacts());
					}			
					break;
					
				case COMPONENT_INSTANCES_ATTRIBUTES:
					if(component.getComponentInstancesAttributes() == null) {
						dataTransfer.setComponentInstancesAttributes(new HashMap<>());
					} else {
						dataTransfer.setComponentInstancesAttributes(component.getComponentInstancesAttributes());
					}		
					break;
					
				case COMPONENT_INSTANCE_INPUTS:
					if(component.getComponentInstancesInputs() == null) {
						dataTransfer.setComponentInstancesInputs(new HashMap<>());
					} else {
						dataTransfer.setComponentInstancesInputs(component.getComponentInstancesInputs());
					}		

					break;
				
					
				default:
					break;
			}	

	}
	
	
	public static UiComponentDataTransfer getUiDataTransferFromResourceByParams(Resource resource, List<String> paramsToReturn) {
		UiResourceDataTransfer dataTransfer = new UiResourceDataTransfer();
				
		for(String fieldName: paramsToReturn){
			
			switch (ComponentFieldsEnum.findByValue(fieldName)) {	
			
				case PROPERTIES:
					if(resource.getProperties() == null) {
						dataTransfer.setProperties(new ArrayList<>());
					} else {
						dataTransfer.setProperties(resource.getProperties());
					}	
					break;

				case INTERFACES:
					if(resource.getInterfaces() == null) {
						dataTransfer.setInterfaces(new HashMap<>());
					} else {
						dataTransfer.setInterfaces(resource.getInterfaces());
					}
					break;
					
				case DERIVED_FROM:
					if(resource.getDerivedFrom() == null) {
						dataTransfer.setDerivedFrom(new ArrayList<>());
					} else {
						dataTransfer.setDerivedFrom(resource.getDerivedFrom());
					}
					break;

				case ATTRIBUTES:
					if(resource.getAttributes() == null) {
						dataTransfer.setAttributes(new ArrayList<>());
					} else {
						dataTransfer.setAttributes(resource.getAttributes());
					}
					break;
					
				case ADDITIONAL_INFORMATION:
					if(resource.getAdditionalInformation() == null) {
						dataTransfer.setAdditionalInformation(new ArrayList<>());
					} else {
						dataTransfer.setAdditionalInformation(resource.getAdditionalInformation());
					}
					break;
				case METADATA:
					UiResourceMetadata metadata = new UiResourceMetadata(resource.getCategories(),  resource.getDerivedFrom(), (ResourceMetadataDataDefinition) resource.getComponentMetadataDefinition().getMetadataDataDefinition());
					dataTransfer.setMetadata(metadata);
					break;
					
				default:
					setUiTranferDataByFieldName(dataTransfer, resource, fieldName);	
			}
		}
		
		return dataTransfer;
	}

	public static UiComponentDataTransfer getUiDataTransferFromServiceByParams(Service service, List<String> paramsToReturn) {
		UiServiceDataTransfer dataTransfer = new UiServiceDataTransfer();
				
		for(String fieldName: paramsToReturn){
			
			switch (ComponentFieldsEnum.findByValue(fieldName)) {	
			
				case SERVICE_API_ARTIFACTS:
					if(service.getServiceApiArtifacts() == null) {
						dataTransfer.setServiceApiArtifacts(new HashMap<>());
					} else {
						dataTransfer.setServiceApiArtifacts(service.getServiceApiArtifacts());
					}
					
					break;		
					
				case METADATA:
					UiServiceMetadata metadata = new UiServiceMetadata(service.getCategories(),  (ServiceMetadataDataDefinition) service.getComponentMetadataDefinition().getMetadataDataDefinition());
					dataTransfer.setMetadata(metadata);
					break;
				default:
					setUiTranferDataByFieldName(dataTransfer, service, fieldName);
				}
		}
		
		return dataTransfer;
	}

	
	public static UiComponentMetadata convertToUiComponentMetadata(Component component) {
		
		UiComponentMetadata uiComponentMetadata = null;
		switch (component.getComponentType()) {
			case RESOURCE: 
				Resource resource = (Resource)component;
				uiComponentMetadata = new UiResourceMetadata(component.getCategories(),  resource.getDerivedFrom(), (ResourceMetadataDataDefinition) resource.getComponentMetadataDefinition().getMetadataDataDefinition());
				break;
			case SERVICE:
				uiComponentMetadata = new UiServiceMetadata(component.getCategories(),  (ServiceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition());
			default:

		}
		return uiComponentMetadata;
	}
}
