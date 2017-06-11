package org.openecomp.sdc.uici.tests.execute.vf;

import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.retryMethodOnException;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.http.HttpStatus;
import org.openecomp.sdc.uici.tests.datatypes.CanvasElement;
import org.openecomp.sdc.uici.tests.datatypes.CanvasManager;
import org.openecomp.sdc.uici.tests.datatypes.CreateAndUpdateStepsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openecomp.sdc.uici.tests.utilities.ArtifactUIUtils;
import org.openecomp.sdc.uici.tests.utilities.FileHandling;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.uici.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.uici.tests.utilities.RestCDUtils;
import org.openecomp.sdc.uici.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.uici.tests.verificator.ServiceVerificator;
import org.openecomp.sdc.uici.tests.verificator.VfVerificator;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceRespJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

public class VfBasicTests extends SetupCDTest {

	@Test
	public void testImportVfTableColumns() {
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.OPEN_MODAL_BUTTON.getValue()).click();

		assertTrue(GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.VENDOR_HEADER_COL.getValue()) != null);
		assertTrue(GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.NAME_HEADER_COL.getValue()) != null);
		assertTrue(GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.CATEGORY_HEADER_COL.getValue()) != null);

		assertTrue(GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.OnBoardingTable.VERSION_HEADER_COL.getValue()) != null);
	}
	
	private void testsToChangeSomeParametersValues(){
		//open parameters form
		GeneralUIUtils.moveToHTMLElementByDataTestId(DataTestIdEnum.Artifatcs.BASE_CMUI_LAB1_ARTIFACT_ITEM.getValue());
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.Artifatcs.OPEN_EDIT_PROPERTIES_FORM_OF_BASE_CMUI_LAB1.getValue()).click();
		//edit values for parameter without default value and for parameter with default
		String valueForFirstParam="111";
		String newValueForSecondParam="222";
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.EnvParametersForm.VALUE_FIELD_OF_AVAILABILITY_ZONE_0.getValue()).sendKeys(valueForFirstParam);
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.EnvParametersForm.VALUE_FIELD_OF_CMAUI_FLAVOR.getValue()).clear();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.EnvParametersForm.VALUE_FIELD_OF_CMAUI_FLAVOR.getValue()).sendKeys(newValueForSecondParam);
		//save changes
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.EnvParametersForm.SAVE_BUTTON.getValue()).click();
		GeneralUIUtils.waitForLoader();
		//open form again
		GeneralUIUtils.moveToHTMLElementByDataTestId(DataTestIdEnum.Artifatcs.BASE_CMUI_LAB1_ARTIFACT_ITEM.getValue());
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.Artifatcs.OPEN_EDIT_PROPERTIES_FORM_OF_BASE_CMUI_LAB1.getValue()).click();
		//check if values were changed
		assertTrue("The parameter value without default was not changed.",
				GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.EnvParametersForm.VALUE_FIELD_OF_AVAILABILITY_ZONE_0.getValue()).getAttribute("value").equals(valueForFirstParam));
		assertTrue("The parameter value with default was not changed.",
				GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.EnvParametersForm.VALUE_FIELD_OF_CMAUI_FLAVOR.getValue()).getAttribute("value").equals(newValueForSecondParam));
		//delete the value of the parameter without default
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.EnvParametersForm.DELETE_AVAILABILITY_ZONE_0.getValue()).click();
		boolean isThereDefaultValue=!GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.EnvParametersForm.DEFAULT_VALUE_COLMN_OF_CMAUI_FLAVOR.getValue()).getText().isEmpty();
		if(isThereDefaultValue){
			//revert the value of the parameter with default
			GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.EnvParametersForm.REVERET_CMAUI_FLAVOR.getValue()).click();
		}
		//save changes
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.EnvParametersForm.SAVE_BUTTON.getValue()).click();
		GeneralUIUtils.waitForLoader();
		//open form again
		GeneralUIUtils.moveToHTMLElementByDataTestId(DataTestIdEnum.Artifatcs.BASE_CMUI_LAB1_ARTIFACT_ITEM.getValue());
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.Artifatcs.OPEN_EDIT_PROPERTIES_FORM_OF_BASE_CMUI_LAB1.getValue()).click();
		//check if values were changed
		assertTrue("The parameter value without default was not deleted.",
				GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.EnvParametersForm.VALUE_FIELD_OF_AVAILABILITY_ZONE_0.getValue()).getAttribute("value").equals(""));
		if(isThereDefaultValue){
			String theDefaultValue="m1.large";
			assertTrue("The parameter value with default was not reverted.",
					GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.EnvParametersForm.VALUE_FIELD_OF_CMAUI_FLAVOR.getValue()).getAttribute("value").equals(theDefaultValue));
		}
	}
	
	@Test
	public void testVFiUpdateHeatENVParameters() throws Exception {
		// create vf
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "vmmc_work.csar";
		ResourceReqDetails importVfResourceInUI = ResourceUIUtils.importVfInUIWithoutCheckin(getUser(), filePath,
				fileName);
		GeneralUIUtils.closeNotificatin();
		GeneralUIUtils.checkIn();
		GeneralUIUtils.closeNotificatin();
		//create service
		ServiceReqDetails createServiceInUI = ServiceUIUtils.createServiceInUI(getUser());
		ServiceVerificator.verifyServiceCreated(createServiceInUI, getUser());
		//go to composition
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		//drag vf into canvas
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement canvasElement = canvasManager.createElementOnCanvas(importVfResourceInUI.getName());
		canvasManager.selectElementFromCanvas(canvasElement);
		GeneralUIUtils.waitForLoader();
		//go to deployment artifacts tab
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.RightBar.DEPLOYMENT_ARTIFACTS.getValue()).click();
		//test change parameters
		testsToChangeSomeParametersValues();
	}
	
	@Test
	public void testVFUpdateHeatENVParameters() throws Exception {
		// create vf
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "vmmc_work.csar";
		ResourceReqDetails importVfResourceInUI = ResourceUIUtils.importVfInUIWithoutCheckin(getUser(), filePath,
				fileName);
		//go to deployment artifacts
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.DEPLOYMENT_ARTIFACT);
		//test change parameters
		testsToChangeSomeParametersValues();	
	}

	@Test
	public void testUpdateVfCreatedFromCsar() throws Exception {
		// create vf
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "Sample_CSAR.csar";
		ResourceReqDetails importVfResourceInUI = ResourceUIUtils.importVfInUIWithoutCheckin(getUser(), filePath,
				fileName);
		// update csar
		fileName = "Sample_CSAR2.csar";
		ResourceUIUtils.updateVfCsar(filePath, fileName);
		VfVerificator.verifyNumOfComponentInstances(importVfResourceInUI, 4);
	}

	@Test
	public void testImportVf() {
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "Sample_CSAR.csar";
		ResourceReqDetails importVfResourceInUI = ResourceUIUtils.importVfInUI(getUser(), filePath, fileName);
		GeneralUIUtils.waitForLoader();
		assertTrue(RestCDUtils.getResource(importVfResourceInUI).getErrorCode() == HttpStatus.SC_OK);
	}

	@Test
	public void testCreateVf() {
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(getUser());
		assertTrue(RestCDUtils.getResource(createResourceInUI).getErrorCode() == HttpStatus.SC_OK);
	}

	@Test
	public void testDeleteInstanceFromCanvas() {
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(getUser());
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);

		CanvasManager canvasManager = CanvasManager.getCanvasManager();

		canvasManager.createElementOnCanvas(LeftPanelCanvasItems.BLOCK_STORAGE);
		CanvasElement computeElement = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.COMPUTE);
		VfVerificator.verifyNumOfComponentInstances(createResourceInUI, 2);
		canvasManager.deleteElementFromCanvas(computeElement);
		VfVerificator.verifyNumOfComponentInstances(createResourceInUI, 1);

	}

	@Test
	public void testUpdateInstanceAttributeValue() {
		// creare vfc with attrs
		String filePath = FileHandling.getResourcesFilesPath();
		String fileName = "VFCWithAttributes.yml";
		ResourceReqDetails importVfcResourceInUI = ResourceUIUtils.importVfcInUI(getUser(), filePath, fileName);
		GeneralUIUtils.checkIn();
		// create vf
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(getUser());
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		// add vfc to canvas
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement canvasElement = canvasManager.createElementOnCanvas(importVfcResourceInUI.getName());
		canvasManager.selectElementFromCanvas(canvasElement);
		// edit value of vfc attr
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.RightBar.PROPERTIES_AND_ATTRIBUTES.getValue())
				.click();
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.RightBar.MYATTR_ATTR_FROM_LIST.getValue()).click();

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.DEFAULT_VAL_FIELD.getValue())
				.sendKeys("2");
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.AttributeForm.DONE_BUTTON.getValue()).click();
		String newValue = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.RightBar.MYATTR_ATTR_VALUE_FROM_LIST.getValue()).getText();
		assertEquals("2", newValue);
	}

	@Test(enabled = false)
	public void testAddInfomratinalArtifact() throws Exception {
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(getUser());

		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.INFORMATION_ARTIFACT);

		ArtifactReqDetails informationalArtifact = ElementFactory.getDefaultArtifact();
		final String FILE_PATH = System.getProperty("user.dir") + "\\src\\main\\resources\\Files\\";
		final String FILE_NAME = "Valid_tosca_Mycompute.yml";

		ArtifactUIUtils.addInformationArtifact(informationalArtifact, FILE_PATH + FILE_NAME,
				DataTestIdEnum.InformationalArtifatcs.FEATURES);
		ArtifactUIUtils.addInformationArtifact(informationalArtifact, FILE_PATH + FILE_NAME,
				DataTestIdEnum.InformationalArtifatcs.CAPACITY);

		RestResponse getResourceResponse = RestCDUtils.getResource(createResourceInUI);
		assertEquals("Did not succeed to get resource after create", HttpStatus.SC_OK,
				getResourceResponse.getErrorCode().intValue());

		Map<String, Map<String, Object>> artifactsListFromResponse = ArtifactUIUtils
				.getArtifactsListFromResponse(getResourceResponse.getResponse(), "artifacts");
		Map<String, Object> map = artifactsListFromResponse.get("Features");

		assertTrue(artifactsListFromResponse.size() >= 2);

	}

	@Test
	public void testVfCertification() throws IOException {
		// Create VF
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(getUser());
		assertTrue(RestCDUtils.getResource(createResourceInUI).getErrorCode() == HttpStatus.SC_OK);

		// Submit For Testing Process
		GeneralUIUtils.submitForTestingElement(createResourceInUI.getName());

		// Tester
		quitAndReLogin(UserRoleEnum.TESTER);
		ResourceUIUtils.testAndAcceptElement(createResourceInUI);

		// Verification
		GeneralUIUtils.waitForLoader();
		VfVerificator.verifyResourceIsCertified(createResourceInUI);

	}

	@Test
	public void testDeploymentArtifactForVFi() {
		User user = getUser();
		// create vf
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(user);
		GeneralUIUtils.checkIn();
		GeneralUIUtils.waitForLoader();
		// create service
		GeneralUIUtils.clickOnCreateEntityFromDashboard(DataTestIdEnum.Dashboard.BUTTON_ADD_SERVICE.getValue());
		ResourceUIUtils.defineResourceName("serv");
		GeneralUIUtils.defineDescription("description");
		GeneralUIUtils.waitForLoader();
		ResourceUIUtils.defineResourceCategory("Mobility", "selectGeneralCategory");
		ResourceUIUtils.defineProjectCode("012345");
		GeneralUIUtils.clickSaveButton();
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		GeneralUIUtils.waitForLoader();
		// add vf to canvas
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement canvasElement = canvasManager.createElementOnCanvas(createResourceInUI.getName());
		canvasManager.selectElementFromCanvas(canvasElement);
		GeneralUIUtils.waitForLoader();
		// add artifact
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.RightBar.DEPLOYMENT_ARTIFACTS.getValue()).click();
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.RightBar.ADD_ARTIFACT_BUTTON.getValue()).click();
		String newArtifactLabel = "newArtifact";
		ArtifactReqDetails details = new ArtifactReqDetails("new_atifact", "DCAE_INVENTORY_EVENT", "desc", "",
				newArtifactLabel);
		ResourceUIUtils.fillinDeploymentArtifactFormAndClickDone(details,
				FileHandling.getResourcesFilesPath() + "yamlSample.yml");
		assertTrue(GeneralUIUtils.isElementPresent("artifact_Display_Name-" + newArtifactLabel));
		// edit artifact
		GeneralUIUtils.getWebElementWaitForClickable("artifact_Display_Name-" + newArtifactLabel).click();
		String newFileName = "yamlSample2.yml";
		retryMethodOnException(
				() -> GeneralUIUtils.getWebElementByDataTestId(DataTestIdEnum.GeneralSection.BROWSE_BUTTON.getValue())
						.sendKeys(FileHandling.getResourcesFilesPath() + newFileName));
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.DONE.getValue()).click();
		GeneralUIUtils.waitForLoader();
		assertEquals(newFileName,
				GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.RightBar.ARTIFACT_NAME.getValue()).getText());
		// delete artifact
		GeneralUIUtils.moveToHTMLElementByDataTestId("artifact_Display_Name-" + newArtifactLabel);
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.RightBar.DELETE_ARTIFACT_BUTTON.getValue()).click();
		GeneralUIUtils.getWebElementWaitForClickable(DataTestIdEnum.ModalItems.OK.getValue()).click();
		GeneralUIUtils.waitForLoader();
		assertTrue(!GeneralUIUtils.isElementPresent("artifact_Display_Name-" + newArtifactLabel));
	}
	
	@Test
	public void testDisplayVfModuleProperies() {
		//create vf with components instances properties
		ResourceReqDetails importedVf = ResourceUIUtils.importVfInUI(getUser(), FileHandling.getResourcesFilesPath(),
				"vmmc_work.csar");
		GeneralUIUtils.waitForLoader(40);
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.DEPLOYMENT);
		GeneralUIUtils.getWebElementWaitForClickable("hierarchy-module-0-title").click();
		assertTrue(GeneralUIUtils.isElementPresent(DataTestIdEnum.DeploymentSection.MODULE_PROPERTIES_HEADER_LIST.getValue()));
	}

	protected ArtifactReqDetails defineInformationalArtifact() throws IOException, Exception {
		return ElementFactory.getDefaultArtifact();
	}

	protected ResourceRespJavaObject buildResourceJavaObject(ResourceReqDetails resource, RestResponse restResponse,
			User user) {
		ResourceRespJavaObject resourceObject = new ResourceRespJavaObject();
		resourceObject = Convertor.constructFieldsForRespValidation(resource, resource.getVersion(), user);
		resourceObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceObject.setAbstractt("false");
		resourceObject.setIcon(resource.getIcon().replace(" ", ""));
		resourceObject.setUniqueId(ResponseParser.getUniqueIdFromResponse(restResponse));
		return resourceObject;
	}

}
