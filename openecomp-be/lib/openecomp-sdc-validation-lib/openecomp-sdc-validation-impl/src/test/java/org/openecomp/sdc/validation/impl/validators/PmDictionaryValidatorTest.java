/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020-2021 Nokia Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

public class PmDictionaryValidatorTest {

    private static final String RESOURCE_PATH = "/org/openecomp/validation/validators/pm_dictionary_validator";
    private static final String VALID_PM_DICTIONARY_PACKAGE_PATH = "valid_file/";
    private static final String INVALID_PM_DICTIONARY_PACKAGE_PATH = "invalid_file/";
    private static final String WRONG_PM_DICTIONARY_TYPE_PACKAGE_PATH = "wrong_file_type/";
    private static final String PM_DICTIONARY_FILE_NAME = "pmdict.yaml";


    @Test
    void shouldNotReturnErrorsWhenValidPmDict() {
        // when
        Map<String, MessageContainer> messages = runValidation(
            RESOURCE_PATH + "/" + VALID_PM_DICTIONARY_PACKAGE_PATH);

        // then
        assertNotNull(messages);
        assertEquals(0, messages.size());
    }

    @Test
    void shouldReturnErrorsWhenInvalidPmDict() {
        // when
        Map<String, MessageContainer> messages = runValidation(
            RESOURCE_PATH + "/" + INVALID_PM_DICTIONARY_PACKAGE_PATH);

        // then
        assertNotNull(messages);
        assertNotNull(messages.get(PM_DICTIONARY_FILE_NAME));
        assertEquals(4, messages.get(PM_DICTIONARY_FILE_NAME).getErrorMessageList().size());
    }

    @Test
    void shouldNotReturnErrorsWhenInvalidPmDictButWrongPmDictionaryTypeInManifest() {
        // when
        Map<String, MessageContainer> messages = runValidation(
                RESOURCE_PATH + "/" + WRONG_PM_DICTIONARY_TYPE_PACKAGE_PATH);

        // then
        assertNotNull(messages);
        assertEquals(0, messages.size());
    }

    private Map<String, MessageContainer> runValidation(String path) {
        PmDictionaryValidator validator = new PmDictionaryValidator();
        return new ValidationTestUtil().testValidator(validator, path);
    }
}
