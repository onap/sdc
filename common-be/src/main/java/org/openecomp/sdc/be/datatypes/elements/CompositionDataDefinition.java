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
