package org.openecomp.sdc.be.dao.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockit.Deencapsulation;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
}