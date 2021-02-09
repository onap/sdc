/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.validation.impl.validators.namingconvention;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.impl.validators.NamingConventionGuideLineValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

public class VirtualMachineInterfaceGuidelineValidatorTest {
  private static final String PATH =
      "/org/openecomp/validation/validators/guideLineValidator/vlan_validation/";

  @Test
  public void modeledThroughResourceGroupPositive() {

    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "modeled_through_resource_group/positive_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

  }

  @Test
  public void modeledThroughResourceGroupNegativeTwoValuesInList() {

    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "modeled_through_resource_group/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [VlANG1]: VLAN Resource will not be translated as the VLAN Sub-interface " +
            "[vdbe_0_subint_untr_vmi_0] is not modeled as resource group");
  }

  @Test
  public void modeledThroughResourceGroupNegativeNonStringGetParam() {

    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "modeled_through_resource_group/negative_test_non_string/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [VlANG1]: VLAN Resource will not be translated as the VLAN Sub-interface " +
            "[vdbe_0_subint_untr_vmi_0] is not modeled as resource group");
  }

  @Test
  public void modeledThroughResourceGroupNegativeResource() {

    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "modeled_through_resource_group/negative_get_resource/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [VlANG1]: VLAN Resource will not be translated as the VLAN Sub-interface " +
            "[vdbe_0_subint_untr_vmi_0] is not modeled as resource group");
  }


  @Test
  public void vlanAloneInFilePositive() {

    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "single_vlan_resource/positive_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

  }

  @Test
  public void vlanAloneInFileNegative_2vlans() {

    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "single_vlan_resource/negative_test/two_vlans");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [VlANG2]: There should not be any Compute Server Node, Port, " +
            "Parent Port in nested file [nested.yml]");
  }

  @Test
  public void vlanAloneInFileNegative_vlanAndNova() {

    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "single_vlan_resource/negative_test/vlan_and_nova");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [VlANG2]: There should not be any Compute Server Node, Port, " +
            "Parent Port in nested file [nested.yml]");
  }

  @Test
  public void namingConventionNegative(){
    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "naming_convention/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [VlANG3]: Network role associated with VLAN Sub-interface " +
            "id[template_wrong_naming_Vlan_2] is not following the naming convention");

  }

  @Test
  public void namingConventionPositive(){
    NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
    VirtualMachineInterfaceGuidelineValidator resourceValidator = new
        VirtualMachineInterfaceGuidelineValidator();
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "naming_convention/positive_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }

}
