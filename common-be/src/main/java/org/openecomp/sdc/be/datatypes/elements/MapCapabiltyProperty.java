package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class MapCapabiltyProperty extends MapDataDefinition<MapPropertiesDataDefinition> {
	@JsonCreator
	public MapCapabiltyProperty(Map<String, MapPropertiesDataDefinition > mapToscaDataDefinition) {
		super(mapToscaDataDefinition);
	}
	public MapCapabiltyProperty() {
		super();
		
	}
	@JsonValue
	@Override
	public Map<String, MapPropertiesDataDefinition> getMapToscaDataDefinition() {
		return mapToscaDataDefinition;
	}
	
	public void setMapToscaDataDefinition(Map<String, MapPropertiesDataDefinition> mapToscaDataDefinition) {
		this.mapToscaDataDefinition = mapToscaDataDefinition;
	}

}
