/*
 * ============LICENSE_START=======================================================
 * GAB
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

package org.onap.sdc.gab.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.onap.sdc.gab.model.GABResult;
import org.onap.sdc.gab.model.GABResults;

class GABYamlParserTest {

    @Test
    void shouldNotParseAnyYamlAndGenerateEmptyMapOfKeys() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.filter("test").collect();
        }
        assertIterableEquals(result.getRows(), Collections.<GABResult>emptyList());
    }

    @Test
    void shouldParseNonexistentYamlAndGenerateEmptyMapOfKeys() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile("nonexistent.file").collect();
        }
        assertIterableEquals(result.getRows(), Collections.<GABResult>emptyList());
    }

    @Test
    void shouldParseExistentFileYamlAndGenerateEmptyMapOfKeysIfNoKeywordsSearched() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile("yaml/faultRegistration.yml").collect();
        }
        assertIterableEquals(result.getRows(), Collections.<GABResult>emptyList());
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnSingleFilter() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile("yaml/faultRegistration.yml")
                .filter("event.presence").collect();

        }
        assertEquals(result.getRows().size(), 5);
        assertEquals(result.getRows().get(0).getEntries().get(0).getData(), "required");
        assertEquals(result.getRows().get(0).getEntries().get(0).getPath(), "event.presence");
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnTwoIndependentFilters() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile("yaml/faultRegistration.yml")
                .filter("event.structure.commonEventHeader.structure.domain.value")
                .filter("event.presence")
                .collect();

        }
        assertEquals(result.getRows().size(), 5);
        assertEquals(result.getRows().get(0).getEntries().get(0).getData(), "fault");
        assertEquals(result.getRows().get(0).getEntries().get(0).getPath(), "event.structure.commonEventHeader.structure.domain.value");
        assertEquals(result.getRows().get(4).getEntries().get(0).getData(), "syslog");
        assertEquals(result.getRows().get(4).getEntries().get(0).getPath(), "event.structure.commonEventHeader.structure.domain.value");
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnTwoDependentFilters() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile("yaml/faultRegistration.yml")
                .filter(Lists.newArrayList("event.structure.commonEventHeader.structure.domain.value", "event.presence"))
                .collect();

        }
        assertEquals(result.getRows().size(), 5);
        assertEquals(result.getRows().get(0).getEntries().get(0).getData(), "fault");
        assertEquals(result.getRows().get(0).getEntries().get(0).getPath(), "event.structure.commonEventHeader.structure.domain.value");
        assertEquals(result.getRows().get(4).getEntries().get(1).getData(), "required");
        assertEquals(result.getRows().get(4).getEntries().get(1).getPath(), "event.presence");
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnThreeCombinedFilters() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile("yaml/faultRegistration.yml")
                .filter(Lists.newArrayList("event.structure.commonEventHeader.structure.domain.value", "event.presence"))
                .filter("event.structure.heartbeatFields.presence")
                .collect();

        }
        assertEquals(result.getRows().size(), 5);
        assertEquals(result.getRows().get(0).getEntries().get(0).getData(), "fault");
        assertEquals(result.getRows().get(0).getEntries().get(0).getPath(), "event.structure.commonEventHeader.structure.domain.value");
        assertEquals(result.getRows().get(0).getEntries().get(1).getData(), "required");
        assertEquals(result.getRows().get(0).getEntries().get(1).getPath(), "event.presence");
        assertEquals(result.getRows().get(1).getEntries().get(0).getData(), "fault");
        assertEquals(result.getRows().get(1).getEntries().get(0).getPath(), "event.structure.commonEventHeader.structure.domain.value");
        assertEquals(result.getRows().get(1).getEntries().get(1).getData(), "required");
        assertEquals(result.getRows().get(1).getEntries().get(1).getPath(), "event.presence");
        assertEquals(result.getRows().get(2).getEntries().get(0).getData(), "heartbeat");
        assertEquals(result.getRows().get(2).getEntries().get(0).getPath(), "event.structure.commonEventHeader.structure.domain.value");
        assertEquals(result.getRows().get(2).getEntries().get(1).getData(), "required");
        assertEquals(result.getRows().get(2).getEntries().get(1).getPath(), "event.presence");
        assertEquals(result.getRows().get(3).getEntries().get(0).getData(), "measurementsForVfScaling");
        assertEquals(result.getRows().get(3).getEntries().get(0).getPath(), "event.structure.commonEventHeader.structure.domain.value");
        assertEquals(result.getRows().get(3).getEntries().get(1).getData(), "required");
        assertEquals(result.getRows().get(3).getEntries().get(1).getPath(), "event.presence");
        assertEquals(result.getRows().get(4).getEntries().get(0).getData(), "syslog");
        assertEquals(result.getRows().get(4).getEntries().get(0).getPath(), "event.structure.commonEventHeader.structure.domain.value");
        assertEquals(result.getRows().get(4).getEntries().get(1).getData(), "required");
        assertEquals(result.getRows().get(4).getEntries().get(1).getPath(), "event.presence");
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnFilterFromArray() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile("yaml/faultRegistration.yml")
                .filter(Lists.newArrayList("event.heartbeatAction[1]", "event.presence"))
                .collect();
        }
        assertEquals(result.getRows().size(), 5);
        assertEquals(result.getRows().get(0).getEntries().get(0).getData(), "required");
        assertEquals(result.getRows().get(0).getEntries().get(0).getPath(), "event.presence");
        assertEquals(result.getRows().get(2).getEntries().get(0).getData(), "vnfDown");
        assertEquals(result.getRows().get(2).getEntries().get(0).getPath(), "event.heartbeatAction[1]");
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysRemovingInvalidKey() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile("yaml/faultRegistration.yml")
                .filter(Lists.newArrayList("event.heartbeatAction.[1]", "event.presence"))
                .collect();
        }
        assertEquals(result.getRows().size(), 5);
        assertEquals(result.getRows().get(0).getEntries().get(0).getData(), "required");
        assertEquals(result.getRows().get(0).getEntries().get(0).getPath(), "event.presence");
    }

    @Test
    void shouldParseInvalidYamlAndGenerateIOException() {
        assertThrows(IOException.class, () -> {
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            yamlParser.parseFile("yaml/invalid.yml")
                .filter("event")
                .collect();
        }});
    }

}