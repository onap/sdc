/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.validation.impl.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.validation.impl.util.HelmValidatorHttpClient;
import org.openecomp.sdc.validation.type.helmvalidator.HelmValidatorConfig;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

@ExtendWith(MockitoExtension.class)
class HelmValidatorTest {

    private static final String RESOURCE_PATH = "/org/openecomp/validation/validators/helm_validator";
    private static final String VALIDATOR_RESPONSE_WITH_ERRORS = "{\"renderErrors\":[\"[ERROR] render error\"],\""
        + "lintWarning\":[\"[WARNING] warning\"],"
        + "\"lintError\":[\"[ERROR] lint error\"],"
        + "\"versionUsed\":\"3.5.2\",\"valid\":false,\"deployable\":true}";
    private static final String VALIDATOR_RESPONSE_WITHOUT_LINTING = "{\"renderErrors\":[\"[ERROR] render error 1\"],"
        + "\"versionUsed\":\"3.5.2\",\"valid\":false,\"deployable\":true}";
    private static final String VALIDATOR_ERROR_RESPONSE = "{\"message\":\"Error response message\"}";
    public static final String TEST_RESOURCES = "./src/test/resources/";

    @InjectMocks
    private HelmValidator validator;
    @Mock
    private HelmValidatorHttpClient helmValidatorHttpClient;
    @Mock
    private HelmValidatorConfig helmValidatorConfig;

    @Test
    void shouldCallHelmValidatorForEveryChartWhenIsEnabled() throws Exception {
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        String chartPath = RESOURCE_PATH + "/valid_two_charts";

        var messages = new ValidationTestUtil().testValidator(validator, chartPath);

        byte[] firstChartContent = Files.readAllBytes(Path.of(TEST_RESOURCES + chartPath + "/chart1.tgz"));
        byte[] secondChartContent = Files.readAllBytes(Path.of(TEST_RESOURCES + chartPath + "/chart2.tgz"));
        verify(helmValidatorHttpClient).execute(eq("chart1.tgz"), eq(firstChartContent), eq(helmValidatorConfig));
        verify(helmValidatorHttpClient).execute(eq("chart2.tgz"), eq(secondChartContent), eq(helmValidatorConfig));
        verify(helmValidatorHttpClient, times(2)).execute(any(), any(), any());
        assertEquals(2, messages.size());
    }

    @Test
    void shouldNotCallHelmValidatorClientWhenIsDisabled() throws Exception {
        when(helmValidatorConfig.isEnabled()).thenReturn(false);
        String chartPath = RESOURCE_PATH + "/valid_two_charts";

        var messages = new ValidationTestUtil().testValidator(validator, chartPath);

        verify(helmValidatorHttpClient, times(0)).execute(any(), any(), any());
        assertEquals(0, messages.size());
    }

    @Test
    void shouldContainsMessagesForEveryChartWhenIsEnabled() throws Exception {
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        String chartPath = RESOURCE_PATH + "/valid_two_charts";
        when(helmValidatorHttpClient.execute(eq("chart1.tgz"), any(), any()))
            .thenReturn(new HttpResponse<>(VALIDATOR_RESPONSE_WITH_ERRORS, 200));
        when(helmValidatorHttpClient.execute(eq("chart2.tgz"), any(), any()))
            .thenReturn(new HttpResponse<>(VALIDATOR_RESPONSE_WITHOUT_LINTING, 200));

        var messages = new ValidationTestUtil().testValidator(validator, chartPath);

        assertEquals(2, messages.size());
        var firstChartErrors = messages.get("chart1.tgz").getErrorMessageList();
        var secondChartErrors = messages.get("chart2.tgz").getErrorMessageList();
        assertEquals(3, firstChartErrors.size());
        assertEquals(1, secondChartErrors.size());
        assertEquals("ERROR: [HELM VALIDATOR]: [ERROR] render error", firstChartErrors.get(0).getMessage());
        assertEquals("WARNING: [HELM VALIDATOR]: [ERROR] lint error", firstChartErrors.get(1).getMessage());
        assertEquals("WARNING: [HELM VALIDATOR]: [WARNING] warning", firstChartErrors.get(2).getMessage());
        assertEquals("ERROR: [HELM VALIDATOR]: [ERROR] render error 1", secondChartErrors.get(0).getMessage());
    }

    @Test
    void shouldCorectlySetErrorsAndWarningsFromHelmValidator() throws Exception {
        String validChartPath = RESOURCE_PATH + "/valid_chart";
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        when(helmValidatorHttpClient.execute(any(), any(), any()))
            .thenReturn(new HttpResponse<>(VALIDATOR_RESPONSE_WITH_ERRORS, 200));

        var messages = new ValidationTestUtil().testValidator(validator, validChartPath);

        var chartErrors = messages.get("chart.tgz").getErrorMessageList();
        assertEquals(1, messages.size());
        assertEquals(3, chartErrors.size());
        assertEquals("ERROR: [HELM VALIDATOR]: [ERROR] render error", chartErrors.get(0).getMessage());
        assertEquals("WARNING: [HELM VALIDATOR]: [ERROR] lint error", chartErrors.get(1).getMessage());
        assertEquals("WARNING: [HELM VALIDATOR]: [WARNING] warning", chartErrors.get(2).getMessage());
    }

    @Test
    void shouldAddWarningWhenErrorResponseFromValidator() throws Exception {
        String chartPath = RESOURCE_PATH + "/valid_chart";
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        when(helmValidatorHttpClient.execute(any(), any(), any()))
            .thenReturn(new HttpResponse<>(VALIDATOR_ERROR_RESPONSE, 400));

        var messages = new ValidationTestUtil().testValidator(validator, chartPath);

        var chartErrors = messages.get("chart.tgz").getErrorMessageList();
        assertEquals(1, chartErrors.size());
        assertEquals(ErrorLevel.WARNING, chartErrors.get(0).getLevel());
        assertEquals("WARNING: [HELM VALIDATOR]: Error response message", chartErrors.get(0).getMessage());
    }

}
