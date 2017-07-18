package org.openecomp.sdc.generator.datatypes.tosca;

@SuppressWarnings("CheckStyle")
public class LicenseFlavor {

  String feature_group_uuid;

  public String getFeature_group_uuid() {
    return feature_group_uuid;
  }

  public void setFeature_group_uuid(String feature_group_uuid) {
    this.feature_group_uuid = feature_group_uuid;
  }

  @Override
  public String toString() {
    return "LicenseFlavor{"
        + "feature_group_uuid='" + feature_group_uuid + '\''
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    LicenseFlavor other = (LicenseFlavor) obj;
    if (this.feature_group_uuid == null) {
      if (other.feature_group_uuid != null)
        return false;
    } else if (!feature_group_uuid.equals(other.feature_group_uuid))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return feature_group_uuid != null ? feature_group_uuid.hashCode() : 0;
  }
}
