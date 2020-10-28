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
public class NeutronSecurityGroupResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  NeutronSecurityGroupResourceValidator resourceValidator = new
      NeutronSecurityGroupResourceValidator();
  private static final String PATH = "/org/openecomp/validation/validators/heat_validator/";

  @Test
  public void testSecurityGroupBaseFileNoPorts() throws IOException {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator,
            HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource(),
            PATH + "security_group_base_file_no_ports/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("baseFile.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("baseFile.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HSG1]: SecurityGroup not in use, Resource Id [jsa_security_group3]");
  }

  @Test
  public void testSecurityGroupsCalledByPort() throws IOException {
    Map<String, MessageContainer> messages =new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator,
            HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource(),
            PATH + "security_group_called_by_port/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HSG1]: SecurityGroup not in use, Resource Id [not_used_security_group]");
  }

}
