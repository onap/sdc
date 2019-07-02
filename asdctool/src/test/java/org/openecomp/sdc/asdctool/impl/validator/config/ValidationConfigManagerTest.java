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

package org.openecomp.sdc.asdctool.impl.validator.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReportManager.class})
public class ValidationConfigManagerTest {

    @Test
    public void testGetOutputFilePath() {
        String result;

        // default test
        result = ValidationConfigManager.getOutputFilePath();
    }

    @Test
    public void testGetCsvReportFilePath() {
        String result;

        // default test
        result = ValidationConfigManager.getCsvReportFilePath();
    }

    @Test
    public void testSetCsvReportFilePath() {
        String outputPath = "";

        // default test
        ValidationConfigManager.setCsvReportFilePath(outputPath);
    }

    @Test
    public void testSetValidationConfiguration() {
        String path = "";
        Properties result;

        // default test
        result = ValidationConfigManager.setValidationConfiguration(path);
    }

    @Test
    public void testGetValidationConfiguration() {
        Properties result;

        // default test
        result = ValidationConfigManager.getValidationConfiguration();
    }

    @Test
    public void testGetOutputFullFilePath() throws Exception {
        String result;

        // default test
        result = ValidationConfigManager.getOutputFullFilePath();
    }

    @Test
    public void testSetOutputFullFilePath() throws Exception {
        String outputPath = "";

        // default test
        ValidationConfigManager.setOutputFullFilePath(outputPath);
    }
}
