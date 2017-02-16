package org.openecomp.sdc.translator.impl.heattotosca.nested.single;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class TranslateHeatNestedSingle extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/nested/single/inputs";
    outputFilesPath = "/mock/heat/nested/single/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}
