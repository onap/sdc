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

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class ConsumerData extends GraphNode {

	private ConsumerDataDefinition consumerDataDefinition;

	public ConsumerData() {
		super(NodeTypeEnum.ConsumerCredentials);
		consumerDataDefinition = new ConsumerDataDefinition();
	}

	public ConsumerData(ConsumerDataDefinition consumerDataDefinition) {
		super(NodeTypeEnum.ConsumerCredentials);
		this.consumerDataDefinition = consumerDataDefinition;

	}

	public ConsumerData(Map<String, Object> properties) {
		super(NodeTypeEnum.ConsumerCredentials);
		consumerDataDefinition = new ConsumerDataDefinition();
		consumerDataDefinition.setConsumerDetailsLastupdatedtime((Long) properties.get(GraphPropertiesDictionary.CONSUMER_DETAILS_LAST_UPDATED_TIME.getProperty()));
		consumerDataDefinition.setConsumerLastAuthenticationTime((Long) properties.get(GraphPropertiesDictionary.CONSUMER_LAST_AUTHENTICATION_TIME.getProperty()));
		consumerDataDefinition.setConsumerName((String) properties.get(GraphPropertiesDictionary.CONSUMER_NAME.getProperty()));
		consumerDataDefinition.setConsumerPassword((String) properties.get(GraphPropertiesDictionary.CONSUMER_PASSWORD.getProperty()));
		consumerDataDefinition.setConsumerSalt((String) properties.get(GraphPropertiesDictionary.CONSUMER_SALT.getProperty()));
		consumerDataDefinition.setLastModfierAtuid((String) properties.get(GraphPropertiesDictionary.LAST_MODIFIER_USER_ID.getProperty()));
	}

	@Override
	public String getUniqueIdKey() {
		return GraphPropertiesDictionary.CONSUMER_NAME.getProperty();
	}

	@Override
	public Object getUniqueId() {
		return consumerDataDefinition.getConsumerName();
	}

	public ConsumerDataDefinition getConsumerDataDefinition() {
		return consumerDataDefinition;
	}

	@Override
	public Map<String, Object> toGraphMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		addIfExists(map, GraphPropertiesDictionary.CONSUMER_NAME, this.consumerDataDefinition.getConsumerName());
		addIfExists(map, GraphPropertiesDictionary.CONSUMER_PASSWORD,
				this.consumerDataDefinition.getConsumerPassword());
		addIfExists(map, GraphPropertiesDictionary.CONSUMER_SALT, this.consumerDataDefinition.getConsumerSalt());
		addIfExists(map, GraphPropertiesDictionary.CONSUMER_LAST_AUTHENTICATION_TIME,
				this.consumerDataDefinition.getConsumerLastAuthenticationTime());
		addIfExists(map, GraphPropertiesDictionary.CONSUMER_DETAILS_LAST_UPDATED_TIME,
				this.consumerDataDefinition.getConsumerDetailsLastupdatedtime());
		addIfExists(map, GraphPropertiesDictionary.LAST_MODIFIER_USER_ID,
				this.consumerDataDefinition.getLastModfierAtuid());

		return map;
	}

	@Override
	public String toString() {
		return "ConsumerData [consumerDataDefinition=" + consumerDataDefinition + "]";
	}
}
