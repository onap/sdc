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

package org.openecomp.sdc.ci.tests.execute.resource;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetAllResourceVersions extends ComponentBaseTest {

	private static Logger logger = LoggerFactory.getLogger(GetAllResourceVersions.class.getName());
	protected User designerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	protected User adminModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

	protected ResourceReqDetails resourceDetails;

	public static TestName name = new TestName();

	public GetAllResourceVersions() {
		super(name, GetAllResourceVersions.class.getName());

	}

	//// NEW

	protected void deleteAllVersionOfResource() throws Exception {
		RestResponse response = null;

		String[] versions = { "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "2.0",
				"2.1", "2.2", "2.3", "2.4", "2.5", "3.0", "4.0", "4.1" };

		for (String version : versions) {

			response = ResourceRestUtils.deleteResourceByNameAndVersion(designerDetails,
					resourceDetails.getName().toUpperCase(), version);
			AssertJUnit.assertTrue("delete request returned status:" + response.getErrorCode(),
					response.getErrorCode() == 204 || response.getErrorCode() == 404);

			response = ResourceRestUtils.deleteResourceByNameAndVersion(designerDetails, resourceDetails.getName(),
					version);
			AssertJUnit.assertTrue("delete request returned status:" + response.getErrorCode(),
					response.getErrorCode() == 204 || response.getErrorCode() == 404);

		}
	}

	@BeforeMethod
	public void init() throws Exception {
		resourceDetails = defineResourse();
		deleteAllVersionOfResource();

	}

	@AfterMethod
	public void endOfTests() throws Exception {
		deleteAllVersionOfResource();
	}

	protected ResourceReqDetails defineResourse() {
		String resourceName = "cisco4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		// String category = ServiceCategoriesEnum.MOBILITY.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("tosca.nodes.Root");
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, null,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_INFRASTRUCTURE.getCategory(),
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE.getSubCategory());

		return resourceDetails;
	}

	@Test
	public void getResourceAllVersions_version15() throws Exception {
		// create resource
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		RestResponse restResponse = createResource(designerDetails, resourceDetails);
		AssertJUnit.assertTrue("create request returned status:" + restResponse.getErrorCode(),
				restResponse.getErrorCode() == 201);
		String resourceName = resourceDetails.getName();
		// resourceUtils.addResourceMandatoryArtifacts(designerDetails,
		// restResponse);

		// change resource version to 0.5
		RestResponse checkoutResource;
		for (int x = 0; x < 4; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			AssertJUnit.assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());

			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			AssertJUnit.assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
		}

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200,
				checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200,
				checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200,
				checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFY);
		AssertJUnit.assertEquals("Check response code after checkout resource", 200,
				checkoutResource.getErrorCode().intValue());
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());
		// change resource version to 1.5
		for (int x = 0; x < 5; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());
			AssertJUnit.assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			AssertJUnit.assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
		}

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails,
				resourceDetails.getUniqueId());
		Resource res = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		AssertJUnit.assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	protected RestResponse createResource(User sdncModifierDetails, ResourceReqDetails resourceDetails)
			throws Exception {
		// clean ES DB
		DbUtils.cleanAllAudits();

		// create resource
		RestResponse restResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response
		AssertJUnit.assertNotNull("check response object is not null after create resource", restResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create resource",
				restResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create resource", 201,
				restResponse.getErrorCode().intValue());

		return restResponse;
	}

	@Test
	public void getResourceAllVersions_version05() throws Exception {

		// create resource
		RestResponse restResponse = createResource(designerDetails, resourceDetails);
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());
		// resourceUtils.addResourceMandatoryArtifacts(designerDetails,
		// restResponse);
		// change resource version to 0.5
		RestResponse checkoutResource;

		logger.debug("Changing resource life cycle ");
		for (int x = 0; x < 4; x++) {
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());
		}
		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails,
				resourceDetails.getUniqueId());
		Resource res = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	@Test
	public void getResourceAllVersions_version01() throws Exception {
		// create resource
		RestResponse restResponse = createResource(designerDetails, resourceDetails);
		String resourceName = resourceDetails.getName();

		Map<String, String> origVersionsMap = new HashMap<String, String>();
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());

		// resourceUtils.addResourceMandatoryArtifacts(designerDetails,
		// restResponse);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails,
				resourceDetails.getUniqueId());
		Resource res = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	@Test
	public void getResourceAllVersions_version25() throws Exception {

		Map<String, String> origVersionsMap = new HashMap<String, String>();

		// create resource
		RestResponse restResponse = createResource(designerDetails, resourceDetails);
		assertTrue("create request returned status:" + restResponse.getErrorCode(), restResponse.getErrorCode() == 201);
		String resourceName = resourceDetails.getName();
		// resourceUtils.addResourceMandatoryArtifacts(designerDetails,
		// restResponse);

		// change resource version to 0.5
		RestResponse checkoutResource;
		for (int x = 0; x < 4; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());

			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
		}

		// resource version 1.0
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());

		// change resource version to 1.5
		for (int x = 0; x < 5; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
		}

		// resource version 2.0
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());

		// change resource version to 2.5
		for (int x = 0; x < 5; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());

			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
		}

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails,
				resourceDetails.getUniqueId());
		Resource res = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	@Test
	public void getResourceAllVersions_ReadyForCertification_version05() throws Exception {
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		// create resource
		RestResponse restResponse = createResource(designerDetails, resourceDetails);
		assertTrue("create request returned status:" + restResponse.getErrorCode(), restResponse.getErrorCode() == 201);
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());
		String resourceName = resourceDetails.getName();
		// resourceUtils.addResourceMandatoryArtifacts(designerDetails,
		// restResponse);

		// change resource version to 0.5
		RestResponse checkoutResource;
		for (int x = 0; x < 4; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());

			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());
		}

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails,
				resourceDetails.getUniqueId());
		Resource res = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	@Test
	public void getResourceAllVersions_CertifactionInProgress_version05() throws Exception {
		Map<String, String> origVersionsMap = new HashMap<String, String>();
		// create resource
		RestResponse restResponse = createResource(designerDetails, resourceDetails);
		assertTrue("create request returned status:" + restResponse.getErrorCode(), restResponse.getErrorCode() == 201);
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());

		String resourceName = resourceDetails.getName();
		// resourceUtils.addResourceMandatoryArtifacts(designerDetails,
		// restResponse);

		// change resource version to 0.5
		RestResponse checkoutResource;
		for (int x = 0; x < 4; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());

			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());
		}

		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails,
				resourceDetails.getUniqueId());
		Resource res = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	@Test
	public void getResourceAllVersions_Certified_version10() throws Exception {

		Map<String, String> origVersionsMap = new HashMap<String, String>();

		// create resource
		RestResponse restResponse = createResource(designerDetails, resourceDetails);
		assertTrue("create request returned status:" + restResponse.getErrorCode(), restResponse.getErrorCode() == 201);
		String resourceName = resourceDetails.getName();
		// resourceUtils.addResourceMandatoryArtifacts(designerDetails,
		// restResponse);

		// change resource version to 0.5
		RestResponse checkoutResource;
		for (int x = 0; x < 4; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());

		}
		logger.debug("Changing resource life cycle ");
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());
		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails,
				resourceDetails.getUniqueId());
		Resource res = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	@Test
	public void getResourceAllVersions_Certified_version20() throws Exception {

		Map<String, String> origVersionsMap = new HashMap<String, String>();

		// create resource
		RestResponse restResponse = createResource(designerDetails, resourceDetails);
		assertTrue("create request returned status:" + restResponse.getErrorCode(), restResponse.getErrorCode() == 201);
		String resourceName = resourceDetails.getName();
		// resourceUtils.addResourceMandatoryArtifacts(designerDetails,
		// restResponse);

		// change resource version to 0.5
		RestResponse checkoutResource;
		for (int x = 0; x < 4; x++) {
			logger.debug("Changing resource life cycle ");
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
		}

		// get to version 1.0
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());

		// change resource version to 1.5
		for (int x = 0; x < 4; x++) {
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
			checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, designerDetails,
					resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
			assertEquals("Check response code after checkout resource", 200,
					checkoutResource.getErrorCode().intValue());
		}

		// get to version 1.0
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		checkoutResource = LifecycleRestUtils.changeResourceState(resourceDetails, adminModifierDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code after checkout resource", 200, checkoutResource.getErrorCode().intValue());
		origVersionsMap.put(resourceDetails.getVersion(), resourceDetails.getUniqueId());

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails,
				resourceDetails.getUniqueId());
		Resource res = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		Map<String, String> getVersionsMap = res.getAllVersions();
		assertTrue(origVersionsMap.equals(getVersionsMap));

	}

	@Test
	public void getResourceAllVersions_ResourceNotFound() throws Exception {

		RestResponse resourceGetResponse = ResourceRestUtils.getResource(designerDetails, "123456789");
		assertEquals("Check response code after checkout resource", 404, resourceGetResponse.getErrorCode().intValue());

	}

}
