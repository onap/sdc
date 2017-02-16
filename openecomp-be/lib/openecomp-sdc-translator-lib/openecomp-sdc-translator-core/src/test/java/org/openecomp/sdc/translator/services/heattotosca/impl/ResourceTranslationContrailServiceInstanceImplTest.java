package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author shiria
 * @since August 07, 2016.
 */
public class ResourceTranslationContrailServiceInstanceImplTest
    extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateOneServiceInstance() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/oneServiceInstance/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/oneServiceInstance/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateDiffServiceTemplate() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/diffServiceTemplate/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/diffServiceTemplate/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateSharedNetworkMulti() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/sharedNetworkMulti/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/sharedNetworkMulti/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateSameServiceTemplate() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/sameServiceTemplate/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/sameServiceTemplate/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


    /*
    //TODO -- need to be tested once the 2 level nested For shared resources bug will be fixed - ATTASDC-1065
    @Test
    public void testTranslateSharedNetworkNested() throws Exception {
        inputFilesPath = "/mock/services/heattotosca/contrail2serviceinstance/sharedNetworkNested/inputfiles";
        outputFilesPath = "/mock/services/heattotosca/contrail2serviceinstance/sharedNetworkNested/expectedoutputfiles";
        initTranslatorAndTranslate();
        testTranslation();
    }
    */
}