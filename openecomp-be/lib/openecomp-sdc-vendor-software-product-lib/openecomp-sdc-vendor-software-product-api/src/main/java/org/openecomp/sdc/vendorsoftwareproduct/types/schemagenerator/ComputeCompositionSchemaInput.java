package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;

public class ComputeCompositionSchemaInput implements SchemaTemplateInput {

  private boolean manual;
  private ComputeData compute;

  public boolean isManual() {
    return manual;
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  public ComputeData getCompute() {
    return compute;
  }

  public void setCompute(ComputeData compute) {
    this.compute = compute;
  }
}
