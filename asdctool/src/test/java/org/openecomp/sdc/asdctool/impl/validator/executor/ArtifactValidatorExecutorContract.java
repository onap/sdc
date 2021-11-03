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

package org.openecomp.sdc.asdctool.impl.validator.executor;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

public interface ArtifactValidatorExecutorContract {

    ArtifactValidatorExecutor createTestSubject(
        JanusGraphDao janusGraphDao,
        ToscaOperationFacade toscaOperationFacade
    );

    @Test
    default void testGetVerticesToValidate() {
        JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
        ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);
        final ArtifactValidatorExecutor testSubject = createTestSubject(janusGraphDaoMock, toscaOperationFacade);

        VertexTypeEnum type = null;
        Map<GraphPropertyEnum, Object> hasProps = null;
        Assertions.assertThrows(NullPointerException.class, () -> testSubject.getVerticesToValidate(type, hasProps)
        );
    }

    @Test
    default void testValidate() {
        JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
        ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);
        final ArtifactValidatorExecutor testSubject = createTestSubject(janusGraphDaoMock, toscaOperationFacade);

        LinkedList<Component> linkedList = new LinkedList<Component>();
        linkedList.add(new Resource());

        Map<String, List<Component>> vertices = new HashMap<>();
        vertices.put("stam", linkedList);

        Assertions.assertFalse(testSubject.validate(vertices, "target/"));
    }
}
