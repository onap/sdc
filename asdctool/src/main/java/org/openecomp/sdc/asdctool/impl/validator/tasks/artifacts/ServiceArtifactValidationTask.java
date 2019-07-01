package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import org.openecomp.sdc.asdctool.impl.validator.tasks.ServiceValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by chaya on 7/6/2017.
 */
public class ServiceArtifactValidationTask extends ServiceValidationTask {

    private ArtifactValidationUtils artifactValidationUtils;

    @Autowired
    public ServiceArtifactValidationTask(ArtifactValidationUtils artifactValidationUtils) {
        this.artifactValidationUtils = artifactValidationUtils;
        this.name = "Service Artifact Validation Task";
    }

    @Override
    public ArtifactsVertexResult validate(GraphVertex vertex) {
        return artifactValidationUtils.validateTopologyTemplateArtifacts(vertex, getTaskName());
    }
}
