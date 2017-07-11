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

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class MapListRequirementDataDefinition extends MapDataDefinition<ListRequirementDataDefinition> {

	public MapListRequirementDataDefinition(MapListRequirementDataDefinition cdt) {
		super(cdt);

	}

	@JsonCreator
	public MapListRequirementDataDefinition(Map<String, ListRequirementDataDefinition> mapToscaDataDefinition) {
		super(mapToscaDataDefinition);
	}

	public MapListRequirementDataDefinition() {
		super();

	}

	@JsonValue
	@Override
	public Map<String, ListRequirementDataDefinition> getMapToscaDataDefinition() {
		return mapToscaDataDefinition;
	}

	public void add(String key, RequirementDataDefinition value) {
		if (mapToscaDataDefinition == null) {
			mapToscaDataDefinition = new HashMap<>();
			ListRequirementDataDefinition newList = new ListRequirementDataDefinition();
			newList.add(value);
			put(key, newList);
		} else {
			ListRequirementDataDefinition existValue = mapToscaDataDefinition.get(key);
			if (existValue == null) {
				ListRequirementDataDefinition newList = new ListRequirementDataDefinition();
				newList.add(value);
				put(key, newList);
			} else {
				existValue.getListToscaDataDefinition().add(value);
			}
		}
	}

}
