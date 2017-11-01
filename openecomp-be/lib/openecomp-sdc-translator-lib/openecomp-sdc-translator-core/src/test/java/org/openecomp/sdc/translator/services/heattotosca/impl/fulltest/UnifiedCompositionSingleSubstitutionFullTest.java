package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionSingleSubstitutionFullTest extends BaseFullTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testComputeWithTwoDifferentPortTypes() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortType");
  }

  @Test
  public void testComputeWithTwoSamePortTypes() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes");
  }

  @Test
  public void testComputeWithTwoSamePortTypes2() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes2");
  }

  @Test
  public void testComputeWithTwoSamePortTypes3() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes3");
  }

  @Test
  public void testComputeWithTwoSamePortTypes4() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes4");
  }

  @Test
  public void testComputeWithTwoDifferentPortTypesAndNested() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwodiffporttypesandnested");
  }

  @Test
  public void testComputeWithTwoDifferentPortAndServerGroup() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeAndServerGroup");
  }

  @Test
  public void testComputeWithTwoPortsDiffTypeAndNodeConnectedIn() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeNodeConnectedIn");
  }

  @Test
  public void testComputeWithTwoPortsSameTypeAndNodeConnectedIn() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computeWithSamePortTypeNodeConnectedIn");
  }

  @Test
  public void testComputeWithTwoPortsDiffTypeAndNodeConnectedOut() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeNodeConnectedOut");
  }

  @Test
  public void testComputeWithTwoPortsSameTypeAndNodeConnectedOut() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/computeWithSamePortTypeNodeConnectedOut");
  }

  @Test
  public void testComputeWithTwoDifferentPortTypesAndOutParamGetAttIn() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/diffPortTypeAndOutParamGetAttrIn");
  }

  @Test
  public void testComputeWithTwoSamePortTypesAndOutParamGetAttIn() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/samePortTypeAndOutParamGetAttrIn");
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
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/twoSetsOfSingle");
  }

  @Test
  public void testTwoSetsOfSingleWithGetAttrBetweenThem() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/twoComputesWithGetAttrBetweenThem");
  }

  @Test
  public void testOneComputeTwoDiffPortsAndGetAttrIn() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeDiffPortTypesAndGetAttIn");
  }

  @Test
  public void testOneComputeTwoSimilarPortsAndGetAttrIn() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeSamePortsAndGetAttrIn");
  }

  @Test
  public void testOneComputeTwoDiffPortsAndGetAttrOut() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeDiffPortTypesAndGetAttOut");
  }

  @Test
  public void testOneComputeTwoSimilarPortsAndGetAttrOut() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeSamePortTypesAndGetAttOut");
  }

  @Test
  public void testThreeNovaSameTypeNoConsolidation() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/threeSameComputesNoConsolidation");
  }

  @Test
  public void testThreeNovaDiffTypeWithPorts() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/threeDiffComputesWithPorts");
  }

  @Test
  public void testThreeNovaDiffTypeWithAllConnectivities() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/threeDiffComputesWithAllConnectivities");
  }

  @Test
  public void testThreeNovaSameTypeWithGetAttrOutFromPort() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/threeNovaSameTypeWithGetAttrFromPort");
  }

  @Test
  public void testInputOutputParameterTypes() throws IOException {
    testTranslationWithInit("/mock/services/heattotosca/fulltest/singleSubstitution/inputOutputParamType");
  }
}
