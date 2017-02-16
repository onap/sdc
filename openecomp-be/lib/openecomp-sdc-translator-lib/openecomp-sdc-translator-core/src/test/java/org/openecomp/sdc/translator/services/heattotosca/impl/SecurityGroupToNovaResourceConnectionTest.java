package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SecurityGroupToNovaResourceConnectionTest extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testSecurityGroupToPortConnectionMultiConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/securitygrouptonovaconnectionmulti/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/securitygrouptonovaconnectionmulti/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}