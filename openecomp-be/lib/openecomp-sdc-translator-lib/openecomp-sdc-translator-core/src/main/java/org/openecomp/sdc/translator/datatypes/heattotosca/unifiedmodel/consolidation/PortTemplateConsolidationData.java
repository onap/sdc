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
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PortTemplateConsolidationData extends EntityConsolidationData {

    // key - sub-interface type - for ResourceGroup it is the nested file name
    // value - List of sub-interfaces of that type in the port
    private final ListMultimap<String, SubInterfaceTemplateConsolidationData> subInterfaceConsolidationData =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    private String networkRole;

    public String getNetworkRole() {
        return networkRole;
    }

    public void setNetworkRole(String networkRole) {
        this.networkRole = networkRole;
    }

    public boolean isPortBoundToSubInterface() {
        return !subInterfaceConsolidationData.isEmpty();
    }

    /**
    * Create Sub interface template consolidation data base on given parameters - if it doesn't exist yet.
    *
    * @param resource resource of sub Interface
    * @param subInterfaceNodeTemplateId template id of sub interface
    * @param parentPortNodeTemplateId node template id of port node to which sub interface is related
    * @return sub interface template consolidation data entity
    */
    public SubInterfaceTemplateConsolidationData addSubInterfaceTemplateConsolidationData(
            Resource resource, String subInterfaceNodeTemplateId, String parentPortNodeTemplateId) {
        String subInterfaceType = createSubInterfaceType(resource);
        List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
                subInterfaceConsolidationData.get(subInterfaceType);
        SubInterfaceTemplateConsolidationData consolidationData = null;
        if (CollectionUtils.isNotEmpty(subInterfaceTemplateConsolidationDataList)) {
            Optional<SubInterfaceTemplateConsolidationData> optionalConsolidationData =
                    subInterfaceTemplateConsolidationDataList.stream()
                    .filter(s -> s.getNodeTemplateId().equals(subInterfaceNodeTemplateId))
                    .findFirst();
            if (optionalConsolidationData.isPresent()) {
                consolidationData = optionalConsolidationData.get();
            }
        }

        if (Objects.isNull(consolidationData)) {
            consolidationData =
                    createSubInterfaceConsolidationData(subInterfaceNodeTemplateId, parentPortNodeTemplateId);
            addSubInterfaceConsolidationData(subInterfaceType, consolidationData);
        }
        return consolidationData;
    }

    private String createSubInterfaceType(Resource resource) {
        return ToscaNodeType.VLAN_SUB_INTERFACE_RESOURCE_TYPE_PREFIX
            + FileUtils.getFileWithoutExtention(
            HeatToToscaUtil.getSubInterfaceResourceType(resource));
    }

    private SubInterfaceTemplateConsolidationData createSubInterfaceConsolidationData(
            String subInterfaceNodeTemplateId, String parentPortNodeTemplateId) {
        SubInterfaceTemplateConsolidationData data = new SubInterfaceTemplateConsolidationData();
        data.setNodeTemplateId(subInterfaceNodeTemplateId);
        data.setParentPortNodeTemplateId(parentPortNodeTemplateId);
        return data;
    }

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
        return isBothSubInterfaceConsolidationDataEmpty(this, other)
               || isBothSubInterfaceConsolidationDataNotEmpty(this, other)
                 && this.subInterfaceConsolidationData.keySet().stream().allMatch(
                         subInterfaceType -> calculateSize(other.subInterfaceConsolidationData.get(subInterfaceType))
                         ==  calculateSize(this.subInterfaceConsolidationData.get(subInterfaceType)));

    }

    private boolean isBothSubInterfaceConsolidationDataEmpty(
            PortTemplateConsolidationData object, PortTemplateConsolidationData other) {
        return object.subInterfaceConsolidationData.isEmpty() && other.subInterfaceConsolidationData.isEmpty();
    }

    private boolean isBothSubInterfaceConsolidationDataNotEmpty(
            PortTemplateConsolidationData object, PortTemplateConsolidationData other) {
        return !object.subInterfaceConsolidationData.isEmpty() && !other.subInterfaceConsolidationData.isEmpty();
    }

    public boolean isSubInterfaceNodeTemplateIdParameter(String nodeTemplateType) {
        List<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationDataList =
                this.subInterfaceConsolidationData.get(nodeTemplateType);
        return (Objects.nonNull(subInterfaceTemplateConsolidationDataList)
            && subInterfaceTemplateConsolidationDataList.size() > 1);
    }

    private int calculateSize(List<SubInterfaceTemplateConsolidationData> subInterfaces) {
        return subInterfaces == null ? 0 : subInterfaces.size();
    }
}