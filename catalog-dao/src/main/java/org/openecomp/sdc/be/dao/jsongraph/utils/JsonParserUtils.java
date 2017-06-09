package org.openecomp.sdc.be.dao.jsongraph.utils;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonParserUtils {
	private static ObjectMapper mapper = new ObjectMapper();
	private static Logger logger = LoggerFactory.getLogger(JsonParserUtils.class.getName());

	
	public static <T> String jsonToString(T elementToRepresent) throws IOException, JsonGenerationException, JsonMappingException {

		
		mapper.configure(Feature.FAIL_ON_EMPTY_BEANS, false);
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		return mapper.writeValueAsString(elementToRepresent);
	}

	public static Map<String, Object> parseToJson(String json) {
		Map<String, Object> object = null;
		if (json == null || json.isEmpty()) {
			return null;
		}
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(Feature.FAIL_ON_EMPTY_BEANS, false);
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
		};

		try {
			object = mapper.readValue(json, typeRef);
		} catch (Exception e) {
			logger.debug("Failed to parse json {}", json, e);
		}
		return object;
	}
	public static <T extends ToscaDataDefinition> Map<String, T> parseToJson(String json, Class <T> clazz) {
		Map<String, T> object = null;
		if (json == null || json.isEmpty()) {
			return null;
		}
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(Feature.FAIL_ON_EMPTY_BEANS, false);
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		JavaType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, clazz);

		try {
			object = mapper.readValue(json, type);
		} catch (Exception e) {
			logger.debug("Failed to parse json {}", json, e);
		}
		return object;
	}
}
