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

package org.openecomp.sdc.be.csar.pnf;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class PnfSoftwareVersion {

    private final String version;
    private final String description;

    /**
     * Stores the pnf software version yaml fields.
     */
    @Getter
    @AllArgsConstructor
    public enum PnfSoftwareVersionField {
        DESCRIPTION("description"),
        PNF_SOFTWARE_VERSION("pnf_software_version");

        private final String fieldName;
    }

    public boolean isValid() {
        return StringUtils.isNotEmpty(version);
    }
}