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
public class ComputeFlavor {

  private int num_cpus;
  private String disk_size;
  private String mem_size;

  public int getNum_cpus() {
    return num_cpus;
  }

  public void setNum_cpus(int num_cpus) {
    this.num_cpus = num_cpus;
  }

  public String getDisk_size() {
    return disk_size;
  }

  public void setDisk_size(String disk_size) {
    this.disk_size = disk_size;
  }

  public String getMem_size() {
    return mem_size;
  }

  public void setMem_size(String mem_size) {
    this.mem_size = mem_size;
  }

  @Override
  public String toString() {
    return "ComputeFlavor{ num_cpus=" + num_cpus + ", disk_size= " + disk_size
        + ", mem_size=" + mem_size + "}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    ComputeFlavor other = (ComputeFlavor) obj;

      if (num_cpus != other.num_cpus)
        return false;
      if (this.disk_size == null) {
        if (other.disk_size != null)
          return false;
      } else if (!disk_size.equals(other.disk_size))
        return false;
      if (this.mem_size == null) {
        if (other.mem_size != null)
          return false;
      } else if (!mem_size.equals(other.mem_size))
        return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = num_cpus;
    result = 31 * result + (disk_size != null ? disk_size.hashCode() : 0);
    result = 31 * result + (mem_size != null ? mem_size.hashCode() : 0);
    return result;
  }
}
