package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class MultipleHeatTranslationTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateBaseHeats() throws Exception {
    inputFilesPath = "/mock/multiHeat/allHeatsAreBase/inputs";
    outputFilesPath = "/mock/multiHeat/allHeatsAreBase/expectedOutput/";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateOneOutOfFourFilesIsNotBase() throws Exception {
    inputFilesPath = "/mock/multiHeat/referencedHeatResources/inputs";
    outputFilesPath = "/mock/multiHeat/referencedHeatResources/expectedOutput/";
    initTranslatorAndTranslate();
    testTranslation();
  }
}
