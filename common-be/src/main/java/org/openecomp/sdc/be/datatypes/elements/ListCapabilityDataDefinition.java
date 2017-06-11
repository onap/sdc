package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class ListCapabilityDataDefinition extends ListDataDefinition<CapabilityDataDefinition> {
	
	public ListCapabilityDataDefinition(ListCapabilityDataDefinition cdt) {
		super(cdt);
		
	}
	
	@JsonCreator
	public ListCapabilityDataDefinition(List< CapabilityDataDefinition > listToscaDataDefinition) {
		super(listToscaDataDefinition);
	}
	public ListCapabilityDataDefinition() {
		super();
		
	}
	@JsonValue
	@Override
	public List<CapabilityDataDefinition> getListToscaDataDefinition() {
		return listToscaDataDefinition;
	}

	
	public void setListToscaDataDefinition(List<CapabilityDataDefinition> listToscaDataDefinition) {
		this.listToscaDataDefinition = listToscaDataDefinition;
	}

}
