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

package org.openecomp.sdc.ci.tests.datatypes.http;

import java.util.HashMap;
import java.util.Map;

public class MustHeaders {

	private Map<String, String> headers = new HashMap<String, String>();

	public MustHeaders(HeaderData headerData) {

		super();
		headers.put(HttpHeaderEnum.Content_MD5.getValue(), headerData.getContentMd5());
		headers.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), headerData.getContentType());
		headers.put(HttpHeaderEnum.ACCEPT.getValue(), headerData.getContentType());
		headers.put(HttpHeaderEnum.USER_ID.getValue(), headerData.getHttpCspUserId());
		headers.put(HttpHeaderEnum.HTTP_CSP_FIRSTNAME.getValue(), headerData.getHttpCspFirstName());
		headers.put(HttpHeaderEnum.HTTP_CSP_LASTNAME.getValue(), headerData.getHttpCspLastName());
		headers.put(HttpHeaderEnum.HTTP_CSP_WSTYPE.getValue(), headerData.getHttpCspWsType());
		headers.put(HttpHeaderEnum.HTTP_IV_REMOTE_ADDRESS.getValue(), headerData.getHttpIvRemoteAddress());
		headers.put(HttpHeaderEnum.HTTP_IV_USER.getValue(), headerData.getHttpIvUser());

	}

	public MustHeaders() {
		super();
	}

	public Map<String, String> getMap() {
		return headers;
	}

}
