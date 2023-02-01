/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.tosca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.version.Version;

class ToscaTypeTest {

    @Test
    void testIsValidValueBoolean() throws Exception {
        ToscaType toscaType = ToscaType.BOOLEAN;

        assertFalse(toscaType.isValidValue(""));
        assertTrue(toscaType.isValidValue("false"));
        assertTrue(toscaType.isValidValue("FalSe"));
        assertTrue(toscaType.isValidValue("true"));
        assertTrue(toscaType.isValidValue("TrUe"));
    }

    @Test
    void testIsValidValueFloat() throws Exception {
        ToscaType toscaType = ToscaType.FLOAT;

        assertFalse(toscaType.isValidValue("float"));
        assertTrue(toscaType.isValidValue("1.2534"));
        assertTrue(toscaType.isValidValue("1.2534f"));
    }

    @Test
    void testIsValidValueString() throws Exception {
        ToscaType toscaType = ToscaType.STRING;

        assertTrue(toscaType.isValidValue("string"));
        assertTrue(toscaType.isValidValue("1 string"));
        assertTrue(toscaType.isValidValue("2 s_t_r_i_n_g"));
    }

    @Test
    void testIsValidValueInteger() throws Exception {
        ToscaType toscaType = ToscaType.INTEGER;

        assertFalse(toscaType.isValidValue("integer"));
        assertTrue(toscaType.isValidValue("1235"));
    }

    @Test
    void testIsValidValueTimestamp() throws Exception {
        ToscaType toscaType = ToscaType.TIMESTAMP;

        assertFalse(toscaType.isValidValue("timestamp"));
        assertTrue(toscaType.isValidValue("2001-12-14t21:59:43.10-05:00"));
        assertFalse(toscaType.isValidValue("30 juin 2009 07:03:47"));
    }

    @Test
    void testIsValidValueVersion() throws Exception {
        ToscaType toscaType = ToscaType.VERSION;

        assertFalse(toscaType.isValidValue("version"));
        assertTrue(toscaType.isValidValue("1.2"));
        assertTrue(toscaType.isValidValue("1.2.3"));
        assertTrue(toscaType.isValidValue("1.2-3"));
    }

    @Test
    void testIsValidValueList() throws Exception {
        ToscaType toscaType = ToscaType.LIST;

        assertFalse(toscaType.isValidValue("list"));
        assertTrue(toscaType.isValidValue("[\"color\",\"type\"]"));
    }

    @Test
    void testIsValidValueMap() throws Exception {
        ToscaType toscaType = ToscaType.MAP;

        assertFalse(toscaType.isValidValue("map"));
        assertTrue(toscaType.isValidValue("{\"color\":\"yellow\",\"type\":\"renault\"}"));
    }

    @Test
    void testNotValidValueScalarUnit() {
        ToscaType testSubject = ToscaType.SCALAR_UNIT;

        assertFalse(testSubject.isValidValue("5"));
    }

    @Test
    void testIsValidValueScalarUnitSize() throws Exception {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_SIZE;

        assertTrue(testSubject.isValidValue("5 TiB"));
        assertFalse(testSubject.isValidValue("5"));
    }

    @Test
    void testIsValidValueScalarUnitTime() throws Exception {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_TIME;

        assertTrue(testSubject.isValidValue("5 d"));
        assertFalse(testSubject.isValidValue("a5 sz"));
    }

    @Test
    void testIsValidValueScalarUnitBitrate() throws Exception {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_BITRATE;

        assertTrue(testSubject.isValidValue("5 TiBps"));
        assertFalse(testSubject.isValidValue("5 bps5"));
    }

    @Test
    void testIsValidValueScalarUnitFrequency() throws Exception {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_FREQUENCY;

        assertTrue(testSubject.isValidValue("5 MHz"));
        assertFalse(testSubject.isValidValue("5"));
    }

    @Test
    void testGetToscaType() throws Exception {
        ToscaType toscaType = ToscaType.MAP;

        assertEquals(ToscaType.getToscaType("map"), toscaType);
        assertNull(ToscaType.getToscaType(null));
        assertNull(ToscaType.getToscaType("InvalidType"));
    }

    @Test
    void testIsPrimitiveType() throws Exception {
        assertFalse(ToscaType.isPrimitiveType(null));
        assertFalse(ToscaType.isPrimitiveType("map"));
        assertFalse(ToscaType.isPrimitiveType("list"));
        assertFalse(ToscaType.isPrimitiveType("String"));
        assertTrue(ToscaType.isPrimitiveType("string"));
        assertTrue(ToscaType.isPrimitiveType("integer"));
    }

    @Test
    void testIsCollectionType() throws Exception {
        assertTrue(ToscaType.isCollectionType("map"));
        assertTrue(ToscaType.isCollectionType("list"));
        assertFalse(ToscaType.isCollectionType("Map"));
        assertFalse(ToscaType.isCollectionType("string"));
        assertFalse(ToscaType.isCollectionType("integer"));
    }

    @Test
    void testConvert() throws Exception {
        ToscaType typeInt = ToscaType.INTEGER;
        assertEquals(123, typeInt.convert("123"));

        ToscaType typeBool = ToscaType.BOOLEAN;
        assertEquals(true, typeBool.convert("true"));

        ToscaType typeStr = ToscaType.STRING;
        assertEquals("str", typeStr.convert("str"));

        ToscaType typeFloat = ToscaType.FLOAT;
        assertTrue(typeFloat.convert("1.2357f") instanceof Float);

        ToscaType typeTimestamp = ToscaType.TIMESTAMP;
        assertTrue(typeTimestamp.convert("Jun 30, 2009 7:03:47 AM") instanceof Date);
        assertThrows(IllegalArgumentException.class, () -> typeTimestamp.convert(""));

        ToscaType typeVersion = ToscaType.VERSION;
        assertTrue(typeVersion.convert("1.2.3.5.6") instanceof Version);

        ToscaType typeList = ToscaType.LIST;
        assertTrue(typeList.convert("[\"str1\",\"str2\"]") instanceof List);
        assertThrows(IllegalArgumentException.class, () -> typeList.convert(""));

        ToscaType typeMap = ToscaType.MAP;
        assertTrue(typeMap.convert("{\"color\":\"yellow\",\"type\":\"renault\"}") instanceof Map);
        assertThrows(IllegalArgumentException.class, () -> typeMap.convert(""));

        ToscaType typeScalarUnit = ToscaType.SCALAR_UNIT;
        assertNull(typeScalarUnit.convert(""));

    }

    @Test
    void testConvertScalarUnitTime() {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_TIME;
        assertTrue(testSubject.convert("5d") instanceof Long);
        assertTrue(testSubject.convert("4 h") instanceof Long);
        assertTrue(testSubject.convert(" 3 m ") instanceof Long);
        assertTrue(testSubject.convert("9.5s") instanceof Long);
        assertTrue(testSubject.convert("90 ms") instanceof Long);
        assertTrue(testSubject.convert("55.44 us") instanceof Long);
        assertTrue(testSubject.convert("111ns") instanceof Long);

        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5z.ms"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("a5 ms"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5 msZ"));
    }

    @Test
    void testConvertScalarUnitSize() {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_SIZE;
        assertTrue(testSubject.convert("5 TiB") instanceof Long);
        assertTrue(testSubject.convert("4 TB") instanceof Long);
        assertTrue(testSubject.convert("31 GiB") instanceof Long);
        assertTrue(testSubject.convert("19.5 GB") instanceof Long);
        assertTrue(testSubject.convert("90 MiB") instanceof Long);
        assertTrue(testSubject.convert("55.44 MB") instanceof Long);
        assertTrue(testSubject.convert("111 KiB") instanceof Long);
        assertTrue(testSubject.convert("123. kB") instanceof Long);
        assertTrue(testSubject.convert("0.9 B") instanceof Long);

        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5z.MB"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("a5 TB"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5 TBz"));
    }

    @Test
    void testConvertScalarUnitFrequency() {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_FREQUENCY;
        assertTrue(testSubject.convert("5 GHz") instanceof Long);
        assertTrue(testSubject.convert("41 MHz") instanceof Long);
        assertTrue(testSubject.convert("319 kHz") instanceof Long);
        assertTrue(testSubject.convert("19.5 Hz") instanceof Long);

        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5z.GHz"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("a5 Hz"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5 Hza"));
    }

    @Test
    void testConvertScalarUnitBitrate() {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_BITRATE;
        assertTrue(testSubject.convert("5 TiBps") instanceof Long);
        assertTrue(testSubject.convert("41 TBps") instanceof Long);
        assertTrue(testSubject.convert("319 GiBps") instanceof Long);
        assertTrue(testSubject.convert("19.5 GBps") instanceof Long);
        assertTrue(testSubject.convert("90 MiBps") instanceof Long);
        assertTrue(testSubject.convert("55.44 MBps") instanceof Long);
        assertTrue(testSubject.convert("111 KiBps") instanceof Long);
        assertTrue(testSubject.convert("123. KBps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Bps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Tibps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Tbps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Gibps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Gbps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Mibps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Mbps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Kibps") instanceof Long);
        assertTrue(testSubject.convert("0.9 Kbps") instanceof Long);
        assertTrue(testSubject.convert("0.9 bps") instanceof Long);

        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5z.Mbps"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("a5 MBps"));
        assertThrows(IllegalArgumentException.class, () -> testSubject.convert("5 bps5"));
    }

    @Test
    void testIsValueTypeValid() {
        ToscaType testSubject = ToscaType.SCALAR_UNIT_BITRATE;
        assertTrue(testSubject.isValueTypeValid(""));

        testSubject = ToscaType.LIST;
        assertTrue(testSubject.isValueTypeValid(""));

        testSubject = ToscaType.INTEGER;
        assertTrue(testSubject.isValueTypeValid(1));

        testSubject = ToscaType.FLOAT;
        assertTrue(testSubject.isValueTypeValid(2.3f));

        testSubject = ToscaType.BOOLEAN;
        assertTrue(testSubject.isValueTypeValid(true));

        testSubject = ToscaType.SCALAR_UNIT;
        assertFalse(testSubject.isValueTypeValid(""));

    }

}
