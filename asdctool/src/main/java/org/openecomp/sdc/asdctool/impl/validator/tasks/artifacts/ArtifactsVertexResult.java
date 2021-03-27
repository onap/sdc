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
package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import java.util.HashSet;
import java.util.Set;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;

/**
 * Created by chaya on 7/25/2017.
 */
public class ArtifactsVertexResult extends VertexResult {

    Set<String> notFoundArtifacts = new HashSet<>();

    public ArtifactsVertexResult() {
    }

    public ArtifactsVertexResult(boolean status) {
        super(status);
    }

    public void addNotFoundArtifact(String artifactId) {
        notFoundArtifacts.add(artifactId);
    }

    @Override
    public String getResult() {
        return notFoundArtifacts.toString();
    }
}
