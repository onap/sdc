package org.openecomp.sdc.be.components.property;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.tosca.constraints.ConstraintType;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.PatternConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class PropertyConstraintsUtilsTest {

    @Test
    public void mergePropertiesConstraintsDeletionNotPermittedTest(){
        Resource newResource = new Resource();
        Resource oldResource = new Resource();

        PropertyDefinition prop1 = new PropertyDefinition();
        prop1.setName("prop1");
        ValidValuesConstraint vvConst = new ValidValuesConstraint();
        vvConst.setValidValues(Lists.newArrayList("3","1","2"));
        InRangeConstraint inRangeConst = new InRangeConstraint();
        PatternConstraint patternConstraint  = new PatternConstraint();
        prop1.setConstraints(Lists.newArrayList(vvConst, inRangeConst, patternConstraint));


        PropertyDefinition prop1_update = new PropertyDefinition();
        prop1_update.setName("prop1");
        vvConst = new ValidValuesConstraint();
        vvConst.setValidValues(Lists.newArrayList("1","2"));
        inRangeConst = new InRangeConstraint();
        patternConstraint  = new PatternConstraint();
        prop1_update.setConstraints(Lists.newArrayList(vvConst, inRangeConst, patternConstraint));

        List<PropertyDefinition> oldProperties = Lists.newArrayList(prop1);
        List<PropertyDefinition> newProperties = Lists.newArrayList(prop1_update);
        oldResource.setProperties(oldProperties);
        newResource.setProperties(newProperties);
        try {
            PropertyConstraintsUtils.validatePropertiesConstraints(newResource, oldResource);
        } catch (ComponentException e){
            assertThat(e.getActionStatus())
                    .isNotNull()
                    .isEqualTo(ActionStatus.CANNOT_DELETE_VALID_VALUES);

            assertThat(e.getParams())
                    .containsExactlyInAnyOrder(ConstraintType.VALID_VALUES.name(),Lists.newArrayList("3").toString());

        }
    }

    @Test
    public void mergePropertiesConstraintsAdditionPermittedTest(){
        Resource newResource = new Resource();
        Resource oldResource = new Resource();

        PropertyDefinition prop2 = new PropertyDefinition();
        prop2.setName("prop2");
        ValidValuesConstraint vvConst = new ValidValuesConstraint();
        vvConst.setValidValues(Lists.newArrayList("def","abc"));
        InRangeConstraint inRangeConst = new InRangeConstraint();
        PatternConstraint patternConstraint  = new PatternConstraint();
        prop2.setConstraints(Lists.newArrayList(vvConst, inRangeConst, patternConstraint));

        PropertyDefinition prop2_update = new PropertyDefinition();
        prop2_update.setName("prop2");
        vvConst = new ValidValuesConstraint();
        vvConst.setValidValues(Lists.newArrayList("ghi","def","abc"));
        inRangeConst = new InRangeConstraint();
        patternConstraint  = new PatternConstraint();
        prop2_update.setConstraints(Lists.newArrayList(vvConst, inRangeConst, patternConstraint));

        List<PropertyDefinition> oldProperties = Lists.newArrayList(prop2);
        List<PropertyDefinition> newProperties = Lists.newArrayList(prop2_update);
        oldResource.setProperties(oldProperties);
        newResource.setProperties(newProperties);
        PropertyConstraintsUtils.validatePropertiesConstraints(newResource, oldResource);

        Optional<PropertyDefinition> prop_merged = newResource.getProperties().stream().filter(p -> p.getName().equals(prop2.getName())).findFirst();
        assertThat(prop_merged.isPresent()).isTrue();
        assertThat(prop_merged.get().getConstraints()).isNotEmpty();
        assertThat(prop_merged.get().getConstraints().size()).isEqualTo(3);
        Optional<PropertyConstraint> vvConst_merged = prop_merged.get().getConstraints()
                .stream()
                .filter(c -> c.getConstraintType() == ConstraintType.VALID_VALUES)
                .findFirst();
        assertThat(vvConst_merged.isPresent()).isTrue();
        assertThat(((ValidValuesConstraint)vvConst_merged.get()).getValidValues()).containsExactlyInAnyOrder("ghi","def","abc");
    }

    @Test
    public void mergePropertiesConstraintsUpdateNotPermittedTest(){
        Resource newResource = new Resource();
        Resource oldResource = new Resource();

        PropertyDefinition prop3 = new PropertyDefinition();
        prop3.setName("prop3");
        ValidValuesConstraint vvConst = new ValidValuesConstraint();
        vvConst.setValidValues(Lists.newArrayList("a2","a3","a1"));
        InRangeConstraint inRangeConst = new InRangeConstraint();
        PatternConstraint patternConstraint  = new PatternConstraint();
        prop3.setConstraints(Lists.newArrayList(vvConst, inRangeConst, patternConstraint));

        PropertyDefinition prop3_update = new PropertyDefinition();
        prop3_update.setName("prop3");
        vvConst = new ValidValuesConstraint();
        vvConst.setValidValues(Lists.newArrayList("a4","a2","a3"));
        inRangeConst = new InRangeConstraint();
        patternConstraint  = new PatternConstraint();
        prop3_update.setConstraints(Lists.newArrayList(vvConst, inRangeConst, patternConstraint));

        List<PropertyDefinition> oldProperties = Lists.newArrayList(prop3);
        List<PropertyDefinition> newProperties = Lists.newArrayList(prop3_update);
        oldResource.setProperties(oldProperties);
        newResource.setProperties(newProperties);
        try {
            PropertyConstraintsUtils.validatePropertiesConstraints(newResource, oldResource);
        } catch (ComponentException e){
            assertThat(e.getActionStatus())
                    .isNotNull()
                    .isEqualTo(ActionStatus.CANNOT_DELETE_VALID_VALUES);

            assertThat(e.getParams())
                    .containsExactlyInAnyOrder(ConstraintType.VALID_VALUES.name(),Lists.newArrayList("a1").toString());

        }

    }
}
