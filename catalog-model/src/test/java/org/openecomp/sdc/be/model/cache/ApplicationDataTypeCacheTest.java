package org.openecomp.sdc.be.model.cache;

import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.resources.data.DataTypeData;
import org.openecomp.sdc.be.unittests.utils.ModelConfDependentTest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApplicationDataTypeCacheTest extends ModelConfDependentTest{

	@InjectMocks
	private ApplicationDataTypeCache testSubject;
	
	@Mock
	PropertyOperation propertyOperation;

	@Before
	public void setUpMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
	}


	@Test
	public void testInit() throws Exception {
		testSubject.init();
	}

	@Test
	public void testDestroy() throws Exception {
		testSubject.init();
		Deencapsulation.invoke(testSubject, "destroy");
	}

	@Test
	public void testShutdownExecutor() throws Exception {

		// default test
		Deencapsulation.invoke(testSubject, "shutdownExecutor");
	}

	@Test
	public void testGetAllDataTypesFromGraph() throws Exception {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getAllDataTypesFromGraph");
	}

	@Test
	public void testGetAll() throws Exception {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> result;

		// default test
		result = testSubject.getAll();
	}

	@Test
	public void testGet() throws Exception {
		String uniqueId = "";
		Either<DataTypeDefinition, JanusGraphOperationStatus> result;

		// default test
		result = testSubject.get(uniqueId);
	}

	@Test
	public void testGet2() throws Exception {
		String uniqueId = "";
		Either<DataTypeDefinition, JanusGraphOperationStatus> result;

		HashMap<String, DataTypeDefinition> a = new HashMap<>();
		DataTypeDefinition value1 = new DataTypeDefinition();
		value1.setUniqueId("mock");
		a.put("mock", value1);
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> value = Either.left(a);
		Mockito.when(propertyOperation.getAllDataTypes()).thenReturn(value);
		// default test
		Deencapsulation.invoke(testSubject, "replaceAllData");
		result = testSubject.get(uniqueId);
	}
	
	@Test
	public void testRun() throws Exception {
		testSubject.run();
	}

	@Test
	public void testRun2() throws Exception {
		Either<List<DataTypeData>, JanusGraphOperationStatus> value = Either.right(
        JanusGraphOperationStatus.GENERAL_ERROR);
		Mockito.when(propertyOperation.getAllDataTypeNodes()).thenReturn(value);
		testSubject.run();
	}
	
	@Test
	public void testRun3() throws Exception {
		LinkedList<DataTypeData> a = new LinkedList<>();
		a.add(new DataTypeData());
		Either<List<DataTypeData>, JanusGraphOperationStatus> value = Either.left(a);
		Mockito.when(propertyOperation.getAllDataTypeNodes()).thenReturn(value);
		
		HashMap<String, DataTypeDefinition> a1 = new HashMap<>();
		DataTypeDefinition value1 = new DataTypeDefinition();
		value1.setUniqueId("mock");
		a1.put("mock", value1);
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> value2 = Either.left(a1);
		Mockito.when(propertyOperation.getAllDataTypes()).thenReturn(value2);
		
		Deencapsulation.invoke(testSubject, "replaceAllData");
		testSubject.run();
	}
	
	@Test
	public void testCompareDataTypes() throws Exception {
		Map<String, ImmutablePair<Long, Long>> dataTypeNameToModificationTime = new HashMap<>();
		Map<String, ImmutablePair<Long, Long>> currentDataTypeToModificationTime = new HashMap<>();
		boolean result;

		// default test
		result = Deencapsulation.invoke(testSubject, "compareDataTypes", dataTypeNameToModificationTime, currentDataTypeToModificationTime);
	}

	@Test
	public void testCompareDataTypes2() throws Exception {
		Map<String, ImmutablePair<Long, Long>> dataTypeNameToModificationTime = new HashMap<>();
		Map<String, ImmutablePair<Long, Long>> currentDataTypeToModificationTime = new HashMap<>();
		boolean result;
		
		currentDataTypeToModificationTime.put("mock", ImmutablePair.of(1L, 2L));
		dataTypeNameToModificationTime.put("mock", ImmutablePair.of(5L, 6L));
		
		// default test
		result = Deencapsulation.invoke(testSubject, "compareDataTypes", dataTypeNameToModificationTime, currentDataTypeToModificationTime);
	}
	
	@Test
	public void testReplaceAllData() throws Exception {
		HashMap<String, DataTypeDefinition> a = new HashMap<>();
		DataTypeDefinition value1 = new DataTypeDefinition();
		value1.setUniqueId("mock");
		a.put("mock", value1);
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> value = Either.left(a);
		Mockito.when(propertyOperation.getAllDataTypes()).thenReturn(value);
		// default test
		Deencapsulation.invoke(testSubject, "replaceAllData");
	}
	
	@Test
	public void testReplaceAllData2() throws Exception {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> value = Either.right(
        JanusGraphOperationStatus.GENERAL_ERROR);
		Mockito.when(propertyOperation.getAllDataTypes()).thenReturn(value);
		// default test
		Deencapsulation.invoke(testSubject, "replaceAllData");
	}
}