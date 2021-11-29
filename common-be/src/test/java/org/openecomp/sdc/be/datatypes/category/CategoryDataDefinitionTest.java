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

package org.openecomp.sdc.be.datatypes.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;


class CategoryDataDefinitionTest {

	private CategoryDataDefinition createTestSubject() {
		return new CategoryDataDefinition();
	}
	
	@Test
	void testCopyConstructor() throws Exception {
		CategoryDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		CategoryDataDefinition categoryDataDefinition = new CategoryDataDefinition(testSubject);
	}

	@Test
	void testCategoryDataDefinition() {
		CategoryDataDefinition categoryDataDefinitionForTest = new CategoryDataDefinition();
		categoryDataDefinitionForTest.setName("Category name");
		categoryDataDefinitionForTest.setDisplayName("Category displayName");
		categoryDataDefinitionForTest.setNormalizedName("Category normalizedName");
		List modelsList = new ArrayList();
		modelsList.add("Category models list item");
		categoryDataDefinitionForTest.setModels(modelsList);
		categoryDataDefinitionForTest.setUniqueId("Category uniqueId");
		List iconList = new ArrayList();
		iconList.add("Category icon list item");
		categoryDataDefinitionForTest.setIcons(iconList);
		categoryDataDefinitionForTest.setUseServiceSubstitutionForNestedServices(true);
		List metadataKeysList = new ArrayList();
		metadataKeysList.add("Category metadataKeys list item");
		categoryDataDefinitionForTest.setMetadataKeys(metadataKeysList);

		CategoryDataDefinition categoryDataDefinitionActual = new CategoryDataDefinition(categoryDataDefinitionForTest);

		assertEquals(categoryDataDefinitionForTest.getName(), categoryDataDefinitionActual.getName());
		assertEquals(categoryDataDefinitionForTest.getDisplayName(), categoryDataDefinitionActual.getDisplayName());
		assertEquals(categoryDataDefinitionForTest.getNormalizedName(), categoryDataDefinitionActual.getNormalizedName());
		assertEquals(categoryDataDefinitionForTest.getModels(), categoryDataDefinitionActual.getModels());
		assertEquals(categoryDataDefinitionForTest.getUniqueId(), categoryDataDefinitionActual.getUniqueId());
		assertEquals(categoryDataDefinitionForTest.getIcons(), categoryDataDefinitionActual.getIcons());
		assertEquals(categoryDataDefinitionForTest.isUseServiceSubstitutionForNestedServices(), categoryDataDefinitionActual.isUseServiceSubstitutionForNestedServices());
		assertEquals(categoryDataDefinitionForTest.getMetadataKeys(), categoryDataDefinitionActual.getMetadataKeys());
	}
}
