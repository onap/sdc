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

package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author KATYR
 * @since January 22, 2018
 */

public class VirtualMachineInterfaceValidatorTest {
  private static final String PATH =
      "/org/openecomp/validation/validators/heat_validator/vlan_resource_validation/";


  @Test
  public void hasSingleParentPortNegative()  {
    HeatResourceValidator baseValidator = new HeatResourceValidator();
    VirtualMachineInterfaceValidator resourceValidator = new VirtualMachineInterfaceValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_multiple_parent_ports/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [VLAN1]: More than one parent port found, " +
            "there should be only one parent port for a VLAN sub-interface ID [template_Vlan_2]");
  }

  @Test
  public void hasSingleParentPortNegativeWithGetResource()  {
    HeatResourceValidator baseValidator = new HeatResourceValidator();
    VirtualMachineInterfaceValidator resourceValidator = new VirtualMachineInterfaceValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_multiple_parent_ports/negative_get_resource/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }


  @Test
  public void hasSingleParentPortPositive()  {
    HeatResourceValidator baseValidator = new HeatResourceValidator();
    VirtualMachineInterfaceValidator resourceValidator = new VirtualMachineInterfaceValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_multiple_parent_ports/positive_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


  }


  @Test
  public void hasBothPropertiesNegativeMissingVlanTag()  {
    HeatResourceValidator baseValidator = new HeatResourceValidator();
    VirtualMachineInterfaceValidator resourceValidator = new VirtualMachineInterfaceValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_has_two_properties/negative_tag_missing/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [VLAN2]: VLAN Tag property " +
            "virtual_machine_interface_properties_sub_interface_vlan_tag " +
            "is missing in VLAN Resource ID [template_Vlan_2]");
  }

  @Test
  public void hasBothPropertiesNegativeMissingRefs()  {
    HeatResourceValidator baseValidator = new HeatResourceValidator();
    VirtualMachineInterfaceValidator resourceValidator = new VirtualMachineInterfaceValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_has_two_properties/negative_refs_missing/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested.yml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested.yml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [VLAN2]: Parent port property virtual_machine_interface_refs is " +
            "missing in VLAN Resource ID [template_Vlan_2]");
  }

  @Test
  public void hasBothPropertiesBothMissingWhichMeansPositive()  {
    HeatResourceValidator baseValidator = new HeatResourceValidator();
    VirtualMachineInterfaceValidator resourceValidator = new VirtualMachineInterfaceValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_has_two_properties/negative_both_missing/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

  }


  @Test
  public void hasBothPropertiesPositive()  {
    HeatResourceValidator baseValidator = new HeatResourceValidator();
    VirtualMachineInterfaceValidator resourceValidator = new VirtualMachineInterfaceValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator,
        HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
        PATH + "vlan_has_two_properties/positive_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


  }



}