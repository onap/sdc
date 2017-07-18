package org.openecomp.sdc.vendorsoftwareproduct.types.composition;

public class ComputeData implements CompositionDataEntity {
  private String name;
  private String description;

  public ComputeData(){}

  public ComputeData(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof ComputeData)) {
      return false;
    }

    ComputeData that = (ComputeData) object;

    if (!name.equals(that.name)) {
      return false;
    }
    return description != null ? description.equals(that.description): that.description == null;
  }
}
