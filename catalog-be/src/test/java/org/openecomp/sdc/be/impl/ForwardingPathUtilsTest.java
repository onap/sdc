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

package org.openecomp.sdc.be.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javatuples.Pair;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.merge.instance.DataForMergeHolder;
import org.openecomp.sdc.be.datamodel.NameIdPair;
import org.openecomp.sdc.be.datamodel.NameIdPairWrapper;
import org.openecomp.sdc.be.datamodel.ServiceRelations;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathElementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;

public class ForwardingPathUtilsTest {

    private ForwardingPathUtils createTestSubject() {
        return new ForwardingPathUtils();
    }

    @Test
    public void testConvertServiceToServiceRelations() {
        ForwardingPathUtils testSubject;
        Service service = new Service();
        ServiceRelations result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.convertServiceToServiceRelations(service);
        assertThat(result).isInstanceOf(ServiceRelations.class);

        List<ComponentInstance> resourceInstances = new LinkedList<>();
        ComponentInstance e = new ComponentInstance();
        e.setCapabilities(new HashMap<>());
        resourceInstances.add(e);
        service.setComponentInstances(resourceInstances);

        result = testSubject.convertServiceToServiceRelations(service);
        assertThat(result).isInstanceOf(ServiceRelations.class);
    }

    @Test
    public void testCreateWrapper() {
        ForwardingPathUtils testSubject;
        NameIdPair cpOption = new NameIdPair("mock", "mock");
        NameIdPairWrapper result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.createWrapper(cpOption);
        assertThat(result).isInstanceOf(NameIdPairWrapper.class);
        assertThat(result.getId()).isEqualTo(cpOption.getId());
    }

    @Test
    public void testGetResponseFormatManager() {
        ForwardingPathUtils testSubject;
        ResponseFormatManager result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getResponseFormatManager();
        assertThat(result).isInstanceOf(ResponseFormatManager.class);
    }

    @Test
    public void testFindForwardingPathNamesToDeleteOnComponentInstanceDeletion() throws Exception {
        ForwardingPathUtils testSubject;
        Service containerService = new Service();
        containerService.setForwardingPaths(new HashMap<>());
        String componentInstanceId = "";
        Set<String> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.findForwardingPathNamesToDeleteOnComponentInstanceDeletion(containerService,
            componentInstanceId);
        assertThat(result).isInstanceOf(Set.class);
    }

    @Test
    public void testFindForwardingPathToDeleteOnCIDeletion() {
        ForwardingPathUtils testSubject;
        Service containerService = new Service();
        containerService.setForwardingPaths(new HashMap<>());
        String componentInstanceId = "";
        Map<String, ForwardingPathDataDefinition> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.findForwardingPathToDeleteOnCIDeletion(containerService, componentInstanceId);
        assertThat(result).isInstanceOf(Map.class);
    }

    @Test
    public void testElementContainsCI_1() {
        ForwardingPathUtils testSubject;
        ForwardingPathElementDataDefinition elementDataDefinitions = new ForwardingPathElementDataDefinition();
        elementDataDefinitions.setFromNode("mock");
        String componentInstanceId = "mock";
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.elementContainsCI(elementDataDefinitions, componentInstanceId);
        assertThat(result).isTrue();
    }

    @Test
    public void testUpdateForwardingPathOnVersionChange() {
        ForwardingPathUtils testSubject;
        Service containerService = new Service();
        containerService.setForwardingPaths(new HashMap<>());
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        Component updatedContainerComponent = new Service();
        String newInstanceId = "";
        Pair<Map<String, ForwardingPathDataDefinition>, Map<String, ForwardingPathDataDefinition>> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.updateForwardingPathOnVersionChange(containerService, dataHolder,
            updatedContainerComponent, newInstanceId);
        assertThat((Iterable<?>) result).isInstanceOf(Pair.class);
    }

    @Test
    public void testGetForwardingPathsToBeDeletedOnVersionChange() {
        ForwardingPathUtils testSubject;
        Service containerService = new Service();
        containerService.setForwardingPaths(new HashMap<>());
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        Component updatedContainerComponent = new Service();
        Set<String> result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.getForwardingPathsToBeDeletedOnVersionChange(containerService, dataHolder,
            updatedContainerComponent);
        assertThat(result).isInstanceOf(Set.class);
    }

    @Test
    public void testUpdateCI() {
        ForwardingPathUtils testSubject;
        ForwardingPathDataDefinition inFP = new ForwardingPathDataDefinition();
        inFP.setPathElements(new ListDataDefinition<>());
        String oldCI = "";
        String newCI = "";
        ForwardingPathDataDefinition result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.updateCI(inFP, oldCI, newCI);
        assertThat(result)
            .isInstanceOf(ForwardingPathDataDefinition.class)
            .isNotSameAs(inFP);
    }

    @Test
    public void testUpdateElement() {
        ForwardingPathUtils testSubject;
        ForwardingPathElementDataDefinition element = new ForwardingPathElementDataDefinition();
        element.setFromNode("mock");
        element.setToNode("mock");
        element.setToCP("mock");
        String oldCI = "";
        String newCI = "";
        ForwardingPathElementDataDefinition result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.updateElement(element, oldCI, newCI);
        assertThat(result)
            .isInstanceOf(ForwardingPathElementDataDefinition.class)
            .isNotSameAs(element);
    }

    @Test
    public void testElementContainsCIAndForwarder_1() {
        ForwardingPathUtils testSubject;
        ForwardingPathElementDataDefinition elementDataDefinitions = new ForwardingPathElementDataDefinition();
        elementDataDefinitions.setFromNode("mock");
        elementDataDefinitions.setToNode("mock");
        elementDataDefinitions.setToCP("mock");
        String oldCIId = "mock";
        Component newCI = new Resource();
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.elementContainsCIAndForwarder(elementDataDefinitions, oldCIId, newCI);
        assertThat(result).isFalse();
    }

    @Test
    public void testCiContainsForwarder() {
        ForwardingPathUtils testSubject;
        Component newCI = new Resource();
        String capabilityID = "mock";
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.ciContainsForwarder(newCI, capabilityID);
        newCI.setCapabilities(new HashMap<>());
        result = testSubject.ciContainsForwarder(newCI, capabilityID);
        assertThat(result).isFalse();
    }

    @Test
    public void testElementContainsCIAndDoesNotContainForwarder() throws Exception {
        ForwardingPathUtils testSubject;
        ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition();
        ListDataDefinition<ForwardingPathElementDataDefinition> pathElements = new ListDataDefinition<>();
        forwardingPathDataDefinition.setPathElements(pathElements);
        String oldCIId = "";
        Component newCI = new Resource();
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.elementContainsCIAndDoesNotContainForwarder(forwardingPathDataDefinition, oldCIId, newCI);
        assertThat(result).isFalse();
    }

    @Test
    public void testElementContainsCIAndDoesNotContainForwarder_1() throws Exception {
        ForwardingPathUtils testSubject;
        ForwardingPathElementDataDefinition elementDataDefinitions = new ForwardingPathElementDataDefinition();
        elementDataDefinitions.setFromNode("mock");
        elementDataDefinitions.setToNode("mock");
        elementDataDefinitions.setToCP("mock");
        String oldCIId = "";
        Component newCI = new Resource();
        boolean result;

        // default test
        testSubject = createTestSubject();
        result = testSubject.elementContainsCIAndDoesNotContainForwarder(elementDataDefinitions, oldCIId, newCI);
        assertThat(result).isFalse();
    }
}
