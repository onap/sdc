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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.common.util.CapabilityTypeNameEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class CapabilityTypeImportManagerTest {
	@InjectMocks
	private CapabilityTypeImportManager manager = new CapabilityTypeImportManager();
	public static final CommonImportManager commonImportManager = Mockito.mock(CommonImportManager.class);
	public static final CapabilityTypeOperation capabilityTypeOperation = Mockito.mock(CapabilityTypeOperation.class);
	public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);

	@BeforeClass
	public static void beforeClass() {
		when(capabilityTypeOperation.addCapabilityType(Mockito.any(CapabilityTypeDefinition.class))).thenAnswer(new Answer<Either<CapabilityTypeDefinition, StorageOperationStatus>>() {
			public Either<CapabilityTypeDefinition, StorageOperationStatus> answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Either<CapabilityTypeDefinition, StorageOperationStatus> ans = Either.left((CapabilityTypeDefinition) args[0]);
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
	public void testCreateCapabilityTypes() throws IOException {
		String ymlContent = getCapabilityTypesYml();
		Either<List<CapabilityTypeDefinition>, ResponseFormat> createCapabilityTypes = manager.createCapabilityTypes(ymlContent);
		assertTrue(createCapabilityTypes.isLeft());

		List<CapabilityTypeDefinition> capabilityTypesList = createCapabilityTypes.left().value();
		assertTrue(capabilityTypesList.size() == 14);
		Map<String, CapabilityTypeDefinition> capibilityTypeMap = new HashMap<>();
		for (CapabilityTypeDefinition capType : capabilityTypesList) {
			capibilityTypeMap.put(capType.getType(), capType);
		}
		assertTrue(capabilityTypesList.size() == 14);

		for (CapabilityTypeNameEnum curr : CapabilityTypeNameEnum.values()) {
			assertTrue(capibilityTypeMap.containsKey(curr.getCapabilityName()));
		}

	}

	private String getCapabilityTypesYml() throws IOException {
		Path filePath = Paths.get("src/test/resources/types/capabilityTypes.yml");
		byte[] fileContent = Files.readAllBytes(filePath);
		String ymlContent = new String(fileContent);
		return ymlContent;
	}

}
