package org.openecomp.sdc.be.datamodel;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class UiComponentDataConverterTest {

    private PolicyDefinition policy1, policy2;

    @Before
    public void setUp() throws Exception {
        policy1 = PolicyDefinitionBuilder.create()
                .setName("policy1")
                .setUniqueId("uid1")
                .build();

        policy2 = PolicyDefinitionBuilder.create()
                .setName("policy2")
                .setUniqueId("uid2")
                .build();
    }

    @Test
    public void getUiDataTransferFromResourceByParams_policies_noPoliciesForResource() {
        UiComponentDataTransfer componentDTO = UiComponentDataConverter.getUiDataTransferFromResourceByParams(new Resource(), Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEmpty();
    }

    @Test
    public void getUiDataTransferFromServiceByParams_policies_noPoliciesForResource() {
        UiComponentDataTransfer componentDTO = UiComponentDataConverter.getUiDataTransferFromServiceByParams(new Service(), Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEmpty();
    }

    @Test
    public void getUiDataTransferFromResourceByParams_policies() {
        Resource resourceWithPolicies = buildResourceWithPolicies();
        UiComponentDataTransfer componentDTO = UiComponentDataConverter.getUiDataTransferFromResourceByParams(resourceWithPolicies, Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEqualTo(resourceWithPolicies.resolvePoliciesList());
    }

    @Test
    public void getUiDataTransferFromServiceByParams_policies() {
        Service resourceWithPolicies = buildServiceWithPolicies();
        UiComponentDataTransfer componentDTO = UiComponentDataConverter.getUiDataTransferFromServiceByParams(resourceWithPolicies, Collections.singletonList("policies"));
        assertThat(componentDTO.getPolicies()).isEqualTo(resourceWithPolicies.resolvePoliciesList());
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
}
