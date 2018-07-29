package org.openecomp.sdc.be.datamodel;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;

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
    private static GroupTypeBusinessLogic groupTypeBusinessLogic;
    private static PolicyTypeBusinessLogic policyTypeBusinessLogic;
    private static UiComponentDataConverter uiComponentDataConverter;

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
    }

    @Test
    public void getUiDataTransferFromResourceByParams_groups_allGroups() {
        Resource resourceWithGroups = buildResourceWithGroups();
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
    public void getUiDataTransferFromServiceByParams_policies_noPoliciesForResource() {
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromServiceByParams(new Service(), Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEmpty();
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
    public void getResourceWithoutGroupsAndPolicies_returnsEmptyLists() {
        Resource resource = new ResourceBuilder().build();
        UiComponentDataTransfer componentDTO = uiComponentDataConverter.getUiDataTransferFromResourceByParams(resource, Arrays.asList("nonExcludedPolicies", "nonExcludedGroups"));
        List<PolicyDefinition> policies = componentDTO.getPolicies();
        assertThat(policies.size()).isZero();
        List<GroupDefinition> groups = componentDTO.getGroups();
        assertThat(groups.size()).isZero();
    }

    private Resource buildResourceWithGroups() {
        return new ResourceBuilder()
                .addGroup(group1)
                .addGroup(group2)
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
