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

package org.openecomp.sdc.be.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

import static java.util.stream.Collectors.groupingBy;

public class Resource extends Component implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6811540567661368482L;
	public static final String ROOT_RESOURCE = "tosca.nodes.Root";

	public Resource() {
		super(new ResourceMetadataDefinition());
		this.getComponentMetadataDefinition().getMetadataDataDefinition().setComponentType(ComponentTypeEnum.RESOURCE);
	}

	public Resource(ComponentMetadataDefinition componentMetadataDefinition) {
		super(componentMetadataDefinition);
		this.getComponentMetadataDefinition().getMetadataDataDefinition().setComponentType(ComponentTypeEnum.RESOURCE);
	}

	private List<String> derivedFrom;

	private List<String> derivedList;

	private List<PropertyDefinition> properties;

	private List<PropertyDefinition> attributes;

	// Later
	private Map<String, InterfaceDefinition> interfaces;

	private List<String> defaultCapabilities;

//	private List<AdditionalInformationDefinition> additionalInformation;

	/**
	 * Please note that more than one "derivedFrom" resource is not currently
	 * supported by the app. The first list element is always addressed.
	 * 
	 * @return
	 */
	public List<String> getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(List<String> derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

	/**
	 * The derivedList is a chain of derivedFrom. e.g. if resource C is derived
	 * from resource B that is derived from resource A - then A, B is the
	 * "DerivedList" of resource C
	 * 
	 * @return
	 */
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

	public List<PropertyDefinition> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<PropertyDefinition> attributes) {
		this.attributes = attributes;
	}

	public Map<String, InterfaceDefinition> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Map<String, InterfaceDefinition> interfaces) {
		this.interfaces = interfaces;
	}

	public Boolean isAbstract() {
		return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.isAbstract();
	}

	public void setAbstract(Boolean isAbstract) {
		((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.setAbstract(isAbstract);
	}

	public List<String> getDefaultCapabilities() {
		return defaultCapabilities;
	}

	public void setDefaultCapabilities(List<String> defaultCapabilities) {
		this.defaultCapabilities = defaultCapabilities;
	}

	public String getCost() {
		return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.getCost();
	}

	public void setCost(String cost) {
		((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition()).setCost(cost);
		;
	}

	public String getLicenseType() {
		return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.getLicenseType();
	}

	public void setLicenseType(String licenseType) {
		((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.setLicenseType(licenseType);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + super.hashCode();

		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		// result = prime * result + ((capabilities == null) ? 0 :
		// capabilities.hashCode());
		result = prime * result + ((defaultCapabilities == null) ? 0 : defaultCapabilities.hashCode());
		result = prime * result + ((derivedFrom == null) ? 0 : derivedFrom.hashCode());
		result = prime * result + ((interfaces == null) ? 0 : interfaces.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((derivedList == null) ? 0 : derivedList.hashCode());
		// result = prime * result + ((requirements == null) ? 0 :
		// requirements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Resource other = (Resource) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (defaultCapabilities == null) {
			if (other.defaultCapabilities != null)
				return false;
		} else if (!defaultCapabilities.equals(other.defaultCapabilities))
			return false;
		if (derivedFrom == null) {
			if (other.derivedFrom != null)
				return false;
		} else if (!derivedFrom.equals(other.derivedFrom))
			return false;
		if (derivedList == null) {
			if (other.derivedList != null)
				return false;
		} else if (!derivedList.equals(other.derivedList))
			return false;
		if (interfaces == null) {
			if (other.interfaces != null)
				return false;
		} else if (!interfaces.equals(other.interfaces))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;

		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "Resource [derivedFrom=" + derivedFrom + ", properties=" + properties + ", attributes=" + attributes
				+ ", interfaces=" + interfaces
				// + ", capabilities=" + capabilities + ", requirements=" +
				// requirements
				+ ", defaultCapabilities=" + defaultCapabilities + ", additionalInformation=" + additionalInformation
				+ "Metadata [" + getComponentMetadataDefinition().getMetadataDataDefinition().toString() + "]";
	}

	public String getToscaResourceName() {
		return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.getToscaResourceName();
	}

	public void setToscaResourceName(String toscaResourceName) {
		((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.setToscaResourceName(toscaResourceName);
	}

	public ResourceTypeEnum getResourceType() {
		return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.getResourceType();
	}

	public void setResourceType(ResourceTypeEnum resourceType) {
		((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.setResourceType(resourceType);
	}

	public void setVendorName(String vendorName) {
		((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.setVendorName(vendorName);
	}

	public void setVendorRelease(String vendorRelease) {
		((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.setVendorRelease(vendorRelease);
	}
	
	public void setResourceVendorModelNumber(String resourceVendorModelNumber){
		((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition()).
		setResourceVendorModelNumber(resourceVendorModelNumber);
	}

	public String getVendorName() {
		return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.getVendorName();
	}

	public String getVendorRelease() {
		return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.getVendorRelease();
	}
	
	public String getResourceVendorModelNumber(){
		return ((ResourceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition())
				.getResourceVendorModelNumber();
	}
	
	@Override
	public String fetchGenericTypeToscaNameFromConfig(){
		String result = super.fetchGenericTypeToscaNameFromConfig();
		if(null == result)
			result = ConfigurationManager.getConfigurationManager().getConfiguration().getGenericAssetNodeTypes().get(ResourceTypeEnum.VFC.getValue());
		return result;
	}
	
	@Override
	public String assetType(){
		return this.getResourceType().name();
	}
	
	@Override
	public boolean shouldGenerateInputs(){
		//TODO add complex VFC condition when supported
		return ResourceTypeEnum.VF == this.getResourceType() || ResourceTypeEnum.CVFC == this.getResourceType() || ResourceTypeEnum.PNF == this.getResourceType();
	}
	
	@Override
	public boolean deriveFromGeneric(){	
		return this.shouldGenerateInputs() || (derivedFrom != null && derivedFrom.contains(fetchGenericTypeToscaNameFromConfig()));
	}

	public Map<String, List<RequirementCapabilityRelDef>> groupRelationsByInstanceName(Resource resource) {
		Map<String, List<RequirementCapabilityRelDef>> relationsByInstanceId = MapUtil.groupListBy(resource.getComponentInstancesRelations(), RequirementCapabilityRelDef::getFromNode);
		return MapUtil.convertMapKeys(relationsByInstanceId, (instId) -> getInstanceNameFromInstanceId(resource, instId));
	}

	private String getInstanceNameFromInstanceId(Resource resource, String instId) {
		return resource.getComponentInstanceById(instId).get().getName();
	}


}
