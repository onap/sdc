/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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


	public String findKeyByItemUidMatch(String uid){
		if(null == mapToscaDataDefinition || uid == null)
			return null;
		Map.Entry<String, T> entry = mapToscaDataDefinition.entrySet().stream().filter(e ->
				e.getValue().findUidMatch(uid))
				.findAny().orElse(null);
		if(null == entry)
			return null;
		return entry.getKey();
	}
	@Override
	public <T extends ToscaDataDefinition>  T removeByOwnerId(Set<String> ownerIdList) {
		Map<String, T > collect = (Map<String, T >)mapToscaDataDefinition.entrySet().stream().filter(e -> ownerIdList.contains(e.getValue().getOwnerId())).collect(Collectors.toMap(Map.Entry::getKey, (Map.Entry::getValue)));
		
		MapDataDefinition collectMap = new MapDataDefinition<>(collect);
		
		mapToscaDataDefinition.entrySet().removeIf(e -> ownerIdList.contains(e.getValue().getOwnerId()));
		
		return (T) collectMap;
	}

	@Override
	public <T extends ToscaDataDefinition> T updateIfExist(T other, boolean allowDefaultValueOverride) {
		
		Map<String, T > map = ((MapDataDefinition)other).getMapToscaDataDefinition();
		
		map.entrySet().forEach(e ->{
			String key = e.getKey();
			if ( mapToscaDataDefinition.containsKey(key) ){
				e.getValue().mergeFunction(mapToscaDataDefinition.get(key), allowDefaultValueOverride);
			}
		});
		return other;
	}
	@Override
	public boolean isEmpty(){
		return mapToscaDataDefinition == null || mapToscaDataDefinition.isEmpty();
	}
}
