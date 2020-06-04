/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.common.log.elements;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class LoggerFactoryTest {

    @Mock
    private Logger logger;

    @Test
    public void getLoggerInstance() {
        Assert.assertNotNull(LoggerFactory.getMdcLogger(LoggerAudit.class, logger));
        Assert.assertNotNull(LoggerFactory.getMdcLogger(LoggerDebug.class, logger));
        Assert.assertNotNull(LoggerFactory.getMdcLogger(LoggerMetric.class, logger));
        Assert.assertNotNull(LoggerFactory.getMdcLogger(LoggerError.class, logger));
        Assert.assertNotNull(LoggerFactory.getMdcLogger(LoggerSupportability.class, logger));
    }

}