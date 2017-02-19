package org.openecomp.sdc.translator.services.heattotosca.mapping;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.services.heattotosca.TranslationContext;
import org.openecomp.core.utilities.file.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class TranslatorHeatToToscaFunctionConverterTest {

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
    Object result = TranslatorHeatToToscaFunctionConverter
        .getToscaFunction(functionName, function, heatFileName, heatOrchestrationTemplate,
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
