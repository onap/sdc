package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PortToContrailV2VirtualNetworkResourceConnectionTest
    extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testPortToNetNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/porttocontrailv2virtualnetworkconnection/nested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/porttocontrailv2virtualnetworkconnection/nested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testPortToSharedNetNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/porttocontrailv2virtualnetworkconnection/shared/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/porttocontrailv2virtualnetworkconnection/shared/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testPortToNetMultiConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/porttocontrailv2virtualnetworkconnection/multi/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/porttocontrailv2virtualnetworkconnection/multi/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }
}