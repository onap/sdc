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

package org.openecomp.sdc.be.components.scheduledtasks;

import java.util.concurrent.ExecutorService;
import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;

public class AsdcComponentsCleanerTaskTest extends BeConfDependentTest {

    private AsdcComponentsCleanerTask createTestSubject() {
        return new AsdcComponentsCleanerTask();
    }

    @Test
    public void testInit() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.init();
    }

    @Test
    public void testDestroy() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.destroy();
    }

    @Test
    public void testStartTask() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.startTask();
    }

    @Test
    public void testStopTask() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.stopTask();
        testSubject.init();
        testSubject.startTask();
        testSubject.stopTask();
    }

    @Test
    public void testRun() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.run();
    }

    @Test
    public void testGetExecutorService() throws Exception {
        AsdcComponentsCleanerTask testSubject;
        ExecutorService result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getExecutorService();
    }
}
