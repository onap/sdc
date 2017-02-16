package org.openecomp.sdc.translator.impl.heattotosca.resources;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;


public class ContrailNetworkRuleTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/resources/OS_Contrail_Network_Rule/inputs";
    outputFilesPath = "/mock/heat/resources/OS_Contrail_Network_Rule/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}
