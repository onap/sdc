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

import static org.openecomp.sdc.generator.data.GeneratorConstants.ARTIFACT_MODEL_INFO;
import static org.openecomp.sdc.generator.data.GeneratorConstants.BEGIN_TIMESTAMP;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ELAPSED_TIME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.END_TIMESTAMP;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AUDIT_NO_ARTIFACT_TYPE_RESPONSE_DESC;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_METRICS_FAILURE_RESPONSE_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_METRICS_FAILURE_RESPONSE_DESC;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_METRICS_SUCCESS_RESPONSE_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_METRICS_SUCCESS_RESPONSE_DESC;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_METRICS_TARGET_ENTITY;
import static org.openecomp.sdc.generator.data.GeneratorConstants.REQUEST_ID;
import static org.openecomp.sdc.generator.data.GeneratorConstants.RESPONSE_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.RESPONSE_DESCRIPTION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.generator.data.GeneratorConstants.SERVICE_NAME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.STATUS_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.TARGET_ENTITY;
import static org.openecomp.sdc.generator.data.GeneratorConstants.TARGET_SERVICE_NAME;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.artifactGeneratorErrorLogProcessor;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.getLogUtcDateStringFromTimestamp;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.resetLoggingContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.logging.CategoryLogLevel;
import org.openecomp.sdc.generator.logging.StatusCode;
import org.slf4j.MDC;

import java.util.Date;

/**
 * Wraps around any method annotated with
 * {@link org.openecomp.sdc.generator.logging.annotations.Metrics} for logging metrics information
 * In order for the aspect to be used, AspectJ annotation processing must be
 * turned on and this particular aspect enabled.
 *
 * @see org.openecomp.sdc.generator.logging.annotations.Metrics
 */
@Aspect
public class MetricsAspect {

  /**
   * Log Audit information for the method.
   *
   * @param pjp the pjp
   * @return the object
   * @throws Throwable the throwable
   */
  @Around("@annotation(org.openecomp.sdc.generator.logging.annotations.Metrics)")
  public Object logMetrics(ProceedingJoinPoint pjp) throws Throwable {

    final Logger logger = LoggerFactory.getLogger(pjp.getSignature().getDeclaringTypeName());
    String [] modelInfo = MDC.get(ARTIFACT_MODEL_INFO).split(",");

    if (modelInfo.length == 2) {
      //Since ARTIFACT_MODEL_INFO is passed as String from caller "null" value is populated.
      //So resetting "null" to "" in logs
      String serviceInstanceId =  modelInfo[0].equals("null") ? "" : modelInfo[0];
      String requestId =  modelInfo[1].equals("null") ? "" : modelInfo[1];
      MDC.put(SERVICE_INSTANCE_ID, serviceInstanceId);
      MDC.put(REQUEST_ID, requestId);
    }

    MDC.put(TARGET_ENTITY, GENERATOR_METRICS_TARGET_ENTITY);
    MDC.put(TARGET_SERVICE_NAME, MDC.get(SERVICE_NAME) + " artifact generation");
    // check if metrics aspect is enabled
    if (logger.isMetricsEnabled()) {
      final long beginTimestamp = System.currentTimeMillis();
      try {
        MDC.put(RESPONSE_DESCRIPTION, String.format(GENERATOR_METRICS_SUCCESS_RESPONSE_DESC, MDC
            .get(SERVICE_NAME)));
        Object obj = pjp.proceed();
        if (obj instanceof  GenerationData) {
          GenerationData data = (GenerationData) obj;
          if (data.getErrorData() != null && !data.getErrorData().isEmpty()) {
            MDC.put(STATUS_CODE, StatusCode.ERROR.name());
            artifactGeneratorErrorLogProcessor(CategoryLogLevel.ERROR, data.getErrorData().get(
                "AAI").get(0));
            MDC.put(RESPONSE_CODE, MDC.get(ERROR_CODE));
            // MDC.put(RESPONSE_CODE, GENERATOR_METRICS_FAILURE_RESPONSE_CODE);
            // If not service name  found log no artifact type found error.
            if (MDC.get(SERVICE_NAME) == null) {
              MDC.put(RESPONSE_DESCRIPTION, GENERATOR_AUDIT_NO_ARTIFACT_TYPE_RESPONSE_DESC);
            } else {
              MDC.put(RESPONSE_DESCRIPTION,
                  String.format(GENERATOR_METRICS_FAILURE_RESPONSE_DESC, MDC
                      .get(SERVICE_NAME)));
            }
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
        logger.metrics("");
        resetLoggingContext();
      }
    } else {
      return pjp.proceed();
    }
  }
}
