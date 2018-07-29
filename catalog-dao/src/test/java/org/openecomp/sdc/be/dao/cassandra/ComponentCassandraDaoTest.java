package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class ComponentCassandraDaoTest {

	@InjectMocks
	ComponentCassandraDao testSubject;

	@Mock
	CassandraClient clientMock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInit() throws Exception {

		// default test
		testSubject.init();

		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		testSubject.init();
	}

	@Test
	public void testInitException() throws Exception {

		// default test
		testSubject.init();

		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either
				.right(CassandraOperationStatus.CLUSTER_NOT_CONNECTED);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		try {
			testSubject.init();
		} catch (Exception e) {
			assertTrue(e.getClass() == RuntimeException.class);
		}
	}

	@Test
	public void testGetComponents() throws Exception {
		List<String> ids;
		Either<List<ComponentCacheData>, ActionStatus> result;

		// test 1
		ids = null;
		result = testSubject.getComponents(ids);

		
		// test 2
		ids = new LinkedList<>();
		result = testSubject.getComponents(ids);
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Result<ComponentCacheData> value2 = Mockito.mock(Result.class);
		Mockito.when(componentCacheAccessorMock.getComponents(Mockito.any())).thenReturn(value2);
		List<ComponentCacheData> value3 = new LinkedList<>();
		value3.add(new ComponentCacheData("mock"));
		Mockito.when(value2.all()).thenReturn(value3);
		testSubject.init();
		
		ids.add("mock");
		testSubject.getComponents(ids);
	}
	
	@Test
	public void testGetComponentsNull() throws Exception {
		List<String> ids = new LinkedList<>();
		Either<List<ComponentCacheData>, ActionStatus> result;
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Mockito.when(componentCacheAccessorMock.getComponents(Mockito.any())).thenReturn(null);
		testSubject.init();
		
		ids.add("mock");
		testSubject.getComponents(ids);
	}
	
	@Test
	public void testGetComponentsException() throws Exception {
		List<String> ids = new LinkedList<>();
		Either<List<ComponentCacheData>, ActionStatus> result;
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Mockito.when(componentCacheAccessorMock.getComponents(Mockito.any())).thenThrow(RuntimeException.class);
		testSubject.init();
		
		ids.add("mock");
		testSubject.getComponents(ids);
	}
	
	@Test
	public void testGetAllComponentIdTimeAndType() throws Exception {
		Either<List<ComponentCacheData>, ActionStatus> result;

		// default test
		result = testSubject.getAllComponentIdTimeAndType();
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Result<ComponentCacheData> value2 = Mockito.mock(Result.class);
		Mockito.when(componentCacheAccessorMock.getAllComponentIdTimeAndType()).thenReturn(value2);
		List<ComponentCacheData> value3 = new LinkedList<>();
		value3.add(new ComponentCacheData("mock"));
		Mockito.when(value2.all()).thenReturn(value3);
		testSubject.init();
		
		testSubject.getAllComponentIdTimeAndType();
	}

	@Test
	public void testGetAllComponentIdTimeAndTypeNull() throws Exception {
		Either<List<ComponentCacheData>, ActionStatus> result;

		// default test
		result = testSubject.getAllComponentIdTimeAndType();
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Mockito.when(componentCacheAccessorMock.getAllComponentIdTimeAndType()).thenReturn(null);
		testSubject.init();
		
		result = testSubject.getAllComponentIdTimeAndType();
	}
	
	@Test
	public void testGetComponent() throws Exception {
		String id = "";
		Either<ComponentCacheData, ActionStatus> result;

		// test 1
		id = null;
		result = testSubject.getComponent(id);
		//Assert.assertEquals(null, result);

		// test 2
		id = "";
		result = testSubject.getComponent(id);
		//Assert.assertEquals(null, result);
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Result<ComponentCacheData> value2 = Mockito.mock(Result.class);
		Mockito.when(componentCacheAccessorMock.getComponent(Mockito.any())).thenReturn(value2);
		ComponentCacheData value3 = new ComponentCacheData();
		Mockito.when(value2.one()).thenReturn(value3);
		testSubject.init();
		
		result = testSubject.getComponent(id);
	}
	
	@Test
	public void testGetComponentNull1() throws Exception {
		String id = "";
		Either<ComponentCacheData, ActionStatus> result;
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Mockito.when(componentCacheAccessorMock.getComponent(Mockito.any())).thenReturn(null);
		testSubject.init();
		
		result = testSubject.getComponent(id);
	}
	
	@Test
	public void testGetComponentNull2() throws Exception {
		String id = "";
		Either<ComponentCacheData, ActionStatus> result;

		// test 1
		id = null;
		result = testSubject.getComponent(id);
		//Assert.assertEquals(null, result);

		// test 2
		id = "";
		result = testSubject.getComponent(id);
		//Assert.assertEquals(null, result);
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Result<ComponentCacheData> value2 = Mockito.mock(Result.class);
		Mockito.when(componentCacheAccessorMock.getComponent(Mockito.any())).thenReturn(value2);
		Mockito.when(value2.one()).thenReturn(null);
		testSubject.init();
		
		result = testSubject.getComponent(id);
	}
	
	@Test
	public void testSaveComponent() throws Exception {
		ComponentCacheData componentCacheData = null;
		CassandraOperationStatus result;

		// default test
		result = testSubject.saveComponent(componentCacheData);
	}

	@Test
	public void testIsTableEmpty() throws Exception {
		String tableName = "";
		Either<Boolean, CassandraOperationStatus> result;

		// default test
		result = testSubject.isTableEmpty(tableName);
	}

	@Test
	public void testGetComponents_1() throws Exception {
		Map<String, Long> idToTimestampMap = null;
		Either<ImmutablePair<List<ComponentCacheData>, Set<String>>, ActionStatus> result;

		// test 1
		idToTimestampMap = null;
		result = testSubject.getComponents(idToTimestampMap);
		//Assert.assertEquals(null, result);
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Result<ComponentCacheData> value2 = Mockito.mock(Result.class);
		Mockito.when(componentCacheAccessorMock.getComponents(Mockito.any(List.class))).thenReturn(value2);
		List<ComponentCacheData> value3 = new LinkedList<>();
		ComponentCacheData e = new ComponentCacheData("mock");
		Mockito.when(value2.all()).thenReturn(value3);
		testSubject.init();
		
		idToTimestampMap = new HashMap<>();
		idToTimestampMap.put("mock", 0L);
		e.setModificationTime(new Date());
		value3.add(e);
		result = testSubject.getComponents(idToTimestampMap);
	}

	@Test
	public void testGetComponents_1Null() throws Exception {
		Map<String, Long> idToTimestampMap = null;
		Either<ImmutablePair<List<ComponentCacheData>, Set<String>>, ActionStatus> result;
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Mockito.when(componentCacheAccessorMock.getComponents(Mockito.any(List.class))).thenReturn(null);
		testSubject.init();
		
		idToTimestampMap = new HashMap<>();
		idToTimestampMap.put("mock", 0L);
		result = testSubject.getComponents(idToTimestampMap);
	}
	
	@Test
	public void testGetComponents_1Exception() throws Exception {
		Map<String, Long> idToTimestampMap = null;
		Either<ImmutablePair<List<ComponentCacheData>, Set<String>>, ActionStatus> result;
		
		Session sessMock = Mockito.mock(Session.class);
		MappingManager mappMock = Mockito.mock(MappingManager.class);
		ComponentCacheAccessor componentCacheAccessorMock = Mockito.mock(ComponentCacheAccessor.class);
		ImmutablePair<Session, MappingManager> ipMock = ImmutablePair.of(sessMock, mappMock);
		
		Mockito.when(clientMock.isConnected()).thenReturn(true);
		Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> value = Either.left(ipMock);
		Mockito.when(clientMock.connect(Mockito.any())).thenReturn(value);
		Mockito.when(mappMock.createAccessor(ComponentCacheAccessor.class)).thenReturn(componentCacheAccessorMock);
		
		Mockito.when(componentCacheAccessorMock.getComponents(Mockito.any(List.class))).thenThrow(RuntimeException.class);
		testSubject.init();
		
		idToTimestampMap = new HashMap<>();
		idToTimestampMap.put("mock", 0L);
		result = testSubject.getComponents(idToTimestampMap);
	}
	
	@Test
	public void testDeleteComponent() throws Exception {
		String id = "";
		CassandraOperationStatus result;

		// default test
		result = testSubject.deleteComponent(id);
	}
}