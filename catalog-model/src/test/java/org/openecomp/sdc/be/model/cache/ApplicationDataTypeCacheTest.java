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

package org.openecomp.sdc.be.model.cache;

import fj.data.Either;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.springframework.context.ApplicationEventPublisher;

public class ApplicationDataTypeCacheTest extends ModelConfDependentTest{

	@InjectMocks
	private ApplicationDataTypeCache testSubject;
	
	@Mock
	PropertyOperation propertyOperation;

	@Mock
	ApplicationEventPublisher applicationEventPublisher;

	@Before
	public void setUpMocks() {
		MockitoAnnotations.initMocks(this);
	}


	@Test
	public void testInit() throws Exception {
		testSubject.init();
	}

	@Test
	public void testDestroy()  {
		testSubject.init();
		testSubject.destroy();
	}

	@Test
	public void testShutdownExecutor() {

		// default test
		testSubject.shutdownExecutor();
	}

	@Test
	public void testGetAllDataTypesFromGraph(){
		// default test
		testSubject.getAllDataTypesFromGraph();
	}

	@Test
	public void testGetAll() {
		// default test
		testSubject.getAll();
	}

	@Test
	public void testGet() throws Exception {
		String uniqueId = "";
		// default test
		testSubject.get(uniqueId);
	}

	@Test
	public void testGet2() {
		String uniqueId = "";

		HashMap<String, DataTypeDefinition> a = new HashMap<>();
		DataTypeDefinition value1 = new DataTypeDefinition();
		value1.setUniqueId("mock");
		a.put("mock", value1);
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> value = Either.left(a);
		Mockito.when(propertyOperation.getAllDataTypes()).thenReturn(value);
		// default test
		testSubject.replaceAllData();
		testSubject.get(uniqueId);
	}
	
	@Test
	public void testRun()  {
		testSubject.run();
	}

	@Test
	public void testRun2()  {
		Either<List<DataTypeData>, JanusGraphOperationStatus> value = Either.right(
        JanusGraphOperationStatus.GENERAL_ERROR);
		Mockito.when(propertyOperation.getAllDataTypeNodes()).thenReturn(value);
		testSubject.run();
	}
	
	@Test
	public void testRun3() {
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
		
		testSubject.replaceAllData();
		testSubject.run();
	}
	
	@Test
	public void testCompareDataTypes() {
		Map<String, ImmutablePair<Long, Long>> dataTypeNameToModificationTime = new HashMap<>();
		Map<String, ImmutablePair<Long, Long>> currentDataTypeToModificationTime = new HashMap<>();
		boolean result;

		// default test
		testSubject.compareDataTypes(dataTypeNameToModificationTime, currentDataTypeToModificationTime);
	}

	@Test
	public void testCompareDataTypes2()  {
		Map<String, ImmutablePair<Long, Long>> dataTypeNameToModificationTime = new HashMap<>();
		Map<String, ImmutablePair<Long, Long>> currentDataTypeToModificationTime = new HashMap<>();

		currentDataTypeToModificationTime.put("mock", ImmutablePair.of(1L, 2L));
		dataTypeNameToModificationTime.put("mock", ImmutablePair.of(5L, 6L));
		
		// default test
		testSubject.compareDataTypes(dataTypeNameToModificationTime, currentDataTypeToModificationTime);
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
		testSubject.replaceAllData();
	}
	
	@Test
	public void testReplaceAllData2() throws Exception {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> value = Either.right(
        JanusGraphOperationStatus.GENERAL_ERROR);
		Mockito.when(propertyOperation.getAllDataTypes()).thenReturn(value);
		// default test
		testSubject.replaceAllData();
	}
}
