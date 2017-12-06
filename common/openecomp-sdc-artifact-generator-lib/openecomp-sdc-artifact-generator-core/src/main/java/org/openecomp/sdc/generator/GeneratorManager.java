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

package org.openecomp.sdc.generator;

import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_ERROR_INVALID_CLIENT_CONFIGURATION;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.logError;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.data.ArtifactType;
import org.openecomp.sdc.generator.data.GeneratorConfiguration;
import org.openecomp.sdc.generator.intf.ArtifactGenerator;
import org.openecomp.sdc.generator.intf.Generator;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeneratorManager {

  private static Logger log = LoggerFactory.getLogger(GeneratorManager.class.getName());
  private static Map<ArtifactType, ArtifactGenerator> generators = new HashMap<>();

  /**
   * Gets active artifact generators.
   *
   * @param clientConfiguration the client configuration
   * @return the active artifact generators
   * @throws Exception the exception
   */
  public static List<ArtifactGenerator> getActiveArtifactGenerators(String clientConfiguration)
      throws Exception {

    if (generators.isEmpty()) {
      log.debug("Getting list of active generators");
      Reflections reflections = new Reflections("org.openecomp.sdc.generator");
      Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Generator.class);
      for (Class<?> clazz : annotated) {
        Generator generator = clazz.getAnnotation(Generator.class);
        generators.put(generator.artifactType(), (ArtifactGenerator) clazz.newInstance());
      }
    }

    log.debug("Parsing generator configuration from the client configuration : "
        + clientConfiguration);
    GeneratorConfiguration gf = getGeneratorConfiguration(clientConfiguration);
    List<ArtifactGenerator> generatorList = new ArrayList<>();
    if (gf.getArtifactTypes() != null && !gf.getArtifactTypes().isEmpty()) {
      for (ArtifactType type : gf.getArtifactTypes()) {
        if (generators.get(type) != null) {
          generatorList.add(generators.get(type));
        }
      }
    }

    return generatorList;
  }

  private static GeneratorConfiguration getGeneratorConfiguration(String jsonConfiguration) {
    try {
      return new ObjectMapper().readValue(jsonConfiguration, GeneratorConfiguration.class);
    } catch (Exception exception) {
      logError(GENERATOR_ERROR_INVALID_CLIENT_CONFIGURATION, exception);
      throw new IllegalArgumentException(
          GENERATOR_ERROR_INVALID_CLIENT_CONFIGURATION, exception);
    }
  }

}
