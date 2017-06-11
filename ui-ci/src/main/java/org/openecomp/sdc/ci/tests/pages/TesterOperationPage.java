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

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;

import com.aventstack.extentreports.Status;

public class TesterOperationPage {

	public TesterOperationPage() {
		super();
	}

	public static void certifyComponent(String componentName) throws Exception{
		clickStartTestingButton();
		clickAccpetCertificationButton(componentName);
	}

	public static void clickAccpetCertificationButton(String componentName) throws Exception {
		ExtentTestActions.log(Status.INFO, "Accepting certifiction of " + componentName);
		String actionDuration = GeneralUIUtils.getActionDuration(() ->
		{
			try {
				clickAccpetCertificationButtonWithoutDuration(componentName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		ExtentTestActions.log(Status.INFO, componentName + " is certifed", actionDuration);

	}
	
	public static void clickStartTestingButton() throws Exception{
		ExtentTestActions.log(Status.INFO, "Starting to test");
		String actionDuration = GeneralUIUtils.getActionDuration(() -> {
			try {
				clickStartTestingButtonWithoutDuration();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		ExtentTestActions.log(Status.INFO, "Ready for certification", actionDuration);
	}
	
	
	private static void certifyComponentWithoutDuration(String componentName) throws Exception {
		clickStartTestingButtonWithoutDuration();
		clickAccpetCertificationButtonWithoutDuration(componentName);
	}
	
	
	private static void clickAccpetCertificationButtonWithoutDuration(String componentName) throws Exception {
		try{
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.LifeCyleChangeButtons.ACCEPT.getValue());
			GeneralUIUtils.ultimateWait();
			GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ModalItems.ACCEP_TESTING_MESSAGE.getValue()).sendKeys(componentName + " tested successfuly");
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ModalItems.OK.getValue());
			GeneralUIUtils.ultimateWait();
			GeneralUIUtils.sleep(2000);
			GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtons.SEARCH_BOX.getValue());
		}
		catch (Exception e){
			throw new Exception("Accepting certification of " + componentName + " falied");
		}
	}
	
	private static void clickStartTestingButtonWithoutDuration() throws Exception {
		try{
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.LifeCyleChangeButtons.START_TESTING.getValue());
//			GeneralUIUtils.ultimateWait();
			GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.LifeCyleChangeButtons.ACCEPT.getValue());
//			GeneralUIUtils.ultimateWait();
//			GeneralUIUtils.sleep(1000);
		}
		catch (Exception e){
			throw new Exception("Start testing falied");
		}
	}

}
