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

package org.openecomp.sdc.ci.tests.execute.sanity;

import java.io.IOException;
import java.util.List;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.AdminGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.AdminWorkspaceUIUtilies;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.UserRestUtils;
import org.openecomp.sdc.ci.tests.verificator.ErrorMessageUIVerificator;
import org.openecomp.sdc.ci.tests.verificator.UserManagementVerificator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

public class AdminUserManagment extends SetupCDTest {
	
	@DataProvider(name = "searchValues")
	private final Object[][] searchString(){
		User newUser = createNewUserUsingAPI();
		GeneralUIUtils.getDriver().navigate().refresh();
 		return new Object[][]{{newUser.getUserId(), newUser}, {newUser.getFirstName(), newUser}, {newUser.getLastName(), newUser}, {newUser.getEmail(), newUser}};
	}
	
	//TC915101
	@Test(dataProvider = "searchValues")
	public void searchUserByCriterionsTest(String searchCriterion, User user) throws IOException{
		setLog(searchCriterion);
		AdminWorkspaceUIUtilies.searchForUser(searchCriterion);
		UserManagementVerificator.validateFirstRowDisplayedCorrectly(user);
	}

	//TC915100
	@Test
	public void creatNewUserTest() throws Exception {
		
		String userId = generateValidUserId();
		UserRoleEnum userRole = UserRoleEnum.DESIGNER;
		AdminWorkspaceUIUtilies.createNewUser(userId, userRole);
		UserManagementVerificator.validateUserCreated(userId, userRole);
	}
	
	//TC922253
	@Test
	public void creatNewUser_MacIdTest() throws Exception {
		
		String macId = generateValidMacId();
		UserRoleEnum userRole = UserRoleEnum.DESIGNER;
		AdminWorkspaceUIUtilies.createNewUser(macId, userRole);
		UserManagementVerificator.validateUserCreated(macId, userRole);
	}
	
	//TC922253
	@Test
	public void createExistingUserTest(){
		String userId = generateValidUserId();
		UserRoleEnum userRole = UserRoleEnum.DESIGNER;
		AdminWorkspaceUIUtilies.createNewUser(userId, userRole);
		ExtentTestActions.log(Status.INFO, "Trying to create the same user once again.");
		GeneralUIUtils.ultimateWait(); // extra wait, necessary for system with large user list
		AdminWorkspaceUIUtilies.createNewUser(userId, userRole);
		ErrorMessageUIVerificator.validateErrorMessage(ActionStatus.USER_ALREADY_EXIST);
	}

	//TC922253
	@Test
	public void createInvalidMacIdTest(){
		
//		if(true){
//			throw new SkipException("Open bug 324032");			
//		}
		
		String macId = generateValidMacId();
		StringBuilder invalidMacId = new StringBuilder(macId);
		invalidMacId.setCharAt(0, 'a');
		UserRoleEnum userRole = UserRoleEnum.DESIGNER;
		ExtentTestActions.log(Status.INFO, "Trying to create an invalid macId.");
		AdminWorkspaceUIUtilies.createNewUser(invalidMacId.toString(), userRole);
		ErrorMessageUIVerificator.validateErrorMessage(ActionStatus.INVALID_USER_ID);
	}
	
	//TC922253
	@Test
	public void specialCharInUserIdTest(){
		String expectedErrorMsg = "User id not valid.";
		String userId = generateValidUserId();
		StringBuilder invalidUserId = new StringBuilder(userId);
		invalidUserId.setCharAt(1, '#');
		ExtentTestActions.log(Status.INFO, String.format("Trying to create an invalid user with special character (%s)", userId));
		AdminGeneralPage.getUserManagementTab().setNewUserBox(invalidUserId.toString());
		ExtentTestActions.log(Status.INFO, "Validating an error message is displayed as a result of invalid character.");
		List<WebElement> inputErrors = null;
		int inputErrorsSize = 0;
		
		try{
			WebElement inputField = GeneralUIUtils.getWebElementByClassName("input-error");
			ExtentTestActions.log(Status.INFO, String.format("Validating the message is : '%s'", expectedErrorMsg));
			inputErrors = inputField.findElements(By.className("ng-scope"));
			inputErrorsSize = inputErrors.size();
			for (WebElement err : inputErrors){
				String actualErrorMessage = err.getText();
				if (actualErrorMessage.equals(expectedErrorMsg)){
					inputErrorsSize--;
				}
			}
		}
		catch(Exception e){
			ExtentTestActions.log(Status.INFO, "Did not find an error input.");
			Assert.fail("Did not find an error message input.");
		}
		
		Assert.assertEquals(inputErrors.size() - 1 , inputErrorsSize, "Did not find an error : " + expectedErrorMsg);
	}
	


	//TC915101
	@Test
	public void searchForUserByRoleTest(){
		String userId = generateValidUserId();
		UserRoleEnum userRole = UserRoleEnum.DESIGNER;
		AdminWorkspaceUIUtilies.createNewUser(userId, userRole);
		AdminWorkspaceUIUtilies.searchForUser(userRole.name());
		List<WebElement> allRows = GeneralUIUtils.getWebElementsListByContainTestID(DataTestIdEnum.UserManagementEnum.USER_ID.getValue());
		ExtentTestActions.log(Status.INFO, String.format("Found %s rows, looking for the user %s.", allRows.size(), userId));
		int rowsCount = allRows.size();
		for (int i = 0 ; i < allRows.size() ; i++){
			String userIdFromTable = GeneralUIUtils.getTextContentAttributeValue(allRows.get(i));
			if (userIdFromTable.equals(userId)){
				rowsCount--;
				break;
			}
		}
		Assert.assertEquals(allRows.size() - 1 , rowsCount , "Did not find a row with the userId " + userId);
	}
	
	//TC915102
	@Test
	public void modifyUserRoleTest(){
		User user = new User();
		user.setUserId(generateValidUserId());
		UserRoleEnum userRole = UserRoleEnum.DESIGNER;
		AdminWorkspaceUIUtilies.createNewUser(user.getUserId(), userRole);
		UserRoleEnum updatedUserRole = UserRoleEnum.TESTER;
		AdminWorkspaceUIUtilies.updateUserRole(0, updatedUserRole);
		UserManagementVerificator.validateUserRoleUpdated(0, updatedUserRole);
		UserManagementVerificator.validateUserRoleUpdatedViaRest(user, getUser(), updatedUserRole);
	}
	
	//TC915103
	@Test
	public void deleteUserTest(){
		User user = new User();
		user.setUserId(generateValidUserId());
		UserRoleEnum userRole = UserRoleEnum.DESIGNER;
		AdminWorkspaceUIUtilies.createNewUser(user.getUserId(), userRole);
		AdminWorkspaceUIUtilies.deleteFirstRow();
		UserManagementVerificator.validateUserIdNotFound(user.getUserId());
		UserManagementVerificator.validateUserNotFoundViaRest(user, getUser());
	}
	
	//TC951428
	@Test
	public void modifyUserRoleWithTaskInHand_Checkout() throws Exception{
		User newUser = new User();
		newUser.setUserId(generateValidUserId());
		UserRoleEnum userRole = UserRoleEnum.DESIGNER;
		AdminWorkspaceUIUtilies.createNewUser(newUser.getUserId(), userRole);
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, newUser);
		ExtentTestActions.log(Status.INFO, "Creating a new VF named " + resourceMetaData.getName() + " with the user " + newUser.getUserId());
		RestResponse createResourceResp = ResourceRestUtils.createResource(resourceMetaData, newUser);
		Assert.assertEquals(createResourceResp.getErrorCode().intValue(), 201, "Did not succeed to create a VF");
		
		UserRoleEnum updatedUserRole = UserRoleEnum.TESTER;
		AdminWorkspaceUIUtilies.updateUserRole(0, updatedUserRole);
		
		ErrorMessageUIVerificator.validateErrorMessage(ActionStatus.CANNOT_UPDATE_USER_WITH_ACTIVE_ELEMENTS);
	}
	
	@Test
	public void modifyUserRoleWithTaskInHand_InTesting() throws Exception{
		User newTesterUser = new User();
		newTesterUser.setUserId(generateValidUserId());
		UserRoleEnum userTesterRole = UserRoleEnum.TESTER;
		userTesterRole.setUserId(newTesterUser.getUserId());
		AdminWorkspaceUIUtilies.createNewUser(newTesterUser.getUserId(), userTesterRole);
		
		reloginWithNewRole(UserRoleEnum.DESIGNER);
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());
		ExtentTestActions.log(Status.INFO, "Creating a new VF named " + resourceMetaData.getName());
		ResourceUIUtils.createVF(resourceMetaData, getUser());
		ResourceGeneralPage.clickSubmitForTestingButton(resourceMetaData.getName());
		Resource resourceObjectByNameAndVersion = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), "0.1");
		ExtentTestActions.log(Status.INFO, "Getting the VF to 'In Testing' state.");
		AtomicOperationUtils.changeComponentState(resourceObjectByNameAndVersion, userTesterRole, LifeCycleStatesEnum.STARTCERTIFICATION, true);
		ExtentTestActions.log(Status.INFO, "Succeeded - The VF is in testing state.");
		
		reloginWithNewRole(UserRoleEnum.ADMIN);
		UserRoleEnum updatedUserRole = UserRoleEnum.DESIGNER;
		AdminWorkspaceUIUtilies.searchForUser(newTesterUser.getUserId());
		AdminWorkspaceUIUtilies.updateUserRole(0, updatedUserRole);
		
		ErrorMessageUIVerificator.validateErrorMessage(ActionStatus.CANNOT_UPDATE_USER_WITH_ACTIVE_ELEMENTS);
	}
	
	
	private static String generateValidUserId() {
		String charsPattern = "abcdefghijklmnopqrstuvwxyz";
		String digitPatter = "0123456789";
		String chars = ResourceUIUtils.buildStringFromPattern(charsPattern, 2);
		String digits = ResourceUIUtils.buildStringFromPattern(digitPatter, 4);
		return chars + digits;
	}
	
	private String generateValidMacId() {
		String digitPatter = "0123456789";
		String digits = ResourceUIUtils.buildStringFromPattern(digitPatter, 5);
		return "m" + digits;
	}
	
	private User createNewUserUsingAPI() {
		UserRoleEnum role = UserRoleEnum.DESIGNER;
		String userId = generateValidUserId();
		User userByEnv = new User(generateValidUserId(), generateValidUserId(), userId, generateValidUserId()+"@intl.sdc.com", role.name(), null);
		User adminUser = getUserByEnv(UserRoleEnum.ADMIN);
		try {
			RestResponse createUserResp = UserRestUtils.createUser(userByEnv, adminUser);
			Assert.assertEquals(createUserResp.getErrorCode().intValue(), 201, "Did not succeed to create a new user using API.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return userByEnv;
	}
	
	
	private User getUserByEnv(UserRoleEnum userRole) {
		try{
			if (!getConfig().getUrl().contains("localhost") && !getConfig().getUrl().contains("127.0.0.1")) {
				return getUserFromFileByRole(userRole);
			}
			else{
				return getUser(userRole);
			}
		}
		catch (Exception e){
			throw new RuntimeException(e);
		}
	}



	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.ADMIN;
	}

}
