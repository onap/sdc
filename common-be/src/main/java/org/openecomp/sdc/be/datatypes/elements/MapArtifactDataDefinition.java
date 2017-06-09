package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class MapArtifactDataDefinition extends MapDataDefinition<ArtifactDataDefinition>{
		
	private String parentName;
	
	public MapArtifactDataDefinition(MapDataDefinition cdt, String parentName) {
		super(cdt);
		this.parentName = parentName;
	}
	
	@JsonCreator
	public MapArtifactDataDefinition(Map<String, ArtifactDataDefinition > mapToscaDataDefinition) {
		super(mapToscaDataDefinition);
	}
	public MapArtifactDataDefinition() {
		super();
		
	}
	@JsonValue
	@Override
	public Map<String, ArtifactDataDefinition> getMapToscaDataDefinition() {
		return mapToscaDataDefinition;
	}

	
	public void setMapToscaDataDefinition(Map<String, ArtifactDataDefinition> mapToscaDataDefinition) {
		this.mapToscaDataDefinition = mapToscaDataDefinition;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	

}
