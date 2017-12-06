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

package org.openecomp.sdc.logging.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;
import org.easymock.EasyMock;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * @author EVITALIY
 * @since 17/08/2016.
 */
@PrepareForTest(LoggerFactory.class)
public class MetricsAspectTest extends PowerMockTestCase {

  private static final Object OBJ_TO_RETURN = new Object();
  private static final String EXPECTED_MESSAGE = "'{}' took {} milliseconds";

  //@Test
  public void testLogExecutionTime() throws Throwable {

    String className = UUID.randomUUID().toString();
    String methodName = UUID.randomUUID().toString();

    TestLogger logger = initLogging(className, true);

    MetricsAspect aspect = new MetricsAspect();
    MockProceedingJoinPoint pjp = new MockProceedingJoinPoint(className, methodName);
    Object returned = aspect.logExecutionTime(pjp);

    Assert.assertEquals(OBJ_TO_RETURN, returned);
    assertExecution(methodName, pjp, logger);
  }

  @Test
  public void testMetricsDisabled() throws Throwable {

    String className = UUID.randomUUID().toString();
    String methodName = UUID.randomUUID().toString();

    TestLogger logger = initLogging(className, false);

    MetricsAspect aspect = new MetricsAspect();
    MockProceedingJoinPoint pjp = new MockProceedingJoinPoint(className, methodName);
    Object returned = aspect.logExecutionTime(pjp);

    Assert.assertEquals(OBJ_TO_RETURN, returned);
    Assert.assertEquals(1, pjp.getCount());
    // return any event - must be empty
    Assert.assertFalse(logger.contains((event) -> true));
  }

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testThrowingError() throws Throwable {

    String className = UUID.randomUUID().toString();
    String methodName = UUID.randomUUID().toString();

    final TestLogger logger = initLogging(className, true);

    MetricsAspect aspect = new MetricsAspect();
    MockProceedingJoinPoint pjp = new MockProceedingJoinPointWithException(className, methodName);

    try {
      aspect.logExecutionTime(pjp);
    } finally {
      assertExecution(methodName, pjp, logger);
    }
  }

  private TestLogger initLogging(String className, boolean enabled) {
    TestLogger logger = new TestLogger(enabled);
    PowerMock.mockStatic(LoggerFactory.class);
    EasyMock.expect(LoggerFactory.getLogger(className)).andReturn(logger);
    PowerMock.replay(LoggerFactory.class);
    return logger;
  }

  private void assertExecution(String methodName, MockProceedingJoinPoint pjp, TestLogger logger) {

    Assert.assertEquals(1, pjp.getCount());
    Assert.assertTrue(logger.contains((event) ->
        (event != null) && (event.length == 3) && EXPECTED_MESSAGE.equals(event[0])
            && methodName.equals(event[1]) && (event[2] instanceof Long)));
  }

  private static class MockSignature implements Signature {

    private final String className;
    private final String methodName;

    private MockSignature(String className, String methodName) {
      this.className = className;
      this.methodName = methodName;
    }

    @Override
    public String toShortString() {
      return null;
    }

    @Override
    public String toLongString() {
      return null;
    }

    @Override
    public String getName() {
      return methodName;
    }

    @Override
    public int getModifiers() {
      return 0;
    }

    @Override
    public Class getDeclaringType() {
      return null;
    }

    @Override
    public String getDeclaringTypeName() {
      return className;
    }
  }

  private static class MockProceedingJoinPoint implements ProceedingJoinPoint {

    private AtomicInteger count = new AtomicInteger(0);
    private Signature signature;

    MockProceedingJoinPoint(String className, String methodName) {
      this.signature = new MockSignature(className, methodName);
    }

    int getCount() {
      return count.get();
    }

    @Override
    public Object proceed() throws Throwable {
      count.incrementAndGet();
      return OBJ_TO_RETURN;
    }

    @Override
    public void set$AroundClosure(AroundClosure aroundClosure) {

    }

    @Override
    public Object proceed(Object[] objects) throws Throwable {
      return null;
    }

    @Override
    public String toShortString() {
      return null;
    }

    @Override
    public String toLongString() {
      return null;
    }

    @Override
    public Object getThis() {
      return null;
    }

    @Override
    public Object getTarget() {
      return null;
    }

    @Override
    public Object[] getArgs() {
      return new Object[0];
    }

    @Override
    public Signature getSignature() {
      return this.signature;
    }

    @Override
    public SourceLocation getSourceLocation() {
      return null;
    }

    @Override
    public String getKind() {
      return null;
    }

    @Override
    public StaticPart getStaticPart() {
      return null;
    }
  }

  private static class MockProceedingJoinPointWithException extends MockProceedingJoinPoint {

    MockProceedingJoinPointWithException(String className, String methodName) {
      super(className, methodName);
    }

    @Override
    public Object proceed() throws Throwable {
      super.proceed();
      throw new IllegalArgumentException();
    }
  }

  private class TestLogger implements Logger {

    private final boolean enabled;
    private List<Object[]> events = Collections.synchronizedList(new ArrayList<>(10));

    public TestLogger(boolean enabled) {
      this.enabled = enabled;
    }

    @Override
    public String getName() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isMetricsEnabled() {
      return this.enabled;
    }

    @Override
    public void metrics(String var1) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void metrics(String var1, Object var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void metrics(String var1, Object var2, Object var3) {

      if (this.enabled) {
        events.add(new Object[]{var1, var2, var3});
      }
    }

    @Override
    public void metrics(String var1, Object... var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void metrics(String var1, Throwable throwable) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isAuditEnabled() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void audit(String var1) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void audit(String var1, Object var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void audit(String var1, Object var2, Object var3) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void audit(String var1, Object... var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void audit(String var1, Throwable throwable) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isDebugEnabled() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void debug(String var1) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void debug(String var1, Object var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void debug(String var1, Object var2, Object var3) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void debug(String var1, Object... var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void debug(String var1, Throwable throwable) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isInfoEnabled() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void info(String var1) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void info(String var1, Object var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void info(String var1, Object var2, Object var3) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void info(String var1, Object... var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void info(String var1, Throwable throwable) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isWarnEnabled() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void warn(String var1) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void warn(String var1, Object var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void warn(String var1, Object... var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void warn(String var1, Object var2, Object var3) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void warn(String var1, Throwable throwable) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isErrorEnabled() {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void error(String var1) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void error(String var1, Object var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void error(String var1, Object var2, Object var3) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void error(String var1, Object... var2) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void error(String var1, Throwable throwable) {
      throw new RuntimeException("Not implemented");
    }

    public boolean contains(Predicate<Object[]> predicate) {
      return events.stream().anyMatch(predicate);
    }
  }
}
