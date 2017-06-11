package org.openecomp.sdc.be.datatypes.tosca;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonValue;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;

import fj.data.Either;

public abstract class ToscaDataDefinition {
	
	protected Map<String, Object> toscaPresentation;

	
	public ToscaDataDefinition(){
		toscaPresentation = new HashMap<String, Object>();
	}
	@JsonCreator
	public ToscaDataDefinition(Map<String, Object> art){
		toscaPresentation = art;
	}
	@JsonValue
	public Object getToscaPresentationValue(JsonPresentationFields name) {
		if (toscaPresentation != null && toscaPresentation.containsKey(name.getPresentation())) {
			return toscaPresentation.get(name.getPresentation());
		}
		return null;
	}
	
	public void setToscaPresentationValue(JsonPresentationFields name, Object value) {
		if (toscaPresentation == null && value !=null) {
			toscaPresentation = new HashMap<String, Object>();			
		}
		toscaPresentation.put(name.getPresentation(), value);
		
	}
	public void setOwnerIdIfEmpty(String ownerId){
		if (  getOwnerId() == null ){
			setOwnerId(ownerId);
		}
	}
	public void setOwnerId(String ownerId){
		setToscaPresentationValue(JsonPresentationFields.OWNER_ID, ownerId);
	}

	public String getOwnerId(){
		return (String) getToscaPresentationValue(JsonPresentationFields.OWNER_ID);
	}
	
	
	public String getType(){
		return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
	}
	
	public String getName(){
		return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
	}
	
	//default merge function for merging data maps - implement where needed and use mergeDataMaps method where applicable instead of map1.putAll(map2) 
	public <T extends ToscaDataDefinition> T mergeFunction(T other, boolean allowDefaultValueOverride){
		other.setOwnerId(getOwnerId());
		return other;
	}
	
	public static <T extends ToscaDataDefinition> Either<Map<String, T>, String> mergeDataMaps(Map<String, T> map1, Map<String, T> map2){
		return mergeDataMaps(map1, map2, false);
	}
	
	//return Either.right(item key) if an illegal merge was attempted (overriding data type is forbidden)
	public static <T extends ToscaDataDefinition> Either<Map<String, T>, String> mergeDataMaps(Map<String, T> map1, Map<String, T> map2, boolean allowDefaultValueOverride){
		for(Entry<String, T> entry : map2.entrySet()){
			map1.merge(entry.getKey(), entry.getValue(), (item1, item2) -> item1.mergeFunction(item2, allowDefaultValueOverride));
		    //validate merge success
		    if(!map1.containsKey(entry.getKey()))
		    	return Either.right(entry.getKey());
		}
		return Either.left(map1);
	}
	
	public static <T extends ToscaDataDefinition> Map<String, T> listToMapByName(List<T> dataList) {
		return null == dataList? new HashMap<>() : dataList.stream()
		.collect(Collectors.toMap(p -> p.getName(), p -> p));
	}
}
