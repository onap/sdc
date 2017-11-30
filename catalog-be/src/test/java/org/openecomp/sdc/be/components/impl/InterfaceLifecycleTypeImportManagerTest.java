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

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;

import fj.data.Either;

public class InterfaceLifecycleTypeImportManagerTest {

	@InjectMocks
	private InterfaceLifecycleTypeImportManager importManager = new InterfaceLifecycleTypeImportManager();
	public static final CommonImportManager commonImportManager = Mockito.mock(CommonImportManager.class);
	public static final IInterfaceLifecycleOperation interfaceLifecycleOperation = Mockito.mock(IInterfaceLifecycleOperation.class);
	public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);

	static Logger log = Mockito.spy(Logger.class);

	@BeforeClass
	public static void beforeClass() throws IOException {
		InterfaceLifecycleTypeImportManager.setLog(log);
		when(interfaceLifecycleOperation.createInterfaceType(Mockito.any(InterfaceDefinition.class))).thenAnswer(new Answer<Either<InterfaceDefinition, StorageOperationStatus>>() {
			public Either<InterfaceDefinition, StorageOperationStatus> answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Either<InterfaceDefinition, StorageOperationStatus> ans = Either.left((InterfaceDefinition) args[0]);
				return ans;
			}

		});
		when(commonImportManager.createElementTypesFromYml(Mockito.anyString(), Mockito.any())).thenCallRealMethod();
	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void importLiecycleTest() throws IOException {
		String ymlContent = getYmlContent();
		Either<List<InterfaceDefinition>, ResponseFormat> createCapabilityTypes = importManager.createLifecycleTypes(ymlContent);
		assertTrue(createCapabilityTypes.isLeft());

	}

	private String getYmlContent() throws IOException {
		Path filePath = Paths.get("src/test/resources/types/interfaceLifecycleTypes.yml");
		byte[] fileContent = Files.readAllBytes(filePath);
		String ymlContent = new String(fileContent);
		return ymlContent;
	}
}
