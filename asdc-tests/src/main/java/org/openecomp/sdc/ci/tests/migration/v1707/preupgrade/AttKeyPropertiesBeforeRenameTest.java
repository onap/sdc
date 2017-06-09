package org.openecomp.sdc.ci.tests.migration.v1707.preupgrade;

import com.thinkaurelius.titan.core.TitanVertex;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.migration.v1707.CommonMigrationUtils;
import org.openecomp.sdc.ci.tests.utils.graph.GraphFileUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class AttKeyPropertiesBeforeRenameTest extends ComponentBaseTest {


    @Rule
    public static TestName name = new TestName();

    public AttKeyPropertiesBeforeRenameTest() {
        super(name, AttKeyPropertiesBeforeRenameTest.class.getName());
    }

    @Test
    public void verifyAttPropertyKeys() throws Exception {
        initGraph();
        CommonMigrationUtils.assertKeyExists(titanGraph, "attContact");
        CommonMigrationUtils.assertKeyExists(titanGraph, "attCreator");
        CommonMigrationUtils.assertKeyExists(titanGraph, "attuid");
        CommonMigrationUtils.assertKeyExists(titanGraph, "pmatt");

        CommonMigrationUtils.assertKeyNotExist(titanGraph, "userId");
        CommonMigrationUtils.assertKeyNotExist(titanGraph, "projectCode");
        CommonMigrationUtils.assertKeyNotExist(titanGraph, "contactId");
        CommonMigrationUtils.assertKeyNotExist(titanGraph, "creatorId");

        saveVerticesWithPropertyKeyToFile("attContact");
        saveVerticesWithPropertyKeyToFile("attCreator");
        saveVerticesWithPropertyKeyToFile("attuid");
        saveVerticesWithPropertyKeyToFile("pmatt");
    }

    private void saveVerticesWithPropertyKeyToFile(String propertyKey) throws IOException {
        Iterable<TitanVertex> vertices = titanGraph.query().has(propertyKey).vertices();
        assertTrue(vertices.iterator().hasNext());
        GraphFileUtils.writeVerticesUIDToFile(propertyKey, vertices);
    }
}
