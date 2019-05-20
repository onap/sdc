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

import org.janusgraph.core.*;
import org.janusgraph.graphdb.relations.StandardVertexProperty;
import org.janusgraph.graphdb.types.system.EmptyVertex;
import org.janusgraph.graphdb.types.system.ImplicitKey;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.api.UserRoleEnum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserAdminOperationTest extends ModelTestBase {
    private static final JanusGraphGenericDao JANUS_GRAPH_GENERIC_DAO = mock(JanusGraphGenericDao.class);
    @InjectMocks
    private static final UserAdminOperation userAdminOperation = new UserAdminOperation(
        JANUS_GRAPH_GENERIC_DAO);
    private static final String ADMIN = "admin";

    @BeforeClass
    public static void setup() {
        ModelTestBase.init();
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        Mockito.reset(JANUS_GRAPH_GENERIC_DAO);
        mockJanusGraphUpdate();
        mockJanusGraphDelete();

    }

    @Test
    public void testDeActivateUserDataSuccess() {
        UserData userData = mockJanusGraphGet(ADMIN, UserRoleEnum.ADMIN, true);

        Either<User, StorageOperationStatus> eitherUser = userAdminOperation.deActivateUser(userAdminOperation.convertToUser(userData));

        verify(JANUS_GRAPH_GENERIC_DAO, times(1)).updateNode(eq(userData), eq(UserData.class));
        verify(JANUS_GRAPH_GENERIC_DAO, times(0)).deleteNode(any(UserData.class), eq(UserData.class));
        assertTrue(eitherUser.isLeft());
        User user = eitherUser.left().value();
        assertSame(user.getStatus(), UserStatusEnum.INACTIVE);
    }

    @Test
    public void testDeleteUserWithoutResources() {
        UserData userData = mockJanusGraphGet(ADMIN, UserRoleEnum.ADMIN, true);

        List<Edge> edgesList = new ArrayList<>();

        Either<List<Edge>, JanusGraphOperationStatus> eitherResult = Either.left(edgesList);
        when(JANUS_GRAPH_GENERIC_DAO.getEdgesForNode(userData, Direction.BOTH)).thenReturn(eitherResult);

        Either<User, ActionStatus> eitherUser = userAdminOperation.deleteUserData(ADMIN);
        verify(JANUS_GRAPH_GENERIC_DAO, times(0)).updateNode(any(UserData.class), eq(UserData.class));
        verify(JANUS_GRAPH_GENERIC_DAO, times(1)).deleteNode(userData, UserData.class);
        assertTrue(eitherUser.isLeft());

    }

    @Test
    public void testDeleteUserWithResources() {
        UserData userData = mockJanusGraphGet(ADMIN, UserRoleEnum.ADMIN, true);

        List<Edge> edgesList = new ArrayList<>();
        edgesList.add(getEmptyEdgeImpl());

        Either<List<Edge>, JanusGraphOperationStatus> eitherResult = Either.left(edgesList);
        when(JANUS_GRAPH_GENERIC_DAO.getEdgesForNode(userData, Direction.BOTH)).thenReturn(eitherResult);

        Either<User, ActionStatus> eitherUser = userAdminOperation.deleteUserData(ADMIN);
        verify(JANUS_GRAPH_GENERIC_DAO, times(0)).updateNode(any(UserData.class), eq(UserData.class));
        verify(JANUS_GRAPH_GENERIC_DAO, times(0)).deleteNode(any(UserData.class), eq(UserData.class));
        assertTrue(eitherUser.isRight());
        assertSame(eitherUser.right().value(), ActionStatus.USER_HAS_ACTIVE_ELEMENTS);

    }

    @Test
    public void getUserPendingTasks_shouldReturnNonDeleted() {
        String userId = "abc123";
        String userKey = UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User);
        User user = new User();
        user.setUserId(userId);
        JanusGraphVertex userVertex = null;
        TestVertex component1 = new TestVertex(null);
        TestVertex component2 = new TestVertex(true);
        TestVertex component3 = new TestVertex(false);
        List<Edge> edges = new ArrayList<>();
        Edge edge1 = new TestEdge(component1, "1");
        Edge edge2 = new TestEdge(component2, "2");
        Edge edge3 = new TestEdge(component3, "3");
        edges.add(edge1);
        edges.add(edge2);
        edges.add(edge3);
        when(JANUS_GRAPH_GENERIC_DAO.getVertexByProperty(userKey, userId)).thenReturn(Either.left(userVertex));
        when(JANUS_GRAPH_GENERIC_DAO.getOutgoingEdgesByCriteria(any(), any(), any())).thenReturn(Either.left(edges));
        Either<List<Edge>, StorageOperationStatus> result = userAdminOperation.getUserPendingTasksList(user, new HashMap<>());
        assertThat(result.isLeft()).isTrue();
        List<Edge> pendingTasks = result.left().value();
        assertThat(pendingTasks.size()).isEqualTo(2);
        assertThat(((TestEdge)pendingTasks.get(0)).getName()).isNotEqualTo("2");
        assertThat(((TestEdge)pendingTasks.get(1)).getName()).isNotEqualTo("2");
    }

    private class TestVertex implements JanusGraphVertex {

        private final Boolean isDeleted;

        private TestVertex(Boolean isDeleted) {
            this.isDeleted = isDeleted;
        }

        @Override
        public JanusGraphEdge addEdge(String s, Vertex vertex, Object... objects) {
            return null;
        }

        @Override
        public <V> JanusGraphVertexProperty<V> property(String s, V v, Object... objects) {
            return null;
        }

        @Override
        public <V> VertexProperty<V> property(String key) {
            if (key.equals(GraphPropertiesDictionary.IS_DELETED.getProperty())) {
                if (isDeleted==null)
                    return VertexProperty.empty();
                return new StandardVertexProperty(1, ImplicitKey.ID, new EmptyVertex(), isDeleted, (byte)1);
            }
            return VertexProperty.empty();
        }

        @Override
        public <V> JanusGraphVertexProperty<V> property(VertexProperty.Cardinality cardinality, String s, V v, Object... objects) {
            return null;
        }

        @Override
        public Iterator<Edge> edges(Direction direction, String... strings) {
            return null;
        }

        @Override
        public Iterator<Vertex> vertices(Direction direction, String... strings) {
            return null;
        }

        @Override
        public Object id() {
            return null;
        }

        @Override
        public long longId() {
            return 0;
        }

        @Override
        public boolean hasId() {
            return false;
        }

        @Override
        public String label() {
            return null;
        }

        @Override
        public VertexLabel vertexLabel() {
            return null;
        }

        @Override
        public JanusGraphVertexQuery<? extends JanusGraphVertexQuery> query() {
            return null;
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public JanusGraphTransaction graph() {
            return null;
        }

        @Override
        public void remove() {

        }

        @Override
        public <V> V valueOrNull(PropertyKey propertyKey) {
            return null;
        }

        @Override
        public boolean isNew() {
            return false;
        }

        @Override
        public boolean isLoaded() {
            return false;
        }

        @Override
        public boolean isRemoved() {
            return false;
        }

        @Override
        public <V> Iterator<VertexProperty<V>> properties(String... strings) {
            return null;
        }
    }

    private class TestEdge implements Edge {

        private final Vertex inVertx;
        private final String name;

        TestEdge(Vertex inVertx, String name) {
            this.inVertx = inVertx;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public Iterator<Vertex> vertices(Direction direction) {
            return null;
        }

        @Override
        public Vertex inVertex() {
            return inVertx;
        }

        @Override
        public Object id() {
            return null;
        }

        @Override
        public String label() {
            return null;
        }

        @Override
        public Graph graph() {
            return null;
        }

        @Override
        public <V> Property<V> property(String s, V v) {
            return null;
        }

        @Override
        public void remove() {

        }

        @Override
        public <V> Iterator<Property<V>> properties(String... strings) {
            return null;
        }
    }

    private Edge getEmptyEdgeImpl() {
        return new Edge() {

            @Override
            public Object id() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String label() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Graph graph() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <V> Property<V> property(String key, V value) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void remove() {
                // TODO Auto-generated method stub

            }

            @Override
            public Iterator<Vertex> vertices(Direction direction) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <V> Iterator<Property<V>> properties(String... propertyKeys) {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }

    private UserData mockJanusGraphGet(String userId, UserRoleEnum role, boolean isActive) {
        UserData userData = buildUserData(userId, role, isActive);
        Either<UserData, JanusGraphOperationStatus> eitherUserData = Either.left(userData);
        when(JANUS_GRAPH_GENERIC_DAO
            .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class)).thenReturn(eitherUserData);
        return userData;
    }

    private static void mockJanusGraphUpdate() {
        doAnswer((Answer<Either<UserData, JanusGraphOperationStatus>>) invocation -> {
            Object[] args = invocation.getArguments();
            UserData retValue = (UserData) args[0];
            return Either.left(retValue);
        }).when(JANUS_GRAPH_GENERIC_DAO).updateNode(any(UserData.class), eq(UserData.class));
    }

    private static void mockJanusGraphDelete() {
        doAnswer((Answer<Either<UserData, JanusGraphOperationStatus>>) invocation -> {
            Object[] args = invocation.getArguments();
            UserData retValue = (UserData) args[0];
            return Either.left(retValue);
        }).when(JANUS_GRAPH_GENERIC_DAO).deleteNode(any(UserData.class), eq(UserData.class));
    }

    private static UserData buildUserData(String userId, UserRoleEnum role, boolean isActive) {
        UserData userData = new UserData();
        userData.setUserId(userId);
        userData.setRole(role.getName());
        userData.setEmail("someEmail@somePlace.com");
        userData.setFirstName("israel");
        userData.setLastName("israeli");
        userData.setLastLoginTime(Instant.MIN.getEpochSecond());
        userData.setStatus(isActive ? UserStatusEnum.ACTIVE.name() : UserStatusEnum.INACTIVE.name());
        return userData;
    }
}
