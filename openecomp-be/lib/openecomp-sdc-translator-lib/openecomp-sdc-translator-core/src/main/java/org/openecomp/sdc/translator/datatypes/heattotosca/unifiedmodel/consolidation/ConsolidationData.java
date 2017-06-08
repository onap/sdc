package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Consolidation data.
 */
public class ConsolidationData {

  //Compute consolidation data
  private ComputeConsolidationData computeConsolidationData;

  //Port Consolidation data
  private PortConsolidationData portConsolidationData;

  //Key - Service Template file name
  //value - List of the abstract/substitute node template id
  private Map<String, List<String>> substituteNodeTemplates; // todo - remove this one and use
  // nestedConsolidationData instead

  //Nested Consolidation data
  private NestedConsolidationData nestedConsolidationData;


  /**
   * Instantiates a new Consolidation data.
   */
  public ConsolidationData() {
    computeConsolidationData = new ComputeConsolidationData();
    portConsolidationData = new PortConsolidationData();
    nestedConsolidationData = new NestedConsolidationData();
    substituteNodeTemplates = new HashMap<>();
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
   * Gets substitute node templates.
   *
   * @return the substitute node templates
   */
  public Map<String, List<String>> getSubstituteNodeTemplates() {
    return substituteNodeTemplates;
  }

  /**
   * Sets substitute node templates.
   *
   * @param substituteNodeTemplates the substitute node templates
   */
  public void setSubstituteNodeTemplates(Map<String, List<String>> substituteNodeTemplates) {
    this.substituteNodeTemplates = substituteNodeTemplates;
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
