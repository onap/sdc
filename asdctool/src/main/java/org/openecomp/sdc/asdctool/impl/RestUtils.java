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

import org.apache.http.HttpStatus;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.Properties;

/**
 * Created by mlando on 2/23/2016.
 */
public class RestUtils {

	final static String DELETE_PRODUCT = "http://%s:%s/sdc2/rest/v1/catalog/products/%s";

	private static Logger log = Logger.getLogger(RestUtils.class.getName());

	public RestUtils() {
	}

	public Integer deleteProduct(String productUid, String beHost, String bePort, String adminUser) {
		String url = String.format(DELETE_PRODUCT, beHost, bePort, productUid);
		
		Properties headers = new Properties();
		headers.put("USER_ID", adminUser);
		try {
		    HttpResponse<String> httpResponse = HttpRequest.delete(url, headers);
            int status = httpResponse.getStatusCode();
            if (status == HttpStatus.SC_OK) {
                log.debug("Product uid:{} succsesfully deleted", productUid);
            }
            else {
                log.error("Product uid:{} delete failed status {}", productUid, status);
            }
            return status;
		}
		catch(Exception e) {
		    log.error("Product uid:{} delete failed with exception",productUid, e);
		}
		return null;		
	}

}
