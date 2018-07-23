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

import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.IGenericSearchDAO;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.openecomp.sdc.be.resources.api.IResourceUploader;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class }) // ,
																								// CassandraUnitTestExecutionListener.class})
public class ArtifactDaoTest extends DAOConfDependentTest {
	private static final String TEST_IMAGES_DIRECTORY = "src/test/resources/images";

	@Resource
	ElasticSearchClient esclient;

	@Resource(name = "resource-upload")
	private IResourceUploader daoUploader;
	ESArtifactData arData;

	@Resource(name = "resource-dao")
	private IGenericSearchDAO resourceDAO;

	private String nodeTypeVersion = "1.0.0";

	private static ConfigurationManager configurationManager;

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


	@Test
	public void testUpdateArtifact() {
		if (daoUploader == null) {
			fail();
		}
		ResourceUploadStatus status = ResourceUploadStatus.OK;

		String myNodeType = "MyUpdatedNodeType";

		ESArtifactData arData = getArtifactData(myNodeType, nodeTypeVersion);
		Either<ESArtifactData, ResourceUploadStatus> getArtifactStatus = daoUploader.getArtifact(arData.getId());

		if (!getArtifactStatus.isLeft())
			status = daoUploader.saveArtifact(arData, false);

		String payload1 = "new payloadjfdsgh";
		arData.setDataAsArray(payload1.getBytes());

		status = daoUploader.updateArtifact(arData);

		assertEquals(status, ResourceUploadStatus.OK);
	}

	private ESArtifactData getArtifactData(String componentName, String componentVersion) {
		String strData = "qweqwqweqw34e4wrwer";

        return new ESArtifactData("updatedArtifact", strData.getBytes());
	}
}
