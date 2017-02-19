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

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ArtifactOperation;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import fj.data.Either;

public class ArtifactBusinessLogicTest {

	static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
	static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

	@InjectMocks
	static ArtifactsBusinessLogic artifactBL = new ArtifactsBusinessLogic();

	public static final ArtifactOperation artifactOperation = Mockito.mock(ArtifactOperation.class);
	public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
	public static final IInterfaceLifecycleOperation lifecycleOperation = Mockito.mock(IInterfaceLifecycleOperation.class);
	public static final IUserAdminOperation userOperation = Mockito.mock(IUserAdminOperation.class);
	public static final IElementOperation elementOperation = Mockito.mock(IElementOperation.class);
	// public static final InformationDeployedArtifactsBusinessLogic
	// informationDeployedArtifactsBusinessLogic =
	// Mockito.mock(InformationDeployedArtifactsBusinessLogic.class);

	public static final Resource resource = Mockito.mock(Resource.class);
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@BeforeClass
	public static void setup() {

		Either<ArtifactDefinition, StorageOperationStatus> NotFoundResult = Either.right(StorageOperationStatus.NOT_FOUND);
		when(artifactOperation.getArtifactById(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(NotFoundResult);

		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> NotFoundResult2 = Either.right(StorageOperationStatus.NOT_FOUND);
		when(artifactOperation.getArtifacts(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Service), Mockito.anyBoolean())).thenReturn(NotFoundResult2);
		when(artifactOperation.getArtifacts(Mockito.anyString(), Mockito.eq(NodeTypeEnum.Resource), Mockito.anyBoolean())).thenReturn(NotFoundResult2);

		Either<Map<String, InterfaceDefinition>, StorageOperationStatus> notFoundInterfaces = Either.right(StorageOperationStatus.NOT_FOUND);
		when(lifecycleOperation.getAllInterfacesOfResource(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(notFoundInterfaces);

		User userJH = new User("John", "Doh", "jh0003", "jh0003@gmail.com", "ADMIN", System.currentTimeMillis());
		Either<User, ActionStatus> getUserResult = Either.left(userJH);

		when(userOperation.getUserData("jh0003", false)).thenReturn(getUserResult);

		Either<List<ArtifactType>, ActionStatus> getType = Either.left(getAllTypes());
		when(elementOperation.getAllArtifactTypes()).thenReturn(getType);

		when(resource.getResourceType()).thenReturn(ResourceTypeEnum.VFC);
		// when(informationDeployedArtifactsBusinessLogic.getAllDeployableArtifacts(Mockito.any(Resource.class))).thenReturn(new
		// ArrayList<ArtifactDefinition>());
	}

	private static List<ArtifactType> getAllTypes() {
		List<ArtifactType> artifactTypes = new ArrayList<ArtifactType>();
		List<String> artifactTypesList = ConfigurationManager.getConfigurationManager().getConfiguration().getArtifactTypes();
		for (String artifactType : artifactTypesList) {
			ArtifactType artifactT = new ArtifactType();
			artifactT.setName(artifactType);
			artifactTypes.add(artifactT);
		}
		return artifactTypes;
	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidJson() {
		ArtifactDefinition ad = createArtifactDef();

		String jsonArtifact = gson.toJson(ad);

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact, ArtifactDefinition.class);
		assertEquals(ad, afterConvert);
	}

	private ArtifactDefinition createArtifactDef() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1.yaml");
		ad.setArtifactLabel("label1");
		ad.setDescription("description");
		ad.setArtifactType(ArtifactTypeEnum.HEAT.getType());
		ad.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);
		ad.setTimeout(15);
		return ad;
	}

	@Test
	public void testInvalidStringGroupType() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1");
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);
		ad.setTimeout(15);

		JsonElement jsonArtifact = gson.toJsonTree(ad);
		jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", "www");

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
		assertNull(afterConvert);
	}

	@Test
	public void testInvalidNumberGroupType() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1");
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);
		ad.setTimeout(15);

		JsonElement jsonArtifact = gson.toJsonTree(ad);
		jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", 123);

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
		assertNull(afterConvert);
	}

	@Test
	public void testInvalidGroupTypeWithSpace() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1");
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);
		ad.setTimeout(15);

		JsonElement jsonArtifact = gson.toJsonTree(ad);
		jsonArtifact.getAsJsonObject().addProperty("artifactGroupType", " DEPLOYMENT");

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
		assertNull(afterConvert);
	}

	@Test
	public void testInvalidTimeoutWithSpace() {
		ArtifactDefinition ad = new ArtifactDefinition();
		ad.setArtifactName("artifact1");
		ad.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		ad.setCreationDate(System.currentTimeMillis());
		ad.setMandatory(false);

		JsonElement jsonArtifact = gson.toJsonTree(ad);
		jsonArtifact.getAsJsonObject().addProperty("timeout", " 15");

		ArtifactDefinition afterConvert = RepresentationUtils.convertJsonToArtifactDefinition(jsonArtifact.toString(), ArtifactDefinition.class);
		assertNull(afterConvert);
	}

	// @Test
	// public void convertAndValidateDeploymentArtifactNonHeatSuccess(){
	// ArtifactDefinition createArtifactDef = createArtifactDef();
	// createArtifactDef.setArtifactType(ArtifactTypeEnum.YANG_XML.getType());
	//
	// Either<ArtifactDefinition, ResponseFormat> validateResult = artifactBL
	// .convertAndValidate(resource, "resourceId",
	// gson.toJson(createArtifactDef), "jh0003", null, null, true,
	// null, NodeTypeEnum.Resource);
	//
	// assertTrue(validateResult.isLeft());
	// ArtifactDefinition validatedArtifact = validateResult.left().value();
	//
	// assertEquals(createArtifactDef.getArtifactGroupType(),
	// validatedArtifact.getArtifactGroupType());
	// assertEquals(new Integer(0), validatedArtifact.getTimeout());
	// assertFalse(validatedArtifact.getMandatory());
	// assertFalse(validatedArtifact.getServiceApi());
	//
	// }
}
