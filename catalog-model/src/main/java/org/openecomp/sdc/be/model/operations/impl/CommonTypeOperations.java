package org.openecomp.sdc.be.model.operations.impl;

import static java.util.Collections.emptyList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.springframework.stereotype.Component;

@Component
public class CommonTypeOperations {

    private final HealingJanusGraphGenericDao janusGraphGenericDao;
    private final PropertyOperation propertyOperation;
    private final OperationUtils operationUtils;

    public CommonTypeOperations(HealingJanusGraphGenericDao janusGraphGenericDao, PropertyOperation propertyOperation,
                                OperationUtils operationUtils) {
        this.janusGraphGenericDao = janusGraphGenericDao;
        this.propertyOperation = propertyOperation;
        this.operationUtils = operationUtils;
    }

    public <T extends GraphNode> void addType(T typeData, Class<T> clazz) {
        janusGraphGenericDao.createNode(typeData, clazz)
            .left()
            .on(operationUtils::onJanusGraphOperationFailure);
    }

    public <T extends GraphNode> Optional<T> getType(String uniqueId, Class<T> clazz, NodeTypeEnum nodeType) {
        T type = janusGraphGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, clazz)
                .left()
                .on(err -> null);
        return Optional.ofNullable(type);
    }

    public <T extends GraphNode> Optional<T> getLatestType(String type, Class<T> clazz, NodeTypeEnum nodeType) {
        Map<String, Object> mapCriteria = new HashMap<>();
        mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
        mapCriteria.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
        return janusGraphGenericDao.getByCriteria(nodeType, mapCriteria, clazz)
                .left()
                .on(err -> emptyList())
                .stream()
                .findFirst();
    }

    public void addProperties(String uniqueId, NodeTypeEnum nodeType, List<PropertyDefinition> properties) {
        propertyOperation.addPropertiesToElementType(uniqueId, nodeType, properties)
            .left()
            .on(operationUtils::onJanusGraphOperationFailure);
    }

    public void fillProperties(String uniqueId, NodeTypeEnum nodeType, Consumer<List<PropertyDefinition>> propertySetter) {
        JanusGraphOperationStatus
            status = propertyOperation.fillPropertiesList(uniqueId, nodeType, propertySetter);
        if (status!= JanusGraphOperationStatus.OK) {
            operationUtils.onJanusGraphOperationFailure(status);
        }
    }

    /**
     * Handle update of type without dervidedFrom attribute
     */
    public  <T extends GraphNode> void updateType(T typeData, List<PropertyDefinition> properties, Class<T> clazz, NodeTypeEnum nodeType) {
        janusGraphGenericDao.updateNode(typeData, clazz)
                .left()
                .on(operationUtils::onJanusGraphOperationFailure);
        Map<String, PropertyDefinition> newProperties = properties.stream()
                .collect(Collectors.toMap(PropertyDefinition::getName, Function.identity()));
        propertyOperation.mergePropertiesAssociatedToNode(nodeType, typeData.getUniqueId(), newProperties)
                .left()
                .on(operationUtils::onJanusGraphOperationFailure);
    }
}
