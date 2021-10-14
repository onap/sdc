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

import static org.apache.commons.collections.ListUtils.union;
import static org.assertj.core.api.Assertions.assertThat;

import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.config.JanusGraphSpringConfig;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {JanusGraphSpringConfig.class, ModelOperationsSpringConfig.class})
public class PolicyOperationIntegrationTest extends ModelTestBase {

    public static final String POLICY_ID = "policy";
    private static final String CONTAINER_ID = "container";
    @Resource
    private TopologyTemplateOperation topologyTemplateOperation;
    @Resource
    private HealingJanusGraphDao janusGraphDao;
    @Resource
    private PolicyOperation policyOperation;
    private PropertyDataDefinition prop1, prop2;
    private PolicyDefinition policy;

    @BeforeAll
    public static void setupBeforeClass() {

        ModelTestBase.init();
    }

    @BeforeEach
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
        topologyTemplateOperation.addToscaDataToToscaElement(loadedResource, EdgeLabelEnum.POLICIES, VertexTypeEnum.POLICIES, policy,
            JsonPresentationFields.UNIQUE_ID);
//        janusGraphDao.commit();
    }

    @AfterEach
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
        Either<ToscaElement, StorageOperationStatus> loadedCmptEither = topologyTemplateOperation.getToscaElement(CONTAINER_ID,
            componentParametersView);

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
