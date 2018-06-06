package org.openecomp.sdc.be.model.jsontitan.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.MapComponentInstanceExternalRefs;
import org.openecomp.sdc.be.model.jsontitan.utils.IdMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

/**
 * Created by yavivi on 26/01/2018.
 */
@Component
public class ExternalReferencesOperation extends BaseOperation {

    private static final Logger log = LoggerFactory.getLogger(ExternalReferencesOperation.class);

    public IdMapper getIdMapper() {
        return idMapper;
    }

    public void setIdMapper(IdMapper idMapper) {
        this.idMapper = idMapper;
    }

    @Autowired
    protected IdMapper idMapper;

    /**
     * Constructor
     */
    public ExternalReferencesOperation(TitanDao titanDao, NodeTypeOperation nto, TopologyTemplateOperation tto, IdMapper idMapper){
        this.titanDao = titanDao;
        this.topologyTemplateOperation = tto;
        this.nodeTypeOperation = nto;
        this.idMapper = idMapper;
    }

    public Either<String, ActionStatus> addExternalReferenceWithCommit(String serviceUuid, String componentInstanceName, String objectType, String reference) {
        Either<String, ActionStatus> addResult = this.addExternalReference(serviceUuid, componentInstanceName, objectType, reference);
        this.titanDao.commit();
        return addResult;
    }

    public Either<String, ActionStatus> deleteExternalReferenceWithCommit(String serviceUuid, String componentInstanceName, String objectType, String reference) {
        Either<String, ActionStatus> result = this.deleteExternalReference(serviceUuid, componentInstanceName, objectType, reference);
        this.titanDao.commit();
        return result;
    }

    public Either<String, ActionStatus> updateExternalReferenceWithCommit(String serviceVertexUuid, String componentInstanceName, String objectType, String oldRef, String newRef) {
        Either<String, ActionStatus> updateResult = this.updateExternalReference(serviceVertexUuid, componentInstanceName, objectType, oldRef, newRef);
        this.titanDao.commit();
        return updateResult;
    }

    public Either<String, ActionStatus> addExternalReference(String assetUuid, String componentInstanceName, String objectType, String reference) {

        //Get Service vertex
        Either<GraphVertex, TitanOperationStatus> vertexById = this.titanDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        GraphVertex serviceVertex = vertexById.left().value();

        final String compInstanceUniqueId = idMapper.mapComponentNameToUniqueId(componentInstanceName, serviceVertex);
        if (compInstanceUniqueId == null) {
            return Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
        }

        //Get the external references map vertex
        final Either<GraphVertex, TitanOperationStatus> dataVertexResult = this.getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);

        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;

        //instanceId -> externalRefsMap
        Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = null;
        if (externalRefsVertex == null) {
            //External Refs vertext does not exist, create its map.
            externalReferencesFullData = new HashMap<String, MapComponentInstanceExternalRefs>() {
                {
                    MapComponentInstanceExternalRefs externalRefsMap = new MapComponentInstanceExternalRefs();
                    put(compInstanceUniqueId, externalRefsMap);
                }
            };
        } else {
            externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            if (externalReferencesFullData.get(compInstanceUniqueId) == null){
                externalReferencesFullData.put(compInstanceUniqueId, new MapComponentInstanceExternalRefs());
            }
        }

        boolean isAdded = this.addExternalRef(externalReferencesFullData, compInstanceUniqueId, objectType, reference);
        this.updateFullToscaData(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, VertexTypeEnum.EXTERNAL_REF, externalReferencesFullData);

        return isAdded ? Either.left(reference) : Either.right(ActionStatus.EXT_REF_ALREADY_EXIST);
    }

    public Either<String, ActionStatus> deleteExternalReference(String assetUuid, String componentInstanceName, String objectType, String reference){
        //Get Service vertex
        Either<GraphVertex, TitanOperationStatus> vertexById = this.titanDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }
        GraphVertex serviceVertex = vertexById.left().value();

        final String compInstanceUniqueId = idMapper.mapComponentNameToUniqueId(componentInstanceName, serviceVertex);
        if (compInstanceUniqueId == null) {
            return Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
        }

        //Get the external references map vertex
        final Either<GraphVertex, TitanOperationStatus> dataVertexResult = this.getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);

        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;
        boolean refDeleted = false;
        if (externalRefsVertex != null) {
            Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            if (externalReferencesFullData != null) {
                refDeleted = this.deleteExternalRef(externalReferencesFullData, compInstanceUniqueId, objectType, reference);
                this.updateFullToscaData(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, VertexTypeEnum.EXTERNAL_REF, externalReferencesFullData); //@ TODO if ref deleted
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
        Either<GraphVertex, TitanOperationStatus> vertexById = this.titanDao.getVertexById(assetUuid);
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
        final Either<GraphVertex, TitanOperationStatus> dataVertexResult = this.getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);

        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;
        boolean refReplaced = false;
        if (externalRefsVertex != null) {
            Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            if (externalReferencesFullData != null) {
                refReplaced = this.updateExternalRef(externalReferencesFullData, compInstanceUniqueId, objectType, oldRef, newRef);
                this.updateFullToscaData(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS, VertexTypeEnum.EXTERNAL_REF, externalReferencesFullData);
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
        Either<GraphVertex, TitanOperationStatus> vertexById = this.titanDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        GraphVertex serviceVertex = vertexById.left().value();

        Map<String, List<String>> result = new HashMap();

        //Get the external references map vertex
        final Either<GraphVertex, TitanOperationStatus> dataVertexResult = this.getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);
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

    public Either<List<String>, ActionStatus> getExternalReferences(String assetUuid, String componentInstanceName, String objectType) {
        //Get Service vertex
        Either<GraphVertex, TitanOperationStatus> vertexById = this.titanDao.getVertexById(assetUuid);
        if (vertexById.isRight()){
            return Either.right(ActionStatus.RESOURCE_NOT_FOUND);
        }

        GraphVertex serviceVertex = vertexById.left().value();
        final String compInstanceUniqueId = idMapper.mapComponentNameToUniqueId(componentInstanceName, serviceVertex);
        if (compInstanceUniqueId == null) {
            return Either.right(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND);
        }

        //Get the external references map vertex
        final Either<GraphVertex, TitanOperationStatus> dataVertexResult = this.getDataVertex(serviceVertex, EdgeLabelEnum.EXTERNAL_REFS);

        //Check whether data vertex found
        GraphVertex externalRefsVertex = dataVertexResult.isLeft() ? dataVertexResult.left().value() : null;
        if (externalRefsVertex != null) {
            Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData = (Map<String, MapComponentInstanceExternalRefs>) externalRefsVertex.getJson();
            if (externalReferencesFullData != null) {
                return Either.left(this.getExternalReferencesByObjectId(externalReferencesFullData, compInstanceUniqueId, objectType));
            }
        }

        //No external References Node found on this asset
        return Either.left(new LinkedList());
    }

    private List<String> getExternalReferencesByObjectId(Map<String, MapComponentInstanceExternalRefs> externalReferencesFullData, String componentInstanceId, String objectType) {
        MapComponentInstanceExternalRefs externalRefsMap = externalReferencesFullData.get(componentInstanceId);
        List<String> externalRefsByObjectType = externalRefsMap.getExternalRefsByObjectType(objectType);
        return externalRefsByObjectType != null ? externalRefsByObjectType : new LinkedList<String>();
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
