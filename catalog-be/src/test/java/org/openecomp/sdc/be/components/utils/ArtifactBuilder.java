/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;

import java.util.ArrayList;

public class ArtifactBuilder {

    private ArtifactDefinition artifactDefinition;

    public ArtifactBuilder() {
        this.artifactDefinition = new ArtifactDefinition();
    }

    public ArtifactBuilder setType(String type) {
        this.artifactDefinition.setArtifactType(type);
        return this;
    }

    public ArtifactBuilder setName(String name) {
        this.artifactDefinition.setArtifactName(name);
        return this;
    }

    public ArtifactBuilder setLabel(String label) {
        this.artifactDefinition.setArtifactLabel(label);
        return this;
    }

    public ArtifactBuilder addHeatParam(HeatParameterDefinition heatParam) {
        if (this.artifactDefinition.getHeatParameters() == null) {
            this.artifactDefinition.setHeatParameters(new ArrayList<>());
        }
        this.artifactDefinition.getHeatParameters().add(heatParam);
        return this;
    }

    public ArtifactDefinition build() {
        return artifactDefinition;
    }
}
