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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.*;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.AssetRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.common.api.Constants;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

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
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, sdncUserDetails);
		Resource resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		expectedAssetNamesList.add(resourceDetails.getName());

		resourceDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, sdncUserDetails);
		resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		expectedAssetNamesList.add(resourceDetails.getName());

		resourceDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, sdncUserDetails);
		resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		expectedAssetNamesList.add(resourceDetails.getName());

		resourceDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, sdncUserDetails);
		resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		expectedAssetNamesList.add(resourceDetails.getName());

		resourceDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, sdncUserDetails);
		resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		expectedAssetNamesList.add(resourceDetails.getName());

		resourceDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, sdncUserDetails);
		resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true).getLeft();
		expectedAssetNamesList.add(resourceDetails.getName());

/*		resourceDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, sdncUserDetails);
		resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();
		expectedAssetNamesList.add(resourceDetails.getName());*/

		System.out.println("6 VF resources were created");

		RestResponse assetResponse = AssetRestUtils.getComponentListByAssetType(true, AssetTypeEnum.RESOURCES);
		BaseRestUtils.checkSuccess(assetResponse);

		List<ResourceAssetStructure> resourceAssetList = AssetRestUtils.getResourceAssetList(assetResponse);
		List<String> getActualAssetNamesList = AssetRestUtils.getResourceNamesList(resourceAssetList);
		Utils.compareArrayLists(getActualAssetNamesList, expectedAssetNamesList, "Element");

		AssetRestUtils.checkComponentTypeInObjectList(resourceAssetList, ComponentTypeEnum.RESOURCE);

	/*	// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetListAudit(AssetTypeEnum.RESOURCES, AuditingActionEnum.GET_ASSET_LIST);
		Map <AuditingFieldsKey, String> body = new HashMap<>();
        body.put(AuditingFieldsKey.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_LIST.getName(), body);*/

	}

	@Test // (enabled = false)
	public void getServiceAssetSuccess() throws Exception {

//		CassandraUtils.truncateAllKeyspaces();
		List<String> expectedAssetNamesList = new ArrayList<>();
		ArtifactReqDetails artifactDetails = ElementFactory.getArtifactByType(ArtifactTypeEnum.OTHER, ArtifactTypeEnum.OTHER, true);

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		Service service = AtomicOperationUtils.createCustomService(serviceDetails, UserRoleEnum.DESIGNER, true).left().value();
		expectedAssetNamesList.add(serviceDetails.getName());

		serviceDetails = ElementFactory.getDefaultService();
		service = AtomicOperationUtils.createCustomService(serviceDetails, UserRoleEnum.DESIGNER, true).left().value();
		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		expectedAssetNamesList.add(serviceDetails.getName());

		serviceDetails = ElementFactory.getDefaultService();
		service = AtomicOperationUtils.createCustomService(serviceDetails, UserRoleEnum.DESIGNER, true).left().value();
		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		expectedAssetNamesList.add(serviceDetails.getName());

		serviceDetails = ElementFactory.getDefaultService();
		service = AtomicOperationUtils.createCustomService(serviceDetails, UserRoleEnum.DESIGNER, true).left().value();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		expectedAssetNamesList.add(serviceDetails.getName());

		serviceDetails = ElementFactory.getDefaultService();
		service = AtomicOperationUtils.createCustomService(serviceDetails, UserRoleEnum.DESIGNER, true).left().value();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		expectedAssetNamesList.add(serviceDetails.getName());

		serviceDetails = ElementFactory.getDefaultService();
		service = AtomicOperationUtils.createCustomService(serviceDetails, UserRoleEnum.DESIGNER, true).left().value();
		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true).getLeft();
		expectedAssetNamesList.add(serviceDetails.getName());

		serviceDetails = ElementFactory.getDefaultService();
		service = AtomicOperationUtils.createCustomService(serviceDetails, UserRoleEnum.DESIGNER, true).left().value();
		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();
		expectedAssetNamesList.add(serviceDetails.getName());

		System.out.println("7 Services were created");

		RestResponse assetResponse = AssetRestUtils.getComponentListByAssetType(true, AssetTypeEnum.SERVICES);
		BaseRestUtils.checkSuccess(assetResponse);

		List<ServiceAssetStructure> serviceAssetList = AssetRestUtils.getServiceAssetList(assetResponse);
		List<String> getActualAssetNamesList = AssetRestUtils.getServiceNamesList(serviceAssetList);
		Utils.compareArrayLists(getActualAssetNamesList, expectedAssetNamesList, "Element");

		/*// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetListAudit(AssetTypeEnum.SERVICES, AuditingActionEnum.GET_ASSET_LIST);
		Map <AuditingFieldsKey, String> body = new HashMap<>();
        body.put(AuditingFieldsKey.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_LIST.getName(), body);*/

	}

	@Test(enabled = true)
	public void getToscaModelSuccess() throws Exception {

		Resource resource;
		HttpResponse assetResponse;
		HttpResponse response;
		InputStream inputStream;
		ArtifactUiDownloadData artifactUiDownloadData;
		int len;
		byte[] res;
		byte[] fromUiDownload;
		String fileName;
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			ResourceReqDetails resourceDetails = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, sdncUserDetails);
			resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
			assetResponse = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
			resource = (Resource) AtomicOperationUtils.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
			String artId = resource.getToscaArtifacts().get("assettoscacsar").getEsId();
			String url = String.format(Urls.UI_DOWNLOAD_RESOURCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), resource.getUniqueId(), artId);
			HttpGet httpGet = createGetRequest(url);
			response = httpclient.execute(httpGet);
		}
		inputStream = response.getEntity().getContent();
		artifactUiDownloadData = getArtifactUiDownloadData(IOUtils.toString(inputStream));
		assetResponse = AssetRestUtils.getComponentToscaModel(AssetTypeEnum.RESOURCES, resource.getUUID());
		inputStream = assetResponse.getEntity().getContent();
		len = (int) assetResponse.getEntity().getContentLength();
		res = new byte[len];
		inputStream.read(res, 0, len);
		fromUiDownload = artifactUiDownloadData.getBase64Contents().getBytes();
		assertEquals(Base64.encodeBase64(res), fromUiDownload);
		fileName = assetResponse.getFirstHeader(Constants.CONTENT_DISPOSITION_HEADER).getValue();
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
