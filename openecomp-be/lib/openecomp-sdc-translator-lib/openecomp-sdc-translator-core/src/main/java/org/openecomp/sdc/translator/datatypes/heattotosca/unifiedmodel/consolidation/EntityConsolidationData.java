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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;

/**
 * The type Entity consolidation data.
 */
public class EntityConsolidationData {

    private String nodeTemplateId;

    //groups that point to this entity node template
    private List<String> groupIds;

    // key - node template id which has connection to this entity
    // value - List of Requirement assignment data which connect to this entity
    private Map<String, List<RequirementAssignmentData>> nodesConnectedIn;

    // key - node template id which connected from this entity
    // List of Requirement assignment data which connect to the key node template id
    private Map<String, List<RequirementAssignmentData>> nodesConnectedOut;

    //key - node template id which include get attribute function from this entity
    //value - List of getAttr data
    private Map<String, List<GetAttrFuncData>> nodesGetAttrIn;

    //key - node template id which is pointed by this entity using get attribute function
    //value - List of getAttr data
    private Map<String, List<GetAttrFuncData>> nodesGetAttrOut;

    //List of getAttr data
    private List<GetAttrFuncData> outputParametersGetAttrIn;

    public String getNodeTemplateId() {
        return nodeTemplateId;
    }

    public void setNodeTemplateId(String nodeTemplateId) {
        this.nodeTemplateId = nodeTemplateId;
    }

    /**
     * Gets group ids point to me.
     *
     * @return the group ids point to me
     */
    public List<String> getGroupIds() {
        return groupIds;
    }

    /**
     * Sets group ids point to me.
     *
     * @param groupIds the group ids point to me
     */
    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }


    /**
     * Sets node connected to me.
     *
     * @param nodesConnectedIn the node connected to me
     */
    public void setNodesConnectedIn(Map<String, List<RequirementAssignmentData>> nodesConnectedIn) {
        this.nodesConnectedIn = nodesConnectedIn;
    }

    /**
     * Add node connected to me.
     *
     * @param nodeTemplateId        the node template id which has connection to me
     * @param requirementId         the requirement id
     * @param requirementAssignment the requirement assignment
     */
    public void addNodesConnectedIn(String nodeTemplateId, String requirementId,
                                           RequirementAssignment requirementAssignment) {

        if (this.nodesConnectedIn == null) {
            this.nodesConnectedIn = new HashMap<>();
        }

        this.nodesConnectedIn.computeIfAbsent(nodeTemplateId, k -> new ArrayList<>());
        this.nodesConnectedIn.get(nodeTemplateId).add(
                new RequirementAssignmentData(requirementId, requirementAssignment));
    }

    /**
     * Gets node connected to me.
     *
     * @return the node connected to me
     */
    public Map<String, List<RequirementAssignmentData>> getNodesConnectedIn() {
        return nodesConnectedIn;
    }


    /**
     * Gets node connected from me.
     *
     * @return the node connected from me
     */
    public Map<String, List<RequirementAssignmentData>> getNodesConnectedOut() {
        return nodesConnectedOut;
    }

    /**
     * Sets node connected from me.
     *
     * @param nodesConnectedOut the node connected from me
     */
    public void setNodesConnectedOut(Map<String, List<RequirementAssignmentData>> nodesConnectedOut) {
        this.nodesConnectedOut = nodesConnectedOut;
    }

    /**
     * Add nodeConnectedOut.
     *
     * @param nodeTemplateId        the node template id which is connected from me
     * @param requirementId         the requirement id
     * @param requirementAssignment the requirement assignment
     */
    public void addNodesConnectedOut(String nodeTemplateId, String requirementId,
                                            RequirementAssignment requirementAssignment) {

        if (this.nodesConnectedOut == null) {
            this.nodesConnectedOut = new HashMap<>();
        }

        this.nodesConnectedOut.computeIfAbsent(nodeTemplateId, k -> new ArrayList<>());
        this.nodesConnectedOut.get(nodeTemplateId).add(
                new RequirementAssignmentData(requirementId, requirementAssignment));
    }

    /**
     * Gets nodes get attr in.
     *
     * @return the get attr in
     */
    public Map<String, List<GetAttrFuncData>> getNodesGetAttrIn() {
        return nodesGetAttrIn;
    }

    /**
     * Sets nodes get attr in.
     *
     * @param nodesGetAttrIn the get attr in
     */
    public void setNodesGetAttrIn(Map<String, List<GetAttrFuncData>> nodesGetAttrIn) {
        this.nodesGetAttrIn = nodesGetAttrIn;
    }

    /**
     * Add nodes get attr in data.
     *
     * @param nodeTemplateId  the node template id
     * @param getAttrFuncData get attr data
     */
    public void addNodesGetAttrIn(String nodeTemplateId, GetAttrFuncData getAttrFuncData) {

        if (nodesGetAttrIn == null) {
            nodesGetAttrIn = new HashMap<>();
        }

        this.nodesGetAttrIn.putIfAbsent(nodeTemplateId, new ArrayList<>());
        this.nodesGetAttrIn.get(nodeTemplateId).add(getAttrFuncData);
    }

    /**
     * Gets output parameters get attr from me.
     *
     * @return the get attr from me
     */
    public List<GetAttrFuncData> getOutputParametersGetAttrIn() {
        return outputParametersGetAttrIn;
    }

    /**
     * Sets output parameters get attr from me.
     *
     * @param outputParametersGetAttrIn the output parameters get attr from me
     */
    public void setOutputParametersGetAttrIn(List<GetAttrFuncData> outputParametersGetAttrIn) {
        this.outputParametersGetAttrIn = outputParametersGetAttrIn;
    }

    /**
     * Add output parameters get attr data.
     *
     * @param getAttrFuncData get attr data
     */
    public void addOutputParamGetAttrIn(GetAttrFuncData getAttrFuncData) {

        if (outputParametersGetAttrIn == null) {
            outputParametersGetAttrIn = new ArrayList<>();
        }

        this.outputParametersGetAttrIn.add(getAttrFuncData);
    }

    /**
     * Gets nodes get attr out.
     *
     * @return the get attr out
     */
    public Map<String, List<GetAttrFuncData>> getNodesGetAttrOut() {
        return nodesGetAttrOut;
    }

    /**
     * Sets nodes get attr out.
     *
     * @param nodesGetAttrOut the get attr out
     */
    public void setNodesGetAttrOut(Map<String, List<GetAttrFuncData>> nodesGetAttrOut) {
        this.nodesGetAttrOut = nodesGetAttrOut;
    }

    /**
     * Add nodes get attr out data.
     *
     * @param nodeTemplateId  the node template id
     * @param getAttrFuncData get attr data
     */
    public void addNodesGetAttrOut(String nodeTemplateId, GetAttrFuncData getAttrFuncData) {

        if (nodesGetAttrOut == null) {
            nodesGetAttrOut = new HashMap<>();
        }

        this.nodesGetAttrOut.putIfAbsent(nodeTemplateId, new ArrayList<>());
        this.nodesGetAttrOut.get(nodeTemplateId).add(getAttrFuncData);
    }

    public void removeParamNameFromAttrFuncList(String paramName) {

        if (outputParametersGetAttrIn == null) {
            return;
        }

        outputParametersGetAttrIn.removeIf(outputParameters -> paramName.equals(outputParameters.getFieldName()));
    }

    /**
     * Per port type compare get attr of entity with given consolidation data list.
     *
     * @param entityConsolidationDataList consolidation data list
     * @param portTypeToIds               the port type to ids
     * @return true in case get attr list same for all port types.
     *         otherwise return false
     */
    public boolean isGetAttrOutFromEntityLegal(Collection<? extends EntityConsolidationData>
                    entityConsolidationDataList, Map<String, List<String>> portTypeToIds) {
        if (CollectionUtils.isEmpty(entityConsolidationDataList)
                || MapUtils.isEmpty(portTypeToIds)) {
            return true;
        }

        for (String portType : portTypeToIds.keySet()) {
            Set<GetAttrFuncData> startingGetAttrFunc =
                    getEntityGetAttrFuncAsSet(portType);

            for (EntityConsolidationData entity : entityConsolidationDataList) {
                Set<GetAttrFuncData> currentGetAttrFuncData =
                        entity.getEntityGetAttrFuncAsSet(portType);
                if (!(startingGetAttrFunc.equals(currentGetAttrFuncData))) {
                    return false;
                }
            }
        }
        return true;
    }

    private Set<GetAttrFuncData> getEntityGetAttrFuncAsSet(String portType) {
        if (MapUtils.isEmpty(nodesGetAttrOut)) {
            return new HashSet<>();
        }

        return nodesGetAttrOut.entrySet().stream()
                              .filter(entry -> portType.equals(ConsolidationDataUtil.getPortType(entry.getKey())))
                              .flatMap(entry -> entry.getValue().stream())
                              .collect(Collectors.toSet());
    }

    /**
     * Add group id information to consolidation data.
     *
     * @param groupId       Group id of which compute node is a part
     */

    void addGroupId(String groupId) {
        if (groupIds == null) {
            groupIds = new ArrayList<>();
        }
        groupIds.add(groupId);
    }
}
