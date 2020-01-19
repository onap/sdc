/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (c) 2019 Samsung
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(MockitoJUnitRunner.class)
@PrepareForTest({ReportManager.class})
public class ReportManagerTest {

    private static final String VERTEX_1_ID = "testID1";
    private static final String TASK_1_FAILED_NAME = "testFailedTask1";
    private static final String TASK_1_NAME = "testTask1";
    private static final String VERTEX_2_ID = "testID2";
    private static final String TASK_2_NAME = "testTask2";
    private static final String TASK_2_FAILED_NAME = "testFailedTask2";
    private static final String UNIQUE_ID = "uniqueID";
    private static final String DUMMY_MESSAGE = "dummyMessage";
    private static final String VALIDATOR_NAME = "testValidatorNamed";
    private static final int COMPONENT_SUM = 0;

    private static final String EXPECTED_CSV_HEADER =
        "Vertex ID,Task Name,Success,Result Details,Result Description";
    private static final String EXPECTED_OUTPUT_FILE_HEADER =
        "-----------------------Validation Tool Results:-------------------------";
    private static final String EXPECTED_OUTPUT_FILE_SUMMARY =
        "-----------------------------------Validator Tool Summary-----------------------------------";

    private final SortedSet<String> failedTasksNames =
        new TreeSet<>(Arrays.asList(TASK_1_FAILED_NAME, TASK_2_FAILED_NAME));
    private final SortedSet<String> successTasksNames =
        new TreeSet<>(Arrays.asList(TASK_1_NAME, TASK_2_NAME));

    private VertexResult successResult = new VertexResult();

    @Mock
    GraphVertex vertexScanned;

	@Before
    public void setup() {
        String resourcePath = new File(Objects
            .requireNonNull(ReportManagerTest.class.getClassLoader().getResource("")).getFile())
                .getAbsolutePath();
        ValidationConfigManager.setOutputFullFilePath(resourcePath);
        ValidationConfigManager.setCsvReportFilePath(resourcePath);
        new ReportManager();

        successResult.setStatus(true);
    }

    @After
    public void clean() {
        ReportManagerHelper.cleanReports();
    }

    @Test
    public void testReportTaskEnd() {
        // when
        ReportManager.reportTaskEnd(VERTEX_1_ID, TASK_1_NAME, successResult);
        ReportManager.reportTaskEnd(VERTEX_2_ID, TASK_2_NAME, successResult);
        ReportManager.printAllResults();

        List reportCsvFile = ReportManagerHelper.getReportCsvFileAsList();

        // then
        assertNotNull(reportCsvFile);
        assertEquals(EXPECTED_CSV_HEADER, reportCsvFile.get(0));
        assertEquals(getCsvExpectedResult(VERTEX_1_ID, TASK_1_NAME), reportCsvFile.get(1));
        assertEquals(getCsvExpectedResult(VERTEX_2_ID, TASK_2_NAME), reportCsvFile.get(2));
    }

    @Test
    public void testAddFailedVertex() {
        // when
        ReportManager.addFailedVertex(TASK_1_NAME, VERTEX_1_ID);
        ReportManager.reportEndOfToolRun();

        List reportOutputFile = ReportManagerHelper.getReportOutputFileAsList();

        // then
        assertNotNull(reportOutputFile);

        assertEquals(EXPECTED_OUTPUT_FILE_HEADER, reportOutputFile.get(0));
        assertEquals(EXPECTED_OUTPUT_FILE_SUMMARY, reportOutputFile.get(2));
        assertEquals("Task: " + TASK_1_NAME, reportOutputFile.get(3));
        assertEquals("FailedVertices: [" + VERTEX_1_ID + "]", reportOutputFile.get(4));
    }

    @Test
    public void testPrintValidationTaskStatus() {
        // given
        when(vertexScanned.getUniqueId()).thenReturn(UNIQUE_ID);

        // when
        ReportManager.printValidationTaskStatus(vertexScanned, TASK_1_NAME, false);

        List reportOutputFile = ReportManagerHelper.getReportOutputFileAsList();

        // then
        assertNotNull(reportOutputFile);

        assertEquals(EXPECTED_OUTPUT_FILE_HEADER, reportOutputFile.get(0));
        assertEquals("-----------------------Vertex: " + UNIQUE_ID + ", Task " + TASK_1_NAME
            + " failed-----------------------", reportOutputFile.get(2));
    }

    @Test
    public void testWriteReportLineToFile() {
        // when
        ReportManager.writeReportLineToFile(DUMMY_MESSAGE);

        List reportOutputFile = ReportManagerHelper.getReportOutputFileAsList();

        // then
        assertNotNull(reportOutputFile);

        assertEquals(EXPECTED_OUTPUT_FILE_HEADER, reportOutputFile.get(0));
        assertEquals(DUMMY_MESSAGE, reportOutputFile.get(2));
    }

    @Test
    public void testReportValidatorTypeSummary() {
        // when
        ReportManager.reportValidatorTypeSummary(VALIDATOR_NAME, failedTasksNames, successTasksNames);

        List reportOutputFile = ReportManagerHelper.getReportOutputFileAsList();

        // then
        assertNotNull(reportOutputFile);
        assertEquals(EXPECTED_OUTPUT_FILE_HEADER, reportOutputFile.get(0));

        assertEquals("-----------------------ValidatorExecuter " + VALIDATOR_NAME
            + " Validation Summary-----------------------", reportOutputFile.get(2));
        assertEquals("Failed tasks: [" + TASK_1_FAILED_NAME + ", " + TASK_2_FAILED_NAME + "]",
            reportOutputFile.get(3));
        assertEquals("Success tasks: [" + TASK_1_NAME + ", " + TASK_2_NAME + "]",
            reportOutputFile.get(4));
    }

	@Test
	public void testReportStartValidatorRun() {
		// when
		ReportManager.reportStartValidatorRun(VALIDATOR_NAME, COMPONENT_SUM);

		List reportOutputFile = ReportManagerHelper.getReportOutputFileAsList();

		// then
        assertNotNull(reportOutputFile);
        assertEquals(EXPECTED_OUTPUT_FILE_HEADER, reportOutputFile.get(0));
        assertEquals("------ValidatorExecuter " + VALIDATOR_NAME + " Validation Started, on "
            + COMPONENT_SUM + " components---------", reportOutputFile.get(2));
	}

    @Test
    public void testReportStartTaskRun() {
        // given
        when(vertexScanned.getUniqueId()).thenReturn(UNIQUE_ID);

        // when
        ReportManager.reportStartTaskRun(vertexScanned, TASK_1_NAME);

        List reportOutputFile = ReportManagerHelper.getReportOutputFileAsList();

        // then
        assertNotNull(reportOutputFile);
        assertEquals(EXPECTED_OUTPUT_FILE_HEADER, reportOutputFile.get(0));
        assertEquals("-----------------------Vertex: " + UNIQUE_ID + ", Task " + TASK_1_NAME
                + " Started-----------------------", reportOutputFile.get(2));
    }

    private String getCsvExpectedResult(String vertexID, String taskID) {
        return String.join(",", new String[] {vertexID, taskID,
            String.valueOf(successResult.getStatus()), successResult.getResult()});
    }
}