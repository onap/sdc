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

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.AssocType;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fj.data.Either;

public class ValidateExtendedVfData extends ComponentBaseTest {

	protected Resource resourceDetailsVF;
	protected Resource resourceDetailsCP_01;
	protected Resource resourceDetailsVL_01;
	protected Resource resourceDetailsVFCcomp;

	protected User sdncUserDetails;

	@Rule
	public static TestName name = new TestName();

	public ValidateExtendedVfData() {
		super(name, ValidateExtendedVfData.class.getName());
	}

	@BeforeMethod
	public void create() throws Exception {

		sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		Either<Resource, RestResponse> resourceDetailsVFe = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true);
		resourceDetailsVF = resourceDetailsVFe.left().value();
		Either<Resource, RestResponse> resourceDetailsCP_01e = AtomicOperationUtils
				.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.CP, NormativeTypesEnum.PORT,
						ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true);
		resourceDetailsCP_01 = resourceDetailsCP_01e.left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsCP_01, UserRoleEnum.DESIGNER,
				true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_VOL, resourceDetailsCP_01,
				UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_VOL, resourceDetailsCP_01,
				UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_NET, resourceDetailsCP_01,
				UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, resourceDetailsCP_01, UserRoleEnum.DESIGNER,
				true, true);
		AtomicOperationUtils.changeComponentState(resourceDetailsCP_01, UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CERTIFY, true);
		Either<Resource, RestResponse> resourceDetailsVL_01e = AtomicOperationUtils
				.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VL, NormativeTypesEnum.NETWORK,
						ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS, UserRoleEnum.DESIGNER, true);
		resourceDetailsVL_01 = resourceDetailsVL_01e.left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVL_01, UserRoleEnum.DESIGNER,
				true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_VOL, resourceDetailsVL_01,
				UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_VOL, resourceDetailsVL_01,
				UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_NET, resourceDetailsVL_01,
				UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, resourceDetailsVL_01, UserRoleEnum.DESIGNER,
				true, true);
		AtomicOperationUtils.changeComponentState(resourceDetailsVL_01, UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CERTIFY, true);

		Either<Resource, RestResponse> resourceDetailsVFCcompE = AtomicOperationUtils
				.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.COMPUTE,
						ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetailsVFCcomp = resourceDetailsVFCcompE.left().value();
		AtomicOperationUtils.changeComponentState(resourceDetailsVFCcomp, UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CERTIFY, true);

		ComponentInstance resourceDetailsCP_01ins = AtomicOperationUtils
				.addComponentInstanceToComponentContainer(resourceDetailsCP_01, resourceDetailsVF,
						UserRoleEnum.DESIGNER, true)
				.left().value();
		ComponentInstance resourceDetailsVL_01ins = AtomicOperationUtils
				.addComponentInstanceToComponentContainer(resourceDetailsVL_01, resourceDetailsVF,
						UserRoleEnum.DESIGNER, true)
				.left().value();
		ComponentInstance resourceDetailsVFCcomp_ins = AtomicOperationUtils
				.addComponentInstanceToComponentContainer(resourceDetailsVFCcomp, resourceDetailsVF,
						UserRoleEnum.DESIGNER, true)
				.left().value();

		resourceDetailsVF = AtomicOperationUtils.getResourceObject(resourceDetailsVF, UserRoleEnum.DESIGNER);
		AtomicOperationUtils.associate2ResourceInstances(resourceDetailsVF, resourceDetailsCP_01ins,
				resourceDetailsVL_01ins, AssocType.LINKABLE.getAssocType(), UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.associate2ResourceInstances(resourceDetailsVF, resourceDetailsCP_01ins,
				resourceDetailsVFCcomp_ins, AssocType.BINDABLE.getAssocType(), UserRoleEnum.DESIGNER, true);

	}

	@Test
	public void getResourceLatestVersion() throws Exception {

		RestResponse response = LifecycleRestUtils.changeComponentState(resourceDetailsVF, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LC state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		// resourceDetailsVF =
		// AtomicOperationUtils.getResourceObject(resourceDetailsVF,
		// UserRoleEnum.DESIGNER);
		RestResponse getResourceLatestVersionResponse = ResourceRestUtils.getResourceLatestVersionList(sdncUserDetails);
		assertTrue("response code is not 200, returned :" + getResourceLatestVersionResponse.getErrorCode(),
				getResourceLatestVersionResponse.getErrorCode() == 200);

		List<Resource> resourceList = ResourceRestUtils
				.restResponseToResourceObjectList(getResourceLatestVersionResponse.getResponse());
		Resource resource = ResourceRestUtils.getResourceObjectFromResourceListByUid(resourceList,
				resourceDetailsVF.getUniqueId());

		callAllCheckMethods(resource);
	}

	@Test
	public void getFollowedResources() throws Exception {

		RestResponse response = LifecycleRestUtils.changeComponentState(resourceDetailsVF, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LC state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		// resourceDetailsVF =
		// AtomicOperationUtils.getResourceObject(resourceDetailsVF,
		// UserRoleEnum.DESIGNER);
		resourceDetailsVF = AtomicOperationUtils.getResourceObject(resourceDetailsVF, UserRoleEnum.DESIGNER);

		RestResponse getFollowedResourcesResponse = ResourceRestUtils.getFollowedList(sdncUserDetails);
		String json = getFollowedResourcesResponse.getResponse();
		JSONObject jsonResp = (JSONObject) JSONValue.parse(json);
		JSONArray resources = (JSONArray) jsonResp.get("resources");

		List<Resource> resourceList = ResourceRestUtils.restResponseToResourceObjectList(resources.toString());
		Resource resource = ResourceRestUtils.getResourceObjectFromResourceListByUid(resourceList,
				resourceDetailsVF.getUniqueId());
		// TODO if get followed list Api should return full object data?
		// callAllCheckMethods(resource);
	}

	@Test
	public void lifeCycleChekInRequest() throws Exception {

		RestResponse response = LifecycleRestUtils.changeComponentState(resourceDetailsVF, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LC state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		// resourceDetailsVF =
		// AtomicOperationUtils.getResourceObject(resourceDetailsVF,
		// UserRoleEnum.DESIGNER);
		resourceDetailsVF = AtomicOperationUtils.getResourceObject(resourceDetailsVF, UserRoleEnum.DESIGNER);

		Resource resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		callAllCheckMethods(resource);
	}

	@Test
	public void lifeCycleChekOutRequest() throws Exception {

		RestResponse response = LifecycleRestUtils.changeComponentState(resourceDetailsVF, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LC state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		response = LifecycleRestUtils.changeComponentState(resourceDetailsVF, sdncUserDetails,
				LifeCycleStatesEnum.CHECKOUT);
		assertTrue("change LC state to CHECKOUT, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		resourceDetailsVF = AtomicOperationUtils.getResourceObject(resourceDetailsVF, UserRoleEnum.DESIGNER);

		Resource resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		callAllCheckMethods(resource);
	}

	@Test
	public void lifeCycleRequestForCertification() throws Exception {

		RestResponse response = LifecycleRestUtils.changeComponentState(resourceDetailsVF, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LC state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		response = LifecycleRestUtils.changeComponentState(resourceDetailsVF, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertTrue("change LC state to CERTIFICATIONREQUEST, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		resourceDetailsVF = AtomicOperationUtils.getResourceObject(resourceDetailsVF, UserRoleEnum.DESIGNER);

		Resource resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		callAllCheckMethods(resource);
	}

	@Test
	public void lifeCycleCertificationRequest() throws Exception {

		RestResponse response = AtomicOperationUtils
				.changeComponentState(resourceDetailsVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, false)
				.getRight();
		assertTrue("change LC state to CERTIFY, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		resourceDetailsVF = AtomicOperationUtils.getResourceObject(resourceDetailsVF, UserRoleEnum.DESIGNER);

		Resource resource = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		callAllCheckMethods(resource);
	}

	@Test
	public void checkGetResourceAfterCertificationRequest() throws Exception {

		RestResponse response = AtomicOperationUtils
				.changeComponentState(resourceDetailsVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, false)
				.getRight();
		assertTrue("change LC state to CERTIFY, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		resourceDetailsVF = AtomicOperationUtils.getResourceObject(resourceDetailsVF, UserRoleEnum.DESIGNER);

		callAllCheckMethods(resourceDetailsVF);
	}

	@Test
	public void updateResourceMetadata() throws Exception {

		resourceDetailsVF.setDescription("stamStam");
		ResourceReqDetails resourceDetailsVFreqD = new ResourceReqDetails(resourceDetailsVF);
		RestResponse updateResourceResponse = ResourceRestUtils.updateResourceMetadata(resourceDetailsVFreqD,
				sdncUserDetails, resourceDetailsVF.getUniqueId());
		assertTrue("response code is not 200, returned :" + updateResourceResponse.getErrorCode(),
				updateResourceResponse.getErrorCode() == 200);

		Resource resource = ResponseParser.convertResourceResponseToJavaObject(updateResourceResponse.getResponse());

		callAllCheckMethods(resource);
	}

	private void checkResourceInstances(Resource resource) {
		assertNotNull("resource component Instances list is null ", resource.getComponentInstances());
		assertTrue("resource component Instances list is empty ", !resource.getComponentInstances().equals(""));
	}

	private void checkResourceInstancesProperties(Resource resource) {
		assertNotNull("component Instances properies list is null ", resource.getComponentInstancesProperties());
		assertTrue("component Instances properies list is empty ",
				!resource.getComponentInstancesProperties().equals(""));
	}

	private void checkResourceInstancesRelations(Resource resource) {
		assertNotNull("component Instances Relations list is null ", resource.getComponentInstancesRelations());
		assertTrue("component Instances Relations list is empty ",
				!resource.getComponentInstancesRelations().equals(""));
	}

	private void checkResourceCapabilities(Resource resource) {
		assertNotNull("component Instances Capabilities list is null ", resource.getCapabilities());
		assertTrue("component Instances Capabilities list is empty ", !resource.getCapabilities().equals(""));
	}

	private void checkResourceRequirements(Resource resource) {
		assertNotNull("component Instances Requirements list is null ", resource.getRequirements());
		assertTrue("component Instances Requirements list is empty ", !resource.getRequirements().equals(""));
	}

	private void callAllCheckMethods(Resource resource) {

		checkResourceInstances(resource);
		checkResourceInstancesProperties(resource);
		checkResourceInstancesRelations(resource);
		checkResourceCapabilities(resource);
		checkResourceRequirements(resource);
	}

}
