package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import org.openecomp.sdc.asdctool.impl.validator.tasks.VfValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.model.jsontitan.operations.TopologyTemplateOperation;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by chaya on 7/4/2017.
 */
public class VfArtifactValidationTask extends VfValidationTask {

    @Autowired
    ArtifactValidationUtils artifactValidationUtils;

    @Autowired
    protected TopologyTemplateOperation topologyTemplateOperation;

    public VfArtifactValidationTask() {
        this.name = "VF Artifact Validation Task";
    }

    @Override
    public VertexResult validate(GraphVertex vertex) {
        return artifactValidationUtils.validateTopologyTemplateArtifacts(vertex, getTaskName());
    }
}
