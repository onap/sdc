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
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FilePortConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for consolidation data collection helper methods.
 */
public class UnifiedCompositionUtil {

  protected static Logger logger = LoggerFactory.getLogger(UnifiedCompositionUtil.class);

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

  static String getComputeTypeSuffix(ServiceTemplate serviceTemplate,
                                      String computeNodeTemplateId) {
    NodeTemplate computeNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate, computeNodeTemplateId);
    return getComputeTypeSuffix(computeNodeTemplate.getType());
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
    PortTemplateConsolidationData portTemplateConsolidationData =
        getSubInterfacePortTemplateConsolidationData(serviceTemplate, subInterfaceTemplateConsolidationData, context);
    NodeTemplate subInterfaceNodeTemplate =
        DataModelUtil.getNodeTemplate(serviceTemplate, subInterfaceTemplateConsolidationData.getNodeTemplateId());
    if (Objects.nonNull(portTemplateConsolidationData)) {
      List<String> subInterfaceNodeTemplateIdsByType =
          UnifiedCompositionUtil.getSubInterfaceNodeTemplateIdsByType(portTemplateConsolidationData,
              subInterfaceNodeTemplate.getType());
      if (CollectionUtils.isNotEmpty(subInterfaceNodeTemplateIdsByType)) {
        //If there are more than one subinterfaces with same type use node template id
        if (subInterfaceNodeTemplateIdsByType.size() > 1) {
          newSubInterfaceNodeTemplateId.append("_").append(subInterfaceTemplateConsolidationData.getNodeTemplateId());
        } else {
          //Add sub interface type since we have only one subinterface per type
          String subInterfaceTypeSuffix = getSubInterfaceTypeSuffix(subInterfaceNodeTemplate.getType());
          newSubInterfaceNodeTemplateId.append("_").append(subInterfaceTypeSuffix);
        }
        return newSubInterfaceNodeTemplateId.toString();
      }
    }
    return subInterfaceTemplateConsolidationData.getNodeTemplateId();
  }

  static PortTemplateConsolidationData getSubInterfacePortTemplateConsolidationData(ServiceTemplate serviceTemplate,
                                                                               SubInterfaceTemplateConsolidationData
                                                                                   subInterfaceTemplateConsolidationData,
                                                                                     TranslationContext context) {
    FilePortConsolidationData filePortConsolidationData = context.getConsolidationData().getPortConsolidationData()
        .getFilePortConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate));
    PortTemplateConsolidationData portTemplateConsolidationData = null;
    if (filePortConsolidationData != null) {
      portTemplateConsolidationData = filePortConsolidationData
          .getPortTemplateConsolidationData(subInterfaceTemplateConsolidationData.getParentPortNodeTemplateId());
    }
    return portTemplateConsolidationData;
  }

  static String getSubInterfaceTypeSuffix(String nodeType) {
    return DataModelUtil.getNamespaceSuffix(nodeType);
  }

  public static List<SubInterfaceTemplateConsolidationData> getSubInterfaceTemplateConsolidationDataList(
      UnifiedCompositionData unifiedCompositionData) {
    return unifiedCompositionData.getSubInterfaceTemplateConsolidationDataList() == null ? new
        ArrayList<>() : unifiedCompositionData.getSubInterfaceTemplateConsolidationDataList();
  }

}
