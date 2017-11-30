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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ArtifactData;
import org.openecomp.sdc.be.resources.data.InterfaceData;
import org.openecomp.sdc.be.resources.data.OperationData;
import org.openecomp.sdc.be.resources.data.ResourceMetadataData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@Component("interface-operation")
public class InterfaceLifecycleOperation implements IInterfaceLifecycleOperation {

	private static Logger log = LoggerFactory.getLogger(InterfaceLifecycleOperation.class.getName());

	public InterfaceLifecycleOperation() {
		super();
	}

	@javax.annotation.Resource
	private ArtifactOperation artifactOperation;

	@javax.annotation.Resource
	private TitanGenericDao titanGenericDao;

	@Override
	public Either<InterfaceDefinition, StorageOperationStatus> addInterfaceToResource(InterfaceDefinition interf, String resourceId, String interfaceName, boolean inTransaction) {

		return createInterfaceOnResource(interf, resourceId, interfaceName, true, inTransaction);

	}

	private Either<OperationData, TitanOperationStatus> addOperationToGraph(InterfaceDefinition interf, String opName, Operation op, InterfaceData interfaceData) {

		op.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId((String) interfaceData.getUniqueId(), opName));
		OperationData operationData = new OperationData(op);

		log.debug("Before adding operation to graph {}", operationData);
		Either<OperationData, TitanOperationStatus> createOpNodeResult = titanGenericDao.createNode(operationData, OperationData.class);
		log.debug("After adding operation to graph {}", operationData);

		if (createOpNodeResult.isRight()) {
			TitanOperationStatus opStatus = createOpNodeResult.right().value();
			log.error("Failed to add operation {} to graph. status is {}", opName, opStatus);
			return Either.right(opStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), opName);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(interfaceData, operationData, GraphEdgeLabels.INTERFACE_OPERATION, props);

		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createOpNodeResult.right().value();
			log.error("Failed to associate operation {} to property {} in graph. status is {}", interfaceData.getUniqueId(), opName, operationStatus);

			return Either.right(operationStatus);
		}

		return Either.left(createOpNodeResult.left().value());

	}

	private InterfaceDefinition convertInterfaceDataToInterfaceDefinition(InterfaceData interfaceData) {

		log.debug("The object returned after create interface is {}", interfaceData);

		InterfaceDefinition interfaceDefResult = new InterfaceDefinition(interfaceData.getInterfaceDataDefinition());

		return interfaceDefResult;

	}

	private Operation convertOperationDataToOperation(OperationData operationData) {

		log.debug("The object returned after create operation is {}", operationData);

		Operation operationDefResult = new Operation(operationData.getOperationDataDefinition());

		return operationDefResult;

	}

	private Either<InterfaceData, TitanOperationStatus> addInterfaceToGraph(InterfaceDefinition interfaceInfo, String interfaceName, String resourceId) {

		InterfaceData interfaceData = new InterfaceData(interfaceInfo);

		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(resourceId);

		String interfaceNameSplitted = getShortInterfaceName(interfaceInfo);

		interfaceInfo.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(resourceId, interfaceNameSplitted));

		Either<InterfaceData, TitanOperationStatus> existInterface = titanGenericDao.getNode(interfaceData.getUniqueIdKey(), interfaceData.getUniqueId(), InterfaceData.class);

		if (existInterface.isRight()) {

			return createInterfaceNodeAndRelation(interfaceNameSplitted, resourceId, interfaceData, resourceData);
		} else {
			log.debug("Interface {} already exist", interfaceData.getUniqueId());
			return Either.right(TitanOperationStatus.ALREADY_EXIST);
		}
	}

	private Either<InterfaceData, TitanOperationStatus> createInterfaceNodeAndRelation(String interfaceName, String resourceId, InterfaceData interfaceData, ResourceMetadataData resourceData) {
		log.debug("Before adding interface to graph {}", interfaceData);
		Either<InterfaceData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(interfaceData, InterfaceData.class);
		log.debug("After adding property to graph {}", interfaceData);

		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add interface {} to graph. status is {}", interfaceName, operationStatus);
			return Either.right(operationStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), interfaceName);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(resourceData, interfaceData, GraphEdgeLabels.INTERFACE, props);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to associate resource {} to property {} in graph. status is {}", resourceId, interfaceName, operationStatus);

			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());
	}

	private Either<TitanVertex, TitanOperationStatus> createInterfaceNodeAndRelation(String interfaceName, String resourceId, InterfaceData interfaceData, TitanVertex metadataVertex) {
		log.debug("Before adding interface to graph {}", interfaceData);
		Either<TitanVertex, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(interfaceData);

		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add interface {} to graph. status is {}", interfaceName, operationStatus);
			return Either.right(operationStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), interfaceName);
		TitanVertex interfaceVertex = createNodeResult.left().value();
		TitanOperationStatus createRelResult = titanGenericDao.createEdge(metadataVertex, interfaceVertex, GraphEdgeLabels.INTERFACE, props);
		if (!createRelResult.equals(TitanOperationStatus.OK)) {
			log.error("Failed to associate resource {} to property {} in graph. status is {}", resourceId, interfaceName, createRelResult);
		}
		return Either.left(interfaceVertex);
	}

	private Either<OperationData, TitanOperationStatus> createOperationNodeAndRelation(String operationName, OperationData operationData, InterfaceData interfaceData) {
		log.debug("Before adding operation to graph {}", operationData);
		Either<OperationData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(operationData, OperationData.class);
		log.debug("After adding operation to graph {}", interfaceData);

		if (createNodeResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to add interfoperationce {} to graph. status is {}", operationName, operationStatus);
			return Either.right(operationStatus);
		}

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(GraphPropertiesDictionary.NAME.getProperty(), operationName);
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(interfaceData, operationData, GraphEdgeLabels.INTERFACE_OPERATION, props);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createNodeResult.right().value();
			log.error("Failed to associate operation {} to interface {} in graph. status is {}", operationName, interfaceData.getUniqueId(), operationStatus);

			return Either.right(operationStatus);
		}

		return Either.left(createNodeResult.left().value());
	}

	// @Override
	// public Either<InterfaceDefinition, StorageOperationStatus> getInterface(
	// String interfaceId) {
	//
	// /*
	// * Either<InterfaceData, TitanOperationStatus> getResult =
	// * this.titanGenericDao
	// * .getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Interface),
	// * interfaceId, InterfaceData.class); if (getResult.isLeft()) {
	// * InterfaceData propertyData = getResult.left().value(); return
	// * Either.left(convertPropertyDataToPropertyDefinition(propertyData)); }
	// * else { TitanOperationStatus titanStatus = getResult.right().value();
	// * log.debug("Node with id " + propertyId +
	// * " was not found in the graph. status: " + titanStatus);
	// * StorageOperationStatus storageOperationStatus =
	// * DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus);
	// * return Either.right(storageOperationStatus); }
	// */
	// return null;
	// }

	// @Override
	// public Either<InterfaceDefinition, StorageOperationStatus> getInterface(
	// String interfaceId, boolean inTransaction) {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public Either<Map<String, InterfaceDefinition>, StorageOperationStatus> getAllInterfacesOfResource(String resourceIdn, boolean recursively) {
		return getAllInterfacesOfResource(resourceIdn, recursively, false);
	}

	@Override
	public Either<Map<String, InterfaceDefinition>, StorageOperationStatus> getAllInterfacesOfResource(String resourceId, boolean recursively, boolean inTransaction) {

		Either<Map<String, InterfaceDefinition>, StorageOperationStatus> result = null;
		Map<String, InterfaceDefinition> interfaces = new HashMap<String, InterfaceDefinition>();
		try {
			if ((resourceId == null) || resourceId.isEmpty()) {
				log.error("resourceId is empty");
				result = Either.right(StorageOperationStatus.INVALID_ID);
				return result;
			}

			TitanOperationStatus findInterfacesRes = TitanOperationStatus.GENERAL_ERROR;
			if (recursively) {
				findInterfacesRes = findAllInterfacesRecursively(resourceId, interfaces);
			} else {
				findInterfacesRes = findAllInterfacesNotRecursively(resourceId, interfaces);
			}
			if (!findInterfacesRes.equals(TitanOperationStatus.OK)) {
				log.error("Failed to get all interfaces of resource {}. status is {}", resourceId, findInterfacesRes);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(findInterfacesRes));
				return result;
			}
			result = Either.left(interfaces);
			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}
	}

	private TitanOperationStatus findAllInterfacesNotRecursively(String resourceId, Map<String, InterfaceDefinition> interfaces) {

		Either<List<ImmutablePair<InterfaceData, GraphEdge>>, TitanOperationStatus> interfaceNodes = titanGenericDao.getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.INTERFACE,
				NodeTypeEnum.Interface, InterfaceData.class);

		if (interfaceNodes.isRight()) {
			TitanOperationStatus status = interfaceNodes.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				return status;
			}
		} else {
			List<ImmutablePair<InterfaceData, GraphEdge>> interfaceList = interfaceNodes.left().value();
			if (interfaceList != null) {
				for (ImmutablePair<InterfaceData, GraphEdge> interfacePair : interfaceList) {
					String interfaceUniqueId = (String) interfacePair.getKey().getUniqueId();
					Either<String, TitanOperationStatus> interfaceNameRes = getPropertyValueFromEdge(interfacePair.getValue(), GraphPropertiesDictionary.NAME);
					if (interfaceNameRes.isRight()) {
						log.error("The requirement name is missing on the edge of requirement {}", interfaceUniqueId);
						return interfaceNameRes.right().value();
					}
					String interfaceName = interfaceNameRes.left().value();
					Either<InterfaceDefinition, TitanOperationStatus> interfaceDefRes = getNonRecursiveInterface(interfacePair.getKey());
					if (interfaceDefRes.isRight()) {
						TitanOperationStatus status = interfaceDefRes.right().value();
						log.error("Failed to get interface actions of interface {}", interfaceUniqueId);
						return status;
					}

					InterfaceDefinition interfaceDefinition = interfaceDefRes.left().value();
					if (true == interfaces.containsKey(interfaceName)) {
						log.debug("The interface {} was already defined in dervied resource. add not overriden operations", interfaceName);
						InterfaceDefinition existInterface = interfaces.get(interfaceName);
						addMissingOperationsToInterface(interfaceDefinition, existInterface);
					} else {
						interfaces.put(interfaceName, interfaceDefinition);
					}

				}
			}
		}
		return TitanOperationStatus.OK;
	}

	public TitanOperationStatus findAllInterfacesRecursively(String resourceId, Map<String, InterfaceDefinition> interfaces) {

		TitanOperationStatus findAllInterfacesNotRecursively = findAllInterfacesNotRecursively(resourceId, interfaces);
		if (!findAllInterfacesNotRecursively.equals(TitanOperationStatus.OK)) {
			log.error("failed to get interfaces for resource {}. status is {}", resourceId, findAllInterfacesNotRecursively);
		}

		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentNodes = titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Resource), resourceId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
				ResourceMetadataData.class);

		if (parentNodes.isRight()) {
			TitanOperationStatus parentNodesStatus = parentNodes.right().value();
			if (parentNodesStatus == TitanOperationStatus.NOT_FOUND) {
				log.debug("Finish to lookup for parnet interfaces");
				return TitanOperationStatus.OK;
			} else {
				log.error("Failed to find parent interfaces of resource {}. status is {}", resourceId, parentNodesStatus);
				return parentNodesStatus;
			}
		}
		ImmutablePair<ResourceMetadataData, GraphEdge> parnetNodePair = parentNodes.left().value();
		String parentUniqueId = parnetNodePair.getKey().getMetadataDataDefinition().getUniqueId();
		TitanOperationStatus addParentIntStatus = findAllInterfacesRecursively(parentUniqueId, interfaces);

		if (addParentIntStatus != TitanOperationStatus.OK) {
			log.error("Failed to fetch all interfaces of resource {}", parentUniqueId);
			return addParentIntStatus;
		}

		return TitanOperationStatus.OK;
	}

	private Either<String, TitanOperationStatus> getPropertyValueFromEdge(GraphEdge edge, GraphPropertiesDictionary property) {
		Map<String, Object> edgeProps = edge.getProperties();
		String interfaceName = null;
		if (edgeProps != null) {
			interfaceName = (String) edgeProps.get(property.getProperty());
			if (interfaceName == null) {
				return Either.right(TitanOperationStatus.INVALID_ELEMENT);
			}
		} else {
			return Either.right(TitanOperationStatus.INVALID_ELEMENT);
		}
		return Either.left(interfaceName);
	}

	private Either<InterfaceDefinition, TitanOperationStatus> getNonRecursiveInterface(InterfaceData interfaceData) {

		log.debug("Going to fetch the operations associate to interface {}", interfaceData.getUniqueId());
		InterfaceDefinition interfaceDefinition = new InterfaceDefinition(interfaceData.getInterfaceDataDefinition());

		String interfaceId = interfaceData.getUniqueId();
		Either<List<ImmutablePair<OperationData, GraphEdge>>, TitanOperationStatus> operationsRes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), interfaceId, GraphEdgeLabels.INTERFACE_OPERATION,
				NodeTypeEnum.InterfaceOperation, OperationData.class);

		if (operationsRes.isRight()) {
			TitanOperationStatus status = operationsRes.right().value();
			if (status != TitanOperationStatus.NOT_FOUND) {
				return Either.right(status);
			} else {
				return Either.left(interfaceDefinition);
			}
		}

		List<ImmutablePair<OperationData, GraphEdge>> operationList = operationsRes.left().value();
		if (operationList != null && !operationList.isEmpty()) {
			for (ImmutablePair<OperationData, GraphEdge> operationPair : operationList) {
				Operation operation = new Operation(operationPair.getKey().getOperationDataDefinition());
				Either<String, TitanOperationStatus> operationNameRes = getPropertyValueFromEdge(operationPair.getValue(), GraphPropertiesDictionary.NAME);
				if (operationNameRes.isRight()) {
					log.error("The operation name is missing on the edge of operation {}", operationPair.getKey().getUniqueId());
					return Either.right(operationNameRes.right().value());
				}
				String operationName = operationNameRes.left().value();
				findOperationImplementation(operation);
				interfaceDefinition.getOperations().put(operationName, operation);
			}
		}

		return Either.left(interfaceDefinition);
	}

	private StorageOperationStatus findOperationImplementation(Operation operation) {

		String operationId = operation.getUniqueId();
		Either<Map<String, ArtifactDefinition>, StorageOperationStatus> artifactsRes = artifactOperation.getArtifacts(operationId, NodeTypeEnum.InterfaceOperation, true);
		if (artifactsRes.isRight() || artifactsRes.left().value() == null) {
			log.error("failed to get artifact from graph for operation id {}. status is {}", operationId, artifactsRes.right().value());
			return artifactsRes.right().value();
		} else {
			Map<String, ArtifactDefinition> artifacts = artifactsRes.left().value();
			Iterator<String> iter = artifacts.keySet().iterator();

			if (iter.hasNext()) {
				operation.setImplementation(artifacts.get(iter.next()));
			}
		}
		return StorageOperationStatus.OK;
	}

	private StorageOperationStatus addMissingOperationsToInterface(InterfaceDefinition interfaceDefinition, InterfaceDefinition existInterface) {
		Map<String, Operation> existOperations = existInterface.getOperationsMap();
		Map<String, Operation> operations = interfaceDefinition.getOperationsMap();
		if (operations != null && !operations.isEmpty()) {
			Set<Entry<String, Operation>> operationsSet = operations.entrySet();
			for (Entry<String, Operation> operation : operationsSet) {
				if (!existOperations.containsKey(operation.getKey())) {
					existOperations.put(operation.getKey(), operation.getValue());
				}
			}
		}
		return StorageOperationStatus.OK;
	}

	@Override
	public Either<Operation, StorageOperationStatus> updateInterfaceOperation(String resourceId, String interfaceName, String operationName, Operation interf) {

		return updateInterfaceOperation(resourceId, interfaceName, operationName, interf, false);
	}

	@Override
	public Either<Operation, StorageOperationStatus> updateInterfaceOperation(String resourceId, String interfaceName, String operationName, Operation operation, boolean inTransaction) {
		Either<Operation, StorageOperationStatus> status = updateOperationOnGraph(operation, resourceId, interfaceName, operationName);

		/*
		 * if (status.isRight()) { if (false == inTransaction) { titanGenericDao.rollback(); } 
		 * log.error("Failed to update operation {} of interfaceName {} of resource {}", operationName, interfaceName, resourceId); 
		 * return
		 * Either.right(DaoStatusConverter .convertTitanStatusToStorageStatus(status.right().value())); } else { if (false == inTransaction) { titanGenericDao.commit(); } OperationData operationData = status.left().value();
		 * 
		 * Operation operationDefResult = convertOperationDataToOperation(operationData);
		 * 
		 * 
		 * log.debug("The returned OperationDefintion is {}", operationDefResult); return Either.left(operationDefResult); }
		 */
		return status;
	}

	private Either<Operation, StorageOperationStatus> updateOperationOnGraph(Operation operation, String resourceId, String interfaceName, String operationName) {

		Either<List<ImmutablePair<InterfaceData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), resourceId, GraphEdgeLabels.INTERFACE, NodeTypeEnum.Interface,
				InterfaceData.class);

		if (childrenNodes.isRight()) {
			/*
			 * InterfaceDefinition intDef = new InterfaceDefinition(); intDef.setType(interfaceName); Map<String, Operation> opMap = new HashMap<String, Operation>(); opMap.put(operationName, operation); intDef.setOperations(opMap);
			 * Either<InterfaceDefinition, StorageOperationStatus> statusRes = this .createInterfaceOnResource(intDef, resourceId, interfaceName, true); if (statusRes.isRight()) return Either.right(statusRes.right().value()); else {
			 * InterfaceDefinition newDef = statusRes.left().value(); Operation res = newDef.getOperations().get(operationName); return Either.left(res); }
			 */
			return updateOperationFromParentNode(operation, resourceId, interfaceName, operationName);

		} else {
			return updateExistingOperation(resourceId, operation, interfaceName, operationName, childrenNodes);

		}

	}

	private Either<Operation, StorageOperationStatus> updateExistingOperation(String resourceId, Operation operation, String interfaceName, String operationName,
			Either<List<ImmutablePair<InterfaceData, GraphEdge>>, TitanOperationStatus> childrenNodes) {
		Operation newOperation = null;
		StorageOperationStatus storageOperationStatus = StorageOperationStatus.GENERAL_ERROR;

		for (ImmutablePair<InterfaceData, GraphEdge> interfaceDataNode : childrenNodes.left().value()) {

			GraphEdge interfaceEdge = interfaceDataNode.getRight();
			Map<String, Object> interfaceEdgeProp = interfaceEdge.getProperties();
			InterfaceData interfaceData = interfaceDataNode.getKey();

			if (interfaceEdgeProp.get(GraphPropertiesDictionary.NAME.getProperty()).equals(interfaceName)) {
				Either<List<ImmutablePair<OperationData, GraphEdge>>, TitanOperationStatus> operationRes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) interfaceDataNode.getLeft().getUniqueId(),
						GraphEdgeLabels.INTERFACE_OPERATION, NodeTypeEnum.InterfaceOperation, OperationData.class);
				if (operationRes.isRight()) {
					log.error("Failed to find operation  {} on interface {}", operationName, interfaceName);
					return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(operationRes.right().value()));
				} else {
					List<ImmutablePair<OperationData, GraphEdge>> operations = operationRes.left().value();
					for (ImmutablePair<OperationData, GraphEdge> operationPairEdge : operations) {
						GraphEdge opEdge = operationPairEdge.getRight();
						OperationData opData = operationPairEdge.getLeft();
						Map<String, Object> opEdgeProp = opEdge.getProperties();
						if (opEdgeProp.get(GraphPropertiesDictionary.NAME.getProperty()).equals(operationName)) {
							ArtifactDefinition artifact = operation.getImplementationArtifact();
							Either<ImmutablePair<ArtifactData, GraphEdge>, TitanOperationStatus> artifactRes = titanGenericDao.getChild(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) opData.getUniqueId(), GraphEdgeLabels.ARTIFACT_REF,
									NodeTypeEnum.ArtifactRef, ArtifactData.class);
							Either<ArtifactDefinition, StorageOperationStatus> artStatus;
							if (artifactRes.isRight()) {
								artStatus = artifactOperation.addArifactToComponent(artifact, (String) operationPairEdge.getLeft().getUniqueId(), NodeTypeEnum.InterfaceOperation, true, true);
							} else {
								artStatus = artifactOperation.updateArifactOnResource(artifact, (String) operationPairEdge.getLeft().getUniqueId(), (String) artifactRes.left().value().getLeft().getUniqueId(), NodeTypeEnum.InterfaceOperation, true);
							}
							if (artStatus.isRight()) {
								titanGenericDao.rollback();
								log.error("Failed to add artifact {} to interface {}", operationName, interfaceName);
								return Either.right(artStatus.right().value());
							} else {
								newOperation = this.convertOperationDataToOperation(opData);
								newOperation.setImplementation(artStatus.left().value());

							}

						}

					}
					if (newOperation == null) {
						Either<InterfaceData, TitanOperationStatus> parentInterfaceStatus = findInterfaceOnParentNode(resourceId, interfaceName);
						if (parentInterfaceStatus.isRight()) {
							log.debug("Interface {} not exist", interfaceName);
							return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(parentInterfaceStatus.right().value()));
						}

						InterfaceData parentInterfaceData = parentInterfaceStatus.left().value();
						Either<List<ImmutablePair<OperationData, GraphEdge>>, TitanOperationStatus> opRes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) parentInterfaceData.getUniqueId(),
								GraphEdgeLabels.INTERFACE_OPERATION, NodeTypeEnum.InterfaceOperation, OperationData.class);
						if (opRes.isRight()) {
							log.error("Failed to find operation  {} on interface {}", operationName, interfaceName);
							return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(operationRes.right().value()));

						} else {
							List<ImmutablePair<OperationData, GraphEdge>> parentOperations = opRes.left().value();
							for (ImmutablePair<OperationData, GraphEdge> operationPairEdge : parentOperations) {
								GraphEdge opEdge = operationPairEdge.getRight();
								OperationData opData = operationPairEdge.getLeft();
								Map<String, Object> opEdgeProp = opEdge.getProperties();
								if (opEdgeProp.get(GraphPropertiesDictionary.NAME.getProperty()).equals(operationName)) {
									return copyAndCreateNewOperation(operation, interfaceName, operationName, null, interfaceData, operationRes, opData);
								}
							}
						}

					}

				}

			} else {
				// not found
				storageOperationStatus = StorageOperationStatus.ARTIFACT_NOT_FOUND;
			}

		}
		if (newOperation == null)
			return Either.right(storageOperationStatus);
		else
			return Either.left(newOperation);
	}

	private Either<Operation, StorageOperationStatus> copyAndCreateNewOperation(Operation operation, String interfaceName, String operationName, Operation newOperation, InterfaceData interfaceData,
			Either<List<ImmutablePair<OperationData, GraphEdge>>, TitanOperationStatus> operationRes, OperationData opData) {
		OperationDataDefinition opDataInfo = opData.getOperationDataDefinition();
		OperationDataDefinition newOperationInfo = new OperationDataDefinition(opDataInfo);
		newOperationInfo.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(interfaceData.getUniqueId(), operationName.toLowerCase()));
		OperationData newopData = new OperationData(newOperationInfo);
		Either<OperationData, TitanOperationStatus> operationStatus = createOperationNodeAndRelation(operationName, newopData, interfaceData);
		if (operationStatus.isRight()) {
			log.error("Failed to create operation  {} on interface {}", operationName, interfaceName);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(operationRes.right().value()));
		}
		ArtifactDefinition artifact = operation.getImplementationArtifact();
		if (artifact != null) {
			Either<ArtifactDefinition, StorageOperationStatus> artStatus = artifactOperation.addArifactToComponent(artifact, (String) operationStatus.left().value().getUniqueId(), NodeTypeEnum.InterfaceOperation, true, true);
			if (artStatus.isRight()) {
				titanGenericDao.rollback();
				log.error("Failed to add artifact {} to interface {}", operationName, interfaceName);
			} else {
				newOperation = this.convertOperationDataToOperation(opData);
				newOperation.setImplementation(artStatus.left().value());

			}
		}
		return Either.left(newOperation);
	}

	private Either<Operation, StorageOperationStatus> updateOperationFromParentNode(Operation operation, String resourceId, String interfaceName, String operationName) {
		// Operation newOperation = null;
		ResourceMetadataData resourceData = new ResourceMetadataData();
		resourceData.getMetadataDataDefinition().setUniqueId(resourceId);
		Either<InterfaceData, TitanOperationStatus> parentInterfaceStatus = findInterfaceOnParentNode(resourceId, interfaceName);
		if (parentInterfaceStatus.isRight()) {
			log.debug("Interface {} not exist", interfaceName);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(parentInterfaceStatus.right().value()));
		}

		InterfaceData interfaceData = parentInterfaceStatus.left().value();
		InterfaceDataDefinition intDataDefinition = interfaceData.getInterfaceDataDefinition();
		InterfaceDataDefinition newInterfaceInfo = new InterfaceDataDefinition(intDataDefinition);

		String interfaceNameSplitted = getShortInterfaceName(intDataDefinition);

		newInterfaceInfo.setUniqueId(UniqueIdBuilder.buildPropertyUniqueId(resourceId, interfaceNameSplitted));
		InterfaceData updatedInterfaceData = new InterfaceData(newInterfaceInfo);
		Either<InterfaceData, TitanOperationStatus> createStatus = createInterfaceNodeAndRelation(interfaceName, resourceId, updatedInterfaceData, resourceData);
		if (createStatus.isRight()) {
			log.debug("failed to create interface node  {} on resource  {}", interfaceName,  resourceId);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(createStatus.right().value()));
		}

		InterfaceData newInterfaceNode = createStatus.left().value();
		Either<GraphRelation, TitanOperationStatus> createRelResult = titanGenericDao.createRelation(newInterfaceNode, interfaceData, GraphEdgeLabels.DERIVED_FROM, null);
		if (createRelResult.isRight()) {
			TitanOperationStatus operationStatus = createRelResult.right().value();
			log.error("Failed to associate interface {} to interface {} in graph. status is {}", interfaceData.getUniqueId(), newInterfaceNode.getUniqueId(),  operationStatus);

			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(operationStatus));
		}
		Either<List<ImmutablePair<OperationData, GraphEdge>>, TitanOperationStatus> operationRes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) interfaceData.getUniqueId(),
				GraphEdgeLabels.INTERFACE_OPERATION, NodeTypeEnum.InterfaceOperation, OperationData.class);
		if (operationRes.isRight()) {
			log.error("Failed to find operation  {} on interface {}", operationName, interfaceName);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(operationRes.right().value()));

		} else {
			List<ImmutablePair<OperationData, GraphEdge>> operations = operationRes.left().value();
			for (ImmutablePair<OperationData, GraphEdge> operationPairEdge : operations) {
				GraphEdge opEdge = operationPairEdge.getRight();
				OperationData opData = operationPairEdge.getLeft();
				Map<String, Object> opEdgeProp = opEdge.getProperties();
				if (opEdgeProp.get(GraphPropertiesDictionary.NAME.getProperty()).equals(operationName)) {

					return copyAndCreateNewOperation(operation, interfaceName, operationName, null, // changed
																									// from
																									// newOperation
							newInterfaceNode, operationRes, opData);

				}
			}
		}
		// if(newOperation == null)
		return Either.right(StorageOperationStatus.GENERAL_ERROR);
		// else
		// return Either.left(newOperation);
	}

	private Either<InterfaceData, TitanOperationStatus> findInterfaceOnParentNode(String resourceId, String interfaceName) {

		Either<ImmutablePair<ResourceMetadataData, GraphEdge>, TitanOperationStatus> parentRes = titanGenericDao.getChild(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), resourceId, GraphEdgeLabels.DERIVED_FROM, NodeTypeEnum.Resource,
				ResourceMetadataData.class);
		if (parentRes.isRight()) {
			log.debug("interface {} not found ", interfaceName);
			return Either.right(parentRes.right().value());
		}
		ImmutablePair<ResourceMetadataData, GraphEdge> parenNode = parentRes.left().value();

		Either<List<ImmutablePair<InterfaceData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), parenNode.getKey().getMetadataDataDefinition().getUniqueId(),
				GraphEdgeLabels.INTERFACE, NodeTypeEnum.Interface, InterfaceData.class);
		if (childrenNodes.isRight()) {
			return findInterfaceOnParentNode(parenNode.getKey().getMetadataDataDefinition().getUniqueId(), interfaceName);

		} else {
			for (ImmutablePair<InterfaceData, GraphEdge> interfaceDataNode : childrenNodes.left().value()) {

				GraphEdge interfaceEdge = interfaceDataNode.getRight();
				Map<String, Object> interfaceEdgeProp = interfaceEdge.getProperties();

				if (interfaceEdgeProp.get(GraphPropertiesDictionary.NAME.getProperty()).equals(interfaceName)) {
					return Either.left(interfaceDataNode.getKey());
				}

			}
			return findInterfaceOnParentNode(parenNode.getKey().getMetadataDataDefinition().getUniqueId(), interfaceName);
		}

	}

	@Override
	public Either<InterfaceDefinition, StorageOperationStatus> createInterfaceOnResource(InterfaceDefinition interf, String resourceId, String interfaceName, boolean failIfExist, boolean inTransaction) {

		Either<InterfaceData, TitanOperationStatus> status = addInterfaceToGraph(interf, interfaceName, resourceId);

		if (status.isRight()) {
			titanGenericDao.rollback();
			log.error("Failed to add interface {} to resource {}", interfaceName, resourceId);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status.right().value()));
		} else {

			if (false == inTransaction) {
				titanGenericDao.commit();
			}
			InterfaceData interfaceData = status.left().value();

			InterfaceDefinition interfaceDefResult = convertInterfaceDataToInterfaceDefinition(interfaceData);
			Map<String, Operation> operations = interf.getOperationsMap();
			if (operations != null && !operations.isEmpty()) {
				Set<String> opNames = operations.keySet();
				Map<String, Operation> newOperations = new HashMap<String, Operation>();
				for (String operationName : opNames) {

					Operation op = operations.get(operationName);
					Either<OperationData, TitanOperationStatus> opStatus = addOperationToGraph(interf, operationName, op, interfaceData);
					if (status.isRight()) {
						titanGenericDao.rollback();
						log.error("Failed to add operation {} to interface {}", operationName, interfaceName);
					} else if (status.isLeft()) {
						if (false == inTransaction) {
							titanGenericDao.commit();
						}
						OperationData opData = opStatus.left().value();
						Operation newOperation = this.convertOperationDataToOperation(opData);

						ArtifactDefinition art = op.getImplementationArtifact();
						if (art != null) {
							Either<ArtifactDefinition, StorageOperationStatus> artRes = artifactOperation.addArifactToComponent(art, (String) opData.getUniqueId(), NodeTypeEnum.InterfaceOperation, failIfExist, true);
							if (artRes.isRight()) {
								titanGenericDao.rollback();
								log.error("Failed to add artifact {} to interface {}", operationName, interfaceName);
							} else {
								newOperation.setImplementation(artRes.left().value());
							}
							newOperations.put(operationName, newOperation);
						}
					}
				}
				interfaceDefResult.setOperationsMap(newOperations);
			}
			log.debug("The returned InterfaceDefintion is {}", interfaceDefResult);
			return Either.left(interfaceDefResult);
		}

	}

	@Override
	public Either<Operation, StorageOperationStatus> deleteInterfaceOperation(String resourceId, String interfaceName, String operationId, boolean inTransaction) {

		Either<Operation, TitanOperationStatus> status = removeOperationOnGraph(resourceId, interfaceName, operationId);
		if (status.isRight()) {
			if (false == inTransaction) {
				titanGenericDao.rollback();
			}
			log.error("Failed to delete operation {} of interface {} resource {}", operationId, interfaceName, resourceId);
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(status.right().value()));
		} else {
			if (false == inTransaction) {
				titanGenericDao.commit();
			}

			Operation opDefResult = status.left().value();// convertOperationDataToOperation(operationData);
			log.debug("The returned Operation is {}", opDefResult);
			return Either.left(opDefResult);
		}

	}

	private Either<Operation, TitanOperationStatus> removeOperationOnGraph(String resourceId, String interfaceName, String operationId) {
		log.debug("Before deleting operation from graph {}", operationId);

		Either<List<ImmutablePair<InterfaceData, GraphEdge>>, TitanOperationStatus> childrenNodes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), resourceId, GraphEdgeLabels.INTERFACE, NodeTypeEnum.Interface,
				InterfaceData.class);

		if (childrenNodes.isRight()) {
			log.debug("Not found interface {}", interfaceName);
			return Either.right(childrenNodes.right().value());
		}
		OperationData opData = null;
		for (ImmutablePair<InterfaceData, GraphEdge> interfaceDataNode : childrenNodes.left().value()) {

			GraphEdge interfaceEdge = interfaceDataNode.getRight();
			Map<String, Object> interfaceEdgeProp = interfaceEdge.getProperties();

			String interfaceSplitedName = splitType(interfaceName);

			if (interfaceEdgeProp.get(GraphPropertiesDictionary.NAME.getProperty()).equals(interfaceSplitedName)) {
				Either<List<ImmutablePair<OperationData, GraphEdge>>, TitanOperationStatus> operationRes = titanGenericDao.getChildrenNodes(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) interfaceDataNode.getLeft().getUniqueId(),
						GraphEdgeLabels.INTERFACE_OPERATION, NodeTypeEnum.InterfaceOperation, OperationData.class);
				if (operationRes.isRight()) {
					log.error("Failed to find operation {} on interface {}", operationId, interfaceName);
					return Either.right(operationRes.right().value());
				}
				List<ImmutablePair<OperationData, GraphEdge>> operations = operationRes.left().value();

				for (ImmutablePair<OperationData, GraphEdge> operationPairEdge : operations) {

					opData = operationPairEdge.getLeft();
					if (opData.getUniqueId().equals(operationId)) {

						Either<ImmutablePair<ArtifactData, GraphEdge>, TitanOperationStatus> artifactRes = titanGenericDao.getChild(GraphPropertiesDictionary.UNIQUE_ID.getProperty(), (String) operationPairEdge.getLeft().getUniqueId(),
								GraphEdgeLabels.ARTIFACT_REF, NodeTypeEnum.ArtifactRef, ArtifactData.class);
						Either<ArtifactDefinition, StorageOperationStatus> arStatus = null;
						if (artifactRes.isLeft()) {
							ArtifactData arData = artifactRes.left().value().getKey();
							arStatus = artifactOperation.removeArifactFromResource((String) operationPairEdge.getLeft().getUniqueId(), (String) arData.getUniqueId(), NodeTypeEnum.InterfaceOperation, true, true);
							if (arStatus.isRight()) {
								log.debug("failed to delete artifact {}", arData.getUniqueId());
								return Either.right(TitanOperationStatus.INVALID_ID);
							}
						}
						Either<OperationData, TitanOperationStatus> deleteOpStatus = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.InterfaceOperation), opData.getUniqueId(), OperationData.class);
						if (deleteOpStatus.isRight()) {
							log.debug("failed to delete operation {}", opData.getUniqueId());
							return Either.right(TitanOperationStatus.INVALID_ID);
						}
						opData = deleteOpStatus.left().value();
						Operation operation = new Operation(opData.getOperationDataDefinition());
						if (arStatus != null) {
							operation.setImplementation(arStatus.left().value());
						}
						if (operations.size() <= 1) {
							Either<InterfaceData, TitanOperationStatus> deleteInterfaceStatus = titanGenericDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Interface), interfaceDataNode.left.getUniqueId(), InterfaceData.class);
							if (deleteInterfaceStatus.isRight()) {
								log.debug("failed to delete interface {}", interfaceDataNode.left.getUniqueId());
								return Either.right(TitanOperationStatus.INVALID_ID);
							}

						}

						return Either.left(operation);

					}
				}
			}
		}

		log.debug("Not found operation {}", interfaceName);
		return Either.right(TitanOperationStatus.INVALID_ID);
	}

	private String splitType(String interfaceName) {
		String interfaceSplittedName;
		String[] packageName = interfaceName.split("\\.");

		if (packageName.length == 0) {
			interfaceSplittedName = interfaceName;
		} else {
			interfaceSplittedName = packageName[packageName.length - 1];
		}

		return interfaceSplittedName.toLowerCase();
	}

	/**
	 * FOR TEST ONLY
	 * 
	 * @param titanGenericDao
	 */
	public void setTitanGenericDao(TitanGenericDao titanGenericDao) {
		this.titanGenericDao = titanGenericDao;
	}

	public void setArtifactOperation(ArtifactOperation artifactOperation) {
		this.artifactOperation = artifactOperation;
	}

	@Override
	public Either<InterfaceDefinition, StorageOperationStatus> createInterfaceType(InterfaceDefinition interf, boolean inTransaction) {
		Either<InterfaceDefinition, StorageOperationStatus> result = null;
		try {

			InterfaceData interfaceData = new InterfaceData(interf);
			interf.setUniqueId(interf.getType().toLowerCase());

			Either<InterfaceData, TitanOperationStatus> existInterface = titanGenericDao.getNode(interfaceData.getUniqueIdKey(), interfaceData.getUniqueId(), InterfaceData.class);

			if (existInterface.isLeft()) {
				// already exist
				log.debug("Interface type already exist {}", interfaceData);
				result = Either.right(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
				return result;
			}

			log.debug("Before adding interface type to graph {}", interfaceData);
			Either<InterfaceData, TitanOperationStatus> createNodeResult = titanGenericDao.createNode(interfaceData, InterfaceData.class);
			log.debug("After adding property type to graph {}", interfaceData);

			if (createNodeResult.isRight()) {
				TitanOperationStatus operationStatus = createNodeResult.right().value();
				log.error("Failed to add interface {} to graph. status is {}", interf.getType(), operationStatus);
				result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(operationStatus));
				return result;
			}

			InterfaceDefinition interfaceDefResult = convertInterfaceDataToInterfaceDefinition(interfaceData);
			Map<String, Operation> operations = interf.getOperationsMap();

			if (operations != null && !operations.isEmpty()) {
				Map<String, Operation> newOperations = new HashMap<String, Operation>();

				for (Map.Entry<String, Operation> operation : operations.entrySet()) {
					Either<OperationData, TitanOperationStatus> opStatus = addOperationToGraph(interf, operation.getKey(), operation.getValue(), interfaceData);
					if (opStatus.isRight()) {
						titanGenericDao.rollback();
						log.error("Failed to add operation {} to interface {}", operation.getKey(), interf.getType());

						result = Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(opStatus.right().value()));
						return result;
					} else {
						OperationData opData = opStatus.left().value();
						Operation newOperation = this.convertOperationDataToOperation(opData);
						newOperations.put(operation.getKey(), newOperation);
					}
				}
				interfaceDefResult.setOperationsMap(newOperations);
			}
			result = Either.left(interfaceDefResult);
			return result;
		} finally {
			if (false == inTransaction) {
				if (result == null || result.isRight()) {
					log.error("Going to execute rollback on graph.");
					titanGenericDao.rollback();
				} else {
					log.debug("Going to execute commit on graph.");
					titanGenericDao.commit();
				}
			}
		}

	}

	@Override
	public Either<InterfaceDefinition, StorageOperationStatus> getInterface(String interfaceId) {
		Either<InterfaceData, TitanOperationStatus> getResult = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.Interface), interfaceId, InterfaceData.class);
		if (getResult.isLeft()) {
			InterfaceData interfaceData = getResult.left().value();
			return Either.left(convertInterfaceDataToInterfaceDefinition(interfaceData));
		} else {
			TitanOperationStatus titanStatus = getResult.right().value();
			log.debug("Node with id {} was not found in the graph. status: {}", interfaceId, titanStatus);
			StorageOperationStatus storageOperationStatus = DaoStatusConverter.convertTitanStatusToStorageStatus(titanStatus);
			return Either.right(storageOperationStatus);
		}
	}

	public String getShortInterfaceName(InterfaceDataDefinition interfaceDefinition) {
		String[] packageName = interfaceDefinition.getType().split("\\.");
		String interfaceName;
		if (packageName.length == 0) {
			interfaceName = interfaceDefinition.getType();
		} else {
			interfaceName = packageName[packageName.length - 1];
		}
		return interfaceName.toLowerCase();
	}

	/** 
	 * 
	 */
	public Either<InterfaceDefinition, StorageOperationStatus> createInterfaceType(InterfaceDefinition interf) {
		return createInterfaceType(interf, false);
	}

}
