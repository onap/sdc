package org.openecomp.sdc.be.components;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;

public interface ArtifactsResolver {

    /**
     * searching for an artifact with the give {@code artifactId} on the given {@code component}
     * @param component the component to look for artifact in
     * @param componentType the type of the component to look for artifact in
     * @param artifactId the id of the artifact to find
     * @return the found artifact or null if not exist
     */
    ArtifactDefinition findArtifactOnComponent(Component component, ComponentTypeEnum componentType, String artifactId);

    /**
     * searching for an artifact with the give {@code artifactId} on the given {@code componentInstance}
     * @param componentInstance the component instance to look for the artifact in
     * @param artifactId the if of the artifact to find
     * @return the found artifact or null if not exist
     */
    ArtifactDefinition findArtifactOnComponentInstance(ComponentInstance componentInstance, String artifactId);

}
