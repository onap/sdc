package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type File nested node consolidation data.
 */
public class FileNestedConsolidationData {

  //key - nested node template id
  private Map<String, NestedTemplateConsolidationData> nestedTemplateConsolidationData;

  public FileNestedConsolidationData() {
    this.nestedTemplateConsolidationData = new HashMap<>();
  }

  /**
   * Gets all nested node template ids.
   *
   * @return the all nested node template ids
   */
  public Set<String> getAllNestedNodeTemplateIds() {
    return nestedTemplateConsolidationData.keySet();
  }

  public Collection<NestedTemplateConsolidationData> getAllNestedConsolidationData() {
    return nestedTemplateConsolidationData.values();
  }

  /**
   * Gets nested template consolidation data.
   *
   * @param nestedNodeTemplateId the nested node template id
   * @return the nested template consolidation data
   */
  public NestedTemplateConsolidationData getNestedTemplateConsolidationData(
      String nestedNodeTemplateId) {
    return nestedTemplateConsolidationData.get(nestedNodeTemplateId);
  }

  /**
   * Sets nested template consolidation data.
   *
   * @param nestedNodeTemplateId            the nested node template id
   * @param nestedTemplateConsolidationData the nested template consolidation data
   */
  public void setNestedTemplateConsolidationData(
      String nestedNodeTemplateId,
      NestedTemplateConsolidationData nestedTemplateConsolidationData) {
    this.nestedTemplateConsolidationData.put(nestedNodeTemplateId, nestedTemplateConsolidationData);
  }
}
