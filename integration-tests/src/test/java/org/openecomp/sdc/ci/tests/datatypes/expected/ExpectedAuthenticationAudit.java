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

package org.openecomp.sdc.ci.tests.datatypes.expected;

public class ExpectedAuthenticationAudit {

	private String url;
	private String realm;
	private String user;
	private String action;
	private String authStatus;

	public ExpectedAuthenticationAudit(String url, String user, String action, String authStatus) {
		super();
		this.url = url;
		this.user = user;
		this.action = action;
		this.authStatus = authStatus;
		this.realm = "ASDC";
	}

	public ExpectedAuthenticationAudit() {

	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getAuthStatus() {
		return authStatus;
	}

	public void setAuthStatus(String authStatus) {
		this.authStatus = authStatus;
	}

	@Override
	public String toString() {
		return "ExpectedAuthenticationAudit [url=" + url + ", realm=" + realm + ", user=" + user + ", action=" + action
				+ ", authStatus=" + authStatus + "]";
	}

}
