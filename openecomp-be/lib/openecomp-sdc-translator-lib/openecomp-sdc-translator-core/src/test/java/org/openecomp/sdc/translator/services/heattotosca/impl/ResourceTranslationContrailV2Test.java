package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ResourceTranslationContrailV2Test extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslate() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/ContrailV2_translation/simple/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/ContrailV2_translation/simple/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testMultiPolicySingleNetTranslate() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/ContrailV2_translation/ContrailV2_MultiPolicy_single_net_translation/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/ContrailV2_translation/ContrailV2_MultiPolicy_single_net_translation/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testMultiNetSinglePolicyTranslate() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/ContrailV2_translation/ContrailV2_Multi_net_single_policy_translation/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/ContrailV2_translation/ContrailV2_Multi_net_single_policy_translation/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testInvalidPolicyResourceTypeTranslate() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/ContrailV2_translation/invalid_policy_resource_type/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/ContrailV2_translation/invalid_policy_resource_type/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


}