package org.openecomp.sdc.tosca.datatypes.model;

import java.util.List;
import java.util.Objects;


public class Implementation {

  private String primary;
  private List<String> dependencies;

  public String getPrimary() {
    return primary;
  }

  public void setPrimary(String primary) {
    this.primary = primary;
  }

  public List<String> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<String> dependencies) {
    this.dependencies = dependencies;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Implementation)) {
      return false;
    }
    Implementation that = (Implementation) o;
    return Objects.equals(primary, that.primary) &&
        Objects.equals(dependencies, that.dependencies);
  }

  @Override
  public int hashCode() {

    return Objects.hash(primary, dependencies);
  }
}
