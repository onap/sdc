package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.config.JanusGraphSpringConfig;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.config.ModelOperationsSpringConfig;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.TopologyTemplate;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.ListUtils.union;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JanusGraphSpringConfig.class, ModelOperationsSpringConfig.class})
public class PolicyOperationIntegrationTest extends ModelTestBase {

    private static final String CONTAINER_ID = "container";
    public static final String POLICY_ID = "policy";
    @Resource
    private TopologyTemplateOperation topologyTemplateOperation;
    @Resource
    private HealingJanusGraphDao janusGraphDao;
    @Resource
    private PolicyOperation policyOperation;
    private PropertyDataDefinition prop1, prop2;
    private PolicyDefinition policy;

    @BeforeClass
    public static void setupBeforeClass() {

        ModelTestBase.init();
    }

    @Before
    public void setUp() throws Exception {
        prop1 = new PropertyDataDefinition();
        prop1.setUniqueId("prop1");
        prop1.setName("prop1");
        prop1.setValue("prop1");

        prop2 = new PropertyDataDefinition();
        prop2.setUniqueId("prop2");
        prop2.setName("prop2");
        prop2.setValue("prop2");

        policy = new PolicyDefinition();
        policy.setUniqueId(POLICY_ID);

        policy.setProperties(Arrays.asList(prop1, prop2));

        GraphVertex resource = new GraphVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        resource.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, CONTAINER_ID);
        janusGraphDao.createVertex(resource);
        GraphVertex loadedResource = janusGraphDao.getVertexById(CONTAINER_ID).left().value();
        topologyTemplateOperation.addToscaDataToToscaElement(loadedResource, EdgeLabelEnum.POLICIES, VertexTypeEnum.POLICIES, policy, JsonPresentationFields.UNIQUE_ID);
//        janusGraphDao.commit();
    }

    @After
    public void tearDown() {
        janusGraphDao.rollback();
    }

    @Test
    public void testUpdatePolicyProperties_singleProperty() {
        PropertyDataDefinition prop1Copy = new PropertyDataDefinition(prop1);
        prop1Copy.setValue("prop1NewValue");
        testUpdatePolicyProperties(Collections.singletonList(prop1Copy), Collections.singletonList(prop2));
    }

    @Test
    public void testUpdatePolicyProperties_multipleProperties() {
        PropertyDataDefinition prop1Copy = new PropertyDataDefinition(prop1);
        prop1Copy.setValue("prop1NewValue");

        PropertyDataDefinition prop2Copy = new PropertyDataDefinition(prop2);
        prop2Copy.setValue("prop2NewValue");

        testUpdatePolicyProperties(Arrays.asList(prop1Copy, prop2Copy), Collections.emptyList());
    }

    private void testUpdatePolicyProperties(List<PropertyDataDefinition> updatedProperties, List<PropertyDataDefinition> nonUpdatedPropeties) {
        Component cmpt = new org.openecomp.sdc.be.model.Resource();
        cmpt.setUniqueId(CONTAINER_ID);
        cmpt.setPolicies(Collections.singletonMap(POLICY_ID, policy));
        StorageOperationStatus storageOperationStatus = policyOperation.updatePolicyProperties(cmpt, POLICY_ID, updatedProperties);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);

        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnorePolicies(false);
        Either<ToscaElement, StorageOperationStatus> loadedCmptEither = topologyTemplateOperation.getToscaElement(CONTAINER_ID, componentParametersView);

        assertThat(loadedCmptEither.isLeft()).isTrue();
        ToscaElement loadedCmpt = loadedCmptEither.left().value();
        assertThat(loadedCmpt).isInstanceOf(TopologyTemplate.class);
        @SuppressWarnings("unchecked") List<PropertyDataDefinition> allProperties = union(updatedProperties, nonUpdatedPropeties);
        verifyPolicyPropertiesValuesUpdated((TopologyTemplate) loadedCmpt, allProperties);
    }

    private void verifyPolicyPropertiesValuesUpdated(TopologyTemplate toscaElement, List<PropertyDataDefinition> expectedUpdatedProperties) {
        Map<String, PolicyDataDefinition> policies = toscaElement.getPolicies();
        PolicyDataDefinition policy = policies.get(POLICY_ID);
        List<PropertyDataDefinition> policyProperties = policy.getProperties();
        assertThat(policyProperties).usingElementComparatorOnFields("value")
                                    .containsAll(expectedUpdatedProperties);
    }
}
