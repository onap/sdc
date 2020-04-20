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

package org.openecomp.sdc.asdctool.impl.validator;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.LinkedList;
import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.executers.NodeToscaArtifactsValidatorExecuter;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

public class ArtifactToolBLTest {

    private ArtifactToolBL createTestSubject() {
        return new ArtifactToolBL(new ArrayList<>());
    }

    //Generated test
    @Test(expected = NullPointerException.class)
    public void testValidateAll() {
        JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
        ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

        ArtifactToolBL testSubject = createTestSubject();
        testSubject.validators = new LinkedList<>();
        testSubject.validators.add(new NodeToscaArtifactsValidatorExecuter(janusGraphDaoMock, toscaOperationFacade));
        // Initially no outputFilePath was passed to this function (hence it is set to null)
        // TODO: Fix this null and see if the argument is used by this function
        testSubject.validateAll(null);
    }
}
