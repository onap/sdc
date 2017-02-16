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

package org.openecomp.sdc.ci.tests.utilities;

import org.junit.rules.TestName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class AdminWorkspaceUIUtilies {

	public AdminWorkspaceUIUtilies(TestName name, String className) {
		super();
		// TODO Auto-generated constructor stub
	}

	protected static WebDriver driver;

	public static void deleteuser(String userId) throws Exception {
		typeToSearchBox(userId);
		if (GeneralUIUtils.getWebElements("tdRow") != null) {
			GeneralUIUtils.getWebElementWaitForVisible("delete" + userId + "").click();
			GeneralUIUtils.clickOkButton();
		}
	}

	public static String defineNewUserId(String userId) {
		GeneralUIUtils.getWebElementWaitForVisible("newUserId").clear();
		GeneralUIUtils.getWebElementWaitForVisible("newUserId").sendKeys(userId);
		;
		return userId;
	}

	public static String defineNewMacUid(String MacUid) {
		GeneralUIUtils.getWebElementWaitForVisible("newUserId").clear();
		GeneralUIUtils.getWebElementWaitForVisible("newUserId").sendKeys(MacUid);
		;
		return MacUid;
	}

	public static String selectUserRole(String Role) {
		Select selectrole = new Select(GeneralUIUtils.getWebElementWaitForVisible("selectrole"));
		selectrole.deselectByVisibleText(Role);
		selectrole.selectByVisibleText(Role);
		return Role;
	}

	public static void typeToSearchBox(String Text) throws Exception {
		GeneralUIUtils.getWebElementWaitForVisible("searchbox").clear();
		GeneralUIUtils.getWebElementWaitForVisible("searchbox").sendKeys(Text);
		Thread.sleep(1000);
	}

}
