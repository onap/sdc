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

package org.openecomp.sdc.translator.services.heattotosca;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for consolidation data collection helper methods.
 */
public class UnifiedCompositionUtil {

  protected static Logger logger = LoggerFactory.getLogger(UnifiedCompositionUtil.class);

  private UnifiedCompositionUtil() {
    //Hiding the impicit public constructor
  }

  /**
   * Gets all ports per port type, which are connected to the computes from the input
   * computeTemplateConsolidationDataCollection.
   *
   * @param computeTemplateConsolidationDataCollection collection of compute template
   *                                                   consolidation data
   * @return set of port ids, per port type
   */
  static Map<String, List<String>> collectAllPortsFromEachTypesFromComputes(
      Collection<ComputeTemplateConsolidationData> computeTemplateConsolidationDataCollection) {
    Map<String, List<String>> portTypeToIds = new HashMap<>();

    for (ComputeTemplateConsolidationData compute : computeTemplateConsolidationDataCollection) {
      Map<String, List<String>> ports = compute.getPorts();
      if (!MapUtils.isEmpty(ports)) {
        addPortsToMap(portTypeToIds, ports);
      }
    }

    return portTypeToIds;
  }

  static ListMultimap<String, SubInterfaceTemplateConsolidationData>
        collectAllSubInterfacesOfEachTypesFromPorts(Collection<PortTemplateConsolidationData>
                                                    portTemplateConsolidationDataCollection) {
    ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfaceDataByType = ArrayListMultimap.create();
    for (PortTemplateConsolidationData port : portTemplateConsolidationDataCollection) {
      Set<String> allSubInterfaceNodeTypes = port.getAllSubInterfaceNodeTypes();
      if (CollectionUtils.isEmpty(allSubInterfaceNodeTypes)) {
        continue;
      }
      for (String subInterfaceNodeType : allSubInterfaceNodeTypes) {
        subInterfaceDataByType.putAll(subInterfaceNodeType,
            port.getSubInterfaceConsolidationData(subInterfaceNodeType));
      }
    }
    return subInterfaceDataByType;
  }

  static List<String> getSubInterfaceNodeTemplateIdsByType(PortTemplateConsolidationData
                                                               portTemplateConsolidationData,
                                                           String subInterfaceType) {
    List<String> subInterfaceNodeTemplateIds = new ArrayList<>();
    List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
        portTemplateConsolidationData.getSubInterfaceConsolidationData(subInterfaceType);
    if (CollectionUtils.isNotEmpty(subInterfaceTemplateConsolidationDataList)) {
      subInterfaceNodeTemplateIds = subInterfaceTemplateConsolidationDataList.stream()
          .map(SubInterfaceTemplateConsolidationData::getNodeTemplateId)
          .collect(Collectors.toList());
    }
    return subInterfaceNodeTemplateIds;
  }

  private static void addPortsToMap(Map<String, List<String>> portTypeToIds,
                                    Map<String, List<String>> ports) {
    for (Map.Entry<String, List<String>> portTypeToIdEntry : ports.entrySet()) {
      portTypeToIds.putIfAbsent(portTypeToIdEntry.getKey(), new ArrayList<>());
      portTypeToIds.get(portTypeToIdEntry.getKey()).addAll(portTypeToIdEntry.getValue());
    }
  }


}
