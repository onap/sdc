package org.openecomp.sdc.translator.impl.heattotosca.nested.separatevol;

import org.openecomp.sdc.translator.services.heattotosca.impl.BaseResourceTranslationTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class NestedAndSeparateVolTranslationTest extends BaseResourceTranslationTest {

  @Before
  public void setUp() throws IOException {
    inputFilesPath = "/mock/services/heattotosca/hot-nimbus-oam-volumes_v1.0/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/hot-nimbus-oam-volumes_v1.0/out";
    super.setUp();
  }

  @Test
  public void testTranslate() throws Exception {
    testTranslation();
  }
}