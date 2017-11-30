/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.utils.graph;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;

import com.thinkaurelius.titan.core.TitanVertex;

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
