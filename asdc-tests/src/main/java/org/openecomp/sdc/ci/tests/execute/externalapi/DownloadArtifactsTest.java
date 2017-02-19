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

package org.openecomp.sdc.ci.tests.execute.externalapi;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class DownloadArtifactsTest extends ComponentBaseTest {
	@Rule
	public static TestName name = new TestName();

	Gson gson = new Gson();

	public DownloadArtifactsTest() {
		super(name, DownloadArtifactsTest.class.getName());
	}

	private User sdncDesignerDetails;
	private User sdncAdminDetails;
	private ImportReqDetails resourceDetailsVF_01;
	private ResourceReqDetails resourceDetailsVF_02;
	private ResourceReqDetails resourceDetailsVF_03;
	private ResourceReqDetails resourceDetailsCP_01;
	private ServiceReqDetails serviceDetails_01;
	private ServiceReqDetails serviceDetails_02;
	public static String rootPath = System.getProperty("user.dir");

	@BeforeMethod(alwaysRun = true)
	public void before() throws Exception {
		init();
		createComponents();
	}

	private void createComponents() throws Exception {
		createAtomicResource(resourceDetailsCP_01);
		importVfWithArtifacts(resourceDetailsVF_01);
		createVF(resourceDetailsVF_03);
		createVF(resourceDetailsVF_02);
		createService(serviceDetails_01);
	}

	public void init() {
		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncAdminDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		resourceDetailsVF_01 = ElementFactory.getDefaultImportResourceByType("VF100", NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VF.toString());
		resourceDetailsVF_02 = ElementFactory.getDefaultResourceByType("VF200", NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VF.toString());
		resourceDetailsVF_03 = ElementFactory.getDefaultResourceByType("VF300", NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncDesignerDetails.getUserId(), ResourceTypeEnum.VF.toString());
		resourceDetailsCP_01 = ElementFactory.getDefaultResourceByType("CP100", NormativeTypesEnum.PORT, ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS, sdncDesignerDetails.getUserId(), ResourceTypeEnum.CP.toString());
		serviceDetails_01 = ElementFactory.getDefaultService("newtestservice1", ServiceCategoriesEnum.MOBILITY, sdncDesignerDetails.getUserId());
		serviceDetails_02 = ElementFactory.getDefaultService("newtestservice2", ServiceCategoriesEnum.MOBILITY, sdncDesignerDetails.getUserId());
	}

	@Test
	public void downloadResourceInstanceArtifactsFromServiceTest() throws Exception {
		Service service = createServiceWithRIsWithArtifacts();
		Map<String, ArtifactDefinition> deploymentArtifacts;
		List<ComponentInstance> resourceInstances = service.getComponentInstances();
		for (ComponentInstance ri : resourceInstances) {
			deploymentArtifacts = ri.getDeploymentArtifacts();
			for (ArtifactDefinition artifact : deploymentArtifacts.values()) {
				assertNotNull(downloadResourceInstanceArtifact(service, ri, artifact));
			}
		}
	}

	@Test
	public void downloadServiceArtifactsTest() throws Exception {
		Service service = createServiceWithArtifacts();
		Map<String, ArtifactDefinition> deploymentArtifacts = service.getDeploymentArtifacts();
		for (ArtifactDefinition artifact : deploymentArtifacts.values()) {
			assertNotNull(downloadServiceArtifact(service, artifact));
		}

	}

	private Service createServiceWithArtifacts() throws Exception {

		ArtifactReqDetails otherArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.OTHER.getType());

		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(otherArtifactDetails, sdncDesignerDetails, serviceDetails_01.getUniqueId());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToService.getErrorCode(), addInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		ArtifactReqDetails yangXmlArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.YANG_XML.getType());

		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(yangXmlArtifactDetails, sdncDesignerDetails, serviceDetails_01.getUniqueId());
		assertTrue("response code is not BaseRestUtils.STATUS_CODE_SUCCESS, returned :" + addInformationalArtifactToService.getErrorCode(), addInformationalArtifactToService.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		RestResponse createServiceResponse = ServiceRestUtils.getService(serviceDetails_01, sdncDesignerDetails);
		return ResponseParser.convertServiceResponseToJavaObject(createServiceResponse.getResponse());
	}

	private RestResponse downloadResourceInstanceArtifact(Service service, ComponentInstance ri, ArtifactDefinition artifact) throws Exception {
		String url = String.format(Urls.GET_DOWNLOAD_SERVICE_RI_ARTIFACT, "localhost", "8080", service.getUUID(), ri.getNormalizedName(), artifact.getArtifactUUID());
		String userId = sdncDesignerDetails.getUserId();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.CACHE_CONTROL.getValue(), "no-cache");
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), "Basic dGVzdDoxMjM0NTY=");
		headersMap.put("X-ECOMP-InstanceID", "test");
		if (userId != null) {
			headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		}
		sendAuthorizationRequest();
		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendGet(url, headersMap);
		if (response.getErrorCode() != 200 && response.getResponse().getBytes() == null && response.getResponse().getBytes().length == 0) {
			return null;
		}
		return response;
	}

	private RestResponse downloadServiceArtifact(Service service, ArtifactDefinition artifact) throws Exception {
		String url = String.format(Urls.GET_DOWNLOAD_SERVICE_ARTIFACT, "localhost", "8080", service.getUUID(), artifact.getArtifactUUID());
		String userId = sdncDesignerDetails.getUserId();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.CACHE_CONTROL.getValue(), "no-cache");
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), "Basic dGVzdDoxMjM0NTY=");
		headersMap.put("X-ECOMP-InstanceID", "test");
		if (userId != null) {
			headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		}
		sendAuthorizationRequest();
		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendGet(url, headersMap);
		if (response.getErrorCode() != 200 && response.getResponse().getBytes() == null && response.getResponse().getBytes().length == 0) {
			return null;
		}
		return response;

	}

	private RestResponse sendAuthorizationRequest() throws IOException {
		String url = String.format(Urls.POST_AUTHORIZATION, "localhost", "8080");
		String userId = sdncAdminDetails.getUserId();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.CACHE_CONTROL.getValue(), "no-cache");
		if (userId != null) {
			headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		}

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPost(url, "{\"consumerName\":\"test\",\"consumerPassword\":\"0a0dc557c3bf594b1a48030e3e99227580168b21f44e285c69740b8d5b13e33b\",\"consumerSalt\":\"2a1f887d607d4515d4066fe0f5452a50\"}", headersMap);
		if (response.getErrorCode() != 201) {
			return null;
		}
		return response;
	}

	private Service createServiceWithRIsWithArtifacts() throws Exception {
		serviceDetails_02.setUniqueId(serviceDetails_01.getUniqueId());
		createTreeCheckedinVFInstances();
		LifecycleRestUtils.changeResourceState(resourceDetailsCP_01, sdncDesignerDetails, "0.1", LifeCycleStatesEnum.CHECKIN);
		createVFInstanceAndAtomicResourceInstanceWithoutCheckin(resourceDetailsVF_01, resourceDetailsCP_01, sdncDesignerDetails);
		RestResponse updateServiceResp = ServiceRestUtils.updateService(serviceDetails_02, sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(updateServiceResp);
		getComponentAndValidateRIs(serviceDetails_01, 5, 0);

		return ResponseParser.convertServiceResponseToJavaObject(updateServiceResp.getResponse());
	}

	private void createTreeCheckedinVFInstances() throws Exception {
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createSecondVFInstResp);
		RestResponse createThirdVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_03, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createThirdVFInstResp);
	}

	private Component getComponentAndValidateRIs(ComponentReqDetails componentDetails, int numberOfRIs, int numberOfRelations) throws IOException, Exception {

		RestResponse getResponse = null;
		Component component = null;
		if (componentDetails instanceof ResourceReqDetails) {
			getResponse = ResourceRestUtils.getResource(sdncAdminDetails, componentDetails.getUniqueId());
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Resource.class);
		} else if (componentDetails instanceof ServiceReqDetails) {
			getResponse = ServiceRestUtils.getService((ServiceReqDetails) componentDetails, sdncAdminDetails);
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Service.class);
		} else if (componentDetails instanceof ProductReqDetails) {
			getResponse = ProductRestUtils.getProduct(componentDetails.getUniqueId(), sdncAdminDetails.getUserId());
			component = ResponseParser.parseToObjectUsingMapper(getResponse.getResponse(), Product.class);
		} else {
			Assert.fail("Unsupported type of componentDetails - " + componentDetails.getClass().getSimpleName());
		}
		ResourceRestUtils.checkSuccess(getResponse);
		int numberOfActualRIs = component.getComponentInstances() != null ? component.getComponentInstances().size() : 0;
		int numberOfActualRelations = component.getComponentInstancesRelations() != null ? component.getComponentInstancesRelations().size() : 0;
		assertEquals("Check number of RIs meet the expected number", numberOfRIs, numberOfActualRIs);
		assertEquals("Check number of RI relations meet the expected number", numberOfRelations, numberOfActualRelations);

		return component;
	}

	private void createVFInstanceAndAtomicResourceInstanceWithoutCheckin(ResourceReqDetails vf, ResourceReqDetails atomicResource, User user) throws Exception {
		RestResponse createVFInstance = createVFInstance(serviceDetails_01, vf, user);
		ResourceRestUtils.checkCreateResponse(createVFInstance);
		RestResponse atomicInstanceForService = createAtomicInstanceForService(serviceDetails_01, atomicResource, user);
		ResourceRestUtils.checkCreateResponse(atomicInstanceForService);
	}

	private RestResponse createCheckedinVFInstance(ServiceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		changeResourceLifecycleState(compInstOriginDetails, modifier.getUserId(), LifeCycleStatesEnum.CHECKIN);
		return createVFInstance(containerDetails, compInstOriginDetails, modifier);
	}

	private RestResponse createVFInstance(ServiceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE, true);
	}

	private RestResponse createAtomicInstanceForService(ServiceReqDetails containerDetails, ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		return createComponentInstance(containerDetails, compInstOriginDetails, modifier, ComponentTypeEnum.SERVICE, true);
	}

	private RestResponse createComponentInstance(ComponentReqDetails containerDetails, ComponentReqDetails compInstOriginDetails, User modifier, ComponentTypeEnum containerComponentTypeEnum, boolean isHighestLevel) throws IOException, Exception {
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory.getComponentResourceInstance(compInstOriginDetails);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, modifier, containerDetails.getUniqueId(), containerComponentTypeEnum);
		return createResourceInstanceResponse;
	}

	private void changeResourceLifecycleState(ResourceReqDetails resourceDetails, String userId, LifeCycleStatesEnum lifeCycleStates) throws Exception {
		RestResponse response = LifecycleRestUtils.changeResourceState(resourceDetails, userId, lifeCycleStates);
		LifecycleRestUtils.checkLCS_Response(response);
	}

	private void createAtomicResource(ResourceReqDetails resourceDetails) throws Exception {
		RestResponse createResourceResponse = ResourceRestUtils.createResource(resourceDetails, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceResponse);

	}

	private void createVF(ResourceReqDetails resourceDetails) throws Exception {
		createVF(resourceDetails, sdncDesignerDetails);

	}

	private void createVF(ResourceReqDetails resourceDetails, User sdncModifier) throws Exception {
		RestResponse createVfResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifier);
		ResourceRestUtils.checkCreateResponse(createVfResponse);
	}

	private void createService(ServiceReqDetails serviceDetails) throws Exception {
		createService(serviceDetails, sdncDesignerDetails);
	}

	private void createService(ServiceReqDetails serviceDetails, User sdncModifier) throws Exception {
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifier);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
	}

	private void importVfWithArtifacts(ImportReqDetails resourceDetailsVF_01) throws Exception {
		String payloadName = "VF_RI2_G4_withArtifacts.csar";
		Path path = Paths.get(rootPath + "/src/test/resources/CI/csars/VF_RI2_G4_withArtifacts.csar");
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetailsVF_01.setPayloadData(payloadData);

		resourceDetailsVF_01.setPayloadName(payloadName);
		resourceDetailsVF_01.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetailsVF_01, sdncDesignerDetails);
		BaseRestUtils.checkCreateResponse(createResource);
	}

}
