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

package org.openecomp.sdc.be.components;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtilsTest;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterOrEqualConstraint;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.PolicyException;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;

import fj.data.Either;

public class ResourceImportManagerTest {

	private static ConfigurationManager configurationManager;
	static ResourceImportManager importManager;
	static IAuditingManager auditingManager = Mockito.mock(IAuditingManager.class);
	static ResponseFormatManager responseFormatManager = Mockito.mock(ResponseFormatManager.class);
	static ResourceBusinessLogic resourceBusinessLogic = Mockito.mock(ResourceBusinessLogic.class);
	static UserBusinessLogic userAdmin = Mockito.mock(UserBusinessLogic.class);
	static ToscaOperationFacade toscaOperationFacade =  Mockito.mock(ToscaOperationFacade.class);
	static Logger log = Mockito.spy(Logger.class);

	@BeforeClass
	public static void beforeClass() throws IOException {
		importManager = new ResourceImportManager();
		importManager.setAuditingManager(auditingManager);
		when(toscaOperationFacade.getLatestByToscaResourceName(Mockito.anyString())).thenReturn(Either.left(null));
		importManager.setResponseFormatManager(responseFormatManager);
		importManager.setResourceBusinessLogic(resourceBusinessLogic);
		importManager.setToscaOperationFacade(toscaOperationFacade);
		ResourceImportManager.setLog(log);

		String appConfigDir = "src/test/resources/config/catalog-be";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		configurationManager = new ConfigurationManager(configurationSource);

		Configuration configuration = new Configuration();
		configuration.setTitanInMemoryGraph(true);
		configurationManager.setConfiguration(configuration);
	}

	@Before
	public void beforeTest() {
		Mockito.reset(auditingManager, responseFormatManager, resourceBusinessLogic, userAdmin, log);
	}

	@Test
	public void testBasicResourceCreation() throws IOException {
		UploadResourceInfo resourceMD = createDummyResourceMD();

		User user = new User();
		user.setUserId(resourceMD.getContactId());
		user.setRole("ADMIN");
		user.setFirstName("Jhon");
		user.setLastName("Doh");
		Either<User, ActionStatus> eitherUser = Either.left(user);

		when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(eitherUser);

		setResourceBusinessLogicMock();

		String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-blockStorage.yml");

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
		assertTrue(createResource.isLeft());
		Resource resource = createResource.left().value().left;

		testSetConstantMetaData(resource);
		testSetMetaDataFromJson(resource, resourceMD);

		testSetDerivedFrom(resource);
		testSetProperties(resource);

		Mockito.verify(resourceBusinessLogic, Mockito.times(1)).propagateStateToCertified(Mockito.eq(user), Mockito.eq(resource), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.eq(false), Mockito.eq(true), Mockito.eq(false));
	}

	@Test
	public void testResourceCreationFailed() throws IOException {
		UploadResourceInfo resourceMD = createDummyResourceMD();
		User user = new User();
		user.setUserId(resourceMD.getContactId());
		Either<User, ActionStatus> eitherUser = Either.left(user);
		when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(eitherUser);
		ResponseFormat dummyResponseFormat = createGeneralErrorInfo();

		when(responseFormatManager.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(dummyResponseFormat);
		setResourceBusinessLogicMock();

		String jsonContent = "this is an invalid yml!";

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
		assertTrue(createResource.isRight());
		ResponseFormat errorInfoFromTest = createResource.right().value();
		assertTrue(errorInfoFromTest.getStatus().equals(dummyResponseFormat.getStatus()));
		assertTrue(errorInfoFromTest.getMessageId().equals(dummyResponseFormat.getMessageId()));
		assertTrue(errorInfoFromTest.getFormattedMessage().equals(dummyResponseFormat.getFormattedMessage()));
		Mockito.verify(log).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Exception.class));
		// Mockito.verify(log).error(Mockito.anyString(), Mockito.anyString(),
		// Mockito.anyString());

		Mockito.verify(resourceBusinessLogic, Mockito.times(0)).createOrUpdateResourceByImport(Mockito.any(Resource.class), Mockito.eq(user), Mockito.eq(true), Mockito.eq(false), Mockito.eq(true), Mockito.eq(null), Mockito.eq(null), Mockito.eq(false));

		Mockito.verify(resourceBusinessLogic, Mockito.times(0)).propagateStateToCertified(Mockito.eq(user), Mockito.any(Resource.class), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.eq(false), Mockito.eq(true), Mockito.eq(false));

	}

	@Test
	public void testResourceCreationWithCapabilities() throws IOException {
		UploadResourceInfo resourceMD = createDummyResourceMD();
		User user = new User();
		user.setUserId(resourceMD.getContactId());
		Either<User, ActionStatus> eitherUser = Either.left(user);

		when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(eitherUser);

		setResourceBusinessLogicMock();

		String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-webServer.yml");

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
		assertTrue(createResource.isLeft());
		Resource resource = createResource.left().value().left;
		testSetCapabilities(resource);

		Mockito.verify(resourceBusinessLogic, Mockito.times(1)).propagateStateToCertified(Mockito.eq(user), Mockito.eq(resource), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.eq(false), Mockito.eq(true), Mockito.eq(false));
		Mockito.verify(resourceBusinessLogic, Mockito.times(1)).createOrUpdateResourceByImport(resource, user, true, false, true, null, null, false);

	}

	@Test
	public void testResourceCreationWithRequirments() throws IOException {
		UploadResourceInfo resourceMD = createDummyResourceMD();
		User user = new User();
		user.setUserId(resourceMD.getContactId());
		Either<User, ActionStatus> eitherUser = Either.left(user);

		when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(eitherUser);

		setResourceBusinessLogicMock();

		String jsonContent = ImportUtilsTest.loadFileNameToJsonString("normative-types-new-port.yml");

		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> createResource = importManager.importNormativeResource(jsonContent, resourceMD, user, true, true);
		assertTrue(createResource.isLeft());
		testSetRequirments(createResource.left().value().left);

	}

	private void setResourceBusinessLogicMock() {
		when(resourceBusinessLogic.getUserAdmin()).thenReturn(userAdmin);
		when(resourceBusinessLogic.createOrUpdateResourceByImport(Mockito.any(Resource.class), Mockito.any(User.class), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.eq(null), Mockito.eq(null), Mockito.eq(false)))
				.thenAnswer(new Answer<Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat>>() {
					public Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						return Either.left(new ImmutablePair<Resource, ActionStatus>((Resource) args[0], ActionStatus.CREATED));

					}
				});
		when(resourceBusinessLogic.propagateStateToCertified(Mockito.any(User.class), Mockito.any(Resource.class), Mockito.any(LifecycleChangeInfoWithAction.class), Mockito.eq(false), Mockito.eq(true), Mockito.eq(false)))
				.thenAnswer(new Answer<Either<Resource, ResponseFormat>>() {
					public Either<Resource, ResponseFormat> answer(InvocationOnMock invocation) throws Throwable {
						Object[] args = invocation.getArguments();
						return Either.left((Resource) args[1]);

					}
				});
		when(resourceBusinessLogic.createResourceByDao(Mockito.any(Resource.class), Mockito.any(User.class), Mockito.any(AuditingActionEnum.class), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any(EnumMap.class))).thenAnswer(new Answer<Either<Resource, ResponseFormat>>() {
			public Either<Resource, ResponseFormat> answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return Either.left((Resource) args[0]);

			}
		});
		when(resourceBusinessLogic.validateResourceBeforeCreate(Mockito.any(Resource.class), Mockito.any(User.class), Mockito.any(AuditingActionEnum.class), Mockito.eq(false), Mockito.eq(null))).thenAnswer(new Answer<Either<Resource, ResponseFormat>>() {
			public Either<Resource, ResponseFormat> answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return Either.left((Resource) args[0]);

			}
		});

		Either<Boolean, ResponseFormat> either = Either.left(true);
		when(resourceBusinessLogic.validatePropertiesDefaultValues(Mockito.any(Resource.class))).thenReturn(either);
	}

	public ResponseFormat createGeneralErrorInfo() {
		ResponseFormat responseFormat = new ResponseFormat(500);
		responseFormat.setPolicyException(new PolicyException("POL5000", "Error: Internal Server Error. Please try again later", null));
		return responseFormat;
	}

	private UploadResourceInfo createDummyResourceMD() {
		UploadResourceInfo resourceMD = new UploadResourceInfo();
		resourceMD.setName("tosca.nodes.BlockStorage");
		resourceMD.setPayloadName("payLoad");
		resourceMD.addSubCategory("Generic", "Infrastructure");
		resourceMD.setContactId("ya107f");
		resourceMD.setResourceIconPath("defaulticon");
		resourceMD.setTags(Arrays.asList(new String[] { "BlockStorage" }));
		resourceMD.setDescription("Represents a server-local block storage device (i.e., not shared) offering evenly sized blocks of data from which raw storage volumes can be created.");
		return resourceMD;
	}

	private void testSetProperties(Resource resource) {
		List<PropertyDefinition> propertiesList = resource.getProperties();

		Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();
		for (PropertyDefinition propertyDefinition : propertiesList) {
			properties.put(propertyDefinition.getName(), propertyDefinition);
		}

		assertTrue(properties.size() == 3);
		assertTrue(properties.containsKey("size"));
		PropertyDefinition propertyDefinition = properties.get("size");
		assertTrue(propertyDefinition.getType().equals("scalar-unit.size"));
		assertTrue(propertyDefinition.getConstraints().size() == 1);
		PropertyConstraint propertyConstraint = propertyDefinition.getConstraints().get(0);
		assertTrue(propertyConstraint instanceof GreaterOrEqualConstraint);

		assertTrue(properties.containsKey("volume_id"));
		propertyDefinition = properties.get("volume_id");
		assertTrue(propertyDefinition.getType().equals("string"));
		assertTrue(propertyDefinition.isRequired() == false);

		assertTrue(properties.containsKey("snapshot_id"));
		propertyDefinition = properties.get("snapshot_id");
		assertTrue(propertyDefinition.getType().equals("string"));
		assertTrue(propertyDefinition.isRequired() == false);

	}

	private void testSetCapabilities(Resource resource) {
		Map<String, List<CapabilityDefinition>> capabilities = resource.getCapabilities();
		assertTrue(capabilities.size() == 3);
		assertTrue(capabilities.containsKey("tosca.capabilities.Endpoint"));
		List<CapabilityDefinition> capabilityList = capabilities.get("tosca.capabilities.Endpoint");
		CapabilityDefinition capability = capabilityList.get(0);
		assertTrue(capability.getType().equals("tosca.capabilities.Endpoint"));
		assertTrue(capability.getName().equals("data_endpoint"));

		assertTrue(capabilities.containsKey("tosca.capabilities.Endpoint.Admin"));
		capabilityList = capabilities.get("tosca.capabilities.Endpoint.Admin");
		capability = capabilityList.get(0);
		assertTrue(capability.getType().equals("tosca.capabilities.Endpoint.Admin"));
		assertTrue(capability.getName().equals("admin_endpoint"));

		assertTrue(capabilities.containsKey("tosca.capabilities.Container"));
		capabilityList = capabilities.get("tosca.capabilities.Container");
		capability = capabilityList.get(0);
		assertTrue(capability.getType().equals("tosca.capabilities.Container"));
		assertTrue(capability.getName().equals("host"));

		List<String> validSourceTypes = capability.getValidSourceTypes();
		assertTrue(validSourceTypes.size() == 1);
		assertTrue(validSourceTypes.get(0).equals("tosca.nodes.WebApplication"));

	}

	private void testSetRequirments(Resource resource) {
		Map<String, List<RequirementDefinition>> requirements = resource.getRequirements();
		assertTrue(requirements.size() == 2);

		assertTrue(requirements.containsKey("tosca.capabilities.network.Linkable"));
		List<RequirementDefinition> requirementList = requirements.get("tosca.capabilities.network.Linkable");
		RequirementDefinition requirement = requirementList.get(0);
		assertTrue(requirement.getCapability().equals("tosca.capabilities.network.Linkable"));
		assertTrue(requirement.getRelationship().equals("tosca.relationships.network.LinksTo"));
		assertTrue(requirement.getName().equals("link"));

		assertTrue(requirements.containsKey("tosca.capabilities.network.Bindable"));
		requirementList = requirements.get("tosca.capabilities.network.Bindable");
		requirement = requirementList.get(0);
		assertTrue(requirement.getCapability().equals("tosca.capabilities.network.Bindable"));
		assertTrue(requirement.getRelationship().equals("tosca.relationships.network.BindsTo"));
		assertTrue(requirement.getName().equals("binding"));

	}

	private void testSetDerivedFrom(Resource resource) {
		assertTrue(resource.getDerivedFrom().size() == 1);
		assertTrue(resource.getDerivedFrom().get(0).equals("tosca.nodes.Root"));

	}

	private void testSetMetaDataFromJson(Resource resource, UploadResourceInfo resourceMD) {

		// assertTrue( resource.getCategory().equals(resourceMD.getCategory())
		// );
		assertTrue(resource.getDescription().equals(resourceMD.getDescription()));
		assertTrue(resource.getIcon().equals(resourceMD.getResourceIconPath()));
		assertTrue(resource.getName().equals(resourceMD.getName()));

		assertTrue(resource.getContactId().equals(resourceMD.getContactId()));
		assertTrue(resource.getCreatorUserId().equals(resourceMD.getContactId()));

		// assertTrue( resource.isAbstract() ==
		// Constants.ABSTRACT_CATEGORY.equals(resourceMD.getCategory()));

		assertTrue(resourceMD.getTags().size() == resource.getTags().size());
		for (String tag : resource.getTags()) {
			assertTrue(resourceMD.getTags().contains(tag));
		}

	}

	private void testSetConstantMetaData(Resource resource) {
		assertTrue(resource.getVersion().equals(ImportUtils.Constants.FIRST_CERTIFIED_VERSION_VERSION));
		assertTrue(resource.getLifecycleState() == ImportUtils.Constants.NORMATIVE_TYPE_LIFE_CYCLE);
		assertTrue(resource.isHighestVersion() == ImportUtils.Constants.NORMATIVE_TYPE_HIGHEST_VERSION);
		assertTrue(resource.getVendorName().equals(ImportUtils.Constants.VENDOR_NAME));
		assertTrue(resource.getVendorRelease().equals(ImportUtils.Constants.VENDOR_RELEASE));
	}

}
