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

package org.openecomp.sdc.asdctool.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.janusgraph.core.JanusGraph;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class GraphMLConverterTest {
	
	public GraphMLConverter createTestSubject() {
		return new GraphMLConverter();
	}
	
	@Test
	public void testImportGraph() throws Exception {
		GraphMLConverter testSubject;
		String[] args = new String[] { "" };
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.importGraph(args);
	}

	@Test
	public void testExportGraph() throws Exception {
		GraphMLConverter testSubject;
		String[] args = new String[] { "" };
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportGraph(args);
	}

	@Test
	public void testExportGraphMl() throws Exception {
		GraphMLConverter testSubject;
		String[] args = new String[] { "" };
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportGraphMl(args);
	}

	@Test
	public void testFindErrorInJsonGraph() throws Exception {
		GraphMLConverter testSubject;
		String[] args = new String[] { "" };
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findErrorInJsonGraph(args);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testOpenGraph() throws Exception {
		GraphMLConverter testSubject;
		String janusGraphFileLocation = "";
		JanusGraph result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.openGraph("src/main/resources/config/janusgraph.properties");
	}

	@Test(expected=NullPointerException.class)
	public void testExportJsonGraph() throws Exception {
		GraphMLConverter testSubject;
		JanusGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportJsonGraph(graph, outputDirectory);
	}

	@Test(expected=NullPointerException.class)
	public void testExportGraphMl_1() throws Exception {
		GraphMLConverter testSubject;
		JanusGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportGraphMl(graph, outputDirectory);
	}

	@Test
	public void testImportJsonGraph() throws Exception {
		GraphMLConverter testSubject;
		JanusGraph graph = null;
		String graphJsonFile = "";
		List<ImmutablePair<String, String>> propertiesCriteriaToDelete = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.importJsonGraph(graph, graphJsonFile, propertiesCriteriaToDelete);
	}

	@Test(expected=NullPointerException.class)
	public void testFindErrorInJsonGraph_1() throws Exception {
		GraphMLConverter testSubject;
		JanusGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findErrorInJsonGraph(graph, outputDirectory);
	}


	@Test(expected=NullPointerException.class)
	public void testExportUsers() throws Exception {
		GraphMLConverter testSubject;
		JanusGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportUsers(graph, outputDirectory);
	}

	@Test(expected=NullPointerException.class)
	public void testGetProperties() throws Exception {
		GraphMLConverter testSubject;
		Element element = null;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties(element);
	}

	@Test
	public void testExportUsers_1() throws Exception {
		GraphMLConverter testSubject;
		String[] args = new String[] { "" };
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportUsers(args);
	}
}
