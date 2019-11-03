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

/**
 * Created by TALIO on 2/28/2017.
 */
public class NeutronPortNamingConventionValidatorTest {

  NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
  NeutronPortNamingConventionValidator resourceValidator = new
      NeutronPortNamingConventionValidator();
  private static final  String PATH = "/org/openecomp/validation/validators/guideLineValidator/heatPortNetworkNamingConvention/";
  @Test
  public void testHeatPortNetworkNamingConvention() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
            PATH + "positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
            PATH + "negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNP3]: Port 'Network' Parameter Name not aligned with Guidelines, Parameter Name [not_valid_network_name], Resource ID [port_resource]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testNeutronFixedIpName() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
            PATH + "positiveFixedIP");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
            PATH + "negativeFixedIP/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNP1]: Port 'Fixed_IPS' Parameter Name not aligned with Guidelines, Parameter Name [pcrf_net_v6_ip_a], Resource ID [port_resource_0]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
            "WARNING: [NNP1]: Port 'Fixed_IPS' Parameter Name not aligned with Guidelines, Parameter Name [indx], Resource ID [port_resource_1]. As a result, VF/VFC Profile may miss this information");

    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(2).getMessage(),
            "WARNING: [NNP1]: Port 'Fixed_IPS' Parameter Name not aligned with Guidelines, Parameter Name [pcrf_net_ipz], Resource ID [port_resource_2]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testMissingParam() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
            PATH + "missingparam/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNP2]: Missing get_param in network or network_id, Resource Id [port_resource_2]");
  }
}
