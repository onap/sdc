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

package org.openecomp.sdc.ci.tests.verificator;

import com.aventstack.extentreports.Status;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.LifeCycleStateEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public final class VfVerificator {
	
	public static void verifyNumOfComponentInstances(ResourceReqDetails createResourceInUI, int numOfVFC, User user) {
		ServiceVerificator.verifyNumOfComponentInstances(createResourceInUI, createResourceInUI.getVersion(), numOfVFC, user);
	}

	public static void verifyRILocationChanged(ResourceReqDetails createResourceInUI,
			ImmutablePair<String, String> prevRIPos, User user) {

		ImmutablePair<String, String> currRIPos = ResourceUIUtils.getFirstRIPos(createResourceInUI, user);
		assertTrue(!prevRIPos.left.equals(currRIPos.left) || !prevRIPos.right.equals(currRIPos.right));
	}

	public static void verifyLinkCreated(ResourceReqDetails createResourceInUI, User user, int expectedRelationsSize) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifing that a link was created on canvas"));
		String responseAfterDrag = RestCDUtils.getResource(createResourceInUI, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		assertTrue(((JSONArray) jsonResource.get("componentInstancesRelations")).size() == expectedRelationsSize);
		ExtentTestActions.log(Status.INFO, "The link was verified.");

	}

	public static void verifyVFMetadataInUI(ResourceReqDetails vf) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifying fields on General screen through UI ..."));
		assertTrue(vf.getName().equals(ResourceGeneralPage.getNameText()));
		assertTrue(vf.getDescription().equals(ResourceGeneralPage.getDescriptionText()));
		assertTrue(vf.getCategories().get(0).getSubcategories().get(0).getName().equals(GeneralUIUtils.getSelectedElementFromDropDown(ResourceGeneralPage.getCategoryDataTestsIdAttribute()).getText()));
		assertTrue(vf.getVendorName().equals(ResourceGeneralPage.getVendorNameText()));
		assertTrue(vf.getVendorRelease().equals(ResourceGeneralPage.getVendorReleaseText()));
		assertTrue(vf.getContactId().equals(ResourceGeneralPage.getContactIdText()));
		List<WebElement> tagsList = ResourceGeneralPage.getElementsFromTagsTable();
		assertTrue(vf.getTags().size() == tagsList.size());
		for (int i = 0 ; i < vf.getTags().size(); i ++ ){
			assertTrue(vf.getTags().contains(tagsList.get(i).getText()));
		}
		assertTrue(vf.getContactId().equals(ResourceGeneralPage.getContactIdText()));
	}
	
	public static void verifyVFUpdated(ResourceReqDetails vf, User user) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verifying fields on General screen through Backend ..."));
		String response = RestCDUtils.getResource(vf, user).getResponse();
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(response);
		assertTrue(vf.getName().equals(resource.getName()));
		assertTrue(vf.getDescription().equals(resource.getDescription()));
		assertTrue(vf.getVendorName().equals(resource.getVendorName()));
		assertTrue(vf.getVendorRelease().equals(resource.getVendorRelease()));
		assertTrue(vf.getContactId().equals(resource.getContactId()));
		
		assertTrue(vf.getCategories().size() == (resource.getCategories().size()));
		for (int i = 0 ; i < vf.getCategories().size() ; i ++)
		{
			CategoryDefinition expectedCategoryDefinition = vf.getCategories().get(i);
			CategoryDefinition actualCategoryDefinition = resource.getCategories().get(i);
			assertTrue(expectedCategoryDefinition.getName().equals(actualCategoryDefinition.getName()));
			assertTrue(expectedCategoryDefinition.getSubcategories().get(i).getName().equals(actualCategoryDefinition.getSubcategories().get(i).getName()));
		}
		
		assertTrue(vf.getTags().size() == (resource.getTags().size()));
		for (int i = 0 ; i < vf.getTags().size() ; i ++){
			List<String> expectedTags = vf.getTags();
			List<String> actualTags = resource.getTags();
			
			assertTrue(actualTags.contains(expectedTags.get(i)));
			
		}				
	}

	public static void verifyVFLifecycle(ResourceReqDetails vf, User user, LifecycleStateEnum expectedLifecycleState) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verfiying that object %s version is %s", vf.getName(),expectedLifecycleState));
		String responseAfterDrag = RestCDUtils.getResource(vf, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		String actualLifecycleState = jsonResource.get("lifecycleState").toString();
		assertTrue(expectedLifecycleState.name().equals(actualLifecycleState), "actual: " + actualLifecycleState + "-- expected: " + expectedLifecycleState);
	}

	public static void verifyVfLifecycleInUI(LifeCycleStateEnum lifecycleState){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verfiying that object version is %s", lifecycleState.getValue()));
		GeneralUIUtils.ultimateWait();
		assertTrue(ResourceGeneralPage.getLifeCycleState().equals(lifecycleState.getValue()));
	}
	
	public static void verifyInstanceVersion(ResourceReqDetails vf, User user, String instanceName, String instanceVersion){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Verfiying that instance %s version is %s", instanceName,instanceVersion));
		String responseAfterDrag = RestCDUtils.getResource(vf, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		JSONArray jsonArrayResource = (JSONArray) jsonResource.get("componentInstances");
		for (int i = 0; i < jsonArrayResource.size(); i++){
			Object object = jsonArrayResource.get(i);
			try{
				JSONObject jRes = (JSONObject) JSONValue.parse(object.toString());
				String componentName = jRes.get("componentName").toString();
				if (componentName.equals(instanceName)){
					String componentVersion = jRes.get("componentVersion").toString();
					assertTrue(componentVersion.equals(instanceVersion));
				}
			}
			catch(Exception e){
				System.out.println("Can't test object in componentInstances array");
				Assert.fail("Can't test object in componentInstances array");
			}
		}
	}
	
	public static void verifyVfDeleted(ResourceReqDetails vf, User user){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating resource %s was deleted", vf.getName()));
		RestResponse response = RestCDUtils.getResource(vf, user);
		assertTrue(response.getErrorCode().intValue() == 404);
	}
	
	public static void verifyPropertiesInUI(List<PropertyTypeEnum> propertyList){
		
		for (PropertyTypeEnum prop : propertyList){
			String propName = prop.getName();
			
			String actualName = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.PROPERTY_NAME.getValue() + propName).getText();
			String actualType = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.PROPERTY_TYPE.getValue() + propName).getText();
			String actualDesciprtion = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.PROPERTY_DESCRIPTION.getValue() + propName).getText();
			
			assertTrue(propName.equals(actualName), String.format("Property name is not correct. expected:%s ; actual %s", propName, actualName));
			assertTrue(prop.getType().equals(actualType), String.format("Property type is not correct. expected:%s ; actual %s", prop.getType(), actualType));
			assertTrue(prop.getDescription().equals(actualDesciprtion), String.format("Property description is not correct. expected:%s ; actual %s", prop.getDescription(), actualDesciprtion));
			
			if (prop.getSchemaDefinition() != null){
				String actualSchema = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesPageEnum.ENTRY_SCHEMA.getValue() + propName).getText();
				assertTrue(prop.getSchemaDefinition().equals(actualSchema), String.format("Property schema is not correct. expected:%s ; actual %s", prop.getSchemaDefinition(), actualSchema));
			}
		}
	}
	
	public static void verifyToscaArtifactsInfo(ResourceReqDetails vf, User user){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Validating Tosca Aritfact Info of resource %s", vf.getName()));
		String responseAfterDrag = RestCDUtils.getResource(vf, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		JSONObject toscaArtifacts = (JSONObject) jsonResource.get("toscaArtifacts");
		
		assertEquals(2, toscaArtifacts.size());
		for (Object artifactObj : toscaArtifacts.keySet()){
			JSONObject artifact = (JSONObject) JSONValue.parse(toscaArtifacts.get(artifactObj).toString());
			assertFalse(artifact.get("artifactUUID").toString().isEmpty(), "artifactUUID field is empty");
			assertFalse(artifact.get("artifactChecksum").toString().isEmpty(), "artifactChecksum filed is empty");
			assertFalse(artifact.get("payloadUpdateDate").toString().isEmpty(), "payloadUpdateDate field is empty");
			assertFalse(artifact.get("artifactVersion").toString().equals("0"), "artifactVersion field is 0");
		}
	}

	public static void verifyVfInputs(String instanceName, Map<String, String> instancePropertiesMapFromJson,List<WebElement> propertyRowsFromTable) {
		
		for (int i = 0 ; i < propertyRowsFromTable.size() ; i++){
			WebElement row = propertyRowsFromTable.get(i);
			String propertyNameFromTable = row.findElement(By.xpath(".//*[@data-tests-id='" + "propertyName']")).getText();
			String propertyTypeFromTable = row.findElement(By.xpath(".//*[@data-tests-id='" + "propertyType']")).getText();
			String instanceNameFromTable = row.findElement(By.xpath(".//*[@data-tests-id='" + "instanceName']")).getText();
			String propertySchemaFromTable = row.findElement(By.xpath(".//*[@data-tests-id='" + "propertySchema']")).getText();
			
			assertTrue(instancePropertiesMapFromJson.containsKey(propertyNameFromTable), "No property named : " + propertyNameFromTable + "for instance " + instanceName);
			String expectedType = instancePropertiesMapFromJson.get(propertyNameFromTable);
			assertTrue(expectedType.equals(propertyTypeFromTable.toLowerCase()), propertyNameFromTable + "type is incorrect");
			assertTrue(instanceName.equals(instanceNameFromTable), "Instance name of property named " + propertyNameFromTable + "is incorrect");
		}
	}

	public static void verifyOnboardedVnfMetadata(String vspName, Map<String, String> vspMetadata) {
		SetupCDTest.getExtendTest().log(Status.INFO, "Verifying metadata");
		assertTrue(vspName.equals(ResourceGeneralPage.getNameText()), "VSP name is not valid.");
		assertTrue(vspMetadata.get("description").equals(ResourceGeneralPage.getDescriptionText()), "VSP description is not valid.");
		assertTrue(vspMetadata.get("subCategory").equals(GeneralUIUtils.getSelectedElementFromDropDown(ResourceGeneralPage.getCategoryDataTestsIdAttribute()).getText().trim()), "VSP category is not valid.");
		assertTrue(vspMetadata.get("vendorName").equals(ResourceGeneralPage.getVendorNameText()), "VSP vendor name is not valid.");
		assertTrue("1.0".equals(ResourceGeneralPage.getVendorReleaseText()), "VSP version is not valid.");
		List<WebElement> tagsList = ResourceGeneralPage.getElementsFromTagsTable();
		assertTrue(tagsList.size() == 1, "VSP tags size is not equal to 1.");
		assertTrue(vspName.equals(tagsList.get(0).getText()), "VSP tag is not its name.");
		assertTrue(vspMetadata.get("attContact").equals(ResourceGeneralPage.getContactIdText()), "VSP attContact is not valid.");
	}
	
	public static void verifyIsElementDisabled(String elementLocator, String elementName){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Checking if %s is disabled", elementName));
		assertTrue(GeneralUIUtils.isElementReadOnly(elementLocator));
	}
	
	public static void verifyFilesChecksum(File actual, File expected){
		try {
			String actualMd5OfFile = FileHandling.getMD5OfFile(actual);
			String expectedMd5OfFile = FileHandling.getMD5OfFile(expected);
			Assert.assertEquals(expectedMd5OfFile, actualMd5OfFile, "File does not exist");
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
	}
	
}
