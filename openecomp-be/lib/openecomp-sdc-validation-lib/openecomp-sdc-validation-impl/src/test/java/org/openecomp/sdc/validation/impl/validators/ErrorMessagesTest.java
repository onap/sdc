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

import org.junit.Test;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.sdc.common.errors.Messages;
import org.testng.Assert;


public class ErrorMessagesTest {

  @Test
  public void testErrorFormatWithOneParam() {
    String error1 = ErrorMessagesFormatBuilder
        .getErrorWithParameters(Messages.MISSING_FILE_NAME_IN_MANIFEST.getErrorMessage(),
            "file.yaml");
    Assert.assertNotNull(error1);
  }

  @Test
  public void testErrorFormatWithTwoParams() {
    String error1 = ErrorMessagesFormatBuilder
        .getErrorWithParameters(Messages.REFERENCED_PARAMETER_NOT_FOUND.getErrorMessage(), "param",
            "res");
    Assert.assertNotNull(error1);
  }
  @Test
  public void testErrorFormatWithErrorCode() {
    String error = ErrorMessagesFormatBuilder
            .getErrorWithParameters(new ErrorMessageCode("TestCode"), Messages.MISSING_NOVA_SERVER_METADATA
                    .getErrorMessage(), "param");
    Assert.assertEquals("[TestCode]: Missing Nova Server Metadata property, Resource ID [param]", error);
  }
}
