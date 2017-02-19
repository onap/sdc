package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Test;

public class ResourceTranslationNeutronSecurityGroupImplTest extends BaseResourceTranslationTest {

  {
    inputFilesPath = "/mock/services/heattotosca/neutron_security_group_translation/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/neutron_security_group_translation/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }


}