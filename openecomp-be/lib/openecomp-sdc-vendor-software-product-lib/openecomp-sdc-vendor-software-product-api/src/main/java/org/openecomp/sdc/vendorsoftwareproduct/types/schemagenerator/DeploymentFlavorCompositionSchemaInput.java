package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;


import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;

import java.util.Collection;

public class DeploymentFlavorCompositionSchemaInput implements SchemaTemplateInput {

  private boolean manual;
  private DeploymentFlavor deploymentFlavor;
  private Collection<String> featureGroupIds;

  public boolean isManual() {
    return manual;
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  public DeploymentFlavor getDeploymentFlavor() {
    return deploymentFlavor;
  }

  public void setDeploymentFlavor(
      DeploymentFlavor deploymentFlavor) {
    this.deploymentFlavor = deploymentFlavor;
  }

  public Collection<String> getFeatureGroupIds() {
    return featureGroupIds;
  }

  public void setFeatureGroupIds(Collection<String> featureGroupIds) {
    this.featureGroupIds = featureGroupIds;
  }

}
