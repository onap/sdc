package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class MapGroupsDataDefinition extends MapDataDefinition<GroupInstanceDataDefinition>{
		
	private String parentName;
	
	public MapGroupsDataDefinition(MapDataDefinition<GroupInstanceDataDefinition> cdt, String parentName) {
		super(cdt);
		this.parentName = parentName;
	}
	
	@JsonCreator
	public MapGroupsDataDefinition(Map<String, GroupInstanceDataDefinition> mapToscaDataDefinition) {
		super(mapToscaDataDefinition);
	}
	public MapGroupsDataDefinition() {
		super();
		
	}
	@JsonValue
	@Override
	public Map<String, GroupInstanceDataDefinition> getMapToscaDataDefinition() {
		return mapToscaDataDefinition;
	}

	
	public void setMapToscaDataDefinition(Map<String, GroupInstanceDataDefinition> mapToscaDataDefinition) {
		this.mapToscaDataDefinition = mapToscaDataDefinition;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	

}
