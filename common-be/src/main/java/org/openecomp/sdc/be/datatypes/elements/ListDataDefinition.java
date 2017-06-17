package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonCreator;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;


public class ListDataDefinition<T extends ToscaDataDefinition> extends ToscaDataDefinition {

	protected List<T> listToscaDataDefinition;

	public ListDataDefinition(ListDataDefinition<T> cdt) {
		super();
		listToscaDataDefinition = cdt.listToscaDataDefinition;

	}

	@JsonCreator
	public ListDataDefinition(List<T> listToscaDataDefinition) {
		super();
		this.listToscaDataDefinition = listToscaDataDefinition;
	}

	public ListDataDefinition() {
		super();
	}

	public List<T> getListToscaDataDefinition() {
		return listToscaDataDefinition;
	}

	public void add(T value) {
		if (listToscaDataDefinition == null) {
			listToscaDataDefinition = new ArrayList<T>();
		}
		listToscaDataDefinition.add(value);
	}

	public void delete(T value) {
		if (listToscaDataDefinition != null) {
			listToscaDataDefinition.remove(value);
		}
	}

	@Override
	public void setOwnerIdIfEmpty(String ownerId) {
		if (listToscaDataDefinition != null) {
			listToscaDataDefinition.forEach(e -> e.setOwnerIdIfEmpty(ownerId));
		}
	}
	
	@Override
	public <S extends ToscaDataDefinition> S mergeFunction(S other, boolean allowDefaultValueOverride){
		Map<String, T> mapByName = listToMapByName(listToscaDataDefinition);
		List<T> otherList = ((ListDataDefinition)other).getListToscaDataDefinition();
		for(T item : otherList){
			mapByName.merge((String)item.getToscaPresentationValue(JsonPresentationFields.NAME), item, (thisItem, otherItem) -> thisItem.mergeFunction(otherItem, allowDefaultValueOverride));
		}
		((ListDataDefinition)other).listToscaDataDefinition = mapByName.values().stream().collect(Collectors.toList());
		return other;	
	}
		

}
