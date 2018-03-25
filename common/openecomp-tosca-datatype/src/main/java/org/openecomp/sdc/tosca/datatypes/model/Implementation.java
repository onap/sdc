package org.openecomp.sdc.tosca.datatypes.model;

import java.util.List;


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

    if (!getPrimary().equals(that.getPrimary())) {
      return false;
    }
    return getDependencies() != null ? getDependencies().equals(that.getDependencies())
        : that.getDependencies() == null;
  }

  @Override
  public int hashCode() {
    int result = getPrimary().hashCode();
    result = 31 * result + (getDependencies() != null ? getDependencies().hashCode() : 0);
    return result;
  }
}
