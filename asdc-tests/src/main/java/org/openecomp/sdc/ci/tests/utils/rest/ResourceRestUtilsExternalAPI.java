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

package org.openecomp.sdc.ci.tests.utils.rest;

import java.util.Map;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ResourceExternalReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.common.util.GeneralUtility;

import com.google.gson.Gson;

public class ResourceRestUtilsExternalAPI extends BaseRestUtils {
	
	public static RestResponse createResource(ResourceExternalReqDetails resourceDetails, User sdncModifierDetails)
			throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.POST_EXTERNAL_API_CREATE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort());

		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(resourceDetails);
		String calculateMD5 = GeneralUtility.calculateMD5Base64EncodedByString(userBodyJson);
		headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), calculateMD5);
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		
		HttpRequest http = new HttpRequest();
		RestResponse createResourceResponse = http.httpSendPost(url, userBodyJson, headersMap);
		
		return createResourceResponse;
	}
	
	
}
