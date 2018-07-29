package org.openecomp.sdc.be.components.path.beans;


import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.schema.ConsistencyModifier;
import com.thinkaurelius.titan.core.schema.TitanGraphIndex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.thinkaurelius.titan.diskstorage.ResourceUnavailableException;
import com.thinkaurelius.titan.diskstorage.locking.PermanentLockingException;
import com.thinkaurelius.titan.graphdb.database.idassigner.IDPoolExhaustedException;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openecomp.sdc.be.dao.TitanClientStrategy;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanGraphClient;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;

@Component("titan-client")
public class InMemoryTitanGraphClient extends TitanGraphClient {


    private static final Logger logger = LoggerFactory.getLogger(InMemoryTitanGraphClient.class);

    private static final String OK = "GOOD";

    public InMemoryTitanGraphClient() {
    }


    private TitanGraph graph;
    TitanClientStrategy titanClientStrategy;

    public InMemoryTitanGraphClient(TitanClientStrategy titanClientStrategy) {
        super();
        this.titanClientStrategy = titanClientStrategy;
        logger.info("** TitanGraphClient created");
    }

    @PostConstruct
    public TitanOperationStatus createGraph() {

        logger.info("** createGraph started **");
        graph = TitanFactory.build().set("storage.backend", "inmemory").open();
        createTitanSchema();

        logger.info("** in memory graph created");
        return TitanOperationStatus.OK;

    }


    public void cleanupGraph() {
        if (graph != null) {
            // graph.shutdown();
            graph.close();
            TitanCleanup.clear(graph);
        }
    }

    public TitanOperationStatus createGraph(String titanCfgFile) {
        logger.info("** open graph with {} started", titanCfgFile);
        try {
            logger.info("openGraph : try to load file {}", titanCfgFile);
            graph = TitanFactory.open(titanCfgFile);
            if (graph.isClosed()) {
                logger.error("titan graph was not initialized");
                return TitanOperationStatus.NOT_CREATED;
            }

        } catch (Exception e) {
            this.graph = null;
            logger.info("createGraph : failed to open Titan graph with configuration file: {}", titanCfgFile, e);
            return TitanOperationStatus.NOT_CONNECTED;
        }

        logger.info("** Titan graph created ");

        return TitanOperationStatus.OK;
    }


    public Either<TitanGraph, TitanOperationStatus> getGraph() {
        if (graph != null) {
            return Either.left(graph);
        } else {
            return Either.right(TitanOperationStatus.NOT_CREATED);
        }
    }

    public TitanOperationStatus commit() {
        if (graph != null) {
            try {
                graph.tx().commit();
                return TitanOperationStatus.OK;
            } catch (Exception e) {
                return handleTitanException(e);
            }
        } else {
            return TitanOperationStatus.NOT_CREATED;
        }
    }

    public TitanOperationStatus rollback() {
        if (graph != null) {
            try {
                // graph.rollback();
                graph.tx().rollback();
                return TitanOperationStatus.OK;
            } catch (Exception e) {
                return handleTitanException(e);
            }
        } else {
            return TitanOperationStatus.NOT_CREATED;
        }
    }

    public static TitanOperationStatus handleTitanException(Exception e) {
        if (e instanceof TitanConfigurationException) {
            return TitanOperationStatus.TITAN_CONFIGURATION;
        }
        if (e instanceof SchemaViolationException) {
            return TitanOperationStatus.TITAN_SCHEMA_VIOLATION;
        }
        if (e instanceof PermanentLockingException) {
            return TitanOperationStatus.TITAN_SCHEMA_VIOLATION;
        }
        if (e instanceof IDPoolExhaustedException) {
            return TitanOperationStatus.GENERAL_ERROR;
        }
        if (e instanceof InvalidElementException) {
            return TitanOperationStatus.INVALID_ELEMENT;
        }
        if (e instanceof InvalidIDException) {
            return TitanOperationStatus.INVALID_ID;
        }
        if (e instanceof QueryException) {
            return TitanOperationStatus.INVALID_QUERY;
        }
        if (e instanceof ResourceUnavailableException) {
            return TitanOperationStatus.RESOURCE_UNAVAILABLE;
        }
        if (e instanceof IllegalArgumentException) {
            // TODO check the error message??
            return TitanOperationStatus.ILLEGAL_ARGUMENT;
        }

        return TitanOperationStatus.GENERAL_ERROR;
    }

    public boolean getHealth() {
        return true;
    }

    private boolean isGraphOpen() {
        return true;
    }


    private static final String TITAN_HEALTH_CHECK_STR = "titanHealthCheck";


    private void createTitanSchema() {

        TitanManagement graphMgt = graph.openManagement();
        TitanGraphIndex index = null;
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
