package org.openecomp.sdc.validation.impl.validators.validators;

import org.openecomp.sdc.validation.impl.validators.EcompGuideLineValidator;

import org.openecomp.sdc.validation.impl.validators.ValidatorBaseTest;
import org.openecomp.core.validation.types.MessageContainer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class EcompGuideLineValidatorTest extends ValidatorBaseTest {

  @Test
  public void testMissingBaseHeat() {
    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/ecompGuideLineValidator/missingBaseHeat/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().get(0).getMessage(),
        "Missing Base HEAT. Pay attention that without Base HEAT, there will be no shared resources");
  }

  @Test
  public void testMultiBaseHeat() {
    Map<String, MessageContainer> messages =
        runValidation("/openecomp/org/validation/validators/ecompGuideLineValidator/multiBaseHeat/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().get(0).getMessage(),
        "Multi Base HEAT. Expected only one. Files [second.yaml,first.yaml].");
  }

  @Test
  public void testBaseHeatExposeVolume() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeVolume/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeVolume/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Resource is not defined as output and thus cannot be Shared. resource id - volume_expose");
  }

  @Test
  public void testBaseHeatExposeServerGroup() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeServerGroup/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeServerGroup/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Resource is not defined as output and thus cannot be Shared. resource id - ServerGroup_expose");
  }

  @Test
  public void testBaseHeatExposeSecurityGroup() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeSecurityGroup/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeSecurityGroup/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Resource is not defined as output and thus cannot be Shared. resource id - SecurityGroup_expose");
  }


  @Test
  public void testBaseHeatExposeNetwork() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeNetwork/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeNetwork/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Resource is not defined as output and thus cannot be Shared. resource id - SecurityGroup_expose");
  }

  @Test
  public void testBaseHeatExposeNetworkAndVolume() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeNetworkAndVolume/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/baseHeatDoesNotExposeNetworkAndVolume/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Resource is not defined as output and thus cannot be Shared. resource id - volume_expose");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "Resource is not defined as output and thus cannot be Shared. resource id - net_expose");
  }

  @Test
  public void testNovaResourceNetworkUniqueRole() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaNetworkUniqueRoleConvention/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaNetworkUniqueRoleConvention/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "A resource is connected twice to the same network role Resource ID [FSB2] Network Role [Internal1].");
  }

  @Test
  public void testHeatVolumeExpose() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatVolumeExpose/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatVolumeExpose/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("firstVol.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("firstVol.yaml").getErrorMessageList().get(0).getMessage(),
        "Volume is not defined as output and thus cannot be attached volume_expose");

  }

  @Test
  public void testHeatPortNetworkNamingConvention() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatPortNetworkNamingConvention/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatPortNetworkNamingConvention/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Network Parameter Name not aligned with Guidelines Parameter Name [not_valid_network_name] Resource ID [port_resource]");
  }

  @Test
  public void testHeatNovaServerMetaDataValidation() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaServerMetaDataValidation/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaServerMetaDataValidation/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Missing VNF_ID Resource id [FSB2]");
  }

  @Test
  public void testNeutronFixedIpName() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatPortFixedIpNamingConvention/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatPortFixedIpNamingConvention/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Fixed_IPS not aligned with Guidelines, Resource ID [port_resource_0]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "Fixed_IPS not aligned with Guidelines, Resource ID [port_resource_1]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(2).getMessage(),
        "Fixed_IPS not aligned with Guidelines, Resource ID [port_resource_2]");
  }


  @Test
  public void testNovaServerName() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaServerNameValidation/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaServerNameValidation/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Server Name not aligned with Guidelines, Resource ID [FSB2]");
  }

  @Test
  public void testAvailabilityZoneName() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaServerAvailabilityZoneName/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaServerAvailabilityZoneName/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Server Availability Zone not aligned with Guidelines, Resource ID [FSB2]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "Server Availability Zone not aligned with Guidelines, Resource ID [FSB3]");
  }

  @Test
  public void testFloatingIpResourceType() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatFloatingIpResourceType/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatFloatingIpResourceType/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "OS::Neutron::FloatingIP is in use, Resource ID [FSB2]");
  }

  @Test
  public void testImageAndFlavorNames() {
    Map<String, MessageContainer> messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaServerImageAndFlavor/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = runValidation(
        "/openecomp/org/validation/validators/ecompGuideLineValidator/heatNovaServerImageAndFlavor/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "Wrong flavor name format in NOVA Server, Resource ID [FSB2]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "Wrong image name format in NOVA Server, Resource ID [FSB3]");
  }


  @Override
  public Map<String, MessageContainer> runValidation(String path) {
    EcompGuideLineValidator validator = new EcompGuideLineValidator();
    return testValidator(validator, path);
  }
}