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

package org.openecomp.sdc.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.openecomp.sdc.logging.logback.EventTypeDiscriminator;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * TODO: Add more negative tests
 *
 * @author EVITALIY
 * @since 17/08/2016.
 */
public class RoutingTest {

  private static final String ERROR = "Error";
  private static final String DEBUG = "Debug";
  private static final String AUDIT = "Audit";
  private static final String METRICS = "Metrics";

  private Logger logger;
  private Map<String, TestAppender> result = new ConcurrentHashMap<>();

  @BeforeClass
  public void setUp() {

    SiftingAppender appender = new SiftingAppender();

    EventTypeDiscriminator discriminator = new EventTypeDiscriminator();
    discriminator.start();

    appender.setDiscriminator(discriminator);
    appender.setAppenderFactory((context, discriminatingValue) ->
        result.computeIfAbsent(discriminatingValue, f -> {
          TestAppender tmp = new TestAppender();
          tmp.start();
          return tmp;
        }));

    appender.start();

    logger = (Logger) LoggerFactory.getLogger(RoutingTest.class.getName());
    // prevent from writing into appenders attached via parent loggers
    logger.setAdditive(false);
    logger.addAppender(appender);
    logger.setLevel(Level.DEBUG);
  }

  @Test
  public void testWarning() {
    String msg = "This is a test warning";
    logger.warn(msg);
    TestAppender appender = result.get(ERROR);
    Assert.assertTrue(appender.contains((event) ->
        Level.WARN.equals(event.getLevel()) && msg.equals(event.getFormattedMessage())));
  }

  @Test
  public void testError() {
    String msg = "This is a test error";
    logger.error(msg);
    TestAppender appender = result.get(ERROR);
    Assert.assertTrue(appender.contains((event) ->
        Level.ERROR.equals(event.getLevel()) && msg.equals(event.getFormattedMessage())));
  }

  @Test
  public void testDebug() {
    String msg = "This is a test debug";
    logger.debug(msg);
    TestAppender appender = result.get(DEBUG);
    Assert.assertTrue(appender.contains((event) ->
        Level.DEBUG.equals(event.getLevel()) && msg.equals(event.getFormattedMessage())));
  }

  @Test
  public void testInfo() {
    String msg = "This is a test info";
    logger.info(msg);
    TestAppender appender = result.get(ERROR);
    Assert.assertTrue(appender.contains((event) ->
        Level.INFO.equals(event.getLevel()) && msg.equals(event.getFormattedMessage())));
  }

  @Test
  public void testAudit() {
    String msg = "This is a test audit";
    logger.info(Markers.AUDIT, msg);
    TestAppender appender = result.get(AUDIT);
    Assert.assertTrue(appender.contains((event) ->
        Level.INFO.equals(event.getLevel()) && msg.equals(event.getFormattedMessage())));
  }

  @Test
  public void testMetrics() {
    String msg = "This is a test metrics";
    logger.info(Markers.METRICS, msg);
    TestAppender appender = result.get(METRICS);
    Assert.assertTrue(appender.contains((event) ->
        Level.INFO.equals(event.getLevel()) && msg.equals(event.getFormattedMessage())));
  }

  /**
   * An appender that just accumulates messages in a list and enables to inspect them
   *
   * @author EVITALIY
   * @since 17/08/2016.
   */
  private static class TestAppender extends AppenderBase<ILoggingEvent> {

    private List<ILoggingEvent> events = Collections.synchronizedList(new ArrayList<>(10));

    @Override
    protected void append(ILoggingEvent event) {
      this.events.add(event);
    }

    public boolean contains(Predicate<ILoggingEvent> predicate) {
      return events.stream().anyMatch(predicate);
    }
  }
}
