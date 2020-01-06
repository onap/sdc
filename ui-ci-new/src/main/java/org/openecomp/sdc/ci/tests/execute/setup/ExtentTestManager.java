/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.ci.tests.execute.setup;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import java.util.HashMap;
import org.openecomp.sdc.ci.tests.api.TestManager;

public class ExtentTestManager implements TestManager {

    private final HashMap<Long, ExtentTest> extentTestByThreadIdMap = new HashMap<>();
    private final ExtentReports extent = ExtentManager.getReporter();
    private static final ExtentTestManager INSTANCE = new ExtentTestManager();

    private ExtentTestManager() {

    }

    public static ExtentTestManager getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized ExtentTest getTest() {
        return extentTestByThreadIdMap.get(Thread.currentThread().getId());
    }

    public synchronized void endTest() {
        extent.flush();
    }

    public synchronized ExtentTest startTest(final String testName) {
        return startTest(testName, "");
    }

    public synchronized ExtentTest startTest(final String testName, final String desc) {
        final ExtentTest test = extent.createTest(testName, desc);
        extentTestByThreadIdMap.put(Thread.currentThread().getId(), test);

        return test;
    }

    public synchronized <T> void assignCategory(Class<T> clazz) {
        String[] parts = clazz.getName().split("\\.");
        String lastOne1 = parts[parts.length - 1];
        String lastOne2 = parts[parts.length - 2];
        extentTestByThreadIdMap.get(Thread.currentThread().getId()).assignCategory(lastOne2 + "-" + lastOne1);
    }
}

