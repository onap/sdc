/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * The type Port template consolidation data.
 */
public class PortTemplateConsolidationData extends EntityConsolidationData {

  // key - subport type - for ResourceGroup it is the nested file name
  // value - List of subports of that type in the port
  private Map<String, List<SubInterfaceTemplateConsolidationData>> subInterfaceConsolidationData =
      new HashMap<>();

  public Map<String, List<SubInterfaceTemplateConsolidationData>> getSubInterfaceConsolidationData() {
    return subInterfaceConsolidationData;
  }

  public void setSubInterfaceConsolidationData(
      Map<String, List<SubInterfaceTemplateConsolidationData>> subInterfaceConsolidationData) {
    this.subInterfaceConsolidationData = subInterfaceConsolidationData;
  }

  public void addSubInterfaceConsolidationData(String subPortType,
                                               SubInterfaceTemplateConsolidationData
                                                   subInterfaceTemplateConsolidationData) {
    if (MapUtils.isEmpty(this.subInterfaceConsolidationData)) {
      this.subInterfaceConsolidationData = new HashMap<>();
    }

    if (Objects.isNull(this.subInterfaceConsolidationData.get(subPortType))) {
      this.subInterfaceConsolidationData.put(subPortType, new ArrayList<>());
    }

    this.subInterfaceConsolidationData.get(subPortType).add(subInterfaceTemplateConsolidationData);
  }

  public Set<String> getAllSubInterfaceNodeTypes() {
    return subInterfaceConsolidationData.keySet();
  }

}