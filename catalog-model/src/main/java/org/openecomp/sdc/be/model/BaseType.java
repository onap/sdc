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

package org.openecomp.sdc.be.model;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


public class BaseType  {
    @Getter
    @Setter
    private String toscaResourceName;
    
    private final List<Semver> versions = new ArrayList<>();
    
    public BaseType(final String toscaResourceName) {
        this.toscaResourceName = toscaResourceName;
    }
    
    public BaseType(final String toscaResourceName, final List<String> versions) {
        this.toscaResourceName = toscaResourceName;
        versions.forEach(version -> this.versions.add(new Semver(version, SemverType.LOOSE)));
    }
    
    public void addVersion(final String version) {
        versions.add(new Semver(version, SemverType.LOOSE));
    }
    
    public List<String> getVersions(){
        Collections.sort(versions);
        final List<String> versionsAsStrings = new ArrayList<>();
        this.versions.forEach(version -> versionsAsStrings.add(version.getValue()));
        return versionsAsStrings;
    }

}
