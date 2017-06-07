package org.openecomp.sdc.ci.tests.migration.v1707;

import com.thinkaurelius.titan.core.TitanGraph;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class CommonMigrationUtils {

    public static void assertKeyNotExist(TitanGraph graph, String key) {
        assertNull(graph.getPropertyKey(key));
    }

    public static void assertKeyExists(TitanGraph graph, String key) {
        assertNotNull(graph.getPropertyKey(key));
    }


}
