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

  private Validator validator = new ContrailValidator();

  @Test
  public void testWarningMessageExistWhenConrailV1AndV2ResourcesCollidesInSameHeatFile() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/contrailvalidatorresources/collidesinsameheatfile/");
    validateMessage(messages,
        "WARNING: HEAT Package includes both Contrail 2 and Contrail 3 resources. Contrail 2 resources can be found in  file 'first.yaml' , resources :'jsa_net1' . Contrail 3 resources can be found in  file 'first.yaml' , resources :'jsa_net2' ",
        "first.yaml", 2);
  }

  @Test
  public void testWarningMessageExistWhenConrailV1AndV2ResourcesCollidesInDifferentHeatFiles() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/contrailvalidatorresources/collidesindifferentheatfiles/");
    validateMessage(messages,
        "WARNING: HEAT Package includes both Contrail 2 and Contrail 3 resources. Contrail 2 resources can be found in  file 'first.yaml' , resources :'jsa_net1', 'jsa_net3' . Contrail 3 resources can be found in  file 'second.yaml' , resources :'jsa_net2', 'jsa_net4',  file 'first.yaml' , resources :'jsa_net5' ",
        "first.yaml", 3);
  }

  @Test
  public void testWarningMessageNotExistWhenConrailV1AndV2ResourcesCollidesInNonHeatFile() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/contrailvalidatorresources/collidesinnontheatfiles/");
    validateMessage(messages,
        "WARNING: HEAT Package includes both Contrail 2 and Contrail 3 resources. Contrail 2 resources can be found in  file 'first.yaml' , resources :'jsa_net1' . Contrail 3 resources can be found in  file 'second.yaml' , resources :'jsa_net2' ",
        "first.yaml", 2);
    ;
  }

  @Test
  public void testWarningMessageNotExistWhenOnlyConrailV1Resources() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/contrailvalidatorresources/notcollides/");
    validateMessage(messages,
        "WARNING: Contrail 2.x deprecated resource is in use, Resource ID [jsa_net1]", "first.yaml",
        2);
  }


  @Test
  public void testWarningMessageOnResourceWithContrailType() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/contrailvalidatorresources/validatenocontrailresource/");
    validateMessage(messages,
        "WARNING: Contrail 2.x deprecated resource is in use, Resource ID [template_NetworkPolicy]",
        "first.yaml", 1);
    ;
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
