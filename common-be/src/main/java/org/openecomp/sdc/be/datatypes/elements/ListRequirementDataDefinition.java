package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

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


	
	
	
}
