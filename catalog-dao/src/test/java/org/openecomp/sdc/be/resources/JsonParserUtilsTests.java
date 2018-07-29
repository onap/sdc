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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.openecomp.sdc.be.utils.FixtureHelpers.fixture;
import static org.openecomp.sdc.be.utils.JsonTester.testJsonMap;

public class JsonParserUtilsTests {

	private static final String FIXTURE_PATH = "fixtures/ListCapabilityDataDefinition.json";

	@Test
	public void testToMap() {
		String json = fixture(FIXTURE_PATH);
		Map<String, ListCapabilityDataDefinition> actual = JsonParserUtils.toMap(json,
				ListCapabilityDataDefinition.class);
		Map<String, ListCapabilityDataDefinition> expected = buildMap();
		assertThat(actual).isEqualToComparingFieldByFieldRecursively(expected);
	}

	@Test
	public void testJacksonFasterXml() {
		ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		assertThatCode(() -> testJsonMap(buildMap(), ListCapabilityDataDefinition.class, FIXTURE_PATH, mapper))
				.doesNotThrowAnyException();
	}

	@Test
	public void testToJson() {
		try {
			JsonParserUtils.toJson(new Object());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testMap() {
		JsonParserUtils.toMap("{}");
		JsonParserUtils.toMap("");
		JsonParserUtils.toMap("****");
		
		JsonParserUtils.toMap("{}", ToscaDataDefinition.class);
		JsonParserUtils.toMap("", ToscaDataDefinition.class);
		JsonParserUtils.toMap("****", ToscaDataDefinition.class);
		
	}

	private Map<String, ListCapabilityDataDefinition> buildMap() {
		Map<String, ListCapabilityDataDefinition> map = new HashMap<>();
		map.put("org.openecomp.capabilities.Forwarder", buildListCapabilityDataDefinition());
		return map;
	}

	private ListCapabilityDataDefinition buildListCapabilityDataDefinition() {
		CapabilityDataDefinition dataDefinition = new CapabilityDataDefinition();
		dataDefinition.setName("forwarder");
		dataDefinition.setType("org.openecomp.capabilities.Forwarder");
		dataDefinition.setUniqueId("capability.deb142fd-95eb-48f7-99ae-81ab09466b1e.forwarder");
		dataDefinition.setOwnerId("deb142fd-95eb-48f7-99ae-81ab09466b1e");
		dataDefinition.setMinOccurrences("1");
		dataDefinition.setLeftOccurrences("UNBOUNDED");
		dataDefinition.setMaxOccurrences("UNBOUNDED");
		dataDefinition.setCapabilitySources(buildCapabilitySources());

		return new ListCapabilityDataDefinition(ImmutableList.of(dataDefinition));
	}

	private List<String> buildCapabilitySources() {
		return ImmutableList.of("org.openecomp.resource.cp.nodes.network.Port", "org.openecomp.resource.cp.v2.extCP",
				"org.openecomp.resource.cp.v2.extContrailCP");
	}
}
