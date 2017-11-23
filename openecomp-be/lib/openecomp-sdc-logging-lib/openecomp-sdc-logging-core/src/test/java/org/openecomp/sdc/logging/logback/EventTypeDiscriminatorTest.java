/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.openecomp.sdc.logging.Markers;
import org.slf4j.MarkerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author EVITALIY
 * @since 17/08/2016.
 */
public class EventTypeDiscriminatorTest {

  private static final String DEBUG = "Debug";
  private static final String ERROR = "Error";

  @Test
  public void testGetDefaultDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    assertEquals(discriminator.getDiscriminatingValue(event), DEBUG);
  }

  @Test
  public void testGetErrorDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.ERROR);
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetWarnDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.WARN);
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetInfoDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.INFO);
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetTraceDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.TRACE);
    assertEquals(discriminator.getDiscriminatingValue(event), DEBUG);
  }

  @Test
  public void testGetErrorWithAuditDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.ERROR);
    event.setMarker(Markers.AUDIT);
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetErrorWithMetricsDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.ERROR);
    event.setMarker(Markers.METRICS);
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetWarnWithAuditDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.WARN);
    event.setMarker(Markers.AUDIT);
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetWarnWithMetricsDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.WARN);
    event.setMarker(Markers.METRICS);
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetDebugWithAuditDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.DEBUG);
    event.setMarker(Markers.AUDIT);
    assertEquals(discriminator.getDiscriminatingValue(event), DEBUG);
  }

  @Test
  public void testGetDebugWithMetricsDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.DEBUG);
    event.setMarker(Markers.METRICS);
    assertEquals(discriminator.getDiscriminatingValue(event), DEBUG);
  }

  @Test
  public void testGetTraceWithAuditDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.TRACE);
    event.setMarker(Markers.AUDIT);
    assertEquals(discriminator.getDiscriminatingValue(event), DEBUG);
  }

  @Test
  public void testGetTraceWithMetricsDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.TRACE);
    event.setMarker(Markers.METRICS);
    assertEquals(discriminator.getDiscriminatingValue(event), DEBUG);
  }

  @Test
  public void testGetErrorWithMarkerDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.ERROR);
    event.setMarker(MarkerFactory.getMarker("Dummy"));
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetWarnWithMarkerDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.WARN);
    event.setMarker(MarkerFactory.getMarker("Dummy"));
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetDebugWithMarkerDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.DEBUG);
    event.setMarker(MarkerFactory.getMarker("Dummy"));
    assertEquals(discriminator.getDiscriminatingValue(event), DEBUG);
  }

  @Test
  public void testGetTraceWithMarkerDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.TRACE);
    event.setMarker(MarkerFactory.getMarker("Dummy"));
    assertEquals(discriminator.getDiscriminatingValue(event), DEBUG);
  }

  @Test
  public void testGetInfoWithMarkerDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.INFO);
    event.setMarker(MarkerFactory.getMarker("Dummy"));
    assertEquals(discriminator.getDiscriminatingValue(event), ERROR);
  }

  @Test
  public void testGetAuditDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.INFO);
    event.setMarker(Markers.AUDIT);
    assertEquals(discriminator.getDiscriminatingValue(event), "Audit");
  }

  @Test
  public void testGetMetricsMarkerDiscriminatingValue() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    LoggingEvent event = new LoggingEvent();
    event.setLevel(Level.INFO);
    event.setMarker(Markers.METRICS);
    assertEquals(discriminator.getDiscriminatingValue(event), "Metrics");
  }

  @Test
  public void testGetKey() throws Exception {
    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    assertEquals("eventType", discriminator.getKey());
  }

}
