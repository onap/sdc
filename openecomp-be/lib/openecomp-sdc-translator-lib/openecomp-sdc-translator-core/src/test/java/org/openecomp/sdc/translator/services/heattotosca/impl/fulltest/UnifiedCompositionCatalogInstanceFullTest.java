package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionCatalogInstanceFullTest extends BaseFullTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testThreeNovaSameTypeDiffGetAttrFromSameEntitiesTypes() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypeGetAttrBetweenThem/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypeGetAttrBetweenThem/out";

    testTranslationWithUnifiedCondition();
  }

  @Test
  public void testThreeNovaSameTypePortsConnectedToDiffNetworks() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypePortsConnectedToDiffNetworks/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypePortsConnectedToDiffNetworks/out";

    testTranslationWithUnifiedCondition();
  }

  @Test
  public void testThreeNovaSameTypeDiffImageName() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypeDiffImageName/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypeDiffImageName/out";

    testTranslationWithUnifiedCondition();
  }



  private void testTranslationWithUnifiedCondition() throws IOException {
      initTranslatorAndTranslate();
      testTranslation();
  }
}
