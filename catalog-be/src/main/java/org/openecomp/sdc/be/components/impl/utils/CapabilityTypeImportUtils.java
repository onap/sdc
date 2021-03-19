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
package org.openecomp.sdc.be.components.impl.utils;

import java.util.Objects;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;

public class CapabilityTypeImportUtils {

    private CapabilityTypeImportUtils() {
    }

    public static boolean isCapabilityTypesEquals(CapabilityTypeDefinition capabilityType1, CapabilityTypeDefinition capabilityType2) {
        if (capabilityType1 == capabilityType2) {
            return true;
        }
        if (capabilityType1 == null || capabilityType2 == null) {
            return false;
        }
        return Objects.equals(capabilityType1.getType(), capabilityType2.getType()) && Objects
            .equals(capabilityType1.getVersion(), capabilityType2.getVersion()) && Objects
            .equals(capabilityType1.getDerivedFrom(), capabilityType2.getDerivedFrom()) && Objects
            .equals(capabilityType1.getValidSourceTypes(), capabilityType2.getValidSourceTypes()) && Objects
            .equals(capabilityType1.getDescription(), capabilityType2.getDescription()) && Objects
            .equals(capabilityType1.getProperties(), capabilityType2.getProperties());
    }
}
