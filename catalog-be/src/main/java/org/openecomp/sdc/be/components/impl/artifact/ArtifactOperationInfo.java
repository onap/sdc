/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.impl.artifact;

import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum.CREATE;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum.DELETE;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum.DOWNLOAD;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum.LINK;
import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum.UPDATE;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;

public final class ArtifactOperationInfo {

    private final ArtifactOperationEnum artifactOperationEnum;
    private final boolean isExternalApi;
    private final boolean ignoreLifecycleState;

    public ArtifactOperationInfo(boolean isExternalApi, boolean ignoreLifecycleState, ArtifactOperationEnum artifactOperationEnum) {
        this.artifactOperationEnum = artifactOperationEnum;
        this.isExternalApi = isExternalApi;
        this.ignoreLifecycleState = ignoreLifecycleState;
    }

    public boolean isExternalApi() {
        return isExternalApi;
    }

    public boolean ignoreLifecycleState() {
        return ignoreLifecycleState;
    }

    public ArtifactOperationEnum getArtifactOperationEnum() {
        return artifactOperationEnum;
    }

    public boolean isCreateOrLink() {
        return artifactOperationEnum == CREATE || artifactOperationEnum == LINK;
    }

    public boolean isNotCreateOrLink() {
        return !isCreateOrLink();
    }

    public boolean isDownload() {
        return artifactOperationEnum == DOWNLOAD;
    }

    public boolean isNotDownload() {
        return !isDownload();
    }

    public boolean isUpdate() {
        return artifactOperationEnum == UPDATE;
    }

    public boolean isDelete() {
        return artifactOperationEnum == DELETE;
    }
}
