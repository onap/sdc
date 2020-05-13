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

import org.openecomp.sdc.asdctool.impl.validator.report.Report;
import org.openecomp.sdc.asdctool.impl.validator.tasks.VfValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by chaya on 7/4/2017.
 */
public class VfArtifactValidationTask extends VfValidationTask {

    ArtifactValidationUtils artifactValidationUtils;

    @Autowired
    public VfArtifactValidationTask(ArtifactValidationUtils artifactValidationUtils) {
        this.artifactValidationUtils = artifactValidationUtils;
        this.name = "VF Artifact Validation Task";
    }

    @Override
    public VertexResult validate(Report report, GraphVertex vertex, String outputFilePath) {
        return artifactValidationUtils.validateTopologyTemplateArtifacts(report, vertex, getTaskName(), outputFilePath);
    }
}
