package org.openecomp.sdc.be.components.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Generated;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.datamodel.api.CategoryTypeEnum;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentMetadataData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.be.ui.model.UiCategories;
import org.openecomp.sdc.exception.ResponseFormat;

import com.google.common.util.concurrent.Service;
import com.thinkaurelius.titan.core.TitanGraph;

import fj.data.Either;
import io.swagger.annotations.Tag;


public class ElementBusinessLogicTest {

	private ElementBusinessLogic createTestSubject() {
		return new ElementBusinessLogic();
	}

	
	@Test
	public void testGetFollowed() throws Exception {
		ElementBusinessLogic testSubject;
		User user = null;
		Either<Map<String, List<? extends Component>>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	
	
	@Test
	public void testGetAllResourceCategories() throws Exception {
		ElementBusinessLogic testSubject;
		Either<List<CategoryDefinition>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetAllServiceCategories() throws Exception {
		ElementBusinessLogic testSubject;
		Either<List<CategoryDefinition>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testCreateCategory() throws Exception {
		ElementBusinessLogic testSubject;
		CategoryDefinition category = null;
		String componentTypeParamName = "";
		String userId = "";
		Either<CategoryDefinition, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		category = null;
	}

	
	@Test
	public void testCreateSubCategory() throws Exception {
		ElementBusinessLogic testSubject;
		SubCategoryDefinition subCategory = null;
		String componentTypeParamName = "";
		String parentCategoryId = "";
		String userId = "";
		Either<SubCategoryDefinition, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		subCategory = null;
	}

	
	@Test
	public void testCreateGrouping() throws Exception {
		ElementBusinessLogic testSubject;
		GroupingDefinition grouping = null;
		String componentTypeParamName = "";
		String grandParentCategoryId = "";
		String parentSubCategoryId = "";
		String userId = "";
		Either<GroupingDefinition, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		grouping = null;
	}

	
	@Test
	public void testGetAllCategories() throws Exception {
		ElementBusinessLogic testSubject;
		String componentType = "";
		String userId = "";
		Either<List<CategoryDefinition>, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		userId = null;

		// test 2
		testSubject = createTestSubject();
		userId = "";
	}

	
	@Test
	public void testGetAllCategories_1() throws Exception {
		ElementBusinessLogic testSubject;
		String userId = "";
		Either<UiCategories, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteCategory() throws Exception {
		ElementBusinessLogic testSubject;
		String categoryId = "";
		String componentTypeParamName = "";
		String userId = "";
		Either<CategoryDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteSubCategory() throws Exception {
		ElementBusinessLogic testSubject;
		String grandParentCategoryId = "";
		String parentSubCategoryId = "";
		String componentTypeParamName = "";
		String userId = "";
		Either<SubCategoryDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteGrouping() throws Exception {
		ElementBusinessLogic testSubject;
		String grandParentCategoryId = "";
		String parentSubCategoryId = "";
		String groupingId = "";
		String componentTypeParamName = "";
		String userId = "";
		Either<GroupingDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	

	
	@Test
	public void testGetAllPropertyScopes() throws Exception {
		ElementBusinessLogic testSubject;
		String userId = "";
		Either<List<PropertyScope>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetAllArtifactTypes() throws Exception {
		ElementBusinessLogic testSubject;
		String userId = "";
		Either<List<ArtifactType>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetAllDeploymentArtifactTypes() throws Exception {
		ElementBusinessLogic testSubject;
		Either<Map<String, Object>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetDefaultHeatTimeout() throws Exception {
		ElementBusinessLogic testSubject;
		Either<Integer, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetCatalogComponents() throws Exception {
		ElementBusinessLogic testSubject;
		String userId = "";
		List<OriginTypeEnum> excludeTypes = null;
		Either<Map<String, List<? extends Component>>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetFilteredCatalogComponents() throws Exception {
		ElementBusinessLogic testSubject;
		String assetType = "";
		Map<FilterKeyEnum, String> filters = null;
		String query = "";
		Either<List<? extends Component>, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		query = null;

		// test 2
		testSubject = createTestSubject();
		query = "";

		// test 3
		testSubject = createTestSubject();
		filters = null;
	}

	
	
	
	@Test
	public void testGetCatalogComponentsByUuidAndAssetType() throws Exception {
		ElementBusinessLogic testSubject;
		String assetType = "";
		String uuid = "";
		Either<List<? extends Component>, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		assetType = null;

		// test 2
		testSubject = createTestSubject();
		assetType = "";

		// test 3
		testSubject = createTestSubject();
		assetType = null;

		// test 4
		testSubject = createTestSubject();
		assetType = "";
	}

	
	@Test
	public void testGetAllComponentTypesParamNames() throws Exception {
		ElementBusinessLogic testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllComponentTypesParamNames();
	}

	
	@Test
	public void testGetAllSupportedRoles() throws Exception {
		ElementBusinessLogic testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAllSupportedRoles();
	}

	
	@Test
	public void testGetResourceTypesMap() throws Exception {
		ElementBusinessLogic testSubject;
		Either<Map<String, String>, ActionStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	
	

	
	@Test
	public void testGetFilteredResouces() throws Exception {
		ElementBusinessLogic testSubject;
		Map<FilterKeyEnum, String> filters = null;
		boolean inTransaction = false;
		Either<List<Component>, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
	}

	

}