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

package org.openecomp.sdc.ci.tests.migration.v1707.preupgrade;

import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.migration.v1707.CommonMigrationUtils;
import org.openecomp.sdc.ci.tests.utils.graph.GraphFileUtils;
import org.testng.annotations.Test;

import com.thinkaurelius.titan.core.TitanVertex;

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
