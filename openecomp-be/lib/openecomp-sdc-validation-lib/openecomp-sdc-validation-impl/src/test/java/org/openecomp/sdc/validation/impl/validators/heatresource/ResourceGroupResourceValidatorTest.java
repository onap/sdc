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
 * Created by TALIO on 2/28/2017.
 */
public class ResourceGroupResourceValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  ResourceGroupResourceValidator resourceValidator = new ResourceGroupResourceValidator();

  @Test
  public void testResourceGroupWithInvalidIndexVar() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/heat_validator/resource_group_invalid_indexvar/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 8);
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: F-1 : Wrong value assigned to a ResourceGroup index_var property (functions are not allowed but only strings), Resource ID [resource_with_resources_group_1]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: Wrong value type assigned to a nested input parameter, nested resource [resource_with_resources_group_2], property name [index_boolean], nested file [yamlFile.yaml]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(2).getMessage(),
        "WARNING: Wrong value type assigned to a nested input parameter, nested resource [resource_with_resources_group_3], property name [index_boolean], nested file [yamlFile.yaml]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(3).getMessage(),
        "WARNING: Wrong value type assigned to a nested input parameter, nested resource [resource_with_resources_group_3], property name [index_number], nested file [yamlFile.yaml]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(4).getMessage(),
        "WARNING: Wrong value type assigned to a nested input parameter, nested resource [resource_with_resources_group_4], property name [index_boolean], nested file [yamlFile.yaml]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(5).getMessage(),
        "WARNING: Wrong value type assigned to a nested input parameter, nested resource [resource_with_resources_group_5], property name [index_boolean], nested file [yamlFile.yaml]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(6).getMessage(),
        "WARNING: Wrong value type assigned to a nested input parameter, nested resource [resource_with_resources_group_5], property name [index_number], nested file [yamlFile.yaml]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(7).getMessage(),
        "ERROR: F-1 : Wrong value assigned to a ResourceGroup index_var property (functions are not allowed but only strings), Resource ID [resource_with_resources_group_6]");
  }

  @Test
  public void testResourceGroupWithInvalidType() {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/heat_validator/resource_group_invalid_type/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: OS::Heat::ResourceGroup resource with resource_def which is not pointing to nested heat file is not supported, Resource ID [resource_with_resources_group_1], resource_def type [{get_param=pcrf_vnf_id}]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: OS::Heat::ResourceGroup resource with resource_def which is not pointing to nested heat file is not supported, Resource ID [resource_with_resources_group_2], resource_def type [OS::Nova::Server]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(2).getMessage(),
        "WARNING: A resource has an invalid or unsupported type - null, Resource ID [resource_with_resources_group_3]");
  }

  @Test
  public void testResourcesGroupWithNested() throws IOException {
    Map<String, MessageContainer> messages =ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource(),
        "/org/openecomp/validation/validators/heat_validator/resources_group_with_nested/negative_test/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 3);

    Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: Referenced parameter not found in nested file - nested-from-resources-group.yaml, parameter name [resource_with_resources_group], Resource ID [property_not_in_nested]");
    Assert.assertEquals(
        messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: OS::Heat::ResourceGroup resource with resource_def which is not pointing to nested heat file is not supported, Resource ID [resource_without_resources_group], resource_def type [OS::Nova::Server]");

    Assert.assertEquals(messages.get("nested-pps_v1.0.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: F-3 : Nested files loop - [nested-pps_v1.0.yaml -- nested-from-resources-group.yaml -- hot-nimbus-pps_v1.0.yaml -- nested-pps_v1.0.yaml]");

    Assert.assertEquals(messages.get("nested-not-exist.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(
        messages.get("nested-not-exist.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: F-2 : Missing nested file - nested-not-exist.yaml");
  }

}
