/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.tosca.utils;

public class InterfaceTypesNameUtil {

    private InterfaceTypesNameUtil() {
    }

    /**
     * Build the short name of an interface_type by grabbing the final name in its path. E.g. "tosca.interfaces.relationship.Configure" will be
     * shortened to "Configure".
     *
     * @param interfaceName the full interface name
     * @return the shortened name of the interface
     */
    public static String buildShortName(final String interfaceName) {
        if (interfaceName == null) {
            throw new IllegalArgumentException("interfaceName cannot be null");
        }
        final int index = interfaceName.lastIndexOf('.');
        return index > 0 && interfaceName.length() > index + 1 ? interfaceName.substring(index + 1) : interfaceName;
    }
}
