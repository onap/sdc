package org.openecomp.sdc.enrichment.impl.tosca;

import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PortMirroringEnricherTest extends BaseToscaEnrichmentTest {

  private PortMirroringEnricher portMirroringEnricher;

  @BeforeMethod(alwaysRun = true)
  public void init() throws IOException {
    portMirroringEnricher = new PortMirroringEnricher();
  }

  @Test
  public void testEnrichNoPorts() throws Exception {
    outputFilesPath = "/mock/enrichPortMirroring/noPorts/out/";
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichPortMirroring/noPorts/in/",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");
    Map<String, List<ErrorMessage>> errors =
        portMirroringEnricher.enrich(toscaServiceModel);

    compareActualAndExpectedModel(toscaServiceModel);
  }

  @Test
  public void testEnrichSingleSubstitutionSamePortType() throws Exception {
    outputFilesPath = "/mock/enrichPortMirroring/singleSubstitution/samePortType/out/";
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichPortMirroring/singleSubstitution/samePortType/in",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");
    Map<String, List<ErrorMessage>> errors =
        portMirroringEnricher.enrich(toscaServiceModel);

    compareActualAndExpectedModel(toscaServiceModel);
  }

  @Test
  public void testEnrichSingleSubstitutionDiffPortType() throws Exception {
    outputFilesPath = "/mock/enrichPortMirroring/singleSubstitution/diffPortType/out/";
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichPortMirroring/singleSubstitution/diffPortType/in",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");
    Map<String, List<ErrorMessage>> errors =
        portMirroringEnricher.enrich(toscaServiceModel);

    compareActualAndExpectedModel(toscaServiceModel);
  }

  @Test
  public void testEnrichScalingInstance() throws Exception {
    outputFilesPath = "/mock/enrichPortMirroring/scalingInstance/out/";
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichPortMirroring/scalingInstance/in",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");
    Map<String, List<ErrorMessage>> errors =
        portMirroringEnricher.enrich(toscaServiceModel);

    compareActualAndExpectedModel(toscaServiceModel);
  }

  @Test
  public void testEnrichCatalogInstance() throws Exception {
    outputFilesPath = "/mock/enrichPortMirroring/catalogInstance/out/";
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichPortMirroring/catalogInstance/in",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");
    Map<String, List<ErrorMessage>> errors =
        portMirroringEnricher.enrich(toscaServiceModel);

    compareActualAndExpectedModel(toscaServiceModel);
  }

  @Test
  public void testEnrichNestedOneLevel() throws Exception {
    outputFilesPath = "/mock/enrichPortMirroring/nested/oneLevel/out/";
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichPortMirroring/nested/oneLevel/in",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");
    Map<String, List<ErrorMessage>> errors =
        portMirroringEnricher.enrich(toscaServiceModel);

    compareActualAndExpectedModel(toscaServiceModel);
  }

  @Test
  public void testEnrichNestedMultiLevel() throws Exception {
    outputFilesPath = "/mock/enrichPortMirroring/nested/multiLevel/out/";
    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichPortMirroring/nested/multiLevel/in",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");
    Map<String, List<ErrorMessage>> errors =
        portMirroringEnricher.enrich(toscaServiceModel);

    compareActualAndExpectedModel(toscaServiceModel);
  }

}
