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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.IAttributeOperation;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.AttributeData;
import org.openecomp.sdc.be.resources.data.AttributeValueData;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

/**
 * Class For Data Model Logic Relevant For Attributes
 * 
 * @author mshitrit
 *
 */
@Component("attribute-operation")
public class AttributeOperation extends AbstractOperation implements IAttributeOperation {
	private static Logger log = LoggerFactory.getLogger(AttributeOperation.class.getName());
	@Autowired
	private IPropertyOperation propertyOperation;

	/**
	 * 
	 * Add attribute to graph.
	 * 
	 * 1. Add attribute node
	 * 
	 * 2. Add edge between the former node to its parent(if exists)
	 * 
	 * 3. Add property node and associate it to the node created at #1. (per property & if exists)
	 * 
	 * @param attributeDefinition
	 * @return
	 */
	private Either<AttributeData, TitanOperationStatus> addAttributeToNodeType(PropertyDefinition attributeDefinition, NodeTypeEnum nodeType, String nodeUniqueId) {
		String attUniqueId = UniqueIdBuilder.buildAttributeUid(nodeUniqueId, attributeDefinition.getName());
		Supplier<AttributeData> dataBuilder = () -> buildAttributeData(attributeDefinition, attUniqueId);
		Supplier<String> defNameGenerator = () -> "Attribute : " + attributeDefinition.getName();

		return addDefinitionToNodeType(attributeDefinition, nodeType, nodeUniqueId, GraphEdgeLabels.ATTRIBUTE, dataBuilder, defNameGenerator);

	}

	private TitanOperationStatus addAttributeToNodeType(TitanVertex metadataVertex, PropertyDefinition attributeDefinition, NodeTypeEnum nodeType, String nodeUniqueId) {
		String attUniqueId = UniqueIdBuilder.buildAttributeUid(nodeUniqueId, attributeDefinition.getName());
		Supplier<AttributeData> dataBuilder = () -> buildAttributeData(attributeDefinition, attUniqueId);
		Supplier<String> defNameGenerator = () -> "Attribute : " + attributeDefinition.getName();

		return addDefinitionToNodeType(metadataVertex, attributeDefinition, nodeType, nodeUniqueId, GraphEdgeLabels.ATTRIBUTE, dataBuilder, defNameGenerator);

	}

	private AttributeData buildAttributeData(PropertyDefinition attributeDefinition, String attUniqueId) {
		attributeDefinition.setUniqueId(attUniqueId);
		return new AttributeData(attributeDefinition);
	}

	@Override
	public Either<AttributeData, StorageOperationStatus> deleteAttribute(String attributeId) {
		Either<AttributeData, TitanOperationStatus> either = deleteAttributeFromGraph(attributeId);
		if (either.isRight()) {
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value());
			return Either.right(storageStatus);
		}
		return Either.left(either.left().value());
	}

	@Override
	public Either<Map<String, PropertyDefinition>, StorageOperationStatus> deleteAllAttributeAssociatedToNode(NodeTypeEnum nodeType, String uniqueId) {
		Wrapper<TitanOperationStatus> errorWrapper;
		List<PropertyDefinition> attributes = new ArrayList<>();
		TitanOperationStatus findAllResourceAttribues = findNodeNonInheretedAttribues(uniqueId, NodeTypeEnum.Resource, attributes);
		errorWrapper = (findAllResourceAttribues != TitanOperationStatus.OK) ? new Wrapper<>(findAllResourceAttribues) : new Wrapper<>();

		if (errorWrapper.isEmpty()) {
			for (PropertyDefinition attDef : attributes) {
				log.debug("Before deleting attribute from graph {}", attDef.getUniqueId());
				Either<AttributeData, TitanOperationStatus> deleteNode = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Attribute), attDef.getUniqueId(), AttributeData.class);
				if (deleteNode.isRight()) {
					errorWrapper.setInnerElement(deleteNode.right().value());
					break;
				}
			}
		}

		if (errorWrapper.isEmpty()) {
			Map<String, PropertyDefinition> attributesMap = attributes.stream().collect(Collectors.toMap(e -> e.getName(), e -> e));
			return Either.left(attributesMap);
		} else {
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(errorWrapper.getInnerElement()));
		}

	}

	private Either<AttributeData, TitanOperationStatus> deleteAttributeFromGraph(String attributeId) {
		log.debug("Before deleting attribute from graph {}", attributeId);
		return titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Attribute), attributeId, AttributeData.class);
	}

	@Override
	public TitanOperationStatus addAttributesToGraph(TitanVertex metadataVertex, Map<String, PropertyDefinition> attributes, String resourceId, Map<String, DataTypeDefinition> dataTypes) {
		TitanOperationStatus titanStatus = TitanOperationStatus.OK;
		for (PropertyDefinition attribute : attributes.values()) {
			TitanOperationStatus eitherAddAttribute = addAttributeToGraphByVertex(metadataVertex, attribute, resourceId, dataTypes);
			if (!eitherAddAttribute.equals(TitanOperationStatus.OK)) {
				titanStatus = eitherAddAttribute;
				break;
			}
		}
		return titanStatus;
	}

	@Override
	public Either<List<ComponentInstanceProperty>, TitanOperationStatus> getAllAttributesOfResourceInstance(ComponentInstance compInstance) {

		Either<List<ComponentInstanceProperty>, TitanOperationStatus> result;

		Either<List<ImmutablePair<AttributeValueData, GraphEdge>>, TitanOperationStatus> attributeImplNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.ResourceInstance), compInstance.getUniqueId(),
				GraphEdgeLabels.ATTRIBUTE_VALUE, NodeTypeEnum.AttributeValue, AttributeValueData.class);

		// Build From Resource
		if (attributeImplNodes.isRight() && attributeImplNodes.right().value() == TitanOperationStatus.NOT_FOUND) {
			result = getAttributesFromResource(compInstance);
		}
		// Build From Instance
		else if (attributeImplNodes.isLeft()) {
			List<ImmutablePair<AttributeValueData, GraphEdge>> attributesFromRI = attributeImplNodes.left().value();
			result = mergeAttributesResults(getAttributesFromResource(compInstance), convertToComponentInstanceAttribute(attributesFromRI));
		}
		// Error
		else {
			TitanOperationStatus status = attributeImplNodes.right().value();
			result = Either.right(status);
		}

		return result;
	}

	private Either<List<ComponentInstanceProperty>, TitanOperationStatus> mergeAttributesResults(Either<List<ComponentInstanceProperty>, TitanOperationStatus> eitherAttributesThatDoesNotExistOnRI,
			Either<List<ComponentInstanceProperty>, TitanOperationStatus> eitherAttributesThatExistOnRI) {

		Either<List<ComponentInstanceProperty>, TitanOperationStatus> result;
		if (eitherAttributesThatExistOnRI.isRight()) {
			result = Either.right(eitherAttributesThatExistOnRI.right().value());
		} else if (eitherAttributesThatDoesNotExistOnRI.isRight()) {
			result = Either.right(eitherAttributesThatDoesNotExistOnRI.right().value());
		} else {
			final List<ComponentInstanceProperty> attributesThatExistOnRI = eitherAttributesThatExistOnRI.left().value();
			final List<ComponentInstanceProperty> attributesThatDoesNotExistOnRI = eitherAttributesThatDoesNotExistOnRI.left().value();
			Set<String> attributesIdThatExistOnRI = attributesThatExistOnRI.stream().map(e -> e.getUniqueId()).collect(Collectors.toSet());
			// Attributes From The Resource Without attributes that also exist
			// on the instance
			Stream<ComponentInstanceProperty> filterAttributesThatDoesNotExistOnRI = attributesThatDoesNotExistOnRI.stream().filter(e -> !attributesIdThatExistOnRI.contains(e.getUniqueId()));
			// Add Fields From Resource Attributes
			fillAttributeInfoFromResource(attributesThatExistOnRI, attributesThatDoesNotExistOnRI);
			// Adding the Attributes on the instance for the full list
			List<ComponentInstanceProperty> mergedList = Stream.concat(filterAttributesThatDoesNotExistOnRI, attributesThatExistOnRI.stream()).collect(Collectors.toList());
			result = Either.left(mergedList);
		}
		return result;
	}

	private void fillAttributeInfoFromResource(List<ComponentInstanceProperty> attributesThatExistOnRI, List<ComponentInstanceProperty> attributesThatDoesNotExistOnRI) {
		attributesThatExistOnRI.stream()
				.forEach(e -> addAttributeInfo(e,
						// Finds the same attribute in the resource
						attributesThatDoesNotExistOnRI.stream().filter(e2 -> e2.getUniqueId().equals(e.getUniqueId())).findAny().get()));

	}

	private void addAttributeInfo(ComponentInstanceProperty attributeFromRI, ComponentInstanceProperty attributeFromResource) {
		attributeFromRI.setName(attributeFromResource.getName());
		attributeFromRI.setDescription(attributeFromResource.getDescription());
		attributeFromRI.setDefaultValue(attributeFromResource.getDefaultValue());
		attributeFromRI.setStatus(attributeFromResource.getStatus());
		attributeFromRI.setSchema(attributeFromResource.getSchema());
		if (StringUtils.isEmpty(attributeFromRI.getValue())) {
			attributeFromRI.setValue(attributeFromResource.getDefaultValue());
		}
	}

	private Either<List<ComponentInstanceProperty>, TitanOperationStatus> getAttributesFromResource(ComponentInstance compInstance) {
		Either<List<ComponentInstanceProperty>, TitanOperationStatus> result;
		List<PropertyDefinition> attributes = new ArrayList<>();
		// Attributes does not exist on Ri - fetch them from resource
		TitanOperationStatus findAllResourceAttribues = findAllResourceAttributesRecursively(compInstance.getComponentUid(), attributes);
		if (findAllResourceAttribues != TitanOperationStatus.OK) {
			result = Either.right(findAllResourceAttribues);
		} else {
			List<ComponentInstanceProperty> buildAttInstanceFromResource = attributes.stream().map(attDef -> new ComponentInstanceProperty(false, attDef, null)).collect(Collectors.toList());

			// Set Value to be default value in case it is empty
			Consumer<ComponentInstanceProperty> valueSetter = att -> {
				if (StringUtils.isEmpty(att.getValue())) {
					att.setValue(att.getDefaultValue());
				}
			};
			buildAttInstanceFromResource.stream().forEach(valueSetter);

			result = Either.left(buildAttInstanceFromResource);
		}
		return result;
	}

	private Either<List<ComponentInstanceProperty>, TitanOperationStatus> convertToComponentInstanceAttribute(List<ImmutablePair<AttributeValueData, GraphEdge>> list) {
		Either<List<ComponentInstanceProperty>, TitanOperationStatus> result = null;
		List<ComponentInstanceProperty> componentInstanceAttribute = new ArrayList<>();
		for (ImmutablePair<AttributeValueData, GraphEdge> attributeValue : list) {
			AttributeValueData attributeValueData = attributeValue.getLeft();
			String attributeValueUid = attributeValueData.getUniqueId();

			Either<ImmutablePair<AttributeData, GraphEdge>, TitanOperationStatus> attributeDefRes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.AttributeValue), attributeValueUid, GraphEdgeLabels.ATTRIBUTE_IMPL,
					NodeTypeEnum.Attribute, AttributeData.class);

			if (attributeDefRes.isRight()) {
				TitanOperationStatus status = attributeDefRes.right().value();
				if (status == TitanOperationStatus.NOT_FOUND) {
					status = TitanOperationStatus.INVALID_ID;
				}
				result = Either.right(status);
				break;
			} else {
				ImmutablePair<AttributeData, GraphEdge> attributeDefPair = attributeDefRes.left().value();
				String attributeUniqueId = attributeDefPair.left.getUniqueId();

				ComponentInstanceProperty resourceInstanceAttribute = new ComponentInstanceProperty();
				// set attribute original unique id
				resourceInstanceAttribute.setUniqueId(attributeUniqueId);
				// set hidden
				resourceInstanceAttribute.setHidden(attributeValueData.isHidden());
				// set value
				resourceInstanceAttribute.setValue(attributeValueData.getValue());
				// set property value unique id
				resourceInstanceAttribute.setValueUniqueUid(attributeValueUid);

				resourceInstanceAttribute.setType(attributeValueData.getType());

				componentInstanceAttribute.add(resourceInstanceAttribute);
			}

		}
		if (result == null) {
			result = Either.left(componentInstanceAttribute);
		}
		return result;
	}

	/**
	 * fetch all attributes under a given resource(includes its parents' resources)
	 * 
	 * @param resourceId
	 * @param attributes
	 * @return
	 */
	@Override
	public TitanOperationStatus findAllResourceAttributesRecursively(String resourceId, List<PropertyDefinition> attributes) {
		final NodeElementFetcher<PropertyDefinition> singleNodeFetcher = (resourceIdParam, attributesParam) -> findNodeNonInheretedAttribues(resourceIdParam, NodeTypeEnum.Resource, attributesParam);
		return findAllResourceElementsDefinitionRecursively(resourceId, attributes, singleNodeFetcher);

	}

	@Override
	public TitanOperationStatus findNodeNonInheretedAttribues(String uniqueId, NodeTypeEnum nodeType, List<PropertyDefinition> attributes) {
		Either<List<ImmutablePair<AttributeData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.ATTRIBUTE, NodeTypeEnum.Attribute,
				AttributeData.class);

		if (childrenNodes.isRight()) {
			TitanOperationStatus status = childrenNodes.right().value();
			if (status == TitanOperationStatus.NOT_FOUND) {
				status = TitanOperationStatus.OK;
			}
			return status;
		}

		List<ImmutablePair<AttributeData, GraphEdge>> values = childrenNodes.left().value();
		if (values != null) {

			for (ImmutablePair<AttributeData, GraphEdge> immutablePair : values) {
				AttributeData attData = immutablePair.getLeft();
				String attributeName = attData.getAttributeDataDefinition().getName();

				log.debug("Attribute {} is associated to node {}", attributeName, uniqueId);
				AttributeData attributeData = immutablePair.getKey();
				PropertyDefinition attributeDefinition = this.convertAttributeDataToAttributeDefinition(attributeData, attributeName, uniqueId);

				attributes.add(attributeDefinition);

				log.trace("findAttributesOfNode - property {} associated to node {}", attributeDefinition, uniqueId);
			}

		}

		return TitanOperationStatus.OK;
	}

	@Override
	public PropertyDefinition convertAttributeDataToAttributeDefinition(AttributeData attributeData, String attributeName, String resourceId) {
		log.debug("The object returned after create attribute is {}", attributeData);
		PropertyDefinition attributeDefResult = new PropertyDefinition(attributeData.getAttributeDataDefinition());
		attributeDefResult.setName(attributeName);
		attributeDefResult.setParentUniqueId(resourceId);
		return attributeDefResult;
	}

	@Override
	public Either<AttributeData, StorageOperationStatus> addAttribute(PropertyDefinition attributeDefinition, String resourceId) {

		Either<AttributeData, StorageOperationStatus> eitherResult;
		Either<Map<String, DataTypeDefinition>, TitanOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
		if (allDataTypes.isRight()) {
			TitanOperationStatus status = allDataTypes.right().value();
			log.debug("Cannot find any data type. Status is {}.", status);
			eitherResult = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status));
		} else {
			Either<AttributeData, TitanOperationStatus> either = addAttributeToGraph(attributeDefinition, resourceId, allDataTypes.left().value());
			if (either.isRight()) {
				StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value());
				eitherResult = Either.right(storageStatus);
			} else {
				eitherResult = Either.left(either.left().value());
			}
		}
		return eitherResult;
	}

	@Override
	public Either<AttributeData, StorageOperationStatus> updateAttribute(String attributeId, PropertyDefinition newAttDef, Map<String, DataTypeDefinition> dataTypes) {

		StorageOperationStatus validateAndUpdateAttribute = propertyOperation.validateAndUpdateProperty(newAttDef, dataTypes);
		if (validateAndUpdateAttribute != StorageOperationStatus.OK) {
			return Either.right(validateAndUpdateAttribute);
		}

		Either<AttributeData, TitanOperationStatus> either = updateAttributeFromGraph(attributeId, newAttDef);
		if (either.isRight()) {
			StorageOperationStatus storageStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(either.right().value());
			return Either.right(storageStatus);
		}
		return Either.left(either.left().value());
	}

	private Either<AttributeData, TitanOperationStatus> updateAttributeFromGraph(String attributeId, PropertyDefinition attributeDefenition) {
		log.debug("Before updating attribute on graph {}", attributeId);

		// get the original property data
		Either<AttributeData, TitanOperationStatus> eitherAttribute = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Attribute), attributeId, AttributeData.class);
		if (eitherAttribute.isRight()) {
			log.debug("Problem while get Attribute with id {}. Reason - {}", attributeId, eitherAttribute.right().value().name());
			return Either.right(eitherAttribute.right().value());
		}
		AttributeData orgAttributeData = eitherAttribute.left().value();
		PropertyDataDefinition orgAttributeDataDefinition = orgAttributeData.getAttributeDataDefinition();

		// create new property data to update
		AttributeData newAttributeData = new AttributeData();
		newAttributeData.setAttributeDataDefinition(attributeDefenition);
		PropertyDataDefinition newAttributeDataDefinition = newAttributeData.getAttributeDataDefinition();

		// update the original property data with new values
		if (!Objects.equals(orgAttributeDataDefinition.getDefaultValue(), newAttributeDataDefinition.getDefaultValue())) {
			orgAttributeDataDefinition.setDefaultValue(newAttributeDataDefinition.getDefaultValue());
		}

		if (!Objects.equals(orgAttributeDataDefinition.getDescription(), newAttributeDataDefinition.getDescription())) {
			orgAttributeDataDefinition.setDescription(newAttributeDataDefinition.getDescription());
		}

		if (!Objects.equals(orgAttributeDataDefinition.getType(), newAttributeDataDefinition.getType())) {
			orgAttributeDataDefinition.setType(newAttributeDataDefinition.getType());
		}

		orgAttributeDataDefinition.setSchema(newAttributeDataDefinition.getSchema());

		return titanGenericDao.updateNode(orgAttributeData, AttributeData.class);
	}

	@Override
	public ComponentInstanceProperty buildResourceInstanceAttribute(AttributeValueData attributeValueData, ComponentInstanceProperty resourceInstanceAttribute) {

		Boolean hidden = attributeValueData.isHidden();
		String uid = attributeValueData.getUniqueId();
		return new ComponentInstanceProperty(hidden, resourceInstanceAttribute, uid);
	}

	@Override
	public Either<AttributeData, TitanOperationStatus> addAttributeToGraph(PropertyDefinition attribute, String resourceId, Map<String, DataTypeDefinition> dataTypes) {
		Either<AttributeData, TitanOperationStatus> eitherResult;
		StorageOperationStatus validateAndUpdateAttribute = propertyOperation.validateAndUpdateProperty(attribute, dataTypes);
		if (validateAndUpdateAttribute != StorageOperationStatus.OK) {
			log.error("Attribute {} is invalid. Status is {}", attribute, validateAndUpdateAttribute);
			eitherResult = Either.right(TitanOperationStatus.ILLEGAL_ARGUMENT);
		} else {
			eitherResult = addAttributeToNodeType(attribute, NodeTypeEnum.Resource, resourceId);

		}
		return eitherResult;
	}

	@Override
	public TitanOperationStatus addAttributeToGraphByVertex(TitanVertex metadataVertex, PropertyDefinition attribute, String resourceId, Map<String, DataTypeDefinition> dataTypes) {
		StorageOperationStatus validateAndUpdateAttribute = propertyOperation.validateAndUpdateProperty(attribute, dataTypes);
		TitanOperationStatus result;
		if (validateAndUpdateAttribute != StorageOperationStatus.OK) {
			log.error("Attribute {} is invalid. Status is {}", attribute, validateAndUpdateAttribute);
			result = TitanOperationStatus.ILLEGAL_ARGUMENT;
		} else {
			result = addAttributeToNodeType(metadataVertex, attribute, NodeTypeEnum.Resource, resourceId);

		}
		return result;
	}
}
