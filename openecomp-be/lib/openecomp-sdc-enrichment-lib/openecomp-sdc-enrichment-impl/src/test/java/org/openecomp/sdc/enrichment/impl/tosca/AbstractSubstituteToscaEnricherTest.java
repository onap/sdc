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

 package org.openecomp.sdc.enrichment.impl.tosca;


 import static org.mockito.Mockito.when;
 import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.HIGH_AVAIL_MODE;
 import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MANDATORY;
 import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MAX_INSTANCES;
 import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MIN_INSTANCES;
 import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.NFC_FUNCTION;
 import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.NFC_NAMING_CODE;
 import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VFC_CODE;

 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.openecomp.sdc.datatypes.error.ErrorMessage;
 import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
 import org.openecomp.sdc.versioning.dao.types.Version;


 public class AbstractSubstituteToscaEnricherTest extends BaseToscaEnrichmentTest {
   @Mock
   ComponentQuestionnaireData utilMock;

   @InjectMocks
   AbstractSubstituteToscaEnricher toscaEnricher;

   private String vspId = null;
   private Version version = new Version();

   @Before
   public void injectDoubles() {
     MockitoAnnotations.openMocks(this);
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

     Map<String, Map<String, Object>> componentTypetoParams = new HashMap<>();
     Map<String, Object> innerProps = new HashMap<>();
     innerProps.put(MANDATORY, "YES");
     innerProps.put(HIGH_AVAIL_MODE, "geo-activestandby");
     innerProps.put(NFC_NAMING_CODE, "Code1");
     innerProps.put(VFC_CODE, "pd_server_code");
     innerProps.put(NFC_FUNCTION, "pd_server_description");
     innerProps.put(MIN_INSTANCES, 1);
     innerProps.put(MAX_INSTANCES, 2);

     componentTypetoParams.put("pd_server", innerProps);

     when(utilMock.getPropertiesfromCompQuestionnaire(vspId,version)).thenReturn
         (componentTypetoParams);

     Map<String,String> map = new HashMap<>();
     Map<String, List<String>> sourceToTargetDependencies = new HashMap<>();
     List<String> targets = new ArrayList<>();
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

     Map<String, Map<String, Object>> componentTypetoParams = new HashMap<>();
     Map<String, Object> innerProps = new HashMap<>();
     innerProps.put(MANDATORY, "NO");
     innerProps.put(HIGH_AVAIL_MODE, "");
     innerProps.put(NFC_NAMING_CODE, "pd_server_code1");
     innerProps.put(VFC_CODE, "pd_server_code");
     innerProps.put(NFC_FUNCTION, "pd_server_description");
     innerProps.put(MIN_INSTANCES, null);
     innerProps.put(MAX_INSTANCES, null);

     componentTypetoParams.put("pd_server", innerProps);

     when(utilMock.getPropertiesfromCompQuestionnaire(vspId,version)).thenReturn
         (componentTypetoParams);

     Map<String,String> map = new HashMap<>();
     Map<String, List<String>> sourceToTargetDependencies = new HashMap<>();

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

     Map<String, Map<String, Object>> componentTypetoParams = new HashMap<>();
     Map<String, Object> innerProps = new HashMap<>();

     innerProps.put(MANDATORY, "");
     innerProps.put(MIN_INSTANCES, 1);
     innerProps.put(MAX_INSTANCES, 5);

     componentTypetoParams.put("pd_server", innerProps);

     when(utilMock.getPropertiesfromCompQuestionnaire(vspId,version)).thenReturn
         (componentTypetoParams);

     Map<String,String> map = new HashMap<>();
     Map<String, List<String>> sourceToTargetDependencies = new HashMap<>();
     List<String> targets = new ArrayList<>();
     targets.add("fe");
     sourceToTargetDependencies.put("pd_server", targets);

     when(utilMock.getSourceToTargetComponent()).thenReturn(map);

     when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

     when(utilMock.getSourceToTargetComponent()).thenReturn(map);
     when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

     Map<String, List<ErrorMessage>> errors =
         toscaEnricher.enrich(toscaServiceModel, vspId, version );

     compareActualAndExpectedModel(toscaServiceModel);

     Assert.assertEquals(errors.size(), 0);
   }

   @Test
   public void testEnrichComponentAddDependencies() throws Exception {
     outputFilesPath = "/mock/enrichComponentAddDependencies/out";
     ToscaServiceModel toscaServiceModel =
         loadToscaServiceModel("/mock/enrichComponentAddDependencies/in",
             "/mock/toscaGlobalServiceTemplates/",
             "MainServiceTemplate.yaml");

     Map<String,String> map = new HashMap<>();
     Map<String, List<String>> sourceToTargetDependencies = new HashMap<>();
     List<String> targets = new ArrayList<>();
     targets.add("ps_server");
     sourceToTargetDependencies.put("pd_server", targets);

     when(utilMock.getSourceToTargetComponent()).thenReturn(map);

     when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);
     when(utilMock.getSourceToTargetComponent()).thenReturn(map);
     when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

     Map<String, List<ErrorMessage>> errors =
         toscaEnricher.enrich(toscaServiceModel, vspId, version );

     compareActualAndExpectedModel(toscaServiceModel);

     Assert.assertEquals(errors.size(), 0);
   }

   @Test
   public void testEnrichComponentNoDependencies() throws Exception {
     outputFilesPath = "/mock/enrichComponentNoDependencies/out";
     ToscaServiceModel toscaServiceModel =
         loadToscaServiceModel("/mock/enrichComponentNoDependencies/in",
             "/mock/toscaGlobalServiceTemplates/",
             "MainServiceTemplate.yaml");

     Map<String,String> map = new HashMap<>();
     Map<String, List<String>> sourceToTargetDependencies = new HashMap<>();

     when(utilMock.getSourceToTargetComponent()).thenReturn(map);

     when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);
     when(utilMock.getSourceToTargetComponent()).thenReturn(map);
     when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

     Map<String, List<ErrorMessage>> errors =
         toscaEnricher.enrich(toscaServiceModel, vspId, version );

     compareActualAndExpectedModel(toscaServiceModel);

     Assert.assertEquals(errors.size(), 0);
   }

   @Test
   public void testEnrichComponentSameVmTypeNfcNamingFunction() throws Exception {
     outputFilesPath = "/mock/enrichNfcNamingFunction/out";
     ToscaServiceModel toscaServiceModel =
             loadToscaServiceModel("/mock/enrichNfcNamingFunction/in",
                     "/mock/toscaGlobalServiceTemplates/",
                     "MainServiceTemplate.yaml");

     Map<String, Map<String, Object>> componentTypetoParams = new HashMap<>();
     Map<String, Object> innerProps = new HashMap<>();
     innerProps.put(MANDATORY, "NO");
     innerProps.put(HIGH_AVAIL_MODE, "");
     innerProps.put(NFC_NAMING_CODE, "cfed_changed_from_ui");
     innerProps.put(VFC_CODE, "pd_server_code");
     innerProps.put(NFC_FUNCTION, "cfed_naming_function");
     innerProps.put(MIN_INSTANCES, null);
     innerProps.put(MAX_INSTANCES, null);

     componentTypetoParams.put("cfed", innerProps);

     when(utilMock.getPropertiesfromCompQuestionnaire(vspId,version)).thenReturn
             (componentTypetoParams);

     Map<String,String> map = new HashMap<>();
     Map<String, List<String>> sourceToTargetDependencies = new HashMap<>();

     when(utilMock.getSourceToTargetComponent()).thenReturn(map);
     when(utilMock.populateDependencies(vspId,version,map)).thenReturn(sourceToTargetDependencies);

     Map<String, List<ErrorMessage>> errors =
             toscaEnricher.enrich(toscaServiceModel, vspId, version );

     compareActualAndExpectedModel(toscaServiceModel);

     Assert.assertEquals(errors.size(), 0);
   }
 }
