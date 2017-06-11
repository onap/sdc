package org.openecomp.sdc.uici.tests.execute.service;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.uici.tests.datatypes.CanvasElement;
import org.openecomp.sdc.uici.tests.datatypes.CanvasManager;
import org.openecomp.sdc.uici.tests.datatypes.CreateAndUpdateStepsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.BreadcrumbsButtonsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.InputsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.ModalItems;
import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openecomp.sdc.uici.tests.utilities.FileHandling;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.uici.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.uici.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.uici.tests.verificator.ServiceVerificator;
import org.testng.annotations.Test;

public class ServiceInputsTests extends SetupCDTest {
	
	public String serviceName = "";

	@Test
	private void testSelectingInputAndAddingItToTheService() {
		ServiceInputsTestsSetUp();	
		
		assertTrue(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.FIRST_INPUT_CHECKBOX.getValue()).getAttribute("class").contains("disabled"));
		assertTrue(GeneralUIUtils.isElementPresent(InputsEnum.SERVICE_INPUT.getValue()));
	}
	
	@Test
	private void testDeletingAnInputFromTheService() {
		ServiceInputsTestsSetUp();
		
		// clicking on the delete input button and accepting the delete
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.DELETE_INPUT.getValue()).click();
		GeneralUIUtils.getWebElementWaitForClickable(ModalItems.OK.getValue()).click();	
		
		assertFalse(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.FIRST_INPUT_CHECKBOX.getValue()).getAttribute("class").contains("disabled"));
		assertFalse(GeneralUIUtils.isElementPresent(InputsEnum.SERVICE_INPUT.getValue()));		
	}
	
	@Test
	private void testCheckingInTheServiceAndButtonsAreDisabled() throws Exception {
		ServiceInputsTestsSetUp();
		
		// Checking in the service and accessing it again in the home
		GeneralUIUtils.checkIn();
		GeneralUIUtils.closeNotificatin();
		GeneralUIUtils.findComponentAndClick(serviceName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.INPUTS);
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.VF_INSTANCE.getValue()).click();
		
		assertTrue(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.FIRST_INPUT_CHECKBOX.getValue()).getAttribute("class").contains("disabled"));
		assertTrue(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.SECOND_INPUT_CHECKBOX.getValue()).getAttribute("class").contains("disabled"));
		assertTrue(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.DELETE_INPUT.getValue()).getAttribute("class").contains("disabled"));
	}
	
	@Test
	private void testInputsSanity() throws Exception {
		ServiceInputsTestsSetUp();		
		
		assertTrue(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.FIRST_INPUT_CHECKBOX.getValue()).getAttribute("class").contains("disabled"));
		assertTrue(GeneralUIUtils.isElementPresent(InputsEnum.SERVICE_INPUT.getValue()));
		
		// clicking on the delete input button and accepting the delete
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.DELETE_INPUT.getValue()).click();
		GeneralUIUtils.getWebElementWaitForClickable(ModalItems.OK.getValue()).click();	
		
		assertFalse(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.FIRST_INPUT_CHECKBOX.getValue()).getAttribute("class").contains("disabled"));
		assertFalse(GeneralUIUtils.isElementPresent(InputsEnum.SERVICE_INPUT.getValue()));
		
		// adding the input to the service again
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.FIRST_INPUT_CHECKBOX.getValue()).click();
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.ADD_INPUTS_BUTTON.getValue()).click();
		
		// Checking in the service and accessing it again in the home
		GeneralUIUtils.checkIn();
		GeneralUIUtils.closeNotificatin();
		GeneralUIUtils.findComponentAndClick(serviceName);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.INPUTS);
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.VF_INSTANCE.getValue()).click();
		
		assertTrue(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.FIRST_INPUT_CHECKBOX.getValue()).getAttribute("class").contains("disabled"));
		assertTrue(GeneralUIUtils.getWebElementWaitForVisible(InputsEnum.SECOND_INPUT_CHECKBOX.getValue()).getAttribute("class").contains("disabled"));
	}
	
	private void ServiceInputsTestsSetUp() {
		// create vf
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "service_with_inputs.csar";
		ResourceReqDetails importVfREsourceInUI = ResourceUIUtils.importVfInUIWithoutCheckin(getUser(), filePath, fileName);
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.closeNotificatin();
		GeneralUIUtils.checkIn();
		GeneralUIUtils.closeNotificatin();
		
		// create service
		ServiceReqDetails createServiceInUI = ServiceUIUtils.createServiceInUI(getUser());
		ServiceVerificator.verifyServiceCreated(createServiceInUI, getUser());
		serviceName = createServiceInUI.getName();
		
		// go to composition
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		
		// drag vf into canvas
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement canvasElement = canvasManager.createElementOnCanvas(importVfREsourceInUI.getName());
		canvasManager.selectElementFromCanvas(canvasElement);
		GeneralUIUtils.waitForLoader();
		
		// moving to inputs view
		GeneralUIUtils.getWebElementWaitForClickable(BreadcrumbsButtonsEnum.COMPONENT.getValue()).click();
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.INPUTS);
		
		// adding the input to the service
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.VF_INSTANCE.getValue()).click();
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.FIRST_INPUT_CHECKBOX.getValue()).click();
		GeneralUIUtils.getWebElementWaitForClickable(InputsEnum.ADD_INPUTS_BUTTON.getValue()).click();
	}	
}
