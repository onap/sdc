package org.openecomp.sdc.enrichment.impl.tosca;


import static org.mockito.Mockito.when;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.HIGH_AVAIL_MODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MANDATORY;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MAX_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MIN_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VFC_NAMING_CODE;

import org.apache.commons.collections.map.HashedMap;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AbstractSubstituteToscaEnricherTest extends BaseToscaEnrichmentTest {
  @Mock
  ComponentQuestionnaireData utilMock;

  @InjectMocks
  AbstractSubstituteToscaEnricher toscaEnricher;

  String vspId = null;
  Version version = new Version();

  @BeforeMethod(alwaysRun = true)
  public void injectDoubles() {
    MockitoAnnotations.initMocks(this);
    vspId = "123";
    version.setMajor(1);
    version.setMinor(0);
  }

  @Test
  public void testEnrich() throws Exception {
    outputFilesPath = "/mock/enrichHA/out/";

    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichHA/in/", "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");

    Map<String, Map<String, Object>> componentTypetoParams = new HashMap();
    Map<String, Object> innerProps = new HashedMap();
    innerProps.put(MANDATORY, "YES");
    innerProps.put(HIGH_AVAIL_MODE, "geo-activestandby");
    innerProps.put(VFC_NAMING_CODE, "Code1");
    innerProps.put(MIN_INSTANCES, 1);
    innerProps.put(MAX_INSTANCES, 2);

    componentTypetoParams.put("pd_server", innerProps);

    when(utilMock.getPropertiesfromCompQuestionnaire(vspId,version)).thenReturn
        (componentTypetoParams);

    Map<String,String> map = new HashMap<String,String>();
    Map<String, List<String>> sourceToTargetDependencies = new HashMap<String, List<String>>();
    List<String> targets = new ArrayList<String>();
    targets.add("fe"); targets.add("be");
    sourceToTargetDependencies.put("pd_server", targets);

    when(utilMock.getSourceToTargetComponent()).thenReturn(map);

    when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

    Map<String, List<ErrorMessage>> errors =
        toscaEnricher.enrich(toscaServiceModel, vspId, version );

    compareActualAndExpectedModel(toscaServiceModel);

    Assert.assertEquals(errors.size(), 0);
  }

  @Test
  public void testEnrichWithoutServiceTemplateFilter() throws Exception {
    outputFilesPath = "/mock/enrichHANoServiceTemplateFilter/out";

    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichHANoServiceTemplateFilter/in",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");

    Map<String, Map<String, Object>> componentTypetoParams = new HashMap();
    Map<String, Object> innerProps = new HashedMap();
    innerProps.put(MANDATORY, "NO");
    innerProps.put(HIGH_AVAIL_MODE, "");
    innerProps.put(VFC_NAMING_CODE, "pd_server_code1");
    innerProps.put(MIN_INSTANCES, null);
    innerProps.put(MAX_INSTANCES, null);

    componentTypetoParams.put("pd_server", innerProps);

    when(utilMock.getPropertiesfromCompQuestionnaire(vspId,version)).thenReturn
        (componentTypetoParams);

    Map<String,String> map = new HashMap<String,String>();
    Map<String, List<String>> sourceToTargetDependencies = new HashMap<String, List<String>>();

    when(utilMock.getSourceToTargetComponent()).thenReturn(map);
    when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

    Map<String, List<ErrorMessage>> errors =
        toscaEnricher.enrich(toscaServiceModel, vspId, version );

    compareActualAndExpectedModel(toscaServiceModel);

    Assert.assertEquals(errors.size(), 0);
  }

  @Test
  public void testEnrichNotMandatory() throws Exception {
    outputFilesPath = "/mock/enrichHANotMandatory/out";

    ToscaServiceModel toscaServiceModel =
        loadToscaServiceModel("/mock/enrichHANotMandatory/in",
            "/mock/toscaGlobalServiceTemplates/",
            "MainServiceTemplate.yaml");

    Map<String, Map<String, Object>> componentTypetoParams = new HashMap();
    Map<String, Object> innerProps = new HashedMap();

    innerProps.put(MANDATORY, "");
    innerProps.put(MIN_INSTANCES, 1);
    innerProps.put(MAX_INSTANCES, 5);

    componentTypetoParams.put("pd_server_vm", innerProps);

    when(utilMock.getPropertiesfromCompQuestionnaire(vspId,version)).thenReturn
        (componentTypetoParams);

    Map<String,String> map = new HashMap<String,String>();
    Map<String, List<String>> sourceToTargetDependencies = new HashMap<String, List<String>>();
    List<String> targets = new ArrayList<String>();
    targets.add("fe");
    sourceToTargetDependencies.put("pd_server_vm", targets);

    when(utilMock.getSourceToTargetComponent()).thenReturn(map);

    when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

    when(utilMock.getSourceToTargetComponent()).thenReturn(map);
    when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

    Map<String, List<ErrorMessage>> errors =
        toscaEnricher.enrich(toscaServiceModel, vspId, version );

    compareActualAndExpectedModel(toscaServiceModel);

    Assert.assertEquals(errors.size(), 0);
  }
}
