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

package org.openecomp.sdc.ci.tests.execute.service;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.lifecycle.LCSbaseTest;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetAllServiceVersions extends ComponentBaseTest {
	protected ResourceReqDetails resourceDetails;
	protected ServiceReqDetails serviceDetails;
	protected User sdncDesignerDetails;
	protected User sdncDesignerDetails2;
	protected User sdncAdminDetails;
	protected User sdncGovernorDeatails;
	protected User sdncTesterDetails;
	protected User sdncOpsDetails;
	protected ComponentInstanceReqDetails resourceInstanceReqDetails;
	protected Component resourceDetailsVFCcomp;
	protected Service serviceServ;

	@Rule
	public static TestName name = new TestName();

	public GetAllServiceVersions() {
		super(name, GetAllServiceVersions.class.getName());
		;
	}

	@BeforeMethod
	public void setUp() throws Exception {

		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncDesignerDetails2 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2);
		sdncAdminDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncGovernorDeatails = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);
		sdncTesterDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		sdncOpsDetails = ElementFactory.getDefaultUser(UserRoleEnum.OPS);
		resourceDetailsVFCcomp = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVFCcomp, UserRoleEnum.DESIGNER,
				true, true);

		AtomicOperationUtils.changeComponentState(resourceDetailsVFCcomp, UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CERTIFY, true);
		serviceServ = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVFCcomp, serviceServ,
				UserRoleEnum.DESIGNER, true);

		serviceDetails = new ServiceReqDetails(serviceServ);

	}

	@Test
	public void GetAllServiceVersions_Version05() throws Exception {

		Map<String, String> origVersionsMap = new HashMap<String, String>();
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());
		for (int x = 0; x < 4; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
			origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		}
		// validate get response
		RestResponse serviceGetResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		Service res = ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	@Test
	public void GetAllServiceVersions_Version01() throws Exception {

		Map<String, String> origVersionsMap = new HashMap<String, String>();
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		RestResponse serviceGetResponse = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		Service res = ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));
	}

	@Test
	public void GetAllServiceVersions_Version15() throws Exception {
		// addMandatoryArtifactsToService();
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		for (int x = 0; x < 4; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
		}

		RestResponse changeServiceState = LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
		// serviceServ.setUniqueId(serviceDetails.getUniqueId());
		// RestResponse changeServiceState =
		// AtomicOperationUtils.changeComponentState(serviceServ,
		// UserRoleEnum.ADMIN, LifeCycleStatesEnum.CERTIFY, false).getRight();

		assertTrue("certify service request returned status:" + changeServiceState.getErrorCode(),
				changeServiceState.getErrorCode() == 200);
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		for (int x = 0; x < 5; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		}

		// validate get response
		RestResponse serviceGetResponse = ServiceRestUtils.getService(serviceDetails, sdncAdminDetails);
		Service res = ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));
	}

	@Test
	public void GetAllServiceVersions_Version25() throws Exception {
		// addMandatoryArtifactsToService();
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		for (int x = 0; x < 4; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
		}

		// getting to version 1.0
		RestResponse changeServiceState = LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
		assertTrue("certify service request returned status:" + changeServiceState.getErrorCode(),
				changeServiceState.getErrorCode() == 200);
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		// getting to version 1.5
		for (int x = 0; x < 5; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
		}

		// getting to version 2.0
		changeServiceState = LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
		assertTrue("certify service request returned status:" + changeServiceState.getErrorCode(),
				changeServiceState.getErrorCode() == 200);
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		// getting to version 2.5
		for (int x = 0; x < 5; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
			origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());
		}

		// validate get response
		RestResponse serviceGetResponse = ServiceRestUtils.getService(serviceDetails, sdncAdminDetails);
		Service res = ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));
	}

	@Test
	public void GetAllServiceVersions_ReadyForCertification_version05() throws Exception {
		// addMandatoryArtifactsToService();
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());
		for (int x = 0; x < 4; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
			origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());
		}

		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);

		// validate get response
		RestResponse serviceGetResponse = ServiceRestUtils.getService(serviceDetails, sdncAdminDetails);
		Service res = ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));
	}

	@Test
	public void GetAllServiceVersions_CertifactionInProgress_version05() throws Exception {
		// addMandatoryArtifactsToService();
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());
		for (int x = 0; x < 4; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
			origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());
		}

		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails, serviceDetails.getVersion(),
				LifeCycleStatesEnum.STARTCERTIFICATION);

		// validate get response
		RestResponse serviceGetResponse = ServiceRestUtils.getService(serviceDetails, sdncAdminDetails);
		Service res = ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));
	}

	@Test
	public void GetAllServiceVersions_Certified_version10() throws Exception {
		// addMandatoryArtifactsToService();
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		// get to version 0.5
		for (int x = 0; x < 4; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);

		}

		// get version 1.0
		RestResponse changeServiceState = LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
		assertTrue("certify service request returned status:" + changeServiceState.getErrorCode(),
				changeServiceState.getErrorCode() == 200);
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		// validate get response
		RestResponse serviceGetResponse = ServiceRestUtils.getService(serviceDetails, sdncAdminDetails);
		Service res = ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));
	}

	@Test
	public void GetAllServiceVersions_Certified_version20() throws Exception {
		// addMandatoryArtifactsToService();
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		// get to version 0.5
		for (int x = 0; x < 4; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
		}

		// get version 1.0
		RestResponse changeServiceState = LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
		assertTrue("certify service request returned status:" + changeServiceState.getErrorCode(),
				changeServiceState.getErrorCode() == 200);
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		// get version 1.5
		for (int x = 0; x < 4; x++) {
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKIN);
			LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails, serviceDetails.getVersion(),
					LifeCycleStatesEnum.CHECKOUT);
		}

		// get version 2.0
		changeServiceState = LCSbaseTest.certifyService(serviceDetails, sdncDesignerDetails);
		assertTrue("certify service request returned status:" + changeServiceState.getErrorCode(),
				changeServiceState.getErrorCode() == 200);
		origVersionsMap.put(serviceDetails.getVersion(), serviceDetails.getUniqueId());

		// validate get response
		RestResponse serviceGetResponse = ServiceRestUtils.getService(serviceDetails, sdncAdminDetails);
		Service res = ResponseParser.convertServiceResponseToJavaObject(serviceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));
	}

	@Test
	public void GetAllServiceVersions_ServiceNotFound() throws Exception {

		RestResponse serviceGetResponse = ServiceRestUtils.getService("123456789", sdncAdminDetails);
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.SERVICE_NOT_FOUND.name());
		assertEquals("Check response code after get service without cache", errorInfo.getCode(),
				serviceGetResponse.getErrorCode());

		List<String> variables = Arrays.asList("123456789");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), variables,
				serviceGetResponse.getResponse());

	}

}
