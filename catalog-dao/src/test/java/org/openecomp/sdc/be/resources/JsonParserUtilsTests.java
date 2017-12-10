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

package org.openecomp.sdc.be.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

public class JsonParserUtilsTests {
	@Test
	public void testProperties() throws JsonGenerationException, JsonMappingException, IOException {

		PropertyDataDefinition dataDefinition = new PropertyDataDefinition();
		dataDefinition.setName("myName");
		dataDefinition.setDescription("description");
		dataDefinition.setDefaultValue("default11");
		SchemaDefinition entrySchema = new SchemaDefinition();
		PropertyDataDefinition property = new PropertyDataDefinition();
		property.setName("name12");
		property.setType("string");
		entrySchema.setProperty(property);
		Map<String, PropertyDataDefinition> properties = new HashMap<>();
		properties.put("key1", property);
		properties.put("key2", property);
		
		entrySchema.setProperties(properties );
		dataDefinition.setSchema(entrySchema);
		
		Map<String, PropertyDataDefinition> map = new HashMap<>();
		map.put("prop", dataDefinition);
		
//		String jsonstring = JsonParserUtils.jsonToString(map);
		
//		Map<String, PropertyDataDefinition> parseToJson = JsonParserUtils.parseToJson(jsonstring, PropertyDataDefinition.class);
//		Map<String, PropertyDataDefinition> parseToJson;
//		TypeReference<Map<String, PropertyDataDefinition>> typeRef = new TypeReference<Map<String, PropertyDataDefinition>>() {
//		};
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			parseToJson = mapper.readValue(jsonstring, typeRef);
//		} catch (Exception e) {
////			logger.debug("Failed to parse json {}", json, e);
//		}
		
//		System.out.println(parseToJson);
		
	}

}
