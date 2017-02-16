package org.openecomp.sdc.translator.impl.heattotosca.nested.multi;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class Translate_Heat_Nested_Multi extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/nested/multi/inputs";
    outputFilesPath = "/mock/heat/nested/multi/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}
