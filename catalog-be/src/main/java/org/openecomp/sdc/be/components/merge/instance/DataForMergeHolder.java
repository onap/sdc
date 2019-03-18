package org.openecomp.sdc.be.components.merge.instance;


import org.openecomp.sdc.be.model.*;

import java.util.*;

/**
 * Created by chaya on 9/7/2017.
 */
public class DataForMergeHolder {

    private List<ComponentInstanceInput> origComponentInstanceInputs;
    private List<ComponentInstanceProperty> origComponentInstanceProperties;
    private List<InputDefinition> origComponentInputs;
    private Map<String, ArtifactDefinition> origCompInstDeploymentArtifactsCreatedOnTheInstance;
    private Map<String, ArtifactDefinition> origCompInstInformationalArtifactsCreatedOnTheInstance;
    private Map<String, List<String>> origComponentInstanceExternalRefs;
    private List<ArtifactDefinition> origComponentInstanceHeatEnvArtifacts;
    private ContainerRelationsMergeInfo containerRelationsMergeInfo;
    private List<CapabilityDefinition> origInstanceCapabilities;
    private Component origInstanceNode;
    private Component currInstanceNode;
    private String origComponentInstId;
    private List<ComponentInstanceInterface> origComponentInstanceInterfaces;

    public DataForMergeHolder() {
        origComponentInstanceInputs = new ArrayList<>();
        origComponentInstanceProperties = new ArrayList<>();
        origComponentInputs = new ArrayList<>();
        origCompInstDeploymentArtifactsCreatedOnTheInstance = new HashMap<>();
        origCompInstDeploymentArtifactsCreatedOnTheInstance = new HashMap<>();
        origInstanceCapabilities = new ArrayList<>();
        origComponentInstanceInterfaces = new ArrayList<>();
    }

    List<ArtifactDefinition> getOrigComponentInstanceHeatEnvArtifacts() {
        return origComponentInstanceHeatEnvArtifacts;
    }

    void setOrigComponentInstanceHeatEnvArtifacts(List<ArtifactDefinition> origComponentInstanceHeatEnvArtifacts) {
        this.origComponentInstanceHeatEnvArtifacts = origComponentInstanceHeatEnvArtifacts;
    }

    List<ComponentInstanceInput> getOrigComponentInstanceInputs() {
        return origComponentInstanceInputs;
    }

    void setOrigComponentInstanceInputs(List<ComponentInstanceInput> origComponentInstanceInputs) {
        Optional.ofNullable(origComponentInstanceInputs).orElse(Collections.emptyList()).stream().forEach(input -> {
            ComponentInstanceInput copyInput = new ComponentInstanceInput();
            copyInput.setType(input.getType());
            copyInput.setPath(input.getPath());
            copyInput.setRules(input.getRules());
            copyInput.setValueUniqueUid(input.getValueUniqueUid());
            copyInput.setDefaultValue(input.getDefaultValue());
            copyInput.setDescription(input.getDescription());
            copyInput.setGetInputValues(input.getGetInputValues());
            copyInput.setInputId(input.getInputId());
            copyInput.setInputPath(input.getInputPath());
            copyInput.setInputs(input.getInputs());
            copyInput.setLabel(input.getLabel());
            copyInput.setName(input.getName());
            copyInput.setParentUniqueId(input.getParentUniqueId());
            copyInput.setProperties(input.getProperties());
            copyInput.setPropertyId(input.getPropertyId());
            copyInput.setSchema(input.getSchema());
            copyInput.setStatus(input.getStatus());
            copyInput.setDefaultValue(input.getDefaultValue());
            copyInput.setValue(input.getValue());
            this.origComponentInstanceInputs.add(copyInput);
        });
    }

    List<ComponentInstanceProperty> getOrigComponentInstanceProperties() {
        return origComponentInstanceProperties;
    }

    void setOrigComponentInstanceProperties(List<ComponentInstanceProperty> origComponentInstanceProperties) {
        Optional.ofNullable(origComponentInstanceProperties).orElse(Collections.emptyList()).stream().forEach(property -> {
            ComponentInstanceProperty propertyCopy = new ComponentInstanceProperty();
            propertyCopy.setType(property.getType());
            propertyCopy.setName(property.getName());
            propertyCopy.setValue(property.getValue());
            propertyCopy.setUniqueId(property.getUniqueId());
            propertyCopy.setDefaultValue(property.getDefaultValue());
            propertyCopy.setInputId(property.getInputId());
            propertyCopy.setGetInputValues(property.getGetInputValues());
            this.origComponentInstanceProperties.add(propertyCopy);
        });
    }

    List<InputDefinition> getOrigComponentInputs() {
        return origComponentInputs;
    }

    void setOrigComponentInputs(List<InputDefinition> origComponentInputs) {
        this.origComponentInputs = origComponentInputs;
    }

    Map<String, ArtifactDefinition> getOrigComponentDeploymentArtifactsCreatedOnTheInstance(){ return this.origCompInstDeploymentArtifactsCreatedOnTheInstance;}

    Map<String, ArtifactDefinition> getOrigComponentInformationalArtifactsCreatedOnTheInstance(){ return origCompInstInformationalArtifactsCreatedOnTheInstance;}

    void setOrigComponentDeploymentArtifactsCreatedOnTheInstance(Map<String, ArtifactDefinition> origDeploymentArtifacts){
        origCompInstDeploymentArtifactsCreatedOnTheInstance = origDeploymentArtifacts;
    }

    void setOrigComponentInformationalArtifactsCreatedOnTheInstance(Map<String, ArtifactDefinition> origInformationalArtifacts){
        origCompInstInformationalArtifactsCreatedOnTheInstance = origInformationalArtifacts;
    }

    Map<String, List<String>> getOrigCompInstExternalRefs() {
        return origComponentInstanceExternalRefs;
    }

    void setOrigComponentInstanceExternalRefs(Map<String, List<String>> origComponentInstanceExternalRefs) {
        this.origComponentInstanceExternalRefs = origComponentInstanceExternalRefs;
    }

    void setVfRelationsInfo(ContainerRelationsMergeInfo containerRelationsMergeInfo) {
        this.containerRelationsMergeInfo = containerRelationsMergeInfo;
    }

    ContainerRelationsMergeInfo getContainerRelationsMergeInfo() {
        return containerRelationsMergeInfo;
    }

    List<CapabilityDefinition> getOrigInstanceCapabilities() {
        return origInstanceCapabilities;
    }

    void setOrigInstanceCapabilities(List<CapabilityDefinition> origInstanceCapabilities) {
        this.origInstanceCapabilities = origInstanceCapabilities;
    }

    Component getOrigInstanceNode() {
        return origInstanceNode;
    }

    void setOrigInstanceNode(Component origInstanceNode) {
        this.origInstanceNode = origInstanceNode;
    }

    Component getCurrInstanceNode() {
        return currInstanceNode;
    }

    public void setCurrInstanceNode(Component currInstanceNode) {
        this.currInstanceNode = currInstanceNode;
    }

    public String getOrigComponentInstId() {
        return origComponentInstId;
    }

    public void setOrigComponentInstId(String origComponentInstId) {
        this.origComponentInstId = origComponentInstId;
    }

    public List<ComponentInstanceInterface> getOrigComponentInstanceInterfaces() {
        return origComponentInstanceInterfaces;
    }

    public void setOrigComponentInstanceInterfaces(List<ComponentInstanceInterface> origComponentInstanceInterfaces) {
        this.origComponentInstanceInterfaces = origComponentInstanceInterfaces;
    }
}
