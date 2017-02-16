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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;

import com.google.gson.Gson;

public class GroupRestUtils extends BaseRestUtils {
	public static String acceptHeaderDate = "application/json";
	static Config config = Config.instance();
	public static Gson gson = new Gson();

	public static RestResponse getGroupById(Component component, String groupId, User user) throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), user.getUserId());

		String url = String.format(Urls.GET_GROUP_BY_ID, config.getCatalogBeHost(), config.getCatalogBePort(),
				ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUniqueId(), groupId);

		RestResponse sendGetServerRequest = sendGet(url, user.getUserId(), headersMap);

		return sendGetServerRequest;

	}

}
