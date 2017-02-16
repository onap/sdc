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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.Constants;
import org.openecomp.sdc.be.datatypes.elements.HeatParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class HeatParameterData extends GraphNode {

	private HeatParameterDataDefinition heatDataDefinition;

	public HeatParameterData() {
		super(NodeTypeEnum.HeatParameter);
		heatDataDefinition = new HeatParameterDataDefinition();
	}

	public HeatParameterData(HeatParameterDataDefinition heatDataDef) {
		super(NodeTypeEnum.HeatParameter);
		this.heatDataDefinition = heatDataDef;
	}

	public HeatParameterData(Map<String, Object> properties) {
		this();

		heatDataDefinition.setUniqueId((String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty()));
		heatDataDefinition.setName((String) properties.get(GraphPropertiesDictionary.NAME.getProperty()));
		String type = (String) properties.get(GraphPropertiesDictionary.TYPE.getProperty());
		heatDataDefinition.setType(type);

		String description = (String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty());
		if (Constants.GRAPH_EMPTY_VALUE.equals(description)) {
			heatDataDefinition.setDescription(null);
		} else {
			heatDataDefinition.setDescription(description);
		}

		String defaultValue = (String) properties.get(GraphPropertiesDictionary.DEFAULT_VALUE.getProperty());
		if (Constants.GRAPH_EMPTY_VALUE.equals(defaultValue)) {
			heatDataDefinition.setDefaultValue(null);
		} else {
			heatDataDefinition.setDefaultValue(getValue(type, defaultValue));
		}

		String value = (String) properties.get(GraphPropertiesDictionary.VALUE.getProperty());
		if (Constants.GRAPH_EMPTY_VALUE.equals(value)) {
			heatDataDefinition.setCurrentValue(null);
		} else {
			heatDataDefinition.setCurrentValue(getValue(type, value));
		}

	}

	private String getValue(String type, String value) {
		if (Constants.GRAPH_EMPTY_VALUE.equals(value)) {
			return value;
		}
		if ("number".equals(type)) {
			return new BigDecimal(value).toPlainString();
		}
		return value;
	}

	public HeatParameterDataDefinition getHeatDataDefinition() {
		return heatDataDefinition;
	}

	public void setHeatDataDefinition(HeatParameterDataDefinition heatDataDefinition) {
		this.heatDataDefinition = heatDataDefinition;
	}

	public String getName() {
		return heatDataDefinition.getName();
	}

	public void setName(String name) {
		heatDataDefinition.setName(name);
	}

	public String getType() {
		return heatDataDefinition.getType();
	}

	public void setType(String type) {
		heatDataDefinition.setType(type);
	}

	public String getDescription() {
		return heatDataDefinition.getDescription();
	}

	public void setDescription(String description) {
		heatDataDefinition.setDescription(description);
	}

	public String getCurrentValue() {
		return heatDataDefinition.getCurrentValue();
	}

	public void setCurrentValue(String currentValue) {
		heatDataDefinition.setCurrentValue(currentValue);
	}

	public String getDefaultValue() {
		return heatDataDefinition.getDefaultValue();
	}

	public void setDefaultValue(String defaultValue) {
		heatDataDefinition.setDefaultValue(defaultValue);
	}

	@Override
	public Object getUniqueId() {
		return heatDataDefinition.getUniqueId();
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, getUniqueId());

		addIfExists(map, GraphPropertiesDictionary.NAME, getName());

		addIfExists(map, GraphPropertiesDictionary.TYPE, getType());

		String description = getDescription();
		if (description == null) {
			description = Constants.GRAPH_EMPTY_VALUE;
		}
		addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, description);

		String defaultVal = getDefaultValue();
		if (defaultVal == null) {
			defaultVal = Constants.GRAPH_EMPTY_VALUE;
		}
		addIfExists(map, GraphPropertiesDictionary.DEFAULT_VALUE, getValue(getType(), defaultVal));

		String currentVal = getCurrentValue();
		if (currentVal == null) {
			currentVal = Constants.GRAPH_EMPTY_VALUE;
		}

		addIfExists(map, GraphPropertiesDictionary.VALUE, getValue(getType(), currentVal));
		return map;
	}

}
