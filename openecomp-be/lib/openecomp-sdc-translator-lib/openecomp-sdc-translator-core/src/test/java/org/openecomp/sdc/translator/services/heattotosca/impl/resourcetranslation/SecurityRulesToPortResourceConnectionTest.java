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
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants;

import java.io.IOException;

import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataTestUtil.validateNestedNodesConnectedInSecurityRuleToPort;


public class SecurityRulesToPortResourceConnectionTest extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateSecurityRuleToPortNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityrulestoportnestedconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityrulestoportnestedconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNestedNodesConnectedInSecurityRuleToPort(TestConstants
        .TEST_SECURITY_RULE_PORT_NESTED_CONNECTION, translationContext);
  }

  @Test
  public void testTranslateSecurityRuleToPortSharedPortNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityruletosharedportlinking/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityruletosharedportlinking/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNestedNodesConnectedInSecurityRuleToPort(TestConstants
        .TEST_SECURITY_RULE_PORT_NESTED_SHARED_PORT, translationContext);
  }

  @Test
  public void testSecurityRuleToPortConnectionSharedAddOnConnection() throws Exception {
    inputFilesPath =
            "/mock/services/heattotosca/securityrulestoportconnection/securityRulesToPortSharedAddOn/inputfiles";
    outputFilesPath =
            "/mock/services/heattotosca/securityrulestoportconnection/securityRulesToPortSharedAddOn/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testSecurityRuleToPortConnectionNestedGetResource() throws Exception {
    inputFilesPath =
            "/mock/services/heattotosca/securityrulestoportconnection/securityRulesToPortGetResource/inputfiles";
    outputFilesPath =
            "/mock/services/heattotosca/securityrulestoportconnection/securityRulesToPortGetResource/out";
    initTranslatorAndTranslate();
    testTranslation();
  }


  @Test
  public void testSecurityRuleToPortConnectionNestedMultiLevelsGetResource() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/nestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/nestedMultiLevels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNestedNodesConnectedInSecurityRuleToPort(TestConstants
        .TEST_SECURITY_RULE_PORT_MULTI_LEVEL_NESTED_CONNECTION, translationContext);
  }

  @Test
  public void testSecurityRuleToPortConnectionSharedNestedMultiLevelsGetResource()
      throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/sharedNestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/sharedNestedMultiLevels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNestedNodesConnectedInSecurityRuleToPort(TestConstants
        .TEST_SECURITY_RULE_PORT_MULTI_LEVEL_NESTED_SHARED_PORT, translationContext);
  }

}
