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

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.data.Artifact;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.intf.ArtifactGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class GeneratorTask extends RecursiveTask<GenerationData> {

  private static Logger log = LoggerFactory.getLogger(GeneratorTask.class.getName());

  List<Artifact> input;
  List<ArtifactGenerator> generators;
  Map<String, String> additionalParams;

  /**
   * Instantiates a new Generator task.
   *
   * @param generators       the generators
   * @param input            the input
   * @param additionalParams the additional params
   */
  public GeneratorTask(List<ArtifactGenerator> generators, List<Artifact> input,
                       Map<String, String> additionalParams ) {
    this.input = input;
    this.generators = generators;
    this.additionalParams = additionalParams;
  }

  @Override
  protected GenerationData compute() {
    if (generators.size() == 1) {
      log.debug("Instantiating Generator : " + generators.get(0).getClass().getName());
      return generators.remove(0).generateArtifact(input, additionalParams);
    } else {
      LinkedList<ArtifactGenerator> generator = new LinkedList<>();
      generator.add(generators.remove(0));
      GeneratorTask tobeDone = new GeneratorTask(generator, input, additionalParams);
      GeneratorTask tobeForked =
          new GeneratorTask(new LinkedList<ArtifactGenerator>(generators), input, additionalParams);
      tobeForked.fork();
      GenerationData output = tobeDone.compute();
      output.add(tobeForked.join());
      return output;
    }
  }

}
