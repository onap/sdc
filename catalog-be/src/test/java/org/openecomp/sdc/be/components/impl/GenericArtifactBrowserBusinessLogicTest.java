/*-
 * ============LICENSE_START=======================================================
 * SDC
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

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import org.onap.sdc.gab.model.GABQuery;
import org.onap.sdc.gab.model.GABQuery.GABQueryType;

public class GenericArtifactBrowserBusinessLogicTest {

    private static String content = "event: {presence: required, action: [ any, any, alarm003,RECO-rebuildVnf ],\n"
        + "        structure: {\n"
        + "          commonEventHeader: {presence: required, structure: {\n"
        + "            domain: {presence: required, value: fault},\n"
        + "            eventName: {presence: required, value: Fault\\_vMrf\\_alarm003},\n"
        + "            eventId: {presence: required},\n"
        + "            nfNamingCode: {value: mrfx},\n"
        + "            priority: {presence: required, value: Medium},\n"
        + "            reportingEntityId: {presence: required},\n"
        + "            reportingEntityName: {presence: required},\n"
        + "            sequence: {presence: required},\n"
        + "            sourceId: {presence: required},\n"
        + "            sourceName: {presence: required},\n"
        + "            startEpochMicrosec: {presence: required},\n"
        + "            lastEpochMicrosec: {presence: required},\n"
        + "            version: {presence: required, value: 3.0}\n"
        + "          }},\n"
        + "          faultFields: {presence: required, structure: {\n"
        + "            alarmCondition: {presence: required, value: alarm003},\n"
        + "            eventSeverity: {presence: required, value: MAJOR},\n"
        + "            eventSourceType: {presence: required, value: virtualNetworkFunction},\n"
        + "            faultFieldsVersion: {presence: required, value: 2.0},\n"
        + "            specificProblem: {presence: required, value: \"Configuration file was\n"
        + "                        corrupt or not present\"},\n"
        + "            vfStatus: {presence: required, value: \"Requesting Termination\"}\n"
        + "          }}\n"
        + "        }}\n"
        + "---\n"
        + "# registration for clearing Fault\\_vMrf\\_alarm003Cleared\n"
        + "# Constants: the values of domain, eventName, priority,\n"
        + "# , version, alarmCondition, eventSeverity, eventSourceType,\n"
        + "# faultFieldsVersion, specificProblem,\n"
        + "# Variables (to be supplied at runtime) include: eventId,lastEpochMicrosec,\n"
        + "# reportingEntityId, reportingEntityName, sequence, sourceId,\n"
        + "# sourceName, startEpochMicrosec, vfStatus\n"
        + "event: {presence: required, action: [ any, any, alarm003, Clear ], structure: {\n"
        + "  commonEventHeader: {presence: required, structure: {\n"
        + "    domain: {presence: required, value: fault},\n"
        + "    eventName: {presence: required, value: Fault\\_vMrf\\_alarm003Cleared},\n"
        + "    eventId: {presence: required},\n"
        + "    nfNamingCode: {value: mrfx},\n"
        + "    priority: {presence: required, value: Medium},\n"
        + "    reportingEntityId: {presence: required},\n"
        + "    reportingEntityName: {presence: required},\n"
        + "    sequence: {presence: required},\n"
        + "    sourceId: {presence: required},\n"
        + "    sourceName: {presence: required},\n"
        + "    startEpochMicrosec: {presence: required},\n"
        + "    lastEpochMicrosec: {presence: required},\n"
        + "    version: {presence: required, value: 3.0}\n"
        + "  }},\n"
        + "  faultFields: {presence: required, structure: {\n"
        + "    alarmCondition: {presence: required, value: alarm003},\n"
        + "    eventSeverity: {presence: required, value: NORMAL},\n"
        + "    eventSourceType: {presence: required, value: virtualNetworkFunction},\n"
        + "    faultFieldsVersion: {presence: required, value: 2.0},\n"
        + "    specificProblem: {presence: required, value: \"Valid configuration file found\"},\n"
        + "    vfStatus: {presence: required, value: \"Requesting Termination\"}\n"
        + "  }}\n"
        + "}}";

    private static String expectedResult = "{\n"
        + "  \"data\": [\n"
        + "    {\n"
        + "      \"event.presence\": \"required\",\n"
        + "      \"event.action[0]\": \"any\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"event.presence\": \"required\",\n"
        + "      \"event.action[0]\": \"any\"\n"
        + "    }\n"
        + "  ]\n"
        + "}";

    @Test
    public void testShouldCorrectlyParseResponse() throws IOException {
        GenericArtifactBrowserBusinessLogic genericArtifactBrowserBusinessLogic = new GenericArtifactBrowserBusinessLogic();
        String result = genericArtifactBrowserBusinessLogic.searchFor(
                new GABQuery(Arrays.asList("event.presence", "event.action[0]"),
                content, GABQueryType.CONTENT));
        assertEquals(result, expectedResult);
    }
}