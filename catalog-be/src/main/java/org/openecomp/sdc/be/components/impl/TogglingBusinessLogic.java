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
package org.openecomp.sdc.be.components.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.togglz.ToggleableFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.NamedFeature;

@Component("togglingBusinessLogic")
public class TogglingBusinessLogic extends BaseBusinessLogic {

    @Autowired
    public TogglingBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation, IGroupInstanceOperation groupInstanceOperation,
                                 IGroupTypeOperation groupTypeOperation, InterfaceOperation interfaceOperation,
                                 InterfaceLifecycleOperation interfaceLifecycleTypeOperation, ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
    }

    public Map<String, Boolean> getAllFeatureStates() {
        return Arrays.stream(ToggleableFeature.values()).collect(Collectors.toMap(Enum::name, ToggleableFeature::isActive));
    }

    public boolean getFeatureState(String featureName) {
        return ToggleableFeature.valueOf(featureName).isActive();
    }

    public void setAllFeatures(boolean state) {
        Arrays.asList(ToggleableFeature.values()).forEach(toggleableFeature -> updateFeatureState(toggleableFeature.name(), state));
    }

    public void updateFeatureState(String featureName, boolean state) {
        Feature feature = new NamedFeature(featureName);
        FeatureState featureState = new FeatureState(feature, state);
        FeatureContext.getFeatureManager().setFeatureState(featureState);
    }
}
