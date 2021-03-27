/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.togglz;

import java.util.Arrays;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum ToggleableFeature implements Feature {
    @Label("Default Feature") DEFAULT_FEATURE, @Label("Healing") HEALING;

    public static Feature getFeatureByName(String featureName) {
        return Arrays.stream(values()).filter(e -> e.name().equals(featureName)).findFirst().orElse(null);
    }

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
