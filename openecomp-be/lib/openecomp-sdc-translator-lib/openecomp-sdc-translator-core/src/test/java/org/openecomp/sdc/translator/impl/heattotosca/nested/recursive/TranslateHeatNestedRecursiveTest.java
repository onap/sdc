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

package org.openecomp.sdc.translator.impl.heattotosca.nested.recursive;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseResourceTranslationTest;

import java.io.IOException;

public class TranslateHeatNestedRecursiveTest extends BaseResourceTranslationTest {


  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateRecursive() throws Exception {
    inputFilesPath = "/mock/heat/nested/recursive/inputs";
    outputFilesPath = "/mock/heat/nested/recursive/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateExposedReq2Level() throws Exception {
    inputFilesPath = "/mock/heat/nested/nested2levels/inputs";
    outputFilesPath = "/mock/heat/nested/nested2levels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNestedTemplateConsolidationData(TestConstants
        .TEST_MULTIPLE_MULTI_LEVEL_NESTED_RESOURCE);
  }

  @Test
  public void testTranslateExposedReq3Level() throws Exception {
    inputFilesPath = "/mock/heat/nested/nested3levels/inputs";
    outputFilesPath = "/mock/heat/nested/nested3levels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


}
