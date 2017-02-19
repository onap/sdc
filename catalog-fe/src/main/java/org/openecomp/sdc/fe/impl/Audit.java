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

package org.openecomp.sdc.fe.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;

public class Audit {

	private Audit() {
	}

	public static void error(Logger log, HttpRequestInfo requestInfo, int status) {
		String errorMsg = "Internal Error";
		if (requestInfo != null && requestInfo.getHeaders() != null) {
			Map<String, String> requestHeaders = requestInfo.getHeaders();
			errorMsg = String.format(Constants.ERROR_LOG_FORMAT, requestHeaders.get(Constants.USER_ID_HEADER),
					requestHeaders.get(Constants.FIRST_NAME_HEADER) + " "
							+ requestHeaders.get(Constants.LAST_NAME_HEADER),
					requestHeaders.get(Constants.ORIGIN_HEADER), requestHeaders.get(Constants.ACCESS_HEADER),
					requestInfo.getRequestURL(), status);
		}
		log.error(errorMsg);
	}

	public static void error(Logger log, HttpServletRequest request, int status) {
		String errorMsg = "Internal Error";
		if (request != null) {

			errorMsg = String.format(Constants.ERROR_LOG_FORMAT, request.getHeader(Constants.USER_ID_HEADER),
					request.getHeader(Constants.FIRST_NAME_HEADER) + " "
							+ request.getHeader(Constants.LAST_NAME_HEADER),
					request.getHeader(Constants.ORIGIN_HEADER), request.getHeader(Constants.ACCESS_HEADER),
					request.getRequestURL(), status);
		}
		log.error(errorMsg);
	}

	public static void info(Logger log, HttpRequestInfo requestInfo, int status) {
		String errorMsg = "Internal Error";
		if (requestInfo != null && requestInfo.getHeaders() != null) {
			Map<String, String> requestHeaders = requestInfo.getHeaders();
			errorMsg = String.format(Constants.ERROR_LOG_FORMAT, requestHeaders.get(Constants.USER_ID_HEADER),
					requestHeaders.get(Constants.FIRST_NAME_HEADER) + " "
							+ requestHeaders.get(Constants.LAST_NAME_HEADER),
					requestHeaders.get(Constants.ORIGIN_HEADER), requestHeaders.get(Constants.ACCESS_HEADER),
					requestInfo.getRequestURL(), status);
		}
		log.info(errorMsg);
	}
}
