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
    if (getClass() != obj.getClass())
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
