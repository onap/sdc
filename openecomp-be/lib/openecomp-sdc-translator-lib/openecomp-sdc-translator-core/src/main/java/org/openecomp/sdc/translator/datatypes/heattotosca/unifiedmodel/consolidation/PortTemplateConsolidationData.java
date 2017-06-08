package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.List;


/**
 * The type Port template consolidation data.
 */
public class PortTemplateConsolidationData extends EntityConsolidationData {

  private List<String> subPortIds; //sub ports which point to this port

  /**
   * Gets sub port ids.
   *
   * @return the sub port ids
   */
  public List<String> getSubPortIds() {
    return subPortIds;
  }

  /**
   * Sets sub port ids.
   *
   * @param subPortIds the sub port ids
   */
  public void setSubPortIds(List<String> subPortIds) {
    this.subPortIds = subPortIds;
  }
}