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
}
