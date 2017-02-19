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

package org.openecomp.sdc.be.model.operations;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.UploadArtifactInfo;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonObjectTest {

	private ObjectMapper mapper;
	UploadResourceInfo inputObjectRef;
	private final String INPUT_RESOURCE_STRING = "{  \"payloadData\" : \"My Test Object\",  \"payloadName\" : \"TestName\", "
			+ "  \"description\":\"my_description\",\"tags\":[\"tag1\"], "
			+ "\"artifactList\" : [ {    \"artifactName\" : \"myArtifact0\",  \"artifactPath\" : \"scripts/\",  \"artifactType\" : \"PUPPET\",   "
			+ " \"artifactDescription\" : \"This is Description\",    \"artifactData\" : null  }, "
			+ "{    \"artifactName\" : \"myArtifact1\",  \"artifactPath\" : \"scripts/\", \"artifactType\" : \"PUPPET\",    \"artifactDescription\" : \"This is Description\", "
			+ "   \"artifactData\" : null  } ], \"contactId\" : null, \"name\" : null, \"resourceIconPath\" : null, \"vendorName\" : null, \"vendorRelease\" : null , \"resourceType\" : \"VFC\" }";

	@Before
	public void setup() {
		mapper = new ObjectMapper();
		ArrayList<UploadArtifactInfo> artifactList = new ArrayList<UploadArtifactInfo>();
		for (int i = 0; i < 2; i++) {
			UploadArtifactInfo artifactInfo = new UploadArtifactInfo("myArtifact" + i, "scripts/",
					ArtifactTypeEnum.PUPPET, "This is Description");
			artifactList.add(artifactInfo);
		}
		ArrayList<String> tags = new ArrayList<>();
		tags.add("tag1");
		inputObjectRef = new UploadResourceInfo("My Test Object", "TestName", "my_description", null, tags,
				artifactList);

	}

	@Test
	public void testStringToUploadResourceInfo() throws JsonParseException, JsonMappingException, IOException {
		UploadResourceInfo resourceObjectTest = mapper.readValue(INPUT_RESOURCE_STRING, UploadResourceInfo.class);
		assertEquals(inputObjectRef, resourceObjectTest);

	}

	// @Test
	public void testUploadResourceInfoToString() throws JsonParseException, JsonMappingException, IOException {
		String refAsString = mapper.writeValueAsString(inputObjectRef);
		String unFormattedString = refAsString.replace("\n", "").replace("\t", "").replace(" ", "");

		assertEquals(unFormattedString, INPUT_RESOURCE_STRING.replace("\n", "").replace("\t", "").replace(" ", ""));

	}
}
