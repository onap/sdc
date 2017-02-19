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

package org.openecomp.sdc.be.resources.data;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import com.google.gson.reflect.TypeToken;

public class AdditionalInfoParameterData extends GraphNode {

	AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition;

	private Map<String, String> parameters;

	private Map<String, String> idToKey;

	public AdditionalInfoParameterData() {
		super(NodeTypeEnum.AdditionalInfoParameters);
		additionalInfoParameterDataDefinition = new AdditionalInfoParameterDataDefinition();
	}

	public AdditionalInfoParameterData(AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition,
			Map<String, String> parameters, Map<String, String> idToKey) {
		super(NodeTypeEnum.AdditionalInfoParameters);
		this.additionalInfoParameterDataDefinition = additionalInfoParameterDataDefinition;
		this.parameters = parameters;
		this.idToKey = idToKey;
	}

	public AdditionalInfoParameterData(Map<String, Object> properties) {

		this();

		additionalInfoParameterDataDefinition
				.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));

		additionalInfoParameterDataDefinition
				.setCreationTime((Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty()));

		additionalInfoParameterDataDefinition
				.setModificationTime((Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty()));

		additionalInfoParameterDataDefinition.setLastCreatedCounter(
				(Integer) properties.get(GraphPropertiesDictionary.PROPERTY_COUNTER.getProperty()));
		Type mapType = new TypeToken<HashMap<String, String>>() {
		}.getType();
		HashMap<String, String> prametersfromJson = getGson().fromJson(
				(String) properties.get(GraphPropertiesDictionary.ADDITIONAL_INFO_PARAMS.getProperty()), mapType);

		this.setParameters(prametersfromJson);

		// this.setParameters((HashMap<String, String>) properties
		// .get(GraphPropertiesDictionary.ADDITIONAL_INFO_PARAMS
		// .getProperty()));

		HashMap<String, String> idToKeyfromJson = getGson().fromJson(
				(String) properties.get(GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY.getProperty()), mapType);

		this.setIdToKey(idToKeyfromJson);
		// this.setIdToKey((HashMap<String, String>) properties
		// .get(GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY
		// .getProperty()));
	}

	@Override
	public Map<String, Object> toGraphMap() {

		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, additionalInfoParameterDataDefinition.getUniqueId());

		// String parametersToJson = getGson().toJson(getParameters());

		// addIfExists(map, GraphPropertiesDictionary.ADDITIONAL_INFO_PARAMS,
		// parametersToJson);
		addIfExists(map, GraphPropertiesDictionary.ADDITIONAL_INFO_PARAMS, getParameters());

		// String idToKeyToJson = getGson().toJson(getIdToKey());
		// addIfExists(map, GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY,
		// idToKeyToJson);
		addIfExists(map, GraphPropertiesDictionary.ADDITIONAL_INFO_ID_TO_KEY, getIdToKey());

		addIfExists(map, GraphPropertiesDictionary.CREATION_DATE,
				additionalInfoParameterDataDefinition.getCreationTime());

		addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE,
				additionalInfoParameterDataDefinition.getModificationTime());

		addIfExists(map, GraphPropertiesDictionary.PROPERTY_COUNTER,
				additionalInfoParameterDataDefinition.getLastCreatedCounter());

		return map;
	}

	@Override
	public Object getUniqueId() {
		return additionalInfoParameterDataDefinition.getUniqueId();
	}

	public AdditionalInfoParameterDataDefinition getAdditionalInfoParameterDataDefinition() {
		return additionalInfoParameterDataDefinition;
	}

	public void setAdditionalInfoParameterDataDefinition(
			AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition) {
		this.additionalInfoParameterDataDefinition = additionalInfoParameterDataDefinition;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getIdToKey() {
		return idToKey;
	}

	public void setIdToKey(Map<String, String> idToKey) {
		this.idToKey = idToKey;
	}

	@Override
	public String toString() {
		return "PropertyData [parameters= " + parameters + " idToKey= " + idToKey
				+ ", additionalInfoParameterDataDefinition=" + additionalInfoParameterDataDefinition + "]";
	}
}
