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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.components.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;
import fj.data.Either;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElementBusinessLogicTest extends BaseBusinessLogicMock {

	private User user;

	@Mock
	private ComponentsUtils componentsUtils;

	@Mock
	private UserBusinessLogic userAdminManager;

	@Mock
	private JanusGraphDao janusGraphDao;

	@Mock
	private UserValidations userValidations;

	@Mock
	private ToscaOperationFacade toscaOperationFacade;

  @InjectMocks
	ElementBusinessLogic elementBusinessLogic;

    @Before
	public void setUp() {
    	MockitoAnnotations.initMocks(this);
    	elementBusinessLogic = new ElementBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
				groupBusinessLogic, interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation, elementDao, userAdminManager);
    	elementBusinessLogic.setComponentsUtils(componentsUtils);
    	elementBusinessLogic.setJanusGraphDao(janusGraphDao);
    	elementBusinessLogic.setToscaOperationFacade(toscaOperationFacade);
    	elementBusinessLogic.setUserValidations(userValidations);
			user = new User();
			user.setUserId("admin");
	}

    @Test
	public void testGetFollowed_givenUserWithDesignerRole_thenReturnsSuccessful() {
    	user.setUserId("designer1");
    	user.setRole(Role.DESIGNER.name());

    	Set<Component> resources = new HashSet<>();
    	Set<Component> services = new HashSet<>();

    	Resource resource = new Resource();
    	Service service = new Service();

    	resources.add(resource);
    	services.add(service);

    	Mockito.when(toscaOperationFacade.getFollowed(eq(user.getUserId()), Mockito.anySet(), Mockito.anySet(), Mockito.eq(ComponentTypeEnum.RESOURCE)))
				.thenReturn(Either.left(resources));
    	Mockito.when(toscaOperationFacade.getFollowed(eq(user.getUserId()), anySet(), anySet(), eq(ComponentTypeEnum.SERVICE)))
				.thenReturn(Either.left(services));

    	Map<String, List<? extends Component>> result = elementBusinessLogic.getFollowed(user).left().value();
    	Assert.assertTrue(result.get("services").size() == 1);
    	Assert.assertTrue(result.get("resources").size() == 1);
	}


	@Test
	public void testGetFollowed_givenUserWithProductStrategistRole_thenReturnsEmptyList(){
		user.setUserId("pstra1");
    	user.setRole(Role.PRODUCT_STRATEGIST.name());

    	Map<String, List<? extends Component>> result = elementBusinessLogic.getFollowed(user).left().value();
    	Assert.assertEquals("products list should be empty", 0, result.get("products").size());

	}

	@Test
	public void testGetFollowed_givenUserWithProductManagerRole_thenReturnsProducts(){
    	user.setUserId("pmanager1");
    	user.setRole(Role.PRODUCT_MANAGER.name());

    	Set<Component> products = new HashSet<>();
    	products.add(new Product());

    	when(toscaOperationFacade.getFollowed(any(), anySet(), any(), eq(ComponentTypeEnum.PRODUCT)))
				.thenReturn(Either.left(products));

    	Map<String, List<? extends Component>> result = elementBusinessLogic.getFollowed(user).left().value();
    	Assert.assertEquals("1 product should exist", 1, result.get("products").size());

	}

	@Test
	public void testGetFollowed_givenUserWithRoleAdminErrorOccursGettingResources_thenReturnsError() {
    	user.setUserId("admin1");
    	user.setRole(Role.ADMIN.name());

    	when(toscaOperationFacade.getFollowed(any(), anySet(), any(), eq(ComponentTypeEnum.RESOURCE)))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

    	Assert.assertTrue(elementBusinessLogic.getFollowed(user).isRight());
	}

	@Test
	public void testGetAllCategories_givenUserIsNull_thenReturnsError() {
    	Assert.assertTrue(elementBusinessLogic.getAllCategories(null, null).isRight());
	}

	@Test(expected = ComponentException.class)
	public void testGetAllCategories_givenValidationOfUserFails_thenReturnsError() {
    	doThrow(new ByResponseFormatComponentException(new ResponseFormat())).when(userValidations).validateUserExists(eq(user.getUserId()));
  		elementBusinessLogic.getAllCategories(null, user.getUserId());
	}

	@Test
	public void testGetAllCategories_givenInvalidComponentType_thenReturnsError() {
    	when(userValidations.validateUserExists(eq(user.getUserId()))).thenReturn(user);

    	Assert.assertTrue(elementBusinessLogic.getAllCategories("NONE", user.getUserId()).isRight());

	}

	@Test
	public void testGetAllCategories_givenValidUserAndComponentType_thenReturnsSuccessful() {

    	List<CategoryDefinition> categoryDefinitionList = new ArrayList<>();
    	categoryDefinitionList.add(new CategoryDefinition());

		when(userValidations.validateUserExists(eq(user.getUserId()))).thenReturn(user);
		when(elementDao.getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
				.thenReturn(Either.left(categoryDefinitionList));
		Assert.assertTrue(elementBusinessLogic.getAllCategories(ComponentTypeEnum.RESOURCE_PARAM_NAME, user.getUserId())
		.isLeft());
	}

	@Test
	public void testGetAllCategories_givenValidUserId_thenReturnsSuccessful() {

    	List<CategoryDefinition> dummyCategoryDefinitionList = new ArrayList<>();
    	dummyCategoryDefinitionList.add(new CategoryDefinition());

    	when(userValidations.validateUserExists(eq(user.getUserId())))
				.thenReturn(user);
    	when(elementDao.getAllCategories(any(NodeTypeEnum.class), anyBoolean()))
				.thenReturn(Either.left(dummyCategoryDefinitionList));

    	Assert.assertTrue(elementBusinessLogic.getAllCategories(user.getUserId()).isLeft());
	}

	@Test
	public void testDeleteCategory_givenValidComponentTypeAndCategoryId_thenReturnsSuccessful() {

    	when(elementDao.deleteCategory(any(NodeTypeEnum.class), anyString()))
				.thenReturn(Either.left(new CategoryDefinition()));

    	Assert.assertTrue(elementBusinessLogic.deleteCategory("cat1", "resources", user.getUserId()).isLeft());
	}

	@Test
	public void testCreateSubCategory_givenValidSubCategory_thenReturnsSuccessful() {
    	user.setRole(Role.ADMIN.name());
		SubCategoryDefinition subCatDef = new SubCategoryDefinition();
		subCatDef.setName("subCat1");

		when(userValidations.validateUserExists(eq(user.getUserId())))
				.thenReturn(user);
		when(elementDao.getCategory(any(NodeTypeEnum.class), anyString()))
				.thenReturn(Either.left(new CategoryDefinition()));
		when(elementDao.isSubCategoryUniqueForCategory(any(NodeTypeEnum.class), anyString(), anyString()))
				.thenReturn(Either.left(Boolean.TRUE));
		when(elementDao.getSubCategoryUniqueForType(any(NodeTypeEnum.class), anyString()))
				.thenReturn(Either.left(subCatDef));
		when(elementDao.createSubCategory(anyString(), any(SubCategoryDefinition.class), any(NodeTypeEnum.class)))
				.thenReturn(Either.left(subCatDef));

		Assert.assertTrue(elementBusinessLogic.createSubCategory(subCatDef, "resources",
				"cat1", user.getUserId()).isLeft());
	}

	@Test
	public void testCreateSubCategory_givenNullSubCategory_thenReturnsError() {
    	Assert.assertTrue(elementBusinessLogic.createSubCategory(null, "resources",
				"cat1", user.getUserId()).isRight());
	}

	@Test(expected = ComponentException.class)
	public void testCreateSubCategory_givenUserValidationFails_thenReturnsException() {
    	SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition();
    	doThrow(new ByResponseFormatComponentException(new ResponseFormat())).when(userValidations).validateUserExists(eq(user.getUserId()));
    	elementBusinessLogic.createSubCategory(subCategoryDefinition, "resources", "cat1", user.getUserId());
	}

	@Test(expected=ComponentException.class)
    public void testcreateCategory_VALIDATION_OF_USER_FAILED() {
        CategoryDefinition catdefinition = new CategoryDefinition();
        String userid = "";
        ResponseFormat responseFormat = new ResponseFormat(7);
        when(userValidations.validateUserExists("")).thenThrow(new ByResponseFormatComponentException(responseFormat));
        elementBusinessLogic.createCategory(catdefinition,"Service", userid);
    }

    @Test
    public void testcreateCategory_MISSING_INFORMATION() throws Exception {
        CategoryDefinition catdefinition = new CategoryDefinition();
        ResponseFormat responseFormat = new ResponseFormat(9);
        User user = new User();
        when(userValidations.validateUserExists("USR")).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);
        Either<CategoryDefinition, ResponseFormat> response = elementBusinessLogic.createCategory(catdefinition,"Service", "USR");
        Assert.assertTrue(response.isRight());
        Assert.assertEquals((Integer) 9, response.right().value().getStatus());
    }


    @Test
    public void testcreateCategory_Invalid_componentType() throws Exception {

        CategoryDefinition catdefinition = new CategoryDefinition();
        catdefinition.setName("CAT01");
        ResponseFormat responseFormat = new ResponseFormat(9);
        User user = new User();

        when(userValidations.validateUserExists("USR")).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);
        Either<CategoryDefinition, ResponseFormat> response = elementBusinessLogic.createCategory(catdefinition,"Service", "USR");
        Assert.assertTrue(response.isRight());
        Assert.assertEquals((Integer) 9, response.right().value().getStatus());
    }

    @Test
    public void testcreateCategory_Invalid() throws Exception {

        CategoryDefinition catdefinition = new CategoryDefinition();
        catdefinition.setName("CAT01");
        ResponseFormat responseFormat = new ResponseFormat(9);
        User user = new User();

        when(userValidations.validateUserExists("USR")).thenReturn(user);
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);
        Either<CategoryDefinition, ResponseFormat> response = elementBusinessLogic.createCategory(catdefinition,"SERVICE_PARAM_NAME", "USR");
        Assert.assertTrue(response.isRight());
        Assert.assertEquals((Integer) 9, response.right().value().getStatus());
    }
}