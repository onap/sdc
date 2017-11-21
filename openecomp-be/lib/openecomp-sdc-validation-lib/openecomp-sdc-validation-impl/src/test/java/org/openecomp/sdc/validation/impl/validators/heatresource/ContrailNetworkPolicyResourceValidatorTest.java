package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Created by TALIO on 2/28/2017.
 */
public class ContrailNetworkPolicyResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  ContrailNetworkPolicyResourceValidator resourceValidator = new
      ContrailNetworkPolicyResourceValidator();

  private static final String PATH = "/org/openecomp/validation/validators/heat_validator/network_policy_associated_with_attach_policy/";
  @Test
  public void testNetworkPolicyAssociatedWithAttachPolicy() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator, resourceValidator
            , HeatResourcesTypes.CONTRAIL_NETWORK_RULE_RESOURCE_TYPE.getHeatResource(),
            PATH + "positive");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HNP2]: NetworkPolicy not in use, Resource Id [not_used_server_pcrf_policy]");
  }

  @Test
  public void testNonNetworkPolicyResource() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator, resourceValidator
            , HeatResourcesTypes.CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource(),
            PATH + "negative");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HNP1]: NetworkPolicy not in use, Resource Id [server_pcrf_network]");
  }
}
