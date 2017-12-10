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

package org.openecomp.sdc.ci.tests.datatypes;

import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

public class UserManagementTab {

	public void searchUser(String searchCriterion){
		ExtentTestActions.log(Status.INFO, "Searching a user by the value : " + searchCriterion);
		WebElement searchBoxWebElement = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.SEARCH_BOX.getValue());
		searchBoxWebElement.clear();
		searchBoxWebElement.sendKeys(searchCriterion);
		GeneralUIUtils.ultimateWait();
	}
	
	public void setNewUserBox(String user){
		ExtentTestActions.log(Status.INFO, "Inserting userid " + user);
		WebElement createNewUserWebElement = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.NEW_USER_FIELD.getValue());
		createNewUserWebElement.clear();
		createNewUserWebElement.sendKeys(user);
		GeneralUIUtils.ultimateWait();
	}
	
	public void selectUserRole(UserRoleEnum userRole){
		String role = userRole.name().toLowerCase();
		ExtentTestActions.log(Status.INFO, "Selecting role " + userRole.name());
		GeneralUIUtils.getSelectList(role, DataTestIdEnum.UserManagementEnum.ROLE_SELECT.getValue());
	}
	
	public void updateUserRole(UserRoleEnum userRole, int rowIndx){
		String role = userRole.name().toLowerCase();
		ExtentTestActions.log(Status.INFO, "Updating the user role to " + userRole.name());
		GeneralUIUtils.getSelectList(role, DataTestIdEnum.UserManagementEnum.UPDATE_ROLE.getValue() + rowIndx);
	}
	
	public void clickCreateButton(){
		ExtentTestActions.log(Status.INFO, "Clicking on 'Create' button.");
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.UserManagementEnum.CREATE_BUTTON.getValue());
	}
	
	public WebElement getRow(int index){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.ROW_TABLE.getValue() + index);
	}
	
	public WebElement getFirstName(int index){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.FIRST_NAME.getValue() + index);
	}
	
	public WebElement getLastName(int index){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.LAST_NAME.getValue() + index);
	}
	
	public WebElement getUserId(int index){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.USER_ID.getValue() + index);
	}
	
	public WebElement getEmail(int index){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.EMAIL.getValue() + index);
	}
	
	public WebElement getRole(int index){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.ROLE.getValue() + index);
	}
	
	public WebElement getLastActive(int index){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.UserManagementEnum.LAST_ACTIVE.getValue() + index);
	}
	
	public void updateUser(int index){
		GeneralUIUtils.ultimateWait();
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.UserManagementEnum.UPDATE_USER_BUTTON.getValue() + index);
	}
	
	public void deleteUser(int index){
		ExtentTestActions.log(Status.INFO, "Deleting the user in row " + (index + 1));
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.UserManagementEnum.DELETE_USER.getValue() + index);
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
	}
	
	public void saveAfterUpdateUser(int index){
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.UserManagementEnum.SAVE_USER.getValue() + index);
	}
	
	public List<WebElement> getAllRowsDisplayed(){
		return GeneralUIUtils.getWebElementsListByContainTestID(DataTestIdEnum.UserManagementEnum.ROW_TABLE.getValue());
	}
	
	
}
