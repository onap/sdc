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
public class NeutronPortResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  NeutronPortResourceValidator resourceValidator = new NeutronPortResourceValidator();

  private static final String PATH = "/org/openecomp/validation/validators/heat_validator/";
  @Test
  public void testMoreThanOneBindFromNovaToPort() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
            PATH + "one_nova_points_to_one_port/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HPR2]: Resource Port oam1_int_port exceed allowed relations from NovaServer");
  }

  @Test
  public void testPortNotBindToAnyNovaServerHPR1() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "no_neutron_port/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HPR1]: Port not bind to any NOVA Server, Resource Id [nova_server_1]");
  }

  @Test
  public void testPortNotBindToAnyNovaServerHPR3() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "port_no_bind_to_any_nova_server/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HPR3]: Port not bind to any NOVA Server, Resource Id [nova_server_1]");
  }
}
