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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
//import org.openecomp.core.model.types.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Utility class for consolidation data collection helper methods.
 */
public class UnifiedCompositionUtil {

  private UnifiedCompositionUtil() {
    //Hiding the implicit public constructor
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
      port.copyMappedInto(subInterfaceDataByType);
    }
    return subInterfaceDataByType;
  }

  private static void addPortsToMap(Map<String, List<String>> portTypeToIds,
                                    Map<String, List<String>> ports) {
    for (Map.Entry<String, List<String>> portTypeToIdEntry : ports.entrySet()) {
      portTypeToIds.putIfAbsent(portTypeToIdEntry.getKey(), new ArrayList<>());
      portTypeToIds.get(portTypeToIdEntry.getKey()).addAll(portTypeToIdEntry.getValue());
    }
  }

  static String getComputeTypeSuffix(ServiceTemplate serviceTemplate,
                                      String computeNodeTemplateId) {
    NodeTemplate computeNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate, computeNodeTemplateId);
    if (Objects.nonNull(computeNodeTemplate)) {
      return getComputeTypeSuffix(computeNodeTemplate.getType());
    }
    return null;
  }

  public static String getNewComputeNodeTemplateId(ServiceTemplate serviceTemplate, String computeNodeTemplateId) {
    return getComputeTypeSuffix(serviceTemplate, computeNodeTemplateId);
  }

  static String getComputeTypeSuffix(String computeType) {
    return DataModelUtil.getNamespaceSuffix(computeType);
  }

  public static ComputeTemplateConsolidationData getConnectedComputeConsolidationData(
      List<UnifiedCompositionData> unifiedCompositionDataList,
      String portNodeTemplateId) {
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      if (Objects.isNull(unifiedCompositionData.getComputeTemplateConsolidationData().getPorts())) {
        continue;
      }
      Collection<List<String>> portsCollection =
          unifiedCompositionData.getComputeTemplateConsolidationData().getPorts().values();
      for (List<String> portIdList : portsCollection) {
        if (portIdList.contains(portNodeTemplateId)) {
          return unifiedCompositionData.getComputeTemplateConsolidationData();
        }
      }
    }
    return null;
  }

  //The ID should be <vm_type>_<port_type> or <vm_type>_<portNodeTemplateId>
  public static String getNewPortNodeTemplateId(
      String portNodeTemplateId,
      String connectedComputeNodeType,
      ComputeTemplateConsolidationData computeTemplateConsolidationData) {

    StringBuilder newPortNodeTemplateId = new StringBuilder();
    String portType = ConsolidationDataUtil.getPortType(portNodeTemplateId);
    newPortNodeTemplateId.append(DataModelUtil.getNamespaceSuffix(connectedComputeNodeType));
    if (computeTemplateConsolidationData.getPorts().get(portType).size() > 1) {
      //single port
      newPortNodeTemplateId.append("_").append(portNodeTemplateId);
    } else {
      //consolidation port
      newPortNodeTemplateId.append("_").append(portType);
    }
    return newPortNodeTemplateId.toString();
  }

  public static String getNewSubInterfaceNodeTemplateId(ServiceTemplate serviceTemplate,
                                                  String connectedComputeNodeType,
                                                  ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                                  SubInterfaceTemplateConsolidationData
                                                            subInterfaceTemplateConsolidationData,
                                                  TranslationContext context) {
    //The ID should be <vm_type>_<portType/NodetemplateId>_<subInterface_type>
    // or <vm_type>_<portType/NodetemplateId>_<subInterfaceNodeTemplateId>
    StringBuilder newSubInterfaceNodeTemplateId = new StringBuilder();
    newSubInterfaceNodeTemplateId.append(getNewPortNodeTemplateId(subInterfaceTemplateConsolidationData
        .getParentPortNodeTemplateId(), connectedComputeNodeType, computeTemplateConsolidationData));
    Optional<PortTemplateConsolidationData> portTemplateConsolidationData =
        subInterfaceTemplateConsolidationData.getParentPortTemplateConsolidationData(serviceTemplate, context);
    NodeTemplate subInterfaceNodeTemplate =
        DataModelUtil.getNodeTemplate(serviceTemplate, subInterfaceTemplateConsolidationData.getNodeTemplateId());
    if (portTemplateConsolidationData.isPresent()) {
      String subInterfaceSuffix = getSubInterfaceSuffix(portTemplateConsolidationData.get(),
          subInterfaceNodeTemplate, subInterfaceTemplateConsolidationData);
      newSubInterfaceNodeTemplateId.append("_").append(subInterfaceSuffix);
      return newSubInterfaceNodeTemplateId.toString();
    }
    return subInterfaceTemplateConsolidationData.getNodeTemplateId();
  }

  static String getSubInterfaceTypeSuffix(String nodeType) {
    return DataModelUtil.getNamespaceSuffix(nodeType);
  }

  public static List<SubInterfaceTemplateConsolidationData> getSubInterfaceTemplateConsolidationDataList(
      UnifiedCompositionData unifiedCompositionData) {
    return unifiedCompositionData.getSubInterfaceTemplateConsolidationDataList() == null ? Collections.emptyList() :
        unifiedCompositionData.getSubInterfaceTemplateConsolidationDataList();
  }

  private static String getSubInterfaceSuffix(PortTemplateConsolidationData portTemplateConsolidationData,
                                              NodeTemplate subInterfaceNodeTemplate,
                                              SubInterfaceTemplateConsolidationData
                                                  subInterfaceTemplateConsolidationData) {
    if (portTemplateConsolidationData.isSubInterfaceNodeTemplateIdParameter(subInterfaceNodeTemplate.getType())) {
      //If there are more than one subinterfaces with same type use node template id
      return subInterfaceTemplateConsolidationData.getNodeTemplateId();
    }
    //Add sub interface type since we have only one subinterface per type
    return getSubInterfaceTypeSuffix(subInterfaceNodeTemplate.getType());
  }

    public static Map<String, List<String>> collectAllPortsOfEachTypeFromComputes(
                                                             List<UnifiedCompositionData> unifiedCompositionDataList) {
        Map<String, List<String>> portIdsPerPortType = new HashMap<>();
        unifiedCompositionDataList
                .forEach(unifiedCompositionData ->
                                 unifiedCompositionData.getComputeTemplateConsolidationData()
                                                       .collectAllPortsOfEachTypeFromCompute(portIdsPerPortType));

        return portIdsPerPortType;
    }
}
