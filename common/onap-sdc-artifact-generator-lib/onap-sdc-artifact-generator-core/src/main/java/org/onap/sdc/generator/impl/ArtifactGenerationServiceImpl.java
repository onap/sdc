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

package org.onap.sdc.generator.impl;

import org.onap.sdc.generator.GeneratorManager;
import org.onap.sdc.generator.GeneratorTask;
import org.onap.sdc.generator.data.Artifact;
import org.onap.sdc.generator.data.GenerationData;
import org.onap.sdc.generator.data.GeneratorConstants;
import org.onap.sdc.generator.intf.ArtifactGenerator;
import org.onap.sdc.generator.service.ArtifactGenerationService;
import org.onap.sdc.generator.util.ArtifactGeneratorUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import static org.onap.sdc.generator.util.ArtifactGeneratorUtil.logError;

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
      ArtifactGeneratorUtil
          .logError(GeneratorConstants.GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED, iae);
      GenerationData errorData = new GenerationData();
      errorData.add(GeneratorConstants.GENERATOR_INVOCATION_ERROR_CODE, iae.getMessage());
      return errorData;
    } catch (Exception ex) {
      ArtifactGeneratorUtil
          .logError(GeneratorConstants.GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED, ex);
      GenerationData errorData = new GenerationData();
      errorData.add(GeneratorConstants.GENERATOR_INVOCATION_ERROR_CODE,
                    GeneratorConstants.GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED);
      return errorData;
    }
  }
}
