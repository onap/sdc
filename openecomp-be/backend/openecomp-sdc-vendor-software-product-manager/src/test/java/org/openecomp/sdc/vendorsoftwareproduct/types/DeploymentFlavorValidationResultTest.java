/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdc.vendorsoftwareproduct.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;

public class DeploymentFlavorValidationResultTest {
    @Test
    public void flavorShouldBeNotValidWhenNotNull() {
        Set<CompositionEntityValidationData> validationData = Collections.emptySet();
        DeploymentFlavorValidationResult componentValidationResult = new DeploymentFlavorValidationResult(validationData);
        assertEquals(componentValidationResult.getValidationData(), validationData);
        assertFalse(componentValidationResult.isValid());
    }

    @Test
    public void flavorShouldBeValidWhenNull() {
        DeploymentFlavorValidationResult componentValidationResult = new DeploymentFlavorValidationResult(null);
        assertTrue(componentValidationResult.isValid());
    }
}