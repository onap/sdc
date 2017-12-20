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

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;

import java.util.HashMap;
import java.util.List;

/**
 * @author SHIRIA
 * @since December 18, 2016.
 */
public class FunctionTranslationGetFileImplTest {
  @Test
  public void testGetFileWithExtensionFunction() {
    String functionName = "get_file";
    Object function = "scripFileName.sh";
    String heatFileName = "heatFileName";
    HeatOrchestrationTemplate heatOrchestrationTemplate = new HeatOrchestrationTemplate();
    NodeTemplate nodeTemplate = new NodeTemplate();
    TranslationContext context = new TranslationContext();

    testGetToscaFunctionForGetFile(functionName, function, heatFileName, heatOrchestrationTemplate,
        nodeTemplate, context);
  }

  @Test
  public void testGetFileWithoutExtensionFunction() {
    String functionName = "get_file";
    Object function = "scripFileName";
    String heatFileName = "heatFileName";
    HeatOrchestrationTemplate heatOrchestrationTemplate = new HeatOrchestrationTemplate();
    NodeTemplate nodeTemplate = new NodeTemplate();
    TranslationContext context = new TranslationContext();

    //#      route_targets: { "Fn::Split" : [ ",", Ref: route_targets ] }
    testGetToscaFunctionForGetFile(functionName, function, heatFileName, heatOrchestrationTemplate,
        nodeTemplate, context);
  }

  private void testGetToscaFunctionForGetFile(String functionName, Object function,
                                              String heatFileName,
                                              HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              NodeTemplate nodeTemplate,
                                              TranslationContext context) {
    Assert.assertEquals(true, FunctionTranslationFactory.getInstance(functionName).isPresent());
    if(FunctionTranslationFactory.getInstance(functionName).isPresent()) {
      Object result = FunctionTranslationFactory.getInstance(functionName).get()
          .translateFunction(null, null, null, functionName, function, heatFileName,
              heatOrchestrationTemplate,
              nodeTemplate, context);
      Assert.assertNotNull(((HashMap) result).get("get_artifact"));
      List artifactParameters = (List) ((HashMap) result).get("get_artifact");
      Assert.assertNotNull(artifactParameters);
      Assert.assertEquals(artifactParameters.size(), 2);
      Assert.assertEquals(artifactParameters.get(0), ToscaConstants.MODELABLE_ENTITY_NAME_SELF);
      Assert.assertEquals(artifactParameters.get(1), ((String) function).split("\\.")[0]);

      Assert.assertNotNull(nodeTemplate.getArtifacts());
      Assert.assertNotNull(
          nodeTemplate.getArtifacts().get(FileUtils.getFileWithoutExtention((String) function)));
      ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
      Assert.assertEquals(
          nodeTemplate.getArtifacts().get(FileUtils.getFileWithoutExtention((String) function))
              .getFile(), "../" + toscaFileOutputService.getArtifactsFolderName() + "/" + function);
    }
  }

}
