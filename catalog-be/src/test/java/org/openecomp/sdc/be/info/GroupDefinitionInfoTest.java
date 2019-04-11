/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.info;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import com.google.code.beanmatchers.BeanMatchers;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

public class GroupDefinitionInfoTest {

    private static final String ARTIFACT_NAME = "artifactName";
    private static final String TO_STRING_REGEXP = "GroupDefinitionInfo \\[org.openecomp.sdc.be.info.GroupDefinitionInfo@" +
        ".*" + ", isBase=true, artifacts=\\[org.openecomp.sdc.be.info.ArtifactDefinitionInfo@" + ".*" + "\\]\\]";
    private static final String GROUP_UUID = "GROUP_UUID";
    private static final String INVARIANT_UUID = "INVARIANT_UUID";
    private static final String VERSION = "VERSION";
    private static final String UNIQUE_ID = "UNIQUE_ID";
    private static final String DESC = "DESC";
    private static final String NAME = "NAME";

    @Test
    public void shouldHaveValidDefaultConstructor() {
        assertThat(GroupDefinitionInfo.class, hasValidBeanConstructor());
    }

    @Test
    public void testShouldConstructObjectFromGroupDefinition() {
        GroupDefinition groupDefinition = createGroupDefinition();
        GroupDefinitionInfo groupDefinitionInfo = new GroupDefinitionInfo(groupDefinition);
        assertThat(groupDefinitionInfo.getName(), is(groupDefinition.getName()));
        assertThat(groupDefinitionInfo.getDescription(), is(groupDefinition.getDescription()));
        assertThat(groupDefinitionInfo.getUniqueId(), is(groupDefinition.getUniqueId()));
        assertThat(groupDefinitionInfo.getVersion(), is(groupDefinition.getVersion()));
        assertThat(groupDefinitionInfo.getInvariantUUID(), is(groupDefinition.getInvariantUUID()));
        assertThat(groupDefinitionInfo.getGroupUUID(), is(groupDefinition.getGroupUUID()));
    }

    @Test
    public void testShouldConstructObjectFromGroupInstance() {
        GroupInstanceDataDefinition groupInstanceDataDefinition = createGroupInstanceDataDefinition();
        GroupInstance groupInstance = new GroupInstance(groupInstanceDataDefinition);
        GroupDefinitionInfo groupDefinitionInfo = new GroupDefinitionInfo(groupInstance);
        assertThat(groupDefinitionInfo.getName(), is(groupInstanceDataDefinition.getGroupName()));
        assertThat(groupDefinitionInfo.getDescription(), is(groupInstanceDataDefinition.getDescription()));
        assertThat(groupDefinitionInfo.getGroupInstanceUniqueId(), is(groupInstanceDataDefinition.getUniqueId()));
        assertThat(groupDefinitionInfo.getVersion(), is(groupInstanceDataDefinition.getVersion()));
        assertThat(groupDefinitionInfo.getInvariantUUID(), is(groupInstanceDataDefinition.getInvariantUUID()));
        assertThat(groupDefinitionInfo.getGroupUUID(), is(groupInstanceDataDefinition.getGroupUUID()));
    }

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(GroupDefinitionInfo.class, BeanMatchers.hasValidGettersAndSettersExcluding("artifacts", "properties"));
    }

    @Test
    public void testToString() {
        GroupDefinitionInfo groupDefinitionInfo = new GroupDefinitionInfo();
        List<ArtifactDefinitionInfo> artifacts = new ArrayList<>();
        ArtifactDefinitionInfo artifactDefinitionInfo = new ArtifactDefinitionInfo();
        artifactDefinitionInfo.setArtifactName(ARTIFACT_NAME);
        artifacts.add(artifactDefinitionInfo);
        groupDefinitionInfo.setArtifacts(artifacts);
        groupDefinitionInfo.setIsBase(true);
        assertThat(groupDefinitionInfo.toString(), matchesPattern(TO_STRING_REGEXP));
    }

    private GroupDefinition createGroupDefinition() {
        GroupDataDefinition groupDataDefinition = new GroupDataDefinition();
        groupDataDefinition.setName(NAME);
        groupDataDefinition.setDescription(DESC);
        groupDataDefinition.setUniqueId(UNIQUE_ID);
        groupDataDefinition.setVersion(VERSION);
        groupDataDefinition.setInvariantUUID(INVARIANT_UUID);
        groupDataDefinition.setGroupUUID(GROUP_UUID);
        return new GroupDefinition(groupDataDefinition);
    }

    private GroupInstanceDataDefinition createGroupInstanceDataDefinition() {
        GroupInstanceDataDefinition groupInstanceDataDefinition = new GroupInstanceDataDefinition();
        groupInstanceDataDefinition.setGroupName(NAME);
        groupInstanceDataDefinition.setDescription(DESC);
        groupInstanceDataDefinition.setUniqueId(UNIQUE_ID);
        groupInstanceDataDefinition.setVersion(VERSION);
        groupInstanceDataDefinition.setInvariantUUID(INVARIANT_UUID);
        groupInstanceDataDefinition.setGroupUUID(GROUP_UUID);
        return groupInstanceDataDefinition;
    }

}