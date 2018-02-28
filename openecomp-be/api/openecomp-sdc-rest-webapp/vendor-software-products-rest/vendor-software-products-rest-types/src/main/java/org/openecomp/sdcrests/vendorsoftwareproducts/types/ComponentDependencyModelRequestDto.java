package org.openecomp.sdcrests.vendorsoftwareproducts.types;


import java.util.List;
import lombok.Data;

@Data
public class ComponentDependencyModelRequestDto {

  private List<ComponentDependencyModel> componentDependencyModels;

  public List<ComponentDependencyModel> getComponentDependencyModels() {
    return componentDependencyModels;
  }
}
