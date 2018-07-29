package org.openecomp.sdc.be.resources.impl;

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ICatalogDAO;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.exception.ResourceDAOException;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

public class ResourceUploaderTest extends DAOConfDependentTest{

	@InjectMocks
	ResourceUploader testSubject;

	@Mock
	ICatalogDAO resourceDAO;

	@Before
	public void setUpMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCtor() throws Exception {
		new ResourceUploader();
		ICatalogDAO resourcetDAO = Mockito.mock(ICatalogDAO.class);
		new ResourceUploader(resourcetDAO);
	}
	
	@Test
	public void testInit() throws Exception {
		// default test
		testSubject.init();
	}

	@Test
	public void testGetResourceDAO() throws Exception {
		ICatalogDAO result;

		// default test
		result = testSubject.getResourceDAO();
	}

	@Test
	public void testSetResourceDAO() throws Exception {
		ICatalogDAO resourceDAO = null;

		// default test
		testSubject.setResourceDAO(resourceDAO);
	}

	@Test
	public void testSaveArtifact() throws Exception {
		ESArtifactData artifactData = new ESArtifactData();
		artifactData.setId("mock");
		boolean isReload = false;
		ResourceUploadStatus result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.left(new ESArtifactData());
		Mockito.when(resourceDAO.getArtifact(Mockito.anyString())).thenReturn(value);
		
		// default test
		result = testSubject.saveArtifact(artifactData, isReload);
	}
	
	@Test
	public void testSaveArtifact2() throws Exception {
		ESArtifactData artifactData = new ESArtifactData();
		artifactData.setId("mock");
		boolean isReload = true;
		ResourceUploadStatus result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.left(new ESArtifactData());
		Mockito.when(resourceDAO.getArtifact(Mockito.anyString())).thenReturn(value);
		
		// default test
		result = testSubject.saveArtifact(artifactData, isReload);
	}

	@Test
	public void testSaveArtifactFailed() throws Exception {
		ESArtifactData artifactData = new ESArtifactData();
		artifactData.setId("mock");
		boolean isReload = false;
		ResourceUploadStatus result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.right(ResourceUploadStatus.ALREADY_EXIST);
		Mockito.when(resourceDAO.getArtifact(Mockito.anyString())).thenReturn(value);
		
		// default test
		result = testSubject.saveArtifact(artifactData, isReload);
	}
	
	@Test
	public void testSaveArtifactFailedException() throws Exception {
		ESArtifactData artifactData = new ESArtifactData();
		artifactData.setId("mock");
		boolean isReload = false;
		ResourceUploadStatus result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.right(ResourceUploadStatus.ALREADY_EXIST);
		Mockito.when(resourceDAO.getArtifact(Mockito.anyString())).thenReturn(value);
		Mockito.doThrow(new ResourceDAOException("mock")).when(resourceDAO).writeArtifact(Mockito.any());
		
		// default test
		result = testSubject.saveArtifact(artifactData, isReload);
	}
	
	@Test
	public void testSaveArtifactFailedDAONull() throws Exception {
		ESArtifactData artifactData = new ESArtifactData();
		boolean isReload = false;
		ResourceUploadStatus result;
		
		// default test
		ResourceUploader resourceUploader = new ResourceUploader(null);
		result = resourceUploader.saveArtifact(artifactData, isReload);
	}
	
	@Test
	public void testUpdateArtifact() throws Exception {
		ESArtifactData artifactUpdateData = new ESArtifactData();
		artifactUpdateData.setId("mock");
		ResourceUploadStatus result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.left(new ESArtifactData());
		Mockito.when(resourceDAO.getArtifact(Mockito.anyString())).thenReturn(value);
		
		// default test
		result = testSubject.updateArtifact(artifactUpdateData);
	}

	@Test
	public void testUpdateArtifactNotFound() throws Exception {
		ESArtifactData artifactUpdateData = new ESArtifactData();
		artifactUpdateData.setId("mock");
		ResourceUploadStatus result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.right(ResourceUploadStatus.NOT_EXIST);
		Mockito.when(resourceDAO.getArtifact(Mockito.anyString())).thenReturn(value);
		
		// default test
		result = testSubject.updateArtifact(artifactUpdateData);
	}
	
	@Test
	public void testUpdateArtifactException() throws Exception {
		ESArtifactData artifactUpdateData = new ESArtifactData();
		artifactUpdateData.setId("mock");
		ResourceUploadStatus result;

		Either<ESArtifactData, ResourceUploadStatus> value = Either.left(new ESArtifactData());
		Mockito.when(resourceDAO.getArtifact(Mockito.anyString())).thenReturn(value);
		Mockito.doThrow(new ResourceDAOException("mock")).when(resourceDAO).writeArtifact(Mockito.any());
		
		// default test
		result = testSubject.updateArtifact(artifactUpdateData);
	}
	
	@Test
	public void testUpdateArtifactDAONull() throws Exception {
		ESArtifactData artifactUpdateData = new ESArtifactData();
		ResourceUploadStatus result;
		
		// default test
		ResourceUploader resourceUploader = new ResourceUploader();
		result = resourceUploader.updateArtifact(artifactUpdateData);
	}
	
	@Test
	public void testGetArtifact() throws Exception {
		String id = "";
		Either<ESArtifactData, ResourceUploadStatus> result;

		// default test
		result = testSubject.getArtifact(id);
	}

	@Test
	public void testGetArtifactNull() throws Exception {
		String id = "";
		Either<ESArtifactData, ResourceUploadStatus> result;

		// default test
		ResourceUploader resourceUploader = new ResourceUploader();
		result = resourceUploader.getArtifact(id);
	}
	
	@Test
	public void testDeleteArtifact() throws Exception {
		String id = "";

		// default test
		testSubject.deleteArtifact(id);
	}

	@Test
	public void testUpdateArtifact_1() throws Exception {
		ESArtifactData artifactUpdateData = new ESArtifactData();
		ESArtifactData existData = new ESArtifactData();
		ResourceUploadStatus result;

		// default test
		result = Deencapsulation.invoke(testSubject, "updateArtifact",
				artifactUpdateData, existData);
	}

	@Test
	public void testUpdateData() throws Exception {
		ESArtifactData artifactUpdateData = new ESArtifactData();
		ESArtifactData existData = new ESArtifactData();

		// default test
		Deencapsulation.invoke(testSubject, "updateData", artifactUpdateData, existData);
	}

	@Test
	public void testDeleteAllArtifacts() throws Exception {
		// default test
		testSubject.deleteAllArtifacts();
	}
}