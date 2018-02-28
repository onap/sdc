package org.openecomp.sdcrests.vendorsoftwareproducts.types;

import lombok.Data;

@Data
public class ComponentDependencyModel {

  private String sourceId;
  private String targetId;
  private String relationType;
}
