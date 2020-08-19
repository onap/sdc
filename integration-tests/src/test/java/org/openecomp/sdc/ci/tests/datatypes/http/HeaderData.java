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

public class HeaderData {
	String contentMd5;
	String contentType;
	String HttpCspUserId;
	String HttpCspFirstName;
	String HttpCspLastName;
	String HttpCspWsType;
	String HttpIvRemoteAddress;
	String HttpIvUser;

	public HeaderData() {
		super();
	}

	public HeaderData(String contentMd5, String contentType, String httpCspUserId, String httpCspFirstName,
			String httpCspLastName, String httpCspWsType, String httpIvRemoteAddress, String httpIvUser) {
		super();
		this.contentMd5 = contentMd5;
		this.contentType = contentType;
		HttpCspUserId = httpCspUserId;
		HttpCspFirstName = httpCspFirstName;
		HttpCspLastName = httpCspLastName;
		HttpCspWsType = httpCspWsType;
		HttpIvRemoteAddress = httpIvRemoteAddress;
		HttpIvUser = httpIvUser;
	}

	public String getContentMd5() {
		return contentMd5;
	}

	public void setContentMd5(String contentMd5) {
		this.contentMd5 = contentMd5;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getHttpCspUserId() {
		return HttpCspUserId;
	}

	public void setHttpCspUserId(String httpCspUserId) {
		HttpCspUserId = httpCspUserId;
	}

	public String getHttpCspFirstName() {
		return HttpCspFirstName;
	}

	public void setHttpCspFirstName(String httpCspFirstName) {
		HttpCspFirstName = httpCspFirstName;
	}

	public String getHttpCspLastName() {
		return HttpCspLastName;
	}

	public void setHttpCspLastName(String httpCspLastName) {
		HttpCspLastName = httpCspLastName;
	}

	public String getHttpCspWsType() {
		return HttpCspWsType;
	}

	public void setHttpCspWsType(String httpCspWsType) {
		HttpCspWsType = httpCspWsType;
	}

	public String getHttpIvRemoteAddress() {
		return HttpIvRemoteAddress;
	}

	public void setHttpIvRemoteAddress(String httpIvRemoteAddress) {
		HttpIvRemoteAddress = httpIvRemoteAddress;
	}

	public String getHttpIvUser() {
		return HttpIvUser;
	}

	public void setHttpIvUser(String httpIvUser) {
		HttpIvUser = httpIvUser;
	}

}
