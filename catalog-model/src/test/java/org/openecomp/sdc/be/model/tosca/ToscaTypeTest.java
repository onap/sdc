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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.tosca.version.Version;


public class ToscaTypeTest {

	@Test
	public void testIsValidValueBoolean() throws Exception {
		ToscaType toscaType = ToscaType.BOOLEAN;;

		assertTrue(!toscaType.isValidValue(""));
		assertTrue(toscaType.isValidValue("false"));
		assertTrue(toscaType.isValidValue("FalSe"));
		assertTrue(toscaType.isValidValue("true"));
		assertTrue(toscaType.isValidValue("TrUe"));
	}

	@Test
	public void testIsValidValueFloat() throws Exception {
		ToscaType toscaType = ToscaType.FLOAT;;

		assertTrue(!toscaType.isValidValue("float"));
		assertTrue(toscaType.isValidValue("1.2534"));
		assertTrue(toscaType.isValidValue("1.2534f"));
	}

	@Test
	public void testIsValidValueInteger() throws Exception {
		ToscaType toscaType = ToscaType.INTEGER;;

		assertTrue(!toscaType.isValidValue("integer"));
		assertTrue(toscaType.isValidValue("1235"));
	}

	@Test
	public void testIsValidValueTimestamp() throws Exception {
		ToscaType toscaType = ToscaType.TIMESTAMP;;

		assertTrue(!toscaType.isValidValue("timestamp"));
		assertTrue(toscaType.isValidValue("2001-12-14t21:59:43.10-05:00"));
		assertTrue(!toscaType.isValidValue("30 juin 2009 07:03:47"));
	}

	@Test
	public void testIsValidValueVersion() throws Exception {
		ToscaType toscaType = ToscaType.VERSION;;

		assertTrue(!toscaType.isValidValue("version"));
		assertTrue(toscaType.isValidValue("1.2"));
		assertTrue(toscaType.isValidValue("1.2.3"));
		assertTrue(toscaType.isValidValue("1.2-3"));
	}

	@Test
	public void testIsValidValueList() throws Exception {
		ToscaType toscaType = ToscaType.LIST;;

		assertTrue(!toscaType.isValidValue("list"));
		assertTrue(toscaType.isValidValue("[\"color\",\"type\"]"));
	}

	@Test
	public void testIsValidValueMap() throws Exception {
		ToscaType toscaType = ToscaType.MAP;;

		assertTrue(!toscaType.isValidValue("map"));
		assertTrue(toscaType.isValidValue("{\"color\":\"yellow\",\"type\":\"renault\"}"));
	}

	@Test
	public void testGetToscaType() throws Exception {
		ToscaType toscaType = ToscaType.MAP;;

		assertEquals(ToscaType.getToscaType("map"), toscaType);
		assertNull(ToscaType.getToscaType(null));
		assertNull(ToscaType.getToscaType("InvalidType"));
	}

	@Test
	public void testIsPrimitiveType() throws Exception {
		assertTrue(!ToscaType.isPrimitiveType("map"));
		assertTrue(!ToscaType.isPrimitiveType("list"));
		assertTrue(!ToscaType.isPrimitiveType("String"));
		assertTrue(ToscaType.isPrimitiveType("string"));
		assertTrue(ToscaType.isPrimitiveType("integer"));
	}

	@Test
	public void testIsCollectionType() throws Exception {
		assertTrue(ToscaType.isCollectionType("map"));
		assertTrue(ToscaType.isCollectionType("list"));
		assertTrue(!ToscaType.isCollectionType("Map"));
		assertTrue(!ToscaType.isCollectionType("string"));
		assertTrue(!ToscaType.isCollectionType("integer"));
	}

	@Test
	public void testConvert() throws Exception {
		ToscaType typeStr = ToscaType.STRING;
		assertEquals(typeStr.convert("str"), "str");

		ToscaType typeFloat = ToscaType.FLOAT;
		assertTrue(typeFloat.convert("1.2357f") instanceof Float);

		ToscaType typeTimestamp = ToscaType.TIMESTAMP;
		assertTrue(typeTimestamp.convert("Jun 30, 2009 7:03:47 AM") instanceof Date);

		ToscaType typeVersion = ToscaType.VERSION;
		assertTrue(typeVersion.convert("1.2.3.5.6") instanceof Version);

		ToscaType typeList = ToscaType.LIST;
		assertTrue(typeList.convert("[\"str1\",\"str2\"]") instanceof List);

		ToscaType typeMap = ToscaType.MAP;
		assertTrue(typeMap.convert("{\"color\":\"yellow\",\"type\":\"renault\"}") instanceof Map);
	}

	@Test
	public void testToString() throws Exception {
		ToscaType testToscaType = ToscaType.SCALAR_UNIT;
		assertEquals(testToscaType.toString(), "scalar_unit");
	}
}
