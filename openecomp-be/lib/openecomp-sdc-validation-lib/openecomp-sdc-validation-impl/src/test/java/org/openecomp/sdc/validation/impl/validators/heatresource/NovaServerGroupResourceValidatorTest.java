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
public class NovaServerGroupResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  NovaServerGroupResourceValidator resourceValidator = new NovaServerGroupResourceValidator();

  @Test
  public void testPolicyIsAffinityOrAntiAffinity() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/heat_validator/policy_is_affinity_or_anti_affinity/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: E-2 : Wrong policy in server group - pcrf_server_policies_1");
  }

  @Test
  public void testServerGroupCalledByServer() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_GROUP_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/heat_validator/server_group_called_by_nova_server/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: E-4 : ServerGroup not in use, Resource Id [not_used_server_group]");

  }
}
