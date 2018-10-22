/*
 * Copyright © 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.validation.base;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.validation.type.ConfigConstants;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Test
public class ResourceBaseValidatorTest {
private String testValidator = "testValidator";

  @Test
  public void testInvalidResourceType(){
    ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(
        resourceBaseValidator, "/InvalidResourceType");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [RBV1]: A resource has an invalid or unsupported type - null, " +
            "Resource ID [FSB2]");
  }

  @Test
  public void testInvalidHeatStructure(){
    ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(resourceBaseValidator,
        "/InvalidHeatStructure");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [RBV2]: Invalid HEAT format problem - [while scanning for the next " +
        "token\n" + "found character '\\t(TAB)' that cannot start any token. " +
        "(Do not use \\t(TAB) for indentation)\n" +
        " in 'reader', line 10, column 1:\n" +
        "    \t\t\tresources:\n" +
        "    ^\n" +
        "]");
  }

  @Test
  public void testInitWithEmptyPropertiesMap() {
    ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
    Map<String, Object> properties = new HashMap<>();
    resourceBaseValidator.init(properties);

    Assert.assertTrue(MapUtils.isEmpty(resourceBaseValidator.getResourceTypeToImpl()));
  }

  @Test
  public void testInitPropertiesMap() {
    ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
    initProperties(resourceBaseValidator, getValidImplementationConfiguration());

    Map<String, ImplementationConfiguration> resourceTypeToImpl =
        resourceBaseValidator.getResourceTypeToImpl();
    Assert.assertTrue(MapUtils.isNotEmpty(resourceTypeToImpl));
    Assert.assertTrue(resourceTypeToImpl.containsKey(testValidator));
  }

  @Test
  public void testInitPropertiesWithString() {
    ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
    Map<String, Object> properties = new HashMap<>();
    properties.put(testValidator, "invalidValue");

    resourceBaseValidator.init(properties);

    Assert.assertTrue(MapUtils.isEmpty(resourceBaseValidator.getResourceTypeToImpl()));
  }

  @Test
  public void testInitPropertiesWithoutImplClass() {
    ResourceBaseValidator resourceBaseValidator = new ResourceBaseValidator();
    initProperties(resourceBaseValidator, new HashMap<>());

    Assert.assertTrue(MapUtils.isEmpty(resourceBaseValidator.getResourceTypeToImpl()));
  }

  public Map<String, Object> getValidImplementationConfiguration() {
    Map<String, Object> implConfiguration = new HashMap<>();
    implConfiguration.put(
        ConfigConstants.Impl_Class, "org.openecomp.sdc.validation.impl.validators.ForbiddenResourceGuideLineValidator");
    implConfiguration.put(ConfigConstants.Enable, true);

    return implConfiguration;
  }

  private void initProperties(ResourceBaseValidator resourceBaseValidator,
                              Map<String, Object> implementationConfiguration) {
    Map<String, Object> properties = new HashMap<>();
    properties.put(testValidator, implementationConfiguration);

    resourceBaseValidator.init(properties);
  }
}
