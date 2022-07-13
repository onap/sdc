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

package org.openecomp.sdc.be.tosca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.exceptions.SdcResourceNotFoundException;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.tosca.model.ToscaMetadata;
import org.openecomp.sdc.be.tosca.model.ToscaPolicyTemplate;

@ExtendWith(MockitoExtension.class)
class PolicyExportParserImplTest {

    private static final String[] POLICY_KEYS = {"policy_key_1", "policy_key_2"};
    private static final String[] VERSIONS = {"version_1", "version_1"};
    private static final String[] POLICY_NAMES = {"name_1", "name_2"};
    private static final String[] POLICY_UUIDS = {"policyUUID_1", "policyUUID_2"};
    private static final String[] INVARIANT_UUIDS = {"invariantUUID_1", "invariantUUID_2"};
    private static final String[] POLICY_TYPE_NAMES = {"policyTypeName_1", "policyTypeName_2"};
    private static final String[] POLICY_COMPONENT_INSTANCES = {"policyComponentInstanceId"};
    private static final String POLICY_COMPONENT_INSTANCES_NAME = "policyComponentInstanceName";
    private static final String[] POLICY_GROUPS = {"policyGroupId"};
    private static final String POLICY_GROUP_NAME = "PolicyGroupName";

    private PolicyExportParser policiyExportParser;

    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;
    @Mock
    private PropertyConvertor propertyConvertor;

    @Mock
    private Component component;

    @Test
    void failToGetAllDataTypes() {

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.right(null));
        assertThatExceptionOfType(SdcResourceNotFoundException.class).isThrownBy(() -> policiyExportParser = new PolicyExportParserImpl(
            applicationDataTypeCache,
            propertyConvertor));
    }

    @Test
    void noPoliciesInComponent() {

        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(null));
        when(component.getPolicies()).thenReturn(null);
        policiyExportParser = new PolicyExportParserImpl(applicationDataTypeCache, propertyConvertor);
        Map<String, ToscaPolicyTemplate> policies = policiyExportParser.getPolicies(component);
        assertNull(policies);
    }

    @Test
    void onePoliciesInComponent() {

        List<Integer> constIndexes = Arrays.asList(new Integer[]{0});
        testPoliciesInComponent(constIndexes);
    }

    @Test
    void twoPoliciesInComponent() {

        List<Integer> constIndexes = Arrays.asList(new Integer[]{0, 1});
        testPoliciesInComponent(constIndexes);
    }

    private void testPoliciesInComponent(List<Integer> constIndexes) {
        when(applicationDataTypeCache.getAll(null)).thenReturn(Either.left(null));
        Map<String, PolicyDefinition> policiesToAdd = getPolicies(constIndexes);

        when(component.getPolicies()).thenReturn(policiesToAdd);
        when(component.getComponentInstances()).thenReturn(getComponentInstances());
        when(component.getGroups()).thenReturn(getGroups());
        policiyExportParser = new PolicyExportParserImpl(applicationDataTypeCache, propertyConvertor);

        Map<String, ToscaPolicyTemplate> policies = policiyExportParser.getPolicies(component);

        for (Integer i : constIndexes) {

            ToscaPolicyTemplate toscaPolicyTemplate = policies.get(POLICY_NAMES[i]);
            ToscaMetadata metadata = (ToscaMetadata) toscaPolicyTemplate.getMetadata();

            assertThat(metadata.getInvariantUUID()).isEqualTo(INVARIANT_UUIDS[i]);
            assertThat(metadata.getUUID()).isEqualTo(POLICY_UUIDS[i]);
            assertThat(metadata.getName()).isEqualTo(POLICY_NAMES[i]);
            assertThat(metadata.getVersion()).isEqualTo(VERSIONS[i]);

            String type = toscaPolicyTemplate.getType();
            assertThat(type).isEqualTo(POLICY_TYPE_NAMES[i]);

            List<String> targets = toscaPolicyTemplate.getTargets();
            assertThat(targets.get(0)).isEqualTo(POLICY_COMPONENT_INSTANCES_NAME);
            assertThat(targets.get(1)).isEqualTo(POLICY_GROUP_NAME);
        }
    }

    private List<GroupDefinition> getGroups() {
        List<GroupDefinition> groups = new ArrayList<>();
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setUniqueId(POLICY_GROUPS[0]);
        groupDefinition.setName(POLICY_GROUP_NAME);
        groups.add(groupDefinition);
        return groups;
    }

    private List<ComponentInstance> getComponentInstances() {
        List<ComponentInstance> componentInstances = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(POLICY_COMPONENT_INSTANCES[0]);
        componentInstance.setName(POLICY_COMPONENT_INSTANCES_NAME);
        componentInstances.add(componentInstance);
        return componentInstances;
    }

    private Map<String, PolicyDefinition> getPolicies(List<Integer> indexes) {
        Map<String, PolicyDefinition> policies = new HashMap<>();

        for (int index : indexes) {

            PolicyDefinition policyDefinition = new PolicyDefinition();

            // Set type
            policyDefinition.setPolicyTypeName(POLICY_TYPE_NAMES[index]);

            // Set Metadata
            policyDefinition.setInvariantUUID(INVARIANT_UUIDS[index]);
            policyDefinition.setPolicyUUID(POLICY_UUIDS[index]);
            policyDefinition.setName(POLICY_NAMES[index]);
            policyDefinition.setVersion(VERSIONS[index]);

            // Set targets
            policyDefinition.setTargets(getTargets());

            policies.put(POLICY_KEYS[index], policyDefinition);
        }
        return policies;
    }

    private Map<PolicyTargetType, List<String>> getTargets() {
        Map<PolicyTargetType, List<String>> targets = new HashMap<>();
        targets.put(PolicyTargetType.COMPONENT_INSTANCES, Arrays.asList(POLICY_COMPONENT_INSTANCES));
        targets.put(PolicyTargetType.GROUPS, Arrays.asList(POLICY_GROUPS));
        return targets;
    }
}
