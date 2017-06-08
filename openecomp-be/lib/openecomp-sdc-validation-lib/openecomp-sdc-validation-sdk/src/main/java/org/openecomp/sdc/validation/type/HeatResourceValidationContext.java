package org.openecomp.sdc.validation.type;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.validation.ValidationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by TALIO on 2/23/2017.
 */
public class HeatResourceValidationContext implements ValidationContext {

  private HeatOrchestrationTemplate heatOrchestrationTemplate;
  // key - resource type, value - map with key = resource id and
  // value = map with key = pointing / pointed resource type and
  // value = pointing / pointed resource id
  private Map<String, Map<String, Map<String, List<String>>>> fileLevelResourceDependencies;
  private String envFileName;


  public HeatResourceValidationContext(
      HeatOrchestrationTemplate heatOrchestrationTemplate,
      Map<String, Map<String, Map<String, List<String>>>> fileLevelResourceDependencies,
      String envFileName) {

    this.heatOrchestrationTemplate = heatOrchestrationTemplate;
    this.fileLevelResourceDependencies = fileLevelResourceDependencies;
//    this.zipLevelResourceDependencies = zipLevelResourceDependencies;
    this.envFileName = envFileName;
  }

  public HeatOrchestrationTemplate getHeatOrchestrationTemplate() {
    return heatOrchestrationTemplate;
  }

  public Map<String, Map<String, Map<String, List<String>>>> getFileLevelResourceDependencies() {
    return fileLevelResourceDependencies;
  }

  public String getEnvFileName() {
    return envFileName;
  }
}
