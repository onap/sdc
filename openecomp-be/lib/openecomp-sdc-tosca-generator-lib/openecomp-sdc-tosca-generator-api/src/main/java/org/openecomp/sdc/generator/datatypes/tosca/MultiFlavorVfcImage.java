/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

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
    if (obj == null || getClass() != obj.getClass())
      return false;
    MultiFlavorVfcImage other = (MultiFlavorVfcImage) obj;

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
