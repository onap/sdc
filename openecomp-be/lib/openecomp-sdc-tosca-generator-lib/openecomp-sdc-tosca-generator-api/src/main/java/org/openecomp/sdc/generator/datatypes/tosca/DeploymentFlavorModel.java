package org.openecomp.sdc.generator.datatypes.tosca;

@SuppressWarnings("CheckStyle")
public class DeploymentFlavorModel {

  private String sp_part_number;
  private VendorInfo vendor_info;
  private ComputeFlavor compute_flavor;
  private LicenseFlavor license_flavor;

  public String getSp_part_number() {
    return sp_part_number;
  }

  public void setSp_part_number(String sp_part_number) {
    this.sp_part_number = sp_part_number;
  }

  public VendorInfo getVendor_info() {
    return vendor_info;
  }

  public void setVendor_info(VendorInfo vendor_info) {
    this.vendor_info = vendor_info;
  }

  public ComputeFlavor getCompute_flavor() {
    return compute_flavor;
  }

  public void setCompute_flavor(ComputeFlavor compute_flavor) {
    this.compute_flavor = compute_flavor;
  }

  public LicenseFlavor getLicense_flavor() {
    return license_flavor;
  }

  public void setLicense_flavor(LicenseFlavor license_flavor) {
    this.license_flavor = license_flavor;
  }

  @Override
  public String toString() {
    return "DeploymentFlavorModel{" + "sp_part_number='" + sp_part_number + '\''
        + ", vendor_info=" + vendor_info
        + ", compute_flavor=" + compute_flavor
        + ", license_flavor=" + license_flavor
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    DeploymentFlavorModel other = (DeploymentFlavorModel) obj;
    if (this.sp_part_number == null) {
      if (other.sp_part_number != null)
        return false;
    } else if (!sp_part_number.equals(other.sp_part_number))
      return false;
    if (this.vendor_info == null) {
      if (other.vendor_info != null)
        return false;
    } else if (!vendor_info.equals(other.vendor_info))
      return false;
    if (this.compute_flavor == null) {
      if (other.compute_flavor != null)
        return false;
    } else if (!compute_flavor.equals(other.compute_flavor))
      return false;
    if (this.license_flavor == null) {
      if (other.license_flavor != null)
        return false;
    } else if (!license_flavor.equals(other.license_flavor))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = sp_part_number != null ? sp_part_number.hashCode() : 0;
    result = 31 * result + (vendor_info != null ? vendor_info.hashCode() : 0);
    result = 31 * result + (compute_flavor != null ? compute_flavor.hashCode() : 0);
    result = 31 * result + (license_flavor != null ? license_flavor.hashCode() : 0);
    return result;
  }
}
