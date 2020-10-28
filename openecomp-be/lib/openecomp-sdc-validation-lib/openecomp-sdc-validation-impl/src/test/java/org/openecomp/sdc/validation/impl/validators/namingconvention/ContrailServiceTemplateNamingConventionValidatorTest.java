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
public class ContrailServiceTemplateNamingConventionValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  ContrailServiceTemplateNamingConventionValidator resourceValidator = new
      ContrailServiceTemplateNamingConventionValidator();
  private static final String PATH="/org/openecomp/validation/validators/guideLineValidator/heatcontrailservicetemplateimageandflavor/";

  @Test
  public void testContrailServiceTemplateImageAndFlavorNamesAlignedWithNamingConventionButDifferentVmType() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource(),
            PATH + "imageandflavordifferentvmtype");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(
            messages.get(messages.keySet().iterator().next()).getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NST1]: Service Template naming convention in Image and Flavor properties is not consistent in Resource, Resource ID service_template");
  }

  @Test
  public void testContrailServiceTemplateImageAndFlavorNamesNotAlignedWithNamingConvention() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource(),
            PATH + "/notaligned");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NST3]: Service Template 'image_name' Parameter Name not aligned with Guidelines, Parameter Name [st_imaage_name], Resource ID [service_template]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
            "WARNING: [NST3]: Service Template 'flavor' Parameter Name not aligned with Guidelines, Parameter Name [st_flavaor_name], Resource ID [service_template]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testContrailServiceTemplateImageAndFlavorNamesAlignedWithNamingConvention() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource(),
            PATH + "aligned");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }

  @Test
  public void testContrailServiceTemplateMissingParam() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource(),
            PATH + "missingparam");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(
            messages.get(messages.keySet().iterator().next()).getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NST2]: Missing get_param in image_name, Resource Id [service_template]");
  }

}
