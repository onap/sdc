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

package org.onap.sdc.backend.ci.tests.api;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import java.util.HashMap;
import java.util.Map;

public class ExtentTestManager implements SomeInterface {

    private static final Map<Long, ExtentTest> extentTestMap = new HashMap<>();
    private static final ExtentReports extent = ExtentManager.getReporter();

    public static synchronized void endTest() {
        extent.flush();
    }

    public static synchronized void startTest(String testName) {
        extentTestMap.put(Thread.currentThread().getId(), extent.createTest(testName));
    }

    public static synchronized <T> void assignCategory(Class<T> clazz) {
        String[] parts = clazz.getName().split("\\.");
        String lastOne1 = parts[parts.length - 1];
        String lastOne2 = parts[parts.length - 2];
        extentTestMap.get(Thread.currentThread().getId()).assignCategory(lastOne2 + "-" + lastOne1);
    }

    @Override
    public synchronized ExtentTest getTest() {
        return extentTestMap.get(Thread.currentThread().getId());
    }

}
