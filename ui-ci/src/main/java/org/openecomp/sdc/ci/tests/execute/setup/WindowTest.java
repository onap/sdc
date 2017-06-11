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

package org.openecomp.sdc.ci.tests.execute.setup;

import java.io.File;

import org.openecomp.sdc.be.model.User;

public class WindowTest {
	
	public WindowTest(){
		refreshAttempts = 0;
		previousRole = "";
		addedValueFromDataProvider = null;
		try {
			downloadDirectory = DriverFactory.getDriverFirefoxProfile().getStringPreference("browser.download.dir", null) + File.separator;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int refreshAttempts;
	private String previousRole;
	private User user;
	private String addedValueFromDataProvider;
	private String downloadDirectory;
	
	public int getRefreshAttempts() {
		return refreshAttempts;
	}
	public void setRefreshAttempts(int refreshAttempts) {
		this.refreshAttempts = refreshAttempts;
	}
	public String getPreviousRole() {
		return previousRole;
	}
	public void setPreviousRole(String previousRole) {
		this.previousRole = previousRole;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public synchronized String getAddedValueFromDataProvider() {
		return addedValueFromDataProvider;
	}
	public synchronized void setAddedValueFromDataProvider(String addedValueFromDataProvider) {
		this.addedValueFromDataProvider = addedValueFromDataProvider;
	}
	public String getDownloadDirectory() {
		return downloadDirectory;
	}
	public void setDownloadDirectory(String downloadDirectory) {
		this.downloadDirectory = downloadDirectory;
	}

}
