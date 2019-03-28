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

package org.openecomp.sdc.be.dao.graph.datatype;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.jsongraph.heal.HealConstants;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public abstract class GraphNode extends GraphElement {

	private static final Gson gson = new Gson();

	private NodeTypeEnum label;

    protected GraphNode(NodeTypeEnum label) {
        super(GraphElementTypeEnum.Node);

        this.label = label;
    }
	
	protected Gson getGson() {
		return gson;
	}

	public String getLabel() {
		return label.getName();
	}

	public ImmutablePair<String, Object> getKeyValueId() {
        return new ImmutablePair<>(getUniqueIdKey(), getUniqueId());
	}

	protected void addIfExists(Map<String, Object> map, GraphPropertiesDictionary property, Object value) {
		if (value != null) {
			if (value instanceof List || value instanceof Map) {
				value = getGson().toJson(value);
			}
			map.put(property.getProperty(), value);
		}
	}



	public abstract String getUniqueId();


	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.UNIQUE_ID.getProperty();
	}

	public String getHealingVersionKey() {
		return GraphPropertyEnum.HEALING_VERSION.getProperty();
	}

	/**
	 * Must be overridden in implelemting classes
	 * @return current heal version. Default heal version if function not implemented.
	 */
	public Integer getHealingVersion(){
		return HealConstants.DEFAULT_HEAL_VERSION;
	}

	/**
	 * Must be overriden in implementing classes
	 * @param version healing version number
	 */
	public void setHealingVersion(Integer version){

	}

	@Override
	public String toString() {
		return "GraphNode [label=" + label + ", parent: " + super.toString() + "]";
	}


}
