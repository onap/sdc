package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionMixPatternFullTest extends BaseFullTranslationTest {

  private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/mixPatterns/";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testMixPatterns() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "oneAppearancePerPattern");
  }

  @Test
  public void testMixPatternsWithConnectivityBetweenPatterns() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "connectivityBetweenPatterns");
  }

  @Test
  public void testMixPatternsWithConnectivityAndMoreThanOneOccurenceForEachPattern()
      throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "twoAppearancePerPatternWithConnectivities");
  }

  @Test
  public void testDuplicateResourceIdsInDiffAddOnFiles() throws IOException {
    exception.expect(CoreException.class);
    exception.expectMessage("Resource with id lb_0_int_oam_int_0_port occurs more " +
        "than once in different addOn files");

    testTranslationWithInit(BASE_DIRECTORY + "duplicateResourceIdsInDiffAddOnFiles");
  }

  @Test
  public void testMixPatternsWithDependencyConnectivity() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "dependencyConnectivity");
  }

}
