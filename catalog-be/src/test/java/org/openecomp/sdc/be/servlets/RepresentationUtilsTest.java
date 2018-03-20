package org.openecomp.sdc.be.servlets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;
import org.openecomp.sdc.be.components.InterfaceOperationTestUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;

public class RepresentationUtilsTest implements InterfaceOperationTestUtils {

    private RepresentationUtils createTestSubject() {
        return new RepresentationUtils();
    }


    @Test
    public void testConvertJsonToArtifactDefinitionForUpdate() throws Exception {
        String content = "";
        Class<ArtifactDefinition> clazz = null;
        ArtifactDefinition result;

        // default test
        result = RepresentationUtils.convertJsonToArtifactDefinitionForUpdate(content, clazz);
    }


    @Test
    public void testToRepresentation() throws Exception {
        T elementToRepresent = null;
        Object result;

        // default test
        result = RepresentationUtils.toRepresentation(elementToRepresent);
    }




    @Test
    public void testConvertJsonToArtifactDefinition() throws Exception {
        String content = "";
        Class<ArtifactDefinition> clazz = null;
        ArtifactDefinition result;

        // default test
        result = RepresentationUtils.convertJsonToArtifactDefinition(content, clazz);
    }

    @Test
    public void checkIsEmptyFiltering() throws Exception {
        HashMap<String, Operation> op = new HashMap<>();
        Operation opValue = new Operation();
        opValue.setName("eee");
        opValue.setDescription("ccc");
        op.put("Bla", opValue);
        InterfaceDefinition anInterface = createInterface("id", "bla bla", "type", "tosca.name", op);
        Object result = RepresentationUtils.toRepresentation(anInterface);
        assertNotNull(result);
        assertTrue(result.toString(), result.toString().contains("empty"));
        result = RepresentationUtils.toFilteredRepresentation(anInterface);
        assertNotNull(result);
        assertFalse(result.toString(), result.toString().contains("empty"));
    }

}