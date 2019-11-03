/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.logback;

import static org.junit.Assert.assertEquals;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.Test;
import org.openecomp.sdc.logging.slf4j.Markers;
import org.slf4j.MarkerFactory;

/**
 * Test categorizing of events.
 *
 * @author EVITALIY
 * @since 17/08/2016.
 */
public class EventTypeDiscriminatorTest {

    private static final String DEBUG = "Debug";
    private static final String ERROR = "Error";

    @Test
    public void testGetDefaultDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        assertEquals(DEBUG, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetErrorDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.ERROR);
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetWarnDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.WARN);
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetInfoDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetTraceDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.TRACE);
        assertEquals(DEBUG, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetErrorWithAuditDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.ERROR);
        event.setMarker(Markers.EXIT);
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetErrorWithMetricsDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.ERROR);
        event.setMarker(Markers.METRICS);
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetWarnWithAuditDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.WARN);
        event.setMarker(Markers.EXIT);
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetWarnWithMetricsDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.WARN);
        event.setMarker(Markers.METRICS);
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetDebugWithAuditDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.DEBUG);
        event.setMarker(Markers.EXIT);
        assertEquals(DEBUG, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetDebugWithMetricsDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.DEBUG);
        event.setMarker(Markers.METRICS);
        assertEquals(DEBUG, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetTraceWithAuditDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.TRACE);
        event.setMarker(Markers.EXIT);
        assertEquals(DEBUG, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetTraceWithMetricsDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.TRACE);
        event.setMarker(Markers.METRICS);
        assertEquals(DEBUG, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetErrorWithMarkerDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.ERROR);
        event.setMarker(MarkerFactory.getMarker("Dummy"));
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetWarnWithMarkerDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.WARN);
        event.setMarker(MarkerFactory.getMarker("Dummy"));
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetDebugWithMarkerDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.DEBUG);
        event.setMarker(MarkerFactory.getMarker("Dummy"));
        assertEquals(DEBUG, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetTraceWithMarkerDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.TRACE);
        event.setMarker(MarkerFactory.getMarker("Dummy"));
        assertEquals(DEBUG, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetInfoWithMarkerDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(MarkerFactory.getMarker("Dummy"));
        assertEquals(ERROR, discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetAuditDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(Markers.EXIT);
        assertEquals("Audit", discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetMetricsMarkerDiscriminatingValue() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(Markers.METRICS);
        assertEquals("Metrics", discriminator.getDiscriminatingValue(event));
    }

    @Test
    public void testGetKey() {
        EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
        assertEquals("eventType", discriminator.getKey());
    }

}
