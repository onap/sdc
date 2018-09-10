package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.Combination;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("combination-operation")
public class CombinationOperation {

    @Autowired
    protected TitanDao titanDao;

    private static Logger log = Logger.getLogger(ToscaElementOperation.class.getName());

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
            //  associateToCatalogRoot(createdVertex.left().value());
            titanDao.commit();
            return Either.left(combination);

        } catch (Exception e) {
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(TitanOperationStatus.NOT_CREATED));
        }
    }

    private GraphVertex fillMetadata
            (GraphVertex combinationVertex, Combination combination, Service service, String combinationJson) {
        combinationVertex.setUniqueId(combination.getName());
        combinationVertex.setLabel(VertexTypeEnum.COMBINATION);
        combinationVertex.setJsonString(combinationJson);
        return combinationVertex;
    }

    public Either<String, ResponseFormat> getCombination(ComponentInstance componentInstance) {

        Either<GraphVertex, TitanOperationStatus> graphVertexEither = titanDao.getVertexById(componentInstance.getComponentUid());
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
            log.debug("Failed to fetch catalog vertex. error {}", catalog.right().value());
            return DaoStatusConverter.convertTitanStatusToStorageStatus(catalog.right().value());
        }
        TitanOperationStatus createEdge = titanDao.createEdge(catalog.left().value(), nodeTypeVertex, EdgeLabelEnum.CATALOG_ELEMENT, null);

        return DaoStatusConverter.convertTitanStatusToStorageStatus(createEdge);
    }
}
