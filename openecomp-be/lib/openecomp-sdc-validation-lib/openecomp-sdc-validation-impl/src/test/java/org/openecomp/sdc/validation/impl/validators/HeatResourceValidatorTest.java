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

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

public class HeatResourceValidatorTest {

  Validator validator=new HeatResourceValidator();

  @Test
  public void testParseException(){
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(validator,
        "/org/openecomp/validation/validators/guideLineValidator/baseHeatDoesNotExposeNetwork/parseException/");
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [RBV2]: Invalid HEAT format problem - [while scanning for the next token\n" +
            "found character '\\t(TAB)' that cannot start any token. (Do not use \\t(TAB) for indentation)\n" +
            " in 'reader', line 5, column 1:\n" +
            "    \t\t\tresources:\n" +
            "    ^\n" +
            "]");
  }
}
