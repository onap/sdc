/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.be.tosca.utils;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil.BPMN_ARTIFACT_PATH;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentMetadataDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class OperationArtifactUtilTest {

    @BeforeClass
    public static void setUp() throws Exception {
        new DummyConfigurationManager();
    }

    @Test
    public void testCorrectPathForOperationArtifacts() {
        ResourceMetadataDataDefinition componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
        componentMetadataDataDefinition.setToscaResourceName("org.openecomp.resource.vf.TestResource");
        final ComponentMetadataDefinition componentMetadataDefinition =
                new ComponentMetadataDefinition(componentMetadataDataDefinition);
        Component component = new Resource(componentMetadataDefinition);
        final OperationDataDefinition op = new OperationDataDefinition();
        final ArtifactDataDefinition implementation = new ArtifactDataDefinition();
        implementation.setArtifactName("createBPMN.bpmn");
        op.setImplementation(implementation);
        final String actualArtifactPath = OperationArtifactUtil.createOperationArtifactPath(component, null, op, false);
        String expectedArtifactPath = CsarUtils.ARTIFACTS + File.separator +
                WordUtils.capitalizeFully(ArtifactGroupTypeEnum.DEPLOYMENT.name()) + File.separator +
                ArtifactTypeEnum.WORKFLOW.name() + File.separator + BPMN_ARTIFACT_PATH + File.separator +
                "createBPMN.bpmn";


        assertEquals(expectedArtifactPath,actualArtifactPath);
    }

    @Test
    public void testCorrectPathForOperationArtifactsInService() {
        Component component = new Resource();
        component.setVersion("1.0");
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setToscaComponentName("org.openecomp.resource.vf.TestResource");
        final OperationDataDefinition op = new OperationDataDefinition();
        final ArtifactDataDefinition implementation = new ArtifactDataDefinition();
        implementation.setArtifactName("createBPMN.bpmn");
        op.setImplementation(implementation);
        final String actualArtifactPath = OperationArtifactUtil.createOperationArtifactPath(component, componentInstance, op, true);
        String expectedArtifactPath = CsarUtils.ARTIFACTS + File.separator +
                "org.openecomp.resource.vf.TestResource_v1.0" + File.separator +
                WordUtils.capitalizeFully(ArtifactGroupTypeEnum.DEPLOYMENT.name()) + File.separator +
                ArtifactTypeEnum.WORKFLOW.name() +
                File.separator + BPMN_ARTIFACT_PATH + File.separator + "createBPMN.bpmn";


        assertEquals(expectedArtifactPath,actualArtifactPath);
    }

    @Test
    public void testGetDistinctInterfaceOperationArtifactsByName() {
        Component component =  new Resource();
        component.setNormalizedName("normalizedComponentName");
        final InterfaceDefinition addedInterface = new InterfaceDefinition();
        OperationDataDefinition op1 = createInterfaceOperation("createBPMN.bpmn");
        OperationDataDefinition op2 = createInterfaceOperation("createBPMN.bpmn");
        addedInterface.setOperations(new HashMap<>());
        addedInterface.getOperations().put("create", op1);
        addedInterface.getOperations().put("update", op2);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);

        Map<String, ArtifactDefinition> distinctInterfaceOperationArtifactsByName =
                OperationArtifactUtil.getDistinctInterfaceOperationArtifactsByName(component);
        Assert.assertEquals(1, distinctInterfaceOperationArtifactsByName.size());
    }

    @Test
    public void testGetDistinctInterfaceOperationArtifactsByNameAllDistinct() {
        Component component = new Resource();
        component.setNormalizedName("normalizedComponentName");
        final InterfaceDefinition addedInterface = new InterfaceDefinition();
        OperationDataDefinition op1 = createInterfaceOperation("createBPMN.bpmn");
        OperationDataDefinition op2 = createInterfaceOperation("updateBPMN.bpmn");
        addedInterface.setOperations(new HashMap<>());
        addedInterface.getOperations().put("create", op1);
        addedInterface.getOperations().put("update", op2);
        final String interfaceType = "normalizedComponentName-interface";
        component.setInterfaces(new HashMap<>());
        component.getInterfaces().put(interfaceType, addedInterface);

        Map<String, ArtifactDefinition> distinctInterfaceOperationArtifactsByName =
                OperationArtifactUtil.getDistinctInterfaceOperationArtifactsByName(component);
        Assert.assertEquals(2, distinctInterfaceOperationArtifactsByName.size());
    }

    private OperationDataDefinition createInterfaceOperation(String artifactName) {
        final OperationDataDefinition op = new OperationDataDefinition();
        final ArtifactDataDefinition implementation = new ArtifactDataDefinition();
        implementation.setUniqueId(UUID.randomUUID().toString());
        implementation.setArtifactName(artifactName);
        op.setImplementation(implementation);
        return op;
    }
}