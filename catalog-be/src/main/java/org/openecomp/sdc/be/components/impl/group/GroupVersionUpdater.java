/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.components.impl.group;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.components.impl.version.OnChangeVersionCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.GroupTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.utils.GroupUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;

/**
 * A Helper class which handles altering the version of a group
 */
@org.springframework.stereotype.Component
public class GroupVersionUpdater implements OnChangeVersionCommand {

    private static final Logger log = Logger.getLogger(GroupVersionUpdater.class);
    private final GroupsOperation groupsOperation;
    private final ComponentsUtils componentsUtils;

    public GroupVersionUpdater(GroupsOperation groupsOperation, ComponentsUtils componentsUtils) {
        this.groupsOperation = groupsOperation;
        this.componentsUtils = componentsUtils;
    }

    static <T> List<T> emptyIfNull(final List<T> list) {
        return (list == null ? Collections.emptyList() : list);
    }

    static <T, U> Map<T, U> emptyIfNull(final Map<T, U> list) {
        return (list == null ? Collections.emptyMap() : list);
    }

    static boolean isGenerateGroupUUID(GroupDefinition group, Component container) {
        if (!GroupTypeEnum.VF_MODULE.getGroupTypeName().equals(group.getType())) {
            return true;
        } else {
            List<String> artifactsUuid = emptyIfNull(group.getArtifactsUuid());
            Map<String, ArtifactDefinition> deploymentArtifacts = emptyIfNull(container.getDeploymentArtifacts());
            List<String> heatArtifactUniqueIDs = emptyIfNull(group.getArtifacts()).stream().filter(a -> !a.endsWith("env"))
                .collect(Collectors.toList());
            for (String heatArtifactUniqueID : heatArtifactUniqueIDs) {
                ArtifactDefinition artifactDefinition = deploymentArtifacts.get(heatArtifactUniqueID.split("\\.", -1)[1]);
                if ((artifactDefinition == null || artifactDefinition.isEmpty()) || !artifactsUuid.contains(artifactDefinition.getArtifactUUID())) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public ActionStatus onChangeVersion(Component container) {
        log.debug("#onChangeVersion - replacing all group members for component instance");
        Consumer<List<GroupDefinition>> replaceGroupMemberTask = (groups) -> increaseVersion(groups, container);
        return updateGroupsVersion(container, replaceGroupMemberTask);
    }

    public void increaseVersion(List<GroupDefinition> groups, Component container) {
        groups.forEach(group -> increaseMajorVersion(group, container));
    }

    private void increaseMajorVersion(GroupDefinition group, Component container) {
        String version = group.getVersion();
        String newVersion = GroupUtils.updateVersion(PromoteVersionEnum.MAJOR, group.getVersion());
        if (!version.equals(newVersion)) {
            if (isGenerateGroupUUID(group, container)) {
                String groupUUID = UniqueIdBuilder.generateUUID();
                group.setGroupUUID(groupUUID);
            }
            group.setVersion(String.valueOf(newVersion));
        }
    }

    private ActionStatus updateGroupsVersion(Component groupsContainer, Consumer<List<GroupDefinition>> updateGroupVersion) {
        List<GroupDefinition> groups = groupsContainer.getGroups();
        if (isEmpty(groups)) {
            return ActionStatus.OK;
        }
        updateGroupVersion.accept(groups);
        return updateGroups(groupsContainer.getUniqueId(), groups);
    }

    private ActionStatus updateGroups(String componentId, List<GroupDefinition> groupsToUpdate) {
        log.debug("#updateGroups - updating {} groups for container {}", groupsToUpdate.size(), componentId);
        return componentsUtils.convertFromStorageResponse(groupsOperation.updateGroupsOnComponent(componentId, groupsToUpdate));
    }
}
