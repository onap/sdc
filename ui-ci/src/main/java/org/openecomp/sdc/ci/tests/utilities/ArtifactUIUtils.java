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

package org.openecomp.sdc.ci.tests.utilities;

import static org.junit.Assert.assertEquals;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactInfo;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.InformationalArtifacts;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.pages.DeploymentArtifactPage;
import org.openecomp.sdc.ci.tests.pages.InformationalArtifactPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.thinkaurelius.titan.diskstorage.util.StaticArrayBuffer;

public final class ArtifactUIUtils {

	private ArtifactUIUtils() {
	}

	public static void fillAndAddNewArtifactParameters(ArtifactInfo artifactInfo) throws Exception {
		DeploymentArtifactPage.artifactPopup().loadFile(artifactInfo.getFilepath(), artifactInfo.getFilename());
		DeploymentArtifactPage.artifactPopup().insertDescription(artifactInfo.getDescription());
		DeploymentArtifactPage.artifactPopup().defineArtifactLabel(artifactInfo.getArtifactLabel());
		DeploymentArtifactPage.artifactPopup().selectArtifactType(artifactInfo.getArtifactType());
		DeploymentArtifactPage.artifactPopup().clickAddButton();
	}

	public static void fillPlaceHolderInformationalArtifact(DataTestIdEnum.InformationalArtifacts artifactLabel,
			String filepath, String filename, String description) throws Exception {
		GeneralUIUtils.getWebButton(artifactLabel.getValue()).click();
		InformationalArtifactPage.artifactPopup().loadFile(filepath, filename);
		InformationalArtifactPage.artifactPopup().insertDescription(description);
		InformationalArtifactPage.artifactPopup().clickAddButton();
	}

	public static RestResponse deploymentArtifactResourceInUI(ResourceReqDetails resource, User user,
			ArtifactReqDetails artifact, String file) throws Exception {
		Thread.sleep(1000);

		List<WebElement> listFormInput = GeneralUIUtils.getDriver()
				.findElements(By.className("i-sdc-designer-sidebar-tab"));
		WebElement addArtifactElement = listFormInput.get(2);
		addArtifactElement.click();

		WebElement addArtifact = GeneralUIUtils.getDriver()
				.findElement(By.className("i-sdc-designer-sidebar-section-content-item-artifact-details-name"));
		addArtifact.click();

		Thread.sleep(1000);
		WebElement descriptionProperty = GeneralUIUtils.getDriver().findElement(By.className("i-sdc-form-textarea"));
		descriptionProperty.clear();
		descriptionProperty.sendKeys(artifact.getDescription());

		WebElement uploadFile = GeneralUIUtils.getDriver().findElement(By.className("i-sdc-form-label-upload"));
		uploadFile.click();

		StringSelection sel = new StringSelection(file);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
		// System.out.println("selection" + sel);
		Thread.sleep(1000);

		Robot robot = new Robot();
		Thread.sleep(1000);

		Thread.sleep(2000);

		robot.keyPress(KeyEvent.VK_ENTER);

		// Release Enter
		robot.keyRelease(KeyEvent.VK_ENTER);

		// Press CTRL+V
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);

		// Release CTRL+V
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_V);
		Thread.sleep(1000);

		// Press Enter
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
		Thread.sleep(3000);

		WebElement clickDone = GeneralUIUtils.getDriver().findElement(By.className("w-sdc-form-action"));
		clickDone.click();

		Thread.sleep(3500);

		GeneralUIUtils.getDriver().findElement(By.cssSelector("button[data-ng-click^=save]")).click();

		RestResponse getResource = RestCDUtils.getResource(resource, user);
		assertEquals("Did not succeed to get resource after create", 200, getResource.getErrorCode().intValue());
		return getResource;
	}

	public static void addInformationArtifact(ArtifactReqDetails artifact, String filePath,
			final InformationalArtifacts dataTestEnum) throws Exception {
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.sleep(2000);
		GeneralUIUtils.getWebElementWaitForVisible(dataTestEnum.getValue()).click();

		final WebElement browseWebElement = GeneralUIUtils.retryMethodOnException(
				() -> GeneralUIUtils.getWebElementByDataTestId(DataTestIdEnum.ModalItems.BROWSE_BUTTON.getValue()));
		browseWebElement.sendKeys(filePath);

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.DESCRIPTION.getValue())
				.sendKeys(artifact.getDescription());
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.ADD.getValue()).click();

	}

	private static void addFileToWindowBrowse(String file) throws InterruptedException, AWTException {
		StringSelection sel = new StringSelection(file);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
		// System.out.println("selection" + sel);
		Thread.sleep(1000);

		Robot robot = new Robot();

		robot.keyPress(KeyEvent.VK_ENTER);

		// Release Enter
		robot.keyRelease(KeyEvent.VK_ENTER);

		// Press CTRL+V
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);

		// Release CTRL+V
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_V);
		Thread.sleep(1000);

		// Press Enter
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
		Thread.sleep(3000);
	}

	static WebElement ArtifactLabel;

	public static Map<String, String> addInformationalArtifact(String artifactLabel) throws Exception {
		String type = GeneralUIUtils.getSelectList(null, "artifacttype").getFirstSelectedOption().getText();
		Map<String, String> artifactValues = new HashMap<String, String>();
		String labelName = GeneralUIUtils.getSelectList(artifactLabel, "selectArtifact").getFirstSelectedOption()
				.getText();
		ArtifactLabel = GeneralUIUtils.getDriver().findElement(By.name("artifactLabel"));
		if (ArtifactLabel.getAttribute("value").equals("")) {
			labelName = "New-Test-Artifact";
			ArtifactLabel.sendKeys(labelName);
			type = GeneralUIUtils.getSelectList("HEAT", "artifacttype").getFirstSelectedOption().getText();
		}
		String description = "This is Description";
		String fileName = "Heat-File.yaml";
		GeneralUIUtils.defineDescription(description);
		ResourceUIUtils.importFileWithSendKeyBrowse(GeneralUIUtils.FILE_PATH, fileName);
		GeneralUIUtils.getWebButton("Add").click();
		GeneralUIUtils.waitFordataTestIdVisibility(labelName);

		artifactValues.put("type", type);
		artifactValues.put("description", description);
		artifactValues.put("name", labelName);
		artifactValues.put("fileName", fileName);
		return artifactValues;
	}

	public static Map<String, String> addDeploymentArtifact(String artifactLabel, String artifactType, String fileName)
			throws Exception {
		String type = null;
		String labelName;
		Map<String, String> artifactValues = new HashMap<String, String>();
		try {
			labelName = GeneralUIUtils.getSelectList(artifactLabel, "selectArtifact").getOptions().get(1).getText();
			GeneralUIUtils.getSelectList(artifactLabel, "selectArtifact").selectByVisibleText(labelName);
		} catch (Exception e) {
			labelName = GeneralUIUtils.getWebElementByName(artifactLabel).getText();
		}
		ArtifactLabel = GeneralUIUtils.getDriver().findElement(By.name("artifactLabel"));
		if (ArtifactLabel.getText().equals("")) {
			labelName = "New-Test-Artifact";
			ArtifactLabel.sendKeys(labelName);
			type = GeneralUIUtils.getSelectList(artifactType, "artifacttype").getFirstSelectedOption().getText();
		}
		String description = "This is Description";
		GeneralUIUtils.defineDescription(description);
		ResourceUIUtils.importFileWithSendKeyBrowse(GeneralUIUtils.FILE_PATH, fileName);
		try {
			GeneralUIUtils.getWebButton("Add").click();
		} catch (Exception e) {
			GeneralUIUtils.getButtonByClassName("w-sdc-form-action add-property").click();
		}

		artifactValues.put("type", artifactType);
		artifactValues.put("description", description);
		artifactValues.put("name", labelName);
		artifactValues.put("fileName", fileName);
		return artifactValues;
	}

	public static Map<String, String> addDeploymentArtifactFromCanvas(String artifactLabel) throws Exception {
		String type = null;
		Map<String, String> artifactValues = new HashMap<String, String>();
		String labelName = GeneralUIUtils.getSelectList(artifactLabel, "selectArtifact").getFirstSelectedOption()
				.getText();
		ArtifactLabel = GeneralUIUtils.getDriver().findElement(By.name("artifactLabel"));
		if (ArtifactLabel.getText().equals("")) {
			labelName = "New-Test-Artifact";
			ArtifactLabel.sendKeys(labelName);
			type = GeneralUIUtils.getSelectList("OTHER", "artifacttype").getFirstSelectedOption().getText();
		}
		String description = "This is Description";
		String filePath = "C:\\Git_work\\ASDC\\d2-sdnc\\ui-ci\\src\\main\\resources\\Files\\";
		String fileName = "Heat-File.yaml";
		GeneralUIUtils.defineDescription(description);
		ResourceUIUtils.importFileWithSendKeyBrowse(filePath, fileName);
		GeneralUIUtils.getWebButton("Add").click();
		artifactValues.put("type", type);
		artifactValues.put("description", description);
		artifactValues.put("name", labelName);
		artifactValues.put("fileName", fileName);
		return artifactValues;
	}

	public static Map<String, String> valideArtifact(Map<String, String> artifactValues, Boolean condition)
			throws Exception {
		if (condition) {
			GeneralUIUtils.getEelementBycontainsClassName("table-edit-btn").click();
		} else {
			System.out.println(artifactValues.get("name"));
			GeneralUIUtils.getWebElementWaitForVisible("edit_" + artifactValues.get("name")).click();
		}
		Thread.sleep(1000);
		String labelname = GeneralUIUtils.getWebElementByName("artifactLabel").getAttribute("value");
		String filename = GeneralUIUtils.getWebElementWaitForVisible("filename").getText();
		String description = GeneralUIUtils.getWebElementWaitForVisible("description").getAttribute("value");
		String type = GeneralUIUtils.getSelectList(null, "artifacttype").getFirstSelectedOption().getText();
		labelname.compareToIgnoreCase(artifactValues.get("name").replaceAll("-", ""));
		assertEquals(filename, artifactValues.get("fileName").replaceAll(" ", "-"));
		assertEquals(type, artifactValues.get("type"));
		assertEquals(description, artifactValues.get("description"));
		GeneralUIUtils.getWebButton("Update").click();
		return artifactValues;
	}

	public static void valideArtifactFromCanvas(Map<String, String> artifactValues) throws Exception {
		GeneralUIUtils.getWebElementWaitForVisible("artifactDisplayName-" + artifactValues.get("name")).click();
		Thread.sleep(1000);
		String labelname = GeneralUIUtils.getWebElementByName("artifactLabel").getAttribute("value");
		String filename = GeneralUIUtils.getWebElementWaitForVisible("filename").getText();
		String description = GeneralUIUtils.getWebElementWaitForVisible("description").getAttribute("value");
		String type = GeneralUIUtils.getSelectList(null, "artifacttype").getFirstSelectedOption().getText();
		labelname.compareToIgnoreCase(artifactValues.get("name").replaceAll("-", ""));
		assertEquals(filename, artifactValues.get("fileName"));
		assertEquals(type, artifactValues.get("type"));
		assertEquals(description, artifactValues.get("description"));
	}

	public static Map<String, Map<String, Object>> getArtifactsListFromResponse(String jsonResponse,
			String fieldOfArtifactList) {
		JSONObject object = (JSONObject) JSONValue.parse(jsonResponse);
		Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) object.get(fieldOfArtifactList);
		return map;
	}

}
