package org.openecomp.sdc.translator.impl.heattotosca.nested.reusenestedfrommultibase;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class Translate_Heat_Nested_From_Multi_Base extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/nested/reusenestedfrommultibase/inputs";
    outputFilesPath = "/mock/heat/nested/reusenestedfrommultibase/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }

}
