package org.openecomp.sdc.translator.impl.heattotosca.nested.recursive;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TranslateHeatNestedRecursiveTest extends BaseResourceTranslationTest {


  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateRecursive() throws Exception {
    inputFilesPath = "/mock/heat/nested/recursive/inputs";
    outputFilesPath = "/mock/heat/nested/recursive/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateExposedReq2Level() throws Exception {
    inputFilesPath = "/mock/heat/nested/nested2levels/inputs";
    outputFilesPath = "/mock/heat/nested/nested2levels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateExposedReq3Level() throws Exception {
    inputFilesPath = "/mock/heat/nested/nested3levels/inputs";
    outputFilesPath = "/mock/heat/nested/nested3levels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


}
