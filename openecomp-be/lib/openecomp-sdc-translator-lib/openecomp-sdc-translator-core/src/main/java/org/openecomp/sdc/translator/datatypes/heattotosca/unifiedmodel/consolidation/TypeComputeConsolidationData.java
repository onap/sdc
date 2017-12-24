package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Type compute consolidation data.
 */
public class TypeComputeConsolidationData {

  //key - compute node template id
  private Map<String, ComputeTemplateConsolidationData> computeTemplateConsolidationData;

  /**
   * Instantiates a new Type compute consolidation data.
   */
  public TypeComputeConsolidationData() {
    computeTemplateConsolidationData = new HashMap<>();
  }

  /**
   * Gets all compute node template ids.
   *
   * @return the all compute node template ids
   */
  public Collection<String> getAllComputeNodeTemplateIds() {
    return computeTemplateConsolidationData.keySet();
  }

  public Collection<ComputeTemplateConsolidationData> getAllComputeTemplateConsolidationData(){
    return computeTemplateConsolidationData.values();
  }

  /**
   * Gets compute template consolidation data.
   *
   * @param computeNodeTemplateId the compute node template id
   * @return the compute template consolidation data
   */
  public ComputeTemplateConsolidationData getComputeTemplateConsolidationData(
      String computeNodeTemplateId) {
    return computeTemplateConsolidationData.get(computeNodeTemplateId);
  }

  /**
   * Sets compute template consolidation data.
   *
   * @param computeNodeTemplateId            the compute node template id
   * @param computeTemplateConsolidationData the compute template consolidation data
   */
  public void setComputeTemplateConsolidationData(String computeNodeTemplateId,
                                                  ComputeTemplateConsolidationData
                                                      computeTemplateConsolidationData) {
    this.computeTemplateConsolidationData.put(computeNodeTemplateId,
        computeTemplateConsolidationData);
  }
}
