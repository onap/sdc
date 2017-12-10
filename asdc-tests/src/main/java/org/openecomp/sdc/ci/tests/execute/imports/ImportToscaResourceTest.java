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

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_CREATED;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_INVALID_CONTENT;
import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.Decoder;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.ImportUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.api.ToscaNodeTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * 
 * @author Andrey + Pavel + Shay
 *
 */

public class ImportToscaResourceTest extends ComponentBaseTest {
	private static Logger logger = LoggerFactory.getLogger(ImportToscaResourceTest.class.getName());
	protected Utils utils = new Utils();

	public ImportToscaResourceTest() {
		super(name, ImportToscaResourceTest.class.getName());
	}

	public ImportReqDetails importReqDetails;
	protected static User sdncUserDetails;
	protected static User testerUser;
	protected String testResourcesPath;
	protected ResourceReqDetails resourceDetails;
	private int actualNumOfReqOrCap;

	@Rule
	public static TestName name = new TestName();

	@BeforeMethod
	public void before() throws Exception {
		importReqDetails = ElementFactory.getDefaultImportResource();
		sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		testerUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		resourceDetails = ElementFactory.getDefaultResource();
		String sourceDir = config.getResourceConfigDir();
		final String workDir = "importToscaResourceByCreateUrl";
		testResourcesPath = sourceDir + File.separator + workDir;
		actualNumOfReqOrCap = 0;
	}

	@DataProvider
	private static final Object[][] getYmlWithInValidListProperties() throws IOException, Exception {
		return new Object[][] { { "ListPropertyFalure02.yml", "[false,\"truee\"]", "boolean" },
				{ "ListPropertyFalure03.yml", "[false,3]", "boolean" },
				{ "ListPropertyFalure04.yml", "[false,3.56]", "boolean" },
				{ "ListPropertyFalure05.yml", "[10000,3.56]", "integer" },
				{ "ListPropertyFalure06.yml", "[10000,\"aaaa\"]", "integer" },
				{ "ListPropertyFalure07.yml", "[10000,true]", "integer" },
				{ "ListPropertyFalure08.yml", "[10.5,true]", "float" },
				{ "ListPropertyFalure09.yml", "[10.5,\"asdc\"]", "float" }, // type
																			// float
				{ "ListPropertyFalure11.yml", "[10.5,\"500.0@\"]", "float" }, // property
																				// list
																				// float
																				// type
																				// contain
																				// @
																				// in
																				// default
																				// value
				{ "ListPropertyFalure12.yml", "[10000,\"3#\"]", "integer" }, // property
																				// list
																				// integer
																				// type
																				// contain
																				// #
																				// in
																				// default
																				// value
				{ "ListPropertyFalure13.yml", "[false,\"true%\"]", "boolean" }, // property
																				// list
																				// boolean
																				// type
																				// contain
																				// %
																				// in
																				// default
																				// value
				{ "ListPropertyFalure14.yml", "[false,\"falsee\",true]", "boolean" },
				{ "ListPropertyFalure15.yml", "[10.5,\"10.6x\",20.5,30.5]", "float" } // float
																						// with
																						// value
																						// 10.6x
																						// instead
																						// 10.6f

		};
	}

	@DataProvider
	private static final Object[][] getYmlWithInValidMapProperties() throws IOException, Exception {
		return new Object[][] { { "MapPropertyFalure02.yml", "[false,\"truee\"]", "boolean" },
				{ "MapPropertyFalure03.yml", "[false,3]", "boolean" },
				{ "MapPropertyFalure04.yml", "[false,3.56]", "boolean" },
				{ "MapPropertyFalure05.yml", "[10000,3.56]", "integer" },
				{ "MapPropertyFalure06.yml", "[10000,\"aaaa\"]", "integer" },
				{ "MapPropertyFalure07.yml", "[10000,true]", "integer" },
				{ "MapPropertyFalure08.yml", "[10.5,true]", "float" },
				{ "MapPropertyFalure09.yml", "[10.5,\"asdc\"]", "float" }, // type
																			// float
				{ "MapPropertyFalure11.yml", "[10.5,\"500.0@\"]", "float" }, // property
																				// list
																				// float
																				// type
																				// contain
																				// @
																				// in
																				// default
																				// value
				{ "MapPropertyFalure12.yml", "[10000,\"3#\"]", "integer" }, // property
																			// list
																			// integer
																			// type
																			// contain
																			// #
																			// in
																			// default
																			// value
				{ "MapPropertyFalure13.yml", "[false,\"true%\"]", "boolean" }, // property
																				// list
																				// boolean
																				// type
																				// contain
																				// %
																				// in
																				// default
																				// value
				{ "MapPropertyFalure14.yml", "[false,\"falsee\",true]", "boolean" },
				{ "MapPropertyFalure15.yml", "[10.5,\"10.6x\",20.5,30.5]", "float" } // float
																						// with
																						// value
																						// 10.6x
																						// instead
																						// 10.6f

		};
	}

	@DataProvider
	private static final Object[][] getYmlWithInValidOccurrences() throws IOException, Exception {
		return new Object[][] { { "occurencyFalure01.yml" }, // requirements [2
																// , 0]
				{ "occurencyFalure02.yml" }, // requirements [-1, 2]
				{ "occurencyFalure03.yml" }, // requirements [1 ,-2]
				{ "occurencyFalure05.yml" }, // requirements MAX occurrences not
												// exist [ 1 , ]
				{ "occurencyFalure06.yml" }, // requirements [ 0 , 0 ]
				{ "occurencyFalure08.yml" }, // requirements [ 1.0 , 2.0 ]
				{ "occurencyFalure09.yml" }, // requirements [ "1" , "2" ]
				{ "occurencyFalure10.yml" }, // requirements [ ]
				{ "occurencyFalure11.yml" }, // requirements [ UNBOUNDED ,
												// UNBOUNDED ]
				{ "occurencyFalure31.yml" }, // capability [ 2, 1]
				{ "occurencyFalure32.yml" }, // capability [-1, 2]
				{ "occurencyFalure33.yml" }, // capability [1, -2]
				{ "occurencyFalure35.yml" }, // capability MAX occurrences not
												// exist [ 1 , ]
				{ "occurencyFalure36.yml" }, // capability [ 0 , 0 ]
				{ "occurencyFalure38.yml" }, // capability [ 1.0 , 2.0 ]
				{ "occurencyFalure39.yml" }, // capability [ "1" , "2" ]
				{ "occurencyFalure40.yml" }, // capability [ ]
				{ "occurencyFalure41.yml" } // capability [ UNBOUNDED ,
											// UNBOUNDED ]
		};
	}

	@DataProvider
	private static final Object[][] getInvalidYmlWithOccurrences() throws IOException, Exception {
		return new Object[][] { { "occurencyFalure04.yml" }, // requirements MIN
																// occurrences
																// not exist [ ,
																// 1]
				{ "occurencyFalure07.yml" }, // requirements [ @ , 1 ]
				{ "occurencyFalure34.yml" }, // capability MIN occurrences not
												// exist [ , 1]
				{ "occurencyFalure37.yml" } // capability [ 0 , # ]

		};
	}

	// US656928
	protected final String importMapPropertySuccess = "importMapPropertySuccessFlow.yml";
	protected final String importAttributeSuccess = "importAttributeSuccessFlow.yml";
	protected final String importSuccessFile = "myCompute.yml";
	protected final String derivedFromMyCompute = "derivedFromMyCompute.yml";
	protected final String importSuccessVFFile = "myComputeVF.yml";
	protected final String importNoDerivedFromFile = "myComputeDerivedFromNotExists.yml";
	protected final String importInvalidDefinitionVersionFile = "myComputeIncorrectDefenitionVersionValue.yml";
	protected final String importIncorrectNameSpaceFormatFile = "myComputeIncorrectNameSpaceFormat.yml";
	protected final String importNoDefenitionVersionFile = "myComputeNoDefenitionVersion.yml";
	protected final String importNodeTypesTwiceFile = "myComputeWithNodeTypesTwice.yml";
	protected final String importTopologyTemplateFile = "myComputeWithTopologyTemplate.yml";
	protected final String importNoContentFile = "noContent.yml";
	protected final String importWithOccurrences = "myComputeOccurencySuccess.yml";
	protected final String importListPropertyBadDefault = "importListPropertyBadDefault.yml";
	protected final String importListPropertyGoodDefault = "importListPropertyGoodDefault.yml";
	protected final String importListPropertySuccess = "importListPropertySuccessFlow.yml";
	// US631462
	protected final String importDuplicateRequirements = "importDuplicateRequirements.yml";
	protected final String importDuplicateCapability = "importDuplicateCapability.yml";
	protected final String importCapabilityNameExistsOnParent = "importCapabilityNameExistsOnParent.yml";
	protected final String importRequirementNameExistsOnParent = "importRequirementNameExistsOnParent.yml";
	protected final String importToscaResourceReqCapDerivedFromParent = "derivedFromWebAppDerivedReqCap.yml";
	protected final String missingCapInReqDef = "missingCapInReqDefinition.yml";
	protected final String missingCapInCapDef = "missingCapInCapDefinition.yml";

	// US558432 - Support for Capability/Requirement "occurences" Import
	@Test(dataProvider = "getYmlWithInValidOccurrences")
	public void importToscaResourceWithOccurrencesFailuresFlow01(String ymlFileWithInvalidCapReqOccurrences)
			throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				ymlFileWithInvalidCapReqOccurrences);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_OCCURRENCES.name(), new ArrayList<String>(),
				importResourceResponse.getResponse());
	}

	@Test(dataProvider = "getInvalidYmlWithOccurrences")
	public void importToscaResourceWithOccurrencesFailuresFlow02(String ymlFileWithInvalidCapReqOccurrences)
			throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				ymlFileWithInvalidCapReqOccurrences);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_YAML_FILE.name(), new ArrayList<String>(),
				importResourceResponse.getResponse());
	}

	@Test
	public void importToscaResource() throws Exception {

		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importSuccessFile);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());
		AssertJUnit.assertTrue("response code is not 201, returned :" + importResourceResponse.getErrorCode(),
				importResourceResponse.getErrorCode() == 201);
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		AssertJUnit.assertTrue("validate toscaResourceName field",
				resourceJavaObject.getToscaResourceName().equals(parseToscaNodeYaml.getNodeName()));
		AssertJUnit.assertTrue("validate resourceType field",
				resourceJavaObject.getResourceType().equals(ResourceTypeEnum.VFC));
		// find derived from resource details
		// Validate resource details after import-create resource including
		// capabilities, interfaces from derived_from resource

		// Validate audit message
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceWithOccurrencesSuccessFlow() throws Exception {

		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importWithOccurrences);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());
		AssertJUnit.assertTrue("response code is not 201, returned :" + importResourceResponse.getErrorCode(),
				importResourceResponse.getErrorCode() == 201);
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		AssertJUnit.assertTrue("validate toscaResourceName field",
				resourceJavaObject.getToscaResourceName().equals(parseToscaNodeYaml.getNodeName()));
		AssertJUnit.assertTrue("validate resourceType field",
				resourceJavaObject.getResourceType().equals(ResourceTypeEnum.VFC));
		String requirementsType = "tosca.capabilities.Attachment";
		String capabilitType = "tosca.capabilities.Endpoint.Admin";
		// Verify Occurrences of requirements and capabilities in resource
		verifyRequirementsOccurrences(resourceJavaObject, requirementsType);
		verifyCapabilitiesOccurrences(resourceJavaObject, capabilitType);
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	// ------------------------------Success---------------------------------

	@Test(enabled = false)
	public void importToscaResourceVFResType() throws Exception {

		String resourceType = ResourceTypeEnum.VF.toString();

		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importSuccessVFFile);
		// importReqDetails.setResourceType(resourceType);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());
		assertTrue("response code is not 201, returned :" + importResourceResponse.getErrorCode(),
				importResourceResponse.getErrorCode() == 201);
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		assertTrue("validate toscaResourceName field",
				resourceJavaObject.getToscaResourceName().equals(parseToscaNodeYaml.getNodeName()));
		assertTrue(
				"validate resourceType field, expected - " + resourceType + ", actual - "
						+ resourceJavaObject.getResourceType(),
				resourceJavaObject.getResourceType().toString().equals(resourceType));

		// Validate audit message
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	// ------------------------------Failure---------------------------------

	@Test
	public void importToscaResourceDerivedFromNotExist() throws Exception {

		String fileName = importNoDerivedFromFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		// List<String> derivedFrom = new ArrayList<String>() ;
		// derivedFrom.add("hh");
		// importReqDetails.setDerivedFrom(derivedFrom);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.PARENT_RESOURCE_NOT_FOUND.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PARENT_RESOURCE_NOT_FOUND.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceIncorrectDefinitionVersion() throws Exception {

		String fileName = importInvalidDefinitionVersionFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_TOSCA_TEMPLATE.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_TOSCA_TEMPLATE.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceIncorrectSpaceNameFormat() throws Exception {

		String fileName = importIncorrectNameSpaceFormatFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_RESOURCE_NAMESPACE.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_RESOURCE_NAMESPACE.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceNoDefinitionVersion() throws Exception {

		String fileName = importNoDefenitionVersionFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_TOSCA_TEMPLATE.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_TOSCA_TEMPLATE.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceNoContent() throws Exception {

		String fileName = importNoContentFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_RESOURCE_PAYLOAD.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_RESOURCE_PAYLOAD.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceWithTopologyTemplate() throws Exception {

		String fileName = importTopologyTemplateFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.NOT_RESOURCE_TOSCA_TEMPLATE.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_RESOURCE_TOSCA_TEMPLATE.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceWithNodeTypesTwice() throws Exception {

		String fileName = importNodeTypesTwiceFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.NOT_SINGLE_RESOURCE.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_SINGLE_RESOURCE.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	// failed case - uniqueness of toscaResourceName - RESOURCE_ALREADY_EXISTS
	@Test
	public void importToscaResourceTwice() throws Exception {
		String fileName = importSuccessFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());
		assertTrue("response code is not 201, returned :" + importResourceResponse.getErrorCode(),
				importResourceResponse.getErrorCode() == 201);
		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		RestResponse checkInresponse = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertTrue("checkIn resource request returned status:" + checkInresponse.getErrorCode(),
				checkInresponse.getErrorCode() == 200);

		// Validate audit message
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		expectedResourceAuditJavaObject.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);

		// import the same tosca resource with different resourceName
		DbUtils.cleanAllAudits();

		importReqDetails.setName("kuku");
		List<String> tags = new ArrayList<String>();
		tags.add(importReqDetails.getName());
		importReqDetails.setTags(tags);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_ALREADY_EXISTS.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_ALREADY_EXISTS.name(), variables,
				importResourceResponse.getResponse());

		expectedResourceAuditJavaObject = ElementFactory.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType(importReqDetails.getToscaResourceName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);

	}

	@Test
	public void importToscaResourceWithTheSameNameAsCreatedResourceBefore() throws Exception {

		// create resource
		String fileName = importSuccessFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);

		resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setName(importReqDetails.getName());

		RestResponse response = ResourceRestUtils.createResource(resourceDetails, sdncUserDetails);
		int status = response.getErrorCode();
		assertEquals("create request returned status:" + status, 201, status);
		assertNotNull("resource uniqueId is null:", resourceDetails.getUniqueId());
		Resource resourceJavaObject = ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
		// assertNull("validate toscaResourceName field",
		// resourceJavaObject.getToscaResourceName());

		// import the same tosca resource
		DbUtils.cleanAllAudits();
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_ALREADY_EXISTS.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_ALREADY_EXISTS.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);

	}

	@Test
	public void importToscaResourceInvalidChecksum() throws Exception {
		String fileName = importSuccessFile;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), "invalidMd5Sum");

		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				headersMap);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		assertNotNull("check response object is not null after import tosca resource", importResourceResponse);
		assertNotNull("check error code exists in response after import tosca resource",
				importResourceResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_RESOURCE_CHECKSUM.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_RESOURCE_CHECKSUM.name(), variables,
				importResourceResponse.getResponse());

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceInvalidResType() throws Exception {

		String resourceType = "invalidResourceType";

		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importSuccessFile);
		importReqDetails.setResourceType(resourceType);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_CONTENT.name());
		assertNotNull("check response object is not null after import resouce", importResourceResponse);
		assertNotNull("check error code exists in response after import resource",
				importResourceResponse.getErrorCode());
		assertEquals("Check response code after import resource", errorInfo.getCode(),
				importResourceResponse.getErrorCode());

		List<String> variables = new ArrayList<>();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), variables,
				importResourceResponse.getResponse());

		// Validate audit message
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void derivedTemplateImportedSecondResourceAsFirstImportedNodeType() throws Exception {

		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importSuccessFile);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());
		assertTrue("response code is not 201, returned :" + importResourceResponse.getErrorCode(),
				importResourceResponse.getErrorCode() == 201);
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		assertTrue("validate toscaResourceName field",
				resourceJavaObject.getToscaResourceName().equals(parseToscaNodeYaml.getNodeName()));
		assertTrue(
				"validate resourceType field, expected - " + importReqDetails.getResourceType() + ", actual - "
						+ resourceJavaObject.getResourceType(),
				resourceJavaObject.getResourceType().toString().equals(importReqDetails.getResourceType()));

		// Validate audit message
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);

		RestResponse certifyResource = LifecycleRestUtils.certifyResource(importReqDetails);
		assertTrue("certify resource request returned status:" + certifyResource.getErrorCode(),
				certifyResource.getErrorCode() == 200);

		// import second resource template derived from first resource
		DbUtils.cleanAllAudits();
		importReqDetails.setName("kuku");
		List<String> tags = new ArrayList<String>();
		tags.add(importReqDetails.getName());
		importReqDetails.setTags(tags);
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				derivedFromMyCompute);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		assertTrue("response code is not 201, returned :" + importResourceResponse.getErrorCode(),
				importResourceResponse.getErrorCode() == 201);
		parseToscaNodeYaml = utils.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		Resource resourceJavaObject2 = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		assertTrue("validate toscaResourceName field",
				resourceJavaObject2.getToscaResourceName().equals(parseToscaNodeYaml.getNodeName()));
		assertTrue(
				"validate resourceType field, expected - " + importReqDetails.getResourceType() + ", actual - "
						+ resourceJavaObject2.getResourceType(),
				resourceJavaObject2.getResourceType().toString().equals(importReqDetails.getResourceType()));

		// Validate audit message
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject2 = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject2.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject2.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject2.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject2.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject2,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);

	}

	@Test
	public void importToscaResourceListPropertyGoodDefault() throws Exception {

		String fileName = importListPropertyGoodDefault;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		assertTrue("response code is not 201, returned :" + importResourceResponse.getErrorCode(),
				importResourceResponse.getErrorCode() == 201);

		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		assertTrue("Properties size : " + resourceJavaObject.getProperties().size(),
				resourceJavaObject.getProperties().size() == 1);
		assertTrue("Property type : " + resourceJavaObject.getProperties().get(0).getType(),
				resourceJavaObject.getProperties().get(0).getType().equals(ToscaPropertyType.LIST.getType()));
		assertTrue(
				"actual Default values  : " + resourceJavaObject.getProperties().get(0).getDefaultValue()
						+ " , expected : " + "[false, true]",
				resourceJavaObject.getProperties().get(0).getDefaultValue().equals("[\"false\",\"true\"]"));

	}

	@Test
	public void importToscaResourceListPropertyBadDefault() throws Exception {

		String fileName = importListPropertyBadDefault;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE.name());
		assertEquals("Check response code after tosca resource import", errorInfo.getCode(),
				importResourceResponse.getErrorCode());
		ArrayList<String> variables = new ArrayList<>();
		variables.add("my_prop");
		variables.add("list");
		variables.add("boolean");
		variables.add("[12,true]");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE.name(), variables,
				importResourceResponse.getResponse());

	}

	// Benny US580744 - Add support for TOSCA "list" type - import

	@Test
	public void importToscaResourceListPropertySuccessFlow() throws Exception {
		String fileName = importListPropertySuccess;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		ResourceRestUtils.checkCreateResponse(importResourceResponse);
		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		// Verify Properties List in resource
		verifyResourcePropertiesList(resourceJavaObject);
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	// DE198534
	@Test(dataProvider = "getYmlWithInValidListProperties") // invalid default
															// values
	public void importToscaResourceListPropertyFailureFlows(String ymlFileWithInvalidPropertyDefualtValues,
			String defualtValues, String enterySchemaType) throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				ymlFileWithInvalidPropertyDefualtValues);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("my_property");
		variables.add("list");
		variables.add(enterySchemaType);
		variables.add(defualtValues);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE.name(), variables,
				importResourceResponse.getResponse());
	}

	// BUG DE198650
	@Test
	public void importToscaResourceListPropertyNonSupportEntrySchemaType() throws Exception {
		String ymlFile = "ListPropertyFalure01.yml";
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				ymlFile);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("booolean"); // property entry_schema data type
		variables.add("my_boolean");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROPERTY_INNER_TYPE.name(), variables,
				importResourceResponse.getResponse());
	}

	// BUG DE198676
	@Test // (enabled=false)
	public void importToscaResourceListPropertyNonSupportedPropertyType() throws Exception { // Not
																								// "list"
																								// type
		String ymlFile = "ListPropertyFalure16.yml";
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				ymlFile);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("koko"); // property data type (koko instead list)
		variables.add("my_boolean");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROPERTY_TYPE.name(), variables,
				importResourceResponse.getResponse());
	}

	/// US656928 - [BE] - Add support for TOSCA "map" type - Phase 1 import
	@Test
	public void importToscaResourceMapPropertySuccessFlow() throws Exception {
		String fileName = importMapPropertySuccess;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		ResourceRestUtils.checkCreateResponse(importResourceResponse);
		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));
		// Verify Properties MAP in resource
		verifyResourcePropertiesMap(resourceJavaObject);
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test(dataProvider = "getYmlWithInValidMapProperties") // invalid default
															// values
	public void importToscaResourceMapPropertyFailureFlows(String ymlFileWithInvalidPropertyDefualtValues,
			String defualtValues, String enterySchemaType) throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				ymlFileWithInvalidPropertyDefualtValues);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("my_property");
		variables.add("map");
		variables.add(enterySchemaType);
		variables.add(defualtValues);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE.name(), variables,
				importResourceResponse.getResponse());
	}

	@Test
	public void importToscaResourceMaptPropertyNonSupportedPropertyType() throws Exception { // Not
																								// "Map"
																								// type
		String ymlFile = "MapPropertyFalure16.yml";
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				ymlFile);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("koko"); // property data type (koko instead list)
		variables.add("my_boolean");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROPERTY_TYPE.name(), variables,
				importResourceResponse.getResponse());
	}

	@Test
	public void importToscaResourceMissingCapabilityInReqDefinition() throws Exception {

		String fileName = missingCapInReqDef;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_CAPABILITY_TYPE.name());
		String missingCapName = "org.openecomp.capabilities.networkInterfaceNotFound";
		BaseRestUtils.checkErrorResponse(importResourceResponse, ActionStatus.MISSING_CAPABILITY_TYPE, missingCapName);

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, Arrays.asList(missingCapName));
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType("org.openecomp.resource.vSCP-03-16");
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceMissingCapabilityInCapDefinition() throws Exception {

		String fileName = missingCapInCapDef;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		logger.debug("import tosca resource response:  {}", importResourceResponse.getResponseMessage());

		// Validate audit message
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_CAPABILITY_TYPE.name());
		String missingCapName = "org.openecomp.capabilities.networkInterfaceNotFound";
		BaseRestUtils.checkErrorResponse(importResourceResponse, ActionStatus.MISSING_CAPABILITY_TYPE, missingCapName);

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, Arrays.asList(missingCapName));
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType("org.openecomp.resource.vSCP-03-16");
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceDuplicateRequirements() throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importDuplicateRequirements);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("requirement");
		variables.add("local_storage");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.IMPORT_DUPLICATE_REQ_CAP_NAME.name(), variables,
				importResourceResponse.getResponse());
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.IMPORT_DUPLICATE_REQ_CAP_NAME.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceDuplicateCapabilities() throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importDuplicateCapability);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("capability");
		variables.add("scalable");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.IMPORT_DUPLICATE_REQ_CAP_NAME.name(), variables,
				importResourceResponse.getResponse());
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.IMPORT_DUPLICATE_REQ_CAP_NAME.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceRequirementNameExistsOnParent() throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importRequirementNameExistsOnParent);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("requirement");
		variables.add("local_storage");
		variables.add("Compute");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED.name(),
				variables, importResourceResponse.getResponse());
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceCapabilityNameExistsOnParent() throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importCapabilityNameExistsOnParent);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertTrue(importResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("capability");
		variables.add("binding");
		variables.add("Compute");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED.name(),
				variables, importResourceResponse.getResponse());
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgFailure(errorInfo, variables);
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	@Test
	public void importToscaResourceReqCapDerivedFromParent() throws Exception {
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				importToscaResourceReqCapDerivedFromParent);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		BaseRestUtils.checkCreateResponse(importResourceResponse);
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType("org.openecomp.resource.MyWebApp");
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	/************************ Shay ************************/

	@Test
	public void caseRequirementInsensitiveTest() throws Exception {
		String fileName = "CaseInsensitiveReqTest_1.yml";
		int expectedNumOfRequirements = 2;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());
		importReqDetails.setRequirements(testResourcesPath, fileName, sdncUserDetails, null);
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String fileName2 = "CaseInsensitiveReqTest_2.yml";
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName2);
		importReqDetails.setName("secondImportedResource");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());
		importReqDetails.setRequirements(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails, null);
		requirements = importReqDetails.getRequirements();
		requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

		checkImportedAssetAssociated(importReqDetails);

	}

	private void checkImportedAssetAssociated(ImportReqDetails importDetails) throws IOException, Exception {
		RestResponse importResourceResponse;
		ImportReqDetails importReqDetails2 = ElementFactory.getDefaultImportResource();
		importReqDetails2 = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails2, testResourcesPath,
				"BindingAsset.yml");
		importReqDetails2.setName("bindingAsset");
		importReqDetails2.setTags(Arrays.asList(importReqDetails2.getName()));
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails2, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		ResourceReqDetails vf = ElementFactory.getDefaultResourceByType("VF100", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, sdncUserDetails.getUserId(),
				ResourceTypeEnum.VF.toString());
		RestResponse createResourceResponse = ResourceRestUtils.createResource(vf, sdncUserDetails);
		ResourceRestUtils.checkCreateResponse(createResourceResponse);

		LifecycleRestUtils.changeResourceState(importDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		LifecycleRestUtils.changeResourceState(importReqDetails2, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);

		RestResponse response = ResourceRestUtils.createResourceInstance(importDetails, sdncUserDetails,
				vf.getUniqueId());
		ResourceRestUtils.checkCreateResponse(response);
		ComponentInstance riCap = ResponseParser.parseToObject(response.getResponse(), ComponentInstance.class);

		response = ResourceRestUtils.createResourceInstance(importReqDetails2, sdncUserDetails, vf.getUniqueId());
		ResourceRestUtils.checkCreateResponse(response);
		ComponentInstance riReq = ResponseParser.parseToObject(response.getResponse(), ComponentInstance.class);

		RestResponse getResourceBeforeAssociate = ComponentRestUtils
				.getComponentRequirmentsCapabilities(sdncUserDetails, vf);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceBeforeAssociate.getResponse(), CapReqDef.class);

		String capbilityUid = capReqDef.getCapabilities().get("tosca.capabilities.network.Bindable").get(0)
				.getUniqueId();
		String requirementUid = capReqDef.getRequirements().get("tosca.capabilities.network.Bindable").get(0)
				.getUniqueId();

		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		requirementDef.setFromNode(riReq.getUniqueId());
		requirementDef.setToNode(riCap.getUniqueId());

		RelationshipInfo pair = new RelationshipInfo();
		pair.setRequirementOwnerId(riReq.getUniqueId());
		pair.setCapabilityOwnerId(riCap.getUniqueId());
		pair.setRequirement("VirtualBinding");
		RelationshipImpl relationship = new RelationshipImpl();
		relationship.setType("tosca.capabilities.network.Bindable");
		pair.setRelationships(relationship);
		pair.setCapabilityUid(capbilityUid);
		pair.setRequirementUid(requirementUid);
		CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
		capReqRel.setRelation(pair);
		List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
		relationships.add(capReqRel);
		requirementDef.setRelationships(relationships);

		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(requirementDef, sdncUserDetails,
				vf.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals("Check response code ", STATUS_CODE_SUCCESS, associateInstances.getErrorCode().intValue());
	}

	@Test
	public void caseCapabilitiesInsensitiveTest() throws Exception {
		String fileName = "CaseInsensitiveCapTest_1.yml";
		int expectedNumOfCapabilities = 6;

		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setCapabilities(testResourcesPath, fileName, sdncUserDetails, null);
		Map<String, Object> capabilities = importReqDetails.getCapabilities();
		Map<String, Object> capabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetails,
				expectedNumOfCapabilities);
		assertEquals(capabilities.keySet().size(), capabilitiesFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(capabilities, capabilitiesFromResponse);

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String fileName2 = "CaseInsensitiveCapTest_2.yml";
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName2);
		importReqDetails.setName("secondImportedResource");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setCapabilities(testResourcesPath, fileName2, sdncUserDetails, null);
		capabilities = importReqDetails.getCapabilities();
		capabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetails,
				expectedNumOfCapabilities);
		assertEquals(capabilities.keySet().size(), capabilitiesFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(capabilities, capabilitiesFromResponse);

	}

	@Test
	public void fatherAndChildHaveDifferentRequirementsTest() throws Exception {
		String fileName = "DifferentReqFromCompute.yml";
		int expectedNumOfRequirements = 3;

		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setRequirements(testResourcesPath, fileName, sdncUserDetails, "Compute");
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

		checkImportedAssetAssociated(importReqDetails);
	}

	@Test
	public void fatherHasNoRequirementsTest() throws Exception {
		String fatherFileName = "CPHasNoReqCap.yml";
		String childFileName = "DerivedFromCPWithOwnReq.yml";
		int expectedNumOfRequirements = 3;

		importReqDetails.setName("father");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String derivedFromResourceName = importReqDetails.getName();
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setRequirements(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromResourceName);
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

	}

	@Test
	public void childHasSameReqNameAndTypeLikeFatherTest() throws Exception {
		String childFileName = "SameReqAsCompute.yml";
		int expectedNumOfRequirements = 2;

		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setRequirements(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails, null);
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);
	}

	@Test
	public void childHasSameCapNameAndTypeLikeFatherTest() throws Exception {
		String childFileName = "SameCapAsCompute.yml";
		int expectedNumOfCapabilities = 6;

		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setCapabilities(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				"Compute");
		Map<String, Object> capabilities = importReqDetails.getCapabilities();
		Map<String, Object> capabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetails,
				expectedNumOfCapabilities);
		assertEquals(capabilities.keySet().size(), capabilitiesFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(capabilities, capabilitiesFromResponse);
	}

	@Test
	public void childGetsAllRequirementsOfFatherAndGrandfatherTest() throws Exception {
		int expectedNumOfRequirements = 4;

		String fatherFileName = "DifferentReqFromCompute.yml";
		importReqDetails.setName("father");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String derivedFromName = importReqDetails.getName();
		String childFileName = "DifferentReqCapFromCompute1.yml";
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setRequirements(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromName);
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

	}

	@Test
	public void childOverridesGrandfatherRequirementsTest() throws Exception {
		int expectedNumOfRequirements = 3;

		String fatherFileName = "DifferentReqFromCompute.yml";
		importReqDetails.setName("father");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String derivedFromName = importReqDetails.getName();
		String childFileName = "SameReqAsCompute_DerivedFromMyCompute1.yml";
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setRequirements(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromName);
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);
	}

	@Test
	public void childAndGrandfatherHaveDifferenetReqiurementTypeTest() throws Exception {
		int expectedNumOfRequirements = 3;
		int expectedNumOfCapabilities = 6;

		String fatherName = "father";
		String fatherFileName = "DifferentReqFromCompute.yml";
		importReqDetails.setName(fatherName);
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String fatherUniqueId = importReqDetails.getUniqueId();
		ImportReqDetails importReqDetailsFather = importReqDetails;

		String childFileName = "importRequirementNameExistsOnParent_DerivedFromMyCompute1.yml";
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_INVALID_CONTENT, importResourceResponse.getErrorCode().intValue());
		ArrayList<String> variables = new ArrayList<>();
		variables.add("requirement");
		variables.add("local_storage");
		variables.add(fatherName);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED.name(),
				variables, importResourceResponse.getResponse());

		importReqDetails.setUniqueId(fatherUniqueId);

		importReqDetailsFather.setRequirements(testResourcesPath, fatherFileName, sdncUserDetails, "Compute");
		Map<String, Object> requirements = importReqDetailsFather.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetailsFather,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetailsFather.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

		importReqDetailsFather.setCapabilities(testResourcesPath, fatherFileName, sdncUserDetails, "Compute");
		Map<String, Object> capabilities = importReqDetailsFather.getCapabilities();
		Map<String, Object> capabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetailsFather,
				expectedNumOfCapabilities);
		assertEquals(capabilities.keySet().size(), capabilitiesFromResponse.keySet().size());
		importReqDetailsFather.compareRequirementsOrCapabilities(capabilities, capabilitiesFromResponse);
	}

	@Test
	public void childHasNoReqCapTest() throws Exception {
		int expectedNumOfRequirements = 3;
		int expectedNumOfCapabilities = 6;

		String fatherFileName = "DifferentReqFromCompute.yml";
		importReqDetails.setName("father");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String derivedFromName = importReqDetails.getName();
		String childFileName = "CPHasNoReqCap_DerivedFromMyCompute1.yml";
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setRequirements(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromName);
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

		importReqDetails.setCapabilities(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromName);
		Map<String, Object> capabilities = importReqDetails.getCapabilities();
		Map<String, Object> capabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetails,
				expectedNumOfCapabilities);
		assertEquals(capabilities.keySet().size(), capabilitiesFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(capabilities, capabilitiesFromResponse);
	}

	@Test
	public void fatherAndChildGetReqCapFromGrandfatherTest() throws Exception {
		int expectedNumOfRequirements = 2;
		int expectedNumOfCapabilities = 6;

		String fatherFileName = "MyFatherCompute_NoReqCap.yml";
		importReqDetails.setName("father");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String derivedFromName = importReqDetails.getName();
		String childFileName = "myChildCompute_NoReqCap.yml";
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setRequirements(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromName);
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

		importReqDetails.setCapabilities(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromName);
		Map<String, Object> capabilities = importReqDetails.getCapabilities();
		Map<String, Object> capabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetails,
				expectedNumOfCapabilities);
		assertEquals(capabilities.keySet().size(), capabilitiesFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(capabilities, capabilitiesFromResponse);
	}

	@Test
	public void reverseInheritanceTest() throws Exception {
		int expectedNumOfRequirements = 2;
		int expectedNumOfCapabilities = 2;

		String fatherName = "father";
		String fatherFileName = "myFatherWebApp_derviedFromDocker.yml";
		importReqDetails.setName(fatherName);
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String fatherUniqueId = importReqDetails.getUniqueId();
		ImportReqDetails importReqDetailsFather = importReqDetails;
		String childFileName = "myChildWebApp_DerivedFromContainer.yml";
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_INVALID_CONTENT, importResourceResponse.getErrorCode().intValue());
		ArrayList<String> variables = new ArrayList<>();
		variables.add("requirement");
		variables.add("host");
		variables.add(fatherName);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.IMPORT_REQ_CAP_NAME_EXISTS_IN_DERIVED.name(),
				variables, importResourceResponse.getResponse());

		importReqDetails.setUniqueId(fatherUniqueId);
		importReqDetailsFather.setRequirements(testResourcesPath, fatherFileName, sdncUserDetails, "Root");
		Map<String, Object> requirements = importReqDetailsFather.getRequirements();
		Map<String, Object> requirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetailsFather,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), requirementsFromResponse.keySet().size());
		importReqDetailsFather.compareRequirementsOrCapabilities(requirements, requirementsFromResponse);

		importReqDetailsFather.setCapabilities(testResourcesPath, fatherFileName, sdncUserDetails, "Root");
		Map<String, Object> capabilities = importReqDetailsFather.getCapabilities();
		Map<String, Object> capabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetailsFather,
				expectedNumOfCapabilities);
		assertEquals(capabilities.keySet().size(), capabilitiesFromResponse.keySet().size());
		importReqDetailsFather.compareRequirementsOrCapabilities(capabilities, capabilitiesFromResponse);
	}

	// DE202329
	@Test(enabled = false)
	public void requirementWithMissingTypeTest() throws Exception {
		String fatherName = "father";
		String fatherFileName = "DerivedFromWebApplication_HasNoReqType.yml";
		importReqDetails.setName(fatherName);
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_INVALID_CONTENT, importResourceResponse.getErrorCode().intValue());
		ArrayList<String> variables = new ArrayList<>();
		variables.add("diff");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_CAPABILITY_TYPE.name(), variables,
				importResourceResponse.getResponse());

	}

	@Test
	public void TwinBrothersHaveSameReqCapTest() throws Exception {
		int expectedNumOfRequirements = 4;
		int expectedNumOfCapabilities = 7;

		String derivedFromName = "father";
		String fatherFileName = "DifferentReqFromCompute.yml";
		importReqDetails.setName(derivedFromName);
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fatherFileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		RestResponse changeResourceState1 = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState1.getErrorCode().intValue());
		RestResponse changeResourceState2 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState2.getErrorCode().intValue());
		RestResponse changeResourceState3 = LifecycleRestUtils.changeResourceState(importReqDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState3.getErrorCode().intValue());

		String childFileName = "DifferentReqCapFromCompute1.yml";
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("child");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				childFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		Map<String, Object> childRequirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		Map<String, Object> childCapabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetails,
				expectedNumOfCapabilities - 1);

		String twinFileName = "DifferentReqCapFromCompute2.yml";
		importReqDetails = ElementFactory.getDefaultImportResource();
		importReqDetails.setName("twin");
		importReqDetails.setTags(Arrays.asList(importReqDetails.getName()));
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				twinFileName);
		importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails, null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());

		importReqDetails.setRequirements(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromName);
		Map<String, Object> requirements = importReqDetails.getRequirements();
		Map<String, Object> twinRequirementsFromResponse = parseReqOrCapFromResponse("requirements", importReqDetails,
				expectedNumOfRequirements);
		assertEquals(requirements.keySet().size(), twinRequirementsFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(requirements, twinRequirementsFromResponse);

		importReqDetails.setCapabilities(testResourcesPath, importReqDetails.getPayloadName(), sdncUserDetails,
				derivedFromName);
		Map<String, Object> capabilities = importReqDetails.getCapabilities();
		Map<String, Object> twinCapabilitiesFromResponse = parseReqOrCapFromResponse("capabilities", importReqDetails,
				expectedNumOfCapabilities);
		assertEquals(capabilities.keySet().size(), twinCapabilitiesFromResponse.keySet().size());
		importReqDetails.compareRequirementsOrCapabilities(capabilities, twinCapabilitiesFromResponse);

		assertEquals(childRequirementsFromResponse.keySet().size(), twinRequirementsFromResponse.keySet().size());
		assertEquals(childCapabilitiesFromResponse.keySet().size(), twinCapabilitiesFromResponse.keySet().size());
	}

	/*
	 * invariantUUID - US672129
	 */

	private void checkInvariantUuidIsImmutableInDifferentAction(ImportReqDetails importReqDetails) throws Exception {
		// create resource
		importReqDetails.setName("import");
		String invariantUuidDefinedByUser = "abcd1234";
		RestResponse importResourceResponse = importResourceWithRequestedInvariantUuid(importReqDetails,
				invariantUuidDefinedByUser);
		String invariantUUIDcreation = ResponseParser.getInvariantUuid(importResourceResponse);
		assertFalse(checkInvariantUuidEqual(invariantUuidDefinedByUser, importResourceResponse));

		// get resource
		RestResponse getResource = ResourceRestUtils.getResource(importReqDetails.getUniqueId());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, getResource));

		// checkin resource
		RestResponse changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// checkout resource
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// checkin resource
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// checkout resource
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// checkin resource
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKIN);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// certification request
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// start certification
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// certify
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, testerUser,
				LifeCycleStatesEnum.CERTIFY);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));
		String certifiedUniqueId = importReqDetails.getUniqueId();

		// update resource
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ResourceReqDetails updatedResourceReqDetails = new ResourceReqDetails(importReqDetails,
				importReqDetails.getVersion());
		updatedResourceReqDetails.setDescription("updatedDescription");
		updatedResourceReqDetails.setVendorRelease("1.2.3.4");
		RestResponse updateResponse = ResourceRestUtils.updateResourceMetadata(updatedResourceReqDetails,
				sdncUserDetails, importReqDetails.getUniqueId());
		assertEquals(STATUS_CODE_SUCCESS, updateResponse.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, updateResponse));

		// certification request
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// checkout resource
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// certification request
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// start certification
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// cancel certification
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, testerUser,
				LifeCycleStatesEnum.CANCELCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// start certification
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// failure
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, testerUser,
				LifeCycleStatesEnum.FAILCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// upload artifact
		changeResourceState = LifecycleRestUtils.changeResourceState(importReqDetails, sdncUserDetails,
				LifeCycleStatesEnum.CHECKOUT);
		ArtifactReqDetails artifactDetails = ElementFactory.getDefaultArtifact();
		ArtifactRestUtils.addInformationalArtifactToResource(artifactDetails, sdncUserDetails,
				importReqDetails.getUniqueId());
		assertEquals(STATUS_CODE_SUCCESS, changeResourceState.getErrorCode().intValue());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, changeResourceState));

		// create instance
		resourceDetails.setResourceType(ResourceTypeEnum.VF.toString());
		ResourceRestUtils.createResource(resourceDetails, sdncUserDetails);
		importReqDetails.setUniqueId(certifiedUniqueId);
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(importReqDetails);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncUserDetails, resourceDetails.getUniqueId(), ComponentTypeEnum.RESOURCE);
		assertEquals(STATUS_CODE_CREATED, createResourceInstanceResponse.getErrorCode().intValue());
		getResource = ResourceRestUtils.getResource(importReqDetails.getUniqueId());
		assertTrue(checkInvariantUuidEqual(invariantUUIDcreation, getResource));
	}

	private boolean checkInvariantUuidEqual(String expectedInvariantUuid, RestResponse response) {
		String invariantUUIDFromResponse = ResponseParser.getInvariantUuid(response);
		return expectedInvariantUuid.equals(invariantUUIDFromResponse);
	}

	@Test
	public void checkCPHasImmutableInvariantUuidTest() throws Exception {
		String filename = "FatherHasNoReqCap.yml";
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				filename);
		checkResourceHasImmutableInvariantUuidTest(importReqDetails);
	}

	@Test
	public void checkVFCHasImmutableInvariantUuidTest() throws Exception {
		String filename = "computeCap11.yml";
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				filename);
		checkResourceHasImmutableInvariantUuidTest(importReqDetails);
	}

	public void checkResourceHasImmutableInvariantUuidTest(ImportReqDetails importReqDetails) throws Exception {
		// invariantUuid is null
		importReqDetails.setName("first");
		RestResponse importResourceResponse = importResourceWithRequestedInvariantUuid(importReqDetails, null);
		String invariantUUIDcreation = ResponseParser.getInvariantUuid(importResourceResponse);
		assertNotNull(invariantUUIDcreation);

		ResourceRestUtils.deleteResource(importReqDetails.getUniqueId(), sdncUserDetails.getUserId());

		// invariantUuid is empty
		importReqDetails.setName("second");
		String invariantUuidDefinedByUser = "";
		importResourceResponse = importResourceWithRequestedInvariantUuid(importReqDetails, invariantUuidDefinedByUser);
		invariantUUIDcreation = ResponseParser.getInvariantUuid(importResourceResponse);
		assertNotNull(invariantUUIDcreation);

		ResourceRestUtils.deleteResource(importReqDetails.getUniqueId(), sdncUserDetails.getUserId());

		checkInvariantUuidIsImmutableInDifferentAction(importReqDetails);
	}

	private static RestResponse importResourceWithRequestedInvariantUuid(ImportReqDetails importDetails,
			String invariantUuid) throws Exception {
		importDetails.setInvariantUUID(invariantUuid);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importDetails, sdncUserDetails,
				null);
		assertEquals(STATUS_CODE_CREATED, importResourceResponse.getErrorCode().intValue());
		return importResourceResponse;
	}

	private Map<String, Object> parseReqOrCapFromResponse(String parsedFieldName, ImportReqDetails importReqDetails,
			int expectedNumOfReqCap) throws ClientProtocolException, IOException {
		RestResponse getResource = ResourceRestUtils.getResource(importReqDetails.getUniqueId());
		assertTrue(getResource.getErrorCode().equals(STATUS_CODE_SUCCESS));
		Map<String, Object> parsedFieldFromResponseToMap = ResponseParser.getJsonValueAsMap(getResource,
				parsedFieldName);
		Iterator<String> iterator = parsedFieldFromResponseToMap.keySet().iterator();
		actualNumOfReqOrCap = 0;
		while (iterator.hasNext()) {
			String next = iterator.next();
			List<Object> object = (List<Object>) parsedFieldFromResponseToMap.get(next);
			actualNumOfReqOrCap += object.size();
		}
		assertEquals(expectedNumOfReqCap, actualNumOfReqOrCap);
		return parsedFieldFromResponseToMap;
	}

	// ---------------------------------

	private void verifyResourcePropertiesList(Resource resourceJavaObject) { // use
																				// importListPropertySuccessFlow.yml
		boolean isPropertyAppear = false;
		List<PropertyDefinition> propertiesList = resourceJavaObject.getProperties();
		for (PropertyDefinition pro : propertiesList) {
			switch (pro.getName()) {
			case "my_boolean":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[false,true]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "my_boolean_array":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[true,false]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "duplicate_boolean_values":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[true,false,true]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_values_Insensitive":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[true,false,true]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "my_integers":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[0,1000,-1000,50]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "my_integers_array":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[10,-1000,0]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "duplicate_integers_values":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[10,10,-1000,0]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "my_string":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("[\"asdc\",\"$?^@ecomp$!#%()_-~@+*^...;;/w#\",\"uc\"]"));
				// assertTrue("Check Property default values ",
				// pro.getDefaultValue().equals("[\"asdc\",\"@=~!@#$%^&*()_+=?><:-w\",\"uc\"]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "my_string_array":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("[\"AAA\",\"~$~#bbb%^*_-\",\"qwe\",\"1.3\",\"500\",\"true\"]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "duplicate_string_values":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("[\"asdc\",\"asdc\",\"uc\"]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_null_value":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[\"asdc\",\"uc\"]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_space_value":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[\"asdc\",\"uc\"]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_array_null_value":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("[\"aaa\",\"bbb\",\"500\"]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "my_float":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[6,1000.000001,-3.0]"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "my_float_array":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[0.01,-5.0,2.1]"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "duplicate_float_values":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[0.0,0.0,4.555555]"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_no_default_values":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertEquals("Check Property  default values ", pro.getDefaultValue(), null);
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "integer_no_default_values":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertEquals("Check Property  default values ", pro.getDefaultValue(), null);
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "string_no_default_values":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertEquals("Check Property  default values ", pro.getDefaultValue(), null);
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "boolean_no_default_values":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertEquals("Check Property  default values ", pro.getDefaultValue(), null);
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "integer_null_value":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[1000,2000]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "boolean_null_value":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[true,false]"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "float_null_value":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[6,-3.0]"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_space_value":
				assertTrue("Check Property Type ", pro.getType().equals("list"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("[6,-3.0]"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;

			}
			assertTrue(isPropertyAppear);
			isPropertyAppear = false;
		}

	}

	private void verifyRequirementsOccurrences(Resource resourceJavaObject, String requirementsType) {
		boolean isRequirementAppear = false;
		// List<RequirementDefinition> requerments =
		// resourceJavaObject.getRequirements().get("tosca.capabilities.Attachment");
		List<RequirementDefinition> requerments = resourceJavaObject.getRequirements().get(requirementsType);

		for (RequirementDefinition req : requerments) {
			switch (req.getName()) {
			case "local_storage100":
				assertTrue("Check Min Requirement Occurrences ", req.getMinOccurrences().equals("1"));
				assertTrue("Check Max Requirement Occurrences ", req.getMaxOccurrences().equals("UNBOUNDED"));
				isRequirementAppear = true;
				break;
			case "local_storage200":
				assertTrue("Check Min Requirement Occurrences ", req.getMinOccurrences().equals("1"));
				assertTrue("Check Max Requirement Occurrences ", req.getMaxOccurrences().equals("1"));
				isRequirementAppear = true;
				break;
			case "local_storage300":
				assertTrue("Check Min Requirement Occurrences ", req.getMinOccurrences().equals("1"));
				assertTrue("Check Max Requirement Occurrences ", req.getMaxOccurrences().equals("10"));
				isRequirementAppear = true;
				break;
			case "local_storage400":
				assertTrue("Check Min Requirement Occurrences ", req.getMinOccurrences().equals("1"));
				assertTrue("Check Max Requirement Occurrences ", req.getMaxOccurrences().equals("10000000"));
				isRequirementAppear = true;
				break;
			case "local_storage500":
				assertTrue("Check Min Requirement Occurrences ", req.getMinOccurrences().equals("2"));
				assertTrue("Check Max Requirement Occurrences ", req.getMaxOccurrences().equals("3"));
				isRequirementAppear = true;
				break;
			case "local_storageNoOccurrences600":
				assertTrue("Check Min Requirement Occurrences ", req.getMinOccurrences().equals("1"));
				assertTrue("Check Max Requirement Occurrences ", req.getMaxOccurrences().equals("1"));
				isRequirementAppear = true;
				break;
			}
			assertTrue(isRequirementAppear);
			isRequirementAppear = false;
		}

	}

	private void verifyCapabilitiesOccurrences(Resource resourceJavaObject, String capabilitType) {
		boolean isCapabilityAppear = false;
		// List<CapabilityDefinition> capabilities =
		// resourceJavaObject.getCapabilities().get("tosca.capabilities.Endpoint.Admin");
		List<CapabilityDefinition> capabilities = resourceJavaObject.getCapabilities().get(capabilitType);

		for (CapabilityDefinition cap : capabilities) {
			switch (cap.getName()) {
			case "endpointNoOccurrence":
				assertTrue("Check Min capability Occurrences ", cap.getMinOccurrences().equals("1"));
				assertTrue("Check Max capability Occurrences ", cap.getMaxOccurrences().equals("UNBOUNDED"));
				isCapabilityAppear = true;
				break;
			case "endpoint200":
				assertTrue("Check Min capability Occurrences ", cap.getMinOccurrences().equals("1"));
				assertTrue("Check Max capability Occurrences ", cap.getMaxOccurrences().equals("2"));
				isCapabilityAppear = true;
				break;
			case "endpoint300":
				assertTrue("Check Min capability Occurrences ", cap.getMinOccurrences().equals("1"));
				assertTrue("Check Max capability Occurrences ", cap.getMaxOccurrences().equals("1"));
				isCapabilityAppear = true;
				break;
			case "endpoint400":
				assertTrue("Check Min capability Occurrences ", cap.getMinOccurrences().equals("1"));
				assertTrue("Check Max capability Occurrences ", cap.getMaxOccurrences().equals("10"));
				isCapabilityAppear = true;
				break;
			case "endpoint500":
				assertTrue("Check Min capability Occurrences ", cap.getMinOccurrences().equals("1"));
				assertTrue("Check Max capability Occurrences ", cap.getMaxOccurrences().equals("10000000"));
				isCapabilityAppear = true;
				break;
			case "endpoint600":
				assertTrue("Check Min capability Occurrences ", cap.getMinOccurrences().equals("1"));
				assertTrue("Check Max capability Occurrences ", cap.getMaxOccurrences().equals("UNBOUNDED"));
				isCapabilityAppear = true;
				break;
			case "endpoint700":
				assertTrue("Check Min capability Occurrences ", cap.getMinOccurrences().equals("2"));
				assertTrue("Check Max capability Occurrences ", cap.getMaxOccurrences().equals("4"));
				isCapabilityAppear = true;
				break;

			}
			assertTrue(isCapabilityAppear);
			isCapabilityAppear = false;
		}

	}

	private void verifyResourcePropertiesMap(Resource resourceJavaObject) { // use
																			// importMapPropertySuccessFlow.yml
		boolean isPropertyAppear = false;
		List<PropertyDefinition> propertiesList = resourceJavaObject.getProperties();
		for (PropertyDefinition pro : propertiesList) {
			switch (pro.getName()) {
			case "string_prop01":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\"val1\",\"keyB\":\"val2\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop02":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\"val1\",\"keyB\":\"val2\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop03":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\"val1\",\"keyB\":\"val2\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop04":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\"10\",\"keyB\":\"true\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop05":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":\"Big\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop06":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\"aaaA\",\"keyB\":null}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop07":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":null}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop08":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\"\",\"keyB\":\"abcd\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop09":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\" \",\"keyB\":\"abcd\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop10":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\" aaaa\",\"keyB\":\" bbbb\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop11":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\"aaaa \",\"keyB\":\"bbbb \"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop12":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":\" aaaa \",\"keyB\":\" bbbb ccccc \"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop13":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":\"aaaa\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop14":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":\" aaaa \"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop15":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":\"AbcD\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop16":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":\"AbcD\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop17":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":\"AbcD\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop18":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":\"AbcD\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop19":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":\"AbcD\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop20":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue()
						.equals("{\"keyA\":\"aaaa\",\"keya\":\"aaaa\",\"Keya\":\"Aaaa\",\"KEYA\":\"nnnn\"}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop21":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":null,\"keyC\":null}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "string_prop22":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertEquals("Check Property  default values ", pro.getDefaultValue(), null);
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("string"));
				isPropertyAppear = true;
				break;
			case "integer_prop01":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":1,\"keyB\":1000}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop02":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":null,\"keyC\":null}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop03":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":800,\"keyB\":-600}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop04":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":-600}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop05":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":100,\"keyB\":0}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop06":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":100,\"keyB\":0}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop07":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":100,\"keyB\":100}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop08":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":100,\"keyB\":200}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop09":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":100,\"keyB\":200}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop10":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":2222}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop11":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":null,\"keyC\":null,\"keyD\":null}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop12":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertEquals("Check Property  default values ", pro.getDefaultValue(), null);
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "integer_prop13":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":200}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("integer"));
				isPropertyAppear = true;
				break;
			case "boolean_prop01":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":true,\"keyB\":false,\"keyC\":false}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_prop02":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":true,\"keyB\":false,\"keyC\":false}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_prop03":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":null,\"keyC\":null,\"keyD\":null}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_prop04":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":null,\"keyC\":null,\"keyD\":null}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_prop05":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":true,\"keyB\":false,\"keyC\":false}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_prop06":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":true,\"keyB\":true,\"keyC\":false}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_prop07":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertEquals("Check Property  default values ", pro.getDefaultValue(), null);
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_prop08":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":true,\"keyB\":false}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "boolean_prop09":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":false,\"keyB\":true}"));
				assertTrue("Check entrySchema Property Type ",
						pro.getSchema().getProperty().getType().equals("boolean"));
				isPropertyAppear = true;
				break;
			case "float_prop01":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":1.2,\"keyB\":3.56,\"keyC\":33}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop02":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":0.0,\"keyB\":0.0,\"keyC\":0}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop03":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":null,\"keyC\":null,\"keyD\":null}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop04":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":1.2,\"keyB\":3.56,\"keyC\":33}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop05":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":33,\"keyB\":1.2,\"keyC\":3.607,\"keyD\":0}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop06":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":33,\"keyB\":1.2,\"keyC\":3.607}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop07":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":null,\"keyB\":null,\"keyC\":null,\"keyD\":null}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop08":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertEquals("Check Property  default values ", pro.getDefaultValue(), null);
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop09":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":0.01,\"keyB\":null}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop10":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ", pro.getDefaultValue().equals("{\"keyA\":0.00020}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			case "float_prop11":
				assertTrue("Check Property Type ", pro.getType().equals("map"));
				assertTrue("Check Property  default values ",
						pro.getDefaultValue().equals("{\"keyA\":3.56,\"keyB\":33}"));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType().equals("float"));
				isPropertyAppear = true;
				break;
			}
			assertTrue(isPropertyAppear);
			isPropertyAppear = false;
		}

	}

	@Test
	public void importToscaResourceAttributeSuccessFlow() throws Exception {

		String fileName = importAttributeSuccess;
		importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, testResourcesPath,
				fileName);
		RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, sdncUserDetails,
				null);
		ResourceRestUtils.checkCreateResponse(importResourceResponse);
		Resource resourceJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
		ToscaNodeTypeInfo parseToscaNodeYaml = utils
				.parseToscaNodeYaml(Decoder.decode(importReqDetails.getPayloadData()));

		HashMap<String, PropertyDefinition> attr = new HashMap<>();

		PropertyDefinition newAttr2 = new PropertyDefinition();
		newAttr2.setName("networks");
		newAttr2.setType("map");
		newAttr2.setDefaultValue("{\"keyA\" : val1 , \"keyB\" : val2}");
		SchemaDefinition schema = new SchemaDefinition();
		PropertyDataDefinition prop = new PropertyDataDefinition();
		prop.setType("string");
		schema.setProperty(prop);
		newAttr2.setSchema(schema);
		attr.put("networks", newAttr2);

		PropertyDefinition newAttr1 = new PropertyDefinition();
		newAttr1.setName("public_address");
		newAttr1.setType("string");
		attr.put("public_address", newAttr1);

		PropertyDefinition newAttr3 = new PropertyDefinition();
		newAttr3.setName("ports");
		newAttr3.setDescription("this is my description");
		attr.put("ports", newAttr3);

		PropertyDefinition newAttr = new PropertyDefinition();
		newAttr.setDefaultValue("myDefault");
		newAttr.setName("private_address");
		newAttr.setStatus("supported");
		newAttr.setType("string");
		attr.put("private_address", newAttr);

		// verify Resource Attributes
		validateResourceAttribute(resourceJavaObject, attr);

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ElementFactory
				.getDefaultImportResourceAuditMsgSuccess();
		expectedResourceAuditJavaObject.setResourceName(importReqDetails.getName());
		expectedResourceAuditJavaObject.setModifierName(sdncUserDetails.getFullName());
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		expectedResourceAuditJavaObject.setToscaNodeType(parseToscaNodeYaml.getNodeName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
				AuditingActionEnum.IMPORT_RESOURCE.getName(), null, false);
	}

	private void validateResourceAttribute(Resource resource, Map<String, PropertyDefinition> attr) {
		List<PropertyDefinition> resList = resource.getAttributes();
		int size = resList.size();
		String attributeName;
		for (int i = 0; i < size; i++) {
			attributeName = resList.get(i).getName();
			assertEquals(attr.get(attributeName).getDefaultValue(), resList.get(i).getDefaultValue());
			assertEquals(attr.get(attributeName).getName(), resList.get(i).getName());
			assertEquals(attr.get(attributeName).getDescription(), resList.get(i).getDescription());
			assertEquals(attr.get(attributeName).getStatus(), resList.get(i).getStatus());
		}
	}

}
