package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Test;

public class ResourceTranslationCinderVolumeImplTest extends BaseResourceTranslationTest {
  public ResourceTranslationCinderVolumeImplTest() {
    inputFilesPath = "/mock/services/heattotosca/cinder_volume_translation/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/cinder_volume_translation/expectedoutputfiles";
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}