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

package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation;

import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorConfig;

@ExtendWith(MockitoExtension.class)
public class CnfPackageValidatorTest {

    private static final String VALIDATOR_RESPONSE_WITH_ERRORS = "{\"renderErrors\":[\"[ERROR] render error\"],\""
        + "lintWarning\":[\"[WARNING] warning\"],"
        + "\"lintError\":[\"[ERROR] lint error\"],"
        + "\"versionUsed\":\"3.5.2\",\"valid\":false,\"deployable\":true}";
    private static final String VALIDATOR_RESPONSE_WITHOUT_LINTING = "{\"renderErrors\":[\"[ERROR] render error\"],"
        + "\"versionUsed\":\"3.5.2\",\"valid\":false,\"deployable\":true}";
    private static final String VALIDATOR_ERROR_RESPONSE = "{\"message\":\"Error response message\"}";

    @InjectMocks
    private CnfPackageValidator validator;
    @Mock
    private HelmValidatorHttpClient helmValidatorHttpClient;
    @Mock
    private HelmValidatorConfig helmValidatorConfig;

    @Test
    void shouldCallHelmValidatorForEveryChartWhenIsEnabled() {
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        FileContentHandler validContent = createValidPackageContent();
        List<FileData> validInput = createValidInput();

        CnfValidatorResult result = validator.validateHelmPackage(validInput, validContent);

        validInput.forEach(fd ->
            verify(helmValidatorHttpClient)
                .execute(eq(fd.getFile()), eq(validContent.getFileContent(fd.getFile())), eq(helmValidatorConfig))
        );
        assertThat(result.getErrorMessages(), is(emptyIterable()));
        assertEquals(result.getWarningMessages().size(), 3);
        validInput.forEach(fd -> assertTrue(result.getWarningMessages()
            .contains(String.format("Could not execute file %s validation using Helm", fd.getFile()))));
    }

    @Test
    void shouldNotCallHelmValidatorClientWhenIsDisabled() {
        when(helmValidatorConfig.isEnabled()).thenReturn(false);
        FileContentHandler validContent = createValidPackageContent();
        List<FileData> validInput = createValidInput();

        CnfValidatorResult result = validator.validateHelmPackage(validInput, validContent);

        verify(helmValidatorHttpClient, times(0)).execute(any(), any(), any());
        assertThat(result.getErrorMessages(), is(emptyIterable()));
        assertThat(result.getWarningMessages(), is(emptyIterable()));
        assertTrue(result.isValid());
    }

    @Test
    void shouldCorectlySetErrorsAndWarningsFromHelmValidator() {
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        when(helmValidatorHttpClient.execute(any(), any(), any()))
            .thenReturn(new HttpResponse<>(VALIDATOR_RESPONSE_WITH_ERRORS, 200));
        FileContentHandler validContent = createValidPackageContent();
        List<FileData> validInput = createValidInput();

        CnfValidatorResult result = validator.validateHelmPackage(validInput, validContent);

        verify(helmValidatorHttpClient, times(3)).execute(any(), any(), any());
        assertTrue(result.getWarningMessages().contains("[ERROR] lint error"));
        assertTrue(result.getWarningMessages().contains("[WARNING] warning"));
        assertTrue(result.getErrorMessages().contains("[ERROR] render error"));
        assertEquals(6, result.getWarningMessages().size());
        assertEquals(3, result.getErrorMessages().size());
        assertFalse(result.isValid());
    }

    @Test
    void shouldAddWarningWhenErrorResponseFromValidator() {
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        when(helmValidatorHttpClient.execute(any(), any(), any()))
            .thenReturn(new HttpResponse<>(VALIDATOR_ERROR_RESPONSE, 400));
        FileContentHandler validContent = createValidPackageContent();
        List<FileData> validInput = createValidInput();

        CnfValidatorResult result = validator.validateHelmPackage(validInput, validContent);

        verify(helmValidatorHttpClient, times(3)).execute(any(), any(), any());
        assertEquals(3, result.getWarningMessages().size());
        assertEquals(0, result.getErrorMessages().size());
        assertTrue(result.getWarningMessages().contains("Error response message"));
        assertTrue(result.isValid());
    }


    @Test
    void shouldResultContainsLintErrorsAsWarning() {
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        when(helmValidatorHttpClient.execute(any(), any(), any()))
            .thenReturn(new HttpResponse<>(VALIDATOR_RESPONSE_WITHOUT_LINTING, 200));
        FileContentHandler validContent = createValidPackageContent();
        List<FileData> validInput = createValidInput();

        CnfValidatorResult result = validator.validateHelmPackage(validInput, validContent);

        verify(helmValidatorHttpClient, times(3)).execute(any(), any(), any());
        assertEquals(0, result.getWarningMessages().size());
        assertEquals(3, result.getErrorMessages().size());
        assertTrue(result.getErrorMessages().contains("[ERROR] render error"));
        assertFalse(result.isValid());
    }

    @Test
    void shouldBeInvalidWhenIsNotDeployable() {
        when(helmValidatorConfig.isEnabled()).thenReturn(true);
        when(helmValidatorHttpClient.execute(any(), any(), any()))
            .thenReturn(new HttpResponse<>("{\"deployable\":false}", 200));
        FileContentHandler validContent = createValidPackageContent();
        List<FileData> validInput = createValidInput();

        CnfValidatorResult result = validator.validateHelmPackage(validInput, validContent);

        verify(helmValidatorHttpClient, times(3)).execute(any(), any(), any());
        assertEquals(0, result.getWarningMessages().size());
        assertEquals(0, result.getErrorMessages().size());
        assertFalse(result.isValid());
    }

    @Test
    void shouldBeValidForNullInput() {
        CnfValidatorResult result = validator.validateHelmPackage(null, null);

        List<String> errorMessages = result.getErrorMessages();
        List<String> warningMessages = result.getWarningMessages();
        assertThat(errorMessages, is(emptyIterable()));
        assertThat(warningMessages, is(emptyIterable()));
        assertTrue(result.isValid());
    }

    @Test
    void shouldBeValidForEmptyInput() {
        CnfValidatorResult result = validator.validateHelmPackage(Collections.emptyList(), null);

        List<String> messages = result.getErrorMessages();
        assertThat(messages, is(emptyIterable()));
        assertTrue(result.isValid());
    }

    @Test
    void shouldBeValid() {
        CnfValidatorResult result = validator.validateHelmPackage(createValidInput(), null);

        List<String> messages = result.getErrorMessages();
        assertThat(messages, is(emptyIterable()));
        assertTrue(result.isValid());
    }

    @Test
    void shouldBeInvalidNoneIsMarkedAsBase() {
        CnfValidatorResult result = validator.validateHelmPackage(noneIsMarkedAsBase(), null);

        List<String> messages = result.getErrorMessages();
        assertEquals(messages.size(), 1);
        assertEquals(messages.get(0), "None of charts is marked as 'isBase'.");
        assertFalse(result.isValid());
    }

    @Test
    void shouldBeInvalidMultipleAreMarkedAsBase() {
        CnfValidatorResult result = validator.validateHelmPackage(multipleAreMarkedAsBase(), null);

        List<String> messages = result.getErrorMessages();
        assertEquals(messages.size(), 1);
        assertEquals(messages.get(0), "More than one chart is marked as 'isBase'.");
        assertFalse(result.isValid());
    }

    @Test
    void shouldBeInvalidIsBaseMissing() {
        CnfValidatorResult result = validator.validateHelmPackage(isBaseMissing(), null);

        List<String> messages = result.getErrorMessages();
        assertEquals(messages.size(), 1);
        assertEquals(messages.get(0), "Definition of 'isBase' is missing in 2 charts.");
        assertFalse(result.isValid());
    }

    @Test
    void shouldBeInvalidDueMultipleReasons() {
        CnfValidatorResult result = validator.validateHelmPackage(invalidMultipleReasons(), null);

        List<String> messages = result.getErrorMessages();
        assertEquals(messages.size(), 2);
        assertEquals(messages.get(0), "Definition of 'isBase' is missing in 1 charts.");
        assertEquals(messages.get(1), "None of charts is marked as 'isBase'.");
        assertFalse(result.isValid());
    }

    private List<FileData> createValidInput() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(true, "test1.tgz"));
        files.add(createFileData(false, "test2.tgz"));
        files.add(createFileData(false, "test3.tgz"));
        return files;
    }

    private FileContentHandler createValidPackageContent() {
        FileContentHandler contentHandler = new FileContentHandler();
        contentHandler.addFile("test1.tgz", "testContent".getBytes());
        contentHandler.addFile("test2.tgz", "testContent34".getBytes());
        contentHandler.addFile("test3.tgz", "testContent65".getBytes());
        return contentHandler;
    }

    private List<FileData> noneIsMarkedAsBase() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(false));
        files.add(createFileData(false));
        files.add(createFileData(false));
        return files;
    }

    private List<FileData> multipleAreMarkedAsBase() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(true));
        files.add(createFileData(true));
        files.add(createFileData(false));
        return files;
    }

    private List<FileData> isBaseMissing() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(true));
        files.add(createFileData(null));
        files.add(createFileData(null));
        files.add(createFileData(false));
        return files;
    }

    private List<FileData> invalidMultipleReasons() {
        List<FileData> files = new ArrayList<>();
        files.add(createFileData(false));
        files.add(createFileData(null));
        files.add(createFileData(false));
        files.add(createFileData(false));
        return files;
    }

    private FileData createFileData(Boolean base) {
        FileData f = new FileData();
        f.setBase(base);
        return f;
    }

    private FileData createFileData(Boolean base, String fileName) {
        FileData f = new FileData();
        f.setBase(base);
        f.setFile(fileName);
        return f;
    }

}
