package org.openecomp.sdc.be.tosca.utils;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;

public class OperationArtifactUtilTest {

    @Test
    public void testCorrectPathForOperationArtifacts() {
        Component component =   new Resource();
        component.setNormalizedName("normalizedComponentName");
        final InterfaceDefinition addedInterface = new InterfaceDefinition();
        final OperationDataDefinition op = new OperationDataDefinition();
        final ArtifactDataDefinition implementation = new ArtifactDataDefinition();
        implementation.setArtifactName("createBPMN.bpmn");
        op.setImplementation(implementation);
        addedInterface.setOperations(new HashMap<>());
        addedInterface.getOperations().put("create", op);
        final String interfaceType = "normalizedComponentName-interface";
        ((Resource) component).setInterfaces(new HashMap<>());
        ((Resource) component).getInterfaces().put(interfaceType, addedInterface);
        final String actualArtifactPath = OperationArtifactUtil.createOperationArtifactPath(component.getNormalizedName(), interfaceType, op);
        String expectedArtifactPath ="Artifacts/normalizedComponentName/normalizedComponentName-interface/Deployment/Workflows/BPMN/createBPMN.bpmn";

        assertEquals(expectedArtifactPath,actualArtifactPath);
    }
}
