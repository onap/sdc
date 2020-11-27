/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.validation.impl.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

public class PmDictionaryValidatorTest {

    private static final String RESOURCE_PATH = "/org/openecomp/validation/validators/pm_dictionary_validator";
    private static final String VALID_PM_DICTIONARY_YAML = "valid_pm_dictionary.yaml";
    private static final String INVALID_PM_DICTIONARY_YAML = "invalid_pm_dictionary.yaml";

    private static final List<String> VALID_PM_DICTIONARY_EXTENSIONS = List.of(
        "pmdict.yml",
        "pmdict.yaml",
        "pm_dict.yml",
        "pm_dict.yaml",
        "pmdictionary.yml",
        "pmdictionary.yaml",
        "pm_dictionary.yml",
        "pm_dictionary.yaml"
    );

    @Test
    public void shouldMatchProperPmDictNames() {
        for (String ext : VALID_PM_DICTIONARY_EXTENSIONS) {
            assertTrue(PmDictionaryValidator.isPmDictionary(ext));
            assertTrue(PmDictionaryValidator.isPmDictionary("my" + ext));
            assertTrue(PmDictionaryValidator.isPmDictionary("my_" + ext));
            assertTrue(PmDictionaryValidator.isPmDictionary("my_" + ext.toUpperCase()));
        }
    }

    @Test
    public void shouldNotReturnErrorsWhenValidPmDict() {
        Map<String, MessageContainer> messages = runValidation(
            RESOURCE_PATH + "/" + VALID_PM_DICTIONARY_YAML);

        assertNotNull(messages);
        assertEquals(0, messages.size());
    }

    @Test
    public void shouldReturnErrorsWhenInvalidPmDict() {
        Map<String, MessageContainer> messages = runValidation(
            RESOURCE_PATH + "/" + INVALID_PM_DICTIONARY_YAML);

        assertNotNull(messages);
        assertNotNull(messages.get(INVALID_PM_DICTIONARY_YAML));
        assertEquals(4, messages.get(INVALID_PM_DICTIONARY_YAML).getErrorMessageList().size());
    }

    private Map<String, MessageContainer> runValidation(String path) {
        PmDictionaryValidator validator = new PmDictionaryValidator();
        return ValidationTestUtil.testValidator(validator, path);
    }
}