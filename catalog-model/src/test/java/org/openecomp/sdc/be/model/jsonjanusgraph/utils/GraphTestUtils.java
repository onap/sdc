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

package org.openecomp.sdc.be.model.jsonjanusgraph.utils;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphVertex;
import fj.data.Either;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.jsongraph.utils.IdBuilderUtils;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class GraphTestUtils {

    public static GraphVertex createRootCatalogVertex(JanusGraphDao janusGraphDao) {
        GraphVertex catalogRootVertex = new GraphVertex(VertexTypeEnum.CATALOG_ROOT);
        catalogRootVertex.setUniqueId(IdBuilderUtils.generateUniqueId());
        return janusGraphDao.createVertex(catalogRootVertex)
                .either(v -> v, s -> null);
    }

    public static GraphVertex createRootArchiveVertex(JanusGraphDao janusGraphDao) {
        GraphVertex archiveRootVertex = new GraphVertex(VertexTypeEnum.ARCHIVE_ROOT);
        archiveRootVertex.setUniqueId(IdBuilderUtils.generateUniqueId());
        return janusGraphDao.createVertex(archiveRootVertex)
                .either(v -> v, s -> null);
    }

    public static GraphVertex createResourceVertex(JanusGraphDao janusGraphDao, Map<GraphPropertyEnum,Object> metadataProps, ResourceTypeEnum type) {
        GraphVertex vertex = new GraphVertex();
        if (type == ResourceTypeEnum.VF) {
            vertex.setLabel(VertexTypeEnum.TOPOLOGY_TEMPLATE);
            vertex.addMetadataProperty(GraphPropertyEnum.LABEL, VertexTypeEnum.TOPOLOGY_TEMPLATE);
        } else {
            vertex.setLabel(VertexTypeEnum.NODE_TYPE);
            vertex.addMetadataProperty(GraphPropertyEnum.LABEL, VertexTypeEnum.NODE_TYPE);
        }
        String uuid = UUID.randomUUID().toString();
        vertex.setUniqueId(uuid);

        vertex.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, uuid);
        vertex.addMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.RESOURCE.name());
        vertex.addMetadataProperty(GraphPropertyEnum.RESOURCE_TYPE, type.name());
        vertex.addMetadataProperty(GraphPropertyEnum.IS_ABSTRACT, false);
        for (Map.Entry<GraphPropertyEnum, Object> prop : metadataProps.entrySet()) {
            vertex.addMetadataProperty(prop.getKey(), prop.getValue());
        }
        janusGraphDao.createVertex(vertex);
        janusGraphDao.commit();
        return vertex;
    }

    public static GraphVertex createServiceVertex(JanusGraphDao janusGraphDao, Map<GraphPropertyEnum, Object> metadataProps){
        GraphVertex vertex = new GraphVertex(VertexTypeEnum.TOPOLOGY_TEMPLATE);
        String uuid = UUID.randomUUID().toString();
        vertex.setUniqueId(uuid);
        vertex.addMetadataProperty(GraphPropertyEnum.LABEL, VertexTypeEnum.TOPOLOGY_TEMPLATE);
        vertex.addMetadataProperty(GraphPropertyEnum.UNIQUE_ID, uuid);
        vertex.addMetadataProperty(GraphPropertyEnum.COMPONENT_TYPE, ComponentTypeEnum.SERVICE.name());
        for (Map.Entry<GraphPropertyEnum, Object> prop : metadataProps.entrySet()) {
            vertex.addMetadataProperty(prop.getKey(), prop.getValue());
        }
        janusGraphDao.createVertex(vertex);
        janusGraphDao.commit();
        return vertex;
    }

    public static void clearGraph(JanusGraphDao janusGraphDao) {
        Either<JanusGraph, JanusGraphOperationStatus> graphResult = janusGraphDao.getGraph();
        JanusGraph graph = graphResult.left().value();

        Iterable<JanusGraphVertex> vertices = graph.query().vertices();
        if (vertices != null) {
            Iterator<JanusGraphVertex> iterator = vertices.iterator();
            while (iterator.hasNext()) {
                JanusGraphVertex vertex = iterator.next();
                vertex.remove();
            }
        }
        janusGraphDao.commit();
    }

    public static String exportGraphMl(JanusGraph graph, String outputDirectory) {
        String result = null;
        String outputFile = outputDirectory + File.separator + "exportGraph." + System.currentTimeMillis() + ".graphml";
        try {
            try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(os, graph);
            }
            result = outputFile;
            graph.tx().commit();
        } catch (Exception e) {
            graph.tx().rollback();
            e.printStackTrace();
        }
        return result;
    }
}
