package org.openecomp.sdc.translator.impl.heattotosca.resources;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Test;

public class OSNeutronNetTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/heat/resources/OS_Neutron_Net/inputs";
    outputFilesPath = "/mock/heat/resources/OS_Neutron_Net/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}
