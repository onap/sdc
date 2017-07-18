package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionNestedPatternsFullTest extends BaseFullTranslationTest {

    @Override
    @Before
    public void setUp() throws IOException {
        // do not delete this function. it prevents the superclass setup from running
    }

    @Test
    public void testNestedWithOneLevelMultipleComputesSingleSubstitution() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedSingleSubstitution1B/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedSingleSubstitution1B/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedWithOneLevelMultipleComputesScalingInstance() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedScalingInstance/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedScalingInstance/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedWithOneLevelMultipleComputesCatalogInstance() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedCatalogInstance/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedCatalogInstance/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedWithOneLevelAllNonNestedPatterns() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedAllNonNestedPatterns/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedAllNonNestedPatterns/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedWithOneLevelNoCompute() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedNoCompute/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedNoCompute/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedWithOneLevelOtherPatternsNoComputeWithConnectivity() throws IOException {
        //One nested resource with no Compute, one nested resource having all non nested patterns
        // with connectivity between themo
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedAllPatternsConnectivity/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedAllPatternsConnectivity/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedWithOneLevelTwoSameFileOneOtherAllPattern1B() throws IOException {
        // Heat file with 3 nested resources, while 2 point to the same nested heat file, and all
        // nested heat file including pattern 1B.
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedTwoSameFileOneDiff/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedTwoSameFileOneDiff/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedWithOneLevelTwoSameFileOneOtherAllPattern1BWithConnectivity() throws
        IOException {
        // Heat file with 3 nested resources, while 2 point to the same nested heat file, and all
        // nested heat file including pattern 1B. +  conectivity between all nested resources
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedTwoSameOneDiffWithConnectivity/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/oneLevel/nestedTwoSameOneDiffWithConnectivity/out";

        testTranslationWithInit();
    }

    //**************** NESTED MULTI-LEVEL TESTS ******************************

    @Test
    public void testNestedMultiLevelPortSecurityGroupNetworkPattern1B() throws
        IOException {
        // heat file - 1 nested resource + security group which will be connected to port in ALL
        // nested levels, network which will be connected from port in ALL nested level.
        //nested heat level 1 - 1 nested resource  + pattern 1B
        //nested heat level 2 - 1 nested resource  + pattern 1B
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/portSecurityGroupNetPattern1B/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/portSecurityGroupNetPattern1B/out";

        testTranslationWithInit();
    }

    @Test
    public void testNestedMultiLevelAllPatternsDependsOnConnectivity() throws
        IOException {
        /*
        HEAT FILE - 1 : nested resource to heat without nova in the nested heat + 1 nested
        resource + pattern 1B +  pattern C1 + pattern 4 + connectivity between them all (using
        depends on from/to the first nested resource, without nova)

        NESTED HEAT LEVEL 1 - 1 nested resource + pattern 1B +  pattern C1 + pattern 4 +
        connectivity between them all (VM Types same as Main)

        NESTED HEAT LEVEL 2 - 1 nested resource + pattern 1B +  pattern C1 + pattern 4 +
        connectivity between them all
        */

        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/allPatternsDependsOnConnectivity/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/allPatternsDependsOnConnectivity/out";

        testTranslationWithInit();
    }

    @Test
    public void testThreeNestedLevelsDiffVmTypePattern1B() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/threeNestedLevelsDiffVmTypePattern1B/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/threeNestedLevelsDiffVmTypePattern1B/out";

        testTranslationWithInit();
    }

    @Test
    public void testThreeNestedLevelsSameVmTypePattern1B() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/threeNestedLevelsSameVmTypePattern1B/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/threeNestedLevelsSameVmTypePattern1B/out";

        testTranslationWithInit();
    }

    @Test
    public void testTwoNestedLevelsWithAllPatternsAndConnectionsBetweenThem() throws IOException {
        inputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/twoNestedLevelsWithAllPatternsAndConnectivities/in";
        outputFilesPath =
            "/mock/services/heattotosca/fulltest/nestedOtherScenarios/multiLevel/twoNestedLevelsWithAllPatternsAndConnectivities/out";

        testTranslationWithInit();
    }

}
