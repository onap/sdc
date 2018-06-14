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

package org.openecomp.sdc.heat.services;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;

public class HeatResourceUtil {

    private static final String UNDERSCORE = "_";
    private static final String WORDS_REGEX = "(\\w+)";
    private static final String PORT_RESOURCE_ID_REGEX_SUFFIX = "(_\\d+)*";
    private static final String PORT_RESOURCE_ID_REGEX_PREFIX =
            WORDS_REGEX + PORT_RESOURCE_ID_REGEX_SUFFIX;
    private static final String PORT_INT_RESOURCE_ID_REGEX_PREFIX = PORT_RESOURCE_ID_REGEX_PREFIX
            + UNDERSCORE + "int_"+ WORDS_REGEX + UNDERSCORE;
    private static final String SUB_INTERFACE_INT_RESOURCE_ID_REGEX_PREFIX =
            PORT_RESOURCE_ID_REGEX_PREFIX + UNDERSCORE + "subint_"+ WORDS_REGEX + UNDERSCORE;

    public static Optional<String> evaluateNetworkRoleFromResourceId(String resourceId,
                                                                     String resourceType) {
        Optional<PortType> portType = getPortType(resourceType);
        if (portType.isPresent()) {
            String portResourceIdRegex =
                    PORT_RESOURCE_ID_REGEX_PREFIX + UNDERSCORE + WORDS_REGEX + UNDERSCORE
                            + portType.get().getPortTypeName() + PORT_RESOURCE_ID_REGEX_SUFFIX;
            String portIntResourceIdRegex =
                    PORT_INT_RESOURCE_ID_REGEX_PREFIX + portType.get().getPortTypeName()
                            + PORT_RESOURCE_ID_REGEX_SUFFIX;

            String portNetworkRole = getNetworkRole(resourceId, portResourceIdRegex);
            String portIntNetworkRole = getNetworkRole(resourceId, portIntResourceIdRegex);

            return Optional.ofNullable(Objects.nonNull(portNetworkRole)
                    ? portNetworkRole : portIntNetworkRole);
        }
        return Optional.empty();
    }

    private static Optional<PortType> getPortType(String resourceType) {
        if (resourceType.equals(
                HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource())) {
            return Optional.of(PortType.VMI);
        } else if (resourceType.equals(
                HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource())) {
            return Optional.of(PortType.PORT);
        }
        return Optional.empty();
    }

    public static Optional<String> extractNetworkRoleFromSubInterfaceId(String  resourceId,
                                                                        String resourceType) {
        Optional<PortType> portType = getPortType(resourceType);
        if (portType.isPresent()) {
            String subInterfaceResourceIdRegex =
                    SUB_INTERFACE_INT_RESOURCE_ID_REGEX_PREFIX + portType.get().getPortTypeName()
                            + PORT_RESOURCE_ID_REGEX_SUFFIX;

            return Optional.ofNullable(getNetworkRole(resourceId, subInterfaceResourceIdRegex));
        }
        return Optional.empty();
    }

    private enum PortType {
        PORT("port"),
        VMI("vmi");

        private String portTypeName;

        PortType(String portTypeName) {
            this.portTypeName = portTypeName;
        }

        public String getPortTypeName() {
            return portTypeName;
        }
    }

    private static String getNetworkRole(String portResourceId, String portIdRegex) {
        Pattern pattern = Pattern.compile(portIdRegex);
        Matcher matcher = pattern.matcher(portResourceId);
        if (matcher.matches()) {
            String networkRole = matcher.group(3);
            //Assuming network role will not contain ONLY digits
            if (!networkRole.matches("\\d+")) {
                return matcher.group(3);
            }
        }
        return null;
    }

}
