package org.openecomp.sdc.asdctool.migration.tasks.mig1802;

import fj.data.Either;
import org.apache.commons.collections.ListUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SdcCatalogMigration implements Migration {
    private static final Logger LOGGER = Logger.getLogger(SdcCatalogMigration.class);
    private static final List<ResourceTypeEnum> EXCLUDE_TYPES = Arrays.asList(ResourceTypeEnum.VFCMT, ResourceTypeEnum.Configuration);

    private ToscaElementOperation toscaElementOperation;
    private TitanDao titanDao;

    public SdcCatalogMigration(TopologyTemplateOperation toscaElementOperation, TitanDao titanDao) {
        this.toscaElementOperation = toscaElementOperation;
        this.titanDao = titanDao;
    }

    @Override
    public String description() {
        return "optimize sdc catalog vertices";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1802), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        TitanOperationStatus status = null;
        try {
            status = getOrCreateCatalogRoot()
                    .either(this::associateCatalogRootToCatalogElements,
                            err -> {LOGGER.error("failed to create catalog root. err: {}", err); return err;});
            return status == TitanOperationStatus.OK ? MigrationResult.success() : MigrationResult.error("failed to create and associate catalog root. error: " + status);
        } finally {
            commitOrRollBack(status);
        }
    }

    private void commitOrRollBack(TitanOperationStatus status) {
        if (status == TitanOperationStatus.OK) {
            titanDao.commit();
        } else {
            titanDao.rollback();
        }
    }

    private Either<GraphVertex, TitanOperationStatus> getOrCreateCatalogRoot() {
        LOGGER.info("creating or getting catalog root vertex");
        return titanDao.getVertexByLabel(VertexTypeEnum.CATALOG_ROOT)
                .right()
                .bind(this::createRootCatalogVertexOrError);
    }


    private Either<GraphVertex, TitanOperationStatus> createRootCatalogVertexOrError(TitanOperationStatus titanOperationStatus) {
        return titanOperationStatus == TitanOperationStatus.NOT_FOUND ? createRootCatalogVertex() : Either.right(titanOperationStatus);
    }

    private Either<GraphVertex, TitanOperationStatus> createRootCatalogVertex() {
        LOGGER.info("Creating root catalog vertex");
        GraphVertex catalogRootVertex = new GraphVertex(VertexTypeEnum.CATALOG_ROOT);
        catalogRootVertex.setUniqueId(IdBuilderUtils.generateUniqueId());
        return titanDao.createVertex(catalogRootVertex);
    }

    private Either<List<GraphVertex>, TitanOperationStatus> getAllCatalogVertices() {
        LOGGER.info("fetching all catalog resources");
        return toscaElementOperation.getListOfHighestComponents(ComponentTypeEnum.RESOURCE, EXCLUDE_TYPES, JsonParseFlagEnum.ParseMetadata)
                .right()
                .bind(this::errOrEmptyListIfNotFound)
                .left()
                .bind(this::getAllCatalogVertices);
    }

    private Either<List<GraphVertex>, TitanOperationStatus> errOrEmptyListIfNotFound(TitanOperationStatus err) {
        return TitanOperationStatus.NOT_FOUND.equals(err) ? Either.left(new ArrayList<>()) : Either.right(err);
    }

    @SuppressWarnings("unchecked")
    private Either<List<GraphVertex>, TitanOperationStatus> getAllCatalogVertices(List<GraphVertex> allResourceCatalogVertices) {
        LOGGER.info("number of resources: {}", allResourceCatalogVertices.size());
        LOGGER.info("fetching all catalog services");
        return toscaElementOperation.getListOfHighestComponents(ComponentTypeEnum.SERVICE, EXCLUDE_TYPES, JsonParseFlagEnum.ParseMetadata)
                .right()
                .bind(this::errOrEmptyListIfNotFound)
                .left()
                .map(allServiceVertices -> ListUtils.union(allServiceVertices, allResourceCatalogVertices));
    }

    private TitanOperationStatus associateCatalogRootToCatalogElements(GraphVertex root) {
        return getAllCatalogVertices()
                .either(catalogVertices -> associateCatalogRootToCatalogElements(root, catalogVertices),
                        err -> err);
    }

    private TitanOperationStatus associateCatalogRootToCatalogElements(GraphVertex root, List<GraphVertex> catalogElements) {
        LOGGER.info("number of catalog elements: {}", catalogElements.size());
        LOGGER.info("connect all catalog elements to root edge");
        List<GraphVertex> nonConnectedElements = catalogElements.stream().filter(this::edgeNotAlreadyExists).collect(Collectors.toList());
        int numOfCreatedEdges = 0;
        for (GraphVertex catalogElement : nonConnectedElements) {
                TitanOperationStatus edgeCreationStatus = titanDao.createEdge(root, catalogElement, EdgeLabelEnum.CATALOG_ELEMENT, null);
                if (edgeCreationStatus != TitanOperationStatus.OK) {
                    LOGGER.error("failed to create edge from catalog element to vertex {}", catalogElement.getUniqueId());
                    return edgeCreationStatus;
                }
                LOGGER.debug("created edge from catalog root to element {}", catalogElement.getUniqueId());
                numOfCreatedEdges++;
        }
        LOGGER.info("number edges created: {}", numOfCreatedEdges);
        return TitanOperationStatus.OK;
    }

    private boolean edgeNotAlreadyExists(GraphVertex catalogElement) {
        return !catalogElement.getVertex().edges(Direction.IN, EdgeLabelEnum.CATALOG_ELEMENT.name()).hasNext();
    }


}
