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

package org.openecomp.sdc.translator.impl.heattotosca.resources;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataValidationType;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseResourceTranslationTest;

import java.io.IOException;

import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_PORT_POSITIVE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_VOLUME_POSITIVE;

public class OSNovaServerTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
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
}
