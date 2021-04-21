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
package org.openecomp.sdc.validation.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.protocol.HTTP;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.http.client.api.HttpExecuteException;
import org.openecomp.sdc.common.http.client.api.HttpRequestHandler;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.validation.type.helmvalidator.HelmValidatorConfig;

@ExtendWith(MockitoExtension.class)
class HelmValidatorHttpClientTest {

    private static final String EXAMPLE_RESPONSE = "{\"renderErrors\":[],\"lintWarning\":[\"[WARNING] warning description\"],\"lintError\":[],\"versionUsed\":\"3.5.2\",\"valid\":false,\"deployable\":true}";
    private static final String TEST_CHART_FILE_NAME = "testchart";
    private static final String HTTP_ENTITY_PATTERN =
        "Content-Disposition: form-data; name=\"file\"; filename=\"{0}\"  {1} "
            + "Content-Disposition: form-data; name=\"isLinted\"  {2} "
            + "Content-Disposition: form-data; name=\"isStrictLinted\"  {3} "
            + "Content-Disposition: form-data; name=\"versionDesired\"  {4}";
    private static final String HTTPS_TEST_URL = "https://test-url";
    private static final String TEST_VERSION = "3.5.6";
    @Mock
    private HttpRequestHandler httpRequestHandler;
    @Mock
    private HelmValidatorConfig helmValidatorConfig;
    @InjectMocks
    private HelmValidatorHttpClient client;
    @Captor
    private ArgumentCaptor<HttpEntity> httpEntityCaptor;
    @TempDir
    static Path tempDir;

    @BeforeEach
    void init() throws HttpExecuteException {
        when(httpRequestHandler.post(any(), any(), any(), any()))
            .thenReturn(new HttpResponse<>(EXAMPLE_RESPONSE, 215));
        when(helmValidatorConfig.getValidatorUrl()).thenReturn(HTTPS_TEST_URL);
        when(helmValidatorConfig.getVersion()).thenReturn(TEST_VERSION);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://test123", "test-url:8080"})
    void shouldSendPostToValidatorUrl(String validatorUrl) throws Exception {
        when(helmValidatorConfig.getValidatorUrl()).thenReturn(validatorUrl);
        //given, when
        var response = client.execute(TEST_CHART_FILE_NAME, "".getBytes(), helmValidatorConfig);
        //then
        Assertions.assertEquals(215, response.getStatusCode());
        Assertions.assertEquals(EXAMPLE_RESPONSE, response.getResponse());
        verify(httpRequestHandler).post(eq(validatorUrl), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.5.4", "v3", "1.2.3"})
    void shouldPrepareRequestWithDesiredVersion(String desiredVersion) throws Exception {
        //given
        Path chartPath = getTestPath(TEST_CHART_FILE_NAME, "");
        when(helmValidatorConfig.getVersion()).thenReturn(desiredVersion);
        //when
        client.execute(chartPath.toString(), "".getBytes(), helmValidatorConfig);
        //then
        verify(httpRequestHandler).post(any(), any(), httpEntityCaptor.capture(), any());

        Object[] testArgs = {chartPath, "", false, false, desiredVersion};
        String expectedHttpEntityContent = new MessageFormat(HTTP_ENTITY_PATTERN).format(testArgs);
        String actualHttpEntityContent = getHttpEntityContent();

        assertEquals(expectedHttpEntityContent, actualHttpEntityContent);

    }

    @ParameterizedTest
    @CsvSource({"fileName,chart content 123", "b,content", "chart,12345\n21234"})
    void shouldPrepareRequestWithChartFromConfig(String testChartFileName, String testChartContent)
        throws Exception {
        //given
        Path chartPath = getTestPath(testChartFileName, testChartContent);
        //when
        client.execute(chartPath.toString(), testChartContent.getBytes(), helmValidatorConfig);
        //then
        verify(httpRequestHandler).post(any(), any(), httpEntityCaptor.capture(), any());

        Object[] testArgs = {chartPath, testChartContent, false, false, "3.5.6"};
        String expectedHttpEntityContent = new MessageFormat(HTTP_ENTITY_PATTERN).format(testArgs);
        String actualHttpEntityContent = getHttpEntityContent();

        assertEquals(expectedHttpEntityContent, actualHttpEntityContent);
    }

    @Test
    void shouldPrepareLintableRequest() throws Exception {
        //given
        Path chartPath = getTestPath(TEST_CHART_FILE_NAME, "");
        when(helmValidatorConfig.isLintable()).thenReturn(true);
        //when
        client.execute(chartPath.toString(), "".getBytes(), helmValidatorConfig);
        //then
        verify(httpRequestHandler).post(any(), any(), httpEntityCaptor.capture(), any());

        Object[] testArgs = {chartPath, "", true, false, "3.5.6"};
        String expectedHttpEntityContent = new MessageFormat(HTTP_ENTITY_PATTERN).format(testArgs);
        String actualHttpEntityContent = getHttpEntityContent();

        assertEquals(expectedHttpEntityContent, actualHttpEntityContent);
    }

    @Test
    void shouldPrepareStrictLintableRequest() throws Exception {
        //given
        Path chartPath = getTestPath(TEST_CHART_FILE_NAME, "");
        when(helmValidatorConfig.isStrictLintable()).thenReturn(true);
        //when
        client.execute(chartPath.toString(), "".getBytes(), helmValidatorConfig);
        //then
        verify(httpRequestHandler).post(any(), any(), httpEntityCaptor.capture(), any());

        Object[] testArgs = {chartPath, "", false, true, "3.5.6"};
        String expectedHttpEntityContent = new MessageFormat(HTTP_ENTITY_PATTERN).format(testArgs);
        String actualHttpEntityContent = getHttpEntityContent();

        assertEquals(expectedHttpEntityContent, actualHttpEntityContent);
    }

    private Path getTestPath(String testChartFileName, String testChartContent) throws IOException {
        Path chartPath = tempDir.resolve(testChartFileName);
        Files.writeString(chartPath, testChartContent);
        return chartPath;
    }

    private String getHttpEntityContent() throws IOException {
        final var httpEntityCaptorValue = httpEntityCaptor.getValue();
        try (InputStream content = httpEntityCaptorValue.getContent()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(content, HTTP.DEF_CONTENT_CHARSET));
            return reader.lines()
                .filter(
                    line -> line.startsWith("Content-Disposition:") || (!line.contains(":") && !line.contains("--")))
                .collect(Collectors.joining(" "));
        }
    }
}
