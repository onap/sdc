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
package org.openecomp.sdc.be.components.impl.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.validation.yaml.YamlContentValidator;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.onap.validation.yaml.exception.YamlProcessingException;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

class PMDictionaryValidatorTest {

    private YamlContentValidator yamlContentValidator;

    @BeforeEach
    void setUp() {
        yamlContentValidator = mock(YamlContentValidator.class);
    }

    @Test
    void shouldNotReturnErrors_whenArtifactTypeDoNotMatch() {
        // when
        Optional<String> errors = new PMDictionaryValidator(yamlContentValidator)
            .validateIfPmDictionary(ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.name(), "".getBytes());

        // then
        assertTrue(errors.isEmpty());
        verifyNoInteractions(yamlContentValidator);
    }

    @Test
    void shouldReturnErrors_whenArtifactTypeIsPmDictionaryAndFileIsInvalid() throws YamlProcessingException {
        // given
        byte[] fileContent = "".getBytes();
        YamlDocumentValidationError validationError = new YamlDocumentValidationError(1, "/", "error");
        when(yamlContentValidator.validate(fileContent)).thenReturn(List.of(validationError));

        // when
        Optional<String> errors = new PMDictionaryValidator(yamlContentValidator)
            .validateIfPmDictionary(ArtifactTypeEnum.PM_DICTIONARY.name(), fileContent);

        // then
        assertTrue(errors.isPresent());
        assertThat(errors.get(), is("Line number: 1, Path: /, Message: error"));
    }

    @Test
    void shouldNotReturnErrors_whenArtifactTypeIsPmDictionaryAndFileIsValid() throws YamlProcessingException {
        // then
        byte[] fileContent = "".getBytes();
        when(yamlContentValidator.validate(fileContent)).thenReturn(List.of());

        // when
        Optional<String> errors = new PMDictionaryValidator(yamlContentValidator)
            .validateIfPmDictionary(ArtifactTypeEnum.PM_DICTIONARY.name(), fileContent);

        // then
        assertTrue(errors.isEmpty());
    }
}