package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Test;

public class ResourceTranslationNovaServerGroupsImplTest extends BaseResourceTranslationTest {
  {
    inputFilesPath = "/mock/services/heattotosca/novaservergroups/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/novaservergroups/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }

}