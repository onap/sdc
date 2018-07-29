package org.openecomp.sdc.be.model.jsontitan.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.model.jsontitan.operations.ArchiveOperation.Action.ARCHIVE;
import static org.openecomp.sdc.be.model.jsontitan.operations.ArchiveOperation.Action.RESTORE;

/**
 * Created by yavivi on 25/03/2018.
 */
@Component
public class ArchiveOperation extends BaseOperation {

    private static final Logger log = Logger.getLogger(ArchiveOperation.class.getName());

    @Autowired
    private IGraphLockOperation graphLockOperation;

    public enum Action {
        ARCHIVE, RESTORE;
    }

    public ArchiveOperation(TitanDao titanDao, IGraphLockOperation graphLockOperation){
        this.titanDao = titanDao;
        this.graphLockOperation = graphLockOperation;
    }

    public Either<List<String>, ActionStatus> archiveComponent(String componentId) {
        final Either<GraphVertex, TitanOperationStatus> vertexResult = this.titanDao.getVertexById(componentId);
        if (vertexResult.isLeft()){
            return doAction(ARCHIVE, vertexResult.left().value());
        } else {
            return Either.right(onError(ARCHIVE.name(), componentId, vertexResult.right().value()));
        }
    }

    public Either<List<String>, ActionStatus> restoreComponent(String componentId) {
        final Either<GraphVertex, TitanOperationStatus> vertexResult = this.titanDao.getVertexById(componentId);
        if (vertexResult.isLeft()){
            return doAction(RESTORE, vertexResult.left().value());
        } else {
            return Either.right(onError(RESTORE.name(), componentId, vertexResult.right().value()));
        }
    }

    public ActionStatus onVspRestored(String csarId){
        return onVspStateChanged(RESTORE, csarId);
    }

    public ActionStatus onVspArchived(String csarId){
        return onVspStateChanged(ARCHIVE, csarId);
    }

    private ActionStatus onVspStateChanged(Action action, String csarId) {
        Map<GraphPropertyEnum, Object> props = new HashMap<>();
        props.put(GraphPropertyEnum.CSAR_UUID, csarId);
        Either<List<GraphVertex>, TitanOperationStatus> vfsE = titanDao.getByCriteria(VertexTypeEnum.TOPOLOGY_TEMPLATE, props);
        return vfsE.either(vList -> setVspArchived(action, vList), s -> onError("VSP_"+action.name(), csarId, s));
    }

    private ActionStatus setVspArchived(Action action, List<GraphVertex> vList) {
        if (!vList.isEmpty()) {
            //Find & Lock the highest version component
            GraphVertex highestVersion = this.getHighestVersionFrom(vList.get(0));
            StorageOperationStatus lockStatus = this.graphLockOperation.lockComponent(highestVersion.getUniqueId(), highestVersion.getType().getNodeType());
            if (lockStatus != StorageOperationStatus.OK){
                return onError(action.name(), highestVersion.getUniqueId(), TitanOperationStatus.ALREADY_LOCKED);
            }

            try {
                //Set isVspArchived flag
                for (GraphVertex v : vList) {
                    boolean val = action == ARCHIVE ? true : false;
                    v.setJsonMetadataField(JsonPresentationFields.IS_VSP_ARCHIVED, val);
                    v.addMetadataProperty(GraphPropertyEnum.IS_VSP_ARCHIVED, val);
                    titanDao.updateVertex(v);
                }
                return commitAndCheck("VSP_"+action.name(), vList.toString());
            } finally {
                this.graphLockOperation.unlockComponent(highestVersion.getUniqueId(), highestVersion.getType().getNodeType());
            }

        }
        return ActionStatus.OK;
    }

    public List<String> setArchivedOriginsFlagInComponentInstances(GraphVertex compositionService) {
        List<String> ciUidsWithArchivedOrigins = new LinkedList();
        Either<List<GraphVertex>, TitanOperationStatus> instanceOfVerticesE = titanDao.getChildrenVertecies(compositionService, EdgeLabelEnum.INSTANCE_OF, JsonParseFlagEnum.NoParse);
        Either<List<GraphVertex>, TitanOperationStatus> proxyOfVerticesE = titanDao.getChildrenVertecies(compositionService, EdgeLabelEnum.PROXY_OF, JsonParseFlagEnum.NoParse);

        List<GraphVertex> all = new LinkedList<>();
        if (instanceOfVerticesE.isLeft()){
            all.addAll(instanceOfVerticesE.left().value());
        }
        if (proxyOfVerticesE.isLeft()){
            all.addAll(proxyOfVerticesE.left().value());
        }

        List<GraphVertex> archivedOrigins = all.stream().filter(v -> Boolean.TRUE.equals(v.getMetadataProperty(GraphPropertyEnum.IS_ARCHIVED))).collect(Collectors.toList());
        List<String> archivedOriginsUids = archivedOrigins.stream().map(GraphVertex::getUniqueId).collect(Collectors.toList());

        Map<String, CompositionDataDefinition> compositionsJson = (Map<String, CompositionDataDefinition>) compositionService.getJson();

        if (compositionsJson != null) {
            CompositionDataDefinition composition = compositionsJson.get(JsonConstantKeysEnum.COMPOSITION.getValue());
            if (composition != null) {

                //Get all component instances from composition
                Map<String, ComponentInstanceDataDefinition> componentInstances = composition.getComponentInstances();

                //Extract component instances uids that has archived origins
                ciUidsWithArchivedOrigins = componentInstances.
                        values().
                        stream().
                        //filter CIs whose origins are marked as archived (componentUid is in archivedOriginsUids) the second condition handles the PROXY_OF case)
                        filter(ci -> archivedOriginsUids.contains(ci.getComponentUid()) || archivedOriginsUids.contains(ci.getToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_UID))).
                        map(ComponentInstanceDataDefinition::getUniqueId).collect(Collectors.toList());

                //set archived origins flag
                componentInstances.
                        values().
                        stream().
                        filter(ci -> archivedOriginsUids.contains(ci.getComponentUid()) || archivedOriginsUids.contains(ci.getToscaPresentationValue(JsonPresentationFields.CI_SOURCE_MODEL_UID))).
                        forEach( ci -> ci.setOriginArchived(true));

            }
        }

        return ciUidsWithArchivedOrigins;
    }

    private Either<List<String>, ActionStatus> doAction(Action action, GraphVertex componentVertex){

        GraphVertex highestVersion = this.getHighestVersionFrom(componentVertex);

        if (action.equals(ARCHIVE) && isInCheckoutState(highestVersion)) {
            return Either.right(ActionStatus.INVALID_SERVICE_STATE);
        }

        //Lock the Highest Version
        StorageOperationStatus lockStatus = this.graphLockOperation.lockComponent(highestVersion.getUniqueId(), highestVersion.getType().getNodeType());
        if (lockStatus != StorageOperationStatus.OK){
            return Either.right(onError(action.name(), componentVertex.getUniqueId(), TitanOperationStatus.ALREADY_LOCKED));
        }

        //Refetch latest version with full parsing
        highestVersion = this.titanDao.getVertexById(highestVersion.getUniqueId(), JsonParseFlagEnum.ParseAll).left().value();

        try {
            //Get Catalog and Archive Roots
            GraphVertex catalogRoot = titanDao.getVertexByLabel(VertexTypeEnum.CATALOG_ROOT).left().value();
            GraphVertex archiveRoot = titanDao.getVertexByLabel(VertexTypeEnum.ARCHIVE_ROOT).left().value();

            if (action == ARCHIVE) {
                archiveEdges(catalogRoot, archiveRoot, highestVersion);
            } else if (action == RESTORE) {
                restoreEdges(catalogRoot, archiveRoot, highestVersion);
            }
            setPropertiesByAction(highestVersion, action);
            titanDao.updateVertex(highestVersion);

            List<String> affectedComponentIds = handleParents(highestVersion, catalogRoot, archiveRoot, action);
            ActionStatus sc = commitAndCheck(action.name(), highestVersion.getUniqueId());
            return  sc == ActionStatus.OK ? Either.left(affectedComponentIds) : Either.right(sc);
        } finally {
            this.graphLockOperation.unlockComponent(highestVersion.getUniqueId(), highestVersion.getType().getNodeType());
        }
    }

    private ActionStatus commitAndCheck(String action, String componentId) {
        TitanOperationStatus status = titanDao.commit();
        if (!status.equals(TitanOperationStatus.OK)){
            return onError(action, componentId, status);
        }
        return ActionStatus.OK;
    }

    private boolean isInCheckoutState(GraphVertex v) {
        if (LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name().equals(v.getMetadataProperty(GraphPropertyEnum.STATE))){
            return true;
        }
        return false;
    }

    /**
     * Walks on children until highest version is reached
     * @param v
     * @return
     */
    private GraphVertex getHighestVersionFrom(GraphVertex v) {
        Either<GraphVertex, TitanOperationStatus> childVertexE = titanDao.getChildVertex(v, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse);
        GraphVertex highestVersionVertex = v;

        while (childVertexE.isLeft()) {
            highestVersionVertex = childVertexE.left().value();
            childVertexE = titanDao.getChildVertex(highestVersionVertex, EdgeLabelEnum.VERSION, JsonParseFlagEnum.NoParse);
        }
        return highestVersionVertex;
    }

    private boolean isHighestVersion(GraphVertex v){
        Boolean highest = (Boolean) v.getMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION);
        return highest != null && highest;
    }

    private List<String> handleParents(GraphVertex v, GraphVertex catalogRoot, GraphVertex archiveRoot, Action action) {
        Either<GraphVertex, TitanOperationStatus> parentVertexE = titanDao.getParentVertex(v, EdgeLabelEnum.VERSION, JsonParseFlagEnum.ParseAll);
        List<String> affectedCompIds = new ArrayList();
        affectedCompIds.add(v.getUniqueId());

        while (parentVertexE.isLeft()){
            GraphVertex cv = parentVertexE.left().value();
            affectedCompIds.add(cv.getUniqueId());
            boolean isHighestVersion = isHighestVersion(cv);
            if (isHighestVersion){
                if (action == ARCHIVE) {
                    archiveEdges(catalogRoot, archiveRoot, cv);
                } else {
                    restoreEdges(catalogRoot, archiveRoot, cv);
                }
            }
            setPropertiesByAction(cv, action);
            titanDao.updateVertex(cv);
            parentVertexE = titanDao.getParentVertex(cv, EdgeLabelEnum.VERSION, JsonParseFlagEnum.ParseAll);
        }
        return affectedCompIds;
    }

    private void archiveEdges(GraphVertex catalogRoot, GraphVertex archiveRoot, GraphVertex v) {
        titanDao.deleteAllEdges(catalogRoot, v, EdgeLabelEnum.CATALOG_ELEMENT);
        titanDao.createEdge(archiveRoot, v, EdgeLabelEnum.ARCHIVE_ELEMENT, null);
        setPropertiesByAction(v, ARCHIVE);
    }

    private void restoreEdges(GraphVertex catalogRoot, GraphVertex archiveRoot, GraphVertex v) {
        titanDao.deleteAllEdges(archiveRoot, v, EdgeLabelEnum.ARCHIVE_ELEMENT);
        titanDao.createEdge(catalogRoot, v, EdgeLabelEnum.CATALOG_ELEMENT, null);
        setPropertiesByAction(v, RESTORE);
    }

    private void setPropertiesByAction(GraphVertex v, Action action) {
        long now = System.currentTimeMillis();

        boolean isArchived = action == ARCHIVE ? true : false;
        v.addMetadataProperty(GraphPropertyEnum.IS_ARCHIVED, isArchived);
        v.addMetadataProperty(GraphPropertyEnum.ARCHIVE_TIME, now);
        v.setJsonMetadataField(JsonPresentationFields.IS_ARCHIVED, isArchived);
        v.setJsonMetadataField(JsonPresentationFields.ARCHIVE_TIME, now);
    }

    private ActionStatus onError(String action, String componentId, TitanOperationStatus s) {
        ActionStatus ret = ActionStatus.GENERAL_ERROR;
        if (s == TitanOperationStatus.NOT_FOUND){
            ret = ActionStatus.RESOURCE_NOT_FOUND;
        } else if (s == TitanOperationStatus.ALREADY_LOCKED) {
            ret = ActionStatus.COMPONENT_IN_USE;
        }
        String retCodeVal = ret.name();
        log.error("error occurred when trying to {} {}. Return code is: {}", action, componentId, retCodeVal);
        return ret;
    }
}
