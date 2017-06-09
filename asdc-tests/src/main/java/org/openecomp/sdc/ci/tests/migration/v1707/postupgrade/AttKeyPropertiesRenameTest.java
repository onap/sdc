package org.openecomp.sdc.ci.tests.migration.v1707.postupgrade;

import com.thinkaurelius.titan.core.TitanVertex;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.migration.v1707.CommonMigrationUtils;
import org.openecomp.sdc.ci.tests.utils.graph.GraphFileUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class AttKeyPropertiesRenameTest extends ComponentBaseTest {

    @Rule
    public static TestName name = new TestName();


    public AttKeyPropertiesRenameTest() {
        super(name, AttKeyPropertiesRenameTest.class.getName());
    }

    @Test
    public void verifyAttPropertyKeys() throws Exception {
        initGraph();
        CommonMigrationUtils.assertKeyNotExist(titanGraph, "attContact");
        CommonMigrationUtils.assertKeyNotExist(titanGraph, "attCreator");
        CommonMigrationUtils.assertKeyNotExist(titanGraph, "attuid");
        CommonMigrationUtils.assertKeyNotExist(titanGraph, "pmatt");

        CommonMigrationUtils.assertKeyExists(titanGraph, "userId");
        CommonMigrationUtils.assertKeyExists(titanGraph, "projectCode");
        CommonMigrationUtils.assertKeyExists(titanGraph, "contactId");
        CommonMigrationUtils.assertKeyExists(titanGraph, "creatorId");

        verifyPropertyKeysVerticesSameAsPreUpgrade("attuid", "userId");
        verifyPropertyKeysVerticesSameAsPreUpgrade("pmatt", "projectCode");
        verifyPropertyKeysVerticesSameAsPreUpgrade("attContact", "contactId");
        verifyPropertyKeysVerticesSameAsPreUpgrade("attCreator", "creatorId");

    }

    private void assertKeyNotExist(String key) {
        assertNotNull(titanGraph.getPropertyKey(key));
    }

    private void assertKeyExists(String key) {
        assertNull(titanGraph.getPropertyKey(key));
    }

    private void verifyPropertyKeysVerticesSameAsPreUpgrade(String oldPropertyKEyName, String newPropertyKeyName) throws IOException {
        List<String> verticesIdsFromGraph = getVerticesIdsFromGRaph(newPropertyKeyName);
        List<String> verticesIdsFromFile = GraphFileUtils.getVerticesIdsFromFile(oldPropertyKEyName);
        Collections.sort(verticesIdsFromFile);
        Collections.sort(verticesIdsFromGraph);
        assertEquals(verticesIdsFromFile, verticesIdsFromGraph);
    }

    private List<String> getVerticesIdsFromGRaph(String newPropertyKeyName) {
        Iterable<TitanVertex> vertices = titanGraph.query().has(newPropertyKeyName).vertices();
        assertTrue(vertices.iterator().hasNext());
        List<String> verticesIdsFromGraph = new ArrayList<>();
        vertices.forEach(vertex -> verticesIdsFromGraph.add(vertex.id().toString()));
        return verticesIdsFromGraph;
    }
}
