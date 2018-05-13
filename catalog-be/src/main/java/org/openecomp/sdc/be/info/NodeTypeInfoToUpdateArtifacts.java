package org.openecomp.sdc.be.info;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;

public class NodeTypeInfoToUpdateArtifacts {

    private String nodeName;
    private Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle;


    public NodeTypeInfoToUpdateArtifacts(String nodeName,
            Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle) {
        super();
        this.nodeName = nodeName;
        this.nodeTypesArtifactsToHandle = nodeTypesArtifactsToHandle;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> getNodeTypesArtifactsToHandle() {
        return nodeTypesArtifactsToHandle;
    }

    public void setNodeTypesArtifactsToHandle(
            Map<String, EnumMap<ArtifactOperationEnum, List<ArtifactDefinition>>> nodeTypesArtifactsToHandle) {
        this.nodeTypesArtifactsToHandle = nodeTypesArtifactsToHandle;
    }


}
