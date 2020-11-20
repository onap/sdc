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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PMDictionaryValidatorTest {

    @Test
    void shouldReportNoErrors_whenPmDictionaryIsValid() {
        // given
        List<String> errors = new ArrayList<>();
        final byte[] pmDictionaryContent = getResourceBytesOrFail(
            "validation.files/measurements/pmEvents-valid.yaml");

        // when
        new PMDictionaryValidator().validate(Stream.of(pmDictionaryContent), errors::add);

        // then
        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldReportErrors_whenPmDictionaryIsInvalid() {
        // given
        List<String> errors = new ArrayList<>();
        final byte[] pmDictionaryContent = getResourceBytesOrFail(
            "validation.files/measurements/pmEvents-invalid.yaml");

        // when
        new PMDictionaryValidator().validate(Stream.of(pmDictionaryContent), errors::add);

        // then
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("Key not found: pmDictionaryHeader"));
    }

    @Test
    void shouldReportEmptyYamlMessage_whenPmDictionaryIsEmpty() {
        // given
        List<String> errors = new ArrayList<>();
        final byte[] pmDictionaryContent = "".getBytes();

        // when
        new PMDictionaryValidator().validate(Stream.of(pmDictionaryContent), errors::add);

        // then
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("PM_Dictionary YAML file is empty"));
    }
}