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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.fe.listen;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.Serializable;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MyObjectMapperProviderTest {

	private class AnyModel implements Serializable {
		private String field1;

		AnyModel(String field1) {
			this.field1 = field1;
		}

		public String getField1() {
			return field1;
		}
	}

	@Test
	public void shouldSerializeItPretty() throws JsonProcessingException {
		String prettyJson = "{\n"
			+ "  \"field1\" : \"Field1\"\n"
			+ "}";

		ObjectMapper objectMapper = new MyObjectMapperProvider().getContext(MyObjectMapperProviderTest.class);
		String serialized = objectMapper.writeValueAsString(new AnyModel("Field1")).replace("\r","");
		Assert.assertEquals(serialized, prettyJson);
	}
}
