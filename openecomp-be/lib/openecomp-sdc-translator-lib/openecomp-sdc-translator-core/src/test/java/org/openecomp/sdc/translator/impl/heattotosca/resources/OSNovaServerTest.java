package org.openecomp.sdc.translator.impl.heattotosca.resources;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class OSNovaServerTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/resources/OS_Nova_Server/inputs";
    outputFilesPath = "/mock/heat/resources/OS_Nova_Server/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}
