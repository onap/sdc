/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition;

import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;

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

  private List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList;

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
   * Gets sub interface template consolidation data list.
   *
   * @return the sub interface template consolidation data list
   */
  public List<SubInterfaceTemplateConsolidationData> getSubInterfaceTemplateConsolidationDataList() {
    return subInterfaceTemplateConsolidationDataList;
  }

  /**
   * Sets sub interface template consolidation data list.
   *
   * @param subInterfaceTemplateConsolidationDataList the sub interface template consolidation data
   *                                                  list
   */
  public void setSubInterfaceTemplateConsolidationDataList(
      List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList) {
    this.subInterfaceTemplateConsolidationDataList = subInterfaceTemplateConsolidationDataList;
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
