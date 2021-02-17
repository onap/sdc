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
package org.openecomp.sdc.be.config.validation;

import java.util.Map;
import org.junit.Test;

public class DeploymentArtifactHeatConfigurationTest {

    private DeploymentArtifactHeatConfiguration createTestSubject() {
        return new DeploymentArtifactHeatConfiguration();
    }

    @Test
    public void testGetHeat_template_version() throws Exception {
        DeploymentArtifactHeatConfiguration testSubject;
        String result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getHeat_template_version();
    }

    @Test
    public void testSetHeat_template_version() throws Exception {
        DeploymentArtifactHeatConfiguration testSubject;
        String heat_template_version = "";
        // default test
        testSubject = createTestSubject();
        testSubject.setHeat_template_version(heat_template_version);
    }

    @Test
    public void testGetResources() throws Exception {
        DeploymentArtifactHeatConfiguration testSubject;
        Map<String, Object> result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getResources();
    }

    @Test
    public void testSetResources() throws Exception {
        DeploymentArtifactHeatConfiguration testSubject;
        Map<String, Object> resources = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setResources(resources);
    }
}
