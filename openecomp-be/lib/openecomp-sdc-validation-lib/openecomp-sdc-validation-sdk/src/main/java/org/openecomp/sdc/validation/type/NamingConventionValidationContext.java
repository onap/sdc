package org.openecomp.sdc.validation.type;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.validation.ValidationContext;

/**
 * Created by TALIO on 2/23/2017.
 */
public class NamingConventionValidationContext implements ValidationContext {

  private HeatOrchestrationTemplate heatOrchestrationTemplate;
  private String envFileName;

  public NamingConventionValidationContext(
      HeatOrchestrationTemplate heatOrchestrationTemplate,
      String envFileName) {
    this.heatOrchestrationTemplate = heatOrchestrationTemplate;
    this.envFileName = envFileName;
  }

  public HeatOrchestrationTemplate getHeatOrchestrationTemplate() {
    return heatOrchestrationTemplate;
  }

  public String getEnvFileName() {
    return envFileName;
  }
}
