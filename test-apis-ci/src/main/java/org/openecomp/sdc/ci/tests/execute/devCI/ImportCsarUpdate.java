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

package org.openecomp.sdc.ci.tests.execute.devCI;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.imports.ImportCsarResourceTest;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ImportCsarUpdate extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public ImportCsarUpdate() {
		super(name, ImportCsarUpdate.class.getName());
	}

	@DataProvider(name = "happyArts")
	public Object[][] getHappyArtifacts() {

		return new Object[][] { { "happy_VF_RI2_G2_two_different_artifacts_under_heatBaseheatVolheatNet2" },
				{ "happy_VF_RI2_G2_two_different_artifacts_under_heatBaseheatVolheatNet" },
				{ "happy_VF_RI2_G2_two_identical_artifacts_under_heatBaseheatVolheatNet" },
				{ "happy_VF_RI2_G2_two_different_artifacts_under_nested" },
				{ "happy_VF_RI2_G2_two_indentical_nested_under_different_groups" },
				{ "happy_VF_RI2_G2_two_different_nested_under_different_groups" },
				{ "happy_VF_RI2_G2_two_different_nested_under_same_group" },

		};
	}

	@DataProvider(name = "negativeArts")
	public Object[][] getNegativeArtifacts() {

		return new Object[][] {

				{ "negative_VF_RI2_G2_same_heatVol_different_groups" },
				{ "negative_VF_RI2_G2_same_heatBase_different_envs" },
				{ "negative_VF_RI2_G2_heatBaseHeatVolHeatNet_under_nested" },
				{ "negative_VF_RI2_G2_two_indentical_artifacts_under_nested" },
				{ "negative_VF_RI2_G2_nested_under_nested" }, { "negative_VF_RI2_G2_same_heatVol_different_groups" }, };
	}

	@BeforeTest
	public void resumeOrigCsarBefore() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ImportCsarResourceTest.copyCsarRest(sdncModifierDetails, "orig.csar", "importCsar_2Gartifacts.csar");

	}

	@AfterTest
	public void resumeOrigCsarAfter() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ImportCsarResourceTest.copyCsarRest(sdncModifierDetails, "orig.csar", "importCsar_2Gartifacts.csar");

	}

	@Test
	public void updateVFsearchByCsarIdCheckInState() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("hardcodedName");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceFirstImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);
		Component resourceObject = AtomicOperationUtils
				.changeComponentState(resourceFirstImport, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true)
				.getLeft();

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"updateImportCsar_2Gartifacts_topologyChanged.csar", "importCsar_2Gartifacts.csar");

		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceSecondImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);

		// Validation Part

		resourceFirstImport.getGroups().equals(resourceSecondImport.getGroups());

	}

	@Test
	public void updateVFsearchByCsarIdCheckInState_checkSum() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("hardcodedName");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceFirstImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);
		Component resourceObject = AtomicOperationUtils
				.changeComponentState(resourceFirstImport, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true)
				.getLeft();

		// User sdncModifierDetails =
		// ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		// RestResponse copyRes =
		// ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,"updateImportCsar_2Gartifacts_topologyChanged.csar","importCsar_2Gartifacts.csar");

		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceSecondImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);

		// Validation Part

		resourceFirstImport.getGroups().equals(resourceSecondImport.getGroups());

	}

	@Test
	public void updateVFsearchByCsarIdCheckOutState() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("hardcodedName");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceFirstImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);
		// Component resourceObject =
		// AtomicOperationUtils.changeComponentState(resourceFirstImport,
		// UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"updateImportCsar_2Gartifacts_topologyChanged.csar", "importCsar_2Gartifacts.csar");

		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceSecondImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);

		// Validation Part

		resourceFirstImport.getGroups().equals(resourceSecondImport.getGroups());

	}

	@Test
	public void updateVFsearchByCsarIdCertifyStat() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("hardcodedName");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceFirstImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);
		Component resourceObject = AtomicOperationUtils
				.changeComponentState(resourceFirstImport, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true)
				.getLeft();

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"updateImportCsar_2Gartifacts_topologyChanged.csar", "importCsar_2Gartifacts.csar");

		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceSecondImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);

		// Validation Part

		resourceFirstImport.getGroups().equals(resourceSecondImport.getGroups());

	}

	@Test
	public void updateVFsearchByCsarStartCertifaicationState() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("hardcodedName");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceFirstImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);
		Component resourceObject = AtomicOperationUtils.changeComponentState(resourceFirstImport, UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"updateImportCsar_2Gartifacts_topologyChanged.csar", "importCsar_2Gartifacts.csar");

		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceSecondImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);

		// Validation Part

		resourceFirstImport.getGroups().equals(resourceSecondImport.getGroups());

	}

	@Test
	public void updateVFsearchBySystemNameCheckInState() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("hardcodedName");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceFirstImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);
		Component resourceObject = AtomicOperationUtils
				.changeComponentState(resourceFirstImport, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true)
				.getLeft();

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"updateImportCsar_2Gartifacts_topologyChanged.csar", "importCsar_2Gartifacts.csar");

		resourceDetails.setName("hardcodedNameChanged");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceSecondImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);

		// Validation Part

		resourceFirstImport.getGroups().equals(resourceSecondImport.getGroups());

	}

	@Test
	public void updateVFsearchBySystemNameCertifyState() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("hardcodedName");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceFirstImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);
		Component resourceObject = AtomicOperationUtils
				.changeComponentState(resourceFirstImport, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true)
				.getLeft();

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"updateImportCsar_2Gartifacts_topologyChanged.csar", "importCsar_2Gartifacts.csar");

		resourceDetails.setName("hardcodedNameChanged");
		resourceDetails.setCsarUUID("importCsar_2Gartifacts");
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));

	}

	@Test
	public void updateVFsearchBySystemNameCsarIdNotExist() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("hardcodedName");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceFirstImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);
		Component resourceObject = AtomicOperationUtils
				.changeComponentState(resourceFirstImport, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true)
				.getLeft();
		// User sdncModifierDetails =
		// ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		// RestResponse copyRes =
		// ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,"updateImportCsar_2Gartifacts_topologyChanged.csar","importCsar_2Gartifacts.csar");
		ResourceReqDetails resourceDetails2 = ElementFactory.getDefaultResource();
		resourceDetails2.setName("hardcodedName");
		resourceDetails2.setCsarUUID("importCsar_2Gartifacts");
		resourceDetails2.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails2,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resourceSecondImport = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(),
				Resource.class);

		// Validation Part

		resourceFirstImport.getGroups().equals(resourceSecondImport.getGroups());

	}

}
