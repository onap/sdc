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

package org.openecomp.sdc.ci.tests.execute.TODO;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.DbUtils.TitanState;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import fj.data.Either;

public class ImportCapabilityTypeCITest {
	public static final DbUtils DbUtils = new DbUtils();

	@AfterClass
	public static void afterClass() {
		DbUtils.shutDowntitan();
	}

	static Config config = Config.instance();

	// private final String IMPORT_CAPABILITY_TYPES_PATH =
	// "src/test/resources/CI/importResourceTests/import_capabilitiesTypes/";

	@Test
	public void testAddingCapabilityTypes() throws IOException {
		TitanState originalState = DbUtils.getCurrentTitanState();

		String importResourceDir = config.getImportResourceConfigDir();

		String capabilityTypes = importResourceDir + File.separator + "capabilityTypesCi.zip";
		// importCapabilityType("src/test/resources/CI/importResource/capabilityTypesCi.zip");
		importCapabilityType(capabilityTypes);
		Either<Vertex, Boolean> eitherVertex = DbUtils.getVertexByUId("tosca.capabilities.Test.Ci");
		AssertJUnit.assertTrue(eitherVertex.isLeft());
		DbUtils.restoreToTitanState(originalState);
		eitherVertex = DbUtils.getVertexByUId("tosca.capabilities.Test.Ci");
		AssertJUnit.assertTrue(eitherVertex.isRight());
	}

	@Test
	public void AddingCapabilityNotFound() throws IOException {
		TitanState originalState = DbUtils.getCurrentTitanState();
		String importResourceTestsDir = config.getImportResourceTestsConfigDir();
		String capabilitiesTests = importResourceTestsDir + File.separator + "capabilityTypesCi.zip";
		importCapabilityType(capabilitiesTests);
		Either<Vertex, Boolean> eitherVertex = DbUtils.getVertexByUId("tosca.capabilities.NonExsitingCapability");
		AssertJUnit.assertTrue(eitherVertex.isRight());
		DbUtils.restoreToTitanState(originalState);
	}

	public static Integer importAllCapabilityTypes() throws IOException {

		String importResourceDir = config.getImportResourceConfigDir() + File.separator + "capabilityTypes.zip";
		// return
		// importCapabilityType("src/test/resources/CI/importResource/capabilityTypes.zip");
		return importCapabilityType(importResourceDir);
	}

	private static Integer importCapabilityType(String filePath) throws IOException {
		Config config = Utils.getConfig();
		CloseableHttpResponse response = null;
		MultipartEntityBuilder mpBuilder = MultipartEntityBuilder.create();

		mpBuilder.addPart("capabilityTypeZip", new FileBody(new File(filePath)));

		String url = String.format(Urls.IMPORT_CAPABILITY_TYPE, config.getCatalogBeHost(), config.getCatalogBePort());

		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("USER_ID", "jh0003");
			httpPost.setEntity(mpBuilder.build());
			response = client.execute(httpPost);
			return response.getStatusLine().getStatusCode();
		} finally {
			closeResponse(response);
			closeHttpClient(client);

		}
	}

	private static void closeHttpClient(CloseableHttpClient client) {
		try {
			if (client != null) {
				client.close();
			}
		} catch (IOException e) {
			System.out.println("failed to close client or response: " + e.getMessage());
		}
	}

	private static void closeResponse(CloseableHttpResponse response) {
		try {
			if (response != null) {
				response.close();
			}
		} catch (IOException e) {
			System.out.println("failed to close client or response: " + e.getMessage());
		}
	}

}
