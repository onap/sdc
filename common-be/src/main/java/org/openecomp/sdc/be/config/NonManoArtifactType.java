/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.sdc.be.config;

import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Stores non mano artifact types.
 */
@Getter
@AllArgsConstructor
public enum NonManoArtifactType {
    ONAP_VES_EVENTS("onap_ves_events"),
    ONAP_PM_DICTIONARY("onap_pm_dictionary"),
    ONAP_YANG_MODULES("onap_yang_modules"),
    ONAP_ANSIBLE_PLAYBOOKS("onap_ansible_playbooks"),
    ONAP_SCRIPTS("onap_scripts"),
    ONAP_OTHERS("onap_others"),
    ONAP_SW_INFORMATION("onap_pnf_sw_information");

    private final String type;

    public static Optional<NonManoArtifactType> parse(final String type) {
        return Arrays.stream(values())
            .filter(nonManoArtifactType -> nonManoArtifactType.getType().equals(type))
            .findFirst();
    }
}
