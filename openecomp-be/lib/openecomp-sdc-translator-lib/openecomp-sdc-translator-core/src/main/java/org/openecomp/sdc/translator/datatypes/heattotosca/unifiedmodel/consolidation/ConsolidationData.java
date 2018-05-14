package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;

import java.util.Optional;

/**
 * The type Consolidation data.
 */
public class ConsolidationData {

  //Compute consolidation data and handler
  private ComputeConsolidationData computeConsolidationData;
  private ComputeConsolidationDataHandler computeConsolidationDataHandler;

  //Port Consolidation data and handler
  private PortConsolidationData portConsolidationData;
  private PortConsolidationDataHandler portConsolidationDataHandler;
  private SubInterfaceConsolidationDataHandler subInterfaceConsolidationDataHandler;

  //Nested Consolidation data and handler
  private NestedConsolidationData nestedConsolidationData;
  private NestedConsolidationDataHandler nestedConsolidationDataHandler;



  /**
   * Instantiates a new Consolidation data.
   */
  public ConsolidationData() {
    computeConsolidationData = new ComputeConsolidationData();
    portConsolidationData = new PortConsolidationData();
    nestedConsolidationData = new NestedConsolidationData();

    computeConsolidationDataHandler = new ComputeConsolidationDataHandler(this.getComputeConsolidationData());
    portConsolidationDataHandler = new PortConsolidationDataHandler(this.getPortConsolidationData());
    nestedConsolidationDataHandler = new NestedConsolidationDataHandler(this.getNestedConsolidationData());
    subInterfaceConsolidationDataHandler = new SubInterfaceConsolidationDataHandler(this.getPortConsolidationData());
  }

  public Optional<IConsolidationDataHandler> getConsolidationDataHelper(ConsolidationEntityType type){

    switch (type) {
      case COMPUTE:
        return Optional.of(computeConsolidationDataHandler);
      case PORT:
        return Optional.of(portConsolidationDataHandler);
      case SUB_INTERFACE:
        return Optional.of(subInterfaceConsolidationDataHandler);
      case NESTED:
      case VFC_NESTED:
        return Optional.of(nestedConsolidationDataHandler);
      default:
        return Optional.empty();
    }

  }

  public ComputeConsolidationDataHandler getComputeConsolidationDataHelper(){
    return computeConsolidationDataHandler;
  }

  public PortConsolidationDataHandler getPortConsolidationDataHandler(){

    return portConsolidationDataHandler;
  }

  public NestedConsolidationDataHandler getNestedConsolidationDataHandler() {
    return nestedConsolidationDataHandler;
  }

  public SubInterfaceConsolidationDataHandler getSubInterfaceConsolidationDataHandler() {
    return subInterfaceConsolidationDataHandler;
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
