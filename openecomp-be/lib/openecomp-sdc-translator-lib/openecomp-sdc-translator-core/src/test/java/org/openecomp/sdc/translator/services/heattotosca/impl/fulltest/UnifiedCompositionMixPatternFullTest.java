package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionMixPatternFullTest extends BaseFullTranslationTest {

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testMixPatterns() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/mixPatterns/oneAppearancePerPattern");
  }

  @Test
  public void testMixPatternsWithConnectivityBetweenPatterns() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/mixPatterns/connectivityBetweenPatterns");
  }

  @Test
  public void testMixPatternsWithConnectivityAndMoreThanOneOccurenceForEachPattern()
      throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/mixPatterns/twoAppearancePerPatternWithConnectivities");
  }

  @Test
  public void testDuplicateResourceIdsInDiffAddOnFiles() throws IOException {
    exception.expect(CoreException.class);
    exception.expectMessage("Resource with id lb_0_int_oam_int_0_port occurs more " +
        "than once in different addOn files");

    testTranslationWithInit("/mock/services/heattotosca/fulltest/mixPatterns/duplicateResourceIdsInDiffAddOnFiles");
  }

  @Test
  public void testMixPatternsWithDependencyConnectivity() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/mixPatterns/dependencyConnectivity");
  }

}
