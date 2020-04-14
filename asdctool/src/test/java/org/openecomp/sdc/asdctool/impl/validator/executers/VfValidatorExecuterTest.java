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

package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.tasks.VfValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.Report;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.openecomp.sdc.asdctool.impl.validator.ReportFileWriterTestFactory.makeConsoleWriter;
import static org.openecomp.sdc.asdctool.impl.validator.utils.ReportFile.makeTxtFile;

public class VfValidatorExecuterTest {

    private VfValidatorExecuter createTestSubject() {
        List<VfValidationTask> validationTasks = new ArrayList<>();
        JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);

        return new VfValidatorExecuter(validationTasks, janusGraphDaoMock);
    }

    @Test
    public void testGetName() {
        createTestSubject().getName();
    }

    @Test(expected = NullPointerException.class)
    public void testExecuteValidations() {
        createTestSubject().executeValidations(
                Report.make(),
                makeTxtFile(makeConsoleWriter())
        );
    }
}
