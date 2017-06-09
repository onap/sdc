package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class MapListCapabiltyDataDefinition extends MapDataDefinition<ListCapabilityDataDefinition> {

	public MapListCapabiltyDataDefinition(MapListCapabiltyDataDefinition cdt) {
		super(cdt);

	}

	@JsonCreator
	public MapListCapabiltyDataDefinition(Map<String, ListCapabilityDataDefinition> mapToscaDataDefinition) {
		super(mapToscaDataDefinition);
	}

	public MapListCapabiltyDataDefinition() {
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
