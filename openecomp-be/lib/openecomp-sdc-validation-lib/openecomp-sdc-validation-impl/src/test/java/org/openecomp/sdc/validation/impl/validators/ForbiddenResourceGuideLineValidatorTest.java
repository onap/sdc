package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.sdc.validation.Validator;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Created by TALIO on 2/16/2017.
 */
public class ForbiddenResourceGuideLineValidatorTest {

  private static String mockConfigFileName =
      "/org/openecomp/validation/configuration/mock_resource_validator_configuration.json";

  ForbiddenResourceGuideLineValidator forbiddenResourceGuideLineValidator = new
      ForbiddenResourceGuideLineValidator();

  @BeforeClass
  public void init() throws IOException {
    Map<String, Object> resourcesMap = ValidationTestUtil.getResourceMap(mockConfigFileName);

    Map<String, Object> resourceBaseValidatorMap =
        (Map<String, Object>) resourcesMap.get("forbiddenResourceGuideLineValidator");
    String implementationClass =
        (String) resourceBaseValidatorMap.get("implementationClass");
    Map<String, Object> properties =
        (Map<String, Object>) resourceBaseValidatorMap.get("properties");

    forbiddenResourceGuideLineValidator.init(properties);
  }

  @Test
  public void testFloatingIpResourceType() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(forbiddenResourceGuideLineValidator,
        "/org/openecomp/validation/validators/attGuideLineValidator/heatFloatingIpResourceType/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


    messages = ValidationTestUtil.testValidator(forbiddenResourceGuideLineValidator,
        "/org/openecomp/validation/validators/attGuideLineValidator/heatFloatingIpResourceType/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: OS::Neutron::FloatingIP is in use, Resource ID [FSB2]");
  }
}
