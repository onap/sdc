/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openecomp.sdc.be.components.impl.utils.ComponentUtils.checkArtifactInComponent;
import static org.openecomp.sdc.be.components.impl.utils.ComponentUtils.checkArtifactInResourceInstance;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.aComponent;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.aComponentInstance;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.aResourceInstanceId;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.aUniqueId;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.anArtifactDefinition;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.anArtifactId;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.anOperation;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.someArtifacts;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.someComponentInstances;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.someInterfaces;
import static org.openecomp.sdc.be.components.impl.utils.TestDataUtils.someOperations;

import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Service;

public class ComponentUtilsTest {

    private static final String ARTIFACT_ID = "SomeArtifactId";

    @Test
    public void checkArtifactInComponentShouldReturnFalseWhenNoConditionsAreMet() {
        assertFalse(checkArtifactInComponent(aComponent(), ARTIFACT_ID));
    }

    @Test
    public void checkArtifactInComponentShouldReturnTrueWhenOneArtifactHasQueriedId() {
        Component component = aComponent();
        component.setArtifacts(someArtifacts(anArtifactDefinition(ARTIFACT_ID)));
        assertTrue(checkArtifactInComponent(component, ARTIFACT_ID));
    }

    @Test
    public void checkArtifactInComponentShouldReturnTrueWhenOneDeploymentArtifactHasQueriedId() {
        Component component = aComponent();
        component.setDeploymentArtifacts(someArtifacts(anArtifactDefinition(ARTIFACT_ID)));
        assertTrue(checkArtifactInComponent(component, ARTIFACT_ID));
    }

    @Test
    public void checkArtifactInComponentShouldReturnTrueWhenOneToscaArtifactHasQueriedId() {
        Component component = aComponent();
        component.setToscaArtifacts(someArtifacts(anArtifactDefinition(ARTIFACT_ID)));
        assertTrue(checkArtifactInComponent(component, ARTIFACT_ID));
    }

    @Test
    public void checkArtifactInComponentShouldReturnTrueWhenOneOperationHasQueriedId() {
        Component component = aComponent();
        component.setInterfaces(someInterfaces(anInterfaceDefinition(ARTIFACT_ID)));
        assertTrue(checkArtifactInComponent(component, ARTIFACT_ID));
    }

    private static InterfaceDefinition anInterfaceDefinition(String artifactUniqueId) {
        Map<String, Operation> operations = someOperations(
            anOperation(aUniqueId(), anArtifactDefinition(artifactUniqueId)));

        InterfaceDefinition id = new InterfaceDefinition();
        id.setOperationsMap(operations);

        return id;
    }

    @Test
    public void checkArtifactInComponentShouldReturnTrueWhenServiceHasArtifactsWithQueriedId() {
        Service service = new Service();
        service.setComponentType(ComponentTypeEnum.SERVICE);
        service.setServiceApiArtifacts(someArtifacts(anArtifactDefinition(ARTIFACT_ID)));

        assertTrue(checkArtifactInComponent(service, ARTIFACT_ID));
    }

    @Test
    public void checkArtifactInResourceInstanceShouldReturnFalseWhenNoConditionIsMet() {
        assertFalse(checkArtifactInResourceInstance(aComponent(), aResourceInstanceId(), anArtifactId()));
    }

    @Test
    public void checkArtifactInResourceInstanceShouldReturnTrueWhenResourceHasQueriedIdAndDeploymentArtifactId() {
        ComponentInstance ci = aComponentInstance(aResourceInstanceId());
        ci.setDeploymentArtifacts(someArtifacts(anArtifactDefinition(ARTIFACT_ID)));

        Component component = aComponent();
        component.setComponentInstances(someComponentInstances(ci));

        assertTrue(checkArtifactInResourceInstance(component, ci.getUniqueId(), ARTIFACT_ID));
    }

    @Test
    public void checkArtifactInResourceInstanceShouldReturnTrueWhenResourceHasQueriedIdAndArtifactId() {
        ComponentInstance ci = aComponentInstance(aResourceInstanceId());
        ci.setArtifacts(someArtifacts(anArtifactDefinition(ARTIFACT_ID)));

        Component component = aComponent();
        component.setComponentInstances(someComponentInstances(ci));

        assertTrue(checkArtifactInResourceInstance(component, ci.getUniqueId(), ARTIFACT_ID));
    }
}
