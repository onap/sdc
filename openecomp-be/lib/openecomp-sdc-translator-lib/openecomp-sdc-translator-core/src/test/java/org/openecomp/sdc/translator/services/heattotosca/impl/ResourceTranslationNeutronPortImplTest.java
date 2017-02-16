package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Test;

public class ResourceTranslationNeutronPortImplTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/services/heattotosca/neutron_port_translation/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/neutron_port_translation/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }

}