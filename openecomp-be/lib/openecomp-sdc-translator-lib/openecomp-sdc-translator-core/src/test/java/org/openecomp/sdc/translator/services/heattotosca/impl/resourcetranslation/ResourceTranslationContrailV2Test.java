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
