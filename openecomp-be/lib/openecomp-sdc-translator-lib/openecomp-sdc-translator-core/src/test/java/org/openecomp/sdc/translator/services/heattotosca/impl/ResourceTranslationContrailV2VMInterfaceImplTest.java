package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Avrahamg
 * @since August 10, 2016
 */
public class ResourceTranslationContrailV2VMInterfaceImplTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateVMIWithGetResource() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/contrailv2VMinterface/oneNet/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/contrailv2VMinterface/oneNet/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVMIWithListOfNetworks() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/contrailv2VMinterface/listNet/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/listNet/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }
}