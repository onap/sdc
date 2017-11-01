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
      testTranslationWithInit("/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypeGetAttrBetweenThem");
  }

  @Test
  public void testThreeNovaSameTypePortsConnectedToDiffNetworks() throws IOException {
      testTranslationWithInit("/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypePortsConnectedToDiffNetworks");
  }

  @Test
  public void testComputeWithTwoSamePortTypesWithDiffPropVal() throws IOException {
      testTranslationWithInit("/mock/services/heattotosca/fulltest/catalogInstances/computewithtwosameporttypes");
  }

  @Test
  public void testThreeNovaSameTypeDiffImageName() throws IOException {
      testTranslationWithInit("/mock/services/heattotosca/fulltest/catalogInstances/threeComputesSameTypeDiffImageName");
  }

  @Test
  public void testTwoNovaWithDiffProperties() throws IOException {
      testTranslationWithInit("/mock/services/heattotosca/fulltest/catalogInstances/twoNovaWithDiffProperties");
  }

  /*private void testTranslationWithUnifiedCondition(String path) throws IOException {
      basePath = path;
      initTranslatorAndTranslate();
      testTranslation();
  }*/
}
