/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia
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
package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.FileData.Type;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;

public class ManifestAnalyzer {

    private final static Set<Type> HEAT_TYPES = Collections.singleton(Type.HEAT);
    private final static Set<Type> HELM_TYPES = Collections.singleton(Type.HELM);
    private final ManifestContent manifest;

    public ManifestAnalyzer(ManifestContent manifest) {
        this.manifest = manifest;
    }

    public boolean hasHeatEntries() {
        return hasEntriesOfType(HEAT_TYPES);
    }

    public boolean hasHelmEntries() {
        return hasEntriesOfType(HELM_TYPES);
    }

    public List<FileData> getHelmEntries() {
        List<FileData> entries = new ArrayList<>();
        if (hasFileData()) {
            for (FileData d : manifest.getData()) {
                if (HELM_TYPES.contains(d.getType())) {
                    entries.add(d);
                }
            }
        }
        return entries;
    }

    private boolean hasEntriesOfType(Set<Type> types) {
        boolean result = false;
        if (hasFileData()) {
            result = manifest.getData().stream().anyMatch(fileData -> types.contains(fileData.getType()));
        }
        return result;
    }

    private boolean hasFileData() {
        return manifest != null && manifest.getData() != null && !manifest.getData().isEmpty();
    }
}
