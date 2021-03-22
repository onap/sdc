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

package org.openecomp.sdc.be.model.tosca.converters;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MapConverterTest {

	@Test
	public void testGetInstance() throws Exception {
		MapConverter result;

		// default test
		result = MapConverter.getInstance();
	}

	@Test
	public void testConvert() throws Exception {
		MapConverter testSubject = new MapConverter();
		Map<String, DataTypeDefinition> dataTypes = null;
		assertTrue(testSubject.convert("", null, dataTypes).isEmpty());
		assertTrue(testSubject.convert("", "string", dataTypes).isEmpty());
		assertNull(testSubject.convert("{\"key\":}", "integer", dataTypes));

		assertEquals("{\"key\":\"value\"}", testSubject.convert("{\"key\":\"value\"}", "list", dataTypes));
		assertEquals("{\"key\":\"value\"}", testSubject.convert("{\"key\":\"value\"}", "string", dataTypes));
		assertEquals("{\"key\":2}", testSubject.convert("{\"key\":2}", "integer", dataTypes));
		assertEquals("{\"key\":null}", testSubject.convert("{\"key\":null}", "integer", dataTypes));
		assertEquals("{\"key\":0.2}", testSubject.convert("{\"key\":0.2}", "float", dataTypes));
		assertEquals("{\"key\":null}", testSubject.convert("{\"key\":null}", "float", dataTypes));
		assertEquals("{\"key\":true}", testSubject.convert("{\"key\":true}", "boolean", dataTypes));
		assertEquals("{\"key\":null}", testSubject.convert("{\"key\":null}", "boolean", dataTypes));
	}
}
