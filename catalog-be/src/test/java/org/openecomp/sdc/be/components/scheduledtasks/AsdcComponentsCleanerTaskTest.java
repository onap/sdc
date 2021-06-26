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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

class AsdcComponentsCleanerTaskTest extends BeConfDependentTest {

    private AsdcComponentsCleanerTask createTestSubject() {
        return new AsdcComponentsCleanerTask();
    }

    // TODO - remove this setup after migration to Junit5 BeConfDependentTest
    @BeforeAll
    private static void setup() {
        configurationManager =
            new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    void testInit() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.init();
    }

    @Test
    void testDestroy() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.destroy();
    }

    @Test
    void testStartTask() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.startTask();
    }

    @Test
    void testStopTask() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.init();
        testSubject.destroy();
    }

    @Test
    void testRun() throws Exception {
        AsdcComponentsCleanerTask testSubject;

        // default test
        testSubject = createTestSubject();
        testSubject.run();
    }

    @Test
    void testGetExecutorService() throws Exception {
        AsdcComponentsCleanerTask testSubject;
        ExecutorService result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getExecutorService();
        Assertions.assertNotNull(result);
    }
}
