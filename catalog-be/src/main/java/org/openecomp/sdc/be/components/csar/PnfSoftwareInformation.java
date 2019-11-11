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

package org.openecomp.sdc.be.components.csar;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the Pnf software information non-mano yaml
 */
@Getter
@Setter
public class PnfSoftwareInformation {

    private String description;
    private String provider;
    private String version;
    @Setter(AccessLevel.NONE)
    private Set<PnfSoftwareVersion> softwareVersionSet = new LinkedHashSet<>();

    /**
     * Adds a {@link PnfSoftwareVersion} instance to the software version set
     * @param softwareVersion the pnf software version to add
     */
    public void addToSoftwareVersionSet(final PnfSoftwareVersion softwareVersion) {
        softwareVersionSet.add(softwareVersion);
    }

    public Set<PnfSoftwareVersion> getSoftwareVersionSet() {
        return new LinkedHashSet<>(softwareVersionSet);
    }

    /**
     * Stores the software information yaml field names.
     */
    @AllArgsConstructor
    @Getter
    public enum PnfSoftwareInformationField {
        DESCRIPTION("description"),
        PROVIDER("provider"),
        VERSION("version"),
        PNF_SOFTWARE_INFORMATION("pnf_software_information");

        private final String fieldName;

    }
}
