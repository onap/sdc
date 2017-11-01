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
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortType/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortType/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoSamePortTypes() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoSamePortTypes2() throws IOException {
    inputFilesPath =
            "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes2/in";
    outputFilesPath =
            "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes2/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoSamePortTypes3() throws IOException {
    inputFilesPath =
            "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes3/in";
    outputFilesPath =
            "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes3/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoSamePortTypes4() throws IOException {
    inputFilesPath =
            "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes4/in";
    outputFilesPath =
            "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwosameporttypes4/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoDifferentPortTypesAndNested() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwodiffporttypesandnested/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computewithtwodiffporttypesandnested/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoDifferentPortAndServerGroup() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeAndServerGroup/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeAndServerGroup/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoPortsDiffTypeAndNodeConnectedIn() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeNodeConnectedIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeNodeConnectedIn/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoPortsSameTypeAndNodeConnectedIn() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithSamePortTypeNodeConnectedIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithSamePortTypeNodeConnectedIn/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoPortsDiffTypeAndNodeConnectedOut() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeNodeConnectedOut/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithDiffPortTypeNodeConnectedOut/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoPortsSameTypeAndNodeConnectedOut() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithSamePortTypeNodeConnectedOut/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/computeWithSamePortTypeNodeConnectedOut/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoDifferentPortTypesAndOutParamGetAttIn() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/diffPortTypeAndOutParamGetAttrIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/diffPortTypeAndOutParamGetAttrIn/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputeWithTwoSamePortTypesAndOutParamGetAttIn() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/samePortTypeAndOutParamGetAttrIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/samePortTypeAndOutParamGetAttrIn/out";

    testTranslationWithInit();
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
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/twoSetsOfSingle/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/twoSetsOfSingle/out";


    testTranslationWithInit();
  }

  @Test
  public void testTwoSetsOfSingleWithGetAttrBetweenThem() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/twoComputesWithGetAttrBetweenThem/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/twoComputesWithGetAttrBetweenThem/out";

    testTranslationWithInit();
  }

  @Test
  public void testOneComputeTwoDiffPortsAndGetAttrIn() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeDiffPortTypesAndGetAttIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeDiffPortTypesAndGetAttIn/out";

    testTranslationWithInit();
  }

  @Test
  public void testOneComputeTwoSimilarPortsAndGetAttrIn() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeSamePortsAndGetAttrIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeSamePortsAndGetAttrIn/out";

    testTranslationWithInit();
  }

  @Test
  public void testOneComputeTwoDiffPortsAndGetAttrOut() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeDiffPortTypesAndGetAttOut/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeDiffPortTypesAndGetAttOut/out";

    testTranslationWithInit();
  }

  @Test
  public void testOneComputeTwoSimilarPortsAndGetAttrOut() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeSamePortTypesAndGetAttOut/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/oneComputeSamePortTypesAndGetAttOut/out";

    testTranslationWithInit();
  }

  @Test
  public void testThreeNovaSameTypeNoConsolidation() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/threeSameComputesNoConsolidation/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/threeSameComputesNoConsolidation/out";

    testTranslationWithInit();
  }

  @Test
  public void testThreeNovaDiffTypeWithPorts() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/threeDiffComputesWithPorts/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/threeDiffComputesWithPorts/out";

    testTranslationWithInit();
  }

  @Test
  public void testThreeNovaDiffTypeWithAllConnectivities() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/threeDiffComputesWithAllConnectivities/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/threeDiffComputesWithAllConnectivities/out";

    testTranslationWithInit();
  }

  @Test
  public void testThreeNovaSameTypeWithGetAttrOutFromPort() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/threeNovaSameTypeWithGetAttrFromPort/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/threeNovaSameTypeWithGetAttrFromPort/out";

    testTranslationWithInit();
  }

  @Test
  public void testInputOutputParameterTypes() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/inputOutputParamType/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/singleSubstitution/inputOutputParamType/out";

    testTranslationWithInit();
  }
}
