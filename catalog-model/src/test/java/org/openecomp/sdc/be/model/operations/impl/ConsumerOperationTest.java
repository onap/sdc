/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom. All rights reserved.
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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fj.data.Either;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ConsumerData;
import org.slf4j.LoggerFactory;

class ConsumerOperationTest {

    private final JanusGraphGenericDao janusGraphGenericDao = mock(JanusGraphGenericDao.class);
    private final ConsumerOperation consumerOperation = new ConsumerOperation(janusGraphGenericDao);
    private ch.qos.logback.classic.Logger logger;
    private ListAppender<ILoggingEvent> logEvents;

    @BeforeEach
    void setUp() {
        logger = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(ConsumerOperation.class.getName());
        logger.setLevel(Level.DEBUG);
        logEvents = new ListAppender<>();
        logEvents.start();
        logger.addAppender(logEvents);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logEvents);
        logger.setLevel(null);
    }

    @Test
    void shouldNotLogAnErrorWhenTheConsumerDoesNotExist() {
        givenTheLookupReturns(JanusGraphOperationStatus.NOT_FOUND);

        final Either<ConsumerData, StorageOperationStatus> result = consumerOperation.getCredentials("aai");

        assertEquals(StorageOperationStatus.NOT_FOUND, result.right().value());
        assertEquals(0, logEvents.list.stream().filter(event -> event.getLevel() == Level.ERROR).count());
    }

    @Test
    void shouldLogAnErrorWhenTheLookupFails() {
        givenTheLookupReturns(JanusGraphOperationStatus.GENERAL_ERROR);

        final Either<ConsumerData, StorageOperationStatus> result = consumerOperation.getCredentials("aai");

        assertEquals(StorageOperationStatus.GENERAL_ERROR, result.right().value());
        assertEquals(1, logEvents.list.stream().filter(event -> event.getLevel() == Level.ERROR).count());
    }

    private void givenTheLookupReturns(final JanusGraphOperationStatus status) {
        when(janusGraphGenericDao.getNode(anyString(), any(), eq(ConsumerData.class))).thenReturn(Either.right(status));
    }
}
