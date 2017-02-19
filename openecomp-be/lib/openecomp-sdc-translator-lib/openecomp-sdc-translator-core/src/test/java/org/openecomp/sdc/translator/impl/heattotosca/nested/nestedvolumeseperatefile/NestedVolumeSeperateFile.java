package org.openecomp.sdc.translator.impl.heattotosca.nested.nestedvolumeseperatefile;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class NestedVolumeSeperateFile extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/nested/nestedvolumeseperatefile/inputs";
    outputFilesPath = "/mock/heat/nested/nestedvolumeseperatefile/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}
