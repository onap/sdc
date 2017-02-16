package org.openecomp.sdc.validation.impl.validators.validators;

import org.openecomp.sdc.validation.impl.validators.EcompGuideLineValidator;
import org.openecomp.sdc.validation.impl.validators.ValidatorBaseTest;
import org.openecomp.core.validation.types.MessageContainer;

import java.io.IOException;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EcompNamingConventionTest extends ValidatorBaseTest {

  @Test
  public void testNeutronFixedIpName() throws IOException {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecomp_naming_convention/neutron_port_fixed_ip_name/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 2);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "Fixed_IPS not aligned with Guidelines, Resource ID [fixed_ip_illegal_name_1]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "Fixed_IPS not aligned with Guidelines, Resource ID [fixed_ip_illegal_name_2]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(2).getMessage(),
        "Fixed_IPS not aligned with Guidelines, Resource ID [fixed_ip_illegal_name_3]");
  }


  @Test
  public void testNovaServerName() throws IOException {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecomp_naming_convention/nova_server_name/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 2);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 6);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "Server Name not aligned with Guidelines, Resource ID [nova_server_ilegal_name_1]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "Server Name not aligned with Guidelines, Resource ID [nova_server_ilegal_name_2]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(2).getMessage(),
        "Server Name not aligned with Guidelines, Resource ID [nova_server_ilegal_name_3]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(3).getMessage(),
        "Server Name not aligned with Guidelines, Resource ID [nova_server_ilegal_name_4]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(4).getMessage(),
        "Server Name not aligned with Guidelines, Resource ID [nova_server_ilegal_name_5]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(5).getMessage(),
        "Missing get_param in nova server name, Resource Id [nova_server_ilegal_name_6]");
  }


  @Test
  public void testAvailabilityZoneName() throws IOException {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecomp_naming_convention/availability_zone_name/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 2);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "Server Availability Zone not aligned with Guidelines, Resource ID [availability_zone_illegal_name_1]");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "Server Availability Zone not aligned with Guidelines, Resource ID [availability_zone_illegal_name_2]");
  }


  @Test
  public void testFloatingIpResourceType() throws IOException {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecomp_naming_convention/floating_ip_resource_type/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 2);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "OS::Neutron::FloatingIP is in use, Resource ID [floating_ip_type]");
  }


  @Override
  public Map<String, MessageContainer> runValidation(String path) {
    EcompGuideLineValidator ecompGuideLineValidator = new EcompGuideLineValidator();
    return testValidator(ecompGuideLineValidator, path);
  }
}