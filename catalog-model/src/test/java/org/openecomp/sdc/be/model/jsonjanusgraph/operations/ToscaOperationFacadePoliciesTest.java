/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import fj.data.Either;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.config.JanusGraphSpringConfig;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.config.ModelOperationsSpringConfig;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {JanusGraphSpringConfig.class, ModelOperationsSpringConfig.class})
public class ToscaOperationFacadePoliciesTest extends ModelTestBase {

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;
    @Autowired
    private HealingJanusGraphDao janusGraphDao;

    private PolicyDefinition policy1, policy2;

    @BeforeAll
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @BeforeEach
    public void setUp() throws Exception {
        policy1 = createPolicyDefinition("type1");
        policy2 = createPolicyDefinition("type2");
        createContainerVertexInDB();
        createPoliciesOnGraph(policy1, policy2);
    }

    private void createPoliciesOnGraph(PolicyDefinition... policies) {
        for (int i = 0; i < policies.length; i++) {
            PolicyDefinition policy = policies[i];
            Either<PolicyDefinition, StorageOperationStatus> createdPolicy = toscaOperationFacade.associatePolicyToComponent(CONTAINER_ID, policy, i);
            assertTrue(createdPolicy.isLeft());
        }
    }

    @AfterEach
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
        Map<PolicyTargetType, List<String>> clonedTargetMap = policy.getTargets().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new ArrayList<>(entry.getValue())));
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
