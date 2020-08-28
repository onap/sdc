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

package org.onap.sdc.frontend.ci.tests.verificator;

import com.aventstack.extentreports.Status;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ErrorInfo;
import org.onap.sdc.backend.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.onap.sdc.frontend.ci.tests.datatypes.ErrorMessageProperties;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.testng.Assert;

public class ErrorMessageUIVerificator {

    private static ErrorMessageProperties getErrorByType(ActionStatus errorType) {
        try {
            ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(errorType.name());
            String messageId = errorInfo.getMessageId();
            String code = errorInfo.getCode().toString();

            return new ErrorMessageProperties(messageId, code);
        } catch (Exception e) {
            return null;
        }
    }

    public static void validateErrorMessage(ActionStatus errorMessage) {
        String errorMessageBox = null;
        try {
            //errorMessageBox = GeneralUIUtils.getWebElementByClassName("w-sdc-modal-caption").getText();
            errorMessageBox = GeneralUIUtils.getWebElementByClassName("error-message-component").getText();
        } catch (Exception e) {
            ExtentTestActions.log(Status.INFO, "Did not find an error message popup.");
            Assert.fail("Did not find an error message popup.");
        }

        ExtentTestActions.log(Status.INFO, "An error message raised, validating its content.");
        ErrorMessageProperties expectedResponseError = getErrorByType(errorMessage);
        Assert.assertTrue(errorMessageBox.contains(expectedResponseError.getCode()), "Error message code is not " + expectedResponseError.getCode());
        Assert.assertTrue(errorMessageBox.contains(expectedResponseError.getMessageId()), "Error message ID is not " + expectedResponseError.getMessageId());
    }

}
