package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class ListMapPropertiesDataDefinition extends ListDataDefinition<MapPropertiesDataDefinition> {
	
	public ListMapPropertiesDataDefinition(ListMapPropertiesDataDefinition cdt) {
		super(cdt);
		
	}
	
	@JsonCreator
	public ListMapPropertiesDataDefinition(List< MapPropertiesDataDefinition > listToscaDataDefinition) {
		super(listToscaDataDefinition);
	}
	public ListMapPropertiesDataDefinition() {
		super();
		
	}
	@JsonValue
	@Override
	public List<MapPropertiesDataDefinition> getListToscaDataDefinition() {
		return listToscaDataDefinition;
	}

	
	public void setMapToscaDataDefinition(List<MapPropertiesDataDefinition> listToscaDataDefinition) {
		this.listToscaDataDefinition = listToscaDataDefinition;
	}


}
