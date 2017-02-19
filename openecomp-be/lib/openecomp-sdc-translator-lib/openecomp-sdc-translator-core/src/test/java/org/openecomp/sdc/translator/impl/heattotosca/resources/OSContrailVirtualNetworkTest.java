package org.openecomp.sdc.translator.impl.heattotosca.resources;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;


public class OSContrailVirtualNetworkTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/resources/OS_Contrail_VirtualNetwork/inputs";
    outputFilesPath = "/mock/heat/resources/OS_Contrail_VirtualNetwork/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}
