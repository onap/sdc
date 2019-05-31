/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 *
 */

package org.openecomp.sdc.common.datastructure;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;


public class ESTimeBasedEventTest {

    private ESTimeBasedEvent createTestSubject() {
        return new ESTimeBasedEvent();
    }


    @Test
    public void testCalculateYearIndexSuffix() throws Exception {
        ESTimeBasedEvent testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.calculateYearIndexSuffix();
    }


    @Test
    public void testCalculateMonthIndexSuffix() throws Exception {
        ESTimeBasedEvent testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.calculateMonthIndexSuffix();
    }


    @Test
    public void testCalculateDayIndexSuffix() throws Exception {
        ESTimeBasedEvent testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.calculateDayIndexSuffix();
    }


    @Test
    public void testCalculateHourIndexSuffix() throws Exception {
        ESTimeBasedEvent testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.calculateHourIndexSuffix();
    }


    @Test
    public void testCalculateMinuteIndexSuffix() throws Exception {
        ESTimeBasedEvent testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.calculateMinuteIndexSuffix();
    }


    @Test
    public void testGetTimestamp() throws Exception {
        ESTimeBasedEvent testSubject;
        String result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getTimestamp();
    }


    @Test
    public void testSetTimestamp() throws Exception {
        ESTimeBasedEvent testSubject;
        String timestamp = "";

        // default test
        testSubject = createTestSubject();
        testSubject.setTimestamp(timestamp);
    }


    @Test
    public void testGetFields() throws Exception {
        ESTimeBasedEvent testSubject;
        Map<String, Object> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getFields();
    }


    @Test
    public void testSetFields() throws Exception {
        ESTimeBasedEvent testSubject;
        Map<String, Object> fields = null;

        // default test
        testSubject = createTestSubject();
        testSubject.setFields(fields);
    }

    @Test
    public void testCreateEventFromJson() throws JSONException {
        //given
        String stringJson =
                "{\n" + "   \"TIMESTAMP\" : \"2000-05-01 20:00:00.000 z\",\n" + "   \"event_type\" : \"activation\" }";
        Map<String, Object> fields;
        //when
        ESTimeBasedEvent esTimeBasedEvent = ESTimeBasedEvent.createEventFromJson(stringJson);
        fields = esTimeBasedEvent.getFields();
        //then
        assertEquals(esTimeBasedEvent.timestamp, "2000-05-01 20:00:00.000 z");
        assertEquals(fields.get("event_type"), "activation");
    }

}