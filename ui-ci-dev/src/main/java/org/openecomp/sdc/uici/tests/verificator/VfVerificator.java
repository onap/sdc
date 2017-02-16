package org.openecomp.sdc.uici.tests.verificator;

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.uici.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.uici.tests.utilities.RestCDUtils;

import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

/**
 * Class to hold Test Verifications relevant for VF
 * 
 * @author mshitrit
 *
 */
public final class VfVerificator {
	private VfVerificator() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Verifies that the resource contains a certain number of component
	 * instances
	 * 
	 * @param createResourceInUI
	 * @param numOfVFC
	 */
	public static void verifyNumOfComponentInstances(ResourceReqDetails createResourceInUI, int numOfVFC) {
		Supplier<Boolean> verificator = () -> {
			String responseAfterDrag = RestCDUtils.getResource(createResourceInUI).getResponse();
			JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
			int size = ((JSONArray) jsonResource.get("componentInstances")).size();
			return size == numOfVFC;
		};
		VerificatorUtil.verifyWithRetry(verificator);
	}

	/**
	 * Verifies That the createResourceInUI is different that prevRIPos.
	 * 
	 * @param createResourceInUI
	 * @param prevRIPos
	 * @param user
	 */
	public static void verifyRILocationChanged(ResourceReqDetails createResourceInUI,
			ImmutablePair<String, String> prevRIPos, User user) {
		Supplier<Boolean> verificator = () -> {
			ImmutablePair<String, String> currRIPos = ResourceUIUtils.getRIPosition(createResourceInUI, user);
			final boolean isXLocationChanged = !prevRIPos.left.equals(currRIPos.left);
			final boolean isYLocationChange = !prevRIPos.right.equals(currRIPos.right);
			return isXLocationChanged || isYLocationChange;
		};
		VerificatorUtil.verifyWithRetry(verificator);
	}

	/**
	 * Verifies That resource contains two connected instances
	 * 
	 * @param createResourceInUI
	 */
	public static void verifyLinkCreated(ResourceReqDetails createResourceInUI) {
		Supplier<Boolean> verificator = () -> {
			String responseAfterDrag = RestCDUtils.getResource(createResourceInUI).getResponse();
			JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
			return ((JSONArray) jsonResource.get("componentInstancesRelations")).size() == 1;
		};
		VerificatorUtil.verifyWithRetry(verificator);

	}

	/**
	 * Verifies That the VF is certified to version 1.0
	 * 
	 * @param vfToVerify
	 */
	public static void verifyResourceIsCertified(ResourceReqDetails vfToVerify) {
		RestResponse certifiedResourceResopnse = RestCDUtils
				.getResourceByNameAndVersionRetryOnFail(UserRoleEnum.ADMIN.getUserId(), vfToVerify.getName(), "1.0");
		assertTrue(certifiedResourceResopnse.getErrorCode().equals(HttpStatus.SC_OK));

	}

	/**
	 * Verifies That the VF exist
	 * 
	 * @param vfToVerify
	 */
	public static void verifyResourceIsCreated(ResourceReqDetails vfToVerify) {
		assertTrue(RestCDUtils.getResource(vfToVerify).getErrorCode() == HttpStatus.SC_OK);
	}

	/**
	 * Verify the resource contains the deployment artifacts in the list
	 * 
	 * @param vfToVerify
	 * @param artifactTypeEnums
	 */
	public static void verifyResourceContainsDeploymentArtifacts(ResourceReqDetails vfToVerify,
			List<ArtifactTypeEnum> artifactTypeEnums) {
		String resourceString = RestCDUtils.getResource(vfToVerify).getResponse();
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(resourceString);
		List<String> foundArtifacts = new ArrayList<>();
		if (resource.getDeploymentArtifacts() != null) {
			foundArtifacts = resource.getDeploymentArtifacts().values().stream()
					.map(artifact -> artifact.getArtifactType()).collect(Collectors.toList());
		}
		List<String> excpectedArtifacts = artifactTypeEnums.stream().map(e -> e.getType()).collect(Collectors.toList());
		assertTrue(foundArtifacts.containsAll(excpectedArtifacts));

	}

	/**
	 * Verifies The life cycle State of the resource
	 * 
	 * @param createResourceInUI
	 * @param requestedLifeCycleState
	 */
	public static void verifyState(ResourceReqDetails createResourceInUI, LifecycleStateEnum requestedLifeCycleState) {
		Resource resource = ResourceUIUtils.waitForState(createResourceInUI, requestedLifeCycleState);
		assertTrue(resource.getLifecycleState() == requestedLifeCycleState);

	}

}
