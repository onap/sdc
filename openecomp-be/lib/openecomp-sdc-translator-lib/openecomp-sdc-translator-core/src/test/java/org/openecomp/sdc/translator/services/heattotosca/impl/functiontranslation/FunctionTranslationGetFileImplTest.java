/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.FunctionTranslationFactory;

import java.util.HashMap;
import java.util.List;

import static org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator.getFunctionTranslateTo;

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
    Assert.assertTrue(FunctionTranslationFactory.getInstance(functionName).isPresent());
    if(FunctionTranslationFactory.getInstance(functionName).isPresent()) {
      FunctionTranslator functionTranslator = new FunctionTranslator(getFunctionTranslateTo(null, null, heatFileName,
              heatOrchestrationTemplate, context), null, function, nodeTemplate);
      Object result = FunctionTranslationFactory.getInstance(functionName).get()
          .translateFunction(functionTranslator);
      Assert.assertNotNull(((HashMap) result).get("get_artifact"));
      List artifactParameters = (List) ((HashMap) result).get("get_artifact");
      Assert.assertNotNull(artifactParameters);
      Assert.assertEquals(2, artifactParameters.size());
      Assert.assertEquals(ToscaConstants.MODELABLE_ENTITY_NAME_SELF, artifactParameters.get(0));
      Assert.assertEquals(((String) function).split("\\.")[0], artifactParameters.get(1));

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
