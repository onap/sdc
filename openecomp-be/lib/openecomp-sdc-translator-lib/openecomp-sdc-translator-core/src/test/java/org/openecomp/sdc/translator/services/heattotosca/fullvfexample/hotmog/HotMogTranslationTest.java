package org.openecomp.sdc.translator.services.heattotosca.fullvfexample.hotmog;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class HotMogTranslationTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/services/heattotosca/hot-mog-0108-bs1271/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/hot-mog-0108-bs1271/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}