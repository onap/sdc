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

package org.openecomp.sdc.externalApis;

import static org.testng.AssertJUnit.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedExternalAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.AssetRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class GetAssetServlet extends ComponentBaseTest {

	protected User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	protected User sdncAdminUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	protected ConsumerDataDefinition consumerDataDefinition;

	@Rule
	public static TestName name = new TestName();

	public GetAssetServlet() {
		super(name, GetAssetServlet.class.getName());
	}

	Gson gson = new Gson();

//	@BeforeMethod
//	public void setup() throws Exception {
//
//		AtomicOperationUtils.createDefaultConsumer(true);
//
//	}

	@Test // (enabled = false)
	public void getResourceAssetSuccess() throws Exception {

//		CassandraUtils.truncateAllKeyspaces();
		List<String> expectedAssetNamesList = new ArrayList<>();

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("ciResource1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		expectedAssetNamesList.add(resource.getName());

		resourceDetails.setName("ciResource2");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		expectedAssetNamesList.add(resource.getName());
		expectedAssetNamesList.add(resource.getName());

		resourceDetails.setName("ciResource3");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		expectedAssetNamesList.add(resource.getName());

		resourceDetails.setName("ciResource4");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		expectedAssetNamesList.add(resource.getName());

		resourceDetails.setName("ciResource5");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		expectedAssetNamesList.add(resource.getName());

		resourceDetails.setName("ciResource6");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true).getLeft();
		expectedAssetNamesList.add(resource.getName());

		resourceDetails.setName("ciResource7");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();
		expectedAssetNamesList.add(resource.getName());
		expectedAssetNamesList.add(resource.getName());

		System.out.println("7 VF resources were created");

		RestResponse assetResponse = AssetRestUtils.getComponentListByAssetType(true, AssetTypeEnum.RESOURCES);
		BaseRestUtils.checkSuccess(assetResponse);

		List<ResourceAssetStructure> resourceAssetList = AssetRestUtils.getResourceAssetList(assetResponse);
		List<String> getActualAssetNamesList = AssetRestUtils.getResourceNamesList(resourceAssetList);
		Utils.compareArrayLists(getActualAssetNamesList, expectedAssetNamesList, "Element");

		AssetRestUtils.checkComponentTypeInObjectList(resourceAssetList, ComponentTypeEnum.RESOURCE);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetListAudit(AssetTypeEnum.RESOURCES, AuditingActionEnum.GET_ASSET_LIST);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_LIST.getName(), body);

	}

	@Test // (enabled = false)
	public void getServiceAssetSuccess() throws Exception {

//		CassandraUtils.truncateAllKeyspaces();
		List<String> expectedAssetNamesList = new ArrayList<>();
		ArtifactReqDetails artifactDetails = ElementFactory.getArtifactByType(ArtifactTypeEnum.OTHER, ArtifactTypeEnum.OTHER, true);

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setName("ciService1");
		RestResponse createService = ServiceRestUtils.createService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		Service service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		expectedAssetNamesList.add(service.getName());

		serviceDetails.setName("ciService2");
		createService = ServiceRestUtils.createService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		expectedAssetNamesList.add(service.getName());
		expectedAssetNamesList.add(service.getName());

		serviceDetails.setName("ciService3");
		createService = ServiceRestUtils.createService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		expectedAssetNamesList.add(service.getName());

		serviceDetails.setName("ciService4");
		createService = ServiceRestUtils.createService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		expectedAssetNamesList.add(service.getName());

		serviceDetails.setName("ciService5");
		createService = ServiceRestUtils.createService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		expectedAssetNamesList.add(service.getName());

		serviceDetails.setName("ciService6");
		createService = ServiceRestUtils.createService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true).getLeft();
		expectedAssetNamesList.add(service.getName());

		serviceDetails.setName("ciService7");
		createService = ServiceRestUtils.createService(serviceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();
		expectedAssetNamesList.add(service.getName());
		expectedAssetNamesList.add(service.getName());

		System.out.println("7 Services were created");

		RestResponse assetResponse = AssetRestUtils.getComponentListByAssetType(true, AssetTypeEnum.SERVICES);
		BaseRestUtils.checkSuccess(assetResponse);

		List<ServiceAssetStructure> serviceAssetList = AssetRestUtils.getServiceAssetList(assetResponse);
		List<String> getActualAssetNamesList = AssetRestUtils.getServiceNamesList(serviceAssetList);
		Utils.compareArrayLists(getActualAssetNamesList, expectedAssetNamesList, "Element");

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetListAudit(AssetTypeEnum.SERVICES, AuditingActionEnum.GET_ASSET_LIST);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_LIST.getName(), body);

	}

	@Test
	public void getToscaModelSuccess() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
//		resourceDetails.setName("ciResource11");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncUserDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		HttpResponse assetResponse = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		String artId = resource.getToscaArtifacts().get("assettoscacsar").getEsId();
		String url = String.format(Urls.UI_DOWNLOAD_RESOURCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), resource.getUniqueId(), artId);
		HttpGet httpGet = createGetRequest(url);
		HttpResponse response = httpclient.execute(httpGet);
		InputStream inputStream = response.getEntity().getContent();
		ArtifactUiDownloadData artifactUiDownloadData = getArtifactUiDownloadData(IOUtils.toString(inputStream));
		assetResponse = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		inputStream = assetResponse.getEntity().getContent();
		int len = (int) assetResponse.getEntity().getContentLength();
		byte[] res = new byte[len];
		inputStream.read(res, 0, len);
		byte[] fromUiDownload = artifactUiDownloadData.getBase64Contents().getBytes();
		assertEquals(Base64.encodeBase64(res), fromUiDownload);
		String fileName = assetResponse.getFirstHeader(Constants.CONTENT_DISPOSITION_HEADER).getValue();
		assertEquals(fileName, new StringBuilder().append("attachment; filename=\"")
				.append(artifactUiDownloadData.getArtifactName()).append("\"").toString());
	}

	private HttpGet createGetRequest(String url) {
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		return httpGet;
	}

	private ArtifactUiDownloadData getArtifactUiDownloadData(String artifactUiDownloadDataStr) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		try {
			ArtifactUiDownloadData artifactUiDownloadData = mapper.readValue(artifactUiDownloadDataStr, ArtifactUiDownloadData.class);
			return artifactUiDownloadData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
