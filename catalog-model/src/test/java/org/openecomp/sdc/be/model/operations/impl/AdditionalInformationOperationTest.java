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

import org.janusgraph.core.JanusGraphEdge;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.UserData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class AdditionalInformationOperationTest extends ModelTestBase {
    private static final JanusGraphGenericDao JANUS_GRAPH_GENERIC_DAO = mock(JanusGraphGenericDao.class);
    private static String USER_ID = "muUserId";
    private static String CATEGORY_NAME = "category/mycategory";
    @Mock
    private JanusGraphVertex janusGraphVertex;

    @javax.annotation.Resource(name = "janusgraph-generic-dao")
    private JanusGraphGenericDao janusGraphDao;

    @javax.annotation.Resource(name = "additional-information-operation")
    private IAdditionalInformationOperation additionalInformationOperation;

    @Before
    public void createUserAndCategory() {
        deleteAndCreateCategory(CATEGORY_NAME);
        deleteAndCreateUser(USER_ID, "first_" + USER_ID, "last_" + USER_ID);

    }

    @BeforeClass
    public static void setupBeforeClass() {

        ModelTestBase.init();

    }

    @Test
    public void testDummy() {

        assertNotNull(additionalInformationOperation);

    }

    @Test
    public void testAddInfoParameter_InvalidId(){
        Either<AdditionalInformationDefinition, JanusGraphOperationStatus> result;
        String uid = "uid";
        String componentId = "componentId";
        when(JANUS_GRAPH_GENERIC_DAO.getVertexByProperty(eq(uid),eq(componentId))).thenReturn(Either.left(janusGraphVertex));
        result = additionalInformationOperation.addAdditionalInformationParameter
                (NodeTypeEnum.Resource,componentId,"key","value");
        assertThat(result.isRight());
    }

    @Test
    public void testUpdateInfoParameter_InvalidId(){
        Either<AdditionalInformationDefinition, JanusGraphOperationStatus> result;
        String uid = "uid";
        String componentId = "componentId";
        when(JANUS_GRAPH_GENERIC_DAO.getVertexByProperty(eq(uid),eq(componentId))).thenReturn(Either.left(janusGraphVertex));
        result = additionalInformationOperation.updateAdditionalInformationParameter
                (NodeTypeEnum.Resource,componentId,"id","key","value");
        assertTrue(result.isRight());
    }

    @Test
    public void testDelAdditionalInfoParam_InvalidId() {
        Either<AdditionalInformationDefinition, JanusGraphOperationStatus> result;
        String id = "uid";
        String componentId = "componentId";
        JanusGraph graph = janusGraphDao.getGraph().left().value();
        JanusGraphVertex v1 = graph.addVertex();
        v1.property("uid", componentId);
        v1.property(GraphPropertiesDictionary.LABEL.getProperty(), "resource");
        JanusGraphVertex v2 = graph.addVertex();
        v2.property(id,id);

        JanusGraphEdge addEdge = v1.addEdge(GraphEdgeLabels.ADDITIONAL_INFORMATION.getProperty(), v2);
        addEdge.property("edgeProp", "resource");
        graph.tx().commit();

        result = additionalInformationOperation.deleteAdditionalInformationParameter(NodeTypeEnum.Resource, componentId, id);
        clearGraph();
        assertTrue(result.isRight());
    }

    private void clearGraph() {
        Either<JanusGraph, JanusGraphOperationStatus> graphResult = janusGraphDao.getGraph();
        JanusGraph graph = graphResult.left().value();

        Iterable<JanusGraphVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<JanusGraphVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                JanusGraphVertex vertex = iterator.next();
                vertex.remove();
            }
        }
        janusGraphDao.commit();
    }

    private UserData deleteAndCreateUser(String userId, String firstName, String lastName) {
        UserData userData = new UserData();
        userData.setUserId(userId);
        userData.setFirstName(firstName);
        userData.setLastName(lastName);

        janusGraphDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
        janusGraphDao.createNode(userData, UserData.class);
        janusGraphDao.commit();

        return userData;
    }

    private void deleteAndCreateCategory(String category) {
        String[] names = category.split("/");
        OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], janusGraphDao);
    }

}
