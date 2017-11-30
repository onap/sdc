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

package org.openecomp.sdc.be.components.validation;

import java.util.Optional;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;

public class ComponentValidations {

    public static boolean validateComponentInstanceExist(Component component, String instanceId) {
        return Optional.ofNullable(component.getComponentInstances())
                       .map(componentInstances -> componentInstances.stream().map(ComponentInstance::getUniqueId).collect(Collectors.toList()))
                       .filter(instancesIds -> instancesIds.contains(instanceId))
                       .isPresent();
    }

}
