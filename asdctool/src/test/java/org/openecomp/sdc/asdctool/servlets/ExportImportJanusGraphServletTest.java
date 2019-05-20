package org.openecomp.sdc.asdctool.servlets;

import org.janusgraph.core.JanusGraph;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.File;

public class ExportImportJanusGraphServletTest {

	private ExportImportJanusGraphServlet createTestSubject() {
		return new ExportImportJanusGraphServlet();
	}

	@Test(expected=NullPointerException.class)
	public void testExport() throws Exception {
		ExportImportJanusGraphServlet testSubject;
		File janusGraphPropertiesFile = null;
		String exportGraphMetadata = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.export(janusGraphPropertiesFile, exportGraphMetadata);
	}

	@Test(expected=NullPointerException.class)
	public void testExportGraph() throws Exception {
		ExportImportJanusGraphServlet testSubject;
		JanusGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportGraph(graph, outputDirectory);
	}
}