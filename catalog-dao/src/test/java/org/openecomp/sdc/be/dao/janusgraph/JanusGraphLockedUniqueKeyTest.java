/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.dao.janusgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.SchemaViolationException;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JanusGraphLockedUniqueKeyTest {

    private JanusGraph graph;

    @BeforeEach
    void openGraphWithLockedUniqueKey() {
        graph = JanusGraphFactory.build().set("storage.backend", "inmemory").open();
        JanusGraphManagement management = graph.openManagement();
        PropertyKey uid = management.makePropertyKey("uid").dataType(String.class).make();
        JanusGraphIndex index = management.buildIndex("uidUnique", Vertex.class).addKey(uid).unique().buildCompositeIndex();
        management.setConsistency(uid, ConsistencyModifier.LOCK);
        management.setConsistency(index, ConsistencyModifier.LOCK);
        management.commit();
    }

    @AfterEach
    void closeGraph() {
        graph.close();
    }

    @Test
    void commitsAWriteToALockedUniqueKey() {
        graph.addVertex("uid", "a");
        graph.tx().commit();

        assertEquals(1L, graph.traversal().V().has("uid", "a").count().next());
    }

    @Test
    void rejectsADuplicateValueOfALockedUniqueKey() {
        graph.addVertex("uid", "a");
        graph.tx().commit();

        assertThrows(SchemaViolationException.class, () -> {
            graph.addVertex("uid", "a");
            graph.tx().commit();
        });
    }
}
