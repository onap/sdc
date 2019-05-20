package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PolicyOperationTest {

    private static final String CONTAINER_ID = "containerId";
    private static final String POLICY_ID = "policyId";
    @InjectMocks
    private PolicyOperation testInstance;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private TopologyTemplateOperation topologyTemplateOperation;
    @Captor
    private ArgumentCaptor<PolicyDefinition> policyDefCaptor;
    private Component component;
    private PolicyDefinition policyDefinition;
    private PropertyDataDefinition prop1, prop2;

    @Before
    public void setUp() throws Exception {
        component = new Resource();
        component.setUniqueId(CONTAINER_ID);
        policyDefinition = new PolicyDefinition();
        policyDefinition.setUniqueId(POLICY_ID);
        prop1 = new PropertyDataDefinition();
        prop1.setName("prop1");
        prop1.setValue("prop1");

        prop2 = new PropertyDataDefinition();
        prop2.setName("prop2");
        prop2.setValue("prop2");
        policyDefinition.setProperties(Arrays.asList(prop1, prop2));
        component.setPolicies(Collections.singletonMap(POLICY_ID, policyDefinition));
    }

    @Test
    public void updatePolicyProperties_failedToFetchContainer() {
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.right(
            JanusGraphOperationStatus.NOT_FOUND));
        StorageOperationStatus storageOperationStatus = testInstance.updatePolicyProperties(component, POLICY_ID, Collections.emptyList());
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.NOT_FOUND);
        verifyZeroInteractions(topologyTemplateOperation);
    }

    @Test
    public void updatePolicyProperties_updateFailed() {
        GraphVertex cmptVertex = new GraphVertex();
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(cmptVertex));
        when(topologyTemplateOperation.updatePolicyOfToscaElement(cmptVertex, policyDefinition)).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        StorageOperationStatus storageOperationStatus = testInstance.updatePolicyProperties(component, POLICY_ID, Collections.emptyList());
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.GENERAL_ERROR);
    }

    @Test
    public void updatePolicyProperties() {
        GraphVertex cmptVertex = new GraphVertex();
        when(janusGraphDao.getVertexById(CONTAINER_ID, JsonParseFlagEnum.NoParse)).thenReturn(Either.left(cmptVertex));
        when(topologyTemplateOperation.updatePolicyOfToscaElement(eq(cmptVertex), policyDefCaptor.capture())).thenReturn(StorageOperationStatus.OK);
        PropertyDataDefinition prop1Copy = new PropertyDataDefinition(prop1);
        prop1Copy.setValue("prop1Copy");
        StorageOperationStatus storageOperationStatus = testInstance.updatePolicyProperties(component, POLICY_ID, Collections.singletonList(prop1Copy));
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        assertThat(policyDefCaptor.getValue().getProperties()).usingElementComparatorOnFields("value")
                .containsExactlyInAnyOrder(prop1Copy, prop2);
    }
}
