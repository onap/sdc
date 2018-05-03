package org.openecomp.sdc.asdctool.servlets;

import java.io.File;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.thinkaurelius.titan.core.TitanGraph;

public class ExportImportTitanServletTest {

	private ExportImportTitanServlet createTestSubject() {
		return new ExportImportTitanServlet();
	}

	@Test(expected=NullPointerException.class)
	public void testExport() throws Exception {
		ExportImportTitanServlet testSubject;
		File titanPropertiesFile = null;
		String exportGraphMetadata = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.export(titanPropertiesFile, exportGraphMetadata);
	}

	@Test(expected=NullPointerException.class)
	public void testExportGraph() throws Exception {
		ExportImportTitanServlet testSubject;
		TitanGraph graph = null;
		String outputDirectory = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.exportGraph(graph, outputDirectory);
	}
}