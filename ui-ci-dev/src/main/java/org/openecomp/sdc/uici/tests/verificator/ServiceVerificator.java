package org.openecomp.sdc.uici.tests.verificator;

import static org.testng.AssertJUnit.assertTrue;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.uici.tests.utilities.RestCDUtils;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;

public class ServiceVerificator {
	public static void verifyNumOfComponentInstances(ServiceReqDetails createServiceInUI, int numOfVFC, User user) {
		String responseAfterDrag = RestCDUtils.getService(createServiceInUI, user).getResponse();
		JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
		int size = ((JSONArray) jsonResource.get("componentInstances")).size();
		assertTrue(size == numOfVFC);
	}

	public static void verifyLinkCreated(ServiceReqDetails createServiceInUI, User user) {
		String responseAfterDrag = RestCDUtils.getService(createServiceInUI, user).getResponse();
		JSONObject jsonService = (JSONObject) JSONValue.parse(responseAfterDrag);
		assertTrue(((JSONArray) jsonService.get("componentInstancesRelations")).size() == 1);

	}

	public static void verifyServiceCreated(ServiceReqDetails createServiceInUI, User user) {
		assertTrue(RestCDUtils.getService(createServiceInUI, user).getErrorCode() == HttpStatus.SC_OK);

	}

	/**
	 * Verifies service is certified with version 1.0
	 * 
	 * @param createServiceInUI
	 * @param user
	 */
	public static void verifyServiceCertified(ServiceReqDetails createServiceInUI, User user) {
		Supplier<RestResponse> serviceGetter = () -> FunctionalInterfaces.swallowException(
				() -> ServiceRestUtils.getServiceByNameAndVersion(user, createServiceInUI.getName(), "1.0"));
		Function<RestResponse, Boolean> serviceVerificator = restResp -> restResp.getErrorCode() == HttpStatus.SC_OK;
		RestResponse certifiedResourceResopnse = FunctionalInterfaces.retryMethodOnResult(serviceGetter,
				serviceVerificator);
		assertTrue(certifiedResourceResopnse.getErrorCode() == HttpStatus.SC_OK);

	}
}
