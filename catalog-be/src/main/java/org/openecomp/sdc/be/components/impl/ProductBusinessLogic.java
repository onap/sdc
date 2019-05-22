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

import fj.data.Either;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.CategoryTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.ICacheMangerOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component("productBusinessLogic")
public class ProductBusinessLogic extends ComponentBusinessLogic {

    private static final String PRODUCT_FULL_NAME = "full";
    private static final String PRODUCT_ABBREVIATED_NAME = "abbreviated";
    private static final Logger log = Logger.getLogger(ProductBusinessLogic.class);
    private static final String INITIAL_VERSION = "0.1";
    private static final String CREATE_PRODUCT = "Create Product";
    private static List<Role> creationRoles;
    private static List<Role> updateRoles;
    private static List<Role> contactsRoles;

    @Autowired
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;

    @Autowired
    private ICacheMangerOperation cacheManagerOperation;

    public ProductBusinessLogic() {
        creationRoles = new ArrayList<>();
        updateRoles = new ArrayList<>();
        contactsRoles = new ArrayList<>();

        // only PM is allowed to create/update products
        creationRoles.add(Role.PRODUCT_MANAGER);
        updateRoles.add(Role.PRODUCT_MANAGER);
        // Only PM is allowed to be product contacts
        contactsRoles.add(Role.PRODUCT_MANAGER);
    }


    public Either<Product, ResponseFormat> createProduct(Product product, User user) {
        AuditingActionEnum actionEnum = AuditingActionEnum.CREATE_RESOURCE;
        ComponentTypeEnum typeEnum = ComponentTypeEnum.PRODUCT;

        // validate user - should be first to get the maximum auditing info in
        // case of subsequent failures
        log.debug("get user from DB");
        user = validateUser(user, CREATE_PRODUCT, product, actionEnum, false);
        // validate user role
        validateUserRole(user, product, creationRoles, actionEnum, null);

        if (product == null) {
            log.debug("Invalid product json. Check product servlet log for createProduct entry params");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, typeEnum);
            return Either.right(responseFormat);
        }

        // warn about non-updatable fields
        checkUnupdatableProductFields(product);

        Either<Product, ResponseFormat> validateProductResponse = validateProductBeforeCreate(product, user, actionEnum);
        if (validateProductResponse.isRight()) {
            return Either.right(validateProductResponse.right().value());
        }

        log.debug("send product {} to dao for create", product.getComponentMetadataDefinition().getMetadataDataDefinition().getName());

        Either<Boolean, ResponseFormat> lockResult = lockComponentByName(product.getSystemName(), product, CREATE_PRODUCT);
        if (lockResult.isRight()) {
            ResponseFormat responseFormat = lockResult.right().value();
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(responseFormat);
        }

        log.debug("Product name locked is {}, status = {}", product.getSystemName(), lockResult);

        try {
            Either<Product, StorageOperationStatus> createProductEither = toscaOperationFacade.createToscaComponent(product);

            if (createProductEither.isRight()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(createProductEither.right().value()), product, typeEnum);
                componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, typeEnum);
                return Either.right(responseFormat);
            }

            log.debug("Product created successfully");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, typeEnum);

            Product createdProduct = createProductEither.left().value();

            return Either.left(createdProduct);

        } finally {
            graphLockOperation.unlockComponentByName(product.getSystemName(), product.getUniqueId(), NodeTypeEnum.Product);
        }

    }

    private void checkUnupdatableProductFields(Product product) {
        checkComponentFieldsForOverrideAttempt(product);
        if (product.getNormalizedName() != null) {
            log.info("NormalizedName cannot be defined by user. This field will be overridden by the application");
        }
    }

    private Either<Product, ResponseFormat> validateProductBeforeCreate(Product product, User user, AuditingActionEnum actionEnum) {

        Either<Boolean, ResponseFormat> validateProductFields = validateProductFieldsBeforeCreate(user, product, actionEnum);
        if (validateProductFields.isRight()) {
            return Either.right(validateProductFields.right().value());
        }

        if (product.getIsActive() == null) {
            log.debug("no isActive value was provided, setting to default: false");
            product.setIsActive(false);
        }

        product.setCreatorUserId(user.getUserId());

        // enrich object
        log.debug("enrich product with version and state");
        product.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        product.setVersion(INITIAL_VERSION);

        // Generate invariant UUID - must be here and not in operation since it
        // should stay constant during clone
        String invariantUUID = UniqueIdBuilder.buildInvariantUUID();
        product.setInvariantUUID(invariantUUID);

        return Either.left(product);
    }

    private Either<Boolean, ResponseFormat> validateProductFieldsBeforeCreate(User user, Product product, AuditingActionEnum actionEnum) {

        // To be removed in 1607
        // See below
        String oldName = product.getName();

        Either<Boolean, ResponseFormat> componentNameValidation = validateProductNameAndCleanup(user, product, actionEnum);
        if (componentNameValidation.isRight()) {
            return componentNameValidation;
        }

        Either<Boolean, ResponseFormat> componentNameUniquenessValidation = validateComponentNameUnique(user, product, actionEnum);
        if (componentNameUniquenessValidation.isRight()) {
            return componentNameUniquenessValidation;
        }

        // To be removed in 1607 and replaced with generic
        // validateTagsListAndRemoveDuplicates()
        // See comments on the validateTagsListAndRemoveDuplicates(user,
        // product, oldName, actionEnum) function
        Either<Boolean, ResponseFormat> tagsValidation = validateTagsListAndRemoveDuplicates(user, product, oldName, actionEnum);
        if (tagsValidation.isRight()) {
            return tagsValidation;
        }

        validateIcon(user, product, actionEnum);

        Either<Boolean, ResponseFormat> projectCodeValidation = validateProjectCode(user, product, actionEnum);
        if (projectCodeValidation.isRight()) {
            return projectCodeValidation;
        }
        Either<Boolean, ResponseFormat> categoryValidation = validateGrouping(user, product, actionEnum);
        if (categoryValidation.isRight()) {
            return categoryValidation;
        }

        Either<Boolean, ResponseFormat> contactsListValidation = validateAndUpdateProductContactsList(user, product, actionEnum);
        if (contactsListValidation.isRight()) {
            return contactsListValidation;
        }

        Either<Boolean, ResponseFormat> productFullNameValidation = validateProductFullNameAndCleanup(user, product, actionEnum);
        if (productFullNameValidation.isRight()) {
            return productFullNameValidation;
        }

        validateDescriptionAndCleanup(user, product, actionEnum);

        return Either.left(true);
    }

    public Either<Map<String, Boolean>, ResponseFormat> validateProductNameExists(String productName, String userId) {

        validateUserExists(userId, "validate Product Name Exists", false);
        Either<Boolean, StorageOperationStatus> dataModelResponse = toscaOperationFacade.validateComponentNameUniqueness(productName, null, ComponentTypeEnum.PRODUCT);
        // DE242223
        titanDao.commit();

        if (dataModelResponse.isLeft()) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("isValid", dataModelResponse.left().value());
            log.debug("validation was successfully performed.");
            return Either.left(result);
        }

        ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(dataModelResponse.right().value()));

        return Either.right(responseFormat);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateProductContactsList(User user, Product product, AuditingActionEnum actionEnum) {
        List<String> contacts = product.getContacts();
        if (!ValidationUtils.validateListNotEmpty(contacts)) {
            log.debug("Contacts list cannot be empty for product {}", product.getName());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.EMPTY_PRODUCT_CONTACTS_LIST);
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(responseFormat);
        }

        boolean isProductCreatorInContacts = false;
        String modifierUserId = user.getUserId();
        for (String contact : contacts) {
            if (contact.equals(modifierUserId)) {
                log.trace("modifier userId found in product contacts");
                isProductCreatorInContacts = true;
                // No need to validate for this userId - it's modifier's
                continue;
            }
            if (!ValidationUtils.validateContactId(contact)) {
                log.debug("Product contacts has invalid userId {} for product {}", contact, product.getName());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INVALID_CONTACT, ComponentTypeEnum.PRODUCT.getValue());
                componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
                return Either.right(responseFormat);
            }

            User contactUser;
            try{
                contactUser = validateUserExists(contact, CREATE_PRODUCT, false);
                validateUserRole(contactUser, contactsRoles);
            } catch(ByActionStatusComponentException e){
                log.debug("Cannot set contact with userId {} as product contact, error: {}", contact, e.getActionStatus());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_PRODUCT_CONTACT, contact);
                componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
                throw new ByActionStatusComponentException(e.getActionStatus(), e.getParams());
            }
        }

        if (!isProductCreatorInContacts) {
            log.debug("modifier userId {} not found in product contacts - adding it", modifierUserId);
            contacts.add(modifierUserId);
        }

        // passed - setting all contacts userIds to lowercase
        List<String> tempContacts = contacts.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        ValidationUtils.removeDuplicateFromList(tempContacts);
        product.setContacts(tempContacts);

        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateGrouping(User user, Product product, AuditingActionEnum actionEnum) {
        List<CategoryDefinition> categories = product.getCategories();
        if (categories == null || categories.isEmpty()) {
            log.debug("Grouping list is empty for product: {}", product.getName());
            return Either.left(true);
        }
        Map<String, Map<String, Set<String>>> nonDuplicatedCategories = new HashMap<>();
        // remove duplicated entries
        for (CategoryDefinition cat : categories) {
            String catName = cat.getName();
            if (!ValidationUtils.validateStringNotEmpty(catName)) {
                // error missing cat name
                log.debug("Missing category name for product: {}", product.getName());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.PRODUCT.getValue());
                componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
                return Either.right(responseFormat);
            }
            Map<String, Set<String>> catEntry = nonDuplicatedCategories.get(catName);
            if (catEntry == null) {
                catEntry = new HashMap<>();
                nonDuplicatedCategories.put(catName, catEntry);
            }
            List<SubCategoryDefinition> subcategories = cat.getSubcategories();
            if (subcategories == null || subcategories.isEmpty()) {
                // error missing subcat for cat
                log.debug("Missing sub-categories for category {} in product {}", catName, product.getName());
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_SUBCATEGORY);
                componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
                return Either.right(responseFormat);
            }
            for (SubCategoryDefinition subcat : subcategories) {
                String subCatName = subcat.getName();
                if (!ValidationUtils.validateStringNotEmpty(subCatName)) {
                    // error missing sub cat name for cat
                    log.debug("Missing or empty sub-category for category {} in product {}", catName, product.getName());
                    ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_SUBCATEGORY);
                    componentsUtils.auditComponentAdmin(responseFormat, user, product,  actionEnum, ComponentTypeEnum.PRODUCT);
                    return Either.right(responseFormat);
                }
                Set<String> subcatEntry = catEntry.get(subCatName);
                if (subcatEntry == null) {
                    subcatEntry = new HashSet<>();
                    catEntry.put(subCatName, subcatEntry);
                }
                List<GroupingDefinition> groupings = subcat.getGroupings();
                for (GroupingDefinition group : groupings) {
                    String groupName = group.getName();
                    if (!ValidationUtils.validateStringNotEmpty(groupName)) {
                        // error missing grouping for sub cat name and cat
                        log.debug("Missing or empty groupng name for sub-category: {} for categor: {} in product: {}", subCatName, catName, product.getName());
                        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_MISSING_SUBCATEGORY);
                        componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
                        return Either.right(responseFormat);
                    }
                    if (!subcatEntry.contains(groupName)) {
                        subcatEntry.add(groupName);
                    } else {
                        log.debug("Grouping: {}, already exist for category: {} and subcategory: {}", groupName, catName, subCatName);
                    }
                }
            }
        } // for end of checking duplicated
            // validate existence
        Either<List<CategoryDefinition>, ActionStatus> allProductCategories = elementDao.getAllProductCategories();

        if (allProductCategories.isRight()) {
            log.debug("No product categories {}", allProductCategories.right().value());
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(allProductCategories.right().value());
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(responseFormat);
        }
        boolean catExist;
        // convert non-duplicated to data modeling format and update in the
        // input object
        List<CategoryDefinition> newCatList = new ArrayList<>();

        // over all categories from request
        for (Map.Entry<String, Map<String, Set<String>>> entry : nonDuplicatedCategories.entrySet()) {
            catExist = false;
            CategoryDefinition categoryDefinition = null;
            // over all categories from Titan
            List<CategoryDefinition> categoriesList = allProductCategories.left().value();
            if (categoriesList != null) {
                for (CategoryDefinition catInDb : categoriesList) {
                    if (entry.getKey().equals(catInDb.getName())) {
                        catExist = true;
                        boolean subcatExist;
                        // copy data
                        categoryDefinition = new CategoryDefinition(catInDb);
                        SubCategoryDefinition subCategory = null;

                        Map<String, Set<String>> subcats = entry.getValue();
                        for (Map.Entry<String, Set<String>> subcat : subcats.entrySet()) {
                            subcatExist = false;
                            List<SubCategoryDefinition> subcategoriesList = catInDb.getSubcategories();
                            if (subcategoriesList != null) {
                                for (SubCategoryDefinition subcatInDb : subcategoriesList) {
                                    if (subcatInDb.getName().equals(subcat.getKey())) {
                                        // copy data
                                        subCategory = new SubCategoryDefinition(subcatInDb);
                                        subcatExist = true;
                                        Set<String> grouping = subcat.getValue();
                                        boolean groupExist;
                                        GroupingDefinition groupingDefinition = null;
                                        for (String group : grouping) {
                                            groupExist = false;
                                            List<GroupingDefinition> groupings = subcatInDb.getGroupings();
                                            if (groupings != null) {
                                                for (GroupingDefinition groupInDb : groupings) {
                                                    if (groupInDb.getName().equals(group)) {
                                                        groupExist = true;
                                                        groupingDefinition = new GroupingDefinition(groupInDb);
                                                    }
                                                }
                                            }
                                            if (!groupExist) {
                                                // error grouping isn't defined
                                                // in Titan
                                                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_GROUP_ASSOCIATION, CategoryTypeEnum.GROUPING.getValue(), group);
                                                componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
                                                return Either.right(responseFormat);
                                            }
                                            subCategory.addGrouping(groupingDefinition);
                                        }
                                    }
                                }
                            }
                            if (!subcatExist) {
                                // error sub category isn't defined in Titan
                                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_GROUP_ASSOCIATION, CategoryTypeEnum.SUBCATEGORY.getValue(), subcat.getKey());
                                componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
                                return Either.right(responseFormat);
                            }
                            categoryDefinition.addSubCategory(subCategory);
                        }
                    }
                }
            }
            if (!catExist) {
                // error category isn't defined in Titan
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_GROUP_ASSOCIATION, CategoryTypeEnum.CATEGORY.getValue(), entry.getKey());
                componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
                return Either.right(responseFormat);
            }
            newCatList.add(categoryDefinition);
        }
        product.setCategories(newCatList);
        return Either.left(true);
    }

    public Either<Product, ResponseFormat> getProduct(String productId, User user) {
        String ecompErrorContext = "Get product";
        validateUserNotEmpty(user, ecompErrorContext);
        validateUserExists(user, ecompErrorContext, false);

        Either<Product, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(productId);

        if (storageStatus.isRight()) {
            log.debug("failed to get resource by id {}", productId);
            if (storageStatus.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                // TODO check error
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.PRODUCT_NOT_FOUND, ComponentTypeEnum.PRODUCT.getValue()));
            } else {
                return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus.right().value()), ""));
            }
        }
        return Either.left(storageStatus.left().value());
    }

    public Either<Product, ResponseFormat> deleteProduct(String productId, User user) {
        String ecompErrorContext = "Delete product";
        validateUserNotEmpty(user, ecompErrorContext);
        validateUserExists(user, ecompErrorContext, false);

        Either<Product, StorageOperationStatus> storageStatus = toscaOperationFacade.deleteToscaComponent(productId);

        if (storageStatus.isRight()) {
            log.debug("failed to delete resource by id {}", productId);
            return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(storageStatus.right().value()), ""));
        }
        return Either.left(storageStatus.left().value());
    }

    private Either<Boolean, ResponseFormat> validateProductFullNameAndCleanup(User user, Product product, AuditingActionEnum actionEnum) {
        String fullName = product.getFullName();
        if (!ValidationUtils.validateStringNotEmpty(fullName)) {
            ResponseFormat errorResponse = componentsUtils.getResponseFormat(ActionStatus.MISSING_ONE_OF_COMPONENT_NAMES, ComponentTypeEnum.PRODUCT.getValue(), PRODUCT_FULL_NAME);
            componentsUtils.auditComponentAdmin(errorResponse, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(errorResponse);
        }

        fullName = ValidationUtils.removeNoneUtf8Chars(fullName);
        fullName = ValidationUtils.removeHtmlTags(fullName);
        fullName = ValidationUtils.normaliseWhitespace(fullName);
        fullName = ValidationUtils.stripOctets(fullName);

        if (!ValidationUtils.validateProductFullNameLength(fullName)) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, ComponentTypeEnum.PRODUCT.getValue(), PRODUCT_FULL_NAME);
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(responseFormat);
        }

        if (!ValidationUtils.validateIsEnglish(fullName)) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, ComponentTypeEnum.PRODUCT.getValue(), PRODUCT_FULL_NAME);
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(responseFormat);
        }

        product.setFullName(fullName);
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateProductNameAndCleanup(User user, Product product, AuditingActionEnum actionEnum) {
        String name = product.getName();
        if (!ValidationUtils.validateStringNotEmpty(name)) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_ONE_OF_COMPONENT_NAMES, ComponentTypeEnum.PRODUCT.getValue(), PRODUCT_ABBREVIATED_NAME);
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(responseFormat);
        }

        // Product name is required to have same validation and normalization as
        // category
        if (!ValidationUtils.validateCategoryDisplayNameFormat(name)) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, ComponentTypeEnum.PRODUCT.getValue(), PRODUCT_ABBREVIATED_NAME);
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(responseFormat);
        }

        String normalizedName4Display = ValidationUtils.normalizeCategoryName4Display(name);

        if (!ValidationUtils.validateCategoryDisplayNameLength(normalizedName4Display)) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, ComponentTypeEnum.PRODUCT.getValue(), PRODUCT_ABBREVIATED_NAME);
            componentsUtils.auditComponentAdmin(responseFormat, user, product, actionEnum, ComponentTypeEnum.PRODUCT);
            return Either.right(responseFormat);
        }

        product.setName(normalizedName4Display);
        String normalizedName4Uniqueness = ValidationUtils.normaliseComponentName(normalizedName4Display);
        product.setNormalizedName(normalizedName4Uniqueness);

        return Either.left(true);
    }

    // This is a workaround for a current tag--->product name behaviour, which
    // will be changed in 1607.
    // It was agreed with Ella on 23/2/16 that the tag validation of product
    // will be made against the old product name (before normalization),
    // and in 1607 US will be defined where UI will no longer pass tag of
    // component name, and BE will add it by itself after all needed
    // normalizations.
    private Either<Boolean, ResponseFormat> validateTagsListAndRemoveDuplicates(User user, Product product, String oldProductName, AuditingActionEnum actionEnum) {
        List<String> tagsList = product.getTags();
        validateComponentTags(tagsList, oldProductName, ComponentTypeEnum.PRODUCT, user, product, actionEnum);
        ValidationUtils.removeDuplicateFromList(tagsList);
        return Either.left(true);
    }

    @Override
    public void setDeploymentArtifactsPlaceHolder(org.openecomp.sdc.be.model.Component component, User user) {

    }

    public Either<Product, ResponseFormat> updateProductMetadata(String productId, Product updatedProduct, User user) {
        ComponentTypeEnum typeEnum = ComponentTypeEnum.PRODUCT;
        user = validateUser(user, "Update Product", updatedProduct, null, false);
        // validate user role
        validateUserRole(user, updatedProduct, updateRoles, null, null);
        if (updatedProduct == null) {
            log.debug("Invalid product json. Check product servlet log for updateProduct entry params");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            return Either.right(responseFormat);
        }

        Either<Product, StorageOperationStatus> storageStatus = toscaOperationFacade.getToscaElement(productId);
        if (storageStatus.isRight()) {
            if (storageStatus.right().value().equals(StorageOperationStatus.NOT_FOUND)) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.PRODUCT_NOT_FOUND, ComponentTypeEnum.PRODUCT.name().toLowerCase()));
            }
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), typeEnum), ""));
        }

        Product currentProduct = storageStatus.left().value();

        if (!ComponentValidationUtils.canWorkOnComponent(productId, toscaOperationFacade, user.getUserId())) {
            log.info("Restricted operation for user: {}, on product: {}" , user.getUserId(), currentProduct.getCreatorUserId());
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
        }

        Either<Product, ResponseFormat> validationRsponse = validateAndUpdateProductMetadata(user, currentProduct, updatedProduct);
        if (validationRsponse.isRight()) {
            log.info("product update metadata: validations field.");
            return validationRsponse;
        }

        Product productToUpdate = validationRsponse.left().value();
        // lock resource
        Either<Boolean, ResponseFormat> lockResult = lockComponent(currentProduct.getUniqueId(), currentProduct, "Update Product Metadata");
        if (lockResult.isRight()) {
            return Either.right(lockResult.right().value());
        }
        try {
            Either<Product, StorageOperationStatus> updateResponse = toscaOperationFacade.updateToscaElement(productToUpdate);
            if (updateResponse.isRight()) {
                toscaOperationFacade.rollback();
                log.debug("failed to update product {}", productToUpdate.getUniqueId());
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            toscaOperationFacade.commit();
            return Either.left(updateResponse.left().value());
        } finally {
            graphLockOperation.unlockComponent(productId, NodeTypeEnum.Product);
        }
    }

    private Either<Product, ResponseFormat> validateAndUpdateProductMetadata(User user, Product currentProduct, Product updatedProduct) {

        boolean hasBeenCertified = ValidationUtils.hasBeenCertified(currentProduct.getVersion());
        Either<Boolean, ResponseFormat> response = validateAndUpdateProductName(user, currentProduct, updatedProduct);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        response = validateAndUpdateFullName(user, currentProduct, updatedProduct);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        response = validateAndUpdateDescription(user, currentProduct, updatedProduct, null);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        response = validateAndUpdateCategory(user, currentProduct, updatedProduct);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        response = validateAndUpdateContactList(user, currentProduct, updatedProduct);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        response = validateAndUpdateTags(user, currentProduct, updatedProduct);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        response = validateAndUpdateProjectCode(user, currentProduct, updatedProduct);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        if (updatedProduct.getIsActive() != null) {
            currentProduct.setIsActive(updatedProduct.getIsActive());
        }

        response = validateAndUpdateIcon(user, currentProduct, updatedProduct, hasBeenCertified);
        if (response.isRight()) {
            ResponseFormat errorResponse = response.right().value();
            return Either.right(errorResponse);
        }

        String currentInvariantUuid = currentProduct.getInvariantUUID();
        String updatedInvariantUuid = updatedProduct.getInvariantUUID();

        if ((updatedInvariantUuid != null) && (!updatedInvariantUuid.equals(currentInvariantUuid))) {
            log.warn("Product invariant UUID is automatically set and cannot be updated");
            updatedProduct.setInvariantUUID(currentInvariantUuid);
        }
        return Either.left(currentProduct);

    }

    private Either<Boolean, ResponseFormat> validateAndUpdateProductName(User user, Product currentProduct, Product updatedProduct) {
        String updatedProductName = updatedProduct.getName();
        String tags;
        String currentProductName = currentProduct.getName();
        if (updatedProductName != null) {
            Either<Boolean, ResponseFormat> validatProductNameResponse = validateProductNameAndCleanup(user, updatedProduct, null);
            if (validatProductNameResponse.isRight()) {
                ResponseFormat errorRespons = validatProductNameResponse.right().value();
                return Either.right(errorRespons);
            }
            updatedProductName = updatedProduct.getName();
            if (!currentProductName.equals(updatedProductName)) {
                Either<Boolean, ResponseFormat> productNameUniquenessValidation = validateComponentNameUnique(user, updatedProduct, null);
                if (productNameUniquenessValidation.isRight()) {
                    return productNameUniquenessValidation;
                }
                currentProduct.setName(updatedProductName);
                tags = updatedProductName;
                updatedProductName = ValidationUtils.normalizeCategoryName4Display(updatedProductName);
                currentProduct.getComponentMetadataDefinition().getMetadataDataDefinition().setNormalizedName(ValidationUtils.normaliseComponentName(updatedProductName));
                List<String> updatedTags = updatedProduct.getTags();
                // As discussed with Ella currently (1604) we are not removing
                // the old name from tags.
                if (updatedTags == null) {
                    updatedTags = currentProduct.getTags();
                }
                updatedTags.add(tags);
            }
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateFullName(User user, Product currentProduct, Product updatedProduct) {
        String updatedProductName = updatedProduct.getFullName();
        String currentProductName = currentProduct.getFullName();
        if (updatedProductName != null && !currentProductName.equals(updatedProductName)) {
            Either<Boolean, ResponseFormat> validatProductNameResponse = validateProductFullNameAndCleanup(user, updatedProduct, null);
            if (validatProductNameResponse.isRight()) {
                ResponseFormat errorRespons = validatProductNameResponse.right().value();
                return Either.right(errorRespons);
            }
            currentProduct.setFullName(updatedProduct.getFullName());
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateCategory(User user, Product currentProduct, Product updatedProduct) {

        Either<Boolean, ResponseFormat> validateCategoryResponse = validateGrouping(user, updatedProduct, null);
        if (validateCategoryResponse.isRight()) {
            ResponseFormat errorResponse = validateCategoryResponse.right().value();
            return Either.right(errorResponse);
        }

        List<CategoryDefinition> categoryUpdated = updatedProduct.getCategories();
        if (categoryUpdated != null) {
            currentProduct.setCategories(categoryUpdated);
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateContactList(User user, Product currentProduct, Product updatedProduct) {
        List<String> updatedContacts = updatedProduct.getContacts();
        List<String> currentContacts = currentProduct.getContacts();
        if (updatedContacts != null) {
            if (!(currentContacts.containsAll(updatedContacts) && updatedContacts.containsAll(currentContacts))) {
                Either<Boolean, ResponseFormat> validatResponse = validateAndUpdateProductContactsList(user, updatedProduct, null);
                if (validatResponse.isRight()) {
                    ResponseFormat errorRespons = validatResponse.right().value();
                    return Either.right(errorRespons);
                }
                currentProduct.setContacts(updatedProduct.getContacts());
            }
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateAndUpdateTags(User user, Product currentProduct, Product updatedProduct) {
        List<String> tagsUpdated = updatedProduct.getTags();
        List<String> tagsCurrent = currentProduct.getTags();
        if (tagsUpdated != null) {
            if (!(tagsCurrent.containsAll(tagsUpdated) && tagsUpdated.containsAll(tagsCurrent))) {
                Either<Boolean, ResponseFormat> validatResponse = validateTagsListAndRemoveDuplicates(user, updatedProduct, currentProduct.getName(), null);
                if (validatResponse.isRight()) {
                    ResponseFormat errorRespons = validatResponse.right().value();
                    return Either.right(errorRespons);
                }
                currentProduct.setTags(updatedProduct.getTags());
            }
        }
        return Either.left(true);
    }

    @Override
    public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
        // markAsDeleted isnt implemented yet
        return Either.left(new ArrayList<>());
    }

    @Override
    protected boolean validateTagPattern(String tag) {
        return ValidationUtils.validateCategoryDisplayNameFormat(tag);
    }

    public Either<Product, ResponseFormat> getProductByNameAndVersion(String productName, String productVersion, String userId) {
        validateUserExists(userId, "get Service By Name And Version", false);
        Either<Product, StorageOperationStatus> storageStatus = toscaOperationFacade.getComponentByNameAndVersion(ComponentTypeEnum.PRODUCT, productName, productVersion);
        if (storageStatus.isRight()) {
            log.debug("failed to get service by name {} and version {}", productName, productVersion);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(storageStatus.right().value(), ComponentTypeEnum.PRODUCT), productName));
        }
        Product product = storageStatus.left().value();
        return Either.left(product);
    }

    @Override
    public ComponentInstanceBusinessLogic getComponentInstanceBL() {
        return componentInstanceBusinessLogic;
    }

    @Override
    public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, String userId) {
        return null;
    }

    public ICacheMangerOperation getCacheManagerOperation() {
        return cacheManagerOperation;
    }

    public void setCacheManagerOperation(ICacheMangerOperation cacheManagerOperation) {
        this.cacheManagerOperation = cacheManagerOperation;
    }

    @Override
    public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String componentId,
            List<String> dataParamsToReturn) {
        // TODO Auto-generated method stub
        return null;
    }

}
