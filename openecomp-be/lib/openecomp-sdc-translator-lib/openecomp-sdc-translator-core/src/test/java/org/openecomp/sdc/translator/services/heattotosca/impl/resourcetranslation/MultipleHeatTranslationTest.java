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


public class MultipleHeatTranslationTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateBaseHeats() throws Exception {
    inputFilesPath = "/mock/multiHeat/allHeatsAreBase/inputs";
    outputFilesPath = "/mock/multiHeat/allHeatsAreBase/expectedOutput/";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateOneOutOfFourFilesIsNotBase() throws Exception {
    inputFilesPath = "/mock/multiHeat/referencedHeatResources/inputs";
    outputFilesPath = "/mock/multiHeat/referencedHeatResources/expectedOutput/";
    initTranslatorAndTranslate();
    testTranslation();
  }
}
