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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.ComponentBusinessLogicMock;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.components.validation.ValidationUtils;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

public class ProductBusinessLogicTest extends ComponentBusinessLogicMock {

    @Mock
    ComponentNameValidator componentNameValidator;
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
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Before
    public void setUp() {
        productBusinessLogic = new ProductBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
            groupTypeOperation, groupBusinessLogic, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactsBusinessLogic, componentInstanceBusinessLogic, artifactToscaOperation, componentContactIdValidator,
            componentNameValidator, componentTagsValidator, componentValidator,
            componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator);
        MockitoAnnotations.openMocks(this);
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


    @Test(expected = ComponentException.class)
    public void testCreateProduct_givenEmptyUserId_thenReturnsException() {
        when(userValidations.validateUserNotEmpty(Mockito.any(User.class), Mockito.anyString()))
            .thenThrow(new ByResponseFormatComponentException(new ResponseFormat()));
        productBusinessLogic.createProduct(product, user);
    }


    @Test(expected = ComponentException.class)
    public void testCreateProduct_givenInvalidUserRole_thenReturnsException() {
        user.setRole("CREATOR");
        doThrow(new ByResponseFormatComponentException(new ResponseFormat())).when(userValidations).validateUserRole(any(), anyList());
        assertTrue(productBusinessLogic.createProduct(product, user).isRight());
    }

    @Test
    public void testCreateProduct_givenProductIsNull_thenReturnsError() {
        product = null;
        assertTrue(productBusinessLogic.createProduct(product, user).isRight());
    }

    @Test
    public void testValidateProductName_givenValidName_thenReturnsSuccessful() {
        when(userValidations.validateUserExists(anyString()))
            .thenReturn(user);
        when(toscaOperationFacade.validateComponentNameUniqueness(eq(pName), any(), any(ComponentTypeEnum.class)))
            .thenReturn(Either.left(Boolean.TRUE));

        Map result = productBusinessLogic.validateProductNameExists(pName, uId).left().value();
        assertEquals(Boolean.TRUE, result.get("isValid"));
    }

    @Test
    public void testValidateProductName_givenInvalidName_thenReturnsError() {
        String invalidProductName = "~~";
        when(userValidations.validateUserExists(anyString()))
            .thenReturn(user);
        when(toscaOperationFacade.validateComponentNameUniqueness(eq(invalidProductName), any(), any(ComponentTypeEnum.class)))
            .thenReturn(Either.left(Boolean.FALSE));
        Map result = productBusinessLogic.validateProductNameExists(invalidProductName, uId).left().value();
        assertEquals(Boolean.FALSE, result.get("isValid"));
    }

    @Test
    public void testValidateProductName_givenNameUniquenessCheckFails_thenReturnsError() {
        when(userValidations.validateUserExists(anyString()))
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
