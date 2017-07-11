/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
