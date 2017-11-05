package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionCatalogInstanceFullTest extends BaseFullTranslationTest {

    private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/catalogInstances/";

    @Test
  public void testThreeNovaSameTypeDiffGetAttrFromSameEntitiesTypes() throws IOException {
      testTranslationWithInit(BASE_DIRECTORY + "threeComputesSameTypeGetAttrBetweenThem");
  }

  @Test
  public void testThreeNovaSameTypePortsConnectedToDiffNetworks() throws IOException {
      testTranslationWithInit(BASE_DIRECTORY + "threeComputesSameTypePortsConnectedToDiffNetworks");
  }

  @Test
  public void testComputeWithTwoSamePortTypesWithDiffPropVal() throws IOException {
      testTranslationWithInit(BASE_DIRECTORY + "computewithtwosameporttypes");
  }

  @Test
  public void testThreeNovaSameTypeDiffImageName() throws IOException {
      testTranslationWithInit(BASE_DIRECTORY + "threeComputesSameTypeDiffImageName");
  }

  @Test
  public void testTwoNovaWithDiffProperties() throws IOException {
      testTranslationWithInit(BASE_DIRECTORY + "twoNovaWithDiffProperties");
  }
}
