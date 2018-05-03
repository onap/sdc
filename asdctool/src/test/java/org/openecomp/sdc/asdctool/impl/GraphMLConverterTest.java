package org.openecomp.sdc.asdctool.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.junit.Test;

import com.thinkaurelius.titan.core.TitanGraph;

public class GraphMLConverterTest {
	
	public GraphMLConverter createTestSubject() {
		return new GraphMLConverter();
	}
	
	/*@Before
	public void createGraphTestSubject() {
		converter = new GraphMLConverter();
		openGraph = converter.openGraph("src/main/resources/config/titan.properties");
	}*/
	
	/*@After
	public void destroyGraphTestSubject() {
		converter = new GraphMLConverter();
		converter.clearGraph(openGraph);
	}*/

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
		String titanFileLocation = "";
		TitanGraph result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.openGraph("src/main/resources/config/titan.properties");
	}

	@Test(expected=NullPointerException.class)
	public void testExportJsonGraph() throws Exception {
		GraphMLConverter testSubject;
		TitanGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportJsonGraph(graph, outputDirectory);
	}

	@Test(expected=NullPointerException.class)
	public void testExportGraphMl_1() throws Exception {
		GraphMLConverter testSubject;
		TitanGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportGraphMl(graph, outputDirectory);
	}

	@Test
	public void testImportJsonGraph() throws Exception {
		GraphMLConverter testSubject;
		TitanGraph graph = null;
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
		TitanGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.findErrorInJsonGraph(graph, outputDirectory);
	}

	@Test
	public void testClearGraph() throws Exception {
		TitanGraph graph = null;

		// default test
		//GraphMLConverter.clearGraph("src/main/resources/config/titan.properties");
	}

	@Test(expected=NullPointerException.class)
	public void testExportUsers() throws Exception {
		GraphMLConverter testSubject;
		TitanGraph graph = null;
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