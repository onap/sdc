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
 * @author KATYR
 * @since January 22, 2018
 */

public class VirtualMachineInterfaceValidatorTest {

  private HeatResourceValidator baseValidator = new HeatResourceValidator();
  private VirtualMachineInterfaceValidator resourceValidator = new VirtualMachineInterfaceValidator();
  private static final String PATH =
      "/org/openecomp/validation/validators/heat_validator/vlan_resource_validation/";


  @Test
  public void hasSingleParentPortNegative() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_multiple_parent_ports/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 2);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [VLAN1]: More than one parent port found, there should be only one parent port " +
            "for a VLAN sub-interface ID [template_Vlan_2]");
  }


  @Test
  public void hasSingleParentPortPositive() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_multiple_parent_ports/positive_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


  }


  @Test
  public void hasBothPropertiesMissingTagNegative() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_has_two_properties/negative_tag_missing/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [VLAN2]: VLAN Tag property virtual_machine_interface_properties_sub_interface_vlan_tag is missing in VLAN Resource ID [template_Vlan_2]");
  }

  @Test
  public void hasBothPropertiesMissingRefsNegative() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_has_two_properties/negative_refs_missing/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [VLAN2]: Parent port property virtual_machine_interface_refs is missing in VLAN Resource ID [template_Vlan_2]");
  }

  @Test
  public void hasBothPropertiesBothMissingPositiveNotVLAN() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_has_two_properties/negative_both_missing/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

  }


  @Test
  public void hasBothPropertiesPositive() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_has_two_properties/positive_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


  }

  @Test
  public void hasSingleParentPortNegativeHasBothPropertiesNegative() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_mixed/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 2);



    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
        Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [VLAN2]: VLAN Tag property virtual_machine_interface_properties_sub_interface_vlan_tag is missing in VLAN Resource ID [template_Vlan_2]");

  }


}