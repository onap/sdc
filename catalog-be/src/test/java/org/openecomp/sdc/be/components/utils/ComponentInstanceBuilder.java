package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;

import java.util.HashMap;
import java.util.Map;

public class ComponentInstanceBuilder {

    private ComponentInstance componentInstance;

    public ComponentInstanceBuilder() {
        componentInstance = new ComponentInstance();
    }

    public ComponentInstanceBuilder setName(String name) {
        componentInstance.setName(name);
        return this;
    }

    public ComponentInstanceBuilder setComponentUid(String componentUid) {
        componentInstance.setComponentUid(componentUid);
        return this;
    }

    public ComponentInstanceBuilder setId(String id) {
        componentInstance.setUniqueId(id);
        return this;
    }

    public ComponentInstanceBuilder setToscaName(String toscaName) {
        componentInstance.setToscaComponentName(toscaName);
        return this;
    }

    public ComponentInstanceBuilder addDeploymentArtifact(ArtifactDefinition artifactDefinition) {
        Map<String, ArtifactDefinition> deploymentArtifacts = componentInstance.getDeploymentArtifacts();
        if (deploymentArtifacts == null) {
            componentInstance.setDeploymentArtifacts(new HashMap<>());
        }
        componentInstance.getDeploymentArtifacts().put(artifactDefinition.getArtifactName(), artifactDefinition);
        return this;
    }

    public ComponentInstance build() {
        return componentInstance;
    }
}
