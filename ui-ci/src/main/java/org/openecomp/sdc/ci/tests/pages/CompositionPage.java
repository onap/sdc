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

import org.openecomp.sdc.ci.tests.datatypes.CanvasElement;
import org.openecomp.sdc.ci.tests.datatypes.CanvasManager;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import com.aventstack.extentreports.Status;

public class CompositionPage extends GeneralPageElements {

	public CompositionPage() {
		super();
	}
	
	public static UploadArtifactPopup artifactPopup() {
		return new UploadArtifactPopup(true);
	}

	public static void searchForElement(String elementName) {
		SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + elementName + " in the left panel");
		WebElement searchField = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionScreenEnum.SEARCH_ASSET.getValue());
		searchField.clear();
		searchField.sendKeys(elementName);
		GeneralUIUtils.ultimateWait();
	}
	
	public static void showDeploymentArtifactTab() throws Exception {
		clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum.DEPLOYMENT_ARTIFACT_TAB);
	}

	public static void showInformationsTab() throws Exception {
		clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum.INFORMATION_TAB);
	}
	
	public static void showPropertiesAndAttributesTab() throws Exception {
		clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum.PROPERTIES_AND_ATTRIBUTES_TAB);
	}

	public static List<WebElement> getProperties() {
		return PropertiesPage.getElemenetsFromTable();
	}
	
	public static List<WebElement> getDeploymentArtifacts() {
		return getAllAddedArtifacts();
	}
	
	public static List<WebElement> getAllAddedArtifacts() {
		String dataTestsId = DataTestIdEnum.CompositionScreenEnum.ARTIFACTS_LIST.getValue();
		return GeneralUIUtils.getWebElementsListBy(By.xpath("//*[contains(@data-tests-id,'" + dataTestsId + "')]"));
	}

	public static void moveToInputsScreen() throws Exception {
		OpenPagesMenu();
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CompositionScreenEnum.MENU_INPUTS.getValue());
		GeneralUIUtils.ultimateWait();
	}

	private static void OpenPagesMenu() {
		Actions actions = new Actions(GeneralUIUtils.getDriver());
		List<WebElement> triangleList = GeneralUIUtils.getWebElementsListByClassName(DataTestIdEnum.CompositionScreenEnum.MENU_TRIANGLE_DROPDOWN.getValue());
		WebElement pagesMenu = triangleList.get(2);
		actions.moveToElement(pagesMenu).perform();
	}

	public static void changeComponentVersion(CanvasManager canvasManager, CanvasElement element, String version) {
		try{
			SetupCDTest.getExtendTest().log(Status.INFO, String.format("Changing component version to  %s", version));
			canvasManager.clickOnCanvaElement(element);
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.CompositionScreenEnum.CHANGE_VERSION.getValue());
			GeneralUIUtils.ultimateWait();
			Select selectlist = new Select(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionScreenEnum.CHANGE_VERSION.getValue()));
			while (selectlist.getOptions().size() == 0) {
				selectlist = new Select(GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionScreenEnum.CHANGE_VERSION.getValue()));
			}
			GeneralUIUtils.ultimateWait();;
			selectlist.selectByValue(version);
			GeneralUIUtils.ultimateWait();
			GeneralUIUtils.clickSomewhereOnPage();
		}
		catch(Exception e){
			throw e;
		}
	}
	
	public static void clickAddArtifactButton() throws Exception{
		clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum.ADD_ARTIFACT);
		GeneralUIUtils.getWebElementByClassName("sdc-add-artifact");
	}
	
	public static String getSelectedInstanceName(){
		return GeneralUIUtils.getWebElementByTestID("selectedCompTitle").getText();
	}
	
	public static void showInformationArtifactTab() throws Exception {
		clickOnTab(DataTestIdEnum.CompositionScreenEnum.INFORMATION_ARTIFACTS);
	}
	
	public static void showAPIArtifactTab() throws Exception {
		clickOnTab(DataTestIdEnum.CompositionScreenEnum.API);
	}
	
	public static void showInformationTab() throws Exception {
		clickOnTab(DataTestIdEnum.CompositionScreenEnum.INFORMATION);
	}
	
	public static void showCompositionTab() throws Exception {
		clickOnTab(DataTestIdEnum.CompositionScreenEnum.COMPOSITION);
	}
	
	public static void showInputsTab() throws Exception {
		clickOnTab(DataTestIdEnum.CompositionScreenEnum.INPUTS);
	}
	
	public static void showRequirementsAndCapabilitiesTab() throws Exception {
		clickOnTab(DataTestIdEnum.CompositionScreenEnum.REQUIREMENTS_AND_CAPABILITIES);		
	}
	
	public static List<WebElement> getOpenTabTitle() throws Exception{
//		return GeneralUIUtils.getElementsByCSS("expand-collapse span");
		return GeneralUIUtils.getElementsByCSS("expand-collapse");
	}
	
	public static void clickOnTab(DataTestIdEnum.CompositionScreenEnum tabSelector) throws Exception{
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", tabSelector.name()));
		GeneralUIUtils.getElementsByCSS(tabSelector.getValue()).get(0).click();
		GeneralUIUtils.ultimateWait();
	}
	
	public static void clickOnTabTestID(DataTestIdEnum.CompositionScreenEnum tabSelector) throws Exception{
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on %s", tabSelector.name()));
		GeneralUIUtils.getWebElementByTestID(tabSelector.getValue()).click();
		GeneralUIUtils.ultimateWait();
	}
	
	public static CanvasElement addElementToCanvasScreen(LeftPanelCanvasItems elementName, CanvasManager vfCanvasManager) throws Exception{
		CompositionPage.searchForElement(elementName.name());
		return vfCanvasManager.createElementOnCanvas(elementName);
	}
	
	public static CanvasElement addElementToCanvasScreen(String elementName, CanvasManager vfCanvasManager) throws Exception{
		CompositionPage.searchForElement(elementName);
		return vfCanvasManager.createElementOnCanvas(elementName);
	}
	
	public static List<WebElement> getCompositionDeplymentArtifacts() {
		return GeneralUIUtils.getWebElementsListByContainTestID(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_DISPLAY_NAME.getValue());
		}
	public static WebElement getCustomizationUUID() throws Exception {
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.CompositionScreenEnum.CUSTOMIZATION_UUID.getValue());
	}
	
	
	public static List<WebElement> getCompositionEnvArtifacts(){
		return GeneralUIUtils.getWebElementsListByContainTestID(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ENV.getValue());
	}
	
	public static WebElement clickDownloadEnvArtifactComposition(String fileName) {
		 GeneralUIUtils.hoverOnAreaByTestId(DataTestIdEnum.DeploymentArtifactCompositionRightMenu.ARTIFACT_ENV.getValue() + fileName);
		 return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT_ENV.getValue() +fileName);
	}

	
	
	
	
	
	
}
