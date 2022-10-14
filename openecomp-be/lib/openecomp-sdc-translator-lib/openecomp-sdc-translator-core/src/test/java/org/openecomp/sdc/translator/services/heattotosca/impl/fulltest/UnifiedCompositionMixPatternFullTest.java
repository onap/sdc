/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

public class UnifiedCompositionMixPatternFullTest extends BaseFullTranslationTest {

    private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/mixPatterns/";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testMixPatterns() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "oneAppearancePerPattern");
    }

    @Test
    public void testMixPatternsWithConnectivityBetweenPatterns() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "connectivityBetweenPatterns");
    }

    @Test
    public void testMixPatternsWithConnectivityAndMoreThanOneOccurenceForEachPattern()
        throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "twoAppearancePerPatternWithConnectivities");
    }

    @Test
    public void testDuplicateResourceIdsInDiffAddOnFiles() throws IOException {
        exception.expect(CoreException.class);
        exception.expectMessage("Resource with id lb_0_int_oam_int_0_port occurs more " +
            "than once in different addOn files");

        testTranslationWithInit(BASE_DIRECTORY + "duplicateResourceIdsInDiffAddOnFiles");
    }

    @Test
    public void testMixPatternsWithDependencyConnectivity() throws IOException {
        testTranslationWithInit(BASE_DIRECTORY + "dependencyConnectivity");
    }

}
