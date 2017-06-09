package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class MapAttributesDataDefinition extends MapDataDefinition<AttributeDataDefinition>{
		
	private String parentName;
	
	public MapAttributesDataDefinition(MapDataDefinition cdt, String parentName) {
		super(cdt);
		this.parentName = parentName;
	}
	
	@JsonCreator
	public MapAttributesDataDefinition(Map<String, AttributeDataDefinition > mapToscaDataDefinition) {
		super(mapToscaDataDefinition);
	}
	public MapAttributesDataDefinition() {
		super();
		
	}
	@JsonValue
	@Override
	public Map<String, AttributeDataDefinition> getMapToscaDataDefinition() {
		return mapToscaDataDefinition;
	}

	
	public void setMapToscaDataDefinition(Map<String, AttributeDataDefinition> mapToscaDataDefinition) {
		this.mapToscaDataDefinition = mapToscaDataDefinition;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	

}
