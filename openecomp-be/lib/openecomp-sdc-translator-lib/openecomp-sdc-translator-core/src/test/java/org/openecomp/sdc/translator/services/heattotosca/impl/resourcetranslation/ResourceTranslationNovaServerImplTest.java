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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataValidationType;
import org.togglz.testing.TestFeatureManagerProvider;

import java.io.IOException;

import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_PORT_POSITIVE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_VOLUME_POSITIVE;

public class ResourceTranslationNovaServerImplTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }
  
  @BeforeClass
  public static void setUpClass() {
      manager.enable(ToggleableFeature.DEFAULT_FEATURE);
      TestFeatureManagerProvider.setFeatureManager(manager);
  }


  @Test
  public void testTranslate() throws Exception {
    inputFilesPath = "/mock/heat/resources/OS_Nova_Server/inputs";
    outputFilesPath = "/mock/heat/resources/OS_Nova_Server/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_VOLUME,
        TEST_VOLUME_POSITIVE);
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_PORT,
        TEST_PORT_POSITIVE);
  }

  @Test
  public void testTranslateWithOnlyPorts() throws Exception {
    inputFilesPath = "/mock/heat/resources/Port/inputfiles";
    outputFilesPath = "/mock/heat/resources/Port/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_PORT,
        TEST_PORT_POSITIVE);
  }
  
  @Test
  public void testFabricConfigurationOnlyOnePortTrueAttrFlag() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/one_port_true/attr_flag/input";
    outputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/one_port_true/attr_flag/output";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testFabricConfigurationOnlyOnePortTrueBindingProfile() throws IOException {
      inputFilesPath =
              "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/one_port_true/binding_profile/input";
      outputFilesPath =
              "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/one_port_true/binding_profile/output";
      initTranslatorAndTranslate();
      testTranslation();
  }
  
  @Test
  public void testFabricConfigurationAllFalseAttrFlag() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/all_false/attr_flag/input";
    outputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/all_false/attr_flag/output";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
   public void testFabricConfigurationAllFalseBindingProfile() throws IOException {
       inputFilesPath =
               "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/all_false/binding_profile/input";
       outputFilesPath =
               "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/all_false/binding_profile/output";
       initTranslatorAndTranslate();
       testTranslation();
   }
  
  @Test
  public void testFabricConfigurationPropertyNullAttrFlag() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/property_null/attr_flag/input";
    outputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/property_null/attr_flag/output";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
   public void testFabricConfigurationPropertyNullBindingProfile() throws IOException {
     inputFilesPath =
             "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/property_null/binding_profile/input";
     outputFilesPath =
             "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/property_null/binding_profile/output";
     initTranslatorAndTranslate();
     testTranslation();
   }
  
  @Test
  public void testFabricConfigurationWithoutPropertyAttrFlag() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/without_property/attr_flag/input";
    outputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/without_property/attr_flag/output";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testFabricConfigurationWithoutPropertyBindingProfile() throws IOException {
      inputFilesPath =
              "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/without_property/binding_profile/input";
      outputFilesPath =
              "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/without_property/binding_profile/output";
      initTranslatorAndTranslate();
      testTranslation();
  }

  @Test
  public void testFabricConfiguration2PortsAttrFlag() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/2ports/attr_flag/input";
    outputFilesPath =
        "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/2ports/attr_flag/output";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testFabricConfiguration2PortsWithBindingProfile() throws IOException {
    inputFilesPath =
            "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/2ports/binding_profile/input";
    outputFilesPath =
            "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/2ports/binding_profile/output";
    initTranslatorAndTranslate();
    testTranslation();
  }
  
  
  @AfterClass
  public static void tearDownClass() {
      manager.disable(ToggleableFeature.DEFAULT_FEATURE);
      manager = null;
      TestFeatureManagerProvider.setFeatureManager(null);
  }

  @Test
  public void testFabricConfigurationMixedBothPropertiesTrue() throws IOException {
      inputFilesPath =
              "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_both_properties_true/input";
      outputFilesPath =
              "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_both_properties_true/output";
      initTranslatorAndTranslate();
      testTranslation();
  }

  @Test
  public void testFabricConfigurationMixedOnePropertyTrue() throws IOException {
      inputFilesPath =
              "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_one_property_true/input";
      outputFilesPath =
              "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_one_property_true/output";
      initTranslatorAndTranslate();
      testTranslation();
  }

    @Test
    public void testFabricConfigurationMixedBothPropertiesFalse() throws IOException {
        inputFilesPath =
                "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_both_properties_false/input";
        outputFilesPath =
                "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_both_properties_false/output";
        initTranslatorAndTranslate();
        testTranslation();
    }

    @Test
    public void testFabricConfigurationMixedBindingProfileIsNotMap() throws IOException {
        inputFilesPath =
                "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_binding_profile_is_not_map/input";
        outputFilesPath =
                "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_binding_profile_is_not_map/output";
        initTranslatorAndTranslate();
        testTranslation();
    }

    @Test
    public void testFabricConfigurationMixedBindingProfileIsNull() throws IOException {
        inputFilesPath =
                "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_binding_profile_is_null/input";
        outputFilesPath =
                "/mock/services/heattotosca/novaservertranslation/fabricConfiguration/mixed_binding_profile_is_null/output";
        initTranslatorAndTranslate();
        testTranslation();
    }

}
