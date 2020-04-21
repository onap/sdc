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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.vavr.collection.HashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.enums.GroupTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.GroupDefinition;

public class GroupVersionUpdaterTest {

    @Test
    @DisplayName("emptyIfNull(List<T>) - should return the same List passed as an argument if it is not null")
    public void safeAccessList_NotNull() {
        //Given
        List<String> arg = defaultArtifactsUuid.get();

        //When
        List<String> result = GroupVersionUpdater.emptyIfNull(arg);

        //Then
        assertSame(arg, result);
    }

    @Test
    @DisplayName("emptyIfNull(List<T>) - should return an empty List if we pass it a null value")
    public void safeAccessList_Null() {
        //Given
        List<String> arg = null;

        //When
        List<String> result = GroupVersionUpdater.emptyIfNull(arg);

        //Then
        assertSame(Collections.EMPTY_LIST, result);
    }

    @Test
    @DisplayName("emptyIfNull(Map<T, U>) - should return the same Map passed as an argument if it is not null")
    public void safeAccessMap_NotNull() {
        //Given
        Map<String, ArtifactDefinition> arg = defaultDeploymentArtifacts.get();

        //When
        Map<String, ArtifactDefinition> result = GroupVersionUpdater.emptyIfNull(arg);

        //Then
        assertSame(arg, result);
    }

    @Test
    @DisplayName("emptyIfNull(Map<T, U>) - should return an empty Map if we pass it a null value")
    public void safeAccessMap_Null() {
        //Given
        Map<String, ArtifactDefinition> arg = null;

        //When
        Map<String, ArtifactDefinition> result = GroupVersionUpdater.emptyIfNull(arg);

        //Then
        assertSame(Collections.EMPTY_MAP, result);
    }


    @Test
    @DisplayName("isGenerateGroupUUID - Return true if there is a deployment artifact in the container that is not"
        + " part of the group")
    public void isGenerateGroupUUID_GoldenPath() {
        // Given
        Supplier<String> type = defaultGroupType;
        Supplier<List<String>> artifactsUuid = defaultArtifactsUuid;
        GroupDefinition group = getGroup(
            type,
            artifactsUuid
        );

        Supplier<Map<String, ArtifactDefinition>> deploymentArtifacts = defaultDeploymentArtifacts;
        Component container = getContainer(deploymentArtifacts);

        // When
        boolean result = GroupVersionUpdater.isGenerateGroupUUID(group, container);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("isGenerateGroupUUID - `group`'s type is not GroupTypeEnum.VF_MODULE")
    public void isGenerateGroupUUID_GroupIsNotVFModule() {
        // Given
        Supplier<String> type = GroupTypeEnum.HEAT_STACK::getGroupTypeName;
        Supplier<List<String>> artifactsUuid = defaultArtifactsUuid;
        GroupDefinition group = getGroup(
            type,
            artifactsUuid
        );

        Supplier<Map<String, ArtifactDefinition>> deploymentArtifacts = defaultDeploymentArtifacts;
        Component container = getContainer(deploymentArtifacts);

        // When
        boolean result = GroupVersionUpdater.isGenerateGroupUUID(group, container);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("isGenerateGroupUUID - `group`'s type is null")
    public void isGenerateGroupUUID_NullGroupType() {
        // Given
        Supplier<String> type = () -> null;
        Supplier<List<String>> artifactsUuid = defaultArtifactsUuid;
        GroupDefinition group = getGroup(
            type,
            artifactsUuid
        );

        Supplier<Map<String, ArtifactDefinition>> deploymentArtifacts = defaultDeploymentArtifacts;
        Component container = getContainer(deploymentArtifacts);

        // When
        boolean result = GroupVersionUpdater.isGenerateGroupUUID(group, container);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("isGenerateGroupUUID - `container`'s DeploymentArtifacts is null")
    public void isGenerateGroupUUID_NullDeploymentArtifacts() {
        // Given
        Supplier<String> type = defaultGroupType;
        Supplier<List<String>> artifactsUuid = defaultArtifactsUuid;
        GroupDefinition group = getGroup(
            type,
            artifactsUuid
        );

        Supplier<Map<String, ArtifactDefinition>> deploymentArtifacts = () -> null;
        Component container = getContainer(deploymentArtifacts);

        // When
        boolean result = GroupVersionUpdater.isGenerateGroupUUID(group, container);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("isGenerateGroupUUID - `group`'s artifactUuid is null")
    public void isGenerateGroupUUID_NullGroupArtifactUuid() {
        // Given
        Supplier<String> type = defaultGroupType;
        Supplier<List<String>> artifactsUuid = () -> null;
        GroupDefinition group = getGroup(
            type,
            artifactsUuid
        );

        Supplier<Map<String, ArtifactDefinition>> deploymentArtifacts = defaultDeploymentArtifacts;
        Component container = getContainer(deploymentArtifacts);

        // When
        boolean result = GroupVersionUpdater.isGenerateGroupUUID(group, container);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("isGenerateGroupUUID - `group`'s artifactUuid only has artifactUuid ending with 'env'")
    public void isGenerateGroupUUID_OnlyArtifactEndingWithEnv() {
        // Given
        Supplier<String> type = defaultGroupType;
        Supplier<List<String>> artifactsUuid = () ->
            defaultArtifactsUuid.get().stream()
                .map(str -> str + ".env")
                .collect(Collectors.toList());
        GroupDefinition group = getGroup(
            type,
            artifactsUuid
        );

        Supplier<Map<String, ArtifactDefinition>> deploymentArtifacts = defaultDeploymentArtifacts;
        Component container = getContainer(deploymentArtifacts);

        // When
        boolean result = GroupVersionUpdater.isGenerateGroupUUID(group, container);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("isGenerateGroupUUID - `group`'s artifactUuid is not part of the container's list of deployment artifacts")
    public void isGenerateGroupUUID_NoMatchingUuidInContainer() {
        // Given
        Supplier<String> type = defaultGroupType;
        Supplier<List<String>> artifactsUuid = defaultArtifactsUuid;
        GroupDefinition group = getGroup(
            type,
            artifactsUuid
        );

        Supplier<Map<String, ArtifactDefinition>> deploymentArtifacts = defaultDeploymentArtifacts;
        Component container = getContainer(deploymentArtifacts);

        // When
        boolean result = GroupVersionUpdater.isGenerateGroupUUID(group, container);

        // Then
        assertTrue(result);
    }

    Supplier<String> defaultGroupType = GroupTypeEnum.VF_MODULE::getGroupTypeName;

    Supplier<List<String>> defaultArtifactsUuid = () -> Arrays.asList(
        "uniqueId1.1",
        "uniqueId2.2",
        "uniqueId3.3",
        "uniqueId4.4",
        "uniqueId5.5",
        "uniqueId6.6",
        "uniqueId7.7",
        "uniqueId8.8",
        "uniqueId9.9"
    );

    Supplier<Map<String, ArtifactDefinition>> defaultDeploymentArtifacts = () -> HashMap.of(
        "uniqueId1.1", getArtifactDefinition("uniqueId1.1"),
        "uniqueId2.2", getArtifactDefinition("uniqueId2.2"),
        "uniqueId3.3", getArtifactDefinition("uniqueId3.3"),
        "uniqueId44", getArtifactDefinition("uniqueId4.4"),
        "uniqueId5.5", getArtifactDefinition("uniqueId5.5"),
        "uniqueId6.6", getArtifactDefinition("uniqueId6.6"),
        "uniqueId7.7", getArtifactDefinition("uniqueId7.7"),
        "uniqueId8.8", getArtifactDefinition("uniqueId8.8"),
        "uniqueId9.9", getArtifactDefinition("uniqueId9.9"),
        "uniqueId.not.found", getArtifactDefinition("uniqueId.not.found")
    ).toJavaMap();

    ArtifactDefinition getArtifactDefinition(String artifactUuid) {
        return new ArtifactDefinition() {
            @Override
            public String getArtifactUUID() {
                return artifactUuid;
            }
        };
    }

    GroupDefinition getGroup(Supplier<String> type, Supplier<List<String>> artifacts) {
        return new GroupDefinition() {
            @Override
            public String getType() {
                return type.get();
            }

            @Override
            public List<String> getArtifacts() {
                return artifacts.get();
            }
        };
    }

    Component getContainer(Supplier<Map<String, ArtifactDefinition>> deploymentArtifacts) {
        return new Component(null) {
            @Override
            public Map<String, ArtifactDefinition> getDeploymentArtifacts() {
                return deploymentArtifacts.get();
            }
        };
    }
}
