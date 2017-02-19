package org.openecomp.sdc.translator.services.heattotosca.fullvfexample.vmmesmall;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class VmmeSmallTranslationTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/services/heattotosca/vmme_small/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/vmme_small/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}