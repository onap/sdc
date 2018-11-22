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
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("interfaces-operation")
public class InterfaceOperation extends BaseOperation {

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
      statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
      return Either.right(statusRes);
    }
    GraphVertex componentVertex = getToscaElementRes.left().value();
    if (!isUpdateAction) {
      interfaceDefinition.setUniqueId(UUID.randomUUID().toString());
    }
    statusRes = performUpdateToscaAction(isUpdateAction, componentVertex,
        Collections.singletonList(interfaceDefinition), EdgeLabelEnum.INTERFACE, VertexTypeEnum.INTERFACE);
    if (!statusRes.equals(StorageOperationStatus.OK)) {
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

    if(isUpdateAction && operation.getImplementationArtifact() != null){
      String artifactUUID = operation.getImplementationArtifact().getArtifactUUID();
      Either<Long, CassandraOperationStatus> artifactCount = artifactCassandraDao.getCountOfArtifactById(artifactUUID);
      if(artifactCount.isLeft()){
        CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(artifactUUID);
        if (cassandraStatus != CassandraOperationStatus.OK) {
          return Either.right(DaoStatusConverter.convertCassandraStatusToStorageStatus(cassandraStatus));
        }
      }
    }

    getToscaElementRes = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
    if (getToscaElementRes.isRight()) {
      TitanOperationStatus status = getToscaElementRes.right().value();
      statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
      return Either.right(statusRes);
    }
    GraphVertex componentVertex = getToscaElementRes.left().value();
    getToscaElementInt = titanDao.getChildVertex(componentVertex, EdgeLabelEnum.INTERFACE, JsonParseFlagEnum.NoParse);
    if (getToscaElementInt.isRight()) {
      TitanOperationStatus status = getToscaElementInt.right().value();
      statusRes = DaoStatusConverter.convertTitanStatusToStorageStatus(status);
      return Either.right(statusRes);
    }
    GraphVertex interfaceVertex = getToscaElementInt.left().value();

    statusRes = performUpdateToscaAction(isUpdateAction, interfaceVertex,
        Collections.singletonList(operation), EdgeLabelEnum.INTERFACE_OPERATION, VertexTypeEnum.INTERFACE_OPERATION);
    if (!statusRes.equals(StorageOperationStatus.OK)) {
      return Either.right(statusRes);
    }

    getUpdatedInterfaceDef(interfaceDef, operation, operation.getUniqueId());
    Either<InterfaceDefinition, StorageOperationStatus> intUpdateStatus = updateInterface(componentId, interfaceDef);
    if (intUpdateStatus.isRight() && !intUpdateStatus.right().value().equals(StorageOperationStatus.OK)) {
      return Either.right(statusRes);
    }

    return Either.left(operation);
  }

  public Either<Operation, StorageOperationStatus> deleteInterfaceOperation(String componentId, InterfaceDefinition interfaceDef, String operationToDelete) {
    Either<GraphVertex, TitanOperationStatus> getInterfaceVertex;
    Either<GraphVertex, TitanOperationStatus> getComponentVertex;
    Operation operation = new Operation();
    StorageOperationStatus status;

    getComponentVertex = titanDao.getVertexById(componentId, JsonParseFlagEnum.NoParse);
    if (getComponentVertex.isRight()) {
      return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getComponentVertex.right().value()));
    }

    getInterfaceVertex = titanDao.getChildVertex(getComponentVertex.left().value(), EdgeLabelEnum.INTERFACE, JsonParseFlagEnum.NoParse);
    if (getInterfaceVertex.isRight()) {
      return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getInterfaceVertex.right().value()));
    }

      if (!interfaceDef.getOperationsMap().isEmpty()) {
          Either<GraphVertex, TitanOperationStatus> getInterfaceOpVertex =
                  titanDao.getChildVertex(getInterfaceVertex.left().value(), EdgeLabelEnum.INTERFACE_OPERATION,
                          JsonParseFlagEnum.NoParse);
          if (getInterfaceOpVertex.isRight()) {
              List<ToscaDataDefinition> toscaDataList = new ArrayList<>(interfaceDef.getOperationsMap().values());
              StorageOperationStatus statusRes =
                      addToscaDataToToscaElement(getInterfaceVertex.left().value(), EdgeLabelEnum.INTERFACE_OPERATION,
                              VertexTypeEnum.INTERFACE_OPERATION, toscaDataList, JsonPresentationFields.UNIQUE_ID);
              if (!statusRes.equals(StorageOperationStatus.OK)) {
                  return Either.right(statusRes);
              }
          }
      }

    Optional<Entry<String, Operation>> operationToRemove = interfaceDef.getOperationsMap().entrySet().stream()
        .filter(entry -> entry.getValue().getUniqueId().equals(operationToDelete)).findAny();
    if (operationToRemove.isPresent()){
      Map.Entry<String, Operation> stringOperationEntry = operationToRemove.get();
      operation = stringOperationEntry.getValue();
      ArtifactDefinition implementationArtifact = operation.getImplementationArtifact();
      if(implementationArtifact != null){
        String artifactUUID = implementationArtifact.getArtifactUUID();
        CassandraOperationStatus cassandraStatus = artifactCassandraDao.deleteArtifact(artifactUUID);
        if (cassandraStatus != CassandraOperationStatus.OK) {
          return Either.right(DaoStatusConverter.convertCassandraStatusToStorageStatus(cassandraStatus));
        }
      }

      if(interfaceDef.getOperationsMap().size() > 1){
        status = deleteToscaDataElements(getInterfaceVertex.left().value(), EdgeLabelEnum.INTERFACE_OPERATION, Collections.singletonList(operationToDelete));
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
        status = deleteToscaDataElements(getComponentVertex.left().value(), EdgeLabelEnum.INTERFACE, Collections.singletonList(interfaceDef.getUniqueId()));
        if (status != StorageOperationStatus.OK) {
          return Either.right(status);
        }
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

  private StorageOperationStatus performUpdateToscaAction(boolean isUpdate, GraphVertex graphVertex,
      List<ToscaDataDefinition> toscaDataList, EdgeLabelEnum edgeLabel, VertexTypeEnum vertexLabel) {
    if (isUpdate) {
      return updateToscaDataOfToscaElement(graphVertex, edgeLabel, vertexLabel, toscaDataList, JsonPresentationFields.UNIQUE_ID);
    } else {
      return addToscaDataToToscaElement(graphVertex, edgeLabel, vertexLabel, toscaDataList, JsonPresentationFields.UNIQUE_ID);
    }
  }

  private void getUpdatedInterfaceDef(InterfaceDefinition interfaceDef, Operation operation, String operationId){
    Map<String, Operation> operationMap = interfaceDef.getOperationsMap();
    if(operation != null){
      operationMap.put(operationId, operation);
      interfaceDef.setOperationsMap(operationMap);
    }
    else {
      operationMap.remove(operationId);
      interfaceDef.setOperationsMap(operationMap);
    }
  }

}


