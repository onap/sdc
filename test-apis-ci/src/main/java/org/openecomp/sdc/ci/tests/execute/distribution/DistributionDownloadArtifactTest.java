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

package org.openecomp.sdc.ci.tests.execute.distribution;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedDistDownloadAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ConsumerRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DistributionDownloadArtifactTest extends ComponentBaseTest {

	protected static ResourceReqDetails resourceDetails;
	protected static User designerUser;
	protected static User adminUser;
	protected static String resourceBaseVersion;
	// user ci password 123456
	protected final String authorizationHeader = "Basic Y2k6MTIzNDU2";
	protected ConsumerDataDefinition consumerDataDefinition;

	@Rule
	public static TestName name = new TestName();
	protected static String artifactInterfaceType;
	protected static String artifactOperationName;

	protected static ServiceReqDetails serviceDetails;
	protected static String serviceBaseVersion;
	protected static String serviceUniqueId;
	protected final String USER = "ci";
	protected final String PASSWORD = "123456";
	protected final String SALT = "2a1f887d607d4515d4066fe0f5452a50";
	protected final String HASHED_PASSWORD = "0a0dc557c3bf594b1a48030e3e99227580168b21f44e285c69740b8d5b13e33b";

	public DistributionDownloadArtifactTest() {
		super(name, DistributionDownloadArtifactTest.class.getName());
	}

	// @BeforeClass
	// public static void InitBeforeTest() throws Exception
	// {
	//
	//
	// resourceBaseVersion = "0.1";
	// serviceBaseVersion = "0.1";
	// designerUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	// adminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	// resourceDetails =
	// ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
	// NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS,
	// adminUser);
	// serviceDetails = ElementFactory.getDefaultService();
	// serviceUniqueId = "svc_" + serviceDetails.getName().toLowerCase() + "." +
	// serviceBaseVersion;
	// artifactInterfaceType = "standard";
	// artifactOperationName = "start";
	// }

	@BeforeMethod
	public void setup() throws Exception {

		resourceBaseVersion = "0.1";
		serviceBaseVersion = "0.1";
		designerUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		adminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		resourceDetails = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, adminUser);
		serviceDetails = ElementFactory.getDefaultService();
		serviceUniqueId = "svc_" + serviceDetails.getName().toLowerCase() + "." + serviceBaseVersion;
		artifactInterfaceType = "standard";
		artifactOperationName = "start";
		consumerDataDefinition = createConsumer();
		RestResponse deleteResponse = ConsumerRestUtils.deleteConsumer(consumerDataDefinition, adminUser);
		BaseRestUtils.checkStatusCode(deleteResponse, "delete operation filed", false, 404, 200);

		RestResponse createResponse = ConsumerRestUtils.createConsumer(consumerDataDefinition, adminUser);
		BaseRestUtils.checkCreateResponse(createResponse);
	}

	@Test
	public void downloadResourceArtifactSuccess() throws Exception {
		// Create service
		RestResponse serviceResponse = ServiceRestUtils.createService(serviceDetails, designerUser);
		AssertJUnit.assertEquals("Check response code after creating resource", 201,
				serviceResponse.getErrorCode().intValue());

		// Create resource
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, designerUser);
		AssertJUnit.assertEquals("Check response code after creating resource", 201,
				createResource.getErrorCode().intValue());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(createResource.getResponse());

		ArtifactReqDetails artifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		// Setting the name to be with space
		artifactDetails.setArtifactName("test artifact file.yaml");
		// artifactDetails.set(ArtifactRestUtils.calculateChecksum(artifactDetails));

		RestResponse addArtifactResponse = ArtifactRestUtils.addInformationalArtifactToResource(artifactDetails,
				designerUser, resource.getUniqueId(), ArtifactRestUtils.calculateChecksum(artifactDetails));
		AssertJUnit.assertEquals("Check response code after adding interface artifact", 200,
				addArtifactResponse.getErrorCode().intValue());

		// Getting expected artifact checksum
		ArtifactDefinition artifactResp = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(addArtifactResponse.getResponse());
		String expectedPayloadChecksum = artifactResp.getArtifactChecksum();

		Config config = Utils.getConfig();
		String relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL,
				ValidationUtils.convertToSystemName(serviceDetails.getName()), serviceBaseVersion,
				ValidationUtils.convertToSystemName(resource.getName()), resource.getVersion(),
				artifactResp.getArtifactName()));
		// String fullUrlFormatted =
		// String.format(Urls.DOWNLOAD_RESOURCE_ARTIFACT_FULL_URL,
		// config.getCatalogBeHost(),config.getCatalogBePort(), relativeUrl);
		// String consumerId = "dummy.ecomp";

		ResourceReqDetails resourceInfo = new ResourceReqDetails();
		resourceInfo.setName(resource.getName());
		resourceInfo.setVersion(resource.getVersion());

		Map<String, String> authorizationHeaders = new HashMap<String, String>();
		authorizationHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		RestResponse restResponse = ArtifactRestUtils.downloadResourceArtifact(serviceDetails, resourceInfo,
				artifactDetails, designerUser, authorizationHeaders);
		// RestResponse restResponse =
		// artifactUtils.downloadResourceArtifact(designerUser,fullUrlFormatted,
		// consumerId,true);
		AssertJUnit.assertEquals("Check response code after download resource", 200,
				restResponse.getErrorCode().intValue());

		// Validating headers
		// content disposition
		List<String> contDispHeaderList = restResponse.getHeaderFields().get(Constants.CONTENT_DISPOSITION_HEADER);
		AssertJUnit.assertNotNull(contDispHeaderList);
		AssertJUnit
				.assertEquals(
						"Check content disposition header", new StringBuilder().append("attachment; filename=\"")
								.append(artifactResp.getArtifactName()).append("\"").toString(),
						contDispHeaderList.get(0));

		// content type
		List<String> contTypeHeaderList = restResponse.getHeaderFields().get(Constants.CONTENT_TYPE_HEADER);
		AssertJUnit.assertNotNull(contTypeHeaderList);
		AssertJUnit.assertEquals("Check content type", "application/octet-stream", contTypeHeaderList.get(0));

		String actualContents = restResponse.getResponse();

		// Contents - comparing decoded content
		AssertJUnit.assertEquals(artifactDetails.getPayload(), Base64.encodeBase64String(actualContents.getBytes()));

		// validating checksum
		String actualPayloadChecksum = GeneralUtility.calculateMD5Base64EncodedByByteArray(actualContents.getBytes());
		AssertJUnit.assertEquals(expectedPayloadChecksum, actualPayloadChecksum);

		// validate audit
		String auditAction = "DArtifactDownload";

		ExpectedDistDownloadAudit expectedDistDownloadAudit = new ExpectedDistDownloadAudit(auditAction,
				BaseRestUtils.ecomp, relativeUrl, "200", "OK");
		AuditValidationUtils.validateAudit(expectedDistDownloadAudit, auditAction);
	}

	protected void download_serviceNameNotFound_inner(String serviceName, String serviceVersion, String resourceName,
			String resourceVersion) throws Exception {
		Config config = Utils.getConfig();
		String artifactName = "kuku";
		ArtifactReqDetails artifact = new ArtifactReqDetails();
		artifact.setArtifactName(artifactName);
		String relativeUrl;
		Map<String, String> authorizationHeaders = new HashMap<String, String>();
		authorizationHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		ServiceReqDetails serviceInfo = new ServiceReqDetails();
		serviceInfo.setName(serviceName);
		serviceInfo.setVersion(serviceVersion);
		RestResponse restResponse = null;
		if (resourceName != null) {
			ResourceReqDetails resourceDetailes = new ResourceReqDetails();
			resourceDetailes.setName(resourceName);
			resourceDetailes.setVersion(resourceVersion);
			relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL,
					ValidationUtils.convertToSystemName(serviceName), serviceVersion,
					ValidationUtils.convertToSystemName(resourceName), resourceVersion, artifactName));
			restResponse = ArtifactRestUtils.downloadResourceArtifact(serviceInfo, resourceDetailes, artifact,
					designerUser, authorizationHeaders);
		} else {
			relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_SERVICE_ARTIFACT_RELATIVE_URL,
					ValidationUtils.convertToSystemName(serviceName), serviceVersion, artifactName));
			restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceInfo, artifact, designerUser,
					authorizationHeaders);
		}

		// RestResponse restResponse =
		// artifactUtils.downloadResourceArtifact(designerUser,fullUrlFormatted,
		// consumerId,true);
		AssertJUnit.assertEquals("Check response code after download resource", 404,
				restResponse.getErrorCode().intValue());

		// validate audit
		String auditAction = "DArtifactDownload";

		ExpectedDistDownloadAudit expectedDistDownloadAudit = new ExpectedDistDownloadAudit(auditAction,
				BaseRestUtils.ecomp, relativeUrl, "404", "SVC4503: Error: Requested '"
						+ ValidationUtils.convertToSystemName(serviceName) + "' service was not found.");
		AuditValidationUtils.validateAudit(expectedDistDownloadAudit, auditAction);
	}

	protected void download_serviceVersionNotFound_inner(String serviceName, String serviceVersion, String resourceName,
			String resourceVersion) throws Exception {
		Config config = Utils.getConfig();
		String artifactName = "kuku";
		String relativeUrl;
		ArtifactReqDetails artifact = new ArtifactReqDetails();
		artifact.setArtifactName(artifactName);
		Map<String, String> authorizationHeaders = new HashMap<String, String>();
		authorizationHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		ServiceReqDetails serviceInfo = new ServiceReqDetails();
		serviceInfo.setName(serviceName);
		serviceInfo.setVersion(serviceVersion);
		RestResponse restResponse = null;
		if (resourceName != null) {
			ResourceReqDetails resourceDetailes = new ResourceReqDetails();
			resourceDetailes.setName(resourceName);
			resourceDetailes.setVersion(resourceVersion);
			relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL,
					ValidationUtils.convertToSystemName(serviceName), serviceVersion,
					ValidationUtils.convertToSystemName(resourceName), resourceVersion, artifactName));
			restResponse = ArtifactRestUtils.downloadResourceArtifact(serviceInfo, resourceDetailes, artifact,
					designerUser, authorizationHeaders);
		} else {
			relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_SERVICE_ARTIFACT_RELATIVE_URL,
					ValidationUtils.convertToSystemName(serviceName), serviceVersion, artifactName));
			restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceInfo, artifact, designerUser,
					authorizationHeaders);
		}
		// String fullUrlFormatted =
		// String.format(Urls.DOWNLOAD_RESOURCE_ARTIFACT_FULL_URL,
		// config.getCatalogBeHost(),config.getCatalogBePort(), relativeUrl);
		// String consumerId = "dummy.ecomp";

		// RestResponse restResponse =
		// artifactUtils.downloadResourceArtifact(designerUser,fullUrlFormatted,
		// consumerId,true);
		AssertJUnit.assertEquals("Check response code after download resource", 404,
				restResponse.getErrorCode().intValue());

		// validate audit
		String auditAction = "DArtifactDownload";

		ExpectedDistDownloadAudit expectedDistDownloadAudit = new ExpectedDistDownloadAudit(auditAction,
				BaseRestUtils.ecomp, relativeUrl, "404",
				"SVC4504: Error: Service version " + serviceVersion + " was not found.");
		AuditValidationUtils.validateAudit(expectedDistDownloadAudit, auditAction);
	}

	protected String encodeUrlForDownload(String url) {
		return url.replaceAll(" ", "%20");
	}

	protected ConsumerDataDefinition createConsumer() {
		ConsumerDataDefinition consumer = new ConsumerDataDefinition();
		consumer.setConsumerName(USER);
		consumer.setConsumerSalt(SALT);
		consumer.setConsumerPassword(HASHED_PASSWORD);
		return consumer;

	}

	@Test(enabled = false)
	public void downloadServiceArtifactSuccess() throws Exception {
		// Create service
		RestResponse serviceResponse = ServiceRestUtils.createService(serviceDetails, designerUser);
		assertEquals("Check response code after creating resource", 201, serviceResponse.getErrorCode().intValue());
		serviceUniqueId = ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse())
				.getUniqueId();

		ArtifactReqDetails artifactDetails = ElementFactory.getDefaultDeploymentArtifactForType("MURANO_PKG");

		RestResponse addArtifactResponse = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails,
				designerUser, serviceUniqueId, ArtifactRestUtils.calculateMD5Header(artifactDetails));
		assertEquals("Check response code after adding interface artifact", 200,
				addArtifactResponse.getErrorCode().intValue());

		// Getting expected artifact checksum

		// ArtifactResJavaObject artifactResp =
		// artifactUtils.parseInformationalArtifactResp(addArtifactResponse);
		String expectedPayloadChecksum = ResponseParser
				.convertArtifactDefinitionResponseToJavaObject(addArtifactResponse.getResponse()).getArtifactChecksum();

		String artifactName = ValidationUtils.normalizeFileName(artifactDetails.getArtifactName());

		String relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_SERVICE_ARTIFACT_RELATIVE_URL,
				ValidationUtils.convertToSystemName(serviceDetails.getName()), serviceBaseVersion, artifactName));

		Map<String, String> authorizationHeaders = new HashMap<String, String>();
		authorizationHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, artifactDetails,
				designerUser, authorizationHeaders);
		assertEquals("Check response code after download resource", 200, restResponse.getErrorCode().intValue());

		// Validating headers
		// content disposition
		List<String> contDispHeaderList = restResponse.getHeaderFields().get(Constants.CONTENT_DISPOSITION_HEADER);
		assertNotNull(contDispHeaderList);
		assertEquals("Check content disposition header",
				new StringBuilder().append("attachment; filename=\"").append(artifactName).append("\"").toString(),
				contDispHeaderList.get(0));

		// content type
		List<String> contTypeHeaderList = restResponse.getHeaderFields().get(Constants.CONTENT_TYPE_HEADER);
		assertNotNull(contTypeHeaderList);
		assertEquals("Check content type", "application/octet-stream", contTypeHeaderList.get(0));

		String actualContents = restResponse.getResponse();

		assertEquals(artifactDetails.getPayload(), Base64.encodeBase64String(actualContents.getBytes()));

		// validating checksum
		byte[] bytes = actualContents.getBytes();
		String actualPayloadChecksum = GeneralUtility.calculateMD5Base64EncodedByByteArray(bytes);
		assertEquals(expectedPayloadChecksum, actualPayloadChecksum);

		// validating valid zip
		InputStream is = new ByteArrayInputStream(bytes);
		InputStream zis = new ZipInputStream(is);
		zis.close();

		// validate audit
		String auditAction = "DArtifactDownload";

		ExpectedDistDownloadAudit expectedDistDownloadAudit = new ExpectedDistDownloadAudit(auditAction,
				ResourceRestUtils.ecomp, encodeUrlForDownload(relativeUrl), "200", "OK");
		AuditValidationUtils.validateAudit(expectedDistDownloadAudit, auditAction);
	}

	@Test
	public void downloadResourceArtifact_NoConsumerId() throws Exception {

		String artifactName = "kuku";
		ArtifactReqDetails artifact = new ArtifactReqDetails();
		artifact.setArtifactName(artifactName);
		ResourceReqDetails resource = new ResourceReqDetails();
		resource.setName("notExisting");
		resource.setVersion("0.1");
		String relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL,
				ValidationUtils.convertToSystemName(serviceDetails.getName()), serviceBaseVersion,
				ValidationUtils.convertToSystemName(resource.getName()), resource.getVersion(), artifactName));
		serviceDetails.setVersion("0.1");
		Map<String, String> authorizationHeaders = new HashMap<String, String>();
		authorizationHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		RestResponse restResponse = ArtifactRestUtils.downloadResourceArtifact(serviceDetails, resource, artifact,
				designerUser, authorizationHeaders, false);
		assertEquals("Check response code after download resource", 400, restResponse.getErrorCode().intValue());

		// validate audit
		String auditAction = "DArtifactDownload";

		ExpectedDistDownloadAudit expectedDistDownloadAudit = new ExpectedDistDownloadAudit(auditAction, "",
				relativeUrl, "400", "POL5001: Error: Missing 'X-ECOMP-InstanceID' HTTP header.");
		AuditValidationUtils.validateAudit(expectedDistDownloadAudit, auditAction);
	}

	@Test
	public void downloadResourceArtifact_ResourceNameNotFound() throws Exception {

		String artifactName = "kuku";
		ArtifactReqDetails artifact = new ArtifactReqDetails();
		artifact.setArtifactName(artifactName);
		ResourceReqDetails resource = new ResourceReqDetails();
		resource.setName("notExisting");
		resource.setVersion("0.1");
		serviceDetails.setVersion("0.1");
		String relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL,
				ValidationUtils.convertToSystemName(serviceDetails.getName()), serviceDetails.getVersion(),
				ValidationUtils.convertToSystemName(resource.getName()), resource.getVersion(), artifactName));

		Map<String, String> authorizationHeaders = new HashMap<String, String>();
		authorizationHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		RestResponse restResponse = ArtifactRestUtils.downloadResourceArtifact(serviceDetails, resource, artifact,
				designerUser, authorizationHeaders);

		assertEquals("Check response code after download resource", 404, restResponse.getErrorCode().intValue());

		// validate audit
		String auditAction = "DArtifactDownload";

		ExpectedDistDownloadAudit expectedDistDownloadAudit = new ExpectedDistDownloadAudit(auditAction,
				BaseRestUtils.ecomp, relativeUrl, "404",
				"SVC4063: Error: Requested 'Notexisting' resource was not found.");
		AuditValidationUtils.validateAudit(expectedDistDownloadAudit, auditAction);
	}

	@Test
	public void downloadResourceArtifact_ResourceVersionNotFound() throws Exception {
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, designerUser);
		assertEquals("Check response code after creating resource", 201, createResource.getErrorCode().intValue());

		Resource resource = ResponseParser.convertResourceResponseToJavaObject(createResource.getResponse());
		ResourceReqDetails resourceDetailes = new ResourceReqDetails();
		resourceDetailes.setName(resource.getName());
		resourceDetailes.setVersion("0.2");

		serviceDetails.setVersion("0.1");

		String artifactName = "kuku";
		ArtifactReqDetails artifact = new ArtifactReqDetails();
		artifact.setArtifactName(artifactName);

		String relativeUrl = encodeUrlForDownload(String.format(Urls.DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL,
				ValidationUtils.convertToSystemName(serviceDetails.getName()), serviceBaseVersion,
				ValidationUtils.convertToSystemName(resourceDetailes.getName()), resourceDetailes.getVersion(),
				artifactName));

		Map<String, String> authorizationHeaders = new HashMap<String, String>();
		authorizationHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		RestResponse restResponse = ArtifactRestUtils.downloadResourceArtifact(serviceDetails, resourceDetailes,
				artifact, designerUser, authorizationHeaders);
		assertEquals("Check response code after download resource", 404, restResponse.getErrorCode().intValue());

		// validate audit
		String auditAction = "DArtifactDownload";

		ExpectedDistDownloadAudit expectedDistDownloadAudit = new ExpectedDistDownloadAudit(auditAction,
				BaseRestUtils.ecomp, relativeUrl, "404", "SVC4504: Error: Resource version 0.2 was not found.");
		AuditValidationUtils.validateAudit(expectedDistDownloadAudit, auditAction);
	}

	@Test
	public void downloadResourceArtifact_ServiceNameNotFound() throws Exception {
		// Create resource
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, designerUser);
		assertEquals("Check response code after creating resource", 201, createResource.getErrorCode().intValue());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(createResource.getResponse());
		download_serviceNameNotFound_inner("notExistingServiceName", serviceBaseVersion, resource.getName(),
				resource.getVersion());

	}

	@Test
	public void downloadResourceArtifact_ServiceVersionNotFound() throws Exception {
		// Create resource
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, designerUser);
		assertEquals("Check response code after creating resource", 201, createResource.getErrorCode().intValue());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(createResource.getResponse());

		// Create service
		RestResponse serviceResponse = ServiceRestUtils.createService(serviceDetails, designerUser);
		assertEquals("Check response code after creating resource", 201, serviceResponse.getErrorCode().intValue());
		serviceUniqueId = ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse())
				.getUniqueId();

		download_serviceVersionNotFound_inner(serviceDetails.getName(), "0.3", resource.getName(),
				resource.getVersion());
	}

	@Test
	public void downloadServiceArtifact_ServiceNameNotFound() throws Exception {
		download_serviceNameNotFound_inner("notExistingServiceName", serviceBaseVersion, null, null);

	}

	@Test
	public void downloadServiceArtifact_ServiceVersionNotFound() throws Exception {

		// Create service
		RestResponse serviceResponse = ServiceRestUtils.createService(serviceDetails, designerUser);
		assertEquals("Check response code after creating resource", 201, serviceResponse.getErrorCode().intValue());
		serviceUniqueId = ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse())
				.getUniqueId();

		download_serviceVersionNotFound_inner(serviceDetails.getName(), "0.2", null, null);
	}

}
