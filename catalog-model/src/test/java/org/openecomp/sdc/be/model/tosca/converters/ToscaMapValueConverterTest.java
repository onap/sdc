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

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class ToscaMapValueConverterTest {

	private ToscaMapValueConverter createTestSubject() {
		return ToscaMapValueConverter.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		ToscaMapValueConverter result;

		// default test
		result = ToscaMapValueConverter.getInstance();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldPreserveStringValuesWithLeadingZerosInMap() {
		ToscaMapValueConverter converter = createTestSubject();

		String value = "{\"id\": \"000123\", \"code\": \"01\"}";

		Object result = converter.convertToToscaValue(value, "string", new HashMap<>());

		Map<String, String> map = (Map<String, String>) result;

		assertEquals("000123", map.get("id"));
		assertEquals("01", map.get("code"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldConvertSimpleJsonMap() {
		ToscaMapValueConverter converter = createTestSubject();

		String value = "{\"key1\": 1, \"key2\": 2}";

		Object result = converter.convertToToscaValue(value, "integer", new HashMap<>());

		Map<String, Integer> map = (Map<String, Integer>) result;

		assertEquals(Integer.valueOf(1), map.get("key1"));
		assertEquals(Integer.valueOf(2), map.get("key2"));
	}

}
