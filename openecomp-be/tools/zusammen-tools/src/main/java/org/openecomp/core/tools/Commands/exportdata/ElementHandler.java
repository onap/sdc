package org.openecomp.core.tools.Commands.exportdata;


import org.openecomp.core.tools.store.ElementCassandraLoader;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

import static java.io.File.separator;
import static java.nio.file.Files.*;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.*;

public class ElementHandler {

    private static final Logger logger = LoggerFactory.getLogger(ElementHandler.class);

    public ElementHandler() {
    }

    public void loadElements(Set<String> filteredItem) {
        ElementCassandraLoader elementCassandraLoader = new ElementCassandraLoader();
          elementCassandraLoader.list().forEach(elementEntity ->  handleElementEntity(elementEntity,filteredItem));
    }

    private void handleElementEntity(ElementEntity elementEntity, Set<String> filteredItem) {
        try {
            String itemId = elementEntity.getItemId();

            if (!filteredItem.isEmpty() && !filteredItem.contains(itemId)){
                return;
            }
            String versionId = elementEntity.getVersionId();
            String space = elementEntity.getSpace();
            String namespace = elementEntity.getNamespace();
            String elementId = elementEntity.getElement_id();

            String namespacePath = separator;
            if (!isNull(namespace)){
                namespacePath =  namespace.replace(ELEMENT_NAMESPACE_SPLITTER,separator)+separator;
            }
            Path elementDirectoryPath;
            if (!isNull(namespace)){
                elementDirectoryPath = Paths.get( ROOT_DIRECTORY + separator + itemId
                        + separator + versionId + separator + space + separator + namespacePath+ separator + elementId);
            } else {
                elementDirectoryPath = Paths.get( ROOT_DIRECTORY + separator + itemId
                        + separator + versionId + separator + space + separator + elementId);
              }

            if (notExists(elementDirectoryPath)) {
                 Path created = createDirectories(elementDirectoryPath);
             }

            String info = elementEntity.getInfo();
            if (!isNull(info)) {
                Path infoFilePath = Paths.get(elementDirectoryPath.toString() + separator + ELEMENT_INFO_PREFIX
                        + elementId + JSON_POSTFIX);
                write(infoFilePath, info.getBytes());
            }

            String relations = elementEntity.getRelations();
            if (!isNull(relations)) {
                Path realtionsFilePath = Paths.get(elementDirectoryPath.toString() + separator
                        + ELEMENT_RELATION_PREFIX + elementId + JSON_POSTFIX);
                write(realtionsFilePath, relations.getBytes());
            }

            ByteBuffer data = elementEntity.getData();
            if (!Objects.isNull(data)) {
                Path dataFilePath = Paths.get(elementDirectoryPath.toString() + separator
                        + ELEMENT_DATA_PREFIX + elementId + JSON_POSTFIX);
                write(dataFilePath, data.array());
            }

            ByteBuffer visualization = elementEntity.getVisualization();
            if (!Objects.isNull(visualization)) {
                Path visualFilePath = Paths.get(elementDirectoryPath.toString() + separator
                        + ELEMENT_VISUALIZATION_PREFIX + elementId );
                write(visualFilePath, visualization.array());
            }

            ByteBuffer searchableData = elementEntity.getSearchableData();
            if (!Objects.isNull(searchableData)) {
                Path searchableFilePath = Paths.get(elementDirectoryPath.toString() + separator
                        + ELEMENT_SEARCHABLE_PREFIX + elementId);
                write(searchableFilePath, searchableData.array());
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    private boolean isNull(String inStr){
        if (Objects.isNull(inStr)){
            return true;
        }
        return inStr.trim().equalsIgnoreCase("null");
    }

}
