package org.openecomp.sdc.ci.tests.utils.graph;

import com.thinkaurelius.titan.core.TitanVertex;
import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GraphFileUtils {

    public static final String TEMP_FILES_PATH = "src/main/resources/ci/tempFiles/%s.txt";

    public static void writeVerticesUIDToFile(String fileName, Iterable<TitanVertex> vertices) throws IOException {
        Path path = Paths.get(String.format(TEMP_FILES_PATH, fileName));
        Files.deleteIfExists(path);
        Path file = Files.createFile(path);
        final String newLine = System.getProperty("line.separator");
        for (TitanVertex vertex : vertices) {
            FileUtils.writeStringToFile(file.toFile(), String.valueOf(vertex.id()) + newLine, Charset.defaultCharset(), true);
        }
    }

    public static List<String> getVerticesIdsFromFile(String fileName) throws IOException {
        List<String> verticesUids = new ArrayList<>();
        Files.lines(Paths.get(String.format(TEMP_FILES_PATH, fileName))).forEach(verticesUids::add);
        return verticesUids;
    }

    private static String getUid(TitanVertex titanVertex) {
        return (String )titanVertex.value(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
    }

}
