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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openecomp.sdc.be.tosca.ComponentCache.MergeStrategy.overwriteIfSameVersions;
import static org.openecomp.sdc.be.tosca.ComponentCache.entry;

import fj.data.Either;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
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
import org.openecomp.sdc.exception.ResponseFormat;

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

	public CsarUtilsTest() throws IOException {
	}

	@Before
	public void setUpMock() throws Exception {
		ExternalConfiguration.setAppName("catalog-be");
		MockitoAnnotations.initMocks(this);
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

	private final List<String> nodesFromPackage = Arrays.asList("tosca.nodes.Root", "tosca.nodes.Container.Application");

	private final byte[] contentData = getFileResource("yamlValidation/resource-serviceTemplate.yml");


	private NonMetaArtifactInfo createNonMetaArtifactInfoTestSubject() {
		return new CsarUtils.NonMetaArtifactInfo("mock", "mock", ArtifactTypeEnum.AAI_SERVICE_MODEL.getType(),
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

		testSubject.createCsar(component, true, true);
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
	public void testAddInnerComponentsToCacheWhenGetToscaElementIsRight() {
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
	public void testExtractVfcsArtifactsFromCsar() {
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
		final NonMetaArtifactInfo testSubject = createNonMetaArtifactInfoTestSubject();
		assertThat("The artifact type should be as expected",
			testSubject.getArtifactType(), is(ArtifactTypeEnum.AAI_SERVICE_MODEL.getType()));
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

    private byte[] getFileResource(final String filePath) throws IOException {
        try (final InputStream inputStream = getFileResourceAsInputStream(filePath)) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private InputStream getFileResourceAsInputStream(final String filePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
    }

}
