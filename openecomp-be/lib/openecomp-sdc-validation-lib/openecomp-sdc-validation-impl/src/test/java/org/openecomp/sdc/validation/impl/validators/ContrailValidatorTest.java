/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.sdc.validation.Validator;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * @author Avrahamg
 * @since October 06, 2016
 */
public class ContrailValidatorTest {

  private static final String RESOURCE_PATH = "/org/openecomp/validation/validators/contrailvalidatorresources";
  private Validator validator = new ContrailValidator();

  @Test
  public void testWarningMessageExistWhenConrailV1AndV2ResourcesCollidesInSameHeatFile() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/collidesinsameheatfile/");
    validateMessage(messages,
        "WARNING: [CTL2]: HEAT Package includes both Contrail 2 and Contrail 3 " +
            "resources. Contrail 2 resources can be found in  file 'first.yaml' , resources :" +
            "'jsa_net1' . Contrail 3 resources can be found in  file 'first.yaml' , resources :" +
            "'jsa_net2' ",
        "first.yaml", 2);
  }

  @Test
  public void testParseException(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/parseException/");
    validateMessage(messages,
        "ERROR: [CTL4]: Invalid HEAT format problem - [while scanning for the next " +
            "token\n" + "found character '\\t(TAB)' that cannot start any token. " +
            "(Do not use \\t(TAB) for indentation)\n" +
            " in 'reader', line 10, column 1:\n" +
            "    \t\t\tresources:\n" +
            "    ^\n" +
            "]",
        "first.yaml", 1);

  }

  @Test
  public void testWarningMessageExistWhenConrailV1AndV2ResourcesCollidesInDifferentHeatFiles() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/collidesindifferentheatfiles/");
    validateMessage(messages,
        "WARNING: [CTL2]: HEAT Package includes both Contrail 2 and Contrail 3 " +
            "resources. Contrail 2 resources can be found in  file 'first.yaml' , resources :" +
            "'jsa_net1', 'jsa_net3' . Contrail 3 resources can be found in  file 'second.yaml' , " +
            "resources :'jsa_net2', 'jsa_net4',  file 'first.yaml' , resources :'jsa_net5' ",
        "first.yaml", 3);
  }

  @Test
  public void testWarningMessageNotExistWhenConrailV1AndV2ResourcesCollidesInNonHeatFile() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/collidesinnontheatfiles/");
    validateMessage(messages,
        "WARNING: [CTL2]: HEAT Package includes both Contrail 2 and Contrail 3 " +
            "resources. Contrail 2 resources can be found in  file 'first.yaml' , resources :" +
            "'jsa_net1' . Contrail 3 resources can be found in  file 'second.yaml' , " +
            "resources :'jsa_net2' ",
        "first.yaml", 2);
    ;
  }

  @Test
  public void testWarningMessageNotExistWhenOnlyConrailV1Resources() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/notcollides/");
    validateMessage(messages,
        "WARNING: [CTL3]: Contrail 2.x deprecated resource is in use, " +
            "Resource ID [jsa_net1]", "first.yaml",
        2);
  }


  @Test
  public void testWarningMessageOnResourceWithContrailType() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/validatenocontrailresource/");
    validateMessage(messages,
        "WARNING: [CTL3]: Contrail 2.x deprecated resource is in use, " +
            "Resource ID [template_NetworkPolicy]",
        "first.yaml", 1);
  }

  @Test
  public void testInvalidHeatStructure(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/invalidHeatStructure/");
    validateMessage(messages,
        "ERROR: [CTL1]: Invalid HEAT format problem - [The file 'first.yaml' " +
            "has no content]",
        "first.yaml", 1);
  }

  @Test
  public void testInvalidHeatStructuredueToParsingError(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/invalidHeatStructure/");
    validateMessage(messages,
        "ERROR: [CTL1]: Invalid HEAT format problem - [The file 'first.yaml' " +
            "has no content]",
        "first.yaml", 1);
  }


  private void validateMessage(Map<String, MessageContainer> messages, String expectedMessage,
                               String fileNameWithErrorToCheck, int sizeOfFileMessageList) {
    Assert.assertEquals(messages.size(), 1);
    List<ErrorMessage> errorMessageList =
        messages.get(fileNameWithErrorToCheck).getErrorMessageList();
    Assert.assertEquals(errorMessageList.size(), sizeOfFileMessageList);
    Assert.assertEquals(errorMessageList.get(0).getMessage(), expectedMessage);
  }


}
