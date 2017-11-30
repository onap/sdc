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

package org.openecomp.sdc.be.dao.jsongraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import com.thinkaurelius.titan.core.TitanVertex;

public class GraphVertex {
	private String uniqueId;

	private TitanVertex vertex;
	private VertexTypeEnum label;

	private Map<String, ? extends ToscaDataDefinition> json;
	private Map<String, Object> metadataJson;
	private Map<GraphPropertyEnum, Object> metadataProperties;

	public GraphVertex() {

	}

	public GraphVertex(VertexTypeEnum label) {
		super();
		this.label = label;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Map<String, ? extends ToscaDataDefinition> getJson() {
		return json;
	}

	public void setJson(Map<String, ? extends ToscaDataDefinition> json) {
		this.json = json;
	}

	public TitanVertex getVertex() {
		return vertex;
	}

	public void setVertex(TitanVertex vertex) {
		this.vertex = vertex;
	}

	public VertexTypeEnum getLabel() {
		return label;
	}

	public void setLabel(VertexTypeEnum label) {
		this.label = label;
	}

	public ComponentTypeEnum getType() {
		ComponentTypeEnum type = ComponentTypeEnum.valueOf((String) getMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE));
		return type;
	}

	public void setType(ComponentTypeEnum type) {
		addMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE, type.name());
	}

	public void addMetadataProperty(GraphPropertyEnum propName, Object propValue) {
		if (metadataProperties == null) {
			metadataProperties = new HashMap<>();
		}
		if (propValue != null) {
			metadataProperties.put(propName, propValue);
		}
	}

	public Object getMetadataProperty(GraphPropertyEnum metadataProperty) {
		if (metadataProperties != null) {
			return metadataProperties.get(metadataProperty);
		}
		return null;
	}

	public Map<GraphPropertyEnum, Object> getMetadataProperties() {
		return metadataProperties;
	}

	public void setMetadataProperties(Map<GraphPropertyEnum, Object> metadataProperties) {
		this.metadataProperties = metadataProperties;
	}

	public Map<String, Object> getMetadataJson() {
		return metadataJson;
	}

	public void setMetadataJson(Map<String, Object> metadataJson) {
		this.metadataJson = metadataJson;
	}

	/**
	 * used for clone vertex in case of copy on update
	 * 
	 * @param other
	 */
	public void cloneData(GraphVertex other) {
		// need to be deep copy???
		json = other.getJson();
		metadataJson = other.getMetadataJson();
		metadataProperties = other.getMetadataProperties();
	}

	public void setJsonMetadataField(JsonPresentationFields field, Object value) {
		if (metadataJson == null) {
			metadataJson = new HashMap<>();
		}
		metadataJson.put(field.getPresentation(), value);
	}

	public Object getJsonMetadataField(JsonPresentationFields field) {
		if (metadataJson != null) {
			return metadataJson.get(field.getPresentation());
		}
		return null;
	}

	/**
	 * Updates metadata json with current metadataProperties. Note that already existing property containing in metadata json can be overrided by new value if metadataProperties contains the same property (by key). Note that metadata json can contain
	 * a property that is not presented in metadataProperties. In such case the property will be put in metadata json.
	 */
	public void updateMetadataJsonWithCurrentMetadataProperties() {
		if (!MapUtils.isEmpty(metadataProperties)) {
			if (metadataJson == null) {
				metadataJson = new HashMap<>();
			}
			for (Entry<GraphPropertyEnum, Object> entry : metadataProperties.entrySet()) {
				String propertyName = JsonPresentationFields.getPresentationByGraphProperty(entry.getKey());
				if (StringUtils.isNotEmpty(propertyName) && entry.getValue() != null) {
					metadataJson.put(propertyName, entry.getValue());
				}
			}
		}
	}
}
