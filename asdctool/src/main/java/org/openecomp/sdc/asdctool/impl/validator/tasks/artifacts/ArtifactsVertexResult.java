package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import java.util.HashSet;
import java.util.Set;

import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;

/**
 * Created by chaya on 7/25/2017.
 */
public class ArtifactsVertexResult extends VertexResult{
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
