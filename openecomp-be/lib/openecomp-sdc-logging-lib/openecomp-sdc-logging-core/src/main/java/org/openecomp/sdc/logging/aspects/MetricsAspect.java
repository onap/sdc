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
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.annotations.Metrics;

/**
 * <p>Wraps around any method annotated with {@link Metrics} to measure and log its execution time
 * in milliseconds.</p>
 * <p>In order for the aspect to be used, AspectJ annotation processing must be tuned on and this
 * particular aspect enabled. Conversely, it can be disabled completely if the application does not
 * need to log metrics.</p>
 * <p>See, for example, <a href="http://docs.spring.io/spring/docs/current/spring-framework-reference/html/aop.html">
 * Aspect Oriented Programming with Spring</a>.</p>
 *
 * @author evitaliy
 * @see Metrics
 * @since 27/07/2016.
 */
@Aspect
public class MetricsAspect {

  private static final String MESSAGE_TEMPLATE = "'{}' took {} milliseconds";

  @Around("@annotation(org.openecomp.sdc.logging.api.annotations.Metrics)")
  public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {

    final Logger logger = LoggerFactory.getLogger(pjp.getSignature().getDeclaringTypeName());
    // measure and log only if the logger for this class is enabled
    if (logger.isMetricsEnabled()) {

      final String method = pjp.getSignature().getName();
      final long start = System.currentTimeMillis();

      try {
        return pjp.proceed();
      } finally {
        logger.metrics(MESSAGE_TEMPLATE, method, System.currentTimeMillis() - start);
      }

    } else {
      return pjp.proceed();
    }
  }
}
