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
