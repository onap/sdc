package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Test;

public class DependsOnResourceBaseTranslationlTest extends BaseResourceTranslationTest {
  public DependsOnResourceBaseTranslationlTest() {
    inputFilesPath = "/mock/services/heattotosca/baseResourceTranslation/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/baseResourceTranslation/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}