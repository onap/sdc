package org.openecomp.sdcrests.vendorsoftwareproducts.types;


import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class DeploymentFlavorRequestDto {
    @NotBlank(message = "is mandatory and should not be empty")
    @Size(min = 0, max = 30,message = "length should not exceed 30 characters.")
    private String model;
    @Size(min = 0, max = 300,message = "length should not exceed 300 characters.")
    private String description;
    private String featureGroupId;
    private List<ComponentComputeAssociation> componentComputeAssociations;

}
