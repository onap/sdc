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

	public ListCapabilityDataDefinition mergeListItemsByName(ListCapabilityDataDefinition other){
		Map<String, CapabilityDataDefinition> mapByName = listToMapByName();
		Map<String, CapabilityDataDefinition> mapOtherByName = other.listToMapByName();
		mapByName.putAll(mapOtherByName);
		return new ListCapabilityDataDefinition(mapByName.values().stream().collect(Collectors.toList()));	
	}
}
