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

    public ArtifactOperationInfo(boolean isExternalApi, boolean ignoreLifecycleState,
        ArtifactOperationEnum artifactOperationEnum) {
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
