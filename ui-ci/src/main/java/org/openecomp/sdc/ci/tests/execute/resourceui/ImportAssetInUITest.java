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

package org.openecomp.sdc.ci.tests.execute.resourceui;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.CreateAndImportButtonsEnum;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.StepsEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ImportAssetUIUtils;
import org.openecomp.sdc.ci.tests.utilities.PropertiesUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ImportAssetInUITest extends SetupCDTest {

	private ResourceReqDetails resourceDetails;

	@BeforeMethod(alwaysRun = true)
	public void inializeBeforeImportTest() {
		GeneralUIUtils.fileName = "JDM_vfc.yml";
		resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VFC.toString());
	}

	@Test
	public void importAssetFillGeneralInfoAndSelectIconTest() throws Exception {
		ResourceUIUtils.importFileWithSendKey(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				CreateAndImportButtonsEnum.IMPORT_CP);
		ResourceUIUtils.fillGeneralInfoValuesAndIcon(resourceDetails, getUser());
		GeneralUIUtils.checkIn();
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
		ResourceUIUtils.getVFCGeneralInfoAndValidate(resourceDetails, getUser());
	}

	@Test
	public void changeImportedAssetFileTest() throws Exception {
		ImportAssetUIUtils.importAsssetAndFillGeneralInfo(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		String firstFileName = GeneralUIUtils.getWebElementWaitForVisible("filename").getText();
		AssertJUnit.assertTrue(firstFileName.equals(GeneralUIUtils.fileName));
		String secondFileName = "Valid_tosca_ReplaceTest.yml";
		ResourceUIUtils.importFileWithSendKeyBrowse(GeneralUIUtils.FILE_PATH, secondFileName);
		String secondFileNameFromField = GeneralUIUtils.getWebElementWaitForVisible("filename").getText();
		// assertThat(fileName, not(secondFileNameFromField));
		assertNotEquals(GeneralUIUtils.fileName, secondFileNameFromField);
	}

	@Test
	public void duplicateFileTest() throws Exception {
		ResourceUIUtils.importFileWithSendKey(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				CreateAndImportButtonsEnum.IMPORT_CP);
		ResourceUIUtils.fillGeneralInfoValuesAndIcon(resourceDetails, getUser());
		String nameofresource = resourceDetails.getName();
		resourceDetails.setName(getRandomComponentName("SecondImportCDTest"));
		GeneralUIUtils.checkIn();
		ImportAssetUIUtils.importAsssetAndFillGeneralInfo(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.clickSaveIcon();
		equals(ResourceUIUtils.getErrorMessageText("w-sdc-modal-body-content") == GeneralUIUtils.allReadyExistErro);
		GeneralUIUtils.clickOkButton();
		resourceDetails.setName(nameofresource);

	}

	@Test
	public void importInvalidFileTest() throws Exception {
		GeneralUIUtils.fileName = "InValid_tosca_File .yml";
		ImportAssetUIUtils.importAsssetAndFillGeneralInfo(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.clickSaveIcon();
		equals(ResourceUIUtils.getErrorMessageText("w-sdc-modal-body-content") == GeneralUIUtils.toscaErrorMessage);
		GeneralUIUtils.clickOkButton();
	}
	
	@Test
	public void deleteImportAssetFileTest() throws Exception {
		ImportAssetUIUtils.importAsssetAndFillGeneralInfo(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		WebElement deleteFileButton = GeneralUIUtils.getDriver()
				.findElement(By.className("i-sdc-form-file-upload-x-btn"));
		deleteFileButton.click();
		WebElement filefield = GeneralUIUtils.getWebElementWaitForVisible("filename");
		AssertJUnit.assertEquals("", filefield.getText());
	}

	// Add artifact by Clicking the Place holders button.
	@Test
	public void importAssetAddInformationArtifactPlaceHoldersTest() throws Exception {
		// fileName = "CP_WAN.yml";
		String artifactByname = "placeHolder";
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.moveToStep(StepsEnum.INFORMATION_ARTIFACT);
		GeneralUIUtils.getWebElementWaitForVisible("Features");
		List<WebElement> Placholders = GeneralUIUtils.getDriver().findElements(By.className("add-button"));
		for (WebElement element : Placholders) {
			Thread.sleep(500);
			element.click();
			// Placholders.get(4).click();
			System.out.println(element.getText());
			if (element.getText().equalsIgnoreCase("Add Other Artifact")) {
				ArtifactUIUtils.valideArtifact(ArtifactUIUtils.addInformationalArtifact("Create New Artifact"), false);
			} else {
				ArtifactUIUtils.valideArtifact(ArtifactUIUtils.addInformationalArtifact(null), false);
			}
		}
	}

	// Add information artifact by Clicking the Add button.
	@Test
	public void importAssetAddInformationArtifactAddButtonTest() throws Exception {
		String type = "Create New Artifact";
		// fileName = "CP_WAN.yml";
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.moveToStep(StepsEnum.INFORMATION_ARTIFACT);
		GeneralUIUtils.actionBuild(GeneralUIUtils.getWebButton("add-information-artifact-button")).click();
		;
		;
		Map<String, String> expected = ArtifactUIUtils.addInformationalArtifact(type);
		Thread.sleep(1000);
		ResourceUIUtils.scrollDownPage();
		ArtifactUIUtils.valideArtifact(expected, false);
	}

	// Add New property String Type.
	@Test
	public void importAssetAddStringPropertyTest() throws Exception {
		WebElement prop = null;
		// fileName = "CP_WAN.yml";
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		Thread.sleep(500);
		GeneralUIUtils.moveToStep(StepsEnum.PROPERTIES);
		GeneralUIUtils.getEelementBycontainsClassName("data-row");
		GeneralUIUtils.getWebButton("addGrey").click();
		Map<String, String> propertyValues = PropertiesUIUtils.addProperties("String-Property", "string",
				"!This is strig123456@#$%$", "This is description.", null);
		WebElement elementTohover = GeneralUIUtils.getEelementBycontainsClassName("table-arrow");
		GeneralUIUtils.actionBuild(elementTohover).click();
		int counter = 0;
		try {
			prop = GeneralUIUtils.getWebElementWaitForVisible(propertyValues.get("name"));
		} catch (Exception e) {
			ResourceUIUtils.scrollDownPage();
			prop = GeneralUIUtils.getDriver()
					.findElement(By.xpath("//*[@data-tests-id='" + propertyValues.get("name") + "']"));
		} finally {
			if (prop.isDisplayed()) {
				counter++;
			}
		}
		PropertiesUIUtils.vlidateProperties(propertyValues);
	}

	// Add New property Integer Type.
	@Test
	public void importAssetAddIntegerPropertyTest() throws Exception {
		WebElement prop;
		// fileName = "CP_WAN.yml";
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		Thread.sleep(500);
		GeneralUIUtils.moveToStep(StepsEnum.PROPERTIES);
		GeneralUIUtils.getEelementBycontainsClassName("data-row");
		GeneralUIUtils.getWebButton("addGrey").click();
		Map<String, String> propertyValues = PropertiesUIUtils.addProperties("Integer-Property", "integer", "123456",
				"This is description.", null);
		WebElement elementTohover = GeneralUIUtils.getEelementBycontainsClassName("table-arrow");
		GeneralUIUtils.actionBuild(elementTohover);
		int counter = 0;
		prop = GeneralUIUtils.getWebElementWaitForVisible(propertyValues.get("name"));
		if (prop.isDisplayed()) {
			counter++;
		}
		if (counter == 0) {
			ResourceUIUtils.scrollDownPage();
			prop = GeneralUIUtils.getDriver()
					.findElement(By.xpath("//*[@data-tests-id='" + propertyValues.get("name") + "']"));
			counter++;
		}
		PropertiesUIUtils.vlidateProperties(propertyValues);
	}

	// Add New property boolean Type.
	@Test
	public void importAssetAddBooleanPropertyTest() throws Exception {
		// fileName = "CP_WAN.yml";
		WebElement prop;
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.moveToStep(StepsEnum.PROPERTIES);
		GeneralUIUtils.getEelementBycontainsClassName("data-row");
		GeneralUIUtils.getWebButton("addGrey").click();
		Map<String, String> propertyValues = PropertiesUIUtils.addProperties("Boolean-Property", "boolean", "true",
				"This is boolean description.", null);
		int counter = 0;
		WebElement elementTohover = GeneralUIUtils.getDriver().findElement(By.className("table-arrow"));
		GeneralUIUtils.actionBuild(elementTohover);
		prop = GeneralUIUtils.getDriver()
				.findElement(By.xpath("//*[@data-tests-id='" + propertyValues.get("name") + "']"));
		if (prop.isDisplayed()) {
			counter++;
		}
		if (counter == 0) {
			ResourceUIUtils.scrollDownPage();
			prop = GeneralUIUtils.getDriver()
					.findElement(By.xpath("//*[@data-tests-id='" + propertyValues.get("name") + "']"));
			counter++;
		}
		AssertJUnit.assertEquals(1, counter);
	}

	// Add New property float Type.
	@Test
	public void importAssetAddFloatPropertyTest() throws Exception {
		WebElement prop;
		// fileName = "CP_WAN.yml";
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.moveToStep(StepsEnum.PROPERTIES);
		GeneralUIUtils.getEelementBycontainsClassName("data-row");
		GeneralUIUtils.getWebButton("addGrey").click();
		Map<String, String> propertyValues = PropertiesUIUtils.addProperties("floatProperty", "float", "22.5",
				"This is description.", null);
		int counter = 0;
		WebElement elementTohover = GeneralUIUtils.getDriver().findElement(By.className("table-arrow"));
		GeneralUIUtils.actionBuild(elementTohover);
		prop = GeneralUIUtils.getDriver()
				.findElement(By.xpath("//*[@data-tests-id='" + propertyValues.get("name") + "']"));
		if (prop.isDisplayed()) {
			counter++;
		}
		if (counter == 0) {
			ResourceUIUtils.scrollDownPage();
			prop = GeneralUIUtils.getDriver()
					.findElement(By.xpath("//*[@data-tests-id='" + propertyValues.get("name") + "']"));
			counter++;
		}
		PropertiesUIUtils.vlidateProperties(propertyValues);
	}

	// Edit property.
	@Test
	public void importAssetEditPropertiesTest() throws Exception {
		// fileName = "CP_WAN.yml";
		Map<String, String> expected = null;
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.moveToStep(StepsEnum.PROPERTIES);
		GeneralUIUtils.getEelementBycontainsClassName("table-col-general");
		GeneralUIUtils.getWebButton("addGrey").click();
		expected = PropertiesUIUtils.addProperties("BooleanProperty", "boolean", "true", "This is boolean description.",
				null);
		GeneralUIUtils.getWebElement(GeneralUIUtils.getDriver(), "table-edit-btn").click();
		;
		List<WebElement> properties = GeneralUIUtils.getDriver().findElements(By.xpath("//*[@*='table-edit-btn']"));
		;
		for (WebElement webElement : properties) {
			webElement.click();
			GeneralUIUtils.defineDescription("This is Property update");
			GeneralUIUtils.getWebButton("Update").click();
			break;
		}
		Thread.sleep(2000);
		GeneralUIUtils.getDriver().findElement(By.xpath("//*[@data-tests-id='BooleanProperty']")).click();
		Thread.sleep(1000);
		String actual = GeneralUIUtils.getDriver()
				.findElement(By.xpath("//*[@class='item-opened ng-binding ng-scope']")).getText();
		assertNotEquals(expected.get("name"), actual);
		// assertThat(expected.get("name"),not(actual));
	}

	// **************************************************************************************************
	// change VFC version

	// this test return error 500;
	@Test(alwaysRun = false)
	public void importAssetChangeVersionOfVFCTest() throws Exception {
		// fileName = "VFC.yml";
		// resourceDetails.setResourceType(ResourceTypeEnum.VFC.toString());
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.checkinCheckout(resourceDetails.getName());
		resourceDetails.setVersion("0.2");
		String version = GeneralUIUtils.getWebElementWaitForVisible("versionHeader").getText();
		version.equals("0.2");
	}

	// change VL version
	// this test return error 500;
	@Test
	public void importAssetChangeVersionOfVLTest() throws Exception {
		GeneralUIUtils.fileName = "VL.yml";
		resourceDetails.setResourceType(ResourceTypeEnum.VL.getValue());
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.checkinCheckout(resourceDetails.getName());
		resourceDetails.setVersion("0.2");
		String version = GeneralUIUtils.getWebElementWaitForVisible("versionHeader").getText();
		version.equals("0.2");
	}
	// change CP version

	@Test
	public void importAssetChangeVersionOfCPTest() throws Exception {
		GeneralUIUtils.fileName = "CP_LAN.yml";
		resourceDetails.setResourceType(ResourceTypeEnum.CP.toString());
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.checkinCheckout(resourceDetails.getName());
		resourceDetails.setVersion("0.2");
		String version = GeneralUIUtils.getWebElementWaitForVisible("versionHeader").getText();
		version.equals("0.2");
	}

	// @Test
	// public void viewPageActivityLogTest() throws Exception {
	// resourceDetails.setResourceType(ResourceTypeEnum.VF.toString());
	// ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH,
	// GeneralUIUtils.fileName , resourceDetails, getUser(),
	// CreateAndImportButtonsEnum.IMPORT_CP);
	// ResourceUIUtils.waitToFinishButtonEnabled().click();
	// RestCDUtils.getResource(resourceDetails, getUser());
	// GeneralUIUtils.checkIn();
	// GeneralUIUtils.openObjectMenuAndSelectOption(resourceDetails.getUniqueId(),
	// MenuOptionsEnum.VIEW);
	// ResourceUIUtils.lifeCycleState();
	// Thread.sleep(1000);
	// ViewPageUIUtils.validateActivityLog("Action: Checkin Performed by: Carlos
	// Santana(cs0008) Status: 200");
	// }

	// @Test
	// public void downloadArtifactViewPaage() throws Exception {
	// importAssetAddInformationArtifactAddButton();
	// ResourceUIUtils.waitToFinishButtonEnabled().click();
	// ViewPageUIUtils.openDropDownListOfObject(resourceDetails.getName(),
	// ImportAssetUIUtils.getAllObjectsOnWorkspace(GeneralUIUtils.getDriver()),
	// "View",
	// ImportAssetUIUtils.scrollElement(GeneralUIUtils.getDriver()));
	// ValidateViewPageParameters.ViewPagedownloadArtifact();
	// String myheatfile="Heat-File.yaml";
	// //Assert.assertTrue((GeneralUIUtils.FILE_PATH, "mailmerge.xls"), "Failed
	// to download
	// Expected document");
	// String dowloadedfile=
	// "C:\\Git_work\\ASDC\\d2-sdnc\\ui-ci\\src\\main\\resources\\Downloads\\"+fileName+"";
	// int index=dowloadedfile.lastIndexOf("\\");
	// System.out.println(dowloadedfile.substring(index+1));
	// File getLatestFile = getLatestFilefromDir();
	// String fileName = getLatestFile.getName();
	// Assert.assertTrue(fileName.equals("mailmerge.xls"), "Downloaded file name
	// is not matching with expected file name");
	//
	// }

	@Test
	public void importAssetcheckInVFCTest() throws Exception {
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.checkIn();
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
		assertTrue(ResourceUIUtils.lifeCycleStateUI().contentEquals(LifeCycleStateEnum.CHECKIN.getValue()));
	}

	@Test
	public void importAssetcheckOutVFCTest() throws Exception {
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.clickSaveIcon();
		GeneralUIUtils.checkinCheckout(resourceDetails.getName());
		GeneralUIUtils.clickASDCLogo();
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
		assertTrue(ResourceUIUtils.lifeCycleStateUI().contentEquals(LifeCycleStateEnum.CHECKOUT.getValue()));
	}

	@Test
	public void importAssetDeleteVFCVersionTest() throws Exception {
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		GeneralUIUtils.checkinCheckout(resourceDetails.getName());
		GeneralUIUtils.deleteVersionInUI();
		Thread.sleep(1000);
		GeneralUIUtils.getWebElementWaitForVisible(resourceDetails.getName()).click();
		String version = GeneralUIUtils.getWebElementWaitForVisible("versionHeader").getText();
		AssertJUnit.assertEquals("0.1", version.replace("V", ""));
	}

	// @Test(enabled = false)
	// public void importAssetVFCPrintScreenTest() throws Exception {
	// ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH,
	// GeneralUIUtils.fileName , resourceDetails, getUser());
	// ResourceUIUtils.waitToFinishButtonEnabled().click();
	// ResourceUIUtils.openDropDownListOfObject(resourceDetails.getName(),
	// ResourceUIUtils.getAllObjectsOnWorkspace(driver,resourceDetails), "Edit",
	// ResourceUIUtils.scrollElement(driver));
	// ResourceUIUtils.waitfunctionforelements("sprite-resource-icons", 7);
	// Thread.sleep(2000);
	// WebElement element =
	// GeneralUIUtils.getDriver().findElement(By.className("network"));
	// WebElement target =
	// GeneralUIUtils.getDriver().findElement(By.className("dropzone"));
	// (new Actions(GeneralUIUtils.getDriver())).dragAndDrop(element,
	// target).perform();
	// WebElement element1 =
	// GeneralUIUtils.getDriver().findElement(By.className("network"));
	// (new Actions(GeneralUIUtils.getDriver())).dragAndDrop(element1,
	// target).perform();
	// ResourceUIUtils.clickPrintScreen();
	//
	// // the firefox not support print screen.
	// }

	@Test
	public void importAssetVFCSubmitForTestingTest() throws Exception {
		ImportAssetUIUtils.importAsssetFillGeneralInfoAndSelectIcon(GeneralUIUtils.FILE_PATH, GeneralUIUtils.fileName,
				resourceDetails, getUser(), CreateAndImportButtonsEnum.IMPORT_CP);
		String name = "";
		GeneralUIUtils.clickSubmitForTest();
		Thread.sleep(2000);
		String url = "http://localhost:8181/sdc1/proxy-tester1#/dashboard";
		navigateToUrl(url);
		GeneralUIUtils.getWebElementWaitForVisible("w-sdc-dashboard-card-info");
		int counter = 0;
		for (WebElement object : ResourceUIUtils.getAllObjectsOnWorkspace(GeneralUIUtils.getDriver(),
				resourceDetails)) {
			if (object.getText().equals(resourceDetails.getName())) {
				name = object.getText();
				counter++;
			}
		}
		AssertJUnit.assertEquals(1, counter);

	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
