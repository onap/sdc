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

package org.openecomp.sdc.logging.context;

import org.slf4j.MDC;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author evitaliy
 * @since 12/09/2016.
 */
public class MDCPropagationFactoryTest {

  // Disable if an old version of MDC implementation is being used.
  // MDCPropagationFactory should be used when MDC is not propagated to child threads.
  // See https://jira.qos.ch/browse/LOGBACK-422 and https://jira.qos.ch/browse/LOGBACK-624
  private static final boolean ENABLED = false;

  @Test(enabled = ENABLED)
  public void testNoPropagation() throws InterruptedException {

    String uuid = UUID.randomUUID().toString();
    AtomicBoolean complete = new AtomicBoolean(false);
    MDC.put("data", uuid);

    Runnable runnable = () -> {
      assertNull(MDC.get("data"), "Data unexpectedly copied to a child thread. " +
              "Are you using an old version of MDC implementation (e.g. logback)?");
      complete.set(true);
    };

    Thread thread = new Thread(runnable);
    thread.start();
    thread.join();

    assertEquals(MDC.get("data"), uuid, "Expected data to be retained in this thread");
    assertTrue(complete.get(), "Expected the inner thread to run");
  }

  @Test(enabled = ENABLED)
  public void testPropagation() throws InterruptedException {

    String uuid = UUID.randomUUID().toString();
    AtomicBoolean complete = new AtomicBoolean(false);
    MDC.put("data", uuid);

    MDCPropagationService factory = new MDCPropagationService();
    Runnable runnable = factory.create(() -> {
      assertEquals(MDC.get("data"), uuid, "Expected data to be propagated to the child thread's MDC");
      complete.set(true);
    });

    Thread thread = new Thread(runnable);
    thread.start();

    thread.join();

    assertEquals(MDC.get("data"), uuid, "Expected data to be retained in this thread");
    assertTrue(complete.get(), "Expected the inner thread to run");
  }

  @Test(enabled = ENABLED)
  public void testReplacement() throws InterruptedException {

    String innerUuid = UUID.randomUUID().toString();
    AtomicBoolean innerComplete = new AtomicBoolean(false);
    AtomicBoolean outerComplete = new AtomicBoolean(false);

    MDC.put("data", innerUuid);

    MDCPropagationService factory = new MDCPropagationService();

    // should run with the context of main thread
    Runnable inner = factory.create(() -> {
      assertEquals(MDC.get("data"), innerUuid, "Expected data to be propagated to the child thread's MDC");
      innerComplete.set(true);
    });

    // pushes its own context, but runs the inner runnable
    Runnable outer = () -> {
      String outerUuid = UUID.randomUUID().toString();
      MDC.put("data", outerUuid);
      inner.run();
      assertEquals(MDC.get("data"), outerUuid, "Expected MDC data to be replaced with stored data");
      outerComplete.set(true);
    };


    Thread thread = new Thread(outer);
    thread.start();
    thread.join();

    assertEquals(MDC.get("data"), innerUuid, "Expected data to be retained in this thread");
    assertTrue(outerComplete.get(), "Expected the outer thread to run");
    assertTrue(innerComplete.get(), "Expected the inner thread to run");
  }

  @Test(enabled = ENABLED)
  public void testEmpty() throws InterruptedException {

    final AtomicBoolean complete = new AtomicBoolean(false);

    MDC.remove("data");
    assertNull(MDC.get("data"), "Expected MDC data to be empty");

    MDCPropagationService factory = new MDCPropagationService();
    Runnable runnable = factory.create(() -> {
      assertNull(MDC.get("data"), "Expected MDC data to be empty");
      complete.set(true);
    });

    Thread thread = new Thread(runnable);
    thread.start();
    thread.join();

    assertNull(MDC.get("data"), "Expected MDC data to be empty");
    assertTrue(complete.get(), "Expected the inner thread to run");
  }

  @Test(enabled = ENABLED)
  public void testCleanup() throws Exception {

    String innerUuid = UUID.randomUUID().toString();
    AtomicBoolean innerComplete = new AtomicBoolean(false);
    AtomicBoolean outerComplete = new AtomicBoolean(false);

    MDC.put("data", innerUuid);

    MDCPropagationService factory = new MDCPropagationService();

    // should run with the context of main thread
    Runnable inner = factory.create(() -> {
      assertEquals(MDC.get("data"), innerUuid, "Expected data to be propagated to the child thread's MDC");
      innerComplete.set(true);
    });

    // pushes its own context, but runs the inner runnable
    Runnable outer = () -> {
      assertNull(MDC.get("data"), "Expected MDC data not to be copied to this thread");
      inner.run();
      assertNull(MDC.get("data"), "Expected MDC data to remain empty in this thread");
      outerComplete.set(true);
    };

    Thread thread = new Thread(outer);
    thread.start();
    thread.join();

    assertEquals(MDC.get("data"), innerUuid, "Expected MDC data to be retained in parent thread");
    assertTrue(outerComplete.get(), "Expected the outer thread to run");
    assertTrue(innerComplete.get(), "Expected the inner thread to run");
  }

  @Test(enabled = ENABLED)
  public void testCleanupAfterError() throws Exception {

    String innerUuid = UUID.randomUUID().toString();
    AtomicBoolean innerComplete = new AtomicBoolean(false);
    AtomicBoolean outerComplete = new AtomicBoolean(false);
    AtomicBoolean exceptionThrown = new AtomicBoolean(false);

    MDC.put("data", innerUuid);

    MDCPropagationService factory = new MDCPropagationService();

    // should run with the context of main thread
    Runnable inner = factory.create(() -> {
      assertEquals(MDC.get("data"), innerUuid, "Expected data to be propagated to the child thread's MDC");
      innerComplete.set(true);
      throw new RuntimeException();
    });

    // pushes its own context, but runs the inner runnable
    Runnable outer = () -> {

      String outerUuid = UUID.randomUUID().toString();
      MDC.put("data", outerUuid);
      assertEquals(MDC.get("data"), outerUuid, "Expected MDC data to be populated in this thread");

      try {
        inner.run();
      } catch (RuntimeException e) {
        exceptionThrown.set(true);
      } finally {
        assertEquals(MDC.get("data"), outerUuid, "Expected MDC data to be reverted even in case of exception");
        outerComplete.set(true);
      }
    };

    Thread thread = new Thread(outer);
    thread.start();
    thread.join();

    assertEquals(MDC.get("data"), innerUuid, "Expected MDC data to be retained in parent thread");
    assertTrue(outerComplete.get(), "Expected the outer thread to run");
    assertTrue(innerComplete.get(), "Expected the inner thread to run");
    assertTrue(exceptionThrown.get(), "Expected the inner class to throw exception");
  }

}
