package org.openecomp.sdc.be.model.cache.jobs;

import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.cache.DaoInfo;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;

import java.util.function.Function;

public class CheckAndUpdateJobTest {

	CheckAndUpdateJob testSubject;

	@Mock
	DaoInfo daoInfo;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		testSubject = new CheckAndUpdateJob(daoInfo, "mock", NodeTypeEnum.Resource, 0L);
	}

	@Test
	public void testDoWorkException() throws Exception {
		Object result;

		// default test
		ToscaOperationFacade answer = Mockito.mock(ToscaOperationFacade.class);
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer);

		result = testSubject.doWork();
	}

	@Test
	public void testDoWorkFalse() throws Exception {
		Object result;

		// default test
		ComponentCache answer = Mockito.mock(ComponentCache.class);
		Mockito.when(answer.getComponentAndTime(Mockito.anyString(), Mockito.any(Function.class)))
				.thenReturn(Either.right(ActionStatus.ACCEPTED));
		Mockito.when(daoInfo.getComponentCache()).thenReturn(answer);
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);

		result = testSubject.doWork();
	}

	@Test
	public void testDoWorkResourceNotFound() throws Exception {
		Object result;

		// default test
		ComponentCache answer = Mockito.mock(ComponentCache.class);
		Either<ImmutablePair<Component, Long>, ActionStatus> value;
		Mockito.when(answer.getComponentAndTime(Mockito.anyString(), Mockito.any(Function.class)))
				.thenReturn(Either.right(ActionStatus.RESOURCE_NOT_FOUND));
		Mockito.when(daoInfo.getComponentCache()).thenReturn(answer);
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		Mockito.when(answer1.getComponentMetadata(Mockito.anyString()))
				.thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);

		result = testSubject.doWork();
	}

	@Test
	public void testDoWork() throws Exception {
		Object result;

		// default test
		ComponentCache answer = Mockito.mock(ComponentCache.class);
		ImmutablePair<Component, Long> value = ImmutablePair.of(new Resource(), 0L);
		Mockito.when(answer.getComponentAndTime(Mockito.anyString(), Mockito.any(Function.class)))
				.thenReturn(Either.left(value));
		Mockito.when(daoInfo.getComponentCache()).thenReturn(answer);
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		Mockito.when(answer1.getComponentMetadata(Mockito.anyString()))
				.thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);

		result = testSubject.doWork();
	}

	@Test
	public void testDoWork1() throws Exception {
		Object result;

		// default test
		ComponentCache answer = Mockito.mock(ComponentCache.class);
		ImmutablePair<Component, Long> value = ImmutablePair.of(new Resource(), 1L);
		Mockito.when(answer.getComponentAndTime(Mockito.anyString(), Mockito.any(Function.class)))
				.thenReturn(Either.left(value));
		Mockito.when(daoInfo.getComponentCache()).thenReturn(answer);
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);

		result = testSubject.doWork();
	}

	@Test
	public void testUpdateCache() throws Exception {
		String componentId = "mock";
		NodeTypeEnum nodeTypeEnum = null;
		Long timestamp = null;
		boolean result;

		// default test
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		ComponentMetadataData a = new ResourceMetadataData();
		a.getMetadataDataDefinition().setLastUpdateDate(0L);
		Mockito.when(answer1.getComponentMetadata(Mockito.anyString()))
				.thenReturn(Either.left(a));
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);
		
		Mockito.when(answer1.getToscaElement(Mockito.anyString())).thenReturn(Either.left(new Resource()));
		ComponentCache compCache = Mockito.mock(ComponentCache.class);
		Mockito.when(compCache.setComponent(Mockito.any(), Mockito.any())).thenReturn(true);
		Mockito.when(daoInfo.getComponentCache()).thenReturn(compCache);
		
		result = Deencapsulation.invoke(testSubject, "updateCache", componentId, NodeTypeEnum.Resource, 0L);
	}
	
	@Test
	public void testUpdateCacheFailedToUpdateCache() throws Exception {
		String componentId = "mock";
		NodeTypeEnum nodeTypeEnum = null;
		Long timestamp = null;
		boolean result;

		// default test
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		ComponentMetadataData a = new ResourceMetadataData();
		a.getMetadataDataDefinition().setLastUpdateDate(0L);
		Mockito.when(answer1.getComponentMetadata(Mockito.anyString()))
				.thenReturn(Either.left(a));
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);
		
		Mockito.when(answer1.getToscaElement(Mockito.anyString())).thenReturn(Either.left(new Resource()));
		ComponentCache compCache = Mockito.mock(ComponentCache.class);
		Mockito.when(daoInfo.getComponentCache()).thenReturn(compCache);
		
		result = Deencapsulation.invoke(testSubject, "updateCache", componentId, NodeTypeEnum.Resource, 0L);
	}
	
	@Test
	public void testUpdateCacheToscaElemntNotFound() throws Exception {
		String componentId = "mock";
		NodeTypeEnum nodeTypeEnum = null;
		Long timestamp = null;
		boolean result;

		// default test
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		ComponentMetadataData a = new ResourceMetadataData();
		a.getMetadataDataDefinition().setLastUpdateDate(0L);
		Mockito.when(answer1.getComponentMetadata(Mockito.anyString()))
				.thenReturn(Either.left(a));
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);
		
		Mockito.when(answer1.getToscaElement(Mockito.anyString())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		
		result = Deencapsulation.invoke(testSubject, "updateCache", componentId, NodeTypeEnum.Resource, 0L);
	}
	
	@Test
	public void testUpdateCacheNotUpdatedTimestamp() throws Exception {
		String componentId = "mock";
		NodeTypeEnum nodeTypeEnum = null;
		Long timestamp = null;
		boolean result;

		// default test
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		ComponentMetadataData a = new ResourceMetadataData();
		a.getMetadataDataDefinition().setLastUpdateDate(1L);
		Mockito.when(answer1.getComponentMetadata(Mockito.anyString()))
				.thenReturn(Either.left(a));
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);
		
		result = Deencapsulation.invoke(testSubject, "updateCache", componentId, NodeTypeEnum.Resource, 0L);
	}
	
	@Test
	public void testUpdateCacheNotFound() throws Exception {
		String componentId = "mock";
		NodeTypeEnum nodeTypeEnum = null;
		Long timestamp = null;
		boolean result;

		// default test
		ToscaOperationFacade answer1 = Mockito.mock(ToscaOperationFacade.class);
		Mockito.when(answer1.getComponentMetadata(Mockito.anyString()))
				.thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
		Mockito.when(daoInfo.getToscaOperationFacade()).thenReturn(answer1);
		
		result = Deencapsulation.invoke(testSubject, "updateCache", componentId, NodeTypeEnum.Resource, 0L);
	}
}