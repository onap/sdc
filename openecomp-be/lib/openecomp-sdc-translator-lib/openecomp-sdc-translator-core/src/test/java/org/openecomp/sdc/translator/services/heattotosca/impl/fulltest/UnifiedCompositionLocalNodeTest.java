package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;


import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionLocalNodeTest extends BaseFullTranslationTest {

  private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/localNode/";

  @Test
  public void testLocalNodeWithFabricConfigurationCapability() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "novaServerWithFabricConfigurationCapability");
  }

 
}

