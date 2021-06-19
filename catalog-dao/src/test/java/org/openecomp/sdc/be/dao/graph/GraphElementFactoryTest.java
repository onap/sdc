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

import static org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum.AnnotationType;
import static org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

class GraphElementFactoryTest {

    private static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
        "src/test/resources/config/catalog-dao");
    private static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @Test
    void testCreateElement() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        Arrays.stream(NodeTypeEnum.values()).filter(nodeTypeEnum -> !nodeTypeEnum.equals(AnnotationType) && !nodeTypeEnum.equals(Component))
            .forEach(nodeTypeEnum -> {
                Assertions.assertTrue(GraphElementFactory
                    .createElement(nodeTypeEnum.getName(), GraphElementTypeEnum.Node, properties, GraphNode.class) instanceof GraphNode);
            });
        Assertions.assertNull(GraphElementFactory.createElement(Component.getName(), GraphElementTypeEnum.Node, properties, GraphNode.class));
        Assertions.assertThrows(NullPointerException.class,
            () -> GraphElementFactory.createElement(AnnotationType.getName(), GraphElementTypeEnum.Node, properties, GraphNode.class));
    }

    @Test
    void testCreateRelation() throws Exception {
        GraphRelation result = GraphElementFactory.createRelation("", new HashMap<>(), Mockito.mock(GraphNode.class), Mockito.mock(GraphNode.class));
        Assertions.assertTrue(result instanceof GraphRelation);
        Assertions.assertTrue(result.getFrom() instanceof RelationEndPoint);
        Assertions.assertTrue(result.getTo() instanceof RelationEndPoint);
    }

    @Test
    void testCreateNode() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        GraphNode result;

        result = GraphElementFactory.createElement(NodeTypeEnum.User.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof GraphNode);
        result = GraphElementFactory.createElement(NodeTypeEnum.ResourceCategory.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof GraphNode);
        result = GraphElementFactory.createElement(NodeTypeEnum.ServiceCategory.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertNotNull(result);
        result = GraphElementFactory.createElement(NodeTypeEnum.Tag.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertTrue(result instanceof GraphNode);
        Assertions.assertNotNull(result);
        result = GraphElementFactory.createElement(NodeTypeEnum.Service.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof GraphNode);
        result = GraphElementFactory.createElement(NodeTypeEnum.Property.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertNotNull(result);
        result = GraphElementFactory.createElement(NodeTypeEnum.Resource.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertTrue(result instanceof GraphNode);
        Assertions.assertNotNull(result);
        result = GraphElementFactory.createElement(NodeTypeEnum.HeatParameter.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof GraphNode);
        result = GraphElementFactory.createElement(NodeTypeEnum.HeatParameterValue.getName(), GraphElementTypeEnum.Node, properties);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result instanceof GraphNode);
    }
}
