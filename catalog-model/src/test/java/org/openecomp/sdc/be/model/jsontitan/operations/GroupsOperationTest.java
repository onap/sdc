package org.openecomp.sdc.be.model.jsontitan.operations;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import fj.data.Either;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.config.TitanSpringConfig;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.HealingTitanDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
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
@ContextConfiguration(classes = {TitanSpringConfig.class, ModelOperationsSpringConfig.class})
public class GroupsOperationTest extends ModelTestBase {

    @Autowired
    private GroupsOperation groupsOperation;

    @Autowired
    HealingTitanDao titanDao;

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
        Either<GraphVertex, TitanOperationStatus> createdCmpt = titanDao.createVertex(createBasicContainerGraphVertex());
        assertThat(createdCmpt.isLeft()).isTrue();

    }

    @After
    public void tearDown() throws Exception {
        titanDao.rollback();
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
        groupDefinition.setName("name" + id);
        return groupDefinition;
    }


}