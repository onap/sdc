package org.openecomp.sdc.validation.impl.validators.namingconvention;

import org.openecomp.sdc.validation.impl.validators.NamingConventionGuideLineValidator;

import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Created by TALIO on 2/28/2017.
 */
public class NeutronPortNamingConventionValidatorTest {

  NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
  NeutronPortNamingConventionValidator resourceValidator = new
      NeutronPortNamingConventionValidator();

  @Test
  public void testHeatPortNetworkNamingConvention() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatPortNetworkNamingConvention/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatPortNetworkNamingConvention/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [J-3] : Port 'Network' Parameter Name not aligned with Guidelines, Parameter Name [not_valid_network_name], Resource ID [port_resource]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testNeutronFixedIpName() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatPortFixedIpNamingConvention/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatPortFixedIpNamingConvention/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 4);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [J-1] : Port 'Fixed_IPS' Parameter Name not aligned with Guidelines, Parameter Name [pcrf_net_v6_ip_a], Resource ID [port_resource_0]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [J-1] : Port 'Fixed_IPS' Parameter Name not aligned with Guidelines, Parameter Name [indx], Resource ID [port_resource_1]. As a result, VF/VFC Profile may miss this information");

    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(2).getMessage(),
        "WARNING: [J-1] : Port 'Fixed_IPS' Parameter Name not aligned with Guidelines, Parameter Name [pcrf_net_ipz], Resource ID [port_resource_2]. As a result, VF/VFC Profile may miss this information");

    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(3).getMessage(),
        "WARNING: [J-1] : Port 'Fixed_IPS' Parameter Name not aligned with Guidelines, Parameter Name [pcrf_net_v0_ip_3], Resource ID [port_resource_2]. As a result, VF/VFC Profile may miss this information");
  }
}
