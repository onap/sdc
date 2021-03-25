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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.asdctool.impl.validator.executor.IArtifactValidatorExecutor;
import org.openecomp.sdc.asdctool.impl.validator.executor.NodeToscaArtifactsValidatorExecutor;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ArtifactToolBLTest {

    @Test
    public void testValidateAllOK() {
        List<IArtifactValidatorExecutor> validators = new ArrayList<>();
        NodeToscaArtifactsValidatorExecutor executor = Mockito.mock(NodeToscaArtifactsValidatorExecutor.class);
        when(executor.executeValidations(Mockito.anyString())).thenReturn(true);
        validators.add(executor);
        ArtifactToolBL testSubject = new ArtifactToolBL(validators);

        verify(executor, Mockito.times(0)).executeValidations(Mockito.anyString());
        assertTrue(testSubject.validateAll(""));
    }

    @Test
    public void testValidateAllNOK() {
        List<IArtifactValidatorExecutor> validators = new ArrayList<>();
        NodeToscaArtifactsValidatorExecutor executor = Mockito.mock(NodeToscaArtifactsValidatorExecutor.class);
        when(executor.executeValidations(Mockito.anyString())).thenReturn(false);
        validators.add(executor);
        ArtifactToolBL testSubject = new ArtifactToolBL(validators);

        verify(executor, Mockito.times(0)).executeValidations(Mockito.anyString());
        assertFalse(testSubject.validateAll(""));
    }

    @Test
    public void testValidateAllException() {
        JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
        ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

        List<IArtifactValidatorExecutor> validators = new ArrayList<>();
        validators.add(new NodeToscaArtifactsValidatorExecutor(janusGraphDaoMock, toscaOperationFacade));
        ArtifactToolBL testSubject = new ArtifactToolBL(validators);

        // Initially no outputFilePath was passed to this function (hence it is set to null)
        // TODO: Fix this null and see if the argument is used by this function
        assertThrows(
                NullPointerException.class,
                () -> testSubject.validateAll(null)
        );
    }
}
