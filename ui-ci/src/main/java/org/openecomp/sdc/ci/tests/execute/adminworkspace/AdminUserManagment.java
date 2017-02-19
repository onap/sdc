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

package org.openecomp.sdc.ci.tests.execute.adminworkspace;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import org.junit.rules.TestName;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.AdminWorkspaceUIUtilies;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Ordering;

public class AdminUserManagment extends SetupCDTest {
	//
	// public AdminUserManagment() {
	// super(new TestName(), AdminUserManagment.class.getName());
	// }

	private WebDriver driver = GeneralUIUtils.getDriver();

	// Create new USER_ID user
	@Test
	public void creatUserIdNewUserTest() throws Exception {
		String userId = AdminWorkspaceUIUtilies.defineNewUserId("th0695");
		String role = AdminWorkspaceUIUtilies.selectUserRole("designer");
		AdminWorkspaceUIUtilies.deleteuser(userId);
		GeneralUIUtils.getWebButton("creategreen").click();
		AdminWorkspaceUIUtilies.typeToSearchBox(userId);
		String createdUserUserId = GeneralUIUtils.getWebElementWaitForVisible("tdUSER_ID").getText();
		userId.equals(createdUserUserId);
		GeneralUIUtils.getWebElementWaitForVisible("tdLast Active").getText().equals("Waiting");
		GeneralUIUtils.getWebElementWaitForVisible("tdRole").getText().equals(role);
		AdminWorkspaceUIUtilies.deleteuser(userId);

	}

	// Create new MacId user
	@Test
	public void creatMacIdNewUserTest() throws Exception {

		String macId = AdminWorkspaceUIUtilies.defineNewUserId("m12345");
		String role = AdminWorkspaceUIUtilies.selectUserRole("designer");
		AdminWorkspaceUIUtilies.deleteuser(macId);
		GeneralUIUtils.getWebButton("creategreen").click();
		AdminWorkspaceUIUtilies.typeToSearchBox(macId);
		String createdUserUserId = GeneralUIUtils.getWebElementWaitForVisible("tdUSER_ID").getText();
		macId.equals(createdUserUserId);
		GeneralUIUtils.getWebElementWaitForVisible("tdLast Active").getText().equals("Waiting");
		GeneralUIUtils.getWebElementWaitForVisible("tdRole").getText().equals(role);
		AdminWorkspaceUIUtilies.deleteuser(macId);
	}

	// Create exist user and get error already exist .
	@Test
	public void createxistUserTest() throws Exception {
		String userId = AdminWorkspaceUIUtilies.defineNewUserId("th0695");
		String role = AdminWorkspaceUIUtilies.selectUserRole("designer");
		AdminWorkspaceUIUtilies.deleteuser(userId);
		GeneralUIUtils.getWebButton("creategreen").click();
		AdminWorkspaceUIUtilies.defineNewUserId(userId);
		AdminWorkspaceUIUtilies.selectUserRole("admin");
		GeneralUIUtils.getWebButton("creategreen").click();
		ResourceUIUtils.getErrorMessageText("w-sdc-modal-body-content")
				.equals("User with '" + userId + "' ID already exists.");
		GeneralUIUtils.clickOkButton();
		AdminWorkspaceUIUtilies.deleteuser(userId);

	}

	// enter Special chars and the create button disabled.
	@Test
	public void insertSpacialcharsTest() throws Exception {
		AdminWorkspaceUIUtilies.defineNewUserId("!@DER%");
		AdminWorkspaceUIUtilies.selectUserRole("designer");
		WebElement createbutton = GeneralUIUtils.getWebElementWaitForVisible("creategreen");
		Assert.assertFalse(createbutton.isEnabled());
	}

	// enter invalid macid and create button shall be disabled.
	@Test
	public void insertInvalidUserMacidTest() throws Exception {
		AdminWorkspaceUIUtilies.defineNewUserId("k12345");
		AdminWorkspaceUIUtilies.selectUserRole("designer");
		WebElement createbutton = GeneralUIUtils.getWebElementWaitForVisible("creategreen");
		Assert.assertFalse(createbutton.isEnabled());
	}

	// enter invalid userId and create button shall be disabled.
	@Test
	public void insertInvalidUserUserIdTest() throws Exception {
		AdminWorkspaceUIUtilies.defineNewUserId("ac1c23");
		AdminWorkspaceUIUtilies.selectUserRole("designer");
		WebElement createbutton = GeneralUIUtils.getWebElementWaitForVisible("creategreen");
		Assert.assertFalse(createbutton.isEnabled());
	}

	// display users list and sort by column name.
	@Test
	public void displayuserslistandsorting() throws Exception {
		GeneralUIUtils.getWebElementWaitForVisible("thFirst Name").click();
		Collection<WebElement> usersFname = GeneralUIUtils.getWebElements("tdFirst Name");
		for (WebElement webElement : usersFname) {
			System.out.println(webElement.getText());
		}
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.ADMIN;
	}

}
