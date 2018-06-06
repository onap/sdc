package org.openecomp.sdc.be.model.operations.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.UniqueIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component
public class DefaultDerivedFromOperation implements DerivedFromOperation {

    private static final Logger log = LoggerFactory.getLogger(DefaultDerivedFromOperation.class);
    private TitanGenericDao titanGenericDao;

    public DefaultDerivedFromOperation(TitanGenericDao titanGenericDao) {
        this.titanGenericDao = titanGenericDao;
    }

    @Override
    public Either<GraphRelation, StorageOperationStatus> addDerivedFromRelation(String parentUniqueId, String derivedFromUniqueId, NodeTypeEnum nodeType) {
        UniqueIdData from = new UniqueIdData(NodeTypeEnum.PolicyType, parentUniqueId);
        UniqueIdData to = new UniqueIdData(NodeTypeEnum.PolicyType, derivedFromUniqueId);
        return titanGenericDao.createRelation(from, to, GraphEdgeLabels.DERIVED_FROM, null)
                .right()
                .map(DaoStatusConverter::convertTitanStatusToStorageStatus);
    }

    @Override
    public <T extends GraphNode> Either<T, StorageOperationStatus> getDerivedFromChild(String uniqueId, NodeTypeEnum nodeType, Class<T> clazz) {
        log.debug("#getDerivedFromChild - fetching derived from entity for node type {} with id {}", nodeType, uniqueId);
        return titanGenericDao.getChild(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, GraphEdgeLabels.DERIVED_FROM, nodeType, clazz)
                .bimap(Pair::getKey,
                       DaoStatusConverter::convertTitanStatusToStorageStatus);
    }

    @Override
    public StorageOperationStatus removeDerivedFromRelation(String uniqueId, String derivedFromUniqueId, NodeTypeEnum nodeType) {
        UniqueIdData from = new UniqueIdData(NodeTypeEnum.PolicyType, uniqueId);
        UniqueIdData to = new UniqueIdData(NodeTypeEnum.PolicyType, derivedFromUniqueId);
        return isDerivedFromExists(from, to)
                .either(isRelationExist -> isRelationExist ? deleteDerivedFrom(from, to) : StorageOperationStatus.OK,
                        DaoStatusConverter::convertTitanStatusToStorageStatus);


    }

    private StorageOperationStatus deleteDerivedFrom(UniqueIdData from,  UniqueIdData to) {
        return titanGenericDao.deleteRelation(from, to, GraphEdgeLabels.DERIVED_FROM)
                .either(deletedRelation -> StorageOperationStatus.OK,
                        DaoStatusConverter::convertTitanStatusToStorageStatus);
    }

    private Either<Boolean, TitanOperationStatus> isDerivedFromExists(UniqueIdData from, UniqueIdData to) {
        return titanGenericDao.isRelationExist(from, to, GraphEdgeLabels.DERIVED_FROM);
    }


}
