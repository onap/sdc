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

import java.io.IOException;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

/**
 * Created by TALIO on 2/28/2017.
 */
public class ContrailNetworkPolicyResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  ContrailNetworkPolicyResourceValidator resourceValidator = new
      ContrailNetworkPolicyResourceValidator();

  private static final String PATH = "/org/openecomp/validation/validators/heat_validator/network_policy_associated_with_attach_policy/";
  @Test
  public void testNetworkPolicyAssociatedWithAttachPolicy() throws IOException {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator, resourceValidator
            , HeatResourcesTypes.CONTRAIL_NETWORK_RULE_RESOURCE_TYPE.getHeatResource(),
            PATH + "positive");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HNP2]: NetworkPolicy not in use, Resource Id [not_used_server_pcrf_policy]");
  }

  @Test
  public void testNonNetworkPolicyResource() throws IOException {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator, resourceValidator
            , HeatResourcesTypes.CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource(),
            PATH + "negative");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HNP1]: NetworkPolicy not in use, Resource Id [server_pcrf_network]");
  }
}
