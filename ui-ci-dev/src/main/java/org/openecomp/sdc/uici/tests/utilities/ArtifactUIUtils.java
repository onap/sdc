package org.openecomp.sdc.uici.tests.utilities;

import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.retryMethodOnException;

import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.uici.tests.datatypes.CreateAndUpdateStepsEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.Artifatcs;
import org.openecomp.sdc.uici.tests.datatypes.DataTestIdEnum.InformationalArtifatcs;
import org.openqa.selenium.WebElement;

import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

public final class ArtifactUIUtils {

	private ArtifactUIUtils() {
		throw new UnsupportedOperationException();
	}

	public static void addInformationArtifact(ArtifactReqDetails artifact, String filePath,
			final InformationalArtifatcs dataTestEnum) {
		GeneralUIUtils.waitForLoader();
		GeneralUIUtils.sleep(2000);
		GeneralUIUtils.getWebElementWaitForVisible(dataTestEnum.getValue()).click();

		final WebElement browseWebElement = FunctionalInterfaces.retryMethodOnException(
				() -> GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.BROWSE_BUTTON.getValue()));
		browseWebElement.sendKeys(filePath);

		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.DESCRIPTION.getValue())
				.sendKeys(artifact.getDescription());
		GeneralUIUtils.getWebElementWaitForVisible(DataTestIdEnum.ModalItems.DONE.getValue()).click();

	}

	public static Map<String, Map<String, Object>> getArtifactsListFromResponse(String jsonResponse,
			String fieldOfArtifactList) {
		JSONObject object = (JSONObject) JSONValue.parse(jsonResponse);
		Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) object.get(fieldOfArtifactList);
		return map;
	}

	/**
	 * Creates a deployment artifact on the vf. <br>
	 * Moves automatically to DeploymentArtifact Section
	 * 
	 * @param artifactPayloadPath
	 * @param artifactType
	 */
	public static void createDeploymentArtifactOnVf(final String artifactPayloadPath,
			final ArtifactTypeEnum artifactType) {
		GeneralUIUtils.moveToStep(CreateAndUpdateStepsEnum.DEPLOYMENT_ARTIFACT);
		GeneralUIUtils.getWebElementWaitForClickable(Artifatcs.ADD_DEPLOYMENT_ARTIFACT.getValue()).click();
		GeneralUIUtils.getSelectList("Create New Artifact", Artifatcs.SELECT_ARTIFACT_DROPDOWN.getValue());
		GeneralUIUtils.getSelectList(artifactType.getType(), Artifatcs.ARTIFACT_TYPE_DROPDOWN.getValue());
		GeneralUIUtils.getWebElementWaitForVisible(Artifatcs.ARTIFACT_DESCRIPTION.getValue())
				.sendKeys("Artifact Description");
		GeneralUIUtils.getWebElementWaitForVisible(Artifatcs.ARTIFACT_LABEL.getValue()).sendKeys("MyArtifactLabel");
		retryMethodOnException(() -> GeneralUIUtils.getWebElementByDataTestId(Artifatcs.BROWSE_BUTTON.getValue())
				.sendKeys(artifactPayloadPath));
		GeneralUIUtils.getWebElementWaitForVisible(Artifatcs.ADD_BUTTON.getValue()).click();
		GeneralUIUtils.waitForLoader();
	}

}
