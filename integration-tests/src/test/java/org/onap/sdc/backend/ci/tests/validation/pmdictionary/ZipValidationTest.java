/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
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
package org.onap.sdc.backend.ci.tests.validation.pmdictionary;

import org.junit.jupiter.api.Test;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ZipValidationTest {

    private static final String PM_DICTIONARY_FILE = "pmdict.yaml";
    private static final String ZIPS_PATH = "Files/VNFs/validation/pmdictionary";

    @Test
    public void shouldReportOnlyFileNotReferenced_whenValidPmDictionaryZip() throws IOException {
        // given
        FileContentHandler fileContentHandler = getFileContentHandler("validPmDict.zip");

        // when
        Map<String, List<ErrorMessage>> errors = ValidationManagerUtil.initValidationManager(fileContentHandler)
                .validate();
        List<ErrorMessage> pmDictValidationErrors = extractPmDictValidationErrors(errors);

        // then
        assertThat(pmDictValidationErrors).isEmpty();
    }

    @Test
    public void shouldReportPmDictionaryErrors_whenInvalidPmDictionaryZip() throws IOException {
        // given
        FileContentHandler fileContentHandler = getFileContentHandler("invalidPmDict.zip");
        ErrorMessage errorMessage = new ErrorMessage(ErrorLevel.ERROR,
                "ERROR: [PM_DICT]: Document Number: 1, Path: /pmMetaData/, Problem: Key not found: pmHeader");


        // when
        Map<String, List<ErrorMessage>> errors = ValidationManagerUtil.initValidationManager(fileContentHandler)
                .validate();
        List<ErrorMessage> pmDictValidationErrors = extractPmDictValidationErrors(errors);

        // then
        assertThat(pmDictValidationErrors).hasSize(1);
        assertThat(pmDictValidationErrors).containsExactly(errorMessage);
    }

    private FileContentHandler getFileContentHandler(String filename) throws IOException {
        return CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP,
                Objects.requireNonNull(this.getClass()
                        .getClassLoader()
                        .getResourceAsStream(ZIPS_PATH + "/" + filename))
                        .readAllBytes());
    }

    private List<ErrorMessage> extractPmDictValidationErrors(Map<String, List<ErrorMessage>> errors) {
        return errors.getOrDefault(PM_DICTIONARY_FILE, List.of())
                .stream()
                .filter(error -> error.getMessage().contains("[PM_DICT]"))
                .collect(Collectors.toList());
    }
}
