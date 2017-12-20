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
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataValidationType;

import java.io.IOException;

import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_CONNECTIVITY_POSITIVE;


public class ResourceTranslationNeutronPortImplTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslate() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/neutron_port_translation/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/neutron_port_translation/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validatePortTemplateConsolidationData(ConsolidationDataValidationType
        .VALIDATE_CONNECTIVITY, TEST_CONNECTIVITY_POSITIVE);
  }

  @Test
  public void testTranslateSecure() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/neutron_security_group_translation/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/neutron_security_group_translation/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validatePortTemplateConsolidationData(ConsolidationDataValidationType
        .VALIDATE_CONNECTIVITY, TEST_CONNECTIVITY_POSITIVE);
  }
}
