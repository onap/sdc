package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionMixPatternFullTest extends BaseFullTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testMixPatterns() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/oneAppearancePerPattern/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/oneAppearancePerPattern/out";

    testTranslationWithInit();
  }

  @Test
  public void testMixPatternsWithConnectivityBetweenPatterns() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/connectivityBetweenPatterns/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/connectivityBetweenPatterns/out";

    testTranslationWithInit();
  }

  @Test
  public void testMixPatternsWithConnectivityAndMoreThanOneOccurenceForEachPattern()
      throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/twoAppearancePerPatternWithConnectivities/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/twoAppearancePerPatternWithConnectivities/out";

    testTranslationWithInit();
  }

  @Test
  public void testDuplicateResourceIdsInDiffAddOnFiles() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/duplicateResourceIdsInDiffAddOnFiles/in";

    try {
      testTranslationWithInit();
    }catch(Exception e){
      Assert.assertEquals(e.getMessage(), "Resource with id lb_0_int_oam_int_0_port occures more " +
          "than once in different addOn files");
    }
  }

  @Test
  public void testMixPatternsWithDependencyConnectivity() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/dependencyConnectivity/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/mixPatterns/dependencyConnectivity/out";

    testTranslationWithInit();
  }

}
