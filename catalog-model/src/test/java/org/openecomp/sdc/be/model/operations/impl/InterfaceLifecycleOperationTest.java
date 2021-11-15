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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ModelTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.InterfaceData;
import org.openecomp.sdc.be.resources.data.ModelData;
import org.openecomp.sdc.be.resources.data.OperationData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = "classpath:application-context-test.xml")
public class InterfaceLifecycleOperationTest {

    private static String USER_ID = "muUserId";
    private static String CATEGORY_NAME = "category/mycategory";
    private static String MODEL_NAME = "Test";
    private static String INTERFACE_TYPE = "tosca.interfaces.standard";

    JanusGraphGenericDao janusGraphGenericDao = Mockito.mock(JanusGraphGenericDao.class);
    @InjectMocks
    private InterfaceLifecycleOperation interfaceLifecycleOperation = new InterfaceLifecycleOperation();

    @BeforeAll
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @BeforeEach
    public void createUserAndCategory() {
        MockitoAnnotations.initMocks(this);
        CategoryData categoryData = new CategoryData(NodeTypeEnum.ResourceCategory);
        when(janusGraphGenericDao.createNode(any(), any())).thenReturn(Either.left(categoryData));
        deleteAndCreateCategory(CATEGORY_NAME);
        deleteAndCreateUser(USER_ID, "first_" + USER_ID, "last_" + USER_ID);
    }

    @Test
    public void testDummy() {
        assertNotNull(interfaceLifecycleOperation);
    }

    private InterfaceDefinition buildInterfaceDefinition() {
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setType(INTERFACE_TYPE);
        interfaceDefinition.setCreationDate(101232L);
        interfaceDefinition.setModel(MODEL_NAME);
        return interfaceDefinition;
    }

    private void deleteAndCreateCategory(String category) {
        String[] names = category.split("/");
        OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], janusGraphGenericDao);
    }

    private UserData deleteAndCreateUser(String userId, String firstName, String lastName) {
        UserData userData = new UserData();
        userData.setUserId(userId);
        userData.setFirstName(firstName);
        userData.setLastName(lastName);

        janusGraphGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId,
            UserData.class);
        janusGraphGenericDao.createNode(userData, UserData.class);
        janusGraphGenericDao.commit();

        return userData;
    }

    @Test
    public void createInterfaceOnResourceTest() {
        when(janusGraphGenericDao.getChildrenNodes(any(), any(), any(), any(), eq(InterfaceData.class))).thenReturn(
            Either.right(JanusGraphOperationStatus.GRAPH_IS_NOT_AVAILABLE));
        when(janusGraphGenericDao.getChild(any(), any(), any(), eq(NodeTypeEnum.Resource), eq(ResourceMetadataData.class))).thenReturn(
            Either.right(JanusGraphOperationStatus.NOT_FOUND));
        when(janusGraphGenericDao.getNode(any(), any(), eq(InterfaceData.class))).thenReturn(Either.right(JanusGraphOperationStatus.ALREADY_EXIST));
        when(janusGraphGenericDao.createNode(any(), eq(InterfaceData.class))).thenReturn(Either.right(JanusGraphOperationStatus.ALREADY_EXIST));
        when(janusGraphGenericDao.createRelation(any(), any(), eq(GraphEdgeLabels.INTERFACE), any())).thenReturn(
            Either.right(JanusGraphOperationStatus.OK));
        Assert.assertTrue(interfaceLifecycleOperation.createInterfaceOnResource(buildInterfaceDefinition(), "", "", false, false).isRight());
        when(janusGraphGenericDao.createRelation(any(), any(), eq(GraphEdgeLabels.INTERFACE), any())).thenReturn(
            Either.left(Mockito.mock(GraphRelation.class)));
        Assert.assertTrue(interfaceLifecycleOperation.createInterfaceOnResource(buildInterfaceDefinition(), "", "", false, false).isRight());
    }

    @Test
    public void getAllInterfacesOfResourceTest() {
        assertTrue(interfaceLifecycleOperation.getAllInterfacesOfResource(null, true).isRight());
        when(janusGraphGenericDao.getChildrenNodes(any(), any(), any(), any(), eq(InterfaceData.class))).thenReturn(
            Either.right(JanusGraphOperationStatus.GRAPH_IS_NOT_AVAILABLE));
        when(janusGraphGenericDao.getChild(any(), any(), any(), eq(NodeTypeEnum.Resource), eq(ResourceMetadataData.class))).thenReturn(
            Either.right(JanusGraphOperationStatus.NOT_FOUND));
        assertTrue(interfaceLifecycleOperation.getAllInterfacesOfResource("null", true).isLeft());

        when(janusGraphGenericDao.getChild(any(), any(), any(), eq(NodeTypeEnum.Resource), eq(ResourceMetadataData.class))).thenReturn(
            Either.right(JanusGraphOperationStatus.ALREADY_EXIST));
        assertTrue(interfaceLifecycleOperation.getAllInterfacesOfResource("null", true).isRight());

        ResourceMetadataData key = Mockito.mock(ResourceMetadataData.class);
        ComponentMetadataDataDefinition def = Mockito.mock(ComponentMetadataDataDefinition.class);
        when(def.getUniqueId()).thenReturn("UNIIIIIIQUE IDDDD");
        when(key.getMetadataDataDefinition()).thenReturn(def);
        ImmutablePair<ResourceMetadataData, GraphEdge> pair = new ImmutablePair<>(key, Mockito.mock(GraphEdge.class));
        when(janusGraphGenericDao.getChild(any(), any(), any(), eq(NodeTypeEnum.Resource), eq(ResourceMetadataData.class)))
            .thenReturn(Either.left(pair))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        assertTrue(interfaceLifecycleOperation.getAllInterfacesOfResource("null", true).isLeft());
    }

    @Test
    public void testGetAllInterfaceLifecycleTypes_TypesNotFound() {
        when(janusGraphGenericDao.getByCriteriaForModel(NodeTypeEnum.Interface, Collections.emptyMap(), StringUtils.EMPTY,
            InterfaceData.class)).thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> types = interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(
            StringUtils.EMPTY);
        Assert.assertEquals(types.isRight(), Boolean.TRUE);
    }

    @Test
    public void testGetAllInterfaceLifecycleTypes_Success() {
        final String UNIQUE_ID = "UNIQUE_ID";
        final String TYPE = "UNIQUE_ID";
        InterfaceData interfaceData = new InterfaceData();
        interfaceData.getInterfaceDataDefinition().setUniqueId(UNIQUE_ID);
        interfaceData.getInterfaceDataDefinition().setType(TYPE);
        List<InterfaceData> interfaceDataList = new ArrayList<>();
        interfaceDataList.add(interfaceData);
        Either<List<InterfaceData>, JanusGraphOperationStatus> allInterfaceTypes = Either.left(interfaceDataList);
        when(janusGraphGenericDao
            .getByCriteriaForModel(NodeTypeEnum.Interface, Collections.emptyMap(), StringUtils.EMPTY, InterfaceData.class)).thenReturn(
            allInterfaceTypes);

        List<ImmutablePair<OperationData, GraphEdge>> list = new ArrayList<>();
        Either<List<ImmutablePair<OperationData, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = Either.left(list);
        when(janusGraphGenericDao.getChildrenNodes(interfaceData.getUniqueIdKey(), interfaceData.getUniqueId(), GraphEdgeLabels.INTERFACE_OPERATION,
            NodeTypeEnum.InterfaceOperation, OperationData.class)).thenReturn(childrenNodes);
        when(janusGraphGenericDao.getParentNode(any(), any(), any(), any(), any()))
            .thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> types = interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(
            StringUtils.EMPTY);
        Assert.assertEquals(types.left().value().size(), 1);
    }

    @Test
    public void testGetAllInterfaceLifecycleTypesWithModel() {
        final var uid = UniqueIdBuilder.buildInterfaceTypeUid(MODEL_NAME, INTERFACE_TYPE);
        final var modelData = new ModelData(MODEL_NAME, uid, ModelTypeEnum.NORMATIVE);
        final ImmutablePair<GraphNode, GraphEdge> modelNode = new ImmutablePair<>(modelData, Mockito.mock(GraphEdge.class));

        final InterfaceData interfaceData = new InterfaceData();
        interfaceData.getInterfaceDataDefinition().setUniqueId(uid);
        interfaceData.getInterfaceDataDefinition().setType(INTERFACE_TYPE);

        final List<InterfaceData> interfaceTypes = new ArrayList<InterfaceData>();
        interfaceTypes.add(interfaceData);

        when(janusGraphGenericDao.getParentNode(any(), any(), any(), any(), any()))
            .thenReturn(Either.left(modelNode));
        when(janusGraphGenericDao
            .getByCriteriaForModel(NodeTypeEnum.Interface, Collections.emptyMap(), MODEL_NAME, InterfaceData.class)).thenReturn(
            Either.left(interfaceTypes));
        when(janusGraphGenericDao.getChildrenNodes(interfaceData.getUniqueIdKey(), interfaceData.getUniqueId(), GraphEdgeLabels.INTERFACE_OPERATION,
            NodeTypeEnum.InterfaceOperation, OperationData.class)).thenReturn(Either.left(Collections.emptyList()));

        Assert.assertEquals(1, interfaceLifecycleOperation.getAllInterfaceLifecycleTypes(MODEL_NAME).left().value().size());
    }

}
