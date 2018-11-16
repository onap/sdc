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

import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
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
    private static final TitanGenericDao titanGenericDao = mock(TitanGenericDao.class);
    private static String USER_ID = "muUserId";
    private static String CATEGORY_NAME = "category/mycategory";
    @Mock
    private TitanVertex titanVertex;

    @javax.annotation.Resource(name = "titan-generic-dao")
    private TitanGenericDao titanDao;

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
        Either<AdditionalInformationDefinition, TitanOperationStatus> result;
        String uid = "uid";
        String componentId = "componentId";
        when(titanGenericDao.getVertexByProperty(eq(uid),eq(componentId))).thenReturn(Either.left(titanVertex));
        result = additionalInformationOperation.addAdditionalInformationParameter
                (NodeTypeEnum.Resource,componentId,"key","value");
        assertThat(result.isRight());
    }

    @Test
    public void testUpdateInfoParameter_InvalidId(){
        Either<AdditionalInformationDefinition, TitanOperationStatus> result;
        String uid = "uid";
        String componentId = "componentId";
        when(titanGenericDao.getVertexByProperty(eq(uid),eq(componentId))).thenReturn(Either.left(titanVertex));
        result = additionalInformationOperation.updateAdditionalInformationParameter
                (NodeTypeEnum.Resource,componentId,"id","key","value");
        assertTrue(result.isRight());
    }

    @Test
    public void testDelAdditionalInfoParam_InvalidId() {
        Either<AdditionalInformationDefinition, TitanOperationStatus> result;
        String id = "uid";
        String componentId = "componentId";
        TitanGraph graph = titanDao.getGraph().left().value();
        TitanVertex v1 = graph.addVertex();
        v1.property("uid", componentId);
        v1.property(GraphPropertiesDictionary.LABEL.getProperty(), "resource");
        TitanVertex v2 = graph.addVertex();
        v2.property(id,id);

        TitanEdge addEdge = v1.addEdge(GraphEdgeLabels.ADDITIONAL_INFORMATION.getProperty(), v2);
        addEdge.property("edgeProp", "resource");
        graph.tx().commit();

        result = additionalInformationOperation.deleteAdditionalInformationParameter(NodeTypeEnum.Resource, componentId, id);
        clearGraph();
        assertTrue(result.isRight());
    }

    private void clearGraph() {
        Either<TitanGraph, TitanOperationStatus> graphResult = titanDao.getGraph();
        TitanGraph graph = graphResult.left().value();

        Iterable<TitanVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<TitanVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                TitanVertex vertex = iterator.next();
                vertex.remove();
            }
        }
        titanDao.commit();
    }

    private UserData deleteAndCreateUser(String userId, String firstName, String lastName) {
        UserData userData = new UserData();
        userData.setUserId(userId);
        userData.setFirstName(firstName);
        userData.setLastName(lastName);

        titanDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
        titanDao.createNode(userData, UserData.class);
        titanDao.commit();

        return userData;
    }

    private void deleteAndCreateCategory(String category) {
        String[] names = category.split("/");
        OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], titanDao);
    }

}
