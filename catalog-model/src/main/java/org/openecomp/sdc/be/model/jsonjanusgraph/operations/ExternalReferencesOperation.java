package org.openecomp.sdc.be.model.jsonjanusgraph.operations;


import fj.data.Either;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.elements.MapComponentInstanceExternalRefs;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.IdMapper;
import org.openecomp.sdc.be.model.operations.impl.OperationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Collections.emptyMap;

/**
 * Created by yavivi on 26/01/2018.
 */
@Component
public class ExternalReferencesOperation extends BaseOperation {

    @Autowired
    private IdMapper idMapper;

    @Autowired
    private OperationUtils operationUtils;


    /**
     * Constructor
     */
    public ExternalReferencesOperation(JanusGraphDao janusGraphDao, NodeTypeOperation nto, TopologyTemplateOperation tto, IdMapper idMapper){
        this.janusGraphDao = janusGraphDao;
        this.topologyTemplateOperation = tto;
        this.nodeTypeOperation = nto;
        this.idMapper = idMapper;
    }

    public Either<String, ActionStatus> addExternalReferenceWithCommit(String serviceUuid, String componentInstanceName, String objectType, String reference) {
        Either<String, ActionStatus> addResult = addExternalReference(serviceUuid, componentInstanceName, objectType, reference);
        janusGraphDao.commit();
        return addResult;
    }

    public Either<String, ActionStatus> deleteExternalReferenceWithCommit(String serviceUuid, String componentInstanceName, String objectType, String reference) {
        Either<String, ActionStatus> result = deleteExternalReference(serviceUuid, componentInstanceName, objectType, reference);
        janusGraphDao.commit();
        return result;
    }

    public Either<String, ActionStatus> updateExternalReferenceWithCommit(String serviceVertexUuid, String componentInstanceName, String objectType, String oldRef, String newRef) {
        Either<String, ActionStatus> updateResult = updateExternalReference(serviceVertexUuid, componentInstanceName, objectType, oldRef, newRef);
        janusGraphDao.commit();
        return updateResult;
    }

    public Either<String, ActionStatus> addExternalReference(String assetUuid, String componentInstanceName, String objectType, String reference) {

        //Get Container vertex
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        GraphVertex serviceVertex = vertexById.left().value();

        final String compInstanceUniqueId = idMapper.mapComponentNameToUniqueId(componentInstanceName, serviceVertex);
        if (compInstanceUniqueId == null) {
            return Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
        }

        //Get the external references map vertex
        final Either<GraphVertex, JanusGraphOperationStatus> dataVertexResult = getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);

        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;

        //instanceId -> externalRefsMap
        Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData;
        if (externalRefsVertex == null) {
            //External Refs vertex does not exist, create its map.
            externalReferencesFullData = new HashMap<>();
            externalReferencesFullData.put(compInstanceUniqueId, new MapComponentInstanceExternalRefs());
        } else {
            externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            externalReferencesFullData.computeIfAbsent(compInstanceUniqueId, k -> new MapComponentInstanceExternalRefs());
        }

        boolean isAdded = addExternalRef(externalReferencesFullData, compInstanceUniqueId, objectType, reference);
        updateFullToscaData(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, VertexTypeEnum.EXTERNAL_REF, externalReferencesFullData);

        return isAdded ? Either.left(reference) : Either.right(ActionStatus.EXT_REF_ALREADY_EXIST);
    }

    public Either<String, ActionStatus> deleteExternalReference(String assetUuid, String componentInstanceName, String objectType, String reference){
        //Get Service vertex
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }
        GraphVertex serviceVertex = vertexById.left().value();

        final String compInstanceUniqueId = idMapper.mapComponentNameToUniqueId(componentInstanceName, serviceVertex);
        if (compInstanceUniqueId == null) {
            return Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
        }

        //Get the external references map vertex
        final Either<GraphVertex, JanusGraphOperationStatus> dataVertexResult = getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);

        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;
        boolean refDeleted = false;
        if (externalRefsVertex != null) {
            Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            if (externalReferencesFullData != null) {
                refDeleted = deleteExternalRef(externalReferencesFullData, compInstanceUniqueId, objectType, reference);
                updateFullToscaData(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, VertexTypeEnum.EXTERNAL_REF, externalReferencesFullData);
            }
        }

        if (refDeleted) {
            return Either.left(reference);
        } else {
            return Either.right(ActionStatus.EXT_REF_NOT_FOUND);
        }
    }

    public Either<String, ActionStatus> updateExternalReference(String assetUuid, String componentInstanceName, String objectType, String oldRef, String newRef) {
        //Get Service vertex
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        GraphVertex serviceVertex = vertexById.left().value();

        //Map instance_name -> uuid
        final String compInstanceUniqueId = idMapper.mapComponentNameToUniqueId(componentInstanceName, serviceVertex);
        if (compInstanceUniqueId == null) {
            return Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
        }

        //Get the external references map vertex
        final Either<GraphVertex, JanusGraphOperationStatus> dataVertexResult = getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);

        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;
        boolean refReplaced = false;
        if (externalRefsVertex != null) {
            Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            if (externalReferencesFullData != null) {
                refReplaced = updateExternalRef(externalReferencesFullData, compInstanceUniqueId, objectType, oldRef, newRef);
                updateFullToscaData(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, VertexTypeEnum.EXTERNAL_REF, externalReferencesFullData);
            }
        }
        if (refReplaced) {
            return Either.left(newRef);
        } else {
            return Either.right(ActionStatus.EXT_REF_NOT_FOUND);
        }
    }

    public Either<Map<String, List<String>>, ActionStatus> getExternalReferences(String assetUuid, String objectType) {
        //Get Service vertex
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        GraphVertex serviceVertex = vertexById.left().value();

        Map<String, List<String>> result = new HashMap();

        //Get the external references map vertex
        final Either<GraphVertex, JanusGraphOperationStatus> dataVertexResult = getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);
        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;
        if (externalRefsVertex != null) {
            Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            if (externalReferencesFullData != null) {
                externalReferencesFullData.entrySet().forEach(
                        s -> {
                            List<String> externalRefsByObjectType = externalReferencesFullData.get(s.getKey()).getExternalRefsByObjectType(objectType);
                            List<String> refList = externalRefsByObjectType == null ? new ArrayList<>() : externalRefsByObjectType;
                            String key = idMapper.mapUniqueIdToComponentNameTo(s.getKey(), serviceVertex);
                            result.put(key, refList);
                        }
                );
                return Either.left(result);
            }
        }
        //No external References Node found on this asset
        return Either.left(new HashMap<>());
    }

    public void addAllExternalReferences(String containerUniqueId,
                                         String compInstanceUniqueId,
                                         Map<String, List<String>> instanceExternalReferences) {

        GraphVertex serviceVertex = janusGraphDao.getVertexById(containerUniqueId)
                .left()
                .on(operationUtils::onJanusGraphOperationFailure);
        Either<GraphVertex, JanusGraphOperationStatus> dataVertex = getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);
        Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData;
        if (dataVertex.isLeft()) {
            externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) dataVertex.left().value().getJson();
        } else {
            externalReferencesFullData = new HashMap<>();
        }
        externalReferencesFullData.put(compInstanceUniqueId, new MapComponentInstanceExternalRefs(instanceExternalReferences));
        updateFullToscaData(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, VertexTypeEnum.EXTERNAL_REF, externalReferencesFullData);
    }

    public Map<String, List<String>> getAllExternalReferences(String containerUniqueId,
                                                              String compInstanceUniqueId) {
        GraphVertex serviceVertex = janusGraphDao.getVertexById(containerUniqueId)
            .left()
            .on(operationUtils::onJanusGraphOperationFailure);

        Either<GraphVertex, JanusGraphOperationStatus> dataVertex = getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);
        if (dataVertex.isRight()) {
            return new HashMap<>();
        }
        GraphVertex externalRefsVertex = dataVertex.left().value();
        Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = externalRefsVertex == null ? null : (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
        if (externalReferencesFullData != null) {
            return externalReferencesFullData
                    .getOrDefault(compInstanceUniqueId, new MapComponentInstanceExternalRefs())
                    .getComponentInstanceExternalRefs();
        }
        return emptyMap();
    }

    public Either<List<String>, ActionStatus> getExternalReferences(String assetUuid, String componentInstanceName, String objectType) {
        //Get Service vertex
        Either<GraphVertex, JanusGraphOperationStatus> vertexById = janusGraphDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        GraphVertex serviceVertex = vertexById.left().value();
        final String compInstanceUniqueId = idMapper.mapComponentNameToUniqueId(componentInstanceName, serviceVertex);
        if (compInstanceUniqueId == null) {
            return Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
        }

        //Get the external references map vertex
        final Either<GraphVertex, JanusGraphOperationStatus> dataVertexResult = getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);

        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;
        if (externalRefsVertex != null) {
            Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            if (externalReferencesFullData != null) {
                return Either.left(getExternalReferencesByObjectId(externalReferencesFullData, compInstanceUniqueId, objectType));
            }
        }

        //No external References Node found on this asset
        return Either.left(new LinkedList());
    }

    public IdMapper getIdMapper() {
        return idMapper;
    }

    public void setIdMapper(IdMapper idMapper) {
        this.idMapper = idMapper;
    }

    private List<String> getExternalReferencesByObjectId(Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData, String componentInstanceId, String objectType) {
        MapComponentInstanceExternalRefs externalRefsMap = externalReferencesFullData.get(componentInstanceId);
        List<String> externalRefsByObjectType = externalRefsMap.getExternalRefsByObjectType(objectType);
        return externalRefsByObjectType != null ? externalRefsByObjectType : new LinkedList<>();
    }

    private boolean updateExternalRef(Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData, String componentInstanceId, String objectType, String oldRef, String newRef) {
        MapComponentInstanceExternalRefs externalRefsMap = externalReferencesFullData.get(componentInstanceId);
        return externalRefsMap.replaceExternalRef(objectType, oldRef, newRef);
    }

    private boolean deleteExternalRef(Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData, String componentInstanceId, String objectType, String reference) {
        MapComponentInstanceExternalRefs externalRefsMap = externalReferencesFullData.get(componentInstanceId);
        return externalRefsMap.deleteExternalRef(objectType, reference);
    }

    private boolean addExternalRef(Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData, String componentInstanceId, String objectType, String reference) {
        MapComponentInstanceExternalRefs externalRefsMap = externalReferencesFullData.get(componentInstanceId);
        return externalRefsMap.addExternalRef(objectType, reference);
    }
}
