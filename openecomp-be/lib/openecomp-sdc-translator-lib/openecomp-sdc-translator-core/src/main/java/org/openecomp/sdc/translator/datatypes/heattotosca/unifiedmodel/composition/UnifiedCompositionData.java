package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition;

import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Unified composition data.
 */
public class UnifiedCompositionData {
  /**
   * The Compute template consolidation data.
   */
  private ComputeTemplateConsolidationData computeTemplateConsolidationData;
  /**
   * The Port template consolidation data list.
   */
  private List<PortTemplateConsolidationData> portTemplateConsolidationDataList;


  private NestedTemplateConsolidationData nestedTemplateConsolidationData;

  /**
   * Gets compute template consolidation data.
   *
   * @return the compute template consolidation data
   */
  public ComputeTemplateConsolidationData getComputeTemplateConsolidationData() {
    return computeTemplateConsolidationData;
  }

  /**
   * Sets compute template consolidation data.
   *
   * @param computeTemplateConsolidationData the compute template consolidation data
   */
  public void setComputeTemplateConsolidationData(
      ComputeTemplateConsolidationData computeTemplateConsolidationData) {
    this.computeTemplateConsolidationData = computeTemplateConsolidationData;
  }

  /**
   * Gets port template consolidation data list.
   *
   * @return the port template consolidation data list
   */
  public List<PortTemplateConsolidationData> getPortTemplateConsolidationDataList() {
    return portTemplateConsolidationDataList;
  }

  /**
   * Sets port template consolidation data list.
   *
   * @param portTemplateConsolidationDataList the port template consolidation data list
   */
  public void setPortTemplateConsolidationDataList(
      List<PortTemplateConsolidationData> portTemplateConsolidationDataList) {
    this.portTemplateConsolidationDataList = portTemplateConsolidationDataList;
  }

  /**
   * Add port consolidation data.
   *
   * @param portTemplateConsolidationData the port consolidation data
   */
  public void addPortTemplateConsolidationData(
      PortTemplateConsolidationData portTemplateConsolidationData) {
    if (this.portTemplateConsolidationDataList == null) {
      this.portTemplateConsolidationDataList = new ArrayList<>();
    }
    this.portTemplateConsolidationDataList.add(portTemplateConsolidationData);
  }

  /**
   * Gets nested template consolidation data.
   *
   * @return the nested template consolidation data
   */
  public NestedTemplateConsolidationData getNestedTemplateConsolidationData() {
    return nestedTemplateConsolidationData;
  }

  /**
   * Sets nested template consolidation data.
   *
   * @param nestedTemplateConsolidationData the nested template consolidation data
   */
  public void setNestedTemplateConsolidationData(
      NestedTemplateConsolidationData nestedTemplateConsolidationData) {
    this.nestedTemplateConsolidationData = nestedTemplateConsolidationData;
  }
}
