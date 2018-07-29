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




import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class MapListCapabilityDataDefinition extends MapDataDefinition<ListCapabilityDataDefinition> {

	public MapListCapabilityDataDefinition(MapListCapabilityDataDefinition cdt) {
		super(cdt);

	}

	@JsonCreator
	public MapListCapabilityDataDefinition(Map<String, ListCapabilityDataDefinition> mapToscaDataDefinition) {
		super(mapToscaDataDefinition);
	}

	public MapListCapabilityDataDefinition() {
		super();

	}

	@JsonValue
	@Override
	public Map<String, ListCapabilityDataDefinition> getMapToscaDataDefinition() {
		return mapToscaDataDefinition;
	}

	public void add(String key, CapabilityDataDefinition value) {
		if (mapToscaDataDefinition == null) {
			mapToscaDataDefinition = new HashMap<>();
			ListCapabilityDataDefinition newList = new ListCapabilityDataDefinition(); 
			newList.add(value);
			put(key, newList);
		} else {
			ListCapabilityDataDefinition existValue = mapToscaDataDefinition.get(key);
			if (existValue == null) {
				ListCapabilityDataDefinition newList = new ListCapabilityDataDefinition(); 
				newList.add(value);
				put(key, newList);
			} else {
				existValue.getListToscaDataDefinition().add(value);
			}
		}
	}
}
