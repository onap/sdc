/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("interfaces-operation")
public class InterfaceOperation extends BaseOperation {

  private static final Logger logger = Logger.getLogger(InterfaceOperation.class.getName());

  @Autowired
  private ArtifactCassandraDao artifactCassandraDao;

  public Either<InterfaceDefinition, StorageOperationStatus> addInterface(String componentId,
      InterfaceDefinition interfaceDefinition) {
    return addOrUpdateInterface(false, componentId, interfaceDefinition);
  }

  public Either<InterfaceDefinition, StorageOperationStatus> updateInterface(String componentId,
      InterfaceDefinition interfaceDefinition) {
    return addOrUpdateInterface(true, componentId, interfaceDefinition);
  }

  private Either<InterfaceDefinition, StorageOperationStatus> addOrUpdateInterface(
      boolean isUpdateAction, String componentId, InterfaceDefinition interfaceDefinition) {

    StorageOperationStatus statusRes;
    Either<GraphVertex, TitanOperationStatus> getToscaElementRes;

    getToscaElementRes = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
    if (getToscaElementRes.isRight()) {
      TitanOperationStatus status = getToscaElementRes.right().value();
      logger.debug("Failed to get tosca element {} while adding or updating interface. Status is {}. ", componentId, status);
      statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
      return Either.right(statusRes);
    }
    GraphVertex componentVertex = getToscaElementRes.left().value();
    if (!isUpdateAction) {
      interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
    }
    statusRes = performUpdateToscaAction(isUpdateAction, componentVertex, Arrays.asList(interfaceDefinition), EdgeLabelEnum.INTERFACE, VertexTypeEnum.INTERFACE);
    if (!statusRes.equals(StorageOperationStatus.OK)) {
      logger.debug("Failed to add or update interface of component {}. status is {}", componentId, statusRes);
      return Either.right(statusRes);
    }
    return Either.left(interfaceDefinition);
  }

  public Either<Operation, StorageOperationStatus> addInterfaceOperation(String componentId, InterfaceDefinition interfaceDef, Operation interfaceOperation) {
    return addOrUpdateInterfaceOperation(false, componentId, interfaceDef, interfaceOperation);
  }

  public Either<Operation, StorageOperationStatus> updateInterfaceOperation(String componentId, InterfaceDefinition interfaceDef, Operation interfaceOperation) {
    return addOrUpdateInterfaceOperation(true, componentId, interfaceDef, interfaceOperation);
  }

  private Either<Operation, StorageOperationStatus> addOrUpdateInterfaceOperation(boolean isUpdateAction, String componentId, InterfaceDefinition interfaceDef, Operation operation) {

    StorageOperationStatus statusRes;
    Either<GraphVertex, TitanOperationStatus> getToscaElementRes;
    Either<GraphVertex, TitanOperationStatus> getToscaElementInt;

    getToscaElementRes = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
    if (getToscaElementRes.isRight()) {
      TitanOperationStatus status = getToscaElementRes.right().value();
      logger.debug("Failed to get tosca element {} while adding or updating operation. Status is {}. ", componentId, status);
      statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
      return Either.right(statusRes);
    }
    GraphVertex componentVertex = getToscaElementRes.left().value();
    getToscaElementInt = titanDao.getChildVertex(componentVertex, EdgeLabelEnum.INTERFACE, JsonParseFlagEnum.NoParse);
    if (getToscaElementInt.isRight()) {
      TitanOperationStatus status = getToscaElementInt.right().value();
      logger.debug("Failed to get tosca element {} while adding or updating operation. Status is {}. ", interfaceDef.getUniqueId(), status);
      statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
      return Either.right(statusRes);
    }
    GraphVertex interfaceVertex = getToscaElementInt.left().value();
    if (!isUpdateAction) {
      initNewOperation(operation);
    }

    statusRes = performUpdateToscaAction(isUpdateAction, interfaceVertex, Arrays.asList(operation),
        EdgeLabelEnum.INTERFACE_OPERATION, VertexTypeEnum.INTERFACE_OPERATION);
    if (!statusRes.equals(StorageOperationStatus.OK)) {
      logger.debug("Failed to add or update operation of interface {}. status is {}", interfaceDef.getUniqueId(), statusRes);
      return Either.right(statusRes);
    }

    getUpdatedInterfaceDef(interfaceDef, operation, operation.getUniqueId());
    Either<InterfaceDefinition, StorageOperationStatus> intUpdateStatus = updateInterface(componentId, interfaceDef);
    if (intUpdateStatus.isRight() && !intUpdateStatus.right().value().equals(StorageOperationStatus.OK)) {
      logger.debug("Failed to update interface details on component {}. status is {}", componentId, statusRes);
      return Either.right(statusRes);
    }

    return Either.left(operation);
  }

  public Either<Operation, StorageOperationStatus> deleteInterfaceOperation(String componentId, InterfaceDefinition interfaceDef, String operationToDelete) {
    Either<GraphVertex, TitanOperationStatus> getInterfaceVertex;
    Either<GraphVertex, TitanOperationStatus> getComponentVertex;
    Operation operation = new Operation();
    StorageOperationStatus status = null;

    getComponentVertex = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
    if (getComponentVertex.isRight()) {
      return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
    }

    getInterfaceVertex = titanDao.getChildVertex(getComponentVertex.left().value(), EdgeLabelEnum.INTERFACE, JsonParseFlagEnum.NoParse);
    if (getInterfaceVertex.isRight()) {
      return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getInterfaceVertex.right().value()));
    }

    Optional<Entry<String, Operation>> operationToRemove = interfaceDef.getOperationsMap().entrySet().stream()
        .filter(entry -> entry.getValue().getUniqueId().equals(operationToDelete)).findAny();
    if (operationToRemove.isPresent()){
      Map.Entry<String, Operation> stringOperationEntry = operationToRemove.get();
      operation = stringOperationEntry.getValue();
      ArtifactDefinition implementationArtifact = operation.getImplementationArtifact();
      String artifactUUID = implementationArtifact.getArtifactUUID();
      CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(artifactUUID);
      if (cassandraStatus != CassandraOperationStatus.OK) {
        logger.debug("Failed to delete the artifact {} from the database. ", artifactUUID);
        return Either.right(DaoStatusConverter.convertCassandraStatusToStorageStatus(cassandraStatus));
      }

      if(interfaceDef.getOperationsMap().size() > 1){
        status = deleteToscaDataElements(getInterfaceVertex.left().value(), EdgeLabelEnum.INTERFACE_OPERATION, Arrays.asList(operationToDelete));
        if (status != StorageOperationStatus.OK) {
          return Either.right(status);
        }
      } else {
        status = removeToscaDataVertex(getInterfaceVertex.left().value(), EdgeLabelEnum.INTERFACE_OPERATION, VertexTypeEnum.INTERFACE_OPERATION);
        if (status != StorageOperationStatus.OK) {
          return Either.right(status);
        }
      }

      getUpdatedInterfaceDef(interfaceDef, null, operationToDelete);
      if (interfaceDef.getOperations().isEmpty()) {
        status = removeToscaDataVertex(getComponentVertex.left().value(), EdgeLabelEnum.INTERFACE, VertexTypeEnum.INTERFACE);
        if (status != StorageOperationStatus.OK) {
          return Either.right(status);
        }
      }
      else {
        Either<InterfaceDefinition, StorageOperationStatus> intUpdateStatus = updateInterface(componentId, interfaceDef);
        if (intUpdateStatus.isRight() && !intUpdateStatus.right().value().equals(StorageOperationStatus.OK)) {
          return Either.right(status);
        }
      }
    }
    return Either.left(operation);
  }

  public Either<Operation, StorageOperationStatus> getInterfaceOperation(InterfaceDefinition interfaceDef, String operationToGet) {
    Operation operation = new Operation();
    Optional<Entry<String, Operation>> operationToFetch = interfaceDef.getOperationsMap().entrySet().stream()
        .filter(entry -> entry.getValue().getUniqueId().equals(operationToGet)).findAny();
    if (operationToFetch.isPresent()){
      Map.Entry<String, Operation> stringOperationEntry = operationToFetch.get();
      operation = stringOperationEntry.getValue();
    }
    return Either.left(operation);
  }

  private StorageOperationStatus performUpdateToscaAction(boolean isUpdate, GraphVertex graphVertex,
      List<ToscaDataDefinition> toscaDataList, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel) {
    if (isUpdate) {
      return updateToscaDataOfToscaElement(graphVertex, edgeLabel, vertexLabel, toscaDataList, JsonPresentationFields.UNIQUE_ID);
    } else {
      return addToscaDataToToscaElement(graphVertex, edgeLabel, vertexLabel, toscaDataList, JsonPresentationFields.UNIQUE_ID);
    }
  }

  private void initNewOperation(Operation operation){
    ArtifactDefinition artifactDefinition = new ArtifactDefinition();
    String artifactUUID = UUID.randomUUID().toString();
    artifactDefinition.setArtifactUUID(artifactUUID);
    artifactDefinition.setUniqueId(artifactUUID);
    artifactDefinition.setArtifactType(ArtifactTypeEnum.PLAN.getType());
    artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.LIFE_CYCLE);
    operation.setImplementation(artifactDefinition);
    operation.setUniqueId(UUID.randomUUID().toString());
  }

  private InterfaceDefinition getUpdatedInterfaceDef(InterfaceDefinition interfaceDef, Operation operation, String operationId){
    Map<String, Operation> operationMap = interfaceDef.getOperationsMap();
    if(operation != null && !operation.isEmpty()){
      operationMap.put(operationId, operation);
      interfaceDef.setOperationsMap(operationMap);
    }
    else {
      operationMap.remove(operationId);
      interfaceDef.setOperationsMap(operationMap);
    }
    return interfaceDef;
  }

}


