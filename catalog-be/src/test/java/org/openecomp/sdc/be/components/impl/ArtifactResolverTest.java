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

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

public class ArtifactResolverTest {

    private ArtifactResolverImpl testInstance = new ArtifactResolverImpl();
    private Service service, noArtifactsService;
    private Resource resource, noArtifactsResource;
    private ComponentInstance componentInstance, noArtifactsInstance;

    @Before
    public void setUp() throws Exception {
        noArtifactsService = new Service();
        noArtifactsResource = new Resource();
        resource = new Resource();
        service = new Service();
        componentInstance = new ComponentInstance();
        noArtifactsInstance = new ComponentInstance();

        ArtifactDefinition artifact1 = new ArtifactDefinition();
        artifact1.setUniqueId("a1");

        ArtifactDefinition artifact2 = new ArtifactDefinition();
        artifact2.setUniqueId("a2");

        ArtifactDefinition artifact3 = new ArtifactDefinition();
        artifact3.setUniqueId("a3");

        Map<String, ArtifactDefinition> artifact1Map = Collections.singletonMap("key1", artifact1);
        Map<String, ArtifactDefinition> artifact2Map = Collections.singletonMap("key1", artifact2);
        Map<String, ArtifactDefinition> artifact3Map = Collections.singletonMap("key1", artifact3);

        resource.setDeploymentArtifacts(artifact1Map);
        resource.setArtifacts(artifact2Map);

        service.setDeploymentArtifacts(artifact1Map);
        service.setArtifacts(artifact2Map);
        service.setServiceApiArtifacts(artifact3Map);

        componentInstance.setDeploymentArtifacts(artifact1Map);
        componentInstance.setArtifacts(artifact2Map);
    }

    @Test
    public void findArtifactOnComponent_noArtifactsOnComponent() throws Exception {
        assertNull(testInstance.findArtifactOnComponent(noArtifactsResource, ComponentTypeEnum.RESOURCE, "someId"));
        assertNull(testInstance.findArtifactOnComponent(noArtifactsService, ComponentTypeEnum.SERVICE, "someId"));
    }

    @Test
    public void findArtifactOnComponent_resource() throws Exception {
        assertNull(testInstance.findArtifactOnComponent(resource, ComponentTypeEnum.RESOURCE, "someId"));
        assertNotNull(testInstance.findArtifactOnComponent(resource, ComponentTypeEnum.RESOURCE, "a1"));
        assertNotNull(testInstance.findArtifactOnComponent(resource, ComponentTypeEnum.RESOURCE, "a2"));
    }

    @Test
    public void findArtifactOnComponent_service() throws Exception {
        assertNull(testInstance.findArtifactOnComponent(service, ComponentTypeEnum.SERVICE, "someId"));
        assertNotNull(testInstance.findArtifactOnComponent(service, ComponentTypeEnum.SERVICE, "a1"));
        assertNotNull(testInstance.findArtifactOnComponent(service, ComponentTypeEnum.SERVICE, "a2"));
        assertNotNull(testInstance.findArtifactOnComponent(service, ComponentTypeEnum.SERVICE, "a3"));
    }

    @Test
    public void findArtifactOnInstance_instanceHasNoArtifacts() throws Exception {
        assertNull(testInstance.findArtifactOnComponentInstance(noArtifactsInstance, "someId"));
    }

    @Test
    public void findArtifactOnInstance() throws Exception {
        assertNull(testInstance.findArtifactOnComponentInstance(componentInstance, "someId"));
        assertNotNull(testInstance.findArtifactOnComponentInstance(componentInstance, "a1"));
        assertNotNull(testInstance.findArtifactOnComponentInstance(componentInstance, "a2"));
    }
}
