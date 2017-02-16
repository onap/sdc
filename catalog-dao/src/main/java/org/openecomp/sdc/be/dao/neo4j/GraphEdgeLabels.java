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

package org.openecomp.sdc.be.dao.neo4j;

import java.util.ArrayList;
import java.util.List;

public enum GraphEdgeLabels {

	// field name
	//
	STATE("STATE"), LAST_STATE("LAST_STATE"), CREATOR("CREATOR"), LAST_MODIFIER("LAST_MODIFIER"), ATTRIBUTE(
			"EDGE_ATTRIBUTE"), PROPERTY("EDGE_PROPERTY"), CATEGORY("CATEGORY"), DERIVED_FROM(
					"DERIVED_FROM"), REQUIREMENT("REQUIREMENT"), CAPABILITY_TYPE("CAPABILITY_TYPE"), RELATIONSHIP_TYPE(
							"RELATIONSHIP_TYPE"), CAPABILITY("CAPABILITY"), INSTANCE_OF("INSTANCE_OF"), INTERFACE(
									"INTERFACE"), INTERFACE_OPERATION("INTERFACE_OPERATION"), ARTIFACT_REF(
											"ARTIFACT_REF"), INPUTS("INPUTS"), REQUIREMENT_IMPL(
													"REQUIREMENT_IMPL"), NODE_IMPL("NODE_IMPL"), IMPLEMENTATION_OF(
															"IMPLEMENTATION_OF"), ATTRIBUTE_VALUE(
																	"ATTRIBUTE_VALUE"), INPUT_VALUE(
																			"INPUT_VALUE"), PROPERTY_VALUE(
																					"PROPERTY_VALUE"), CAPABILITY_INST(
																							"CAPABILITY_INST"), TYPE_OF(
																									"TYPE_OF"), RESOURCE_INST(
																											"RESOURCE_INST"), RELATIONSHIP_INST(
																													"RELATIONSHIP_INST"), CAPABILITY_NODE(
																															"CAPABILITY_NODE"), LAST_DISTRIBUTION_STATE_MODIFAIER(
																																	"LAST_DISTRIBUTION_STATE_MODIFAIER"), ATTRIBUTE_IMPL(
																																			"ATTRIBUTE_IMPL"), INPUT_IMPL(
																																					"INPUT_IMPL"), PROPERTY_IMPL(
																																							"PROPERTY_IMPL"), ADDITIONAL_INFORMATION(
																																									"ADDITIONAL_INFORMATION"), HEAT_PARAMETER(
																																											"HEAT_PARAMETER"), SUB_CATEGORY(
																																													"SUB_CATEGORY"), GROUPING(
																																															"GROUPING"), CATEGORIZED_TO(
																																																	"CATEGORIZED_TO"), GENERATED_FROM(
																																																			"GENERATED_FROM"), PARAMETER_VALUE(
																																																					"PARAMETER_VALUE"), PARAMETER_IMPL(
																																																							"PARAMETER_IMPL"),
	// VF additions
	CALCULATED_REQUIREMENT("CALCULATED_REQUIREMENT"), CALCULATED_CAPABILITY(
			"CALCULATED_CAPABILITY"), RELATIONSHIP_ORIGIN("RELATIONSHIP_ORIGIN"), CAPABILITY_ORIGIN(
					"CAPABILITY_ORIGIN"), CALCULATED_REQUIREMENT_FULLFILLED(
							"CALCULATED_REQUIREMENT_FULLFILLED"), CALCULATED_CAPABILITY_FULLFILLED(
									"CALCULATED_CAPABILITY_FULLFILLED"),
	// Group
	GROUP("GROUP"), GROUP_ARTIFACT_REF("GROUP_ARTIFACT_REF"), GROUP_MEMBER("GROUP_MEMBER"), INPUT(
			"EDGE_INPUT"), GET_INPUT("GET_INPUT"),;

	private String property;

	GraphEdgeLabels(String property) {
		this.property = property;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public static List<String> getAllProperties() {

		List<String> arrayList = new ArrayList<String>();

		for (GraphEdgeLabels graphProperty : GraphEdgeLabels.values()) {
			arrayList.add(graphProperty.getProperty());
		}

		return arrayList;
	}

	public static GraphEdgeLabels getByName(String property) {
		for (GraphEdgeLabels inst : GraphEdgeLabels.values()) {
			if (inst.getProperty().equals(property)) {
				return inst;
			}
		}
		return null;
	}
}
