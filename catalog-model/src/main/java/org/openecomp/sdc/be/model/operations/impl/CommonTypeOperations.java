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
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.HealingTitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.springframework.stereotype.Component;

@Component
public class CommonTypeOperations {

    private final HealingTitanGenericDao titanGenericDao;
    private final PropertyOperation propertyOperation;
    private final OperationUtils operationUtils;

    public CommonTypeOperations(HealingTitanGenericDao titanGenericDao, PropertyOperation propertyOperation,
            OperationUtils operationUtils) {
        this.titanGenericDao = titanGenericDao;
        this.propertyOperation = propertyOperation;
        this.operationUtils = operationUtils;
    }

    public <T extends GraphNode> void addType(T typeData, Class<T> clazz) {
        titanGenericDao.createNode(typeData, clazz)
            .left()
            .on(operationUtils::onTitanOperationFailure);
    }

    public <T extends GraphNode> Optional<T> getType(String uniqueId, Class<T> clazz, NodeTypeEnum nodeType) {
        T type = titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(nodeType), uniqueId, clazz)
                .left()
                .on(err -> null);
        return Optional.ofNullable(type);
    }

    public <T extends GraphNode> Optional<T> getLatestType(String type, Class<T> clazz, NodeTypeEnum nodeType) {
        Map<String, Object> mapCriteria = new HashMap<>();
        mapCriteria.put(GraphPropertiesDictionary.TYPE.getProperty(), type);
        mapCriteria.put(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty(), true);
        return titanGenericDao.getByCriteria(nodeType, mapCriteria, clazz)
                .left()
                .on(err -> emptyList())
                .stream()
                .findFirst();
    }

    public void addProperties(String uniqueId, NodeTypeEnum nodeType, List<PropertyDefinition> properties) {
        propertyOperation.addPropertiesToElementType(uniqueId, nodeType, properties)
            .left()
            .on(operationUtils::onTitanOperationFailure);
    }

    public void fillProperties(String uniqueId, NodeTypeEnum nodeType, Consumer<List<PropertyDefinition>> propertySetter) {
        TitanOperationStatus status = propertyOperation.fillPropertiesList(uniqueId, nodeType, propertySetter);
        if (status!=TitanOperationStatus.OK) {
            operationUtils.onTitanOperationFailure(status);
        }
    }

    /**
     * Handle update of type without dervidedFrom attribute
     */
    public  <T extends GraphNode> void updateType(T typeData, List<PropertyDefinition> properties, Class<T> clazz, NodeTypeEnum nodeType) {
        titanGenericDao.updateNode(typeData, clazz)
                .left()
                .on(operationUtils::onTitanOperationFailure);
        Map<String, PropertyDefinition> newProperties = properties.stream()
                .collect(Collectors.toMap(PropertyDefinition::getName, Function.identity()));
        propertyOperation.mergePropertiesAssociatedToNode(nodeType, typeData.getUniqueId(), newProperties)
                .left()
                .on(operationUtils::onTitanOperationFailure);
    }
}
