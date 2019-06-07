/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.asdctool.main;

import static org.junit.Assert.assertEquals;

import java.nio.file.NoSuchFileException;
import java.security.Permission;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class ExportImportMenuTest{

    private static final String EXPORT_USAGE = "Usage: export <titan.properties> <output directory>\n";
    private static final String EXPORT_AS_GRAPH_ML_USAGE = "Usage: export-as-graph-ml <titan.properties> <output directory>\n";
    private static final String IMPORT_USAGE = "Usage: import <titan.properties> <graph file location>\n";
    private static final String EXPORT_USERS_USAGE = "Usage: exportusers <titan.properties> <output directory>\n";
    private static final String EXPORT_WITH_REPORT_USAGE = "Usage: export-as-graph-ml-with-data-report <titan.properties> <output directory>\n";
    private static final String DATA_REPORT_USAGE = "Usage: get-data-report-from-graph-ml <full path of .graphml file>\n";
    private static final String VALIDATE_JSON_USAGE = "Usage: validate-json <export graph path>\n";
    private static final String FIND_PROBLEM_USAGE = "Usage: findproblem <titan.properties> <graph file location>\n";
    private static final String USAGE = DATA_REPORT_USAGE + EXPORT_USAGE + EXPORT_AS_GRAPH_ML_USAGE + EXPORT_USERS_USAGE
        + EXPORT_WITH_REPORT_USAGE + FIND_PROBLEM_USAGE + IMPORT_USAGE + VALIDATE_JSON_USAGE;
    private static final String PARAM_3 = "param3";
    private static final String PARAM_2 = "param2";
    private static final String EXPORT = "export";
    private static final String EXPORT_AS_GRAPH_ML = "export-as-graph-ml";
    private static final String NONEXISTENT = "nonexistent";
    private static final String IMPORT = "import";
    private static final String EXPORT_USERS = "exportusers";
    private static final String DATA_REPORT = "get-data-report-from-graph-ml";
    private static final String FIND_PROBLEM = "findproblem";
    private static final String VALIDATE_JSON = "validate-json";
    private static final String EXPORT_WITH_REPORT = "export-as-graph-ml-with-data-report";

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void testOfMainWithInvalidLengthOfArgs() throws Exception {
        String [] args = {};
        exit.expectSystemExitWithStatus(1);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithInvalidLengthOfArgs() {
        String [] args = {};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, USAGE);
    }

    @Test
    public void testOfMainWithDefaultOperation() throws Exception {
        String [] args = {NONEXISTENT};
        exit.expectSystemExitWithStatus(1);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfOfMainWithDefaultOperation() {
        String [] args = {NONEXISTENT};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, USAGE);
    }

    @Test
    public void testOfMainWithExportOperationAndInvalidNoArgs() throws Exception {
        String [] args = {EXPORT};
        exit.expectSystemExitWithStatus(1);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithExportOperationAndInvalidNoArgs(){
        String [] args = {EXPORT};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, EXPORT_USAGE);
    }

    @Test
    public void testOfMainWithExportOperationAndValidNoArgs() throws Exception {
        String [] args = {EXPORT, PARAM_2, PARAM_3};
        exit.expectSystemExitWithStatus(2);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithExportUsersOperationAndInvalidNoArgs(){
        String [] args = {EXPORT_USERS};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, EXPORT_USERS_USAGE);
    }

    @Test
    public void testOfMainWithExportUsersOperationAndValidNoArgs() throws Exception {
        String [] args = {EXPORT_USERS, PARAM_2, PARAM_3};
        exit.expectSystemExitWithStatus(2);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithImportOperationAndInvalidNoArgs(){
        String [] args = {IMPORT};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, IMPORT_USAGE);
    }

    @Test
    public void testOfMainWithImportOperationAndValidNoArgs() throws Exception {
        String [] args = {IMPORT, PARAM_2, PARAM_3};
        exit.expectSystemExitWithStatus(2);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithDataReportOperationAndInvalidNoArgs(){
        String [] args = {DATA_REPORT};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, DATA_REPORT_USAGE);
    }

    @Test
    public void testOfMainWithDataReportOperationAndValidNoArgs() throws Exception {
        String [] args = {DATA_REPORT, PARAM_2};
        exit.expectSystemExitWithStatus(2);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithExportAsGraphMLOperationAndInvalidNoArgs(){
        String [] args = {EXPORT_AS_GRAPH_ML};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, EXPORT_AS_GRAPH_ML_USAGE);
    }

    @Test
    public void testMainWithExportAsGraphMLOperationAndInvalidNoArgs() throws Exception {
        String [] args = {EXPORT_AS_GRAPH_ML};
        exit.expectSystemExitWithStatus(1);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOfMainWithExportAsGraphMLOperationAndValidNoArgs() throws Exception {
        String [] args = {EXPORT_AS_GRAPH_ML, PARAM_2, PARAM_3};
        exit.expectSystemExitWithStatus(2);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithFindProblemOperationAndInvalidNoArgs(){
        String [] args = {FIND_PROBLEM};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, FIND_PROBLEM_USAGE);
    }

    @Test
    public void testMainWithFindProblemOperationAndInvalidNoArgs() throws Exception {
        String [] args = {FIND_PROBLEM};
        exit.expectSystemExitWithStatus(1);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOfMainWithFindProblemOperationAndValidNoArgs() throws Exception {
        String [] args = {FIND_PROBLEM, PARAM_2, PARAM_3};
        exit.expectSystemExitWithStatus(2);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithExportWithReportOperationAndInvalidNoArgs(){
        String [] args = {EXPORT_WITH_REPORT};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, EXPORT_WITH_REPORT_USAGE);
    }

    @Test
    public void testMainWithExportWithReportOperationAndInvalidNoArgs() throws Exception {
        String [] args = {EXPORT_WITH_REPORT};
        exit.expectSystemExitWithStatus(1);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOfMainWithExportWithReportOperationAndValidNoArgs() throws Exception {
        String [] args = {EXPORT_WITH_REPORT, PARAM_2, PARAM_3};
        exit.expectSystemExitWithStatus(2);
        ExportImportMenu.main(args);
    }

    @Test
    public void testOutputOfMainWithValidateJsonOperationAndInvalidNoArgs(){
        String [] args = {VALIDATE_JSON};
        callMainWithoutSystemExit(args);
        String log = systemOutRule.getLog();
        assertEquals(log, VALIDATE_JSON_USAGE);
    }

    @Test
    public void testMainWithValidateJsonOperationAndInvalidNoArgs() throws Exception {
        String [] args = {VALIDATE_JSON};
        exit.expectSystemExitWithStatus(1);
        ExportImportMenu.main(args);
    }

    @Test(expected = NoSuchFileException.class)
    public void testOfMainWithValidateJsonOperationAndValidNoArgs() throws Exception {
        String [] args = {VALIDATE_JSON, PARAM_2, PARAM_3};
        ExportImportMenu.main(args);
    }

    private void callMainWithoutSystemExit(String[] params) {

        class NoExitException extends RuntimeException {}

        SecurityManager securityManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager(){

            @Override
            public void checkPermission(Permission permission) {
            }

            @Override
            public void checkPermission(Permission permission, Object o) {
            }

            @Override
            public void checkExit(int status) {
                super.checkExit(status);
                throw new NoExitException();
            }
        });
        try {
            ExportImportMenu.main(params);
        }catch (Exception ignore){}
        System.setSecurityManager(securityManager);
    }

}