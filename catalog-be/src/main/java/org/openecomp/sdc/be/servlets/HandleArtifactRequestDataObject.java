package org.openecomp.sdc.be.servlets;

import javax.servlet.http.HttpServletRequest;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

public class HandleArtifactRequestDataObject {

    private String data; 
    private HttpServletRequest request; 
    private String componentId; 
    private String interfaceName;
    private String operationName;
    private String artifactId;
    private ComponentTypeEnum componentType;
    private ArtifactOperationEnum operationEnum;
    private String parentId;
    private String containerComponentType;

    
    public String getData() {
        return data;
    }

    public HandleArtifactRequestDataObject setData(String data) {
        this.data = data;
        return this;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HandleArtifactRequestDataObject setRequest(HttpServletRequest request) {
        this.request = request;
        return this;
    }

    public String getComponentId() {
        return componentId;
    }

    public HandleArtifactRequestDataObject setComponentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public HandleArtifactRequestDataObject setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        return this;
    }

    public String getOperationName() {
        return operationName;
    }

    public HandleArtifactRequestDataObject setOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public HandleArtifactRequestDataObject setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public ComponentTypeEnum getComponentType() {
        return componentType;
    }

    public HandleArtifactRequestDataObject setComponentType(ComponentTypeEnum componentType) {
        this.componentType = componentType;
        return this;
    }

    public ArtifactOperationEnum getOperationEnum() {
        return operationEnum;
    }

    public HandleArtifactRequestDataObject setOperationEnum(ArtifactOperationEnum operationEnum) {
        this.operationEnum = operationEnum;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public HandleArtifactRequestDataObject setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getContainerComponentType() {
        return containerComponentType;
    }

    public HandleArtifactRequestDataObject setContainerComponentType(String containerComponentType) {
        this.containerComponentType = containerComponentType;
        return this;
    }


}
