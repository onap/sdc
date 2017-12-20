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

package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Avrahamg
 * @since November 07, 2016
 */
public class FileDataStructureDto {
    private List<Module> modules = new ArrayList<>();
    private List<String> unassigned = new ArrayList<>();
    private List<String> artifacts = new ArrayList<>();
    private List<String> nested = new ArrayList<>();

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public List<String> getUnassigned() {
        return unassigned;
    }

    public void setUnassigned(List<String> unassigned) {
        this.unassigned = unassigned;
    }

    public List<String> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<String> artifacts) {
        this.artifacts = artifacts;
    }

    public List<String> getNested() {
        return nested;
    }

    public void setNested(List<String> nested) {
        this.nested = nested;
    }
}
