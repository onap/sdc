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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager.csvReportFilePath;
import static org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager.txtReportFilePath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReportManager.class})
public class ValidationConfigManagerTest {

    @Test
    public void testTxtReportFilePath() {
        String randomOutput = System.currentTimeMillis() + "";
        assertThat(txtReportFilePath(randomOutput), equalTo(randomOutput + "/reportOutput.txt"));
    }

    @Test
    public void testCsvReportFilePath() {
        String randomOutput = System.currentTimeMillis() + "";
        long millis = System.currentTimeMillis();
        assertThat(
            csvReportFilePath(randomOutput, () -> millis),
            is(randomOutput + "/csvSummary_" + millis + ".csv"));
    }
}
