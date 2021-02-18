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

package org.openecomp.sdc.validation.impl.validators;

import java.io.IOException;
import java.util.Map;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

/**
 * Created by TALIO on 2/16/2017.
 */
public class ForbiddenResourceGuideLineValidatorTest {

  private static final String RESOURCE_PATH = "/org/openecomp/validation/validators" +
      "/guideLineValidator/heatFloatingIpResourceType";
  private static String mockConfigFileName =
      "/org/openecomp/validation/configuration/mock_resource_validator_configuration.json";

  private static ForbiddenResourceGuideLineValidator forbiddenResourceGuideLineValidator = new
      ForbiddenResourceGuideLineValidator();

  @BeforeClass
  public static void init() throws IOException {
    Map<String, Object> resourcesMap = new ValidationTestUtil().getResourceMap(mockConfigFileName);

    Map<String, Object> resourceBaseValidatorMap =
        (Map<String, Object>) resourcesMap.get("forbiddenResourceGuideLineValidator");
    Map<String, Object> properties =
        (Map<String, Object>) resourceBaseValidatorMap.get("properties");

    forbiddenResourceGuideLineValidator.init(properties);
  }

  @Test
  public void testFloatingIpResourceType() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(
        forbiddenResourceGuideLineValidator, RESOURCE_PATH + "/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


    messages = new ValidationTestUtil().testValidator(forbiddenResourceGuideLineValidator,
        RESOURCE_PATH + "/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [FRG2]: OS::Neutron::FloatingIP is in use, Resource ID [FSB2]");
  }
  @Test
  public void testParseException(){
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(
        forbiddenResourceGuideLineValidator, RESOURCE_PATH + "/parseException");
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [FRG3]: Invalid HEAT format problem - [while scanning for the next token\n" +
            "found character '\\t(TAB)' that cannot start any token. (Do not use \\t(TAB) " +
            "for indentation)\n" + " in 'reader', line 5, column 1:\n" +
            "    \t\t\tresources:\n" +
            "    ^\n" +
            "]");
  }

  @Test
  public void testInvalidResourceType(){
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(
        forbiddenResourceGuideLineValidator, RESOURCE_PATH + "/TestInvalidResourceType");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [FRG1]: A resource has an invalid or unsupported type - null, " +
            "Resource ID [FSB2]");
  }
}
