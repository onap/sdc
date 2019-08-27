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

package org.openecomp.sdc.asdctool.impl;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONReader;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphQuery;
import org.janusgraph.core.JanusGraphVertex;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class GraphMLConverter {

    private static final String STORAGE_BACKEND = "storage.backend";

    private static final String INMEMORY = "inmemory";

    private static final String EXPORT_GRAPH = "exportGraph.";

    private static final String DOT_JSON = ".json";

    private static final String EXPORTED_FILE = "Exported file=";

    private static final String NODE_LABEL = "nodeLabel";

    private static Logger log = Logger.getLogger(GraphMLConverter.class.getName());

    private Gson gson = new Gson();

    public boolean importGraph(String[] args) {

        JanusGraph graph = null;
        try {
            String janusGraphFileLocation = args[1];
            String inputFile = args[2];
            graph = openGraph(janusGraphFileLocation);

            List<ImmutablePair<String, String>> propertiesCriteriaToDelete = new ArrayList<>();
            ImmutablePair<String, String> immutablePair1 = new ImmutablePair<>("healthcheckis", "GOOD");
            ImmutablePair<String, String> immutablePair2 = new ImmutablePair<>(NODE_LABEL, "user");
            ImmutablePair<String, String> immutablePair3 = new ImmutablePair<>(NODE_LABEL, "resourceCategory");
            ImmutablePair<String, String> immutablePair4 = new ImmutablePair<>(NODE_LABEL, "serviceCategory");

            propertiesCriteriaToDelete.add(immutablePair1);
            propertiesCriteriaToDelete.add(immutablePair2);
            propertiesCriteriaToDelete.add(immutablePair3);
            propertiesCriteriaToDelete.add(immutablePair4);

            return importJsonGraph(graph, inputFile, propertiesCriteriaToDelete);

        } catch (Exception e) {
            log.info("import graph failed ", e);
            return false;
        } finally {
            if (graph != null) {
                graph.close();
            }
        }

    }

    public boolean exportGraph(String[] args) {

        JanusGraph graph = null;
        try {
            String janusGraphFileLocation = args[1];
            String outputDirectory = args[2];
            graph = openGraph(janusGraphFileLocation);

            String result = exportJsonGraph(graph, outputDirectory);

            if (result == null) {
                return false;
            }

            log.info("{} {}", EXPORTED_FILE, result);
        } catch (Exception e) {
            log.info("export graph failed ", e);
            return false;
        } finally {
            if (graph != null) {
                graph.close();
            }
        }

        return true;
    }

    public String exportGraphMl(String[] args) {

        JanusGraph graph = null;
        String result = null;
        try {
            String janusGraphFileLocation = args[1];
            String outputDirectory = args[2];
            graph = openGraph(janusGraphFileLocation);

            result = exportGraphMl(graph, outputDirectory);

            log.info("{} {}", EXPORTED_FILE, result);
        } catch (Exception e) {
            log.info("export exportGraphMl failed ", e);
            return null;
        } finally {
            if (graph != null) {
                graph.close();
            }
        }

        return result;
    }

    public boolean findErrorInJsonGraph(String[] args) {

        JanusGraph graph = null;
        try {
            String janusGraphFileLocation = args[1];
            String outputDirectory = args[2];
            graph = openGraph(janusGraphFileLocation);

            String result = findErrorInJsonGraph(graph, outputDirectory);

            if (result == null) {
                return false;
            }

            log.info("{} {}", EXPORTED_FILE, result);
        } catch (Exception e) {
            log.info("find Error In Json Graph failed ", e);
            return false;
        } finally {
            if (graph != null) {
                graph.close();
            }
        }

        return true;
    }

    public JanusGraph openGraph(String janusGraphFileLocation) {

        return JanusGraphFactory.open(janusGraphFileLocation);

    }

    public String exportJsonGraph(JanusGraph graph, String outputDirectory) {

        String result = null;

        String outputFile = outputDirectory + File.separator + EXPORT_GRAPH + System.currentTimeMillis() + DOT_JSON;

        try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            final GraphSONWriter.Builder builder = GraphSONWriter.build();
            final GraphSONMapper mapper = newGraphSONMapper(graph);
            builder.mapper(mapper);
            final GraphSONWriter writer = builder.create();
            writer.writeGraph(out, graph);

            graph.tx().commit();

            result = outputFile;

        } catch (Exception e) {
            log.info("export Json Graph failed ", e);
            graph.tx().rollback();
        }
        return result;

    }

    public String exportGraphMl(JanusGraph graph, String outputDirectory) {
        String result = null;
        String outputFile =
            outputDirectory + File.separator + EXPORT_GRAPH + System.currentTimeMillis() + ".graphml";
        try {
            try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(os, graph);
            }
            result = outputFile;
            graph.tx().commit();
        } catch (Exception e) {
            graph.tx().rollback();
            log.info("export Graph Ml failed ", e);
        }
        return result;

    }

    private static GraphSONMapper newGraphSONMapper(final Graph graph) {
        final GraphSONMapper.Builder builder = graph.io(IoCore.graphson()).mapper();
        return builder.create();
    }

    public boolean importJsonGraph(JanusGraph graph, String graphJsonFile,
                                   List<ImmutablePair<String, String>> propertiesCriteriaToDelete) {

        boolean result = false;

        if (propertiesCriteriaToDelete != null) {
            for (Entry<String, String> entry : propertiesCriteriaToDelete

            ) {

                String key = entry.getKey();
                String value = entry.getValue();
                Iterator iterator = graph.query().has(key, value).vertices().iterator();
                while (iterator.hasNext()) {
                    Vertex vertex = (Vertex) iterator.next();
                    vertex.remove();
                    log.info("Remove vertex of type{} ", key, " and value {}", value);
                }

            }
        }
        File file = new File(graphJsonFile);
        if (!file.isFile()) {
            log.info("File ", graphJsonFile, " cannot be found.");
            return result;
        }

        try (final InputStream is = new BufferedInputStream(new FileInputStream(graphJsonFile))) {

            log.info("Before importing file ", graphJsonFile);

            GraphSONReader create = GraphSONReader.build().create();
            create.readGraph(is, graph);

            graph.tx().commit();

            result = true;

        } catch (Exception e) {
            log.info("Failed to import graph ", e);
            graph.tx().rollback();
        }
        return result;

    }

    public String findErrorInJsonGraph(JanusGraph graph, String outputDirectory) {

        String result = null;

        String outputFile = outputDirectory + File.separator + EXPORT_GRAPH + System.currentTimeMillis() + DOT_JSON;

        try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            graph.query().has(GraphPropertiesDictionary.HEALTH_CHECK.getProperty(), "GOOD").vertices();

            BaseConfiguration conf = new BaseConfiguration();
            conf.setProperty(STORAGE_BACKEND, INMEMORY);
            for (NodeTypeEnum nodeTypeEnum : NodeTypeEnum.values()) {
                removeNodesByLabel(graph, nodeTypeEnum.getName());
            }

            GraphSONWriter create = GraphSONWriter.build().create();
            create.writeGraph(out, graph);

            graph.tx().rollback();

        } catch (Exception e) {
            log.info("find Error In Json Graph failed ", e);
            graph.tx().rollback();
        }
        return result;

    }

    private void removeNodesByLabel(JanusGraph graph, String label) {
        Iterable<JanusGraphVertex> vertices =
            graph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), label).vertices();
        Iterator<JanusGraphVertex> iterator = vertices.iterator();
        while (iterator.hasNext()) {
            Vertex next2 = iterator.next();
            next2.remove();
        }
    }

    public String exportUsers(JanusGraph graph, String outputDirectory) {

        List<Map<String, Object>> users = new ArrayList<>();
        String result = null;

        String outputFile = outputDirectory + File.separator + "users." + System.currentTimeMillis() + DOT_JSON;

        JanusGraphQuery graphQuery =
            graph.query().has(GraphPropertiesDictionary.LABEL.getProperty(), NodeTypeEnum.User.getName());

        @SuppressWarnings("unchecked")
        Iterable<JanusGraphVertex> vertices = graphQuery.vertices();

        if (vertices != null) {
            for (Vertex v : vertices) {
                Map<String, Object> properties = getProperties(v);
                properties.remove(GraphPropertiesDictionary.LABEL.getProperty());
                users.add(properties);
            }
        }

        graph.tx().commit();

        String jsonUsers = gson.toJson(users);

        try (final FileWriter fileWriter = new FileWriter(outputFile)) {

            fileWriter.write(jsonUsers);

            result = outputFile;

        } catch (Exception e) {
            log.info("export Users failed ", e);
            graph.tx().rollback();
        }
        return result;

    }

    public Map<String, Object> getProperties(Element element) {

        Map<String, Object> result = new HashMap<>();
        ;

        if (element.keys() != null && !element.keys().isEmpty()) {
            Map<String, Property> propertyMap =
                ElementHelper.propertyMap(element, element.keys().toArray(new String[element.keys().size()]));

            for (Entry<String, Property> entry : propertyMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue().value();

                result.put(key, value);
            }
        }
        return result;
    }

    public boolean exportUsers(String[] args) {

        JanusGraph graph = null;
        try {
            String janusGraphFileLocation = args[1];
            String outputDirectory = args[2];
            graph = openGraph(janusGraphFileLocation);

            String result = exportUsers(graph, outputDirectory);

            if (result == null) {
                return false;
            }

            log.info(EXPORTED_FILE, result);
        } catch (Exception e) {
            log.info("export Users failed ", e);
            return false;
        } finally {
            if (graph != null) {
                graph.close();
            }
        }

        return true;
    }
}
