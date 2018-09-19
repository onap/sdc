package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Combination;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component("combination-operation")
public class CombinationOperation {
    private static final Logger log = Logger.getLogger(CombinationOperation.class);

    @Autowired
    protected TitanDao titanDao;

    public Either<Combination, StorageOperationStatus> createCombinationElement
            (Service service, Combination combination, String combinationJson) {

        try {
            GraphVertex combinationVertex = new GraphVertex();
            combinationVertex = fillMetadata(combinationVertex, combination, service, combinationJson);
            Either<GraphVertex, TitanOperationStatus> createdVertex = titanDao.createVertex(combinationVertex);
            if (createdVertex.isRight()) {
                log.debug("error");
                return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(createdVertex.right().value()));
            }
            StorageOperationStatus status = associateToCatalogRoot(createdVertex.left().value());
            if (status != StorageOperationStatus.OK) {
                log.error("Failed to attach Vertex to Catalog Root");
                return Either.right(status);
            }
            titanDao.commit();
            return Either.left(combination);

        } catch (Exception e) {
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.NOT_CREATED));
        }
    }

    private GraphVertex fillMetadata
            (GraphVertex combinationVertex, Combination combination, Service service, String combinationJson) {
        combinationVertex.setUniqueId(combination.getUniqueId());
        combinationVertex.setLabel(VertexTypeEnum.COMBINATION);
        combinationVertex.setType(ComponentTypeEnum.COMBINATION);
        combinationVertex.addMetadataProperty(GraphPropertyEnum.NORMALIZED_NAME, ValidationUtils.normaliseComponentName
                (combination.getName()));
        combinationVertex.setJsonString(combinationJson);
        return combinationVertex;
    }

    public Either<String, ResponseFormat> getCombination(String combinationUid) {

        Either<GraphVertex, TitanOperationStatus> graphVertexEither = titanDao.getVertexById(combinationUid);
        if (graphVertexEither.isRight()) {
            log.error("failed  to get Vertex");
        }
        GraphVertex graphVertex = graphVertexEither.left().value();
        String combinationJson = graphVertex.getJsonString();

        return Either.left(combinationJson);
    }

    private StorageOperationStatus associateToCatalogRoot(GraphVertex nodeTypeVertex) {
        Either<GraphVertex, TitanOperationStatus> catalog = titanDao.getVertexByLabel(VertexTypeEnum.CATALOG_ROOT);
        if (catalog.isRight()) {
            log.debug("Failed to fetch catalog root vertex. error {}", catalog.right().value());
            return DaoStatusConverter.convertTitanStatusToStorageStatus(catalog.right().value());
        }
        TitanOperationStatus createEdge = titanDao.createEdge(catalog.left().value(), nodeTypeVertex, EdgeLabelEnum.CATALOG_ELEMENT, null);

        return DaoStatusConverter.convertTitanStatusToStorageStatus(createEdge);
    }

    public Either<List<String>, TitanOperationStatus> getAllCombinations() {
        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        List<String> combJsonStringList = new ArrayList<>();
        props.put(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.COMBINATION.name());
        Either<List<GraphVertex>, TitanOperationStatus> combinationVertexEither = titanDao.getByCriteria(VertexTypeEnum.COMBINATION, props);
        if (combinationVertexEither.isRight()) {
            log.error("Cannot find Vertex");
            return Either.right(combinationVertexEither.right().value());
        }
        List<GraphVertex> combinationVertexList = combinationVertexEither.left().value();
        for (GraphVertex graphVertex : combinationVertexList) {
            String combinationJson = graphVertex.getJsonString();
            combJsonStringList.add(combinationJson);
        }
        return Either.left(combJsonStringList);
    }

    public void setTitanDao(TitanDao titanDao) {
        this.titanDao = titanDao;
    }
}






















