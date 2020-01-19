/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.dao.graph;

import mockit.Deencapsulation;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import java.util.HashMap;
import java.util.Map;

public class GraphElementFactoryTest {

	private static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
			"src/test/resources/config/catalog-dao");
	private static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

	@Test
	public void testCreateElement() throws Exception {
		String label = "mock";
		Map<String, Object> properties = new HashMap<>();
		Class<GraphNode> clazz = (Class<GraphNode>) (Mockito.mock(GraphNode.class)).getClass();

		// default test
		GraphElementFactory.createElement(label, GraphElementTypeEnum.Node, properties, clazz);
	}

	@Test
	public void testCreateElement_1() throws Exception {
		String label = "mock";
		GraphElementTypeEnum type = null;
		Map<String, Object> properties = new HashMap<>();
		GraphNode result;

		// default test
		result = GraphElementFactory.createElement(label, GraphElementTypeEnum.Node, properties);
	}

	@Test
	public void testCreateRelation() throws Exception {
		String type = "";
		Map<String, Object> properties = new HashMap<>();
		GraphNode from = Mockito.mock(GraphNode.class);
		GraphNode to = Mockito.mock(GraphNode.class);
		;
		GraphRelation result;

		// default test
		result = GraphElementFactory.createRelation(type, properties, from, to);
	}

	@Test
	public void testCreateNode() throws Exception {
		Map<String, Object> properties = new HashMap<>();
		GraphNode result;

		result = Deencapsulation.invoke(GraphElementFactory.class, "createNode", NodeTypeEnum.User.getName(),
				properties);
		result = Deencapsulation.invoke(GraphElementFactory.class, "createNode",
				NodeTypeEnum.ResourceCategory.getName(), properties);
		result = Deencapsulation.invoke(GraphElementFactory.class, "createNode", NodeTypeEnum.ServiceCategory.getName(),
				properties);
		result = Deencapsulation.invoke(GraphElementFactory.class, "createNode", NodeTypeEnum.Tag.getName(),
				properties);
		result = Deencapsulation.invoke(GraphElementFactory.class, "createNode", NodeTypeEnum.Service.getName(),
				properties);
		result = Deencapsulation.invoke(GraphElementFactory.class, "createNode", NodeTypeEnum.Property.getName(),
				properties);
		result = Deencapsulation.invoke(GraphElementFactory.class, "createNode", NodeTypeEnum.HeatParameter.getName(),
				properties);
		result = Deencapsulation.invoke(GraphElementFactory.class, "createNode",
				NodeTypeEnum.HeatParameterValue.getName(), properties);
	}
}
