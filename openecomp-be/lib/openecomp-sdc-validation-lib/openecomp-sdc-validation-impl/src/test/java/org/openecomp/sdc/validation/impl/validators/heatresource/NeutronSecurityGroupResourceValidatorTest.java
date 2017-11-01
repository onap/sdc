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
public class NeutronSecurityGroupResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  NeutronSecurityGroupResourceValidator resourceValidator = new
      NeutronSecurityGroupResourceValidator();

  @Test
  public void testSecurityGroupBaseFileNoPorts() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/heat_validator/security_group_base_file_no_ports/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("baseFile.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("baseFile.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: D-1 : SecurityGroup not in use, Resource Id [jsa_security_group3]");
  }

  @Test
  public void testSecurityGroupsCalledByPort() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/heat_validator/security_group_called_by_port/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: D-1 : SecurityGroup not in use, Resource Id [not_used_security_group]");
  }

}
