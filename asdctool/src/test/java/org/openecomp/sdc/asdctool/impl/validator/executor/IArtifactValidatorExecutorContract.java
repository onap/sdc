/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Bell Intellectual Property. All rights reserved.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

public abstract class IArtifactValidatorExecutorContract {

    protected abstract IArtifactValidatorExecutor createTestSubject(
        JanusGraphDao janusGraphDao,
        ToscaOperationFacade toscaOperationFacade
    );

    private IArtifactValidatorExecutor createTestSubject() {
        return createTestSubject(mock(JanusGraphDao.class), mock(ToscaOperationFacade.class));
    }

    @Test
    public void testExecuteValidations() {
        Assertions.assertThrows(NullPointerException.class, () ->
            // Initially no outputFilePath was passed to this function (hence it is set to null)
            // TODO: Fix this null and see if the argument is used by this function
            createTestSubject().executeValidations(null)
        );
    }
}
