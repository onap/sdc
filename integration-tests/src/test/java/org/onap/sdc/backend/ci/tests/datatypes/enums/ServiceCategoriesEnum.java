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

package org.onap.sdc.backend.ci.tests.datatypes.enums;

import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServiceCategoriesEnum {

    E2E_SERVICE("E2E Service"),
    ETSI_NFV_NETWORK_SERVICE("ETSI NFV Network Service"),
    MOBILITY("Mobility"),
    NETWORK_L3("Network L1-3"),
    NETWORK_L4("Network L4+"),
    PARTNER_SERVICE("Partner Domain Service"),
    VOIP("VoIP Call Control");

    private final String value;

    public static ServiceCategoriesEnum getRandomElement() {
        final Random random = new Random();
        return ServiceCategoriesEnum.values()[random.nextInt(ServiceCategoriesEnum.values().length)];
    }
}
