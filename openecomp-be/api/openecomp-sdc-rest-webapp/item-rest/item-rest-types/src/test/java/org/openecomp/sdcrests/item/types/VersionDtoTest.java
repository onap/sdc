/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdcrests.item.types;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.code.beanmatchers.BeanMatchers;
import com.google.code.beanmatchers.ValueGenerator;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

public class VersionDtoTest {

    @BeforeAll
    public static void registerInstantGenerator() {
        BeanMatchers.registerValueGenerator(
            new ValueGenerator<Instant>() {
                @Override
                public Instant generate() {
                    return Instant.now();
                }
            },
            Instant.class
        );
    }

    @Test
    void testBean() {
        assertThat(VersionDto.class,  allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSetters()
        ));
    }

    @Test
    void shouldSerializeInstantFieldsAsIsoStrings() throws Exception {
        VersionDto dto = new VersionDto();
        dto.setCreationTime(Instant.parse("2026-03-25T10:01:14.817Z"));
        dto.setModificationTime(Instant.parse("2026-03-25T10:01:14.874Z"));

        String json = new ObjectMapper().writeValueAsString(dto);

        assertTrue(json.contains("\"creationTime\":\"2026-03-25T10:01:14.817Z\""));
        assertTrue(json.contains("\"modificationTime\":\"2026-03-25T10:01:14.874Z\""));
    }
}