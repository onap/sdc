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
public class NovaServerResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  NovaServerResourceValidator resourceValidator = new NovaServerResourceValidator();
  private static final String PATH = "/org/openecomp/validation/validators/heat_validator/";
  @Test
  public void testNovaPropertiesHasAssignedValue() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "nova_properties_has_assigned_value/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HNS1]: Missing both Image and Flavor in NOVA Server, Resource ID [nova_server_resource_missing_both]");
  }

  @Test
  public void testServerGroupsPointedByServersDefinedCorrectly() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "server_groups_defined_correctly/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HNS2]: Missing server group definition - BE_Affinity_2, nova_server_1");
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
            "ERROR: [HNS2]: Missing server group definition - BE_Affinity_2, nova_server_2");
  }
}
