package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Avrahamg
 * @since August 10, 2016
 */
public class ContrailV2VMInterfaceToNetResourceConnectionTest extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateVMIToNetNestedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VMInterfaceToNettworkConnection/nested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/nested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVMIToSharedNetNestedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VMInterfaceToNettworkConnection/shared/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/shared/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVMIToNetMultiConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VMInterfaceToNettworkConnection/multi/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/multi/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}