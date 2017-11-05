package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionNestedSingleComputeFullTest extends BaseFullTranslationTest {

    private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/nestedSingleCompute/";

    @Test
    public void testNestedWithOneCompute() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "nestedWithOneCompute");
    }

    @Test
    public void testNestedWithOneComputeSamePortType() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "nestedWithOneComputeDiffPortType");
    }

    @Test
    public void testOneNestedWithTwoComputesOfSameType() throws IOException {
        //Not pattern 4 (Complex VFC)
        testTranslationWithInit(BASE_DIRECTORY + "nestedWithTwoComputesOfSameType");
    }

    @Test
    public void testOneNestedWithTwoDiffComputeTypes() throws IOException {
        //Not pattern 4 (Complex VFC)
        testTranslationWithInit(BASE_DIRECTORY + "nestedWithTwoDiffComputeTypes");
    }

    @Test
    public void testTwoNestedNodeTemplatesOfSameType() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "twoNestedNodeTemplatesWithSameComputeType");
    }

    @Test
    public void testTwoDiffNestedFilesWithSameComputeType() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "diffNestedFilesWithSameComputeType");
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
        testTranslationWithInit(BASE_DIRECTORY + "nestedNodesConnectedIn");
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
        testTranslationWithInit(BASE_DIRECTORY + "nestedNodesGetAttrIn");
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
        testTranslationWithInit(BASE_DIRECTORY + "nestedOutputParamGetAttrIn");
    }

    @Test
    public void testMultiLevelNestedComposition() throws IOException {
        //Not pattern 4 (Multi level Complex VFC)
        testTranslationWithInit(BASE_DIRECTORY + "nestedMultiLevels");
    }

    @Test
    public void testThreeNestedSameTypeTwoPointingToSameNestedFile() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "threeNestedSameTypeTwoPointingOnSameNestedFile");
    }

    @Test
    public void testThreeNestedSameTypePointingToDiffFiles() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "threeNestedPointingToThreeDiffNestedFilesSameComputeType");
    }

    @Test
    public void testMultipleReferencesToSameNestedFilesWithSameComputeType() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "multipleReferencesToSameNestedFilesWithSameComputeType");
    }

    @Test
    public void testDuplicateReq() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "duplicateReqs");
    }
}
