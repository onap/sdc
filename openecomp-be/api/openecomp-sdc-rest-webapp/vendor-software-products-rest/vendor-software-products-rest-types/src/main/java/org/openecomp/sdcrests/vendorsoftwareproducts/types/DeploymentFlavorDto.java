package org.openecomp.sdcrests.vendorsoftwareproducts.types;
import lombok.*;

@Data
public class DeploymentFlavorDto extends DeploymentFlavorRequestDto implements CompositionDataEntityDto {
    private String id;
}
