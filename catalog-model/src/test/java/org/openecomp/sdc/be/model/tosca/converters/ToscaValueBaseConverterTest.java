/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.JsonPrimitive;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;


public class ToscaValueBaseConverterTest {

	private ToscaValueBaseConverter converter = new ToscaValueBaseConverter();

	@Test
	public void testJson2JavaPrimitive() throws Exception {
		JsonPrimitive prim1 = new JsonPrimitive(Boolean.FALSE);
		Object res1 = converter.json2JavaPrimitive(prim1);
		Assert.assertFalse((Boolean)res1);

		JsonPrimitive prim2 = new JsonPrimitive("Test");
		Object res2 = converter.json2JavaPrimitive(prim2);
		Assert.assertTrue(res2.equals("Test"));

		JsonPrimitive prim3 = new JsonPrimitive(3);
		Object res3 = converter.json2JavaPrimitive(prim3);
		Assert.assertTrue((Integer)res3 == 3);

		JsonPrimitive prim4 = new JsonPrimitive(3.6);
		Object res4 = converter.json2JavaPrimitive(prim4);
		Assert.assertTrue((Double)res4 == 3.6);
	}

	@Test
	public void testIsEmptyObjectValue() throws Exception {
		boolean res1 = ToscaValueBaseConverter.isEmptyObjectValue(null);
		Assert.assertTrue(res1);

		boolean res2 = ToscaValueBaseConverter.isEmptyObjectValue("");
		Assert.assertTrue(res2);

		boolean res3 = ToscaValueBaseConverter.isEmptyObjectValue(new HashMap<>());
		Assert.assertTrue(res3);

		boolean res4 = ToscaValueBaseConverter.isEmptyObjectValue(new LinkedList<>());
		Assert.assertTrue(res4);

		boolean res5 = ToscaValueBaseConverter.isEmptyObjectValue("test");
		Assert.assertFalse(res5);
	}
}
