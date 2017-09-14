package org.openecomp.core.tools.Commands.importdata;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.tools.store.ElementCassandraLoader;
import org.openecomp.core.tools.store.ElementNamespaceHandler;
import org.openecomp.core.tools.store.VersionCassandraLoader;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.io.File.separator;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.*;

public class ElementImport {
    private static final Logger logger = LoggerFactory.getLogger(ElementImport.class);
    public static final String ROOT_ITEM = Id.ZERO.getValue();

    private ElementCassandraLoader elementCassandraLoader = new ElementCassandraLoader();
    private ElementNamespaceHandler cassandraElementRepository = new ElementNamespaceHandler();
    private VersionCassandraLoader versionCassandraLoader = new VersionCassandraLoader();

    public void loadPath(SessionContext sessionContext, Path elementDir, String elementId, String[]
            pathObjects) {
        try {
            if (!Files.isDirectory(elementDir)){
                return;
            }
            // load info file
            ElementEntity elementEntity = new ElementEntity();
            Path infoFilePath = Paths.get(elementDir.toString() + separator + ELEMENT_INFO_PREFIX
                    + elementId + JSON_POSTFIX);
            if (Files.exists(infoFilePath)) {
                String info = new String(Files.readAllBytes(infoFilePath));
                elementEntity.setInfo(info);
            }
            // load relation file
            Path realtionsFilePath = Paths.get(elementDir.toString() + separator
                    + ELEMENT_RELATION_PREFIX + elementId + JSON_POSTFIX);
            if (Files.exists(realtionsFilePath)) {
                String relations = new String(Files.readAllBytes(realtionsFilePath));
                elementEntity.setRelations(relations);
            }

            //load entity data
            Path dataFilePath = Paths.get(elementDir.toString() + separator
                    + ELEMENT_DATA_PREFIX + elementId + JSON_POSTFIX);
            if (Files.exists(dataFilePath)) {
                byte[] bytes = Files.readAllBytes(dataFilePath);
                ByteBuffer data = ByteBuffer.wrap(bytes);
                elementEntity.setData(data);
            }

            //load visualization
            Path visualFilePath = Paths.get(elementDir.toString() + separator
                    + ELEMENT_VISUALIZATION_PREFIX + elementId);
            if (Files.exists(visualFilePath)) {
                byte[] bytes = Files.readAllBytes(visualFilePath);
                ByteBuffer visualization = ByteBuffer.wrap(bytes);
                elementEntity.setVisualization(visualization);
            }

            //load searchable
            Path searchableFilePath = Paths.get(elementDir.toString() + separator
                    + ELEMENT_SEARCHABLE_PREFIX + elementId);
            if (Files.exists(searchableFilePath)) {
                byte[] bytes = Files.readAllBytes(searchableFilePath);
                ByteBuffer searchable = ByteBuffer.wrap(bytes);
                elementEntity.setSearchableData(searchable);
            }
            String element_Id = pathObjects[pathObjects.length - 1];
            elementEntity.setSpace(pathObjects[2]);
            elementEntity.setItemId(pathObjects[0]);
            elementEntity.setVersionId(pathObjects[1]);
            elementEntity.setElement_id(element_Id);
            elementEntity.setNamespace(getNameSpace(pathObjects));
            elementEntity.setParentId(getParentId(pathObjects));
            elementEntity.setSubElementIds(getAllSubElementsIds(elementDir, element_Id));
            elementCassandraLoader.createEntity(elementEntity);
            cassandraElementRepository.createElementNamespace(elementEntity);
            versionCassandraLoader.insertElementToVersion(elementEntity);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private String getParentId(String[] pathObjects) {

        if (pathObjects[pathObjects.length - 1].equals(ROOT_ITEM)) {
            return null;
        }
        if (pathObjects.length == 4) {
            return ROOT_ITEM;
        }
        return pathObjects[pathObjects.length - 2];
    }

    private Set<String> getAllSubElementsIds(Path root, String elementId) throws IOException {
        if (elementId.equals(ROOT_ITEM)) {
            root = root.getParent();
        }
        File file = root.toFile();
        Set<String> retVal = new HashSet<>();
        File[] files = file.listFiles();
        for (File f : files){
            if (f.isDirectory()){
                retVal.add(f.getName());
            }
        }
        retVal.remove(ROOT_ITEM);
        return retVal;

    }

    private String getNameSpace(String[] pathObjects) {
        if (pathObjects.length <= 4) {
            return "";
        }
        if (pathObjects.length == 5) {
            return pathObjects[3];
        }
        return Arrays.stream(pathObjects, 3, pathObjects.length - 1)
                .reduce("", (s1, s2) -> s1 + File.separator + s2);
    }
}
