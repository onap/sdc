package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public  class MapDataDefinition <T extends ToscaDataDefinition>  extends ToscaDataDefinition  {
	
	protected Map<String, T > mapToscaDataDefinition;
	
	public MapDataDefinition(MapDataDefinition<T> cdt) {
		super();
		mapToscaDataDefinition = cdt.mapToscaDataDefinition;	
		
	}
	@JsonCreator
	public MapDataDefinition(Map<String, T > mapToscaDataDefinition) {
		super();
		this.mapToscaDataDefinition = mapToscaDataDefinition;	
	}

	public MapDataDefinition() {
		super();
	}
	@JsonValue
	public Map<String, T > getMapToscaDataDefinition() {
		return mapToscaDataDefinition;
	}
	
	public void put(String key, T value){
		if(mapToscaDataDefinition == null){
			mapToscaDataDefinition = new HashMap<String, T>();
		}
		mapToscaDataDefinition.put(key, value);
	}
	
	public void delete(String key){
		if(mapToscaDataDefinition != null && mapToscaDataDefinition.containsKey(key)){
			mapToscaDataDefinition.remove(key);
		}
	}
	
	public T findByKey(String key){
		T value = null;
		if(mapToscaDataDefinition != null && mapToscaDataDefinition.containsKey(key)){
			value = mapToscaDataDefinition.get(key);
		}
		return value;
	}
	@Override
	public void setOwnerIdIfEmpty(String ownerId) {
		if ( mapToscaDataDefinition != null ){
			mapToscaDataDefinition.entrySet().forEach(e -> e.getValue().setOwnerIdIfEmpty(ownerId));
		}
	}
	
}
