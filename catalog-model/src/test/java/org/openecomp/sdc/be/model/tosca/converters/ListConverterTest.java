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

import fj.data.Either;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.DataTypeDefinition;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ListConverterTest {

	private ListConverter listConverter = ListConverter.getInstance();
	private Map<String, DataTypeDefinition> dataTypesMap = new HashMap<>();

	@Test
	public void testConvertWithErrorResult() throws Exception {
		Either<String, Boolean> result = listConverter.convertWithErrorResult(null, null, dataTypesMap);
		assertNull(result.left().value());

		result = listConverter.convertWithErrorResult("[]", "error", dataTypesMap);
		assertTrue(result.isRight());

		result = listConverter.convertWithErrorResult("[\"test\":1]", "json", dataTypesMap);
		assertTrue(result.isRight());

		result = listConverter.convertWithErrorResult("[\"\"]", "string", dataTypesMap);
		assertEquals("[]", result.left().value());
		result = listConverter.convertWithErrorResult("[\"test\"]", "string", dataTypesMap);
		assertEquals("[\"test\"]", result.left().value());

		result = listConverter.convertWithErrorResult("[1, 0x01, 0o01]", "integer", dataTypesMap);
		assertEquals("[1,1,1]", result.left().value());

		result = listConverter.convertWithErrorResult("[0.1]", "float", dataTypesMap);
		assertEquals("[0.1]", result.left().value());

		result = listConverter.convertWithErrorResult("[true]", "boolean", dataTypesMap);
		assertEquals("[true]", result.left().value());

		result = listConverter.convertWithErrorResult("[{\"test\":1}]", "json", dataTypesMap);
		assertEquals("[{\"test\":1}]", result.left().value());

		result = listConverter.convertWithErrorResult("[1.1]", "version", dataTypesMap);
		assertEquals("[1.1]", result.left().value());
	}

	@Test
	public void testConvert() throws Exception {
		assertEquals("[0.1]", listConverter.convertWithErrorResult("[0.1]", "float", dataTypesMap).left().value());
		assertNull(listConverter.convert("[]", "error", dataTypesMap));
	}
}
