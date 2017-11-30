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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
	public <S extends ToscaDataDefinition> S mergeFunction(S other, boolean allowDefaultValueOverride) {
		Map<String, T> mapByName = listToMapByName(listToscaDataDefinition);
		List<T> otherList = ((ListDataDefinition) other).getListToscaDataDefinition();
		for (T item : otherList) {
			mapByName.merge((String) item.getToscaPresentationValue(JsonPresentationFields.NAME), item, (thisItem, otherItem) -> thisItem.mergeFunction(otherItem, allowDefaultValueOverride));
		}
		((ListDataDefinition) other).listToscaDataDefinition = mapByName.values().stream().collect(Collectors.toList());
		return other;
	}

	@Override
	public boolean findUidMatch(String uid) {
		return listToscaDataDefinition.stream().anyMatch(p -> p.findUidMatch(uid));
	}

	@Override
	public <T extends ToscaDataDefinition> T removeByOwnerId(Set<String> ownerIdList) {
		List<T> collect1 = (List<T>) listToscaDataDefinition.stream().filter(e -> ownerIdList.contains(e.getOwnerId())).collect(Collectors.toList());
		ListDataDefinition listDef = new ListDataDefinition(collect1);

		listToscaDataDefinition.removeIf(e -> ownerIdList.contains(e.getOwnerId()));
		return (T) listDef;
	}

	@Override
	public <T extends ToscaDataDefinition> T updateIfExist(T other, boolean allowDefaultValueOverride) {
		
		List<T> list = ((ListDataDefinition)other).getListToscaDataDefinition();
		list.forEach(e -> {
			String nameFromPrev = (String)e.getToscaPresentationValue(JsonPresentationFields.NAME);
			if ( nameFromPrev != null ){
				Optional<T> findAny = (Optional<T>) listToscaDataDefinition.stream().filter(o->nameFromPrev.equals(e.getToscaPresentationValue(JsonPresentationFields.NAME))).findAny();
				if ( findAny.isPresent() ){
					e.mergeFunction(findAny.get(), allowDefaultValueOverride);
				}
			}
		});
		return other;
	}
	@Override
	public boolean isEmpty(){
		return listToscaDataDefinition == null || listToscaDataDefinition.isEmpty();
	}
}
