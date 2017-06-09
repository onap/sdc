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

package org.openecomp.sdc.be.datatypes.enums;

public enum NodeTypeEnum {
	User("user"), 
	Service("service"), 
	Resource("resource"), 
	Product("product"), 
	ResourceCategory("resourceCategory"), 
	ServiceCategory("serviceCategory"), 
	ServiceNewCategory("serviceNewCategory"), 
	ResourceNewCategory("resourceNewCategory"), 
	ProductCategory("productCategory"), 
	ResourceSubcategory("resourceSubcategory"), 
	ProductSubcategory("productSubcategory"), 
	ProductGrouping("productGrouping"), 
	Tag("tag"), 
	Property("property"), 
	Attribute("attribute"), 
	CapabilityType("capabilityType"), 
	Requirement("requirement"), 
	RelationshipType("relationshipType"), 
	Capability("capability"), 
	RequirementImpl("requirementImpl"), 
	CapabilityInst("capabilityInst"), 
	AttributeValue("attributeValue"), 
	InputValue("inputValue"), 
	PropertyValue("propertyValue"), 
	LockNode("lockNode"), 
	ArtifactRef("artifactRef"), 
	Interface("interface"), 
	InterfaceOperation("interfaceOperation"), 
	ResourceInstance("resourceInstance"), 
	RelationshipInst("relationshipInst"), 
	AdditionalInfoParameters("additionalInfoParameters"), 
	ConsumerCredentials("consumerCredentials"), 
	HeatParameter("heatParameter"), 
	HeatParameterValue("heatParameterValue"), 
	DataType("dataType"), 
	GroupType("groupType"), 
	PolicyType("policyType"), 
	Group("group"), 
	UserFunctionalMenu("userFunctionalMenu"), 
	Input("input"),
	GroupInstance("groupInstance"), ;

	private String name;

	NodeTypeEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static NodeTypeEnum getByName(String name) {
		for (NodeTypeEnum inst : NodeTypeEnum.values()) {
			if (inst.getName().equals(name)) {
				return inst;
			}
		}
		return null;
	}

	public static NodeTypeEnum getByNameIgnoreCase(String name) {
		for (NodeTypeEnum inst : NodeTypeEnum.values()) {
			if (inst.getName().equalsIgnoreCase(name)) {
				return inst;
			}
		}
		return null;
	}
}
