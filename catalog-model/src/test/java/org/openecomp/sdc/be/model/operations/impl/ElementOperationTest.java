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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ElementOperationTest extends ModelTestBase {

	@javax.annotation.Resource(name = "element-operation")
	private ElementOperation elementOperation;

	@javax.annotation.Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	private static String CATEGORY = "category";
	private static String SUBCATEGORY = "subcategory";

	@BeforeClass
	public static void setupBeforeClass() {
		// ExternalConfiguration.setAppName("catalog-model");
		// String appConfigDir = "src/test/resources/config/catalog-model";
		// ConfigurationSource configurationSource = new
		// FSConfigurationSource(ExternalConfiguration.getChangeListener(),
		// appConfigDir);

		ModelTestBase.init();

	}

	@Test
	public void testGetArtifactsTypes() {

		List<String> artifactTypesCfg = new ArrayList<String>();
		artifactTypesCfg.add("type1");
		artifactTypesCfg.add("type2");
		artifactTypesCfg.add("type3");
		artifactTypesCfg.add("type4");
		configurationManager.getConfiguration().setArtifactTypes(artifactTypesCfg);
		Either<List<ArtifactType>, ActionStatus> allArtifactTypes = elementOperation.getAllArtifactTypes();
		assertTrue(allArtifactTypes.isLeft());
		assertEquals(artifactTypesCfg.size(), allArtifactTypes.left().value().size());

		artifactTypesCfg.remove(0);
		allArtifactTypes = elementOperation.getAllArtifactTypes();
		assertTrue(allArtifactTypes.isLeft());
		assertEquals(artifactTypesCfg.size(), allArtifactTypes.left().value().size());

		artifactTypesCfg.add("type5");
	}

	// @Test
	public void testGetResourceAndServiceCategoty() {
		String id = OperationTestsUtil.deleteAndCreateResourceCategory(CATEGORY, SUBCATEGORY, titanDao);

		Either<CategoryDefinition, ActionStatus> res = elementOperation.getCategory(NodeTypeEnum.ResourceNewCategory, id);
		assertTrue(res.isLeft());
		CategoryDefinition categoryDefinition = (CategoryDefinition) res.left().value();
		assertEquals(CATEGORY, categoryDefinition.getName());
		assertEquals(SUBCATEGORY, categoryDefinition.getSubcategories().get(0).getName());

		id = OperationTestsUtil.deleteAndCreateServiceCategory(CATEGORY, titanDao);

		res = elementOperation.getCategory(NodeTypeEnum.ServiceNewCategory, id);
		assertTrue(res.isLeft());
		categoryDefinition = (CategoryDefinition) res.left().value();
		assertEquals(CATEGORY, categoryDefinition.getName());
	}
}
