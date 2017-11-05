package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionSingleSubstitutionFullTest extends BaseFullTranslationTest {

  private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/singleSubstitution/";

  @Test
  public void testComputeWithTwoDifferentPortTypes() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computeWithDiffPortType");
  }

  @Test
  public void testComputeWithTwoSamePortTypes() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computewithtwosameporttypes");
  }

  @Test
  public void testComputeWithTwoSamePortTypes2() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computewithtwosameporttypes2");
  }

  @Test
  public void testComputeWithTwoSamePortTypes3() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computewithtwosameporttypes3");
  }

  @Test
  public void testComputeWithTwoSamePortTypes4() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computewithtwosameporttypes4");
  }

  @Test
  public void testComputeWithTwoDifferentPortTypesAndNested() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computewithtwodiffporttypesandnested");
  }

  @Test
  public void testComputeWithTwoDifferentPortAndServerGroup() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computeWithDiffPortTypeAndServerGroup");
  }

  @Test
  public void testComputeWithTwoPortsDiffTypeAndNodeConnectedIn() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computeWithDiffPortTypeNodeConnectedIn");
  }

  @Test
  public void testComputeWithTwoPortsSameTypeAndNodeConnectedIn() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computeWithSamePortTypeNodeConnectedIn");
  }

  @Test
  public void testComputeWithTwoPortsDiffTypeAndNodeConnectedOut() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computeWithDiffPortTypeNodeConnectedOut");
  }

  @Test
  public void testComputeWithTwoPortsSameTypeAndNodeConnectedOut() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "computeWithSamePortTypeNodeConnectedOut");
  }

  @Test
  public void testComputeWithTwoDifferentPortTypesAndOutParamGetAttIn() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "diffPortTypeAndOutParamGetAttrIn");
  }

  @Test
  public void testComputeWithTwoSamePortTypesAndOutParamGetAttIn() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "samePortTypeAndOutParamGetAttrIn");
  }

//  @Test
//  public void testGeneralVf() throws IOException {
//    inputFilesPath =
//        "/mock/services/heattotosca/fulltest/singleSubstitution/generalVf/in";
//    outputFilesPath =
//        "/mock/services/heattotosca/fulltest/singleSubstitution/generalVf/out";
//
//    testTranslationWithInit();
//  }

  @Test
  public void testTwoSetsOfSingle() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "twoSetsOfSingle");
  }

  @Test
  public void testTwoSetsOfSingleWithGetAttrBetweenThem() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "twoComputesWithGetAttrBetweenThem");
  }

  @Test
  public void testOneComputeTwoDiffPortsAndGetAttrIn() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeDiffPortTypesAndGetAttIn");
  }

  @Test
  public void testOneComputeTwoSimilarPortsAndGetAttrIn() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeSamePortsAndGetAttrIn");
  }

  @Test
  public void testOneComputeTwoDiffPortsAndGetAttrOut() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeDiffPortTypesAndGetAttOut");
  }

  @Test
  public void testOneComputeTwoSimilarPortsAndGetAttrOut() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeSamePortTypesAndGetAttOut");
  }

  @Test
  public void testThreeNovaSameTypeNoConsolidation() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "threeSameComputesNoConsolidation");
  }

  @Test
  public void testThreeNovaDiffTypeWithPorts() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "threeDiffComputesWithPorts");
  }

  @Test
  public void testThreeNovaDiffTypeWithAllConnectivities() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "threeDiffComputesWithAllConnectivities");
  }

  @Test
  public void testThreeNovaSameTypeWithGetAttrOutFromPort() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "threeNovaSameTypeWithGetAttrFromPort");
  }

  @Test
  public void testInputOutputParameterTypes() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "inputOutputParamType");
  }
}
