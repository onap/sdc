package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

import java.util.Set;

public class ReportManagerTest {

	@Test
	public void testReportTaskEnd() throws Exception {
		String vertexId = "";
		String taskName = "";
		VertexResult result = null;

		// default test
		ReportManager.reportTaskEnd(vertexId, taskName, result);
	}

	@Test
	public void testAddFailedVertex() throws Exception {
		String taskName = "";
		String vertexId = "";

		// default test
		ReportManager.addFailedVertex(taskName, vertexId);
	}

	@Test(expected = NullPointerException.class)
	public void testPrintValidationTaskStatus() throws Exception {
		GraphVertex vertexScanned = null;
		String taskName = "";
		boolean success = false;

		// default test
		ReportManager.printValidationTaskStatus(vertexScanned, taskName, success);
	}

	@Test(expected = NullPointerException.class)
	public void testWriteReportLineToFile() throws Exception {
		String message = "";

		// default test
		ReportManager.writeReportLineToFile(message);
	}

	@Test(expected = NullPointerException.class)
	public void testReportValidatorTypeSummary() throws Exception {
		String validatorName = "";
		Set<String> failedTasksNames = null;
		Set<String> successTasksNames = null;

		// default test
		ReportManager.reportValidatorTypeSummary(validatorName, failedTasksNames, successTasksNames);
	}

	@Test(expected = NullPointerException.class)
	public void testReportStartValidatorRun() throws Exception {
		String validatorName = "";
		int componenentsNum = 0;

		// default test
		ReportManager.reportStartValidatorRun(validatorName, componenentsNum);
	}

	@Test(expected = NullPointerException.class)
	public void testReportStartTaskRun() throws Exception {
		GraphVertex vertex = null;
		String taskName = "";

		// default test
		ReportManager.reportStartTaskRun(vertex, taskName);
	}

	@Test(expected = NullPointerException.class)
	public void testReportEndOfToolRun() throws Exception {

		// default test
		ReportManager.reportEndOfToolRun();
	}

	@Test(expected = NullPointerException.class)
	public void testPrintAllResults() throws Exception {

		// default test
		ReportManager.printAllResults();
	}
}