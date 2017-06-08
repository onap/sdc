package org.openecomp.sdcrests.vendorsoftwareproducts.types;


import java.util.List;

public class ComponentDependencyModelRequestDto {

  private List<ComponentDependencyModel> componentDependencyModels;

  public List<ComponentDependencyModel> getComponentDependencyModels() {
    return componentDependencyModels;
  }

  public void setComponentDependencyModels(
      List<ComponentDependencyModel> componentDependencyModels) {
    this.componentDependencyModels = componentDependencyModels;
  }
}
