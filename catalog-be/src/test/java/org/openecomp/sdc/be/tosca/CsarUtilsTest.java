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

package org.openecomp.sdc.be.tosca;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.openecomp.sdc.be.tosca.ComponentCache.MergeStrategy.overwriteIfSameVersions;
import static org.openecomp.sdc.be.tosca.ComponentCache.entry;

import fj.data.Either;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mockit.Deencapsulation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;
import org.openecomp.sdc.be.tosca.ComponentCache.CacheEntry;
import org.openecomp.sdc.be.tosca.CsarUtils.NonMetaArtifactInfo;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.test.BaseConfDependent;
import org.openecomp.sdc.exception.ResponseFormat;
import com.datastax.driver.mapping.Mapper.Option;

class CsarUtilsTest extends BaseConfDependent {

	@InjectMocks
	CsarUtils testSubject;

	@Mock
	private ArtifactCassandraDao artifactCassandraDao;

	@Mock
	private ComponentsUtils componentsUtils;

	@Mock
	private ToscaExportHandler toscaExportUtils;

	@Mock
	private SdcSchemaFilesCassandraDao sdcSchemaFilesCassandraDao;

	@Mock
	private ToscaOperationFacade toscaOperationFacade;

	@Mock
	private ArtifactsBusinessLogic artifactsBusinessLogic;

	private final List<String> nodesFromPackage = Arrays.asList("tosca.nodes.Root", "tosca.nodes.Container.Application");

	private final byte[] contentData;

	public CsarUtilsTest() throws IOException {
		contentData = getFileResource("yamlValidation/resource-serviceTemplate.yml");
	}

	@BeforeAll
	public static void setupBeforeClass() {
		componentName = "catalog-be";
		confPath = "src/test/resources/config";
		setUp();
	}

	@BeforeEach
	public void setUpMock() {
		ExternalConfiguration.setAppName("catalog-be");
		MockitoAnnotations.openMocks(this);
		initConfigurationManager();
	}

	private static void initConfigurationManager() {
		final String confPath = new File(Objects
			.requireNonNull(
				CsarUtilsTest.class.getClassLoader().getResource("config/catalog-be/configuration.yaml"))
			.getFile()).getParent();
		final ConfigurationSource confSource =
			new FSConfigurationSource(ExternalConfiguration.getChangeListener(), confPath);
		new ConfigurationManager(confSource);
	}

	private NonMetaArtifactInfo createNonMetaArtifactInfoTestSubject() {
		return new CsarUtils.NonMetaArtifactInfo("mock", "mock", ArtifactTypeEnum.AAI_SERVICE_MODEL.getType(),
				ArtifactGroupTypeEnum.DEPLOYMENT, new byte[0], "mock", true);
	}

	@Test
	void testCreateCsar() {
		Component component = new Resource();
		Map<String, ArtifactDefinition> artifactDefinitionHashMap = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifactDefinitionHashMap.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(artifactDefinitionHashMap);
		component.setArtifacts(artifactDefinitionHashMap);
		component.setDeploymentArtifacts(artifactDefinitionHashMap);

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class)))
				.thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));
		
        Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class), Mockito.any(Option.class)))
                .thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));

		Mockito.when(componentsUtils.convertFromStorageResponse(Mockito.any(StorageOperationStatus.class)))
				.thenReturn(ActionStatus.GENERAL_ERROR);

		testSubject.createCsar(component, true, true);
	}

	@Test
	void testCreateCsarWithGenerateCsarZipResponseIsLeft() {
		Component component = new Resource();
		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);
		DAOArtifactData artifactData = new DAOArtifactData();
		byte[] data = "value".getBytes();
		ByteBuffer bufferData = ByteBuffer.wrap(data);
		artifactData.setData(bufferData);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		toscaTemplate.setDependencies(dependencies);

		List<SdcSchemaFilesData> filesData = new ArrayList<>();
		SdcSchemaFilesData filedata = new SdcSchemaFilesData();
		filedata.setPayloadAsArray(data);
		filesData.add(filedata);

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(componentsUtils.convertFromStorageResponse(Mockito.any(StorageOperationStatus.class)))
				.thenReturn(ActionStatus.GENERAL_ERROR);

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.left(filesData));

		testSubject.createCsar(component, false, true);
	}

	@Test
	void testPopulateZipWhenGetDependenciesIsRight() {
		Component component = new Service();
		boolean getFromCS = false;

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		DAOArtifactData artifactData = new DAOArtifactData();
		byte[] data = "value".getBytes();
		ByteBuffer bufferData = ByteBuffer.wrap(data);
		artifactData.setData(bufferData);

		ToscaRepresentation tosca = ToscaRepresentation.make("value".getBytes());

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.right(ToscaError.GENERAL_ERROR));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out);) {
			Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testPopulateZipWhenExportComponentIsRight() {
		Component component = new Resource();
		boolean getFromCS = false;

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		DAOArtifactData artifactData = new DAOArtifactData();
		byte[] data = "value".getBytes();
		ByteBuffer bufferData = ByteBuffer.wrap(data);
		artifactData.setData(bufferData);

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class)))
				.thenReturn(Either.right(ToscaError.GENERAL_ERROR));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out);) {
			Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testPopulateZipWhenComponentIsServiceAndCollectComponentCsarDefinitionIsRight() {
		Component component = new Service();
		boolean getFromCS = false;

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		artifact.setDescription("description");
		artifact.setArtifactLabel("artifactLabel");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		component.setVersion("1.0");
		component.setLastUpdaterUserId("userId");
		component.setUniqueId("uid");
		DAOArtifactData artifactData = new DAOArtifactData();
		ByteBuffer bufferData = ByteBuffer.wrap(contentData);
		artifactData.setData(bufferData);

		List<SdcSchemaFilesData> filesData = new ArrayList<>();
		SdcSchemaFilesData filedata = new SdcSchemaFilesData();
		filedata.setPayloadAsArray(contentData);
		filesData.add(filedata);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "cassandraId", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		ToscaRepresentation tosca = ToscaRepresentation.make("value".getBytes());

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.left(filesData));

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		Mockito.when(artifactsBusinessLogic.validateUserExists(Mockito.any(User.class))).thenReturn(new User());


		Mockito.when(artifactsBusinessLogic.validateAndHandleArtifact(Mockito.any(String.class),
				Mockito.any(ComponentTypeEnum.class), Mockito.any(ArtifactOperationInfo.class), Mockito.isNull(),
				Mockito.any(ArtifactDefinition.class), Mockito.any(String.class), Mockito.any(String.class),
				Mockito.isNull(), Mockito.isNull(), Mockito.any(User.class), Mockito.any(Component.class),
				Mockito.any(Boolean.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class)))
				.thenReturn(Either.left(Mockito.any(ArtifactDefinition.class)));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out);) {
			Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testPopulateZipWhenGetEntryDataIsRight() {
		Component component = new Service();
		boolean getFromCS = true;

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		artifact.setDescription("description");
		artifact.setArtifactLabel("artifactLabel");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		component.setVersion("1.0");
		component.setLastUpdaterUserId("userId");
		component.setUniqueId("uid");
		DAOArtifactData artifactData = new DAOArtifactData();
		byte[] data = "value".getBytes();
		ByteBuffer bufferData = ByteBuffer.wrap(data);
		artifactData.setData(bufferData);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class)))
				.thenReturn(Either.right(ToscaError.GENERAL_ERROR));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out);) {
			Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testPopulateZipWhenGetEntryDataOfInnerComponentIsRight() {
		Component component = new Service();
		boolean getFromCS = false;

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		artifact.setDescription("description");
		artifact.setArtifactLabel("artifactLabel");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		component.setVersion("1.0");
		component.setLastUpdaterUserId("userId");
		component.setUniqueId("uid");
		DAOArtifactData artifactData = new DAOArtifactData();
		ByteBuffer bufferData = ByteBuffer.wrap(contentData);
		artifactData.setData(bufferData);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		ToscaRepresentation tosca = ToscaRepresentation.make(contentData);

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca),
				Either.left(tosca), Either.right(ToscaError.GENERAL_ERROR));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out);) {
			Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testPopulateZipWhenLatestSchemaFilesFromCassandraIsRight() {
		Component component = new Service();
		boolean getFromCS = false;

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		artifact.setDescription("description");
		artifact.setArtifactLabel("artifactLabel");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		component.setVersion("1.0");
		component.setLastUpdaterUserId("userId");
		component.setUniqueId("uid");
		DAOArtifactData artifactData = new DAOArtifactData();
		ByteBuffer bufferData = ByteBuffer.wrap(contentData);
		artifactData.setData(bufferData);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		ToscaRepresentation tosca = ToscaRepresentation.make(contentData);

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out);) {
			Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testAddInnerComponentsToCache() {
		ComponentCache componentCache = ComponentCache.overwritable(overwriteIfSameVersions());
		Component childComponent = new Resource();
		Component componentRI = new Service();
		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setComponentUid("resourceUid");
		componentInstances.add(instance);
		childComponent.setComponentInstances(componentInstances);

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		artifact.setDescription("description");
		artifact.setArtifactLabel("artifactLabel");
		toscaArtifacts.put("assettoscatemplate", artifact);

		componentRI.setToscaArtifacts(toscaArtifacts);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class)))
				.thenReturn(Either.left(componentRI));

		Deencapsulation.invoke(testSubject, "addInnerComponentsToCache", componentCache, childComponent);

		io.vavr.collection.List<CacheEntry> expected = io.vavr.collection.List.of(entry("esId","artifactName",componentRI));
		assertEquals(expected, componentCache.all().toList());
	}

	@Test
	void testAddInnerComponentsToCacheWhenGetToscaElementIsRight() {
		Map<String, ImmutableTriple<String, String, Component>> componentCache = new HashMap<>();
		Component childComponent = new Resource();

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setComponentUid("abc");
		componentInstances.add(instance);
		childComponent.setComponentInstances(componentInstances);

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		artifact.setDescription("description");
		artifact.setArtifactLabel("artifactLabel");
		toscaArtifacts.put("assettoscatemplate", artifact);

		Component componentRI = new Service();

		componentRI.setToscaArtifacts(toscaArtifacts);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));


		assertTrue(componentCache.isEmpty());
	}

	@Test
	void testWriteComponentInterface() throws IOException {
		String fileName = "name.hello";
		ToscaRepresentation tosca = ToscaRepresentation.make("value".getBytes());

		Mockito.when(toscaExportUtils.exportComponentInterface(Mockito.any(Component.class), Mockito.any(Boolean.class)))
				.thenReturn(Either.left(tosca));


		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
		    List<Triple<String, String, Component>> output = Deencapsulation.invoke(testSubject, "writeComponentInterface", new Resource(), zip, fileName);
			assertNotNull(output);
		}
	}

	@Test
	void testGetEntryData() {
		String cassandraId = "id";
		Component childComponent = new Resource();

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class)))
				.thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));
		
        Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class), Mockito.any(Option.class)))
                .thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));

		Either<byte[], ActionStatus> output = Deencapsulation.invoke(testSubject, "getEntryData", cassandraId, childComponent);

		assertNotNull(output);
		assertTrue(output.isRight());
	}

	@Test
	void testGetLatestSchemaFilesFromCassandraWhenListOfSchemasIsEmpty() {
		List<SdcSchemaFilesData> filesData = new ArrayList<>();

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.left(filesData));

		Either<byte[], ResponseFormat> output = Deencapsulation.invoke(testSubject, "getLatestSchemaFilesFromCassandra");

		assertNotNull(output);
		assertTrue(output.isRight());
	}

	@Test
	void testExtractVfcsArtifactsFromCsar() {
		String key = "Artifacts/org.openecomp.resource.some/Deployment/to/resource";
		byte[] data = "value".getBytes();

		Map<String, byte[]> csar = new HashMap<>();
		csar.put(key, data);

		Map<String, List<ArtifactDefinition>> output = CsarUtils.extractVfcsArtifactsFromCsar(csar);

		assertNotNull(output);
		assertTrue(output.containsKey("org.openecomp.resource.some"));
		assertEquals(1, output.get("org.openecomp.resource.some").size());
	}

	@Test
	void testAddExtractedVfcArtifactWhenArtifactsContainsExtractedArtifactKey() {
		ImmutablePair<String, ArtifactDefinition> extractedVfcArtifact = new ImmutablePair<String, ArtifactDefinition>(
				"key", new ArtifactDefinition());
		Map<String, List<ArtifactDefinition>> artifacts = new HashMap<>();
		artifacts.put("key", new ArrayList<>());

		Deencapsulation.invoke(testSubject, "addExtractedVfcArtifact", extractedVfcArtifact, artifacts);

		assertEquals(1, artifacts.get("key").size());
	}

	@Test
	void testAddExtractedVfcArtifactWhenArtifactsDoesntContainsExtractedArtifactKey() {
		ImmutablePair<String, ArtifactDefinition> extractedVfcArtifact = new ImmutablePair<String, ArtifactDefinition>(
				"key", new ArtifactDefinition());
		Map<String, List<ArtifactDefinition>> artifacts = new HashMap<>();
		artifacts.put("key1", new ArrayList<>());

		Deencapsulation.invoke(testSubject, "addExtractedVfcArtifact", extractedVfcArtifact, artifacts);

		assertEquals(0, artifacts.get("key1").size());
		assertEquals(1, artifacts.get("key").size());
		assertEquals(2, artifacts.size());
	}

	@Test
	void testExtractVfcArtifact() {
		String path = "path/to/informational/artificat";
		Map<String, byte[]> map = new HashMap<>();
		map.put(path, "value".getBytes());
		Entry<String, byte[]> entry = map.entrySet().iterator().next();

		Optional<ImmutablePair<String, ArtifactDefinition>> output =
			Deencapsulation.invoke(testSubject, "extractVfcArtifact", entry, new HashMap<>());

		if(output.isPresent()) {
			assertEquals("to", output.get().left);
		} else {
			fail("`output` is empty!");
		}
	}

	@Test
	void testDetectArtifactGroupTypeWithExceptionBeingCaught() {
		Either<ArtifactGroupTypeEnum, Boolean> output = Deencapsulation.invoke(testSubject, "detectArtifactGroupType", "type", Map.class);

		assertNotNull(output);
		assertTrue(output.isRight());
		assertFalse(output.right().value());
	}

	@Test
	void testDetectArtifactGroupTypeWWhenCollectedWarningMessagesContainesKey() {
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();

		collectedWarningMessages.put("Warning - unrecognized artifact group type {} was received.", new HashSet<>());
		Either<ArtifactGroupTypeEnum, Boolean> output = Deencapsulation.invoke(testSubject, "detectArtifactGroupType", "type", collectedWarningMessages);

		assertNotNull(output);
		assertTrue(output.isRight());
		assertFalse(output.right().value());
	}

	@Test
	void testNonMetaArtifactInfoCtor() {
		createNonMetaArtifactInfoTestSubject();
	}

	@Test
	void testNonMetaArtifactInfoGetPath() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getPath();
	}

	@Test
	void testNonMetaArtifactInfoGetArtifactName() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactName();
	}

	@Test
	void testNonMetaArtifactInfoGetArtifactType() {
		final NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();
		assertThat("The artifact type should be as expected",
			testSubject.getArtifactType(), is(ArtifactTypeEnum.AAI_SERVICE_MODEL.getType()));
	}

	@Test
	void testNonMetaArtifactInfoGetDisplayName() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getDisplayName();
	}

	@Test
	void testNonMetaArtifactInfoGetArtifactGroupType() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactGroupType();
	}

	@Test
	void testNonMetaArtifactInfoGetArtifactLabel() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactLabel();
	}

	@Test
	void testNonMetaArtifactInfoGetIsFromCsar() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.isFromCsar();
	}

	@Test
	void testNonMetaArtifactInfoGetPayloadData() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getPayloadData();
	}

	@Test
	void testNonMetaArtifactInfoGetArtifaactChecksum() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactChecksum();
	}

	@Test
	void testNonMetaArtifactInfoGetArtifactUniqueId() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactUniqueId();
	}

	@Test
	void testNonMetaArtifactInfosetArtifactUniqueId() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.setArtifactUniqueId("artifactUniqueId");
	}

	@Test
	void testValidateNonMetaArtifactWithExceptionCaught() {
		CsarUtils.validateNonMetaArtifact("", new byte[0], new HashMap<>());
	}

	@Test
	void testCollectComponentCsarDefinitionWhenComponentIsServiceAndGetToscaElementIsLeft() {
		Component component = new Service();
		component.setUniqueId("uniqueId");
		List<ComponentInstance> resourceInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setComponentUid("resourceUid");
		instance.setOriginType(OriginTypeEnum.SERVICE);
		resourceInstances.add(instance);
		component.setComponentInstances(resourceInstances);

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class))).thenReturn(Either.left(component),
				Either.right(StorageOperationStatus.BAD_REQUEST));

		Either<Object, ResponseFormat> output = Deencapsulation.invoke(testSubject, "collectComponentCsarDefinition", component);

		assertNotNull(output);
		assertTrue(output.isRight());
	}

	@Test
	void testCollectComponentTypeArtifactsWhenFetchedComponentHasComponentInstances() {
		Component component = new Service();
		Component fetchedComponent = new Resource();
		component.setUniqueId("uniqueId");
		List<ComponentInstance> resourceInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setComponentUid("resourceUid");
		instance.setOriginType(OriginTypeEnum.SERVICE);
		resourceInstances.add(instance);
		component.setComponentInstances(resourceInstances);
		fetchedComponent.setComponentInstances(resourceInstances);

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("YANG");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);

		fetchedComponent.setToscaArtifacts(toscaArtifacts);
		fetchedComponent.setDeploymentArtifacts(toscaArtifacts);
		fetchedComponent.setArtifacts(toscaArtifacts);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class))).thenReturn(Either.left(component),
				Either.left(fetchedComponent), Either.right(StorageOperationStatus.BAD_REQUEST));

		Either<Object, ResponseFormat> output = Deencapsulation.invoke(testSubject, "collectComponentCsarDefinition", component);

		assertNotNull(output);
		assertTrue(output.isRight());
	}

	@Test
	void testCollectComponentTypeArtifactsWhenFetchedComponentDontHaveComponentInstances() {
		Component component = new Service();
		Component fetchedComponent = new Resource();
		component.setUniqueId("uniqueId");
		List<ComponentInstance> resourceInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
		instance.setComponentUid("resourceUid");
		instance.setOriginType(OriginTypeEnum.SERVICE);

		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		artifact.setArtifactUUID("artifactUUID");
		artifact.setArtifactType("PLAN");
		toscaArtifacts.put("assettoscatemplate", artifact);

		instance.setDeploymentArtifacts(toscaArtifacts);

		resourceInstances.add(instance);
		component.setComponentInstances(resourceInstances);

		component.setToscaArtifacts(toscaArtifacts);
		component.setDeploymentArtifacts(toscaArtifacts);
		component.setArtifacts(toscaArtifacts);

		fetchedComponent.setToscaArtifacts(toscaArtifacts);
		fetchedComponent.setDeploymentArtifacts(toscaArtifacts);
		fetchedComponent.setArtifacts(toscaArtifacts);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class))).thenReturn(Either.left(component),
				Either.left(fetchedComponent));

		Either<Object, ResponseFormat> output = Deencapsulation.invoke(testSubject, "collectComponentCsarDefinition", component);

		assertNotNull(output);
		assertTrue(output.isLeft());
	}

	@Test
	void testValidateNonMetaArtifactHappyScenario() {
		String artifactPath = "Artifacts/Deployment/YANG_XML/myYang.xml";
		byte[] payloadData = "some payload data".getBytes();
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
		Either<NonMetaArtifactInfo, Boolean> eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath,
				payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());
		assertTrue(collectedWarningMessages.isEmpty());

		artifactPath = "Artifacts/Informational/OTHER/someArtifact.xml";
		eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath, payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());
		assertTrue(collectedWarningMessages.isEmpty());
	}

	@Test
	void testValidateNonMetaArtifactScenarioWithWarnnings() {
		String artifactPath = "Artifacts/Deployment/Buga/myYang.xml";
		byte[] payloadData = "some payload data".getBytes();
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
		Either<NonMetaArtifactInfo, Boolean> eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath,
				payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());

		artifactPath = "Artifacts/Informational/Buga2/someArtifact.xml";
		eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath, payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());

		assertEquals(1, collectedWarningMessages.size());
		assertEquals(2, collectedWarningMessages.values().iterator().next().size());
	}

	@Test
	void testValidateNonMetaArtifactUnhappyScenario() {
		String artifactPath = "Artifacts/Buga/YANG_XML/myYang.xml";
		byte[] payloadData = "some payload data".getBytes();
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
		Either<NonMetaArtifactInfo, Boolean> eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath,
				payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isRight());
		assertFalse(collectedWarningMessages.isEmpty());
	}

	@Test
	void testAddSchemaFilesFromCassandraAddingDuplicatedEntry() throws IOException {
		final String rootPath = System.getProperty("user.dir");
		final Path path = Paths.get(rootPath + "/src/test/resources/sdc.zip");
		final byte[] data = Files.readAllBytes(path);
		try (final ByteArrayOutputStream out = new ByteArrayOutputStream(); final ZipOutputStream zip = new ZipOutputStream(out)) {
			Deencapsulation.invoke(testSubject, "addSchemaFilesFromCassandra", zip, data, nodesFromPackage);
			final IOException actualException = assertThrows(IOException.class, () -> zip.putNextEntry(new ZipEntry("Definitions/nodes.yml")));
			assertEquals("duplicate entry: Definitions/nodes.yml", actualException.getMessage());
		}
	}

	@Test
	void testFindNonRootNodesFromPackage() {
		final Resource resource = new Resource();
		resource.setDerivedList(nodesFromPackage);
		final Component component = resource;
		final List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		final Triple<String, String, Component> triple = Triple.of("fileName", "cassandraId", component);
		dependencies.add(triple);
		final List<String> expectedResult = Arrays.asList("tosca.nodes.Container.Application");
		final List<String> result = Deencapsulation.invoke(testSubject,
			"findNonRootNodesFromPackage", dependencies);
		assertTrue(CollectionUtils.isNotEmpty(result));
		assertEquals(expectedResult, result);
	}

    private byte[] getFileResource(final String filePath) throws IOException {
        try (final InputStream inputStream = getFileResourceAsInputStream(filePath)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private InputStream getFileResourceAsInputStream(final String filePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
    }

}
