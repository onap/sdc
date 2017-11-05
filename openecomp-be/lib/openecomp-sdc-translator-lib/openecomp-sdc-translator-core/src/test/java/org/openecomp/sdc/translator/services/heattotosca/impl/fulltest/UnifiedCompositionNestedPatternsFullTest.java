package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionNestedPatternsFullTest extends BaseFullTranslationTest {

    private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/nestedOtherScenarios/";

    @Test
    public void testNestedWithOneLevelMultipleComputesSingleSubstitution() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "oneLevel/nestedSingleSubstitution1B");
    }

    @Test
    public void testNestedWithOneLevelMultipleComputesScalingInstance() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "oneLevel/nestedScalingInstance");
    }

    @Test
    public void testNestedWithOneLevelMultipleComputesCatalogInstance() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "oneLevel/nestedCatalogInstance");
    }

    @Test
    public void testNestedWithOneLevelAllNonNestedPatterns() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "oneLevel/nestedAllNonNestedPatterns");
    }

    @Test
    public void testNestedWithOneLevelNoCompute() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "oneLevel/nestedNoCompute");
    }

    @Test
    public void testNestedWithOneLevelOtherPatternsNoComputeWithConnectivity() throws IOException {
        //One nested resource with no Compute, one nested resource having all non nested patterns
        // with connectivity between themo
        testTranslationWithInit(BASE_DIRECTORY + "oneLevel/nestedAllPatternsConnectivity");
    }

    @Test
    public void testNestedWithOneLevelTwoSameFileOneOtherAllPattern1B() throws IOException {
        // Heat file with 3 nested resources, while 2 point to the same nested heat file, and all
        // nested heat file including pattern 1B.
        testTranslationWithInit(BASE_DIRECTORY + "oneLevel/nestedTwoSameFileOneDiff");
    }

    @Test
    public void testNestedWithOneLevelTwoSameFileOneOtherAllPattern1BWithConnectivity() throws
        IOException {
        // Heat file with 3 nested resources, while 2 point to the same nested heat file, and all
        // nested heat file including pattern 1B. +  conectivity between all nested resources
        testTranslationWithInit(BASE_DIRECTORY + "oneLevel/nestedTwoSameOneDiffWithConnectivity");
    }

    //**************** NESTED MULTI-LEVEL TESTS ******************************

    @Test
    public void testNestedMultiLevelPortSecurityGroupNetworkPattern1B() throws
        IOException {
        // heat file - 1 nested resource + security group which will be connected to port in ALL
        // nested levels, network which will be connected from port in ALL nested level.
        //nested heat level 1 - 1 nested resource  + pattern 1B
        //nested heat level 2 - 1 nested resource  + pattern 1B
        testTranslationWithInit(BASE_DIRECTORY + "multiLevel/portSecurityGroupNetPattern1B");
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

        testTranslationWithInit(BASE_DIRECTORY + "multiLevel/allPatternsDependsOnConnectivity");
    }

    @Test
    public void testThreeNestedLevelsDiffVmTypePattern1B() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "multiLevel/threeNestedLevelsDiffVmTypePattern1B");
    }

    @Test
    public void testThreeNestedLevelsSameVmTypePattern1B() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "multiLevel/threeNestedLevelsSameVmTypePattern1B");
    }

    @Test
    public void testTwoNestedLevelsWithAllPatternsAndConnectionsBetweenThem() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "multiLevel/twoNestedLevelsWithAllPatternsAndConnectivities");
    }

}
