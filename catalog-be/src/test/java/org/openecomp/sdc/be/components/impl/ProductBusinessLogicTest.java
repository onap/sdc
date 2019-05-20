package org.openecomp.sdc.be.components.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.components.validation.ValidationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.ProductMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentMetadataDefinition;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import fj.data.Either;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class ProductBusinessLogicTest {

	private Product product;
	private User user;
	private List<String> contacts;
	private List<String> tags;

	private String pId;
	private String pName;
	private String uId;
	private String pCode;
	private String pIcon;
	private String desc;
	private String role;

	@InjectMocks
	private ProductBusinessLogic productBusinessLogic;

	@Mock
	private ToscaOperationFacade toscaOperationFacade;

	@Mock
	private JanusGraphDao janusGraphDao;

	@Mock
	private ValidationUtils validationUtils;

	@Mock
	private ComponentsUtils componentsUtils;

	@Mock
	private IGraphLockOperation iGraphLockOperation;

	@Mock
	private UserValidations userValidations;

	@Mock
	private IElementOperation elementOperation;

	@Before
	public void setUp() {
		productBusinessLogic = new ProductBusinessLogic();
		MockitoAnnotations.initMocks(this);
		product = new Product();
		user = new User();
		contacts = new ArrayList<>();
		tags = new ArrayList<>();

		pName = "product1";
		pId = "productId";
		uId = "userId";
		pCode = "productCode";
		pIcon = "projectIcon";
		desc = "Testing Product Business Logic";
		role = "PROJECT_MANAGER";

		user.setUserId(uId);
		user.setRole(role);
	}

	@Test
	public void testCreateProduct_givenValidProductAndUser_thenReturnsProduct() {
		product.setName(pName);
		product.setFullName("avengers");
		product.setInvariantUUID("ABCD1234");
		product.setContacts(getContacts());
		product.setTags(getTags());
		product.setIcon(pIcon);
		product.setProjectCode(pCode);
		product.setDescription(desc);

		when(userValidations.validateUserNotEmpty(Mockito.any(User.class), Mockito.anyString()))
				.thenReturn(user);
		when(userValidations.validateUserExists(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean()))
				.thenReturn(user);
		when(toscaOperationFacade.validateComponentNameExists(Mockito.anyString(), Mockito.any(), Mockito.any(ComponentTypeEnum.class)))
				.thenReturn(Either.left(Boolean.FALSE));
		when(iGraphLockOperation.lockComponentByName(Mockito.any(), Mockito.any(NodeTypeEnum.class)))
				.thenReturn(StorageOperationStatus.OK);
		when(toscaOperationFacade.createToscaComponent(any(org.openecomp.sdc.be.model.Product.class)))
				.thenReturn(Either.left(product));
		Either result = productBusinessLogic.createProduct(product, user);
		assertTrue(result.isLeft());
		Product returnedProduct = (Product) result.left().value();

		assertEquals(product.getFullName(), returnedProduct.getFullName());

	}

	@Test(expected = ComponentException.class)
	public void testCreateProduct_givenEmptyUserId_thenReturnsException() {
		when(userValidations.validateUserNotEmpty(Mockito.any(User.class), Mockito.anyString()))
				.thenThrow(new ComponentException(new ResponseFormat()));
		productBusinessLogic.createProduct(product, user);
	}

	@Test(expected = ComponentException.class)
	public void testCreateProduct_givenUnknownUser_thenReturnsException() {
		ComponentException componentException = new ComponentException(ActionStatus.USER_NOT_FOUND);
		when(userValidations.validateUserNotEmpty(any(User.class), anyString()))
				.thenReturn(user);
		when(userValidations.validateUserExists(anyString(), anyString(), anyBoolean()))
				.thenThrow(componentException);
		productBusinessLogic.createProduct(product, user);
	}

	@Test(expected = ComponentException.class)
	public void testCreateProduct_givenInvalidUserRole_thenReturnsException() {
		user.setRole("CREATOR");
		doThrow(new ComponentException(new ResponseFormat())).when(userValidations).validateUserRole(any(), anyList());
		assertTrue(productBusinessLogic.createProduct(product, user).isRight());
	}

	@Test
	public void testCreateProduct_givenProductIsNull_thenReturnsError() {
		product = null;
		assertTrue(productBusinessLogic.createProduct(product, user).isRight());
	}

	@Test
	public void testCreateProduct_givenInvalidProductFullNames_thenReturnsErrors() {
		List<String> invalidProductNames = new ArrayList<>();
		invalidProductNames.add(null);
		invalidProductNames.add("~~");
		invalidProductNames.add("yo");
		invalidProductNames.add("infinity");
		when(toscaOperationFacade.validateComponentNameExists(anyString(), any(), any(ComponentTypeEnum.class)))
				.thenReturn(Either.left(Boolean.TRUE));
		for (String s : invalidProductNames) {
			product.setName(s);
			assertTrue(productBusinessLogic.createProduct(product, user).isRight());
		}
	}

	@Test
	public void testValidateProductName_givenValidName_thenReturnsSuccessful() {
		when(userValidations.validateUserExists(anyString(), anyString(), anyBoolean()))
				.thenReturn(user);
		when(toscaOperationFacade.validateComponentNameUniqueness(eq(pName), any(), any(ComponentTypeEnum.class)))
				.thenReturn(Either.left(Boolean.TRUE));

		Map result = productBusinessLogic.validateProductNameExists(pName, uId).left().value();
		assertEquals(Boolean.TRUE, result.get("isValid"));
	}

	@Test
	public void testValidateProductName_givenInvalidName_thenReturnsError() {
		String invalidProductName = "~~";
		when(userValidations.validateUserExists(anyString(), anyString(), anyBoolean()))
				.thenReturn(user);
		when(toscaOperationFacade.validateComponentNameUniqueness(eq(invalidProductName), any(), any(ComponentTypeEnum.class)))
				.thenReturn(Either.left(Boolean.FALSE));
		Map result = productBusinessLogic.validateProductNameExists(invalidProductName, uId).left().value();
		assertEquals(Boolean.FALSE, result.get("isValid"));
	}

	@Test
	public void testValidateProductName_givenNameUniquenessCheckFails_thenReturnsError() {
		when(userValidations.validateUserExists(anyString(), anyString(), anyBoolean()))
				.thenReturn(user);
		when(toscaOperationFacade.validateComponentNameUniqueness(eq(pName), any(), any(ComponentTypeEnum.class)))
				.thenReturn(Either.right(StorageOperationStatus.ENTITY_ALREADY_EXISTS));
		assertTrue(productBusinessLogic.validateProductNameExists(pName, uId).isRight());
	}

	@Test
	public void testGetProduct_givenValidProductIdAndUser_thenReturnsSuccessful() {
		when(toscaOperationFacade.getToscaElement(eq(pName)))
				.thenReturn(Either.left(product));
		assertTrue(productBusinessLogic.getProduct(pName, user).isLeft());
	}

	@Test
	public void testGetProduct_givenInvalidProductId_thenReturnsError() {
		when(toscaOperationFacade.getToscaElement(eq(pName)))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		assertTrue(productBusinessLogic.getProduct(pName, user).isRight());
	}

	@Test
	public void testDeleteProduct_givenValidProductIdAndUser_thenReturnsSuccessful() {
		when(toscaOperationFacade.deleteToscaComponent(pId))
				.thenReturn(Either.left(product));
		assertTrue(productBusinessLogic.deleteProduct(pId, user).isLeft());
	}

	@Test
	public void testDeleteProduct_givenInvalidProductId_thenReturnsError() {
		when(toscaOperationFacade.deleteToscaComponent(pId))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		assertTrue(productBusinessLogic.deleteProduct(pId, user).isRight());
	}

	@Test
	public void testUpdateProductMetadata_givenValidProductAndUser_thenReturnsSuccessful() {
		String componentId = "component1";
		String projectName = "Product1";
		String version = "2.0";
		String lifecycleState = "NOT_CERTIFIED_CHECKOUT";
		String uniqueId = "pUniqueId";

		Product product = new Product();
		ProductMetadataDataDefinition productMetadataDataDefinition = new ProductMetadataDataDefinition();
		ComponentMetadataDefinition componentMetadataDefinition = new ComponentMetadataDefinition(productMetadataDataDefinition);
		CategoryDefinition categoryDefinition = new CategoryDefinition();
		SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition();
		GroupingDefinition groupingDefinition = new GroupingDefinition();

		List<CategoryDefinition> categoryDefinitionList = new ArrayList<>();
		List<SubCategoryDefinition> subCategoryDefinitionList = new ArrayList<>();
		List<GroupingDefinition> groupingDefinitionsList = new ArrayList<>();

		categoryDefinition.setName("cat1");
		subCategoryDefinition.setName("subCat1");
		groupingDefinition.setName("subCatGroup1");

		groupingDefinitionsList.add(groupingDefinition);
		subCategoryDefinition.setGroupings(groupingDefinitionsList);
		subCategoryDefinitionList.add(subCategoryDefinition);
		categoryDefinition.setSubcategories(subCategoryDefinitionList);
		categoryDefinitionList.add(categoryDefinition);

		productMetadataDataDefinition.setFullName(projectName);
		productMetadataDataDefinition.setName(projectName);
		productMetadataDataDefinition.setState(lifecycleState);
		productMetadataDataDefinition.setUniqueId(uniqueId);
		productMetadataDataDefinition.setComponentType(ComponentTypeEnum.PRODUCT);

		product.setMetadataDefinition(componentMetadataDefinition);
		product.setLastUpdaterUserId(uId);
		product.setDescription(desc);
		product.setVersion(version);
		product.setProjectCode(pCode);
		product.setIcon(pIcon);
		product.setCategories(categoryDefinitionList);
		product.setContacts(contacts);
		product.setTags(tags);

		when(userValidations.validateUserExists(eq(uId), anyString(), anyBoolean()))
				.thenReturn(user);
		when(toscaOperationFacade.getToscaElement(eq(componentId)))
				.thenReturn(Either.left(product));
		when(toscaOperationFacade.getToscaElement(eq(componentId), any(JsonParseFlagEnum.class)))
				.thenReturn(Either.left(product));
		when(elementOperation.getAllProductCategories())
				.thenReturn(Either.left(categoryDefinitionList));
		when(iGraphLockOperation.lockComponent(anyString(), any(NodeTypeEnum.class)))
				.thenReturn(StorageOperationStatus.OK);
		when(toscaOperationFacade.updateToscaElement(any(Product.class)))
				.thenReturn(Either.left(product));

		assertTrue(productBusinessLogic.updateProductMetadata(componentId, product, user).isLeft());
	}

	@Test
	public void testUpdateProductMetadata_givenUpdateProductNull_thenReturnsError() {
		Product updateProduct = null;
		String productId = null;
		assertTrue(productBusinessLogic.updateProductMetadata(productId, updateProduct, user).isRight());
	}

	@Test
	public void testUpdateProductMetadata_givenProductDoesNotExist_thenReturnsError() {
		String productId = "product1";
		when(toscaOperationFacade.getToscaElement(eq(productId)))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		assertTrue(productBusinessLogic.updateProductMetadata(productId, product, user).isRight());
	}

	@Test
	public void testUpdateProductMetada_givenUserRestricted_thenReturnsError() {

		ProductMetadataDataDefinition productMetadataDataDefinition = new ProductMetadataDataDefinition();
		productMetadataDataDefinition.setLifecycleState("CERTIFIED");
		ComponentMetadataDefinition componentMetadataDefinition = new ComponentMetadataDefinition(productMetadataDataDefinition);
		product.setMetadataDefinition(componentMetadataDefinition);


		when(userValidations.validateUserExists(eq(uId), anyString(), anyBoolean()))
				.thenReturn(user);
		when(toscaOperationFacade.getToscaElement(eq(pId)))
				.thenReturn(Either.left(product));
		when(toscaOperationFacade.getToscaElement(eq(pId), eq(JsonParseFlagEnum.ParseMetadata)))
				.thenReturn(Either.left(product));
		assertTrue(productBusinessLogic.updateProductMetadata(pId, product, user).isRight());
	}


	@Test
	public void testGetProductByNameAndVersion_givenValidNameAndVersion_thenReturnsSuccessful() {
		String productVersion = "2.0";

		when(toscaOperationFacade.getComponentByNameAndVersion(eq(ComponentTypeEnum.PRODUCT), eq(pName), eq(productVersion)))
				.thenReturn(Either.left(product));
		assertTrue(productBusinessLogic.getProductByNameAndVersion(pName, productVersion, uId).isLeft());
	}

	@Test
	public void testGetProductByNameAndVersion_givenInvalidDetails_thenReturnsError() {
		String productVersion = "2.0";
		when(toscaOperationFacade.getComponentByNameAndVersion(eq(ComponentTypeEnum.PRODUCT), eq(pName), eq(productVersion)))
				.thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
		assertTrue(productBusinessLogic.getProductByNameAndVersion(pName, productVersion, uId).isRight());
	}

	private List<String> getContacts() {
		contacts.add("user1");
		return contacts;
	}

	private List<String> getTags() {
		tags.add("product1");
		return tags;
	}
}