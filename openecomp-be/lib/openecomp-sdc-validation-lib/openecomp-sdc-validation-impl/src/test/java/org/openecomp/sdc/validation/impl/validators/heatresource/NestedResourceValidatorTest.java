/*
 * Copyright © 2016-2017 European Support Limited
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

import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

/**
 * Created by TALIO on 2/28/2017.
 */
public class NestedResourceValidatorTest {

  private final HeatResourceValidator baseValidator = new HeatResourceValidator();
  private final NestedResourceValidator resourceValidator = new NestedResourceValidator();
  private static final String  PATH = "/org/openecomp/validation/validators/heat_validator/";

  @Test
  public void testNoLoopsNesting() {

    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, null,
            PATH + "no_loops_nesting/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 4);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HNR2]: Nested files loop - [hot-nimbus-psm_v1.0.yaml -- nested-psm_v1.0.yaml -- nested-points-to-hot-nimbus-psm.yaml -- hot-nimbus-psm_v1.0.yaml]");

    Assert.assertEquals(
            messages.get("nested-points-to-hot-nimbus-psm.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
            messages.get("nested-points-to-hot-nimbus-psm.yaml").getErrorMessageList().get(0)
                    .getMessage(),
            "ERROR: [HNR2]: Nested files loop - [nested-points-to-hot-nimbus-psm.yaml -- hot-nimbus-psm_v1.0.yaml -- nested-psm_v1.0.yaml -- nested-points-to-hot-nimbus-psm.yaml]");
    Assert.assertEquals(
            messages.get("nested-points-to-hot-nimbus-psm.yaml").getErrorMessageList().get(1)
                    .getMessage(),
            "ERROR: [HNR2]: Nested files loop - [nested-points-to-hot-nimbus-psm.yaml -- nested-psm_v1.0.yaml -- nested-points-to-hot-nimbus-psm.yaml]");

    Assert.assertEquals(messages.get("yaml-point-to-itself.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("yaml-point-to-itself.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HNR2]: Nested files loop - [yaml-point-to-itself.yaml -- yaml-point-to-itself.yaml]");

    Assert.assertEquals(messages.get("nested-psm_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("nested-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HNR2]: Nested files loop - [nested-psm_v1.0.yaml -- nested-points-to-hot-nimbus-psm.yaml -- hot-nimbus-psm_v1.0.yaml -- nested-psm_v1.0.yaml]");
  }

  @Test
  public void testPropertiesMatchNestedParameters() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, null,
            PATH + "properties_match_nested_parameters/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HNR3]: Referenced parameter not found in nested file - nested-pps_v1.0.yaml, parameter name [server_pcrf_pps_001], Resource ID [parameter_not_existing_in_nested]");
  }

  @Test
  public void testWrongValueTypeAssigned() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, null,
            PATH + "properties_match_nested_parameters/wrong_value_type_assigned/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HNR4]: Wrong value type assigned to a nested input parameter, nested resource [server_pcrf_pps_001], property name [index_integer], nested file [nested-pps_v1.0.yaml]");
  }

  @Test
  public void testMissingNestedFile() {
    final Resource resource = new Resource();
    resource.setType("nested-pps_v1.0.yaml");

    final GlobalValidationContext globalValidationContext = ValidationTestUtil.createGlobalContextFromPath(PATH + "missing_nested_file/input");

    NestedResourceValidator.validateAllPropertiesMatchNestedParameters(null, null, resource, Optional.empty(), globalValidationContext);

    Map<String, MessageContainer> messages = globalValidationContext.getContextMessageContainers();
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("nested-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HNR1]: Missing nested file - nested-pps_v1.0.yaml");
  }
}
