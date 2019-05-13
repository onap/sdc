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

package org.openecomp.sdc.be.datamodel;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.InputsBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetPolicyValueDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiComponentMetadata;
import org.openecomp.sdc.be.ui.model.UiServiceDataTransfer;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UiComponentDataConverterTest {

    private PolicyDefinition policy1, policy2;
    private GroupDefinition group1, group2;
    private InputDefinition input1;
    private PropertyDefinition propertyDef;
    private InterfaceDefinition interfaceDef;


    private static GroupTypeBusinessLogic groupTypeBusinessLogic;
    private static PolicyTypeBusinessLogic policyTypeBusinessLogic;
    private static UiComponentDataConverter uiComponentDataConverter;

    private static final String PROPERTY_UID = "propertyUid";
    private static final String NODE_FILTER_UID = "nodeFilterUid";
    private static final String COMPONENT_UID = "componentUid";

    @BeforeClass
    public static void initClass() {
        groupTypeBusinessLogic = mock(GroupTypeBusinessLogic.class);
        policyTypeBusinessLogic = mock(PolicyTypeBusinessLogic.class);
        uiComponentDataConverter = new UiComponentDataConverter(groupTypeBusinessLogic, policyTypeBusinessLogic);
    }

    @Before
    public void setUp() throws Exception {
        policy1 = PolicyDefinitionBuilder.create()
                .setName("policy1")
                .setUniqueId("uid1")
                .setType("a")
                .build();

        policy2 = PolicyDefinitionBuilder.create()
                .setName("policy2")
                .setUniqueId("uid2")
                .setType("b")
                .build();
        group1 = GroupDefinitionBuilder.create()
                .setUniqueId("group1")
                .setName("Group 1")
                .setType("a")
                .build();
        group2 = GroupDefinitionBuilder.create()
                .setUniqueId("group2")
                .setName("Group 2")
                .setType("b")
                .build();

        input1 = InputsBuilder.create()
                .setName("input1")
                .setPropertyId("inputid")
                .build();

        propertyDef = new PropertyDataDefinitionBuilder()
                .setName("propety1")
                .setValue("true")
                .setType("boolean")
                .setUniqueId("property1")
                .build();


    }

    @Test
    public void getUiDataTransferFromResourceByParams_groups_allGroups() {
        Resource resourceWithGroups = buildResourceWithGroups();
        UiComponentDataTransfer componentDTO1 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("PROPERTIES"));
        UiComponentDataTransfer componentDTO2 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("properties"));
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("groups"));
        assertThat(componentDTO.getGroups()).isEqualTo(resourceWithGroups.getGroups());
    }

    @Test
    public void getUiDataTransferFromResourceByParams_groups_excludedGroups() {
        Resource resourceWithGroups = buildResourceWithGroups();
        when(groupTypeBusinessLogic.getExcludedGroupTypes("VFC")).thenReturn(buildExcludedTypesList());
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("nonExcludedGroups"));
        List<GroupDefinition> groups = componentDTO.getGroups();
        assertThat(groups.size()).isEqualTo(1);
        assertThat(groups.get(0)).isEqualTo(group2);
    }

    @Test
    public void getUiDataTransferFromResourceByParams_policies_noPoliciesForResource() {
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromResourceByParams(new Resource(), Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEmpty();
    }

    @Test
    public void getUiDataTransferFromResourceByParams_All() {
        Resource resourceWithGroups = buildResourceWithGroups();
        Resource resourceWithInputs = buildResourceWithInputs();

        UiComponentDataTransfer componentDTO1 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("PROPERTIES"));
        UiComponentDataTransfer componentDTO2 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("properties"));
        UiComponentDataTransfer componentDTO3 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("interfaces"));
        UiComponentDataTransfer componentDTO4 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("attributes"));
        UiComponentDataTransfer componentDTO5 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("metadata"));
        UiComponentDataTransfer componentDTO6 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("derivedFrom"));
        UiComponentDataTransfer componentDTO7 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("additionalInformation"));

        UiComponentDataTransfer componentDTO8 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("inputs"));
        UiComponentDataTransfer componentDTO81 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithInputs, Collections.singletonList("inputs"));

        UiComponentDataTransfer componentDTO9 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("users"));
        UiComponentDataTransfer componentDTO10 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("componentInstances"));
        UiComponentDataTransfer componentDTO11 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("componentInstancesProperties"));
        UiComponentDataTransfer componentDTO12 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("capabilities"));
        UiComponentDataTransfer componentDTO13 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("requirements"));
        UiComponentDataTransfer componentDTO14 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("allVersions"));
        UiComponentDataTransfer componentDTO15 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("artifacts"));

        UiComponentDataTransfer componentDTO16 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("interfaces"));
        UiComponentDataTransfer componentDTO17 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("componentInstancesAttributes"));
        UiComponentDataTransfer componentDTO18= uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("componentInstancesInputs"));
        UiComponentDataTransfer componentDTO19 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("toscaArtifacts"));

        UiComponentDataTransfer componentDTO21= uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("componentInstancesRelations"));
        UiComponentDataTransfer componentDTO20 = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("deploymentArtifacts"));

        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithGroups, Collections.singletonList("groups"));
        assertThat(componentDTO.getGroups()).isEqualTo(resourceWithGroups.getGroups());
    }

    @Test
    public void getUiDataTransferFromServiceByParams_policies_noPoliciesForResource() {
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromServiceByParams(new Service(), Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEmpty();
    }

    @Test
    public void getUiDataTransferFromServiceByParams_SERVICE_API_ARTIFACTS() {
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromServiceByParams(new Service(), Collections.singletonList("serviceApiArtifacts"));
        assertThat(componentDTO.getArtifacts()).isNull();
    }

    @Test
    public void getUiDataTransferFromServiceByParams_FORWARDING_PATHS() {
        UiServiceDataTransfer componentDTO = (UiServiceDataTransfer) uiComponentDataConverter.getUiDataTransferFromServiceByParams(new Service(), Collections.singletonList("forwardingPaths"));
        assertThat(componentDTO.getForwardingPaths()).isEmpty();
    }

    @Test
    public void getUiDataTransferFromServiceByParams_METADATA() {
        UiServiceDataTransfer componentDTO = (UiServiceDataTransfer) uiComponentDataConverter.getUiDataTransferFromServiceByParams(new Service(), Collections.singletonList("metadata"));
        assertThat(componentDTO.getMetadata().getNamingPolicy()).isEqualTo("");
    }

    @Test
    public void getUiDataTransferFromServiceByParams_INTERFACES() {
        UiServiceDataTransfer componentDTO = (UiServiceDataTransfer) uiComponentDataConverter.getUiDataTransferFromServiceByParams(new Service(), Collections.singletonList("interfaces"));
        assertThat(componentDTO.getInterfaces()).isEmpty();
    }

    @Test
    public void getUiDataTransferFromResourceByParams_policies() {
        Resource resourceWithPolicies = buildResourceWithPolicies();
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithPolicies, Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEqualTo(resourceWithPolicies.resolvePoliciesList());
    }

    @Test
    public void getUiDataTransferFromServiceByParams_policies() {
        Service resourceWithPolicies = buildServiceWithPolicies();
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromServiceByParams(resourceWithPolicies, Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEqualTo(resourceWithPolicies.resolvePoliciesList());
    }

    @Test
    public void getUiDataTransferFromResourceByParams_policies_excludedPolicies() {
        Resource resourceWithPolicies = buildResourceWithPolicies();
        when(policyTypeBusinessLogic.getExcludedPolicyTypes("VFC")).thenReturn(buildExcludedTypesList());
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithPolicies, Collections.singletonList("nonExcludedPolicies"));
        List<PolicyDefinition> policies = componentDTO.getPolicies();
        assertThat(policies.size()).isEqualTo(1);
        assertThat(policies.get(0)).isEqualTo(policy2);
    }

    @Test
    public void convertToUiComponentMetadataTestResource() {
        Resource resourceWithPolicies = buildResourceWithPolicies();
        when(policyTypeBusinessLogic.getExcludedPolicyTypes("VFC")).thenReturn(buildExcludedTypesList());
        UiComponentMetadata componentMd = uiComponentDataConverter.convertToUiComponentMetadata(resourceWithPolicies);
        assertThat(componentMd.getComponentType().getValue()).isEqualTo("Resource");

    }

    @Test
    public void convertToUiComponentMetadataTestService() {
        Service resourceWithPolicies = buildServiceWithPolicies();
        when(policyTypeBusinessLogic.getExcludedPolicyTypes("VFC")).thenReturn(buildExcludedTypesList());
        UiComponentMetadata componentMd = uiComponentDataConverter.convertToUiComponentMetadata(resourceWithPolicies);
        assertThat(componentMd.getComponentType().getValue()).isEqualTo("Service");

    }
    @Test
    public void getResourceWithoutGroupsAndPolicies_returnsEmptyLists() {
        Resource resource = new ResourceBuilder().build();
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resource, Arrays.asList("nonExcludedPolicies", "nonExcludedGroups"));
        List<PolicyDefinition> policies = componentDTO.getPolicies();
        assertThat(policies.size()).isZero();
        List<GroupDefinition> groups = componentDTO.getGroups();
        assertThat(groups.size()).isZero();
    }

    @Test
    public void testGetDeclaredPolicies() {
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setName(PROPERTY_UID);

        GetPolicyValueDataDefinition getPolicy = new GetPolicyValueDataDefinition();
        getPolicy.setPolicyId(PROPERTY_UID);
        getPolicy.setPropertyName(PROPERTY_UID);
        property.setGetPolicyValues(Collections.singletonList(getPolicy));

        Map<String, List<ComponentInstanceProperty>> instanceProperties = new HashMap<>();
        instanceProperties.put(COMPONENT_UID, Collections.singletonList(property));

        Resource resource = new ResourceBuilder().build();
        resource.setComponentInstancesProperties(instanceProperties);

        UiComponentDataTransfer uiComponentDataTransfer = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resource,
                Collections.singletonList("policies"));

        assertThat(CollectionUtils.isNotEmpty(uiComponentDataTransfer.getPolicies()));
    }

    @Test
    public void testGetNodeFilterEmptyList() {
        Resource resource = new ResourceBuilder().build();
        UiComponentDataTransfer uiComponentDataTransfer = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resource,
                Collections.singletonList("nodeFilter"));

        assertThat(MapUtils.isEmpty(uiComponentDataTransfer.getNodeFilter()));
    }

    @Test
    public void testGetNodeFilter() {
        CINodeFilterDataDefinition nodeFilter = new CINodeFilterDataDefinition();
        nodeFilter.setID(NODE_FILTER_UID);

        Map<String, CINodeFilterDataDefinition> nodeFilterMap = new HashMap<>();
        nodeFilterMap.put(NODE_FILTER_UID, nodeFilter);

        Resource resource = new ResourceBuilder().build();
        resource.setNodeFilterComponents(nodeFilterMap);

        UiComponentDataTransfer uiComponentDataTransfer = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resource,
                Collections.singletonList("nodeFilter"));

        assertThat(MapUtils.isNotEmpty(uiComponentDataTransfer.getNodeFilter()));
    }

    private Resource buildResourceWithGroups() {
        return new ResourceBuilder()
                .addGroup(group1)
                .addGroup(group2)
                .build();
    }

    private Resource buildResourceWithInputs() {
        return new ResourceBuilder()
                .addInput(input1)
                .build();
    }

    private Resource buildResourceWithParameter(String field) {
        ResourceBuilder res =  new ResourceBuilder();
        switch(field){
            case "inputs":
                res.addInput(input1);
                break;
            case "properties":
                //res.addProperty(propertyDef);
                break;


        }


        return new ResourceBuilder()
                .addInput(input1)
                .build();
    }

    private Resource buildResourceWithPolicies() {
        return new ResourceBuilder()
                .addPolicy(policy1)
                .addPolicy(policy2)
                .build();
    }

    private Service buildServiceWithPolicies() {
        return new ServiceBuilder()
                .addPolicy(policy1)
                .addPolicy(policy2)
                .build();
    }

    private Set<String> buildExcludedTypesList() {
        return asSet("a");
    }

}
