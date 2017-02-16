package org.openecomp.sdc.uici.tests.execute.service;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

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

import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class ServiceBasicTests extends SetupCDTest {

	@Test
	public void testCreateService() {
		ServiceReqDetails createServiceInUI = ServiceUIUtils.createServiceInUI(getUser());
		ServiceVerificator.verifyServiceCreated(createServiceInUI, getUser());
	}

	@Test
	public void testLinkTwoRI() {

		// create 1st VF
		ResourceReqDetails resourceOne = ResourceUIUtils.createResourceInUI(getUser());
		assertTrue(RestCDUtils.getResource(resourceOne).getErrorCode() == HttpStatus.SC_OK);
		// add LoadBalancer to resource
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		canvasManager.createElementOnCanvas(LeftPanelCanvasItems.OBJECT_STORAGE);
		GeneralUIUtils.checkIn();

		// create 2nd VF
		ResourceReqDetails resourceTwo = ResourceUIUtils.createResourceInUI(getUser());
		assertTrue(RestCDUtils.getResource(resourceTwo).getErrorCode() == HttpStatus.SC_OK);
		// add ObjectStorage to resource
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		canvasManager = CanvasManager.getCanvasManager();
		canvasManager.createElementOnCanvas(LeftPanelCanvasItems.LOAD_BALANCER);
		GeneralUIUtils.checkIn();

		// create service
		ServiceReqDetails createServiceInUI = ServiceUIUtils.createServiceInUI(getUser());
		// Verify Service is Created
		ServiceVerificator.verifyServiceCreated(createServiceInUI, getUser());

		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		canvasManager = CanvasManager.getCanvasManager();

		// adding two resource instances
		CanvasElement vfOne = canvasManager.createElementOnCanvas(resourceOne.getName());

		CanvasElement vfTwo = canvasManager.createElementOnCanvas(resourceTwo.getName());
		// link elements
		canvasManager.linkElements(vfOne, vfTwo);

		// check results
		ServiceVerificator.verifyServiceCreated(createServiceInUI, getUser());
		ServiceVerificator.verifyLinkCreated(createServiceInUI, getUser());

	}

	/**
	 * This method tests the following: <br>
	 * 1. Import of VF <br>
	 * 2. Certification Of Vf <br>
	 * 3. Adding deployment artifact to VF <br>
	 * 4. Creation of Service <br>
	 * 5. Adding Vf instance to Service <br>
	 * 6. Service Certification <br>
	 * 7. Approving Service to distribution by Governor <br>
	 * 8. Making sure service is ready to distribute by ops <br>
	 */
	@Test
	public void testBuildServiceForDistribution() {
		ResourceReqDetails importedVf = ResourceUIUtils.importVfInUI(getUser(), FileHandling.getResourcesFilesPath(),
				"valid_vf.csar");
		GeneralUIUtils.waitForLoader(20);
		// Verify Import
		VfVerificator.verifyResourceIsCreated(importedVf);

		// Create Deployment Artifact
		ArtifactUIUtils.createDeploymentArtifactOnVf(FileHandling.getResourcesFilesPath() + "myYang.xml",
				ArtifactTypeEnum.YANG_XML);
		VfVerificator.verifyResourceContainsDeploymentArtifacts(importedVf,
				Arrays.asList(new ArtifactTypeEnum[] { ArtifactTypeEnum.YANG_XML }));

		// Submit For Testing Process VF
		GeneralUIUtils.submitForTestingElement(importedVf.getName());

		// Certify The VF
		quitAndReLogin(UserRoleEnum.TESTER);
		ResourceUIUtils.testAndAcceptElement(importedVf);

		// Verify Certification
		GeneralUIUtils.waitForLoader();
		VfVerificator.verifyResourceIsCertified(importedVf);

		// Create Service
		quitAndReLogin(UserRoleEnum.DESIGNER);
		ServiceReqDetails createServiceInUI = ServiceUIUtils.createServiceInUI(getUser());
		ServiceVerificator.verifyServiceCreated(createServiceInUI, getUser());

		// Drag the VF To the Service
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		canvasManager.createElementOnCanvas(importedVf.getName());

		// Submit For Testing Process Service
		GeneralUIUtils.submitForTestingElement(null);

		// Certify The Service
		quitAndReLogin(UserRoleEnum.TESTER);
		ResourceUIUtils.testAndAcceptElement(createServiceInUI);
		ServiceVerificator.verifyServiceCertified(createServiceInUI, getUser());

		// Approve with governor
		quitAndReLogin(UserRoleEnum.GOVERNOR);
		ServiceUIUtils.approveServiceForDistribution(createServiceInUI);

		// Log in with Ops and verify that can distribute
		quitAndReLogin(UserRoleEnum.OPS);
		GeneralUIUtils.getWebElementWaitForVisible(createServiceInUI.getName()).click();
		WebElement distributeWebElement = GeneralUIUtils
				.getWebElementWaitForVisible(DataTestIdEnum.LifeCyleChangeButtons.DISTRIBUTE.getValue());
		assertTrue(distributeWebElement != null);

	}

}
