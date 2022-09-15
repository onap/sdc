/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.resources.data.PropertyData;

@ExtendWith(MockitoExtension.class)
class AttributeOperationTest extends ModelTestBase {

    @InjectMocks
    private AttributeOperation attributeOperation;

    @Mock
    private HealingJanusGraphGenericDao janusGraphGenericDao;

    @BeforeAll
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @BeforeEach
    void setUp() {
        attributeOperation = new AttributeOperation(janusGraphGenericDao);
    }

    @Test
    void isAttributeTypeValid() {
        final List<ImmutablePair<GraphNode, GraphEdge>> list = new ArrayList<>();
        final GraphEdge edge = new GraphEdge();
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", "property");
        edge.setProperties(map);
        final ImmutablePair<GraphNode, GraphEdge> pairPropertyData = new ImmutablePair<>(new PropertyData(), edge);
        list.add(pairPropertyData);
        final ImmutablePair<GraphNode, GraphEdge> pairDataTypeData = new ImmutablePair<>(new DataTypeData(), edge);

        when(janusGraphGenericDao.getNode(any(), any(), any())).thenReturn(Either.left(new DataTypeData()));
        doReturn(Either.<List<ImmutablePair<GraphNode, GraphEdge>>, JanusGraphOperationStatus>left(list))
            .when(janusGraphGenericDao)
            .getChildrenNodes("uid", "null.dataType", GraphEdgeLabels.PROPERTY, NodeTypeEnum.Property, PropertyData.class);
        doReturn(Either.<ImmutablePair<GraphNode, GraphEdge>, JanusGraphOperationStatus>left(pairDataTypeData))
            .when(janusGraphGenericDao).getChild("uid", "null.dataType", GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.DataType, DataTypeData.class);
        doReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND))
            .when(janusGraphGenericDao)
            .getChildrenNodes("uid", null, GraphEdgeLabels.PROPERTY, NodeTypeEnum.Property, PropertyData.class);
        when(janusGraphGenericDao.getChild(eq("uid"), eq(null), any(GraphEdgeLabels.class), any(NodeTypeEnum.class), any()))
            .thenReturn(Either.right(JanusGraphOperationStatus.GENERAL_ERROR));

        final var attributeDefinition = new AttributeDataDefinition();
        assertFalse(attributeOperation.isAttributeTypeValid(attributeDefinition));
    }

    @Test
    void isAttributeTypeValid_when_Null() {
        assertFalse(attributeOperation.isAttributeTypeValid(null));
    }

    @Test
    void isAttributeTypeValid_when_type_valid() {
        final var attributeDefinition = new AttributeDataDefinition();
        attributeDefinition.setType("string");
        assertTrue(attributeOperation.isAttributeTypeValid(attributeDefinition));
    }

    @Test
    void isAttributeInnerTypeValid() {
        final var attributeDefinition = new AttributeDataDefinition();
        attributeDefinition.setType("string");
        final var result = attributeOperation.isAttributeInnerTypeValid(attributeDefinition, new HashMap<>());
        assertNotNull(result);
        assertNull(result.getLeft());
        assertFalse(result.getRight());
    }

    @Test
    void isAttributeInnerTypeValid_when_null() {
        final var result = attributeOperation.isAttributeInnerTypeValid(null, new HashMap<>());
        assertNotNull(result);
        assertNull(result.getLeft());
        assertFalse(result.getRight());
    }

    @Test
    void isAttributeDefaultValueValid() {
        final var attributeDefinition = new AttributeDataDefinition();
        attributeDefinition.setType("string");
        final var result = attributeOperation.isAttributeDefaultValueValid(attributeDefinition, new HashMap<>());
        assertTrue(result);
    }

    @Test
    void isAttributeDefaultValueValid_when_list() {
        final var attributeDefinition = new AttributeDataDefinition();
        attributeDefinition.setType("list");
        final SchemaDefinition schema = new SchemaDefinition();
        schema.setProperty(new PropertyDataDefinition());
        attributeDefinition.setSchema(schema);
        final var result = attributeOperation.isAttributeDefaultValueValid(attributeDefinition, new HashMap<>());
        assertTrue(result);
    }

    @Test
    void validateAndUpdateAttributeValue() {
        final var attributeDefinition = new AttributeDataDefinition();
        attributeDefinition.setType("string");
        attributeDefinition.setValue("new string");
        final var result = attributeOperation.validateAndUpdateAttributeValue(attributeDefinition, "", new HashMap<>());
        assertNotNull(result);
    }

    @Test
    void validateAndUpdateAttributeValue_when_type_null() {
        final var attributeDefinition = new AttributeDataDefinition();
        attributeDefinition.setValue("[ 'test' : 123 ]");
        final var result = attributeOperation.validateAndUpdateAttributeValue(attributeDefinition, "", new HashMap<>());
        assertNotNull(result);
        assertTrue(result.isRight());
        assertFalse(result.right().value());
    }

    @Test
    void validateAndUpdateAttribute() {
        final var attributeDefinition = new AttributeDataDefinition();
        attributeDefinition.setType("string");
        final var result = attributeOperation.validateAndUpdateAttribute(attributeDefinition, new HashMap<>());
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
    }

    @Test
    void validateAndUpdateAttribute_without_type() {
        final var attributeDefinition = new AttributeDataDefinition();
        attributeDefinition.setType("double");
        final HashMap<String, DataTypeDefinition> dataTypes = new HashMap<>();
        dataTypes.put("double", new DataTypeDefinition());
        final var result = attributeOperation.validateAndUpdateAttribute(attributeDefinition, dataTypes);
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
    }

}
