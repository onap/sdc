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

package org.openecomp.sdc.ci.tests.execute.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.ci.tests.utils.Decoder;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class ProductToscaYamlGenerationTest extends ProductTestBase {

	@Rule
	public static TestName name = new TestName();

	public ProductToscaYamlGenerationTest() {
		super(name, ProductToscaYamlGenerationTest.class.getName());
	}

	@Test
	public void productToscaYamlFormat() throws IOException {

		String heatEnvArtifactHeader = (String) Utils.parseYamlConfig("heatEnvArtifactHeader");
		// System.out.println("heatEnvArtifactHeader = \n" +
		// heatEnvArtifactHeader);

		String heatEnvArtifactFooter = (String) Utils.parseYamlConfig("heatEnvArtifactFooter");
		// System.out.println("heatEnvArtifactFooter = \n" +
		// heatEnvArtifactFooter);

		// temporary commented
		// RestResponse downloadResourceInstanceHeatArtifact =
		// ArtifactRestUtils.downloadResourceInstanceArtifact(service.getUniqueId(),
		// resourceInstanceId, sdncUserDetails,
		// heatArtifactDefinition.getUniqueId());
		// assertTrue("expected request returned status:" + 200 + ", actual: " +
		// downloadResourceInstanceHeatArtifact.getErrorCode(),
		// downloadResourceInstanceHeatArtifact.getErrorCode()==200);

		// InputStream stream =
		// getDecodedSteramFromString(downloadResourceInstanceHeatArtifact.getResponse());
		// System.out.println(Utils.getParamValueFromYamlByKey(stream,
		// "description", String.class));

		// node_types

	}

	private InputStream getDecodedSteramFromString(String encoded64Payload) throws IOException {

		Gson gson = new Gson();
		ArtifactUiDownloadData artifactUiDownloadData = gson.fromJson(encoded64Payload, ArtifactUiDownloadData.class);
		String decodedPayload = Decoder.decode(artifactUiDownloadData.getBase64Contents());
		return new ByteArrayInputStream(decodedPayload.getBytes());

	}

}
