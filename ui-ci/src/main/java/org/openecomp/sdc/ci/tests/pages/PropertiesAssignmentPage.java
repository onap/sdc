package org.openecomp.sdc.ci.tests.pages;


import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.PropertiesAssignmentScreen;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

public class PropertiesAssignmentPage {
	public static void clickOnInputTab(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Input Tab"));
		GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.INPUTS_TAB.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static void clickOnPropertiesTab(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Properties Tab"));
		GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.PROPERTIES_TAB.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static void clickOnCompositionRightTab(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Composition Right Tab"));
		GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.COMPOSITION_TAB.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static void clickOnPropertyStructureRightTab(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Property Structure Right Tab"));
		GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.PROPERTY_STRUCTURE_TAB.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static void clickOnDeclareButton(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Declare Button"));
		GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.DECLARE_BUTTON.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static void findSearchBoxAndClick(String resourceName) throws Exception {
		SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + resourceName + " in homepage");
		WebElement searchTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesAssignmentScreen.SEARCH_BOX.getValue());
		try{
			searchTextbox.clear();
			searchTextbox.sendKeys(resourceName);
			GeneralUIUtils.ultimateWait();
		}
		catch(Exception e){
			SetupCDTest.getExtendTest().log(Status.INFO, "Can't interact with search bar");
			e.printStackTrace();
		}
		
		
		try{
			SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on the %s component from home screen", resourceName));
			GeneralUIUtils.clickOnElementByTestId(resourceName);
			GeneralUIUtils.ultimateWait();
			GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
		}
		catch(Exception e){
			SetupCDTest.getExtendTest().log(Status.INFO, "Can't click on component named " + resourceName);
			e.printStackTrace();
		}
	}
	
	public static void clickOnProperty(String propertyName) {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on property %s ", propertyName));
		GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.InputsScreenService.RESOURCE_INSTANCE_PROPERTY_NAME.getValue() + propertyName);
		GeneralUIUtils.ultimateWait();
	}
	
	public static void clickOnDeleteInputButton(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Delete Input Button"));
		GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.INPUT_DELETE_BUTTON.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static void clickOnDeleteInputDialogConfirmationButton(){
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on Delete Input Dialog Confirmation Button"));
		GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.INPUT_DELETE_DIALOG_DELETE.getValue());
		GeneralUIUtils.ultimateWait();		
	}
	
	public static void clickOnFilterAll() {
		SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on property %s "));
		GeneralUIUtils.clickOnElementByTestId(PropertiesAssignmentScreen.FILTER_CHECKBOX_ALL.getValue());
		GeneralUIUtils.ultimateWait();
	}
	
	public static void clickOnComponentInComposition(String resourceName) throws Exception{
		try{
			SetupCDTest.getExtendTest().log(Status.INFO, String.format("Clicking on the %s component from properties composition", resourceName));
			GeneralUIUtils.clickOnElementByTestId(resourceName);
			GeneralUIUtils.ultimateWait();
			GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.GeneralElementsEnum.LIFECYCLE_STATE.getValue());
		}
		catch(Exception e){
			SetupCDTest.getExtendTest().log(Status.INFO, "Can't click on component named " + resourceName);
			e.printStackTrace();
		}
	}
	
	public static void findInput(String componentName, String resourceName) throws Exception {
		SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + componentName + "_" + resourceName + " in homepage");
		WebElement searchTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesAssignmentScreen.SEARCH_BOX.getValue());
		String searchPattern = componentName + "_" + resourceName;
		try{
			searchTextbox.clear();
			searchTextbox.sendKeys(searchPattern);
			GeneralUIUtils.ultimateWait();
		}
		catch(Exception e){
			SetupCDTest.getExtendTest().log(Status.INFO, "Can't interact with search bar");
			e.printStackTrace();
		}
		
	}
	
	public static void findProperty(String resourceName) throws Exception {
		SetupCDTest.getExtendTest().log(Status.INFO, "Searching for " + resourceName + " in homepage");
		WebElement searchTextbox = GeneralUIUtils.getWebElementByTestID(DataTestIdEnum.PropertiesAssignmentScreen.SEARCH_BOX.getValue());
		
		try{
			searchTextbox.clear();
			searchTextbox.sendKeys(resourceName);
			GeneralUIUtils.ultimateWait();
		}
		catch(Exception e){
			SetupCDTest.getExtendTest().log(Status.INFO, "Can't interact with search bar");
			e.printStackTrace();
		}
		
	}
	
	public static Boolean isPropertyChecked(String resourceName){
//		TODO add better implementation for css string
		GeneralUIUtils.ultimateWait();
		Boolean isDisabled = GeneralUIUtils.checkForDisabledAttributeInHiddenElement("checkbox[data-tests-id='" + resourceName + "'] input");
		if (isDisabled)
			return true;
		return false;
		
	}
	
	
}
