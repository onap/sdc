/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.cucumber.steps;

import com.google.common.net.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockserver.model.HttpResponse.response;

public class PrecannedTestExpectationCallback implements ExpectationCallback  {
	private static volatile int countRequests;

	static HttpResponse httpResponse = response()
			.withStatusCode(200)
			.withHeaders(new Header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()));

	@Override
	public HttpResponse handle(HttpRequest httpRequest) {
		countRequests++;
		
		System.out.println(
				String.format("MSO Server Simulator Recieved %s Final Distribution Complete Rest Reports From ASDC",
						countRequests));
		
		return httpResponse;
	}
	
}
