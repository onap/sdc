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

 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import org.junit.Before;
 import org.junit.Test;
 import org.openecomp.sdc.datatypes.error.ErrorMessage;
 import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

 public class PortMirroringEnricherTest extends BaseToscaEnrichmentTest {

   private PortMirroringEnricher portMirroringEnricher;

   @Before
   public void init() {
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
