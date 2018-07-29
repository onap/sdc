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

package org.openecomp.sdc.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.*;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.verificator.PropertiesAssignmentVerificator;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class PropertiesAssignment extends SetupCDTest {

	private static String filePath;
	private static String csarFile = "PCRF_OS_FIXED.csar";
    private static String csarFile1 = "437285.csar";
	
	@BeforeClass
	public void beforeClass(){
		filePath = FileHandling.getFilePath("");
	}
	
	@BeforeMethod
	public void beforeTest(){
		System.out.println("File repository is : " + filePath);
		getExtendTest().log(Status.INFO, "File repository is : " + filePath);
	}
	

	//VF - Simple Properties Tests
	@Test
	public void declareAndDeleteInputVfTest() throws Exception {

		String csarTestFile = csarFile;
//		String componentName = "abstract_pcm";
        String componentName = "abstract_psm";
//      String propertyName = "min_instances";
        String propertyName = "service_template_filter";

		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		resourceMetaData.setVersion("0.1");
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());
		

		ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
		PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
		PropertiesAssignmentPage.findSearchBoxAndClick(propertyName);
		PropertiesAssignmentPage.clickOnDeclareButton();
        GeneralUIUtils.ultimateWait();
		AssertJUnit.assertTrue(PropertiesAssignmentPage.isPropertyChecked(propertyName));
		
		PropertiesAssignmentPage.clickOnInputTab();
		PropertiesAssignmentPage.findInput(componentName, propertyName);
		PropertiesAssignmentPage.clickOnDeleteInputButton();
		PropertiesAssignmentPage.clickOnDeleteInputDialogConfirmationButton();
		PropertiesAssignmentPage.clickOnPropertiesTab();
		PropertiesAssignmentPage.findProperty(propertyName);
		AssertJUnit.assertFalse(PropertiesAssignmentPage.isPropertyChecked(propertyName));
		

	}


	@Test
    public void editAndSaveSimplePropertyValueTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "abstract_psm";
        String propertyNameString = PropertyNameBuilder.buildSimpleField("nfc_function");
        String propertyNameInt = PropertyNameBuilder.buildSimpleField("index_value");
        String propertyValueString = "abc";
        String propertyValueInt = "123";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(
                "ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit simple properties values and save
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
//        PropertiesAssignmentPage.editPropertyValue(propertyNameString, propertyValueString);
        PropertiesAssignmentPage.editPropertyValue(propertyNameString, propertyValueString);
        PropertiesAssignmentPage.editPropertyValue(propertyNameInt, propertyValueInt);
        PropertiesAssignmentPage.clickOnSaveButton();

        //Verify that properties values are saved
        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameString,propertyValueString);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameInt,propertyValueInt);
    }

    @Test
    public void editAndSaveBooleanPropertyValueTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "oam_volume_0";
        String propertyNameTrue = "value-prop-read_only";
        String propertyValueTrue = "TRUE";
        String propertyNameFalse = "value-prop-delete_on_termination";
        String propertyValueFalse = "FALSE";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit simple properties values and save from a dialog
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.selectBooleanPropertyValue(propertyNameTrue, propertyValueTrue);
        PropertiesAssignmentPage.selectBooleanPropertyValue(propertyNameFalse, propertyValueFalse);
        PropertiesAssignmentPage.clickOnSaveButton();

        //Verify that properties values are saved
        ResourceGeneralPage.getLeftMenu().moveToDeploymentViewScreen();
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validateBooleanPropertyValue(propertyNameTrue,propertyValueTrue);
        PropertiesAssignmentVerificator.validateBooleanPropertyValue(propertyNameFalse,propertyValueFalse);
    }

    @Test
    public void editAndSavePopupPropertyValueTest() throws Exception {
//        Internal bug 391466
//	      Popup Property: value editing is done in a popup text field, opened by clicking Edit button
//        Example: subnetpoolid property

        String csarTestFile = csarFile1;
        String componentName = "abstract_cif";
        String propertyName = "port_cif_imsli_port_subnetpoolid";
        String propertyValue = "updated by designer";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(
                "ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit popup property value and save
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnEditButton(propertyName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildSimpleField(propertyName), propertyValue);
        PropertiesAssignmentPage.clickOnSetButton();
        PropertiesAssignmentPage.clickOnSaveButton();

        //Verify that popup property value is saved
//        HomePage.navigateToHomePage();
//        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
//        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        ResourceGeneralPage.getLeftMenu().moveToDeploymentViewScreen();
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnEditButton(propertyName);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(propertyName),propertyValue);

    }


    @Test
    public void editAndDiscardSimplePropertyValueTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "abstract_psm";
        String propertyNameString = "value-prop-nfc_function";
        String propertyNameInt = "value-prop-index_value";
        String propertyValueString = "abc";
        String propertyValueInt = "123";
        String propertyOrigValueString = "";
        String propertyOrigValueInt = "0";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit simple properties values and discard changes
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.editPropertyValue(propertyNameString, propertyValueString);
        PropertiesAssignmentPage.editPropertyValue(propertyNameInt, propertyValueInt);
        PropertiesAssignmentPage.clickOnDiscardButton();

        //Verify that properties values are not saved
        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameString,propertyOrigValueString);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameInt,propertyOrigValueInt);
    }

    @Test
    public void editAndSaveSimplePropertyFromDialogTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "abstract_psm";
        String propertyNameString = "value-prop-nfc_function";
        String propertyNameInt = "value-prop-index_value";
        String propertyValueString = "abc";
        String propertyValueInt = "123";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit simple properties values and save from a dialog
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.editPropertyValue(propertyNameString, propertyValueString);
        PropertiesAssignmentPage.editPropertyValue(propertyNameInt, propertyValueInt);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentPage.clickOnDialogCancelButton();

        //Verify that properties values are not removed
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameString,propertyValueString);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameInt,propertyValueInt);

        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentPage.clickOnDialogSaveButton();

        //Verify that properties values are saved
        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameString,propertyValueString);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameInt,propertyValueInt);
    }


    @Test
    public void editAndDiscardSimplePropertyFromDialogTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "abstract_psm";
        String propertyNameString = "value-prop-nfc_function";
        String propertyNameInt = "value-prop-index_value";
        String propertyValueString = "abc";
        String propertyValueInt = "123";
        String propertyOrigValueString = "";
        String propertyOrigValueInt = "0";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit simple properties values and save from a dialog
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.editPropertyValue(propertyNameString, propertyValueString);
        PropertiesAssignmentPage.editPropertyValue(propertyNameInt, propertyValueInt);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentPage.clickOnDialogCancelButton();

        ///Verify that properties values are not removed
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameString,propertyValueString);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameInt,propertyValueInt);

        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentPage.clickOnDialogDiscardButton();

        //Verify that properties values are saved
        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameString,propertyOrigValueString);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameInt,propertyOrigValueInt);
    }


    @Test
    public void editAndSaveInputValueTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "abstract_psm";
        String propertyNameString = "value-input-nf_type";
        String propertyNameFloat = "value-input-pcm_volume_size_0";
        String propertyNameBoolean = "value-input-multi_stage_design";
        String propertyValueString = "POLICY";
        String propertyValueFloat = "80.1";
        String propertyValueBoolean = "TRUE";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen - Inputs
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnInputTab();

        //Verify that Save button is disabled when inserting invalid value into Input field
        PropertiesAssignmentPage.editPropertyValue(propertyNameFloat, propertyValueString);
        AssertJUnit.assertTrue(PropertiesAssignmentPage.isButtonDisabled(DataTestIdEnum.PropertiesAssignmentScreen.SAVE_BUTTON.getValue()));

        //Insert valid values for different Input types and save
        PropertiesAssignmentPage.editPropertyValue(propertyNameFloat, propertyValueFloat);
        PropertiesAssignmentPage.editPropertyValue(propertyNameString, propertyValueString);
        PropertiesAssignmentPage.selectBooleanPropertyValue(propertyNameBoolean, propertyValueBoolean);
        PropertiesAssignmentPage.clickOnSaveButton();

        //Verify that input values are saved after changing VF version
        ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        GeneralPageElements.clickCheckoutButton();
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameString,propertyValueString);
        PropertiesAssignmentVerificator.validatePropertyValue(propertyNameFloat,propertyValueFloat);
        PropertiesAssignmentVerificator.validateBooleanPropertyValue(propertyNameBoolean,propertyValueBoolean);
    }

    //VF - List and Map Properties Tests

    @Test
    public void editAndSaveListPropertyValueTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "abstract_pps";
        String propertyName = "compute_pps_user_data_format";
        String propertyListValueZero = "RAW";
        String propertyListValueOne = "property-value-one";
        String propertyListValueTwo = "property-value-two";


        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit "list of strings" properties values and save
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnAddValueToList(propertyName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIndexedField(propertyName,1), propertyListValueOne);
        PropertiesAssignmentPage.clickOnAddValueToList(propertyName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIndexedField(propertyName,2), propertyListValueTwo);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.clickODeleteValueFromList(propertyName,1);
        PropertiesAssignmentPage.clickOnSaveButton();

        //Verify that properties values are saved
        ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        GeneralPageElements.clickCheckoutButton();
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildIndexedField(propertyName,0),propertyListValueZero);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildIndexedField(propertyName,1),propertyListValueTwo);


        //Declare property as input, delete input
        PropertiesAssignmentPage.findSearchBoxAndClick(propertyName);
        PropertiesAssignmentPage.clickOnDeclareButton();
        AssertJUnit.assertTrue(PropertiesAssignmentPage.isPropertyChecked(propertyName));

        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentPage.findInput(componentName, propertyName);
        PropertiesAssignmentPage.clickOnDeleteInputButton();
        PropertiesAssignmentPage.clickOnDeleteInputDialogConfirmationButton();
        PropertiesAssignmentPage.clickOnPropertiesTab();
        PropertiesAssignmentPage.findProperty(propertyName);
        AssertJUnit.assertFalse(PropertiesAssignmentPage.isPropertyChecked(propertyName));
    }

    @Test
    public void editAndSaveListOfComplexPropertyValueTest() throws Exception {

	    //External Defect 437285 - PLEASE DON'T DELETE THE TEST!!!!

        String csarTestFile = csarFile1;
        String componentName = "abstract_cdi";
        String propertyName = "port_cdi_imscore_port_ip_requirements";
        String nestedPropertyName = "ip_count";
        String propertyListValue = "12345";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit "list of strings" properties values and save
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnAddValueToList(propertyName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIComplexListField(propertyName,nestedPropertyName,1),propertyListValue);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.clickOnExpandButton(propertyName,1);
        PropertiesAssignmentPage.deletePropertyValue(PropertyNameBuilder.buildIComplexListField(propertyName,nestedPropertyName,1));
        PropertiesAssignmentPage.clickOnSaveButton();

        //Verify that properties values are saved
        HomePage.navigateToHomePage();
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnExpandButton(propertyName,1);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildIComplexListField(propertyName,nestedPropertyName,1),"");


    }


    @Test
    public void editAndSaveMapPropertyValueTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "oam_volume_0";
        String propertyName = "metadata";
        String propertyMapKeyOne = "KEY1";
        String propertyMapValueOne = "property-value-one";
        String propertyMapKeyTwo = "KEY2";
        String propertyMapValueTwo = "property-value-two";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit "map of strings" properties values and save
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.clickOnAddValueToList(propertyName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIndexedField(propertyName,0),propertyMapValueOne);


        //Verify that Save and Declare buttons are disabled when leaving Key value empty
        AssertJUnit.assertTrue(PropertiesAssignmentPage.isButtonDisabled(DataTestIdEnum.PropertiesAssignmentScreen.SAVE_BUTTON.getValue()));
        AssertJUnit.assertTrue(PropertiesAssignmentPage.isButtonDisabled(DataTestIdEnum.PropertiesAssignmentScreen.DECLARE_BUTTON.getValue()));

        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIndexedKeyField(propertyName,0),propertyMapKeyOne);
        PropertiesAssignmentPage.clickOnSaveButton();
        PropertiesAssignmentPage.clickOnAddValueToList(propertyName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIndexedKeyField(propertyName,1),propertyMapKeyTwo);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIndexedField(propertyName,1),propertyMapValueTwo);
        PropertiesAssignmentPage.clickODeleteValueFromList(propertyName,0);
        PropertiesAssignmentPage.clickOnSaveButton();

//        //Verify that properties values are saved
        ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        GeneralPageElements.clickCheckoutButton();
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildIndexedKeyField(propertyName,0),propertyMapKeyTwo);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildIndexedField(propertyName,0),propertyMapValueTwo);

//        //Declare property as input, delete input
        PropertiesAssignmentPage.findSearchBoxAndClick(propertyName);
        PropertiesAssignmentPage.clickOnDeclareButton();
        AssertJUnit.assertTrue(PropertiesAssignmentPage.isPropertyChecked(propertyName));

        PropertiesAssignmentPage.clickOnInputTab();
        PropertiesAssignmentPage.findInput(componentName, propertyName);
        PropertiesAssignmentPage.clickOnDeleteInputButton();
        PropertiesAssignmentPage.clickOnDeleteInputDialogConfirmationButton();
        PropertiesAssignmentPage.clickOnPropertiesTab();
        PropertiesAssignmentPage.findProperty(propertyName);
        AssertJUnit.assertFalse(PropertiesAssignmentPage.isPropertyChecked(propertyName));
    }


    //VF - Complex Properties Tests
    @Test
    public void editAndSaveComplexPropertyValueTest() throws Exception {

        String csarTestFile = csarFile;
        String componentName = "abstract_psm";
        String propertyName = "service_template_filter";
        String propertyNameString = "substitute_service_template";
        String propertyNameInt = "index_value";
        String propertyNameBoolean = "scaling_enabled";
        String propertyValueString = "Modified_psmServiceTemplate.yaml";
        String propertyValueInt = "2147483647";
        String propertyValueBoolean = "FALSE";

        //Create VF
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
        resourceMetaData.setVersion("0.1");
        ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());

        //Navigate to Properties Assignment screen, edit simple properties values and save
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIComplexField(propertyName,propertyNameString), propertyValueString);
        PropertiesAssignmentPage.editPropertyValue(PropertyNameBuilder.buildIComplexField(propertyName,propertyNameInt), propertyValueInt);
        PropertiesAssignmentPage.selectBooleanPropertyValue(PropertyNameBuilder.buildIComplexField(propertyName,propertyNameBoolean), propertyValueBoolean);
        PropertiesAssignmentPage.clickOnSaveButton();

        //Verify that properties values are saved
        ResourceGeneralPage.clickCheckinButton(resourceMetaData.getName());
        GeneralUIUtils.findComponentAndClick(resourceMetaData.getName());
        GeneralPageElements.clickCheckoutButton();
        ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
        PropertiesAssignmentPage.clickOnComponentInComposition(componentName);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildIComplexField(propertyName,propertyNameString),propertyValueString);
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildIComplexField(propertyName,propertyNameInt),propertyValueInt);
        PropertiesAssignmentVerificator.validateBooleanPropertyValue(PropertyNameBuilder.buildIComplexField(propertyName,propertyNameBoolean),propertyValueBoolean);

        //Select complex property and declare as input
        PropertiesAssignmentPage.findSearchBoxAndClick(propertyName);
        PropertiesAssignmentPage.clickOnDeclareButton();
        PropertiesAssignmentVerificator.validatePropertyValue(PropertyNameBuilder.buildSimpleField(propertyName),PropertyNameBuilder.buildVfDeclaredPropValue(componentName, propertyName));
    }
	
	
	//VF - Filter Tests
    @Test
	public void filterAllVfTest() throws Exception {

		String csarTestFile = csarFile;
		String propertyName = "name";
		String propertyLocation = DataTestIdEnum.PropertiesAssignmentScreen.PROPERTY_NAME_COLUMN.getValue();
		int propertiesCountFilter = 22;
		int propertiesCountWithoutFilter = 0;
		
		ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType("ciRes", NormativeTypesEnum.ROOT, ResourceCategoryEnum.APPLICATION_L4_DATABASE, getUser().getUserId(), ResourceTypeEnum.VF.toString());
		resourceMetaData.setVersion("0.1");
		ResourceUIUtils.importVfFromCsar(resourceMetaData, filePath, csarTestFile, getUser());
		

		ResourceGeneralPage.getLeftMenu().moveToPropertiesAssignmentScreen();
		//Count current properties number before filter is applied
		propertiesCountWithoutFilter = GeneralUIUtils.getWebElementsListByContainsClassName(propertyLocation).size();
		PropertiesAssignmentPage.clickOnFilterButton();
		PropertiesAssignmentPage.clickOnFilterAllCheckbox();
		PropertiesAssignmentPage.findFilterBoxAndClick(propertyName);
		PropertiesAssignmentPage.clickOnFilterApplyButton();
		PropertiesAssignmentVerificator.validateFilteredPropertiesCount(propertiesCountFilter, propertyLocation);

		PropertiesAssignmentPage.clickOnFilterClearAllButton();
		PropertiesAssignmentVerificator.validateFilteredPropertiesCount(propertiesCountWithoutFilter, propertyLocation);
	}

	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}

