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

package org.openecomp.sdc.logging.context;

import org.slf4j.MDC;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.*;

/**
 * @author evitaliy
 * @since 12/09/2016.
 */
public class MDCPropagationFactoryTest {

  //@Test
  public void testNoPropagation() throws InterruptedException {

    String uuid = UUID.randomUUID().toString();
    AtomicBoolean complete = new AtomicBoolean(false);
    MDC.put("data", uuid);

    Runnable runnable = () -> {
      assertNull(MDC.get("data"));
      complete.set(true);
    };
    Thread thread = new Thread(runnable);
    thread.start();
    thread.join();

    assertEquals(MDC.get("data"), uuid);
    assertTrue(complete.get());
  }

  @Test
  public void testPropagation() throws InterruptedException {

    String uuid = UUID.randomUUID().toString();
    AtomicBoolean complete = new AtomicBoolean(false);
    MDC.put("data", uuid);

    MDCPropagationService factory = new MDCPropagationService();
    Runnable runnable = factory.create(() -> {
      assertEquals(MDC.get("data"), uuid);
      complete.set(true);
    });

    Thread thread = new Thread(runnable);
    thread.start();

    thread.join();

    assertEquals(MDC.get("data"), uuid);
    assertTrue(complete.get());
  }

  @Test
  public void testReplacement() throws InterruptedException {

    String innerUuid = UUID.randomUUID().toString();
    AtomicBoolean innerComplete = new AtomicBoolean(false);
    AtomicBoolean outerComplete = new AtomicBoolean(false);

    MDC.put("data", innerUuid);

    MDCPropagationService factory = new MDCPropagationService();

    // should run with the context of main thread
    Runnable inner = factory.create(() -> {
      assertEquals(MDC.get("data"), innerUuid);
      innerComplete.set(true);
    });

    // pushes its own context, but runs the inner runnable
    Runnable outer = () -> {
      String outerUuid = UUID.randomUUID().toString();
      MDC.put("data", outerUuid);
      inner.run();
      assertEquals(MDC.get("data"), outerUuid);
      outerComplete.set(true);
    };


    Thread thread = new Thread(outer);
    thread.start();
    thread.join();

    assertEquals(MDC.get("data"), innerUuid);
    assertTrue(outerComplete.get());
    assertTrue(innerComplete.get());
  }

  @Test
  public void testEmpty() throws InterruptedException {

    final AtomicBoolean complete = new AtomicBoolean(false);

    MDC.remove("data");
    assertNull(MDC.get("data"));

    MDCPropagationService factory = new MDCPropagationService();
    Runnable runnable = factory.create(() -> {
      assertNull(MDC.get("data"));
      complete.set(true);
    });

    Thread thread = new Thread(runnable);
    thread.start();
    thread.join();

    assertNull(MDC.get("data"));
    assertTrue(complete.get());
  }

  //@Test
  public void testCleanup() throws Exception {

    String innerUuid = UUID.randomUUID().toString();
    AtomicBoolean innerComplete = new AtomicBoolean(false);
    AtomicBoolean outerComplete = new AtomicBoolean(false);

    MDC.put("data", innerUuid);

    MDCPropagationService factory = new MDCPropagationService();

    // should run with the context of main thread
    Runnable inner = factory.create(() -> {
      assertEquals(MDC.get("data"), innerUuid);
      innerComplete.set(true);
    });

    // pushes its own context, but runs the inner runnable
    Runnable outer = () -> {
      assertNull(MDC.get("data"));
      inner.run();
      assertNull(MDC.get("data"));
      outerComplete.set(true);
    };

    Thread thread = new Thread(outer);
    thread.start();
    thread.join();

    assertEquals(MDC.get("data"), innerUuid);
    assertTrue(outerComplete.get());
    assertTrue(innerComplete.get());
  }

  @Test
  public void testCleanupAfterError() throws Exception {

    String innerUuid = UUID.randomUUID().toString();
    AtomicBoolean innerComplete = new AtomicBoolean(false);
    AtomicBoolean outerComplete = new AtomicBoolean(false);
    AtomicBoolean exceptionThrown = new AtomicBoolean(false);

    MDC.put("data", innerUuid);

    MDCPropagationService factory = new MDCPropagationService();

    // should run with the context of main thread
    Runnable inner = factory.create(() -> {
      assertEquals(MDC.get("data"), innerUuid);
      innerComplete.set(true);
      throw new RuntimeException();
    });

    // pushes its own context, but runs the inner runnable
    Runnable outer = () -> {

      String outerUuid = UUID.randomUUID().toString();
      MDC.put("data", outerUuid);
      assertEquals(MDC.get("data"), outerUuid);

      try {
        inner.run();
      } catch (RuntimeException e) {
        exceptionThrown.set(true);
      } finally {
        assertEquals(MDC.get("data"), outerUuid);
        outerComplete.set(true);
      }
    };

    Thread thread = new Thread(outer);
    thread.start();
    thread.join();

    assertEquals(MDC.get("data"), innerUuid);
    assertTrue(outerComplete.get());
    assertTrue(innerComplete.get());
    assertTrue(exceptionThrown.get());
  }

}
