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
