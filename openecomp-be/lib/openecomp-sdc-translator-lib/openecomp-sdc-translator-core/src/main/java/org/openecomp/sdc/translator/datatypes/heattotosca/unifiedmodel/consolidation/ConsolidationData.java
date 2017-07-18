package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

/**
 * The type Consolidation data.
 */
public class ConsolidationData {

  //Compute consolidation data
  private ComputeConsolidationData computeConsolidationData;

  //Port Consolidation data
  private PortConsolidationData portConsolidationData;

  //Nested Consolidation data
  private NestedConsolidationData nestedConsolidationData;


  /**
   * Instantiates a new Consolidation data.
   */
  public ConsolidationData() {
    computeConsolidationData = new ComputeConsolidationData();
    portConsolidationData = new PortConsolidationData();
    nestedConsolidationData = new NestedConsolidationData();
  }

  /**
   * Gets compute consolidation data.
   *
   * @return the compute consolidation data
   */
  public ComputeConsolidationData getComputeConsolidationData() {
    return computeConsolidationData;
  }

  /**
   * Sets compute consolidation data.
   *
   * @param computeConsolidationData the compute consolidation data
   */
  public void setComputeConsolidationData(ComputeConsolidationData computeConsolidationData) {
    this.computeConsolidationData = computeConsolidationData;
  }

  /**
   * Gets port consolidation data.
   *
   * @return the port consolidation data
   */
  public PortConsolidationData getPortConsolidationData() {
    return portConsolidationData;
  }

  /**
   * Sets port consolidation data.
   *
   * @param portConsolidationData the port consolidation data
   */
  public void setPortConsolidationData(
      PortConsolidationData portConsolidationData) {
    this.portConsolidationData = portConsolidationData;
  }

  /**
   * Gets nested consolidation data.
   *
   * @return the nested consolidation data
   */
  public NestedConsolidationData getNestedConsolidationData() {
    return nestedConsolidationData;
  }

  /**
   * Sets nested consolidation data.
   *
   * @param nestedConsolidationData the nested consolidation data
   */
  public void setNestedConsolidationData(NestedConsolidationData nestedConsolidationData) {
    this.nestedConsolidationData = nestedConsolidationData;
  }
}
