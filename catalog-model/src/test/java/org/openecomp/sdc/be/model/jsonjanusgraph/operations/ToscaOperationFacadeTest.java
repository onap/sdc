/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Collections;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class ToscaOperationFacadeTest {
    private static final String COMPONENT_ID = "componentId";
    private static final String PROPERTY1_NAME = "prop1";
    private static final String PROPERTY1_TYPE = "string";
    private static final String PROPERTY2_NAME = "prop2";
    private static final String PROPERTY2_TYPE = "integer";

    @InjectMocks
    private ToscaOperationFacade testInstance;

    @Mock
    private HealingJanusGraphDao janusGraphDaoMock;

    @Mock
    private TopologyTemplateOperation topologyTemplateOperationMock;

    @Mock
    private NodeTypeOperation nodeTypeOperation;

    @Mock
    private NodeTemplateOperation nodeTemplateOperationMock;

    @Before
    public void setUp() throws Exception {
        testInstance = new ToscaOperationFacade();
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchMetaDataByResourceType() throws Exception {
        ArgumentCaptor<Map> criteriaCapture = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> criteriaNotCapture = ArgumentCaptor.forClass(Map.class);
        ComponentParametersView dataFilter = new ComponentParametersView();
        List<GraphVertex> mockVertices = getMockVertices(2);
        Either<List<GraphVertex>, JanusGraphOperationStatus> returnedVertices = Either.left(mockVertices);

        when(janusGraphDaoMock.getByCriteria(eq(null), criteriaCapture.capture(), criteriaNotCapture.capture(), eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(returnedVertices);
        when(topologyTemplateOperationMock.getToscaElement(mockVertices.get(0), dataFilter)).thenReturn(Either.left(getResourceToscaElement("0")));
        when(topologyTemplateOperationMock.getToscaElement(mockVertices.get(1), dataFilter)).thenReturn(Either.left(getResourceToscaElement("1")));
        Either<List<Component>, StorageOperationStatus> fetchedComponents = testInstance.fetchMetaDataByResourceType(ResourceTypeEnum.VF.getValue(), dataFilter);

        verifyCriteriaForHighestVersionAndVfResourceType(criteriaCapture);
        verifyCriteriaNotIsDeleted(criteriaNotCapture);

        assertTrue(fetchedComponents.isLeft());
        List<Component> cmpts = fetchedComponents.left().value();
        assertEquals(2, cmpts.size());
        assertEquals("0", cmpts.get(0).getUniqueId());
        assertEquals("1", cmpts.get(1).getUniqueId());
    }

    private void verifyCriteriaForHighestVersionAndVfResourceType(ArgumentCaptor<Map> criteriaCapture) {
        Map<GraphPropertyEnum, Object> criteria = (Map<GraphPropertyEnum, Object>)criteriaCapture.getValue();
        assertEquals(2, criteria.size());
        assertEquals(criteria.get(GraphPropertyEnum.RESOURCE_TYPE), "VF");
        assertEquals(criteria.get(GraphPropertyEnum.IS_HIGHEST_VERSION), true);
    }

    private void verifyCriteriaNotIsDeleted(ArgumentCaptor<Map> criteriaNotCapture) {
        Map<GraphPropertyEnum, Object> notCriteria = (Map<GraphPropertyEnum, Object>)criteriaNotCapture.getValue();
        assertEquals(1, notCriteria.size());
        assertEquals(notCriteria.get(GraphPropertyEnum.IS_DELETED), true);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchMetaDataByResourceType_failedToGetData() throws Exception {
        when(janusGraphDaoMock.getByCriteria(eq(null), anyMap(), anyMap(), eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(Either.right(
            JanusGraphOperationStatus.GENERAL_ERROR));
        Either<List<Component>, StorageOperationStatus> fetchedComponents = testInstance.fetchMetaDataByResourceType(ResourceTypeEnum.VF.getValue(), new ComponentParametersView());
        assertTrue(fetchedComponents.isRight());
        assertEquals(StorageOperationStatus.GENERAL_ERROR, fetchedComponents.right().value());
    }

    @Test
    public void associatePolicyToComponentSuccessTest(){
        Either<PolicyDefinition, StorageOperationStatus> result = associatePolicyToComponentWithStatus(StorageOperationStatus.OK);
        assertTrue(result.isLeft());
    }

    @Test
    public void associatePolicyToComponentFailureTest(){
        Either<PolicyDefinition, StorageOperationStatus> result = associatePolicyToComponentWithStatus(StorageOperationStatus.BAD_REQUEST);
        assertTrue(result.isRight() && result.right().value() == StorageOperationStatus.BAD_REQUEST);
    }

    @Test
    public void updatePolicyOfComponentSuccessTest(){
        Either<PolicyDefinition, StorageOperationStatus> result = updatePolicyOfComponentWithStatus(StorageOperationStatus.OK);
        assertTrue(result.isLeft());
    }

    @Test
    public void updatePolicyOfComponentFailureTest(){
        Either<PolicyDefinition, StorageOperationStatus> result = updatePolicyOfComponentWithStatus(StorageOperationStatus.NOT_FOUND);
        assertTrue(result.isRight() && result.right().value() == StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void removePolicyFromComponentSuccessTest(){
        removePolicyFromComponentWithStatus(StorageOperationStatus.OK);
    }

    @Test
    public void removePolicyFromComponentFailureTest(){
        removePolicyFromComponentWithStatus(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void testFindLastCertifiedToscaElementByUUID(){
        Either<Component, StorageOperationStatus> result;
        Component component = new Resource();
        List<GraphVertex> list = new ArrayList<>();
        GraphVertex graphVertex = getTopologyTemplateVertex();
        list.add(graphVertex);
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.UUID, component.getUUID());
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        ToscaElement toscaElement = getToscaElementForTest();
        when(topologyTemplateOperationMock.getToscaElement(ArgumentMatchers.eq(graphVertex),any(ComponentParametersView.class))).thenReturn(Either.left(toscaElement));
        when(janusGraphDaoMock.getByCriteria(ModelConverter.getVertexType(component), props)).thenReturn(Either.left(list));
        result = testInstance.findLastCertifiedToscaElementByUUID(component);
        Component resultComp = result.left().value();
        assertEquals(resultComp.getToscaType(),ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());
    }

    @Test
    public void testLatestComponentByToscaResourceName(){
        Either<Component, StorageOperationStatus> result;
        TopologyTemplate toscaElement = new TopologyTemplate();
        toscaElement.setComponentType(ComponentTypeEnum.SERVICE);
        List<GraphVertex> list = new ArrayList<>();
        GraphVertex graphVertex = getTopologyTemplateVertex();
        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        props.put(GraphPropertyEnum.VERSION, "1.0");
        graphVertex.setMetadataProperties(props);
        list.add(graphVertex);

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, "toscaResourceName");
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(list));
        when(topologyTemplateOperationMock.getToscaElement(ArgumentMatchers.eq(graphVertex),any(ComponentParametersView.class))).thenReturn(Either.left(toscaElement));

        result = testInstance.getFullLatestComponentByToscaResourceName("toscaResourceName");
        assertThat(result.isLeft());
    }

    @Test
    public void testValidateCsarUuidUniqueness() {
        StorageOperationStatus result;
        String csarUUID = "csarUUID";
        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.CSAR_UUID, csarUUID);
        List<GraphVertex> vertexList = new ArrayList<>();
        when(janusGraphDaoMock.getByCriteria(null, properties, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(vertexList));
        result = testInstance.validateCsarUuidUniqueness(csarUUID);
        assertEquals(StorageOperationStatus.ENTITY_ALREADY_EXISTS, result);
    }

    @Test
    public void testValidateCsarUuidUnique_true() {
        StorageOperationStatus result;
        String csarUUID = "csarUUID";
        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.CSAR_UUID, csarUUID);
        when(janusGraphDaoMock.getByCriteria(null, properties, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.right(
            JanusGraphOperationStatus.NOT_FOUND));
        result = testInstance.validateCsarUuidUniqueness(csarUUID);
        assertEquals(StorageOperationStatus.OK, result);
    }

    @Test
    public void testGetLatestCertiNodeTypeByToscaResourceName() {
        Either<Resource, StorageOperationStatus> result;
        String toscaResourceName = "resourceName";
        String uniqueId = "uniqueId";
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertex.setJsonMetadataField(JsonPresentationFields.VERSION, "1.0");
        graphVertex.setUniqueId(uniqueId);
        List<GraphVertex> vertexList = new ArrayList<>();
        vertexList.add(graphVertex);
        Map<GraphPropertyEnum, Object> props = new EnumMap<>(GraphPropertyEnum.class);
        props.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        props.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        props.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        ToscaElement topologyTemplate = new TopologyTemplate();
        topologyTemplate.setComponentType(ComponentTypeEnum.SERVICE);
        when(janusGraphDaoMock.getByCriteria(VertexTypeEnum.NODE_TYPE, props, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(vertexList));
        when(janusGraphDaoMock.getVertexById(uniqueId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(graphVertex));
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class))).thenReturn(Either.left(topologyTemplate));
        result = testInstance.getLatestCertifiedNodeTypeByToscaResourceName(toscaResourceName);
        assertThat(result.isLeft());
    }

    @Test
    public void testValidateCompExists() {
        Either<Boolean, StorageOperationStatus> result;
        String componentId = "componentId";
        GraphVertex graphVertex = getTopologyTemplateVertex();
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(graphVertex));
        result = testInstance.validateComponentExists(componentId);
        assertEquals(true, result.left().value());
    }

    @Test
    public void testValidateCompExists_NotFound() {
        Either<Boolean, StorageOperationStatus> result;
        String componentId = "componentId";
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(
            JanusGraphOperationStatus.NOT_FOUND));
        result = testInstance.validateComponentExists(componentId);
        assertEquals(false, result.left().value());
    }

    @Test
    public void testValidateToscaResourceNameExists() {
        Either<Boolean, StorageOperationStatus> result;
        String templateName = "templateName";
        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, templateName);
        List<GraphVertex> graphVertexList = new ArrayList<>();
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertexList.add(graphVertex);
        when(janusGraphDaoMock.getByCriteria(null, properties, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(graphVertexList));
        result = testInstance.validateToscaResourceNameExists(templateName);
        assertEquals(true, result.left().value());
    }

    @Test
    public void testValidateToscaResourceNameExists_false() {
        Either<Boolean, StorageOperationStatus> result;
        String templateName = "templateName";
        Map<GraphPropertyEnum, Object> properties = new EnumMap<>(GraphPropertyEnum.class);
        properties.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, templateName);
        List<GraphVertex> graphVertexList = new ArrayList<>();
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertexList.add(graphVertex);
        when(janusGraphDaoMock.getByCriteria(null, properties, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.right(
            JanusGraphOperationStatus.NOT_FOUND));
        result = testInstance.validateToscaResourceNameExists(templateName);
        assertEquals(false, result.left().value());
    }

    @Test
    public void testOverrideComponent() {
        Either<Resource, StorageOperationStatus> result;
        Resource resource = new Resource();
        String id = "id";
        resource.setUniqueId(id);
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        NodeType nodeType = new NodeType();
        nodeType.setComponentType(ComponentTypeEnum.RESOURCE);
        ToscaElement toscaElement = new TopologyTemplate();
        toscaElement.setComponentType(ComponentTypeEnum.SERVICE);
        when(janusGraphDaoMock.getVertexById(id, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(graphVertex));
        when(janusGraphDaoMock.getParentVertex(graphVertex, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(graphVertex));
        when(topologyTemplateOperationMock.deleteToscaElement(graphVertex)).thenReturn(Either.left(toscaElement));
        when(nodeTypeOperation.createToscaElement(any(ToscaElement.class))).thenReturn(Either.left(nodeType));
        when(janusGraphDaoMock.getVertexById(null, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(graphVertex));
        when(janusGraphDaoMock.createEdge(graphVertex, graphVertex, EdgeLabelEnum.VERSION, null)).thenReturn(
            JanusGraphOperationStatus.OK);
        result = testInstance.overrideComponent(resource, resource);
        assertTrue(result.isLeft());
    }

    @Test
    public void testGetToscaElement() {
        Either<Component, StorageOperationStatus> result;
        String id = "id";
        GraphVertex graphVertex = getTopologyTemplateVertex();
        ToscaElement toscaElement = getToscaElementForTest();
        when(janusGraphDaoMock.getVertexById(id, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(graphVertex));
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class))).thenReturn(Either.left(toscaElement));
        result = testInstance.getToscaElement(id, JsonParseFlagEnum.ParseAll);
        assertTrue(result.isLeft());
    }

    @Test
    public void testMarkComponentToDelete() {
        StorageOperationStatus result;
        Component component = new Resource();
        String id = "id";
        component.setUniqueId(id);
        GraphVertex graphVertex = getTopologyTemplateVertex();
        when(janusGraphDaoMock.getVertexById(id, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(graphVertex));
        when(nodeTypeOperation.markComponentToDelete(graphVertex)).thenReturn(Either.left(graphVertex));
        result = testInstance.markComponentToDelete(component);
        assertEquals(result, StorageOperationStatus.OK);
    }

    @Test
    public void testDelToscaComponent() {
        Either<Component, StorageOperationStatus> result;
        String componentId = "compId";
        GraphVertex graphVertex = getTopologyTemplateVertex();
        ToscaElement toscaElement = getToscaElementForTest();
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(graphVertex));
        when(topologyTemplateOperationMock.deleteToscaElement(graphVertex)).thenReturn(Either.left(toscaElement));
        result = testInstance.deleteToscaComponent(componentId);
        assertTrue(result.isLeft());
    }

    @Test
    public void testGetLatestByToscaResourceName() {
        Either<Component, StorageOperationStatus> result;
        String toscaResourceName = "name";
        ToscaElement toscaElement = getToscaElementForTest();

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        List<GraphVertex> graphVertexList = new ArrayList<>();
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertex.setUniqueId(toscaResourceName);
        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        props.put(GraphPropertyEnum.VERSION, "1.0");
        graphVertex.setMetadataProperties(props);
        graphVertexList.add(graphVertex);

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(graphVertexList));
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class))).thenReturn(Either.left(toscaElement));
        result = testInstance.getLatestByToscaResourceName(toscaResourceName);
        assertTrue(result.isLeft());
    }

    @Test
    public void testGetFollowed() {
        Either<Set<Component>, StorageOperationStatus> result;
        String userId = "id";
        Set<LifecycleStateEnum> lifecycleStates = new HashSet<>();
        Set<LifecycleStateEnum> lastStateStates = new HashSet<>();
        lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        lifecycleStates.add(LifecycleStateEnum.READY_FOR_CERTIFICATION);
        lifecycleStates.add(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
        lifecycleStates.add(LifecycleStateEnum.CERTIFIED);
        lastStateStates.add(LifecycleStateEnum.READY_FOR_CERTIFICATION);
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        List<ToscaElement> toscaEleList = new ArrayList<>();
        ToscaElement toscaElement = getToscaElementForTest();
        toscaEleList.add(toscaElement);
        when(nodeTypeOperation.getFollowedComponent(userId, lifecycleStates, lastStateStates, componentType)).thenReturn(Either.left(toscaEleList));
        result = testInstance.getFollowed(userId, lifecycleStates, lastStateStates, componentType);
        assertTrue(result.isLeft());
        assertEquals(1, result.left().value().size());
    }

    @Test
    public void testGetBySystemName() {
        Either<List<Component>, StorageOperationStatus> result;
        String sysName = "sysName";
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.RESOURCE;
        ToscaElement toscaElement = getToscaElementForTest();
        List<GraphVertex> componentVertices = new ArrayList<>();
        GraphVertex graphVertex = getTopologyTemplateVertex();
        componentVertices.add(graphVertex);
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> propertiesNotToMatch = new EnumMap<>(GraphPropertyEnum.class);

        propertiesToMatch.put(GraphPropertyEnum.SYSTEM_NAME, sysName);
        propertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, componentTypeEnum.name());

        propertiesNotToMatch.put(GraphPropertyEnum.IS_DELETED, true);

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(componentVertices));
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class))).thenReturn(Either.left(toscaElement));
        result = testInstance.getBySystemName(componentTypeEnum, sysName);
        assertTrue(result.isLeft());
        assertEquals(1, result.left().value().size());
    }

    @Test
    public void testGetCompByNameAndVersion() {
        Either<Component, StorageOperationStatus> result;
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        String name = "name";
        String version = "1.0";
        JsonParseFlagEnum parseFlag = JsonParseFlagEnum.ParseAll;
        List<GraphVertex> graphVertexList = new ArrayList<>();
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertexList.add(graphVertex);
        ToscaElement toscaElement = getToscaElementForTest();
        Map<GraphPropertyEnum, Object> hasProperties = new EnumMap<>(GraphPropertyEnum.class);
        Map<GraphPropertyEnum, Object> hasNotProperties = new EnumMap<>(GraphPropertyEnum.class);

        hasProperties.put(GraphPropertyEnum.NAME, name);
        hasProperties.put(GraphPropertyEnum.VERSION, version);
        hasNotProperties.put(GraphPropertyEnum.IS_DELETED, true);
        hasProperties.put(GraphPropertyEnum.COMPONENT_TYPE, componentType.name());
        when(janusGraphDaoMock.getByCriteria(null, hasProperties, hasNotProperties, parseFlag)).thenReturn(Either.left(graphVertexList));
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class))).thenReturn(Either.left(toscaElement));
        result = testInstance.getComponentByNameAndVersion(componentType, name, version, parseFlag);
        assertTrue(result.isLeft());
    }

    private ToscaElement getToscaElementForTest() {
        ToscaElement toscaElement = new TopologyTemplate();
        toscaElement.setComponentType(ComponentTypeEnum.RESOURCE);
        return toscaElement;
    }

    @Test
    public void addDataTypesToComponentSuccessTest(){
        Either<List<DataTypeDefinition>, StorageOperationStatus> result = addDataTypesToComponentWithStatus(StorageOperationStatus.OK);
        assertTrue(result.isLeft());
    }

    @Test
    public void addDataTypesToComponentFailureTest_BadRequest(){
        Either<List<DataTypeDefinition>, StorageOperationStatus> result = addDataTypesToComponentWithStatus(StorageOperationStatus.BAD_REQUEST);
        assertTrue(result.isRight() && result.right().value() == StorageOperationStatus.BAD_REQUEST);
    }

    private Either<List<DataTypeDefinition>, StorageOperationStatus> addDataTypesToComponentWithStatus(StorageOperationStatus status) {
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        String componentId = "componentid";
        String Id = "id";

        PropertyDefinition noDefaultProp = new PropertyDefinition();
        noDefaultProp.setName("noDefaultProp");
        PropertyDefinition prop1 = new PropertyDefinition();
        prop1.setDefaultValue("def1");
        prop1.setName("prop1");
        PropertyDefinition prop2 = new PropertyDefinition();
        prop2.setType("dataType1");
        prop2.setName("prop2");
        PropertyDefinition prop3 = new PropertyDefinition();
        prop3.setDefaultValue("def3");
        prop3.setName("prop3");

        DataTypeDefinition noDefaultValue = new DataTypeDefinition();
        noDefaultValue.setProperties(Collections.singletonList(noDefaultProp));
        noDefaultValue.setDerivedFromName("name0");

        DataTypeDefinition dataType1 = new DataTypeDefinition();
        dataType1.setProperties(Arrays.asList(prop1, prop3));
        dataType1.setName("name1");
        dataType1.setDerivedFromName("derivedfromname1");

        DataTypeDefinition dataType2 = new DataTypeDefinition();
        dataType2.setDerivedFrom(dataType1);
        dataType2.setName("name2");
        dataType2.setDerivedFromName("derivedfromname2");

        DataTypeDefinition dataType3 = new DataTypeDefinition();
        dataType3.setProperties(Collections.singletonList(prop2));
        dataType3.setDerivedFrom(noDefaultValue);
        dataType3.setName("name3");
        dataType3.setDerivedFromName("derivedfromname3");

        dataTypes.put("noDefault", noDefaultValue);
        dataTypes.put("dataType1", dataType1);
        dataTypes.put("dataType2", dataType2);
        dataTypes.put("dataType3", dataType3);

        GraphVertex vertex;
        if(status == StorageOperationStatus.OK){
            vertex = getTopologyTemplateVertex();
        } else {
            vertex = getNodeTypeVertex();
        }
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = Either.left(vertex);
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.addToscaDataToToscaElement(eq(vertex),
                eq(EdgeLabelEnum.DATA_TYPES), eq(VertexTypeEnum.DATA_TYPES), anyMap(), eq(JsonPresentationFields.NAME))).thenReturn(status);
        return testInstance.addDataTypesToComponent(dataTypes, componentId);
    }

    @Test
    public void testDataTypesToComponentFailureTest_NotFound() {
        Either<List<DataTypeDefinition>, StorageOperationStatus> result;
        String componentId = "componentId";
        GraphVertex vertex = getNodeTypeVertex();
        Map<String, DataTypeDefinition> dataTypes = new HashMap<>();
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(
            JanusGraphOperationStatus.NOT_FOUND));
        result = testInstance.addDataTypesToComponent(dataTypes, componentId);
        assertTrue(result.isRight() && result.right().value() == StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void testDeleteDataTypeOfComponent() {
        StorageOperationStatus result;
        Component component = new Resource();
        String id = "id";
        component.setUniqueId(id);
        String datatype = null;

        DataTypeDefinition dataType1 = new DataTypeDefinition();
        dataType1.setName("name1");
        Map<String, DataTypeDataDefinition> dataTypeDataMap = new HashMap<>();
        dataTypeDataMap.put("datatype1", dataType1);
        List<DataTypeDefinition> dataTypeMap = dataTypeDataMap.values().stream().map(e -> { DataTypeDefinition dataType = new DataTypeDefinition(e);return dataType; }).collect(Collectors.toList());
        component.setDataTypes(dataTypeMap);
        GraphVertex graphVertex = getTopologyTemplateVertex();
        result = testInstance.deleteDataTypeOfComponent(component, "datatype1");
        assertEquals(datatype, result);
    }

    @Test
    public void testAddComponentInstancePropertiesToComponent() {
        // set up component object
        Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);
        List<ComponentInstanceProperty> instanceProps = new ArrayList<>();
        ComponentInstanceProperty instanceProp = new ComponentInstanceProperty();
        instanceProp.setName(PROPERTY1_NAME);
        instanceProp.setType(PROPERTY1_TYPE);
        instanceProps.add(instanceProp);
        instanceProp = new ComponentInstanceProperty();
        instanceProp.setName(PROPERTY2_NAME);
        instanceProp.setType(PROPERTY2_TYPE);
        instanceProps.add(instanceProp);
        Map<String, List<ComponentInstanceProperty>> instancePropsMap =
            Collections.singletonMap(COMPONENT_ID, instanceProps);
        component.setComponentInstancesProperties(Collections.singletonMap(COMPONENT_ID, new ArrayList<>()));

        when(nodeTemplateOperationMock.addComponentInstanceProperty(any(), any(), any()))
            .thenReturn(StorageOperationStatus.OK);

        Either<Map<String, List<ComponentInstanceProperty>>, StorageOperationStatus> result =
            testInstance.addComponentInstancePropertiesToComponent(component, instancePropsMap);
        assertTrue(result.isLeft());
        verify(nodeTemplateOperationMock, times(2)).addComponentInstanceProperty(any(), any(), any());
        List<ComponentInstanceProperty> resultProps = result.left().value().get(COMPONENT_ID);
        assertTrue(resultProps.stream().anyMatch(e -> e.getName().equals(PROPERTY1_NAME)));
        assertTrue(resultProps.stream().anyMatch(e -> e.getName().equals(PROPERTY2_NAME)));
    }

    private Either<PolicyDefinition, StorageOperationStatus> associatePolicyToComponentWithStatus(StorageOperationStatus status) {
        PolicyDefinition policy = new PolicyDefinition();
        String componentId = "componentId";
        int counter = 0;
        GraphVertex vertex;
        if(status == StorageOperationStatus.OK){
            vertex = getTopologyTemplateVertex();
        } else {
            vertex = getNodeTypeVertex();
        }
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = Either.left(vertex);
        when(janusGraphDaoMock.getVertexById(eq(componentId), eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.addPolicyToToscaElement(eq(vertex), any(PolicyDefinition.class), anyInt())).thenReturn(status);
        return testInstance.associatePolicyToComponent(componentId, policy, counter);
    }

    private Either<PolicyDefinition, StorageOperationStatus> updatePolicyOfComponentWithStatus(StorageOperationStatus status) {
        PolicyDefinition policy = new PolicyDefinition();
        String componentId = "componentId";
        GraphVertex vertex = getTopologyTemplateVertex();
        when(janusGraphDaoMock.getVertexById(eq(componentId), eq(JsonParseFlagEnum.NoParse))).thenReturn(Either.left(vertex));
        when(topologyTemplateOperationMock.updatePolicyOfToscaElement(eq(vertex), any(PolicyDefinition.class))).thenReturn(status);
        return testInstance.updatePolicyOfComponent(componentId, policy);
    }

    private void removePolicyFromComponentWithStatus(StorageOperationStatus status) {
        String componentId = "componentId";
        String policyId = "policyId";
        GraphVertex vertex = getTopologyTemplateVertex();
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = Either.left(vertex);
        when(janusGraphDaoMock.getVertexById(eq(componentId), eq(JsonParseFlagEnum.NoParse))).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.removePolicyFromToscaElement(eq(vertex), eq(policyId))).thenReturn(status);
        StorageOperationStatus result = testInstance.removePolicyFromComponent(componentId, policyId);
        assertSame(result, status);
    }

    private List<GraphVertex> getMockVertices(int numOfVertices) {
        return IntStream.range(0, numOfVertices).mapToObj(i -> getTopologyTemplateVertex()).collect(Collectors.toList());
    }

    private ToscaElement getResourceToscaElement(String id) {
        ToscaElement toscaElement = new TopologyTemplate();
        toscaElement.setMetadata(new HashMap<>());
        toscaElement.getMetadata().put(JsonPresentationFields.COMPONENT_TYPE.getPresentation(), "RESOURCE");
        toscaElement.getMetadata().put(JsonPresentationFields.UNIQUE_ID.getPresentation(), id);
        return toscaElement;
    }

    private GraphVertex getTopologyTemplateVertex() {
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        return graphVertex;
    }

    private GraphVertex getNodeTypeVertex() {
        GraphVertex graphVertex = new GraphVertex();
        graphVertex.setLabel(VertexTypeEnum.NODE_TYPE);
        return graphVertex;
    }
}
