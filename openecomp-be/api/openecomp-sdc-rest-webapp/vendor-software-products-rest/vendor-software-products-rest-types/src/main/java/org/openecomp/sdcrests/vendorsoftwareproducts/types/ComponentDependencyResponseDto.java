package org.openecomp.sdcrests.vendorsoftwareproducts.types;
import lombok.Data;

@Data
public class ComponentDependencyResponseDto {
  private String sourceId;
  private String targetId;
  private String relationType;
  private String id;
}
