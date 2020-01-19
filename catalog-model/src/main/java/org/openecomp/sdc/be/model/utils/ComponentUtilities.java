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

package org.openecomp.sdc.be.model.utils;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class ComponentUtilities {
    private ComponentUtilities() {
    }

    public static Optional<String> getComponentInstanceNameByInstanceId(Component component, String id) {
        return component.getComponentInstanceById(id)
                .flatMap(instance -> component.getComponentInstanceByName(instance.getName()))
                .map(ComponentInstance::getName);
    }

    public static List<Annotation> getInputAnnotations(Component component, String inputName) {
        return getInputByName(component, inputName)
                .map(InputDefinition::getAnnotations)
                .orElse(emptyList());
    }

    private static Optional<InputDefinition> getInputByName(Component component, String inputName) {
        return component.safeGetInputs().stream()
                .filter(input -> input.getName().equals(inputName))
                .findFirst();
    }

    public static boolean isNotUpdatedCapReqName(String prefix, String currName, String previousName) {
        return StringUtils.isEmpty(previousName) || StringUtils.isEmpty(currName) || !currName.equals(prefix + previousName);
    }
}
