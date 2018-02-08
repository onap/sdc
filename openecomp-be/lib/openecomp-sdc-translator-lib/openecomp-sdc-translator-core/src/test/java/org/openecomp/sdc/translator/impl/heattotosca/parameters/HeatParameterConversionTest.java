/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.translator.impl.heattotosca.parameters;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseResourceTranslationTest;

import java.io.IOException;

public class HeatParameterConversionTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslate() throws Exception {
    inputFilesPath = "/mock/heat/parameters/single/inputs";
    outputFilesPath = "/mock/heat/parameters/single/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateAllBaseMultipleHeat() throws Exception {
    inputFilesPath = "/mock/heat/parameters/allHeatsAreBase/inputs";
    outputFilesPath = "/mock/heat/parameters/allHeatsAreBase/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateMultipleHeatWithNested() throws Exception {
    inputFilesPath = "/mock/heat/parameters/multipleHeatWithNested/inputs";
    outputFilesPath = "/mock/heat/parameters/multipleHeatWithNested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateNestedWithAssociatedHeat() throws Exception {
    inputFilesPath = "/mock/heat/parameters/nestedWithAssociatedHeat/inputs";
    outputFilesPath = "/mock/heat/parameters/nestedWithAssociatedHeat/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}
