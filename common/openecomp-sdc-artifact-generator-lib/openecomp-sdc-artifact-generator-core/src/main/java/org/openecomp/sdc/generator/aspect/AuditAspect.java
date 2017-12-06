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

package org.openecomp.sdc.generator.aspect;

import static org.openecomp.sdc.generator.data.GeneratorConstants.BEGIN_TIMESTAMP;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ELAPSED_TIME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.END_TIMESTAMP;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AUDIT_NO_ARTIFACT_TYPE_RESPONSE_DESC;
import static org.openecomp.sdc.generator.data.GeneratorConstants
    .GENERATOR_METRICS_FAILURE_RESPONSE_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_METRICS_FAILURE_RESPONSE_DESC;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_METRICS_SUCCESS_RESPONSE_DESC;

import static org.openecomp.sdc.generator.data.GeneratorConstants.RESPONSE_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.RESPONSE_DESCRIPTION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.SERVICE_NAME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.STATUS_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.TARGET_ENTITY;
import static org.openecomp.sdc.generator.data.GeneratorConstants.TARGET_SERVICE_NAME;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.artifactGeneratorErrorLogProcessor;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.getLogUtcDateStringFromTimestamp;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.logError;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.resetLoggingContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.logging.CategoryLogLevel;
import org.openecomp.sdc.generator.logging.StatusCode;
import org.openecomp.sdc.generator.util.ArtifactGeneratorUtil;
import org.slf4j.MDC;

import java.util.Date;

/**
 * Wraps around any method annotated with
 * {@link org.openecomp.sdc.generator.logging.annotations.Audit} for auditing information
 * In order for the aspect to be used, AspectJ annotation processing must be
 * turned on and this particular aspect enabled.
 *
 * @see org.openecomp.sdc.generator.logging.annotations.Audit
 */
@Aspect
public class AuditAspect {

  /**
   * Log Audit information for the method.
   *
   * @param pjp the pjp
   * @return the object
   * @throws Throwable the throwable
   */
  @Around("@annotation(org.openecomp.sdc.generator.logging.annotations.Audit)")
  public Object logAuditInfo(ProceedingJoinPoint pjp) throws Throwable {

    final Logger logger = LoggerFactory.getLogger(pjp.getSignature().getDeclaringTypeName());
    // check if audit aspect is enabled
    if (logger.isAuditEnabled()) {
      final String method = pjp.getSignature().getName();
      final long beginTimestamp = System.currentTimeMillis();
      try {
        Object obj = pjp.proceed();
        MDC.put(RESPONSE_DESCRIPTION, String.format(GENERATOR_METRICS_SUCCESS_RESPONSE_DESC, MDC
            .get(SERVICE_NAME)));

        GenerationData data = (GenerationData)obj;
        if (data.getErrorData() != null && !data.getErrorData().isEmpty()) {
          MDC.put(STATUS_CODE, StatusCode.ERROR.name());
          artifactGeneratorErrorLogProcessor(CategoryLogLevel.ERROR,data.getErrorData().get("AAI")
              .get(0));
          MDC.put(RESPONSE_CODE, MDC.get(ERROR_CODE));
          // MDC.put(RESPONSE_CODE, GENERATOR_METRICS_FAILURE_RESPONSE_CODE);
          // If not service name  found log no artifact type found error.
          if (MDC.get(SERVICE_NAME) == null) {
            MDC.put(RESPONSE_DESCRIPTION, GENERATOR_AUDIT_NO_ARTIFACT_TYPE_RESPONSE_DESC);
          } else {
            MDC.put(RESPONSE_DESCRIPTION, String.format(GENERATOR_METRICS_FAILURE_RESPONSE_DESC, MDC
                .get(SERVICE_NAME)));
          }
        }
        return obj;

      } catch (Exception ex) {
        artifactGeneratorErrorLogProcessor(CategoryLogLevel.ERROR,ex.getMessage());
        MDC.put(STATUS_CODE, StatusCode.ERROR.name());
        MDC.put(RESPONSE_CODE, MDC.get(ERROR_CODE));
        MDC.put(RESPONSE_DESCRIPTION, String.format(GENERATOR_METRICS_FAILURE_RESPONSE_DESC, MDC
              .get(SERVICE_NAME)));
        throw ex;
      } finally {
        long endTimestamp = System.currentTimeMillis();
        MDC.put(BEGIN_TIMESTAMP, getLogUtcDateStringFromTimestamp(new Date(beginTimestamp)));
        MDC.put(END_TIMESTAMP, getLogUtcDateStringFromTimestamp(new Date(endTimestamp)));
        MDC.put(ELAPSED_TIME, String.valueOf(endTimestamp - beginTimestamp));

        logger.audit("");
        resetLoggingContext();
        MDC.remove(SERVICE_NAME);
      }
    } else {
      return pjp.proceed();
    }
  }
}
