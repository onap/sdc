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

public enum GraphEdgePropertiesDictionary {

	// field name class type
	// stored in graph
	STATE("state", String.class), 
	NAME("name", String.class), 
	GROUP_TYPE("groupType", String.class), 
	SOURCE("source", String.class), 
	OWNER_ID("ownerId", String.class), 
	REQUIRED_OCCURRENCES("requiredOccurrences", String.class), 
	LEFT_OCCURRENCES("leftOccurrences", String.class), 
	GET_INPUT_INDEX("get_input_index", String.class);

	private String property;
	private Class clazz;

	GraphEdgePropertiesDictionary(String property, Class clazz) {
		this.property = property;
		this.clazz = clazz;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Class getClazz() {
		return clazz;
	}

	public void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public static List<String> getAllProperties() {

		List<String> arrayList = new ArrayList<String>();

		for (GraphEdgePropertiesDictionary graphProperty : GraphEdgePropertiesDictionary.values()) {
			arrayList.add(graphProperty.getProperty());
		}

		return arrayList;
	}

	public static GraphEdgePropertiesDictionary getByName(String property) {
		for (GraphEdgePropertiesDictionary inst : GraphEdgePropertiesDictionary.values()) {
			if (inst.getProperty().equals(property)) {
				return inst;
			}
		}
		return null;
	}
}
