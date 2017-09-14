package org.openecomp.sdc.generator.datatypes.tosca;

@SuppressWarnings("CheckStyle")
public class MultiFlavorVfcImage {

  private String file_name;
  private String file_hash;
  private String file_hash_type;
  private String software_version;

  public String getFile_name() {
    return file_name;
  }

  public void setFile_name(String file_name) {
    this.file_name = file_name;
  }

  public String getFile_hash() {
    return file_hash;
  }

  public void setFile_hash(String file_hash) {
    this.file_hash = file_hash;
  }

  public String getFile_hash_type() {
    return file_hash_type;
  }

  public void setFile_hash_type(String file_hash_type) {
    this.file_hash_type = file_hash_type;
  }

  public String getSoftware_version() {
    return software_version;
  }

  public void setSoftware_version(String software_version) {
    this.software_version = software_version;
  }

  @Override
  public String toString() {
    return "MultiFlavorVfcImage{"
        + "file_name='" + file_name + '\''
        + ", file_hash='" + file_hash + '\''
        + ", file_hash_type='" + file_hash_type + '\''
        + ", software_version='" + software_version + '\''
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj != null && getClass() != obj.getClass())
      return false;
    MultiFlavorVfcImage other = (MultiFlavorVfcImage) obj;
    if (other != null) {
      if (this.file_name == null) {
        if (other.file_name != null)
          return false;
      } else if (!file_name.equals(other.file_name))
        return false;
      if (this.file_hash == null) {
        if (other.file_hash != null)
          return false;
      } else if (!file_hash.equals(other.file_hash))
        return false;
      if (this.file_hash_type == null) {
        if (other.file_hash_type != null)
          return false;
      } else if (!file_hash_type.equals(other.file_hash_type))
        return false;
      if (this.software_version == null) {
        if (other.software_version != null)
          return false;
      } else if (!software_version.equals(other.software_version))
        return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = file_name != null ? file_name.hashCode() : 0;
    result = 31 * result + (file_hash != null ? file_hash.hashCode() : 0);
    result = 31 * result + (file_hash_type != null ? file_hash_type.hashCode() : 0);
    result = 31 * result + (software_version != null ? software_version.hashCode() : 0);
    return result;
  }
}
