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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
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
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ServiceValidationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChangeServiceDistributionStatusApiTest extends ComponentBaseTest {

	protected ResourceReqDetails resourceDetails;
	protected ServiceReqDetails serviceDetails;
	protected User sdncDesignerDetails;
	protected User sdncAdminDetails;
	protected User sdncGovernorDeatails;
	protected User sdncTesterDetails;
	protected User sdncOpsDetails;
	protected ComponentInstanceReqDetails resourceInstanceReqDetails;
	protected Component resourceDetailsVFCcomp;
	protected Component serviceDetailsCompp;

	private String userRemarks = "commentTest";

	private List<String> variablesAsList;

	@Rule
	public static TestName name = new TestName();

	public ChangeServiceDistributionStatusApiTest() throws Exception {
		super(name, ChangeServiceDistributionStatusApiTest.class.getName());

	}

	@BeforeMethod
	public void init() throws Exception {

		variablesAsList = new ArrayList<String>();
		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
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
		Service serviceServ = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVFCcomp, serviceServ,
				UserRoleEnum.DESIGNER, true);

		serviceDetails = new ServiceReqDetails(serviceServ);

	}

	// -----------------------------------------------T E S T
	// S--------------------------------------------//

	@Test
	public void approveNotCertifiedService_checkout() throws Exception {
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 403, serviceDetails.getVersion());

		variablesAsList = Arrays.asList(serviceDetails.getVersion(), serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name(),
				variablesAsList, changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		validateAudit("DApprove", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				"403", ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, sdncAdminDetails);
	}

	@Test
	public void approveNotCertifiedService_checkedin() throws Exception {
		RestResponse checkinResp = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals(200, checkinResp.getErrorCode().intValue());

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 403, serviceDetails.getVersion());

		variablesAsList = Arrays.asList(serviceDetails.getVersion(), serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name(),
				variablesAsList, changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		validateAudit("DApprove", LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				"403", ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, sdncAdminDetails);
	}

	@Test
	public void approveNotCertifiedService_inProgress() throws Exception {
		RestResponse certReqResp = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(200, certReqResp.getErrorCode().intValue());

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 403, serviceDetails.getVersion());

		variablesAsList = Arrays.asList(serviceDetails.getVersion(), serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name(),
				variablesAsList, changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name());
		// String auditAction="DApprove";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(serviceDetails,
		// version, sdncAdminDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.READY_FOR_CERTIFICATION.name());
		// expectedResourceAuditJavaObject.setDprevStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setStatus("403");
		// expectedResourceAuditJavaObject.setDesc(String.format(errorInfo.getMessageId()
		// + ": " + errorInfo.getMessage(), version,
		// serviceDetails.getServiceName()));
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DApprove", LifecycleStateEnum.READY_FOR_CERTIFICATION,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				"403", ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, sdncAdminDetails);

	}

	@Test
	public void approveNotCertifiedService_readyForCer() throws Exception {
		approveNotCertifiedService_inProgress();
		DbUtils.deleteFromEsDbByPattern("_all");

		RestResponse startCertResp = LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(200, startCertResp.getErrorCode().intValue());

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 403, serviceDetails.getVersion());

		variablesAsList = Arrays.asList(serviceDetails.getVersion(), serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name(),
				variablesAsList, changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name());
		// String auditAction="DApprove";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(serviceDetails,
		// version, sdncAdminDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setDprevStatus("");
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setStatus("403");
		// expectedResourceAuditJavaObject.setDesc(String.format(errorInfo.getMessageId()
		// + ": " + errorInfo.getMessage(), version,
		// serviceDetails.getServiceName()));
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DApprove", LifecycleStateEnum.CERTIFICATION_IN_PROGRESS,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				"403", ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, sdncAdminDetails);
	}

	@Test
	public void rejectNotCertifiedService_checkeout() throws Exception {
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 403, serviceDetails.getVersion());

		variablesAsList = Arrays.asList(serviceDetails.getVersion(), serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name(),
				variablesAsList, changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name());
		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(serviceDetails,
		// version, sdncAdminDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setDprevStatus("");
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setStatus("403");
		// expectedResourceAuditJavaObject.setDesc(String.format(errorInfo.getMessageId()
		// + ": " + errorInfo.getMessage(), version,
		// serviceDetails.getServiceName()));
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				"403", ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, sdncAdminDetails);
	}

	@Test
	public void rejectNotCertifiedService_checkedin() throws Exception {
		RestResponse startCertResp = LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals(200, startCertResp.getErrorCode().intValue());

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 403, serviceDetails.getVersion());

		variablesAsList = Arrays.asList(serviceDetails.getVersion(), serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name(),
				variablesAsList, changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name());
		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(serviceDetails,
		// version, sdncAdminDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN.name());
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setDprevStatus("");
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setStatus("403");
		// expectedResourceAuditJavaObject.setDesc(String.format(errorInfo.getMessageId()
		// + ": " + errorInfo.getMessage(), version,
		// serviceDetails.getServiceName()));
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.NOT_CERTIFIED_CHECKIN,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				"403", ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, sdncAdminDetails);
	}

	@Test
	public void rejectNotCertifiedService_inProgress() throws Exception {
		RestResponse startCertResp = LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(200, startCertResp.getErrorCode().intValue());

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 403, serviceDetails.getVersion());

		variablesAsList = Arrays.asList(serviceDetails.getVersion(), serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name(),
				variablesAsList, changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name());
		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(serviceDetails,
		// version, sdncAdminDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.READY_FOR_CERTIFICATION.name());
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setDprevStatus("");
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setStatus("403");
		// expectedResourceAuditJavaObject.setDesc(String.format(errorInfo.getMessageId()
		// + ": " + errorInfo.getMessage(), version,
		// serviceDetails.getServiceName()));
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.READY_FOR_CERTIFICATION,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				"403", ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, sdncAdminDetails);
	}

	@Test
	public void rejectNotCertifiedService_readyForCer() throws Exception {
		rejectNotCertifiedService_inProgress();
		DbUtils.deleteFromEsDbByPattern("_all");

		RestResponse startCertResp = LifecycleRestUtils.changeServiceState(serviceDetails, sdncAdminDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(200, startCertResp.getErrorCode().intValue());

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 403, serviceDetails.getVersion());

		variablesAsList = Arrays.asList(serviceDetails.getVersion(), serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name(),
				variablesAsList, changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION.name());
		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(serviceDetails,
		// version, sdncAdminDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setDprevStatus("");
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setStatus("403");
		// expectedResourceAuditJavaObject.setDesc(String.format(errorInfo.getMessageId()
		// + ": " + errorInfo.getMessage(), version,
		// serviceDetails.getServiceName()));
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.CERTIFICATION_IN_PROGRESS,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				"403", ActionStatus.SERVICE_NOT_AVAILABLE_FOR_DISTRIBUTION, sdncAdminDetails);

	}

	@Test
	public void approveCertifiedService_bysdncGovernorDeatails() throws Exception {

		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncGovernorDeatails, 200, certifyService.getVersion());
		getDistrubtionStatusValue(changeDistStatusAndValidate, DistributionStatusEnum.DISTRIBUTION_APPROVED);

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_APPROVED);

		validateAudit("DApprove", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_APPROVED, null, null, sdncGovernorDeatails);
	}

	@Test
	public void approveCertifiedService_bysdncAdminDetails() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 200, certifyService.getVersion());
		getDistrubtionStatusValue(changeDistStatusAndValidate, DistributionStatusEnum.DISTRIBUTION_APPROVED);

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_APPROVED);

		validateAudit("DApprove", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_APPROVED, null, null, sdncAdminDetails);
	}

	@Test
	public void approveCertifiedService_byDesigner() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncDesignerDetails, 409, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.RESTRICTED_OPERATION.name());
		// String auditAction="DApprove";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(certifyService,
		// certifyService.getVersion(), sdncDesignerDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.name());
		// expectedResourceAuditJavaObject.setDprevStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setStatus("409");
		// expectedResourceAuditJavaObject.setDesc(errorInfo.getMessageId() + ":
		// " + errorInfo.getMessage());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DApprove", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, "409", ActionStatus.RESTRICTED_OPERATION,
				sdncDesignerDetails);
	}

	@Test
	public void approveCertifiedService_byTester() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncTesterDetails, 409, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.RESTRICTED_OPERATION.name());
		// String auditAction="DApprove";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(certifyService,
		// certifyService.getVersion(), sdncTesterDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.name());
		// expectedResourceAuditJavaObject.setDprevStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setStatus("409");
		// expectedResourceAuditJavaObject.setDesc(errorInfo.getMessageId() + ":
		// " + errorInfo.getMessage());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DApprove", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, "409", ActionStatus.RESTRICTED_OPERATION,
				sdncTesterDetails);
	}

	@Test
	public void approveCertifiedService_byOps() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncOpsDetails, 409, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		validateAudit("DApprove", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, "409", ActionStatus.RESTRICTED_OPERATION,
				sdncOpsDetails);

	}

	@Test
	public void rejectCertifiedService_bysdncGovernorDeatails() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncGovernorDeatails, 200, certifyService.getVersion());
		getDistrubtionStatusValue(changeDistStatusAndValidate, DistributionStatusEnum.DISTRIBUTION_REJECTED);

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_REJECTED);

		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(certifyService,
		// certifyService.getVersion(), sdncGovernorDeatails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.name());
		// expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
		// expectedResourceAuditJavaObject.setDprevStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_REJECTED.name());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_REJECTED, null, null, sdncGovernorDeatails);

	}

	@Test
	public void rejectCertifiedService_bysdncAdminDetails() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 200, certifyService.getVersion());
		getDistrubtionStatusValue(changeDistStatusAndValidate, DistributionStatusEnum.DISTRIBUTION_REJECTED);

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_REJECTED);

		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(certifyService,
		// certifyService.getVersion(), sdncAdminDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.name());
		// expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
		// expectedResourceAuditJavaObject.setDprevStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_REJECTED.name());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_REJECTED, null, null, sdncAdminDetails);
	}

	@Test
	public void rejectCertifiedService_byDesigner() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncDesignerDetails, 409, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.RESTRICTED_OPERATION.name());
		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(certifyService,
		// certifyService.getVersion(), sdncDesignerDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.name());
		// expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
		// expectedResourceAuditJavaObject.setDprevStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setStatus("409");
		// expectedResourceAuditJavaObject.setDesc(errorInfo.getMessageId() + ":
		// " + errorInfo.getMessage());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, "409", ActionStatus.RESTRICTED_OPERATION,
				sdncDesignerDetails);
	}

	@Test
	public void rejectCertifiedService_byTester() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncTesterDetails, 409, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.RESTRICTED_OPERATION.name());
		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(certifyService,
		// certifyService.getVersion(), sdncTesterDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.name());
		// expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
		// expectedResourceAuditJavaObject.setDprevStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setStatus("409");
		// expectedResourceAuditJavaObject.setDesc(errorInfo.getMessageId() + ":
		// " + errorInfo.getMessage());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, "409", ActionStatus.RESTRICTED_OPERATION,
				sdncTesterDetails);
	}

	@Test
	public void rejectCertifiedService_byOps() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncOpsDetails, 409, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

		// ErrorInfo errorInfo =
		// utils.parseYaml(ActionStatus.RESTRICTED_OPERATION.name());
		// String auditAction="DReject";
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// ServiceValidationUtils.constructFieldsForAuditValidation(certifyService,
		// certifyService.getVersion(), sdncOpsDetails);
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setResourceType("Service");
		// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.CERTIFIED.name());
		// expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS.name());
		// expectedResourceAuditJavaObject.setDprevStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setDcurrStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED.name());
		// expectedResourceAuditJavaObject.setStatus("409");
		// expectedResourceAuditJavaObject.setDesc(errorInfo.getMessageId() + ":
		// " + errorInfo.getMessage());
		// expectedResourceAuditJavaObject.setComment(userRemarks);
		// expectedResourceAuditJavaObject.setPrevVersion("0.1");
		//
		// AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject,
		// auditAction);

		validateAudit("DReject", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED, "409", ActionStatus.RESTRICTED_OPERATION,
				sdncOpsDetails);
	}

	@Test
	public void approveServiceNotFound() throws Exception {
		String previuosId = serviceDetails.getUniqueId();
		serviceDetails.setUniqueId("dummyId");

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 404, serviceDetails.getVersion());
		serviceDetails.setUniqueId(previuosId);

		variablesAsList = Arrays.asList("dummyId");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), variablesAsList,
				changeDistStatusAndValidate.getResponse());

	}

	@Test
	public void rejectServiceNotFound() throws Exception {
		String previuosId = serviceDetails.getUniqueId();
		serviceDetails.setUniqueId("dummyId");

		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 404, serviceDetails.getVersion());
		serviceDetails.setUniqueId(previuosId);

		variablesAsList = Arrays.asList("dummyId");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), variablesAsList,
				changeDistStatusAndValidate.getResponse());

	}

	@Test
	public void rejectService_emptyComment() throws Exception {
		userRemarks = "";

		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 400, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

	}

	@Test
	public void rejectService_nullComment() throws Exception {
		userRemarks = null;

		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 400, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);
	}

	@Test
	public void rejectService_spaceComment() throws Exception {
		userRemarks = " ";

		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncAdminDetails, 400, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

	}

	@Test
	public void approveService_emptyComment() throws Exception {
		userRemarks = "";

		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 400, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

	}

	@Test
	public void approveService_nullComment() throws Exception {
		userRemarks = null;

		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 400, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

	}

	@Test
	public void approveService_spaceComment() throws Exception {
		userRemarks = " ";

		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse changeDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 400, certifyService.getVersion());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), new ArrayList<String>(),
				changeDistStatusAndValidate.getResponse());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

	}

	@Test
	public void distributionStatusChange_approve_Reject_AprroveBysdncAdminDetails() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);

		RestResponse approveDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncGovernorDeatails, 200, certifyService.getVersion());
		getDistrubtionStatusValue(approveDistStatusAndValidate, DistributionStatusEnum.DISTRIBUTION_APPROVED);

		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse rejectDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_REJECTED, sdncGovernorDeatails, 200, certifyService.getVersion());
		getDistrubtionStatusValue(rejectDistStatusAndValidate, DistributionStatusEnum.DISTRIBUTION_REJECTED);

		validateAudit("DReject", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_APPROVED,
				DistributionStatusEnum.DISTRIBUTION_REJECTED, null, null, sdncGovernorDeatails);

		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse secondApproveDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncAdminDetails, 200, certifyService.getVersion());
		getDistrubtionStatusValue(secondApproveDistStatusAndValidate, DistributionStatusEnum.DISTRIBUTION_APPROVED);

		validateAudit("DApprove", LifecycleStateEnum.CERTIFIED, DistributionStatusEnum.DISTRIBUTION_REJECTED,
				DistributionStatusEnum.DISTRIBUTION_APPROVED, null, null, sdncAdminDetails);

	}

	@Test
	public void distributeNotCertifiedServiceTest() throws Exception {
		RestResponse approveDistStatusAndValidate = changeDistStatusAndValidate(DistributionStatusEnum.DISTRIBUTED,
				sdncGovernorDeatails, 200, serviceDetails.getVersion());

		RestResponse getService = ServiceRestUtils.getService(serviceDetails, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTED);

	}

	@Test
	public void distributeCertifiedServiceTest() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse approveDistStatusAndValidate = changeDistStatusAndValidate(DistributionStatusEnum.DISTRIBUTED,
				sdncGovernorDeatails, 200, certifyService.getVersion());

		RestResponse getService = ServiceRestUtils.getService(certifyService, sdncDesignerDetails);
		getDistrubtionStatusValue(getService, DistributionStatusEnum.DISTRIBUTED);

	}

	@Test
	public void approveCheckedoutCertifiedServiceTest() throws Exception {
		RestResponse certifyServiceResp = LifecycleRestUtils.certifyService(serviceDetails);
		Service certifyServiceServ = ResponseParser
				.convertServiceResponseToJavaObject(certifyServiceResp.getResponse());
		ServiceReqDetails certifyService = new ServiceReqDetails(certifyServiceServ);
		RestResponse approveDistStatusAndValidate = changeDistStatusAndValidate(
				DistributionStatusEnum.DISTRIBUTION_APPROVED, sdncGovernorDeatails, 200, certifyService.getVersion());
		getDistrubtionStatusValue(approveDistStatusAndValidate, DistributionStatusEnum.DISTRIBUTION_APPROVED);

		RestResponse checkoutResp = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		assertEquals(200, checkoutResp.getErrorCode().intValue());
		// Utils r = new Utils();

		String distributionStatus = ResponseParser.getValueFromJsonResponse(checkoutResp.getResponse(),
				"distributionStatus");
		// Utils r1 = new Utils();
		String lifecycleState = ResponseParser.getValueFromJsonResponse(checkoutResp.getResponse(), "lifecycleState");

		assertTrue("NOT_CERTIFIED_CHECKOUT".equals(lifecycleState));
		assertTrue("DISTRIBUTION_NOT_APPROVED".equals(distributionStatus));
	}

	private RestResponse changeDistStatusAndValidate(DistributionStatusEnum distStatus, User user, int errorCode,
			String serviceVersion) throws Exception {
		RestResponse distributionResponse = LifecycleRestUtils.changeDistributionStatus(serviceDetails, serviceVersion,
				user, userRemarks, distStatus);
		assertNotNull(distributionResponse);
		assertNotNull(distributionResponse.getErrorCode());
		assertEquals(errorCode, distributionResponse.getErrorCode().intValue());

		if (userRemarks == " " || userRemarks == null) {
			userRemarks = "";
		}

		return distributionResponse;
	}

	private void getDistrubtionStatusValue(RestResponse response, DistributionStatusEnum expectedDistributionValue)
			throws Exception {
		String actualDistributionValue = ResponseParser.getValueFromJsonResponse(response.getResponse(),
				"distributionStatus");
		assertEquals(expectedDistributionValue.name(), actualDistributionValue);
	}

	private void validateAudit(String Action, LifecycleStateEnum currState, DistributionStatusEnum dPrevStatus,
			DistributionStatusEnum dCurrStatus, String status, ActionStatus errorInfoFromFile, User user)
			throws Exception {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceDetails.getVersion(), user);
		expectedResourceAuditJavaObject.setAction(Action);
		expectedResourceAuditJavaObject.setResourceType("Service");
		expectedResourceAuditJavaObject.setCurrState(currState.name());
		expectedResourceAuditJavaObject.setDprevStatus(dPrevStatus.name());
		expectedResourceAuditJavaObject.setDcurrStatus(dCurrStatus.name());
		expectedResourceAuditJavaObject.setComment(userRemarks);
		expectedResourceAuditJavaObject.setDesc("OK");

		if (errorInfoFromFile != null) {
			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(errorInfoFromFile.name());
			expectedResourceAuditJavaObject
					.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variablesAsList));
		}

		if (status != null)
			expectedResourceAuditJavaObject.setStatus(status);

		if (currState != LifecycleStateEnum.CERTIFIED) {
			expectedResourceAuditJavaObject.setModifierName("");
		}

		AuditValidationUtils.validateAuditDistribution(expectedResourceAuditJavaObject, Action);
	}

	// private ServiceReqDetails certifyService() throws Exception
	// {
	// ServiceReqDetails certifyService =
	// LifecycleRestUtils.certifyService(serviceDetails,
	// serviceDetails.getVersion(), sdncAdminDetails);
	//// version = certifyService.getVersion();
	//
	// return certifyService;
	// }

}
