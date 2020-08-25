/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.sdc.frontend.ci.tests.verificator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.AssertJUnit.assertTrue;

public class CustomizationUUIDVerificator {

    public static void validateCustomizationUUIDuniqueness(List customizationUUIDs) {
        boolean hasNoDuplicates = CustomizationUUIDVerificator.containsUnique(customizationUUIDs);
        assertTrue("There are duplicate customizationUUIDs in list", hasNoDuplicates);
    }

    private static <T> boolean containsUnique(List<T> list) {
        Set<T> set = new HashSet<>();

        for (T t : list) {
            if (!set.add(t)) {
                return false;
            }
        }

        return true;
    }

}
