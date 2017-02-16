package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PortToNetResourceConnectionTest extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslatePortToNetNestedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/porttonetconnection/nested/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/porttonetconnection/nested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslatePortToSharedNetNestedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/porttonetconnection/shared/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/porttonetconnection/shared/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslatePortToNetMultiConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/porttonetconnection/multi/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/porttonetconnection/multi/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }
}