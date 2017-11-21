package org.openecomp.sdc.validation.impl.validators.heatresource;

import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Created by TALIO on 2/28/2017.
 */
public class NestedResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  NestedResourceValidator resourceValidator = new NestedResourceValidator();

  private static final String PATH = "/org/openecomp/validation/validators/heat_validator/";
  @Test
  public void testNoLoopsNesting() throws IOException {
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
  public void testPropertiesMatchNestedParameters() throws IOException {
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
  public void testWrongValueTypeAssigned() throws IOException {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
            resourceValidator, null,
             "/org/openecomp/validation/validators/heat_validator/properties_match_nested_parameters/wrong_value_type_assigned/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [HNR4]: Wrong value type assigned to a nested input parameter, nested resource [server_pcrf_pps_001], property name [index_integer], nested file [nested-pps_v1.0.yaml]");
  }

  @Test
  public void testMissingNestedFile() throws IOException {
    final Resource resource = new Resource();
    resource.setType("nested-pps_v1.0.yaml");

    final GlobalValidationContext globalValidationContext = ValidationTestUtil.createGlobalContextFromPath(PATH + "missing_nested_file/input");

    resourceValidator.validateAllPropertiesMatchNestedParameters(null, null, resource, null, globalValidationContext);

    Map<String, MessageContainer> messages = globalValidationContext.getContextMessageContainers();
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("nested-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
            messages.get("nested-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [HNR1]: Missing nested file - nested-pps_v1.0.yaml");
  }
}
