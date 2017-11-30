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

package org.openecomp.sdc.ci.tests.utils.general;

import java.io.IOException;
import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;

public class ImportUtils {

	public static ImportReqDetails getImportResourceDetailsByPathAndName(ImportReqDetails importReqDetails,
			String filePath, String fileName) throws IOException {

		// ImportReqDetails importReqDetails;
		// User sdncUserDetails;
		// String testResourcesPath;
		// ResourceReqDetails resourceDetails;
		// Config config;
		// config = Utils.getConfig();
		//
		// importReqDetails = ElementFactory.getDefaultImportResource();
		// User sdncUserDetails =
		// ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		// ResourceReqDetails resourceDetails =
		// ElementFactory.getDefaultResource();
		// String sourceDir = config.getResourceConfigDir();
		// String testResourcesPath = sourceDir + File.separator + workDir;
		// final String workDir = "importToscaResourceByCreateUrl";

		List<String> listFileName = FileUtils.getFileListFromBaseDirectoryByTestName(filePath);
		importReqDetails.setPayloadName(fileName);
		String payloadData = FileUtils.loadPayloadFile(listFileName, fileName, true);
		importReqDetails.setPayloadData(payloadData);

		return importReqDetails;
	}

}
