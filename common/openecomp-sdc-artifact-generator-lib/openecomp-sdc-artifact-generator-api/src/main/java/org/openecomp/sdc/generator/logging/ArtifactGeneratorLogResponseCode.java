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

package org.openecomp.sdc.generator.logging;

import java.util.HashMap;
import java.util.Map;

public enum ArtifactGeneratorLogResponseCode {
  INTERNAL_SERVER_ERROR(201),
  MISSING_CONFIG_PROPERTIES_FILE(202),
  MISSING_SYSTME_PROPERY_CONFIGURATION(203),
  MANDATORY_ATTRIBUTE_MISSING(313),
  SERVICE_TOSCA_MISSING(314),
  INVALID_TOSCA_YAML(315),
  INVALID_CLIENT_CONFIGURATION(316),
  UNABLE_TO_GENERATE_ARTIFACT(317),
  MISSING_WIDGET_CONFIGURATION(318),
  INVALID_ID_VALUE(319),
  MISSING_SERVICE_VERSION(320),
  INVALID_SERVICE_VERSION(321),
  MISSING_RESOURCE_VERSION(322),
  INVALID_RESOURCE_VERSION(323),
  MISSING_PRO_SERVICE(324),
  MISSING_PRO_SERVICE_METADATA(325),
  RESOURCE_TOSCA_MISSING(326);


  private static Map<Integer, ArtifactGeneratorLogResponseCode> mapValueToEnum = new HashMap<>();

  static {
    for (ArtifactGeneratorLogResponseCode responseCode :
        ArtifactGeneratorLogResponseCode.values()) {
      mapValueToEnum.put(responseCode.value, responseCode);
    }
  }

  private int value;

  ArtifactGeneratorLogResponseCode(int value) {
    this.value = value;
  }

  public static ArtifactGeneratorLogResponseCode parseValue(int value) {
    return mapValueToEnum.get(value);
  }

  public int getValue() {
    return value;
  }
}
