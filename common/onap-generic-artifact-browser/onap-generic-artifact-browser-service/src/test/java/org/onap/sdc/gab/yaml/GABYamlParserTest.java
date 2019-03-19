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

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.onap.sdc.gab.model.GABResult;
import org.onap.sdc.gab.model.GABResultEntry;
import org.onap.sdc.gab.model.GABResults;

class GABYamlParserTest {

    private static final String EVENT_PRESENCE = "event.presence";
    private static final String REQUIRED = "required";
    private static final String INVALID_EVENT_HEARTBEAT_ACTION = "event.heartbeatAction.[1]";
    private static final String EVENT_HEARTBEAT_ACTION = "event.heartbeatAction[1]";
    private static final String FAULT = "fault";
    private static final String DOMAIN_VALUE = "event.structure.commonEventHeader.structure.domain.value";
    private static final String VNF_DOWN = "vnfDown";
    private static final String SYSLOG = "syslog";
    private static final String EVENT_STRUCTURE_HEARTBEAT_FIELDS_PRESENCE = "event.structure.heartbeatFields.presence";
    private static final String HEARTBEAT = "heartbeat";
    private static final String MEASUREMENTS_FOR_VF_SCALING = "measurementsForVfScaling";
    private static final String EVENT = "event";
    private static final String TEST = "test";
    private static final String NONEXISTENT_FILE = "nonexistent.file";
    private static final String FAULT_REGISTRATION_YML = "yaml/faultRegistration.yml";

    @Test
    void shouldNotParseAnyYamlAndGenerateEmptyMapOfKeys() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.filter(TEST).collect();
        }
        assertIterableEquals(result.getRows(), Collections.<GABResult>emptyList());
    }

    @Test
    void shouldParseNonexistentYamlAndGenerateEmptyMapOfKeys() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile(NONEXISTENT_FILE).collect();
        }
        assertIterableEquals(result.getRows(), Collections.<GABResult>emptyList());
    }

    @Test
    void shouldParseExistentFileYamlAndGenerateEmptyMapOfKeysIfNoKeywordsSearched() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile(FAULT_REGISTRATION_YML).collect();
        }
        assertIterableEquals(result.getRows(), Collections.<GABResult>emptyList());
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnSingleFilter() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile(FAULT_REGISTRATION_YML)
                .filter(EVENT_PRESENCE).filter(EVENT_PRESENCE).collect();

        }
        assertEquals(result.getRows().size(), 5);
        assertThatEntryIsEqualTo(result, 0, 0, EVENT_PRESENCE, REQUIRED);
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnTwoIndependentFilters() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile(FAULT_REGISTRATION_YML)
                .filter(DOMAIN_VALUE)
                .filter(EVENT_PRESENCE)
                .collect();

        }
        assertEquals(result.getRows().size(), 5);
        assertThatEntryIsEqualTo(result, 0 , 0, DOMAIN_VALUE, FAULT);
        assertThatEntryIsEqualTo(result, 4 , 0, DOMAIN_VALUE, SYSLOG);
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnTwoDependentFilters() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile(FAULT_REGISTRATION_YML)
                .filter(Sets.newHashSet(DOMAIN_VALUE, EVENT_PRESENCE))
                .collect();

        }
        assertEquals(result.getRows().size(), 5);
        assertThatEntryIsEqualTo(result, 0, 0, DOMAIN_VALUE, FAULT);
        assertThatEntryIsEqualTo(result, 4, 0, DOMAIN_VALUE, SYSLOG);
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnThreeCombinedFilters() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile(FAULT_REGISTRATION_YML)
                .filter(Sets.newHashSet(DOMAIN_VALUE, EVENT_PRESENCE))
                .filter(EVENT_STRUCTURE_HEARTBEAT_FIELDS_PRESENCE)
                .collect();

        }
        assertEquals(result.getRows().size(), 5);
        assertThatEntryIsEqualTo(result, 0,0, DOMAIN_VALUE, FAULT);
        assertThatEntryIsEqualTo(result, 0,1, EVENT_PRESENCE, REQUIRED);
        assertThatEntryIsEqualTo(result, 1,0, DOMAIN_VALUE, FAULT);
        assertThatEntryIsEqualTo(result, 1,1, EVENT_PRESENCE, REQUIRED);
        assertThatEntryIsEqualTo(result, 2,0, DOMAIN_VALUE, HEARTBEAT);
        assertThatEntryIsEqualTo(result, 2,1, EVENT_PRESENCE, REQUIRED);
        assertThatEntryIsEqualTo(result, 3,0, DOMAIN_VALUE, MEASUREMENTS_FOR_VF_SCALING);
        assertThatEntryIsEqualTo(result, 3,1, EVENT_PRESENCE, REQUIRED);
        assertThatEntryIsEqualTo(result, 4,0, DOMAIN_VALUE, SYSLOG);
        assertThatEntryIsEqualTo(result, 4,1, EVENT_PRESENCE, REQUIRED);
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysOnFilterFromArray() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile(FAULT_REGISTRATION_YML)
                .filter(Sets.newHashSet(EVENT_HEARTBEAT_ACTION, EVENT_PRESENCE))
                .collect();
        }
        assertEquals(result.getRows().size(), 5);
        assertThatEntryIsEqualTo(result,0,0, EVENT_PRESENCE, REQUIRED);
        assertThatEntryIsEqualTo(result,2,1, EVENT_HEARTBEAT_ACTION, VNF_DOWN);
    }

    @Test
    void shouldParseFaultRegistrationAndGenerateMapOfKeysRemovingInvalidKey() throws Exception {
        GABResults result;
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            result = yamlParser.parseFile(FAULT_REGISTRATION_YML)
                .filter(Sets.newHashSet(INVALID_EVENT_HEARTBEAT_ACTION, EVENT_PRESENCE))
                .collect();
        }
        assertEquals(result.getRows().size(), 5);
        assertThatEntryIsEqualTo(result, 0, 0, EVENT_PRESENCE, REQUIRED);
    }

    @Test
    void shouldParseInvalidYamlAndGenerateIOException() {
        assertThrows(IOException.class, () -> {
        try (GABYamlParser yamlParser = new GABYamlParser(new YamlParser())){
            yamlParser.parseFile("yaml/invalid.yml")
                .filter(EVENT)
                .collect();
        }});
    }

    private void assertThatEntryIsEqualTo(GABResults result, int rowIndex, int entryIndex, String path, String data){
        GABResultEntry entry = result.getRows().get(rowIndex).getEntries().get(entryIndex);
        assertEquals(entry.getData(), data);
        assertEquals(entry.getPath(), path);
    }


}