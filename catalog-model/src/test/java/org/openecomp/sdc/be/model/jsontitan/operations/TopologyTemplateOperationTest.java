package org.openecomp.sdc.be.model.jsontitan.operations;

import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
//error scenarios
public class TopologyTemplateOperationTest {

    private static final String CONTAINER_ID = "containerId";
    @InjectMocks
    private TopologyTemplateOperation topologyTemplateOperation;
    @Mock
    private TitanDao titanDao;

    @Test
    public void overrideToscaDataOfToscaElement_failedToFetchContainerVertex() {
        when(titanDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(TitanOperationStatus.INVALID_ID));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.overrideToscaDataOfToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, Collections.emptyMap());
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.INVALID_ID);
    }

    @Test
    public void overrideToscaDataOfToscaElement_failedToFetchDataVertex() {
        GraphVertex containerVertex = new GraphVertex();
        when(titanDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(containerVertex));
        when(titanDao.getChildVertex(containerVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, JsonParseFlagEnum.ParseJson)).thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.overrideToscaDataOfToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, Collections.emptyMap());
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void updateToscaDataDeepElements_failedToFetchContainerVertex() {
        when(titanDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(TitanOperationStatus.INVALID_ID));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.updateToscaDataDeepElementsBlockToToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, new MapCapabilityProperty(), "");
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.INVALID_ID);
    }

    @Test
    public void updateToscaDataDeepElements_failedToFetchDataVertex() {
        GraphVertex containerVertex = new GraphVertex();
        when(titanDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(containerVertex));
        when(titanDao.getChildVertex(containerVertex, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, JsonParseFlagEnum.ParseJson)).thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        StorageOperationStatus storageOperationStatus = topologyTemplateOperation.updateToscaDataDeepElementsBlockToToscaElement(CONTAINER_ID, EdgeLabelEnum.CALCULATED_CAP_PROPERTIES, new MapCapabilityProperty(), "");
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }
    
    @Test
    public void addPolicyToToscaElementSuccessTest(){
        TitanOperationStatus status = TitanOperationStatus.OK;
        StorageOperationStatus result = addPolicyToToscaElementWithStatus(status);
        assertThat(result).isEqualTo(StorageOperationStatus.OK);
    }
    
    @Test
    public void addPolicyToToscaElementFailureTest(){
        TitanOperationStatus status = TitanOperationStatus.ALREADY_EXIST;
        StorageOperationStatus result = addPolicyToToscaElementWithStatus(status);
        assertThat(result).isEqualTo(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
    }

    @SuppressWarnings("unchecked")
    private StorageOperationStatus addPolicyToToscaElementWithStatus(TitanOperationStatus status) {
        GraphVertex componentV = new GraphVertex();
        componentV.setVertex(Mockito.mock(TitanVertex.class));
        GraphVertex dataV = new GraphVertex();
        dataV.setVertex(Mockito.mock(TitanVertex.class));
        String componentName = "componentName";
        String componentId = UniqueIdBuilder.buildResourceUniqueId();
        String policyTypeName = "org.openecomp.policies.placement.valet.Affinity";
        componentV.setJsonMetadataField(JsonPresentationFields.NAME, componentName);
        componentV.setUniqueId(componentId);
        PolicyDefinition policy = new PolicyDefinition();
        policy.setPolicyTypeName(policyTypeName);
        int counter = 0;
        Either<GraphVertex, TitanOperationStatus> toscaDataVertexRes = Either.right(TitanOperationStatus.NOT_FOUND);
        when(titanDao.getChildVertex(eq(componentV), eq(EdgeLabelEnum.POLICIES), eq(JsonParseFlagEnum.ParseJson))).thenReturn(toscaDataVertexRes);
        Either<GraphVertex, TitanOperationStatus> createVertex = Either.left(dataV);
        when(titanDao.createVertex(any(GraphVertex.class))).thenReturn(createVertex);
        when(titanDao.createEdge(any(TitanVertex.class), any(TitanVertex.class), any(EdgeLabelEnum.class), any(HashMap.class))).thenReturn(status);
        return topologyTemplateOperation.addPolicyToToscaElement(componentV, policy, counter);
    }
    
}
