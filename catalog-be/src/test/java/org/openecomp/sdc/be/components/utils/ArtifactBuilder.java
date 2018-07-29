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
