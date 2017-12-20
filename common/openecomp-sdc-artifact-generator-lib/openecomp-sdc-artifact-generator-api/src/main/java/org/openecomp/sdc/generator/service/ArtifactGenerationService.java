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

package org.openecomp.sdc.generator.service;

import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_CATEGORY;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ERROR_DESCRIPTION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_ERROR_CODE;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_ERROR_SERVICE_INSTANTIATION_FAILED;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_PARTNER_NAME;
import static org.openecomp.sdc.generator.data.GeneratorConstants.PARTNER_NAME;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.data.Artifact;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.logging.CategoryLogLevel;
import org.slf4j.MDC;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;


/**
 * Artifact Generation Service interface.
 */
public interface ArtifactGenerationService {


  /**
   * Method to get artifact generation service implementation.
   *
   * @return Artifact generation implementation instance
   */
  public static ArtifactGenerationService lookup() {

    Logger log = LoggerFactory.getLogger(ArtifactGenerationService.class.getName());
    log.debug("Instantiating Artifact Generation Service");
    try {
      return ArtifactGenerationService.class.cast(
          Class.forName("org.openecomp.sdc.generator.impl.ArtifactGenerationServiceImpl")
              .newInstance());
    } catch (Exception exception) {
      MDC.put(PARTNER_NAME, GENERATOR_PARTNER_NAME);
      MDC.put(ERROR_CATEGORY, CategoryLogLevel.ERROR.name());
      MDC.put(ERROR_CODE, GENERATOR_ERROR_CODE);
      MDC.put(ERROR_DESCRIPTION, GENERATOR_ERROR_SERVICE_INSTANTIATION_FAILED);
      log.error(GENERATOR_ERROR_SERVICE_INSTANTIATION_FAILED, exception);
    }
    log.debug(GENERATOR_ERROR_SERVICE_INSTANTIATION_FAILED);
    return null;
  }

  public GenerationData generateArtifact(List<Artifact> input, String overridingConfiguration,
                                         Map<String, String> additionalParams);

}
