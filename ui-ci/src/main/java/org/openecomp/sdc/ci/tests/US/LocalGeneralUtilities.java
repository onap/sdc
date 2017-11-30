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

package org.openecomp.sdc.ci.tests.US;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.AmdocsLicenseMembers;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;

import com.clearspring.analytics.util.Pair;

public class LocalGeneralUtilities {

	public LocalGeneralUtilities() {
	}
	public static final String FILE_PATH = FileHandling.getBasePath() + "\\src\\main\\resources\\Files\\VNFs\\";
	public static final String Env_FILE_PATH = FileHandling.getBasePath() + "\\src\\main\\resources\\Files\\Env_files\\";
	public static String downloadPath = "C:\\Users\\th0695\\Downloads";

public static String getValueFromJsonResponse(String response, String fieldName) {
	try {
		JSONObject jsonResp = (JSONObject) JSONValue.parse(response);
		Object fieldValue = jsonResp.get(fieldName);
		return fieldValue.toString();

	} catch (Exception e) {
		return null;
	}

}

public static List<String> getValuesFromJsonArray(RestResponse message) throws Exception {
	List<String> artifactTypesArrayFromApi = new ArrayList<String>();

	org.json.JSONObject responseObject = new org.json.JSONObject(message.getResponse());
	JSONArray jArr = responseObject.getJSONArray("componentInstances");

	for (int i = 0; i < jArr.length(); i++) {
		org.json.JSONObject jObj = jArr.getJSONObject(i);
		String value = jObj.get("uniqueId").toString();

		artifactTypesArrayFromApi.add(value);
	}
	return artifactTypesArrayFromApi;
}

public static String simpleOnBoarding(ResourceReqDetails resourceReqDetails, String fileName, String filePath,User user) throws Exception {
	AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUiUtils.createVendorLicense(user);
	Pair<String, Map<String, String>> createVendorSoftwareProduct = OnboardingUtils.createVendorSoftwareProduct(resourceReqDetails, fileName, filePath, user, amdocsLicenseMembers);
	String vspName = createVendorSoftwareProduct.left;
	HomePage.showVspRepository();
	OnboardingUiUtils.importVSP(createVendorSoftwareProduct);
	GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue()).click();
	GeneralUIUtils.waitForLoader();
	return vspName;
}

//check if file downloaded successfully.
public static boolean isFileDownloaded(String downloadPath, String fileName) {
	boolean flag = false;
	File dir = new File(downloadPath);
	File[] dir_contents = dir.listFiles();
	for (int i = 0; i < dir_contents.length; i++) {
		if (dir_contents[i].getName().equals(fileName))
			return flag = true;
	}
	return flag;
}
}
