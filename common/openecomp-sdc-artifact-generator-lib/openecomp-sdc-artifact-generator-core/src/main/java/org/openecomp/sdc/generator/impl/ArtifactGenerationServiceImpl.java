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

package org.openecomp.sdc.generator.impl;

import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_INVOCATION_ERROR_CODE;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.logError;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.GeneratorManager;
import org.openecomp.sdc.generator.GeneratorTask;
import org.openecomp.sdc.generator.data.Artifact;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.intf.ArtifactGenerator;
import org.openecomp.sdc.generator.service.ArtifactGenerationService;
import org.openecomp.sdc.generator.util.ArtifactGeneratorUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/**
 * Artifact Generation Service implementation class.
 */
public class ArtifactGenerationServiceImpl implements ArtifactGenerationService {

  private static Logger log =
      LoggerFactory.getLogger(ArtifactGenerationServiceImpl.class.getName());

  /**
   * Artifact generator method.
   *
   * @param input                   List of input files as {@link Artifact} models
   * @param overridingConfiguration Configuration data for invoking generators
   * @param additionalParams Additional Parameters
   * @return Generated artifacts/Error data in a {@link GenerationData} object
   */
  @Override
  public GenerationData generateArtifact(List<Artifact> input, String overridingConfiguration,
                                         Map<String, String> additionalParams) {
    try {
      //Initialize artifact generation logging context
      ArtifactGeneratorUtil.initializeLoggingContext();

      List<ArtifactGenerator> generatorsToBeUsed =
          GeneratorManager.getActiveArtifactGenerators(overridingConfiguration);
      if (generatorsToBeUsed.size() > 0) {
        return ForkJoinPool.commonPool().invoke(new GeneratorTask(generatorsToBeUsed, input,
            additionalParams));
      } else {
        return new GenerationData();
      }
    } catch (IllegalArgumentException iae) {
      //Invalid client configuration
      logError(GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED, iae);
      GenerationData errorData = new GenerationData();
      errorData.add(GENERATOR_INVOCATION_ERROR_CODE, iae.getMessage());
      return errorData;
    } catch (Exception ex) {
      logError(GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED, ex);
      GenerationData errorData = new GenerationData();
      errorData.add(GENERATOR_INVOCATION_ERROR_CODE,
                    GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED);
      return errorData;
    }
  }
}
