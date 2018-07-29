package org.openecomp.sdc.be.model.jsontitan.operations;

import com.thinkaurelius.titan.core.TitanVertex;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgePropertyEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ComponentDependency;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UpgradeOperation extends BaseOperation {
    private static final Logger log = Logger.getLogger(UpgradeOperation.class.getName());

    public Either<List<ComponentDependency>, StorageOperationStatus> getComponentDependencies(String componentId) {
        Either<GraphVertex, TitanOperationStatus> vertexById = titanDao.getVertexById(componentId);
        if (vertexById.isRight()) {
            log.debug("Failed to fetch vertex with id {} error {}", componentId, vertexById.right().value());
            return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(vertexById.right().value()));
        }
        List<ComponentDependency> dependencies = new ArrayList<>();

        GraphVertex vertex = vertexById.left().value();

        StorageOperationStatus status = fillDependenciesByVertex(componentId, dependencies, vertex);
        if (status != StorageOperationStatus.OK) {
            return Either.right(status);
        }

        GraphVertex vertexToStart = vertex;
        Function<GraphVertex, Either<GraphVertex, TitanOperationStatus>> getNextElement = vertexP -> titanDao.getParentVertex(vertexP, EdgeLabelEnum.VERSION, JsonParseFlagEnum.ParseAll);
        status = handleVersionChain(componentId, dependencies, vertex, getNextElement);
        if (status != StorageOperationStatus.OK) {
            return Either.right(status);
        }
        vertex = vertexToStart;
        getNextElement = vertexP -> titanDao.getChildVertex(vertexP, EdgeLabelEnum.VERSION, JsonParseFlagEnum.ParseAll);
        status = handleVersionChain(componentId, dependencies, vertex, getNextElement);

        return status == StorageOperationStatus.OK ? Either.left(dependencies) : Either.right(status);
    }

    private StorageOperationStatus handleVersionChain(String componentId, List<ComponentDependency> dependencies, GraphVertex vertexToStart, Function<GraphVertex, Either<GraphVertex, TitanOperationStatus>> getNextElement) {

        StorageOperationStatus status;
        boolean nextInChain = true;
        GraphVertex vertex = vertexToStart;
        Either<GraphVertex, TitanOperationStatus> nextInChainV;
        while (nextInChain) {
            nextInChainV = getNextElement.apply(vertex);
            if (nextInChainV.isRight()) {
                nextInChain = false;
            } else {
                vertex = nextInChainV.left().value();
                status = fillDependenciesByVertex(componentId, dependencies, vertex);
                if (status != StorageOperationStatus.OK) {
                    return status;
                }
            }
        }
        return StorageOperationStatus.OK;
    }

    private StorageOperationStatus fillDependenciesByVertex(String componentId, List<ComponentDependency> dependencies, GraphVertex vertex) {
        StorageOperationStatus status = StorageOperationStatus.OK;
        if ( needToAddToDepenedency(vertex) ) {
            ComponentDependency dependency = fillDataFromVertex(vertex, null, null);

            List<EdgeLabelEnum> dependList = Arrays.asList(EdgeLabelEnum.INSTANCE_OF, EdgeLabelEnum.PROXY_OF, EdgeLabelEnum.ALLOTTED_OF);
            for (EdgeLabelEnum label : dependList) {
                status = fillDependenciesByLabel(componentId, vertex, dependency, label);
                if (status != StorageOperationStatus.OK) {
                    log.debug("Failed to create dependencies for component {} and label {} status {}", componentId, label, status);
                    break;
                }
            }
            if (status == StorageOperationStatus.OK) {
                dependencies.add(dependency);
            }
        }
        return status;
    }
    private boolean needToAddToDepenedency(GraphVertex vertex){
        Boolean isDeleted = (Boolean) vertex.getMetadataProperty(GraphPropertyEnum.IS_DELETED);     
        Boolean isArchived = (Boolean) vertex.getMetadataProperty(GraphPropertyEnum.IS_ARCHIVED);
        return ( isDeleted == Boolean.TRUE || isArchived == Boolean.TRUE) ? false : true;
    }

    private StorageOperationStatus fillDependenciesByLabel(String componentId, GraphVertex vertex, ComponentDependency dependency, EdgeLabelEnum label) {
        Either<List<GraphVertex>, TitanOperationStatus> parentVertecies = titanDao.getParentVertecies(vertex, label, JsonParseFlagEnum.ParseAll);
        if (parentVertecies.isRight() && parentVertecies.right().value() != TitanOperationStatus.NOT_FOUND) {
            log.debug("Failed to fetch parent verticies by label INSTANCE_OF for vertex with id {} error {}", componentId, parentVertecies.right().value());
            return DaoStatusConverter.convertTitanStatusToStorageStatus(parentVertecies.right().value());
        }
        if (parentVertecies.isLeft()) {
            List<ComponentDependency> existIn = new ArrayList<>( );
            parentVertecies.left().value().forEach(v -> handleHighestVersion(vertex, label, existIn, v) );
            dependency.addDependencies(existIn);
        }
        return StorageOperationStatus.OK;
    }

    private void handleHighestVersion(GraphVertex vertexOrigin, EdgeLabelEnum label, List<ComponentDependency> exisIn, GraphVertex containerVertex) {
        Boolean isHighest = (Boolean) containerVertex.getMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION);
        if ( isHighest && needToAddToDepenedency(containerVertex) ) {  
            TitanVertex titanVertex = containerVertex.getVertex();
            Iterator<Edge> edges = titanVertex.edges(Direction.OUT, EdgeLabelEnum.VERSION.name());
            //verify that it is a last version - highest by version number
            if ( edges == null || !edges.hasNext() ){
                ComponentDependency container = fillDataFromVertex(containerVertex, vertexOrigin.getUniqueId(), label);
                boolean addToDependency = true;
                if (label == EdgeLabelEnum.ALLOTTED_OF) {
                    //in case of not full allotted chain not add to dependency list
                    addToDependency = findAllottedChain(containerVertex, container);
                }
                if ( addToDependency ){
                    exisIn.add(container);
                 }
            }
        }
    }

    private boolean findAllottedChain(GraphVertex vertex, ComponentDependency container) {
        Either<List<GraphVertex>, TitanOperationStatus> parentVertecies = titanDao.getParentVertecies(vertex, EdgeLabelEnum.INSTANCE_OF, JsonParseFlagEnum.ParseAll);
        if (parentVertecies.isLeft()) {
            List<ComponentDependency> existIn = new ArrayList<>();
            parentVertecies.left().value().forEach(v -> {
                Boolean isHighest = (Boolean) v.getMetadataProperty(GraphPropertyEnum.IS_HIGHEST_VERSION);
                if ( isHighest && needToAddToDepenedency(v) ) {
                   TitanVertex titanVertex = v.getVertex();
                   Iterator<Edge> edges = titanVertex.edges(Direction.OUT, EdgeLabelEnum.VERSION.name());
                   //verify that it is a last version - highest by version number
                   if ( edges == null || !edges.hasNext() ){
                       ComponentDependency parentContainer = fillDataFromVertex(v, vertex.getUniqueId(), EdgeLabelEnum.INSTANCE_OF);
                       existIn.add(parentContainer);
                   }
                }
            });
            if ( !existIn.isEmpty() ){
                container.setDependencies(existIn);
                return true;
            }
        }
        return false;
    }

    private ComponentDependency fillDataFromVertex(GraphVertex v, String originId, EdgeLabelEnum edgeLabel) {
        ComponentDependency container = new ComponentDependency();
        container.setName((String) v.getMetadataProperty(GraphPropertyEnum.NAME));
        container.setVersion((String) v.getMetadataProperty(GraphPropertyEnum.VERSION));
        container.setUniqueId(v.getUniqueId());
        container.setType((String) v.getMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE));
        container.setIcon((String) v.getJsonMetadataField(JsonPresentationFields.ICON));
        container.setState((String) v.getMetadataProperty(GraphPropertyEnum.STATE));

        if (edgeLabel == EdgeLabelEnum.PROXY_OF || edgeLabel == EdgeLabelEnum.ALLOTTED_OF) {
            findInstanceNames(v, originId, edgeLabel, container);
        }
        return container;
    }

    private void findInstanceNames(GraphVertex v, String originId, EdgeLabelEnum edgeLabel, ComponentDependency container) {
        Map<String, CompositionDataDefinition> jsonComposition = (Map<String, CompositionDataDefinition>) v.getJson();
        CompositionDataDefinition compositionDataDefinition = jsonComposition.get(JsonConstantKeysEnum.COMPOSITION.getValue());
        TitanVertex vertex = v.getVertex();
        Iterator<Edge> edges = vertex.edges(Direction.OUT, edgeLabel.name());
        while (edges != null && edges.hasNext()) {
            Edge edge = edges.next();
            TitanVertex inVertex = (TitanVertex) edge.inVertex();
            String id = (String) titanDao.getProperty(inVertex, GraphPropertyEnum.UNIQUE_ID.getProperty());
            if (id.equals(originId)) {
                List<String> instanceOnEdge = (List<String>) titanDao.getProperty(edge, EdgePropertyEnum.INSTANCES);
                Map<String, ComponentInstanceDataDefinition> componentInstances = compositionDataDefinition.getComponentInstances();

                if (componentInstances != null) {
                    List<String> ciNames = componentInstances
                            .values()
                            .stream()
                            .filter(ci -> instanceOnEdge.contains(ci.getUniqueId()))
                            .map(ComponentInstanceDataDefinition::getName)
                            .collect(Collectors.toList());
                    if (ciNames != null && !ciNames.isEmpty()) {
                        container.setInstanceNames(ciNames);
                        break;
                    }
                }
            }
        }
    }

    public List<String> getInstanceIdFromAllottedEdge(String resourceId, String serviceInvariantUUID) {
      Either<GraphVertex, TitanOperationStatus> vertexById = titanDao.getVertexById(resourceId);
      if ( vertexById.isLeft() ){
          GraphVertex vertexG = vertexById.left().value();
          TitanVertex vertex = vertexG.getVertex();
          Iterator<Edge> edges = vertex.edges(Direction.OUT, EdgeLabelEnum.ALLOTTED_OF.name());
          while ( edges != null && edges.hasNext() ){
              Edge edge = edges.next();
              TitanVertex inVertex = (TitanVertex)edge.inVertex();
              String vertexInInvUUID = (String) titanDao.getProperty(inVertex, GraphPropertyEnum.INVARIANT_UUID.getProperty());
              if ( vertexInInvUUID.equals(serviceInvariantUUID) ){
                  return (List<String>) titanDao.getProperty(edge, EdgePropertyEnum.INSTANCES) ;
              }
          }
      }
      return new ArrayList<>();
    }

}
