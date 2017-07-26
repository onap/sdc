package org.openecomp.sdc.generator.datatypes.tosca;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;

import java.util.List;
import java.util.Map;

public class VspModelInfo {
  private String releaseVendor;
  //Map of component id and name
  private Map<String, String> components;
  //Map of part number and deployment flavor model
  private Map<String, DeploymentFlavorModel> allowedFlavors;
  //Map of component id and images
  private Map<String, List<MultiFlavorVfcImage>> multiFlavorVfcImages;
  //Map of component and ports (NICs)
  private Map<String, List<Nic>> nics;

  public String getReleaseVendor() {
    return releaseVendor;
  }

  public void setReleaseVendor(String releaseVendor) {
    this.releaseVendor = releaseVendor;
  }

  public Map<String, String> getComponents() {
    return components;
  }

  public void setComponents(Map<String, String> components) {
    this.components = components;
  }

  public Map<String, DeploymentFlavorModel> getAllowedFlavors() {
    return allowedFlavors;
  }

  public void setAllowedFlavors(Map<String, DeploymentFlavorModel> allowedFlavors) {
    this.allowedFlavors = allowedFlavors;
  }

  public Map<String, List<MultiFlavorVfcImage>> getMultiFlavorVfcImages() {
    return multiFlavorVfcImages;
  }

  public void setMultiFlavorVfcImages(Map<String, List<MultiFlavorVfcImage>> multiFlavorVfcImages) {
    this.multiFlavorVfcImages = multiFlavorVfcImages;
  }

  public Map<String, List<Nic>> getNics() {
    return nics;
  }

  public void setNics(Map<String, List<Nic>> nics) {
    this.nics = nics;
  }

  @Override
  public int hashCode() {
    int result = releaseVendor != null ? releaseVendor.hashCode() : 0;
    result = 31 * result + (components != null ? components.hashCode() : 0);
    result = 31 * result + (allowedFlavors != null ? allowedFlavors.hashCode() : 0);
    result = 31 * result + (multiFlavorVfcImages != null ? multiFlavorVfcImages.hashCode() : 0);
    result = 31 * result + (nics != null ? nics.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "VspModelInfo{"
       + "releaseVendor='" + releaseVendor + '\''
       + ", components=" + components
       + ", allowedFlavors=" + allowedFlavors
       + ", multiFlavorVfcImages=" + multiFlavorVfcImages
       + ", nics=" + nics
       + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    VspModelInfo other = (VspModelInfo) obj;
    if (this.releaseVendor == null) {
      if (other.releaseVendor != null) {
        return false;
      }
    } else if (!releaseVendor.equals(other.releaseVendor)) {
      return false;
    }
    if (this.components == null) {
      if (other.components != null) {
        return false;
      }
    } else if (!components.equals(other.components)) {
      return false;
    }
    if (this.allowedFlavors == null) {
      if (other.allowedFlavors != null) {
        return false;
      }
    } else if (!allowedFlavors.equals(other.allowedFlavors)) {
      return false;
    }
    if (this.multiFlavorVfcImages == null) {
      if (other.multiFlavorVfcImages != null) {
        return false;
      }
    } else if (!multiFlavorVfcImages.equals(other.multiFlavorVfcImages)) {
      return false;
    }
    if (this.multiFlavorVfcImages == null) {
      if (other.multiFlavorVfcImages != null) {
        return false;
      }
    } else if (!multiFlavorVfcImages.equals(other.multiFlavorVfcImages)) {
      return false;
    }
    if (this.nics == null) {
      if (other.nics != null) {
        return false;
      }
    } else if (!nics.equals(other.nics)) {
      return false;
    }
    return true;
  }
}
