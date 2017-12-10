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

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.FileUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import fj.data.Either;

public class GetComponentAuditApiTest extends ComponentBaseTest {

	public static final String SERVICES_API = "services";
	public static final String RESOURCES_API = "resources";

	protected User sdncAdminUser;
	protected User sdncDesignerUser;
	protected User sdncTesterUser;

	@Rule
	public static TestName name = new TestName();

	public GetComponentAuditApiTest() {
		super(name, GetComponentAuditApiTest.class.getName());
	}

	// in case tests fail, run this method as test to create mapping in ES
	public void updateElasticSearchMapping() throws IOException {
		Either<String, Exception> fileContentUTF8 = FileUtils
				.getFileContentUTF8("src\\test\\resources\\CI\\other\\mapping.json");
		AssertJUnit.assertTrue(fileContentUTF8.isLeft());

		final String ES_TEMPLATE_URL = "http://%s:%s/_template/audit_template";
		String url = String.format(ES_TEMPLATE_URL, config.getEsHost(), config.getEsPort());

		RestResponse sendHttpPost = new HttpRequest().sendHttpPost(url, fileContentUTF8.left().value(), null);
		AssertJUnit.assertTrue(sendHttpPost.getErrorCode() == HttpStatus.SC_OK);
	}

	@BeforeMethod
	public void init() {
		sdncAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncDesignerUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncTesterUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		;

	}

	@Test
	public void testServiceAuditCertifiedVersion() throws Exception {

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		Wrapper<String> versionZeroOneIDWrapper = new Wrapper<String>(),
				versionZeroTwoIDWrapper = new Wrapper<String>();

		createBasicServiceForAudit(versionZeroOneIDWrapper, versionZeroTwoIDWrapper, serviceDetails, true);
		// First Certification

		LifecycleRestUtils.certifyService(serviceDetails);
		// LCSbaseTest.certifyService(serviceDetails);
		AssertJUnit.assertTrue(serviceDetails.getVersion().equals("1.0"));

		// Second Certification
		increaseServiceVersion(serviceDetails, "1.1");
		increaseServiceVersion(serviceDetails, "1.2");
		increaseServiceVersion(serviceDetails, "1.3");
		increaseServiceVersion(serviceDetails, "1.4");
		LifecycleRestUtils.certifyService(serviceDetails);
		AssertJUnit.assertTrue(serviceDetails.getVersion().equals("2.0"));
		String certifiedId = serviceDetails.getUniqueId();
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerUser, LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerUser, LifeCycleStatesEnum.CHECKIN);

		JsonElement element = getAuditJson(SERVICES_API, certifiedId);
		// audits kept: 5*check ins + 4*check outs + 2*artifact payload
		// updates(tosca) + certification request + certification start +
		// certification success
		// + 3 A&AI(ArtifactDelete, ArtifactUpload, ArtifactUpdate)
		List<String> actions = new ArrayList<>();
		JsonArray jsonArray = element.getAsJsonArray();
		for( int i =0 ; i < jsonArray.size(); i++){
			actions.add(jsonArray.get(i).getAsJsonObject().get(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName()).getAsString());
		}
		long checkinCount = actions.stream().filter( e -> e.equals(AuditingActionEnum.CHECKIN_RESOURCE.getName())).count();
		assertTrue(checkinCount == 5);
		
		long checkOutCount = actions.stream().filter( e -> e.equals(AuditingActionEnum.CHECKOUT_RESOURCE.getName())).count();
		assertTrue(checkOutCount == 4);
		
		long certificationRequestCount = actions.stream().filter( e -> e.equals(AuditingActionEnum.CERTIFICATION_REQUEST_RESOURCE.getName())).count();
		assertTrue(certificationRequestCount == 1);
		
		long certificationStartCount = actions.stream().filter( e -> e.equals(AuditingActionEnum.START_CERTIFICATION_RESOURCE.getName())).count();
		assertTrue(certificationStartCount == 1);
		
		long certificationSuccessCount = actions.stream().filter( e -> e.equals(AuditingActionEnum.CERTIFICATION_SUCCESS_RESOURCE.getName())).count();
		assertTrue(certificationSuccessCount == 1);
		

	}

	protected void certifyResource(ResourceReqDetails defaultResource) throws IOException {
		RestResponse response = LifecycleRestUtils.changeResourceState(defaultResource, sdncDesignerUser,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
		response = LifecycleRestUtils.changeResourceState(defaultResource, sdncTesterUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
		response = LifecycleRestUtils.changeResourceState(defaultResource, sdncTesterUser, LifeCycleStatesEnum.CERTIFY);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
	}

	protected JsonElement getAuditJson(String componentType, String componentId) throws IOException {
		Map<String, String> headers = new HashMap<String, String>() {
			{
				put(Constants.USER_ID_HEADER, UserRoleEnum.ADMIN.getUserId());
			}
		};
		String url = String.format(Urls.GET_COMPONENT_AUDIT_RECORDS, config.getCatalogBeHost(),
				config.getCatalogBePort(), componentType, componentId);

		RestResponse httpSendGet = new HttpRequest().httpSendGet(url, headers);
		AssertJUnit.assertTrue(httpSendGet.getErrorCode() == HttpStatus.SC_OK);
		JsonElement element = ResponseParser.parseToObject(httpSendGet.getResponse(), JsonElement.class);
		AssertJUnit.assertTrue(element.isJsonArray());
		return element;
	}

	protected void createBasicServiceForAudit(Wrapper<String> versionZeroOneIDWrapper,
			Wrapper<String> versionZeroTwoIDWrapper, ServiceReqDetails serviceDetails, Boolean withResInst)
			throws Exception {

		User designerUser = sdncDesignerUser;

		RestResponse response = ServiceRestUtils.createService(serviceDetails, designerUser);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_CREATED);
		versionZeroOneIDWrapper.setInnerElement(serviceDetails.getUniqueId());

		if (withResInst) {
			Resource resourceObj = AtomicOperationUtils
					.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
			AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceObj, UserRoleEnum.DESIGNER, true,
					true);
			AtomicOperationUtils.changeComponentState(resourceObj, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY,
					true);
			ResourceReqDetails resource = new ResourceReqDetails(resourceObj);
			ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory.getDefaultComponentInstance();
			resourceInstanceReqDetails.setComponentUid(resource.getUniqueId());
			ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, sdncDesignerUser,
					serviceDetails.getUniqueId(), ComponentTypeEnum.SERVICE);

			// ServiceUtils.createCertResourceWithDeploymentArt(serviceDetails,
			// "myResource");
		}

		response = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser, LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
		AssertJUnit.assertTrue(serviceDetails.getVersion().equals("0.1"));

		response = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser, LifeCycleStatesEnum.CHECKOUT);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
		// ServiceUtils.addServiceDeploymentArtifact(serviceDetails.getUniqueId(),
		// designerUser);
		versionZeroTwoIDWrapper.setInnerElement(serviceDetails.getUniqueId());
		AssertJUnit.assertTrue(serviceDetails.getVersion().equals("0.2"));
		response = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser, LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);

		increaseServiceVersion(serviceDetails, "0.3");

		increaseServiceVersion(serviceDetails, "0.4");

		increaseServiceVersion(serviceDetails, "0.5");

	}

	protected void increaseServiceVersion(ServiceReqDetails serviceDetails, String excpectedVersion) throws Exception {
		RestResponse response = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerUser,
				LifeCycleStatesEnum.CHECKOUT);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
		AssertJUnit.assertTrue(serviceDetails.getVersion().equals(excpectedVersion));
		response = LifecycleRestUtils.changeServiceState(serviceDetails, sdncDesignerUser, LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
	}

	protected void createBasicResourceForAudit(Wrapper<String> versionOnePointTwoIDWrapper,
			ResourceReqDetails defaultResource) throws Exception {

		RestResponse response = ResourceRestUtils.createResource(defaultResource, sdncDesignerUser);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_CREATED);

		// ArtifactDefinition artifactDef = new
		// ArtifactUtils().constructDefaultArtifactInfo();
		// response = resourceUtils.add_artifact(defaultResource,
		// sdncDesignerUser, defaultResource.getVersion(), artifactDef);
		// assertTrue(response.getErrorCode() == HttpStatus.SC_OK);

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerUser,
				defaultResource.getUniqueId());
		AssertJUnit.assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		response = LifecycleRestUtils.changeResourceState(defaultResource, sdncDesignerUser,
				LifeCycleStatesEnum.CHECKIN);

		increaseResourceVersion(defaultResource, "0.2");

		increaseResourceVersion(defaultResource, "0.3");

		increaseResourceVersion(defaultResource, "0.4");

		increaseResourceVersion(defaultResource, "0.5");

		certifyResource(defaultResource);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
		AssertJUnit.assertTrue(defaultResource.getVersion().equals("1.0"));

		increaseResourceVersion(defaultResource, "1.1");

		increaseResourceVersion(defaultResource, "1.2");
		versionOnePointTwoIDWrapper.setInnerElement(defaultResource.getUniqueId());

		increaseResourceVersion(defaultResource, "1.3");

		increaseResourceVersion(defaultResource, "1.4");

	}

	protected void increaseResourceVersion(ResourceReqDetails defaultResource, String expectedVersion)
			throws IOException {
		RestResponse response = LifecycleRestUtils.changeResourceState(defaultResource, sdncDesignerUser,
				LifeCycleStatesEnum.CHECKOUT);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
		AssertJUnit.assertTrue(defaultResource.getVersion().equals(expectedVersion));
		response = LifecycleRestUtils.changeResourceState(defaultResource, sdncDesignerUser,
				LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertTrue(response.getErrorCode() == HttpStatus.SC_OK);
	}

	@Test
	public void testServiceAuditLastUncertifiedVersion() throws Exception {

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		Wrapper<String> versionZeroOneIDWrapper = new Wrapper<String>(),
				versionZeroTwoIDWrapper = new Wrapper<String>();

		createBasicServiceForAudit(versionZeroOneIDWrapper, versionZeroTwoIDWrapper, serviceDetails, false);

		JsonElement element = getAuditJson(SERVICES_API, versionZeroTwoIDWrapper.getInnerElement());

		assertTrue(element.getAsJsonArray().size() == 3);

	}

	@Test
	public void testServiceAuditFirstUncertifiedVersion() throws Exception {

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		Wrapper<String> versionZeroOneIDWrapper = new Wrapper<String>(),
				versionZeroTwoIDWrapper = new Wrapper<String>();

		createBasicServiceForAudit(versionZeroOneIDWrapper, versionZeroTwoIDWrapper, serviceDetails, false);

		JsonElement element = getAuditJson(SERVICES_API, versionZeroOneIDWrapper.getInnerElement());

		assertTrue(element.getAsJsonArray().size() == 3);

	}

	@Test
	public void testResourceAuditUncertifiedVersion() throws Exception {

		ResourceReqDetails defaultResource = ElementFactory.getDefaultResource();
		Wrapper<String> versionOnePointTwoIDWrapper = new Wrapper<String>();

		createBasicResourceForAudit(versionOnePointTwoIDWrapper, defaultResource);

		JsonElement element = getAuditJson(RESOURCES_API, versionOnePointTwoIDWrapper.getInnerElement());

		assertTrue(element.getAsJsonArray().size() == 3);

	}

	@Test
	public void testResourceAuditCertifiedVersion() throws Exception {

		ResourceReqDetails defaultResource = ElementFactory.getDefaultResource();
		Wrapper<String> versionOnePointTwoIDWrapper = new Wrapper<String>();

		createBasicResourceForAudit(versionOnePointTwoIDWrapper, defaultResource);

		certifyResource(defaultResource);
		assertTrue(defaultResource.getVersion().equals("2.0"));
		String certifiedId = defaultResource.getUniqueId();

		increaseResourceVersion(defaultResource, "2.1");

		increaseResourceVersion(defaultResource, "2.2");

		JsonElement element = getAuditJson(RESOURCES_API, certifiedId);

		assertTrue(element.getAsJsonArray().size() == 13);

	}

}
