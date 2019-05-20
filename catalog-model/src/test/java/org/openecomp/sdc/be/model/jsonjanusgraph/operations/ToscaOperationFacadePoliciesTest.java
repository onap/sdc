package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.config.JanusGraphSpringConfig;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.config.ModelOperationsSpringConfig;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JanusGraphSpringConfig.class, ModelOperationsSpringConfig.class})
public class ToscaOperationFacadePoliciesTest extends ModelTestBase {

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;
    @Autowired
    private HealingJanusGraphDao janusGraphDao;

    private PolicyDefinition policy1, policy2;

    @BeforeClass
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @Before
    public void setUp() throws Exception {
        policy1 = createPolicyDefinition("type1");
        policy2 = createPolicyDefinition("type2");
        createContainerVertexInDB();
        createPoliciesOnGraph(policy1, policy2);
    }

    private void createPoliciesOnGraph(PolicyDefinition ... policies) {
        for (int i = 0; i < policies.length; i++) {
            PolicyDefinition policy = policies[i];
            Either<PolicyDefinition, StorageOperationStatus> createdPolicy = toscaOperationFacade.associatePolicyToComponent(CONTAINER_ID, policy, i);
            assertTrue(createdPolicy.isLeft());
        }
    }

    @After
    public void tearDown() {
        janusGraphDao.rollback();
    }

    @Test
    public void updatePoliciesTargetsOfComponent_updateSinglePolicy() {
        List<String> updatedTargetIds = asList("instance1new", "instance2");
        PolicyDefinition originalPolicy2 = clonePolicyWithTargets(policy2);
        updatePolicyTypeTargetsIds(policy1, PolicyTargetType.COMPONENT_INSTANCES, updatedTargetIds);
        updatePolicyTypeTargetsIds(policy2, PolicyTargetType.COMPONENT_INSTANCES, updatedTargetIds);

        StorageOperationStatus storageOperationStatus = toscaOperationFacade.updatePoliciesOfComponent(CONTAINER_ID, singletonList(policy1));
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        Component updatedComponent = fetchComponentFromDB();
        verifyPolicyTargets(updatedComponent.getPolicyById(policy1.getUniqueId()), policy1);
        verifyPolicyTargets(updatedComponent.getPolicyById(policy2.getUniqueId()), originalPolicy2);
    }

    @Test
    public void updatePoliciesTargetsOfComponent_updateMultiplePolicies() {
        List<String> updatedTargetIds = asList("instance1new", "instance2");
        updatePolicyTypeTargetsIds(policy1, PolicyTargetType.COMPONENT_INSTANCES, updatedTargetIds);
        updatePolicyTypeTargetsIds(policy2, PolicyTargetType.COMPONENT_INSTANCES, updatedTargetIds);
        StorageOperationStatus storageOperationStatus = toscaOperationFacade.updatePoliciesOfComponent(CONTAINER_ID, asList(policy1, policy2));
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        Component updatedComponent = fetchComponentFromDB();
        verifyPolicyTargets(updatedComponent.getPolicyById(policy1.getUniqueId()), policy1);
        verifyPolicyTargets(updatedComponent.getPolicyById(policy2.getUniqueId()), policy2);
    }

    private PolicyDefinition clonePolicyWithTargets(PolicyDefinition policy) {
        PolicyDefinition originalPolicy = new PolicyDefinition(policy);
        Map<PolicyTargetType, List<String>> clonedTargetMap = policy.getTargets().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
        originalPolicy.setTargets(clonedTargetMap);
        return originalPolicy;
    }

    private void verifyPolicyTargets(PolicyDefinition updatedPolicy, PolicyDefinition expectedPolicy) {
        assertThat(updatedPolicy.getTargets())
                .isEqualTo(expectedPolicy.getTargets());
    }

    private void updatePolicyTypeTargetsIds(PolicyDefinition policy, PolicyTargetType targetType, List<String> updatedTargetIds) {
        policy.getTargets().put(targetType, updatedTargetIds);
    }

    private Component fetchComponentFromDB() {
        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnorePolicies(false);
        return toscaOperationFacade.getToscaElement(CONTAINER_ID, componentParametersView).left().value();
    }

    private void createContainerVertexInDB() {
        GraphVertex resource = createBasicContainerGraphVertex();
        Either<GraphVertex, JanusGraphOperationStatus> container = janusGraphDao.createVertex(resource);
        assertTrue(container.isLeft());
    }

    private PolicyDefinition createPolicyDefinition(String type) {
        PolicyDefinition policy = new PolicyDefinition();
        policy.setPolicyTypeName(type);
        policy.setTargets(new HashMap<>());
        policy.getTargets().put(PolicyTargetType.COMPONENT_INSTANCES, asList("instance1", "instance2"));
        return policy;
    }
}
