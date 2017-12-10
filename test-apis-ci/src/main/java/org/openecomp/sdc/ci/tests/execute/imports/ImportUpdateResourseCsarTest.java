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

package org.openecomp.sdc.ci.tests.execute.imports;

import static org.testng.AssertJUnit.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.common.api.Constants;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class ImportUpdateResourseCsarTest extends ComponentBaseTest {
	@Rule
	public static TestName name = new TestName();

	Gson gson = new Gson();
	public static String userDefinedNodeYaml = "mycompute2.yml";
	public static String rootPath = System.getProperty("user.dir");
	public static String csarFolderPath = "/src/test/resources/CI/csars/";

	public ImportUpdateResourseCsarTest() {
		super(name, ImportUpdateResourseCsarTest.class.getName());
	}

	@Test
	public void createUpdateImportResourceFromCsarTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		RestResponse updateResource = null;
		RestResponse createResource = null;
		Resource resource = null;
		String payloadName = "orig2G.csar";
		String rootPath = System.getProperty("user.dir");
		Path path = Paths.get(rootPath + csarFolderPath + "orig2G.csar");
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setName("TEST01");
		resourceDetails.setCsarUUID("orig2G.csar");
		resourceDetails.setCsarVersion("1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		// create new resource from Csar
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		// update scar with new artifacts
		path = Paths.get(rootPath + csarFolderPath + "orig2G_update.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setDescription("update");
		resourceDetails.setCsarVersion("2");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
	}

	@Test
	public void createUpdateImportResourceFromCsarWithArtifactsGroupNamingTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes;
		ResourceReqDetails resourceDetails;
		RestResponse updateResource;
		RestResponse createResource;
		Resource resource;

		// back original scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"VF_RI2_G4_withArtifacts_group_naming_a.csar", "VF_RI2_G4_withArtifacts_group_naming.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("TEST01");
		resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts_group_naming.csar");
		resourceDetails.setCsarVersion("1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		// create new resource from Csar
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		List<GroupDefinition> groups = resource.getGroups();
		assertTrue(groups != null && groups.size() == 6);
		assertTrue(groups.stream()
				.filter(g -> g.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)
						&& !Pattern.compile(Constants.MODULE_NEW_NAME_PATTERN).matcher(g.getName()).matches())
				.count() == 0);
		// update scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"VF_RI2_G4_withArtifacts_group_naming_delete_update.csar", "VF_RI2_G4_withArtifacts_group_naming.csar");
		BaseRestUtils.checkSuccess(copyRes);
		resourceDetails.setDescription("BLA BLA BLA");
		resourceDetails.setCsarVersion("2");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		groups = resource.getGroups();
		assertTrue(groups != null && groups.size() == 5);
		// back original scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"VF_RI2_G4_withArtifacts_group_naming_a.csar", "VF_RI2_G4_withArtifacts_group_naming.csar");
		BaseRestUtils.checkSuccess(copyRes);
		resourceDetails.setDescription("BLA BLA BLA");
		resourceDetails.setCsarVersion("3");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		groups = resource.getGroups();
		assertTrue(groups != null && groups.size() == 6);
		assertTrue(groups.stream()
				.filter(g -> g.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)
						&& !Pattern.compile(Constants.MODULE_NEW_NAME_PATTERN).matcher(g.getName()).matches())
				.count() == 0);
	}

	@Test
	public void createUpdateDeleteAllRequiredArtifactsTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes;
		ResourceReqDetails resourceDetails;
		RestResponse updateResource;
		RestResponse createResource;
		Resource resource;
		String artifactName = "heatnested7";

		ImportReqDetails resourceDetails0 = ElementFactory.getDefaultImportResource();
		createResource = importUserDefinedNodeType(userDefinedNodeYaml, sdncModifierDetails, resourceDetails0);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		// back original scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails, "orig2GV001_a.csar", "orig2GV001.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("TEST01");
		resourceDetails.setCsarUUID("orig2GV001.csar");
		resourceDetails.setCsarVersion("1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		// create new resource from Csar
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertTrue(resource.getDeploymentArtifacts().get(artifactName).getRequiredArtifacts().size() == 2);
		List<GroupDefinition> groups = resource.getGroups();
		// update scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"orig2GV006-remove-all-nested-artifacts.csar", "orig2GV001.csar");
		BaseRestUtils.checkSuccess(copyRes);
		resourceDetails.setDescription("BLA BLA BLA");
		resourceDetails.setCsarVersion("2");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertTrue(resource.getDeploymentArtifacts().get(artifactName).getRequiredArtifacts().size() == 0);
		groups = resource.getGroups();
		// back original scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails, "orig2GV001_a.csar", "orig2GV001.csar");
		BaseRestUtils.checkSuccess(copyRes);
	}

	// First create from orig2GV006-remove-all-nested-artifacts.csar (without
	// requiredArtifact)
	// Submit for testing
	// Login as tester -> Certification
	// Login as designer
	// then update to orig2GV008-change-nested-oam-fileContent.csar (with
	// requiredArtifact)
	// Expected: requiredArtifact: ["hot-nimbus-psm_v1.0.yaml",
	// "hot-nimbus-swift-container_v1.0.yaml"]
	// Actual: no requiredArtifact
	@Test
	public void createUpdateAddRequiredArtifactsTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes;
		ResourceReqDetails resourceDetails;
		RestResponse updateResource;
		RestResponse createResource;
		Resource resource;
		String artifactName = "heatnested7";

		ImportReqDetails resourceDetails0 = ElementFactory.getDefaultImportResource();
		createResource = importUserDefinedNodeType(userDefinedNodeYaml, sdncModifierDetails, resourceDetails0);
		BaseRestUtils.checkCreateResponse(createResource);
		createResource = LifecycleRestUtils.certifyResource(resourceDetails0);
		BaseRestUtils.checkSuccess(createResource);

		// back original scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"orig2GV006-remove-all-nested-artifacts.csar", "orig2GV001.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("TEST01");
		resourceDetails.setCsarUUID("orig2GV001.csar");
		resourceDetails.setCsarVersion("1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		// create new resource from Csar
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		createResource = LifecycleRestUtils.certifyResource(resourceDetails);
		BaseRestUtils.checkSuccess(createResource);

		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertTrue(resource.getDeploymentArtifacts().get(artifactName).getRequiredArtifacts().size() == 0);
		List<GroupDefinition> groups = resource.getGroups();
		// update scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails,
				"orig2GV008-change-nested-oam-fileContent.csar", "orig2GV001.csar");
		BaseRestUtils.checkSuccess(copyRes);
		resourceDetails.setDescription("BLA BLA BLA");
		resourceDetails.setCsarVersion("2");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails,
				resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertTrue(resource.getDeploymentArtifacts().get(artifactName).getRequiredArtifacts().size() == 2);
		groups = resource.getGroups();
		// back original scar
		copyRes = ImportCsarResourceTest.copyCsarRest(sdncModifierDetails, "orig2GV001_a.csar", "orig2GV001.csar");
		BaseRestUtils.checkSuccess(copyRes);
	}

	private RestResponse importUserDefinedNodeType(String payloadName, User sdncModifierDetails,
			ImportReqDetails resourceDetails) throws Exception {

		Path path = Paths.get(rootPath + csarFolderPath + payloadName);
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VFC.name());
		return ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
	}

}
