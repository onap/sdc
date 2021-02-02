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

import fj.data.Either;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.PropertyValueConverter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class holds the business logic relevant for attributes manipulation.
 *
 * @author mshitrit
 */
@org.springframework.stereotype.Component("attributeBusinessLogic")
public class AttributeBusinessLogic extends BaseBusinessLogic {

    private static final String CREATE_ATTRIBUTE = "CreateAttribute";
    private static final String UPDATE_ATTRIBUTE = "UpdateAttribute";
    private static final String DELETE_ATTRIBUTE = "DeleteAttribute";

    private static final Logger log = Logger.getLogger(AttributeBusinessLogic.class);
    private static final String FAILED_TO_LOCK_COMPONENT_ERROR = "Failed to lock component {}. Error - {}";

    @Autowired
    public AttributeBusinessLogic(IElementOperation elementDao,
                                  IGroupOperation groupOperation,
                                  IGroupInstanceOperation groupInstanceOperation,
                                  IGroupTypeOperation groupTypeOperation,
                                  InterfaceOperation interfaceOperation,
                                  InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                  ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
    }

    /**
     * Created attribute on the resource with resourceId
     *
     * @param resourceId
     * @param newAttributeDef
     * @param userId
     * @return AttributeDefinition if created successfully Or ResponseFormat
     */
    public Either<AttributeDefinition, ResponseFormat> createAttribute(String resourceId,
                                                                       AttributeDefinition newAttributeDef,
                                                                       String userId) {
        Either<AttributeDefinition, ResponseFormat> result = null;
        validateUserExists(userId);

        StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
        if (lockResult != StorageOperationStatus.OK) {
            BeEcompErrorManager.getInstance()
                .logBeFailedLockObjectError(CREATE_ATTRIBUTE, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
            log.info(FAILED_TO_LOCK_COMPONENT_ERROR, resourceId, lockResult);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

        try {
            // Get the resource from DB
            Either<Resource, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(resourceId);
            if (status.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
            }
            Resource resource = status.left().value();

            // verify that resource is checked-out and the user is the last updater
            if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            }

            // verify attribute does not exist in resource
            if (isAttributeExist(resource.getAttributes(), resourceId, newAttributeDef.getName())) {
                return Either.right(
                    componentsUtils.getResponseFormat(ActionStatus.ATTRIBUTE_ALREADY_EXIST, newAttributeDef.getName()));
            }
            Map<String, DataTypeDefinition> eitherAllDataTypes = getAllDataTypes(applicationDataTypeCache);
            // validate property default values
            Either<Boolean, ResponseFormat> defaultValuesValidation = validateAttributeDefaultValue(newAttributeDef,
                eitherAllDataTypes);
            if (defaultValuesValidation.isRight()) {
                return Either.right(defaultValuesValidation.right().value());
            }

            handleAttributeDefaultValue(newAttributeDef, eitherAllDataTypes);

            // add the new attribute to resource on graph
            // need to get StorageOperationStatus and convert to ActionStatus from
            // componentsUtils
            Either<AttributeDefinition, StorageOperationStatus> either = toscaOperationFacade
                .addAttributeOfResource(resource, newAttributeDef);
            if (either.isRight()) {
                result = Either.right(componentsUtils
                    .getResponseFormat(componentsUtils.convertFromStorageResponse(either.right().value()),
                        resource.getName()));
                return result;
            }
            result = Either.left(either.left().value());

            return result;
        } finally {
            commitOrRollback(result);
            graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
        }
    }

    private Either<Boolean, ResponseFormat> validateAttributeDefaultValue(final AttributeDefinition attributeDefinition,
                                                                          final Map<String, DataTypeDefinition> dataTypes) {

        if (!attributeOperation.isAttributeTypeValid(attributeDefinition)) {
            log.info("Invalid type for attribute '{}' type '{}'", attributeDefinition.getName(),
                attributeDefinition.getType());
            final ResponseFormat responseFormat = componentsUtils
                .getResponseFormat(ActionStatus.INVALID_PROPERTY_TYPE, attributeDefinition
                    .getType(), attributeDefinition.getName());
            return Either.right(responseFormat);
        }
        String type = attributeDefinition.getType();
        String innerType = null;
        if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
            final ImmutablePair<String, Boolean> propertyInnerTypeValid = attributeOperation.isAttributeInnerTypeValid(
                attributeDefinition, dataTypes);
            innerType = propertyInnerTypeValid.getLeft();
            if (!Boolean.TRUE.equals(propertyInnerTypeValid.getRight())) {
                log.info("Invalid inner type for attribute '{}' type '{}', dataTypeCount '{}'",
                    attributeDefinition.getName(), attributeDefinition.getType(), dataTypes.size());
                final ResponseFormat responseFormat = componentsUtils
                    .getResponseFormat(ActionStatus.INVALID_PROPERTY_INNER_TYPE, innerType,
                        attributeDefinition.getName());
                return Either.right(responseFormat);
            }
        }
        if (!attributeOperation.isAttributeDefaultValueValid(attributeDefinition, dataTypes)) {
            log.info("Invalid default value for attribute '{}' type '{}'", attributeDefinition.getName(),
                attributeDefinition.getType());
            ResponseFormat responseFormat;
            if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE,
                    attributeDefinition.getName(), type, innerType,
                    (String) attributeDefinition.get_default());
            } else {
                responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEFAULT_VALUE,
                    attributeDefinition.getName(), type, (String) attributeDefinition.get_default());
            }
            return Either.right(responseFormat);

        }
        return Either.left(true);
    }

    private void handleAttributeDefaultValue(final AttributeDefinition newAttributeDefinition,
                                             final Map<String, DataTypeDefinition> dataTypes) {
        final ToscaPropertyType type = ToscaPropertyType.isValidType(newAttributeDefinition.getType());
        final PropertyValueConverter converter = type.getConverter();
        // get inner type
        String innerType = null;
        final SchemaDefinition schema = newAttributeDefinition.getSchema();
        if (schema != null) {
            final PropertyDataDefinition prop = schema.getProperty();
            if (schema.getProperty() != null) {
                innerType = prop.getType();
            }
        }
        if (newAttributeDefinition.get_default() != null) {
            newAttributeDefinition.set_default(converter
                .convert((String) newAttributeDefinition.get_default(), innerType, dataTypes));
        }
    }

    private boolean isAttributeExist(List<AttributeDefinition> attributes, String resourceUid, String propertyName) {
        boolean isExist = false;
        if (attributes != null) {
            isExist = attributes.stream().anyMatch(
                p -> Objects.equals(p.getName(), propertyName) && Objects.equals(p.getOwnerId(), resourceUid));
        }
        return isExist;

    }

    /**
     * @param resourceId
     * @param attributeId
     * @param userId
     * @return
     */
    public Either<AttributeDefinition, ResponseFormat> getAttribute(String resourceId, String attributeId,
                                                                    String userId) {

        validateUserExists(userId);

        // Get the resource from DB
        Either<Resource, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(resourceId);
        if (status.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
        }
        Resource resource = status.left().value();

        List<AttributeDefinition> attributes = resource.getAttributes();
        if (attributes == null) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ATTRIBUTE_NOT_FOUND, ""));
        } else {
            // verify attribute exist in resource
            Optional<AttributeDefinition> optionalAtt = attributes.stream().filter(att ->
                att.getUniqueId().equals(attributeId)).findAny();
            return optionalAtt.<Either<AttributeDefinition, ResponseFormat>>map(Either::left).orElseGet(() ->
                Either.right(componentsUtils.getResponseFormat(ActionStatus.ATTRIBUTE_NOT_FOUND, "")));
        }
    }

    /**
     * Updates Attribute on resource
     *
     * @param resourceId
     * @param attributeId
     * @param newAttDef
     * @param userId
     * @return
     */
    public Either<AttributeDefinition, ResponseFormat> updateAttribute(String resourceId, String attributeId,
                                                                       AttributeDefinition newAttDef, String userId) {
        Either<AttributeDefinition, ResponseFormat> result = null;

        StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
        if (lockResult != StorageOperationStatus.OK) {
            BeEcompErrorManager.getInstance()
                .logBeFailedLockObjectError(UPDATE_ATTRIBUTE, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
            log.info(FAILED_TO_LOCK_COMPONENT_ERROR, resourceId, lockResult);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        try {
            // Get the resource from DB
            Either<Resource, StorageOperationStatus> eitherResource = toscaOperationFacade.getToscaElement(resourceId);
            if (eitherResource.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
            }
            Resource resource = eitherResource.left().value();

            // verify that resource is checked-out and the user is the last updater
            if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            }

            // verify attribute exist in resource
            Either<AttributeDefinition, ResponseFormat> eitherAttribute = getAttribute(resourceId, attributeId, userId);
            if (eitherAttribute.isRight()) {
                return Either.right(eitherAttribute.right().value());
            }
            Map<String, DataTypeDefinition> eitherAllDataTypes = getAllDataTypes(applicationDataTypeCache);

            // validate attribute default values
            Either<Boolean, ResponseFormat> defaultValuesValidation = validateAttributeDefaultValue(newAttDef,
                eitherAllDataTypes);
            if (defaultValuesValidation.isRight()) {
                return Either.right(defaultValuesValidation.right().value());
            }

            // add the new property to resource on graph
            StorageOperationStatus validateAndUpdateAttribute = attributeOperation
                .validateAndUpdateAttribute(newAttDef, eitherAllDataTypes);
            if (validateAndUpdateAttribute != StorageOperationStatus.OK) {
                log.debug("Problem while updating attribute with id {}. Reason - {}", attributeId,
                    validateAndUpdateAttribute);
                result = Either.right(componentsUtils
                    .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(validateAndUpdateAttribute),
                        resource.getName()));
            }

            Either<AttributeDefinition, StorageOperationStatus> eitherAttUpdate = toscaOperationFacade
                .updateAttributeOfResource(resource, newAttDef);

            if (eitherAttUpdate.isRight()) {
                log.debug("Problem while updating attribute with id {}. Reason - {}", attributeId,
                    eitherAttUpdate.right().value());
                result = Either.right(componentsUtils.getResponseFormatByResource(
                    componentsUtils.convertFromStorageResponse(eitherAttUpdate.right().value()), resource.getName()));
                return result;
            }

            result = Either.left(eitherAttUpdate.left().value());
            return result;
        } finally {
            commitOrRollback(result);
            graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
        }

    }

    /**
     * Deletes Attribute on resource
     *
     * @param resourceId
     * @param attributeId
     * @param userId
     * @return
     */
    public Either<AttributeDefinition, ResponseFormat> deleteAttribute(String resourceId, String attributeId,
                                                                       String userId) {

        Either<AttributeDefinition, ResponseFormat> result = null;

        validateUserExists(userId);

        StorageOperationStatus lockResult = graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource);
        if (lockResult != StorageOperationStatus.OK) {
            BeEcompErrorManager.getInstance()
                .logBeFailedLockObjectError(DELETE_ATTRIBUTE, NodeTypeEnum.Resource.name().toLowerCase(), resourceId);
            log.info(FAILED_TO_LOCK_COMPONENT_ERROR, resourceId, lockResult);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

        try {
            // Get the resource from DB
            Either<Resource, StorageOperationStatus> eitherResource = toscaOperationFacade.getToscaElement(resourceId);
            if (eitherResource.isRight()) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
            }
            Resource resource = eitherResource.left().value();

            // verify that resource is checked-out and the user is the last updater
            if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
            }

            // verify attribute exist in resource
            Either<AttributeDefinition, ResponseFormat> eitherAttributeExist = getAttribute(resourceId, attributeId,
                userId);
            if (eitherAttributeExist.isRight()) {
                return Either.right(eitherAttributeExist.right().value());
            }
            String attributeName = eitherAttributeExist.left().value().getName();

            // delete attribute of resource from graph
            StorageOperationStatus eitherAttributeDelete = toscaOperationFacade
                .deleteAttributeOfResource(resource, attributeName);
            if (eitherAttributeDelete != StorageOperationStatus.OK) {
                result = Either.right(componentsUtils
                    .getResponseFormat(componentsUtils.convertFromStorageResponse(eitherAttributeDelete),
                        resource.getName()));
                return result;
            }

            result = Either.left(eitherAttributeExist.left().value());
            return result;
        } finally {
            commitOrRollback(result);
            graphLockOperation.unlockComponent(resourceId, NodeTypeEnum.Resource);
        }
    }

    public Either<List<AttributeDefinition>, ResponseFormat> getAttributesList(final String componentId,
                                                                               final String userId) {
        validateUserExists(userId);

        // Get the resource from DB
        final ComponentParametersView filter = new ComponentParametersView(true);
        filter.setIgnoreAttributes(false);
        final Either<Component, StorageOperationStatus> status = toscaOperationFacade.getToscaElement(componentId, filter);
        if (status.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESOURCE_NOT_FOUND, ""));
        }

        return Either.left(status.left().value().getAttributes());
    }


}
