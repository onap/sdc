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
import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

/**
 * Created by TALIO on 2/28/2017.
 */
public class ContrailServiceInstanceNamingConventionValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  ContrailServiceInstanceNamingConventionValidator resourceValidator = new
      ContrailServiceInstanceNamingConventionValidator();
  private static final String PATH = "/org/openecomp/validation/validators/guideLineValidator/heatcontrailserviceinstanceavailabilityzone/";

  @Test
  public void testContrailServiceInstanceAvailabilityZoneNotAlignedWithNamingConvention() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_INSTANCE.getHeatResource(),
            PATH + "notaligned");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NSI1]: Service Instance 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone_1a], Resource ID [service_instance_1]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testContrailServiceInstanceAvailabilityZoneAlignedWithNamingConvention() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_INSTANCE.getHeatResource(),
            PATH + "aligned");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }
  @Test
  public void testContrailServiceInstanceAvailabilityZoneNotAlignedWithNamingConventionMissingParam() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_INSTANCE.getHeatResource(),
            PATH + "missingparam");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NSI2]: Missing get_param in availability_zone, Resource Id [service_instance_1]");
  }
}
