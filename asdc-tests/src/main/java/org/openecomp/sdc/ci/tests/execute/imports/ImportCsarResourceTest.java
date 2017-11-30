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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.WordUtils;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.GroupRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ImportRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class ImportCsarResourceTest extends ComponentBaseTest {
	private static Logger log = LoggerFactory.getLogger(ImportCsarResourceTest.class.getName());
	@Rule
	public static TestName name = new TestName();
	private static final String CSARS_PATH = "/src/test/resources/CI/csars/";
	Gson gson = new Gson();

	public ImportCsarResourceTest() {
		super(name, ImportCsarResourceTest.class.getName());
	}

	private String buildAssertMessage(String expectedString, String actualString) {
		return String.format("expected is : %s , actual is: %s", expectedString, actualString);
	}

	/**
	 * 
	 * User Story : US640615 [BE] - Extend create VF API with Import TOSCA CSAR
	 */

	@Test(enabled = true)
	public void createResourceFromCsarHappy() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("AF7F231969C5463F9C968570070E8877");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		String expectedCsarUUID = resourceDetails.getCsarUUID();
		String expectedToscaResourceName = "org.openecomp.resource.vf." + WordUtils.capitalize(resourceDetails.getName().toLowerCase());

		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, resource.getCsarUUID()), expectedCsarUUID.equals(resource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, resource.getToscaResourceName()), expectedToscaResourceName.equals(resource.getToscaResourceName()));

		RestResponse getResourceResponse = ResourceRestUtils.getResource(resource.getUniqueId());
		Resource getResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, getResource.getCsarUUID()), expectedCsarUUID.equals(getResource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, getResource.getToscaResourceName()), expectedToscaResourceName.equals(getResource.getToscaResourceName()));
	}

	@Test(enabled = true)
	public void emptyStringInCsarUUIDFieldTest() throws Exception {
		String emptyString = "";
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(emptyString);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(null, resource.getComponentInstances());

		String expectedToscaResourceName = "org.openecomp.resource.vf." + WordUtils.capitalize(resourceDetails.getName().toLowerCase());

		assertTrue("csarUUID : " + buildAssertMessage(emptyString, resource.getCsarUUID()), resource.getCsarUUID() == emptyString);
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, resource.getToscaResourceName()), expectedToscaResourceName.equals(resource.getToscaResourceName()));

		RestResponse getResourceResponse = ResourceRestUtils.getResource(resource.getUniqueId());
		Resource getResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		assertTrue("csarUUID : " + buildAssertMessage(emptyString, getResource.getCsarUUID()), getResource.getCsarUUID() == emptyString);
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, getResource.getToscaResourceName()), expectedToscaResourceName.equals(getResource.getToscaResourceName()));
	}

	@Test(enabled = true)
	public void createResourceFromScratchTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(null, resource.getComponentInstances());

		String expectedToscaResourceName = "org.openecomp.resource.vf." + WordUtils.capitalize(resourceDetails.getName().toLowerCase());

		assertTrue("csarUUID : " + buildAssertMessage(null, resource.getCsarUUID()), resource.getCsarUUID() == null);
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, resource.getToscaResourceName()), expectedToscaResourceName.equals(resource.getToscaResourceName()));

		RestResponse getResourceResponse = ResourceRestUtils.getResource(resource.getUniqueId());
		Resource getResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		assertTrue("csarUUID : " + buildAssertMessage(null, getResource.getCsarUUID()), getResource.getCsarUUID() == null);
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, getResource.getToscaResourceName()), expectedToscaResourceName.equals(getResource.getToscaResourceName()));
	}

	@Test(enabled = true)
	public void fileNotCsarTypeTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("valid_vf_zip");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_NOT_FOUND.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void missingToscaMetadataFolderTest() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("toscaFolderNotExists");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void missingToscaMetaFileTest() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("toscaMetaFileNotExists");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void toscaMetaFileOutsideTheFolderTest() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("toscaMetaOutsideTheFolder");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void caseSensitiveTest_1() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("caseSensitiveTest_1");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void caseSensitiveTest_2() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("caseSensitiveTest_2");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void missingOneLineInToscaMetaFileTest() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("missingOneLineInToscaMeta");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void noCSARVersionTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("noCSARVersion");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void noCreatedByValueTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("noCreatedByValue");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void noEntryDefinitionsValueTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("noEntryDefinitionsValue");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void noTOSCAMetaFileVersionValueTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("noTOSCAMetaFileVersionValue");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void invalidCsarVersionInMetaFileTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("invalidCsarVersion");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());

		resourceDetails.setCsarUUID("invalidCsarVersion2");
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());

		resourceDetails.setCsarUUID("invalidCsarVersion3");
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());

		resourceDetails.setCsarUUID("invalidCsarVersion4");
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());

		resourceDetails.setCsarUUID("invalidCsarVersion5");
		createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());

	}

	@Test(enabled = true)
	public void validCsarVersionInMetaFileTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("validCsarVersion");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		String expectedCsarUUID = resourceDetails.getCsarUUID();
		String expectedToscaResourceName = "org.openecomp.resource.vf." + WordUtils.capitalize(resourceDetails.getName().toLowerCase());

		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, resource.getCsarUUID()), expectedCsarUUID.equals(resource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, resource.getToscaResourceName()), expectedToscaResourceName.equals(resource.getToscaResourceName()));

		RestResponse getResourceResponse = ResourceRestUtils.getResource(resource.getUniqueId());
		Resource getResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, getResource.getCsarUUID()), expectedCsarUUID.equals(getResource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, getResource.getToscaResourceName()), expectedToscaResourceName.equals(getResource.getToscaResourceName()));
	}

	@Test(enabled = true)
	public void underscoreInToscaMetaFileVersionNameTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("underscoreInsteadOfDash");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void missingEntryDefintionInMetaFileTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("missingEntryDefintionPair");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = false)
	public void noNewLineAfterBLock0Test() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("noNewLineAfterBLock0");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void moreThanOneYamlFileTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("moreThenOneYamlFile");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		String expectedCsarUUID = resourceDetails.getCsarUUID();
		String expectedToscaResourceName = "org.openecomp.resource.vf." + WordUtils.capitalize(resourceDetails.getName().toLowerCase());

		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, resource.getCsarUUID()), expectedCsarUUID.equals(resource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, resource.getToscaResourceName()), expectedToscaResourceName.equals(resource.getToscaResourceName()));

		RestResponse getResourceResponse = ResourceRestUtils.getResource(resource.getUniqueId());
		Resource getResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, getResource.getCsarUUID()), expectedCsarUUID.equals(getResource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, getResource.getToscaResourceName()), expectedToscaResourceName.equals(getResource.getToscaResourceName()));
	}

	@Test(enabled = true)
	public void moreThanOneMetaFileTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("moreThanOneMetaFile");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		String expectedCsarUUID = resourceDetails.getCsarUUID();
		String expectedToscaResourceName = "org.openecomp.resource.vf." + WordUtils.capitalize(resourceDetails.getName().toLowerCase());

		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, resource.getCsarUUID()), expectedCsarUUID.equals(resource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, resource.getToscaResourceName()), expectedToscaResourceName.equals(resource.getToscaResourceName()));

		RestResponse getResourceResponse = ResourceRestUtils.getResource(resource.getUniqueId());
		Resource getResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, getResource.getCsarUUID()), expectedCsarUUID.equals(getResource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, getResource.getToscaResourceName()), expectedToscaResourceName.equals(getResource.getToscaResourceName()));
	}

	@Test(enabled = true)
	public void csarNotContainsYamlAndMetaFilesTest() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("notContainYamlAndMetaFiles");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void csarNotContainsYamlFileTest() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("notContainYamlFile");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		variables.add("Definitions/tosca_mock_vf.yaml");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.YAML_NOT_FOUND_IN_CSAR.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void missingCsarFileTest() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("abc");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_NOT_FOUND.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void longNamesInToscaMetaFileTest_1() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("longNamesInToscaMetaFile1");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void longNamesInToscaMetaFileTest_2() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("longNamesInToscaMetaFile2");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void longNamesInToscaMetaFileTest_3() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("longNamesInToscaMetaFile3");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void longNamesInToscaMetaFileTest_4() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("longNamesInToscaMetaFile4");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void longNamesInToscaMetaFileTest_5() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("longNamesInToscaMetaFile5");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	// possible to have more than four lines in block 0
	// @Test (enabled = true)
	public void fiveLinesAsBlock0Test() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		resourceDetails.setCsarUUID("fiveLinesAsBlock0");
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		List<String> variables = new ArrayList<String>();
		variables.add(resourceDetails.getCsarUUID());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.CSAR_INVALID_FORMAT.name(), variables, createResource.getResponse());
	}

	@Test(enabled = true)
	public void lifecycleChangingToResourceFromCsarTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("valid_vf");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);

		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertTrue("0.1".equals(resource.getVersion()));
		assertTrue(LifeCycleStatesEnum.CHECKOUT.getComponentState().equals(resource.getLifecycleState().toString()));

		String designerUserId = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId();
		String testerUserId = ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getUserId();
		String csarUniqueId = resourceDetails.getUniqueId();
		assertNotNull(csarUniqueId);

		RestResponse lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUserId, LifeCycleStatesEnum.STARTCERTIFICATION);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUserId, LifeCycleStatesEnum.CERTIFY);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);

		resource = ResponseParser.parseToObjectUsingMapper(lifecycleChangeResponse.getResponse(), Resource.class);
		Map<String, String> allVersions = resource.getAllVersions();
		assertEquals(2, allVersions.keySet().size());
		assertEquals(2, allVersions.values().size());
		Set<String> keySet = allVersions.keySet();
		assertTrue(keySet.contains("1.0"));
		assertTrue(keySet.contains("1.1"));
	}

	@Test(enabled = true)
	public void csarWithJsonPromEnvTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("VSPPackageJsonProp.csar");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);

		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

	}

	@Test(enabled = true)
	public void uploadArtifactToResourceFromCsarTest() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("valid_vf");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);

		User designer = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ArtifactReqDetails artifactDetails = ElementFactory.getDefaultArtifact("firstArtifact");
		String firstArtifactLabel = artifactDetails.getArtifactLabel();
		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(artifactDetails, designer, resourceDetails.getUniqueId());
		ArtifactRestUtils.checkSuccess(addInformationalArtifactToResource);
		RestResponse getResourceResponse = ResourceRestUtils.getResource(resourceDetails.getUniqueId());
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		Map<String, ArtifactDefinition> informationalArtifacts = resource.getArtifacts();
		assertEquals(1, informationalArtifacts.keySet().size());
		Set<String> keySet = informationalArtifacts.keySet();
		assertTrue(keySet.contains(firstArtifactLabel.toLowerCase()));
		Collection<ArtifactDefinition> values = informationalArtifacts.values();
		assertEquals(1, values.size());
		Iterator<ArtifactDefinition> iterator = values.iterator();
		while (iterator.hasNext()) {
			ArtifactDefinition actualArtifact = iterator.next();
			assertTrue(firstArtifactLabel.equals(actualArtifact.getArtifactDisplayName()));
		}

		RestResponse lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designer.getUserId(), LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designer.getUserId(), LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);

		ArtifactReqDetails artifactDetails2 = ElementFactory.getDefaultArtifact("secondArtifact");
		artifactDetails2.setArtifactName("secondArtifact");
		addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(artifactDetails2, designer, resourceDetails.getUniqueId());
		ArtifactRestUtils.checkSuccess(addInformationalArtifactToResource);

		getResourceResponse = ResourceRestUtils.getResource(resourceDetails.getUniqueId());
		resource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		informationalArtifacts = resource.getArtifacts();
		assertEquals(2, informationalArtifacts.keySet().size());
		keySet = informationalArtifacts.keySet();
		assertTrue(keySet.contains(firstArtifactLabel.toLowerCase()));
		assertTrue(keySet.contains(artifactDetails2.getArtifactLabel().toLowerCase()));
		values = informationalArtifacts.values();
		assertEquals(2, values.size());
		ArtifactDefinition[] actualArtifacts = values.toArray(new ArtifactDefinition[2]);
		assertTrue(firstArtifactLabel.equals(actualArtifacts[0].getArtifactDisplayName()));
		assertTrue(artifactDetails2.getArtifactLabel().equals(actualArtifacts[1].getArtifactDisplayName()));
	}

	/*
	 * // @Test (enabled = true) public void createUpdateImportResourceFromCsarArtifactsWereNotChangedTest() throws Exception { // User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER); // //back original scar RestResponse
	 * copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar"); BaseRestUtils.checkSuccess(copyRes);
	 * 
	 * // resourceDetails.setResourceType(ResourceTypeEnum.VF.name()); // RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails); resourceDetails.setName("test5");
	 * resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts.csar"); resourceDetails.setCsarVersion("1"); // String invariantUUID = resource.getInvariantUUID(); // // RestResponse changeResourceState =
	 * LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN); // assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
	 * 
	 * // BaseRestUtils.checkSuccess(copyRes); // //change name (temporary) resourceDetails.setCsarVersion("2"); resourceDetails.setName("test6"); createResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails,
	 * resourceDetails.getUniqueId()); Resource updatedResource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class); Map<String, ArtifactDefinition> updatedArtifacts = updatedResource.getDeploymentArtifacts(); for
	 * (Entry<String, ArtifactDefinition> artifactEntry : resource.getDeploymentArtifacts().entrySet()) { if (updatedArtifacts.containsKey(artifactEntry.getKey())) { ArtifactDefinition currArt = updatedArtifacts.get(artifactEntry.getKey());
	 * assertEquals(currArt.getArtifactVersion(), artifactEntry.getValue().getArtifactVersion()); assertEquals(currArt.getArtifactUUID(), artifactEntry.getValue().getArtifactUUID()); assertEquals(currArt.getArtifactChecksum(),
	 * artifactEntry.getValue().getArtifactChecksum()); } } // resourceDetails = ElementFactory.getDefaultResource(); // resourceDetails.setName("test5"); // resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts.csar"); }
	 */

	@Test(enabled = true)
	public void createImportResourceFromCsarDissotiateArtifactFromGroupTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar");

		// create new resource from Csar
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts.csar");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		String invariantUUID = resource.getInvariantUUID();

		// add artifact from metadata (resource metadata should be updated)
		// RestResponse addInformationalArtifactToResource =
		// ArtifactRestUtils.addInformationalArtifactToResource(ElementFactory.getDefaultArtifact(),
		// sdncModifierDetails, resourceDetails.getUniqueId());
		// ArtifactRestUtils.checkSuccess(addInformationalArtifactToResource);
		resourceDetails.setName("test4");
		RestResponse updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertEquals(invariantUUID, resource.getInvariantUUID());

		// wrong RI (without node types, resource shouldn't be updated)
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_dissociate.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
		// change name (temporary)
		resourceDetails.setName("test4");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertEquals(invariantUUID, resource.getInvariantUUID());

		// back original scar
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
	}

	@Test(enabled = true)
	public void createImportResourceFromCsarNewgroupTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar");

		// create new resource from Csar
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts.csar");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		String invariantUUID = resource.getInvariantUUID();

		// update scar
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_UpdateToscaAndArtifacts.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails.setName("test2");
		// change resource metaData (resource should be updated)
		resourceDetails.setDescription("It is new description bla bla bla");
		RestResponse updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		assertEquals(invariantUUID, resource.getInvariantUUID());

		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
	}

	@Test(enabled = true)
	public void createImportResourceFromCsarGetGroupTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		// RestResponse copyRes =
		// copyCsarRest(sdncModifierDetails,"VF_RI2_G4_withArtifacts_a.csar","VF_RI2_G4_withArtifacts.csar");

		// create new resource from Csar
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("VSPPackage");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		String invariantUUID = resource.getInvariantUUID();
		List<GroupDefinition> groups = resource.getGroups();

		GroupDefinition groupWithArtifact = groups.stream().filter(p -> p.getArtifacts() != null && !p.getArtifacts().isEmpty()).findFirst().get();

		RestResponse groupRest = GroupRestUtils.getGroupById(resource, groupWithArtifact.getUniqueId(), sdncModifierDetails);
		BaseRestUtils.checkSuccess(groupRest);

		GroupDefinition groupWithoutArtifact = groups.stream().filter(p -> p.getArtifacts() == null || p.getArtifacts().isEmpty()).findFirst().get();

		groupRest = GroupRestUtils.getGroupById(resource, groupWithoutArtifact.getUniqueId(), sdncModifierDetails);
		BaseRestUtils.checkSuccess(groupRest);
	}

	@Test(enabled = true)
	public void createImportResourceFromCsarUITest() throws Exception {
		RestResponse getResource = null;
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String payloadName = "valid_vf.csar";
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		String rootPath = System.getProperty("user.dir");
		Path path = null;
		byte[] data = null;
		String payloadData = null;

		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		// create new resource from Csar
		resourceDetails.setCsarUUID(payloadName);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		RestResponse changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// change composition (resource should be updated)
		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf_b.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		// change name
		resourceDetails.setName("test1");
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(2, resource.getComponentInstances().size());

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// change name
		resourceDetails.setName("test2");
		// change resource metaData (resource should be updated)
		resourceDetails.setDescription("It is new description bla bla bla");
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(2, resource.getComponentInstances().size());

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// wrong RI (without node types, resource shouldn't be updated)
		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf_c.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		// change name
		resourceDetails.setName("test3");
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.INVALID_NODE_TEMPLATE, "Definitions/tosca_mock_vf.yaml", "nodejs", "tosca.nodes.Weber");
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(null, resource);
		getResource = ResourceRestUtils.getResourceByNameAndVersion(sdncModifierDetails.getUserId(), "test3", resourceDetails.getVersion());
		BaseRestUtils.checkErrorResponse(getResource, ActionStatus.RESOURCE_NOT_FOUND, "test3");

		// create new resource from other Csar
		resourceDetails = ElementFactory.getDefaultImportResource();
		path = Paths.get(rootPath + "/src/main/resources/ci/VF_RI2_G4_withArtifacts.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName("VF_RI2_G4_withArtifacts.csar");
		resourceDetails.setName("test4");
		resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts.csar");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// wrong RI (with node types) resource shouldn't be created
		resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts_b.csar");
		path = Paths.get(rootPath + "/src/main/resources/ci/VF_RI2_G4_withArtifacts_b.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName("VF_RI2_G4_withArtifacts_b.csar");
		resourceDetails.setName("test5");
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.INVALID_NODE_TEMPLATE, "Definitions/VF_RI2_G1.yaml", "ps04_port_0", "org.openecomp.resource.cp.nodes.heat.network.neutron.Portur");
	}

	@Test(enabled = true)
	public void createUpdateImportResourceFromCsarUITest() throws Exception {
		RestResponse getResource = null;
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String payloadName = "valid_vf.csar";
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		String rootPath = System.getProperty("user.dir");
		Path path = null;
		byte[] data = null;
		String payloadData = null;

		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		// create new resource from Csar
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		RestResponse changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// change composition and update resource
		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf_b.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setUniqueId(resource.getUniqueId());
		// change name
		RestResponse updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resource.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertEquals(2, resource.getComponentInstances().size());

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// change name
		resourceDetails.setName("test2");
		// change resource metaData (resource should be updated)
		resourceDetails.setDescription("It is new description bla bla bla");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resource.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertEquals(2, resource.getComponentInstances().size());

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// try to update resource with wrong RI (without node types, resource
		// shouldn't be updated)
		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf_c.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		// change name
		resourceDetails.setName("test3");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resource.getUniqueId());
		BaseRestUtils.checkErrorResponse(updateResource, ActionStatus.INVALID_NODE_TEMPLATE, "Definitions/tosca_mock_vf.yaml", "nodejs", "tosca.nodes.Weber");
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertEquals(null, resource);
		getResource = ResourceRestUtils.getResourceByNameAndVersion(sdncModifierDetails.getUserId(), "test3", resourceDetails.getVersion());
		BaseRestUtils.checkErrorResponse(getResource, ActionStatus.RESOURCE_NOT_FOUND, "test3");
	}

	@Test(enabled = true)
	public void createUpdateImportResourceFromCsarTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = null;
		RestResponse getResource = null;
		ResourceReqDetails resourceDetails = null;
		RestResponse updateResource = null;
		RestResponse createResource = null;
		Resource resource = null;
		RestResponse changeResourceState = null;

		// create new resource from Csar
		copyRes = copyCsarRest(sdncModifierDetails, "valid_vf_a.csar", "valid_vf.csar");
		resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("valid_vf.csar");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());
		String invariantUUID = resource.getInvariantUUID();

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// change composition and update resource
		copyRes = copyCsarRest(sdncModifierDetails, "valid_vf_b.csar", "valid_vf.csar");
		BaseRestUtils.checkSuccess(copyRes);
		// change name
		resourceDetails.setName("test1");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resource.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertEquals(2, resource.getComponentInstances().size());
		assertEquals(invariantUUID, resource.getInvariantUUID());

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// back original scar
		copyRes = copyCsarRest(sdncModifierDetails, "valid_vf_a.csar", "valid_vf.csar");
		BaseRestUtils.checkSuccess(copyRes);

		// change name
		resourceDetails.setName("test2");
		// change resource metaData and update resource
		resourceDetails.setDescription("It is new description bla bla bla");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resource.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());
		assertEquals(invariantUUID, resource.getInvariantUUID());

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// back original scar
		copyRes = copyCsarRest(sdncModifierDetails, "valid_vf_a.csar", "valid_vf.csar");
		BaseRestUtils.checkSuccess(copyRes);

		// try to update resource with wrong RI (without node types, resource
		// shouldn't be updated)
		copyRes = copyCsarRest(sdncModifierDetails, "valid_vf_c.csar", "valid_vf.csar");
		BaseRestUtils.checkSuccess(copyRes);
		// change name (temporary)
		resourceDetails.setName("test3");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resource.getUniqueId());
		BaseRestUtils.checkErrorResponse(updateResource, ActionStatus.INVALID_NODE_TEMPLATE, "Definitions/tosca_mock_vf.yaml", "nodejs", "tosca.nodes.Weber");

		getResource = ResourceRestUtils.getResourceByNameAndVersion(sdncModifierDetails.getUserId(), "test3", resourceDetails.getVersion());
		BaseRestUtils.checkErrorResponse(getResource, ActionStatus.RESOURCE_NOT_FOUND, "test3");
		getResource = ResourceRestUtils.getResourceByNameAndVersion(sdncModifierDetails.getUserId(), "test2", resourceDetails.getVersion());
		BaseRestUtils.checkSuccess(getResource);

		// back original scar
		copyRes = copyCsarRest(sdncModifierDetails, "valid_vf_a.csar", "valid_vf.csar");
		BaseRestUtils.checkSuccess(copyRes);

		// create new resource from Csar
		// back original scar
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("TEST01");
		resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts.csar");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// scar with wrong RI
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_b.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
		resourceDetails.setDescription("BLA BLA BLA");
		// wrong RI (with node types) resource shouldn't be created
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resourceDetails.getUniqueId());
		BaseRestUtils.checkErrorResponse(updateResource, ActionStatus.INVALID_NODE_TEMPLATE, "Definitions/VF_RI2_G1.yaml", "ps04_port_0", "org.openecomp.resource.cp.nodes.heat.network.neutron.Portur");
		// back original scar
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
	}

	@Test(enabled = true)
	public void createUpdateImportResourceFromCsarWithArtifactsTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = null;
		ResourceReqDetails resourceDetails = null;
		RestResponse updateResource = null;
		RestResponse createResource = null;
		Resource resource = null;
		RestResponse changeResourceState = null;

		// back original scar
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);

		resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName("TEST01");
		resourceDetails.setCsarUUID("VF_RI2_G4_withArtifacts.csar");
		resourceDetails.setCsarVersion("1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		// create new resource from Csar
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

		List<String> requiredArtifactsOld = resource.getDeploymentArtifacts().get("heat5").getRequiredArtifacts();
		assertTrue(requiredArtifactsOld != null && !requiredArtifactsOld.isEmpty() && requiredArtifactsOld.size() == 3);
		assertTrue(requiredArtifactsOld.contains("hot-nimbus-pcm-volumes_v1.0.yaml"));
		assertTrue(requiredArtifactsOld.contains("nested-pcm_v1.0.yaml"));
		assertTrue(requiredArtifactsOld.contains("hot-nimbus-oam-volumes_v1.0.yaml"));

		// update scar with new artifacts
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_updated.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
		resourceDetails.setDescription("BLA BLA BLA");
		resourceDetails.setCsarVersion("2");
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);

		List<String> requiredArtifactsNew = resource.getDeploymentArtifacts().get("heat5").getRequiredArtifacts();
		assertTrue(requiredArtifactsNew != null && !requiredArtifactsNew.isEmpty() && requiredArtifactsNew.size() == 3);
		assertTrue(requiredArtifactsNew.contains("hot-nimbus-swift-container_v1.0.yaml"));
		assertTrue(requiredArtifactsNew.contains("hot-nimbus-oam-volumes_v1.0.yaml"));
		assertTrue(requiredArtifactsNew.contains("nested-oam_v1.0.yaml"));

		// back original scar
		copyRes = copyCsarRest(sdncModifierDetails, "VF_RI2_G4_withArtifacts_a.csar", "VF_RI2_G4_withArtifacts.csar");
		BaseRestUtils.checkSuccess(copyRes);
	}

	@Test(enabled = true)
	public void createUpdateImportWithPropertiesFromCsarUITest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String payloadName = "valid_vf.csar";
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		String rootPath = System.getProperty("user.dir");
		Path path = null;
		byte[] data = null;
		String payloadData = null;

		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		// create new resource from Csar
		resourceDetails.setCsarUUID(payloadName);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		RestResponse changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// change composition (add new RI with specified property values)
		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf_d.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		// change name
		resourceDetails.setName("test1");
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(6, resource.getComponentInstances().size());

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		// change composition (add new specified property values to existing RI)
		path = Paths.get(rootPath + "/src/main/resources/ci/valid_vf_f.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		// change name
		resourceDetails.setName("test2");
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(6, resource.getComponentInstances().size());

	}

	public static RestResponse copyCsarRest(User sdncModifierDetails, String sourceCsarUuid, String targetCsarUuid) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.COPY_CSAR_USING_SIMULATOR, config.getCatalogBeHost(), config.getCatalogBePort(), sourceCsarUuid, targetCsarUuid);
		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();

		RestResponse copyCsarResponse = http.httpSendPost(url, "dummy", headersMap);
		if (copyCsarResponse.getErrorCode() != 200) {
			return null;
		}
		return copyCsarResponse;

	}

	public static RestResponse getCsarRest(User sdncModifierDetails, String sourceCsarUuid) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_CSAR_USING_SIMULATOR, config.getCatalogBeHost(), config.getCatalogBePort(), sourceCsarUuid);
		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();

		RestResponse copyCsarResponse = http.httpSendGet(url, headersMap);
		if (copyCsarResponse.getErrorCode() != 200) {
			return null;
		}
		return copyCsarResponse;

	}

	@Test(enabled = true)
	public void updateResourceFromCsarHappy() throws Exception {
		RestResponse copyRes = copyCsarRest(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), "valid_vf_a.csar", "valid_vf.csar");
		BaseRestUtils.checkSuccess(copyRes);
		// create
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("valid_vf");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		String expectedCsarUUID = resourceDetails.getCsarUUID();
		String expectedToscaResourceName = "org.openecomp.resource.vf." + WordUtils.capitalize(resourceDetails.getName().toLowerCase());

		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, resource.getCsarUUID()), expectedCsarUUID.equals(resource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, resource.getToscaResourceName()), expectedToscaResourceName.equals(resource.getToscaResourceName()));

		RestResponse getResourceResponse = ResourceRestUtils.getResource(resource.getUniqueId());
		Resource getResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, getResource.getCsarUUID()), expectedCsarUUID.equals(getResource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, getResource.getToscaResourceName()), expectedToscaResourceName.equals(getResource.getToscaResourceName()));

		RestResponse updateResource = ResourceRestUtils.updateResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);

	}

	@Test(enabled = true)
	public void createResourceFromCsarWithGroupsHappy() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("csarWithGroups");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);

		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		assertEquals("verify there are 2 groups", 2, resource.getGroups().size());

		Map<String, String> compNameToUniqueId = resource.getComponentInstances().stream().collect(Collectors.toMap(p -> p.getName(), p -> p.getUniqueId()));

		// Verify 2 members on group1
		// members: [ app_server, mongo_server ]
		String[] membersNameGroup1 = { "app_server", "mongo_server" };
		verifyMembersInResource(resource, compNameToUniqueId, "group1", membersNameGroup1);
		// Verify 4 members on group2
		// members: [ mongo_db, nodejs, app_server, mongo_server ]
		String[] membersNameGroup2 = { "app_server", "mongo_server", "mongo_db", "nodejs" };
		verifyMembersInResource(resource, compNameToUniqueId, "group2", membersNameGroup2);

		// Check OUT
		resourceDetails.setUniqueId(resource.getUniqueId());
		RestResponse changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		changeResourceState = LifecycleRestUtils.changeResourceState(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());

		Resource checkedOutResource = ResponseParser.parseToObjectUsingMapper(changeResourceState.getResponse(), Resource.class);
		compNameToUniqueId = checkedOutResource.getComponentInstances().stream().collect(Collectors.toMap(p -> p.getName(), p -> p.getUniqueId()));

		// Verify 2 members on group1
		// members: [ app_server, mongo_server ]
		verifyMembersInResource(checkedOutResource, compNameToUniqueId, "group1", membersNameGroup1);
		// Verify 4 members on group2
		// members: [ mongo_db, nodejs, app_server, mongo_server ]
		verifyMembersInResource(checkedOutResource, compNameToUniqueId, "group2", membersNameGroup2);

	}

	private void verifyMembersInResource(Resource resource, Map<String, String> compNameToUniqueId, String groupName, String[] membersName) {
		GroupDefinition groupDefinition = resource.getGroups().stream().filter(p -> p.getName().equals(groupName)).findFirst().get();
		assertEquals("Verify number of members", membersName.length, groupDefinition.getMembers().size());
		Map<String, String> createdMembers = groupDefinition.getMembers();
		Arrays.asList(membersName).forEach(p -> {
			assertTrue("check member name exist", createdMembers.containsKey(p));
		});

		verifyMembers(createdMembers, compNameToUniqueId);
	}

	@Test(enabled = true)
	public void createResourceFromCsarWithGroupsAndPropertiesHappy() throws Exception {

		RestResponse importNewGroupTypeByName = ImportRestUtils.importNewGroupTypeByName("myHeatStack1", UserRoleEnum.ADMIN);
		// BaseRestUtils.checkCreateResponse(importNewGroupTypeByName);

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("csarWithGroupsWithProps");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);

		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());

		assertEquals("verify there are 2 groups", 2, resource.getGroups().size());

		Map<String, String> compNameToUniqueId = resource.getComponentInstances().stream().collect(Collectors.toMap(p -> p.getName(), p -> p.getUniqueId()));

		// Verify 2 members on group1
		// members: [ app_server, mongo_server ]
		List<GroupDefinition> groupDefinition1 = resource.getGroups().stream().filter(p -> p.getName().equals("group1")).collect(Collectors.toList());
		assertEquals("Verify number of members", 2, groupDefinition1.get(0).getMembers().size());
		Map<String, String> createdMembers = groupDefinition1.get(0).getMembers();
		verifyMembers(createdMembers, compNameToUniqueId);

		List<GroupProperty> properties = groupDefinition1.get(0).convertToGroupProperties();
		assertEquals("Verify number of members", 2, properties.size());

		GroupProperty heatFiles = properties.stream().filter(p -> p.getName().equals("heat_files")).findFirst().get();
		assertNotNull("check heat files not empty", heatFiles);
		List<String> heatFilesValue = new ArrayList<>();
		heatFilesValue.add("heat1.yaml");
		heatFilesValue.add("heat2.yaml");
		String heatFilesJson = gson.toJson(heatFilesValue);
		log.debug(heatFiles.getValue());
		assertEquals("check heat files value", heatFilesJson, heatFiles.getValue());

		GroupProperty urlCredential = properties.stream().filter(p -> p.getName().equals("url_credential")).findFirst().get();
		assertNotNull("check heat files not empty", urlCredential);
		log.debug(urlCredential.getValue());
		assertEquals("check url credential", "{\"protocol\":\"protocol1\",\"keys\":{\"keya\":\"valuea\",\"keyb\":\"valueb\"}}", urlCredential.getValue());
	}

	@Test(enabled = true)
	public void createResourceFromCsarWithGroupsAndPropertyInvalidValue() throws Exception {

		RestResponse importNewGroupTypeByName = ImportRestUtils.importNewGroupTypeByName("myHeatStack1", UserRoleEnum.ADMIN);
		// BaseRestUtils.checkCreateResponse(importNewGroupTypeByName);

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("csarWithGroupsInvalidPropertyValue");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));

		BaseRestUtils.checkStatusCode(createResource, "Check bad request error", false, 400);

	}

	@Test(enabled = true)
	public void createResourceFromCsarWithGroupsAndInvalidPropertyName() throws Exception {

		RestResponse importNewGroupTypeByName = ImportRestUtils.importNewGroupTypeByName("myHeatStack1", UserRoleEnum.ADMIN);
		// BaseRestUtils.checkCreateResponse(importNewGroupTypeByName);

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("csarWithGroupsPropertyNotExist");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));

		BaseRestUtils.checkStatusCode(createResource, "Check bad request error", false, 400);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.GROUP_PROPERTY_NOT_FOUND, "url_credential111", "group1", "org.openecomp.groups.MyHeatStack1");

	}

	@Test(enabled = true)
	public void createResourceFromCsarGroupTypeNotExist() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("csarWithGroupsInvalidGroupType");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));

		BaseRestUtils.checkStatusCode(createResource, "Check bad request error", false, 400);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.GROUP_TYPE_IS_INVALID, "org.openecomp.groups.stamGroupType");

	}

	@Test(enabled = true)
	public void createResourceFromCsarMemberNotExist() throws Exception {

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("csarWithGroupsInvalidMember");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));

		BaseRestUtils.checkStatusCode(createResource, "Check bad request error", false, 400);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.GROUP_INVALID_COMPONENT_INSTANCE, "mycomp", "mygroup", ValidationUtils.normaliseComponentName(resourceDetails.getName()), "VF");

	}

	@Test(enabled = true)
	public void createResourceFromCsarMemberNotAllowed() throws Exception {

		RestResponse importNewGroupTypeByName = ImportRestUtils.importNewGroupTypeByName("myHeatStack2", UserRoleEnum.ADMIN);

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("csarWithGroupsNotAllowedMember");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));

		BaseRestUtils.checkStatusCode(createResource, "Check bad request error", false, 400);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.GROUP_INVALID_TOSCA_NAME_OF_COMPONENT_INSTANCE, "nodejs", "group1", "org.openecomp.groups.MyHeatStack2");

	}

	@Test(enabled = true)
	public void getResourceFromCsarUuidHappy() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("tam");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(6, resource.getComponentInstances().size());

		String expectedCsarUUID = resourceDetails.getCsarUUID();
		String expectedToscaResourceName = "org.openecomp.resource.vf." + WordUtils.capitalize(resourceDetails.getName().toLowerCase());

		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, resource.getCsarUUID()), expectedCsarUUID.equals(resource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, resource.getToscaResourceName()), expectedToscaResourceName.equals(resource.getToscaResourceName()));

		RestResponse getResourceResponse = ResourceRestUtils.getLatestResourceFromCsarUuid(resource.getCsarUUID());
		Resource getResource = ResponseParser.parseToObjectUsingMapper(getResourceResponse.getResponse(), Resource.class);
		assertTrue("csarUUID : " + buildAssertMessage(expectedCsarUUID, getResource.getCsarUUID()), expectedCsarUUID.equals(getResource.getCsarUUID()));
		assertTrue("toscaResourceName : " + buildAssertMessage(expectedToscaResourceName, getResource.getToscaResourceName()), expectedToscaResourceName.equals(getResource.getToscaResourceName()));
	}

	@Test(enabled = true)
	public void getResourceFromCsarResourceNotFound() throws Exception {
		String csarUUID = "tam";
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());

		RestResponse resResponse = ResourceRestUtils.getLatestResourceFromCsarUuid(csarUUID);

		BaseRestUtils.checkStatusCode(resResponse, "Check bad request error", false, 400);
		BaseRestUtils.checkErrorResponse(resResponse, ActionStatus.RESOURCE_FROM_CSAR_NOT_FOUND, csarUUID);

	}

	@Test(enabled = true)
	public void getResourceFromMissingCsar() throws Exception {
		String csarUUID = "abcdefg12345";
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID(csarUUID);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());

		RestResponse resResponse = ResourceRestUtils.getLatestResourceFromCsarUuid(csarUUID);

		BaseRestUtils.checkStatusCode(resResponse, "Check bad request error", false, 400);
		BaseRestUtils.checkErrorResponse(resResponse, ActionStatus.RESOURCE_FROM_CSAR_NOT_FOUND, csarUUID);

	}

	@Test(enabled = true)
	public void createUpdateCertifiedImportResourceFromCsarTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse copyRes = copyCsarRest(sdncModifierDetails, "valid_vf_a.csar", "valid_vf.csar");
		RestResponse updateResponse = null;
		String oldName = null;
		// create new resource from Csar
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("valid_vf.csar");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());
		String invariantUUID = resource.getInvariantUUID();

		// change metadata
		// resource name, icon, vendor name, category, template derivedFrom
		oldName = resourceDetails.getName();
		resourceDetails.setName("test1");
		resourceDetails.setIcon("newicon");
		resourceDetails.setVendorName("newname");
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.VSP_ALREADY_EXISTS, "valid_vf.csar", oldName);

		updateResponse = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResponse);

		LifecycleRestUtils.certifyResource(resourceDetails);
		// change metadata
		// resource name, icon, vendor name, category, template derivedFrom
		resourceDetails.setName("test2");
		resourceDetails.setIcon("new icon1");
		resourceDetails.setVendorName("new name1");
		resourceDetails.setDescription("bla bla bla");
		updateResponse = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResponse);
		resource = ResponseParser.parseToObjectUsingMapper(updateResponse.getResponse(), Resource.class);
		assertEquals(5, resource.getComponentInstances().size());
		assertEquals(invariantUUID, resource.getInvariantUUID());
		assertEquals(resource.getName(), "test1");
		assertEquals(resource.getIcon(), "newicon");
		assertEquals(resource.getVendorName(), "newname");
		assertEquals(resource.getDescription(), "bla bla bla");
		assertEquals(resource.getTags().contains("test2"), false);
	}

	@Test
	public void createImportRIRelationByCapNameFromCsarUITest() throws Exception {
		Resource resource = ResourceRestUtils.importResourceFromCsar("vmmc_relate_by_cap_name.csar");
		// assert all relations created
		assertEquals(80, resource.getComponentInstancesRelations().size());
	}

	@Test
	public void createImportRIRelationByCapNameFromCsarUITest2() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String payloadName = "vf_relate_by_cap_name.csar";
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		String rootPath = System.getProperty("user.dir");
		Path path = null;
		byte[] data = null;
		String payloadData = null;

		path = Paths.get(rootPath + CSARS_PATH + "vf_relate_by_cap_name.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);

		// create new resource from Csar
		resourceDetails.setCsarUUID(payloadName);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		// assert relations created: 1.by name: virtual_linkable. 2.by name:
		// link
		Map<String, ComponentInstance> nodes = resource.getComponentInstances().stream().collect(Collectors.toMap(n -> n.getName(), n -> n));
		Map<String, CapabilityDefinition> capabilities = nodes.get("elinenode").getCapabilities().get("tosca.capabilities.network.Linkable").stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
		String cp1Uid = nodes.get("cp1node").getUniqueId();
		String cp2Uid = nodes.get("cp2node").getUniqueId();
		Map<String, List<RequirementCapabilityRelDef>> mappedByReqOwner = resource.getComponentInstancesRelations().stream().collect(Collectors.groupingBy(e -> e.getFromNode()));
		assertEquals(mappedByReqOwner.get(cp1Uid).get(0).getRelationships().get(0).getCapabilityUid(), capabilities.get("virtual_linkable").getUniqueId());
		assertEquals(mappedByReqOwner.get(cp2Uid).get(0).getRelationships().get(0).getCapabilityUid(), capabilities.get("link").getUniqueId());
	}

	@Test(enabled = true)
	public void importCsarCheckVfHeatEnv() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("csar_1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

	
		Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
		assertNotNull(deploymentArtifacts);
		// 2 lisence, 1 heat, 1 heatenv
		assertEquals(4, deploymentArtifacts.size());

		ArtifactDefinition artifactHeat = deploymentArtifacts.get("heat0");
		assertNotNull(artifactHeat);

		ArtifactDefinition artifactHeatEnv = deploymentArtifacts.get("heat0env");
		assertNotNull(artifactHeatEnv);

		assertEquals(artifactHeat.getUniqueId(), artifactHeatEnv.getGeneratedFromId());
		assertEquals("VF HEAT ENV", artifactHeatEnv.getArtifactDisplayName());
		assertEquals("HEAT_ENV", artifactHeatEnv.getArtifactType());
		assertEquals("VF Auto-generated HEAT Environment deployment artifact", artifactHeatEnv.getDescription());

		String designerUserId = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId();
		String testerUserId = ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getUserId();
		RestResponse lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CHECKOUT);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CHECKIN);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUserId, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUserId, LifeCycleStatesEnum.STARTCERTIFICATION);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		lifecycleChangeResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUserId, LifeCycleStatesEnum.CERTIFY);
		LifecycleRestUtils.checkSuccess(lifecycleChangeResponse);
		Resource certifiedResource = ResponseParser.parseToObjectUsingMapper(lifecycleChangeResponse.getResponse(), Resource.class);


		User modifier = new User();
		modifier.setUserId(designerUserId);

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("newtestservice1", ServiceCategoriesEnum.MOBILITY, designerUserId);
		
		RestResponse serviceRes = ServiceRestUtils.createService(serviceDetails, modifier);
		ResourceRestUtils.checkCreateResponse(serviceRes);
		Service service =  ResponseParser.parseToObjectUsingMapper(serviceRes.getResponse(), Service.class);

		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory.getComponentInstance(certifiedResource);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, modifier, service.getUniqueId(), service.getComponentType());
		BaseRestUtils.checkCreateResponse(createResourceInstanceResponse);
		RestResponse serviceByGet = ServiceRestUtils.getService(service.getUniqueId());
		service =  ResponseParser.parseToObjectUsingMapper(serviceByGet.getResponse(), Service.class);
		
		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		
		assertEquals(1, componentInstances.size());
		ComponentInstance ci = componentInstances.get(0);
		Map<String, ArtifactDefinition> instDepArtifacts = ci.getDeploymentArtifacts();
		assertNotNull(instDepArtifacts);
		ArtifactDefinition instArtifactHeat = instDepArtifacts.get("heat0");
		assertNotNull(instArtifactHeat);

		ArtifactDefinition instArtifactHeatEnv = instDepArtifacts.get("heat0env");
		assertNotNull(instArtifactHeatEnv);
		assertEquals(artifactHeat.getUniqueId(), instArtifactHeatEnv.getGeneratedFromId());
		assertEquals("HEAT ENV", instArtifactHeatEnv.getArtifactDisplayName());
		assertEquals("HEAT_ENV", instArtifactHeatEnv.getArtifactType());

		assertEquals(artifactHeat.getUniqueId(), instArtifactHeat.getUniqueId());
		//different artifacts
		assertTrue( !artifactHeatEnv.getUniqueId().equals(instArtifactHeat.getUniqueId()) );
	

	}
	
	@Test(enabled = true)
	public void createAndUpdateCsarCheckVfHeatEnv() throws Exception {
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setCsarUUID("orig2G_org");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);

	
		Map<String, ArtifactDefinition> deploymentArtifacts = resource.getDeploymentArtifacts();
		assertNotNull(deploymentArtifacts);
		
		assertEquals(13, deploymentArtifacts.size());

		ArtifactDefinition artifactHeat = deploymentArtifacts.get("heat0");
		assertNotNull(artifactHeat);

		ArtifactDefinition artifactHeatEnv = deploymentArtifacts.get("heat0env");
		assertNotNull(artifactHeatEnv);

		assertEquals(artifactHeat.getUniqueId(), artifactHeatEnv.getGeneratedFromId());
		assertEquals("VF HEAT ENV", artifactHeatEnv.getArtifactDisplayName());
		assertEquals("HEAT_ENV", artifactHeatEnv.getArtifactType());
		assertEquals("VF Auto-generated HEAT Environment deployment artifact", artifactHeatEnv.getDescription());
		
		List<GroupDefinition>  groups = resource.getGroups();
		assertEquals(2, groups.size());
		GroupDefinition group1 = groups.stream().filter(p -> p.getName().contains("module-0")).findAny().get();
		GroupDefinition group2 = groups.stream().filter(p -> p.getName().contains("module-1")).findAny().get();
		assertEquals(11, group1.getArtifacts().size());
		assertEquals(3, group2.getArtifacts().size());
		
		resourceDetails.setCsarUUID("orig2G_update");
		
		RestResponse updateResource = ResourceRestUtils.updateResource(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);

		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);

		
		Map<String, ArtifactDefinition> deploymentArtifactsUpd = resource.getDeploymentArtifacts();
		assertNotNull(deploymentArtifactsUpd);
		
		assertEquals(13, deploymentArtifactsUpd.size());

		ArtifactDefinition artifactHeatUpd = deploymentArtifacts.get("heat0");
		assertNotNull(artifactHeatUpd);

		ArtifactDefinition artifactHeatEnvUpd = deploymentArtifacts.get("heat0env");
		assertNotNull(artifactHeatEnvUpd);
		
		groups = resource.getGroups();
		assertEquals(2, groups.size());
		assertEquals(7, groups.get(0).getArtifacts().size());
		assertEquals(7, groups.get(1).getArtifacts().size());
	

	}
	
	@Test
	public void importInnerVfcWithArtifactsSucceed() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String rootPath = System.getProperty("user.dir");
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		
		String payloadName = "ImportArtifactsToVFC.csar";
		Path path = Paths.get(rootPath + CSARS_PATH + "ImportArtifactsToVFC.csar");
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		
		List<ComponentInstance> componentInstances = resource.getComponentInstances();
		List<ComponentInstance> reducedComponentInstances = componentInstances.stream()
				.filter(ci->ci.getNormalizedName().contains("server_sm"))
				.collect(Collectors.toList());
		assertTrue(!reducedComponentInstances.isEmpty() && reducedComponentInstances.size() == 2);
		reducedComponentInstances.stream().forEach(ci->isValidArtifacts(ci));
		
		payloadName = "ImportArtifactsToVFC_empty.csar";
		path = Paths.get(rootPath + CSARS_PATH + "ImportArtifactsToVFC_empty.csar");
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setName(resourceDetails.getName()+"2");
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		
		createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		
		componentInstances = resource.getComponentInstances();
		reducedComponentInstances = componentInstances.stream()
				.filter(ci->ci.getNormalizedName().contains("server_sm"))
				.collect(Collectors.toList());
		assertTrue(!reducedComponentInstances.isEmpty() && reducedComponentInstances.size() == 2);
		reducedComponentInstances.stream()
		.forEach(ci->assertTrue(
				(ci.getDeploymentArtifacts()==null || ci.getDeploymentArtifacts().isEmpty()) &&
				(ci.getArtifacts()==null || ci.getArtifacts().isEmpty()))
				);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void importInnerVfcWithArtifactsUpdateSucceed() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		String rootPath = System.getProperty("user.dir");
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		
		String payloadName = "vfc_artifacts.csar";
		Path path = Paths.get(rootPath + CSARS_PATH + payloadName);
		byte[] data = Files.readAllBytes(path);
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		
		Map<String,String> validCreatedInformationalArtifactVersions = new HashMap<>();
		validCreatedInformationalArtifactVersions.put("GuideInfoDelete.mib","1");
		validCreatedInformationalArtifactVersions.put("GuideInfoUpdate.mib","1");
		validCreatedInformationalArtifactVersions.put("OtherInfoIgnore.mib","1");
		
		Map<String,String> validCreatedDeploymentArtifactVersions = new HashMap<>();
		validCreatedDeploymentArtifactVersions.put("PollDelete.mib","1");
		validCreatedDeploymentArtifactVersions.put("PollUpdate.mib","1");
		validCreatedDeploymentArtifactVersions.put("TrapDelete.mib","1");
		validCreatedDeploymentArtifactVersions.put("TrapUpdate.mib","1");
		
		Map<String,String> validUpdatedInformationalArtifactVersions = new HashMap<>();
		validUpdatedInformationalArtifactVersions.put("GuideInfoNew.mib","1");
		validUpdatedInformationalArtifactVersions.put("GuideInfoUpdate.mib","2");
		validUpdatedInformationalArtifactVersions.put("OtherInfoIgnore.mib","1");
		
		Map<String,String> validUpdatedDeploymentArtifactVersions = new HashMap<>();
		validUpdatedDeploymentArtifactVersions.put("PollNew.mib","1");
		validUpdatedDeploymentArtifactVersions.put("PollUpdate.mib","2");
		validUpdatedDeploymentArtifactVersions.put("TrapNew.mib","1");
		validUpdatedDeploymentArtifactVersions.put("TrapUpdate.mib","2");

		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		
		List<ComponentInstance> componentInstances = resource.getComponentInstances().stream()
				.filter(ci->ci.getNormalizedName().contains("ltm_server"))
				.collect(Collectors.toList());
		assertTrue(!componentInstances.isEmpty() && componentInstances.size() == 1);
		ComponentInstance componentInstance = componentInstances.get(0);
		assertTrue(!componentInstance.getArtifacts().isEmpty() && componentInstance.getArtifacts().size() == 3);
		componentInstance.getArtifacts().values().stream()
		.forEach(a->assertTrue(validCreatedInformationalArtifactVersions.containsKey(a.getArtifactName()) && 
				validCreatedInformationalArtifactVersions.get(a.getArtifactName()).equals(a.getArtifactVersion())));
		
		assertTrue(!componentInstance.getDeploymentArtifacts().isEmpty() && componentInstance.getDeploymentArtifacts().size() == 4);
		componentInstance.getDeploymentArtifacts().values().stream()
		.forEach(a->assertTrue(validCreatedDeploymentArtifactVersions.containsKey(a.getArtifactName()) && 
				validCreatedDeploymentArtifactVersions.get(a.getArtifactName()).equals(a.getArtifactVersion())));
		
		payloadName = "vfc_artifacts_update.csar";
		path = Paths.get(rootPath + CSARS_PATH + payloadName);
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName(payloadName);
		
		RestResponse updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resource.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		
		componentInstances = resource.getComponentInstances().stream()
				.filter(ci->ci.getNormalizedName().contains("ltm_server"))
				.collect(Collectors.toList());
		assertTrue(!componentInstances.isEmpty() && componentInstances.size() == 1);
		componentInstance = componentInstances.get(0);
		assertTrue(!componentInstance.getArtifacts().isEmpty() && componentInstance.getArtifacts().size() == 3);
		componentInstance.getArtifacts().values().stream()
		.forEach(a->assertTrue(validUpdatedInformationalArtifactVersions.containsKey(a.getArtifactName()) && 
				validUpdatedInformationalArtifactVersions.get(a.getArtifactName()).equals(a.getArtifactVersion())));
		
		assertTrue(!componentInstance.getDeploymentArtifacts().isEmpty() && componentInstance.getDeploymentArtifacts().size() == 4);
		componentInstance.getDeploymentArtifacts().values().stream()
		.forEach(a->assertTrue(validUpdatedDeploymentArtifactVersions.containsKey(a.getArtifactName()) && 
				validUpdatedDeploymentArtifactVersions.get(a.getArtifactName()).equals(a.getArtifactVersion())));
		
		
		payloadName = "vfc_artifacts_delete_all.csar";
		path = Paths.get(rootPath + CSARS_PATH + payloadName);
		data = Files.readAllBytes(path);
		payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setPayloadName(payloadName);
		
		updateResource = ResourceRestUtils.updateResource(resourceDetails, sdncModifierDetails, resource.getUniqueId());
		BaseRestUtils.checkSuccess(updateResource);
		resource = ResponseParser.parseToObjectUsingMapper(updateResource.getResponse(), Resource.class);
		
		componentInstances = resource.getComponentInstances().stream()
				.filter(ci->ci.getNormalizedName().contains("ltm_server"))
				.collect(Collectors.toList());
		assertTrue(!componentInstances.isEmpty() && componentInstances.size() == 1);
		componentInstance = componentInstances.get(0);
		assertTrue(componentInstance.getArtifacts() == null || componentInstance.getArtifacts().isEmpty());
		assertTrue(componentInstance.getDeploymentArtifacts() == null || componentInstance.getDeploymentArtifacts().isEmpty());
	}
	
	private void isValidArtifacts(ComponentInstance ci) {
		assertTrue(!ci.getDeploymentArtifacts().isEmpty() && ci.getDeploymentArtifacts().size() == 11);
		ci.getDeploymentArtifacts().values().stream()
						 .forEach(a->assertTrue(a.getArtifactName().startsWith("Some")));
		
		assertTrue(!ci.getArtifacts().isEmpty() && ci.getArtifacts().size() == 1);
		ci.getArtifacts().values().stream()
						 .forEach(a->assertTrue(a.getArtifactName().startsWith("Process")));
	}

	private void verifyMembers(Map<String, String> createdMembers, Map<String, String> compNameToUniqueId) {
		for (Map.Entry<String, String> entry : createdMembers.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			String comparedValue = compNameToUniqueId.get(key);

			assertEquals("compare instance ids", comparedValue, value);
		}

	}

	private static Map<String, String> prepareHeadersMap(String userId) {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		if (userId != null) {
			headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		}
		return headersMap;
	}

}
