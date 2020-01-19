/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import fj.data.Either;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.config.JanusGraphSpringConfig;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.config.ModelOperationsSpringConfig;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JanusGraphSpringConfig.class, ModelOperationsSpringConfig.class})
public class GroupsOperationTest extends ModelTestBase {

    @Autowired
    private GroupsOperation groupsOperation;

    @Autowired
    HealingJanusGraphDao janusGraphDao;

    @Autowired
    private ToscaOperationFacade toscaOperationFacade;
    private Component container;

    @BeforeClass
    public static void initClass() {
        ModelTestBase.init();
    }

    @Before
    public void setUp() throws Exception {
        container = new Resource();
        container.setUniqueId(CONTAINER_ID);
        Either<GraphVertex, JanusGraphOperationStatus> createdCmpt = janusGraphDao.createVertex(createBasicContainerGraphVertex());
        assertThat(createdCmpt.isLeft()).isTrue();

    }

    @After
    public void tearDown() throws Exception {
        janusGraphDao.rollback();
    }

    @Test
    public void addGroups_whenContainerHasNoGroups_associateContainerWithGroup() {
        GroupDefinition g1 = createGroupDefinition("g1");
        GroupDefinition g2 = createGroupDefinition("g2");
        Either<List<GroupDefinition>, StorageOperationStatus> createGroups = groupsOperation.addGroups(container, asList(g1, g2));
        assertThat(createGroups.isLeft()).isTrue();

        ComponentParametersView getGroupsFilter = new ComponentParametersView(true);
        getGroupsFilter.setIgnoreGroups(false);
        Component cmptWithGroups = toscaOperationFacade.getToscaElement(CONTAINER_ID, getGroupsFilter).left().value();
        assertThat(cmptWithGroups.getGroups())
                .usingElementComparatorOnFields("name", "uniqueId")
                .containsExactlyInAnyOrder(g1, g2);
    }

    @Test
    public void addGroups_whenContainerHasGroups_addTheGivenGroupsToTheGroupsList() {
        GroupDefinition g1 = createGroupDefinition("g1");
        GroupDefinition g2 = createGroupDefinition("g2");
        groupsOperation.addGroups(container, asList(g1, g2)).left().value();

        GroupDefinition g3 = createGroupDefinition("g3");
        GroupDefinition g4 = createGroupDefinition("g4");

        groupsOperation.addGroups(container, asList(g3, g4)).left().value();

        ComponentParametersView getGroupsFilter = new ComponentParametersView(true);
        getGroupsFilter.setIgnoreGroups(false);
        Component cmptWithGroups = toscaOperationFacade.getToscaElement(CONTAINER_ID, getGroupsFilter).left().value();
        assertThat(cmptWithGroups.getGroups())
                .usingElementComparatorOnFields("name", "uniqueId")
                .containsExactlyInAnyOrder(g1, g2, g3, g4);

    }

    private GroupDefinition createGroupDefinition(String id) {
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setUniqueId(id);
        groupDefinition.setInvariantName("name" + id);
        return groupDefinition;
    }


}
