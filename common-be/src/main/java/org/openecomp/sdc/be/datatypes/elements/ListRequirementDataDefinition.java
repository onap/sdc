package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class ListRequirementDataDefinition extends ListDataDefinition<RequirementDataDefinition> {
	
	public ListRequirementDataDefinition(ListRequirementDataDefinition cdt) {
		super(cdt);
		
	}
	
	@JsonCreator
	public ListRequirementDataDefinition(List< RequirementDataDefinition > listToscaDataDefinition) {
		super(listToscaDataDefinition);
	}
	public ListRequirementDataDefinition() {
		super();
		
	}
	@JsonValue
	@Override
	public List<RequirementDataDefinition> getListToscaDataDefinition() {
		return listToscaDataDefinition;
	}

	
	public void setListToscaDataDefinition(List<RequirementDataDefinition> listToscaDataDefinition) {
		this.listToscaDataDefinition = listToscaDataDefinition;
	}

	public ListRequirementDataDefinition mergeListItemsByName(ListRequirementDataDefinition other){
		Map<String, RequirementDataDefinition> mapByName = listToMapByName();
		Map<String, RequirementDataDefinition> mapOtherByName = other.listToMapByName();
		mapByName.putAll(mapOtherByName);
		return new ListRequirementDataDefinition(mapByName.values().stream().collect(Collectors.toList()));	
	}
	
}
