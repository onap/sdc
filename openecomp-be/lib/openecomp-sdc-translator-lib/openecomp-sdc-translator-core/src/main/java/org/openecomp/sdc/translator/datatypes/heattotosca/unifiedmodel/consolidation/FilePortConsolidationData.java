package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type File port consolidation data.
 */
public class FilePortConsolidationData {

  //key - port node template id
  private Map<String, PortTemplateConsolidationData> portTemplateConsolidationData;

  public FilePortConsolidationData() {
    this.portTemplateConsolidationData = new HashMap<>();
  }

  /**
   * Gets all port node template ids.
   *
   * @return the all port node template ids
   */
  public Set<String> getAllPortNodeTemplateIds() {
    return portTemplateConsolidationData.keySet();
  }

  public Collection<PortTemplateConsolidationData> getAllPortConsolidationData() {
    return portTemplateConsolidationData.values();
  }

  /**
   * Gets port template consolidation data.
   *
   * @param portNodeTemplateId the port node template id
   * @return the port template consolidation data
   */
  public PortTemplateConsolidationData getPortTemplateConsolidationData(String portNodeTemplateId) {
    return portTemplateConsolidationData.get(portNodeTemplateId);
  }

  /**
   * Sets port template consolidation data.
   *
   * @param portNodeTemplateId                the port node template id
   * @param portTemplateConsolidationData the port template consolidation data
   */
  public void setPortTemplateConsolidationData(String portNodeTemplateId,
                                               PortTemplateConsolidationData
                                                   portTemplateConsolidationData) {
    this.portTemplateConsolidationData.put(portNodeTemplateId, portTemplateConsolidationData);
  }
}
