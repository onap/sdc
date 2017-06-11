package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionNestedSingleComputeFullTest extends BaseFullTranslationTest {

    @Override
    @Before
    public void setUp() throws IOException {
        // do not delete this function. it prevents the superclass setup from running
    }

    @Test
    public void testNestedWithOneCompute() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedWithOneCompute/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedWithOneCompute/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedWithOneComputeSamePortType() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedWithOneComputeDiffPortType/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedWithOneComputeDiffPortType/out";

        testTranslationWithInit();
    }

    @Test
    public void testOneNestedWithTwoComputesOfSameType() throws IOException {
        //Not pattern 4 (Complex VFC)
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedWithTwoComputesOfSameType/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedWithTwoComputesOfSameType/out";

        testTranslationWithInit();
    }

    @Test
    public void testOneNestedWithTwoDiffComputeTypes() throws IOException {
        //Not pattern 4 (Complex VFC)
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedWithTwoDiffComputeTypes/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedWithTwoDiffComputeTypes/out";

        testTranslationWithInit();
    }

    @Test
    public void testTwoNestedNodeTemplatesOfSameType() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/twoNestedNodeTemplatesWithSameComputeType/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/twoNestedNodeTemplatesWithSameComputeType/out";

        testTranslationWithInit();
    }

    @Test
    public void testTwoDiffNestedFilesWithSameComputeType() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/diffNestedFilesWithSameComputeType/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/diffNestedFilesWithSameComputeType/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedCompositionNodesConnectedIn() throws IOException {
       /*
        Nested Composition + node connected In
         a. Dependency between a nested compute and a non-nested compute resource
         b. Dependency between a nested compute and another nested resource (same type)
         c. Dependency between a nested compute and another nested resource (different type)
         d. Dependency between a non-consolidation entity resource and a nested compute resource
         e. Dependency between a non-consolidation entity resource and a non-nested compute resource
         f. Security Rule to Port nested connection
         g. Security Rule to Port nested shared connection
         */
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedNodesConnectedIn/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedNodesConnectedIn/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedCompositionNodesGetAttrIn() throws IOException {
       /*
        Nested composition with Get attribute in -
          a. Get attribute in a non-nested compute from a nested compute resource
          b. Get attribute in a non-nested compute from another consolidation entity resource
          c. Get attribute in a nested compute from another nested compute resource of same type
          d. Get attribute in a nested compute from another nested compute resource of different type
          e. Get attribute in a nested compute from a regular consolidation entity resource
          f. Get attribute in a non-consolidation entity resource from a nested compute resource
          g. Get attribute in a non-consolidation entity resource from a non-nested compute resource
         */
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedNodesGetAttrIn/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedNodesGetAttrIn/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedCompositionOutputParamGetAttrIn() throws IOException {
       /*
        Nested Composition + Output Param get attribute In
          a. From a nested resource
          b. From another nested resource of same type (represented by same nested file)
          c. From a nested resource of different type (represented by different nested file)
          d. From a non-nested consolidation entity resource
          e. From a regular non-consolidation entity resource
         */
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedOutputParamGetAttrIn/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedOutputParamGetAttrIn/out";

        testTranslationWithInit();
    }

    @Test
    public void testMultiLevelNestedComposition() throws IOException {
        //Not pattern 4 (Multi level Complex VFC)
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedMultiLevels/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/nestedMultiLevels/out";

        testTranslationWithInit();
    }

    @Test
    public void testThreeNestedSameTypeTwoPointintToSameNestedFile() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/threeNestedSameTypeTwoPointingOnSameNestedFile/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/threeNestedSameTypeTwoPointingOnSameNestedFile/out";

        testTranslationWithInit();
    }

    @Test
    public void testThreeNestedSameTypePointingToDiffFiles() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/threeNestedPointingToThreeDiffNestedFilesSameComputeType/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedSingleCompute/threeNestedPointingToThreeDiffNestedFilesSameComputeType/out";

        testTranslationWithInit();
    }
}
