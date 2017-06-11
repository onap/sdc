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

package org.openecomp.sdc.ci.tests.pages;

import java.util.List;
import java.util.function.Supplier;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

public class GeneralPageElements {

    public GeneralPageElements() {
                    super();
    }

    public static ResourceLeftMenu getLeftMenu() {
                    return new ResourceLeftMenu();
    }
                
    public static void clickOKButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the OK button");
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
        GeneralUIUtils.waitForLoader();
	}

    public static void clickCreateButton() {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the CREATE/UPDATE button.");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue()); 
        ExtentTestActions.log(Status.INFO, "Succeeded.");
    }
    
    public static void clickCreateButton(int timeout) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the CREATE/UPDATE button");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue(), timeout);
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
    }
    
	public static void clickUpdateButton(){
		clickCreateButton();
	}

    public static void clickCheckinButton(String componentName) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the CHECKIN button");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.ACCEP_TESTING_MESSAGE.getValue()).sendKeys("Checkin " + componentName);
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue(), 60);
        GeneralUIUtils.ultimateWait();
    }

    public static void clickSubmitForTestingButton(String componentName) throws Exception {
        SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on the submiting for testing button");
        GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.SUBMIT_FOR_TESTING_BUTTON.getValue());
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.SUMBIT_FOR_TESTING_MESSAGE.getValue()).sendKeys("Submit for testing " + componentName);
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
        GeneralUIUtils.ultimateWait();
        GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue(), 60);
        GeneralUIUtils.ultimateWait();;
    }
    
    public static void clickCheckoutButton() throws Exception{
    	SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on CHECKOUT button ...");
    	GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.CHECKOUT_BUTTON.getValue());
    	GeneralUIUtils.ultimateWait();
    }

	public static void clickDeleteVersionButton() throws Exception {
		SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on DELETE VERSION button ...");
		GeneralUIUtils.ultimateWait();
		GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.DELETE_VERSION_BUTTON.getValue());
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.OK.getValue());
		GeneralUIUtils.ultimateWait();
    }

	public static void clickRevertButton() throws Exception{
		SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on REVERT button ...");
		GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.REVERT_BUTTON.getValue());
    }

    public static String getLifeCycleState() {
        return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue()).getText();
    }

    public static void selectVersion(String version) {
        GeneralUIUtils.getSelectList(version, DataTestIdEnum.GeneralElementsEnum.VERSION_HEADER.getValue());
        GeneralUIUtils.ultimateWait();
    }

    public static List<WebElement> getElemenetsFromTable() {
	    GeneralUIUtils.ultimateWait();;
	    return GeneralUIUtils.getElemenetsFromTable(By.className("flex-container"));
    }

    public static boolean checkElementsCountInTable(int expectedElementsCount) {
        return checkElementsCountInTable(expectedElementsCount, () -> getElemenetsFromTable());
    }
                
                
    public static void clickTrashButtonAndConfirm() throws InterruptedException{
    	SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on TRASH button ...");
		GeneralUIUtils.clickOnAreaJS(DataTestIdEnum.GeneralElementsEnum.DELETE_VERSION_BUTTON.getValue());
		clickOKButton();
	}                
    
    public static boolean checkElementsCountInTable(int expectedElementsCount, Supplier<List<WebElement>> func) {
    	SetupCDTest.getExtendTest().log(Status.INFO, "Checking the number of elements in the table; should be " + (expectedElementsCount - 1));
//        int maxWaitingPeriodMS = 10*1000;
//        int napPeriodMS = 200;
//        int sumOfWaiting = 0;
//        List<WebElement> elements = null;
//        boolean isKeepWaiting = false;
//        while (!isKeepWaiting) {
//	        GeneralUIUtils.sleep(napPeriodMS);
//	        sumOfWaiting += napPeriodMS;
//	        elements = func.get();
//	        isKeepWaiting = (expectedElementsCount == elements.size());
//	        if (sumOfWaiting > maxWaitingPeriodMS)
//                return false;
//        }
    	GeneralUIUtils.ultimateWait();
        return true;
    }
    
    public static void clickDeleteFile() throws Exception{
    	SetupCDTest.getExtendTest().log(Status.INFO, "Clicking on delete file X-button ...");
    	GeneralUIUtils.clickOnAreaJS(GeneralUIUtils.getWebElementBy
    			(By.cssSelector("div[class='i-sdc-form-file-upload-x-btn']")));
    	GeneralUIUtils.ultimateWait();;
    }    

}
