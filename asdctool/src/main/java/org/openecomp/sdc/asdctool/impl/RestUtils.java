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

package org.openecomp.sdc.asdctool.impl;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mlando on 2/23/2016.
 */
public class RestUtils {

	final String DELETE_PRODUCT = "http://%s:%s/sdc2/rest/v1/catalog/products/%s";
	final Integer DELETE_SUCCSES_RESPONSE = 200;

	private static Logger log = LoggerFactory.getLogger(RestUtils.class.getName());
	CloseableHttpClient httpClient;

	public RestUtils() {
		this.httpClient = HttpClients.createDefault();
	}

	private CloseableHttpResponse exacuteRequest(HttpUriRequest httpRequest) throws IOException {
		log.debug("received http request: {}", httpRequest.toString());
		return httpClient.execute(httpRequest);
	}

	public void closeClient() {
		log.debug("closing http client");
		try {
			this.httpClient.close();
			log.debug("closed http client");
		} catch (IOException e) {
			log.debug("close http client failed", e);

		}
	}

	public Integer deleteProduct(String productUid, String beHost, String bePort, String adminUser) {
		String url = String.format(DELETE_PRODUCT, beHost, bePort, productUid);
		HttpDelete deleteRequest = new HttpDelete(url);
		deleteRequest.setHeader("USER_ID", adminUser);
		try (CloseableHttpResponse response = this.httpClient.execute(deleteRequest)) {
			int status = response.getStatusLine().getStatusCode();
			if (DELETE_SUCCSES_RESPONSE.equals(status)) {
				log.debug("Product uid:{} succsesfully deleted", productUid);
			} else {
				log.error("Product uid:{} delete failed status {}", productUid, status);
			}
			return status;
		} catch (IOException e) {
			log.error("Product uid:{} delete failed with exception",productUid, e);
		}
		return null;
	}

}
