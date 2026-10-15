/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = "classpath:application-context-test.xml")
class ToscaElementOperationFollowedTest extends ModelTestBase {

    private static final Set<LifecycleStateEnum> STATES = EnumSet.of(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
        LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, LifecycleStateEnum.CERTIFIED);
    private static final Set<LifecycleStateEnum> LAST_STATES = EnumSet.of(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
    private static final String CHECKIN = LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.name();
    private static final String CHECKOUT = LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name();
    private static final String CERTIFIED = LifecycleStateEnum.CERTIFIED.name();

    @javax.annotation.Resource
    private JanusGraphDao janusGraphDao;
    @javax.annotation.Resource
    private NodeTypeOperation nodeTypeOperation;
    @javax.annotation.Resource
    private TopologyTemplateOperation topologyTemplateOperation;

    private GraphVertex follower;
    private GraphVertex otherUser;
    private final Map<String, String> expectedById = new LinkedHashMap<>();

    @BeforeAll
    static void initModel() {
        ModelTestBase.init();
    }

    @BeforeEach
    void buildGraph() {
        clearGraph();
        follower = createUser("follower", "Jane", "Doe");
        otherUser = createUser("other", "Max", null);
        GraphVertex networkL4 = createResourceCategory("Network L4+", "Common Network Resources");
        GraphVertex generic = createResourceCategory("Generic", "Infrastructure");
        GraphVertex mobility = createServiceCategory("Mobility");
        GraphVertex e2e = createServiceCategory("E2E Service");

        GraphVertex vfc = createComponent("vfc", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, networkL4, Map.of());
        stateEdge(follower, vfc, EdgeLabelEnum.STATE, CHECKOUT);
        stateEdge(follower, vfc, EdgeLabelEnum.LAST_STATE, CHECKIN);
        expect(vfc, "follower|Jane Doe|other|Max |Network L4+/Common Network Resources");

        GraphVertex vf = createComponent("vf", VertexTypeEnum.TOPOLOGY_TEMPLATE, ComponentTypeEnum.RESOURCE, generic, Map.of(),
            otherUser, follower);
        stateEdge(follower, vf, EdgeLabelEnum.STATE, CHECKIN);
        expect(vf, "other|Max |follower|Jane Doe|Generic/Infrastructure");

        GraphVertex certifiedVfc = createComponent("certifiedVfc", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, networkL4,
            Map.of());
        stateEdge(follower, certifiedVfc, EdgeLabelEnum.STATE, CERTIFIED);
        expect(certifiedVfc, "follower|Jane Doe|other|Max |Network L4+/Common Network Resources");

        GraphVertex deleted = createComponent("deleted", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, networkL4,
            Map.of(GraphPropertyEnum.IS_DELETED, true));
        stateEdge(follower, deleted, EdgeLabelEnum.STATE, CHECKOUT);

        GraphVertex archived = createComponent("archived", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, networkL4,
            Map.of(GraphPropertyEnum.IS_ARCHIVED, true));
        stateEdge(follower, archived, EdgeLabelEnum.STATE, CHECKOUT);

        GraphVertex notHighest = createComponent("notHighest", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, networkL4,
            Map.of(GraphPropertyEnum.IS_HIGHEST_VERSION, false));
        stateEdge(follower, notHighest, EdgeLabelEnum.STATE, CHECKIN);

        GraphVertex abstractVfc = createComponent("abstract", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, networkL4,
            Map.of(GraphPropertyEnum.IS_ABSTRACT, true));
        stateEdge(follower, abstractVfc, EdgeLabelEnum.STATE, CHECKIN);

        GraphVertex unknownState = createComponent("unknownState", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, networkL4,
            Map.of());
        stateEdge(follower, unknownState, EdgeLabelEnum.STATE, "READY_FOR_CERTIFICATION");

        GraphVertex noCreator = createComponent("noCreator", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, networkL4, Map.of(),
            null, otherUser);
        stateEdge(follower, noCreator, EdgeLabelEnum.STATE, CHECKOUT);

        GraphVertex takenOver = createComponent("takenOver", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, generic, Map.of());
        stateEdge(otherUser, takenOver, EdgeLabelEnum.STATE, CHECKOUT);
        stateEdge(follower, takenOver, EdgeLabelEnum.LAST_STATE, CHECKIN);
        expect(takenOver, "follower|Jane Doe|other|Max |Generic/Infrastructure");

        GraphVertex certifiedByOther = createComponent("certifiedByOther", VertexTypeEnum.NODE_TYPE, ComponentTypeEnum.RESOURCE, generic,
            Map.of());
        stateEdge(otherUser, certifiedByOther, EdgeLabelEnum.STATE, CHECKOUT);
        stateEdge(follower, certifiedByOther, EdgeLabelEnum.LAST_STATE, CERTIFIED);

        GraphVertex service = createComponent("service", VertexTypeEnum.TOPOLOGY_TEMPLATE, ComponentTypeEnum.SERVICE, mobility,
            Map.of());
        stateEdge(follower, service, EdgeLabelEnum.STATE, CERTIFIED);
        expect(service, "follower|Jane Doe|other|Max |Mobility");

        GraphVertex otherService = createComponent("otherService", VertexTypeEnum.TOPOLOGY_TEMPLATE, ComponentTypeEnum.SERVICE, e2e,
            Map.of());
        stateEdge(otherUser, otherService, EdgeLabelEnum.STATE, CHECKOUT);
        stateEdge(follower, otherService, EdgeLabelEnum.LAST_STATE, CHECKIN);
        expect(otherService, "follower|Jane Doe|other|Max |E2E Service");

        GraphVertex deletedService = createComponent("deletedService", VertexTypeEnum.TOPOLOGY_TEMPLATE, ComponentTypeEnum.SERVICE,
            mobility, Map.of(GraphPropertyEnum.IS_DELETED, true));
        stateEdge(follower, deletedService, EdgeLabelEnum.STATE, CHECKIN);

        GraphVertex otherUsersService = createComponent("otherUsersService", VertexTypeEnum.TOPOLOGY_TEMPLATE,
            ComponentTypeEnum.SERVICE, mobility, Map.of());
        stateEdge(otherUser, otherUsersService, EdgeLabelEnum.STATE, CHECKIN);

        assertEquals(JanusGraphOperationStatus.OK, janusGraphDao.commit());
    }

    @AfterEach
    void cleanUp() {
        clearGraph();
    }

    @Test
    void followedResources() {
        Either<List<ToscaElement>, StorageOperationStatus> result = nodeTypeOperation
            .getFollowedComponent("follower", STATES, LAST_STATES, ComponentTypeEnum.RESOURCE);

        assertTrue(result.isLeft());
        assertEquals(expectedInTraversalOrder("vfc-id", "vf-id", "certifiedVfc-id", "takenOver-id"), describe(result.left().value()));
    }

    @Test
    void followedServices() {
        Either<List<ToscaElement>, StorageOperationStatus> result = topologyTemplateOperation
            .getFollowedComponent("follower", STATES, LAST_STATES, ComponentTypeEnum.SERVICE);

        assertTrue(result.isLeft());
        assertEquals(expectedInTraversalOrder("service-id", "otherService-id"), describe(result.left().value()));
    }

    @Test
    void lastStateIsIgnoredWithoutLastStateStates() {
        Either<List<ToscaElement>, StorageOperationStatus> result = nodeTypeOperation
            .getFollowedComponent("follower", STATES, EnumSet.noneOf(LifecycleStateEnum.class), ComponentTypeEnum.RESOURCE);

        assertTrue(result.isLeft());
        assertEquals(expectedInTraversalOrder("vfc-id", "vf-id", "certifiedVfc-id"), describe(result.left().value()));
    }

    @Test
    void userWithoutStateEdgesFollowsNothing() {
        createUser("idle", "Idle", "User");
        janusGraphDao.commit();

        Either<List<ToscaElement>, StorageOperationStatus> result = nodeTypeOperation
            .getFollowedComponent("idle", STATES, LAST_STATES, ComponentTypeEnum.RESOURCE);

        assertTrue(result.isLeft());
        assertTrue(result.left().value().isEmpty());
    }

    /**
     * The followed list keeps the order in which the user's STATE edges are traversed, followed by the LAST_STATE-only components.
     */
    private List<String> expectedInTraversalOrder(String... ids) {
        Set<String> wanted = Set.of(ids);
        List<String> ordered = new ArrayList<>();
        for (EdgeLabelEnum label : List.of(EdgeLabelEnum.STATE, EdgeLabelEnum.LAST_STATE)) {
            janusGraphDao.getChildrenVertices(follower, label, JsonParseFlagEnum.NoParse).left().value().stream()
                .map(GraphVertex::getUniqueId)
                .filter(wanted::contains)
                .filter(id -> !ordered.contains(id))
                .forEach(ordered::add);
        }
        janusGraphDao.rollback();
        assertEquals(wanted.size(), ordered.size());
        return ordered.stream().map(id -> id + "|" + expectedById.get(id)).collect(Collectors.toList());
    }

    private List<String> describe(List<ToscaElement> elements) {
        return elements.stream().map(this::describe).collect(Collectors.toList());
    }

    private String describe(ToscaElement element) {
        CategoryDefinition category = element.getCategories().get(0);
        String categoryText = category.getName();
        if (category.getSubcategories() != null) {
            categoryText += "/" + category.getSubcategories().get(0).getName();
        }
        assertEquals(element.getUniqueId(), element.getMetadataValue(JsonPresentationFields.UNIQUE_ID));
        return String.join("|", element.getUniqueId(), element.getCreatorUserId(), element.getCreatorFullName(),
            element.getLastUpdaterUserId(), element.getLastUpdaterFullName(), categoryText);
    }

    private void expect(GraphVertex component, String description) {
        expectedById.put(component.getUniqueId(), description);
    }

    private GraphVertex createUser(String userId, String firstName, String lastName) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.USERID, userId);
        props.put(GraphPropertyEnum.FIRST_NAME, firstName);
        if (lastName != null) {
            props.put(GraphPropertyEnum.LAST_NAME, lastName);
        }
        return createVertex(VertexTypeEnum.USER, userId, props);
    }

    private GraphVertex createResourceCategory(String name, String subCategoryName) {
        GraphVertex category = createVertex(VertexTypeEnum.RESOURCE_CATEGORY, "resourceCategory." + name, categoryProps(name));
        GraphVertex subCategory = createVertex(VertexTypeEnum.RESOURCE_SUBCATEGORY, "resourceSubCategory." + subCategoryName,
            categoryProps(subCategoryName));
        createEdge(category, subCategory, EdgeLabelEnum.SUB_CATEGORY, new HashMap<>());
        return subCategory;
    }

    private GraphVertex createServiceCategory(String name) {
        return createVertex(VertexTypeEnum.SERVICE_CATEGORY, "serviceCategory." + name, categoryProps(name));
    }

    private Map<GraphPropertyEnum, Object> categoryProps(String name) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.NAME, name);
        props.put(GraphPropertyEnum.NORMALIZED_NAME, name.toLowerCase());
        return props;
    }

    private GraphVertex createComponent(String name, VertexTypeEnum label, ComponentTypeEnum type, GraphVertex category,
                                        Map<GraphPropertyEnum, Object> overrides) {
        return createComponent(name, label, type, category, overrides, follower, otherUser);
    }

    private GraphVertex createComponent(String name, VertexTypeEnum label, ComponentTypeEnum type, GraphVertex category,
                                        Map<GraphPropertyEnum, Object> overrides, GraphVertex creator, GraphVertex lastModifier) {
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.COMPONENT_TYPE, type.name());
        props.put(GraphPropertyEnum.NAME, name);
        props.put(GraphPropertyEnum.VERSION, "0.1");
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        if (type == ComponentTypeEnum.RESOURCE) {
            props.put(GraphPropertyEnum.IS_ABSTRACT, false);
        }
        props.putAll(overrides);
        GraphVertex component = createVertex(label, name + "-id", props);
        if (creator != null) {
            createEdge(creator, component, EdgeLabelEnum.CREATOR, new HashMap<>());
        }
        createEdge(lastModifier, component, EdgeLabelEnum.LAST_MODIFIER, new HashMap<>());
        createEdge(component, category, EdgeLabelEnum.CATEGORY, new HashMap<>());
        return component;
    }

    private void stateEdge(GraphVertex user, GraphVertex component, EdgeLabelEnum label, String state) {
        Map<EdgePropertyEnum, Object> props = new EnumMap<>(EdgePropertyEnum.class);
        props.put(EdgePropertyEnum.STATE, state);
        createEdge(user, component, label, props);
    }

    private GraphVertex createVertex(VertexTypeEnum label, String uniqueId, Map<GraphPropertyEnum, Object> props) {
        GraphVertex vertex = new GraphVertex(label);
        vertex.setUniqueId(uniqueId);
        Map<GraphPropertyEnum, Object> metadata = new EnumMap<>(props);
        metadata.put(GraphPropertyEnum.UNIQUE_ID, uniqueId);
        metadata.put(GraphPropertyEnum.LABEL, label.getName());
        vertex.setMetadataProperties(metadata);
        vertex.updateMetadataJsonWithCurrentMetadataProperties();
        Either<GraphVertex, JanusGraphOperationStatus> created = janusGraphDao.createVertex(vertex);
        assertTrue(created.isLeft());
        return created.left().value();
    }

    private void createEdge(GraphVertex from, GraphVertex to, EdgeLabelEnum label, Map<EdgePropertyEnum, Object> props) {
        assertEquals(JanusGraphOperationStatus.OK, janusGraphDao.createEdge(from, to, label, props));
    }

    private void clearGraph() {
        JanusGraph graph = janusGraphDao.getJanusGraphClient().getGraph().left().value();
        Iterator<JanusGraphVertex> vertices = graph.query().vertices().iterator();
        while (vertices.hasNext()) {
            vertices.next().remove();
        }
        janusGraphDao.commit();
    }
}
