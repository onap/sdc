package org.openecomp.sdc.translator.impl.heattotosca.sharedresource;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class HeatSharedResourceTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/sharedresources/inputs";
    outputFilesPath = "/mock/heat/sharedresources/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }


}
