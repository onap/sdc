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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datamodel.api.CategoryTypeEnum;
import org.openecomp.sdc.be.datamodel.utils.NodeTypeConvertUtils;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.CatalogUpdateTimestamp;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;
import org.openecomp.sdc.be.ui.model.UiCategories;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang.BooleanUtils.isTrue;
import static org.openecomp.sdc.be.components.impl.ImportUtils.Constants.DEFAULT_ICON;

@org.springframework.stereotype.Component("elementsBusinessLogic")
public class ElementBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(ElementBusinessLogic.class);
    private static final String SERVICES = "services";
    private static final String RESOURCES = "resources";
    private static final String VALIDATION_OF_USER_FAILED_USER_ID = "Validation of user failed, userId {}";
    private static final String COMPONENT_TYPE_IS_INVALID = "Component type {} is invalid";
    private static final String VALIDATION_OF_USER_ROLE_FAILED_USER_ID = "Validation of user role failed, userId {}";

    private final IElementOperation elementOperation;
    private final UserBusinessLogic userAdminManager;

    @Autowired
    public ElementBusinessLogic(IElementOperation elementDao,
        IGroupOperation groupOperation,
        IGroupInstanceOperation groupInstanceOperation,
        IGroupTypeOperation groupTypeOperation,
        GroupBusinessLogic groupBusinessLogic,
        InterfaceOperation interfaceOperation,
        InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
        ArtifactsOperations artifactToscaOperation, IElementOperation elementOperation,
        UserBusinessLogic userAdminManager) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
        this.elementOperation = elementOperation;
        this.userAdminManager = userAdminManager;
    }
    /**
     *
     * @param user
     * @return
     */
    public Either<Map<String, List<? extends Component>>, ResponseFormat> getFollowed(User user) {
        // Used for not getting duplicated followed. Cheaper than checking ArrayList.contains
        Either<Map<String, Set<? extends Component>>, ResponseFormat> response = null;
        // Getting the role
        String role = user.getRole();
        String userId = user.getUserId();
        Role currentRole = Role.valueOf(role);

        switch (currentRole) {
        case DESIGNER:
            response = handleDesigner(userId);
            break;

        case PRODUCT_STRATEGIST:
            response = handleProductStrategist();
            break;

        case PRODUCT_MANAGER:
            response = handleProductManager(userId);
            break;

        case ADMIN:
            response = handleAdmin();
            break;

        default:
            response = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            break;
        }
        // converting the Set to List so the rest of the code will handle it normally (Was changed because the same element with the same uuid was returned twice)
        return convertedToListResponse(response);

    }

    private Either<Map<String, List<? extends Component>>, ResponseFormat> convertedToListResponse(Either<Map<String, Set<? extends Component>>, ResponseFormat> setResponse) {

        Map<String, List<? extends Component>> arrayResponse = new HashMap<>();
        if (setResponse.isLeft()) {
            for (Map.Entry<String, Set<? extends Component>> entry : setResponse.left().value().entrySet()) {
                arrayResponse.put(entry.getKey(), new ArrayList(new HashSet(entry.getValue())));
            }
            return Either.left(arrayResponse);
        }
        return Either.right(setResponse.right().value());
    }

    private Either<Map<String, Set<? extends Component>>, ResponseFormat> handleAdmin() {
        Either<Map<String, Set<? extends Component>>, ResponseFormat> response;
        // userId should stay null
        Set<LifecycleStateEnum> lifecycleStates = new HashSet<>();
        lifecycleStates.add(LifecycleStateEnum.CERTIFIED);
        response = getFollowedResourcesAndServices(null, lifecycleStates, new HashSet<>());
        return response;
    }

    private Either<Map<String, Set<? extends Component>>, ResponseFormat> handleDesigner(String userId) {
        Set<LifecycleStateEnum> lifecycleStates = new HashSet<>();
        Set<LifecycleStateEnum> lastStateStates = new HashSet<>();
        Either<Map<String, Set<? extends Component>>, ResponseFormat> response;
        lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        lifecycleStates.add(LifecycleStateEnum.CERTIFIED);
        // more states
        lastStateStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        response = getFollowedResourcesAndServices(userId, lifecycleStates, lastStateStates);
        return response;
    }

    private Either<Map<String, Set<? extends Component>>, ResponseFormat> handleGovernor() {
        return handleFollowedCertifiedServices(null);
    }

    private Either<Map<String, Set<? extends Component>>, ResponseFormat> handleProductStrategist() {
        // Should be empty list according to Ella, 13/03/16
        Map<String, Set<? extends Component>> result = new HashMap<>();
        result.put("products", new HashSet<>());
        return Either.left(result);
    }

    private Either<Map<String, Set<? extends Component>>, ResponseFormat> handleProductManager(String userId) {
        Set<LifecycleStateEnum> lifecycleStates = new HashSet<>();
        Set<LifecycleStateEnum> lastStateStates = new HashSet<>();
        Either<Map<String, Set<? extends Component>>, ResponseFormat> response;
        lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        lifecycleStates.add(LifecycleStateEnum.CERTIFIED);
        // more states
        lastStateStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        response = getFollowedProducts(userId, lifecycleStates, lastStateStates);
        return response;
    }

    private Either<Map<String, Set<? extends Component>>, ResponseFormat> handleFollowedCertifiedServices(Set<DistributionStatusEnum> distStatus) {

        Either<List<Service>, StorageOperationStatus> services = toscaOperationFacade.getCertifiedServicesWithDistStatus(distStatus);
        if (services.isLeft()) {
            Map<String, Set<? extends Component>> result = new HashMap<>();
            Set<Service> set = new HashSet<>();
            set.addAll(services.left().value());
            result.put(SERVICES, set);
            return Either.left(result);
        } else {
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(services.right().value())));
        }
    }

    private Either<Map<String, Set<? extends Component>>, ResponseFormat> getFollowedResourcesAndServices(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates) {

        try {
            Either<Set<Resource>, StorageOperationStatus> resources = toscaOperationFacade.getFollowed(userId, lifecycleStates, lastStateStates, ComponentTypeEnum.RESOURCE);

            if (resources.isLeft()) {
                Either<Set<Service>, StorageOperationStatus> services = toscaOperationFacade.getFollowed(userId, lifecycleStates, lastStateStates, ComponentTypeEnum.SERVICE);
                if (services.isLeft()) {
                    Map<String, Set<? extends Component>> result = new HashMap<>();
                    result.put(SERVICES, services.left().value());
                    result.put(RESOURCES, resources.left().value());
                    return Either.left(result);
                } else {
                    return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(services.right().value())));
                }
            } else {
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resources.right().value())));
            }
        } finally {
            janusGraphDao.commit();
        }
    }

    private Either<Map<String, Set<? extends Component>>, ResponseFormat> getFollowedProducts(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates) {
        Either<Set<Product>, StorageOperationStatus> products = toscaOperationFacade.getFollowed(userId, lifecycleStates, lastStateStates, ComponentTypeEnum.PRODUCT);
        if (products.isLeft()) {
            Map<String, Set<? extends Component>> result = new HashMap<>();
            result.put("products", products.left().value());
            return Either.left(result);
        } else {
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(products.right().value())));
        }
    }

    /*
     * New categories flow - start
     */
    public Either<List<CategoryDefinition>, ActionStatus> getAllResourceCategories() {
        return elementOperation.getAllResourceCategories();
    }

    public Either<List<CategoryDefinition>, ActionStatus> getAllServiceCategories() {
        return elementOperation.getAllServiceCategories();
    }

    public Either<CategoryDefinition, ResponseFormat> createCategory(CategoryDefinition category, String componentTypeParamName, String userId) {

        AuditingActionEnum auditingAction = AuditingActionEnum.ADD_CATEGORY;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentTypeParamName);
        String componentType = componentTypeEnum == null ? componentTypeParamName : componentTypeEnum.getValue();
        CategoryTypeEnum categoryType = CategoryTypeEnum.CATEGORY;

        User user = validateUserExists(userId);
        if (category == null) {
            log.debug("Category json is invalid");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            handleCategoryAuditing(responseFormat, user, null, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        String categoryName = category.getName();
        // For auditing of failures we need the original non-normalized name
        String origCategoryName = categoryName;
        if (componentTypeEnum == null) {
            log.debug(COMPONENT_TYPE_IS_INVALID, componentTypeParamName);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            handleCategoryAuditing(responseFormat, user, origCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Either<Boolean, ResponseFormat> validateUserRole = validateUserRole(user, componentTypeEnum);
        if (validateUserRole.isRight()) {
            log.debug(VALIDATION_OF_USER_ROLE_FAILED_USER_ID, userId);
            ResponseFormat responseFormat = validateUserRole.right().value();
            handleCategoryAuditing(responseFormat, user, origCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        if (!ValidationUtils.validateCategoryDisplayNameFormat(categoryName)) {
            log.debug("Category display name format is invalid, name {}, componentType {}", categoryName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, componentType, categoryType.getValue());
            handleCategoryAuditing(responseFormat, user, origCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        categoryName = ValidationUtils.normalizeCategoryName4Display(categoryName);

        if (!ValidationUtils.validateCategoryDisplayNameLength(categoryName)) {
            log.debug("Category display name length is invalid, should be from 4 to 25 chars, name {}, componentType {}", categoryName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, componentType, categoryType.getValue());
            handleCategoryAuditing(responseFormat, user, origCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        category.setName(categoryName);

        String normalizedName = ValidationUtils.normalizeCategoryName4Uniqueness(categoryName);
        category.setNormalizedName(normalizedName);

        if (ValidationUtils.validateCategoryIconNotEmpty(category.getIcons())){
            log.debug("createCategory: setting category icon to default icon since service category was created without an icon ");
            category.setIcons(Arrays.asList(DEFAULT_ICON));
        }

        NodeTypeEnum nodeType = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, categoryType);

        Either<Boolean, ActionStatus> categoryUniqueEither = elementOperation.isCategoryUniqueForType(nodeType, normalizedName);
        if (categoryUniqueEither.isRight()) {
            log.debug("Failed to check category uniqueness, name {}, componentType {}", categoryName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(categoryUniqueEither.right().value());
            handleCategoryAuditing(responseFormat, user, origCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Boolean isCategoryUnique = categoryUniqueEither.left().value();
        if (!isCategoryUnique) {
            log.debug("Category is not unique, name {}, componentType {}", categoryName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_CATEGORY_ALREADY_EXISTS, componentType, categoryName);
            handleCategoryAuditing(responseFormat, user, origCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Either<CategoryDefinition, ActionStatus> createCategoryByType = elementOperation.createCategory(category, nodeType);
        if (createCategoryByType.isRight()) {
            log.debug("Failed to create category, name {}, componentType {}", categoryName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_CATEGORY_ALREADY_EXISTS, componentType, categoryName);
            handleCategoryAuditing(responseFormat, user, origCategoryName, auditingAction, componentType);
            return Either.right(componentsUtils.getResponseFormat(createCategoryByType.right().value()));
        }
        category = createCategoryByType.left().value();
        log.debug("Created category for component {}, name {}, uniqueId {}", componentType, categoryName, category.getUniqueId());
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
        handleCategoryAuditing(responseFormat, user, category.getName(), auditingAction, componentType);
        return Either.left(category);
    }

    public Either<SubCategoryDefinition, ResponseFormat> createSubCategory(SubCategoryDefinition subCategory, String componentTypeParamName, String parentCategoryId, String userId) {

        AuditingActionEnum auditingAction = AuditingActionEnum.ADD_SUB_CATEGORY;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentTypeParamName);
        String componentType = componentTypeEnum == null ? componentTypeParamName : componentTypeEnum.getValue();
        CategoryTypeEnum categoryType = CategoryTypeEnum.SUBCATEGORY;
        // For auditing
        String parentCategoryName = parentCategoryId;

        if (subCategory == null) {
            log.debug("Sub-category json is invalid");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            handleCategoryAuditing(responseFormat, null, parentCategoryName, null, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        String subCategoryName = subCategory.getName();
        // For auditing of failures we need the original non-normalized name
        String origSubCategoryName = subCategoryName;
        User user;
        try{
            user =  validateUserExists(userId);
        } catch(ByActionStatusComponentException e){
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            handleComponentException(userId, auditingAction, componentType, parentCategoryName, origSubCategoryName,
                responseFormat);
            throw e;
        } catch(ByResponseFormatComponentException e){
            ResponseFormat responseFormat = e.getResponseFormat();
            handleComponentException(userId, auditingAction, componentType, parentCategoryName, origSubCategoryName,
                responseFormat);
            throw e;
        }
        if (componentTypeEnum == null) {
            log.debug(COMPONENT_TYPE_IS_INVALID, componentTypeParamName);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Either<Boolean, ResponseFormat> validateComponentType = validateComponentTypeForCategory(componentTypeEnum, categoryType);
        if (validateComponentType.isRight()) {
            log.debug("Validation of component type for sub-category failed");
            ResponseFormat responseFormat = validateComponentType.right().value();
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Either<Boolean, ResponseFormat> validateUserRole = validateUserRole(user, componentTypeEnum);
        if (validateUserRole.isRight()) {
            log.debug(VALIDATION_OF_USER_ROLE_FAILED_USER_ID, userId);
            ResponseFormat responseFormat = validateUserRole.right().value();
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        NodeTypeEnum parentNodeType = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.CATEGORY);
        NodeTypeEnum childNodeType = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.SUBCATEGORY);

        CategoryDefinition categoryDefinition;
        Either<CategoryDefinition, ResponseFormat> validateCategoryExists = validateCategoryExists(parentNodeType, parentCategoryId, componentTypeEnum);
        if (validateCategoryExists.isRight()) {
            log.debug("Validation of parent category exists failed, parent categoryId {}", parentCategoryId);
            ResponseFormat responseFormat = validateCategoryExists.right().value();
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        categoryDefinition = validateCategoryExists.left().value();
        parentCategoryName = categoryDefinition.getName();

        if (!ValidationUtils.validateCategoryDisplayNameFormat(subCategoryName)) {
            log.debug("Sub-category display name format is invalid, name {}, componentType {}", subCategoryName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, componentType, categoryType.getValue());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        subCategoryName = ValidationUtils.normalizeCategoryName4Display(subCategoryName);

        if (!ValidationUtils.validateCategoryDisplayNameLength(subCategoryName)) {
            log.debug("Sub-category display name length is invalid, should be from 4 to 25 chars, name {}, componentType {}", subCategoryName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, componentType, categoryType.getValue());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        String normalizedName = ValidationUtils.normalizeCategoryName4Uniqueness(subCategoryName);
        subCategory.setNormalizedName(normalizedName);

        // Uniqueness under this category
        Either<Boolean, ActionStatus> subCategoryUniqueForCategory = elementOperation.isSubCategoryUniqueForCategory(childNodeType, normalizedName, parentCategoryId);
        if (subCategoryUniqueForCategory.isRight()) {
            log.debug("Failed to check sub-category uniqueness, parent name {}, subcategory norm name {}, componentType {}", parentCategoryName, normalizedName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(subCategoryUniqueForCategory.right().value());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Boolean isSubUnique = subCategoryUniqueForCategory.left().value();
        if (!isSubUnique) {
            log.debug("Sub-category is not unique for category, parent name {}, subcategory norm name {}, componentType {}", parentCategoryName, normalizedName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_SUB_CATEGORY_EXISTS_FOR_CATEGORY, componentType, subCategoryName, parentCategoryName);
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        // Setting name of subcategory to fit the similar subcategory name
        // ignoring cases.
        // For example if Network-->kUKU exists for service category Network,
        // and user is trying to create Router-->Kuku for service category
        // Router,
        // his subcategory name will be Router-->kUKU.
        Either<SubCategoryDefinition, ActionStatus> subCategoryUniqueForType = elementOperation.getSubCategoryUniqueForType(childNodeType, normalizedName);
        if (subCategoryUniqueForType.isRight()) {
            log.debug("Failed validation of whether similar sub-category exists, normalizedName {} componentType {}", normalizedName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(subCategoryUniqueForType.right().value());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }
        SubCategoryDefinition subCategoryDefinition = subCategoryUniqueForType.left().value();
        if (subCategoryDefinition != null) {
            subCategoryName = subCategoryDefinition.getName();
        }

        subCategory.setName(subCategoryName);
        ///////////////////////////////////////////// Validations end

        Either<SubCategoryDefinition, ActionStatus> createSubCategory = elementOperation.createSubCategory(parentCategoryId, subCategory, childNodeType);
        if (createSubCategory.isRight()) {
            log.debug("Failed to create sub-category, name {}, componentType {}", subCategoryName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(subCategoryUniqueForType.right().value());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        SubCategoryDefinition subCategoryCreated = createSubCategory.left().value();
        log.debug("Created sub-category for component {}, name {}, uniqueId {}", componentType, subCategoryName, subCategoryCreated.getUniqueId());
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
        handleCategoryAuditing(responseFormat, user, parentCategoryName, subCategoryCreated.getName(), auditingAction, componentType);
        return Either.left(subCategoryCreated);
    }

    private void handleComponentException(String userId, AuditingActionEnum auditingAction, String componentType,
        String parentCategoryName, String origSubCategoryName, ResponseFormat responseFormat) {
        User user;
        log.debug(VALIDATION_OF_USER_FAILED_USER_ID, userId);
        user = new User();
        user.setUserId(userId);
        handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction,
            componentType);
    }

    private void handleComponentException(GroupingDefinition grouping, String userId, AuditingActionEnum auditingAction,
        String componentType, String parentCategoryName, String parentSubCategoryName, ResponseFormat responseFormat) {
        User user;
        log.debug(VALIDATION_OF_USER_FAILED_USER_ID, userId);
        user = new User();
        user.setUserId(userId);
        String groupingNameForAudit = grouping == null ? null : grouping.getName();
        handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, groupingNameForAudit,
            auditingAction, componentType);
    }

    private void handleComponentException(String componentType, String userId, ResponseFormat responseFormat) {
        User user;
        user = new User();
        user.setUserId(userId);
        log.debug(VALIDATION_OF_USER_FAILED_USER_ID, userId);
        componentsUtils.auditGetCategoryHierarchy(user, componentType, responseFormat);
    }

    public Either<GroupingDefinition, ResponseFormat> createGrouping(GroupingDefinition grouping, String componentTypeParamName, String grandParentCategoryId, String parentSubCategoryId, String userId) {

        AuditingActionEnum auditingAction = AuditingActionEnum.ADD_GROUPING;
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentTypeParamName);
        String componentType = componentTypeEnum == null ? componentTypeParamName : componentTypeEnum.getValue();
        CategoryTypeEnum categoryType = CategoryTypeEnum.GROUPING;
        // For auditing
        String parentCategoryName = grandParentCategoryId;
        String parentSubCategoryName = parentSubCategoryId;

        User user;
        try{
            user = validateUserExists(userId);
        } catch(ByResponseFormatComponentException e){
            ResponseFormat responseFormat = e.getResponseFormat();
            handleComponentException(grouping, userId, auditingAction, componentType, parentCategoryName,
                parentSubCategoryName,
                responseFormat);
            throw e;
        } catch(ByActionStatusComponentException e){
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            handleComponentException(grouping, userId, auditingAction, componentType, parentCategoryName,
                parentSubCategoryName, responseFormat);
            throw e;
        }

        if (grouping == null) {
            log.debug("Grouping json is invalid");
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, null, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        String groupingName = grouping.getName();
        // For auditing of failures we need the original non-normalized name
        String origGroupingName = groupingName;

        if (componentTypeEnum == null) {
            log.debug(COMPONENT_TYPE_IS_INVALID, componentTypeParamName);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Either<Boolean, ResponseFormat> validateComponentType = validateComponentTypeForCategory(componentTypeEnum, categoryType);
        if (validateComponentType.isRight()) {
            log.debug("Validation of component type for grouping failed");
            ResponseFormat responseFormat = validateComponentType.right().value();
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Either<Boolean, ResponseFormat> validateUserRole = validateUserRole(user, componentTypeEnum);
        if (validateUserRole.isRight()) {
            log.debug(VALIDATION_OF_USER_ROLE_FAILED_USER_ID, userId);
            ResponseFormat responseFormat = validateUserRole.right().value();
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        NodeTypeEnum grandParentNodeType = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.CATEGORY);
        NodeTypeEnum parentNodeType = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.SUBCATEGORY);
        NodeTypeEnum childNodeType = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.GROUPING);

        // Validate category
        CategoryDefinition categoryDefinition;
        Either<CategoryDefinition, ResponseFormat> validateCategoryExists = validateCategoryExists(grandParentNodeType, grandParentCategoryId, componentTypeEnum);
        if (validateCategoryExists.isRight()) {
            log.debug("Validation of parent category exists failed, parent categoryId {}", grandParentCategoryId);
            ResponseFormat responseFormat = validateCategoryExists.right().value();
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        categoryDefinition = validateCategoryExists.left().value();
        parentCategoryName = categoryDefinition.getName();

        // Validate subcategory
        SubCategoryDefinition subCategoryDefinition;
        Either<SubCategoryDefinition, ResponseFormat> validateSubCategoryExists = validateSubCategoryExists(parentNodeType, parentSubCategoryId, componentTypeEnum);
        if (validateSubCategoryExists.isRight()) {
            log.debug("Validation of parent sub-category exists failed, parent sub-category id {}", parentSubCategoryId);
            ResponseFormat responseFormat = validateSubCategoryExists.right().value();
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        subCategoryDefinition = validateSubCategoryExists.left().value();
        parentSubCategoryName = subCategoryDefinition.getName();

        if (!ValidationUtils.validateCategoryDisplayNameFormat(groupingName)) {
            log.debug("Sub-category display name format is invalid, name {}, componentType {}", groupingName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_FORMAT, componentType, categoryType.getValue());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        groupingName = ValidationUtils.normalizeCategoryName4Display(groupingName);

        if (!ValidationUtils.validateCategoryDisplayNameLength(groupingName)) {
            log.debug("Grouping display name length is invalid, should be from 4 to 25 chars, name {}, componentType {}", groupingName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_ELEMENT_INVALID_NAME_LENGTH, componentType, categoryType.getValue());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        String normalizedName = ValidationUtils.normalizeCategoryName4Uniqueness(groupingName);
        grouping.setNormalizedName(normalizedName);

        // Uniqueness under this category
        Either<Boolean, ActionStatus> groupingUniqueForSubCategory = elementOperation.isGroupingUniqueForSubCategory(childNodeType, normalizedName, parentSubCategoryId);
        if (groupingUniqueForSubCategory.isRight()) {
            log.debug("Failed to check grouping uniqueness, parent name {}, grouping norm name {}, componentType {}", parentSubCategoryName, normalizedName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(groupingUniqueForSubCategory.right().value());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        Boolean isGroupingUnique = groupingUniqueForSubCategory.left().value();
        if (!isGroupingUnique) {
            log.debug("Grouping is not unique for sub-category, parent name {}, grouping norm name {}, componentType {}", parentSubCategoryName, normalizedName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.COMPONENT_GROUPING_EXISTS_FOR_SUB_CATEGORY, componentType, groupingName, parentSubCategoryName);
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        // Setting name of grouping to fit the similar grouping name ignoring
        // cases.
        // For example if Network-->kUKU exists for service sub-category
        // Network, and user is trying to create grouping Router-->Kuku for
        // service sub-category Router,
        // his grouping name will be Router-->kUKU.
        Either<GroupingDefinition, ActionStatus> groupingUniqueForType = elementOperation.getGroupingUniqueForType(childNodeType, normalizedName);
        if (groupingUniqueForType.isRight()) {
            log.debug("Failed validation of whether similar grouping exists, normalizedName {} componentType {}", normalizedName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(groupingUniqueForType.right().value());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }
        GroupingDefinition groupingDefinition = groupingUniqueForType.left().value();
        if (groupingDefinition != null) {
            groupingName = groupingDefinition.getName();
        }

        grouping.setName(groupingName);
        ///////////////////////////////////////////// Validations end

        Either<GroupingDefinition, ActionStatus> createGrouping = elementOperation.createGrouping(parentSubCategoryId, grouping, childNodeType);
        if (createGrouping.isRight()) {
            log.debug("Failed to create grouping, name {}, componentType {}", groupingName, componentType);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(createGrouping.right().value());
            handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, origGroupingName, auditingAction, componentType);
            return Either.right(responseFormat);
        }

        GroupingDefinition groupingCreated = createGrouping.left().value();
        log.debug("Created grouping for component {}, name {}, uniqueId {}", componentType, groupingName, groupingCreated.getUniqueId());
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CREATED);
        handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, groupingCreated.getName(), auditingAction, componentType);
        return Either.left(groupingCreated);
    }



    public Either<List<CategoryDefinition>, ResponseFormat> getAllCategories(String componentType, String userId) {
        ResponseFormat responseFormat;
        User user = new User();
        if (userId == null) {
            user.setUserId("UNKNOWN");
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
            componentsUtils.auditGetCategoryHierarchy(user, componentType, responseFormat);
            return Either.right(responseFormat);
        }
        try {
            user = validateUserExists(userId);
        } catch (ByActionStatusComponentException e){
            responseFormat = componentsUtils.getResponseFormat(e.getActionStatus(), e.getParams());
            handleComponentException(componentType, userId, responseFormat);
            throw e;
        } catch (ByResponseFormatComponentException e){
            responseFormat = e.getResponseFormat();
            handleComponentException(componentType, userId, responseFormat);
            throw e;
        }
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum == null) {
            log.debug("Cannot create category for component type {}", componentType);
            responseFormat = componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, "component type");
            componentsUtils.auditGetCategoryHierarchy(user, componentType, responseFormat);
            return Either.right(responseFormat);
        }

        NodeTypeEnum nodeTypeEnum = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.CATEGORY);
        Either<List<CategoryDefinition>, ActionStatus> getAllCategoriesByType = elementOperation.getAllCategories(nodeTypeEnum, false);
        if (getAllCategoriesByType.isRight()) {
            responseFormat = componentsUtils.getResponseFormat(getAllCategoriesByType.right().value());
            componentsUtils.auditGetCategoryHierarchy(user, componentType, responseFormat);
            return Either.right(responseFormat);
        }
        List<CategoryDefinition> categories = getAllCategoriesByType.left().value();
        responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
        componentsUtils.auditGetCategoryHierarchy(user, componentType, responseFormat);
        return Either.left(categories);
    }



    public Either<UiCategories, ResponseFormat> getAllCategories(String userId) {
        ResponseFormat responseFormat;
        UiCategories categories = new UiCategories();

        User user = validateUserExists(userId);

        // GET resource categories
        Either<List<CategoryDefinition>, ActionStatus> getResourceCategoriesByType = elementOperation.getAllCategories(NodeTypeEnum.ResourceNewCategory, false);
        if (getResourceCategoriesByType.isRight()) {
            responseFormat = componentsUtils.getResponseFormat(getResourceCategoriesByType.right().value());
            componentsUtils.auditGetCategoryHierarchy(user, ComponentTypeEnum.RESOURCE.getValue(), responseFormat);
            return Either.right(responseFormat);
        }
        categories.setResourceCategories(getResourceCategoriesByType.left().value());

        // GET service categories
        Either<List<CategoryDefinition>, ActionStatus> getServiceCategoriesByType = elementOperation.getAllCategories(NodeTypeEnum.ServiceNewCategory, false);
        if (getServiceCategoriesByType.isRight()) {
            responseFormat = componentsUtils.getResponseFormat(getServiceCategoriesByType.right().value());
            componentsUtils.auditGetCategoryHierarchy(user, ComponentTypeEnum.SERVICE.getValue(), responseFormat);
            return Either.right(responseFormat);
        }
        categories.setServiceCategories(getServiceCategoriesByType.left().value());
        categories.setProductCategories(new ArrayList<>());
        return Either.left(categories);
    }

    public Either<CategoryDefinition, ResponseFormat> deleteCategory(String categoryId, String componentTypeParamName, String userId) {

        validateUserExists(userId);

        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentTypeParamName);
        if (componentTypeEnum == null) {
            log.debug("Cannot create category for component type {}", componentTypeParamName);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        NodeTypeEnum nodeTypeEnum = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.CATEGORY);

        Either<CategoryDefinition, ActionStatus> deleteCategoryByType = elementOperation.deleteCategory(nodeTypeEnum, categoryId);
        if (deleteCategoryByType.isRight()) {
            // auditing, logging here...
            return Either.right(componentsUtils.getResponseFormat(deleteCategoryByType.right().value()));
        }
        CategoryDefinition category = deleteCategoryByType.left().value();
        log.debug("Delete category for component {}, name {}, uniqueId {}", nodeTypeEnum, category.getName(), category.getUniqueId());
        return Either.left(category);
    }

    public Either<SubCategoryDefinition, ResponseFormat> deleteSubCategory(String parentSubCategoryId, String componentTypeParamName, String userId) {

        validateUserExists(userId);

        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentTypeParamName);
        if (componentTypeEnum == null) {
            log.debug("Cannot delete sub-category for component type {}", componentTypeParamName);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        NodeTypeEnum nodeTypeEnum = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.SUBCATEGORY);

        Either<SubCategoryDefinition, ActionStatus> deleteSubCategoryByType = elementOperation.deleteSubCategory(nodeTypeEnum, parentSubCategoryId);
        if (deleteSubCategoryByType.isRight()) {
            // auditing, logging here...
            return Either.right(componentsUtils.getResponseFormat(deleteSubCategoryByType.right().value()));
        }
        SubCategoryDefinition subCategory = deleteSubCategoryByType.left().value();
        log.debug("Deleted sub-category for component {}, name {}, uniqueId {}", nodeTypeEnum, subCategory.getName(), subCategory.getUniqueId());
        return Either.left(subCategory);
    }

    public Either<GroupingDefinition, ResponseFormat> deleteGrouping(String groupingId, String componentTypeParamName, String userId) {

        validateUserExists(userId);

        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentTypeParamName);
        if (componentTypeEnum == null) {
            log.debug("Cannot delete grouping for component type {}", componentTypeParamName);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        NodeTypeEnum nodeTypeEnum = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.GROUPING);

        Either<GroupingDefinition, ActionStatus> deleteGroupingByType = elementOperation.deleteGrouping(nodeTypeEnum, groupingId);
        if (deleteGroupingByType.isRight()) {
            // auditing, logging here...
            return Either.right(componentsUtils.getResponseFormat(deleteGroupingByType.right().value()));
        }
        GroupingDefinition deletedGrouping = deleteGroupingByType.left().value();
        log.debug("Deleted grouping for component {}, name {}, uniqueId {}", nodeTypeEnum, deletedGrouping.getName(), deletedGrouping.getUniqueId());
        return Either.left(deletedGrouping);
    }

    private Either<Boolean, ResponseFormat> validateUserRole(User user, ComponentTypeEnum componentTypeEnum) {
        String role = user.getRole();
        boolean validAdminAction = role.equals(Role.ADMIN.name()) && (componentTypeEnum == ComponentTypeEnum.SERVICE || componentTypeEnum == ComponentTypeEnum.RESOURCE);
        boolean validProductAction = role.equals(Role.PRODUCT_STRATEGIST.name()) && (componentTypeEnum == ComponentTypeEnum.PRODUCT);

        if (!(validAdminAction || validProductAction)) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
            log.debug("User not permitted to perform operation on category, userId = {}, role = {}, componentType = {}", user.getUserId(), role, componentTypeEnum);
            return Either.right(responseFormat);
        }
        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validateComponentTypeForCategory(ComponentTypeEnum componentType, CategoryTypeEnum categoryType) {
        boolean validResourceAction = componentType == ComponentTypeEnum.RESOURCE && (categoryType == CategoryTypeEnum.CATEGORY || categoryType == CategoryTypeEnum.SUBCATEGORY);
        boolean validServiceAction = componentType == ComponentTypeEnum.SERVICE && categoryType == CategoryTypeEnum.CATEGORY;
        boolean validProductAction = componentType == ComponentTypeEnum.PRODUCT; // can
                                                                                 // be
                                                                                 // any
                                                                                 // category
                                                                                 // type

        if (!(validResourceAction || validServiceAction || validProductAction)) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
            log.debug("It's not allowed to create category type {} for component type {}", categoryType, componentType);
            return Either.right(responseFormat);
        }
        return Either.left(true);
    }

    private Either<CategoryDefinition, ResponseFormat> validateCategoryExists(NodeTypeEnum nodeType, String categoryId, ComponentTypeEnum componentType) {
        Either<CategoryDefinition, ActionStatus> categoryByTypeAndId = elementOperation.getCategory(nodeType, categoryId);
        if (categoryByTypeAndId.isRight()) {
            log.debug("Failed to fetch parent category, parent categoryId {}", categoryId);
            ActionStatus actionStatus = categoryByTypeAndId.right().value();
            ResponseFormat responseFormat;
            if (actionStatus == ActionStatus.COMPONENT_CATEGORY_NOT_FOUND) {
                responseFormat = componentsUtils.getResponseFormat(actionStatus, componentType.getValue().toLowerCase(), CategoryTypeEnum.CATEGORY.getValue(), "");
            } else {
                responseFormat = componentsUtils.getResponseFormat(actionStatus);
            }
            return Either.right(responseFormat);
        }
        return Either.left(categoryByTypeAndId.left().value());
    }

    private Either<SubCategoryDefinition, ResponseFormat> validateSubCategoryExists(NodeTypeEnum nodeType, String subCategoryId, ComponentTypeEnum componentType) {
        Either<SubCategoryDefinition, ActionStatus> subCategoryByTypeAndId = elementOperation.getSubCategory(nodeType, subCategoryId);
        if (subCategoryByTypeAndId.isRight()) {
            log.debug("Failed to fetch parent category, parent categoryId {}", subCategoryId);
            ActionStatus actionStatus = subCategoryByTypeAndId.right().value();
            ResponseFormat responseFormat;
            if (actionStatus == ActionStatus.COMPONENT_CATEGORY_NOT_FOUND) {
                responseFormat = componentsUtils.getResponseFormat(actionStatus, componentType.getValue().toLowerCase(), CategoryTypeEnum.SUBCATEGORY.getValue(), "");
            } else {
                responseFormat = componentsUtils.getResponseFormat(actionStatus);
            }
            return Either.right(responseFormat);
        }
        return Either.left(subCategoryByTypeAndId.left().value());
    }

    private void handleCategoryAuditing(ResponseFormat responseFormat, User user, String category, AuditingActionEnum auditingAction, String componentType) {
        componentsUtils.auditCategory(responseFormat, user, category, null, null, auditingAction, componentType);
    }

    private void handleCategoryAuditing(ResponseFormat responseFormat, User user, String category, String subCategory, AuditingActionEnum auditingAction, String componentType) {
        componentsUtils.auditCategory(responseFormat, user, category, subCategory, null, auditingAction, componentType);
    }

    private void handleCategoryAuditing(ResponseFormat responseFormat, User user, String category, String subCategory, String grouping, AuditingActionEnum auditingAction, String componentType) {
        componentsUtils.auditCategory(responseFormat, user, category, subCategory, grouping, auditingAction, componentType);
    }

    /*
     * New categories flow - end
     */

    public Either<List<Tag>, ActionStatus> getAllTags(String userId) {
        ActionStatus status = validateUserExistsActionStatus(userId);
        if (ActionStatus.OK != status) {
            return Either.right(status);
        }
        return elementOperation.getAllTags();
    }

    public Either<List<PropertyScope>, ActionStatus> getAllPropertyScopes(String userId) {
        ActionStatus status = validateUserExistsActionStatus(userId);
        if (ActionStatus.OK != status) {
            return Either.right(status);
        }
        return elementOperation.getAllPropertyScopes();
    }

    public Either<List<ArtifactType>, ActionStatus> getAllArtifactTypes(String userId) {
        ActionStatus status = validateUserExistsActionStatus(userId);
        if (ActionStatus.OK != status) {
            return Either.right(status);
        }
        return elementOperation.getAllArtifactTypes();
    }

    public Either<Map<String, Object>, ActionStatus> getAllDeploymentArtifactTypes() {
        return elementOperation.getAllDeploymentArtifactTypes();
    }

    public Either<Configuration.HeatDeploymentArtifactTimeout, ActionStatus> getDefaultHeatTimeout() {
        return elementOperation.getDefaultHeatTimeout();
    }

	public Either<Map<String, List<CatalogComponent>>, ResponseFormat> getCatalogComponents(String userId, List<OriginTypeEnum> excludeTypes) {
        try {
            return toscaOperationFacade.getCatalogOrArchiveComponents(true, excludeTypes)
                    .bimap(this::groupByComponentType,
                            err -> componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(err)));
        } finally {
            janusGraphDao.commit();
        }
    }

    private Map<String, List<CatalogComponent>> groupByComponentType(List<CatalogComponent> components) {
        Map<String, List<CatalogComponent>> map = components.stream().collect(Collectors.groupingBy(cmpt -> cmptTypeToString(cmpt.getComponentType())));

        // fixed response for UI!!! UI need to receive always map!
        if (map == null) {
            map = new HashMap<>();
        }
        map.computeIfAbsent(RESOURCES, k -> new ArrayList());
        map.computeIfAbsent(SERVICES, k -> new ArrayList());
        return map;
    }

    private String cmptTypeToString(ComponentTypeEnum componentTypeEnum) {
        switch (componentTypeEnum) {
        case RESOURCE:
            return RESOURCES;
        case SERVICE:
            return SERVICES;
        default:
            throw new IllegalStateException("resources or services only");
        }
    }

    public Either<List<? extends Component>, ResponseFormat> getFilteredCatalogComponents(String assetType, Map<FilterKeyEnum, String> filters, String query) {
        ComponentTypeEnum assetTypeEnum = AssetTypeEnum.convertToComponentTypeEnum(assetType);

        if (query != null) {
            Optional<NameValuePair> invalidFilter = findInvalidFilter(query, assetTypeEnum);
            if (invalidFilter.isPresent()) {
                log.debug("getFilteredAssetList: invalid filter key");
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_FILTER_KEY, invalidFilter.get().getName(), FilterKeyEnum.getValidFiltersByAssetType(assetTypeEnum).toString()));
            }
        }

        if (filters == null || filters.isEmpty()) {
            Either<List<Component>, StorageOperationStatus> componentsList = toscaOperationFacade.getCatalogComponents(assetTypeEnum, null, false);
            if (componentsList.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(componentsList.right().value())));
            }
            return Either.left(componentsList.left().value());
        }

        Either<List<Component>, StorageOperationStatus> result = getFilteredComponents(filters, assetTypeEnum, false);

        // category hierarchy mismatch or category/subCategory/distributionStatus not found
        if (result.isRight()) {
            List<String> params = getErrorResponseParams(filters, assetTypeEnum);
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value()), params.get(0), params.get(1), params.get(2)));
        }
        if (result.left().value().isEmpty()) {// no assets found for requested
                                              // criteria
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.NO_ASSETS_FOUND, assetType, query));
        }
        return Either.left(result.left().value());
    }

    private Either<List<Component>, StorageOperationStatus> getFilteredComponents(Map<FilterKeyEnum, String> filters, ComponentTypeEnum assetType, boolean inTransaction) {
        Either<List<Component>, StorageOperationStatus> assetResult = Either.left(new LinkedList<>());
        if (assetType == ComponentTypeEnum.RESOURCE) {

            assetResult = getFilteredResources(filters, inTransaction);

        } else if (assetType == ComponentTypeEnum.SERVICE) {

            assetResult = getFilteredServices(filters, inTransaction);
        }
        return assetResult;
    }

    private <T extends Component> Either<List<T>, StorageOperationStatus> getFilteredServices(Map<FilterKeyEnum, String> filters, boolean inTransaction) {

        Either<List<T>, StorageOperationStatus> components = null;

        String categoryName = filters.get(FilterKeyEnum.CATEGORY);
        String distributionStatus = filters.get(FilterKeyEnum.DISTRIBUTION_STATUS);
        DistributionStatusEnum distEnum = DistributionStatusEnum.findState(distributionStatus);
        if (distributionStatus != null && distEnum == null) {
            filters.remove(FilterKeyEnum.CATEGORY);
            return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
        }

        if (categoryName != null) { // primary filter
            components = fetchByCategoryOrSubCategoryName(categoryName, NodeTypeEnum.ServiceNewCategory, NodeTypeEnum.Service, inTransaction, null);
            if (components.isLeft() && distEnum != null) {// secondary filter
                Predicate<T> statusFilter = p -> ((Service) p).getDistributionStatus() == distEnum;
                return Either.left(components.left().value().stream().filter(statusFilter).collect(Collectors.toList()));
            }
            filters.remove(FilterKeyEnum.DISTRIBUTION_STATUS);
            return components;
        }

        Set<DistributionStatusEnum> distStatusSet = new HashSet<>();
        distStatusSet.add(distEnum);
        Either<List<Service>, StorageOperationStatus> servicesWithDistStatus = toscaOperationFacade.getServicesWithDistStatus(distStatusSet, null);
        if (servicesWithDistStatus.isRight()) { // not found == empty list
            return Either.left(new ArrayList<>());
        }

        return Either.left((List<T>) servicesWithDistStatus.left().value());
    }

    public Either<List<? extends Component>, ResponseFormat> getCatalogComponentsByUuidAndAssetType(String assetType, String uuid) {

        if (assetType == null || uuid == null) {
            log.debug("getCatalogComponentsByUuidAndAssetType: One of the function parameteres is null");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        ComponentTypeEnum assetTypeEnum = AssetTypeEnum.convertToComponentTypeEnum(assetType);

        if (assetTypeEnum == null) {
            log.debug("getCatalogComponentsByUuidAndAssetType: Corresponding ComponentTypeEnum not found");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        Map<GraphPropertyEnum, Object> additionalPropertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);

        switch (assetTypeEnum) {
        case RESOURCE:
            additionalPropertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
            break;
        case SERVICE:
            additionalPropertiesToMatch.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
            break;
        default:
            log.debug("getCatalogComponentsByUuidAndAssetType: Corresponding ComponentTypeEnum not allowed for this API");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
        }

        Either<List<Component>, StorageOperationStatus> componentsListByUuid = toscaOperationFacade.getComponentListByUuid(uuid, additionalPropertiesToMatch);
        if (componentsListByUuid.isRight()) {
            log.debug("getCatalogComponentsByUuidAndAssetType: " + assetTypeEnum.getValue() + " fetching failed");
            ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(componentsListByUuid.right().value(), assetTypeEnum);
            return Either.right(componentsUtils.getResponseFormat(actionStatus, uuid));
        }

        log.debug("getCatalogComponentsByUuidAndAssetType: " + assetTypeEnum.getValue() + assetTypeEnum.getValue() + "fetching successful");
        return Either.left(componentsListByUuid.left().value());
    }

    public List<String> getAllComponentTypesParamNames() {
        List<String> paramNames = new ArrayList<>();
        paramNames.add(ComponentTypeEnum.SERVICE_PARAM_NAME);
        paramNames.add(ComponentTypeEnum.RESOURCE_PARAM_NAME);
        paramNames.add(ComponentTypeEnum.PRODUCT_PARAM_NAME);
        return paramNames;
    }

    public List<String> getAllSupportedRoles() {
        Role[] values = Role.values();
        List<String> roleNames = new ArrayList<>();
        for (Role role : values) {
            roleNames.add(role.name());
        }
        return roleNames;
    }

    public Either<Map<String, String>, ActionStatus> getResourceTypesMap() {
        return elementOperation.getResourceTypesMap();
    }

    private Optional<NameValuePair> findInvalidFilter(String query, ComponentTypeEnum assetType) {
        List<NameValuePair> params = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
        List<String> validKeys = FilterKeyEnum.getValidFiltersByAssetType(assetType);
        Predicate<NameValuePair> noMatch = p -> !validKeys.contains(p.getName());
        return params.stream().filter(noMatch).findAny();
    }

    private List<String> getErrorResponseParams(Map<FilterKeyEnum, String> filters, ComponentTypeEnum assetType) {
        List<String> params = new ArrayList<>();
        if (1 == filters.size()) {
            params.add(assetType.getValue().toLowerCase());
            params.add(filters.keySet().iterator().next().getName());
            params.add(filters.values().iterator().next());
        } else {
            params.add(assetType.getValue());
            params.add(filters.get(FilterKeyEnum.SUB_CATEGORY));
            params.add(filters.get(FilterKeyEnum.CATEGORY));
        }
        return params;
    }

    public Either<List<Component>, StorageOperationStatus> getFilteredResources(Map<FilterKeyEnum, String> filters, boolean inTransaction) {

        String subCategoryName = filters.get(FilterKeyEnum.SUB_CATEGORY);
        String categoryName = filters.get(FilterKeyEnum.CATEGORY);
        ResourceTypeEnum resourceType = ResourceTypeEnum.getType(filters.get(FilterKeyEnum.RESOURCE_TYPE));
        Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, StorageOperationStatus> subcategories = null;
        Optional<ImmutablePair<SubCategoryData, GraphEdge>> subCategoryData;

        if (categoryName != null) {
            subcategories = getAllSubCategories(categoryName);
            if (subcategories.isRight()) {
                filters.remove(FilterKeyEnum.SUB_CATEGORY);
                return Either.right(subcategories.right().value());
            }
        }
        if (subCategoryName != null) { // primary filter
            if (categoryName != null) {
                subCategoryData = validateCategoryHierarcy(subcategories.left().value(), subCategoryName);
                if (!subCategoryData.isPresent()) {
                    return Either.right(StorageOperationStatus.MATCH_NOT_FOUND);
                }
                return fetchByCategoryOrSubCategoryUid(subCategoryData.get().getLeft().getUniqueId(), NodeTypeEnum.Resource, inTransaction, resourceType);
            }

            return fetchByCategoryOrSubCategoryName(subCategoryName, NodeTypeEnum.ResourceSubcategory, NodeTypeEnum.Resource, inTransaction, resourceType);
        }
        if (subcategories != null) {
            return fetchByMainCategory(subcategories.left().value(), inTransaction, resourceType);
        }
        return fetchComponentMetaDataByResourceType(filters.get(FilterKeyEnum.RESOURCE_TYPE), inTransaction);
    }

    private Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, StorageOperationStatus> getAllSubCategories(String categoryName) {
        Either<CategoryData, StorageOperationStatus> categoryResult = elementOperation.getNewCategoryData(categoryName, NodeTypeEnum.ResourceNewCategory, CategoryData.class);
        if (categoryResult.isRight()) {
            return Either.right(categoryResult.right().value());
        }
        CategoryData categoryData = categoryResult.left().value();

        Either<List<ImmutablePair<SubCategoryData, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
            .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceNewCategory), (String) categoryData.getUniqueId(),
                GraphEdgeLabels.SUB_CATEGORY, NodeTypeEnum.ResourceSubcategory, SubCategoryData.class);
        if (childrenNodes.isRight()) {
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(childrenNodes.right().value()));
        }
        return Either.left(childrenNodes.left().value());
    }

    private Optional<ImmutablePair<SubCategoryData, GraphEdge>> validateCategoryHierarcy(List<ImmutablePair<SubCategoryData, GraphEdge>> childNodes, String subCategoryName) {
        Predicate<ImmutablePair<SubCategoryData, GraphEdge>> matchName = p -> p.getLeft().getSubCategoryDataDefinition().getName().equals(subCategoryName);
        return childNodes.stream().filter(matchName).findAny();
    }

    protected <T extends Component> Either<List<T>, StorageOperationStatus> fetchByCategoryOrSubCategoryUid(String categoryUid, NodeTypeEnum categoryType, boolean inTransaction, ResourceTypeEnum resourceType) {
        try {
            return collectComponents(categoryType, categoryUid, resourceType);
        } finally {
            if (!inTransaction) {
                janusGraphDao.commit();
            }
        }
    }

    protected <T extends Component> Either<List<T>, StorageOperationStatus> fetchByCategoryOrSubCategoryName(String categoryName, NodeTypeEnum categoryType, NodeTypeEnum neededType, boolean inTransaction, ResourceTypeEnum resourceType) {
        List<T> components = new ArrayList<>();
        try {
            Class categoryClazz = categoryType == NodeTypeEnum.ServiceNewCategory ? CategoryData.class : SubCategoryData.class;
            Map<String, Object> props = new HashMap<>();
            props.put(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty(), ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
            Either<List<GraphNode>, JanusGraphOperationStatus> getCategory = janusGraphGenericDao.getByCriteria(categoryType, props, categoryClazz);
            if (getCategory.isRight()) {
                return Either.right(StorageOperationStatus.CATEGORY_NOT_FOUND);
            }
            for (GraphNode category : getCategory.left().value()) {
                Either<List<T>, StorageOperationStatus> result = collectComponents(neededType, category.getUniqueId(), resourceType);
                if (result.isRight() && result.right().value() != StorageOperationStatus.NOT_FOUND) {
                    return result;
                } else if (result.isLeft()){
                    components.addAll(result.left().value());
                }
            }
            if (components.isEmpty()){
                return Either.right(StorageOperationStatus.NOT_FOUND);
            }
            return Either.left(components);
        } finally {
            if (!inTransaction) {
                janusGraphDao.commit();
            }
        }
    }


    private <T extends Component> Either<List<T>, StorageOperationStatus> collectComponents(NodeTypeEnum neededType, String categoryUid, ResourceTypeEnum resourceType) {
        List<T> components = new ArrayList<>();
        Either<GraphVertex, JanusGraphOperationStatus> categoryVertexById = janusGraphDao.getVertexById(categoryUid, JsonParseFlagEnum.NoParse);
        if (categoryVertexById.isRight()){
            JanusGraphOperationStatus status = categoryVertexById.right().value();
            log.debug("#collectComponents Failed to get category vertex with uid {}, status is {}.", categoryUid, status);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        GraphVertex categoryVertex = categoryVertexById.left().value();
        Either<List<GraphVertex>, JanusGraphOperationStatus> componentsVertices = janusGraphDao.getParentVertices(categoryVertex, EdgeLabelEnum.CATEGORY, JsonParseFlagEnum.ParseMetadata);
        if (componentsVertices.isRight()){
            JanusGraphOperationStatus status = componentsVertices.right().value();
            log.debug("#collectComponents Failed to get components vertices of category {}, status is {}.", categoryVertex, status);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status));
        }
        List<ComponentMetadataDataDefinition> componentsMetadataDataDefinition = componentsVertices.left().value()
                .stream()
                .filter(Objects::nonNull)
                .filter(componentsVertex -> Objects.nonNull(componentsVertex.getType()))
                .map(ModelConverter::convertToComponentMetadataDataDefinition)
                .collect(Collectors.toList());
        for (ComponentMetadataDataDefinition component : componentsMetadataDataDefinition) {
            boolean isHighest = isTrue(component.isHighestVersion());
            boolean isMatchingResourceType = isMatchingByResourceType(neededType, resourceType, component);
            boolean isDeleted = isTrue(component.isDeleted());
            boolean isArchived = isTrue(component.isArchived());
            if (isHighest && isMatchingResourceType && !isDeleted && !isArchived) {
                Either<T, StorageOperationStatus> result = toscaOperationFacade.getToscaElement(component.getUniqueId(), JsonParseFlagEnum.ParseMetadata);
                 if (result.isRight()) {
                    return Either.right(result.right().value());
                }
                components.add(result.left().value());
            }
        }
        return Either.left(components);
    }

    private boolean isMatchingByResourceType(NodeTypeEnum componentType, ResourceTypeEnum resourceType, ComponentMetadataDataDefinition componentData) {

        boolean isMatching;
        if (componentType == NodeTypeEnum.Resource) {
            if (resourceType == null) {
                isMatching = true;
            } else {
                isMatching = resourceType == ((ResourceMetadataDataDefinition) componentData).getResourceType();
            }
        } else {
            isMatching = true;
        }
        return isMatching;
    }

    private <T extends Component> Either<List<T>, StorageOperationStatus> fetchByMainCategory(List<ImmutablePair<SubCategoryData, GraphEdge>> subcategories, boolean inTransaction, ResourceTypeEnum resourceType) {
        List<T> components = new ArrayList<>();

        for (ImmutablePair<SubCategoryData, GraphEdge> subCategory : subcategories) {
            Either<List<T>, StorageOperationStatus> fetched = fetchByCategoryOrSubCategoryUid(subCategory.getLeft().getUniqueId(), NodeTypeEnum.Resource,
                    inTransaction, resourceType);
            if (fetched.isRight()) {
                continue;
            }
            components.addAll(fetched.left().value());
        }
        return Either.left(components);
    }

    private Either<List<Component>, StorageOperationStatus> fetchComponentMetaDataByResourceType(String resourceType, boolean inTransaction) {
        List<Component> components = null;
        StorageOperationStatus status;
        Wrapper<StorageOperationStatus> statusWrapper = new Wrapper<>();
        Either<List<Component>, StorageOperationStatus> result;
        try {
            ComponentParametersView fetchUsersAndCategoriesFilter = new ComponentParametersView(Arrays.asList(ComponentFieldsEnum.USERS.getValue(), ComponentFieldsEnum.CATEGORIES.getValue()));
            Either<List<Component>, StorageOperationStatus> getResources = toscaOperationFacade.fetchMetaDataByResourceType(resourceType, fetchUsersAndCategoriesFilter);
            if (getResources.isRight()) {
                status = getResources.right().value();
                if (status != StorageOperationStatus.NOT_FOUND) {
                    statusWrapper.setInnerElement(getResources.right().value());
                } else {
                    components = new ArrayList<>();
                }
            } else {
                components = getResources.left().value();
            }
            if (!statusWrapper.isEmpty()) {
                result = Either.right(statusWrapper.getInnerElement());
            } else {
                result = Either.left(components);
            }
            return result;
        } finally {
            if (!inTransaction) {
                janusGraphDao.commit();
            }
        }
    }


    public CatalogUpdateTimestamp getCatalogUpdateTime(String userId) {

        try{
            return toscaOperationFacade.getCatalogTimes();

        } finally {
            janusGraphDao.commit();
        }

    }
}
