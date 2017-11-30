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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;

public class CatalogRestUtils extends BaseRestUtils {

	public static RestResponse getAbstractResources() throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_ALL_ABSTRACT_RESOURCES, config.getCatalogBeHost(),
				config.getCatalogBePort());

		return sendGet(url, UserRoleEnum.DESIGNER.getUserId());
	}

	public static RestResponse getCatalog() throws IOException {
		return getCatalog(UserRoleEnum.DESIGNER.getUserId());
	}

	public static RestResponse getCatalog(String userId) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_CATALOG_DATA, config.getCatalogBeHost(), config.getCatalogBePort());
		return sendGet(url, userId);
	}

	public static RestResponse getCatalog(String userId, List<String> excludeList) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_CATALOG_DATA, config.getCatalogBeHost(), config.getCatalogBePort());
		StringBuilder sb = new StringBuilder();
		sb.append(url).append("?");
		Optional.ofNullable(excludeList).orElse(Collections.emptyList()).forEach(type -> sb.append("excludeTypes="+type+"&"));
		return sendGet(sb.toString(), userId);
	}

	public static RestResponse getAllCategoriesTowardsCatalogBe() throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_ALL_CATEGORIES, config.getCatalogBeHost(), config.getCatalogBePort(),
				BaseRestUtils.RESOURCE_COMPONENT_TYPE);

		return sendGet(url, UserRoleEnum.DESIGNER.getUserId());
	}

	public static RestResponse getAllCategoriesTowardsCatalogFeWithUuid(String uuid) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_ALL_CATEGORIES_FE, config.getCatalogFeHost(), config.getCatalogFePort(),
				BaseRestUtils.RESOURCE_COMPONENT_TYPE);

		Map<String, String> additionalHeaders = new HashMap<String, String>();
		additionalHeaders.put(HttpHeaderEnum.X_ECOMP_REQUEST_ID_HEADER.getValue(), uuid);

		return sendGet(url, UserRoleEnum.DESIGNER.getUserId(), additionalHeaders);
	}
}
