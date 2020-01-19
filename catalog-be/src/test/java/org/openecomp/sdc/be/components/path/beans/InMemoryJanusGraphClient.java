/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.path.beans;


import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.InvalidElementException;
import org.janusgraph.core.InvalidIDException;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphConfigurationException;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.QueryException;
import org.janusgraph.core.SchemaViolationException;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.diskstorage.ResourceUnavailableException;
import org.janusgraph.diskstorage.locking.PermanentLockingException;
import org.janusgraph.graphdb.database.idassigner.IDPoolExhaustedException;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;

@Component("janusgraph-client")
public class InMemoryJanusGraphClient extends JanusGraphClient {


    private static final Logger logger = LoggerFactory.getLogger(InMemoryJanusGraphClient.class);

    private static final String OK = "GOOD";

    private JanusGraph graph;

    public InMemoryJanusGraphClient() {
        super();
        logger.info("** JanusGraphClient created");
    }

    @PostConstruct
    public JanusGraphOperationStatus createGraph() {

        logger.info("** createGraph started **");
        graph = JanusGraphFactory.build().set("storage.backend", "inmemory").open();
        createJanusGraphSchema();

        logger.info("** in memory graph created");
        return JanusGraphOperationStatus.OK;

    }


    public void cleanupGraph() {
        if (graph != null) {
            // graph.shutdown();
            graph.close();
            try {
                JanusGraphFactory.drop(graph);
            } catch (BackendException e) {
                e.printStackTrace();
            }
        }
    }

    public JanusGraphOperationStatus createGraph(String janusGraphCfgFile) {
        logger.info("** open graph with {} started", janusGraphCfgFile);
        try {
            logger.info("openGraph : try to load file {}", janusGraphCfgFile);
            graph = JanusGraphFactory.open(janusGraphCfgFile);
            if (graph.isClosed()) {
                logger.error("janusgraph graph was not initialized");
                return JanusGraphOperationStatus.NOT_CREATED;
            }

        } catch (Exception e) {
            this.graph = null;
            logger.info("createGraph : failed to open JanusGraph graph with configuration file: {}", janusGraphCfgFile, e);
            return JanusGraphOperationStatus.NOT_CONNECTED;
        }

        logger.info("** JanusGraph graph created ");

        return JanusGraphOperationStatus.OK;
    }


    public Either<JanusGraph, JanusGraphOperationStatus> getGraph() {
        if (graph != null) {
            return Either.left(graph);
        } else {
            return Either.right(JanusGraphOperationStatus.NOT_CREATED);
        }
    }

    public JanusGraphOperationStatus commit() {
        if (graph != null) {
            try {
                graph.tx().commit();
                return JanusGraphOperationStatus.OK;
            } catch (Exception e) {
                return handleJanusGraphException(e);
            }
        } else {
            return JanusGraphOperationStatus.NOT_CREATED;
        }
    }

    public JanusGraphOperationStatus rollback() {
        if (graph != null) {
            try {
                // graph.rollback();
                graph.tx().rollback();
                return JanusGraphOperationStatus.OK;
            } catch (Exception e) {
                return handleJanusGraphException(e);
            }
        } else {
            return JanusGraphOperationStatus.NOT_CREATED;
        }
    }

    public static JanusGraphOperationStatus handleJanusGraphException(Exception e) {
        if (e instanceof JanusGraphConfigurationException) {
            return JanusGraphOperationStatus.JANUSGRAPH_CONFIGURATION;
        }
        if (e instanceof SchemaViolationException) {
            return JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION;
        }
        if (e instanceof PermanentLockingException) {
            return JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION;
        }
        if (e instanceof IDPoolExhaustedException) {
            return JanusGraphOperationStatus.GENERAL_ERROR;
        }
        if (e instanceof InvalidElementException) {
            return JanusGraphOperationStatus.INVALID_ELEMENT;
        }
        if (e instanceof InvalidIDException) {
            return JanusGraphOperationStatus.INVALID_ID;
        }
        if (e instanceof QueryException) {
            return JanusGraphOperationStatus.INVALID_QUERY;
        }
        if (e instanceof ResourceUnavailableException) {
            return JanusGraphOperationStatus.RESOURCE_UNAVAILABLE;
        }
        if (e instanceof IllegalArgumentException) {
            // TODO check the error message??
            return JanusGraphOperationStatus.ILLEGAL_ARGUMENT;
        }

        return JanusGraphOperationStatus.GENERAL_ERROR;
    }

    public boolean getHealth() {
        return true;
    }

    private boolean isGraphOpen() {
        return true;
    }


    private static final String JANUSGRAPH_HEALTH_CHECK_STR = "janusGraphHealthCheck";


    private void createJanusGraphSchema() {

        JanusGraphManagement graphMgt = graph.openManagement();
        JanusGraphIndex index = null;
        for (GraphPropertiesDictionary prop : GraphPropertiesDictionary.values()) {
            PropertyKey propKey = null;
            if (!graphMgt.containsPropertyKey(prop.getProperty())) {
                Class<?> clazz = prop.getClazz();
                if (!ArrayList.class.getName().equals(clazz.getName()) && !HashMap.class.getName().equals(clazz.getName())) {
                    propKey = graphMgt.makePropertyKey(prop.getProperty()).dataType(prop.getClazz()).make();
                }
            } else {
                propKey = graphMgt.getPropertyKey(prop.getProperty());
            }
            if (prop.isIndexed()) {
                if (!graphMgt.containsGraphIndex(prop.getProperty())) {
                    if (prop.isUnique()) {
                        index = graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).unique().buildCompositeIndex();

                        graphMgt.setConsistency(propKey, ConsistencyModifier.LOCK); // Ensures
                        // only
                        // one
                        // name
                        // per
                        // vertex
                        graphMgt.setConsistency(index, ConsistencyModifier.LOCK); // Ensures
                        // name
                        // uniqueness
                        // in
                        // the
                        // graph

                    } else {
                        graphMgt.buildIndex(prop.getProperty(), Vertex.class).addKey(propKey).buildCompositeIndex();
                    }
                }
            }
        }
        graphMgt.commit();
    }

}
