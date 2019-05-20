package org.openecomp.sdc.be.model.operations.impl;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class DefaultDerivedFromOperation implements DerivedFromOperation {

    private static final Logger log = Logger.getLogger(DefaultDerivedFromOperation.class.getName());
    private JanusGraphGenericDao janusGraphGenericDao;

    public DefaultDerivedFromOperation(JanusGraphGenericDao janusGraphGenericDao) {
        this.janusGraphGenericDao = janusGraphGenericDao;
    }

    @Override
    public Either<GraphRelation, StorageOperationStatus> addDerivedFromRelation(String parentUniqueId, String derivedFromUniqueId, NodeTypeEnum nodeType) {
        UniqueIdData from = new UniqueIdData(nodeType, parentUniqueId);
        UniqueIdData to = new UniqueIdData(nodeType, derivedFromUniqueId);
        return janusGraphGenericDao.createRelation(from, to, GraphEdgeLabels.DERIVED_FROM, null)
                .right()
                .map(DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    @Override
    public <T extends GraphNode> Either<T, StorageOperationStatus> getDerivedFromChild(String uniqueId, NodeTypeEnum nodeType, Class<T> clazz) {
        log.debug("#getDerivedFromChild - fetching derived from entity for node type {} with id {}", nodeType, uniqueId);
        return janusGraphGenericDao
            .getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.DERIVED_FROM, nodeType, clazz)
                .bimap(Pair::getKey,
                       DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    @Override
    public StorageOperationStatus removeDerivedFromRelation(String uniqueId, String derivedFromUniqueId, NodeTypeEnum nodeType) {
        UniqueIdData from = new UniqueIdData(nodeType, uniqueId);
        UniqueIdData to = new UniqueIdData(nodeType, derivedFromUniqueId);
        return isDerivedFromExists(from, to)
                .either(isRelationExist -> isRelationExist ? deleteDerivedFrom(from, to) : StorageOperationStatus.OK,
                        DaoStatusConverter::convertJanusGraphStatusToStorageStatus);


    }

    private StorageOperationStatus deleteDerivedFrom(UniqueIdData from,  UniqueIdData to) {
        return janusGraphGenericDao.deleteRelation(from, to, GraphEdgeLabels.DERIVED_FROM)
                .either(deletedRelation -> StorageOperationStatus.OK,
                        DaoStatusConverter::convertJanusGraphStatusToStorageStatus);
    }

    private Either<Boolean, JanusGraphOperationStatus> isDerivedFromExists(UniqueIdData from, UniqueIdData to) {
        return janusGraphGenericDao.isRelationExist(from, to, GraphEdgeLabels.DERIVED_FROM);
    }
    
    @Override
    public <T extends GraphNode> Either<Boolean, StorageOperationStatus> isTypeDerivedFrom(String childCandidateType, String parentCandidateType, String currentChildType, 
                                                                                                    NodeTypeEnum nodeType, Class<T> clazz, Function<T, String> typeProvider) {
        Map<String, Object> propertiesToMatch = new HashMap<>();
        propertiesToMatch.put(GraphPropertiesDictionary.TYPE.getProperty(), childCandidateType);
        
        Either<List<T>, JanusGraphOperationStatus> getResponse = janusGraphGenericDao
            .getByCriteria(nodeType, propertiesToMatch, clazz);
        if (getResponse.isRight()) {
            JanusGraphOperationStatus janusGraphOperationStatus = getResponse.right().value();
            log.debug("Couldn't fetch type {}, error: {}", childCandidateType,
                janusGraphOperationStatus);
            return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                janusGraphOperationStatus));
        }
        T node = getResponse.left().value().get(0);
        String childUniqueId = node.getUniqueId();
        String childType = typeProvider.apply(node);
        
        Set<String> travelledTypes = new HashSet<>();
        if (currentChildType != null) {
            travelledTypes.add(currentChildType);
        }
        
        do {
            travelledTypes.add(childType);
            Either<List<ImmutablePair<T, GraphEdge>>, JanusGraphOperationStatus> childrenNodes = janusGraphGenericDao
                .getChildrenNodes(UniqueIdBuilder.getKeyByNodeType(nodeType), childUniqueId, GraphEdgeLabels.DERIVED_FROM,
                    nodeType, clazz);
            if (childrenNodes.isRight()) {
                if (childrenNodes.right().value() != JanusGraphOperationStatus.NOT_FOUND) {
                    JanusGraphOperationStatus janusGraphOperationStatus = getResponse.right().value();
                    log.debug("Couldn't fetch derived from node for type {}, error: {}", childCandidateType,
                        janusGraphOperationStatus);
                    return Either.right(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(
                        janusGraphOperationStatus));
                } else {
                    log.debug("Derived from node is not found for type {} - this is OK for root capability.", childCandidateType);
                    return Either.left(false);
                }
            }
            String derivedFromUniqueId = childrenNodes.left().value().get(0).getLeft().getUniqueId();
            String derivedFromType = typeProvider.apply(childrenNodes.left().value().get(0).getLeft());
            if (derivedFromType.equals(parentCandidateType)) {
                log.debug("Verified that type {} derives from type {}", childCandidateType, parentCandidateType);
                return Either.left(true);
            }
            childUniqueId = derivedFromUniqueId;
            childType = derivedFromType;
        } while (!travelledTypes.contains(childType));
        // this stop condition should never be used, if we use it, we have an
        // illegal cycle in graph - "derived from" hierarchy cannot be cycled.
        // It's here just to avoid infinite loop in case we have such cycle.
        log.error("Detected a cycle of \"derived from\" edges starting at type node {}", childType);
        return Either.right(StorageOperationStatus.GENERAL_ERROR);
    }
    
    
    
    @Override
    public <T extends GraphNode> StorageOperationStatus isUpdateParentAllowed(String oldTypeParent, String newTypeParent, String childType,
                                                                              NodeTypeEnum nodeType, Class<T> clazz,
                                                                              Function<T, String> typeProvider) {
        StorageOperationStatus status;
        if (oldTypeParent != null) {
            
            Either<Boolean, StorageOperationStatus> result = isTypeDerivedFrom(newTypeParent, oldTypeParent, childType, nodeType, clazz, typeProvider);
            if (result.isRight()) {
                log.debug("#isUpdateParentAllowed - failed to detect that new parent {} is derived from the current parent {}",  newTypeParent, oldTypeParent);
                status = result.right().value();
            }
            else {
                if (result.left().value()) {
                    log.debug("#isUpdateParentAllowed - update is allowed since new parent {} is derived from the current parent {}",  newTypeParent, oldTypeParent);
                    status = StorageOperationStatus.OK;
                }
                else {
                    log.debug("#isUpdateParentAllowed - update is not allowed since new parent {} is not derived from the current parent {}",  newTypeParent, oldTypeParent);
                    status = StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY;
                }
            }
                        
        }
        else {
            log.debug("#isUpdateParentAllowed - the update is allowed since the parent still has been not set." );
            status = StorageOperationStatus.OK;
        }
        
        return status;
    }

}
