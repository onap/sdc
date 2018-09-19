/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.be.model.jsontitan.utils;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.model.InterfaceDefinition;

import java.util.Collection;
import java.util.Formatter;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openecomp.sdc.common.util.ValidationUtils;

public class InterfaceUtils {

    public static final String INTERFACE_TOSCA_RESOURCE_NAME = "org.openecomp.interfaces.node.lifecycle.%s";

    public static final Optional<InterfaceDefinition> getInterfaceDefinitionFromToscaName(
            Collection<InterfaceDefinition> interfaces,
            String resourceName) {
        if (CollectionUtils.isEmpty(interfaces)) {
            return Optional.empty();
        }

        String toscaName = createInterfaceToscaResourceName(resourceName);
        return interfaces.stream().filter(
                interfaceDefinition -> interfaceDefinition.getToscaResourceName() != null && interfaceDefinition
                        .getToscaResourceName().equals(toscaName)).findAny();
    }

    public static Collection<InterfaceDefinition> getInterfaceDefinitionListFromToscaName(Collection<InterfaceDefinition> interfaces,
                                                                                          String resourceName) {
        if (CollectionUtils.isEmpty(interfaces)) {
            return CollectionUtils.EMPTY_COLLECTION;
        }

        String toscaName = createInterfaceToscaResourceName(resourceName);
        return interfaces.stream().filter(
                interfaceDefinition -> interfaceDefinition.getToscaResourceName() != null && interfaceDefinition
                        .getToscaResourceName().equals(toscaName)).collect(Collectors.toList());
    }

    public static String createInterfaceToscaResourceName(String resourceName) {
        StringBuilder sb = new StringBuilder();
        try (Formatter formatter = new Formatter(sb)) {
            return formatter.format(INTERFACE_TOSCA_RESOURCE_NAME, ValidationUtils.convertToSystemName(resourceName)).toString();
        }
    }
}
