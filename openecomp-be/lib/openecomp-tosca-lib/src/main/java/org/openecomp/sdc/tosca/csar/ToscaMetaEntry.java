/*
 *(===========LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 * (===============================================================================
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
 * (===========LICENSE_END=========================================================
 */

package org.openecomp.sdc.tosca.csar;

import java.util.Arrays;
import java.util.Optional;

public enum ToscaMetaEntry {
    TOSCA_META_PATH_FILE_NAME("TOSCA-Metadata/TOSCA.meta"),
    TOSCA_META_FILE_VERSION_ENTRY("TOSCA-Meta-File-Version"),
    CSAR_VERSION_ENTRY("CSAR-Version"),
    CREATED_BY_ENTRY("Created-By"),
    ENTRY_DEFINITIONS("Entry-Definitions"),
    ENTRY_EVENTS("Entry-Events"),
    ETSI_ENTRY_MANIFEST("ETSI-Entry-Manifest"),
    ETSI_ENTRY_CHANGE_LOG("ETSI-Entry-Change-Log"),
    ETSI_ENTRY_TESTS("ETSI-Entry-Tests"),
    ETSI_ENTRY_LICENSES("ETSI-Entry-Licenses"),
    ETSI_ENTRY_CERTIFICATE("ETSI-Entry-Certificate"),
    TOSCA_META_FILE_VERSION("1.0");

    private final String name;

    ToscaMetaEntry(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<ToscaMetaEntry> parse(final String name) {
        return Arrays.stream(values()).filter(toscaMetaEntry -> toscaMetaEntry.getName().equals(name)).findFirst();
    }
}
