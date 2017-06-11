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

import java.util.ArrayList;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

/**
 * @author al714h
 *
 */

public class ProductGeneralPage extends ResourceGeneralPage {

	public ProductGeneralPage() {
		super();
	}
	
	public static ProductLeftMenu getProductLeftMenu() {
        return new ProductLeftMenu();
    }

	public static void defineName(String productName) {
		WebElement productNameTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ProductMetadataEnum.PRODUCT_NAME.getValue());
		productNameTextbox.clear();
		productNameTextbox.sendKeys(productName);
	}

	public static void defineFullName(String productFullName) {
		WebElement productFullNameTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ProductMetadataEnum.FULL_NAME.getValue());
		productFullNameTextbox.clear();
		productFullNameTextbox.sendKeys(productFullName);
	}
	
	public static void defineProjectCode(String pmat) {
		WebElement pmattTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ProductMetadataEnum.PROJECT_CODE.getValue());
		pmattTextbox.clear();
		pmattTextbox.sendKeys(pmat);
	}
	
	public static String getPmattText(){
		return getPmattField().getAttribute("value");
	}
	
	public static String[] getTags(){
		return ResourceGeneralPage.getElementsFromTagsTable().stream().map(WebElement::getText).toArray(String[]::new);
	}
	
	private static WebElement getPmattField() {
		return GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.ProductMetadataEnum.PROJECT_CODE.getValue());
	}
	
	public static void deleteOldTags(ProductReqDetails product){
		// Delete tag elements
		int i = GeneralUIUtils.getWebElementsListByTestID("i-sdc-tag-delete").size();
		while (i > 0){			
			GeneralUIUtils.getWebElementByTestID("i-sdc-tag-delete").click();
			i--;
		}
		
		product.setTags(new ArrayList<String>());
	}
	
	public static void clickAddWorkflow (){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Adding workflow..."));
		GeneralUIUtils.clickOnElementByText("Add Workflow");
	}
	
	public static void fillAndAddNewWorkflow(String name, String description ) throws InterruptedException{
		GeneralUIUtils.ultimateWait();
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Filling name field with %s", name));
		insertText(name, "label + input");
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Filling description filed with %s", name));
		insertText(description,"label + textarea");
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking save button "));
		clickSave();
	}
	
	public static void insertText(String artifactDescriptoin, String element) throws InterruptedException {
		GeneralUIUtils.getElementsByCSS("div[class='modal-content']");
		WebElement artifactDescriptionTextbox = GeneralUIUtils.getWebElementBy(By.cssSelector(element));
		artifactDescriptionTextbox.clear();
		artifactDescriptionTextbox.sendKeys(artifactDescriptoin);
		GeneralUIUtils.ultimateWait();		
	}
	
	public static void clickSave() {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Save button"));
		GeneralUIUtils.clickOnElementByText("Save");
		GeneralUIUtils.ultimateWait();
	}

}
