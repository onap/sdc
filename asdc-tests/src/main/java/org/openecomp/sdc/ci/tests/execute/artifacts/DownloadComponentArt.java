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

package org.openecomp.sdc.ci.tests.execute.artifacts;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactUiDownloadData;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Decoder;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ServiceValidationUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fj.data.Either;

public class DownloadComponentArt extends ComponentBaseTest {

	private static Logger log = LoggerFactory.getLogger(DownloadComponentArt.class.getName());
	protected static final String UPLOAD_ARTIFACT_PAYLOAD = "UHVUVFktVXNlci1LZXktRmlsZS0yOiBzc2gtcnNhDQpFbmNyeXB0aW9uOiBhZXMyNTYtY2JjDQpDb21tZW5wOA0K";
	protected static final String UPLOAD_ARTIFACT_NAME = "TLV_prv.ppk";

	protected Config config = Config.instance();
	protected String contentTypeHeaderData = "application/json";
	protected String acceptHeaderDate = "application/json";



	protected Gson gson = new Gson();
	protected JSONParser jsonParser = new JSONParser();


	protected String serviceVersion;
	protected ResourceReqDetails resourceDetails;
	protected User sdncUserDetails;
	protected ServiceReqDetails serviceDetails;
	

	@BeforeMethod
	public void init() throws Exception{
		sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		Resource resourceObj = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		Service serviceObj = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		
		
		resourceDetails = new ResourceReqDetails(resourceObj); 
		serviceDetails = new ServiceReqDetails(serviceObj);
	}
	
	@Rule 
	public static TestName name = new TestName();

	public DownloadComponentArt() {
		super(name, DownloadComponentArt.class.getName());

	}
		

	
	// External API - Download artifact for resource
	@Test
	public void downloadArtifactFromResourceViaExternalAPI() throws Exception {
		Resource resourceDetailsVF;
		Either<Resource, RestResponse> createdResource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetailsVF = createdResource.left().value();
		ArtifactDefinition heatArtifact = AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVF, UserRoleEnum.DESIGNER, true, true).left().value();
		resourceDetails = new ResourceReqDetails(resourceDetailsVF); 
		
		String resourceUUID = resourceDetailsVF.getUUID();
		String artifactUUID = heatArtifact.getArtifactUUID();
		
		System.out.println("Resource UUID: " + resourceUUID);
		System.out.println("Artifact UUID: " + artifactUUID);
		
		RestResponse restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(resourceUUID, artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), "Resource");
		
		Integer responseCode = restResponse.getErrorCode();
		Integer expectedCode = 200;
		Assert.assertEquals(responseCode,expectedCode, "Response code is not correct.");
		
		String response = restResponse.getResponse();
		
		String payloadData = "aGVhdF90ZW1wbGF0ZV92ZXJzaW9uOiAyMDEzLTA1LTIzDQoNCmRlc2NyaXB0aW9uOiBTaW1wbGUgdGVtcGxhdGUgdG8gZGVwbG95IGEgc3RhY2sgd2l0aCB0d28gdmlydHVhbCBtYWNoaW5lIGluc3RhbmNlcw0KDQpwYXJhbWV0ZXJzOg0KICBpbWFnZV9uYW1lXzE6DQogICAgdHlwZTogc3RyaW5nDQogICAgbGFiZWw6IEltYWdlIE5hbWUNCiAgICBkZXNjcmlwdGlvbjogU0NPSU1BR0UgU3BlY2lmeSBhbiBpbWFnZSBuYW1lIGZvciBpbnN0YW5jZTENCiAgICBkZWZhdWx0OiBjaXJyb3MtMC4zLjEteDg2XzY0DQogIGltYWdlX25hbWVfMjoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogSW1hZ2UgTmFtZQ0KICAgIGRlc2NyaXB0aW9uOiBTQ09JTUFHRSBTcGVjaWZ5IGFuIGltYWdlIG5hbWUgZm9yIGluc3RhbmNlMg0KICAgIGRlZmF1bHQ6IGNpcnJvcy0wLjMuMS14ODZfNjQNCiAgbmV0d29ya19pZDoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogTmV0d29yayBJRA0KICAgIGRlc2NyaXB0aW9uOiBTQ09ORVRXT1JLIE5ldHdvcmsgdG8gYmUgdXNlZCBmb3IgdGhlIGNvbXB1dGUgaW5zdGFuY2UNCiAgICBoaWRkZW46IHRydWUNCiAgICBjb25zdHJhaW50czoNCiAgICAgIC0gbGVuZ3RoOiB7IG1pbjogNiwgbWF4OiA4IH0NCiAgICAgICAgZGVzY3JpcHRpb246IFBhc3N3b3JkIGxlbmd0aCBtdXN0IGJlIGJldHdlZW4gNiBhbmQgOCBjaGFyYWN0ZXJzLg0KICAgICAgLSByYW5nZTogeyBtaW46IDYsIG1heDogOCB9DQogICAgICAgIGRlc2NyaXB0aW9uOiBSYW5nZSBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3ZhbHVlczoNCiAgICAgICAgLSBtMS5zbWFsbA0KICAgICAgICAtIG0xLm1lZGl1bQ0KICAgICAgICAtIG0xLmxhcmdlDQogICAgICAgIGRlc2NyaXB0aW9uOiBBbGxvd2VkIHZhbHVlcyBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbYS16QS1aMC05XSsiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IGNvbnNpc3Qgb2YgY2hhcmFjdGVycyBhbmQgbnVtYmVycyBvbmx5Lg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbQS1aXStbYS16QS1aMC05XSoiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IHN0YXJ0IHdpdGggYW4gdXBwZXJjYXNlIGNoYXJhY3Rlci4NCiAgICAgIC0gY3VzdG9tX2NvbnN0cmFpbnQ6IG5vdmEua2V5cGFpcg0KICAgICAgICBkZXNjcmlwdGlvbjogQ3VzdG9tIGRlc2NyaXB0aW9uDQoNCnJlc291cmNlczoNCiAgbXlfaW5zdGFuY2UxOg0KICAgIHR5cGU6IE9TOjpOb3ZhOjpTZXJ2ZXINCiAgICBwcm9wZXJ0aWVzOg0KICAgICAgaW1hZ2U6IHsgZ2V0X3BhcmFtOiBpbWFnZV9uYW1lXzEgfQ0KICAgICAgZmxhdm9yOiBtMS5zbWFsbA0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9DQogIG15X2luc3RhbmNlMjoNCiAgICB0eXBlOiBPUzo6Tm92YTo6U2VydmVyDQogICAgcHJvcGVydGllczoNCiAgICAgIGltYWdlOiB7IGdldF9wYXJhbTogaW1hZ2VfbmFtZV8yIH0NCiAgICAgIGZsYXZvcjogbTEudGlueQ0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9";
		String decodedPaypload = Decoder.decode(payloadData);
		
		Assert.assertEquals(response, decodedPaypload, "Response deployment artifact not correct.");
		
		String auditAction = "DownloadArtifact";
		
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setResourceName(resourceDetails.getName());
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		
		expectedResourceAuditJavaObject.setCONSUMER_ID("ci");
		String resource_url = String.format("/sdc/v1/catalog/resources/%s/artifacts/%s", resourceUUID, artifactUUID);
		expectedResourceAuditJavaObject.setRESOURCE_URL(resource_url);
		
		AuditValidationUtils.validateAuditDownloadExternalAPI(expectedResourceAuditJavaObject, auditAction, null, false);
	}
	
	
	// External API - Download artifact for resource - negative test
	@Test
	public void downloadArtifactFromResourceViaExternalAPINegativeTest() throws Exception {
		Resource resourceDetailsVF;
		Either<Resource, RestResponse> createdResource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetailsVF = createdResource.left().value();
		ArtifactDefinition heatArtifact = AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVF, UserRoleEnum.DESIGNER, true, true).left().value();
		resourceDetails = new ResourceReqDetails(resourceDetailsVF); 
		
		String resourceUUID = resourceDetailsVF.getUUID();
		String artifactUUID = heatArtifact.getArtifactUUID();
		
		System.out.println("Resource UUID: " + resourceUUID);
		System.out.println("Artifact UUID: " + artifactUUID);
		
		RestResponse restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(resourceUUID, "dfsgfdsg324", ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), "Resource");
		
		Integer responseCode = restResponse.getErrorCode();
		Integer expectedCode = 200;
		Assert.assertEquals(responseCode,expectedCode, "Response code is not correct.");
	}
	
	
	
	
	
	// External API - Download artifact for service - negative test
	@Test
	public void downloadArtifactFromServiceViaExternalAPI() throws Exception {
		
		Service resourceDetailsService;
		Either<Service, RestResponse> createdResource = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		resourceDetailsService = createdResource.left().value();
		
		ArtifactDefinition heatArtifact = AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, resourceDetailsService, UserRoleEnum.DESIGNER, true, true).left().value();

		String resourceUUID = resourceDetailsService.getUUID();
		String artifactUUID = heatArtifact.getArtifactUUID();
		
		System.out.println("Resource UUID: " + resourceUUID);
		System.out.println("Artifact UUID: " + artifactUUID);
		
		RestResponse restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(resourceUUID, artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), "Service");
		
		Integer responseCode = restResponse.getErrorCode();
		Integer expectedCode = 200;
		Assert.assertEquals(responseCode,expectedCode, "Response code is not correct.");
		
		String response = restResponse.getResponse();
		
		String payloadData = "aGVhdF90ZW1wbGF0ZV92ZXJzaW9uOiAyMDEzLTA1LTIzDQoNCmRlc2NyaXB0aW9uOiBTaW1wbGUgdGVtcGxhdGUgdG8gZGVwbG95IGEgc3RhY2sgd2l0aCB0d28gdmlydHVhbCBtYWNoaW5lIGluc3RhbmNlcw0KDQpwYXJhbWV0ZXJzOg0KICBpbWFnZV9uYW1lXzE6DQogICAgdHlwZTogc3RyaW5nDQogICAgbGFiZWw6IEltYWdlIE5hbWUNCiAgICBkZXNjcmlwdGlvbjogU0NPSU1BR0UgU3BlY2lmeSBhbiBpbWFnZSBuYW1lIGZvciBpbnN0YW5jZTENCiAgICBkZWZhdWx0OiBjaXJyb3MtMC4zLjEteDg2XzY0DQogIGltYWdlX25hbWVfMjoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogSW1hZ2UgTmFtZQ0KICAgIGRlc2NyaXB0aW9uOiBTQ09JTUFHRSBTcGVjaWZ5IGFuIGltYWdlIG5hbWUgZm9yIGluc3RhbmNlMg0KICAgIGRlZmF1bHQ6IGNpcnJvcy0wLjMuMS14ODZfNjQNCiAgbmV0d29ya19pZDoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogTmV0d29yayBJRA0KICAgIGRlc2NyaXB0aW9uOiBTQ09ORVRXT1JLIE5ldHdvcmsgdG8gYmUgdXNlZCBmb3IgdGhlIGNvbXB1dGUgaW5zdGFuY2UNCiAgICBoaWRkZW46IHRydWUNCiAgICBjb25zdHJhaW50czoNCiAgICAgIC0gbGVuZ3RoOiB7IG1pbjogNiwgbWF4OiA4IH0NCiAgICAgICAgZGVzY3JpcHRpb246IFBhc3N3b3JkIGxlbmd0aCBtdXN0IGJlIGJldHdlZW4gNiBhbmQgOCBjaGFyYWN0ZXJzLg0KICAgICAgLSByYW5nZTogeyBtaW46IDYsIG1heDogOCB9DQogICAgICAgIGRlc2NyaXB0aW9uOiBSYW5nZSBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3ZhbHVlczoNCiAgICAgICAgLSBtMS5zbWFsbA0KICAgICAgICAtIG0xLm1lZGl1bQ0KICAgICAgICAtIG0xLmxhcmdlDQogICAgICAgIGRlc2NyaXB0aW9uOiBBbGxvd2VkIHZhbHVlcyBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbYS16QS1aMC05XSsiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IGNvbnNpc3Qgb2YgY2hhcmFjdGVycyBhbmQgbnVtYmVycyBvbmx5Lg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbQS1aXStbYS16QS1aMC05XSoiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IHN0YXJ0IHdpdGggYW4gdXBwZXJjYXNlIGNoYXJhY3Rlci4NCiAgICAgIC0gY3VzdG9tX2NvbnN0cmFpbnQ6IG5vdmEua2V5cGFpcg0KICAgICAgICBkZXNjcmlwdGlvbjogQ3VzdG9tIGRlc2NyaXB0aW9uDQoNCnJlc291cmNlczoNCiAgbXlfaW5zdGFuY2UxOg0KICAgIHR5cGU6IE9TOjpOb3ZhOjpTZXJ2ZXINCiAgICBwcm9wZXJ0aWVzOg0KICAgICAgaW1hZ2U6IHsgZ2V0X3BhcmFtOiBpbWFnZV9uYW1lXzEgfQ0KICAgICAgZmxhdm9yOiBtMS5zbWFsbA0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9DQogIG15X2luc3RhbmNlMjoNCiAgICB0eXBlOiBPUzo6Tm92YTo6U2VydmVyDQogICAgcHJvcGVydGllczoNCiAgICAgIGltYWdlOiB7IGdldF9wYXJhbTogaW1hZ2VfbmFtZV8yIH0NCiAgICAgIGZsYXZvcjogbTEudGlueQ0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9";
		String decodedPaypload = Decoder.decode(payloadData);
		
		Assert.assertEquals(response, decodedPaypload, "Response deployment artifact not correct.");
		
		String auditAction = "DownloadArtifact";
		
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setResourceName(resourceDetailsService.getName());
		expectedResourceAuditJavaObject.setResourceType("Service");
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		
		expectedResourceAuditJavaObject.setCONSUMER_ID("ci");
		String resource_url = String.format("/sdc/v1/catalog/services/%s/artifacts/%s", resourceUUID, artifactUUID);
		expectedResourceAuditJavaObject.setRESOURCE_URL(resource_url);
		
		AuditValidationUtils.validateAuditDownloadExternalAPI(expectedResourceAuditJavaObject, auditAction, null, false);
	}
	
	
	
	
	
	
	// External API - Download ComponentInstance artifact of service - negative test
	@Test
	public void downloadArtifactOfComponentInstanceFromServiceViaExternalAPI() throws Exception {
		
		Either<Resource, RestResponse> resourceDetailsVF_01e = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		Component resourceDetailsVF_01 = resourceDetailsVF_01e.left().value();
		ArtifactDefinition heatArtifact = AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVF_01, UserRoleEnum.DESIGNER, true, true).left().value();

		resourceDetailsVF_01 = AtomicOperationUtils.changeComponentState(resourceDetailsVF_01, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		
		Service resourceDetailsService;
		Either<Service, RestResponse> createdResource = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		resourceDetailsService = createdResource.left().value();
		
		
		ComponentInstance resourceDetailsVF1ins_01 = AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVF_01, resourceDetailsService, UserRoleEnum.DESIGNER, true).left().value();
		
		
		System.out.println("-----");
		
		
		String resourceUUID = resourceDetailsService.getUUID();
		String componentNormalizedName = resourceDetailsVF1ins_01.getNormalizedName();
		String artifactUUID = heatArtifact.getArtifactUUID();
			
		System.out.println("Resource UUID: " + resourceUUID);
		System.out.println("Component NormalizedName: " + componentNormalizedName);
		System.out.println("Artifact UUID: " + artifactUUID);
		
		RestResponse restResponse = ArtifactRestUtils.getComponentInstanceDeploymentArtifactExternalAPI(resourceUUID, componentNormalizedName, artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), "Service");
//			
		Integer responseCode = restResponse.getErrorCode();
		Integer expectedCode = 200;
		Assert.assertEquals(responseCode,expectedCode, "Response code is not correct.");
			
		String response = restResponse.getResponse();
			
		String payloadData = "aGVhdF90ZW1wbGF0ZV92ZXJzaW9uOiAyMDEzLTA1LTIzDQoNCmRlc2NyaXB0aW9uOiBTaW1wbGUgdGVtcGxhdGUgdG8gZGVwbG95IGEgc3RhY2sgd2l0aCB0d28gdmlydHVhbCBtYWNoaW5lIGluc3RhbmNlcw0KDQpwYXJhbWV0ZXJzOg0KICBpbWFnZV9uYW1lXzE6DQogICAgdHlwZTogc3RyaW5nDQogICAgbGFiZWw6IEltYWdlIE5hbWUNCiAgICBkZXNjcmlwdGlvbjogU0NPSU1BR0UgU3BlY2lmeSBhbiBpbWFnZSBuYW1lIGZvciBpbnN0YW5jZTENCiAgICBkZWZhdWx0OiBjaXJyb3MtMC4zLjEteDg2XzY0DQogIGltYWdlX25hbWVfMjoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogSW1hZ2UgTmFtZQ0KICAgIGRlc2NyaXB0aW9uOiBTQ09JTUFHRSBTcGVjaWZ5IGFuIGltYWdlIG5hbWUgZm9yIGluc3RhbmNlMg0KICAgIGRlZmF1bHQ6IGNpcnJvcy0wLjMuMS14ODZfNjQNCiAgbmV0d29ya19pZDoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogTmV0d29yayBJRA0KICAgIGRlc2NyaXB0aW9uOiBTQ09ORVRXT1JLIE5ldHdvcmsgdG8gYmUgdXNlZCBmb3IgdGhlIGNvbXB1dGUgaW5zdGFuY2UNCiAgICBoaWRkZW46IHRydWUNCiAgICBjb25zdHJhaW50czoNCiAgICAgIC0gbGVuZ3RoOiB7IG1pbjogNiwgbWF4OiA4IH0NCiAgICAgICAgZGVzY3JpcHRpb246IFBhc3N3b3JkIGxlbmd0aCBtdXN0IGJlIGJldHdlZW4gNiBhbmQgOCBjaGFyYWN0ZXJzLg0KICAgICAgLSByYW5nZTogeyBtaW46IDYsIG1heDogOCB9DQogICAgICAgIGRlc2NyaXB0aW9uOiBSYW5nZSBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3ZhbHVlczoNCiAgICAgICAgLSBtMS5zbWFsbA0KICAgICAgICAtIG0xLm1lZGl1bQ0KICAgICAgICAtIG0xLmxhcmdlDQogICAgICAgIGRlc2NyaXB0aW9uOiBBbGxvd2VkIHZhbHVlcyBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbYS16QS1aMC05XSsiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IGNvbnNpc3Qgb2YgY2hhcmFjdGVycyBhbmQgbnVtYmVycyBvbmx5Lg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbQS1aXStbYS16QS1aMC05XSoiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IHN0YXJ0IHdpdGggYW4gdXBwZXJjYXNlIGNoYXJhY3Rlci4NCiAgICAgIC0gY3VzdG9tX2NvbnN0cmFpbnQ6IG5vdmEua2V5cGFpcg0KICAgICAgICBkZXNjcmlwdGlvbjogQ3VzdG9tIGRlc2NyaXB0aW9uDQoNCnJlc291cmNlczoNCiAgbXlfaW5zdGFuY2UxOg0KICAgIHR5cGU6IE9TOjpOb3ZhOjpTZXJ2ZXINCiAgICBwcm9wZXJ0aWVzOg0KICAgICAgaW1hZ2U6IHsgZ2V0X3BhcmFtOiBpbWFnZV9uYW1lXzEgfQ0KICAgICAgZmxhdm9yOiBtMS5zbWFsbA0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9DQogIG15X2luc3RhbmNlMjoNCiAgICB0eXBlOiBPUzo6Tm92YTo6U2VydmVyDQogICAgcHJvcGVydGllczoNCiAgICAgIGltYWdlOiB7IGdldF9wYXJhbTogaW1hZ2VfbmFtZV8yIH0NCiAgICAgIGZsYXZvcjogbTEudGlueQ0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9";
		String decodedPaypload = Decoder.decode(payloadData);
			
		Assert.assertEquals(response, decodedPaypload, "Response deployment artifact not correct.");
			
		String auditAction = "DownloadArtifact";
			
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setResourceName(resourceDetailsVF1ins_01.getName());
		expectedResourceAuditJavaObject.setResourceType("Service");
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
			
		expectedResourceAuditJavaObject.setCONSUMER_ID("ci");
		String resource_url = String.format("/sdc/v1/catalog/services/%s/resourceInstances/%s/artifacts/%s", resourceUUID, componentNormalizedName, artifactUUID);
		expectedResourceAuditJavaObject.setRESOURCE_URL(resource_url);
			
		AuditValidationUtils.validateAuditDownloadExternalAPI(expectedResourceAuditJavaObject, auditAction, null, false);
	}
	
	
	
	
	
	
	
	
	
	@Test
	public void downloadArtifactFromResourceTest() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			String jsonBody = createUploadArtifactBodyJson();

			String resourceId = resourceDetails.getUniqueId();
			String url = String.format(Urls.ADD_ARTIFACT_TO_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId);
			HttpPost httppost = createPostAddArtifactRequeast(jsonBody, url, true);
			HttpResponse response = httpclient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("failed to add artifact", 200, status);

			ArtifactDefinition origArtifact = getArtifactDataFromJson(jsonBody);
			addArtifactDataFromResponse(response, origArtifact);
			String artifactId = origArtifact.getUniqueId();
			
			url = String.format(Urls.UI_DOWNLOAD_RESOURCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId, artifactId);
			HttpGet httpGet = createGetRequest(url);
			response = httpclient.execute(httpGet);
			status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("failed to download artifact", 200, status);
									
			InputStream inputStream = response.getEntity().getContent();
			ArtifactUiDownloadData artifactUiDownloadData = getArtifactUiDownloadData(IOUtils.toString(inputStream));
			AssertJUnit.assertEquals("Downloaded payload is different from uploaded one", UPLOAD_ARTIFACT_PAYLOAD, artifactUiDownloadData.getBase64Contents());
			AssertJUnit.assertEquals("Downloaded artifact name is different from uploaded one", UPLOAD_ARTIFACT_NAME, artifactUiDownloadData.getArtifactName());

			// validate audit
					
			ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor.constructFieldsForAuditValidation(resourceDetails, resourceDetails.getVersion(), sdncUserDetails);
			String auditAction = "ArtifactDownload";
			expectedResourceAuditJavaObject.setAction(auditAction);
			expectedResourceAuditJavaObject.setPrevState("");
			expectedResourceAuditJavaObject.setPrevVersion("");
			expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
			expectedResourceAuditJavaObject.setStatus("200");
			expectedResourceAuditJavaObject.setDesc("OK");
			expectedResourceAuditJavaObject.setArtifactData(AuditValidationUtils.buildArtifactDataAudit(origArtifact));
			expectedResourceAuditJavaObject.setCurrArtifactUuid(origArtifact.getUniqueId());
			expectedResourceAuditJavaObject.setPrevArtifactUuid("");
			AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
			
		} finally {
			httpclient.close();
		}

	}

	@Test
	public void downloadArtifactFromServiceTest() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();

		try {

			String jsonStr = createUploadArtifactBodyJson();

			String url = String.format(Urls.ADD_ARTIFACT_TO_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), serviceDetails.getUniqueId());
			HttpPost httpPost = createPostAddArtifactRequeast(jsonStr, url, true);
			CloseableHttpResponse result = httpclient.execute(httpPost);
			int status = result.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("failed to add artifact", 200, status);

			ArtifactDefinition origArtifact = getArtifactDataFromJson(jsonStr);
			addArtifactDataFromResponse(result, origArtifact);
			String artifactId = origArtifact.getUniqueId();

			url = String.format(Urls.UI_DOWNLOAD_SERVICE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), serviceDetails.getUniqueId(), artifactId);
			HttpGet httpGet = createGetRequest(url);
			CloseableHttpResponse response2 = httpclient.execute(httpGet);
			status = response2.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("failed to download artifact", 200, status);
			InputStream inputStream = response2.getEntity().getContent();
			ArtifactUiDownloadData artifactUiDownloadData = getArtifactUiDownloadData(IOUtils.toString(inputStream));
			AssertJUnit.assertEquals("Downloaded payload is different from uploaded one", UPLOAD_ARTIFACT_PAYLOAD, artifactUiDownloadData.getBase64Contents());
			AssertJUnit.assertEquals("Downloaded artifact name is different from uploaded one", UPLOAD_ARTIFACT_NAME, artifactUiDownloadData.getArtifactName());

			// validate audit
			ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = AuditValidationUtils.constructFieldsForAuditValidation(serviceDetails, serviceDetails.getVersion(), sdncUserDetails);
			String auditAction = "ArtifactDownload";
			expectedResourceAuditJavaObject.setAction(auditAction);
			expectedResourceAuditJavaObject.setPrevState("");
			expectedResourceAuditJavaObject.setPrevVersion("");
			expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
			expectedResourceAuditJavaObject.setStatus("200");
			expectedResourceAuditJavaObject.setDesc("OK");
			expectedResourceAuditJavaObject.setArtifactData(AuditValidationUtils.buildArtifactDataAudit(origArtifact));
			expectedResourceAuditJavaObject.setCurrArtifactUuid(origArtifact.getUniqueId());
			expectedResourceAuditJavaObject.setPrevArtifactUuid("");
			
			AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

		} finally {
//			RestResponse response = ServiceRestUtils.deleteService(serviceDetails, serviceVersion, sdncUserDetails );
//			checkDeleteResponse(response);
			httpclient.close();
		}
	}

	@Test
	public void downloadArtifactFromResourceNotFound() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			String resourceId = resourceDetails.getUniqueId();
			String artifactIdNotFound = "11111";

			ArtifactDefinition origArtifact = new ArtifactDefinition();
			origArtifact.setUniqueId(artifactIdNotFound);
			
			String url = String.format(Urls.UI_DOWNLOAD_RESOURCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId, artifactIdNotFound);
			HttpGet httpGet = createGetRequest(url);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			int status = response.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("expected 404 not found", 404, status);

			// validate audit
			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name());
			ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor.constructFieldsForAuditValidation(resourceDetails, resourceDetails.getVersion(), sdncUserDetails);
			String auditAction = "ArtifactDownload";
			expectedResourceAuditJavaObject.setAction(auditAction);
			expectedResourceAuditJavaObject.setPrevState("");
			expectedResourceAuditJavaObject.setPrevVersion("");
			expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
			expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
			expectedResourceAuditJavaObject.setDesc(errorInfo.getAuditDesc(""));
			expectedResourceAuditJavaObject.setArtifactData("");
			expectedResourceAuditJavaObject.setCurrArtifactUuid(origArtifact.getUniqueId());
			expectedResourceAuditJavaObject.setPrevArtifactUuid("");
			AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
			expectedResourceAuditJavaObject.setPrevArtifactUuid(null);
		} finally {
			httpclient.close();
		}

	}

	@Test
	public void downloadArtifactFromServiceNotFound() throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			String artifactIdNotFound = "11111";
			ArtifactDefinition origArtifact = new ArtifactDefinition();
			origArtifact.setUniqueId(artifactIdNotFound);

			String url = String.format(Urls.UI_DOWNLOAD_SERVICE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), serviceDetails.getUniqueId(), artifactIdNotFound);
			HttpGet httpGet = createGetRequest(url);
			CloseableHttpResponse response2 = httpclient.execute(httpGet);
			int status = response2.getStatusLine().getStatusCode();
			AssertJUnit.assertEquals("expected 404 not found", 404, status);

			// validate audit
			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name());
			ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils.constructFieldsForAuditValidation(serviceDetails, serviceDetails.getVersion(), sdncUserDetails);
			String auditAction = "ArtifactDownload";
			expectedResourceAuditJavaObject.setAction(auditAction);
			expectedResourceAuditJavaObject.setPrevState("");
			expectedResourceAuditJavaObject.setPrevVersion("");
			expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
			expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
			expectedResourceAuditJavaObject.setDesc(errorInfo.getAuditDesc(""));
			expectedResourceAuditJavaObject.setArtifactData("");
			expectedResourceAuditJavaObject.setCurrArtifactUuid(origArtifact.getUniqueId());
			expectedResourceAuditJavaObject.setPrevArtifactUuid("");
			AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

		} finally {
			httpclient.close();
		}

	}

	@Test
	public void addArtifactToResourceTest() throws Exception {

		ArtifactReqDetails defaultArtifact = ElementFactory.getDefaultArtifact();

		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(defaultArtifact, sdncUserDetails, resourceDetails.getUniqueId());
		int status = response.getErrorCode();
		AssertJUnit.assertEquals("add informational artifact request returned status: " + response.getErrorCode(), 200, status);

		RestResponse resourceResp = ResourceRestUtils.getResource(resourceDetails.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
		AssertJUnit.assertNotNull(resource);

		Map<String, ArtifactDefinition> artifacts = resource.getArtifacts();
		boolean isExist = false;
		for (Map.Entry<String, ArtifactDefinition> entry : artifacts.entrySet()) {
			if (entry.getKey().equals(defaultArtifact.getArtifactLabel())) {
				isExist = true;

			}
		}
		AssertJUnit.assertTrue(isExist);
	}
	
	
	protected String createUploadArtifactBodyJson() {
		Map<String, Object> jsonBody = new HashMap<String, Object>();
		jsonBody.put("artifactName", UPLOAD_ARTIFACT_NAME);
		jsonBody.put("artifactDisplayName", "configure");
		jsonBody.put("artifactType", "SHELL");
		jsonBody.put("mandatory", "false");
		jsonBody.put("description", "ff");
		jsonBody.put("payloadData", UPLOAD_ARTIFACT_PAYLOAD);
		jsonBody.put("artifactLabel", "configure");
		return gson.toJson(jsonBody);
	}
	
	protected ArtifactDefinition getArtifactDataFromJson(String json) {
		Gson gson = new Gson();
		JsonObject jsonElement = new JsonObject();
		jsonElement = gson.fromJson(json, jsonElement.getClass());
		ArtifactDefinition artifact = new ArtifactDefinition(); 
		String payload = null;
		JsonElement artifactPayload = jsonElement.get(Constants.ARTIFACT_PAYLOAD_DATA);
		if (artifactPayload != null && !artifactPayload.isJsonNull()) {
			payload = artifactPayload.getAsString();
		}
		jsonElement.remove(Constants.ARTIFACT_PAYLOAD_DATA);
		artifact = gson.fromJson(jsonElement, ArtifactDefinition.class);
		artifact.setPayloadData(payload);
		
		/*atifact.setArtifactName(UPLOAD_ARTIFACT_NAME);
artifact.setArtifactDisplayName("configure");
artifact.setArtifactType("SHELL");
artifact.setMandatory(false);
artifact.setDescription("ff");
artifact.setPayloadData(UPLOAD_ARTIFACT_PAYLOAD);
artifact.setArtifactLabel("configure");*/
		return artifact;
	}
	
	protected HttpGet createGetRequest(String url) {
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		httpGet.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		httpGet.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		return httpGet;
	}
	
	protected String getArtifactUid(HttpResponse response) throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		String artifactId = (String) responseMap.get("uniqueId");
		return artifactId;
	}
	
	protected String getArtifactEsId(HttpResponse response) throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		String esId = (String) responseMap.get("EsId");
		return esId;
	}
	
	protected ArtifactDefinition addArtifactDataFromResponse(HttpResponse response, ArtifactDefinition artifact) throws HttpResponseException, IOException, ParseException {
		//String responseString = new BasicResponseHandler().handleResponse(response);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity);				
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		artifact.setEsId((String)responseMap.get("esId"));
		artifact.setUniqueId((String) responseMap.get("uniqueId"));
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.findType((String) responseMap.get("artifactGroupType")));
		artifact.setTimeout(((Long) responseMap.get("timeout")).intValue());
		return artifact;
	}
	
	protected String getLifecycleArtifactUid(CloseableHttpResponse response) throws HttpResponseException, IOException, ParseException {
		String responseString = new BasicResponseHandler().handleResponse(response);
		JSONObject responseMap = (JSONObject) jsonParser.parse(responseString);
		responseMap = (JSONObject) responseMap.get("implementation");
		String artifactId = (String) responseMap.get("uniqueId");
		return artifactId;
	}
	
	protected HttpDelete createDeleteArtifactRequest(String url) {
		HttpDelete httpDelete = new HttpDelete(url);
		httpDelete.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		httpDelete.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		return httpDelete;
	}
	
	protected HttpPost createPostAddArtifactRequeast(String jsonBody, String url, boolean addMd5Header) throws UnsupportedEncodingException {
		HttpPost httppost = new HttpPost(url);
		httppost.addHeader(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		httppost.addHeader(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		httppost.addHeader(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		if (addMd5Header) {
			httppost.addHeader(HttpHeaderEnum.Content_MD5.getValue(), GeneralUtility.calculateMD5Base64EncodedByString(jsonBody));
		}
		StringEntity input = new StringEntity(jsonBody);
		input.setContentType("application/json");
		httppost.setEntity(input);
		log.debug("Executing request {}" , httppost.getRequestLine());
		return httppost;
	}
	
	protected String createLoadArtifactBody() {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("artifactName", "install_apache2.sh");
		json.put("artifactType", "SHELL");
		json.put("description", "ddd");
		json.put("payloadData", "UEsDBAoAAAAIAAeLb0bDQz");
		json.put("artifactLabel", "name123");
		
		String jsonStr = gson.toJson(json);
		return jsonStr;
	}
	
	protected void checkDeleteResponse(RestResponse response) {
		BaseRestUtils.checkStatusCode(response, "delete request failed", false, 204, 404);
	}
	
	protected ArtifactUiDownloadData getArtifactUiDownloadData(String artifactUiDownloadDataStr) throws Exception {
		
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
