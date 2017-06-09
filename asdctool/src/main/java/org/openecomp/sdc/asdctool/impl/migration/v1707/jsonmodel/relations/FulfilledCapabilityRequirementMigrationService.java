package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel.relations;

import fj.Function;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.asdctool.impl.migration.MigrationException;
import org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils;
import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.api.ToscaDefinitionPathCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openecomp.sdc.asdctool.impl.migration.MigrationMsg.*;
import static org.openecomp.sdc.asdctool.impl.migration.v1707.MigrationUtils.willThrowException;

public abstract class FulfilledCapabilityRequirementMigrationService<T extends ToscaDataDefinition, S extends GraphNode> {

    private static Logger LOGGER = LoggerFactory.getLogger(FulfilledCapabilityRequirementMigrationService.class);

    @Resource(name = "topology-template-operation")
    TopologyTemplateOperation topologyTemplateOperation;

    @Resource(name = "tosca-path-calculator")
    private ToscaDefinitionPathCalculator toscaDefinitionPathCalculator;

    @Resource(name = "titan-dao")
    TitanDao titanDao;

    public boolean associateToscaDefinitions(Component component, NodeTypeEnum componentType) {
        try {
            return isDefinitionsAlreadyAssociated(component) || doAssociateToscaDefinitions(component, componentType);
        } catch (MigrationException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private boolean isDefinitionsAlreadyAssociated(Component component) {
        GraphVertex componentVertex =  titanDao.getVertexById(component.getUniqueId()).left().on((err) -> willThrowException(FAILED_TO_RETRIEVE_VERTEX.getMessage(component.getName(), err.name())));
        return this.getAssociatedDefinitions(componentVertex)
                   .either(vertex -> true,
                           errorStatus -> notFoundStatusOrFail(component, errorStatus));

    }

    private boolean notFoundStatusOrFail(Component component, TitanOperationStatus error) {
        if (error.equals(TitanOperationStatus.NOT_FOUND)) {
            return false;
        }
        throw new MigrationException(FAILED_TO_RETRIEVE_CAP_REQ_VERTEX.getMessage(component.getName(), error.name()));
    }

    private boolean doAssociateToscaDefinitions(Component component, NodeTypeEnum componentType) {
        try {
            Map<String, MapDataDefinition> toscaDefByInstance = groupToscaDefinitionByInstance(component, componentType);
            return toscaDefByInstance.isEmpty() || updateOnGraph(component, toscaDefByInstance);
        } catch (MigrationException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    private  Map<String, MapDataDefinition> groupToscaDefinitionByInstance(Component component, NodeTypeEnum componentType) {
        Map<String, MapDataDefinition> toscaDefByInstance = new HashMap<>();
        for (ComponentInstance componentInstance : component.getComponentInstances()) {
                List<ImmutablePair<S, GraphEdge>> fulfilledCapReq = getFulfilledCapReqs(componentType, componentInstance);
                if (fulfilledCapReq.isEmpty()) {
                    continue;
                }
                toscaDefByInstance.put(componentInstance.getUniqueId(), getReqCapToscaDefs(fulfilledCapReq, componentInstance));
        }
        return toscaDefByInstance;
    }

    private MapDataDefinition getReqCapToscaDefs(List<ImmutablePair<S, GraphEdge>> capReqsData, ComponentInstance componentInstance) {
        Map<String, List<T>> capReqDefinitions = getCapReqDefinitions(componentInstance, capReqsData);
        return convertToMapDefinition(capReqDefinitions);
    }

    private List<ImmutablePair<S, GraphEdge>> getFulfilledCapReqs(NodeTypeEnum componentType, ComponentInstance componentInstance) {
        return getFulfilledCapReqs(componentInstance, componentType)
                .either(Function.identity(),
                        error ->  emptyListOrFail(error, componentInstance.getName()));
    }

    private List<ImmutablePair<S, GraphEdge>> emptyListOrFail(TitanOperationStatus error, String instanceName) {
        if (error.equals(TitanOperationStatus.NOT_FOUND)) {
            return Collections.emptyList();
        }
        String errorMsg = FAILED_TO_RETRIEVE_REQ_CAP.getMessage(instanceName, error.name());
        throw new MigrationException(errorMsg);
    }

    private Map<String, List<T>> getCapReqDefinitions(ComponentInstance componentInstance, List<ImmutablePair<S, GraphEdge>> capReqDataList) {
        return capReqDataList.stream()
                .map(capReqData -> convertToToscaDef(componentInstance, capReqData))
                .collect(Collectors.groupingBy(this::getType));
    }

    private T convertToToscaDef(ComponentInstance componentInstance, ImmutablePair<S, GraphEdge> data) {
        T def = getReqCapDataDefinition(data);
        List<String> definitionPath = toscaDefinitionPathCalculator.calculateToscaDefinitionPath(componentInstance, data.getRight());
        setPath(def, definitionPath);
        return def;
    }

    private T getReqCapDataDefinition(ImmutablePair<S, GraphEdge> data) {
        S capReqData = data.getLeft();
        return getToscaDefinition(capReqData).left().on(err -> willThrowException(FAILED_TO_RETRIEVE_TOSCA_DEF.getMessage(capReqData.getUniqueId().toString(), err.toString())));
    }

    private boolean updateOnGraph(Component component, Map<String, MapDataDefinition> defsByInstance) {
        GraphVertex graphVertex = getComponentGraphVertex(component);
        Either<GraphVertex, StorageOperationStatus> associatedVertex = associateToGraph(graphVertex, defsByInstance);
        return associatedVertex.either(vertex -> true, err -> MigrationUtils.handleError(FAILED_TO_ASSOCIATE_CAP_REQ.getMessage(component.getName(), err.name())));
    }

    private GraphVertex getComponentGraphVertex(Component component) {
        return titanDao.getVertexById(component.getUniqueId())
                       .left().on(error -> willThrowException(FAILED_TO_RETRIEVE_VERTEX.getMessage(component.getUniqueId(), error.name())));
    }

    private MapDataDefinition convertToMapDefinition(Map<String, List<T>> toscaDefs) {
        Map<String, ListDataDefinition> defsListByType = toscaDefs.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertToDefinitionListObject(entry.getValue())));
        return convertToDefinitionMapObject(defsListByType);
    }

    abstract Either<T, ?> getToscaDefinition(S data);

    abstract void setPath(T def, List<String> path);

    abstract String getType(T def);

    abstract Either<List<ImmutablePair<S, GraphEdge>>, TitanOperationStatus> getFulfilledCapReqs(ComponentInstance instance, NodeTypeEnum nodeTypeEnum);

    abstract ListDataDefinition convertToDefinitionListObject(List<T> capReqDefList);

    abstract MapDataDefinition convertToDefinitionMapObject(Map<String, ListDataDefinition> reqCapForInstance);

    abstract Either<GraphVertex, TitanOperationStatus> getAssociatedDefinitions(GraphVertex component);

    abstract Either<GraphVertex, StorageOperationStatus> associateToGraph(GraphVertex graphVertex, Map<String, MapDataDefinition> defsByInstance);

}
