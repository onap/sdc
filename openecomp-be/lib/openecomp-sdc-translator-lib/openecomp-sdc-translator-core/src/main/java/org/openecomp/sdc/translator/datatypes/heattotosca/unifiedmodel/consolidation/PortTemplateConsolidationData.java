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

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.List;


/**
 * The type Port template consolidation data.
 */
public class PortTemplateConsolidationData extends EntityConsolidationData {

  // key - subport type - for ResourceGroup it is the nested file name
  // value - List of subports of that type in the port
    private final ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfaceConsolidationData =
        Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

  public void addSubInterfaceConsolidationData(String subPortType,
                                               SubInterfaceTemplateConsolidationData
                                                   subInterfaceTemplateConsolidationData) {
    this.subInterfaceConsolidationData.put(subPortType, subInterfaceTemplateConsolidationData);
  }

  public boolean hasSameSubInterfaceTypes(PortTemplateConsolidationData other) {
    return other != null && this.subInterfaceConsolidationData.keySet().equals(
        other.subInterfaceConsolidationData.keySet());
  }

    public void copyMappedInto(ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfaceTypeToEntity) {
    subInterfaceTypeToEntity.putAll(this.subInterfaceConsolidationData);
  }

    public void copyFlatInto(List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList) {
        subInterfaceTemplateConsolidationDataList.addAll(subInterfaceConsolidationData.values());
    }

  public boolean isNumberOfSubInterfacesPerTypeSimilar(PortTemplateConsolidationData other) {

    if (this.subInterfaceConsolidationData.isEmpty() &&
        other.subInterfaceConsolidationData.isEmpty()) {
      return true;
    }

    return !this.subInterfaceConsolidationData.isEmpty()
        && !other.subInterfaceConsolidationData.isEmpty()
        && this.subInterfaceConsolidationData.keySet().stream().allMatch(subInterfaceType ->
        calculateSize(other.subInterfaceConsolidationData.get(subInterfaceType)) ==
            calculateSize(this.subInterfaceConsolidationData.get(subInterfaceType)));
  }

  private int calculateSize(List<SubInterfaceTemplateConsolidationData> subInterfaces) {
    return subInterfaces == null ? 0 : subInterfaces.size();
  }
}