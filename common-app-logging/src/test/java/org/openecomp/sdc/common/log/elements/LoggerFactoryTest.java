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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class LoggerFactoryTest {

    @Mock
    private Logger logger;

    @Test
    void getMdcLoggerInstantiateProperly() {
        assertNotNull(LoggerFactory.getMdcLogger(LoggerAudit.class, logger));
        assertNotNull(LoggerFactory.getMdcLogger(LoggerDebug.class, logger));
        assertNotNull(LoggerFactory.getMdcLogger(LoggerMetric.class, logger));
        assertNotNull(LoggerFactory.getMdcLogger(LoggerError.class, logger));
        assertNotNull(LoggerFactory.getMdcLogger(LoggerSupportability.class, logger));
    }

    @Test
    void getLoggerInstantiateProperly() {
        assertNotNull(LoggerFactory.getLogger(LoggerAudit.class, logger));
        assertNotNull(LoggerFactory.getLogger(LoggerDebug.class, logger));
        assertNotNull(LoggerFactory.getLogger(LoggerMetric.class, logger));
        assertNotNull(LoggerFactory.getLogger(LoggerError.class, logger));
    }

    @Test
    void getMdcLoggerReturnsNullForSomeInvalidClasses() {
        assertNull(LoggerFactory.getMdcLogger(Integer.class, logger));
    }

    @Test
    void getLoggerReturnsNullForSomeInvalidClasses() {
        assertNull(LoggerFactory.getLogger(Integer.class, logger));
    }

}