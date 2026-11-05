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

package org.openecomp.sdc.be.dao.janusgraph;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.graphdb.query.JanusGraphPredicate;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
public class JanusGraphUtilsTest {

    @Test
    public void exclusionIncludesVerticesWhenPropertyIsNotInSchema() {
        try (JanusGraph graph = JanusGraphFactory.build().set("storage.backend", "inmemory").open()) {
            graph.addVertex();
            graph.tx().commit();

            assertTrue(JanusGraphUtils.hasNotIncludingMissing(graph, graph.query(), "unknown", true)
                .vertices().iterator().hasNext());
        }
    }

    @Test
    public void nullExclusionRequiresPropertyToExist() {
        try (JanusGraph graph = JanusGraphFactory.build().set("storage.backend", "inmemory").open()) {
            graph.addVertex();
            graph.tx().commit();
            assertFalse(JanusGraphUtils.hasNotIncludingMissing(graph, graph.query(), "flag", null)
                .vertices().iterator().hasNext());

            graph.addVertex().property("flag", false);
            graph.tx().commit();
            Iterator<JanusGraphVertex> vertices = JanusGraphUtils
                .hasNotIncludingMissing(graph, graph.query(), "flag", null).vertices().iterator();
            assertTrue(vertices.hasNext());
            assertFalse(vertices.next().value("flag"));
            assertFalse(vertices.hasNext());
        }
    }

	@Test
	public void testBuildNotInPredicate() throws Exception {
		String propKey = "";
		Collection<T> notInCollection = null;
		Map<String, Entry<JanusGraphPredicate, Object>> result;

		// default test
		result = JanusGraphUtils.buildNotInPredicate(propKey, notInCollection);
	}
}
