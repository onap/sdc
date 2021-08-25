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

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.asdctool.impl.validator.report.Report;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;

import java.util.ArrayList;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.openecomp.sdc.asdctool.impl.validator.executor.TopologyTemplateValidatorExecutor.vfValidatorExecutor;
import static org.openecomp.sdc.asdctool.impl.validator.report.ReportFile.makeTxtFile;
import static org.openecomp.sdc.asdctool.impl.validator.report.ReportFileWriterTestFactory.makeConsoleWriter;

public final class ValidatorExecutorTest {

    @Test
    public void executeValidationsWithServiceValidator() {
        testExecuteValidations(TopologyTemplateValidatorExecutor::serviceValidatorExecutor);
    }

    @Test
    public void executeValidationsWithVFValidator() {
        testExecuteValidations(dao -> vfValidatorExecutor(new ArrayList<>(), dao));
    }

    private void testExecuteValidations(Function<JanusGraphDao, ValidatorExecutor> factory) {
        Report report = Report.make();
        JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
        assertThrows(NullPointerException.class, () ->
            factory.apply(janusGraphDaoMock).executeValidations(report, makeTxtFile(makeConsoleWriter()))
        );
    }
}
