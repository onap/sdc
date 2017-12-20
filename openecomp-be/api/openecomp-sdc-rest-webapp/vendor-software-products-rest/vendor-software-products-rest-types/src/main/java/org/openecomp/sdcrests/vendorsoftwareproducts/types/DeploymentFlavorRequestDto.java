package org.openecomp.sdcrests.vendorsoftwareproducts.types;


import org.hibernate.validator.constraints.NotBlank;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;

import javax.validation.constraints.Size;
import java.util.List;

public class DeploymentFlavorRequestDto {
    @NotBlank(message = "is mandatory and should not be empty")
    @Size(min = 0, max = 30,message = "length should not exceed 30 characters.")
    private String model;
    @Size(min = 0, max = 300,message = "length should not exceed 300 characters.")
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
