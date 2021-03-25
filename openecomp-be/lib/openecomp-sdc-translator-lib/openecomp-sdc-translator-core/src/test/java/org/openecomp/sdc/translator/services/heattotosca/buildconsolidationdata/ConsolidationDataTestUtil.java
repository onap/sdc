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

package org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata;

import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.MAIN_SERVICE_TEMPLATE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_INVALID_DEPENDENCY_CANDIDATE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_MULTIPLE_COMPUTE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NODES_CONNECTED_IN;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NODES_CONNECTED_IN_AND_OUT;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NODES_CONNECTED_OUT;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NO_DEPENDENCY;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_MULTIPLE_MULTI_LEVEL_NESTED_RESOURCE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_MULTIPLE_NESTED_RESOURCE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_SECURITY_RULE_PORT_MULTI_LEVEL_NESTED_CONNECTION;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_SECURITY_RULE_PORT_MULTI_LEVEL_NESTED_SHARED_PORT;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_SECURITY_RULE_PORT_NESTED_CONNECTION;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_SINGLE_NESTED_RESOURCE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.onap.sdc.tosca.datatypes.model.GroupDefinition;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileNestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.GetAttrFuncData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.NestedTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.PortTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.RequirementAssignmentData;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

public class ConsolidationDataTestUtil {

    public static void validateVolumeInConsolidationData(String computeNodeTemplateId,
                                                         ComputeTemplateConsolidationData
                                                             computeTemplateConsolidationData,
                                                         ServiceTemplate expectedServiceTemplate,
                                                         String testName) {
        Assert.assertNotNull(computeTemplateConsolidationData);
        //Get the volume information from consolidation data
        Multimap<String, RequirementAssignmentData> volumeConsolidationData =
            computeTemplateConsolidationData.getVolumes();

        if (testName.equals("Negative")) {
            Assert.assertNull(volumeConsolidationData);
            return;
        }

        //Get the volume requirement information from the output translated template
        NodeTemplate computeNode = DataModelUtil.getNodeTemplate(expectedServiceTemplate,
            computeNodeTemplateId);

        if (!isComputeNodeType(expectedServiceTemplate, computeNode.getType())) {
            //According to toplogy only Compute->volume relationship is valid
            Assert.assertNull(volumeConsolidationData);
            return;
        }

        Assert.assertNotNull(computeNode);
        List<String> computeVolumeRequirementsNodes = new ArrayList<>();
        List<Map<String, RequirementAssignment>> requirementList = computeNode.getRequirements();
        if (requirementList != null) {
            for (Map<String, RequirementAssignment> req : requirementList) {
                Set<String> reqKeySet = req.keySet();
                for (String reqKey : reqKeySet) {
                    //populating the "node" property of all the requirements "local_storage" related to volume
                    if (reqKey.equals(ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID)) {
                        RequirementAssignment requirementAssignment = new ObjectMapper().convertValue(req.get
                            (reqKey), RequirementAssignment.class);
                        computeVolumeRequirementsNodes.add(requirementAssignment.getNode());
                    }
                }
            }
            isVolumeComputeRequirement(computeVolumeRequirementsNodes, volumeConsolidationData);
        }
    }

    private static void isVolumeComputeRequirement(List<String> computeVolumeRequirementsNodes,
                                                   Multimap<String, RequirementAssignmentData>
                                                       volumeConsolidationData) {
        Assert.assertEquals(computeVolumeRequirementsNodes.size(), volumeConsolidationData.size());
        for (String volumeNodeTemplateId : computeVolumeRequirementsNodes) {
            Assert.assertTrue(volumeConsolidationData.containsKey(volumeNodeTemplateId));
            Collection<RequirementAssignmentData> requirementAssignmentDataList = volumeConsolidationData.get(volumeNodeTemplateId);
            for (RequirementAssignmentData requirementAssignmentData : requirementAssignmentDataList) {
                Assert.assertEquals(ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID, requirementAssignmentData.getRequirementId());
            }
        }
    }

    public static void validatePortsInConsolidationData(String computeNodeTemplateId,
                                                        ComputeTemplateConsolidationData
                                                            computeTemplateConsolidationData,
                                                        ServiceTemplate outputServiceTemplate) {
        Map<String, List<String>> consolidatedMap = computeTemplateConsolidationData.getPorts();
        Map<String, List<String>> expectedMap = getPortsInConsolidationData(outputServiceTemplate).get
            (computeNodeTemplateId);
        if (expectedMap == null && consolidatedMap == null) {
            return;
        }
        for (String consolidatedKey : consolidatedMap.keySet()) {
            List<String> consolidatedList = consolidatedMap.get(consolidatedKey);
            if (expectedMap != null) {
                List<String> expectedList = expectedMap.get(consolidatedKey);
                if (expectedList == null) {
                    Assert.fail();
                }
                if (!CollectionUtils.isEqualCollection(consolidatedList, expectedList)) {
                    Assert.fail();
                }
            }
        }
    }

    public static void validateDependsOnInConsolidationData(String computeNodeTemplateId,
                                                            ComputeTemplateConsolidationData
                                                                computeTemplateConsolidationData,
                                                            ServiceTemplate outputServiceTemplate,
                                                            String testName) {
        Map<String, NodeTemplate> outputNodeTemplates = outputServiceTemplate.getTopology_template()
            .getNode_templates();
        Multimap<String, RequirementAssignmentData> nodesConnectedIn =
            computeTemplateConsolidationData.getNodesConnectedIn();
        Multimap<String, RequirementAssignmentData> nodesConnectedOut =
            computeTemplateConsolidationData.getNodesConnectedOut();

        if (testName.equals(TEST_DEPENDS_ON_INVALID_DEPENDENCY_CANDIDATE) ||
            testName.equals(TEST_DEPENDS_ON_NO_DEPENDENCY)) {
            Assert.assertNull(nodesConnectedIn);
            Assert.assertNull(nodesConnectedOut);
            return;
        }
        //key - nodetemplate id , value - requirementassignment
        Multimap<String, RequirementAssignment> outputDependsOnNodeRequirementMap = ArrayListMultimap.create();
        for (Map.Entry<String, NodeTemplate> entry : outputNodeTemplates.entrySet()) {
            NodeTemplate nodeTemplate = entry.getValue();
            List<Map<String, RequirementAssignment>> nodeRequirements = nodeTemplate.getRequirements();
            if (nodeRequirements != null) {
                for (Map<String, RequirementAssignment> req : nodeRequirements) {
                    Set<String> keySet = req.keySet();
                    for (String key : keySet) {
                        if (key.equals(ToscaConstants.DEPENDS_ON_REQUIREMENT_ID))
                        //collect all dependency requirements in a map with key -> node template id
                        {
                            outputDependsOnNodeRequirementMap.put(entry.getKey(), req.get(key));
                        }
                    }
                }
            }
        }

        if (testName.equals(TEST_DEPENDS_ON_NODES_CONNECTED_OUT)) {
            Assert.assertNull(nodesConnectedIn);
            validateDependsOnNodesConnectedOut(computeNodeTemplateId, nodesConnectedOut,
                outputDependsOnNodeRequirementMap, outputServiceTemplate);
        }

        if (testName.equals(TEST_DEPENDS_ON_NODES_CONNECTED_IN)) {
            Assert.assertNull(nodesConnectedOut);
            validateDependsOnNodesConnectedIn(computeNodeTemplateId, nodesConnectedIn,
                outputDependsOnNodeRequirementMap,
                outputServiceTemplate);
        }

        if (testName.equals(TEST_DEPENDS_ON_NODES_CONNECTED_IN_AND_OUT)) {
            Assert.assertNotNull(nodesConnectedIn);
            Assert.assertNotNull(nodesConnectedOut);
            validateDependsOnNodesConnectedOut(computeNodeTemplateId, nodesConnectedOut,
                outputDependsOnNodeRequirementMap,
                outputServiceTemplate);
            validateDependsOnNodesConnectedIn(computeNodeTemplateId, nodesConnectedIn,
                outputDependsOnNodeRequirementMap,
                outputServiceTemplate);
        }

        if (testName.equals(TEST_DEPENDS_ON_MULTIPLE_COMPUTE)) {
            if (nodesConnectedOut != null) {
                validateDependsOnNodesConnectedOut(computeNodeTemplateId, nodesConnectedOut,
                    outputDependsOnNodeRequirementMap,
                    outputServiceTemplate);
            }
            if (nodesConnectedIn != null) {
                validateDependsOnNodesConnectedIn(computeNodeTemplateId, nodesConnectedIn,
                    outputDependsOnNodeRequirementMap,
                    outputServiceTemplate);
            }
        }


    }

    private static void validateDependsOnNodesConnectedIn(String computeNodeTemplateId,
                                                          Multimap<String, RequirementAssignmentData>
                                                              nodesConnectedIn,
                                                          Multimap<String, RequirementAssignment>
                                                              outputDependsOnNodeRequirementMap,
                                                          ServiceTemplate outputServiceTemplate) {
        ToscaAnalyzerServiceImpl analyzerService = new ToscaAnalyzerServiceImpl();
        for (String sourceNodeTemplateId : outputDependsOnNodeRequirementMap.keySet()) {
            Optional<NodeTemplate> sourceNodeTemplate = analyzerService.getNodeTemplateById
                (outputServiceTemplate, sourceNodeTemplateId);
            String sourceNodeType = sourceNodeTemplate.get().getType();
            for (Object obj : outputDependsOnNodeRequirementMap.get(sourceNodeTemplateId)) {
                RequirementAssignment req = new ObjectMapper().convertValue(obj, RequirementAssignment
                    .class);
                String targetNodeTemplateId = req.getNode();
                Optional<NodeTemplate> targetNodeTemplate = analyzerService.getNodeTemplateById
                    (outputServiceTemplate, targetNodeTemplateId);

                String targetNodeType = targetNodeTemplate.get().getType();
                boolean isValidTargetForConnectedIn = false;
                if (isComputeNodeType(outputServiceTemplate, targetNodeType)) {
                    isValidTargetForConnectedIn = true;
                } else if (isPortNodeType(outputServiceTemplate, targetNodeType)) {
                    isValidTargetForConnectedIn = true;
                }

                if (isValidTargetForConnectedIn) {
                    //Should be present if target node is compute or port
                    if (computeNodeTemplateId.equals(sourceNodeTemplateId)) {
                        Assert.assertTrue(nodesConnectedIn.containsKey(sourceNodeTemplateId));
                    }
                }

                if (sourceNodeType.startsWith(ToscaNodeType.NOVA_SERVER)
                    && (targetNodeType.startsWith(ToscaNodeType.NOVA_SERVER)
                    || targetNodeType.startsWith(ToscaNodeType.NEUTRON_PORT)
                    || targetNodeType.startsWith(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE))) {
                    //Ignore Compute->Port, Compute->Compute, Compute->Volume relationship
                    Assert.assertFalse(nodesConnectedIn.containsKey(targetNodeTemplateId));
                }

                if (sourceNodeType.startsWith(ToscaNodeType.NEUTRON_PORT)
                    && (targetNodeType.startsWith(ToscaNodeType.NOVA_SERVER)
                    || targetNodeType.startsWith(ToscaNodeType.NEUTRON_PORT)
                    || targetNodeType.startsWith(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE))) {
                    //Ignore Port->Port, Port->Compute, Port->Volume relationship
                    Assert.assertFalse(nodesConnectedIn.containsKey(targetNodeTemplateId));
                }
            }
        }
    }

    private static boolean isComputeNodeType(ServiceTemplate serviceTemplate,
                                             String nodeType) {

        if (nodeType.equals(ToscaNodeType.NOVA_SERVER) ||
            nodeType.equals(ToscaNodeType.NATIVE_COMPUTE)) {
            return true;
        }

        Map<String, NodeType> nodeTypes = serviceTemplate.getNode_types();
        if (nodeTypes.containsKey(nodeType)) {
            NodeType nodeTypeInfo = nodeTypes.get(nodeType);
            if (nodeTypeInfo.getDerived_from().equals(ToscaNodeType.NOVA_SERVER)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPortNodeType(ServiceTemplate serviceTemplate,
                                          String nodeType) {
        if (nodeType.equals(ToscaNodeType.NEUTRON_PORT) ||
            nodeType.equals(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE)) {
            return true;
        }

        Map<String, NodeType> nodeTypes = serviceTemplate.getNode_types();
        if (nodeTypes.containsKey(nodeType)) {
            NodeType nodeTypeInfo = nodeTypes.get(nodeType);
            if (nodeTypeInfo.getDerived_from().equals(ToscaNodeType.NEUTRON_PORT) ||
                nodeTypeInfo.getDerived_from().equals(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE)) {
                return true;
            }
        }
        return false;
    }

    private static void validateDependsOnNodesConnectedOut(String computeNodeTemplateId,
                                                           Multimap<String, RequirementAssignmentData>
                                                               nodesConnectedOut,
                                                           Multimap<String, RequirementAssignment>
                                                               outputDependsOnNodeRequirementMap,
                                                           ServiceTemplate outputServiceTemplate) {
        ToscaAnalyzerServiceImpl analyzerService = new ToscaAnalyzerServiceImpl();
        //Iterating the map <nodeTemplateId, all the requirements of that node>
    /*for(Map.Entry<String, List<RequirementAssignment>> entry : outputDependsOnNodeRequirementMap
        .entrySet()) {*/
        for (String sourceNodeTemplateId : outputDependsOnNodeRequirementMap.keySet()) {
            Optional<NodeTemplate> sourceNodeTemplate = analyzerService.getNodeTemplateById
                (outputServiceTemplate, sourceNodeTemplateId);

            String sourceNodeType = sourceNodeTemplate.get().getType();
            boolean isValidSourceForConnectedOut = false;
            if (isComputeNodeType(outputServiceTemplate, sourceNodeType)) {
                isValidSourceForConnectedOut = true;
            } else if (isPortNodeType(outputServiceTemplate, sourceNodeType)) {
                isValidSourceForConnectedOut = true;
            }
            for (Object obj : outputDependsOnNodeRequirementMap.get(sourceNodeTemplateId)) {
                RequirementAssignment req = new ObjectMapper().convertValue(obj, RequirementAssignment
                    .class);
                String targetNodeTemplateId = req.getNode();
                Optional<NodeTemplate> targetNodeTemplate = analyzerService.getNodeTemplateById
                    (outputServiceTemplate, targetNodeTemplateId);
                String targetNodeType = targetNodeTemplate.get().getType();

                if (isValidSourceForConnectedOut) {
                    //Should be present if source node is compute or port
                    if (computeNodeTemplateId.equals(sourceNodeTemplateId)) {
                        Assert.assertTrue(nodesConnectedOut.containsKey(targetNodeTemplateId));
                    }
                }

                if (sourceNodeType.startsWith(ToscaNodeType.NOVA_SERVER)
                    && (targetNodeType.startsWith(ToscaNodeType.NOVA_SERVER)
                    || targetNodeType.startsWith(ToscaNodeType.NEUTRON_PORT)
                    || targetNodeType.startsWith(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE))) {
                    //Ignore Compute->Port, Compute->Compute, Compute->Volume relationship
                    Assert.assertFalse(nodesConnectedOut.containsKey(targetNodeTemplateId));
                }

                if (sourceNodeType.startsWith(ToscaNodeType.NEUTRON_PORT)
                    && (targetNodeType.startsWith(ToscaNodeType.NOVA_SERVER)
                    || targetNodeType.startsWith(ToscaNodeType.NEUTRON_PORT)
                    || targetNodeType.startsWith(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE))) {
                    //Ignore Port->Port, Port->Compute, Port->Volume relationship
                    Assert.assertFalse(nodesConnectedOut.containsKey(targetNodeTemplateId));
                }
            }
        }

    }

    private static Map<String, Map<String, List<String>>> getPortsInConsolidationData(ServiceTemplate
                                                                                          output) {
        Map<String, Map<String, List<String>>> portMap = new LinkedHashMap<>();
        Map<String, NodeTemplate> nodeTempMap = output.getTopology_template().getNode_templates();
        for (String nodeName : nodeTempMap.keySet()) {
            NodeTemplate node = nodeTempMap.get(nodeName);
            if (ToscaNodeType.NEUTRON_PORT.equals(node.getType()) || ToscaNodeType
                .CONTRAILV2_VIRTUAL_MACHINE_INTERFACE.equals(node.getType())) {
                List<Map<String, RequirementAssignment>> reqAssignList = node.getRequirements();
                if (reqAssignList != null) {
                    for (Map<String, RequirementAssignment> reqAssignMap : reqAssignList) {
                        //RequirementAssignment req = reqAssignMap.get("binding");
                        RequirementAssignment req = new ObjectMapper().convertValue(reqAssignMap.get("binding"),
                            RequirementAssignment.class);

                        if (req != null) {
                            String portNode = req.getNode();
                            if (!portMap.containsKey(portNode)) {
                                portMap.put(portNode, new LinkedHashMap<>());
                            }
                            Map<String, List<String>> portTypMaps = portMap.get(portNode);
                            String id = ConsolidationDataUtil.getPortType(nodeName, "a");
                            if (!portTypMaps.containsKey(id)) {
                                portTypMaps.put(id, new ArrayList<>());
                            }
                            List<String> portIds = portTypMaps.get(id);
                            portIds.add(nodeName);
                        }
                    }
                }
            }
        }
        return portMap;
    }

    public static void validateGroupsInConsolidationData(String computeNodeTemplateId,
                                                         ComputeTemplateConsolidationData
                                                             computeTemplateConsolidationData,
                                                         ServiceTemplate expectedServiceTemplate) {
        Assert.assertNotNull(computeTemplateConsolidationData);
        List<String> groupIds = computeTemplateConsolidationData.getGroupIds();
        if (groupIds != null) {
            for (String groupId : groupIds) {
                isComputeGroupMember(expectedServiceTemplate, computeNodeTemplateId, groupId);
            }
        }
    }

    private static void isComputeGroupMember(ServiceTemplate expectedServiceTemplate, String
        computeNodeTemplateId, String groupId) {
        //Check if the collected group id is in the member list of the groups
        GroupDefinition group = expectedServiceTemplate.getTopology_template().getGroups().get(groupId);
        List<String> groupMembers = group.getMembers();
        Assert.assertNotNull(groupMembers);
        Assert.assertTrue(groupMembers.contains(computeNodeTemplateId));
    }

    public static void validateNestedConsolidationDataNodeTemplateIds(ConsolidationData consolidationData,
                                                                      Map<String, ServiceTemplate>
                                                                          expectedServiceTemplateModels) {
        Map<String, List<String>> consolidatedMap = getSubstituteNodeTemplateIds(consolidationData);
        Map<String, List<String>> expectedMap = getSubstituteMapping(expectedServiceTemplateModels);
        for (String consolidatedKey : consolidatedMap.keySet()) {
            List<String> consolidatedList = consolidatedMap.get(consolidatedKey);
            List<String> expectedList = expectedMap.get(consolidatedKey);
            if (expectedList == null) {
                Assert.fail();
            }
            if (!CollectionUtils.isEqualCollection(consolidatedList, expectedList)) {
                Assert.fail();
            }
        }
    }

    private static Map<String, List<String>> getSubstituteNodeTemplateIds(ConsolidationData
                                                                              consolidationData) {
        Map<String, List<String>> nestedNodeTemplateIdMap = new HashMap<>();
        NestedConsolidationData nestedConsolidationData =
            consolidationData.getNestedConsolidationData();
        Set<String> serviceTemplateFileNames =
            nestedConsolidationData.getAllServiceTemplateFileNames();
        for (String fileName : serviceTemplateFileNames) {
            FileNestedConsolidationData fileNestedConsolidationData =
                nestedConsolidationData.getFileNestedConsolidationData(fileName);
            if (Objects.isNull(fileNestedConsolidationData)) {
                continue;
            }
            Set<String> nestedNodeTemplateIds =
                fileNestedConsolidationData.getAllNestedNodeTemplateIds();
            if (nestedNodeTemplateIds != null) {
                List<String> fileNestedNodeTemplateIds = new ArrayList<>(nestedNodeTemplateIds);
                nestedNodeTemplateIdMap.put(fileName, fileNestedNodeTemplateIds);
            }
        }
        return nestedNodeTemplateIdMap;
    }

    private static Map<String, List<String>> getSubstituteMapping(Map<String, ServiceTemplate>
                                                                      expectedServiceTemplateModels) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (String key : expectedServiceTemplateModels.keySet()) {
            ServiceTemplate serviceTemplate = expectedServiceTemplateModels.get(key);
            if (serviceTemplate.getTopology_template() != null && serviceTemplate
                .getTopology_template().getNode_templates() != null) {
                for (String key1 : serviceTemplate.getTopology_template().getNode_templates().keySet()) {
                    NodeTemplate nodeTemplate = serviceTemplate.getTopology_template().getNode_templates()
                        .get(key1);
                    if (nodeTemplate.getType().contains(ToscaNodeType.ABSTRACT_NODE_TYPE_PREFIX + "heat.")) {
                        List<String> subNodeTempIdList = map.computeIfAbsent(key, k -> new ArrayList<>());
                        subNodeTempIdList.add(key1);
                    }
                }
            }
        }
        return map;
    }

    public static void validateComputeConnectivityIn(ComputeTemplateConsolidationData
                                                         computeTemplateConsolidationData,
                                                     ServiceTemplate expectedServiceTemplate) {
        Multimap<String, RequirementAssignmentData> nodesConnectedIn =
            computeTemplateConsolidationData.getNodesConnectedIn();
        if (nodesConnectedIn == null) {
            return;
        }

        validateConnectivity(null, nodesConnectedIn, expectedServiceTemplate);
    }

    public static void validateComputeConnectivityOut(String computeNodeTemplateId,
                                                      ComputeTemplateConsolidationData computeTemplateConsolidationData,
                                                      ServiceTemplate expectedServiceTemplate) {
        Multimap<String, RequirementAssignmentData> nodesConnectedOut =
            computeTemplateConsolidationData.getNodesConnectedOut();
        if (nodesConnectedOut == null) {
            return;
        }

        validateConnectivity(computeNodeTemplateId, nodesConnectedOut, expectedServiceTemplate);
    }

    public static void validatePortConnectivityIn(PortTemplateConsolidationData portTemplateConsolidationData,
                                                  ServiceTemplate expectedServiceTemplate) {
        Multimap<String, RequirementAssignmentData> nodesConnectedIn =
            portTemplateConsolidationData.getNodesConnectedIn();
        if (nodesConnectedIn == null) {
            return;
        }

        validateConnectivity(null, nodesConnectedIn, expectedServiceTemplate);
    }

    public static void validatePortConnectivityOut(String portNodeTemplateId,
                                                   PortTemplateConsolidationData portTemplateConsolidationData,
                                                   ServiceTemplate expectedServiceTemplate) {
        Multimap<String, RequirementAssignmentData> nodesConnectedOut =
            portTemplateConsolidationData.getNodesConnectedOut();
        if (nodesConnectedOut == null) {
            return;
        }

        validateConnectivity(portNodeTemplateId, nodesConnectedOut, expectedServiceTemplate);
    }

    private static void validateConnectivity(String nodeTemplateId,
                                             Multimap<String, RequirementAssignmentData> nodesConnected,
                                             ServiceTemplate expectedServiceTemplate) {
        boolean found = false;
        for (String nodeIdConnTo : nodesConnected.keySet()) {
            Collection<RequirementAssignmentData> connectToList = nodesConnected.get(nodeIdConnTo);
            List<Map<String, RequirementAssignment>> requirementsList = expectedServiceTemplate
                .getTopology_template().getNode_templates()
                .get(nodeTemplateId != null ? nodeTemplateId : nodeIdConnTo).getRequirements();

            for (RequirementAssignmentData requirementAssignmentData : connectToList) {
                for (Map<String, RequirementAssignment> requirementAssignmentMap : requirementsList) {
                    RequirementAssignment requirementAssignment =
                        new ObjectMapper().convertValue(requirementAssignmentMap.values().iterator().next(),
                            RequirementAssignment.class);
                    if (requirementAssignment.getNode()
                        .equals(requirementAssignmentData.getRequirementAssignment().getNode())) {
                        Assert.assertEquals(requirementAssignment.getCapability(),
                            requirementAssignmentData.getRequirementAssignment().getCapability());
                        Assert.assertEquals(requirementAssignment.getNode(),
                            requirementAssignmentData.getRequirementAssignment().getNode());
                        Assert.assertEquals(requirementAssignment.getRelationship(),
                            requirementAssignmentData.getRequirementAssignment()
                                .getRelationship());
                        found = true;
                    }
                }
                if (!found) {
                    Assert.fail();
                }
                found = false;
            }
        }
    }

    public static void validateGetAttr(TranslationContext translationContext, String testName) {
        ConsolidationData consolidationData = translationContext.getConsolidationData();
        Assert.assertNotNull(consolidationData);
        if (TestConstants.TEST_GET_ATTR_FOR_MORE_THAN_ONE_ATTR_IN_ATTR_LIST.equals(testName)) {
            PortTemplateConsolidationData portTemplateConsolidationData = consolidationData
                .getPortConsolidationData().getFilePortConsolidationData("ep-jsa_netServiceTemplate.yaml")
                .getPortTemplateConsolidationData("VMI1");
            Assert.assertNotNull(portTemplateConsolidationData);
            Assert.assertEquals(2, portTemplateConsolidationData.getNodesGetAttrIn().size());
            List<GetAttrFuncData> attrFuncDataList = portTemplateConsolidationData.getNodesGetAttrIn()
                .get("FSB1");
            Assert.assertEquals(1, attrFuncDataList.size());
            Assert.assertEquals("name", attrFuncDataList.get(0).getFieldName());
            Assert.assertEquals("name", attrFuncDataList.get(0).getAttributeName());

            attrFuncDataList = portTemplateConsolidationData.getNodesGetAttrIn()
                .get("FSB2");
            Assert.assertEquals(1, attrFuncDataList.size());
            Assert.assertEquals("name", attrFuncDataList.get(0).getFieldName());
            Assert.assertEquals("virtual_machine_interface_allowed_address_pairs", attrFuncDataList.get(0).getAttributeName());

            ComputeTemplateConsolidationData computeTemplateConsolidationDataFSB2 = consolidationData
                .getComputeConsolidationData()
                .getFileComputeConsolidationData("ep-jsa_netServiceTemplate.yaml")
                .getTypeComputeConsolidationData("org.openecomp.resource.vfc.nodes.heat.FSB2")
                .getComputeTemplateConsolidationData("FSB2");
            Assert.assertEquals(1, computeTemplateConsolidationDataFSB2.getNodesGetAttrOut().size());
            List<GetAttrFuncData> attrFuncDataOutList = computeTemplateConsolidationDataFSB2
                .getNodesGetAttrOut().get("VMI1");
            Assert.assertEquals(1, attrFuncDataOutList.size());
            Assert.assertEquals("name", attrFuncDataOutList.get(0).getFieldName());
            Assert.assertEquals("virtual_machine_interface_allowed_address_pairs", attrFuncDataOutList
                .get(0).getAttributeName());
            ComputeTemplateConsolidationData computeTemplateConsolidationDataFSB1 = consolidationData
                .getComputeConsolidationData()
                .getFileComputeConsolidationData("ep-jsa_netServiceTemplate.yaml")
                .getTypeComputeConsolidationData("org.openecomp.resource.vfc.nodes.heat.FSB1")
                .getComputeTemplateConsolidationData("FSB1");
            Assert.assertEquals(1, computeTemplateConsolidationDataFSB1.getNodesGetAttrOut().size());
            List<GetAttrFuncData> attrFuncDataOutList2 = computeTemplateConsolidationDataFSB1
                .getNodesGetAttrOut().get("VMI1");
            Assert.assertEquals(1, attrFuncDataOutList2.size());
            Assert.assertEquals("name", attrFuncDataOutList2.get(0).getFieldName());
            Assert.assertEquals("name", attrFuncDataOutList2
                .get(0).getAttributeName());
        } else if (TestConstants.TEST_IGNORE_GET_ATTR_FROM_OUTPUT.equals(testName)) {
            if (!consolidationData.getPortConsolidationData().getAllServiceTemplateFileNames().isEmpty()) {
                Iterator<String> itr = consolidationData.getPortConsolidationData()
                    .getFilePortConsolidationData("MainServiceTemplate.yaml").getAllPortNodeTemplateIds()
                    .iterator();
                while (itr.hasNext()) {
                    String key = itr.next();
                    PortTemplateConsolidationData portTemplateConsolidationData = consolidationData
                        .getPortConsolidationData()
                        .getFilePortConsolidationData("MainServiceTemplate.yaml")
                        .getPortTemplateConsolidationData(key);
                    Assert.assertNull(portTemplateConsolidationData.getOutputParametersGetAttrIn());
                }
            }
        } else if (TestConstants.TEST_GET_ATTR_FOR_NOT_SUPPORTED_ATTR_IN_ATTR_LIST.equals(testName)) {
            Assert.assertNull(consolidationData.getPortConsolidationData()
                .getFilePortConsolidationData("MainServiceTemplate.yaml")
                .getPortTemplateConsolidationData("FSB1_Internal2").getNodesGetAttrIn());
        } else if (TestConstants.TEST_GET_ATTR_FOR_ONLY_RESOURCE_NAME.equals(testName)) {
            PortTemplateConsolidationData portTemplateConsolidationData = consolidationData
                .getPortConsolidationData().getFilePortConsolidationData("MainServiceTemplate.yaml")
                .getPortTemplateConsolidationData("VMI1");
            Assert.assertNotNull(portTemplateConsolidationData);
            Assert.assertEquals("name", portTemplateConsolidationData.getNodesGetAttrIn().get("FSB1").
                get(0).getFieldName());
            Assert.assertEquals("tenant_id", portTemplateConsolidationData.getNodesGetAttrIn().get("FSB1").
                get(0).getAttributeName());
        } else if (TestConstants.TEST_GET_ATTR_FOR_NONE_TO_PORT_OR_COMPUTE.equals(testName)) {
            ComputeTemplateConsolidationData computeTemplateConsolidationData = consolidationData
                .getComputeConsolidationData()
                .getFileComputeConsolidationData("MainServiceTemplate.yaml")
                .getTypeComputeConsolidationData("org.openecomp.resource.vfc.nodes.heat.compute")
                .getComputeTemplateConsolidationData("server_compute_get_attr_test");
            Assert.assertEquals("user_data_format", computeTemplateConsolidationData
                .getNodesGetAttrOut().get("server_pcm_001").get(0).getFieldName());
            Assert.assertEquals("oam_net_gw", computeTemplateConsolidationData
                .getNodesGetAttrOut().get("server_pcm_001").get(0).getAttributeName());
        } else if (TestConstants.TEST_OUTPUT_GET_ATTR.equals(testName)) {
            ComputeTemplateConsolidationData computeTemplateConsolidationData1 = consolidationData
                .getComputeConsolidationData()
                .getFileComputeConsolidationData("firstnet_fgi_frwlServiceTemplate.yaml")
                .getTypeComputeConsolidationData("org.openecomp.resource.vfc.nodes.heat.cgi_fw")
                .getComputeTemplateConsolidationData("CGI_FW_SERVER_1");
            Assert.assertEquals("cgi_fw_01_left_mac_1", computeTemplateConsolidationData1
                .getOutputParametersGetAttrIn()
                .get(0).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData1.getOutputParametersGetAttrIn()
                .get(0).getAttributeName());
            ComputeTemplateConsolidationData computeTemplateConsolidationData2 = consolidationData
                .getComputeConsolidationData()
                .getFileComputeConsolidationData("firstnet_fgi_frwlServiceTemplate.yaml")
                .getTypeComputeConsolidationData("org.openecomp.resource.vfc.nodes.heat.cgi_fw")
                .getComputeTemplateConsolidationData("CGI_FW_SERVER_2");
            Assert.assertEquals(1, computeTemplateConsolidationData2
                .getNodesGetAttrIn().get("CGI_FW_SERVER_2").size());
            Assert.assertEquals("availability_zone", computeTemplateConsolidationData2
                .getNodesGetAttrIn().get("CGI_FW_SERVER_2").get(0).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getNodesGetAttrIn().get("CGI_FW_SERVER_2").get(0).getAttributeName());
            Assert.assertEquals(1, computeTemplateConsolidationData2
                .getNodesGetAttrOut().get("CGI_FW_SERVER_2").size());
            Assert.assertEquals("availability_zone", computeTemplateConsolidationData2
                .getNodesGetAttrOut().get("CGI_FW_SERVER_2").get(0).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getNodesGetAttrOut().get("CGI_FW_SERVER_2").get(0).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_2", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(0).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(0).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_3", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(1).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(1).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_4", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(2).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(2).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_5", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(3).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(3).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_5", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(4).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(4).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_6", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(5).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(5).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_9", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(6).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(6).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_10", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(7).getFieldName());
            Assert.assertEquals("addresses", computeTemplateConsolidationData2
                .getOutputParametersGetAttrIn().get(7).getAttributeName());
            PortTemplateConsolidationData portTemplateConsolidationData = consolidationData
                .getPortConsolidationData().getFilePortConsolidationData("firstnet_fgi_frwlServiceTemplate.yaml")
                .getPortTemplateConsolidationData("contrail_vmi_subinterface");
            Assert.assertEquals("cgi_fw_01_left_mac_7", portTemplateConsolidationData
                .getOutputParametersGetAttrIn().get(0).getFieldName());
            Assert.assertEquals("virtual_machine_interface_properties", portTemplateConsolidationData
                .getOutputParametersGetAttrIn().get(0).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_8", portTemplateConsolidationData
                .getOutputParametersGetAttrIn().get(1).getFieldName());
            Assert.assertEquals("virtual_machine_interface_allowed_address_pairs",
                portTemplateConsolidationData.getOutputParametersGetAttrIn()
                    .get(1).getAttributeName());
            Assert.assertEquals("cgi_fw_01_left_mac_10", portTemplateConsolidationData
                .getOutputParametersGetAttrIn().get(2).getFieldName());
            Assert.assertEquals("virtual_machine_interface_allowed_address_pairs",
                portTemplateConsolidationData.getOutputParametersGetAttrIn()
                    .get(2).getAttributeName());
        }
    }

    public static void validateNestedConsolidationData(TranslationContext context,
                                                       String testName) {
        ConsolidationData consolidationData = context.getConsolidationData();
        if (testName.equals(TEST_SINGLE_NESTED_RESOURCE)) {
            String nestedNodeTemplateId = "server_pcm_001";
            NestedTemplateConsolidationData nestedTemplateConsolidationData =
                consolidationData.getNestedConsolidationData()
                    .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                    .getNestedTemplateConsolidationData(nestedNodeTemplateId);
            //Validate basic null attributes
            validateBasicNestedConsolidationData(nestedTemplateConsolidationData);
            //Validate nodeTemplateId
            Assert.assertEquals(nestedTemplateConsolidationData.getNodeTemplateId(), nestedNodeTemplateId);
            //Validate nodes connected in (will only be populated for dependsOn relationships)
            List<String> dependentNodes = new LinkedList<>();
            dependentNodes.add("packet_mirror_network");

            //Validate get attribute in
            Map<String, List<GetAttrFuncData>> nodesGetAttrIn =
                nestedTemplateConsolidationData.getNodesGetAttrIn();
            String getAttrNodeTemplateId = "server_compute_get_attr_test";
            List<GetAttrFuncData> getAttrFuncData = nodesGetAttrIn.get(getAttrNodeTemplateId);
            Assert.assertNotNull(getAttrFuncData);
            Assert.assertEquals(2, getAttrFuncData.size());
            Assert.assertEquals("metadata", getAttrFuncData.get(0).getFieldName());
            Assert.assertEquals("server_pcm_id", getAttrFuncData.get(0).getAttributeName());
            Assert.assertEquals("user_data_format", getAttrFuncData.get(1).getFieldName());
            Assert.assertEquals("oam_net_gw", getAttrFuncData.get(1).getAttributeName());

            //Validate output parameter get attribute in
            List<GetAttrFuncData> outputParametersGetAttrIn =
                nestedTemplateConsolidationData.getOutputParametersGetAttrIn();
            Assert.assertNotNull(outputParametersGetAttrIn);
            Assert.assertEquals(1, outputParametersGetAttrIn.size());
            Assert.assertEquals("output_attr_1", outputParametersGetAttrIn.get(0).getFieldName());
            Assert.assertEquals("pcm_vol", outputParametersGetAttrIn.get(0).getAttributeName());

        } else if (testName.equals(TEST_MULTIPLE_NESTED_RESOURCE)) {
            List<String> nestedNodeTemplateIds = new ArrayList<>();
            nestedNodeTemplateIds.add("server_pcm_001");
            nestedNodeTemplateIds.add("server_pcm_002");
            nestedNodeTemplateIds.add("server_pcm_003");

            for (String nestedNodeTemplateId : nestedNodeTemplateIds) {
                NestedTemplateConsolidationData nestedTemplateConsolidationData =
                    consolidationData.getNestedConsolidationData()
                        .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                        .getNestedTemplateConsolidationData(nestedNodeTemplateId);
                //Validate basic null attributes
                validateBasicNestedConsolidationData(nestedTemplateConsolidationData);
                //Validate nodeTemplateId
                Assert.assertEquals(nestedTemplateConsolidationData.getNodeTemplateId(),
                    nestedNodeTemplateId);
                if (!nestedNodeTemplateId.equals("server_pcm_001")) {
                    Assert.assertNull(nestedTemplateConsolidationData.getNodesConnectedIn());
                }
            }
            String nestedNodeTemplateId = "server_pcm_001";

            //Validate get attribute in
            NestedTemplateConsolidationData nestedTemplateConsolidationData =
                consolidationData.getNestedConsolidationData()
                    .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                    .getNestedTemplateConsolidationData("server_pcm_002");
            Map<String, List<GetAttrFuncData>> nodesGetAttrIn =
                nestedTemplateConsolidationData.getNodesGetAttrIn();
            String getAttrNodeTemplateId = "server_pcm_001";
            List<GetAttrFuncData> getAttrFuncData = nodesGetAttrIn.get(getAttrNodeTemplateId);
            Assert.assertNotNull(getAttrFuncData);
            Assert.assertEquals(1, getAttrFuncData.size());
            Assert.assertEquals("user_data_format", getAttrFuncData.get(0).getFieldName());
            Assert.assertEquals("pcm_vol", getAttrFuncData.get(0).getAttributeName());
            //Validate output parameter get attribute in
            List<GetAttrFuncData> outputParametersGetAttrIn =
                nestedTemplateConsolidationData.getOutputParametersGetAttrIn();
            Assert.assertNotNull(outputParametersGetAttrIn);
            Assert.assertEquals(1, outputParametersGetAttrIn.size());
            Assert.assertEquals("output_attr_2", outputParametersGetAttrIn.get(0).getFieldName());
            Assert.assertEquals("oam_net_ip", outputParametersGetAttrIn.get(0).getAttributeName());

            nestedTemplateConsolidationData = consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                .getNestedTemplateConsolidationData("server_pcm_001");
            nodesGetAttrIn = nestedTemplateConsolidationData.getNodesGetAttrIn();
            getAttrNodeTemplateId = "server_pcm_002";
            getAttrFuncData = nodesGetAttrIn.get(getAttrNodeTemplateId);
            Assert.assertNotNull(getAttrFuncData);
            Assert.assertEquals(1, getAttrFuncData.size());
            Assert.assertEquals("metadata", getAttrFuncData.get(0).getFieldName());
            Assert.assertEquals("server_pcm_id", getAttrFuncData.get(0).getAttributeName());
            //Validate output parameter get attribute in
            outputParametersGetAttrIn = nestedTemplateConsolidationData.getOutputParametersGetAttrIn();
            Assert.assertNotNull(outputParametersGetAttrIn);
            Assert.assertEquals(1, outputParametersGetAttrIn.size());
            Assert.assertEquals("output_attr_1", outputParametersGetAttrIn.get(0).getFieldName());
            Assert.assertEquals("pcm_vol", outputParametersGetAttrIn.get(0).getAttributeName());

            nestedTemplateConsolidationData = consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                .getNestedTemplateConsolidationData("server_pcm_003");
            Assert.assertNull(nestedTemplateConsolidationData.getNodesGetAttrIn());
            Assert.assertNull(nestedTemplateConsolidationData.getOutputParametersGetAttrIn());

        } else if (testName.equals(TEST_MULTIPLE_MULTI_LEVEL_NESTED_RESOURCE)) {
            String nestedNodeTemplateId = "test_nested";
            NestedTemplateConsolidationData nestedTemplateConsolidationData =
                consolidationData.getNestedConsolidationData()
                    .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                    .getNestedTemplateConsolidationData(nestedNodeTemplateId);
            //Validate basic null attributes
            validateBasicNestedConsolidationData(nestedTemplateConsolidationData);
            //Validate nodeTemplateId
            Assert.assertEquals(nestedTemplateConsolidationData.getNodeTemplateId(),
                nestedNodeTemplateId);
            //Validate nodes connected in (will only be populated for dependsOn relationships)
            Multimap<String, RequirementAssignmentData> nodesConnectedIn =
                nestedTemplateConsolidationData.getNodesConnectedIn();
            List<String> dependentNodes = new LinkedList<>();
            dependentNodes.add("packet_mirror_network");
            //Validate output parameter get attribute in
            List<GetAttrFuncData> getAttrFuncData =
                nestedTemplateConsolidationData.getNodesGetAttrIn().get("packet_mirror_network");
            Assert.assertNotNull(getAttrFuncData);
            Assert.assertEquals(1, getAttrFuncData.size());
            Assert.assertEquals("shared", getAttrFuncData.get(0).getFieldName());
            Assert.assertEquals("output_attr_1", getAttrFuncData.get(0).getAttributeName());
            Assert.assertNull(nestedTemplateConsolidationData.getOutputParametersGetAttrIn());

            nestedNodeTemplateId = "test_nested2";
            nestedTemplateConsolidationData = consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData("nestedServiceTemplate.yaml")
                .getNestedTemplateConsolidationData(nestedNodeTemplateId);
            //Validate basic null attributes
            validateBasicNestedConsolidationData(nestedTemplateConsolidationData);
            //Validate nodeTemplateId
            Assert.assertEquals(nestedTemplateConsolidationData.getNodeTemplateId(),
                nestedNodeTemplateId);
            Assert.assertNull(nestedTemplateConsolidationData.getNodesConnectedIn());
            //Validate output parameter get attribute in
            getAttrFuncData = nestedTemplateConsolidationData.getNodesGetAttrIn().get("server_cmaui_1");
            Assert.assertNotNull(getAttrFuncData);
            Assert.assertEquals(1, getAttrFuncData.size());
            Assert.assertEquals("metadata", getAttrFuncData.get(0).getFieldName());
            Assert.assertEquals("availability_zone_0", getAttrFuncData.get(0).getAttributeName());

            List<GetAttrFuncData> outputParametersGetAttrIn1 =
                nestedTemplateConsolidationData.getOutputParametersGetAttrIn();
            Assert.assertNotNull(outputParametersGetAttrIn1);
            Assert.assertEquals(1, outputParametersGetAttrIn1.size());
            Assert.assertEquals("output_attr_1", outputParametersGetAttrIn1.get(0).getFieldName());
            Assert.assertEquals("availability_zone_0", outputParametersGetAttrIn1.get(0).getAttributeName());
        }
    }

    private static void validateBasicNestedConsolidationData(NestedTemplateConsolidationData
                                                                 nestedTemplateConsolidationData) {
        Assert.assertNull(nestedTemplateConsolidationData.getGroupIds());
        Assert.assertNull(nestedTemplateConsolidationData.getNodesConnectedOut());
    }

    public static void validateNestedNodesConnectedInSecurityRuleToPort(String testName, TranslationContext context) {
        ConsolidationData consolidationData = context.getConsolidationData();
        if (testName.equals(TEST_SECURITY_RULE_PORT_NESTED_CONNECTION) ||
            testName.equals(TestConstants.TEST_SECURITY_RULE_PORT_NESTED_SHARED_PORT)) {
            String nestedNodeTemplateId = "test_nested";
            NestedTemplateConsolidationData nestedTemplateConsolidationData =
                consolidationData.getNestedConsolidationData()
                    .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                    .getNestedTemplateConsolidationData(nestedNodeTemplateId);
            //Validate basic null attributes
            validateBasicNestedConsolidationData(nestedTemplateConsolidationData);
            //Validate nodeTemplateId
            Assert
                .assertEquals(nestedTemplateConsolidationData.getNodeTemplateId(), nestedNodeTemplateId);
            String securityRuleNodeTemplateId = "jsa_security_group1";
            validateNestedNodesConnectedInSecurityRuleToPort(HeatToToscaUtil
                    .getServiceTemplateFromContext(MAIN_SERVICE_TEMPLATE, context).get(),
                nestedNodeTemplateId, securityRuleNodeTemplateId,
                nestedTemplateConsolidationData);
            securityRuleNodeTemplateId = "jsa_security_group2";
            validateNestedNodesConnectedInSecurityRuleToPort(HeatToToscaUtil
                    .getServiceTemplateFromContext(MAIN_SERVICE_TEMPLATE, context).get(),
                nestedNodeTemplateId, securityRuleNodeTemplateId,
                nestedTemplateConsolidationData);
            if (testName.equals(TestConstants.TEST_SECURITY_RULE_PORT_NESTED_SHARED_PORT)) {
                nestedNodeTemplateId = "test_nestedArrayParam";
                Assert.assertEquals(nestedNodeTemplateId, consolidationData.getNestedConsolidationData()
                    .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                    .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodeTemplateId());
                Assert.assertNull(consolidationData.getNestedConsolidationData()
                    .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                    .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodesConnectedIn());
            }
        } else if (testName.equals(TEST_SECURITY_RULE_PORT_MULTI_LEVEL_NESTED_CONNECTION) ||
            testName.equals(TEST_SECURITY_RULE_PORT_MULTI_LEVEL_NESTED_SHARED_PORT)) {
            String nestedNodeTemplateId = "test_nested2Level";
            Assert.assertEquals(nestedNodeTemplateId, consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData("nested1ServiceTemplate.yaml")
                .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodeTemplateId());
            Assert.assertNull(consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData("nested1ServiceTemplate.yaml")
                .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodesConnectedIn());
            nestedNodeTemplateId = "test_nested3Level";
            Assert.assertEquals(nestedNodeTemplateId, consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData("nested2ServiceTemplate.yaml")
                .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodeTemplateId());
            Assert.assertNull(consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData("nested2ServiceTemplate.yaml")
                .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodesConnectedIn());
            nestedNodeTemplateId = "test_nested4Level";
            Assert.assertEquals(nestedNodeTemplateId, consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData("nested3ServiceTemplate.yaml")
                .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodeTemplateId());
            Assert.assertNull(consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData("nested3ServiceTemplate.yaml")
                .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodesConnectedIn());

            //Validate main service template

            nestedNodeTemplateId = "test_nested1Level";
            NestedTemplateConsolidationData nestedTemplateConsolidationData =
                consolidationData.getNestedConsolidationData()
                    .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                    .getNestedTemplateConsolidationData(nestedNodeTemplateId);
            //Validate basic null attributes
            validateBasicNestedConsolidationData(nestedTemplateConsolidationData);
            //Validate nodeTemplateId
            Assert
                .assertEquals(nestedTemplateConsolidationData.getNodeTemplateId(), nestedNodeTemplateId);
            String securityRuleNodeTemplateId = "jsa_security_group1";
            validateNestedNodesConnectedInSecurityRuleToPort(HeatToToscaUtil
                    .getServiceTemplateFromContext(MAIN_SERVICE_TEMPLATE, context).get(),
                nestedNodeTemplateId, securityRuleNodeTemplateId,
                nestedTemplateConsolidationData);
            securityRuleNodeTemplateId = "jsa_security_group2";
            validateNestedNodesConnectedInSecurityRuleToPort(HeatToToscaUtil
                    .getServiceTemplateFromContext(MAIN_SERVICE_TEMPLATE, context).get(),
                nestedNodeTemplateId, securityRuleNodeTemplateId,
                nestedTemplateConsolidationData);

            nestedNodeTemplateId = "test_resourceGroup";
            nestedTemplateConsolidationData = consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                .getNestedTemplateConsolidationData(nestedNodeTemplateId);
            //Validate basic null attributes
            validateBasicNestedConsolidationData(nestedTemplateConsolidationData);
            //Validate nodeTemplateId
            Assert
                .assertEquals(nestedTemplateConsolidationData.getNodeTemplateId(), nestedNodeTemplateId);
            securityRuleNodeTemplateId = "jsa_security_group2";
            validateNestedNodesConnectedInSecurityRuleToPort(HeatToToscaUtil
                    .getServiceTemplateFromContext(MAIN_SERVICE_TEMPLATE, context).get(),
                nestedNodeTemplateId, securityRuleNodeTemplateId,
                nestedTemplateConsolidationData);
            securityRuleNodeTemplateId = "jsa_security_group2";
            validateNestedNodesConnectedInSecurityRuleToPort(HeatToToscaUtil
                    .getServiceTemplateFromContext(MAIN_SERVICE_TEMPLATE, context).get(),
                nestedNodeTemplateId, securityRuleNodeTemplateId,
                nestedTemplateConsolidationData);

            nestedNodeTemplateId = "test_nestedInvalidConnectionToNova";
            Assert.assertEquals(nestedNodeTemplateId, consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodeTemplateId());
            Assert.assertNull(consolidationData.getNestedConsolidationData()
                .getFileNestedConsolidationData(MAIN_SERVICE_TEMPLATE)
                .getNestedTemplateConsolidationData(nestedNodeTemplateId).getNodesConnectedIn());
        }
    }

    private static void validateNestedNodesConnectedInSecurityRuleToPort(ServiceTemplate serviceTemplate,
                                                                         String nestedNodeTemplateId,
                                                                         String securityRuleNodeTemplateId,
                                                                         NestedTemplateConsolidationData
                                                                             nestedTemplateConsolidationData) {
        Multimap<String, RequirementAssignmentData> consolidationDataNodesConnectedIn =
            nestedTemplateConsolidationData.getNodesConnectedIn();
        Assert.assertNotNull(consolidationDataNodesConnectedIn);
        NodeTemplate securityRuleNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate,
            securityRuleNodeTemplateId);
        List<Map<String, RequirementAssignment>> securityRuleNodeTemplateRequirements =
            securityRuleNodeTemplate.getRequirements();
        for (Map<String, RequirementAssignment> req : securityRuleNodeTemplateRequirements) {
            String requirementId = req.keySet().toArray()[0].toString();
            if (requirementId.equals(ToscaConstants.PORT_REQUIREMENT_ID)) {
                RequirementAssignment requirementAssignment = req.get(requirementId);
                if (requirementAssignment.getNode().equals(nestedNodeTemplateId)) {
                    validateSecurityRulePortNestedConsolidationData(requirementAssignment,
                        securityRuleNodeTemplateId, consolidationDataNodesConnectedIn);
                }
            }
        }
    }

    private static void validateSecurityRulePortNestedConsolidationData(RequirementAssignment requirementAssignment,
                                                                        String securityRuleNodeTemplateId,
                                                                        Multimap<String, RequirementAssignmentData>
                                                                            consolidationDataNodesConnectedIn) {
        Collection<RequirementAssignmentData> requirementAssignmentDataList =
            consolidationDataNodesConnectedIn.get(securityRuleNodeTemplateId);
        Assert.assertNotNull(requirementAssignmentDataList);
        boolean result = false;
        for (RequirementAssignmentData data : requirementAssignmentDataList) {
            RequirementAssignment dataRequirementAssignment = data.getRequirementAssignment();
            result = DataModelUtil
                .compareRequirementAssignment(requirementAssignment, dataRequirementAssignment);
            if (result) {
                break;
            }
        }
        Assert.assertTrue(result);
    }
}
