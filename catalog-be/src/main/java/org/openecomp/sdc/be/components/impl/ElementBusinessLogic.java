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

package org.openecomp.sdc.be.components.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datamodel.api.CategoryTypeEnum;
import org.openecomp.sdc.be.datamodel.utils.NodeTypeConvertUtils;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("elementsBusinessLogic")
public class ElementBusinessLogic extends BaseBusinessLogic {

	private static Logger log = LoggerFactory.getLogger(ElementBusinessLogic.class.getName());

	@javax.annotation.Resource
	private IElementOperation elementOperation;

	@javax.annotation.Resource
	private ComponentsUtils componentsUtils;

	@javax.annotation.Resource
	private UserBusinessLogic userAdminManager;

	/**
	 * 
	 * @param user
	 * @return
	 */
	public Either<Map<String, List<? extends Component>>, ResponseFormat> getFollowed(User user) {
		Either<Map<String, List<? extends Component>>, ResponseFormat> response = null;
		// Getting the role
		String role = user.getRole();
		String userId = null;
		Role currentRole = Role.valueOf(role);

		switch (currentRole) {
		case DESIGNER:
			userId = user.getUserId();
			response = handleDesigner(userId);
			break;

		case TESTER:
			userId = user.getUserId();
			response = handleTester(userId);
			break;

		case GOVERNOR:
			userId = user.getUserId();
			response = handleGovernor(userId);
			break;

		case OPS:
			userId = user.getUserId();
			response = handleOps(userId);
			break;

		case PRODUCT_STRATEGIST:
			userId = user.getUserId();
			response = handleProductStrategist(userId);
			break;

		case PRODUCT_MANAGER:
			userId = user.getUserId();
			response = handleProductManager(userId);
			break;

		case ADMIN:
			response = handleAdmin();
			break;

		default:
			response = Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			break;
		}
		return response;

	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> handleAdmin() {
		Either<Map<String, List<? extends Component>>, ResponseFormat> response;
		// userId should stay null
		Set<LifecycleStateEnum> lifecycleStates = new HashSet<LifecycleStateEnum>();
		Set<LifecycleStateEnum> lastStateStates = new HashSet<LifecycleStateEnum>();
		lifecycleStates.add(LifecycleStateEnum.CERTIFIED);
		response = getFollowedResourcesAndServices(null, lifecycleStates, lastStateStates);
		return response;
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> handleDesigner(String userId) {
		Set<LifecycleStateEnum> lifecycleStates = new HashSet<LifecycleStateEnum>();
		Set<LifecycleStateEnum> lastStateStates = new HashSet<LifecycleStateEnum>();
		Either<Map<String, List<? extends Component>>, ResponseFormat> response;
		lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		lifecycleStates.add(LifecycleStateEnum.READY_FOR_CERTIFICATION);
		lifecycleStates.add(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		lifecycleStates.add(LifecycleStateEnum.CERTIFIED);
		// more states
		lastStateStates.add(LifecycleStateEnum.READY_FOR_CERTIFICATION);
		response = getFollowedResourcesAndServices(userId, lifecycleStates, lastStateStates);
		return response;
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> handleGovernor(String userId) {
		Either<Map<String, List<? extends Component>>, ResponseFormat> result = handleFollowedCertifiedServices(null);
		return result;
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> handleProductStrategist(String userId) {
		// Should be empty list according to Ella, 13/03/16
		Map<String, List<? extends Component>> result = new HashMap<String, List<? extends Component>>();
		result.put("products", new ArrayList<>());
		return Either.left(result);
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> handleProductManager(String userId) {
		Set<LifecycleStateEnum> lifecycleStates = new HashSet<LifecycleStateEnum>();
		Set<LifecycleStateEnum> lastStateStates = new HashSet<LifecycleStateEnum>();
		Either<Map<String, List<? extends Component>>, ResponseFormat> response;
		lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		lifecycleStates.add(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		lifecycleStates.add(LifecycleStateEnum.READY_FOR_CERTIFICATION);
		lifecycleStates.add(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		lifecycleStates.add(LifecycleStateEnum.CERTIFIED);
		// more states
		lastStateStates.add(LifecycleStateEnum.READY_FOR_CERTIFICATION);
		response = getFollowedProducts(userId, lifecycleStates, lastStateStates);
		return response;
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> handleOps(String userId) {
		Set<DistributionStatusEnum> distStatus = new HashSet<DistributionStatusEnum>();
		distStatus.add(DistributionStatusEnum.DISTRIBUTION_APPROVED);
		distStatus.add(DistributionStatusEnum.DISTRIBUTED);

		Either<Map<String, List<? extends Component>>, ResponseFormat> result = handleFollowedCertifiedServices(distStatus);
		return result;
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> handleFollowedCertifiedServices(Set<DistributionStatusEnum> distStatus) {
		Map<String, Object> propertiesToMatch = new HashMap<>();
		propertiesToMatch.put(GraphPropertiesDictionary.STATE.getProperty(), LifecycleStateEnum.CERTIFIED.name());

		Either<Set<Service>, StorageOperationStatus> services = serviceOperation.getCertifiedServicesWithDistStatus(propertiesToMatch, distStatus, false);
		if (services.isLeft()) {
			Map<String, List<? extends Component>> result = new HashMap<String, List<? extends Component>>();
			List<Service> list = new ArrayList<>();
			list.addAll(services.left().value());
			result.put("services", list);
			return Either.left(result);
		} else {
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(services.right().value())));
		}
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> handleTester(String userId) {
		Set<LifecycleStateEnum> lifecycleStates = new HashSet<LifecycleStateEnum>();
		lifecycleStates.add(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<List<Resource>, StorageOperationStatus> resources = resourceOperation.getTesterFollowed(userId, lifecycleStates, false);

		if (resources.isLeft()) {
			Either<List<Service>, StorageOperationStatus> services = serviceOperation.getTesterFollowed(userId, lifecycleStates, false);
			if (services.isLeft()) {
				Map<String, List<? extends Component>> result = new HashMap<String, List<? extends Component>>();
				result.put("services", services.left().value());
				result.put("resources", resources.left().value());
				return Either.left(result);
			} else {
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(services.right().value())));
			}
		} else {
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resources.right().value())));
		}
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> getFollowedResourcesAndServices(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates) {
		Either<List<Resource>, StorageOperationStatus> resources = resourceOperation.getFollowed(userId, lifecycleStates, lastStateStates, false);

		if (resources.isLeft()) {
			Either<List<Service>, StorageOperationStatus> services = serviceOperation.getFollowed(userId, lifecycleStates, lastStateStates, false);
			if (services.isLeft()) {
				Map<String, List<? extends Component>> result = new HashMap<String, List<? extends Component>>();
				result.put("services", services.left().value());
				result.put("resources", resources.left().value());
				return Either.left(result);
			} else {
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(services.right().value())));
			}
		} else {
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resources.right().value())));
		}
	}

	private Either<Map<String, List<? extends Component>>, ResponseFormat> getFollowedProducts(String userId, Set<LifecycleStateEnum> lifecycleStates, Set<LifecycleStateEnum> lastStateStates) {
		Either<List<Product>, StorageOperationStatus> products = productOperation.getFollowed(userId, lifecycleStates, lastStateStates, false);
		if (products.isLeft()) {
			Map<String, List<? extends Component>> result = new HashMap<String, List<? extends Component>>();
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
		String componentType = (componentTypeEnum == null ? componentTypeParamName : componentTypeEnum.getValue());
		CategoryTypeEnum categoryType = CategoryTypeEnum.CATEGORY;

		User user = new User();
		Either<User, ResponseFormat> validateUser = validateUser(userId);
		if (validateUser.isRight()) {
			log.debug("Validation of user failed, userId {}", userId);
			ResponseFormat responseFormat = validateUser.right().value();
			user = new User();
			user.setUserId(userId);
			String currCategoryName = (category == null ? null : category.getName());
			handleCategoryAuditing(responseFormat, user, currCategoryName, auditingAction, componentType);
			return Either.right(responseFormat);
		}

		user = validateUser.left().value();

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
			log.debug("Component type {} is invalid", componentTypeParamName);
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
			handleCategoryAuditing(responseFormat, user, origCategoryName, auditingAction, componentType);
			return Either.right(responseFormat);
		}

		Either<Boolean, ResponseFormat> validateUserRole = validateUserRole(user, componentTypeEnum);
		if (validateUserRole.isRight()) {
			log.debug("Validation of user role failed, userId {}", userId);
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
		String componentType = (componentTypeEnum == null ? componentTypeParamName : componentTypeEnum.getValue());
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

		User user = new User();
		/*
		 * if (userId == null) { user.setUserId("UNKNOWN"); ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION); handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName,
		 * auditingAction, componentType); return Either.right(responseFormat); }
		 */
		Either<User, ResponseFormat> validateUser = validateUserExists(userId, "createSubCategory", false);
		if (validateUser.isRight()) {
			log.debug("Validation of user failed, userId {}", userId);
			ResponseFormat responseFormat = validateUser.right().value();
			user = new User();
			user.setUserId(userId);
			handleCategoryAuditing(responseFormat, user, parentCategoryName, origSubCategoryName, auditingAction, componentType);
			return Either.right(responseFormat);
		}

		user = validateUser.left().value();

		if (componentTypeEnum == null) {
			log.debug("Component type {} is invalid", componentTypeParamName);
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
			log.debug("Validation of user role failed, userId {}", userId);
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

	public Either<GroupingDefinition, ResponseFormat> createGrouping(GroupingDefinition grouping, String componentTypeParamName, String grandParentCategoryId, String parentSubCategoryId, String userId) {

		AuditingActionEnum auditingAction = AuditingActionEnum.ADD_GROUPING;
		ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentTypeParamName);
		String componentType = (componentTypeEnum == null ? componentTypeParamName : componentTypeEnum.getValue());
		CategoryTypeEnum categoryType = CategoryTypeEnum.GROUPING;
		// For auditing
		String parentCategoryName = grandParentCategoryId;
		String parentSubCategoryName = parentSubCategoryId;

		User user;
		Either<User, ResponseFormat> validateUser = validateUserExists(userId, "create Grouping", false);
		if (validateUser.isRight()) {
			log.debug("Validation of user failed, userId {}", userId);
			ResponseFormat responseFormat = validateUser.right().value();
			user = new User();
			user.setUserId(userId);
			String groupingNameForAudit = (grouping == null ? null : grouping.getName());
			handleCategoryAuditing(responseFormat, user, parentCategoryName, parentSubCategoryName, groupingNameForAudit, auditingAction, componentType);
			return Either.right(responseFormat);
		}

		user = validateUser.left().value();

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
			log.debug("Component type {} is invalid", componentTypeParamName);
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
			log.debug("Validation of user role failed, userId {}", userId);
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
		AuditingActionEnum auditingAction = AuditingActionEnum.GET_CATEGORY_HIERARCHY;
		ResponseFormat responseFormat;
		User user = new User();
		if (userId == null) {
			user.setUserId("UNKNOWN");
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION);
			componentsUtils.auditGetCategoryHierarchy(auditingAction, user, componentType, responseFormat);
			return Either.right(responseFormat);
		}

		Either<User, ResponseFormat> validateUser = validateUserExists(userId, "get All Categories", false);
		if (validateUser.isRight()) {
			user.setUserId(userId);
			log.debug("Validation of user failed, userId {}", userId);
			responseFormat = validateUser.right().value();
			componentsUtils.auditGetCategoryHierarchy(auditingAction, user, componentType, responseFormat);
			return Either.right(responseFormat);
		}
		user = validateUser.left().value();

		ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
		if (componentTypeEnum == null) {
			log.debug("Cannot create category for component type {}", componentType);
			responseFormat = componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, "component type");
			componentsUtils.auditGetCategoryHierarchy(auditingAction, user, componentType, responseFormat);
			return Either.right(responseFormat);
		}

		NodeTypeEnum nodeTypeEnum = NodeTypeConvertUtils.getCategoryNodeTypeByComponentParam(componentTypeEnum, CategoryTypeEnum.CATEGORY);
		Either<List<CategoryDefinition>, ActionStatus> getAllCategoriesByType = elementOperation.getAllCategories(nodeTypeEnum, false);
		if (getAllCategoriesByType.isRight()) {
			responseFormat = componentsUtils.getResponseFormat(getAllCategoriesByType.right().value());
			componentsUtils.auditGetCategoryHierarchy(auditingAction, user, componentType, responseFormat);
			return Either.right(responseFormat);
		}
		List<CategoryDefinition> categories = getAllCategoriesByType.left().value();
		responseFormat = componentsUtils.getResponseFormat(ActionStatus.OK);
		componentsUtils.auditGetCategoryHierarchy(auditingAction, user, componentType, responseFormat);
		return Either.left(categories);
	}

	public Either<CategoryDefinition, ResponseFormat> deleteCategory(String categoryId, String componentTypeParamName, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "delete Category", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

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

	public Either<SubCategoryDefinition, ResponseFormat> deleteSubCategory(String grandParentCategoryId, String parentSubCategoryId, String componentTypeParamName, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "delete Sub Category", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

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

	public Either<GroupingDefinition, ResponseFormat> deleteGrouping(String grandParentCategoryId, String parentSubCategoryId, String groupingId, String componentTypeParamName, String userId) {

		Either<User, ResponseFormat> resp = validateUserExists(userId, "delete Grouping", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}

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

	private Either<User, ResponseFormat> validateUser(String userId) {

		// validate user exists
		if (userId == null) {
			log.debug("User id is null");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.MISSING_INFORMATION));
		}

		Either<User, ActionStatus> userResult = userAdminManager.getUser(userId, false);
		if (userResult.isRight()) {
			ResponseFormat responseFormat;
			if (userResult.right().value().equals(ActionStatus.USER_NOT_FOUND)) {
				log.debug("Not authorized user, userId = {}", userId);
				responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			} else {
				log.debug("Failed to authorize user, userId = {}", userId);
				responseFormat = componentsUtils.getResponseFormat(userResult.right().value());
			}

			return Either.right(responseFormat);
		}
		return Either.left(userResult.left().value());
		// ========================================-
	}

	private Either<Boolean, ResponseFormat> validateUserRole(User user, ComponentTypeEnum componentTypeEnum) {
		String role = user.getRole();
		boolean validAdminAction = (role.equals(Role.ADMIN.name()) && (componentTypeEnum == ComponentTypeEnum.SERVICE || componentTypeEnum == ComponentTypeEnum.RESOURCE));
		boolean validProductAction = (role.equals(Role.PRODUCT_STRATEGIST.name()) && (componentTypeEnum == ComponentTypeEnum.PRODUCT));

		if (!(validAdminAction || validProductAction)) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
			log.debug("User not permitted to perform operation on category, userId = {}, role = {}, componentType = {}", user.getUserId(), role, componentTypeEnum);
			return Either.right(responseFormat);
		}
		return Either.left(true);
	}

	private Either<Boolean, ResponseFormat> validateComponentTypeForCategory(ComponentTypeEnum componentType, CategoryTypeEnum categoryType) {
		boolean validResourceAction = (componentType == ComponentTypeEnum.RESOURCE && (categoryType == CategoryTypeEnum.CATEGORY || categoryType == CategoryTypeEnum.SUBCATEGORY));
		boolean validServiceAction = (componentType == ComponentTypeEnum.SERVICE && categoryType == CategoryTypeEnum.CATEGORY);
		boolean validProductAction = (componentType == ComponentTypeEnum.PRODUCT); // can
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
		Either<User, ActionStatus> resp = validateUserExistsActionStatus(userId, "get All Tags");
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		return elementOperation.getAllTags();
	}

	public Either<List<PropertyScope>, ActionStatus> getAllPropertyScopes(String userId) {
		Either<User, ActionStatus> resp = validateUserExistsActionStatus(userId, "get All Property Scopes");
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		return elementOperation.getAllPropertyScopes();
	}

	public Either<List<ArtifactType>, ActionStatus> getAllArtifactTypes(String userId) {
		Either<User, ActionStatus> resp = validateUserExistsActionStatus(userId, "get All Artifact Types");
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		return elementOperation.getAllArtifactTypes();
	}

	public Either<Map<String, Object>, ActionStatus> getAllDeploymentArtifactTypes() {
		return elementOperation.getAllDeploymentArtifactTypes();
	}

	public Either<Integer, ActionStatus> getDefaultHeatTimeout() {
		return elementOperation.getDefaultHeatTimeout();
	}

	public Either<Map<String, List<? extends Component>>, ResponseFormat> getCatalogComponents(String userId) {
		Either<User, ResponseFormat> resp = validateUserExists(userId, "get Catalog Components", false);
		if (resp.isRight()) {
			return Either.right(resp.right().value());
		}
		Map<String, List<? extends Component>> resMap = new HashMap<>();

		Either<List<Resource>, StorageOperationStatus> resResources = resourceOperation.getResourceCatalogData(false);
		if (resResources.isLeft()) {
			Either<List<Service>, StorageOperationStatus> resServices = serviceOperation.getServiceCatalogData(false);
			if (resServices.isLeft()) {
				Either<List<Product>, StorageOperationStatus> resProducts = productOperation.getProductCatalogData(false);
				if (resProducts.isLeft()) {
					resMap.put("resources", resResources.left().value());
					resMap.put("services", resServices.left().value());
					resMap.put("products", resProducts.left().value());
					return Either.left(resMap);
				} else {
					return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resProducts.right().value())));
				}
			} else {
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resServices.right().value())));
			}
		} else {
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resResources.right().value())));
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
			return getCatalogComponentsByAssetType(assetTypeEnum);
		}

		ComponentOperation componentOperation = getComponentOperation(assetTypeEnum);
		Either<List<Component>, StorageOperationStatus> result = componentOperation.getFilteredComponents(filters, false);

		if (result.isRight()) {// category hierarchy mismatch or
								// category/subCategory/distributionStatus not
								// found
			List<String> params = getErrorResponseParams(filters, assetTypeEnum);
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(result.right().value()), params.get(0), params.get(1), params.get(2)));
		}
		if (result.left().value().isEmpty()) {// no assets found for requested
												// criteria
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.NO_ASSETS_FOUND, assetType, query));
		}
		return Either.left(result.left().value());
	}

	public Either<List<? extends Component>, ResponseFormat> getCatalogComponentsByAssetType(ComponentTypeEnum assetTypeEnum) {

		if (assetTypeEnum == null) {
			log.debug("getCatalogComponentsByAssetType: Corresponding ComponentTypeEnum not found");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		switch (assetTypeEnum) {
		case RESOURCE:

			Either<List<Resource>, StorageOperationStatus> resourceCatalogData = resourceOperation.getResourceCatalogDataVFLatestCertifiedAndNonCertified(false);
			if (resourceCatalogData.isLeft()) {
				log.debug("getCatalogComponentsByAssetType: Resource fetching successful");
				return Either.left(resourceCatalogData.left().value());
			} else {
				log.debug("getCatalogComponentsByAssetType: Resource fetching failed");
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(resourceCatalogData.right().value())));
			}

		case SERVICE:
			Either<List<Service>, StorageOperationStatus> serviceCatalogData = serviceOperation.getServiceCatalogDataLatestCertifiedAndNotCertified(false);
			if (serviceCatalogData.isLeft()) {
				log.debug("getCatalogComponentsByAssetType: Service fetching successful");
				return Either.left(serviceCatalogData.left().value());
			} else {
				log.debug("getCatalogComponentsByAssetType: Service fetching failed");
				return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceCatalogData.right().value())));
			}
			/*
			 * case PRODUCT: Either<List<Product>, StorageOperationStatus> resCatalogData = productOperation.getProductCatalogData(false); if(resCatalogData.isLeft()){ log. debug("getCatalogComponentsByAssetType: Product fetching successful" );
			 * return Either.left(resCatalogData.left().value()); }else { log. debug("getCatalogComponentsByAssetType: Product fetching failed" ); return Either.right(componentsUtils .getResponseFormat(componentsUtils.convertFromStorageResponse(
			 * resCatalogData.right().value()))); }
			 */
		default:
			log.debug("Invalid Asset Type");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
	}

	// TODO new story Tal
	public Either<List<? extends Component>, ResponseFormat> getCatalogComponentsByUuidAndAssetType(String assetType, String uuid) {

		if (assetType == null || assetType == null) {
			log.debug("getCatalogComponentsByUuidAndAssetType: One of the function parameteres is null");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		ComponentTypeEnum assetTypeEnum = AssetTypeEnum.convertToComponentTypeEnum(assetType);

		if (assetTypeEnum == null) {
			log.debug("getCatalogComponentsByUuidAndAssetType: Corresponding ComponentTypeEnum not found");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}

		switch (assetTypeEnum) {

		case RESOURCE:
			Either<List<Resource>, StorageOperationStatus> resourceListByUuid = resourceOperation.getLatestResourceByUuid(uuid, false);

			if (resourceListByUuid.isLeft()) {
				log.debug("getCatalogComponentsByUuidAndAssetType: Resource fetching successful");
				return Either.left(resourceListByUuid.left().value());
			}

			ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(resourceListByUuid.right().value());
			log.debug("getCatalogComponentsByUuidAndAssetType: Resource fetching failed");
			return Either.right(componentsUtils.getResponseFormat(actionStatus));

		case SERVICE:
			Either<List<Service>, StorageOperationStatus> serviceCatalogData = serviceOperation.getLatestServiceByUuid(uuid, false);

			if (serviceCatalogData.isLeft()) {
				log.debug("getCatalogComponentsByUuidAndAssetType: Service fetching successful");
				return Either.left(serviceCatalogData.left().value());
			}

			log.debug("getCatalogComponentsByUuidAndAssetType: Service fetching failed");
			return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(serviceCatalogData.right().value())));
		// case Product is for future US
		/*
		 * case PRODUCT: Either<List<Product>, StorageOperationStatus> resCatalogData = productOperation.getProductCatalogData(false); if(resCatalogData.isLeft()){ log. debug("getCatalogComponentsByAssetType: Product fetching successful" ); return
		 * Either.left(resCatalogData.left().value()); }else { log. debug("getCatalogComponentsByAssetType: Product fetching failed" ); return Either.right(componentsUtils .getResponseFormat(componentsUtils.convertFromStorageResponse(
		 * resCatalogData.right().value()))); }
		 */
		default:
			log.debug("Invalid Asset Type");
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
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
		List<String> params = new ArrayList<String>();
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

}
