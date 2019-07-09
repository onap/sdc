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

package org.openecomp.sdc.be.impl;

import com.att.aft.dme2.internal.jersey.core.util.Base64;
import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.info.ArtifactAccessInfo;
import org.openecomp.sdc.be.resources.data.ESArtifactData;

import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

public class DownloadArtifactLogicTest {

	private DownloadArtifactLogic createTestSubject() {
		return new DownloadArtifactLogic();
	}

	@Test
	public void testDownloadArtifact() throws Exception {
		DownloadArtifactLogic testSubject;
		String artifactName = "";
		String artifactId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.downloadArtifact(artifactName, Either.right(ResourceUploadStatus.COMPONENT_NOT_EXIST), artifactId);
		result = testSubject.downloadArtifact(artifactName, Either.right(ResourceUploadStatus.ALREADY_EXIST), artifactId);
		ESArtifactData ad = new ESArtifactData();
		ad.setDataAsArray(Base64.encode("mock"));
		result = testSubject.downloadArtifact(artifactName, Either.left(ad ), artifactId);
	}

	@Test
	public void testConvertArtifactList() throws Exception {
		DownloadArtifactLogic testSubject;
		List<ESArtifactData> artifactsList = new LinkedList<>();
		artifactsList.add(new ESArtifactData());
		String servletPath = "mock";
		List<ArtifactAccessInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertArtifactList(artifactsList, servletPath);
	}

	@Test
	public void testCreateArtifactListResponse() throws Exception {
		DownloadArtifactLogic testSubject;
		String serviceName = "mock";
		Either<List<ESArtifactData>, ResourceUploadStatus> getArtifactsStatus = Either.right(ResourceUploadStatus.COMPONENT_NOT_EXIST);
		String servletPath = "mock";
		Response result;

		// default test
		testSubject = createTestSubject();
		
		result = testSubject.createArtifactListResponse(serviceName, getArtifactsStatus, servletPath);
		getArtifactsStatus = Either.right(ResourceUploadStatus.ALREADY_EXIST);
		result = testSubject.createArtifactListResponse(serviceName, getArtifactsStatus, servletPath);
		List<ESArtifactData> artifactsList = new LinkedList<>();
		artifactsList.add(new ESArtifactData());
		getArtifactsStatus = Either.left(artifactsList);
		result = testSubject.createArtifactListResponse(serviceName, getArtifactsStatus, servletPath);
	}

	@Test
	public void testBuildResponse() throws Exception {
		DownloadArtifactLogic testSubject;
		int status = 0;
		String errorMessage = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.buildResponse(status, errorMessage);
	}
}
