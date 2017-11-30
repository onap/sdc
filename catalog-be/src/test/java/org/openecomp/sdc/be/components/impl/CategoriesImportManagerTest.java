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
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;

import fj.data.Either;

public class CategoriesImportManagerTest {
	@InjectMocks
	static CategoriesImportManager importManager = new CategoriesImportManager();
	public static final IElementOperation elementOperation = Mockito.mock(IElementOperation.class);
	public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);

	static Logger log = Mockito.spy(Logger.class);

	private static SubCategoryDefinition subcategory;

	@BeforeClass
	public static void beforeClass() throws IOException {
		InterfaceLifecycleTypeImportManager.setLog(log);

		subcategory = new SubCategoryDefinition();
		subcategory.setUniqueId("123");

		when(elementOperation.createCategory(Mockito.any(CategoryDefinition.class), Mockito.any(NodeTypeEnum.class))).thenAnswer((Answer<Either<CategoryDefinition, ActionStatus>>) invocation -> {
			Object[] args = invocation.getArguments();
			CategoryDefinition category = (CategoryDefinition) args[0];
			category.setUniqueId("123");
			Either<CategoryDefinition, ActionStatus> ans = Either.left(category);
			return ans;
		});
		when(elementOperation.createSubCategory(Mockito.any(String.class), Mockito.any(SubCategoryDefinition.class), Mockito.any(NodeTypeEnum.class))).thenAnswer(new Answer<Either<SubCategoryDefinition, ActionStatus>>() {
			public Either<SubCategoryDefinition, ActionStatus> answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				// subcategory.setName(((SubCategoryDefinition)args[0]).getName());
				Either<SubCategoryDefinition, ActionStatus> ans = Either.left(subcategory);
				return ans;
			}

		});

		// when(Mockito.any(SubCategoryDefinition.class).getUniqueId()).thenReturn("123");
	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void importCategoriesTest() throws IOException {
		String ymlContent = getYmlContent();
		Either<Map<String, List<CategoryDefinition>>, ResponseFormat> createCapabilityTypes = importManager.createCategories(ymlContent);
		assertTrue(createCapabilityTypes.isLeft());

	}

	private String getYmlContent() throws IOException {
		Path filePath = Paths.get("src/test/resources/types/categoryTypes.yml");
		byte[] fileContent = Files.readAllBytes(filePath);
		String ymlContent = new String(fileContent);
		return ymlContent;
	}
}
