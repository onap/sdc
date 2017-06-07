package org.openecomp.sdc.be.ui.model;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

public class UiResourceDataTransfer extends UiComponentDataTransfer{
	
	private UiResourceMetadata metadata;

	private List<String> derivedFrom;

	private List<String> derivedList;

	private List<PropertyDefinition> properties;

	private List<AttributeDefinition> attributes;

	private Map<String, InterfaceDefinition> interfaces;

	private List<String> defaultCapabilities;
	
	private List<AdditionalInformationDefinition> additionalInformation;
	
	public List<AdditionalInformationDefinition> getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(List<AdditionalInformationDefinition> additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public UiResourceMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(UiResourceMetadata metadata) {
		this.metadata = metadata;
	}

	public List<String> getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(List<String> derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

	public List<String> getDerivedList() {
		return derivedList;
	}

	public void setDerivedList(List<String> derivedList) {
		this.derivedList = derivedList;
	}

	public List<PropertyDefinition> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyDefinition> properties) {
		this.properties = properties;
	}

	public List<AttributeDefinition> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDefinition> attributes) {
		this.attributes = attributes;
	}

	public Map<String, InterfaceDefinition> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Map<String, InterfaceDefinition> interfaces) {
		this.interfaces = interfaces;
	}

	public List<String> getDefaultCapabilities() {
		return defaultCapabilities;
	}

	public void setDefaultCapabilities(List<String> defaultCapabilities) {
		this.defaultCapabilities = defaultCapabilities;
	}

}
