package org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseResourceTranslationTest;

import java.io.IOException;

public class BuildConsolidationDataTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testNovaServerGroupConsolidationData() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/novaservergroups/staticPolicy/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/novaservergroups/staticPolicy/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }
}
