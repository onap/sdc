package org.openecomp.sdc.validation.impl.validators.namingconvention;

import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.impl.validators.NamingConventionGuideLineValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Created by TALIO on 2/28/2017.
 */
public class NovaServerNamingConventionGuideLineValidatorTest {

  NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
  NovaServerNamingConventionGuideLineValidator resourceValidator = new
      NovaServerNamingConventionGuideLineValidator();

  @Test
  public void testHeatNovaServerMetaDataValidation() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaServerMetaDataValidation/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaServerMetaDataValidation/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [NNS3]:Missing VNF_ID in Metadata property, Resource ID [FSB2]");
  }

  @Test
  public void testNovaServerAvailabilityZoneName() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaServerAvailabilityZoneName/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaServerAvailabilityZoneName/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [NNS5]:Server 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone_a], Resource ID [FSB2]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [NNS5]:Server 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone], Resource ID [FSB3]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testNovaImageAndFlavorNames() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaServerImageAndFlavor/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaServerImageAndFlavor/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [NNS13]:Server 'flavor' Parameter Name not aligned with Guidelines, Parameter Name [fsb2-flavor], Resource ID [FSB2]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [NNS13]:Server 'image' Parameter Name not aligned with Guidelines, Parameter Name [fsb2-image], Resource ID [FSB3]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testNovaResourceNetworkUniqueRole() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaNetworkUniqueRoleConvention/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaNetworkUniqueRoleConvention/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [NNS12]:A resource is connected twice to the same network role, Network Role [FSB2], Resource ID [Internal1]");
  }

  @Test
  public void testNovaServerName() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaServerNameValidation/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatNovaServerNameValidation/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [NNS10]:Server 'name' Parameter Name not aligned with Guidelines, Parameter Name [pcrf_pps_server_4], Resource ID [FSB2]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testVMNameSyncInNova() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/novaVMNameSync/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 4);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [NNS10]:Server 'name' Parameter Name not aligned with Guidelines, Parameter Name [CE_server_name], Resource ID [FSB2_legal_2]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [NNS13]:Server 'flavor' Parameter Name not aligned with Guidelines, Parameter Name [fsb_flavor_names], Resource ID [FSB2_legal_3]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(2).getMessage(),
        "WARNING: [NNS11]:Nova Server naming convention in image, flavor and name properties is not consistent, Resource ID [FSB2_illegal_1]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(3).getMessage(),
        "WARNING: [NNS11]:Nova Server naming convention in image, flavor and name properties is not consistent, Resource ID [FSB2_illegal_2]");
  }

  @Test
  public void testAvailabilityZoneName() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/naming_convention/availability_zone_name/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [NNS5]:Server 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone_name], Resource ID [availability_zone_illegal_name_1]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(
        messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [NNS5]:Server 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone], Resource ID [availability_zone_illegal_name_2]. As a result, VF/VFC Profile may miss this information");
  }
}
