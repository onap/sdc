/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.Multiplicity;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;

public class JanusGraphInitializerTest {

	private JanusGraph graph;

	@Before
	public void openGraph() {
		graph = JanusGraphFactory.build().set("storage.backend", "inmemory").open();
	}

	@After
	public void closeGraph() {
		graph.close();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateGraph() throws Exception {
		String janusGraphCfgFile = "";
		boolean result;

		// default test
		result = JanusGraphInitializer.createGraph("");
		
	}

	@Test
	public void createEdgeLabelsCreatesEveryLabelTheBackendWrites() {
		createEdgeLabels();

		JanusGraphManagement graphMgt = graph.openManagement();
		for (EdgeLabelEnum label : EdgeLabelEnum.values()) {
			assertTrue(label.name(), graphMgt.containsEdgeLabel(label.name()));
		}
		for (GraphEdgeLabels label : GraphEdgeLabels.values()) {
			assertTrue(label.getProperty(), graphMgt.containsEdgeLabel(label.getProperty()));
		}
		assertEquals(Multiplicity.MULTI, graphMgt.getEdgeLabel(EdgeLabelEnum.INPUTS.name()).multiplicity());
		graphMgt.rollback();
	}

	@Test
	public void createEdgeLabelsKeepsLabelsThatAlreadyExist() {
		JanusGraphManagement graphMgt = graph.openManagement();
		graphMgt.makeEdgeLabel(EdgeLabelEnum.INPUTS.name()).multiplicity(Multiplicity.SIMPLE).make();
		graphMgt.commit();

		createEdgeLabels();
		createEdgeLabels();

		graphMgt = graph.openManagement();
		assertEquals(Multiplicity.SIMPLE, graphMgt.getEdgeLabel(EdgeLabelEnum.INPUTS.name()).multiplicity());
		graphMgt.rollback();
	}

	private void createEdgeLabels() {
		JanusGraphManagement graphMgt = graph.openManagement();
		JanusGraphInitializer.createEdgeLabels(graphMgt);
		graphMgt.commit();
	}
}
