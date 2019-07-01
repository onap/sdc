package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

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
    public VertexResult validate(GraphVertex vertex) {
        return artifactValidationUtils.validateTopologyTemplateArtifacts(vertex, getTaskName());
    }
}
