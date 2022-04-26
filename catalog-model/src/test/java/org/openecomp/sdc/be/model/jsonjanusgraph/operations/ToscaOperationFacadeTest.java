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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.config.ComponentType;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.NodeType;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@RunWith(MockitoJUnitRunner.class)
public class ToscaOperationFacadeTest {

    private static final String COMPONENT_ID = "componentId";
    private static final String PROPERTY1_NAME = "prop1";
    private static final String PROPERTY1_TYPE = "string";
    private static final String PROPERTY2_NAME = "prop2";
    private static final String PROPERTY2_TYPE = "integer";
    private static final String ICON_NAME = "icon";
    private static final String SERVICE_MODEL_NAME = "Test_Service";
    private static final String SERVICE_PROXY_INSTANCE0_NAME = "testservice_proxy0";
    private static final String SERVICE_SUBSTITUTION_INSTANCE0_NAME = "testservice0";

    @InjectMocks
    private ToscaOperationFacade testInstance;

    @Mock
    private HealingJanusGraphDao janusGraphDaoMock;

    @Mock
    private TopologyTemplateOperation topologyTemplateOperationMock;

    @Mock
    private NodeTypeOperation nodeTypeOperationMock;

    @Mock
    private NodeTemplateOperation nodeTemplateOperationMock;

    @Mock
    private IGraphLockOperation graphLockOperationMock;

    @Before
    public void setUp() throws Exception {
        testInstance = new ToscaOperationFacade();
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchMetaDataByResourceType() throws Exception {
        ArgumentCaptor<Map> criteriaCapture = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map> criteriaNotCapture = ArgumentCaptor.forClass(Map.class);
        ComponentParametersView dataFilter = new ComponentParametersView();
        List<GraphVertex> mockVertices = getMockVertices(2);
        Either<List<GraphVertex>, JanusGraphOperationStatus> returnedVertices = Either.left(mockVertices);

        when(janusGraphDaoMock.getByCriteria(eq(null), criteriaCapture.capture(), criteriaNotCapture.capture(), eq(JsonParseFlagEnum.ParseMetadata)))
            .thenReturn(returnedVertices);
        when(topologyTemplateOperationMock.getToscaElement(mockVertices.get(0), dataFilter)).thenReturn(Either.left(getResourceToscaElement("0")));
        when(topologyTemplateOperationMock.getToscaElement(mockVertices.get(1), dataFilter)).thenReturn(Either.left(getResourceToscaElement("1")));
        Either<List<Component>, StorageOperationStatus> fetchedComponents = testInstance
            .fetchMetaDataByResourceType(ResourceTypeEnum.VF.getValue(), dataFilter);

        verifyCriteriaForHighestVersionAndVfResourceType(criteriaCapture);
        verifyCriteriaNotIsDeleted(criteriaNotCapture);

        assertTrue(fetchedComponents.isLeft());
        List<Component> cmpts = fetchedComponents.left().value();
        assertEquals(2, cmpts.size());
        assertEquals("0", cmpts.get(0).getUniqueId());
        assertEquals("1", cmpts.get(1).getUniqueId());
    }

    private void verifyCriteriaForHighestVersionAndVfResourceType(ArgumentCaptor<Map> criteriaCapture) {
        Map<GraphPropertyEnum, Object> criteria = (Map<GraphPropertyEnum, Object>) criteriaCapture.getValue();
        assertEquals(2, criteria.size());
        assertEquals("VF", criteria.get(GraphPropertyEnum.RESOURCE_TYPE));
        assertEquals(true, criteria.get(GraphPropertyEnum.IS_HIGHEST_VERSION));
    }

    private void verifyCriteriaNotIsDeleted(ArgumentCaptor<Map> criteriaNotCapture) {
        Map<GraphPropertyEnum, Object> notCriteria = (Map<GraphPropertyEnum, Object>) criteriaNotCapture.getValue();
        assertEquals(1, notCriteria.size());
        assertEquals(true, notCriteria.get(GraphPropertyEnum.IS_DELETED));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchMetaDataByResourceType_failedToGetData() throws Exception {
        when(janusGraphDaoMock.getByCriteria(eq(null), anyMap(), anyMap(), eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(Either.right(
            JanusGraphOperationStatus.GENERAL_ERROR));
        Either<List<Component>, StorageOperationStatus> fetchedComponents = testInstance
            .fetchMetaDataByResourceType(ResourceTypeEnum.VF.getValue(), new ComponentParametersView());
        assertTrue(fetchedComponents.isRight());
        assertEquals(StorageOperationStatus.GENERAL_ERROR, fetchedComponents.right().value());
    }

    @Test
    public void associatePolicyToComponentSuccessTest() {
        Either<PolicyDefinition, StorageOperationStatus> result = associatePolicyToComponentWithStatus(StorageOperationStatus.OK);
        assertTrue(result.isLeft());
    }

    @Test
    public void associatePolicyToComponentFailureTest() {
        Either<PolicyDefinition, StorageOperationStatus> result = associatePolicyToComponentWithStatus(StorageOperationStatus.BAD_REQUEST);
        assertTrue(result.isRight() && result.right().value() == StorageOperationStatus.BAD_REQUEST);
    }

    @Test
    public void updatePolicyOfComponentSuccessTest() {
        Either<PolicyDefinition, StorageOperationStatus> result = updatePolicyOfComponentWithStatus(StorageOperationStatus.OK);
        assertTrue(result.isLeft());
    }

    @Test
    public void updatePolicyOfComponentFailureTest() {
        Either<PolicyDefinition, StorageOperationStatus> result = updatePolicyOfComponentWithStatus(StorageOperationStatus.NOT_FOUND);
        assertTrue(result.isRight() && result.right().value() == StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void removePolicyFromComponentSuccessTest() {
        removePolicyFromComponentWithStatus(StorageOperationStatus.OK);
    }

    @Test
    public void removePolicyFromComponentFailureTest() {
        removePolicyFromComponentWithStatus(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void testFindLastCertifiedToscaElementByUUID() {
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
        when(topologyTemplateOperationMock.getToscaElement(ArgumentMatchers.eq(graphVertex), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toscaElement));
        when(janusGraphDaoMock.getByCriteria(ModelConverter.getVertexType(component), props)).thenReturn(Either.left(list));
        result = testInstance.findLastCertifiedToscaElementByUUID(component);
        Component resultComp = result.left().value();
        assertEquals(resultComp.getToscaType(), ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());
    }

    @Test
    public void testLatestComponentByToscaResourceName() {
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

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll, null))
            .thenReturn(Either.left(list));
        when(topologyTemplateOperationMock.getToscaElement(ArgumentMatchers.eq(graphVertex), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toscaElement));

        result = testInstance.getFullLatestComponentByToscaResourceName("toscaResourceName");
        assertTrue(result.isLeft());
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
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(topologyTemplate));
        result = testInstance.getLatestCertifiedNodeTypeByToscaResourceName(toscaResourceName);
        assertTrue(result.isLeft());
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
        when(nodeTypeOperationMock.createToscaElement(any(ToscaElement.class))).thenReturn(Either.left(nodeType));
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
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toscaElement));
        result = testInstance.getToscaElement(id, JsonParseFlagEnum.ParseAll);
        assertTrue(result.isLeft());
    }

    @Test
    public void testDeleteService_ServiceInUse() {
        String invariantUUID = "12345";
        String serviceUid = "1";
        GraphVertex service1 = getTopologyTemplateVertex();
        service1.setUniqueId(serviceUid);
        List<GraphVertex> allResourcesToDelete = new ArrayList<>();
        allResourcesToDelete.add(service1);
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, invariantUUID);
        ToscaElement toscaElement = getToscaElementForTest();
        toscaElement.setUniqueId(serviceUid);
        String service2Name = "service2Name";
        Map<String, Object> service2MetadataJson = new HashMap<>();
        service2MetadataJson.put(GraphPropertyEnum.COMPONENT_TYPE.getProperty(), ComponentType.SERVICE);
        service2MetadataJson.put(GraphPropertyEnum.NAME.getProperty(), service2Name);
        String service2Uid = "2";
        GraphVertex usingService = getTopologyTemplateVertex();
        usingService.setUniqueId(service2Uid);
        usingService.setMetadataJson(service2MetadataJson);
        List<GraphVertex> inUseBy = new ArrayList<>();
        inUseBy.add(usingService);

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).
                thenReturn(Either.left(allResourcesToDelete));
        doReturn(Either.left(toscaElement)).when(topologyTemplateOperationMock).getToscaElement(eq(service1), any(ComponentParametersView.class));
        when(topologyTemplateOperationMock.
                getComponentByLabelAndId(serviceUid, ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll)).
                thenReturn(Either.left(service1));
        when(janusGraphDaoMock.getParentVertices(any(GraphVertex.class), any(), eq(JsonParseFlagEnum.ParseAll))).
                thenReturn(Either.left(inUseBy)).thenReturn(Either.left(inUseBy));
        final OperationException actualException = assertThrows(OperationException.class, () -> testInstance.deleteService(invariantUUID, true));
        assertEquals(actualException.getActionStatus(), ActionStatus.COMPONENT_IN_USE_BY_ANOTHER_COMPONENT);
        assertEquals(actualException.getParams()[0], ComponentType.SERVICE +  " " + service2Name);
    }

    @Test
    public void testDeleteService_WithOneVersion() {
        String invariantUUID = "12345";
        String serviceUid = "1";
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, invariantUUID);
        GraphVertex service1 = getTopologyTemplateVertex();
        service1.setUniqueId(serviceUid);
        List<GraphVertex> allResourcesToDelete = new ArrayList<>();
        allResourcesToDelete.add(service1);
        ToscaElement toscaElement = getToscaElementForTest();
        toscaElement.setUniqueId(serviceUid);
        List<String> affectedComponentIds = new ArrayList<>();
        affectedComponentIds.add(service1.getUniqueId());

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).
                thenReturn(Either.left(allResourcesToDelete));
        doReturn(Either.left(toscaElement)).when(topologyTemplateOperationMock).getToscaElement(eq(service1), any(ComponentParametersView.class));
        when(topologyTemplateOperationMock.
                getComponentByLabelAndId(serviceUid, ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll)).
                thenReturn(Either.left(service1));
        when(janusGraphDaoMock.getParentVertices(eq(service1), any(), eq(JsonParseFlagEnum.ParseAll))).
                thenReturn(Either.right(JanusGraphOperationStatus.OK));
        when(graphLockOperationMock.lockComponent(service1.getUniqueId(), NodeTypeEnum.Service)).
                thenReturn(StorageOperationStatus.OK);
        when(topologyTemplateOperationMock.deleteToscaElement(service1)).thenReturn(Either.left(toscaElement));
        assertEquals(affectedComponentIds, testInstance.deleteService(invariantUUID, true));
    }

    @Test
    public void testDeleteService_WithTwoVersions() {
        String invariantUUID = "12345";
        String serviceUid = "1";
        String service2Uid = "2";
        GraphVertex service = getTopologyTemplateVertex();
        service.setUniqueId(serviceUid);
        GraphVertex serviceV2 = getTopologyTemplateVertex();
        serviceV2.setUniqueId(service2Uid);
        ToscaElement toscaElement = getToscaElementForTest();
        toscaElement.setUniqueId(serviceUid);
        ToscaElement toscaElement2 = getToscaElementForTest();
        toscaElement2.setUniqueId(service2Uid);
        List<String> affectedComponentIds = new ArrayList<>();
        affectedComponentIds.add(service.getUniqueId());
        affectedComponentIds.add(serviceV2.getUniqueId());
        List<GraphVertex> allResourcesToDelete = new ArrayList<>();
        allResourcesToDelete.add(service);
        allResourcesToDelete.add(serviceV2);
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, invariantUUID);

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).
                thenReturn(Either.left(allResourcesToDelete));
        doReturn(Either.left(toscaElement)).when(topologyTemplateOperationMock).
                getToscaElement(eq(service), any(ComponentParametersView.class));
        doReturn(Either.left(toscaElement2)).when(topologyTemplateOperationMock).
                getToscaElement(eq(serviceV2), any(ComponentParametersView.class));
        when(topologyTemplateOperationMock.
                getComponentByLabelAndId(serviceUid, ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll)).
                thenReturn(Either.left(service));
        when(topologyTemplateOperationMock.
                getComponentByLabelAndId(service2Uid, ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll)).
                thenReturn(Either.left(serviceV2));
        when(janusGraphDaoMock.getParentVertices(any(GraphVertex.class), any(), eq(JsonParseFlagEnum.ParseAll))).
                thenReturn(Either.right(JanusGraphOperationStatus.OK));
        when(graphLockOperationMock.lockComponent(service.getUniqueId(), NodeTypeEnum.Service)).
            thenReturn(StorageOperationStatus.OK);
        when(graphLockOperationMock.lockComponent(serviceV2.getUniqueId(), NodeTypeEnum.Service)).
                thenReturn(StorageOperationStatus.OK);
        when(topologyTemplateOperationMock.deleteToscaElement(service)).thenReturn(Either.left(toscaElement));
        when(topologyTemplateOperationMock.deleteToscaElement(serviceV2)).thenReturn(Either.left(toscaElement));
        assertEquals(affectedComponentIds, testInstance.deleteService(invariantUUID, true));
    }

    @Test
    public void testDeleteService_FailDelete() {
        String invariantUUID = "12345";
        String serviceUid = "1";
        GraphVertex service = getTopologyTemplateVertex();
        service.setUniqueId(serviceUid);
        ToscaElement toscaElement = getToscaElementForTest();
        toscaElement.setUniqueId(serviceUid);
        List<GraphVertex> allResourcesToDelete = new ArrayList<>();
        allResourcesToDelete.add(service);
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, invariantUUID);

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).
                thenReturn(Either.left(allResourcesToDelete));
        doReturn(Either.left(toscaElement)).when(topologyTemplateOperationMock).getToscaElement(eq(service), any(ComponentParametersView.class));
        when(topologyTemplateOperationMock.getComponentByLabelAndId(serviceUid, ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll)).
                thenReturn(Either.left(service));
        when(janusGraphDaoMock.getParentVertices(eq(service), any(), eq(JsonParseFlagEnum.ParseAll))).
                thenReturn(Either.right(JanusGraphOperationStatus.OK));
        when(graphLockOperationMock.lockComponent(service.getUniqueId(), NodeTypeEnum.Service)).
                thenReturn(StorageOperationStatus.OK);
        when(topologyTemplateOperationMock.deleteToscaElement(service))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        assertThrows(StorageException.class, () -> testInstance.deleteService(invariantUUID, true));
    }

    @Test
    public void testDeleteService_NotFound() {
        String invariantUUID = "12345";
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, invariantUUID);
        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).
                thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        assertThrows(StorageException.class, () -> testInstance.deleteService(invariantUUID, true));
    }

    @Test
    public void testMarkComponentToDelete() {
        StorageOperationStatus result;
        Component component = new Resource();
        String id = "id";
        component.setUniqueId(id);
        GraphVertex graphVertex = getTopologyTemplateVertex();
        when(janusGraphDaoMock.getVertexById(id, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(graphVertex));
        when(nodeTypeOperationMock.markComponentToDelete(graphVertex)).thenReturn(Either.left(graphVertex));
        result = testInstance.markComponentToDelete(component);
        assertEquals(StorageOperationStatus.OK, result);
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
        String model = "testModel";
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

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseMetadata, model))
            .thenReturn(Either.left(graphVertexList));
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toscaElement));
        result = testInstance.getLatestByToscaResourceName(toscaResourceName, model);
        assertTrue(result.isLeft());
    }


    @Test
    public void testGetLatestResourceByToscaResourceName() {
        Either<Resource, StorageOperationStatus> result;
        String toscaResourceName = "org.openecomp.resource.vf";
        ToscaElement toscaElement = getToscaElementForTest();

        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.TOSCA_RESOURCE_NAME, toscaResourceName);
        propertiesToMatch.put(GraphPropertyEnum.IS_HIGHEST_VERSION, true);
        if (!toscaResourceName.contains("org.openecomp.resource.vf")) {
            propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
        }

        List<GraphVertex> graphVertexList = new ArrayList<>();
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertex.setUniqueId(toscaResourceName);
        Map<JsonPresentationFields, Object> props = new HashMap<>();
        props.put(JsonPresentationFields.VERSION, "1.0");
        graphVertex.setJsonMetadataField(JsonPresentationFields.VERSION, props.get(JsonPresentationFields.VERSION));
        graphVertexList.add(graphVertex);

        when(janusGraphDaoMock.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, propertiesToMatch, JsonParseFlagEnum.ParseMetadata))
            .thenReturn(Either.left(graphVertexList));
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toscaElement));

        when(janusGraphDaoMock.getVertexById(toscaResourceName, JsonParseFlagEnum.ParseAll)).thenReturn(Either.left(graphVertex));

        result = testInstance.getLatestResourceByToscaResourceName(toscaResourceName);
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
        lifecycleStates.add(LifecycleStateEnum.CERTIFIED);
        ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
        List<ToscaElement> toscaEleList = new ArrayList<>();
        ToscaElement toscaElement = getToscaElementForTest();
        toscaEleList.add(toscaElement);
        when(nodeTypeOperationMock.getFollowedComponent(userId, lifecycleStates, lastStateStates, componentType))
            .thenReturn(Either.left(toscaEleList));
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

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, propertiesNotToMatch, JsonParseFlagEnum.ParseAll))
            .thenReturn(Either.left(componentVertices));
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toscaElement));
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
        when(topologyTemplateOperationMock.getToscaElement(any(GraphVertex.class), any(ComponentParametersView.class)))
            .thenReturn(Either.left(toscaElement));
        result = testInstance.getComponentByNameAndVersion(componentType, name, version, parseFlag);
        assertTrue(result.isLeft());
    }

    private ToscaElement getToscaElementForTest() {
        ToscaElement toscaElement = new TopologyTemplate();
        toscaElement.setComponentType(ComponentTypeEnum.RESOURCE);
        return toscaElement;
    }

    @Test
    public void addDataTypesToComponentSuccessTest() {
        Either<List<DataTypeDefinition>, StorageOperationStatus> result = addDataTypesToComponentWithStatus(StorageOperationStatus.OK);
        assertTrue(result.isLeft());
    }

    @Test
    public void addDataTypesToComponentFailureTest_BadRequest() {
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
        if (status == StorageOperationStatus.OK) {
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
        Component component = new Resource();
        String id = "id";
        component.setUniqueId(id);

        DataTypeDefinition dataType1 = new DataTypeDefinition();
        dataType1.setName("name1");
        Map<String, DataTypeDataDefinition> dataTypeDataMap = new HashMap<>();
        dataTypeDataMap.put("datatype1", dataType1);
        List<DataTypeDefinition> dataTypeMap = dataTypeDataMap.values().stream().map(e -> {
            return new DataTypeDefinition(e);
        }).collect(Collectors.toList());
        component.setDataTypes(dataTypeMap);
        GraphVertex graphVertex = getTopologyTemplateVertex();
        assertNull(testInstance.deleteDataTypeOfComponent(component, "datatype1"));
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

    @Test
    public void testAddComponentInstanceToTopologyTemplate_ServiceProxy() {
        Component containerComponent = new Service();
        Component originalComponent = new Service();
        ComponentInstance componentInstance = new ComponentInstance();
        ComponentInstance existingComponentInstance = new ComponentInstance();
        User user = new User();

        containerComponent.setComponentType(ComponentTypeEnum.SERVICE);

        originalComponent.setComponentType(ComponentTypeEnum.SERVICE);
        originalComponent.setIcon(ICON_NAME);

        componentInstance.setOriginType(OriginTypeEnum.ServiceProxy);
        componentInstance.setSourceModelName(SERVICE_MODEL_NAME);

        List<ComponentInstance> existingInstances = new ArrayList<>();
        existingComponentInstance.setNormalizedName(SERVICE_PROXY_INSTANCE0_NAME);
        existingInstances.add(existingComponentInstance);
        containerComponent.setComponentInstances(existingInstances);

        when(nodeTemplateOperationMock
            .addComponentInstanceToTopologyTemplate(any(), any(), eq("1"), eq(componentInstance), eq(false), eq(user)))
            .thenReturn(Either.left(new ImmutablePair<>(new TopologyTemplate(), COMPONENT_ID)));
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        // preset COMPONENT_TYPE field for internal ModelConverter call
        topologyTemplate.setMetadataValue(JsonPresentationFields.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        when(topologyTemplateOperationMock.getToscaElement(containerComponent.getUniqueId()))
            .thenReturn(Either.left(topologyTemplate));

        Either<ImmutablePair<Component, String>, StorageOperationStatus> result =
            testInstance.addComponentInstanceToTopologyTemplate(
                containerComponent, originalComponent, componentInstance, false, user);

        assertTrue(result.isLeft());
        assertEquals(ICON_NAME, componentInstance.getIcon());
        assertEquals(COMPONENT_ID, result.left().value().getRight());
        // the instance counter must be 1 because the service proxy instance with suffix 0 already exists.
        verify(nodeTemplateOperationMock, times(1))
            .addComponentInstanceToTopologyTemplate(any(), any(), eq("1"), eq(componentInstance), eq(false), eq(user));
    }

    @Test
    public void testAddComponentInstanceToTopologyTemplate_ServiceSubstitution() {
        Component containerComponent = new Service();
        Component originalComponent = new Service();
        ComponentInstance componentInstance = new ComponentInstance();
        ComponentInstance existingComponentInstance = new ComponentInstance();
        User user = new User();

        containerComponent.setComponentType(ComponentTypeEnum.SERVICE);

        originalComponent.setComponentType(ComponentTypeEnum.SERVICE);
        originalComponent.setIcon(ICON_NAME);

        componentInstance.setOriginType(OriginTypeEnum.ServiceSubstitution);
        componentInstance.setSourceModelName(SERVICE_MODEL_NAME);

        List<ComponentInstance> existingInstances = new ArrayList<>();
        existingComponentInstance.setNormalizedName(SERVICE_SUBSTITUTION_INSTANCE0_NAME);
        existingInstances.add(existingComponentInstance);
        containerComponent.setComponentInstances(existingInstances);

        when(nodeTemplateOperationMock
            .addComponentInstanceToTopologyTemplate(any(), any(), eq("1"), eq(componentInstance), eq(false), eq(user)))
            .thenReturn(Either.left(new ImmutablePair<>(new TopologyTemplate(), COMPONENT_ID)));
        TopologyTemplate topologyTemplate = new TopologyTemplate();
        topologyTemplate.setMetadataValue(JsonPresentationFields.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        when(topologyTemplateOperationMock.getToscaElement(containerComponent.getUniqueId()))
            .thenReturn(Either.left(topologyTemplate));

        Either<ImmutablePair<Component, String>, StorageOperationStatus> result =
            testInstance.addComponentInstanceToTopologyTemplate(
                containerComponent, originalComponent, componentInstance, false, user);

        assertTrue(result.isLeft());
        assertEquals(ICON_NAME, componentInstance.getIcon());
        assertEquals(COMPONENT_ID, result.left().value().getRight());
        verify(nodeTemplateOperationMock, times(1))
            .addComponentInstanceToTopologyTemplate(any(), any(), eq("1"), eq(componentInstance), eq(false), eq(user));
    }

    @Test
    public void testUpdateComponentInstanceRequirement() {
        String containerComponentId = "containerComponentId";
        String componentInstanceUniqueId = "componentInstanceUniqueId";
        RequirementDataDefinition requirementDataDefinition = Mockito.mock(RequirementDataDefinition.class);

        when(nodeTemplateOperationMock.updateComponentInstanceRequirement(containerComponentId, componentInstanceUniqueId, requirementDataDefinition))
            .thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus result = testInstance
            .updateComponentInstanceRequirement(containerComponentId, componentInstanceUniqueId, requirementDataDefinition);
        assertEquals(StorageOperationStatus.OK, result);
        verify(nodeTemplateOperationMock, times(1))
            .updateComponentInstanceRequirement(containerComponentId, componentInstanceUniqueId, requirementDataDefinition);

    }

    @Test
    public void associateCapabilitiesToServiceFailureTest() {
        StorageOperationStatus result = associateCapabilitiesToServiceWithStatus(StorageOperationStatus.BAD_REQUEST);
        assertSame(StorageOperationStatus.BAD_REQUEST, result);
    }

    @Test
    public void associateCapabilitiesToServiceSuccessTest() {
        StorageOperationStatus result = associateCapabilitiesToServiceWithStatus(StorageOperationStatus.OK);
        assertSame(StorageOperationStatus.OK, result);
    }

    private StorageOperationStatus associateCapabilitiesToServiceWithStatus(StorageOperationStatus status) {
        Map<String, ListCapabilityDataDefinition> capabilitiesMap = new HashedMap();
        String componentId = "componentid";

        ListCapabilityDataDefinition listCapabilityDataDefinition1 = new ListCapabilityDataDefinition();
        capabilitiesMap.put("capabilities1", listCapabilityDataDefinition1);

        GraphVertex vertex;
        if (status == StorageOperationStatus.OK) {
            vertex = getTopologyTemplateVertex();
        } else {
            vertex = getNodeTypeVertex();
        }

        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = Either.left(vertex);
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.associateElementToData(eq(vertex),
            eq(VertexTypeEnum.CAPABILITIES), eq(EdgeLabelEnum.CAPABILITIES), anyMap())).thenReturn(Either.right(status));
        return testInstance.associateCapabilitiesToService(capabilitiesMap, componentId);
    }

    @Test
    public void associateRequirementsToServiceFailureTest() {
        StorageOperationStatus result = associateRequirementsToServiceWithStatus(StorageOperationStatus.BAD_REQUEST);
        assertSame(StorageOperationStatus.BAD_REQUEST, result);
    }

    @Test
    public void associateRequirementsToServiceSuccessTest() {
        StorageOperationStatus result = associateRequirementsToServiceWithStatus(StorageOperationStatus.OK);
        assertSame(StorageOperationStatus.OK, result);
    }

    @Test
    public void test_addOutputsToComponent() {
        final GraphVertex graphVertex = getTopologyTemplateVertex();
        final String componentId = "componentId";

        doReturn(Either.left(graphVertex)).when(janusGraphDaoMock).getVertexById(componentId, JsonParseFlagEnum.NoParse);
        doReturn(StorageOperationStatus.OK).when(topologyTemplateOperationMock)
            .addToscaDataToToscaElement(
                any(GraphVertex.class), eq(EdgeLabelEnum.OUTPUTS), eq(VertexTypeEnum.OUTPUTS), anyMap(), eq(JsonPresentationFields.NAME));

        final Map<String, OutputDefinition> outputs = new HashMap<>();
        final OutputDefinition outputDefinition = new OutputDefinition();
        outputs.put("mock", outputDefinition);
        final Either<List<OutputDefinition>, StorageOperationStatus> result = testInstance.addOutputsToComponent(outputs, componentId);
        assertNotNull(result);
        assertTrue(result.isLeft());
        assertFalse(result.left().value().isEmpty());
        assertThat(result.left().value().get(0)).isInstanceOf(OutputDefinition.class);
        verify(janusGraphDaoMock, times(1)).getVertexById(componentId, JsonParseFlagEnum.NoParse);
        verify(topologyTemplateOperationMock, times(1)).addToscaDataToToscaElement(
            any(GraphVertex.class), eq(EdgeLabelEnum.OUTPUTS), eq(VertexTypeEnum.OUTPUTS), anyMap(), eq(JsonPresentationFields.NAME));
    }

    @Test
    public void test_addComponentInstanceOutputsToComponent_updateComponentInstanceOutput() {
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);
        final Map<String, List<ComponentInstanceOutput>> map = new HashMap<>();
        final List<ComponentInstanceOutput> componentInstanceOutputList = new ArrayList<>();
        final ComponentInstanceOutput componentInstanceOutput = new ComponentInstanceOutput();
        componentInstanceOutput.setComponentInstanceId(COMPONENT_ID);
        componentInstanceOutput.setComponentInstanceName(COMPONENT_ID);
        componentInstanceOutput.setName(COMPONENT_ID);
        componentInstanceOutputList.add(componentInstanceOutput);
        map.put("mock", componentInstanceOutputList);
        component.setComponentInstancesOutputs(map);

        doReturn(StorageOperationStatus.OK).when(nodeTemplateOperationMock)
            .updateComponentInstanceOutput(any(Component.class), anyString(), any(ComponentInstanceOutput.class));

        final Either<Map<String, List<ComponentInstanceOutput>>, StorageOperationStatus> result
            = testInstance.addComponentInstanceOutputsToComponent(component, map);
        assertNotNull(result);
        assertTrue(result.isLeft());
        assertFalse(result.left().value().isEmpty());
        assertSame(result.left().value(), map);
        verify(nodeTemplateOperationMock, times(1))
            .updateComponentInstanceOutput(any(Component.class), anyString(), any(ComponentInstanceOutput.class));

    }

    @Test
    public void test_addComponentInstanceOutputsToComponent_addComponentInstanceOutput() {
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);
        Map<String, List<ComponentInstanceOutput>> map = new HashMap<>();
        List<ComponentInstanceOutput> componentInstanceOutputList = new ArrayList<>();
        ComponentInstanceOutput componentInstanceOutput = new ComponentInstanceOutput();
        componentInstanceOutput.setComponentInstanceId(COMPONENT_ID);
        componentInstanceOutput.setComponentInstanceName(COMPONENT_ID);
        componentInstanceOutput.setName(COMPONENT_ID);
        componentInstanceOutputList.add(componentInstanceOutput);
        map.put("mock", componentInstanceOutputList);
        component.setComponentInstancesOutputs(map);

        map = new HashMap<>();
        componentInstanceOutputList = new ArrayList<>();
        componentInstanceOutput = new ComponentInstanceOutput();
        componentInstanceOutput.setComponentInstanceId("mock");
        componentInstanceOutput.setComponentInstanceName("mock");
        componentInstanceOutput.setName("mock");
        componentInstanceOutputList.add(componentInstanceOutput);
        map.put("mock", componentInstanceOutputList);

        final Either<Map<String, List<ComponentInstanceOutput>>, StorageOperationStatus> result = testInstance
            .addComponentInstanceOutputsToComponent(component, map);
        assertNotNull(result);
        assertTrue(result.isRight());
    }

    @Test
    public void test_addComponentInstanceAttributesToComponent() {
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);
        Map<String, List<ComponentInstanceAttribute>> map = new HashMap<>();
        List<ComponentInstanceAttribute> componentInstanceOutputList = new ArrayList<>();
        ComponentInstanceAttribute componentInstanceAttribute = new ComponentInstanceAttribute();
        componentInstanceAttribute.setComponentInstanceId(COMPONENT_ID);
        componentInstanceAttribute.setUniqueId(COMPONENT_ID);
        componentInstanceOutputList.add(componentInstanceAttribute);
        map.put("mock", componentInstanceOutputList);
        component.setComponentInstancesAttributes(map);

        doReturn(StorageOperationStatus.OK).when(nodeTemplateOperationMock)
            .updateComponentInstanceAttribute(any(Component.class), anyString(), any(ComponentInstanceAttribute.class));

        final Either<Map<String, List<ComponentInstanceAttribute>>, StorageOperationStatus> result
            = testInstance.addComponentInstanceAttributesToComponent(component, map);

        assertNotNull(result);
        assertTrue(result.isLeft());
        assertFalse(result.left().value().isEmpty());
        assertSame(result.left().value(), map);
        verify(nodeTemplateOperationMock, times(1))
            .updateComponentInstanceAttribute(any(Component.class), anyString(), any(ComponentInstanceAttribute.class));
    }

    @Test
    public void test_updateAttributeOfComponent_success() {
        final GraphVertex graphVertex = getTopologyTemplateVertex();
        final String componentId = "componentId";
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);
        doReturn(StorageOperationStatus.OK).when(nodeTypeOperationMock).updateToscaDataOfToscaElement(
            anyString(), eq(EdgeLabelEnum.ATTRIBUTES), eq(VertexTypeEnum.ATTRIBUTES), any(AttributeDefinition.class),
            eq(JsonPresentationFields.NAME));
        doReturn(Either.left(graphVertex)).when(janusGraphDaoMock).getVertexById(eq(componentId), any(JsonParseFlagEnum.class));

        final ToscaElement toscaElement = getToscaElementForTest();
        final Map<String, AttributeDataDefinition> attributes = new HashMap<>();
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setName("mock");
        attributes.put("mock", attributeDefinition);
        toscaElement.setAttributes(attributes);
        doReturn(Either.left(toscaElement)).when(topologyTemplateOperationMock)
            .getToscaElement(ArgumentMatchers.eq(graphVertex), any(ComponentParametersView.class));

        final Either<AttributeDefinition, StorageOperationStatus> result
            = testInstance.updateAttributeOfComponent(component, attributeDefinition);
        assertNotNull(result);
    }

    @Test
    public void test_updateAttributeOfComponent_isNotPresent() {
        final GraphVertex graphVertex = getTopologyTemplateVertex();
        final String componentId = "componentId";
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);
        doReturn(StorageOperationStatus.OK).when(nodeTypeOperationMock).updateToscaDataOfToscaElement(
            anyString(), eq(EdgeLabelEnum.ATTRIBUTES), eq(VertexTypeEnum.ATTRIBUTES), any(AttributeDefinition.class),
            eq(JsonPresentationFields.NAME));
        doReturn(Either.left(graphVertex)).when(janusGraphDaoMock).getVertexById(eq(componentId), any(JsonParseFlagEnum.class));

        final ToscaElement toscaElement = getToscaElementForTest();
        final Map<String, AttributeDataDefinition> attributes = new HashMap<>();
        final AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setName("mock");
        attributes.put("mock", attributeDefinition);
        toscaElement.setAttributes(attributes);
        doReturn(Either.left(toscaElement)).when(topologyTemplateOperationMock)
            .getToscaElement(ArgumentMatchers.eq(graphVertex), any(ComponentParametersView.class));

        final AttributeDefinition attributeDefinitionOneMore = new AttributeDefinition();
        attributeDefinitionOneMore.setName("Anothermock");

        final Either<AttributeDefinition, StorageOperationStatus> result
            = testInstance.updateAttributeOfComponent(component, attributeDefinitionOneMore);
        assertNotNull(result);
    }

    @Test
    public void test_updateComponentInstanceAttributes() {
        final GraphVertex graphVertex = getTopologyTemplateVertex();
        final String componentId = "componentId";
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);

        final List<ComponentInstanceAttribute> attributes = new ArrayList<>();
        final ComponentInstanceAttribute attributeDefinition = new ComponentInstanceAttribute();
        attributeDefinition.setName("mock");
        attributes.add(attributeDefinition);

        doReturn(StorageOperationStatus.OK).when(nodeTemplateOperationMock).updateComponentInstanceAttributes(component, componentId, attributes);

        final StorageOperationStatus result = testInstance.updateComponentInstanceAttributes(component, componentId, attributes);
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
        verify(nodeTemplateOperationMock, times(1)).updateComponentInstanceAttributes(component, componentId, attributes);
    }

    @Test
    public void test_updateComponentInstanceOutputs() {
        final GraphVertex graphVertex = getTopologyTemplateVertex();
        final String componentId = "componentId";
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);

        final List<ComponentInstanceOutput> list = new ArrayList<>();
        final ComponentInstanceOutput output = new ComponentInstanceOutput();
        output.setName("mock");
        list.add(output);

        doReturn(StorageOperationStatus.OK).when(nodeTemplateOperationMock).updateComponentInstanceOutputs(component, componentId, list);

        final StorageOperationStatus result = testInstance.updateComponentInstanceOutputs(component, componentId, list);
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
        verify(nodeTemplateOperationMock, times(1)).updateComponentInstanceOutputs(component, componentId, list);
    }

    @Test
    public void test_deleteOutputOfResource() {
        final Component component = new Resource();
        component.setUniqueId(COMPONENT_ID);

        doReturn(StorageOperationStatus.OK).when(nodeTypeOperationMock)
            .deleteToscaDataElement(anyString(), eq(EdgeLabelEnum.OUTPUTS), eq(VertexTypeEnum.OUTPUTS), anyString(), eq(JsonPresentationFields.NAME));

        final StorageOperationStatus result = testInstance.deleteOutputOfResource(component, "mock");
        assertNotNull(result);
        assertEquals(StorageOperationStatus.OK, result);
        verify(nodeTypeOperationMock, times(1))
            .deleteToscaDataElement(anyString(), eq(EdgeLabelEnum.OUTPUTS), eq(VertexTypeEnum.OUTPUTS), anyString(), eq(JsonPresentationFields.NAME));
    }

    @Test
    public void testDeleteResource_ResourceInUse() {
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertex.setUniqueId("1");
        GraphVertex usingComponent = getTopologyTemplateVertex();
        usingComponent.setUniqueId("2");
        Map<String, Object> metadataJson = new HashMap<>();
        metadataJson.put("COMPONENT_TYPE", "SERVICE");
        metadataJson.put("NAME", "serviceName");
        usingComponent.setMetadataJson(metadataJson);
        List<GraphVertex> inUseBy = new ArrayList<>();
        inUseBy.add(usingComponent);
        Map<String,Object> metadata = new HashMap<>();
        metadata.put("ex1", new Object());
        graphVertex.setMetadataJson(metadata);
        ToscaElement toscaElement = getToscaElementForTest();
        toscaElement.setUniqueId("1");
        List<GraphVertex> allResourcesToDelete = new ArrayList<>();
        allResourcesToDelete.add(graphVertex);
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, "1");

        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).
                thenReturn(Either.left(allResourcesToDelete));
        when(topologyTemplateOperationMock.
                getComponentByLabelAndId("1", ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll)).
                thenReturn(Either.left(graphVertex));
        doReturn(Either.left(toscaElement)).when(topologyTemplateOperationMock).getToscaElement(eq(graphVertex), any(ComponentParametersView.class));
        when(janusGraphDaoMock.getParentVertices(any(GraphVertex.class), any(), eq(JsonParseFlagEnum.ParseAll))).thenReturn(Either.left(inUseBy));
        assertThrows(OperationException.class, () -> testInstance.deleteComponent("1", NodeTypeEnum.Resource, false));
    }

    @Test
    public void testDeleteResource_WithTwoVersions() {
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertex.setUniqueId("1");
        Map<String,Object> metadata1 = new HashMap<>();
        metadata1.put("ex1", new Object());
        graphVertex.setMetadataJson(metadata1);
        ToscaElement toscaElement1 = getToscaElementForTest();
        toscaElement1.setUniqueId("1");
        ToscaElement toscaElement2 = getToscaElementForTest();
        toscaElement2.setUniqueId("2");
        GraphVertex graphVertex2 = getTopologyTemplateVertex();
        graphVertex2.setUniqueId("2");
        Map<String,Object> metadata2 = new HashMap<>();
        metadata2.put("ex2", new Object());
        graphVertex.setMetadataJson(metadata2);
        List<GraphVertex> parentVertices = new ArrayList<>();
        parentVertices.add(graphVertex2);
        List<String> affectedComponentIds = new ArrayList<>();
        affectedComponentIds.add(graphVertex.getUniqueId());
        affectedComponentIds.add(graphVertex2.getUniqueId());

        when(graphLockOperationMock.lockComponent(graphVertex.getUniqueId(), NodeTypeEnum.Resource)).
                thenReturn(StorageOperationStatus.OK);
        when(graphLockOperationMock.lockComponent(graphVertex2.getUniqueId(), NodeTypeEnum.Resource)).
                thenReturn(StorageOperationStatus.OK);
        when(topologyTemplateOperationMock.deleteToscaElement(graphVertex)).thenReturn(Either.left(toscaElement1));
        when(topologyTemplateOperationMock.deleteToscaElement(graphVertex2)).thenReturn(Either.left(toscaElement2));
        List<GraphVertex> allResourcesToDelete = new ArrayList<>();
        allResourcesToDelete.add(graphVertex);
        allResourcesToDelete.add(graphVertex2);
        Map<GraphPropertyEnum, Object> propertiesToMatch1 = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch1.put(GraphPropertyEnum.INVARIANT_UUID, "1");
        Map<GraphPropertyEnum, Object> propertiesToMatch2 = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch2.put(GraphPropertyEnum.INVARIANT_UUID, "2");
        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch1, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(allResourcesToDelete));
        when(topologyTemplateOperationMock.getComponentByLabelAndId(graphVertex.getUniqueId(), ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll))
                .thenReturn(Either.left(graphVertex));
        when(topologyTemplateOperationMock.getComponentByLabelAndId(graphVertex2.getUniqueId(), ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll))
                .thenReturn(Either.left(graphVertex2));
        doReturn(Either.left(toscaElement1)).when(topologyTemplateOperationMock).getToscaElement(eq(graphVertex), any(ComponentParametersView.class));
        doReturn(Either.left(toscaElement2)).when(topologyTemplateOperationMock).getToscaElement(eq(graphVertex2), any(ComponentParametersView.class));
        when(janusGraphDaoMock.getParentVertices(any(GraphVertex.class), any(), any())).thenReturn(Either.right(JanusGraphOperationStatus.OK));
        assertEquals(affectedComponentIds, testInstance.deleteComponent("1", NodeTypeEnum.Resource, false));
    }

    @Test
    public void testDeleteResource_WithOneVersion() {
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertex.setUniqueId("1");
        Map<String,Object> metadata = new HashMap<>();
        metadata.put("ex1", new Object());
        graphVertex.setMetadataJson(metadata);
        ToscaElement toscaElement = getToscaElementForTest();
        List<String> affectedComponentIds = new ArrayList<>();
        affectedComponentIds.add(graphVertex.getUniqueId());
        when(graphLockOperationMock.lockComponent(graphVertex.getUniqueId(), NodeTypeEnum.Resource)).
                thenReturn(StorageOperationStatus.OK);
        when(topologyTemplateOperationMock.deleteToscaElement(graphVertex)).thenReturn(Either.left(toscaElement));
        List<GraphVertex> allResourcesToDelete = new ArrayList<>();
        allResourcesToDelete.add(graphVertex);
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, "1");
        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(allResourcesToDelete));
        when(topologyTemplateOperationMock.getComponentByLabelAndId(any(), any(), any())).thenReturn(Either.left(graphVertex));
        doReturn(Either.left(toscaElement)).when(topologyTemplateOperationMock).getToscaElement(eq(graphVertex), any(ComponentParametersView.class));
        when(janusGraphDaoMock.getParentVertices(any(GraphVertex.class), any(), any())).thenReturn(Either.right(JanusGraphOperationStatus.OK));
        assertEquals(affectedComponentIds, testInstance.deleteComponent("1", NodeTypeEnum.Resource, true));
    }

    @Test
    public void testDeleteResource_FailDelete() {
        Map<GraphPropertyEnum, Object> metadataProperties = new HashMap<>();
        metadataProperties.put(GraphPropertyEnum.NAME, "graphVertex");
        GraphVertex graphVertex = getTopologyTemplateVertex();
        graphVertex.setUniqueId("1");
        graphVertex.setMetadataProperties(metadataProperties);
        ToscaElement toscaElement1 = getToscaElementForTest();
        toscaElement1.setUniqueId("1");
        List<String> affectedComponentIds = new ArrayList<>();
        affectedComponentIds.add(graphVertex.getUniqueId());

        List<GraphVertex> allResourcesToDelete = new ArrayList<>();
        allResourcesToDelete.add(graphVertex);
        when(graphLockOperationMock.lockComponent(graphVertex.getUniqueId(), NodeTypeEnum.Resource)).
                thenReturn(StorageOperationStatus.OK);
        when(topologyTemplateOperationMock.deleteToscaElement(graphVertex))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, "1");
        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).thenReturn(Either.left(allResourcesToDelete));
        doReturn(Either.left(toscaElement1)).when(topologyTemplateOperationMock).getToscaElement(eq(graphVertex), any(ComponentParametersView.class));
        when(topologyTemplateOperationMock.getComponentByLabelAndId(graphVertex.getUniqueId(), ToscaElementTypeEnum.TOPOLOGY_TEMPLATE, JsonParseFlagEnum.ParseAll))
                .thenReturn(Either.left(graphVertex));
        when(janusGraphDaoMock.getParentVertices(any(GraphVertex.class), any(), any())).thenReturn(Either.right(JanusGraphOperationStatus.OK));
        StorageException actualException = assertThrows(StorageException.class, () -> testInstance.deleteComponent("1", NodeTypeEnum.Resource, false));
        assertEquals(StorageOperationStatus.NOT_FOUND, actualException.getStorageOperationStatus());
        assertEquals(0, actualException.getParams().length);
    }

    @Test
    public void testDeleteResource_NotFound() {
        Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
        propertiesToMatch.put(GraphPropertyEnum.INVARIANT_UUID, "1");
        when(janusGraphDaoMock.getByCriteria(null, propertiesToMatch, JsonParseFlagEnum.ParseMetadata)).
                thenReturn(Either.right(JanusGraphOperationStatus.NOT_FOUND));
        StorageException actualException = assertThrows(StorageException.class, () -> testInstance.deleteComponent("1", NodeTypeEnum.Resource, false));
        assertEquals(StorageOperationStatus.NOT_FOUND, actualException.getStorageOperationStatus());
        assertEquals(0, actualException.getParams().length);
    }

    private StorageOperationStatus associateRequirementsToServiceWithStatus(StorageOperationStatus status) {
        Map<String, ListRequirementDataDefinition> requirementsMap = new HashedMap();
        String componentId = "componentid";

        ListRequirementDataDefinition listRequirementDataDefinition1 = new ListRequirementDataDefinition();
        requirementsMap.put("requirements1", listRequirementDataDefinition1);

        GraphVertex vertex;
        if (status == StorageOperationStatus.OK) {
            vertex = getTopologyTemplateVertex();
        } else {
            vertex = getNodeTypeVertex();
        }

        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = Either.left(vertex);
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.associateElementToData(eq(vertex),
            eq(VertexTypeEnum.REQUIREMENTS), eq(EdgeLabelEnum.REQUIREMENTS), anyMap())).thenReturn(Either.right(status));
        return testInstance.associateRequirementsToService(requirementsMap, componentId);
    }

    private Either<PolicyDefinition, StorageOperationStatus> associatePolicyToComponentWithStatus(StorageOperationStatus status) {
        PolicyDefinition policy = new PolicyDefinition();
        String componentId = "componentId";
        int counter = 0;
        GraphVertex vertex;
        if (status == StorageOperationStatus.OK) {
            vertex = getTopologyTemplateVertex();
        } else {
            vertex = getNodeTypeVertex();
        }
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = Either.left(vertex);
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.ParseMetadata)).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.addPolicyToToscaElement(eq(vertex), any(PolicyDefinition.class), anyInt())).thenReturn(status);
        return testInstance.associatePolicyToComponent(componentId, policy, counter);
    }

    private Either<PolicyDefinition, StorageOperationStatus> updatePolicyOfComponentWithStatus(StorageOperationStatus status) {
        PolicyDefinition policy = new PolicyDefinition();
        String componentId = "componentId";
        GraphVertex vertex = getTopologyTemplateVertex();
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(vertex));
        when(topologyTemplateOperationMock.updatePolicyOfToscaElement(eq(vertex), any(PolicyDefinition.class))).thenReturn(status);
        return testInstance.updatePolicyOfComponent(componentId, policy, PromoteVersionEnum.NONE);
    }

    private void removePolicyFromComponentWithStatus(StorageOperationStatus status) {
        String componentId = "componentId";
        String policyId = "policyId";
        GraphVertex vertex = getTopologyTemplateVertex();
        Either<GraphVertex, JanusGraphOperationStatus> getVertexEither = Either.left(vertex);
        when(janusGraphDaoMock.getVertexById(componentId, JsonParseFlagEnum.NoParse)).thenReturn(getVertexEither);
        when(topologyTemplateOperationMock.removePolicyFromToscaElement(vertex, policyId)).thenReturn(status);
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
