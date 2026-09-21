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

package org.openecomp.sdc.be.servlets.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class WebApplicationExceptionMapperTest {

    private final WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();
    private ch.qos.logback.classic.Logger logger;
    private ListAppender<ILoggingEvent> logEvents;

    @BeforeEach
    void setUp() {
        logger = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger(WebApplicationExceptionMapper.class);
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
    void shouldKeepTheNotFoundStatus() {
        final Response response = mapper.toResponse(new NotFoundException());

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    void shouldKeepTheStatusAndEntityOfTheGivenResponse() {
        final Response given = Response.status(Status.METHOD_NOT_ALLOWED).entity("no PUT here").build();

        final Response response = mapper.toResponse(new NotAllowedException(given));

        assertEquals(Status.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatus());
        assertEquals("no PUT here", response.getEntity());
    }

    @Test
    void shouldLogClientErrorsWithoutTheStackTrace() {
        mapper.toResponse(new NotFoundException());

        assertEquals(1, logEvents.list.size());
        final ILoggingEvent event = logEvents.list.get(0);
        assertEquals(Level.DEBUG, event.getLevel());
        assertNull(event.getThrowableProxy());
        assertTrue(event.getFormattedMessage().contains("404"));
    }

    @Test
    void shouldLogServerErrorsWithTheStackTrace() {
        final WebApplicationException exception = new InternalServerErrorException();

        mapper.toResponse(exception);

        assertEquals(1, logEvents.list.size());
        final ILoggingEvent event = logEvents.list.get(0);
        assertEquals(Level.ERROR, event.getLevel());
        assertEquals(exception.getClass().getName(), event.getThrowableProxy().getClassName());
    }
}
