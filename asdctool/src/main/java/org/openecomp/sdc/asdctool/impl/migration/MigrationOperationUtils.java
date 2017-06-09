package org.openecomp.sdc.asdctool.impl.migration;

import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component("migrationUtils")
public class MigrationOperationUtils {

    private static Logger log = LoggerFactory.getLogger(MigrationOperationUtils.class);

    @Autowired
    private TitanGenericDao titanGenericDao;

    /**
     * rename a set or property keys
     *
     * @param propertyKeys a mapping between the old property key name and the property key name to replace it with
     *
     * @return true if rename ended successfully or false otherwise
     */
    public boolean renamePropertyKeys(Map<String, String> propertyKeys) {
        Either<TitanGraph, TitanOperationStatus> graph = titanGenericDao.getGraph();
        return graph.either((titanGraph) ->  renamePropertyKeys(titanGraph, propertyKeys),
                            (titanOperationStatus) -> operationFailed(MigrationMsg.FAILED_TO_RETRIEVE_GRAPH.getMessage(titanOperationStatus.name())));
    }

    private boolean renamePropertyKeys(TitanGraph titanGraph, Map<String, String> propertyKeys) {
        try {
            for (Map.Entry<String, String> propertyKeyEntry : propertyKeys.entrySet()) {
                boolean renameSucceeded = renamePropertyKey(titanGraph, propertyKeyEntry);
                if (!renameSucceeded) {
                    return false;
                }
            }
            return true;
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private Boolean renamePropertyKey(TitanGraph titanGraph, Map.Entry<String, String> propertyKeyEntry) {
        String renameFromKey = propertyKeyEntry.getKey();
        String renameToKey = propertyKeyEntry.getValue();
        log.info(String.format("renaming property key %s to %s", renameFromKey, renameToKey));
        return renameProperty(titanGraph, renameFromKey, renameToKey);
    }

    private Boolean renameProperty(TitanGraph titanGraph, String renameFromKey, String renameToKey) {
        if (titanGraph.containsPropertyKey(renameFromKey) && titanGraph.containsPropertyKey(renameToKey)) {//new property already exist, we cant rename to it we need to add new and remove old on every vertices which has the old one.
            return renamePropertyOnEachVertex(titanGraph, renameFromKey, renameToKey);
        }
        return renamePropertyOnGraphLevel(titanGraph, renameFromKey, renameToKey);
    }

    private Boolean renamePropertyOnGraphLevel(TitanGraph titanGraph, String renameFromKey, String renameToKey) {
        TitanManagement titanManagement = titanGraph.openManagement();
        return Optional.ofNullable(titanManagement.getPropertyKey(renameFromKey))
                .map(propertyKey -> renamePropertyOnGraph(titanManagement, propertyKey, renameToKey))
                .orElseGet(() -> {log.info(MigrationMsg.PROPERTY_KEY_NOT_EXIST.getMessage(renameFromKey)); return true;}) ;//if property key not exist rename is considered to be successful
    }

    private boolean renamePropertyOnEachVertex(TitanGraph graph, String oldKey, String newKey) {
        addNewPropertyKeyOnVertices(graph, oldKey, newKey);
        removeOldPropertyKeyFromGraph(graph, oldKey);
        graph.tx().commit();
        return true;
    }

    private void removeOldPropertyKeyFromGraph(TitanGraph graph, String oldKey) {
        graph.getPropertyKey(oldKey).remove();
    }

	private void addNewPropertyKeyOnVertices(TitanGraph graph, String oldKey, String newKey) {
        graph.query().has(oldKey).vertices().forEach(titanVertex -> {
            copyOldKeyValueAndDropKey(oldKey, newKey, (TitanVertex) titanVertex);
        });
    }

    private void copyOldKeyValueAndDropKey(String oldKey, String newKey, TitanVertex titanVertex) {
        VertexProperty<Object> oldProperty = titanVertex.property(oldKey);
        Object oldKeyValue = oldProperty.value();

        titanVertex.property(newKey, oldKeyValue);
        oldProperty.remove();
    }

    private boolean renamePropertyOnGraph(TitanManagement titanManagement, PropertyKey fromPropertyKey, String toKey) {
        try {
            titanManagement.changeName(fromPropertyKey, toKey);
            titanManagement.commit();
            return true;
        } catch (RuntimeException e) {
            log.error(MigrationMsg.RENAME_KEY_PROPERTY_FAILED.getMessage(fromPropertyKey.name()), e.getMessage());
            titanManagement.rollback();
            return false;
        }
    }

    private boolean operationFailed(String errorMessage) {
        log.error(errorMessage);
        return false;
    }


}
