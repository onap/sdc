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


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;
import org.openecomp.sdc.be.tosca.CsarUtils.NonMetaArtifactInfo;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;
import mockit.Deencapsulation;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;

public class CsarUtilsTest extends BeConfDependentTest {

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

	@Before
	public void setUpMock() throws Exception {
		MockitoAnnotations.initMocks(this);
		
	}

	private NonMetaArtifactInfo createNonMetaArtifactInfoTestSubject() {
		return new CsarUtils.NonMetaArtifactInfo("mock", "mock", ArtifactTypeEnum.AAI_SERVICE_MODEL,
				ArtifactGroupTypeEnum.DEPLOYMENT, new byte[0], "mock", true);
	}

	@Test
	public void testCreateCsar() {
		Component component = new Resource();
		Map<String, ArtifactDefinition> toscaArtifacts = new HashMap<>();
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactName("artifactName");
		artifact.setEsId("esId");
		toscaArtifacts.put("assettoscatemplate", artifact);

		component.setToscaArtifacts(toscaArtifacts);

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class)))
				.thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));

		Mockito.when(componentsUtils.convertFromStorageResponse(Mockito.any(StorageOperationStatus.class)))
				.thenReturn(ActionStatus.GENERAL_ERROR);

		Either<byte[], ResponseFormat> csar = testSubject.createCsar(component, true, true);

		assertNotNull(csar);
		assertTrue(csar.isRight());
	}

	@Test
	public void testCreateCsarWithGenerateCsarZipResponseIsLeft() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

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

		Either<byte[], ResponseFormat> csar = testSubject.createCsar(component, false, true);

		assertNotNull(csar);
		assertTrue(csar.isLeft());
	}

	@Test
	public void testGenerateCsarZipThrowsIOException() {

		Deencapsulation.invoke(testSubject, "generateCsarZip", new byte[]{},new  byte[]{}, new Resource(), true, false);
	}

	@Test
	public void testPopulateZipWhenGetDependenciesIsRight() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

		ToscaRepresentation tosca = new ToscaRepresentation();
		tosca.setMainYaml("value");

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.right(ToscaError.GENERAL_ERROR));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, false);

			assertNotNull(output);
			assertTrue(output.isRight());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPopulateZipWhenExportComponentIsRight() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class)))
				.thenReturn(Either.right(ToscaError.GENERAL_ERROR));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, false);

			assertNotNull(output);
			assertTrue(output.isRight());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPopulateZipWhenComponentIsServiceAndCollectComponentCsarDefinitionIsRight() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

		List<SdcSchemaFilesData> filesData = new ArrayList<>();
		SdcSchemaFilesData filedata = new SdcSchemaFilesData();
		filedata.setPayloadAsArray(data);
		filesData.add(filedata);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "cassandraId", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		ToscaRepresentation tosca = new ToscaRepresentation();
		tosca.setMainYaml("value");

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.left(filesData));

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class)))
				.thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));

		Mockito.when(artifactsBusinessLogic.validateUserExists(Mockito.any(String.class), Mockito.any(String.class),
				Mockito.any(Boolean.class))).thenReturn(new User());

		Mockito.when(artifactsBusinessLogic.validateAndHandleArtifact(Mockito.any(String.class),
				Mockito.any(ComponentTypeEnum.class), Mockito.any(ArtifactOperationInfo.class), Mockito.isNull(),
				Mockito.any(ArtifactDefinition.class), Mockito.any(String.class), Mockito.any(String.class),
				Mockito.isNull(), Mockito.isNull(), Mockito.any(User.class), Mockito.any(Component.class),
				Mockito.any(Boolean.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class)))
				.thenReturn(Either.left(Mockito.any(Either.class)));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);

			assertNotNull(output);
			assertTrue(output.isRight());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPopulateZipWhenGetEntryDataIsRight() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

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

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);

			assertNotNull(output);
			assertTrue(output.isRight());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPopulateZipWhenGetEntryDataOfInnerComponentIsRight() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		ToscaRepresentation tosca = new ToscaRepresentation();
		tosca.setMainYaml("value");

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca),
				Either.left(tosca), Either.right(ToscaError.GENERAL_ERROR));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);

			assertNotNull(output);
			assertTrue(output.isRight());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPopulateZipWhenLatestSchemaFilesFromCassandraIsRight() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		ToscaRepresentation tosca = new ToscaRepresentation();
		tosca.setMainYaml("value");

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);

			assertNotNull(output);
			assertTrue(output.isRight());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPopulateZipWhenAddSchemaFilesFromCassandraIsRight() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		ToscaRepresentation tosca = new ToscaRepresentation();
		tosca.setMainYaml("value");

		List<SdcSchemaFilesData> schemaList = new ArrayList<>();
		SdcSchemaFilesData schemaData = new SdcSchemaFilesData();
		schemaData.setPayloadAsArray(null);
		schemaList.add(schemaData);

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.left(schemaList));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);

			assertNotNull(output);
			assertTrue(output.isRight());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPopulateZipWhenHandleAllAAIArtifactsInDataModelIsRight() {
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
		ESArtifactData artifactData = new ESArtifactData();
		byte[] data = "value".getBytes();
		artifactData.setDataAsArray(data);

		ToscaTemplate toscaTemplate = new ToscaTemplate("version");
		List<Triple<String, String, Component>> dependencies = new ArrayList<>();
		Triple<String, String, Component> triple = Triple.of("fileName", "", component);
		dependencies.add(triple);
		toscaTemplate.setDependencies(dependencies);

		ToscaRepresentation tosca = new ToscaRepresentation();
		tosca.setMainYaml("value");

		List<SdcSchemaFilesData> schemaList = new ArrayList<>();
		SdcSchemaFilesData schemaData = new SdcSchemaFilesData();
		schemaData.setPayloadAsArray(data);
		schemaList.add(schemaData);

		Mockito.when(toscaOperationFacade.getToscaElement(Mockito.any(String.class))).thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class))).thenReturn(Either.left(artifactData));

		Mockito.when(toscaExportUtils.exportComponent(Mockito.any(Component.class))).thenReturn(Either.left(tosca));

		Mockito.when(toscaExportUtils.getDependencies(Mockito.any(Component.class)))
				.thenReturn(Either.left(toscaTemplate));

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.left(schemaList));

		Mockito.when(artifactsBusinessLogic.validateUserExists(Mockito.any(String.class), Mockito.any(String.class),
				Mockito.any(Boolean.class))).thenReturn(new User());


		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "populateZip", component, getFromCS, zip, true);

			assertNotNull(output);
			assertTrue(output.isRight());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAddSchemaFilesFromCassandra() {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				ZipOutputStream zip = new ZipOutputStream(out);
				ByteArrayOutputStream outMockStream = new ByteArrayOutputStream();
				ZipOutputStream outMock = new ZipOutputStream(outMockStream);) {

			outMock.putNextEntry(new ZipEntry("mock1"));
			outMock.write(new byte[1]);
			outMock.putNextEntry(new ZipEntry("mock2"));
			outMock.write(new byte[3]);
			outMock.close();
			byte[] byteArray = outMockStream.toByteArray();
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "addSchemaFilesFromCassandra", zip, byteArray);

			assertNotNull(output);
			assertTrue(output.isLeft());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testAddInnerComponentsToCache() {
		Map<String, ImmutableTriple<String, String, Component>> componentCache = new HashMap<>();
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

		assertFalse(componentCache.containsValue(ImmutableTriple.of("esId","artifactName",componentRI)));

		Deencapsulation.invoke(testSubject, "addInnerComponentsToCache", componentCache, childComponent);

		assertTrue(componentCache.containsValue(ImmutableTriple.of("esId","artifactName",componentRI)));
	}

	@Test
	public void testAddInnerComponentsToCacheWhenGetToscaElementIsRight() {
		Map<String, ImmutableTriple<String, String, Component>> componentCache = new HashMap<>();
		Component childComponent = new Resource();

		List<ComponentInstance> componentInstances = new ArrayList<>();
		ComponentInstance instance = new ComponentInstance();
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

		Deencapsulation.invoke(testSubject, "addInnerComponentsToCache", componentCache, childComponent);

		assertTrue(componentCache.isEmpty());
	}

	@Test
	public void testAddComponentToCache() {
		Map<String, ImmutableTriple<String, String, Component>> componentCache = new HashMap<>();
		String id = "id";
		String fileName = "fileName";
		Component component = new Resource();
		component.setInvariantUUID("key");
		component.setVersion("1.0");

		Component cachedComponent = new Resource();
		cachedComponent.setVersion("0.3");

		componentCache.put("key", new ImmutableTriple<String, String, Component>(id, fileName, cachedComponent));

		Deencapsulation.invoke(testSubject, "addComponentToCache", componentCache, id, fileName, component);

		assertSame("id", componentCache.get("key").left);
		assertSame("fileName", componentCache.get("key").middle);
		assertSame(componentCache.get("key").right, component);
	}

	@Test
	public void testWriteComponentInterface() {
		String fileName = "name.hello";
		ToscaRepresentation tosca = new ToscaRepresentation();
		tosca.setMainYaml("value");

		Mockito.when(toscaExportUtils.exportComponentInterface(Mockito.any(Component.class), Mockito.any(Boolean.class)))
				.thenReturn(Either.left(tosca));


		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "writeComponentInterface", new Resource(), zip, fileName, false);

			assertNotNull(output);
			assertTrue(output.isLeft());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetEntryData() {
		String cassandraId = "id";
		Component childComponent = new Resource();

		Mockito.when(artifactCassandraDao.getArtifact(Mockito.any(String.class)))
				.thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));

		Either<byte[], ActionStatus> output = Deencapsulation.invoke(testSubject, "getEntryData", cassandraId, childComponent);

		assertNotNull(output);
		assertTrue(output.isRight());
	}

	@Test
	public void testGetLatestSchemaFilesFromCassandraWhenListOfSchemasIsEmpty() {
		List<SdcSchemaFilesData> filesData = new ArrayList<>();

		Mockito.when(
				sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(Mockito.any(String.class), Mockito.any(String.class)))
				.thenReturn(Either.left(filesData));

		Either<byte[], ResponseFormat> output = Deencapsulation.invoke(testSubject, "getLatestSchemaFilesFromCassandra");

		assertNotNull(output);
		assertTrue(output.isRight());
	}

	@Test
	public void testExtractVfcsArtifactsFromCsar() {
		String key = "Artifacts/org.openecomp.resource.some/path/to/resource";
		byte[] data = "value".getBytes();

		Map<String, byte[]> csar = new HashMap<>();
		csar.put(key, data);

		Map<String, List<ArtifactDefinition>> output = CsarUtils.extractVfcsArtifactsFromCsar(csar);

		assertNotNull(output);
	}

	@Test
	public void testAddExtractedVfcArtifactWhenArtifactsContainsExtractedArtifactKey() {
		ImmutablePair<String, ArtifactDefinition> extractedVfcArtifact = new ImmutablePair<String, ArtifactDefinition>(
				"key", new ArtifactDefinition());
		Map<String, List<ArtifactDefinition>> artifacts = new HashMap<>();
		artifacts.put("key", new ArrayList<>());

		Deencapsulation.invoke(testSubject, "addExtractedVfcArtifact", extractedVfcArtifact, artifacts);

		assertEquals(1, artifacts.get("key").size());
	}

	@Test
	public void testAddExtractedVfcArtifactWhenArtifactsDoesntContainsExtractedArtifactKey() {
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
	public void testExtractVfcArtifact() {
		String path = "path/to/informational/artificat";
		Map<String, byte[]> map = new HashMap<>();
		map.put(path, "value".getBytes());
		Entry<String, byte[]> entry = map.entrySet().iterator().next();

		ImmutablePair<String, ArtifactDefinition> output = Deencapsulation.invoke(testSubject, "extractVfcArtifact", entry, new HashMap<>());

		assertNotNull(output);
		assertEquals("to",output.left);
	}

	@Test
	public void testDetectArtifactGroupTypeWithExceptionBeingCaught() {
		Either<ArtifactGroupTypeEnum, Boolean> output = Deencapsulation.invoke(testSubject, "detectArtifactGroupType", "type", Map.class);

		assertNotNull(output);
		assertTrue(output.isRight());
		assertFalse(output.right().value());
	}

	@Test
	public void testDetectArtifactGroupTypeWWhenCollectedWarningMessagesContainesKey() {
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();

		collectedWarningMessages.put("Warning - unrecognized artifact group type {} was received.", new HashSet<>());
		Either<ArtifactGroupTypeEnum, Boolean> output = Deencapsulation.invoke(testSubject, "detectArtifactGroupType", "type", collectedWarningMessages);

		assertNotNull(output);
		assertTrue(output.isRight());
		assertFalse(output.right().value());
	}

	@Test
	public void testNonMetaArtifactInfoCtor() {
		createNonMetaArtifactInfoTestSubject();
	}

	@Test
	public void testNonMetaArtifactInfoGetPath() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getPath();
	}

	@Test
	public void testNonMetaArtifactInfoGetArtifactName() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactName();
	}

	@Test
	public void testNonMetaArtifactInfoGetArtifactType() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactType();
	}

	@Test
	public void testNonMetaArtifactInfoGetDisplayName() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getDisplayName();
	}

	@Test
	public void testNonMetaArtifactInfoGetArtifactGroupType() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactGroupType();
	}

	@Test
	public void testNonMetaArtifactInfoGetArtifactLabel() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactLabel();
	}

	@Test
	public void testNonMetaArtifactInfoGetIsFromCsar() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.isFromCsar();
	}

	@Test
	public void testNonMetaArtifactInfoGetPayloadData() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getPayloadData();
	}

	@Test
	public void testNonMetaArtifactInfoGetArtifaactChecksum() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactChecksum();
	}

	@Test
	public void testNonMetaArtifactInfoGetArtifactUniqueId() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.getArtifactUniqueId();
	}

	@Test
	public void testNonMetaArtifactInfosetArtifactUniqueId() {
		NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();

		testSubject.setArtifactUniqueId("artifactUniqueId");
	}

	@Test
	public void testValidateNonMetaArtifactWithExceptionCaught() {
		CsarUtils.validateNonMetaArtifact("", new byte[0], new HashMap<>());
	}

	@Test
	public void testWriteArtifactDefinition() {
		Component component = new Service();
		List<ArtifactDefinition> artifactDefinitionList = new ArrayList<>();
		String artifactPathAndFolder = "";

		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactType(ArtifactTypeEnum.HEAT_ENV.getType());
		artifactDefinitionList.add(artifact);

		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out);) {
			Either<ZipOutputStream, ResponseFormat> output = Deencapsulation.invoke(testSubject, "writeArtifactDefinition", component, zip, artifactDefinitionList, artifactPathAndFolder, false);

			assertNotNull(output);
			assertTrue(output.isLeft());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCollectComponentCsarDefinitionWhenComponentIsServiceAndGetToscaElementIsLeft() {
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
	public void testCollectComponentTypeArtifactsWhenFetchedComponentHasComponentInstances() {
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
	public void testCollectComponentTypeArtifactsWhenFetchedComponentDontHaveComponentInstances() {
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
	public void testValidateNonMetaArtifactHappyScenario() {
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
	public void testValidateNonMetaArtifactScenarioWithWarnnings() {
		String artifactPath = "Artifacts/Deployment/Buga/myYang.xml";
		byte[] payloadData = "some payload data".getBytes();
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
		Either<NonMetaArtifactInfo, Boolean> eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath,
				payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());

		artifactPath = "Artifacts/Informational/Buga2/someArtifact.xml";
		eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath, payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());

		assertTrue(collectedWarningMessages.size() == 1);
		assertTrue(collectedWarningMessages.values().iterator().next().size() == 2);
	}

	@Test
	public void testValidateNonMetaArtifactUnhappyScenario() {
		String artifactPath = "Artifacts/Buga/YANG_XML/myYang.xml";
		byte[] payloadData = "some payload data".getBytes();
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
		Either<NonMetaArtifactInfo, Boolean> eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath,
				payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isRight());
		assertTrue(!collectedWarningMessages.isEmpty());
	}
}
