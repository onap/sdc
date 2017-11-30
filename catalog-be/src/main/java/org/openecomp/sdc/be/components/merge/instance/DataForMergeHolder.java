package org.openecomp.sdc.be.components.merge.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;

/**
 * Created by chaya on 9/7/2017.
 */
public class DataForMergeHolder {

    private List<ComponentInstanceInput> origComponentInstanceInputs;
    private List<ComponentInstanceProperty> origComponentInstanceProperties;
    private List<InputDefinition> origComponentInputs;
    private Map<String, ArtifactDefinition> origCompInstDeploymentArtifactsCreatedOnTheInstance;
    private Map<String, ArtifactDefinition> origCompInstInformationalArtifactsCreatedOnTheInstance;
    private List<ArtifactDefinition> origComponentInstanceHeatEnvArtifacts;

    public DataForMergeHolder() {
        origComponentInstanceInputs = new ArrayList<>();
        origComponentInstanceProperties = new ArrayList<>();
        origComponentInputs = new ArrayList<>();
        origCompInstDeploymentArtifactsCreatedOnTheInstance = new HashMap<>();
        origCompInstDeploymentArtifactsCreatedOnTheInstance = new HashMap<>();

    }

    public List<ArtifactDefinition> getOrigComponentInstanceHeatEnvArtifacts() {
        return origComponentInstanceHeatEnvArtifacts;
    }

    public void setOrigComponentInstanceHeatEnvArtifacts(List<ArtifactDefinition> origComponentInstanceHeatEnvArtifacts) {
        this.origComponentInstanceHeatEnvArtifacts = origComponentInstanceHeatEnvArtifacts;
    }

    public List<ComponentInstanceInput> getOrigComponentInstanceInputs() {
        return origComponentInstanceInputs;
    }

    public void setOrigComponentInstanceInputs(List<ComponentInstanceInput> origComponentInstanceInputs) {
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

    public List<ComponentInstanceProperty> getOrigComponentInstanceProperties() {
        return origComponentInstanceProperties;
    }

    public void setOrigComponentInstanceProperties(List<ComponentInstanceProperty> origComponentInstanceProperties) {
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

    public List<InputDefinition> getOrigComponentInputs() {
        return origComponentInputs;
    }

    public void setOrigComponentInputs(List<InputDefinition> origComponentInputs) {
        this.origComponentInputs = origComponentInputs;
    }

    public Map<String, ArtifactDefinition> getOrigComponentDeploymentArtifactsCreatedOnTheInstance(){ return this.origCompInstDeploymentArtifactsCreatedOnTheInstance;}

    public Map<String, ArtifactDefinition> getOrigComponentInformationalArtifactsCreatedOnTheInstance(){ return origCompInstInformationalArtifactsCreatedOnTheInstance;}

    public void setOrigComponentDeploymentArtifactsCreatedOnTheInstance(Map<String, ArtifactDefinition> origDeploymentArtifacts){
        origCompInstDeploymentArtifactsCreatedOnTheInstance = origDeploymentArtifacts;
    }

    public void setOrigComponentInformationalArtifactsCreatedOnTheInstance(Map<String, ArtifactDefinition> origInformationalArtifacts){
        origCompInstInformationalArtifactsCreatedOnTheInstance = origInformationalArtifacts;
    }



}
