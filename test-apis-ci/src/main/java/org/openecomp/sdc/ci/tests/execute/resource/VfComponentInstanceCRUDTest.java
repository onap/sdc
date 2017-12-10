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

import static org.junit.Assert.assertTrue;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_ALREADY_EXISTS;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_COMPONENT_NAME_EXCEEDS_LIMIT;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_DELETE;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_INVALID_CONTENT;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_NOT_FOUND;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentInstanceBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.BaseValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VfComponentInstanceCRUDTest extends ComponentInstanceBaseTest {

	@Rule
	public static TestName name = new TestName();

	public VfComponentInstanceCRUDTest() {
		super(name, VfComponentInstanceCRUDTest.class.getName());
	}

	@BeforeMethod
	public void before() throws Exception {
		init();
		createComponents();
	}

	// CREATE Resource
	private void createComponents() throws Exception {
		createAtomicResource(resourceDetailsVFC_01);
		LifecycleRestUtils.changeResourceState(resourceDetailsVFC_01, sdncAdminDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		createAtomicResource(resourceDetailsVFC_02);
		LifecycleRestUtils.changeResourceState(resourceDetailsVFC_02, sdncAdminDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		createAtomicResource(resourceDetailsCP_01);
		LifecycleRestUtils.changeResourceState(resourceDetailsCP_01, sdncAdminDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		createAtomicResource(resourceDetailsCP_02);
		LifecycleRestUtils.changeResourceState(resourceDetailsCP_02, sdncAdminDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		createAtomicResource(resourceDetailsVL_01);
		LifecycleRestUtils.changeResourceState(resourceDetailsVL_01, sdncAdminDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		createAtomicResource(resourceDetailsVL_02);
		LifecycleRestUtils.changeResourceState(resourceDetailsVL_02, sdncAdminDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		createVF(resourceDetailsVF_02);
	}

	@Test
	public void createVfcInstanceByDesigner() throws Exception {
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVFC_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test(enabled = false) // DE189419
	public void createInstanceOfVfToItself() throws Exception {
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVF_02);
		createAtomicResourceInstance = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails,
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertTrue(createAtomicResourceInstance.getErrorCode() == STATUS_CODE_NOT_FOUND);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void createVfcInstanceByAdmin() throws Exception {
		User user = sdncAdminDetails;
		createVF(resourceDetailsVF_01, user);
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_01,
				resourceDetailsVFC_01, user);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_01, 1, 0);
	}

	@Test
	public void createCpInstance() throws Exception {
		// Create CP instance
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsCP_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void createVlInstance() throws Exception {
		// Create VL instance
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void updateResourceInstanceNameLessMaxLegth() throws Exception {
		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		vfcResourceInstanceReqDetails.setName("xxxxXthisXstringxisx49XcharcatersXlengthXxxxxxxxx");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);

	}

	@Test
	public void updateInstanceNameExceedMaxLegth() throws Exception {
		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		vfcResourceInstanceReqDetails.setName("xxxxXthisXstringxisx51XcharcatersXlengthXxxxxxxxxxx");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_COMPONENT_NAME_EXCEEDS_LIMIT,
				updateResourceInstanceResponse.getErrorCode().intValue());
	}

	@Test
	public void updateResourceInstanceNameHasMaxLegth() throws Exception {
		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		vfcResourceInstanceReqDetails.setName("xxxxXthisXstringxisx50XcharcatersXlengthXxxxxxxxxx");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
	}

	@Test
	public void resourceInstanceNameIsEmpty() throws Exception {
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		resourceInstanceReqDetails.setName("");
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		String instanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"name");
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "1").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance Name ", (resourceDetailsVFC_01.getName() + " 1"),
				instanceName);
	}

	@Test
	public void resourceInstanceNameIsNull() throws Exception {
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		resourceInstanceReqDetails.setName(null);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		String instanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"name");
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "1").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance Name ", (resourceDetailsVFC_01.getName() + " 1"),
				instanceName);
	}

	@Test
	public void resourceInstanceNameValidation01() throws Exception {
		// 2 Instances
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String instanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"name");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "1").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance Name ", (resourceDetailsVFC_01.getName() + " 1"),
				instanceName);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails,
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		instanceNormalizedName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"normalizedName");
		instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "name");
		componentInstance = ResponseParser.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(),
				ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "2").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ", (resourceDetailsVFC_01.getName() + " 2"),
				instanceName);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void resourceInstanceNameValidation02() throws Exception {

		// 2 Instances
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String instanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"name");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "1").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance Name ", (resourceDetailsVFC_01.getName() + " 1"),
				instanceName);
		resourceInstanceReqDetails = ElementFactory.getComponentResourceInstance(resourceDetailsCP_01);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails,
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		instanceNormalizedName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"normalizedName");
		instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "name");
		componentInstance = ResponseParser.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(),
				ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsCP_01.getName() + "2").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ", (resourceDetailsCP_01.getName() + " 2"),
				instanceName);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void createVfcInstanceByTester() throws Exception { // Response 409

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncTesterDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	@Test
	public void createVfcInstance_UserIdIsEmpty() throws Exception {

		User sdncUserDetails = new User();
		sdncUserDetails.setUserId("");
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncUserDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	@Test
	public void createVfcInstance_UserIdIsNonAsdcUser() throws Exception {

		User sdncUserDetails = new User();
		sdncUserDetails.setUserId("bt4567");
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncUserDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	@Test
	public void createAllAtomicInstances() throws Exception {

		// Add to VF resource VFC, CP and VL instances
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsCP_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVFC_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 3, 0);
	}

	@Test
	public void createDefferentVfcInstances() throws Exception {

		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVFC_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVFC_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void createDefferentCpInstances() throws Exception {

		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsCP_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsCP_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void createDefferentVLInstances() throws Exception {

		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVL_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void createSeveralInstanceOfSameVFC() throws Exception {

		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVFC_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVFC_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void createSeveralInstanceOfSameVL() throws Exception {

		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVL_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void createSeveralInstanceOfSameCP() throws Exception {

		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsCP_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsCP_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void createInstanceOfCpToVfc() throws Exception { // Add to CP to VFC
																// (not allowed)

		ComponentInstanceReqDetails resourceInstanceReqDetailsCP = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		ComponentInstanceReqDetails resourceInstanceReqDetailsVFC = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetailsCP, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetailsCP, sdncDesignerDetails, resourceInstanceReqDetailsVFC.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void createInstanceVfcToCp() throws Exception { // (not allowed)

		ComponentInstanceReqDetails resourceInstanceReqDetailsCP = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		ComponentInstanceReqDetails resourceInstanceReqDetailsVFC = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetailsCP, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetailsVFC, sdncDesignerDetails, resourceInstanceReqDetailsCP.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void createInstanceVlToVfc() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetailsVL = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		ComponentInstanceReqDetails resourceInstanceReqDetailsVFC = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetailsVFC, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetailsVL, sdncDesignerDetails, resourceInstanceReqDetailsVFC.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void createInstanceToNonSupportedComponentType() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetailsCP = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetailsCP, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE_INSTANCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_INVALID_CONTENT,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	// ("Create instance without position is allowed")
	@Test
	public void createInstanceOfVlWithoutPosXAndPosY() throws Exception { // instance
																			// does
																			// not
																			// have
																			// position

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		resourceInstanceReqDetails.setPosX("");
		resourceInstanceReqDetails.setPosY("");
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	// Create instance without position is allowed")
	@Test
	public void createInstanceOfVlWithPositionNull() throws Exception { // instance
																		// does
																		// not
																		// have
																		// position

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		resourceInstanceReqDetails.setPosX(null);
		resourceInstanceReqDetails.setPosY(null);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void createResourceInstanceForNonCheckedOutVF() throws Exception {

		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF_02, sdncAdminDetails,
				"0.1", LifeCycleStatesEnum.CHECKIN);
		resourceDetailsVF_02.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		AssertJUnit.assertEquals("Check response code after create user", STATUS_CODE_SUCCESS,
				checkInResponse.getErrorCode().intValue());
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				createResourceInstanceResponse.getErrorCode().intValue());
	}

	@Test
	public void createResourceInstanceVfCheckedOutByOtherUser() throws Exception {

		// Admin try to add RI to VF which is checked-Out By Designer
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncAdminDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	@Test
	public void createResourceInstanceForNonExistingVF() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, "blablabla", ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				createResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	// Delete
	@Test
	public void deleteVfcInstanceByDesigner() throws Exception {

		// Create RI
		RestResponse createResourceInstanceResponse = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVFC_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String compInstId = ResponseParser.getUniqueIdFromResponse(createResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		// Delete Resource instance
		RestResponse deleteResourceInstanceResponse = deleteAtomicInstanceForVF(compInstId, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkDeleteResponse(deleteResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	@Test
	public void deleteVfcInstanceByAdmin() throws Exception {
		createVF(resourceDetailsVF_01, sdncAdminDetails);
		RestResponse createResourceInstanceResponse = createAtomicInstanceForVF(resourceDetailsVF_01,
				resourceDetailsVL_01, sdncAdminDetails);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String compInstId = ResponseParser.getUniqueIdFromResponse(createResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_01, 1, 0);
		// Delete Resource instance
		RestResponse deleteResourceInstanceResponse = deleteAtomicInstanceForVF(compInstId, resourceDetailsVF_01,
				sdncAdminDetails);
		ResourceRestUtils.checkDeleteResponse(deleteResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_01, 0, 0);
	}

	@Test
	public void deleteCpInstance() throws Exception {

		RestResponse createResourceInstanceResponse = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsCP_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String compInstId = ResponseParser.getUniqueIdFromResponse(createResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		// Delete Resource instance
		RestResponse deleteResourceInstanceResponse = deleteAtomicInstanceForVF(compInstId, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkDeleteResponse(deleteResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	@Test
	public void deleteVlInstance() throws Exception {

		RestResponse createResourceInstanceResponse = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String compInstId = ResponseParser.getUniqueIdFromResponse(createResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		// Delete Resource instance
		RestResponse deleteResourceInstanceResponse = deleteAtomicInstanceForVF(compInstId, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkDeleteResponse(deleteResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
	}

	@Test
	public void deleteOneVlInstance() throws Exception {

		// RI-1
		RestResponse createResourceInstanceResponse = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String compInstId = ResponseParser.getUniqueIdFromResponse(createResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		// RI-2
		createResourceInstanceResponse = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVL_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
		// Delete Resource instance RI-1
		RestResponse deleteResourceInstanceResponse = deleteAtomicInstanceForVF(compInstId, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkDeleteResponse(deleteResourceInstanceResponse);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void deleteVfcInstanceCheckedByOtherUser() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		// Delete Resource instance
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncTesterDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void deleteInstanceNonSupportedComponentType() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE_INSTANCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_INVALID_CONTENT,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void deleteInstanceFromNonVF() throws Exception {
		// RI-1

		ComponentInstanceReqDetails resourceInstanceVlReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceVlReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance1 = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance1, ComponentTypeEnum.RESOURCE);
		// RI-2
		ComponentInstanceReqDetails resourceInstanceCplReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceCplReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance2 = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance2, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
		// Delete VL instance from CP instance
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncDesignerDetails, resourceInstanceCplReqDetails.getUniqueId(),
				resourceInstanceVlReqDetails.getUniqueId(), ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
	}

	@Test
	public void deleteNonExistingInstanceFromVF() throws Exception {

		ComponentInstanceReqDetails resourceInstanceVlReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceVlReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance1 = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance1, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		resourceInstanceVlReqDetails.setUniqueId("1234567890");
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceVlReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void deleteCpInstanceFromNonCheckOutVF() throws Exception {

		ComponentInstanceReqDetails resourceInstanceCpReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceCpReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance1 = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance1, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF_02, sdncDesignerDetails,
				"0.1", LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_SUCCESS,
				checkInResponse.getErrorCode().intValue());
		resourceDetailsVF_02.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		// Delete Resource instance
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceCpReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void deleteVlInstanceFromNonCheckOutVF() throws Exception {

		ComponentInstanceReqDetails resourceInstanceVlReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceVlReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance1 = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance1, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF_02, sdncDesignerDetails,
				"0.1", LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_SUCCESS,
				checkInResponse.getErrorCode().intValue());
		resourceDetailsVF_02.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		// Delete Resource instance
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceVlReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void deleteVfcInstanceFromNonCheckOutVF() throws Exception {

		ComponentInstanceReqDetails resourceInstanceVfcReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceVfcReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance1 = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance1, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetailsVF_02, sdncDesignerDetails,
				"0.1", LifeCycleStatesEnum.CHECKIN);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_SUCCESS,
				checkInResponse.getErrorCode().intValue());
		resourceDetailsVF_02.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		// Delete Resource instance
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceVfcReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void deleteVlInstance_UserIdIsNonAsdcUser() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		// Delete Resource instance by non-ASDC User
		User sdncUserDetails = new User();
		sdncUserDetails.setUserId("bt4567");
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncUserDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void deleteAlreadyDeletedInstance() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_DELETE,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		deleteCompInstReqCapFromExpected(componentInstance.getUniqueId());
		getComponentAndValidateRIs(resourceDetailsVF_02, 0, 0);
		deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(sdncDesignerDetails,
				resourceDetailsVF_02.getUniqueId(), resourceInstanceReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				deleteResourceInstanceResponse.getErrorCode().intValue());
	}

	@Test
	public void reCreateDeletedInstance() throws Exception {

		// 2 Instances
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String instanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"name");
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "1").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance Name ", (resourceDetailsVFC_01.getName() + " 1"),
				instanceName);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails,
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		instanceNormalizedName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"normalizedName");
		instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "name");
		componentInstance = ResponseParser.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(),
				ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "2").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ", (resourceDetailsVFC_01.getName() + " 2"),
				instanceName);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
		// Delete one instance
		RestResponse deleteResourceInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), resourceInstanceReqDetails.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_DELETE,
				deleteResourceInstanceResponse.getErrorCode().intValue());
		deleteCompInstReqCapFromExpected(componentInstance.getUniqueId());
		// Create same instance again
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails,
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		instanceNormalizedName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(),
				"normalizedName");
		instanceName = ResponseParser.getValueFromJsonResponse(createResourceInstanceResponse.getResponse(), "name");
		componentInstance = ResponseParser.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(),
				ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "3").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance Name ", (resourceDetailsVFC_01.getName() + " 3"),
				instanceName);

	}

	// Update
	@Test
	public void updateVfcInstanceNameByDesigner() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		vfcResourceInstanceReqDetails.setName("abcd");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String resourceNameFromJsonResponse = ResponseParser.getNameFromResponse(updateResourceInstanceResponse);
		AssertJUnit.assertEquals(resourceNameFromJsonResponse, vfcResourceInstanceReqDetails.getName());
		String riNormalizedName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"normalizedName");
		String riName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "name");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riNormalizedName, "abcd");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riName, "abcd");
	}

	@Test
	public void updateVfcInstanceNameByAdmin() throws Exception {
		User user = sdncAdminDetails;
		createVF(resourceDetailsVF_01, user);
		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncAdminDetails, resourceDetailsVF_01.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_01, 1, 0);
		vfcResourceInstanceReqDetails.setName("ABCD E");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncAdminDetails, resourceDetailsVF_01.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String resourceNameFromJsonResponse = ResponseParser.getNameFromResponse(updateResourceInstanceResponse);
		AssertJUnit.assertEquals(resourceNameFromJsonResponse, vfcResourceInstanceReqDetails.getName());
		String riNormalizedName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"normalizedName");
		String riName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "name");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riNormalizedName, "abcde");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riName, "ABCD E");
	}

	@Test
	public void updateInstanceNameAllowedCharacters() throws Exception {
		// Allowed characters: Alphanumeric (a-zA-Z0-9), space (' '), underscore
		// ('_'), dash ('-'), dot ('.'))

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		vfcResourceInstanceReqDetails.setName("Abcd_1234567890-qwert-yuiop.zxcvb");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String resourceNameFromJsonResponse = ResponseParser.getNameFromResponse(updateResourceInstanceResponse);
		AssertJUnit.assertEquals(resourceNameFromJsonResponse, vfcResourceInstanceReqDetails.getName());
		String riNormalizedName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"normalizedName");
		String riName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "name");
		// assertEquals("Check if RI normalizedName is correct ",
		// riNormalizedName, "abcd_1234567890-qwert-yuiop.zxcv" );
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riName, "Abcd_1234567890-qwert-yuiop.zxcvb");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riNormalizedName,
				"abcd1234567890qwertyuiopzxcvb");

	}

	@Test
	public void updateVfcInstanceNameEmpty() throws Exception {
		// see US534663 In case a designer removes the current resource instance
		// name then BE has to generate again the "default" resource instance
		// name

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String newName = "";
		vfcResourceInstanceReqDetails.setName(newName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String instanceNormalizedName = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"name");
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "2").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ", (resourceDetailsVFC_01.getName() + " 2"),
				instanceName);
	}

	@Test
	public void updateVfcInstanceNameNull() throws Exception {
		// see US534663 In case a designer removes the current resource instance
		// name then BE has to generate again the "default" resource instance
		// name

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String newName = null;
		vfcResourceInstanceReqDetails.setName(newName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		final String updateResponse = updateResourceInstanceResponse.getResponse();
		String instanceNormalizedName = ResponseParser.getValueFromJsonResponse(updateResponse, "normalizedName");
		String instanceName = ResponseParser.getValueFromJsonResponse(updateResponse, "name");
		AssertJUnit.assertEquals("check Resource Instance normalizedName ",
				(resourceDetailsVFC_01.getName() + "2").toLowerCase(), instanceNormalizedName);
		AssertJUnit.assertEquals("check Resource Instance normalizedName ", (resourceDetailsVFC_01.getName() + " 2"),
				instanceName);
	}

	@Test
	public void updateCpInstanceName() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		vfcResourceInstanceReqDetails.setName("AbcD");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String resourceNameFromJsonResponse = ResponseParser.getNameFromResponse(updateResourceInstanceResponse);
		AssertJUnit.assertEquals(resourceNameFromJsonResponse, vfcResourceInstanceReqDetails.getName());
		String riNormalizedName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"normalizedName");
		String riName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "name");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riNormalizedName, "abcd");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riName, "AbcD");
	}

	@Test
	public void updateVlInstanceName() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		vfcResourceInstanceReqDetails.setName("ABCD");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String resourceNameFromJsonResponse = ResponseParser.getNameFromResponse(updateResourceInstanceResponse);
		AssertJUnit.assertEquals(resourceNameFromJsonResponse, vfcResourceInstanceReqDetails.getName());
		String riNormalizedName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"normalizedName");
		String riName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "name");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riNormalizedName, "abcd");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riName, "ABCD");
	}

	@Test
	public void updateInstanceNameToArleadyExistInstanceName02() throws Exception {

		// Create VFC instance
		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance vfcComponentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(vfcComponentInstance, ComponentTypeEnum.RESOURCE);
		// Create CP instance
		ComponentInstanceReqDetails cpResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				cpResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance cpComponentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(cpComponentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 2, 0);
		cpResourceInstanceReqDetails.setName(vfcComponentInstance.getName());
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				cpResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_ALREADY_EXISTS,
				updateResourceInstanceResponse.getErrorCode().intValue());
	}

	@Test
	public void updateInstanceNameMaxLength() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String newName = "Qwertyuiop1234567890asdfAhjklzxcvbnmasdfghjkl12345";
		vfcResourceInstanceReqDetails.setName(newName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String resourceNameFromJsonResponse = ResponseParser.getNameFromResponse(updateResourceInstanceResponse);
		AssertJUnit.assertEquals(resourceNameFromJsonResponse, vfcResourceInstanceReqDetails.getName());
		String riNormalizedName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"normalizedName");
		String riName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "name");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riNormalizedName, newName.toLowerCase());
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riName, newName);
	}

	@Test
	public void updateInstanceNameExceedMaxLength() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String newName = "Qwertyuiop1234567890asdfAhjklzxcvbnmasdfghjkl123456";
		vfcResourceInstanceReqDetails.setName(newName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_COMPONENT_NAME_EXCEEDS_LIMIT,
				updateResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void updateCpInstanceCheckedByOtherUser() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String newName = "Qwertyuiop1234567890";
		resourceInstanceReqDetails.setName(newName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				resourceInstanceReqDetails, sdncAdminDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				updateResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void UpdateVfcInstance_UserIdIsNonAsdcUser() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String newName = "Qwertyuiop1234567890";
		resourceInstanceReqDetails.setName(newName);
		User nonSdncUserDetails = new User();
		nonSdncUserDetails.setUserId("bt4567");
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				resourceInstanceReqDetails, nonSdncUserDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_RESTRICTED_OPERATION,
				updateResourceInstanceResponse.getErrorCode().intValue());
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
	}

	@Test
	public void UpdateResourceInstanceFormNonExistingVF() throws Exception {

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		// LifecycleRestUtils.changeResourceState(resourceDetailsVL_01,
		// sdncAdminDetails, "0.1", LifeCycleStatesEnum.CHECKIN);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String newName = "Qwertyuiop1234567890";
		resourceInstanceReqDetails.setName(newName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, "blablabla", ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				updateResourceInstanceResponse.getErrorCode().intValue());
	}

	@Test
	public void updateNonExistingInstanceFromVF() throws Exception {

		ComponentInstanceReqDetails resourceInstanceVlReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceVlReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance1 = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance1, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		resourceInstanceVlReqDetails.setUniqueId("1234567890");
		// String newName= "Qwertyuiop1234567890";
		// resourceInstanceVlReqDetails.setName(newName);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				resourceInstanceVlReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				updateResourceInstanceResponse.getErrorCode().intValue());
	}

	// Update
	@Test
	public void updateVfcInstanceNameAsVfName() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		vfcResourceInstanceReqDetails.setName(resourceDetailsVF_02.getName());
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String resourceNameFromJsonResponse = ResponseParser.getNameFromResponse(updateResourceInstanceResponse);
		AssertJUnit.assertEquals(resourceNameFromJsonResponse, vfcResourceInstanceReqDetails.getName());
		String riNormalizedName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"normalizedName");
		String riName = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "name");
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riNormalizedName,
				resourceDetailsVF_02.getName().toLowerCase());
		AssertJUnit.assertEquals("Check if RI normalizedName is correct ", riName, resourceDetailsVF_02.getName());
	}

	@Test
	public void updateInstanceNameInvalidCharacters() throws Exception {
		char invalidChars[] = { '~', '!', '$', '%', '^', '*', '(', ')', '"', '{', '}', '[', ']', '?', '>', '<', '/',
				'|', '\\', ',' };

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String newName = "Abcd1";
		String updateName;
		for (int i = 0; i < invalidChars.length; i++) {
			updateName = newName + invalidChars[i];
			vfcResourceInstanceReqDetails.setName(updateName);
			RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
					vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
					ComponentTypeEnum.RESOURCE);
			AssertJUnit.assertEquals("Check response code ", STATUS_CODE_INVALID_CONTENT,
					updateResourceInstanceResponse.getErrorCode().intValue());
		}
	}

	// Update Position
	@Test
	public void updateVfcInstancePosition() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String updatePosX = "130";
		String updatePosY = "180";
		vfcResourceInstanceReqDetails.setPosX(updatePosX);
		vfcResourceInstanceReqDetails.setPosY(updatePosY);
		vfcResourceInstanceReqDetails.setName(null);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String posXFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posX");
		String posYFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posY");
		AssertJUnit.assertEquals(posXFromJsonResponse, updatePosX);
		AssertJUnit.assertEquals(posYFromJsonResponse, updatePosY);
	}

	@Test
	public void updateVlInstancePosition() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVL_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String updatePosX = "130";
		String updatePosY = "180";
		vfcResourceInstanceReqDetails.setPosX(updatePosX);
		vfcResourceInstanceReqDetails.setPosY(updatePosY);
		vfcResourceInstanceReqDetails.setName(null);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String posXFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posX");
		String posYFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posY");
		AssertJUnit.assertEquals(posXFromJsonResponse, updatePosX);
		AssertJUnit.assertEquals(posYFromJsonResponse, updatePosY);
	}

	@Test
	public void updateCpInstancePosition() throws Exception {

		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String updatePosX = "130";
		String updatePosY = "180";
		vfcResourceInstanceReqDetails.setPosX(updatePosX);
		vfcResourceInstanceReqDetails.setPosY(updatePosY);
		vfcResourceInstanceReqDetails.setName(null);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String posXFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posX");
		String posYFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posY");
		AssertJUnit.assertEquals(posXFromJsonResponse, updatePosX);
		AssertJUnit.assertEquals(posYFromJsonResponse, updatePosY);
	}

	@Test
	public void updateInstancePositionNegativePosition() throws Exception {

		ComponentInstanceReqDetails cpResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				cpResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstance componentInstance = ResponseParser
				.parseToObjectUsingMapper(createResourceInstanceResponse.getResponse(), ComponentInstance.class);
		addCompInstReqCapToExpected(componentInstance, ComponentTypeEnum.RESOURCE);
		getComponentAndValidateRIs(resourceDetailsVF_02, 1, 0);
		String updatePosX = "-100";
		String updatePosY = "-100";
		cpResourceInstanceReqDetails.setPosX(updatePosX);
		cpResourceInstanceReqDetails.setPosY(updatePosY);
		cpResourceInstanceReqDetails.setName(null);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				cpResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String posXFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posX");
		String posYFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posY");
		AssertJUnit.assertEquals(posXFromJsonResponse, updatePosX);
		AssertJUnit.assertEquals(posYFromJsonResponse, updatePosY);
	}

	@Test
	public void updateInstancesPositionSameLocationForBothInstances() throws Exception {

		ComponentInstanceReqDetails cpResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				cpResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		ComponentInstanceReqDetails vfcResourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsVFC_01);
		createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		String updatePosX = "100";
		String updatePosY = "500";
		vfcResourceInstanceReqDetails.setPosX(updatePosX);
		vfcResourceInstanceReqDetails.setPosY(updatePosY);
		vfcResourceInstanceReqDetails.setName(null);
		cpResourceInstanceReqDetails.setPosX(updatePosX);
		cpResourceInstanceReqDetails.setPosY(updatePosY);
		cpResourceInstanceReqDetails.setName(null);
		RestResponse updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				vfcResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		String posXFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posX");
		String posYFromJsonResponse = ResponseParser
				.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(), "posY");
		AssertJUnit.assertEquals(posXFromJsonResponse, updatePosX);
		AssertJUnit.assertEquals(posYFromJsonResponse, updatePosY);
		updateResourceInstanceResponse = ComponentInstanceRestUtils.updateComponentInstance(
				cpResourceInstanceReqDetails, sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(),
				ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkSuccess(updateResourceInstanceResponse);
		posXFromJsonResponse = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"posX");
		posYFromJsonResponse = ResponseParser.getValueFromJsonResponse(updateResourceInstanceResponse.getResponse(),
				"posY");
		AssertJUnit.assertEquals(posXFromJsonResponse, updatePosX);
		AssertJUnit.assertEquals(posYFromJsonResponse, updatePosY);
	}

	@Test
	public void createAllAtomicInstancesTestGetReqCapAPI_suc() throws Exception {

		// Add to VF resource VFC, CP and VL instances
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsCP_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVFC_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);

		getVfResourceReqCapUsingAPI(3, 0, sdncDesignerDetails);

	}

	// END of Update

	@Test
	public void createAllAtomicInstancesTestGetReqCapAPIfailed() throws Exception {

		// Add to VF resource VFC, CP and VL instances
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsVL_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsCP_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVFC_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		resourceDetailsVF_02.setUniqueId("dummy");
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncAdminDetails,
				resourceDetailsVF_02);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_NOT_FOUND,
				getResourceResponse.getErrorCode().intValue());

	}

	@Test
	public void associateInVF() throws Exception {

		ResourceReqDetails resourceDetailsReq = ElementFactory.getDefaultResourceByType("SoftCompRouter",
				NormativeTypesEnum.SOFTWARE_COMPONENT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS,
				sdncDesignerDetails.getUserId(), ResourceTypeEnum.VFC); // resourceType
																		// = VFC
		ResourceReqDetails resourceDetailsCap = ElementFactory.getDefaultResourceByType("MyComput",
				NormativeTypesEnum.COMPUTE, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, sdncDesignerDetails.getUserId(),
				ResourceTypeEnum.VFC); // resourceType = VFC
		createAtomicResource(resourceDetailsReq);
		LifecycleRestUtils.changeResourceState(resourceDetailsReq, sdncAdminDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		createAtomicResource(resourceDetailsCap);
		LifecycleRestUtils.changeResourceState(resourceDetailsCap, sdncAdminDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);

		RestResponse riReqR = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsReq, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(riReqR);
		RestResponse riCapR = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsCap, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(riCapR);

		ComponentInstance riReq = ResponseParser.parseToObject(riReqR.getResponse(), ComponentInstance.class);
		ComponentInstance riCap = ResponseParser.parseToObject(riCapR.getResponse(), ComponentInstance.class);

		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				resourceDetailsVF_02);

		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);

		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get("tosca.capabilities.Container");
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get("tosca.capabilities.Container");

		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		requirementDef.setFromNode(riReq.getUniqueId());
		requirementDef.setToNode(riCap.getUniqueId());

		RelationshipInfo pair = new RelationshipInfo();
		pair.setRequirementOwnerId(riReq.getUniqueId());
		pair.setCapabilityOwnerId(riCap.getUniqueId());
		pair.setRequirement("host");
		RelationshipImpl relationship = new RelationshipImpl();
		relationship.setType("tosca.capabilities.Container");
		pair.setRelationships(relationship);
		pair.setCapabilityUid(capList.get(0).getUniqueId());
		pair.setRequirementUid(reqList.get(0).getUniqueId());
		List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
		CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
		relationships.add(capReqRel);
		capReqRel.setRelation(pair);
		requirementDef.setRelationships(relationships);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_SUCCESS,
				associateInstances.getErrorCode().intValue());

		getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				resourceDetailsVF_02);
		capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);

		List<RequirementDefinition> list = capReqDef.getRequirements().get("tosca.capabilities.Container");
		AssertJUnit.assertEquals("Check requirement", null, list);

		RestResponse dissociateInstances = ComponentInstanceRestUtils.dissociateInstances(requirementDef,
				sdncDesignerDetails, resourceDetailsVF_02.getUniqueId(), ComponentTypeEnum.RESOURCE);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_SUCCESS,
				dissociateInstances.getErrorCode().intValue());

		getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				resourceDetailsVF_02);
		capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);

		list = capReqDef.getRequirements().get("tosca.capabilities.Container");
		AssertJUnit.assertEquals("Check requirement", 1, list.size());

	}

	@Test
	public void testUnsatisfiedCpReqInVF() throws Exception {

		// Certify all the needed atomic resources
		RestResponse response = LifecycleRestUtils.certifyResource(resourceDetailsVFC_02);
		ResourceRestUtils.checkSuccess(response);
		response = LifecycleRestUtils.certifyResource(resourceDetailsCP_01);
		ResourceRestUtils.checkSuccess(response);

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails,
				resourceDetailsVF_02.getUniqueId());
		ResourceRestUtils.checkSuccess(response);

		RestResponse createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02,
				resourceDetailsCP_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String compInstName = ResponseParser.getNameFromResponse(createAtomicResourceInstance);
		String cpCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		RestResponse submitForTesting = LifecycleRestUtils.changeResourceState(resourceDetailsVF_02,
				sdncDesignerDetails, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		String[] variables = new String[] { resourceDetailsVF_02.getName(), "VF", "CP (Connection Point)", compInstName,
				"requirement", "tosca.capabilities.network.Bindable", "fulfilled" };
		BaseValidationUtils.checkErrorResponse(submitForTesting,
				ActionStatus.REQ_CAP_NOT_SATISFIED_BEFORE_CERTIFICATION, variables);

		createAtomicResourceInstance = createAtomicInstanceForVF(resourceDetailsVF_02, resourceDetailsVFC_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String computeCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);
		fulfillCpRequirement(resourceDetailsVF_02, cpCompInstId, computeCompInstId, computeCompInstId,
				sdncDesignerDetails, ComponentTypeEnum.RESOURCE);

		submitForTesting = LifecycleRestUtils.changeResourceState(resourceDetailsVF_02, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		BaseValidationUtils.checkSuccess(submitForTesting);
	}

	private void getVfResourceReqCapUsingAPI(int numberOfRIs, int numberOfRelations, User user)
			throws IOException, Exception {
		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncAdminDetails,
				resourceDetailsVF_02);
		AssertJUnit.assertEquals("Check response code ", STATUS_CODE_SUCCESS,
				getResourceResponse.getErrorCode().intValue());
		// ResourceValidationUtils.validateResp(getResourceResponse,
		// resourceRespJavaObject);
		// int numberOfActualRIs = resource.getComponentInstances()!=null ?
		// resource.getComponentInstances().size() : 0;
		// int numberOfActualRelations =
		// resource.getComponentInstancesRelations()!=null ?
		// resource.getComponentInstancesRelations().size() : 0;
		// assertEquals("Check number of RIs meet the expected number",
		// numberOfRIs ,numberOfActualRIs);
		// assertEquals("Check number of RI relations meet the expected number",
		// numberOfRelations ,numberOfActualRelations);

		//// get VF actual Capabilities and Requirements and validate according
		//// to expected
		Resource vfResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(),
				Resource.class);
		verifyReqCap(vfResource);
	}
}
