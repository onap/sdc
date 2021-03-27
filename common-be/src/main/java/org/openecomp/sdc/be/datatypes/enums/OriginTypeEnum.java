/*-
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.datatypes.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OriginTypeEnum {
    // @formatter:off
    PRODUCT("Product", "Product", "product instance", ComponentTypeEnum.PRODUCT, false),
    SERVICE("Service", "Service", "service instance", ComponentTypeEnum.SERVICE, false),
    VF("VF", "VF (Virtual Function)", "resource instance", ComponentTypeEnum.RESOURCE, false),
    VFC("VFC", "VFC (Virtual Function Component)", "resource instance", ComponentTypeEnum.RESOURCE, true),
    CP("CP", "CP (Connection Point)", "resource instance", ComponentTypeEnum.RESOURCE, true),
    VL("VL", "VL (Virtual Link)", "resource instance", ComponentTypeEnum.RESOURCE, true),
    Configuration("Configuration", "Configuration ()", "resource instance", ComponentTypeEnum.RESOURCE, true),
    VFCMT("VFCMT", "VFCMT (VFC Monitoring Template)", "resource instance", ComponentTypeEnum.RESOURCE, true),
    CVFC("CVFC", "CVFC (Complex Virtual Function Component)", "resource instance", ComponentTypeEnum.RESOURCE, false),
    PNF("PNF", "PNF (Physical Network Function)", "resource instance", ComponentTypeEnum.RESOURCE, false),
    CR("CR", "CR (Complex Resource)", "resource instance", ComponentTypeEnum.RESOURCE, false),
    ServiceProxy("Service Proxy", "Service Proxy", "service proxy", ComponentTypeEnum.RESOURCE, false),
    ServiceSubstitution("Service Substitution", "Service Substitution", "service substitution", ComponentTypeEnum.RESOURCE, false);
    // @formatter:on

    private final String value;
    private final String displayValue;
    private final String instanceType;
    private final ComponentTypeEnum componentType;
    private final boolean isAtomicType;

    public static OriginTypeEnum findByValue(String value) {
        OriginTypeEnum ret = null;
        for (OriginTypeEnum curr : OriginTypeEnum.values()) {
            if (curr.getValue().equals(value)) {
                ret = curr;
                break;
            }
        }
        return ret;
    }
}
