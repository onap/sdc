package org.openecomp.sdc.translator.services.heattotosca;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class GlobalTypesGeneratorTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/services/heattotosca/global_types/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/global_types/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }

}