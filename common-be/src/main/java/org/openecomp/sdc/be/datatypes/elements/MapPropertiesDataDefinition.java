package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class MapPropertiesDataDefinition extends MapDataDefinition<PropertyDataDefinition>{
		
	private String parentName;
	
	public MapPropertiesDataDefinition(MapDataDefinition cdt, String parentName) {
		super(cdt);
		this.parentName = parentName;
	}
	
	@JsonCreator
	public MapPropertiesDataDefinition(Map<String, PropertyDataDefinition > mapToscaDataDefinition) {
		super(mapToscaDataDefinition);
	}
	public MapPropertiesDataDefinition() {
		super();
		
	}
	@JsonValue
	@Override
	public Map<String, PropertyDataDefinition> getMapToscaDataDefinition() {
		return mapToscaDataDefinition;
	}

	
	public void setMapToscaDataDefinition(Map<String, PropertyDataDefinition> mapToscaDataDefinition) {
		this.mapToscaDataDefinition = mapToscaDataDefinition;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	

}
