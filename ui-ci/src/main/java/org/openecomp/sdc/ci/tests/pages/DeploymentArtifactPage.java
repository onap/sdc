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

import com.aventstack.extentreports.Status;
import org.apache.commons.lang.WordUtils;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.collections.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DeploymentArtifactPage extends GeneralPageElements {

	public DeploymentArtifactPage() {
		super();
	}

	public static ResourceLeftMenu getLeftPanel() {
		return new ResourceLeftMenu();
	}

	public static UploadArtifactPopup artifactPopup() {
		return new UploadArtifactPopup();
	}

	protected static void addNewArtifact(ArtifactGroupTypeEnum artifactGroupType) {
		switch (artifactGroupType) {
			case DEPLOYMENT:
			GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.ADD_DEPLOYMENT_ARTIFACT.getValue()).click();
				break;
			case INFORMATIONAL:
			GeneralUIUtils.getInputElement(DataTestIdEnum.ArtifactPageEnum.ADD_INFORMATIONAL_ARTIFACT.getValue()).click();
				break;
			default:
				break;
			}
	}

	public static void clickAddNewArtifact() {
		addNewArtifact(ArtifactGroupTypeEnum.DEPLOYMENT);
	}

	public static void clickAddAnotherArtifact() {
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.ADD_ANOTHER_ARTIFACT.getValue()).click();
	}

	public static void clickEditArtifact(String artifactLabel) {
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.EDIT_ARTIFACT.getValue() + artifactLabel).click();
	}
	
	public static void clickEditEnvArtifact(String artifactLabel) {
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.EDIT_PARAMETERS_OF_ARTIFACT.getValue() + artifactLabel).click();
	}

	public static void clickDeleteArtifact(String artifactLabel) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Deleting %s Artefact ",artifactLabel));
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.DELETE_ARTIFACT.getValue() + artifactLabel).click();
	}

	public static WebElement clickDownloadArtifact(String artifactLabel) {
		WebElement downloadButton = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT.getValue() + artifactLabel);

		return downloadButton;
	}
	
	public static void clickDownloadEnvArtifact(String envFileNameToDownload) {
		ExtentTestActions.log(Status.INFO, String.format("Downloading the updated  %s artifact for validate parameters with the response after the update...", envFileNameToDownload));
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ArtifactPageEnum.DOWNLOAD_ARTIFACT_ENV.getValue() + envFileNameToDownload);
		ExtentTestActions.log(Status.INFO, String.format("%s Envartifact was downloaded successfully!", envFileNameToDownload));
	}
	
	public static  void clickSaveEnvParameters() {
		 GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPopup.SAVE.getValue()).click();
		 GeneralUIUtils.ultimateWait();
	}
	
	public static WebElement getAddOtherArtifactButton(){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.ADD_ANOTHER_ARTIFACT.getValue());
	}
	
	public static void clickOK(){
		SetupCDTest.getExtendTest().log(Status.INFO, "Artifact Page, Clicking OK");
		GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.OK.getValue()).click();
		GeneralUIUtils.getWebElementBy(By.className("flex-container"));
		GeneralUIUtils.waitForAngular();
	}

	public static String getArtifactDescription(String artifactLabel) throws Exception {
		clickOnArtifactDescription(artifactLabel); // open artifact
		WebElement artifactDescriptionElement = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.GET_DEPLOYMENT_ARTIFACT_DESCRIPTION.getValue());
		String artifactDesc = artifactDescriptionElement.getAttribute("value");
		closeArtifactDescription(artifactLabel); // close artifact
		return artifactDesc;
	}

	public static void closeArtifactDescription(String artifactLabel) {
		GeneralUIUtils.clickOnElementByTestId("popover-x-button");
	}

	public static WebElement clickOnArtifactDescription(String artifactLabel) throws Exception {
		try{
			WebElement artifact = GeneralUIUtils.getWebElementByTestID("descriptionIcon_" + artifactLabel);
			artifact.click();
			GeneralUIUtils.waitForLoader();
			return artifact;
		}
		catch(Exception e){
			throw new Exception("Artifact " + artifactLabel + "is not found");
		}
	}
	
	public static boolean checkElementsCountInTable(int expectedElementsCount) {
		return GeneralPageElements.checkElementsCountInTable(expectedElementsCount + 1);
	}
	
	public static String[] verifyArtifactsExistInTable(String filepath, String vnfFile) throws Exception{
		String[] artifactNamesFromZipFile = FileHandling.getArtifactsFromZip(filepath, vnfFile);
		return verifyArtifactsExistInTable(artifactNamesFromZipFile);
	}
	
	public static String[] verifyArtifactsExistInTable(String[] artifactNamesFromZipFile) throws Exception{
		if (artifactNamesFromZipFile != null){
			checkArtifactsDisplayed(artifactNamesFromZipFile);
			checkEnvArtifactsDisplayed();
		}
		
		return artifactNamesFromZipFile;
	}

	public static void checkArtifactsDisplayed(String[] artifactsFromZipFile) throws Exception {
		SetupCDTest.getExtendTest().log(Status.INFO, "Verifying the artifacts in the table");
		List<String> artifactList = Lists.newArrayList(artifactsFromZipFile).stream().filter(p -> !p.contains(".env")).map(p -> getVisualArtifactName(p)).collect(Collectors.toList());
		try{
//			List<WebElement> rows = GeneralUIUtils.getElementsByCSS("div div[data-tests-id^='artifact-item'] span.ng-binding:nth-of-type(2)");
			List<WebElement> rows = GeneralUIUtils.getElementsByCSS("div div[data-tests-id^='artifact-item'] span[data-tests-id^='artifactDisplayName']");
			for (WebElement r : rows){
				String artifactDisplayed = r.getAttribute("textContent").trim();
				if (artifactList.contains(artifactDisplayed)){
					artifactList.remove(artifactDisplayed);
				}
				else if (artifactDisplayed.toLowerCase().contains("license")){
					artifactList.add(artifactDisplayed);
				}
			}
			checkLicenseArtifactsDisplayed(artifactList);
		}
		catch(Exception e){
			throw new Exception("Table problem");
		}
		
		
		if (!artifactList.isEmpty()){
			throw new Exception(String.format("missing the following artifact(s) : %s", artifactList.toString()));
		}
	}

	public static void checkEnvArtifactsDisplayed() throws Exception {
		List<WebElement> envRows;
		List<WebElement> heatRows;
		List<WebElement> heatNetRows;
		List<WebElement> heatVolRows;
		int envArtifactsSize = 0;
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Verifying the HEAT_ENV artifacts in the table");
		
		try{
			envRows = GeneralUIUtils.getElementsByCSS("div div[data-tests-id='HEAT_ENV']");
			
			heatRows = GeneralUIUtils.getElementsByCSS("div div[tooltip-content='HEAT']");
			heatNetRows = GeneralUIUtils.getElementsByCSS("div div[tooltip-content='HEAT_NET']");
			heatVolRows = GeneralUIUtils.getElementsByCSS("div div[tooltip-content='HEAT_VOL']");
			
			envArtifactsSize = heatRows.size() + heatNetRows.size() + heatVolRows.size();
		}
		catch(Exception e){
			throw new Exception("Table problem");
		}
		
		if (envArtifactsSize !=envRows.size()){
			throw new Exception(String.format("some env artifacts are missing... there is %s instead of %s", envRows.size(), envArtifactsSize));	
		}
		
	}

	public static void checkLicenseArtifactsDisplayed(List<String> rowsFromTable) throws Exception {
		SetupCDTest.getExtendTest().log(Status.INFO, "Verifying the license artifacts in the table");
		String vfLicense =   getPreparedLicense(ArtifactTypeEnum.VF_LICENSE.getType());
		String[] split = vfLicense.split(" ");
		vfLicense = vfLicense.replaceAll(split[0], split[0].toUpperCase());
		if (rowsFromTable.contains(vfLicense)){
			rowsFromTable.remove(vfLicense);
		}
		
		String vendorLicense = getPreparedLicense(ArtifactTypeEnum.VENDOR_LICENSE.getType());
		if (rowsFromTable.contains(vendorLicense)){ 
			rowsFromTable.remove(vendorLicense);
		}

	}

	public static String getPreparedLicense(String license) {
		return WordUtils.capitalizeFully(license.replaceAll("_", " "));
	}
	

	private static String getVisualArtifactName(String artifactName) {
		if (artifactName.contains(".")){
			return artifactName.substring(0, artifactName.lastIndexOf("."));
		}
		return artifactName;
	}
	
	public static void updateDescription(String newDescription, ArtifactInfo artefact) throws Exception{
		UploadArtifactPopup artifactPopup = new UploadArtifactPopup(true);
		DeploymentArtifactPage.clickEditArtifact(artefact.getArtifactLabel());
		artifactPopup.insertDescription(newDescription);
		artifactPopup.clickDoneButton();
	}
	
	public static List<String> getDeploymentArtifactsNamesWorkSpace() {
		return GeneralUIUtils.getWebElementListText(GeneralUIUtils.getWebElementsListByContainTestID(DataTestIdEnum.ArtifactPageEnum.ARTIFACT_NAME.getValue()));
	}
	
	//Get Artifact Type by Artifact Name.
	public static String getArtifactType(String artifactName){
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ArtifactPageEnum.TYPE.getValue()+artifactName).getText();
	}
	
	public static List<String> getHeatParametersInUI(String dataTestId){
		List<WebElement>elements;
		List<String>Names=new ArrayList<>();
		elements=GeneralUIUtils.getWebElementsListByContainTestID(dataTestId);
				for (WebElement webElement : elements) {
					String attributevalue=webElement.getAttribute("data-tests-id");
					Names.add(attributevalue.replace("heatParameterName_", ""));
				}
		return Names;
	}
	
	public static void searchBoxEnv(String parameterName) {
		GeneralUIUtils.getWebElementByContainsClassName("w-sdc-env-search-input").sendKeys(parameterName);
	}
	
	public static void clearSearchBoxEnv() {
		GeneralUIUtils.getWebElementByContainsClassName("w-sdc-env-search-input").clear();
	}

}
