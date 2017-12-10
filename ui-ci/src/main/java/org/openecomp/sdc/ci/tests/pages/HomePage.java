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

import java.io.File;
import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.utilities.DownloadManager;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage extends GeneralPageElements {

	public HomePage() {
		super();
	}
	
	public static void showVspRepository(){
		GeneralUIUtils.waitForElementInVisibilityBy(By.className("ui-notification"), 30);
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.MainMenuButtons.REPOSITORY_ICON.getValue());
	}
	
	public static boolean searchForVSP(String vspName){
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ImportVfRepository.SEARCH.getValue()).sendKeys(vspName);
		GeneralUIUtils.ultimateWait();
//		return checkElementsCountInTable(2);
		return true;
	}
	
	public static void importVSP(String vspName){
		HomePage.showVspRepository();
		boolean vspFound = HomePage.searchForVSP(vspName);
		if (vspFound){
			List<WebElement> elemenetsFromTable = getElemenetsFromTable();
//			GeneralUIUtils.waitForLoader();
			WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 30);
			WebElement findElement = wait.until(ExpectedConditions.visibilityOf(elemenetsFromTable.get(1)));
			findElement.click();
			GeneralUIUtils.waitForLoader();
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.IMPORT_VSP.getValue());
        	GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.GeneralElementsEnum.CREATE_BUTTON.getValue());
        	GeneralUIUtils.waitForLoader(60*10);
        	GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.CHECKIN_BUTTON.getValue());
		}
	}
				
			
	public static boolean navigateToHomePage() {
		try{
			System.out.println("Searching for reporsitory icon.");
			WebElement repositoryIcon = GeneralUIUtils.getInputElement("repository-icon");
			if (repositoryIcon != null){
				return true;
			}
			else{
				GeneralUIUtils.ultimateWait();
				List<WebElement> homeButtons = GeneralUIUtils.getElemenetsFromTable(By.xpath("//a[contains(.,'HOME')]"));
				if (homeButtons.size() != 0){
					for (WebElement home : homeButtons){
						if (home.isDisplayed()){
							home.click();
							System.out.println("Clicked on home button");
							break;
						}
					}
					GeneralUIUtils.closeErrorMessage();
					WebElement homeButton = GeneralUIUtils.getInputElement(DataTestIdEnum.MainMenuButtons.HOME_BUTTON.getValue());
					return homeButton.isDisplayed();
				}
	
			}
		}
		catch(Exception innerException){
			System.out.println("Can't click on home button.");
			return false;
		}
		return false;
	}

	public static File downloadVspCsarToDefaultDirectory(String vspName) throws Exception {
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.MainMenuButtonsFromInsideFrame.HOME_BUTTON.getValue()).click();
		DownloadManager.downloadCsarByNameFromVSPRepository(vspName, "");
		File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
		return latestFilefromDir;
	}
	
}
