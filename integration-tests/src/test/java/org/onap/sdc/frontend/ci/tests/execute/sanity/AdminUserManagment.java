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

package org.onap.sdc.frontend.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import org.apache.http.HttpStatus;
import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.frontend.ci.tests.pages.AdminGeneralPage;
import org.onap.sdc.frontend.ci.tests.utilities.AdminWorkspaceUIUtilies;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;
import org.onap.sdc.frontend.ci.tests.utilities.ResourceUIUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.UserRestUtils;
import org.onap.sdc.frontend.ci.tests.verificator.ErrorMessageUIVerificator;
import org.onap.sdc.frontend.ci.tests.verificator.UserManagementVerificator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class AdminUserManagment extends SetupCDTest {

    @DataProvider(name = "searchValues")
    private Object[][] searchString() {
        User newUser = createNewUserUsingAPI();
        GeneralUIUtils.getDriver().navigate().refresh();
        return new Object[][]{{newUser.getUserId(), newUser}, {newUser.getFirstName(), newUser}, {newUser.getLastName(), newUser}, {newUser.getEmail(), newUser}};
    }

    //TC915101
    @Test(dataProvider = "searchValues")
    public void searchUserByCriterionsTest(String searchCriterion, User user) throws IOException {
        setLog(searchCriterion);
        AdminWorkspaceUIUtilies.searchForUser(searchCriterion);
        UserManagementVerificator.validateFirstRowDisplayedCorrectly(user);
    }

    //TC915100
    @Test
    public void createNewUserTest() throws Exception {

        String userId = generateValidUserId();
        UserRoleEnum userRole = UserRoleEnum.DESIGNER;
        AdminWorkspaceUIUtilies.createNewUser(userId, userRole);
        GeneralUIUtils.ultimateWait();
        UserManagementVerificator.validateUserCreated(userId, userRole);
    }

    //TC922253
    @Test
    public void createNewUser_MacIdTest() throws Exception {

        String macId = generateValidMacId();
        UserRoleEnum userRole = UserRoleEnum.DESIGNER;
        AdminWorkspaceUIUtilies.createNewUser(macId, userRole);
        GeneralUIUtils.ultimateWait();
        UserManagementVerificator.validateUserCreated(macId, userRole);
    }

    //TC922253
    @Test
    public void createExistingUserTest() {
        String userId = generateValidUserId();
        UserRoleEnum userRole = UserRoleEnum.DESIGNER;
        AdminWorkspaceUIUtilies.createNewUser(userId, userRole);
        ExtentTestActions.log(Status.INFO, "Trying to create the same user once again.");
        GeneralUIUtils.ultimateWait(); // extra wait, necessary for system with large user list
        AdminWorkspaceUIUtilies.createNewUser(userId, userRole);
        ErrorMessageUIVerificator.validateErrorMessage(ActionStatus.USER_ALREADY_EXIST);
    }


    // design changed and now one letter user should exist
    //TC922253
    @Test(enabled = true)
    public void createInvalidMacIdTest() {

        ExtentTestActions.log(Status.INFO, "Open bug 324032");

        String macId = generateValidMacId();
        StringBuilder invalidMacId = new StringBuilder(macId);
        invalidMacId.setCharAt(0, 'a');
        UserRoleEnum userRole = UserRoleEnum.DESIGNER;
        ExtentTestActions.log(Status.INFO, "Trying to create an invalid macId.");
        AdminWorkspaceUIUtilies.createNewUser(invalidMacId.toString(), userRole);
        GeneralUIUtils.ultimateWait();
        ErrorMessageUIVerificator.validateErrorMessage(ActionStatus.INVALID_USER_ID);
    }

    //TC922253
    @Test
    public void specialCharInUserIdTest() {
        String expectedErrorMsg = "User id not valid.";
        String userId = generateValidUserId();
        StringBuilder invalidUserId = new StringBuilder(userId);
        invalidUserId.setCharAt(1, '#');
        ExtentTestActions.log(Status.INFO, String.format("Trying to create an invalid user with special character (%s)", userId));
        AdminGeneralPage.getUserManagementTab().setNewUserBox(invalidUserId.toString());
        ExtentTestActions.log(Status.INFO, "Validating an error message is displayed as a result of invalid character.");
        List<WebElement> inputErrors = null;
        int inputErrorsSize = 0;

        try {
            WebElement inputField = GeneralUIUtils.getWebElementByClassName("input-error");
            ExtentTestActions.log(Status.INFO, String.format("Validating the message is : '%s'", expectedErrorMsg));
            inputErrors = inputField.findElements(By.className("ng-scope"));
            inputErrorsSize = inputErrors.size();
            for (WebElement err : inputErrors) {
                String actualErrorMessage = err.getText();
                if (actualErrorMessage.equals(expectedErrorMsg)) {
                    inputErrorsSize--;
                }
            }
        } catch (Exception e) {
            ExtentTestActions.log(Status.INFO, "Did not find an error input.");
            Assert.fail("Did not find an error message input.");
        }

        Assert.assertEquals(inputErrors.size() - 1, inputErrorsSize, "Did not find an error : " + expectedErrorMsg);
    }


    //TC915101
    @Test
    public void searchForUserByRoleTest() {
        String userId = generateValidUserId();
        UserRoleEnum userRole = UserRoleEnum.DESIGNER;
        AdminWorkspaceUIUtilies.createNewUser(userId, userRole);
        AdminWorkspaceUIUtilies.searchForUser(userRole.name());
        List<WebElement> allRows = GeneralUIUtils.getWebElementsListByContainTestID(DataTestIdEnum.UserManagementEnum.USER_ID.getValue());
        ExtentTestActions.log(Status.INFO, String.format("Found %s rows, looking for the user %s.", allRows.size(), userId));
        int rowsCount = allRows.size();
        for (WebElement allRow : allRows) {
            String userIdFromTable = GeneralUIUtils.getTextContentAttributeValue(allRow);
            if (userIdFromTable.equals(userId)) {
                rowsCount--;
                break;
            }
        }
        Assert.assertEquals(allRows.size() - 1, rowsCount, "Did not find a row with the userId " + userId);
    }

    //TC915102
    @Test
    public void modifyUserRoleTest() {
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
    public void deleteUserTest() {
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
    public void modifyUserRoleWithTaskInHand_Checkout() throws Exception {
        User newUser = new User();
        newUser.setUserId(generateValidUserId());
        UserRoleEnum userRole = UserRoleEnum.DESIGNER;
        AdminWorkspaceUIUtilies.createNewUser(newUser.getUserId(), userRole);

        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, newUser);
        ExtentTestActions.log(Status.INFO, "Creating a new VF named " + resourceMetaData.getName() + " with the user " + newUser.getUserId());
        RestResponse createResourceResp = ResourceRestUtils.createResource(resourceMetaData, newUser);
        Assert.assertEquals(createResourceResp.getErrorCode().intValue(), HttpStatus.SC_CREATED, "Did not succeed to create a VF");

        UserRoleEnum updatedUserRole = UserRoleEnum.TESTER;
        AdminWorkspaceUIUtilies.updateUserRole(0, updatedUserRole);

        ErrorMessageUIVerificator.validateErrorMessage(ActionStatus.CANNOT_UPDATE_USER_WITH_ACTIVE_ELEMENTS);
    }


    private static String generateValidUserId() {
        final int charsPatternLength = 2;
        final int digitsPatternLength = 4;
        String charsPattern = "abcdefghijklmnopqrstuvwxyz";
        String digitPatter = "0123456789";
        String chars = ResourceUIUtils.buildStringFromPattern(charsPattern, charsPatternLength);
        String digits = ResourceUIUtils.buildStringFromPattern(digitPatter, digitsPatternLength);
        return chars + digits;
    }

    private String generateValidMacId() {
        final int digitsPatternLength = 5;
        String digitPatter = "0123456789";
        String digits = ResourceUIUtils.buildStringFromPattern(digitPatter, digitsPatternLength);
        return "m" + digits;
    }

    private User createNewUserUsingAPI() {
        UserRoleEnum role = UserRoleEnum.DESIGNER;
        String userId = generateValidUserId();
        User userByEnv = new User(generateValidUserId(), generateValidUserId(), userId, generateValidUserId() + "@intl.sdc.com", role.name(), null);
        User adminUser = getUserByEnv(UserRoleEnum.ADMIN);
        try {
            RestResponse createUserResp = UserRestUtils.createUser(userByEnv, adminUser);
            Assert.assertEquals(createUserResp.getErrorCode().intValue(), HttpStatus.SC_CREATED, "Did not succeed to create a new user using API.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userByEnv;
    }


    private User getUserByEnv(UserRoleEnum userRole) {
        try {
            return getUser(userRole);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.ADMIN;
    }

}
