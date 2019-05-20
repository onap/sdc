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
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
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
public class ElementBusinessLogicTest {

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

    @Mock
	private IElementOperation elementOperation;

    @InjectMocks
    ElementBusinessLogic elementBusinessLogic;

    @Before
	public void setUp() {

    	elementBusinessLogic = new ElementBusinessLogic();
    	MockitoAnnotations.initMocks(this);
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
	public void testGetFollowed_givenUserWithTesterRoleErrorOccursGettingService_thenReturnsError () {
		user.setUserId("tester1");
		user.setRole(Role.TESTER.name());

		Set<Component> resources = new HashSet<>();

		Resource resource = new Resource();
		resources.add(resource);

		Mockito.when(toscaOperationFacade.getFollowed(any(), Mockito.anySet(), any(), Mockito.eq(ComponentTypeEnum.RESOURCE)))
				.thenReturn(Either.left(resources));
		Mockito.when(toscaOperationFacade.getFollowed(any(), anySet(), any(), eq(ComponentTypeEnum.SERVICE)))
				.thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
		Assert.assertTrue(elementBusinessLogic.getFollowed(user).isRight());
	}

	@Test
	public void testGetFollowed_givenUserWithGovernorRole_thenReturnsSuccessful(){
		user.setUserId("governor1");
    	user.setRole(Role.GOVERNOR.name());

    	List<Service> services = new ArrayList<>();
    	services.add(new Service());

    	when(toscaOperationFacade.getCertifiedServicesWithDistStatus(any()))
				.thenReturn(Either.left(services));
		Assert.assertTrue(elementBusinessLogic.getFollowed(user).isLeft());
	}

	@Test
	public void testGetFollowed_givenUserWithOPSRoleErrorOccursGettingServices_thenReturnsError(){
		user.setUserId("ops1");
    	user.setRole(Role.OPS.name());

		when(toscaOperationFacade.getCertifiedServicesWithDistStatus(any()))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));

		Assert.assertTrue(elementBusinessLogic.getFollowed(user).isRight());

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
    	doThrow(new ComponentException(new ResponseFormat())).when(userValidations).validateUserExists(eq(user.getUserId()),
				anyString(), anyBoolean());
  		elementBusinessLogic.getAllCategories(null, user.getUserId());
	}

	@Test
	public void testGetAllCategories_givenInvalidComponentType_thenReturnsError() {
    	when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), anyBoolean())).thenReturn(user);

    	Assert.assertTrue(elementBusinessLogic.getAllCategories("NONE", user.getUserId()).isRight());

	}

	@Test
	public void testGetAllCategories_givenValidUserAndComponentType_thenReturnsSuccessful() {

    	List<CategoryDefinition> categoryDefinitionList = new ArrayList<>();
    	categoryDefinitionList.add(new CategoryDefinition());

		when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), anyBoolean())).thenReturn(user);
		when(elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false))
				.thenReturn(Either.left(categoryDefinitionList));
		Assert.assertTrue(elementBusinessLogic.getAllCategories(ComponentTypeEnum.RESOURCE_PARAM_NAME, user.getUserId())
		.isLeft());
	}

	@Test
	public void testGetAllCategories_givenValidUserId_thenReturnsSuccessful() {

    	List<CategoryDefinition> dummyCategoryDefinitionList = new ArrayList<>();
    	dummyCategoryDefinitionList.add(new CategoryDefinition());

    	when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), anyBoolean()))
				.thenReturn(user);
    	when(elementOperation.getAllCategories(any(NodeTypeEnum.class), anyBoolean()))
				.thenReturn(Either.left(dummyCategoryDefinitionList));

    	Assert.assertTrue(elementBusinessLogic.getAllCategories(user.getUserId()).isLeft());
	}

	@Test
	public void testDeleteCategory_givenValidComponentTypeAndCategoryId_thenReturnsSuccessful() {

    	when(elementOperation.deleteCategory(any(NodeTypeEnum.class), anyString()))
				.thenReturn(Either.left(new CategoryDefinition()));

    	Assert.assertTrue(elementBusinessLogic.deleteCategory("cat1", "resources", user.getUserId()).isLeft());
	}

	@Test
	public void testCreateSubCategory_givenValidSubCategory_thenReturnsSuccessful() {
    	user.setRole(Role.ADMIN.name());
		SubCategoryDefinition subCatDef = new SubCategoryDefinition();
		subCatDef.setName("subCat1");

		when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), anyBoolean()))
				.thenReturn(user);
		when(elementOperation.getCategory(any(NodeTypeEnum.class), anyString()))
				.thenReturn(Either.left(new CategoryDefinition()));
		when(elementOperation.isSubCategoryUniqueForCategory(any(NodeTypeEnum.class), anyString(), anyString()))
				.thenReturn(Either.left(Boolean.TRUE));
		when(elementOperation.getSubCategoryUniqueForType(any(NodeTypeEnum.class), anyString()))
				.thenReturn(Either.left(subCatDef));
		when(elementOperation.createSubCategory(anyString(), any(SubCategoryDefinition.class), any(NodeTypeEnum.class)))
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
    	doThrow(new ComponentException(new ResponseFormat())).when(userValidations).validateUserExists(eq(user.getUserId()),
				anyString(), anyBoolean());
    	elementBusinessLogic.createSubCategory(subCategoryDefinition, "resources", "cat1", user.getUserId());
	}

	@Test
    public void testcreateCategory_VALIDATION_OF_USER_FAILED() throws Exception {


        CategoryDefinition catdefinition = new CategoryDefinition();
        String userid=null;
        ResponseFormat responseFormat = new ResponseFormat(7);
        when(componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION)).thenReturn(responseFormat);
        Either<CategoryDefinition, ResponseFormat> response = elementBusinessLogic.createCategory(catdefinition,"Service", userid);
        Assert.assertEquals(true,response.isRight());
        Assert.assertEquals((Integer) 7, response.right().value().getStatus());
    }

    @Test
    public void testcreateCategory_MISSING_INFORMATION() throws Exception {

        CategoryDefinition catdefinition = new CategoryDefinition();
        ResponseFormat responseFormat = new ResponseFormat(9);
        User user = new User();
        when(userAdminManager.getUser("USR", false)).thenReturn(Either.left(user));
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);
        Either<CategoryDefinition, ResponseFormat> response = elementBusinessLogic.createCategory(catdefinition,"Service", "USR");
        Assert.assertEquals(true,response.isRight());
        Assert.assertEquals((Integer) 9, response.right().value().getStatus());
    }

    @Test
    public void testcreateCategory_RESTRICTED_OPERATION() throws Exception {

        CategoryDefinition catdefinition = new CategoryDefinition();
        ResponseFormat responseFormat = new ResponseFormat(9);
        User user = new User();
        when(userAdminManager.getUser("USR", false)).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
        when(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION)).thenReturn(responseFormat);
        Either<CategoryDefinition, ResponseFormat> response = elementBusinessLogic.createCategory(catdefinition,"Service", "USR");
        Assert.assertEquals(true,response.isRight());
        Assert.assertEquals((Integer) 9, response.right().value().getStatus());
    }

    @Test
    public void testcreateCategory_Invalid_componentType() throws Exception {

        CategoryDefinition catdefinition = new CategoryDefinition();
        catdefinition.setName("CAT01");
        ResponseFormat responseFormat = new ResponseFormat(9);
        User user = new User();

        when(userAdminManager.getUser("USR", false)).thenReturn(Either.left(user));
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);
        Either<CategoryDefinition, ResponseFormat> response = elementBusinessLogic.createCategory(catdefinition,"Service", "USR");
        Assert.assertEquals(true,response.isRight());
        Assert.assertEquals((Integer) 9, response.right().value().getStatus());
    }

    @Test
    public void testcreateCategory_Invalid() throws Exception {

        CategoryDefinition catdefinition = new CategoryDefinition();
        catdefinition.setName("CAT01");
        ResponseFormat responseFormat = new ResponseFormat(9);
        User user = new User();

        when(userAdminManager.getUser("USR", false)).thenReturn(Either.left(user));
        when(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(responseFormat);
        Either<CategoryDefinition, ResponseFormat> response = elementBusinessLogic.createCategory(catdefinition,"SERVICE_PARAM_NAME", "USR");
        Assert.assertEquals(true,response.isRight());
        Assert.assertEquals((Integer) 9, response.right().value().getStatus());
    }
}