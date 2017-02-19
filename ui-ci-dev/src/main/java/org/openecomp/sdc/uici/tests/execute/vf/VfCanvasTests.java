package org.openecomp.sdc.uici.tests.execute.vf;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.uici.tests.datatypes.CanvasElement;
import org.openecomp.sdc.uici.tests.datatypes.CanvasManager;
import org.openecomp.sdc.uici.tests.datatypes.CreateAndUpdateStepsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.LeftPanelCanvasItems;
import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.uici.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.uici.tests.verificator.VfVerificator;
import org.testng.annotations.Test;

import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;

public class VfCanvasTests extends SetupCDTest {

	@Test
	public void testCanvasDrag() {
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(getUser());
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);

		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement createElementOnCanvas = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.BLOCK_STORAGE);

		ImmutablePair<String, String> preMovePos = ResourceUIUtils.getRIPosition(createResourceInUI, getUser());

		canvasManager.moveElementOnCanvas(createElementOnCanvas);

		VfVerificator.verifyRILocationChanged(createResourceInUI, preMovePos, getUser());

	}

	@Test
	public void testCanvasConnectComponents() {
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(getUser());
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);

		CanvasManager canvasManager = CanvasManager.getCanvasManager();
		CanvasElement bsElement = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.BLOCK_STORAGE);
		CanvasElement computeElement = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.COMPUTE);

		canvasManager.linkElements(bsElement, computeElement);

		VfVerificator.verifyLinkCreated(createResourceInUI);

	}

	@Test
	public void testCanvasVFSanity() {
		ResourceReqDetails createResourceInUI = ResourceUIUtils.createResourceInUI(getUser());
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.COMPOSITION);
		CanvasManager canvasManager = CanvasManager.getCanvasManager();

		CanvasElement bsElement = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.BLOCK_STORAGE);
		CanvasElement compElement = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.COMPUTE);

		ImmutablePair<String, String> preMovePos = ResourceUIUtils.getRIPosition(createResourceInUI, getUser());
		canvasManager.moveElementOnCanvas(bsElement);
		canvasManager.moveElementOnCanvas(compElement);

		VfVerificator.verifyRILocationChanged(createResourceInUI, preMovePos, getUser());

		CanvasElement bsElement2 = canvasManager.createElementOnCanvas(LeftPanelCanvasItems.BLOCK_STORAGE);

		canvasManager.linkElements(bsElement2, compElement);

		VfVerificator.verifyLinkCreated(createResourceInUI);

		VfVerificator.verifyNumOfComponentInstances(createResourceInUI, 3);

		canvasManager.moveElementOnCanvas(compElement);

		canvasManager.deleteElementFromCanvas(bsElement);

		VfVerificator.verifyNumOfComponentInstances(createResourceInUI, 2);

	}

}
