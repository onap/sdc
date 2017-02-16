package org.openecomp.sdc.translator.impl.heattotosca.nested.nestedvolumelocal;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class NestedVolumelocal extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/nested/nestedvolumelocal/inputs";
    outputFilesPath = "/mock/heat/nested/nestedvolumelocal/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}
