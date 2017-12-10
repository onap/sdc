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

package org.openecomp.sdc.ci.tests.migration.v1707.postupgrade;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.migration.v1707.CommonMigrationUtils;
import org.openecomp.sdc.ci.tests.utils.graph.GraphFileUtils;
import org.testng.annotations.Test;

import com.thinkaurelius.titan.core.TitanVertex;

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
