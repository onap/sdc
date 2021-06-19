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

package org.openecomp.sdc.be.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

class GroupInstanceTest {

    private GroupInstance createTestSubject() {
        return new GroupInstance();
    }

    @Test
    void testCtor() throws Exception {
        Assertions.assertNotNull(new GroupInstance(new GroupInstanceDataDefinition()));
    }

    @Test
    void testConvertToGroupInstancesProperties() throws Exception {
        final GroupInstance testSubject = createTestSubject();
        List<GroupInstanceProperty> result;

        result = testSubject.convertToGroupInstancesProperties();
        Assertions.assertNull(result);

        final List<PropertyDataDefinition> properties = new LinkedList<>();
        properties.add(new PropertyDataDefinition());
        testSubject.setProperties(properties);
        result = testSubject.convertToGroupInstancesProperties();
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void testConvertFromGroupInstancesProperties() throws Exception {
        final GroupInstance testSubject = createTestSubject();
        List<GroupInstanceProperty> groupInstancesProperties = null;

        groupInstancesProperties = null;
        testSubject.convertFromGroupInstancesProperties(groupInstancesProperties);
        List<PropertyDataDefinition> properties = testSubject.getProperties();
        Assertions.assertNull(properties);

        groupInstancesProperties = new LinkedList<>();
        groupInstancesProperties.add(new GroupInstanceProperty());
        testSubject.convertFromGroupInstancesProperties(groupInstancesProperties);
        properties = testSubject.getProperties();
        Assertions.assertNotNull(properties);
        Assertions.assertFalse(properties.isEmpty());
    }

    @Test
    void testAlignArtifactsUuid() throws Exception {
        final GroupInstance testSubject = createTestSubject();
        Map<String, ArtifactDefinition> deploymentArtifacts = null;

        testSubject.alignArtifactsUuid(deploymentArtifacts);
        Assertions.assertNull(testSubject.getArtifacts());

        deploymentArtifacts = new HashMap<>();

        final List<String> artifacts = new ArrayList<>();
        artifacts.add("mock1");
        testSubject.setArtifacts(artifacts);

        final List<String> artifactUuids = new ArrayList<>();
        artifactUuids.add("mock1");
        testSubject.setArtifactsUuid(artifactUuids);
        final var artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setArtifactUUID("mock1");
        deploymentArtifacts.put("mock1", artifactDefinition);
        testSubject.alignArtifactsUuid(deploymentArtifacts);
        List<String> groupInstanceArtifactsUuid = testSubject.getGroupInstanceArtifactsUuid();
        Assertions.assertNotNull(groupInstanceArtifactsUuid);
        Assertions.assertTrue(groupInstanceArtifactsUuid.isEmpty());

        artifactDefinition.setArtifactUUID("mock2");
        artifactDefinition.setArtifactType("HEAT_ENV");
        deploymentArtifacts.put("mock2", artifactDefinition);
        testSubject.alignArtifactsUuid(deploymentArtifacts);
        groupInstanceArtifactsUuid = testSubject.getGroupInstanceArtifactsUuid();
        Assertions.assertNotNull(groupInstanceArtifactsUuid);
        Assertions.assertFalse(groupInstanceArtifactsUuid.isEmpty());
        List<String> artifactsUuid = testSubject.getArtifactsUuid();
        Assertions.assertNotNull(artifactsUuid);
        Assertions.assertTrue(artifactsUuid.isEmpty());

        testSubject.setArtifactsUuid(null);
        testSubject.alignArtifactsUuid(deploymentArtifacts);
        artifactsUuid = testSubject.getArtifactsUuid();
        Assertions.assertNotNull(artifactsUuid);
        Assertions.assertTrue(artifactsUuid.isEmpty());

        List<String> groupInstanceArtifacts = new ArrayList<>();
        groupInstanceArtifacts.add("mock1");
        testSubject.setGroupInstanceArtifacts(groupInstanceArtifacts);
        testSubject.setGroupInstanceArtifactsUuid(null);
        testSubject.alignArtifactsUuid(deploymentArtifacts);
        groupInstanceArtifactsUuid = testSubject.getGroupInstanceArtifactsUuid();
        Assertions.assertNotNull(groupInstanceArtifactsUuid);
        Assertions.assertFalse(groupInstanceArtifactsUuid.isEmpty());

        testSubject.setGroupInstanceArtifactsUuid(groupInstanceArtifacts);
        testSubject.alignArtifactsUuid(deploymentArtifacts);
        groupInstanceArtifactsUuid = testSubject.getGroupInstanceArtifactsUuid();
        Assertions.assertNotNull(groupInstanceArtifactsUuid);
        Assertions.assertFalse(groupInstanceArtifactsUuid.isEmpty());
    }

}
