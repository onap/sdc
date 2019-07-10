/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.configuration.mocks.es;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;

import java.util.List;

public class ESCatalogDAOMockTest {

	private ESCatalogDAOMock createTestSubject() {
		return new ESCatalogDAOMock();
	}

	@Test
	public void testAddToIndicesMap() throws Exception {
		ESCatalogDAOMock testSubject;
		String typeName = "";
		String indexName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.addToIndicesMap(typeName, indexName);
	}

	@Test
	public void testWriteArtifact() throws Exception {
		ESCatalogDAOMock testSubject;
		ESArtifactData artifactData = null;

		// default test
		testSubject = createTestSubject();
		testSubject.writeArtifact(artifactData);
	}

	@Test
	public void testGetArtifact() throws Exception {
		ESCatalogDAOMock testSubject;
		String id = "";
		Either<ESArtifactData, ResourceUploadStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifact(id);
	}

	@Test
	public void testGetArtifacts() throws Exception {
		ESCatalogDAOMock testSubject;
		String[] ids = new String[] { "" };
		Either<List<ESArtifactData>, ResourceUploadStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts(ids);
	}

	@Test
	public void testDeleteArtifact() throws Exception {
		ESCatalogDAOMock testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.deleteArtifact(id);
	}

	@Test
	public void testDeleteAllArtifacts() throws Exception {
		ESCatalogDAOMock testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.deleteAllArtifacts();
	}
}
