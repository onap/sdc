package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

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
  }

  @Test
  public void testTranslateSecurityRuleToPortSharedPortNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityruletosharedportlinking/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityruletosharedportlinking/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testSecurityRuleToPortConnectionMultiConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityrulestoportconnectionmulti/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityrulestoportconnectionmulti/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testSecurityRuleToPortConnectionNestedGetResource() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityrulestoportconnectiongetresource/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securityrulestoportconnection/securityrulestoportconnectiongetresource/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}