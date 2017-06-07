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

package org.openecomp.sdc.be.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.IGenericSearchDAO;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.be.resources.api.IResourceUploader;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class }) // ,
																								// CassandraUnitTestExecutionListener.class})
// @EmbeddedCassandra(host ="localhost", port=9042)
public class ArtifactDaoTest {
	private static final String TEST_IMAGES_DIRECTORY = "src/test/resources/images";

	@Resource
	ElasticSearchClient esclient;

	/*
	 * @Resource(name = "artifact-dao") private IArtifactDAO artifactDAO;
	 */

	@Resource(name = "resource-upload")
	private IResourceUploader daoUploader;
	ESArtifactData arData;

	@Resource(name = "resource-dao")
	private IGenericSearchDAO resourceDAO;

	private String nodeType = "NodeType1";
	private String nodeTypeVersion = "1.0.0";

	private String nodeType2 = "NodeType2";
	private String nodeTypeVersion2 = "1.0.1";

	private String nodeType3 = "NodeType3";
	private String nodeNypeVersion3 = "1.1.1";

	private String topologyId = "topology";
	private String topologyTemplateName = "topologyTemplate";
	private String topologyTemplateVersion = "1.1.1";

	private String nodeTypeTemplate1 = "NodeTypeTemplate1";
	private String nodeTypeTemplate2 = "NodeTypeTemplate2";
	private String nodeTypeTemplate3 = "NodeTypeTemplate3";

	private static ConfigurationManager configurationManager;

	@Before
	public void before() {
		// try {
		// clearIndex(ICatalogDAO.RESOURCES_INDEX, ArtifactData.class);
		// clearIndex(ICatalogDAO.RESOURCES_INDEX, ServiceArtifactData.class);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

	}

	@BeforeClass
	public static void setupBeforeClass() {
		ExternalConfiguration.setAppName("catalog-dao");
		String appConfigDir = "src/test/resources/config/catalog-dao";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		configurationManager = new ConfigurationManager(configurationSource);
	}

	// @Before
	// public void createSchema(){
	// SdcSchemaBuilder.createSchema();
	// }
	//

	@Test
	public void testSaveNewArtifact() {
		// daoUploader = new ArtifactUploader(artifactDAO);
		if (daoUploader == null) {
			assertTrue(false);
		}
		String strData = "qweqwqweqw34e4wrwer";

		String myNodeType = "MyNewNodeType";

		ESArtifactData arData = new ESArtifactData("artifactNewMarina11", strData.getBytes());

		ResourceUploadStatus status = daoUploader.saveArtifact(arData, true);

		assertEquals(status, ResourceUploadStatus.OK);

		daoUploader.deleteArtifact(arData.getId());

	}

	/*
	 * @Test public void testSaveNewImage(){
	 * 
	 * Path iconPath = Paths.get(TEST_IMAGES_DIRECTORY, "apache.png");
	 * 
	 * ImageData imageData = new ImageData(); try {
	 * imageData.setData(Files.readAllBytes(iconPath));
	 * imageData.setComponentName("ComponentMarina");
	 * imageData.setComponentVersion("v.1.0");
	 * imageData.setArtifactName("apache.png");
	 * imageData.setResourceCreator("Marina");
	 * imageData.setResourceLastUpdater("Marina"); ResourceUploadStatus status =
	 * daoUploader.saveImage(imageData, true); assertEquals(status,
	 * ResourceUploadStatus.OK); } catch (IOException e) { e.printStackTrace(); }
	 * 
	 * 
	 * }
	 */

	// @Test
	// public void testGetArtifactsList() {
	// //daoUploader = new ArtifactUploader(artifactDAO);
	// if(daoUploader==null){
	// assertTrue(false);
	// }
	// String myNodeType = "MyListNodeType";
	//
	//
	//
	// //resourceDAO.save(indexedNodeType);
	//
	// String strData = "qweqwqweqw34e4wrwer";
	// ESArtifactData arData1 = new ESArtifactData("artifactNewMarina_1",
	// strData.getBytes());
	//
	//
	// ResourceUploadStatus status = daoUploader.saveArtifact(arData1, true);
	// assertEquals(status, ResourceUploadStatus.OK);
	//
	// ESArtifactData arData2 = new ESArtifactData("artifactNewMarina_2",
	// strData.getBytes());
	//
	//
	// status = daoUploader.saveArtifact(arData2, true);
	// assertEquals(status, ResourceUploadStatus.OK);
	//
	// ESArtifactData arData3 = new ESArtifactData("artifactNewMarina_3",
	// strData.getBytes());
	//
	//
	// status = daoUploader.saveArtifact(arData3, true);
	// assertEquals(status, ResourceUploadStatus.OK);
	//
	//
	//
	// Either<List<ESArtifactData>, ResourceUploadStatus> arrArray =
	// daoUploader.getArtifacts(myNodeType, nodeTypeVersion);
	// assertTrue(arrArray.isLeft());
	//
	// assertEquals(3, arrArray.left().value().size());
	//
	// daoUploader.deleteArtifact(arData1.getId());
	// daoUploader.deleteArtifact(arData2.getId());
	// daoUploader.deleteArtifact(arData3.getId());
	//
	// //resourceDAO.delete(IndexedNodeType.class, indexedNodeType.getId());
	//
	// }
	//

	/*
	 * @Test public void testGetSeviceArtifactsList() {
	 * 
	 * if(daoUploader==null){ assertTrue(false); } String strData =
	 * "qweqwqweqw34e4wrwer";
	 * 
	 * ServiceArtifactData serviceArData = new
	 * ServiceArtifactData("serviceArData", topologyTemplateName,
	 * topologyTemplateVersion, nodeTypeTemplate1, nodeType, nodeTypeVersion,
	 * "YANG", strData.getBytes(), strData.getBytes(), "Marina", null);
	 * //serviceArData.setRefArtifactId(arData.getId()); ResourceUploadStatus
	 * status = daoUploader.saveServiceArtifact(serviceArData, true);
	 * 
	 * ServiceArtifactData serviceArData1 = new
	 * ServiceArtifactData("serviceArData1", topologyTemplateName,
	 * topologyTemplateVersion, nodeTypeTemplate2, nodeType2, nodeTypeVersion2,
	 * "YANG", strData.getBytes(), strData.getBytes(), "Marina", null);
	 * //serviceArData1.setRefArtifactId(arData4.getId()); status =
	 * daoUploader.saveServiceArtifact(serviceArData1, true);
	 * ServiceArtifactData getServiceData =
	 * daoUploader.getServiceArtifact(serviceArData.getId()).left().value();
	 * 
	 * List<ServiceArtifactData> arrArray =
	 * daoUploader.getServiceArtifacts(topologyTemplateName,
	 * topologyTemplateVersion).left().value();
	 * 
	 * assertEquals(2, arrArray.size());
	 * 
	 * daoUploader.deleteArtifact(serviceArData.getId());
	 * daoUploader.deleteArtifact(serviceArData1.getId());
	 * 
	 * 
	 * }
	 */

	@Test
	public void testGetArtifact() {

		String myNodeType = "MyNodeType";

		// resourceDAO.save(indexedNodeType);
		ESArtifactData arData = getArtifactData(myNodeType, nodeTypeVersion);

		ESArtifactData getData = null;
		Either<ESArtifactData, ResourceUploadStatus> getArtifactStatus = daoUploader
				.getArtifact(myNodeType + "- dassasd" + ":" + nodeTypeVersion + ":updatedArtifact");
		if (getArtifactStatus.isRight()) {
			daoUploader.saveArtifact(arData, true);
			getArtifactStatus = daoUploader.getArtifact(arData.getId());
		}
		assertNotNull(getArtifactStatus.left().value());

	}

	/*
	 * @Test public void testGetSeviceArtifact() {
	 * 
	 * ServiceArtifactData servArData = getServiceArtifactData();
	 * 
	 * Either<ServiceArtifactData, ResourceUploadStatus>
	 * getServiceArtifactStatus =
	 * daoUploader.getServiceArtifact("MyService:v.1.1:updatedServiceArtifact");
	 * if (!getServiceArtifactStatus.isLeft()){
	 * daoUploader.saveServiceArtifact(servArData, true);
	 * getServiceArtifactStatus =
	 * daoUploader.getServiceArtifact(servArData.getId()); }
	 * 
	 * assertNotNull(getServiceArtifactStatus.left().value());
	 * 
	 * daoUploader.deleteArtifact(getServiceArtifactStatus.left().value().getId(
	 * ));
	 * 
	 * 
	 * }
	 */

	/*
	 * @Test public void testGetSeviceArtifactsCollection() {
	 * 
	 * prepareTopolgyService(); prepareTestTopolgyService();
	 * Either<ServiceArtifactsDataCollection, ResourceUploadStatus>
	 * getServiceArtifactsCollectionStatus =
	 * daoUploader.getServiceArtifactsCollection(topologyTemplateName,
	 * topologyTemplateVersion); ServiceArtifactsDataCollection serviceAtrifacts
	 * = getServiceArtifactsCollectionStatus.left().value();
	 * 
	 * Map<String, List<ArtifactData>> map =
	 * serviceAtrifacts.getServiceArtifactDataMap();
	 * 
	 * List<ArtifactData> list = map.get(nodeType); assertNotNull(list);
	 * assertEquals(2, list.size());
	 * 
	 * 
	 * list = map.get(nodeTypeTemplate1 ); assertNotNull(list); assertEquals(1,
	 * list.size());
	 * 
	 * list = map.get(nodeTypeTemplate2 ); assertNotNull(list); assertEquals(1,
	 * list.size());
	 * 
	 * 
	 * }
	 */

	@Test
	public void testUpdateArtifact() {
		// daoUploader = new ArtifactUploader(artifactDAO);
		if (daoUploader == null) {
			assertTrue(false);
		}
		ResourceUploadStatus status = ResourceUploadStatus.OK;

		String myNodeType = "MyUpdatedNodeType";

		// resourceDAO.save(indexedNodeType);

		ESArtifactData arData = getArtifactData(myNodeType, nodeTypeVersion);
		Either<ESArtifactData, ResourceUploadStatus> getArtifactStatus = daoUploader.getArtifact(arData.getId());

		if (!getArtifactStatus.isLeft())
			status = daoUploader.saveArtifact(arData, false);

		String payload1 = "new payloadjfdsgh";
		arData.setDataAsArray(payload1.getBytes());

		status = daoUploader.updateArtifact(arData);

		assertEquals(status, ResourceUploadStatus.OK);
		// resourceDAO.delete(IndexedNodeType.class, indexedNodeType.getId());

	}

	private ESArtifactData getArtifactData(String componentName, String componentVersion) {
		String strData = "qweqwqweqw34e4wrwer";
		ESArtifactData arData = new ESArtifactData("updatedArtifact", strData.getBytes());

		return arData;
	}

	/*
	 * private ServiceArtifactData getServiceArtifactData(){ String strData =
	 * "qweqwqweqw34e4wrwer"; ServiceArtifactData arData = new
	 * ServiceArtifactData("updatedServiceArtifact", "MyService", "v.1.1",
	 * "MyComponentTemplate", "MyComponent", "v.1.1", "YANG",
	 * strData.getBytes(), strData.getBytes(), "Marina", null);
	 * 
	 * return arData; }
	 */

	/*
	 * private void prepareTopolgyService(){
	 * 
	 * List<String> listCap = new ArrayList<String>(); listCap.add("very_evil");
	 * List<String> listCap1 = new ArrayList<String>(); listCap.add("evil");
	 * try{ // Initialize test data IndexedNodeType indexedNodeType = new
	 * IndexedNodeType(); CSARDependency dep = new CSARDependency();
	 * dep.setName(nodeType); dep.setVersion(nodeTypeVersion);
	 * indexedNodeType.setElementId(nodeType);
	 * indexedNodeType.setArchiveName(nodeType);
	 * indexedNodeType.setArchiveVersion(nodeTypeVersion);
	 * indexedNodeType.setCreationDate(new Date());
	 * indexedNodeType.setLastUpdateDate(new Date());
	 * indexedNodeType.setDefaultCapabilities(listCap);
	 * resourceDAO.save(indexedNodeType);
	 * 
	 * 
	 * IndexedNodeType indexedNodeType1 = new IndexedNodeType();
	 * indexedNodeType1.setElementId(nodeType2);
	 * indexedNodeType1.setArchiveName(nodeType2);
	 * indexedNodeType1.setArchiveVersion(nodeTypeVersion2); CSARDependency dep1
	 * = new CSARDependency(); dep1.setName(nodeType2);
	 * dep1.setVersion(nodeTypeVersion2); indexedNodeType1.setCreationDate(new
	 * Date()); indexedNodeType1.setLastUpdateDate(new Date());
	 * indexedNodeType1.setDefaultCapabilities(listCap1);
	 * resourceDAO.save(indexedNodeType1);
	 * 
	 * 
	 * indexedNodeType.setElementId(nodeType3);
	 * indexedNodeType.setArchiveName(nodeType3);
	 * indexedNodeType.setArchiveVersion(nodeNypeVersion3); CSARDependency dep2
	 * = new CSARDependency(); dep2.setName(nodeType3);
	 * dep2.setVersion(nodeNypeVersion3); indexedNodeType.setCreationDate(new
	 * Date()); indexedNodeType.setLastUpdateDate(new Date());
	 * indexedNodeType.setDefaultCapabilities(null);
	 * resourceDAO.save(indexedNodeType); String osgiliath100Id =
	 * indexedNodeType.getId();
	 * 
	 * Topology topology = new Topology(); topology.setId(topologyId);
	 * Set<CSARDependency> dependencies = new HashSet<CSARDependency>();
	 * dependencies.add(dep); dependencies.add(dep2); dependencies.add(dep1);
	 * topology.setDependencies(dependencies); Map<String, NodeTemplate>
	 * nodeTemplates = new HashMap <String, NodeTemplate>();
	 * 
	 * NodeTemplate template1 = new NodeTemplate(nodeType, null, null, null,
	 * null, null, null); template1.setName(nodeTypeTemplate1);
	 * nodeTemplates.put(nodeTypeTemplate1, template1 );
	 * 
	 * NodeTemplate template2 = new NodeTemplate(nodeType2, null, null, null,
	 * null, null, null); template2.setName(nodeTypeTemplate2 );
	 * nodeTemplates.put(nodeTypeTemplate2, template2 );
	 * 
	 * NodeTemplate template3 = new NodeTemplate(nodeType, null, null, null,
	 * null, null, null); template3.setName(nodeTypeTemplate3 );
	 * nodeTemplates.put(nodeTypeTemplate3, template3);
	 * 
	 * topology.setNodeTemplates(nodeTemplates); resourceDAO.save(topology);
	 * 
	 * TopologyTemplate topologyTemplate = new TopologyTemplate();
	 * topologyTemplate.setId(topologyTemplateName);
	 * topologyTemplate.setName(topologyTemplateName);
	 * topologyTemplate.setTopologyId(topology.getId());
	 * topologyTemplate.setDescription("my topology template");
	 * resourceDAO.save(topologyTemplate);
	 * 
	 * String strData = "qweqwqweqw34e4wrwer"; ArtifactData arData = new
	 * ArtifactData("artifact1", nodeType, nodeTypeVersion, "YANG",
	 * strData.getBytes(), strData.getBytes(), "Marina"); ArtifactData arData1 =
	 * new ArtifactData("artifact2", nodeType, nodeTypeVersion, "YANG",
	 * strData.getBytes(), strData.getBytes(), "Marina"); ResourceUploadStatus
	 * status = daoUploader.saveArtifact(arData, true); status =
	 * daoUploader.saveArtifact(arData1, true);
	 * 
	 * ArtifactData arData3 = new ArtifactData("artifact1", nodeType2,
	 * nodeTypeVersion2, "YANG", strData.getBytes(), strData.getBytes(),
	 * "Marina"); status = daoUploader.saveArtifact(arData3, true);
	 * 
	 * ArtifactData arData4 = new ArtifactData("artifact2", nodeType2,
	 * nodeTypeVersion2, "YANG", strData.getBytes(), strData.getBytes(),
	 * "Marina"); status = daoUploader.saveArtifact(arData4, true);
	 * 
	 * ServiceArtifactData serviceArData = new
	 * ServiceArtifactData("serviceArData", topologyTemplateName,
	 * topologyTemplateVersion, nodeTypeTemplate1, nodeType, nodeTypeVersion,
	 * "YANG", strData.getBytes(), strData.getBytes(), "Marina",
	 * arData.getId());
	 * 
	 * status = daoUploader.saveServiceArtifact(serviceArData, true);
	 * 
	 * ServiceArtifactData serviceArData1 = new
	 * ServiceArtifactData("serviceArData1", topologyTemplateName,
	 * topologyTemplateVersion, nodeTypeTemplate2, nodeType2, nodeTypeVersion2,
	 * "YANG", strData.getBytes(), strData.getBytes(), "Marina",
	 * arData4.getId());
	 * 
	 * status = daoUploader.saveServiceArtifact(serviceArData1, true);
	 * 
	 * 
	 * } catch (Exception e) {
	 * e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * private void prepareTestTopolgyService(){
	 * 
	 * List<String> listCap = new ArrayList<String>();
	 * listCap.add("very_evil test"); List<String> listCap1 = new
	 * ArrayList<String>(); listCap.add("evil test"); try{ // Initialize test
	 * data IndexedNodeType indexedNodeType = new IndexedNodeType();
	 * CSARDependency dep = new CSARDependency(); dep.setName(nodeType +
	 * " test"); dep.setVersion(nodeTypeVersion);
	 * indexedNodeType.setElementId(nodeType + " test");
	 * indexedNodeType.setArchiveName(nodeType + " test");
	 * indexedNodeType.setArchiveVersion(nodeTypeVersion);
	 * indexedNodeType.setCreationDate(new Date());
	 * indexedNodeType.setLastUpdateDate(new Date());
	 * indexedNodeType.setDefaultCapabilities(listCap);
	 * resourceDAO.save(indexedNodeType);
	 * 
	 * 
	 * IndexedNodeType indexedNodeType1 = new IndexedNodeType();
	 * indexedNodeType1.setElementId(nodeType2 + " test");
	 * indexedNodeType1.setArchiveName(nodeType2 + " test");
	 * indexedNodeType1.setArchiveVersion(nodeTypeVersion2); CSARDependency dep1
	 * = new CSARDependency(); dep1.setName(nodeType2 + " test");
	 * dep1.setVersion(nodeTypeVersion2); indexedNodeType1.setCreationDate(new
	 * Date()); indexedNodeType1.setLastUpdateDate(new Date());
	 * indexedNodeType1.setDefaultCapabilities(listCap1);
	 * resourceDAO.save(indexedNodeType1);
	 * 
	 * 
	 * indexedNodeType.setElementId(nodeType3 + " test");
	 * indexedNodeType.setArchiveName(nodeType3 + " test");
	 * indexedNodeType.setArchiveVersion(nodeNypeVersion3); CSARDependency dep2
	 * = new CSARDependency(); dep2.setName(nodeType3 + " test");
	 * dep2.setVersion(nodeNypeVersion3); indexedNodeType.setCreationDate(new
	 * Date()); indexedNodeType.setLastUpdateDate(new Date());
	 * indexedNodeType.setDefaultCapabilities(null);
	 * resourceDAO.save(indexedNodeType); String osgiliath100Id =
	 * indexedNodeType.getId();
	 * 
	 * Topology topology = new Topology(); topology.setId(topologyId + " test");
	 * Set<CSARDependency> dependencies = new HashSet<CSARDependency>();
	 * dependencies.add(dep); dependencies.add(dep2); dependencies.add(dep1);
	 * topology.setDependencies(dependencies); Map<String, NodeTemplate>
	 * nodeTemplates = new HashMap <String, NodeTemplate>();
	 * 
	 * NodeTemplate template1 = new NodeTemplate(nodeType + " test", null, null,
	 * null, null, null, null); template1.setName(nodeTypeTemplate1 + " test");
	 * nodeTemplates.put(nodeTypeTemplate1 + " test", template1 );
	 * 
	 * NodeTemplate template2 = new NodeTemplate(nodeType2 + " test", null,
	 * null, null, null, null, null); template2.setName(nodeTypeTemplate2 +
	 * " test" ); nodeTemplates.put(nodeTypeTemplate2 + " test", template2 );
	 * 
	 * NodeTemplate template3 = new NodeTemplate(nodeType, null, null, null,
	 * null, null, null); template3.setName(nodeTypeTemplate3 + " test" );
	 * nodeTemplates.put(nodeTypeTemplate3 + " test", template3);
	 * 
	 * topology.setNodeTemplates(nodeTemplates); resourceDAO.save(topology);
	 * 
	 * TopologyTemplate topologyTemplate = new TopologyTemplate();
	 * topologyTemplate.setId(topologyTemplateName + " test");
	 * topologyTemplate.setName(topologyTemplateName + " test");
	 * topologyTemplate.setTopologyId(topology.getId());
	 * topologyTemplate.setDescription("my topology template");
	 * resourceDAO.save(topologyTemplate);
	 * 
	 * String strData = "qweqwqweqw34e4wrwer"; ArtifactData arData = new
	 * ArtifactData("artifact1 test", nodeType + " test", nodeTypeVersion,
	 * "YANG", strData.getBytes(), strData.getBytes(), "Marina"); ArtifactData
	 * arData1 = new ArtifactData("artifact2 test", nodeType + " test",
	 * nodeTypeVersion, "YANG", strData.getBytes(), strData.getBytes(),
	 * "Marina"); ResourceUploadStatus status = daoUploader.saveArtifact(arData,
	 * true); status = daoUploader.saveArtifact(arData1, true);
	 * 
	 * ArtifactData arData3 = new ArtifactData("artifact1 test", nodeType2 +
	 * " test", nodeTypeVersion2, "YANG", strData.getBytes(),
	 * strData.getBytes(), "Marina"); status = daoUploader.saveArtifact(arData3,
	 * true);
	 * 
	 * ArtifactData arData4 = new ArtifactData("artifact2 test", nodeType2 +
	 * " test", nodeTypeVersion2, "YANG", strData.getBytes(),
	 * strData.getBytes(), "Marina"); status = daoUploader.saveArtifact(arData4,
	 * true);
	 * 
	 * ServiceArtifactData serviceArData = new
	 * ServiceArtifactData("serviceArData test" , topologyTemplateName +
	 * " test", topologyTemplateVersion, nodeTypeTemplate1 + " test", nodeType +
	 * " test", nodeTypeVersion, "YANG", strData.getBytes(), strData.getBytes(),
	 * "Marina", arData.getId());
	 * 
	 * status = daoUploader.saveServiceArtifact(serviceArData, true);
	 * 
	 * ServiceArtifactData serviceArData1 = new
	 * ServiceArtifactData("serviceArData1 test", topologyTemplateName +
	 * " test", topologyTemplateVersion, nodeTypeTemplate2 + " test", nodeType2
	 * + " test", nodeTypeVersion2, "YANG", strData.getBytes(),
	 * strData.getBytes(), "Marina", arData4.getId());
	 * 
	 * status = daoUploader.saveServiceArtifact(serviceArData1, true);
	 * 
	 * 
	 * } catch (Exception e) {
	 * e.printStackTrace(); }
	 * 
	 * }
	 */

	private void clearIndex(String indexName, Class<?> clazz) throws InterruptedException {

		DeleteIndexResponse actionGet = esclient.getClient().admin().indices().delete(new DeleteIndexRequest(indexName))
				.actionGet();
		assertTrue(actionGet.isAcknowledged());
	}

}
