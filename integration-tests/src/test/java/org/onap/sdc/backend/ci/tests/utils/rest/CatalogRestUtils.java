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

package org.onap.sdc.backend.ci.tests.utils.rest;

import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpHeaderEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.api.Urls;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

	public static RestResponse getCatalogDataType(String userId) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_CATALOG_DATATYPE, config.getCatalogBeHost(), config.getCatalogBePort());
		return sendGet(url, userId);
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

		Map<String, String> additionalHeaders = new HashMap<>();
		additionalHeaders.put(HttpHeaderEnum.X_ECOMP_REQUEST_ID_HEADER.getValue(), uuid);

		return sendGet(url, UserRoleEnum.DESIGNER.getUserId(), additionalHeaders);
	}
	
	public static RestResponse getOnboardVersion() throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.ONBOARD_VERSION, config.getOnboardingBeHost(),
				config.getOnboardingBePort());

		return sendGet(url, UserRoleEnum.DESIGNER.getUserId());
	}
	
	public static RestResponse getOsVersion() throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.OS_VERSION, config.getCatalogBeHost(), config.getCatalogBePort());

		return sendGet(url, UserRoleEnum.DESIGNER.getUserId());
	}
}
