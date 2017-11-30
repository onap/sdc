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

package org.openecomp.sdc.ci.tests.verificator;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.pages.AdminGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.aventstack.extentreports.Status;

public class UserManagementVerificator {


	
	public static void validateUserCreated(String userId, UserRoleEnum role){
		
		ExtentTestActions.log(Status.INFO, "Validating that a new user is created and displayed in the first row in the table.");
		
		final int firstRow = 0;
		
		WebElement actualFirstName = AdminGeneralPage.getUserManagementTab().getFirstName(firstRow);
		WebElement actualLastName = AdminGeneralPage.getUserManagementTab().getLastName(firstRow);
		WebElement actualUserId = AdminGeneralPage.getUserManagementTab().getUserId(firstRow);
		WebElement actualEmail = AdminGeneralPage.getUserManagementTab().getEmail(firstRow);
		WebElement actualRole = AdminGeneralPage.getUserManagementTab().getRole(firstRow);
		WebElement actualLastActive = AdminGeneralPage.getUserManagementTab().getLastActive(firstRow);
		
		
		String actualFirstNameText = actualFirstName.getText();
		String actualLastNameText = actualLastName.getText();
		String actualUserIdText = actualUserId.getText();
		String actualEmailText = actualEmail.getText();
		String actualRoleText = actualRole.getText();
		String actualLastActiveText = actualLastActive.getText();
		
		Assert.assertTrue(actualFirstNameText.equals("---"), "Actual first name is not '---'.");
		Assert.assertTrue(actualLastNameText.equals("---"), "Actual last name is not '---'.");
		Assert.assertTrue(actualUserIdText.equals(userId), "Actual user id is not  " + userId);
		Assert.assertTrue(actualEmailText.equals("---"), "Actual email is not '---'.");
		Assert.assertTrue(actualRoleText.equals(WordUtils.capitalize(role.name().toLowerCase())), "Actual role is not " + role.name());
		Assert.assertTrue(actualLastActiveText.equals("Waiting"), "Actual role is not 'Waiting'.");
	}
	
	
	public static void validateUserRoleUpdated(int rowIndx, UserRoleEnum updatedRole){
		ExtentTestActions.log(Status.INFO, "Validating role is updated to " + updatedRole.name() + " in UI.");
		WebElement actualRole = AdminGeneralPage.getUserManagementTab().getRole(rowIndx);
		String actualRoleText = actualRole.getText();
		Assert.assertTrue(actualRoleText.equals(WordUtils.capitalize(updatedRole.name().toLowerCase())), "Actual role is not " + updatedRole.name());
	}
	
	public static void validateUserRoleUpdatedViaRest(User reqUser, User user, UserRoleEnum expectedUserRole){
		try{
			ExtentTestActions.log(Status.INFO, "Validating role is updated to " + expectedUserRole.name() + " in BE.");
			String actualUserRole = RestCDUtils.getUserRole(reqUser, user);
			Assert.assertTrue(expectedUserRole.name().toLowerCase().equals(actualUserRole.toLowerCase()), "User role is not updated.");
		}
		catch(Exception e){
			Assert.fail("The actual user role is null");
		}
	}
	
	public static void validateUserNotFoundViaRest(User reqUser, User user){
		try{
			ExtentTestActions.log(Status.INFO, "Validating user " + reqUser.getUserId() + " is not found in BE.");
			RestResponse getUserResp = RestCDUtils.getUser(reqUser, user);
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), Arrays.asList(reqUser.getUserId()), getUserResp.getResponse());
		}
		catch(Exception e){
			Assert.fail("The response message does not describe the user is not found.");
		}
	}
	
	public static void validateUserIdNotFound(String userId){
		ExtentTestActions.log(Status.INFO, "Validating that user " + userId + " is not found.");
		AdminGeneralPage.getUserManagementTab().searchUser(userId);
		List<WebElement> rows = AdminGeneralPage.getUserManagementTab().getAllRowsDisplayed();
		Assert.assertEquals(rows.size(), 0, String.format("There are %s rows instead of none.", rows.size()));
	}
	
	public static void validateOnlySingleRowDisplayed(){
		ExtentTestActions.log(Status.INFO, "Validating that only a single row is displayed in table.");
		List<WebElement> rows = AdminGeneralPage.getUserManagementTab().getAllRowsDisplayed();
		Assert.assertEquals(rows.size(), 1, String.format("There are %s rows instead of %s.", rows.size(), 1));
	}
	
	public static void validateRowDisplayedCorrectly(User user, int rowindex){
		String role = user.getRole();
		String userId = user.getUserId();
		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		String email = user.getEmail();
		
		ExtentTestActions.log(Status.INFO, "Validating that the row is properly displayed.");

		WebElement actualFirstName = AdminGeneralPage.getUserManagementTab().getFirstName(rowindex);
		WebElement actualLastName = AdminGeneralPage.getUserManagementTab().getLastName(rowindex);
		WebElement actualUserId = AdminGeneralPage.getUserManagementTab().getUserId(rowindex);
		WebElement actualEmail = AdminGeneralPage.getUserManagementTab().getEmail(rowindex);
		WebElement actualRole = AdminGeneralPage.getUserManagementTab().getRole(rowindex);
		
		
		String actualFirstNameText = actualFirstName.getText();
		String actualLastNameText = actualLastName.getText();
		String actualUserIdText = actualUserId.getText();
		String actualEmailText = actualEmail.getText();
		String actualRoleText = actualRole.getText();
		
		Assert.assertTrue(actualFirstNameText.equals(firstName), "Actual first name is not " + firstName);
		Assert.assertTrue(actualLastNameText.equals(lastName), "Actual last name is not " + lastName);
		Assert.assertTrue(actualUserIdText.equals(userId), "Actual user id is not  " + userId);
		Assert.assertTrue(actualEmailText.contains(email), "Actual email does not contain " + email);
		Assert.assertTrue(actualRoleText.equals(WordUtils.capitalize(role.toLowerCase())), "Actual role is not " + role);
	}
	
	public static void validateFirstRowDisplayedCorrectly(User user){
		validateRowDisplayedCorrectly(user, 0);
	}
	
	
}
