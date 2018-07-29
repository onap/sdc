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