package org.openecomp.sdc.be.dao.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.utils.vint.EncodedDataInputStream;
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