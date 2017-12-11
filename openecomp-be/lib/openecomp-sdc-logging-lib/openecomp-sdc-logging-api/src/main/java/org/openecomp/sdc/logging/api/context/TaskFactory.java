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

package org.openecomp.sdc.logging.api.context;

import org.openecomp.sdc.logging.api.BaseFactory;

/**
 * <p>Should be used to propagate a diagnostic context (for instance <a
 * href="http://www.slf4j.org/manual.html#mdc">MDC</a>) to other threads.</p>
 * <p>Applicable when creating a child thread directly, or submitting tasks for potentially
 * postponed execution via an <a href="http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executor.html">Executor</a>
 * (including any of the <a href="http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html">executor
 * services</a> and <a href="http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html">ForkJoinPool</a>).</p>
 * <p>The service used by this factory must implement {@link ContextPropagationService}.</p>
 *
 * @author evitaliy
 * @see ContextPropagationService
 * @since 12/09/2016.
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public class TaskFactory extends BaseFactory {

  private static final ContextPropagationService SERVICE;
  private static final RuntimeException ERROR;

  static {

    ContextPropagationService service = null;
    RuntimeException error = null;

    try {
      service = locateService(ContextPropagationService.class);
    } catch (Exception ex) {
      error = new RuntimeException("Failed to instantiate task factory", ex);
    }

    SERVICE = service;
    ERROR = error;
  }

  /**
   * Modify a task so that a diagnostic context is propagated to the thread when the task runs. Done
   * in a logging-framework specific way.
   *
   * @param task any Runnable that will run in a thread
   * @return modified (wrapped) original task that runs the same business logic, but also takes care
   * of copying the diagnostic context for logging
   */
  public static Runnable create(Runnable task) {

    if (SERVICE == null) {
      throw ERROR;
    }

    return SERVICE.create(task);
  }
}
