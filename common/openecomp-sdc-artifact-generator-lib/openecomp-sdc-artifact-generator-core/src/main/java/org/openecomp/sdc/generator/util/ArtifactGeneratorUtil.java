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

package org.openecomp.sdc.generator.util;

import static org.openecomp.sdc.generator.data.GeneratorConstants.BEGIN_TIMESTAMP;
import static org.openecomp.sdc.generator.data.GeneratorConstants.BE_FQDN;
import static org.openecomp.sdc.generator.data.GeneratorConstants.CATEGORY_LOG_LEVEL;
import static org.openecomp.sdc.generator.data.GeneratorConstants.CLIENT_IP;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ELAPSED_TIME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.END_TIMESTAMP;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_CATEGORY;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_DESCRIPTION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGFILE_NOT_FOUND;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGLPROP_NOT_FOUND;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_PROVIDING_SERVICE_METADATA_MISSING;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_PROVIDING_SERVICE_MISSING;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_ID;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_RESOURCE_VERSION_IN_SERVICE_TOSCA;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_TOSCA_MSG;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION_MSG;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_MISSING_RESOURCE_TOSCA;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_MISSING_SERVICE_TOSCA_MSG;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_MISSING_SERVICE_VERSION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_NULL_RESOURCE_VERSION_IN_SERVICE_TOSCA;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_INVALID_SERVICE_VERSION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED_MSG;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_ERROR_INVALID_CLIENT_CONFIGURATION_MSG;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_PARTNER_NAME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.INSTANCE_UUID;
import static org.openecomp.sdc.generator.data.GeneratorConstants.LOCAL_ADDR;
import static org.openecomp.sdc.generator.data.GeneratorConstants.MDC_SDC_INSTANCE_UUID;
import static org.openecomp.sdc.generator.data.GeneratorConstants.PARTNER_NAME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.REMOTE_HOST;
import static org.openecomp.sdc.generator.data.GeneratorConstants.REQUEST_ID;
import static org.openecomp.sdc.generator.data.GeneratorConstants.RESPONSE_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.RESPONSE_DESCRIPTION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.generator.data.GeneratorConstants.SERVICE_METRIC_BEGIN_TIMESTAMP;
import static org.openecomp.sdc.generator.data.GeneratorConstants.SERVICE_NAME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.STATUS_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.TARGET_ENTITY;
import static org.openecomp.sdc.generator.data.GeneratorConstants.TARGET_SERVICE_NAME;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.INTERNAL_SERVER_ERROR;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.INVALID_CLIENT_CONFIGURATION;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.INVALID_ID_VALUE;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.INVALID_RESOURCE_VERSION;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.INVALID_SERVICE_VERSION;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.INVALID_TOSCA_YAML;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.MANDATORY_ATTRIBUTE_MISSING;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.MISSING_CONFIG_PROPERTIES_FILE;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.MISSING_PRO_SERVICE;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.MISSING_PRO_SERVICE_METADATA;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.MISSING_RESOURCE_VERSION;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.MISSING_SERVICE_VERSION;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.MISSING_SYSTME_PROPERY_CONFIGURATION;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.MISSING_WIDGET_CONFIGURATION;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.RESOURCE_TOSCA_MISSING;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.SERVICE_TOSCA_MISSING;
import static org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode.UNABLE_TO_GENERATE_ARTIFACT;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.data.Artifact;
import org.openecomp.sdc.generator.logging.ArtifactGeneratorLogResponseCode;
import org.openecomp.sdc.generator.logging.CategoryLogLevel;
import org.openecomp.sdc.generator.logging.StatusCode;
import org.slf4j.MDC;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class ArtifactGeneratorUtil {

  private static Logger log = LoggerFactory.getLogger(ArtifactGeneratorUtil.class.getName());
  private static final String LOG_UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

  /**
   * Artifact Generator Error logging Helper.
   * @param errorDescription Description of the error
   */
  public static void logError(String errorDescription) {
    logError(errorDescription, "");
  }

  /**
   * Artifact Generator Error logging Helper.
   * @param errorDescription Description of the error
   * @param ex Exception object for stackstrace
   */
  public static void logError(String errorDescription, Exception ex) {
    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    String detailMessage = sw.toString();
    logError(CategoryLogLevel.ERROR, errorDescription, detailMessage);
  }

  /**
   * Artifact Generator Error logging Helper.
   * @param errorDescription Description of the error
   * @param detailMessage Detailed Error message
   */
  public static void logError(String errorDescription, String detailMessage) {
    logError(CategoryLogLevel.ERROR, errorDescription, detailMessage);
  }

  /**
   * Artifact Generator Error logging Helper.
   * @param errorCategory    ERROR
   * @param errorDescription Description of the error
   * @param detailMessage Detailed Error message
   */
  public static void logError(CategoryLogLevel errorCategory,
                              String errorDescription, String detailMessage) {
    MDC.put(ERROR_CATEGORY, errorCategory.name());
    MDC.put(STATUS_CODE, StatusCode.ERROR.name());
    artifactGeneratorErrorLogProcessor(errorCategory,errorDescription);
    log.error(detailMessage);
    resetLoggingContext();
  }

  /**
   * Initialize generic MDC attributes for logging the current request.
   *
   */
  public static void initializeLoggingContext() {
    log.debug("Initializing generic logging context ");
    MDC.put(PARTNER_NAME, GENERATOR_PARTNER_NAME);
    MDC.put(SERVICE_METRIC_BEGIN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
    MDC.put(INSTANCE_UUID, MDC_SDC_INSTANCE_UUID);
    MDC.put(STATUS_CODE, StatusCode.COMPLETE.name());
    MDC.put(CLIENT_IP, MDC.get(REMOTE_HOST));

    try {
      InetAddress ip = InetAddress.getLocalHost();
      MDC.put(LOCAL_ADDR, ip.getHostAddress());
      String hostname = ip.getHostName();
      MDC.put(BE_FQDN, hostname);
    } catch (UnknownHostException uhe) {
      log.error("Failed to get server FQDN", uhe);
    }

    if (log.isDebugEnabled()) {
      MDC.put(CATEGORY_LOG_LEVEL, CategoryLogLevel.DEBUG.name());
    } else if (log.isInfoEnabled()) {
      MDC.put(CATEGORY_LOG_LEVEL, CategoryLogLevel.INFO.name());
    } else if (log.isWarnEnabled()) {
      MDC.put(CATEGORY_LOG_LEVEL, CategoryLogLevel.WARN.name());
    } else if (log.isErrorEnabled()) {
      MDC.put(CATEGORY_LOG_LEVEL, CategoryLogLevel.ERROR.name());
    }
  }

  /**
   * Initialize MDC for logging the current artifact request.
   *
   * @param artifact Current artifact
   */
  public static void initializeArtifactLoggingContext(Artifact artifact) {
    log.debug("Initializing logging context for " + artifact.getLabel());
    MDC.put(REQUEST_ID, artifact.getLabel());
    MDC.put(SERVICE_NAME, artifact.getType());
    MDC.put(SERVICE_INSTANCE_ID, artifact.getName());
  }

  /**
   * Reset the logging context after a Audit/Metrics logging operation.
   */
  public static void resetLoggingContext() {
    MDC.remove(ERROR_CATEGORY);
    MDC.remove(ERROR_CODE);
    MDC.remove(STATUS_CODE);
    MDC.remove(ERROR_DESCRIPTION);
    MDC.remove(BEGIN_TIMESTAMP);
    MDC.remove(END_TIMESTAMP);
    MDC.remove(ELAPSED_TIME);
    MDC.put(STATUS_CODE, StatusCode.COMPLETE.name());
    MDC.remove(RESPONSE_CODE);
    MDC.remove(RESPONSE_DESCRIPTION);
    MDC.remove(TARGET_ENTITY);
    MDC.remove(TARGET_SERVICE_NAME);
  }

  /**
   * Convert timestamp to UTC format date string.
   *
   * @param timeStamp UTC timestamp to be converted to the UTC Date format.
   * @return UTC formatted Date string from timestamp.
   */
  public static String getLogUtcDateStringFromTimestamp(Date timeStamp) {
    DateFormat df = new SimpleDateFormat(LOG_UTC_DATE_FORMAT);
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    return df.format(timeStamp);
  }

  /**
   * Artifact Gnenerator Error logging Helper.
   *
   * @param errorCategory    WARN or ERROR.
   * @param errorDescription Description of the error.
   */
  public static void artifactGeneratorErrorLogProcessor(CategoryLogLevel errorCategory,
                                                        String errorDescription) {
    MDC.put(ERROR_CATEGORY, errorCategory.name());
    if (errorDescription != null) {
      String errorType = "";
      switch (errorCategory) {
        case WARN:
          errorType = "W";
          break;
        case ERROR:
          errorType = "E";
          break;
        case FATAL:
          errorType = "F";
          break;
        default:
          break;
      }
      MDC.put(ERROR_CODE, getLogResponseCode(errorDescription) + errorType);
    }
    MDC.put(ERROR_DESCRIPTION, errorDescription);
  }


  /**
   *
   * @return Audit log code corresponding to the Artifact Generator exception.
   */
  public static int getLogResponseCode(String errorDescription) {
    ArtifactGeneratorLogResponseCode responseCode = INTERNAL_SERVER_ERROR;
    if (errorDescription.contains(GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION_MSG)) {
      responseCode = MANDATORY_ATTRIBUTE_MISSING;
    } else if (errorDescription.contains(GENERATOR_AAI_ERROR_INVALID_TOSCA_MSG)) {
      responseCode = INVALID_TOSCA_YAML;
    } else if (errorDescription.contains(GENERATOR_AAI_ERROR_MISSING_SERVICE_TOSCA_MSG)) {
      responseCode = SERVICE_TOSCA_MISSING;
    } else if (errorDescription.contains(GENERATOR_ERROR_INVALID_CLIENT_CONFIGURATION_MSG)) {
      responseCode = INVALID_CLIENT_CONFIGURATION;
    } else if (errorDescription.contains(GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED_MSG)) {
      responseCode = UNABLE_TO_GENERATE_ARTIFACT;
    } else if (errorDescription.contains(GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND.split("%s")[0])) {
      responseCode = MISSING_SYSTME_PROPERY_CONFIGURATION;
    } else if (errorDescription.contains(GENERATOR_AAI_CONFIGFILE_NOT_FOUND.split("%s")[0])) {
      responseCode = MISSING_CONFIG_PROPERTIES_FILE;
    } else if (errorDescription.contains(GENERATOR_AAI_CONFIGLPROP_NOT_FOUND.split("%s")[0])) {
      responseCode = MISSING_WIDGET_CONFIGURATION;
    } else if (errorDescription.contains(GENERATOR_AAI_ERROR_INVALID_ID.split("%s")[0])) {
      responseCode = INVALID_ID_VALUE;
    } else if (errorDescription.contains(GENERATOR_AAI_ERROR_MISSING_RESOURCE_TOSCA.split("%s")[0]))
      {
         responseCode = RESOURCE_TOSCA_MISSING;
    } else if(errorDescription.contains(GENERATOR_AAI_ERROR_MISSING_SERVICE_VERSION)) {
      responseCode = MISSING_SERVICE_VERSION;
    } else if(errorDescription.contains(GENERATOR_AAI_INVALID_SERVICE_VERSION))
    {
      responseCode = INVALID_SERVICE_VERSION;
    } else if(errorDescription.contains(GENERATOR_AAI_ERROR_NULL_RESOURCE_VERSION_IN_SERVICE_TOSCA.
        split("%s")[0])) {
      responseCode = MISSING_RESOURCE_VERSION;
    } else if(errorDescription.contains(
        GENERATOR_AAI_ERROR_INVALID_RESOURCE_VERSION_IN_SERVICE_TOSCA.split("%s")[0])) {
      responseCode = INVALID_RESOURCE_VERSION;
    } else if(errorDescription.contains(GENERATOR_AAI_PROVIDING_SERVICE_MISSING.split("%s")[0])) {
      responseCode = MISSING_PRO_SERVICE;
    } else if(errorDescription.contains(
        GENERATOR_AAI_PROVIDING_SERVICE_METADATA_MISSING.split("%s")[0])) {
      responseCode = MISSING_PRO_SERVICE_METADATA;
    }
    return responseCode.getValue();
  }
}
