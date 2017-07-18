package org.openecomp.sdc.vendorsoftwareproduct.types.composition;


import java.util.List;

public class DeploymentFlavor implements CompositionDataEntity {
    private String model;
    private String description;
    private String featureGroupId;
    private List<ComponentComputeAssociation> componentComputeAssociations;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeatureGroupId() {
        return featureGroupId;
    }

    public void setFeatureGroupId(String featureGroupId) {
        this.featureGroupId = featureGroupId;
    }

    public List<ComponentComputeAssociation> getComponentComputeAssociations() {
        return componentComputeAssociations;
    }

    public void setComponentComputeAssociations(List<ComponentComputeAssociation> componentComputeAssociations) {
        this.componentComputeAssociations = componentComputeAssociations;
    }

}
