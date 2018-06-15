/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2018 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.dao.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import mockit.Deencapsulation;

public class JsonUtilTest {
	
	@Test
	public void testGetOneObjectMapper() throws Exception {
		ObjectMapper result;

		// default test
		result = Deencapsulation.invoke(JsonUtil.class, "getOneObjectMapper");
	}

	@Test
	public void testReadObject() throws Exception {
		String objectText = "{}";
		Class objectClass = Object.class;
		Object result;

		// default test
		result = JsonUtil.readObject(objectText, objectClass);
	}

	@Ignore
	@Test
	public void testReadObject_1() throws Exception {
		InputStream jsonStream = Mockito.mock(InputStream.class);
		Class objectClass = Object.class;
		Object result;

		// default test
		result = JsonUtil.readObject(jsonStream, objectClass);
	}

	@Test
	public void testReadObject_2() throws Exception {
		String objectText = "{}";
		Object result;

		// default test
		result = JsonUtil.readObject(objectText);
	}

	@Test
	public void testToMap() throws Exception {
		String json = "{\"name\":\"mock\",\"age\":0}";
		Map<String, Object> result;

		// default test
		result = JsonUtil.toMap(json);
	}

	@Test
	public void testToMap_1() throws Exception {
		String json = "{\"name\":\"mock\",\"age\":0}";
		Class keyTypeClass = Object.class;
		Class valueTypeClass = Object.class;
		Map result;

		// default test
		result = JsonUtil.toMap(json, keyTypeClass, valueTypeClass);
	}

	@Test
	public void testToArray() throws Exception {
		String json = "[]";
		Class valueTypeClass = Object.class;
		Object[] result;

		// default test
		result = JsonUtil.toArray(json, valueTypeClass);
	}

	@Test
	public void testToList() throws Exception {
		String json = "[]";
		Class clazz = Object.class;
		List result;

		// default test
		result = JsonUtil.toList(json, clazz);
	}

	@Test
	public void testToList_1() throws Exception {
		String json = "[]";
		Class elementClass = List.class;;
		Class elementGenericClass = List.class;;
		List result;

		// default test
		result = JsonUtil.toList(json, elementClass, elementGenericClass);
	}
}