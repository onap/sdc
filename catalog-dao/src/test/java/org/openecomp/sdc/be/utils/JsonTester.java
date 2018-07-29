package org.openecomp.sdc.be.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openecomp.sdc.be.utils.FixtureHelpers.fixture;

public class JsonTester {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public JsonTester() {

    }

    public static <T> void testJson(T object, String fixturePath) throws Exception {
        testJson(object, fixturePath, MAPPER);
    }

    @SuppressWarnings("unchecked")
    public static <T> void testJson(T object, String fixturePath, ObjectMapper mapper) throws Exception {
        T expectedObject = (T) mapper.readValue(fixture(fixturePath), object.getClass());
        String expectedJson = mapper.writeValueAsString(expectedObject);
        String actualJson = mapper.writeValueAsString(object);

        assertThat(actualJson).isEqualTo(expectedJson);
    }

    @SuppressWarnings("unchecked")
    public static <T> void testJsonMap(Map<String, T> map, Class<T> valueClass, String fixturePath, ObjectMapper mapper) throws Exception {
        MapType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, valueClass);
        Map<String, T> expectedObject = mapper.readValue(fixture(fixturePath), mapType);
        String expectedJson = mapper.writeValueAsString(expectedObject);

        String actualJson = mapper.writeValueAsString(map);

        assertThat(actualJson).isEqualTo(expectedJson);
    }
}