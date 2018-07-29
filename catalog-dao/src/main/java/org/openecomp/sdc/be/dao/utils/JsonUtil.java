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
 */

package org.openecomp.sdc.be.dao.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple utility for JSon processing.
 */
public final class JsonUtil {
	private static ObjectMapper getOneObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		return mapper;
	}

	private JsonUtil() {
	    // No instances allowed
	}

	/**
	 * Deserialize json text to object
	 *
	 * @param objectText
	 * @param objectClass
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T readObject(String objectText, Class<T> objectClass) throws IOException {
		return getOneObjectMapper().readValue(objectText, objectClass);
	}

	/**
	 * Deserialize json stream to object
	 *
	 * @param jsonStream
	 * @param objectClass
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T readObject(InputStream jsonStream, Class<T> objectClass) throws IOException {
		return getOneObjectMapper().readValue(jsonStream, objectClass);
	}

	/**
	 * Deserialize json text to object
	 *
	 * @param objectText
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T readObject(String objectText) throws IOException {
		TypeReference<T> typeRef = new TypeReference<T>() {
		};
		return getOneObjectMapper().readValue(objectText, typeRef);
	}

	/**
	 * Deserialize the given json string to a map
	 *
	 * @param json
	 *            json text
	 * @return map object
	 * @throws IOException
	 */
	public static Map<String, Object> toMap(String json) throws IOException {
		ObjectMapper mapper = getOneObjectMapper();
		JavaType mapStringObjectType = mapper.getTypeFactory().constructParametricType(HashMap.class, String.class,
				Object.class);
		return mapper.readValue(json, mapStringObjectType);
	}

	/**
	 * Deserialize the given json string to a map
	 *
	 * @param json
	 * @param keyTypeClass
	 * @param valueTypeClass
	 * @return
	 * @throws IOException
	 */
	public static <K, V> Map<K, V> toMap(String json, Class<K> keyTypeClass, Class<V> valueTypeClass)
			throws IOException {
		ObjectMapper mapper = getOneObjectMapper();
		JavaType mapStringObjectType = mapper.getTypeFactory().constructParametricType(HashMap.class, keyTypeClass,
				valueTypeClass);
		return mapper.readValue(json, mapStringObjectType);
	}

	public static <V> V[] toArray(String json, Class<V> valueTypeClass) throws IOException {
		ObjectMapper mapper = getOneObjectMapper();
		JavaType arrayStringObjectType = mapper.getTypeFactory().constructArrayType(valueTypeClass);
		return mapper.readValue(json, arrayStringObjectType);
	}

	/**
	 * Deserialize the given json string to a list
	 *
	 * @param json
	 *            json text
	 * @return list object
	 * @throws IOException
	 */
	public static <T> List<T> toList(String json, Class<T> clazz) throws IOException {
		ObjectMapper mapper = getOneObjectMapper();
		JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
		return mapper.readValue(json, type);
	}

	public static <T> List<T> toList(String json, Class<T> elementClass, Class<?> elementGenericClass)
			throws IOException {
		ObjectMapper mapper = getOneObjectMapper();
		JavaType elementType = mapper.getTypeFactory().constructParametricType(elementClass, elementGenericClass);
		JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
		return mapper.readValue(json, listType);
	}
}
