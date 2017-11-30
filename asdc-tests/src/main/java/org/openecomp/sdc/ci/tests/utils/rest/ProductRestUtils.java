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

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ProductRestUtils extends BaseRestUtils {
	private static Gson gson = new Gson();
	private static Logger logger = LoggerFactory.getLogger(ProductRestUtils.class.getName());

	public static RestResponse createProduct(ProductReqDetails product, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_PRODUCT, config.getCatalogBeHost(), config.getCatalogBePort());
		String serviceBodyJson = gson.toJson(product);

		logger.debug("Send POST request to create service: {}",url);
		logger.debug("Service body: {}",serviceBodyJson);

		RestResponse res = sendPost(url, serviceBodyJson, user.getUserId(), acceptHeaderData);
		if (res.getErrorCode() == STATUS_CODE_CREATED) {
			product.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
			product.setVersion(ResponseParser.getVersionFromResponse(res));
			product.setUUID(ResponseParser.getUuidFromResponse(res));
			// Creator details never change after component is created - Ella,
			// 12/1/2016
			product.setCreatorUserId(user.getUserId());
			product.setCreatorFullName(user.getFullName());
			product.setLastUpdaterFullName(user.getFullName());
			product.setLastUpdaterUserId(user.getUserId());
			product.setLastUpdaterFullName(user.getFullName());
			product.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
			product.setVersion("0.1");
			String lastUpdate = ResponseParser.getValueFromJsonResponse(res.getResponse(), "lastUpdateDate");
			product.setLastUpdateDate(Long.parseLong(lastUpdate, 10));
			product.setCreationDate(Long.parseLong(lastUpdate, 10));
		}
		return res;
	}

	public static RestResponse updateProduct(ProductReqDetails product, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_PRODUCT, config.getCatalogBeHost(), config.getCatalogBePort(),
				product.getUniqueId());
		String serviceBodyJson = gson.toJson(product);

		logger.debug("Send POST request to create service: {}",url);
		logger.debug("Service body: {}",serviceBodyJson);

		RestResponse res = sendPut(url, serviceBodyJson, user.getUserId(), acceptHeaderData);
		if (res.getErrorCode() == STATUS_CODE_CREATED) {
			product.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
			product.setVersion(ResponseParser.getVersionFromResponse(res));
			product.setUUID(ResponseParser.getUuidFromResponse(res));
			// Creator details never change after component is created - Ella,
			// 12/1/2016
			product.setCreatorUserId(user.getUserId());
			product.setCreatorFullName(user.getFullName());
			product.setLastUpdaterFullName(user.getFullName());
			product.setLastUpdaterUserId(user.getUserId());
			product.setLastUpdaterFullName(user.getFullName());
			product.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
			String valueFromJsonResponse = ResponseParser.getValueFromJsonResponse(res.getResponse(), "version");
			product.setVersion(valueFromJsonResponse);
			String lastUpdate = ResponseParser.getValueFromJsonResponse(res.getResponse(), "lastUpdateDate");
			product.setLastUpdateDate(Long.parseLong(lastUpdate, 10));
			product.setCreationDate(Long.parseLong(lastUpdate, 10));
		}
		return res;
	}

	public static RestResponse createProduct_Invalid_Json(String userId) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_PRODUCT, config.getCatalogBeHost(), config.getCatalogBePort());

		RestResponse res = sendPost(url, "kukumuku", userId, acceptHeaderData);
		return res;
	}

	public static RestResponse deleteProduct(String id, String userId) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_PRODUCT, config.getCatalogBeHost(), config.getCatalogBePort(), id);
		return sendDelete(url, userId);
	}

	public static RestResponse getProduct(String productId) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_PRODUCT, config.getCatalogBeHost(), config.getCatalogBePort(), productId);
		logger.debug("Send GET request to get product: {}",url);

		return sendGet(url, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
	}

	public static RestResponse getProduct(String productId, String userId) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_PRODUCT, config.getCatalogBeHost(), config.getCatalogBePort(), productId);
		logger.debug("Send GET request to get product: {}",url);

		return sendGet(url, userId);
	}

	public static RestResponse getFollowed(String userId) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_FOLLWED_LIST, config.getCatalogBeHost(), config.getCatalogBePort());
		logger.debug("Send GET request to get user followed page: {}",url);
		return sendGet(url, userId);

	}

	public static RestResponse changeProductLifeCycle(Product product, User userModifier, LifeCycleStatesEnum lifeCycle)
			throws Exception {
		String checkinComment = "my comment";
		RestResponse changeLifeCycleResponse = LifecycleRestUtils.changeProductState(product, userModifier, lifeCycle,
				checkinComment);
		if (changeLifeCycleResponse.getErrorCode() == STATUS_CODE_SUCCESS) {
			product.setLastUpdaterUserId(userModifier.getUserId());
			product.setLastUpdaterFullName(userModifier.getFullName());
			String latestVersion = ResponseParser.getValueFromJsonResponse(changeLifeCycleResponse.getResponse(),
					"version");
			product.setVersion(latestVersion);
			String lifecycleState = ResponseParser.getValueFromJsonResponse(changeLifeCycleResponse.getResponse(),
					"lifecycleState");
			product.setLifecycleState((LifecycleStateEnum.valueOf(lifecycleState)));
			String uniqueId = ResponseParser.getValueFromJsonResponse(changeLifeCycleResponse.getResponse(),
					"uniqueId");
			product.setUniqueId(uniqueId);
			String lastUpdate = ResponseParser.getValueFromJsonResponse(changeLifeCycleResponse.getResponse(),
					"lastUpdateDate");
			product.setLastUpdateDate((Long.parseLong(lastUpdate, 10)));
			String uuid = ResponseParser.getValueFromJsonResponse(changeLifeCycleResponse.getResponse(), "uuid");
			product.setUUID(uuid);
		}
		return changeLifeCycleResponse;
	}

	public static RestResponse changeServiceInstanceVersion(String componentUniqueId,
			String serviceInstanceToReplaceUniqueId, String serviceUniqueId, User sdncModifierDetails,
			ComponentTypeEnum componentType) throws IOException {
		Config config = Utils.getConfig();
		String resourceUid = ("{\"componentUid\":\"" + serviceUniqueId + "\"}");
		String url = String.format(Urls.CHANGE_RESOURCE_INSTANCE_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), ComponentTypeEnum.findParamByType(componentType), componentUniqueId,
				serviceInstanceToReplaceUniqueId);
		RestResponse changeResourceInstanceVersion = sendPost(url, resourceUid, sdncModifierDetails.getUserId(),
				acceptHeaderData);
		return changeResourceInstanceVersion;

	}

	public static RestResponse getProductByNameAndVersion(String productName, String productVersion, String userId)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_PRODUCT_BY_NAME_AND_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), productName, productVersion);
		logger.debug("Send GET request to get product by name and version: {}",url);
		return sendGet(url, userId);
	}

}
