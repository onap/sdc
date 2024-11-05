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

package org.openecomp.sdc.asdctool.configuration;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.asdctool.impl.internal.tool.DeleteComponentHandler;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

public class InternalToolConfigurationTest {

    private InternalToolConfiguration createTestSubject() {
        return new InternalToolConfiguration();
    }

    @Test
    public void testDeleteComponentHandler() throws Exception {
        InternalToolConfiguration testSubject;
        DeleteComponentHandler result;

        // default test
        testSubject = createTestSubject();
        JanusGraphDao janusGraphDao = mock(JanusGraphDao.class);
        NodeTypeOperation nodeTypeOperation = mock(NodeTypeOperation.class);
        TopologyTemplateOperation topologyTemplateOperation = mock(TopologyTemplateOperation.class);

        result = testSubject.deleteComponentHandler(janusGraphDao, nodeTypeOperation, topologyTemplateOperation);
    }

}
