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

import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class ElementOperationTest extends ModelTestBase {

    @javax.annotation.Resource(name = "element-operation")
    private ElementOperation elementOperation;

    @javax.annotation.Resource(name = "janusgraph-generic-dao")
    private JanusGraphGenericDao janusGraphDao;

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

        List<String> artifactTypesCfg = new ArrayList<>();
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

	@Test
	public void testAllDeploymentArtifactTypes() {

		List<String> artifactTypesCfg = new ArrayList<String>();
		artifactTypesCfg.add("type1");
		artifactTypesCfg.add("type2");
		artifactTypesCfg.add("type3");
		configurationManager.getConfiguration().setArtifactTypes(artifactTypesCfg);
		Either<Map<String, Object>, ActionStatus> allDeploymentArtifactTypes = elementOperation
				.getAllDeploymentArtifactTypes();
		assertTrue(allDeploymentArtifactTypes.isLeft());
		assertEquals(artifactTypesCfg.size(), allDeploymentArtifactTypes.left().value().size());

	}

    // @Test
    public void testGetResourceAndServiceCategoty() {
        String id = OperationTestsUtil.deleteAndCreateResourceCategory(CATEGORY, SUBCATEGORY, janusGraphDao);

        Either<CategoryDefinition, ActionStatus> res = elementOperation.getCategory(NodeTypeEnum.ResourceNewCategory, id);
        assertTrue(res.isLeft());
        CategoryDefinition categoryDefinition = (CategoryDefinition) res.left().value();
        assertEquals(CATEGORY, categoryDefinition.getName());
        assertEquals(SUBCATEGORY, categoryDefinition.getSubcategories().get(0).getName());

        id = OperationTestsUtil.deleteAndCreateServiceCategory(CATEGORY, janusGraphDao);

        res = elementOperation.getCategory(NodeTypeEnum.ServiceNewCategory, id);
        assertTrue(res.isLeft());
        categoryDefinition = (CategoryDefinition) res.left().value();
        assertEquals(CATEGORY, categoryDefinition.getName());
	}

	private ElementOperation createTestSubject() {
		return new ElementOperation(new JanusGraphGenericDao(new JanusGraphClient()));
	}

	
	@Test
	public void testGetAllServiceCategories() throws Exception {
		ElementOperation testSubject;
		Either<List<CategoryDefinition>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllServiceCategories();
	}

	
	@Test
	public void testGetAllResourceCategories() throws Exception {
		ElementOperation testSubject;
		Either<List<CategoryDefinition>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllResourceCategories();
	}

	
	@Test
	public void testGetAllProductCategories() throws Exception {
		ElementOperation testSubject;
		Either<List<CategoryDefinition>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllProductCategories();
	}

	
	@Test
	public void testCreateCategory() throws Exception {
		ElementOperation testSubject;
		CategoryDefinition category = new CategoryDefinition();
		NodeTypeEnum nodeType = NodeTypeEnum.AdditionalInfoParameters;
		Either<CategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createCategory(category, nodeType);
	}

	
	@Test
	public void testCreateCategory_1() throws Exception {
		ElementOperation testSubject;
		CategoryDefinition category = new CategoryDefinition();
		NodeTypeEnum nodeType = NodeTypeEnum.ArtifactRef;
		boolean inTransaction = false;
		Either<CategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createCategory(category, nodeType, inTransaction);
	}

	
	@Test
	public void testCreateSubCategory() throws Exception {
		ElementOperation testSubject;
		String categoryId = "";
		SubCategoryDefinition subCategory = null;
		NodeTypeEnum nodeType = null;
		Either<SubCategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createSubCategory(categoryId, subCategory, nodeType);
	}

	
	@Test
	public void testCreateSubCategory_1() throws Exception {
		ElementOperation testSubject;
		String categoryId = "";
		SubCategoryDefinition subCategory = null;
		NodeTypeEnum nodeType = null;
		boolean inTransaction = false;
		Either<SubCategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createSubCategory(categoryId, subCategory, nodeType, inTransaction);
	}

	
	@Test
	public void testCreateGrouping() throws Exception {
		ElementOperation testSubject;
		String subCategoryId = "";
		GroupingDefinition grouping = null;
		NodeTypeEnum nodeType = null;
		Either<GroupingDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createGrouping(subCategoryId, grouping, nodeType);
	}

	
	@Test
	public void testGetAllCategories() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = NodeTypeEnum.Capability;
		boolean inTransaction = false;
		Either<List<CategoryDefinition>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllCategories(nodeType, inTransaction);
	}

	


	

	
	
	
	@Test
	public void testGetCategory() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = NodeTypeEnum.CapabilityType;
		String categoryId = "";
		Either<CategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategory(nodeType, categoryId);
	}

	
	@Test
	public void testGetSubCategory() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = NodeTypeEnum.Group;
		String subCategoryId = "";
		Either<SubCategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubCategory(nodeType, subCategoryId);
	}

	
	@Test
	public void testDeleteCategory() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = NodeTypeEnum.getByName("resource");
		String categoryId = "";
		Either<CategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteCategory(nodeType, categoryId);
	}

	
	@Test
	public void testDeleteSubCategory() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = NodeTypeEnum.Attribute;
		String subCategoryId = "";
		Either<SubCategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteSubCategory(nodeType, subCategoryId);
	}

	
	@Test
	public void testDeleteGrouping() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = NodeTypeEnum.DataType;
		String groupingId = "";
		Either<GroupingDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteGrouping(nodeType, groupingId);
	}

	
	@Test
	public void testIsCategoryUniqueForType() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = null;
		String normalizedName = "";
		Either<Boolean, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isCategoryUniqueForType(nodeType, normalizedName);
	}

	
	@Test
	public void testIsSubCategoryUniqueForCategory() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = null;
		String subCategoryNormName = "";
		String parentCategoryId = "";
		Either<Boolean, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isSubCategoryUniqueForCategory(nodeType, subCategoryNormName, parentCategoryId);
	}

	
	@Test
	public void testIsGroupingUniqueForSubCategory() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = null;
		String groupingNormName = "";
		String parentSubCategoryId = "";
		Either<Boolean, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isGroupingUniqueForSubCategory(nodeType, groupingNormName, parentSubCategoryId);
	}

	
	@Test
	public void testGetSubCategoryUniqueForType() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = null;
		String normalizedName = "";
		Either<SubCategoryDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubCategoryUniqueForType(nodeType, normalizedName);
	}

	
	@Test
	public void testGetGroupingUniqueForType() throws Exception {
		ElementOperation testSubject;
		NodeTypeEnum nodeType = null;
		String groupingNormalizedName = "";
		Either<GroupingDefinition, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupingUniqueForType(nodeType, groupingNormalizedName);
	}

	
	@Test
	public void testGetAllTags() throws Exception {
		ElementOperation testSubject;
		Either<List<Tag>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllTags();
	}

	
	@Test
	public void testGetCategoryData() throws Exception {
		ElementOperation testSubject;
		String name = "";
		NodeTypeEnum type = NodeTypeEnum.DataType;
		Class<T> clazz = null;
		Either<org.openecomp.sdc.be.resources.data.CategoryData, StorageOperationStatus> result;

		// test 1
		testSubject = createTestSubject();
		name = null;
		result = testSubject.getCategoryData(name, type, null);

		// test 2
		testSubject = createTestSubject();
		name = "";
		result = testSubject.getCategoryData(name, type, null);
	}

	


	
	@Test
	public void testGetAllPropertyScopes() throws Exception {
		ElementOperation testSubject;
		Either<List<PropertyScope>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllPropertyScopes();
	}

	
	@Test
	public void testGetAllArtifactTypes() throws Exception {
		ElementOperation testSubject;
		Either<List<ArtifactType>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllArtifactTypes();
	}

	
	@Test
	public void testGetAllDeploymentArtifactTypes() throws Exception {
		ElementOperation testSubject;
		Either<Map<String, Object>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllDeploymentArtifactTypes();
	}

	
	@Test
	public void testGetDefaultHeatTimeout() throws Exception {
		ElementOperation testSubject;
		Either<Integer, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultHeatTimeout();
	}

	
	@Test
	public void testGetResourceTypesMap() throws Exception {
		ElementOperation testSubject;
		Either<Map<String, String>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceTypesMap();
	}

	
	@Test
	public void testGetNewCategoryData() throws Exception {
		ElementOperation testSubject;
		String name = "";
		NodeTypeEnum type = NodeTypeEnum.HeatParameter;
		Class<T> clazz = null;
		Either<CategoryData, StorageOperationStatus> result;

		// test 1
		testSubject = createTestSubject();
		name = null;
		result = testSubject.getNewCategoryData(name, type, null);

		// test 2
		testSubject = createTestSubject();
		name = "";
		result = testSubject.getNewCategoryData(name, type, null);
    }
}
