package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.asdctool.impl.migration.MigrationMsg;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fj.data.List.list;
import static org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils.handleError;

public class UserStatesMigration extends JsonModelMigration<Edge> {

    private static final String MIGRATING_USER_ID = "jh0003";
    private static final int OUT_VERTEX_INDEX = 0;
    private static final int IN_VERTEX_INDEX = 1;
    private static Logger LOGGER = LoggerFactory.getLogger(UserStatesMigration.class);

    @Resource(name = "titan-generic-dao")
    private TitanGenericDao genericDao;

    @Resource(name = "titan-generic-dao-migration")
    private TitanGenericDao genericDaoMigration;

    @Override
    public String description() {
        return "migrate user states";
    }


    @Override
    public boolean migrate() {
//        return removeMigratingUserStates() && super.migrate();
        return super.migrate();
    }

    @Override
    Either<List<Edge>, TitanOperationStatus> getElementsToMigrate() {
        LOGGER.debug("fetching user states edges from old graph");
        return genericDao.getAll(NodeTypeEnum.User, UserData.class)
                         .left().bind(this::getEdgesForUsers);
    }

    @Override
    Either<Edge, TitanOperationStatus> getElementFromNewGraph(Edge edge) {
        LOGGER.debug("finding user state edge in new graph");
        Vertex outVertex = edge.outVertex();
        String outVertexUIDKey = getVertexUniqueId(outVertex);
        String outVertexUIDValue = outVertex.property(outVertexUIDKey).value().toString();

        Vertex inVertex = edge.inVertex();
        String inVertexUIDKey = getVertexUniqueId(inVertex);
        String inVertexUIDValue = inVertex.property(inVertexUIDKey).value().toString();

        return genericDaoMigration.getEdgeByVerticies(outVertexUIDKey, outVertexUIDValue, inVertexUIDKey, inVertexUIDValue, edge.label());
    }

    @Override
    boolean save(Edge userState) {
        Either<InOutVertices, TitanOperationStatus> titanVertices = findEdgeInOutVerticesInNewGraph(userState);
        return titanVertices.either(inOutVertices -> saveUserState(inOutVertices, userState),
                                    err ->  handleError(String.format("could not find user edge %s in vertx. error: %s", userState.label(), err.name())));
    }

    private boolean saveUserState(InOutVertices inOutVertices, Edge userState) {
        return genericDaoMigration.copyEdge(inOutVertices.getOutVertex(), inOutVertices.getInVertex(), userState)
                .either(edge -> true,
                        err -> handleError(String.format("failed to save user state edge %s. reason: %s", userState.label(), err.name())));
    }

    @Override
    TitanOperationStatus getNotFoundErrorStatus() {
        return TitanOperationStatus.NOT_FOUND;
    }

//    private boolean removeMigratingUserStates() {
//        Either<UserData, TitanOperationStatus> migratingUser = genericDaoMigration.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), MIGRATING_USER_ID, UserData.class);
//        return migratingUser.either(user -> deleteAllEdges(user, Direction.OUT),
//                                    errorStatus -> MigrationUtils.handleError(MigrationMsg.FAILED_TO_RETRIEVE_MIGRATION_USER.getMessage(MIGRATING_USER_ID, errorStatus.name())));
//    }

    private Either<List<Edge>, TitanOperationStatus> getEdgesForUsers(List<UserData> users) {
        List<Edge> edges = new ArrayList<>();
        for (UserData user : users) {
            Either<List<Edge>, TitanOperationStatus> edgesForNode = genericDao.getEdgesForNode(user, Direction.OUT);
            if (edgesForNode.isRight()) {
                TitanOperationStatus errorStatus = edgesForNode.right().value();
                LOGGER.error(MigrationMsg.FAILED_TO_RETRIEVE_USER_STATES.getMessage(user.getEmail(), errorStatus.name()));
                return Either.right(errorStatus);
            }
            edges.addAll(edgesForNode.left().value());
        }
        return Either.left(ignoreProductEdges(edges));
    }

    private List<Edge> ignoreProductEdges(List<Edge> edges) {
        return edges.stream().filter(edge -> !isInEdgeOfProductType(edge.inVertex())).collect(Collectors.toList());
    }

    private boolean isInEdgeOfProductType(Vertex inVertex) {
        Property<Object> nodeLabelProperty = inVertex.property(GraphPropertiesDictionary.LABEL.getProperty());
        return nodeLabelProperty != null && nodeLabelProperty.value().equals(NodeTypeEnum.Product.getName());
    }

    private String getVertexUniqueId(Vertex vertex) {
        String nodeLabel = vertex.property(GraphPropertiesDictionary.LABEL.getProperty()).value().toString();
        return UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.getByName(nodeLabel));
    }

    private Either<InOutVertices, TitanOperationStatus> findEdgeInOutVerticesInNewGraph(Edge userEdge) {
        Either<TitanVertex, TitanOperationStatus> outVertex = getMigratedVertexByOldVertex(userEdge.outVertex());
        Either<TitanVertex, TitanOperationStatus> inVertex = getMigratedVertexByOldVertex(userEdge.inVertex());
        return Either.sequenceLeft(list(outVertex, inVertex)).left().map(InOutVertices::new);
    }

    private Either<TitanVertex, TitanOperationStatus> getMigratedVertexByOldVertex(Vertex vertex) {
        String vertexUniqueId = getVertexUniqueId(vertex);
        LOGGER.debug(String.format("fetching vertex %s from new graph", vertexUniqueId));
        return genericDaoMigration.getVertexByProperty(vertexUniqueId, vertex.property(vertexUniqueId).value())
                                   .right().map(err -> handleError(err, String.format("could not find vertex %s in new graph.", vertexUniqueId)))  ;
    }

//    private boolean deleteAllEdges(UserData userData, Direction direction) {
//        Either<List<Edge>, TitanOperationStatus> edgesForNode = genericDaoMigration.getEdgesForNode(userData, direction);
//        if (edgesForNode.isRight()) {
//            LOGGER.error(MigrationMsg.FAILED_TO_RETRIEVE_MIGRATION_USER_STATES.getMessage(MIGRATING_USER_ID, edgesForNode.right().value().name()));
//            return false;
//        }
//        edgesForNode.left().value().forEach(Edge::remove);
//        return true;
//    }

    private class InOutVertices {
        private TitanVertex outVertex;
        private TitanVertex inVertex;

        InOutVertices(fj.data.List<TitanVertex> inOutVertices) {
            outVertex = inOutVertices.index(OUT_VERTEX_INDEX);
            inVertex = inOutVertices.index(IN_VERTEX_INDEX);
        }

        TitanVertex getOutVertex() {
            return outVertex;
        }

        TitanVertex getInVertex() {
            return inVertex;
        }

    }

}
